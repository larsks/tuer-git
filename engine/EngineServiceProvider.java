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
package engine;

import java.io.File;
import java.io.IOException;
import java.nio.Buffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import com.ardor3d.scenegraph.Mesh;
import com.ardor3d.scenegraph.MeshData;
import com.ardor3d.scenegraph.Node;
import com.ardor3d.util.export.binary.BinaryExporter;
import com.sun.opengl.util.BufferUtil;

public final class EngineServiceProvider implements I3DServiceProvider{
    
    
    private static final EngineServiceProvider instance=new EngineServiceProvider();
    
    
    
    public static final EngineServiceProvider getInstance(){
        return(instance);
    }
    
    @Override
    public void writeLevel(File levelFile,ArrayList<? extends ILevelRelativeVolumeElement[][]> volumeElementsList){
        boolean success=true;
        if(!levelFile.exists())
            {try{success=levelFile.createNewFile();} 
             catch(IOException ioe)
             {success=false;}
             if(!success)
                 throw new RuntimeException("The file "+levelFile.getAbsolutePath()+" cannot be created!");
            }
        if(success)
            {final long time=System.currentTimeMillis();
             System.out.println("[INFO] JFPSM attempts to create a level node...");
             // Create one node per level
             final Node levelNode=new Node(levelFile.getName().substring(0,levelFile.getName().lastIndexOf(".")));
             Node floorNode;
             HashMap<Integer,ArrayList<float[]>> volumeParamLocationTable;
             HashMap<Integer,ArrayList<Buffer>> volumeParamTable;
             // Use the identifier of the volume parameter as a key rather than the vertex buffer
             Integer key;
             FloatBuffer vertexBuffer,normalBuffer,texCoordBuffer,totalVertexBuffer,totalNormalBuffer,totalTexCoordBuffer;
             IntBuffer totalIndexBuffer,indexBuffer;
             ArrayList<float[]> locationList;
             ArrayList<Buffer> bufferList;
             int floorIndex=0,meshIndex,indexOffset,vertexCoordIndex;
             MeshData volumeElementMeshData;
             Mesh volumeElementMesh;
             for(ILevelRelativeVolumeElement[][] floorVolumeElements:volumeElementsList)
                 {// Create one node per floor (array)
                  floorNode=new Node("level "+levelNode.getName()+" floor "+floorIndex);
                  // Create a table to sort all elements using the same volume parameter
                  volumeParamLocationTable=new HashMap<Integer,ArrayList<float[]>>();
                  volumeParamTable=new HashMap<Integer,ArrayList<Buffer>>();
                  for(int i=0;i<floorVolumeElements.length;i++)
                      for(int j=0;j<floorVolumeElements[i].length;j++)
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
                               }
                           locationList.add(floorVolumeElements[i][j].getLevelRelativePosition());
                          }                         
                  meshIndex=0;
                  // Use the geometries passed as arguments to build the mesh data
                  for(Map.Entry<Integer,ArrayList<float[]>> entry:volumeParamLocationTable.entrySet())
                      {key=entry.getKey();
                       locationList=entry.getValue();
                       bufferList=volumeParamTable.get(key);
                       vertexBuffer=(FloatBuffer)bufferList.get(0);
                       indexBuffer=(IntBuffer)bufferList.get(1);
                       normalBuffer=(FloatBuffer)bufferList.get(2);
                       texCoordBuffer=(FloatBuffer)bufferList.get(3);
                       totalVertexBuffer=BufferUtil.newFloatBuffer(vertexBuffer.capacity()*locationList.size());
                       totalIndexBuffer=BufferUtil.newIntBuffer(indexBuffer.capacity()*locationList.size());
                       totalNormalBuffer=BufferUtil.newFloatBuffer(normalBuffer.capacity()*locationList.size());
                       totalTexCoordBuffer=BufferUtil.newFloatBuffer(texCoordBuffer.capacity()*locationList.size());           
                       indexOffset=0;
                       for(float[] location:locationList)
                           {// Use the location to translate the vertices
                            vertexCoordIndex=0;
                            while(vertexBuffer.hasRemaining())
                                {totalVertexBuffer.put(vertexBuffer.get()+location[vertexCoordIndex]);
                                 vertexCoordIndex=(vertexCoordIndex+1)%location.length;
                                }
                            vertexBuffer.rewind();
                            // Add an offset to the indices
                            while(indexBuffer.hasRemaining())
                                totalIndexBuffer.put(indexBuffer.get()+indexOffset);
                            indexBuffer.rewind();
                            // Update the offset
                            indexOffset+=vertexBuffer.capacity()/3;
                            totalNormalBuffer.put(normalBuffer);
                            normalBuffer.rewind();
                            totalTexCoordBuffer.put(texCoordBuffer);
                            texCoordBuffer.rewind();
                           }
                       totalVertexBuffer.rewind();
                       totalIndexBuffer.rewind();
                       totalNormalBuffer.rewind();
                       totalTexCoordBuffer.rewind();
                       volumeElementMeshData=new MeshData();
                       volumeElementMeshData.setVertexBuffer(totalVertexBuffer);
                       volumeElementMeshData.setIndexBuffer(totalIndexBuffer);
                       volumeElementMeshData.setNormalBuffer(totalNormalBuffer);
                       volumeElementMeshData.setTextureBuffer(totalTexCoordBuffer,0);
                       volumeElementMesh=new Mesh(floorNode.getName()+" mesh "+meshIndex);
                       volumeElementMesh.setMeshData(volumeElementMeshData);
                       floorNode.attachChild(volumeElementMesh);
                       meshIndex++;
                      }
                  levelNode.attachChild(floorNode);
                  floorIndex++;
                 }
             System.out.println("[INFO] level node successfully created");
             System.out.println("[INFO] JFPSM attempts to write the level into the file "+levelFile.getName());
             // Export the level node
             try{BinaryExporter.getInstance().save(levelNode,levelFile);}
             catch(IOException ioe)
             {success=false;
              ioe.printStackTrace();
             }
             if(success)
                 {System.out.println("[INFO] Export into the file "+levelFile.getName()+" successful");
                  System.out.println("[INFO] Elapsed time: "+(System.currentTimeMillis()-time)/1000.0f+" seconds");
                 }
             else
                 System.out.println("[WARNING]Export into the file "+levelFile.getName()+" not successful!");
            }
    }
}
