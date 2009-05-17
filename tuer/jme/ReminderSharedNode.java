package jme;

import com.jme.scene.Node;
import com.jme.scene.SharedNode;

final class ReminderSharedNode extends SharedNode{

    
    private static final long serialVersionUID = 1L;
    
    private transient Node target;

    ReminderSharedNode(){}
    
    ReminderSharedNode(Node target){
        this(target.getName(),target);
    }
    
    ReminderSharedNode(String name,Node target){
        super(name,target);
        this.target=target;
    }
    
    Node getTarget(){
        return(target);
    }
}
