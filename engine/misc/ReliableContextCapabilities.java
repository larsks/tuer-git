package engine.misc;

import com.ardor3d.renderer.ContextCapabilities;

public class ReliableContextCapabilities extends ContextCapabilities {

	public ReliableContextCapabilities(final ContextCapabilities defaultCaps){
		super(defaultCaps);
        //System.err.println(defaultCaps.getDisplayRenderer());
        //System.err.println(defaultCaps.getDisplayVendor());
        //System.err.println(defaultCaps.getDisplayVersion());
        if(defaultCaps.getDisplayRenderer().startsWith("Mesa DRI R200 "))
      	    /**
      	     * Some very old ATI Radeon graphics cards do not support 2048*2048 textures
      	     * despite their specifications.
      	     */
            _maxTextureSize=defaultCaps.getMaxTextureSize()/2;
	}
}
