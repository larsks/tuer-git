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

import com.ardor3d.input.logical.LogicalLayer;
import com.ardor3d.scenegraph.Node;
import com.ardor3d.scenegraph.Spatial;
import com.ardor3d.util.ReadOnlyTimer;

public class ScenegraphStateMachine extends StateMachine {

	
	private final StateMachineSwitchNode switchNode;
	
	public ScenegraphStateMachine(final Node parent){
		super();
		switchNode=new StateMachineSwitchNode();
		parent.attachChild(switchNode);
	}
	
	@Override
	public void addState(final State state){
        super.addState(state);
        switchNode.attachChild(((ScenegraphState)state).getRoot());
    }
	
	public final void updateLogicalLayer(final ReadOnlyTimer timer){
		for(int i=0;i<getStateCount();i++)
            if(isEnabled(i))
        		((ScenegraphState)getState(i)).getLogicalLayer().checkTriggers(timer.getTimePerFrame());            
    }  
    
    public final LogicalLayer getLogicalLayer(final int index){
        return(((ScenegraphState)getState(index)).getLogicalLayer());
    }
    
    public final int attachChild(int index,Spatial child){
        return(((ScenegraphState)getState(index)).getRoot().attachChild(child));
    }
    
    public final Runnable getStateInitializationTask(final int index){
    	return(new StateInitializationRunnable(getState(index)));
    }
    
    private static final class StateInitializationRunnable implements Runnable{
        
        
        private final State state;
        
        
        private StateInitializationRunnable(final State state){
            this.state=state;
        }
        
        @Override
        public final void run(){
            state.init();
        }  
    }
}
