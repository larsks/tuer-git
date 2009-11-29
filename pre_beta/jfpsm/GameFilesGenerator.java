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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;
import javax.imageio.ImageIO;
import com.sun.opengl.util.BufferUtil;

final class GameFilesGenerator{

    
    private static final GameFilesGenerator instance=new GameFilesGenerator();
    
    
    static final GameFilesGenerator getInstance(){
        return(instance);
    }
    
    final void writeLevel(final FloorSet level,final int levelIndex,final Project project,final File destFile)throws Exception{
        BufferedImage image;
        int width=0,depth=0,rgb;
        Map map;
        AbsoluteVolumeParameters[][] avp;
        ArrayList<AbsoluteVolumeParameters[][]> volumeElementsList=new ArrayList<AbsoluteVolumeParameters[][]>();
        for(Floor floor:level.getFloorsList())
            {map=floor.getMap(MapType.CONTAINER_MAP);
             image=map.getImage();
             width=Math.max(width,image.getWidth());
             depth=Math.max(depth,image.getHeight());
            }        
        final RegularGrid grid=new RegularGrid(width,level.getFloorsList().size(),depth,1,1,1);
        /**floor index*/
        int j=0;
        float[] gridSectionPos;
        for(Floor floor:level.getFloorsList())
            {map=floor.getMap(MapType.CONTAINER_MAP);
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
        boolean success=true;
        if(!destFile.exists())
            {success=destFile.createNewFile();
             if(!success)
                 throw new RuntimeException("The file "+destFile.getAbsolutePath()+" cannot be created!");
            }
        if(success)
            {final long time=System.currentTimeMillis();
             System.out.println("[INFO] JFPSM attempts to create a level node...");
             final String levelNodeName="LID"+levelIndex;
             // Create one node per level
             final Object levelNode=EngineServiceSeeker.getInstance().createNode(levelNodeName);
             String floorNodeName,meshName,tilePath;
             File tileFile;
             Object volumeElementMesh,floorNode;
             HashMap<Integer,ArrayList<float[]>> volumeParamLocationTable=new HashMap<Integer,ArrayList<float[]>>();
             HashMap<Integer,ArrayList<Buffer>> volumeParamTable=new HashMap<Integer,ArrayList<Buffer>>();
             HashMap<Integer,String> tileNameTable=new HashMap<Integer,String>();
             HashMap<Integer,Boolean> mergeTable=new HashMap<Integer,Boolean>();
             HashMap<Integer,int[][][]> verticesIndicesOfMergeableFacesTable=new HashMap<Integer, int[][][]>();
             int[][][] verticesIndicesOfMergeableFaces=null;
             // Use the identifier of the volume parameter as a key rather than the vertex buffer
             Integer key;
             FloatBuffer vertexBuffer,normalBuffer,texCoordBuffer,totalVertexBuffer,totalNormalBuffer,totalTexCoordBuffer,localVertexBuffer,localNormalBuffer,localTexCoordBuffer;
             IntBuffer totalIndexBuffer,indexBuffer,mergeableIndexBuffer,localIndexBuffer,localMergeableIndexBuffer;
             int totalVertexBufferSize,totalIndexBufferSize,totalNormalBufferSize,totalTexCoordBufferSize;
             ArrayList<float[]> locationList;
             ArrayList<Buffer> bufferList;
             j=0;
             int meshIndex,indexOffset;
             boolean isMergeEnabled;
             float[] vertexCoords=new float[3],normals=new float[3],texCoords=new float[3];
             int[] indices=new int[3];
             Buffer[][][][] buffersGrid=new Buffer[grid.getLogicalWidth()][grid.getLogicalHeight()][grid.getLogicalDepth()][5];       
             int[] logicalGridPos;
             for(AbsoluteVolumeParameters[][] floorVolumeElements:volumeElementsList)
                 {volumeParamLocationTable.clear();
                  volumeParamTable.clear();
                  tileNameTable.clear();
                  mergeTable.clear();
                  for(int i=0;i<floorVolumeElements.length;i++)
                      for(int k=0;k<floorVolumeElements[i].length;k++)
                          if(!floorVolumeElements[i][k].isVoid())
                              {key=Integer.valueOf(floorVolumeElements[i][k].getVolumeParamIdentifier());
                               locationList=volumeParamLocationTable.get(key);
                               if(locationList==null)
                                   {locationList=new ArrayList<float[]>();
                                    volumeParamLocationTable.put(key,locationList);
                                    bufferList=new ArrayList<Buffer>();
                                    bufferList.add(floorVolumeElements[i][k].getVertexBuffer());
                                    bufferList.add(floorVolumeElements[i][k].getIndexBuffer());
                                    bufferList.add(floorVolumeElements[i][k].getNormalBuffer());
                                    bufferList.add(floorVolumeElements[i][k].getTexCoordBuffer());
                                    bufferList.add(floorVolumeElements[i][k].getMergeableIndexBuffer());
                                    volumeParamTable.put(key,bufferList);
                                    tileNameTable.put(key,floorVolumeElements[i][k].getName());
                                    mergeTable.put(key,Boolean.valueOf(floorVolumeElements[i][k].isMergeEnabled()));
                                    if(floorVolumeElements[i][k].isMergeEnabled())
                                        verticesIndicesOfMergeableFacesTable.put(key,floorVolumeElements[i][k].getVerticesIndicesOfMergeableFaces());
                                   }
                               locationList.add(floorVolumeElements[i][k].getLevelRelativePosition());
                              }
                  // Create one node per floor (array)
                  floorNodeName=levelNodeName+"NID"+j;
                  floorNode=EngineServiceSeeker.getInstance().createNode(floorNodeName);                
                  meshIndex=0;
                  // Use the geometries passed as arguments to build the mesh data
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
                       /*totalVertexBuffer=BufferUtil.newFloatBuffer(vertexBuffer.capacity()*locationList.size());
                       totalIndexBuffer=BufferUtil.newIntBuffer(indexBuffer.capacity()*locationList.size());
                       totalNormalBuffer=BufferUtil.newFloatBuffer(normalBuffer.capacity()*locationList.size());
                       totalTexCoordBuffer=BufferUtil.newFloatBuffer(texCoordBuffer.capacity()*locationList.size());*/           
                       indexOffset=0;
                       isMergeEnabled=mergeTable.get(key).booleanValue();
                       if(isMergeEnabled)
                           verticesIndicesOfMergeableFaces=verticesIndicesOfMergeableFacesTable.get(key);
                       for(float[] location:locationList)
                           {vertexBuffer.get(vertexCoords,0,vertexBuffer.capacity());
                            vertexBuffer.rewind();
                            // Use the location to translate the vertices
                            for(int i=0;i<vertexBuffer.capacity();i++)
                                if(i%location.length!=1)
                                    vertexCoords[i]+=location[i%location.length];
                                else
                                    //FIXME: remove "-0.5f"
                                    vertexCoords[i]+=location[i%location.length]-0.5f;
                            indexBuffer.get(indices,0,indexBuffer.capacity());
                            indexBuffer.rewind();
                            // Add an offset to the indices
                            for(int i=0;i<indexBuffer.capacity();i++)
                                indices[i]+=indexOffset;                           
                            normalBuffer.get(normals,0,normalBuffer.capacity());
                            normalBuffer.rewind();
                            texCoordBuffer.get(texCoords,0,texCoordBuffer.capacity());
                            texCoordBuffer.rewind();
                            /*totalVertexBuffer.put(vertexCoords,0,vertexBuffer.capacity());
                            totalIndexBuffer.put(indices,0,indexBuffer.capacity());
                            totalNormalBuffer.put(normals,0,normalBuffer.capacity());
                            totalTexCoordBuffer.put(texCoords,0,texCoordBuffer.capacity());*/
                            logicalGridPos=grid.getSectionLogicalPosition(location[0],location[1],location[2]);
                            localVertexBuffer=BufferUtil.newFloatBuffer(vertexBuffer.capacity());
                            localVertexBuffer.put(vertexCoords,0,vertexBuffer.capacity());
                            localVertexBuffer.rewind();
                            buffersGrid[logicalGridPos[0]][logicalGridPos[1]][logicalGridPos[2]][0]=localVertexBuffer;
                            localIndexBuffer=BufferUtil.newIntBuffer(indexBuffer.capacity());
                            localIndexBuffer.put(indices,0,indexBuffer.capacity());
                            localIndexBuffer.rewind();
                            buffersGrid[logicalGridPos[0]][logicalGridPos[1]][logicalGridPos[2]][1]=localIndexBuffer;
                            localNormalBuffer=BufferUtil.newFloatBuffer(normalBuffer.capacity());
                            localNormalBuffer.put(normals,0,normalBuffer.capacity());
                            localNormalBuffer.rewind();
                            buffersGrid[logicalGridPos[0]][logicalGridPos[1]][logicalGridPos[2]][2]=localNormalBuffer;
                            localTexCoordBuffer=BufferUtil.newFloatBuffer(texCoordBuffer.capacity());
                            localTexCoordBuffer.put(texCoords,0,texCoordBuffer.capacity());
                            localTexCoordBuffer.rewind();
                            buffersGrid[logicalGridPos[0]][logicalGridPos[1]][logicalGridPos[2]][3]=localTexCoordBuffer;
                            if(isMergeEnabled&&verticesIndicesOfMergeableFaces!=null)
                                {mergeableIndexBuffer.get(indices,0,mergeableIndexBuffer.capacity());
                                 mergeableIndexBuffer.rewind();
                                 // Add an offset to the indices
                                 for(int i=0;i<mergeableIndexBuffer.capacity();i++)
                                     indices[i]+=indexOffset;
                                 localMergeableIndexBuffer=BufferUtil.newIntBuffer(mergeableIndexBuffer.capacity());
                                 localMergeableIndexBuffer.put(indices,0,mergeableIndexBuffer.capacity());
                                 localMergeableIndexBuffer.rewind();
                                 buffersGrid[logicalGridPos[0]][logicalGridPos[1]][logicalGridPos[2]][4]=localMergeableIndexBuffer;
                                }
                            else
                                buffersGrid[logicalGridPos[0]][logicalGridPos[1]][logicalGridPos[2]][4]=null;
                            // Update the offset
                            indexOffset+=vertexBuffer.capacity()/3;
                           }
                       
                       if(isMergeEnabled&&verticesIndicesOfMergeableFaces!=null)
                           {for(int i=0;i<grid.getLogicalWidth();i++)
                                for(int k=0;k<grid.getLogicalDepth();k++)
                                    if(buffersGrid[i][j][k][4]!=null)
                                        {/**
                                          * find the smallest value in the mergeable index buffer
                                          * use it to compute the index offset: Math.floor(min/(vertexBuffer.capacity()/3)*(vertexBuffer.capacity()/3)
                                          * for each set of 3 indices
                                          *     if it is in the mergeable faces
                                          *         check if adjoining faces can be merged with it
                                          */
                                        }
                           }
                       
                       totalVertexBufferSize=0;
                       totalIndexBufferSize=0;
                       totalNormalBufferSize=0;
                       totalTexCoordBufferSize=0;
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
                       totalVertexBuffer=BufferUtil.newFloatBuffer(totalVertexBufferSize);
                       totalIndexBuffer=BufferUtil.newIntBuffer(totalIndexBufferSize);
                       totalNormalBuffer=BufferUtil.newFloatBuffer(totalNormalBufferSize);
                       totalTexCoordBuffer=BufferUtil.newFloatBuffer(totalTexCoordBufferSize);
                       
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
                       
                       //TODO: put null values in the grid of buffers 
                       
                       totalVertexBuffer.rewind();
                       totalIndexBuffer.rewind();
                       totalNormalBuffer.rewind();
                       totalTexCoordBuffer.rewind();
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
                           if(tile.getName().equals(tileNameTable.get(key)))
                               {ImageIO.write(tile.getTexture(),"png",tileFile);
                                break;
                               }
                       EngineServiceSeeker.getInstance().attachTextureToSpatial(volumeElementMesh,tileFile.toURI().toURL());
                       //this file is now useless, delete it
                       tileFile.delete();
                       EngineServiceSeeker.getInstance().attachChildToNode(floorNode,volumeElementMesh);
                       meshIndex++;
                      }
                  EngineServiceSeeker.getInstance().attachChildToNode(levelNode,floorNode);
                  j++;
                 }
             System.out.println("[INFO] level node successfully created");
             System.out.println("[INFO] JFPSM attempts to write the level into the file "+destFile.getName());
             success=EngineServiceSeeker.getInstance().writeSavableInstanceIntoFile(levelNode,destFile);
             if(success)
                 {System.out.println("[INFO] Export into the file "+destFile.getName()+" successful");
                  System.out.println("[INFO] Elapsed time: "+(System.currentTimeMillis()-time)/1000.0f+" seconds");
                 }
             else
                 System.out.println("[WARNING]Export into the file "+destFile.getName()+" not successful!");
            }
    }
}
