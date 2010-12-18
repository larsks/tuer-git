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
package engine.statemachine;

import java.util.ArrayList;


public class StateMachine{
    
    
    private final ArrayList<State> statesList;
    
    
    public StateMachine(){
        statesList=new ArrayList<State>();
    }
    
    
    public void addState(final State state){
        statesList.add(state);
    }
    
    public final void setEnabled(final int index,final boolean enabled){
        statesList.get(index).setEnabled(enabled);
        /*for(State state:statesList)
        	if(statesList.indexOf(state)!=index && state.isEnabled()==enabled)
                state.setEnabled(!enabled);*/
    }
    
    public final boolean isEnabled(final int index){
        return(0<=index&&index<statesList.size()?statesList.get(index).isEnabled():false);
    }
    
    protected final int getStateCount(){
    	return(statesList.size());
    }
    
    protected final State getState(final int index){
    	return(0<=index&&index<statesList.size()?statesList.get(index):null);
    }
}
