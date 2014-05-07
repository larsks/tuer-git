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
package connection;

import java.io.File;
import java.net.URL;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;

import engine.service.EngineServiceProvider;
import jfpsm.EngineServiceSeeker;
import jfpsm.I3DServiceSeeker;
import jfpsm.MainWindow;

/**
 * Service provider of JFPSM. It allows to separate the concerns, not to mix 
 * the editor and the 3D engine
 * 
 * @author Julien Gouesse
 *
 */
public final class JFPSMServiceProvider implements I3DServiceSeeker{
    
    /**delegate that executes the services of the engine*/
    private final engine.service.I3DServiceProvider delegate;
    
    
    private JFPSMServiceProvider(final engine.service.I3DServiceProvider 
    		factory,final I3DServiceSeeker seeker){
        delegate=factory;
        bind3DServiceSeeker(seeker);
    }
    
    
    @Override
    public final void bind3DServiceSeeker(I3DServiceSeeker seeker){
        seeker.bind3DServiceSeeker(this);
    }
    
    @Override
    public final boolean writeSavableInstanceIntoFile(final Object savable,
    		final File file){
    	return(delegate.writeSavableInstanceIntoFile(savable,file));
    }
    
    @Override
    public final boolean writeSavableInstancesListIntoFile(
    		final ArrayList<?> savablesList,final File file){
        return(delegate.writeSavableInstancesListIntoFile(savablesList,file));
    }
    
    @Override
    public final void attachChildToNode(final Object parent,final Object child){
    	delegate.attachChildToNode(parent,child);
    }
    
    @Override
    public final Object createNode(final String name){
    	return(delegate.createNode(name));
    }
    
    @Override
    public final Object createMeshFromBuffers(final String name,
    		final FloatBuffer vertexBuffer,final IntBuffer indexBuffer,
    		final FloatBuffer normalBuffer,final FloatBuffer texCoordBuffer){
    	return(delegate.createMeshFromBuffers(name,vertexBuffer,indexBuffer,
    		   normalBuffer,texCoordBuffer));
    }
    
    @Override
    public final void attachTextureToSpatial(final Object spatial,
    		final URL url){
        delegate.attachTextureToSpatial(spatial,url);
    }
    
    public static final void main(String[] args){
        //Disables DirectDraw under Windows in order to avoid conflicts with
    	//OpenGL
        System.setProperty("sun.java2d.noddraw","true");
        new JFPSMServiceProvider(EngineServiceProvider.getInstance(),
                                 EngineServiceSeeker.getInstance());
        MainWindow.runInstance(args);
    }
}
