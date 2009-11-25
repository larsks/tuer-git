package jfpsm;

import java.io.File;
import java.net.URL;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

public final class EngineServiceSeeker implements I3DServiceSeeker{

    
    private static final EngineServiceSeeker instance=new EngineServiceSeeker();
    
    private I3DServiceSeeker delegate;
    
    
    @Override
    public void bind3DServiceSeeker(I3DServiceSeeker seeker){
        delegate=seeker;
    }

    public static final EngineServiceSeeker getInstance(){
        return(instance);
    }
    
    @Override
    public final boolean writeSavableInstanceIntoFile(final Object savable,final File file){
    	return(delegate.writeSavableInstanceIntoFile(savable,file));
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
    	return(delegate.createMeshFromBuffers(name,vertexBuffer,indexBuffer,normalBuffer,texCoordBuffer));
    }
    
    @Override
    public final void attachTextureToSpatial(final Object spatial,final URL url){
        delegate.attachTextureToSpatial(spatial,url);
    }
}
