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
package engine.statemachine;

import java.util.concurrent.Callable;

import com.ardor3d.framework.NativeCanvas;
import com.ardor3d.framework.jogl.JoglNewtWindow;
import com.ardor3d.renderer.RenderContext;
import com.ardor3d.scenegraph.Node;
import com.ardor3d.scenegraph.controller.SpatialController;
import com.ardor3d.util.ContextGarbageCollector;
import com.ardor3d.util.GameTaskQueueManager;

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

	public ExitGameState(final NativeCanvas canvas,final SoundManager soundManager){
		super(soundManager);
		this.canvas=canvas;
		this.soundManager=soundManager;
		getRoot().addController(new SpatialController<Node>(){
			@Override
			public void update(final double timeSinceLastCall,final Node caller){
				performFinalCleanupAndExit();
			}
		});
	}
	
	protected void performFinalCleanupAndExit(){
		try{soundManager.cleanup();
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
