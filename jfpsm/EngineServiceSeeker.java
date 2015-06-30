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
package jfpsm;

import java.io.File;
import java.net.URL;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;

import com.ardor3d.scenegraph.Mesh;
import com.ardor3d.scenegraph.Node;
import com.ardor3d.scenegraph.Spatial;
import com.ardor3d.util.export.Savable;

public final class EngineServiceSeeker implements I3DServiceSeeker<Savable,Node,Spatial,Mesh>{

    
    private static final EngineServiceSeeker instance=new EngineServiceSeeker();
    
    private I3DServiceSeeker<Savable,Node,Spatial,Mesh> delegate;
    
    
    @Override
    public void bind3DServiceSeeker(I3DServiceSeeker<Savable,Node,Spatial,Mesh> seeker){
        delegate=seeker;
    }

    public static final EngineServiceSeeker getInstance(){
        return(instance);
    }
    
    @Override
    public final boolean writeSavableInstanceIntoFile(final Savable savable,final File file){
    	return(delegate.writeSavableInstanceIntoFile(savable,file));
    }
    
    @Override
    public final boolean writeSavableInstancesListIntoFile(final ArrayList<Savable> savablesList,final File file){
        return(delegate.writeSavableInstancesListIntoFile(savablesList,file));
    }
    
    @Override
    public final void attachChildToNode(final Node parent,final Spatial child){
    	delegate.attachChildToNode(parent,child);
    }
    
    @Override
    public final Node createNode(final String name){
    	return(delegate.createNode(name));
    }
    
    @Override
    public final Mesh createMeshFromBuffers(final String name,
    		final FloatBuffer vertexBuffer,final IntBuffer indexBuffer,
    		final FloatBuffer normalBuffer,final FloatBuffer texCoordBuffer){
    	return(delegate.createMeshFromBuffers(name,vertexBuffer,indexBuffer,normalBuffer,texCoordBuffer));
    }
    
    @Override
    public final void attachTextureToSpatial(final Spatial spatial,final URL url){
        delegate.attachTextureToSpatial(spatial,url);
    }
}
