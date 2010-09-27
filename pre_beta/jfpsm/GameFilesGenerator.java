/*This program is free software; you can redistribute it and/or
  modify it under the terms of the GNU General Public License
  as published by the Free Software Foundation, version 2
  of the License.
  This program is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  GNU General Public License for more details.
  You should have received a copy of the GNU General Public License
  along with this program; if not, write to the Free Software
  Foundation, Inc., 59 Temple Place - Suite 330, Boston,
  MA 02111-1307, USA.
*/
package jfpsm;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.Buffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map.Entry;
import javax.imageio.ImageIO;
import com.sun.opengl.util.BufferUtil;

final class GameFilesGenerator{

    
    private static final GameFilesGenerator instance=new GameFilesGenerator();
    
    
    static final GameFilesGenerator getInstance(){
        return(instance);
    }
    
    private final Entry<ArrayList<AbsoluteVolumeParameters[][]>,RegularGrid> createVolumeParametersAndGridFromLevel(final FloorSet level,final Project project){        
        BufferedImage image;
        int width=0,depth=0,rgb;
        Map map;
        AbsoluteVolumeParameters[][] avp;
        ArrayList<AbsoluteVolumeParameters[][]> volumeElementsList=new ArrayList<AbsoluteVolumeParameters[][]>();
        //get the biggest dimension of the floors
        for(Floor floor:level.getFloorsList())
            {map=floor.getMap(MapType.CONTAINER_MAP);
             image=map.getImage();
             width=Math.max(width,image.getWidth());
             depth=Math.max(depth,image.getHeight());
            }
        //use them to build the regular grid
        final RegularGrid grid=new RegularGrid(width,level.getFloorsList().size(),depth,1,1,1);
        //floor index
        int j=0;
        float[] gridSectionPos;
        //loop on all floors
        for(Floor floor:level.getFloorsList())
            {//we will have to look at each kind of map...
             map=floor.getMap(MapType.CONTAINER_MAP);
             image=map.getImage();
             width=image.getWidth();
             depth=image.getHeight();
             avp=new AbsoluteVolumeParameters[width][depth];
             /**
              * use the colors and the tiles to 
              * compute the geometry, the image 
              * is seen as a grid.
              */
             for(int i=0;i<width;i++)
                 for(int k=0;k<depth;k++)
                     {avp[i][k]=new AbsoluteVolumeParameters();
                      //compute the absolute coordinates of the left bottom back vertex
                      gridSectionPos=grid.getSectionPhysicalPosition(i,j,k);
                      avp[i][k].setLevelRelativePosition(gridSectionPos[0],gridSectionPos[1],gridSectionPos[2]);
                      rgb=image.getRGB(i,k);
                      //use the color of the image to get the matching tile
                      for(Tile tile:project.getTileSet().getTilesList())
                          if(tile.getColor().getRGB()==rgb)
                              {avp[i][k].setVolumeParam(tile.getVolumeParameters());
                               avp[i][k].setName(tile.getName());
                               break;
                              }
                     }
             volumeElementsList.add(avp);
             j++;
            }
        return(new AbstractMap.SimpleEntry<ArrayList<AbsoluteVolumeParameters[][]>,RegularGrid>(volumeElementsList,grid));
    }
    
    /**
     * write the level mesh into a file and the bounding boxes used for the collisions in another one
     * @param level
     * @param levelIndex
     * @param project
     * @param destFile
     * @param destCollisionFile
     * @throws Exception
     */
    final void writeLevel(final FloorSet level,final int levelIndex,final Project project,final File destFile,final File destCollisionFile)throws Exception{            
        boolean success=true;
        //create the file used to store a level if it does not yet exist
        if(!destFile.exists())
            {success=destFile.createNewFile();
             if(!success)
                 throw new RuntimeException("The file "+destFile.getAbsolutePath()+" cannot be created!");
            }
        //create the file used to store the bounding boxes of the level used for the collisions if it does not yet exist
        if(success&&!destCollisionFile.exists())
            {success=destCollisionFile.createNewFile();
             if(!success)
                 throw new RuntimeException("The file "+destCollisionFile.getAbsolutePath()+" cannot be created!");
            }
        if(success)
            {final long time=System.currentTimeMillis();
             System.out.println("[INFO] JFPSM attempts to create a level node...");
             final String levelNodeName="LID"+levelIndex;
             //create one node per level
             final Object levelNode=EngineServiceSeeker.getInstance().createNode(levelNodeName);
             String floorNodeName,meshName,tilePath;
             File tileFile;
             Object volumeElementMesh,floorNode;
             HashMap<Integer,ArrayList<float[]>> volumeParamLocationTable=new HashMap<Integer,ArrayList<float[]>>();
             HashMap<Integer,ArrayList<Buffer>> volumeParamTable=new HashMap<Integer,ArrayList<Buffer>>();
             HashMap<Integer,String> tileNameTable=new HashMap<Integer,String>();
             HashMap<Integer,Entry<Boolean,Boolean>> removalOfIdenticalFacesAndMergeOfAdjacentFacesTable=new HashMap<Integer,Entry<Boolean,Boolean>>();
             HashMap<Integer,int[][][]> verticesIndicesOfPotentiallyIdenticalFacesTable=new HashMap<Integer,int[][][]>();
             HashMap<Integer,Entry<int[][][],int[][]>> verticesIndicesOfAdjacentMergeableFacesAndAdjacencyCoordIndicesTable=new HashMap<Integer,Entry<int[][][],int[][]>>();
             int[][][] verticesIndicesOfPotentiallyIdenticalFaces=null;
             Entry<int[][][],int[][]> verticesIndicesOfAdjacentMergeableFacesAndAdjacencyCoordIndices=null;
             int[][][] absoluteVerticesIndicesOfPotentiallyIdenticalFaces=null;
             //use the identifier of the volume parameter as a key rather than the vertex buffer
             Integer key;
             FloatBuffer vertexBuffer,normalBuffer,texCoordBuffer,totalVertexBuffer,totalNormalBuffer,totalTexCoordBuffer,localVertexBuffer,localNormalBuffer,localTexCoordBuffer;
             IntBuffer totalIndexBuffer,indexBuffer,mergeableIndexBuffer,localIndexBuffer,localMergeableIndexBuffer,localMergeIndexBuffer;
             int totalVertexBufferSize,totalIndexBufferSize,totalNormalBufferSize,totalTexCoordBufferSize;
             ArrayList<float[]> locationList;
             ArrayList<Buffer> bufferList;
             int meshIndex,indexOffset;
             boolean isRemovalOfIdenticalFacesEnabled,isMergeOfAdjacentFacesEnabled;
             float[] vertexCoords=new float[3],normals=new float[3],texCoords=new float[3];
             int[] indices=new int[3];
             //create the grid and the absolute volume parameters
             Entry<ArrayList<AbsoluteVolumeParameters[][]>,RegularGrid> volumParamsAndGrid=createVolumeParametersAndGridFromLevel(level,project);
             ArrayList<AbsoluteVolumeParameters[][]> volumeElementsList=volumParamsAndGrid.getKey();
             RegularGrid grid=volumParamsAndGrid.getValue();
             Buffer[][][][] buffersGrid=new Buffer[grid.getLogicalWidth()][grid.getLogicalHeight()][grid.getLogicalDepth()][6];       
             int[][][] indexOffsetArray=new int[grid.getLogicalWidth()][grid.getLogicalHeight()][grid.getLogicalDepth()];
             int[][][] newIndexOffsetArray=new int[grid.getLogicalWidth()][grid.getLogicalHeight()][grid.getLogicalDepth()];
             int[][][] newOtherIndexOffsetArray=new int[grid.getLogicalWidth()][grid.getLogicalHeight()][grid.getLogicalDepth()];
             ArrayList<int[]> indexArrayOffsetIndicesList=new ArrayList<int[]>();
             ArrayList<Object> boundingBoxesList=new ArrayList<Object>();
             int[] logicalGridPos;
             //start with the first floor
             int j=0;
             //loop on 2D arrays of volume elements of all floors
             for(AbsoluteVolumeParameters[][] floorVolumeElements:volumeElementsList)
                 {volumeParamLocationTable.clear();
                  volumeParamTable.clear();
                  tileNameTable.clear();
                  removalOfIdenticalFacesAndMergeOfAdjacentFacesTable.clear();
                  //loop on volume elements of a 2D array of a floor
                  for(int i=0;i<floorVolumeElements.length;i++)
                      for(int k=0;k<floorVolumeElements[i].length;k++)
                          //check if the volume element is pertinent
                          if(!floorVolumeElements[i][k].isVoid())
                              {key=Integer.valueOf(floorVolumeElements[i][k].getVolumeParamIdentifier());
                               //get the list of locations of this volume element if any
                               locationList=volumeParamLocationTable.get(key);
                               //if there is no such list
                               if(locationList==null)
                                   {//create it
                                    locationList=new ArrayList<float[]>();
                                    volumeParamLocationTable.put(key,locationList);
                                    bufferList=new ArrayList<Buffer>();
                                    //copy all buffers
                                    bufferList.add(floorVolumeElements[i][k].getVertexBuffer());
                                    bufferList.add(floorVolumeElements[i][k].getIndexBuffer());
                                    bufferList.add(floorVolumeElements[i][k].getNormalBuffer());
                                    bufferList.add(floorVolumeElements[i][k].getTexCoordBuffer());
                                    bufferList.add(floorVolumeElements[i][k].getMergeableIndexBuffer());
                                    volumeParamTable.put(key,bufferList);
                                    tileNameTable.put(key,floorVolumeElements[i][k].getName());
                                    removalOfIdenticalFacesAndMergeOfAdjacentFacesTable.put(key,new AbstractMap.SimpleEntry<Boolean,Boolean>(Boolean.valueOf(floorVolumeElements[i][k].isRemovalOfIdenticalFacesEnabled()),Boolean.valueOf(floorVolumeElements[i][k].isMergeOfAdjacentFacesEnabled())));
                                    verticesIndicesOfAdjacentMergeableFacesAndAdjacencyCoordIndices=floorVolumeElements[i][k].getVerticesIndicesOfAdjacentMergeableFacesAndAdjacencyCoordIndices(grid);
                                    if(verticesIndicesOfAdjacentMergeableFacesAndAdjacencyCoordIndices!=null)
                                    	verticesIndicesOfAdjacentMergeableFacesAndAdjacencyCoordIndicesTable.put(key,verticesIndicesOfAdjacentMergeableFacesAndAdjacencyCoordIndices);                                  	
                                    //faces that are potentially identical are used to compute the bounding boxes of the system of collision detection too 
                                    if(floorVolumeElements[i][k].getVerticesIndicesOfPotentiallyIdenticalFaces()!=null)
                                        verticesIndicesOfPotentiallyIdenticalFacesTable.put(key,floorVolumeElements[i][k].getVerticesIndicesOfPotentiallyIdenticalFaces());
                                   }
                               locationList.add(floorVolumeElements[i][k].getLevelRelativePosition());
                              }
                  //create one node per floor (array)
                  floorNodeName=levelNodeName+"NID"+j;
                  floorNode=EngineServiceSeeker.getInstance().createNode(floorNodeName);                
                  meshIndex=0;
                  //use the geometries passed as arguments to build the mesh data
                  for(Entry<Integer,ArrayList<float[]>> entry:volumeParamLocationTable.entrySet())
                      {key=entry.getKey();
                       locationList=entry.getValue();
                       bufferList=volumeParamTable.get(key);
                       vertexBuffer=(FloatBuffer)bufferList.get(0);
                       indexBuffer=(IntBuffer)bufferList.get(1);
                       normalBuffer=(FloatBuffer)bufferList.get(2);
                       texCoordBuffer=(FloatBuffer)bufferList.get(3);
                       mergeableIndexBuffer=(IntBuffer)bufferList.get(4);
                       if(vertexCoords.length<vertexBuffer.capacity())
                           vertexCoords=new float[vertexBuffer.capacity()];
                       if(indices.length<indexBuffer.capacity())
                           indices=new int[indexBuffer.capacity()];
                       if(normals.length<normalBuffer.capacity())
                           normals=new float[normalBuffer.capacity()];
                       if(texCoords.length<texCoordBuffer.capacity())
                           texCoords=new float[texCoordBuffer.capacity()];                      
                       isRemovalOfIdenticalFacesEnabled=removalOfIdenticalFacesAndMergeOfAdjacentFacesTable.get(key).getKey().booleanValue();
                       isMergeOfAdjacentFacesEnabled=removalOfIdenticalFacesAndMergeOfAdjacentFacesTable.get(key).getValue().booleanValue();
                       verticesIndicesOfPotentiallyIdenticalFaces=verticesIndicesOfPotentiallyIdenticalFacesTable.get(key);
                       verticesIndicesOfAdjacentMergeableFacesAndAdjacencyCoordIndices=verticesIndicesOfAdjacentMergeableFacesAndAdjacencyCoordIndicesTable.get(key);
                       if(verticesIndicesOfPotentiallyIdenticalFaces!=null)
                           {//copy the vertices indices
                            absoluteVerticesIndicesOfPotentiallyIdenticalFaces=new int[verticesIndicesOfPotentiallyIdenticalFaces.length][2][3];
                            for(int ii=0;ii<verticesIndicesOfPotentiallyIdenticalFaces.length;ii++)
                                for(int jj=0;jj<2;jj++)
                                    for(int kk=0;kk<3;kk++)
                                        absoluteVerticesIndicesOfPotentiallyIdenticalFaces[ii][jj][kk]=verticesIndicesOfPotentiallyIdenticalFaces[ii][jj][kk];
                           }
                       indexOffset=0;
                       for(float[] location:locationList)
                           {vertexBuffer.get(vertexCoords,0,vertexBuffer.capacity());
                            vertexBuffer.rewind();
                            //use the location to translate the vertices
                            for(int i=0;i<vertexBuffer.capacity();i++)
                                vertexCoords[i]+=location[i%location.length];
                            indexBuffer.get(indices,0,indexBuffer.capacity());
                            indexBuffer.rewind();                           
                            logicalGridPos=grid.getSectionLogicalPosition(location[0],location[1],location[2]);
                            //add an offset to the indices
                            for(int i=0;i<indexBuffer.capacity();i++)
                                indices[i]+=indexOffset;                           
                            normalBuffer.get(normals,0,normalBuffer.capacity());
                            normalBuffer.rewind();
                            texCoordBuffer.get(texCoords,0,texCoordBuffer.capacity());
                            texCoordBuffer.rewind();                           
                            localVertexBuffer=FloatBuffer.allocate(vertexBuffer.capacity());
                            localVertexBuffer.put(vertexCoords,0,vertexBuffer.capacity());
                            localVertexBuffer.rewind();
                            buffersGrid[logicalGridPos[0]][logicalGridPos[1]][logicalGridPos[2]][0]=localVertexBuffer;
                            localIndexBuffer=IntBuffer.allocate(indexBuffer.capacity());
                            localIndexBuffer.put(indices,0,indexBuffer.capacity());
                            localIndexBuffer.rewind();
                            buffersGrid[logicalGridPos[0]][logicalGridPos[1]][logicalGridPos[2]][1]=localIndexBuffer;
                            localNormalBuffer=FloatBuffer.allocate(normalBuffer.capacity());
                            localNormalBuffer.put(normals,0,normalBuffer.capacity());
                            localNormalBuffer.rewind();
                            buffersGrid[logicalGridPos[0]][logicalGridPos[1]][logicalGridPos[2]][2]=localNormalBuffer;
                            localTexCoordBuffer=FloatBuffer.allocate(texCoordBuffer.capacity());
                            localTexCoordBuffer.put(texCoords,0,texCoordBuffer.capacity());
                            localTexCoordBuffer.rewind();
                            buffersGrid[logicalGridPos[0]][logicalGridPos[1]][logicalGridPos[2]][3]=localTexCoordBuffer;
                            if(verticesIndicesOfPotentiallyIdenticalFaces!=null)
                                {mergeableIndexBuffer.get(indices,0,mergeableIndexBuffer.capacity());
                                 mergeableIndexBuffer.rewind();
                                 //add an offset to the indices
                                 for(int i=0;i<mergeableIndexBuffer.capacity();i++)
                                     indices[i]+=indexOffset;
                                 localMergeableIndexBuffer=IntBuffer.allocate(mergeableIndexBuffer.capacity());
                                 localMergeableIndexBuffer.put(indices,0,mergeableIndexBuffer.capacity());
                                 localMergeableIndexBuffer.rewind();
                                 //fill the buffer used for the merge, it will contain rearranged indices (easier to compare)
                                 buffersGrid[logicalGridPos[0]][logicalGridPos[1]][logicalGridPos[2]][4]=localMergeableIndexBuffer;
                                 localMergeIndexBuffer=IntBuffer.allocate(mergeableIndexBuffer.capacity());
                                 localMergeIndexBuffer.put(indices,0,mergeableIndexBuffer.capacity());
                                 localMergeIndexBuffer.rewind();
                                 //fill the buffer used for the merge, it will contain the markers (-1 for vertices that have to be removed)
                                 buffersGrid[logicalGridPos[0]][logicalGridPos[1]][logicalGridPos[2]][5]=localMergeIndexBuffer;
                                 indexArrayOffsetIndicesList.add(logicalGridPos);
                                }
                            else
                                buffersGrid[logicalGridPos[0]][logicalGridPos[1]][logicalGridPos[2]][4]=null;
                            //store the index offset that can be used for the both kind of merge
                            indexOffsetArray[logicalGridPos[0]][logicalGridPos[1]][logicalGridPos[2]]=indexOffset;
                            //update the offset
                            indexOffset+=vertexBuffer.capacity()/3;
                           }                      
                       //if the merge is possible, compute the merged structures that can be used both for the merge itself and
                       //for the computation of the bounding boxes of the system handling the detection of collisions
                       if(verticesIndicesOfPotentiallyIdenticalFaces!=null)
                           {computeStructuresWithoutIdenticalFaces(verticesIndicesOfPotentiallyIdenticalFaces,
                                absoluteVerticesIndicesOfPotentiallyIdenticalFaces,                            
                                isRemovalOfIdenticalFacesEnabled,grid,buffersGrid,indexOffsetArray,newIndexOffsetArray,
                                indexArrayOffsetIndicesList,j);
                           }
                       //if the merge between adjacent faces is possible, just do it
                       //TODO: uncomment this when it is ready
                       if(verticesIndicesOfAdjacentMergeableFacesAndAdjacencyCoordIndices!=null)
                           {/*computeMergedStructuresWithAdjacentFaces(verticesIndicesOfAdjacentMergeableFacesAndAdjacencyCoordIndices,
                        		isMergeOfAdjacentFacesEnabled,grid,buffersGrid,newIndexOffsetArray,newOtherIndexOffsetArray,indexArrayOffsetIndicesList,j);*/
                           }
                       //regroup all buffers of a single floor using the same volume parameter
                       totalVertexBufferSize=0;
                       totalIndexBufferSize=0;
                       totalNormalBufferSize=0;
                       totalTexCoordBufferSize=0;
                       //compute the size of the buffers resulting of the grouping
                       for(int i=0;i<grid.getLogicalWidth();i++)
                           for(int k=0;k<grid.getLogicalDepth();k++)
                               {if(buffersGrid[i][j][k][0]!=null)
                                    totalVertexBufferSize+=buffersGrid[i][j][k][0].capacity();
                                if(buffersGrid[i][j][k][1]!=null)
                                    totalIndexBufferSize+=buffersGrid[i][j][k][1].capacity();
                                if(buffersGrid[i][j][k][2]!=null)
                                    totalNormalBufferSize+=buffersGrid[i][j][k][2].capacity();
                                if(buffersGrid[i][j][k][3]!=null)
                                    totalTexCoordBufferSize+=buffersGrid[i][j][k][3].capacity();
                               }
                       //create these buffers
                       totalVertexBuffer=BufferUtil.newFloatBuffer(totalVertexBufferSize);
                       totalIndexBuffer=BufferUtil.newIntBuffer(totalIndexBufferSize);
                       totalNormalBuffer=BufferUtil.newFloatBuffer(totalNormalBufferSize);
                       totalTexCoordBuffer=BufferUtil.newFloatBuffer(totalTexCoordBufferSize);
                       //fill them
                       for(int i=0;i<grid.getLogicalWidth();i++)
                           for(int k=0;k<grid.getLogicalDepth();k++)
                               {if(buffersGrid[i][j][k][0]!=null)
                                    totalVertexBuffer.put((FloatBuffer)buffersGrid[i][j][k][0]);
                                if(buffersGrid[i][j][k][1]!=null)
                                    totalIndexBuffer.put((IntBuffer)buffersGrid[i][j][k][1]);
                                if(buffersGrid[i][j][k][2]!=null)
                                    totalNormalBuffer.put((FloatBuffer)buffersGrid[i][j][k][2]);
                                if(buffersGrid[i][j][k][3]!=null)
                                    totalTexCoordBuffer.put((FloatBuffer)buffersGrid[i][j][k][3]);
                               }                      
                       //put null values into the grid of buffers
                       //because it has to be emptied before using another volume parameter
                       for(int i=0;i<grid.getLogicalWidth();i++)
                           for(int k=0;k<grid.getLogicalDepth();k++)
                               for(int bi=0;bi<5;bi++)
                                   buffersGrid[i][j][k][bi]=null;
                       //it has to be emptied too before using another volume parameter
                       indexArrayOffsetIndicesList.clear();
                       totalVertexBuffer.rewind();
                       totalIndexBuffer.rewind();
                       totalNormalBuffer.rewind();
                       totalTexCoordBuffer.rewind();
                       /**
                        * FIXME: loop on each sub-volume parameter
                        * A sub-volume parameter is a piece of volume parameter that represents
                        * the data of a single face.
                        * 
                        * It should use only one set of buffers per kind of face (use all textures)
                        * */
                       //create a mesh for this floor and for the volume parameter currently in use
                       meshName=floorNodeName+"CID"+meshIndex;
                       volumeElementMesh=EngineServiceSeeker.getInstance().createMeshFromBuffers(meshName,
                               totalVertexBuffer,totalIndexBuffer,totalNormalBuffer,totalTexCoordBuffer);
                       tilePath=destFile.getParent()+System.getProperty("file.separator")+tileNameTable.get(key)+".png";
                       //create a file containing the texture of the tile because it 
                       //is necessary to attach a texture to a mesh (the URL cannot 
                       //point to a file that does not exist)
                       tileFile=new File(tilePath);
                       if(!tileFile.exists())
                           if(!tileFile.createNewFile())
                               throw new IOException("The file "+tilePath+" cannot be created!");
                       for(Tile tile:project.getTileSet().getTilesList())
                           if(tile.getName().equals(tileNameTable.get(key))&&tile.getTexture()!=null)
                               {ImageIO.write(tile.getTexture(),"png",tileFile);
                                break;
                               }
                       EngineServiceSeeker.getInstance().attachTextureToSpatial(volumeElementMesh,tileFile.toURI().toURL());
                       //this file is now useless, delete it
                       tileFile.delete();
                       //attach the newly created mesh to its floor
                       EngineServiceSeeker.getInstance().attachChildToNode(floorNode,volumeElementMesh);
                       meshIndex++;
                      }
                  //attach the floor to its level
                  EngineServiceSeeker.getInstance().attachChildToNode(levelNode,floorNode);
                  //look at the next floor
                  j++;
                 }
             System.out.println("[INFO] level node successfully created");
             System.out.println("[INFO] JFPSM attempts to write the level into the file "+destFile.getName());
             //write the level into a file
             success=EngineServiceSeeker.getInstance().writeSavableInstanceIntoFile(levelNode,destFile);
             if(success)
                 {System.out.println("[INFO] Export into the file "+destFile.getName()+" successful");
                  //System.out.println("[INFO] Elapsed time: "+(System.currentTimeMillis()-time)/1000.0f+" seconds");
                 }
             else
                 System.out.println("[WARNING]Export into the file "+destFile.getName()+" not successful!");
             System.out.println("[INFO] JFPSM attempts to write the bounding boxes of the level into the file "+destCollisionFile.getName());
             //write the bounding boxes of the level into a file
             success=EngineServiceSeeker.getInstance().writeSavableInstancesListIntoFile(boundingBoxesList,destCollisionFile);
             if(success)
                 {System.out.println("[INFO] Export into the file "+destCollisionFile.getName()+" successful");
                  System.out.println("[INFO] Elapsed time: "+(System.currentTimeMillis()-time)/1000.0f+" seconds");
                 }
             else
                 System.out.println("[WARNING]Export into the file "+destCollisionFile.getName()+" not successful!");
            }
    }
    
    /**
     * merge adjacent faces
     * @param verticesIndicesOfAdjacentMergeableFacesAndAdjacencyCoordIndices vertices indices of adjacent faces that can be merged and adjacency coordinate indices indicating the direction used to merge them
     * @param isMergeOfAdjacentFacesEnabled flag indicating whether the merge of adjacent faces is enabled
     * @param grid regular grid of the level
     * @param buffersGrid grid containing the buffers
     * @param indexOffsetArray array containing the index offsets that allow to go from local indices to global indices
     * @param newIndexOffsetArray
     * @param indexArrayOffsetIndicesList
     * @param j floor index
     */
    @SuppressWarnings("unused")
    private static final void computeMergedStructuresWithAdjacentFaces(final Entry<int[][][],int[][]> verticesIndicesOfAdjacentMergeableFacesAndAdjacencyCoordIndices,
    		final boolean isMergeOfAdjacentFacesEnabled,final RegularGrid grid,final Buffer[][][][] buffersGrid,int[][][] indexOffsetArray,
    		int[][][] newIndexOffsetArray,final ArrayList<int[]> indexArrayOffsetIndicesList,final int j){
    	int[][][] verticesIndicesOfAdjacentMergeableFaces=verticesIndicesOfAdjacentMergeableFacesAndAdjacencyCoordIndices.getKey();
    	int[][] adjacencyCoordIndices=verticesIndicesOfAdjacentMergeableFacesAndAdjacencyCoordIndices.getValue();
    	int[][] verticesIndicesOfAdjacentMergeableFace;
    	int[] verticesIndicesOfAdjacentMergeableFaceArray,indexBufferPart=null;
    	int indexOffset,secondaryIndexOffset,adjacencyCoordIndex,indexIndexOffset,secondaryIndexIndexOffset,mergedGridSectionsCount;
    	int[] indices=null,secondaryIndices=null;
    	int[][] verticesIndicesOfAdjacentMergeableFacesArray=new int[verticesIndicesOfAdjacentMergeableFaces.length][];
    	int indicesPerFaceCount;
    	//align each line of verticesIndicesOfAdjacentMergeableFaces on a 1D array
    	for(int faceIndex=0;faceIndex<verticesIndicesOfAdjacentMergeableFaces.length;faceIndex++)
    		if(verticesIndicesOfAdjacentMergeableFaces[faceIndex]!=null)
    	        {//compute the count of indices per face
    			 indicesPerFaceCount=0;
    		     for(int triangleIndex=0;triangleIndex<verticesIndicesOfAdjacentMergeableFaces[faceIndex].length;triangleIndex++)
    		    	 if(verticesIndicesOfAdjacentMergeableFaces[faceIndex][triangleIndex]!=null)
    			         indicesPerFaceCount+=verticesIndicesOfAdjacentMergeableFaces[faceIndex][triangleIndex].length;
    		     //allocate a 1D array per face
    		     verticesIndicesOfAdjacentMergeableFacesArray[faceIndex]=new int[indicesPerFaceCount];
    		     //copy the data
    		     for(int triangleIndex=0,indexIndex1D=0;triangleIndex<verticesIndicesOfAdjacentMergeableFaces[faceIndex].length;triangleIndex++)
    		    	 if(verticesIndicesOfAdjacentMergeableFaces[faceIndex][triangleIndex]!=null)
    		    	     {for(int indexIndex=0;indexIndex<verticesIndicesOfAdjacentMergeableFaces[faceIndex][triangleIndex].length;indexIndex++)
    		    	          {//FIXME: use System.arraycopy
    		    	    	   verticesIndicesOfAdjacentMergeableFacesArray[faceIndex][indexIndex1D]=verticesIndicesOfAdjacentMergeableFaces[faceIndex][triangleIndex][indexIndex];
    		    	           indexIndex1D++;
    		    	          }
    		    	     }
    	        }
    	//copy the index buffers to ease the later removal of the useless geometry
    	for(int i=0;i<grid.getLogicalWidth();i++)
            for(int k=0;k<grid.getLogicalDepth();k++)
            	if(buffersGrid[i][j][k][1]!=null)
            	    {//(re)allocate the copy if needed
            		 if(buffersGrid[i][j][k][5]==null||buffersGrid[i][j][k][5].capacity()!=buffersGrid[i][j][k][1].capacity())
            			 buffersGrid[i][j][k][5]=IntBuffer.allocate(buffersGrid[i][j][k][1].capacity());
            		 //perform the copy
            		 for(int indexIndex=0;indexIndex<buffersGrid[i][j][k][1].capacity();indexIndex++)
            			 ((IntBuffer)buffersGrid[i][j][k][5]).put(indexIndex,((IntBuffer)buffersGrid[i][j][k][1]).get(indexIndex));
            	    }
    	//merge the adjacent faces and detect the useless geometry
    	for(int i=0;i<grid.getLogicalWidth();i++)
            for(int k=0;k<grid.getLogicalDepth();k++)
            	//check if the index buffer of this section is not null or empty
            	if(buffersGrid[i][j][k][1]!=null&&buffersGrid[i][j][k][1].capacity()>0)
                    {//get the index offset that allows to go from the local index to the global index
            		 indexOffset=indexOffsetArray[i][j][k]; 
                     if(indices==null||indices.length!=buffersGrid[i][j][k][1].capacity())
            	    	 indices=new int[buffersGrid[i][j][k][1].capacity()];
            	     //copy the index buffer (with the offset)
            	     ((IntBuffer)buffersGrid[i][j][k][1]).get(indices,0,indices.length);
            	     //compute the indices without the offset
            	     for(int indexIndex=0;indexIndex<indices.length;indexIndex++)
            	    	 //preserve invalid values
            	    	 if(indices[indexIndex]!=-1)
                             indices[indexIndex]-=indexOffset;
            	     for(int faceIndex=0;faceIndex<verticesIndicesOfAdjacentMergeableFaces.length;faceIndex++)
            	    	 {verticesIndicesOfAdjacentMergeableFace=verticesIndicesOfAdjacentMergeableFaces[faceIndex];
            	    	  if(verticesIndicesOfAdjacentMergeableFace!=null&&adjacencyCoordIndices[faceIndex]!=null)
            	    		  {//get the 1D array that matches with this face
            	    		   verticesIndicesOfAdjacentMergeableFaceArray=verticesIndicesOfAdjacentMergeableFacesArray[faceIndex];
            	    		   //store an invalid value to check further if a valid one has been found
            	    		   indexIndexOffset=-1;
            	    		   //allocate the index buffer part if needed
            	    		   if(indexBufferPart==null||indexBufferPart.length!=verticesIndicesOfAdjacentMergeableFaceArray.length)
            	    			   indexBufferPart=new int[verticesIndicesOfAdjacentMergeableFaceArray.length];
            	    		   //compare the 1D array with indices to find the index index offset
            	    		   if(indexBufferPart.length>0)
            	    		       for(int indexIndex=0;indexIndex<indices.length-indexBufferPart.length+1&&indexIndexOffset==-1;indexIndex++)
            	    		           {//fill the index buffer part
            	    		    	    System.arraycopy(indices,indexIndex,indexBufferPart,0,indexBufferPart.length);
            	    		    	    //compare the index buffer part with the vertices indices of this face
            	    		    	    if(Arrays.equals(indexBufferPart,verticesIndicesOfAdjacentMergeableFaceArray))
            	    		    	    	indexIndexOffset=indexIndex;
            	    		           }
            	    		   //if a valid index index offset has been found
            	    		   if(indexIndexOffset!=-1)
            	    		       for(int adjacencyCoordIndexIndex=0;adjacencyCoordIndexIndex<adjacencyCoordIndices[faceIndex].length;adjacencyCoordIndexIndex++)            	    		  
            	    	               {adjacencyCoordIndex=adjacencyCoordIndices[faceIndex][adjacencyCoordIndexIndex];           	    		        
            	    	                switch(adjacencyCoordIndex)
            	    	                    {case 0:
            	    	                         {mergedGridSectionsCount=1;
            	    	                          secondaryIndexIndexOffset=Integer.MAX_VALUE;
            	    	                          for(int ii=i+1;ii<grid.getLogicalWidth()&&buffersGrid[ii][j][k][1]!=null&&buffersGrid[ii][j][k][1].capacity()>0&&secondaryIndexIndexOffset>=0;ii++)
            	    	                              {//get the index offset that allows to go from the local index to the global index
            	    	                        	   secondaryIndexOffset=indexOffsetArray[ii][j][k];
            	    	                        	   if(secondaryIndices==null||secondaryIndices.length!=buffersGrid[ii][j][k][1].capacity())
            	    	                        		   secondaryIndices=new int[buffersGrid[ii][j][k][1].capacity()];
            	    	                        	   //copy the index buffer (with the offset)
            	    	                      	       ((IntBuffer)buffersGrid[ii][j][k][1]).get(secondaryIndices,0,secondaryIndices.length);
            	    	                      	       //compute the indices without the offset
            	    	                      	       for(int indexIndex=0;indexIndex<secondaryIndices.length;indexIndex++)
            	    	                      	    	   //preserve invalid values
            	    	                      	    	   if(secondaryIndices[indexIndex]!=-1)
            	    	                      	    		   secondaryIndices[indexIndex]-=secondaryIndexOffset;
            	    	                      	       //store an invalid value to check further if a valid one has been found
            	    	                      	       secondaryIndexIndexOffset=-1;
            	    	                      	       //look for verticesIndicesOfAdjacentMergeableFaceArray in these indices
            	    	                      	       for(int indexIndex=0;indexIndex<secondaryIndices.length-indexBufferPart.length+1&&secondaryIndexIndexOffset==-1;indexIndex++)
            	    	                      	           {//fill the index buffer part
            	               	    		    	        System.arraycopy(secondaryIndices,indexIndex,indexBufferPart,0,indexBufferPart.length);
            	               	    		    	        //compare the index buffer part with the vertices indices of this face
            	                	    		    	    if(Arrays.equals(indexBufferPart,verticesIndicesOfAdjacentMergeableFaceArray))
            	                	    		    	    	secondaryIndexIndexOffset=indexIndex;
            	    	                      	           }
            	    	                      	       if(secondaryIndexIndexOffset!=-1)
            	    	                      	           {//mark useless indices with -1
            	    	                      	    	    for(int indexIndex=0;indexIndex<verticesIndicesOfAdjacentMergeableFaceArray.length;indexIndex++)
            	    	                      	    	        //update rather the copy of the index buffer
             	    	                        		        ((IntBuffer)buffersGrid[ii][j][k][5]).put(secondaryIndexIndexOffset+indexIndex,-1);
            	    	                      	    	    mergedGridSectionsCount++;
            	    	                      	           }
            	    	                              }
            	    	                          if(mergedGridSectionsCount>1)
            	    	                              {//translate all vertices of the concerned face whose abscissa is equal to the rightmost abscissa of the section
            	    	                        	   for(int indexBufferPartIndex=0;indexBufferPartIndex<verticesIndicesOfAdjacentMergeableFaceArray.length;indexBufferPartIndex++)
            	    	                        		   if(((FloatBuffer)buffersGrid[i][j][k][0]).get(verticesIndicesOfAdjacentMergeableFaceArray[indexBufferPartIndex]*3)==((i+1)*grid.getSectionPhysicalWidth()))
            	    	                        		       {((FloatBuffer)buffersGrid[i][j][k][0]).put(verticesIndicesOfAdjacentMergeableFaceArray[indexBufferPartIndex]*3,((FloatBuffer)buffersGrid[i][j][k][0]).get(verticesIndicesOfAdjacentMergeableFaceArray[indexBufferPartIndex]*3)+((mergedGridSectionsCount-1)*grid.getSectionPhysicalWidth()));
            	    	                        			    //update the texture coordinates (U only) of those vertices
            	    	                        			    ((FloatBuffer)buffersGrid[i][j][k][3]).put(verticesIndicesOfAdjacentMergeableFaceArray[indexBufferPartIndex]*2,((FloatBuffer)buffersGrid[i][j][k][3]).get(verticesIndicesOfAdjacentMergeableFaceArray[indexBufferPartIndex]*2)+(mergedGridSectionsCount-1));
            	    	                        		       }
            	    	                              }
            	    	                          break;
            	    	                         }
            	    	                     case 1:
            	    	                         {//ignore silently the adjacency on Y because merging tiles of different levels is not allowed
            	    	                	      break;
            	    	                         }
            	    	                     case 2:
            	    	                         {//TODO: handle the case of the applicate
            	    	                          break;
            	    	                         }
            	    	                     default:
            	    	                         {//absurd coordinate index
            	    	                	      throw new IllegalArgumentException("The coordinate index "+adjacencyCoordIndex+" is not a valid coordinate index, only 0, 1 and 2 are accepted");
            	    	                         }
            	    	                    }           	    	            
            	    	               }
            	    		   else
            	    			   System.out.println("[WARN] no valid index index offset found");
            	    		  }
            	    	 }
                    }
    	//N.B: we consider each index is useless only in a single face
    	//remove all useless geometry
    	/*int newBufferSize;
    	for(int i=0;i<grid.getLogicalWidth();i++)
            for(int k=0;k<grid.getLogicalDepth();k++)
            	if(buffersGrid[i][j][k][1]!=null&&buffersGrid[i][j][k][1].capacity()>0)
            	    {indexOffset=indexOffsetArray[i][j][k];
            	     //compute the new size (look for useless indices in the copy)
            	     newBufferSize=0;
            	     ((IntBuffer)buffersGrid[i][j][k][5]).rewind();
                     while(((IntBuffer)buffersGrid[i][j][k][5]).hasRemaining())
                         if(((IntBuffer)buffersGrid[i][j][k][5]).get()!=-1)
                             newBufferSize++;     
                     ((IntBuffer)buffersGrid[i][j][k][5]).rewind();
            		 //deduce removed indices from the original index buffer
            		 //remove useless vertices
            		 //remove useless normals
            		 //remove useless texture coordinates
            		 //remove useless indices (marked with -1)
            	    }*/
    	rebuildBuffersWithoutUselessData(isMergeOfAdjacentFacesEnabled,grid,buffersGrid,
				indexOffsetArray,newIndexOffsetArray,
				indexArrayOffsetIndicesList,j,1);
    }

    /**
     * compute the structures without identical faces for a single type of volume parameter in a floor
     * 
     * @param verticesIndicesOfPotentiallyIdenticalFaces
     * @param absoluteVerticesIndicesOfPotentiallyIdenticalFaces
     * @param isRemovalOfIdenticalFacesEnabled flag indicating whether the removal of layered identical faces is enabled
     * @param grid regular grid of the level
     * @param buffersGrid
     * @param indexOffsetArray
     * @param newIndexOffsetArray
     * @param indexArrayOffsetIndicesList
     * @param j floor index
     */
    private static final void computeStructuresWithoutIdenticalFaces(final int[][][] verticesIndicesOfPotentiallyIdenticalFaces,
            final int[][][] absoluteVerticesIndicesOfPotentiallyIdenticalFaces,
            final boolean isRemovalOfIdenticalFacesEnabled,final RegularGrid grid,
            final Buffer[][][][] buffersGrid,
            final int[][][] indexOffsetArray,final int[][][] newIndexOffsetArray,
            final ArrayList<int[]> indexArrayOffsetIndicesList,
            final int j) {
        boolean mergeableFacesFound;
        boolean mergeableFaceFound;
        int indexOffset;
        int startPos;
        int localStartPos;
        //indices of the vertices composing the triangle
        final int[] triIndices=new int[3];
        float[] vertex=new float[3],localVertex=new float[3];
        int[] localAbsoluteVerticesIndicesOfMergeableFace=new int[3];
        //mark all useless indices to ease their later removal
        for(int i=0;i<grid.getLogicalWidth();i++)
            for(int k=0;k<grid.getLogicalDepth();k++)
                if(buffersGrid[i][j][k][4]!=null)
                    {indexOffset=indexOffsetArray[i][j][k];
                     //add the offset
                     for(int ii=0;ii<absoluteVerticesIndicesOfPotentiallyIdenticalFaces.length;ii++)
                         for(int jj=0;jj<2;jj++)
                             for(int kk=0;kk<3;kk++)
                            	 absoluteVerticesIndicesOfPotentiallyIdenticalFaces[ii][jj][kk]+=indexOffset;
                     while(buffersGrid[i][j][k][4].hasRemaining())
                         {startPos=((IntBuffer)buffersGrid[i][j][k][4]).position();
                          //get the indices of the vertices composing the current triangle
                          ((IntBuffer)buffersGrid[i][j][k][4]).get(triIndices,0,3);      
                          mergeableFaceFound=false;
                          //loop on all merge candidates
                          for(int ii=0;!mergeableFaceFound&&ii<absoluteVerticesIndicesOfPotentiallyIdenticalFaces.length;ii++)
                              for(int jj=0;!mergeableFaceFound&&jj<2;jj++)
                                  //if the indices are identical
                                  if(mergeableFaceFound=Arrays.equals(triIndices,absoluteVerticesIndicesOfPotentiallyIdenticalFaces[ii][jj]))
                                      {for(int jjj=0;jjj<2;jjj++)
                                           if(jjj!=jj)
                                               {//check if adjoining faces can be merged with it
                                                for(int locali=i;locali<i+2&&locali<grid.getLogicalWidth();locali++)
                                                    for(int localj=j;localj<j+2&&localj<grid.getLogicalHeight();localj++)
                                                        for(int localk=k;localk<k+2&&localk<grid.getLogicalDepth();localk++)
                                                            //if this section is different of the current section
                                                            if((locali!=i||localj!=j||localk!=k)&&buffersGrid[locali][localj][localk][4]!=null)
                                                                {//we assume this face is a good candidate                                                                                   
                                                                 mergeableFacesFound=true;
                                                                 //for each vertex of the triangle
                                                                 for(int index=0;index<3&&mergeableFacesFound;index++)
                                                                     {//convert into absolute indexing
                                                                      localAbsoluteVerticesIndicesOfMergeableFace[index]=verticesIndicesOfPotentiallyIdenticalFaces[ii][jjj][index]+indexOffsetArray[locali][localj][localk];
                                                                      //for each coordinate
                                                                      for(int subIndex=0;subIndex<3;subIndex++)
                                                                          {//use internal indexing
                                                                           localVertex[subIndex]=((FloatBuffer)buffersGrid[locali][localj][localk][0]).get((verticesIndicesOfPotentiallyIdenticalFaces[ii][jjj][index]*3)+subIndex);
                                                                           vertex[subIndex]=((FloatBuffer)buffersGrid[i][j][k][0]).get((verticesIndicesOfPotentiallyIdenticalFaces[ii][jj][index]*3)+subIndex);
                                                                          }
                                                                      if(!Arrays.equals(localVertex,vertex))
                                                                          mergeableFacesFound=false;
                                                                     }
                                                                 //if the vertices compose 2 identical triangles
                                                                 if(mergeableFacesFound)
                                                                     {//replace the indices that have to be deleted by -1
                                                                      for(int delIndex=0;delIndex<3;delIndex++)
                                                                          ((IntBuffer)buffersGrid[i][j][k][5]).put(startPos+delIndex,-1);
                                                                      localStartPos=-1;
                                                                      //look for the indices of the vertices of this face in 
                                                                      //the part of the grid in order to get the position of 
                                                                      //the indice of the first vertex
                                                                      for(int delIndex=0;delIndex<((IntBuffer)buffersGrid[i][j][k][5]).capacity();delIndex+=3)
                                                                          if(((IntBuffer)buffersGrid[locali][localj][localk][5]).get(delIndex)==localAbsoluteVerticesIndicesOfMergeableFace[delIndex%3]&&
                                                                             ((IntBuffer)buffersGrid[locali][localj][localk][5]).get(delIndex+1)==localAbsoluteVerticesIndicesOfMergeableFace[(delIndex+1)%3]&&
                                                                             ((IntBuffer)buffersGrid[locali][localj][localk][5]).get(delIndex+2)==localAbsoluteVerticesIndicesOfMergeableFace[(delIndex+2)%3])
                                                                              {localStartPos=delIndex;
                                                                               break;
                                                                              }
                                                                      //if these indices have been found
                                                                      if(localStartPos!=-1)
                                                                          //replace the indices that have to be deleted by -1
                                                                          for(int delIndex=0;delIndex<3;delIndex++)
                                                                              ((IntBuffer)buffersGrid[locali][localj][localk][5]).put(localStartPos+delIndex,-1);
                                                                      else
                                                                          //something is wrong, the face is orphaned
                                                                          System.out.println("[WARNING] indices not found");
                                                                     }                                                                                                                                                                      
                                                                }
                                               }                           
                                           break;
                                      }                         
                         }
                     buffersGrid[i][j][k][4].rewind();
                     //subtract the offset
                     for(int ii=0;ii<absoluteVerticesIndicesOfPotentiallyIdenticalFaces.length;ii++)
                         for(int jj=0;jj<2;jj++)
                             for(int kk=0;kk<3;kk++)
                            	 absoluteVerticesIndicesOfPotentiallyIdenticalFaces[ii][jj][kk]-=indexOffset;
                    }
        rebuildBuffersWithoutUselessData(isRemovalOfIdenticalFacesEnabled,grid,buffersGrid,
				indexOffsetArray,newIndexOffsetArray,
				indexArrayOffsetIndicesList,j,4);
    }

	private static final void rebuildBuffersWithoutUselessData(
			final boolean isMergeEnabled,final RegularGrid grid,
			final Buffer[][][][] buffersGrid,final int[][][] indexOffsetArray,
			final int[][][] newIndexOffsetArray,
			final ArrayList<int[]> indexArrayOffsetIndicesList,final int j,
			final int bufferIndexToCheck) {
		FloatBuffer newVertexBuffer;
		FloatBuffer newNormalBuffer;
		FloatBuffer newTexCoordBuffer;
		IntBuffer newIndiceBuffer;
		int indexOffset;
		int deletedIndicesCount;
		int[] logicalGridPos;
		int newBufferSize;
		int indice;
		//table of the occurrences of the indices
        HashMap<Integer,Integer> indexCountTable=new HashMap<Integer,Integer>();
        HashMap<Integer,Integer> reindexationTable=new HashMap<Integer,Integer>();
		//rebuild all index buffers by retaining only useful indices and do the same for all other buffers
        for(int i=0;i<grid.getLogicalWidth();i++)
            for(int k=0;k<grid.getLogicalDepth();k++)
                if(buffersGrid[i][j][k][bufferIndexToCheck]!=null)
                    {indexOffset=indexOffsetArray[i][j][k];
                     //compute the new size
                     newBufferSize=0;
                     ((IntBuffer)buffersGrid[i][j][k][5]).rewind();
                     while(((IntBuffer)buffersGrid[i][j][k][5]).hasRemaining())
                         if(((IntBuffer)buffersGrid[i][j][k][5]).get()!=-1)
                             newBufferSize++;     
                     ((IntBuffer)buffersGrid[i][j][k][5]).rewind();
                     //check if the index buffer has to be rebuilt
                     if(newBufferSize!=((IntBuffer)buffersGrid[i][j][k][5]).capacity())
                         {//if the new buffer is empty
                          if(newBufferSize==0)
                              {//replace all buffers by empty buffers
                               for(int bufIndex=0;bufIndex<6;bufIndex++)
                                   if(bufIndex==1||bufIndex==4||bufIndex==5)
                                       buffersGrid[i][j][k][bufIndex]=IntBuffer.allocate(0);
                                   else
                                       buffersGrid[i][j][k][bufIndex]=FloatBuffer.allocate(0);
                              }
                          else
                              {((IntBuffer)buffersGrid[i][j][k][1]).rewind();
                               while(((IntBuffer)buffersGrid[i][j][k][1]).hasRemaining())
                                   indexCountTable.put(Integer.valueOf(((IntBuffer)buffersGrid[i][j][k][1]).get()),Integer.valueOf(0));
                               ((IntBuffer)buffersGrid[i][j][k][1]).rewind();
                               newIndiceBuffer=IntBuffer.allocate(newBufferSize);
                               //make the new index buffer by retaining the valid indices
                               while(((IntBuffer)buffersGrid[i][j][k][5]).hasRemaining())
                                   {//get the index from the buffer containing the markers
                                    indice=((IntBuffer)buffersGrid[i][j][k][5]).get();
                                    if(indice!=-1)
                                        {//get the index from the buffer containing the valid indices
                                         indice=((IntBuffer)buffersGrid[i][j][k][1]).get();
                                         newIndiceBuffer.put(indice);
                                         //increase the counter of occurrence
                                         indexCountTable.put(Integer.valueOf(indice),Integer.valueOf(indexCountTable.get(Integer.valueOf(indice)).intValue()+1));
                                        }
                                    else
                                        {//skip the index that has to be deleted
                                         ((IntBuffer)buffersGrid[i][j][k][1]).get();
                                        }
                                   }
                               newIndiceBuffer.rewind();
                               ((IntBuffer)buffersGrid[i][j][k][1]).rewind();
                               ((IntBuffer)buffersGrid[i][j][k][5]).rewind();
                               newBufferSize=0;
                               //check if some vertices have become useless
                               for(Entry<Integer,Integer> countEntry:indexCountTable.entrySet())
                                   if(countEntry.getValue().intValue()>0)                                           
                                       newBufferSize++;
                               //create a new vertex buffer (3 coordinates per vertex)
                               newVertexBuffer=FloatBuffer.allocate(newBufferSize*3);
                               newNormalBuffer=FloatBuffer.allocate(newBufferSize*3);
                               newTexCoordBuffer=FloatBuffer.allocate(newBufferSize*2);
                               for(int newIndice=indexOffset,oldIndice=indexOffset;newIndice<(newVertexBuffer.capacity()/3)+indexOffset;oldIndice++,newIndice++)                     
                                   {//retain the valid coordinates, skip the others
                                    while(indexCountTable.get(Integer.valueOf(oldIndice)).intValue()==0)
                                        oldIndice++;
                                    //there are 3 coordinates per vertex
                                    newVertexBuffer.put(((FloatBuffer)buffersGrid[i][j][k][0]).get((oldIndice-indexOffset)*3));
                                    newVertexBuffer.put(((FloatBuffer)buffersGrid[i][j][k][0]).get(((oldIndice-indexOffset)*3)+1));
                                    newVertexBuffer.put(((FloatBuffer)buffersGrid[i][j][k][0]).get(((oldIndice-indexOffset)*3)+2));
                                    //there are 3 coordinates per normal
                                    newNormalBuffer.put(((FloatBuffer)buffersGrid[i][j][k][2]).get((oldIndice-indexOffset)*3));
                                    newNormalBuffer.put(((FloatBuffer)buffersGrid[i][j][k][2]).get(((oldIndice-indexOffset)*3)+1));
                                    newNormalBuffer.put(((FloatBuffer)buffersGrid[i][j][k][2]).get(((oldIndice-indexOffset)*3)+2));
                                    //there are 2 texture coordinates
                                    newTexCoordBuffer.put(((FloatBuffer)buffersGrid[i][j][k][3]).get((oldIndice-indexOffset)*2));
                                    newTexCoordBuffer.put(((FloatBuffer)buffersGrid[i][j][k][3]).get(((oldIndice-indexOffset)*2)+1));
                                    //associate the previous index to the next index
                                    reindexationTable.put(Integer.valueOf(oldIndice),Integer.valueOf(newIndice));
                                   }
                               newVertexBuffer.rewind();
                               newNormalBuffer.rewind();
                               newTexCoordBuffer.rewind();
                               //update the index buffer to take into account the new indexing
                               for(int l=0;l<newIndiceBuffer.capacity();l++)
                                   //replace each old index by its corresponding new index
                                   newIndiceBuffer.put(l,reindexationTable.get(Integer.valueOf(newIndiceBuffer.get(l))));
                               //not mandatory as we have just used an absolute access above
                               newIndiceBuffer.rewind();
                               reindexationTable.clear();
                               //reset the counters of occurrence
                               indexCountTable.clear();
                               //if the merged structures have to be used for the merge
                               if(isMergeEnabled)
                                   {//replace the old vertex buffer by the new one
                                    buffersGrid[i][j][k][0]=newVertexBuffer;
                                    //replace the old index buffer by the new index buffer
                                    buffersGrid[i][j][k][1]=newIndiceBuffer;
                                    //do the same for the normal buffer
                                    buffersGrid[i][j][k][2]=newNormalBuffer;
                                    //do the same for the texture coordinates buffer
                                    buffersGrid[i][j][k][3]=newTexCoordBuffer;
                                   }
                              }
                         }                                       
                    }
        //each individual change of index interval impacts all index buffers
        //then, remove all useless index intervals and update all index buffers
        indexOffset=0;
        //compute new index offsets
        for(int indexOffsetIndex=0;indexOffsetIndex<indexArrayOffsetIndicesList.size();indexOffsetIndex++)
            {logicalGridPos=indexArrayOffsetIndicesList.get(indexOffsetIndex);
             newIndexOffsetArray[logicalGridPos[0]][logicalGridPos[1]][logicalGridPos[2]]=indexOffset;
             indexOffset+=buffersGrid[logicalGridPos[0]][logicalGridPos[1]][logicalGridPos[2]][0].capacity()/3;
            }
        //use these new index offsets
        //the lines below do not change anything when the merge is disabled
        for(int indexOffsetIndex=0;indexOffsetIndex<indexArrayOffsetIndicesList.size();indexOffsetIndex++)
            {logicalGridPos=indexArrayOffsetIndicesList.get(indexOffsetIndex);
             indexOffset=newIndexOffsetArray[logicalGridPos[0]][logicalGridPos[1]][logicalGridPos[2]];
             if(indexOffset!=indexOffsetArray[logicalGridPos[0]][logicalGridPos[1]][logicalGridPos[2]])
                 {//use the subtraction of the both values to update all index buffers
                  deletedIndicesCount=indexOffsetArray[logicalGridPos[0]][logicalGridPos[1]][logicalGridPos[2]]-indexOffset;
                  for(int l=0;l<buffersGrid[logicalGridPos[0]][logicalGridPos[1]][logicalGridPos[2]][1].capacity();l++)
                      ((IntBuffer)buffersGrid[logicalGridPos[0]][logicalGridPos[1]][logicalGridPos[2]][1]).put(l,((IntBuffer)buffersGrid[logicalGridPos[0]][logicalGridPos[1]][logicalGridPos[2]][1]).get(l)-deletedIndicesCount);
                 }
            }
	}
}
