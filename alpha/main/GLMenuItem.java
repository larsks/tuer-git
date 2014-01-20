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
package main;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Vector;

import javax.media.opengl.GL;
import javax.media.opengl.GL2;
import javax.media.opengl.GLAutoDrawable;

import com.jogamp.opengl.util.awt.TextRenderer;

//import com.sun.opengl.util.j2d.TextRenderer;

public final class GLMenuItem {
	
    private MenuItemState state;
    
    private Vector<ActionListener> listeners;
    
    private String label;
    
    private Color UNSELECTED_COLOR = new Color(0,0,0,255);
    
    private Color SELECTED_COLOR = new Color(132,132,132,255);
    
    private Color PRESSED_COLOR = new Color(255,0,0,255);
    
    private Color DISABLED_COLOR = new Color(214,214,214,255);
    
    
    public GLMenuItem(){
    	state = MenuItemState.UNSELECTED;
    	listeners = new Vector<ActionListener>();
    	label = "";
    }
    
    public GLMenuItem(String label){
    	state = MenuItemState.UNSELECTED;
    	listeners = new Vector<ActionListener>();
    	if(label != null)
    		this.label = label;
    	else
    		this.label = "";
    }
    
    public void display(GLAutoDrawable drawable,float x,float y,TextRenderer textRenderer){
		GL gl=drawable.getGL();
		gl.getGL2().glPushAttrib(GL2.GL_CURRENT_BIT);		
		textRenderer.beginRendering(drawable.getWidth(),drawable.getHeight());	
		switch(state)
		    {case UNSELECTED:
		         {textRenderer.setColor(UNSELECTED_COLOR); 
                  break;
		         }
		     case SELECTED:
                 {textRenderer.setColor(SELECTED_COLOR); 
                  break;
                 }
		     case PRESSED:
                 {textRenderer.setColor(PRESSED_COLOR); 
                  break;
                 }
		     case DISABLED:
                 {textRenderer.setColor(DISABLED_COLOR); 
                  break;
                 }
		    }
        textRenderer.draw(label,(int)x,(int)y);
        textRenderer.endRendering();
		gl.getGL2().glPopAttrib();
	}
	
	final void processActionEvent(ActionEvent ae){
		for(ActionListener al:listeners)
		    al.actionPerformed(ae);
	}
	
	public final void addActionListener(ActionListener al){
		listeners.add(al);
	}
	
	public final void removeActionListener(ActionListener al){
		listeners.remove(al);
	}
	
	public final boolean isEnabled(){
		return(state != MenuItemState.DISABLED);
	}
	
	public final void setEnabled(boolean enabled){
		if(enabled)
		    {if(state == MenuItemState.DISABLED)
		    	state = MenuItemState.UNSELECTED;
		    }
		else
			state = MenuItemState.DISABLED;
	}
	
	public final boolean isSelected(){
		return(state == MenuItemState.SELECTED || state == MenuItemState.PRESSED);
	}
	
	public final void setSelected(boolean selected){
		if(selected)
		    {if(state == MenuItemState.UNSELECTED || state == MenuItemState.PRESSED)
		    	 state = MenuItemState.SELECTED;
		    }
		else
			{if(state == MenuItemState.SELECTED)
				 state = MenuItemState.UNSELECTED;
			}
	}
	
	public final void setPressed(boolean pressed){
		if(pressed)
		    {if(state == MenuItemState.SELECTED)
		    	state = MenuItemState.PRESSED;
		    }
		else
		    {if(state == MenuItemState.PRESSED)
		    	state = MenuItemState.SELECTED;
		    }
	}
	
	public final String getLabel(){
	    return(label);
	}

}
