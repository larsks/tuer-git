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
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import com.ardor3d.image.Texture;
import com.ardor3d.image.util.AWTImageLoader;
import com.ardor3d.renderer.state.TextureState;
import com.ardor3d.scenegraph.Mesh;
import com.ardor3d.scenegraph.MeshData;
import com.ardor3d.scenegraph.Node;
import com.ardor3d.scenegraph.Spatial;
import com.ardor3d.util.TextureManager;
import com.ardor3d.util.export.Savable;
import com.ardor3d.util.export.binary.BinaryExporter;
import com.ardor3d.util.resource.URLResourceSource;

/**
 * service provider of the engine, this part is dependent on the underneath 3D engine. It should be quite
 * easy to modify this class to support any other engine
 * @author Julien Gouesse
 *
 */
public final class EngineServiceProvider implements I3DServiceProvider{
    
    /**unique instance of the engine service provider (design pattern "singleton")*/
    private static final EngineServiceProvider instance=new EngineServiceProvider();
    
    
    private EngineServiceProvider(){
        // Add our awt based image loader.
        AWTImageLoader.registerLoader();
    }
    
    
    public static final EngineServiceProvider getInstance(){
        return(instance);
    }
    
    @Override
    public final boolean writeSavableInstanceIntoFile(final Object savable,final File file){
    	boolean success=savable instanceof Savable;
    	if(success)
    	    try{BinaryExporter.getInstance().save((Savable)savable,file);}
            catch(IOException ioe)
            {success=false;
             ioe.printStackTrace();
            }
        return(success);
    }
    
    @Override
    public final boolean writeSavableInstancesListIntoFile(final ArrayList<?> savablesList,final File file){
        boolean success=true;
        FileOutputStream fos=null;
        try{fos=new FileOutputStream(file);} 
        catch(FileNotFoundException fnfe)
        {success=false;
         fnfe.printStackTrace();
        }
        if(success)
            try{for(Object savable:savablesList)
                    {try{BinaryExporter.getInstance().save((Savable)savable,fos);}
                     catch(Throwable t)
                     {success=false;}
            	     if(!success)
                        break;
                    }
                fos.close(); 
               }
            catch(IOException ioe)
            {success=false;
             ioe.printStackTrace();
            }
            catch(ClassCastException cce)
            {success=false;
             cce.printStackTrace();
            }
        return(success);
    }
    
    @Override
    public final void attachChildToNode(final Object parent,final Object child){
    	((Node)parent).attachChild((Spatial)child);
    }
    
    @Override
    public final Object createNode(final String name){
    	return(new Node(name));
    }
    
    @Override
    public final Object createMeshFromBuffers(final String name,
    		final FloatBuffer vertexBuffer,final IntBuffer indexBuffer,
    		final FloatBuffer normalBuffer,final FloatBuffer texCoordBuffer){  	
    	MeshData meshData=new MeshData();
        meshData.setVertexBuffer(vertexBuffer);
        meshData.setIndexBuffer(indexBuffer);
        meshData.setNormalBuffer(normalBuffer);
        meshData.setTextureBuffer(texCoordBuffer,0);
        Mesh mesh=new Mesh(name);
        mesh.setMeshData(meshData);
    	return(mesh);
    }
    
    @Override
    public final void attachTextureToSpatial(final Object spatial,final URL url){
        TextureState ts=new TextureState();
        ts.setEnabled(true);
        ts.setTexture(TextureManager.load(new URLResourceSource(url),Texture.MinificationFilter.Trilinear,true));
        ((Spatial)spatial).setRenderState(ts);
    }
}
