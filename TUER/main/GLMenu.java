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

import java.awt.event.ActionEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.Vector;

import javax.media.opengl.GLAutoDrawable;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

//import com.sun.opengl.util.j2d.TextRenderer;

public final class GLMenu extends KeyAdapter {
	
	private final static int NO_ITEM_INDEX = -1;
	
	private boolean visible;
	
	private Vector<GLMenuItem> itemList;
	
	private Vector<ListSelectionListener> listSelectionListenerList;
	
	private float x;
	
	private float y;
	
	private TextRenderer textRenderer;
	
	private boolean emitEvents=true;
	
	private static final int HORIZONTAL_GAP=0;
	
	private static final int VERTICAL_GAP=10;
	
	
	public GLMenu(TextRenderer textRenderer){
		this(0.0f,0.0f,true,textRenderer);
	}
	
	public GLMenu(float x,float y,boolean visible,TextRenderer textRenderer){
		this.x=x;
		this.y=y;
		this.visible = visible;
		this.listSelectionListenerList=new Vector<ListSelectionListener>();
		this.itemList = new Vector<GLMenuItem>();
		this.textRenderer=textRenderer;
	}
	
	public final void addGLMenuItem(GLMenuItem glMenuItem){
	    if(getSelectedIndex() == NO_ITEM_INDEX)
	        {if(glMenuItem.isEnabled() && !glMenuItem.isSelected())
	             {emitEvents=false;
	              glMenuItem.setSelected(true);
	              emitEvents=true;
	             }
	        }
	    else
	        if(glMenuItem.isSelected())
               glMenuItem.setSelected(false);
		itemList.add(glMenuItem);
	}
	
	final void processActionEvent(ActionEvent ae){
		((GLMenuItem)ae.getSource()).processActionEvent(ae);
	}
	
	public final void keyPressed(KeyEvent ke){
	    if(visible)
		    switch(ke.getKeyCode())
		        {case(KeyEvent.VK_UP):
		             {int index = getSelectedIndex();
			          if(index != NO_ITEM_INDEX)
			              {int previouslySelectedIndex=index,futureSelectedIndex=getPreviousUnselectedEnabledIndexFrom(index);
			               if(previouslySelectedIndex!=futureSelectedIndex && futureSelectedIndex!=NO_ITEM_INDEX)
			                   {setUnselectedIndex(previouslySelectedIndex);
			                    setSelectedIndex(futureSelectedIndex);
			                   }			               		               
			              }
			          break;
		             }
		         case(KeyEvent.VK_DOWN):
		             {int index = getSelectedIndex();
		              if(index != NO_ITEM_INDEX)
		                  {int previouslySelectedIndex=index,futureSelectedIndex=getNextUnselectedEnabledIndexFrom(index);
		                   if(previouslySelectedIndex!=futureSelectedIndex && futureSelectedIndex!=NO_ITEM_INDEX)
		                       {setUnselectedIndex(previouslySelectedIndex);
		                        setSelectedIndex(futureSelectedIndex);
		                       }	                   	                             
		                  }
		              break;
	                 }
		         case(KeyEvent.VK_ENTER):
		             {int index = getSelectedIndex();
		              if(index != NO_ITEM_INDEX)	          
			              {GLMenuItem selectedItem = getItem(index);
			               selectedItem.setPressed(true);
			               processActionEvent(new ActionEvent(selectedItem,ActionEvent.ACTION_PERFORMED,""));
			              }
			          break;
		             }
		}
	}
	
	public final void keyReleased(KeyEvent ke){
	    if(visible)
	        switch(ke.getKeyCode())
	            {case(KeyEvent.VK_ENTER):
	                 {int index = getSelectedIndex();
	                  if(index != NO_ITEM_INDEX)	       
	                      getItem(index).setPressed(false);
	                  break;
	                 }
	            }
	}
	
	public final void display(GLAutoDrawable drawable){
	    if(visible)
	        {float ix=x;
	         float iy=y;
	         for(GLMenuItem item:itemList)
	             {item.display(drawable,ix,iy,textRenderer);
	              ix+=HORIZONTAL_GAP;
	              iy-=VERTICAL_GAP;
	              iy-=(float) textRenderer.getBounds(item.getLabel()).getHeight();
	             }
	         //unpress the pressed item
	         getSelectedItem().setSelected(true);
	        }
	}
	
	public final void setUnselectedIndex(int index){
		getItem(index).setSelected(false);
	}
	
	public final void setSelectedIndex(int index){    
	    int previouslySelectedIndex = getSelectedIndex();
	    if(index != previouslySelectedIndex)
	        {//unselect the previously selected index
	         if(previouslySelectedIndex != NO_ITEM_INDEX)
	            setUnselectedIndex(previouslySelectedIndex);
	         getItem(index).setSelected(true);
	         if(emitEvents)
	             fireValueChanged(index,index,false);
	        }    	
	}
	
	private final void fireValueChanged(int firstIndex,int lastIndex,boolean isAdjusting){      
        processListSelectionEvent(new ListSelectionEvent(this,firstIndex,lastIndex,isAdjusting));
    }

    private final void processListSelectionEvent(ListSelectionEvent event){      
        for(ListSelectionListener listener:listSelectionListenerList)
            listener.valueChanged(event);
    }
    
    public final void addListSelectionListener(ListSelectionListener listener){
        listSelectionListenerList.add(listener);
    }
    
    public final void removeListSelectionListener(ListSelectionListener listener){
        listSelectionListenerList.remove(listener);
    }

    public final int getSelectedIndex(){
		int index = NO_ITEM_INDEX;
		int i = 0;
		for(GLMenuItem item:itemList)
		    if(item.isSelected())
		        {index=i;
		         break;
		        }
		    else
		    	i++;
		return(index);
	}
	
	public final void setEnabledIndex(int index,boolean isEnabled){
	    if(isEnabled)
	        getItem(index).setEnabled(true);
	    else
	        {getItem(index).setEnabled(false);
	         int futureSelectedIndex=getNextUnselectedEnabledIndexFrom(index);
	         if(futureSelectedIndex!=NO_ITEM_INDEX)
	             {emitEvents=false;
	              setSelectedIndex(futureSelectedIndex);
	              emitEvents=true;
	             }
	        }	    
	}
	
	public final int getItemCount(){
		return(itemList.size());
	}
	
	public final GLMenuItem getItem(int index){
		return(itemList.get(index));
	}
	
	public final GLMenuItem getSelectedItem(){
		int index = getSelectedIndex();
		if(index == NO_ITEM_INDEX)
			return(null);
		else
			return(getItem(index));
	}
	
	private final int getNextUnselectedEnabledIndexFrom(int index){
	    GLMenuItem item;
	    for(int i=(index+1)%getItemCount();i!=index;i=(i+1)%getItemCount())
	        if((item=getItem(i)).isEnabled() && !item.isSelected())
	            return(i);
	    return(NO_ITEM_INDEX);
	}
	
	private final int getPreviousUnselectedEnabledIndexFrom(int index){
        GLMenuItem item;
        int i;
        if(index==0)
            i=getItemCount()-1;
        else
            i=index-1;
        while(i!=index)
            if((item=getItem(i)).isEnabled() && !item.isSelected())
                return(i);
            else
                if(i==0)
                    i=getItemCount()-1;
                else
                    i--;
        return(NO_ITEM_INDEX);
    }
	
	public final boolean isVisible(){
		return(visible);
	}
    
	public final void setVisible(boolean visible){
		this.visible = visible;
		if(!this.visible)
		    //unpress the latest pressed item
		    getSelectedItem().setSelected(true);
	}
}
