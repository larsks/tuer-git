/**
 * Copyright (c) 2006-2015 Julien Gouesse
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
import java.io.IOException;
import java.net.URL;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.List;

/**
 * Concrete engine service seeker
 * 
 * @author Julien Gouesse
 *
 * @param <S>
 * @param <T>
 * @param <U>
 * @param <V>
 */
public class EngineServiceSeeker<S,T,U,V,W> implements I3DServiceSeeker<S,T,U,V,W>{
    
    private I3DServiceSeeker<S,T,U,V,W> delegate;
    
    
    @Override
    public void bind3DServiceSeeker(I3DServiceSeeker<S,T,U,V,W> seeker){
        delegate=seeker;
    }
    
    @Override
    public boolean writeSavableInstanceIntoFile(final S savable,final File file){
    	return(delegate.writeSavableInstanceIntoFile(savable,file));
    }
    
    @Override
    public boolean writeSavableInstancesListIntoFile(final List<S> savablesList,final File file){
        return(delegate.writeSavableInstancesListIntoFile(savablesList,file));
    }
    
    @Override
    public void attachChildToNode(final T parent,final U child){
    	delegate.attachChildToNode(parent,child);
    }
    
    @Override
    public T createNode(final String name){
    	return(delegate.createNode(name));
    }
    
    @Override
    public V createMeshFromBuffers(final String name,
    		final FloatBuffer vertexBuffer,final IntBuffer indexBuffer,
    		final FloatBuffer normalBuffer,final FloatBuffer texCoordBuffer){
    	return(delegate.createMeshFromBuffers(name,vertexBuffer,indexBuffer,normalBuffer,texCoordBuffer));
    }
    
    @Override
    public void attachTextureToSpatial(final U spatial,final URL url){
        delegate.attachTextureToSpatial(spatial,url);
    }

	@Override
	public W createBoundingBox(final double xCenter,final double yCenter,final double zCenter,final double xExtent,final double yExtent,final double zExtent) {
		return(delegate.createBoundingBox(xCenter,yCenter,zCenter,xExtent,yExtent,zExtent));
	}

	@Override
	public U load(final File inputModelFile,final String inputModelFileFormat)throws IOException,UnsupportedOperationException{
		return(delegate.load(inputModelFile,inputModelFileFormat));
	}

	@Override
	public void save(final File outputModelFile,final String outputModelFileFormat,final File secondaryOutputModelFile,final U convertible)throws IOException,UnsupportedOperationException{
		delegate.save(outputModelFile,outputModelFileFormat,secondaryOutputModelFile,convertible);
	}
}
