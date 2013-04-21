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
package engine.input;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Set;

import com.ardor3d.input.Key;
import com.ardor3d.input.MouseButton;

public class ActionMap{

	private final HashMap<Action,HashSet<Input>> internalActionMap;
	
	public ActionMap(){
		this(null);
	}
	
	public ActionMap(final ActionMap actionMap){
		internalActionMap=new HashMap<Action,HashSet<Input>>();
		if(actionMap!=null)
		    {for(Entry<Action,HashSet<Input>> actionInputEntry:actionMap.internalActionMap.entrySet())
		    	 {final Action action=actionInputEntry.getKey();
		    	  final HashSet<Input> inputs=actionInputEntry.getValue();
		    	  final HashSet<Input> inputsCopy=new HashSet<Input>(inputs);
		    	  internalActionMap.put(action,inputsCopy);
		    	 }
		    }
	}
	
	public Set<Input> getInputs(final Action action){
		return(Collections.unmodifiableSet(internalActionMap.get(action)));
	}
	
	public void setKeyActionBinding(final Action action,final Key key){
		prepareInputActionBinding(action,key);
		final Input input=new KeyInput(key);
		doInputActionBinding(action,input);
	}
	
	public void setMouseButtonActionBinding(final Action action,final MouseButton mouseButton){
		prepareInputActionBinding(action,mouseButton);
		final Input input=new MouseButtonInput(mouseButton);
		doInputActionBinding(action,input);
	}
	
	public void setMouseWheelMoveActionBinding(final Action action,final Boolean wheelUpFlag){
		prepareInputActionBinding(action,wheelUpFlag);
		final Input input=new MouseWheelMoveInput(wheelUpFlag);
		doInputActionBinding(action,input);
	}
	
	protected void prepareInputActionBinding(final Action action,final Object inputObject){
		if(action==null)
			throw new IllegalArgumentException("The action cannot be null");
		if(inputObject==null)
			throw new IllegalArgumentException("The input object cannot be null");
		if(!internalActionMap.containsKey(action))
		    internalActionMap.put(action,new HashSet<Input>());
	}
	
	protected void doInputActionBinding(final Action action,final Input input){
		final HashSet<Input> inputs=internalActionMap.get(action);
		if(!inputs.contains(input))
		    {//removes this input from existing sets of inputs
		     for(Entry<Action,HashSet<Input>> actionInputEntry:internalActionMap.entrySet())
			     actionInputEntry.getValue().remove(input);
		     //adds this input into the set of inputs for this action
		     inputs.add(input);
		    }
	}
	
	public static abstract class Input{
		
		protected Input(Object o){
			if(o==null)
				throw new IllegalArgumentException("The object input cannot be null");
		}
		
		public abstract Object getInputObject();
	}
	
    public static class KeyInput extends Input{
		
    	private final Key key;
    	
    	public KeyInput(final Key key){
    		super(key);
    		this.key=key;
    	}
    	
    	@Override
		public boolean equals(Object o){
			final boolean result;
			if(o==null||!(o instanceof KeyInput))
				result=false;
			else
				result=key.equals(((KeyInput)o).getInputObject());
			return(result);
		}
		
		public Key getInputObject(){
			return(key);
		}
		
		@Override
		public String toString(){
			return("key "+key.name().toUpperCase());
		}
	}
	
    public static class MouseButtonInput extends Input{
		
    	private final MouseButton mouseButton;
    	
    	public MouseButtonInput(final MouseButton mouseButton){
    		super(mouseButton);
    		this.mouseButton=mouseButton;
    	}
    	
    	@Override
		public boolean equals(Object o){
			final boolean result;
			if(o==null||!(o instanceof MouseButtonInput))
				result=false;
			else
				result=mouseButton.equals(((MouseButtonInput)o).getInputObject());
			return(result);
		}
		
		public MouseButton getInputObject(){
			return(mouseButton);
		}
		
		@Override
		public String toString(){
			return(mouseButton.name().toLowerCase()+" mouse button");
		}
	}
    
    public static class MouseWheelMoveInput extends Input{
		
    	private final Boolean mouseWheelUpFlag;
    	
    	public MouseWheelMoveInput(final Boolean mouseWheelUpFlag){
    		super(mouseWheelUpFlag);
    		this.mouseWheelUpFlag=mouseWheelUpFlag;
    	}
    	
    	@Override
		public boolean equals(Object o){
			final boolean result;
			if(o==null||!(o instanceof MouseWheelMoveInput))
				result=false;
			else
				result=mouseWheelUpFlag.equals(((MouseWheelMoveInput)o).getInputObject());
			return(result);
		}
		
		public Boolean getInputObject(){
			return(mouseWheelUpFlag);
		}
		
		@Override
		public String toString(){
			return(mouseWheelUpFlag.equals(Boolean.TRUE)?"mouse wheel up":"mouse wheel down");
		}
	}
}
