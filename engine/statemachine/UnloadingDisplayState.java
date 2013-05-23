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
import com.ardor3d.renderer.RenderContext;
import com.ardor3d.util.GameTaskQueueManager;

import engine.sound.SoundManager;
import engine.taskmanagement.TaskManager;

/**
 * State that carefully unloads all resources used by the latest game
 * 
 * @author Julien Gouesse
 *
 */
public class UnloadingDisplayState extends ScenegraphState{
	
	private final NativeCanvas canvas;
	
	private final TaskManager taskManager;
	
	private final SoundManager soundManager;

	public UnloadingDisplayState(final NativeCanvas canvas,final TaskManager taskManager,final SoundManager soundManager){
		super();
		this.canvas=canvas;
		this.taskManager=taskManager;
		this.soundManager=soundManager;
	}
	
	@Override
    public void init(){
		taskManager.enqueueTask(new Runnable(){			
			@Override
			public final void run() {
				unloadUnusedSounds();
			}
		});
		taskManager.enqueueTask(new Runnable(){			
			@Override
			public final void run() {
				performRuntimeCleanup();
			}
		});
	}
	
	protected void unloadUnusedSounds(){
		//FIXME detect the sound samples used in the latest game and unload them
	}
	
	protected void performRuntimeCleanup(){
		final RenderContext renderContext=canvas.getCanvasRenderer().getRenderContext();
	    GameTaskQueueManager.getManager(renderContext).render(new Callable<Void>(){
            @Override
            public Void call() throws Exception{
            	//FIXME do almost the same thing except that you have to destroy the direct NIO buffers by using the dedicated method in the renderer
        	    //ContextGarbageCollector.doRuntimeCleanup(canvas.getCanvasRenderer().getRenderer());
                return(null);
            }
        });
	}
}
