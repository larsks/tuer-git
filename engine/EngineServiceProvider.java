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
import java.util.ArrayList;

import com.ardor3d.scenegraph.Mesh;
import com.ardor3d.scenegraph.MeshData;
import com.ardor3d.scenegraph.Node;
import com.ardor3d.util.export.binary.BinaryExporter;

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
            {System.out.println("JFPSM attempts to write the level into the file "+levelFile.getName());
             // Create one node per level
             final Node levelNode=new Node(levelFile.getName().substring(0,levelFile.getName().lastIndexOf(".")));
             Node floorNode;
             int floorIndex=0,meshIndex;
             MeshData volumeElementMeshData;
             Mesh volumeElementMesh;
             for(ILevelRelativeVolumeElement[][] floorVolumeElements:volumeElementsList)
                 {// Create one node per floor (array)
                  floorNode=new Node("level "+levelNode.getName()+" floor "+floorIndex);
                  meshIndex=0;
                  // Use the geometries passed as arguments to build the mesh data
                  for(int i=0;i<floorVolumeElements.length;i++)
                      for(int j=0;j<floorVolumeElements[i].length;j++)
                          {volumeElementMeshData=new MeshData();
                           volumeElementMeshData.setVertexBuffer(floorVolumeElements[i][j].getVertexBuffer());
                           volumeElementMeshData.setIndexBuffer(floorVolumeElements[i][j].getIndexBuffer());
                           volumeElementMeshData.setTextureBuffer(floorVolumeElements[i][j].getTexCoordBuffer(),0);
                           volumeElementMeshData.setNormalBuffer(floorVolumeElements[i][j].getNormalBuffer());
                           volumeElementMesh=new Mesh(floorNode.getName()+" mesh "+meshIndex);
                           floorNode.attachChild(volumeElementMesh);
                           meshIndex++;
                          }
                  levelNode.attachChild(floorNode);
                  floorIndex++;
                 }
             System.out.println("JFPSM attempts to write the level into the file "+levelFile.getName());
             // Export the level node
             try{BinaryExporter.getInstance().save(levelNode,levelFile);}
             catch(IOException ioe)
             {success=false;
              ioe.printStackTrace();
             }
             if(success)
                 System.out.println("[INFO] Export into the file "+levelFile.getName()+" successful");
             else
                 System.out.println("[WARNING]Export into the file "+levelFile.getName()+" not successful!");
            }
    }
}
