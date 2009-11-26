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
        int floorIndex=0;
        BufferedImage image;
        int w,h,rgb;
        Map map;
        AbsoluteVolumeParameters[][] avp;
        ArrayList<AbsoluteVolumeParameters[][]> volumeElementsList=new ArrayList<AbsoluteVolumeParameters[][]>();
        for(Floor floor:level.getFloorsList())
            {map=floor.getMap(MapType.CONTAINER_MAP);
             image=map.getImage();
             w=image.getWidth();
             h=image.getHeight();
             avp=new AbsoluteVolumeParameters[w][h];
             /**
              * use the colors and the tiles to 
              * compute the geometry, the image 
              * is seen as a grid.
              */
             for(int i=0;i<w;i++)
                 for(int j=0;j<h;j++)
                     {avp[i][j]=new AbsoluteVolumeParameters();
                      //compute the absolute coordinates of the left bottom back vertex
                      avp[i][j].setLevelRelativePosition(i,floorIndex-0.5f,j);
                      rgb=image.getRGB(i,j);
                      //use the color of the image to get the matching tile
                      for(Tile tile:project.getTileSet().getTilesList())
                          if(tile.getColor().getRGB()==rgb)
                              {avp[i][j].setVolumeParam(tile.getVolumeParameters());
                               avp[i][j].setName(tile.getName());
                               break;
                              }
                     }
             volumeElementsList.add(avp);
             floorIndex++;
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
             FloatBuffer vertexBuffer,normalBuffer,texCoordBuffer,totalVertexBuffer,totalNormalBuffer,totalTexCoordBuffer;
             IntBuffer totalIndexBuffer,indexBuffer;
             ArrayList<float[]> locationList;
             ArrayList<Buffer> bufferList;
             floorIndex=0;
             int meshIndex,indexOffset;
             boolean isMergeEnabled;
             float[] vertexCoords=new float[3],normals=new float[3],texCoords=new float[3];
             int[] indices=new int[3];
             for(AbsoluteVolumeParameters[][] floorVolumeElements:volumeElementsList)
                 {volumeParamLocationTable.clear();
                  volumeParamTable.clear();
                  tileNameTable.clear();
                  mergeTable.clear();
                  for(int i=0;i<floorVolumeElements.length;i++)
                      for(int j=0;j<floorVolumeElements[i].length;j++)
                          if(!floorVolumeElements[i][j].isVoid())
                              {key=Integer.valueOf(floorVolumeElements[i][j].getVolumeParamIdentifier());
                               locationList=volumeParamLocationTable.get(key);
                               if(locationList==null)
                                   {locationList=new ArrayList<float[]>();
                                    volumeParamLocationTable.put(key,locationList);
                                    bufferList=new ArrayList<Buffer>();
                                    bufferList.add(floorVolumeElements[i][j].getVertexBuffer());
                                    bufferList.add(floorVolumeElements[i][j].getIndexBuffer());
                                    bufferList.add(floorVolumeElements[i][j].getNormalBuffer());
                                    bufferList.add(floorVolumeElements[i][j].getTexCoordBuffer());
                                    volumeParamTable.put(key,bufferList);
                                    tileNameTable.put(key,floorVolumeElements[i][j].getName());
                                    mergeTable.put(key,Boolean.valueOf(floorVolumeElements[i][j].isMergeEnabled()));
                                    if(floorVolumeElements[i][j].isMergeEnabled())
                                        verticesIndicesOfMergeableFacesTable.put(key,floorVolumeElements[i][j].getVerticesIndicesOfMergeableFaces());
                                   }
                               locationList.add(floorVolumeElements[i][j].getLevelRelativePosition());
                              }
                  // Create one node per floor (array)
                  floorNodeName=levelNodeName+"NID"+floorIndex;
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
                       if(vertexCoords.length<vertexBuffer.capacity())
                           vertexCoords=new float[vertexBuffer.capacity()];
                       if(indices.length<indexBuffer.capacity())
                           indices=new int[indexBuffer.capacity()];
                       if(normals.length<normalBuffer.capacity())
                           normals=new float[normalBuffer.capacity()];
                       if(texCoords.length<texCoordBuffer.capacity())
                           texCoords=new float[texCoordBuffer.capacity()];
                       totalVertexBuffer=BufferUtil.newFloatBuffer(vertexBuffer.capacity()*locationList.size());
                       totalIndexBuffer=BufferUtil.newIntBuffer(indexBuffer.capacity()*locationList.size());
                       totalNormalBuffer=BufferUtil.newFloatBuffer(normalBuffer.capacity()*locationList.size());
                       totalTexCoordBuffer=BufferUtil.newFloatBuffer(texCoordBuffer.capacity()*locationList.size());           
                       indexOffset=0;
                       isMergeEnabled=mergeTable.get(key).booleanValue();
                       if(isMergeEnabled)
                           verticesIndicesOfMergeableFaces=verticesIndicesOfMergeableFacesTable.get(key);
                       for(float[] location:locationList)
                           {vertexBuffer.get(vertexCoords,0,vertexBuffer.capacity());
                            vertexBuffer.rewind();
                            // Use the location to translate the vertices
                            for(int i=0;i<vertexBuffer.capacity();i++)
                                vertexCoords[i]+=location[i%location.length];
                            indexBuffer.get(indices,0,indexBuffer.capacity());
                            indexBuffer.rewind();
                            // Add an offset to the indices
                            for(int i=0;i<indexBuffer.capacity();i++)
                                indices[i]+=indexOffset;                           
                            normalBuffer.get(normals,0,normalBuffer.capacity());
                            normalBuffer.rewind();
                            texCoordBuffer.get(texCoords,0,texCoordBuffer.capacity());
                            texCoordBuffer.rewind();
                            /*if(isMergeEnabled&&verticesIndicesOfMergeableFaces!=null)
                                {
                                 //TODO: use verticesIndicesOfMergeableFaces
                                }
                            else
                                {*/totalVertexBuffer.put(vertexCoords,0,vertexBuffer.capacity());
                                 totalIndexBuffer.put(indices,0,indexBuffer.capacity());
                                 totalNormalBuffer.put(normals,0,normalBuffer.capacity());
                                 totalTexCoordBuffer.put(texCoords,0,texCoordBuffer.capacity());
                                 // Update the offset
                                 indexOffset+=vertexBuffer.capacity()/3;                             
                                /*}*/                        
                           }
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
                  floorIndex++;
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
