/**
 * Copyright (c) 2006-2015 Julien Gouesse
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

import com.ardor3d.input.logical.LogicalLayer;
import com.ardor3d.scenegraph.Node;
import com.ardor3d.scenegraph.extension.SwitchNode;

import engine.sound.SoundManager;

public class ScenegraphState{

	/**flag indicating whether this state is enabled*/
    private boolean enabled;
	/**layer used to handle the input*/
    private LogicalLayer logicalLayer;    
    /**root node*/
    private final Node root;
    /**class used to play some sound samples*/
    private final SoundManager soundManager;
	
    public ScenegraphState(){
    	this(null,null,null);
    }
	
	public ScenegraphState(final SoundManager soundManager){
		this(soundManager,new LogicalLayer(),new Node());
	}
	
	protected ScenegraphState(final SoundManager soundManager,final LogicalLayer logicalLayer,final Node root){
		super();
		this.soundManager=soundManager;
        this.logicalLayer=logicalLayer;
        this.root=root;
	}
	
	public void init(){}
	
	public final boolean isEnabled(){
        return(this.enabled);
    }

	public final Node getRoot(){
        return(root);
    }

	public final LogicalLayer getLogicalLayer(){
        return(logicalLayer);
    }

	public final SoundManager getSoundManager(){
	    return(soundManager);
    }
    
    public void setEnabled(final boolean enabled){
    	if(this.enabled!=enabled)
    	    {this.enabled=enabled;
    	     if(root!=null)
    	         {final SwitchNode switchNode=(SwitchNode)root.getParent();
       	          final int index=switchNode.getChildIndex(root);
                  if(index!=-1)
                	  //shows only this child node
                      switchNode.setVisible(index,enabled);
                  else
                	  //shows no child node
                	  switchNode.setAllNonVisible();
    	         }
    	    }
    }
}
