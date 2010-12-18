package engine.statemachine;

import com.ardor3d.input.logical.LogicalLayer;
import com.ardor3d.scenegraph.Node;
import com.ardor3d.scenegraph.extension.SwitchNode;

import engine.sound.SoundManager;

public class ScenegraphState extends State{

	/**layer used to handle the input*/
    private LogicalLayer logicalLayer;    
    /**root node*/
    private final Node root;
    /**class used to play some sound samples*/
    private final SoundManager soundManager;
	
	
	public ScenegraphState(final SoundManager soundManager){
		super();
		this.soundManager=soundManager;
        this.logicalLayer=new LogicalLayer();
        root=new Node();
	}

    final Node getRoot(){
        return(root);
    }

    final LogicalLayer getLogicalLayer(){
        return(logicalLayer);
    }

    final SoundManager getSoundManager(){
	    return(soundManager);
    }
    
    @Override
    public void setEnabled(final boolean enabled){
    	final boolean wasEnabled=isEnabled();
    	super.setEnabled(enabled);
    	if(wasEnabled!=isEnabled())
    	    {final SwitchNode switchNode=(SwitchNode)root.getParent();
       	     final int index=switchNode.getChildIndex(root);
             if(index!=-1)                
                 switchNode.setVisible(index,enabled);   		 
    	    }
    }
}
