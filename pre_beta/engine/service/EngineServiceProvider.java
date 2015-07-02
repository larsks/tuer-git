/**
 * Copyright (c) 2006-2014 Julien Gouesse
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package engine.service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import com.ardor3d.image.Texture;
import com.ardor3d.image.util.jogl.JoglImageLoader;
import com.ardor3d.renderer.state.TextureState;
import com.ardor3d.scenegraph.Mesh;
import com.ardor3d.scenegraph.MeshData;
import com.ardor3d.scenegraph.Node;
import com.ardor3d.scenegraph.Spatial;
import com.ardor3d.util.TextureManager;
import com.ardor3d.util.export.Savable;
import com.ardor3d.util.export.binary.BinaryClassObject;
import com.ardor3d.util.export.binary.BinaryExporter;
import com.ardor3d.util.export.binary.BinaryIdContentPair;
import com.ardor3d.util.export.binary.BinaryOutputCapsule;
import com.ardor3d.util.resource.URLResourceSource;

/**
 * service provider of the engine, this part is dependent on the underneath 3D engine. It should be quite
 * easy to modify this class to support any other engine
 * 
 * @author Julien Gouesse
 *
 */
public class EngineServiceProvider implements I3DServiceProvider<Savable,Node,Spatial,Mesh>{
    
    private static final class DirectBinaryExporter extends BinaryExporter{
        @Override
		protected BinaryIdContentPair generateIdContentPair(final BinaryClassObject bco) {
            final BinaryIdContentPair pair = new BinaryIdContentPair(_idCount++, new BinaryOutputCapsule(this, bco, true));
            return pair;
        }
    }
    
    private final BinaryExporter binaryExporter;
    
    public EngineServiceProvider(){
    	super();
    	this.binaryExporter=new DirectBinaryExporter();
        JoglImageLoader.registerLoader();
    }
    
    @Override
    public boolean writeSavableInstanceIntoFile(final Savable savable,final File file){
    	boolean success=true;
    	try{binaryExporter.save(savable,file);}
        catch(IOException ioe)
        {success=false;
         ioe.printStackTrace();
        }
        return(success);
    }
    
    @Override
    public boolean writeSavableInstancesListIntoFile(final ArrayList<Savable> savablesList,final File file){
        boolean success=true;
        try(FileOutputStream fos=new FileOutputStream(file)){
        	for(Savable savable:savablesList)
                {try{binaryExporter.save(savable,fos);}
                 catch(Throwable t)
                 {success=false;}
            	  if(!success)
                      break;
                 }
        }
        catch(Throwable t)
        {success=false;
         t.printStackTrace();
        }
        return(success);
    }
    
    @Override
    public void attachChildToNode(final Node parent,final Spatial child){
    	parent.attachChild(child);
    }
    
    @Override
    public Node createNode(final String name){
    	return(new Node(name));
    }
    
    @Override
    public Mesh createMeshFromBuffers(final String name,
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
    public void attachTextureToSpatial(final Spatial spatial,final URL url){
        TextureState ts=new TextureState();
        ts.setEnabled(true);
        ts.setTexture(TextureManager.load(new URLResourceSource(url),Texture.MinificationFilter.Trilinear,true));
        spatial.setRenderState(ts);
    }
}
