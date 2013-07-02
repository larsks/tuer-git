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

import com.ardor3d.framework.NativeCanvas;
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

	//TODO pass a runnable to perform the cleanup of the game state
	public UnloadingDisplayState(final NativeCanvas canvas,final TaskManager taskManager,final SoundManager soundManager){
		super(soundManager);
		this.canvas=canvas;
		this.taskManager=taskManager;
	}
	
	@Override
    public void init(){
		//TODO prepare the task manager and the task node
	}
	
	@Override
    public void setEnabled(final boolean enabled){
        final boolean wasEnabled=isEnabled();
        if(wasEnabled!=enabled)
            {super.setEnabled(enabled);
             if(enabled)
                 {//TODO prepare the cleanup
            	  //taskManager.enqueueTask(gameStateCleanupRunnable);
                  //taskNode.reset();
                  //updates the position of the task node (the resolution might have been modified)
                  //final int x=(cam.getWidth()-taskNode.getBounds().getWidth())/2;
                  //final int y=(cam.getHeight()/20);
                  //taskNode.setTranslation(x,y,0);
                  //taskNode.updateGeometricState(0);
                 }
            }
    }
}
