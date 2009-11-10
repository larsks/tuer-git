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
package engine;

import java.util.ArrayList;

import com.ardor3d.input.logical.LogicalLayer;
import com.ardor3d.scenegraph.Node;
import com.ardor3d.scenegraph.Spatial;
import com.ardor3d.scenegraph.extension.SwitchNode;
import com.ardor3d.util.ReadOnlyTimer;


final class StateMachine{
    
    
    private final ArrayList<State> statesList;
    
    private final SwitchNode switchNode;
    
    
    StateMachine(final Node parent){
        statesList=new ArrayList<State>();
        switchNode=new SwitchNode();
        parent.attachChild(switchNode);
    }
    
    
    final void addState(final State state){
        statesList.add(state);
        switchNode.attachChild(state.getRoot());
    }
    
    final void updateLogicalLayer(final ReadOnlyTimer timer){
        int i=0;
        for(State state:statesList)
            {if(isEnabled(i))
                 state.getLogicalLayer().checkTriggers(timer.getTimePerFrame());
             i++;
            }
    }
    
    final void setEnabled(final int index,final boolean enabled){
        statesList.get(index).setEnabled(enabled);
    }
    
    final boolean isEnabled(final int index){
        return(switchNode.getVisible(index));
    }
    
    final LogicalLayer getLogicalLayer(final int index){
        return(statesList.get(index).getLogicalLayer());
    }
    
    final int attachChild(int index,Spatial child){
        return(statesList.get(index).getRoot().attachChild(child));
    }
    
    final Runnable getStateInitializationTask(final int index){
    	return(new StateInitializationRunnable(statesList.get(index)));
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
