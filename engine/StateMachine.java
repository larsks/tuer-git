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
        State state=new State();
        statesList.add(state);
        switchNode.attachChild(state.root);
    }
    
    final void updateLogicalLayer(final ReadOnlyTimer timer){
        for(State state:statesList)
            if(state.enabled)
                state.logicalLayer.checkTriggers(timer.getTimePerFrame());
    }
    
    final void setEnabled(int index,boolean enabled){
        statesList.get(index).enabled=enabled;
        switchNode.setVisible(index,enabled);
    }
    
    final boolean isEnabled(int index){
        return(statesList.get(index).enabled);
    }
    
    final LogicalLayer getLogicalLayer(int index){
        return(statesList.get(index).logicalLayer);
    }
    
    final int attachChild(int index,Spatial child){
        return(statesList.get(index).root.attachChild(child));
    }
    
    private static final class State{

        
        /**
         * layer used to handle the input
         */
        private final LogicalLayer logicalLayer;
        
        /**
         * root node
         */
        private final Node root;
        
        private boolean enabled;
        
        
        State(){
            logicalLayer=new LogicalLayer();
            root=new Node();
            enabled=false;
        }
    }
}
