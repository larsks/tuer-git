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
    
    final void addState(){
        addState(new State(this));        
    }
    
    final void addState(final State state){
        statesList.add(state);
        switchNode.attachChild(state.root);
    }
    
    final void updateLogicalLayer(final ReadOnlyTimer timer){
        int i=0;
        for(State state:statesList)
            {if(isEnabled(i))
                 state.logicalLayer.checkTriggers(timer.getTimePerFrame());
             i++;
            }
    }
    
    final void setEnabled(int index,boolean enabled){
        switchNode.setVisible(index,enabled);
    }
    
    final boolean isEnabled(int index){
        return(switchNode.getVisible(index));
    }
    
    final LogicalLayer getLogicalLayer(int index){
        return(statesList.get(index).logicalLayer);
    }
    
    final int attachChild(int index,Spatial child){
        return(statesList.get(index).root.attachChild(child));
    }
    
    private final int getStateIndex(State state){      
        return(statesList.indexOf(state));
    }
    
    
    static class State{

        
        /**
         * layer used to handle the input
         */
        protected final LogicalLayer logicalLayer;
        
        /**
         * root node
         */
        protected final Node root;
        
        private final StateMachine stateMachine;
        
        
        protected State(final StateMachine stateMachine){
            this.stateMachine=stateMachine;
            logicalLayer=new LogicalLayer();
            root=new Node();
        }
        
        
        public final boolean isEnabled(){
            final int index=stateMachine.getStateIndex(this);
            return(index==-1?false:stateMachine.switchNode.getVisible(index));
        }
        
        public final void setEnabled(final boolean enabled){
            final int index=stateMachine.getStateIndex(this);
            if(index!=-1)
                stateMachine.switchNode.setVisible(index,enabled);
        }
    }
}
