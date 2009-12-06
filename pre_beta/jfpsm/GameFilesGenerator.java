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
             int[][][] absoluteVerticesIndicesOfMergeableFaces=null;
             int[] localAbsoluteVerticesIndicesOfMergeableFace=new int[3];
             float[] vertex=new float[3],localVertex=new float[3];
             boolean mergeableFacesFound=false;
             boolean mergeableFaceFound=false;
             // Use the identifier of the volume parameter as a key rather than the vertex buffer
             Integer key;
             FloatBuffer vertexBuffer,normalBuffer,texCoordBuffer,totalVertexBuffer,totalNormalBuffer,totalTexCoordBuffer,localVertexBuffer,localNormalBuffer,localTexCoordBuffer,newVertexBuffer,newNormalBuffer,newTexCoordBuffer;
             IntBuffer totalIndexBuffer,indexBuffer,mergeableIndexBuffer,localIndexBuffer,localMergeableIndexBuffer,localMergeIndexBuffer,newIndiceBuffer;
             int totalVertexBufferSize,totalIndexBufferSize,totalNormalBufferSize,totalTexCoordBufferSize;
             ArrayList<float[]> locationList;
             ArrayList<Buffer> bufferList;
             j=0;
             int meshIndex,indexOffset;
             boolean isMergeEnabled;
             float[] vertexCoords=new float[3],normals=new float[3],texCoords=new float[3];
             int[] indices=new int[3];
             Buffer[][][][] buffersGrid=new Buffer[grid.getLogicalWidth()][grid.getLogicalHeight()][grid.getLogicalDepth()][6];       
             HashMap<Integer,Integer> indexCountTable=new HashMap<Integer,Integer>();
             HashMap<Integer,Integer> reindexationTable=new HashMap<Integer,Integer>();
             int[][][] indexOffsetArray=new int[grid.getLogicalWidth()][grid.getLogicalHeight()][grid.getLogicalDepth()];
             int[][][] newIndexOffsetArray=new int[grid.getLogicalWidth()][grid.getLogicalHeight()][grid.getLogicalDepth()];
             int[] logicalGridPos;
             final int[] triIndices=new int[3];
             int startPos,localStartPos,newBufferSize,indice;
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
                       isMergeEnabled=mergeTable.get(key).booleanValue();
                       if(isMergeEnabled)
                           {verticesIndicesOfMergeableFaces=verticesIndicesOfMergeableFacesTable.get(key);
                            if(verticesIndicesOfMergeableFaces!=null)
                                {//copy the vertices indices
                                 absoluteVerticesIndicesOfMergeableFaces=new int[verticesIndicesOfMergeableFaces.length][2][3];
                                 for(int ii=0;ii<verticesIndicesOfMergeableFaces.length;ii++)
                                     for(int jj=0;jj<2;jj++)
                                         for(int kk=0;kk<3;kk++)
                                             absoluteVerticesIndicesOfMergeableFaces[ii][jj][kk]=verticesIndicesOfMergeableFaces[ii][jj][kk];
                                }
                           }
                       indexOffset=0;
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
                            logicalGridPos=grid.getSectionLogicalPosition(location[0],location[1],location[2]);
                            // Add an offset to the indices
                            for(int i=0;i<indexBuffer.capacity();i++)
                                indices[i]+=indexOffset;                           
                            normalBuffer.get(normals,0,normalBuffer.capacity());
                            normalBuffer.rewind();
                            texCoordBuffer.get(texCoords,0,texCoordBuffer.capacity());
                            texCoordBuffer.rewind();                           
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
                                 //fill the buffer used for the merge, it will contain rearranged indices (easier to compare)
                                 buffersGrid[logicalGridPos[0]][logicalGridPos[1]][logicalGridPos[2]][4]=localMergeableIndexBuffer;
                                 localMergeIndexBuffer=BufferUtil.newIntBuffer(mergeableIndexBuffer.capacity());
                                 localMergeIndexBuffer.put(indices,0,mergeableIndexBuffer.capacity());
                                 localMergeIndexBuffer.rewind();
                                 //fill the buffer used for the merge, it will contain the markers
                                 buffersGrid[logicalGridPos[0]][logicalGridPos[1]][logicalGridPos[2]][5]=localMergeIndexBuffer;
                                 indexOffsetArray[logicalGridPos[0]][logicalGridPos[1]][logicalGridPos[2]]=indexOffset;
                                }
                            else
                                buffersGrid[logicalGridPos[0]][logicalGridPos[1]][logicalGridPos[2]][4]=null;
                            // Update the offset
                            indexOffset+=vertexBuffer.capacity()/3;
                           }
                       //if the merge is enabled, perform the merge
                       if(isMergeEnabled&&verticesIndicesOfMergeableFaces!=null)
                           {//mark all useless indices to ease their later removal
                            for(int i=0;i<grid.getLogicalWidth();i++)
                                for(int k=0;k<grid.getLogicalDepth();k++)
                                    if(buffersGrid[i][j][k][4]!=null)
                                        {indexOffset=indexOffsetArray[i][j][k];
                                         //add the offset
                                         for(int ii=0;ii<absoluteVerticesIndicesOfMergeableFaces.length;ii++)
                                             for(int jj=0;jj<2;jj++)
                                                 for(int kk=0;kk<3;kk++)
                                                     absoluteVerticesIndicesOfMergeableFaces[ii][jj][kk]+=indexOffset;
                                         while(buffersGrid[i][j][k][4].hasRemaining())
                                             {startPos=((IntBuffer)buffersGrid[i][j][k][4]).position();
                                              ((IntBuffer)buffersGrid[i][j][k][4]).get(triIndices,0,3);      
                                              mergeableFaceFound=false;
                                              for(int ii=0;!mergeableFaceFound&&ii<absoluteVerticesIndicesOfMergeableFaces.length;ii++)
                                                  for(int jj=0;!mergeableFaceFound&&jj<2;jj++)
                                                      if(mergeableFaceFound=Arrays.equals(triIndices,absoluteVerticesIndicesOfMergeableFaces[ii][jj]))
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
                                                                                          localAbsoluteVerticesIndicesOfMergeableFace[index]=verticesIndicesOfMergeableFaces[ii][jjj][index]+indexOffsetArray[locali][localj][localk];
                                                                                          //for each coordinate
                                                                                          for(int subIndex=0;subIndex<3;subIndex++)
                                                                                              {//use internal indexing
                                                                                               localVertex[subIndex]=((FloatBuffer)buffersGrid[locali][localj][localk][0]).get((verticesIndicesOfMergeableFaces[ii][jjj][index]*3)+subIndex);
                                                                                               vertex[subIndex]=((FloatBuffer)buffersGrid[i][j][k][0]).get((verticesIndicesOfMergeableFaces[ii][jj][index]*3)+subIndex);
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
                                                                                          for(int delIndex=0;delIndex<((IntBuffer)buffersGrid[i][j][k][5]).capacity();delIndex+=3)
                                                                                              if(((IntBuffer)buffersGrid[locali][localj][localk][5]).get(delIndex)==localAbsoluteVerticesIndicesOfMergeableFace[delIndex%3]&&
                                                                                                 ((IntBuffer)buffersGrid[locali][localj][localk][5]).get(delIndex+1)==localAbsoluteVerticesIndicesOfMergeableFace[(delIndex+1)%3]&&
                                                                                                 ((IntBuffer)buffersGrid[locali][localj][localk][5]).get(delIndex+2)==localAbsoluteVerticesIndicesOfMergeableFace[(delIndex+2)%3])
                                                                                                  {localStartPos=delIndex;
                                                                                                   break;
                                                                                                  }
                                                                                          if(localStartPos!=-1)
                                                                                              for(int delIndex=0;delIndex<3;delIndex++)
                                                                                                  ((IntBuffer)buffersGrid[locali][localj][localk][5]).put(localStartPos+delIndex,-1);
                                                                                          else
                                                                                              System.out.println("[WARNING] indices not found");
                                                                                         }                                                                                                                                                                      
                                                                                    }
                                                                   }                           
                                                           break;
                                                          }                         
                                             }
                                         buffersGrid[i][j][k][4].rewind();
                                         //subtract the offset
                                         for(int ii=0;ii<absoluteVerticesIndicesOfMergeableFaces.length;ii++)
                                             for(int jj=0;jj<2;jj++)
                                                 for(int kk=0;kk<3;kk++)
                                                     absoluteVerticesIndicesOfMergeableFaces[ii][jj][kk]-=indexOffset;
                                        }
                           //rebuild all index buffers by retaining only useful indices and do the same for all other buffers
                           for(int i=0;i<grid.getLogicalWidth();i++)
                               for(int k=0;k<grid.getLogicalDepth();k++)
                                   if(buffersGrid[i][j][k][4]!=null)
                                       {indexOffset=indexOffsetArray[i][j][k];
                                        //compute the new size
                                        newBufferSize=0;
                                        ((IntBuffer)buffersGrid[i][j][k][5]).rewind();
                                        while(((IntBuffer)buffersGrid[i][j][k][5]).hasRemaining())
                                            if(((IntBuffer)buffersGrid[i][j][k][5]).get()!=-1)
                                                newBufferSize++;     
                                        ((IntBuffer)buffersGrid[i][j][k][5]).rewind();
                                        //check if the index buffer has to be remade
                                        if(newBufferSize!=((IntBuffer)buffersGrid[i][j][k][5]).capacity())
                                            {//if the new buffer is empty
                                             if(newBufferSize==0)
                                                 {//replace all buffers by empty buffers
                                                  for(int bufIndex=0;bufIndex<6;bufIndex++)
                                                      if(bufIndex==1||bufIndex==4||bufIndex==5)
                                                          buffersGrid[i][j][k][bufIndex]=BufferUtil.newIntBuffer(0);
                                                      else
                                                          buffersGrid[i][j][k][bufIndex]=BufferUtil.newFloatBuffer(0);
                                                 }
                                             else
                                                 {((IntBuffer)buffersGrid[i][j][k][1]).rewind();
                                                  while(((IntBuffer)buffersGrid[i][j][k][1]).hasRemaining())
                                                      indexCountTable.put(Integer.valueOf(((IntBuffer)buffersGrid[i][j][k][1]).get()),Integer.valueOf(0));
                                                  ((IntBuffer)buffersGrid[i][j][k][1]).rewind();
                                                  newIndiceBuffer=BufferUtil.newIntBuffer(newBufferSize);
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
                                                  newVertexBuffer=BufferUtil.newFloatBuffer(newBufferSize*3);
                                                  newNormalBuffer=BufferUtil.newFloatBuffer(newBufferSize*3);
                                                  newTexCoordBuffer=BufferUtil.newFloatBuffer(newBufferSize*2);
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
                                                  //update the index buffer to take into account the new indexation
                                                  for(int l=0;l<newIndiceBuffer.capacity();l++)
                                                      //replace each old index by its corresponding new index
                                                      newIndiceBuffer.put(l,reindexationTable.get(Integer.valueOf(newIndiceBuffer.get(l))));
                                                  //not mandatory as we have just used an absolute access above
                                                  newIndiceBuffer.rewind();
                                                  reindexationTable.clear();
                                                  //replace the old vertex buffer by the new one
                                                  buffersGrid[i][j][k][0]=newVertexBuffer;
                                                  //do the same for the normal buffer
                                                  buffersGrid[i][j][k][2]=newNormalBuffer;
                                                  //do the same for the texture coordinates buffer
                                                  buffersGrid[i][j][k][3]=newTexCoordBuffer;
                                                  //replace the old index buffer by the new index buffer
                                                  buffersGrid[i][j][k][1]=newIndiceBuffer;
                                                  //reset the counters of occurrence
                                                  indexCountTable.clear();
                                                 }
                                            }                                       
                                       }
                            //each individual change of index interval impacts all index buffers
                            //TODO: then, remove all useless index intervals and update all index buffers
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
                       
                       //put null values into the grid of buffers
                       //because it has to be emptied before using another volume parameter
                       for(int i=0;i<grid.getLogicalWidth();i++)
                           for(int k=0;k<grid.getLogicalDepth();k++)
                               for(int bi=0;bi<5;bi++)
                                   buffersGrid[i][j][k][bi]=null;
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
