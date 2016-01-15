/**
 * Copyright (c) 2006-2016 Julien Gouesse
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
package engine.statemachine;

import java.util.concurrent.Callable;

import com.ardor3d.framework.NativeCanvas;
import com.ardor3d.framework.jogl.JoglNewtWindow;
import com.ardor3d.renderer.RenderContext;
import com.ardor3d.scenegraph.Node;
import com.ardor3d.scenegraph.controller.SpatialController;
import com.ardor3d.util.ContextGarbageCollector;
import com.ardor3d.util.GameTaskQueueManager;

import engine.data.ProfileData;
import engine.misc.SettingsProvider;
import engine.sound.SoundManager;

/**
 * State reached when exiting the game once for all, used for the final cleanup
 * 
 * @author Julien Gouesse
 *
 */
public class ExitGameState extends ScenegraphState{
	
	private final NativeCanvas canvas;
	
	private final SoundManager soundManager;
	
	private final ProfileData profileData;
	
	private final SettingsProvider settingsProvider;

	public ExitGameState(final NativeCanvas canvas,final SoundManager soundManager,final ProfileData profileData,final SettingsProvider settingsProvider){
		super(soundManager);
		this.canvas=canvas;
		this.soundManager=soundManager;
		this.profileData=profileData;
		this.settingsProvider=settingsProvider;
		getRoot().addController(new SpatialController<Node>(){
			@Override
			public void update(final double timeSinceLastCall,final Node caller){
				performFinalCleanupAndExit();
			}
		});
	}
	
	protected void performFinalCleanupAndExit(){
		try{settingsProvider.save();
			profileData.save();
			soundManager.stop();
			soundManager.cleanup();
		    final RenderContext renderContext=canvas.getCanvasRenderer().getRenderContext();
		    GameTaskQueueManager.getManager(renderContext).render(new Callable<Void>(){
                @Override
                public Void call() throws Exception{
            	    ContextGarbageCollector.doFinalCleanup(canvas.getCanvasRenderer().getRenderer());
                    return(null);
                }
            });
		    ((JoglNewtWindow)canvas).getNewtWindow().destroy();
		   }
		finally
		{//necessary for Java Web Start
	     System.exit(0);
		}
	}
}
