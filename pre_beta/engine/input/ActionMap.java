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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Set;
import com.ardor3d.input.Key;
import com.ardor3d.input.MouseButton;
import com.ardor3d.input.logical.KeyPressedCondition;
import com.ardor3d.input.logical.KeyReleasedCondition;
import com.ardor3d.input.logical.MouseButtonPressedCondition;
import com.ardor3d.input.logical.MouseButtonReleasedCondition;
import com.ardor3d.input.logical.TriggerConditions;
import com.ardor3d.input.logical.TwoInputStates;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;

public class ActionMap{

	private final HashMap<Action,HashSet<Input>> internalActionMap;
	
	public ActionMap(){
		this(null);
	}
	
	public ActionMap(final ActionMap actionMap){
		internalActionMap=new HashMap<Action,HashSet<Input>>();
		set(actionMap);
	}
	
	public void set(final ActionMap actionMap){
		internalActionMap.clear();
		if(actionMap!=null)
	        {for(Entry<Action,HashSet<Input>> actionInputEntry:actionMap.internalActionMap.entrySet())
	    	     {final Action action=actionInputEntry.getKey();
	    	      final HashSet<Input> inputs=actionInputEntry.getValue();
	    	      final HashSet<Input> inputsCopy=new HashSet<Input>(inputs);
	    	      internalActionMap.put(action,inputsCopy);
	    	     }
	        }
	}
	
	@Override
	public boolean equals(Object o){
		boolean result;
		if(o==null||!(o instanceof ActionMap))
			result=false;
		else
			{result=true;
			 final ActionMap that=(ActionMap)o;
			 for(Entry<Action,HashSet<Input>> actionInputEntry:internalActionMap.entrySet())
			     {final Action action=actionInputEntry.getKey();
			      final HashSet<Input> inputs=actionInputEntry.getValue();
			      final int inputCount=inputs==null?0:inputs.size();
			      final HashSet<Input> thatInputs=that.internalActionMap.get(action);
			      final int thatInputCount=thatInputs==null?0:thatInputs.size();
			      if(inputCount==thatInputCount)
			          {if(inputCount>0)
			               {for(Input input:inputs)
			        	        if(!thatInputs.contains(input))
			        	        	{result=false;
			        	        	 break;
			        	        	}
			                if(!result)
			                	break;
			               }
			          }
			      else
			          {result=false;
			    	   break;
			          }
			     }
			}
		return(result);
	}
	
	protected Predicate<TwoInputStates> getCondition(final Input input,final boolean pressed){
		return(input.getCondition(pressed));
	}
	
	public Predicate<TwoInputStates> getCondition(final Action action,final boolean pressed){
    	final Set<Input> inputs=getInputs(action);
    	final Predicate<TwoInputStates> predicate;
    	if(inputs==null||inputs.isEmpty())
    		{//it should never happen
    		 predicate=TriggerConditions.alwaysFalse();
    		}
    	else
    	    {if(inputs.size()==1)
    	    	 predicate=getCondition(inputs.iterator().next(),pressed);
    	     else
    	    	 {final ArrayList<Predicate<TwoInputStates>> conditions=new ArrayList<Predicate<TwoInputStates>>();
    	    	  for(Input input:inputs)
    	    		  conditions.add(getCondition(input,pressed));
    	    	  predicate=Predicates.or(conditions);
    	    	 }
    	    }
    	return(predicate);
    }
	
	@SuppressWarnings("unchecked")
	public <T extends Input> Set<T> getInputs(final Action... actions){
		final Set<T> inputs;
		if(actions==null||actions.length==0)
			inputs=Collections.<T>emptySet();
		else
		    {final Set<T> tmpInputs=new HashSet<T>();
			 for(Action action:actions)
				 if(action!=null)
					 {for(Input input:internalActionMap.get(action))
					      {T tInput=null;
						   try{tInput=(T)input;}
					       catch(ClassCastException cce){}
						   if(tInput!=null)
							   tmpInputs.add(tInput);
					      }
					 }
		     inputs=Collections.unmodifiableSet(tmpInputs);
		    }
		return(inputs);
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
		
		public abstract Predicate<TwoInputStates> getCondition(final boolean pressed);
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
		
    	@Override
		public Key getInputObject(){
			return(key);
		}
    	
    	@Override
    	public Predicate<TwoInputStates> getCondition(final boolean pressed){
    		return(pressed?new KeyPressedCondition(key):new KeyReleasedCondition(key));
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
		
    	@Override
		public MouseButton getInputObject(){
			return(mouseButton);
		}
		
		@Override
    	public Predicate<TwoInputStates> getCondition(final boolean pressed){
    		return(pressed?new MouseButtonPressedCondition(mouseButton):new MouseButtonReleasedCondition(mouseButton));
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
		
    	@Override
		public Boolean getInputObject(){
			return(mouseWheelUpFlag);
		}
    	
    	@Override
    	public Predicate<TwoInputStates> getCondition(final boolean pressed){
    		return(mouseWheelUpFlag?new MouseWheelMovedUpCondition():new MouseWheelMovedDownCondition());
    	}
		
		@Override
		public String toString(){
			return(mouseWheelUpFlag.equals(Boolean.TRUE)?"mouse wheel up":"mouse wheel down");
		}
	}
}
