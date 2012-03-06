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

/**
 * Task scheduled to be executed some time after a state change. It is not focused on a given 
 * transition but rather on the entry or the exit from a state.
 * 
 * @author Julien Gouesse
 *
 */
public class StateChangeScheduledTask<S> {

    private final S state;

    private final StateChangeType stateChangeType;
    
    private final double timeOffsetInSeconds;
    
    private final Runnable runnable;
    
    private final int executionCount;
    
    public StateChangeScheduledTask(final S state,final StateChangeType stateChangeType,
            final double timeOffsetInSeconds,final Runnable runnable,final int executionCount){
        this.state=state;
        this.stateChangeType=stateChangeType;
        if(timeOffsetInSeconds<0)
            throw new IllegalArgumentException("The time offset must be positive");
        this.timeOffsetInSeconds=timeOffsetInSeconds;
        this.runnable=runnable;
        if(executionCount<=0)
            throw new IllegalArgumentException("The execution count must be strictly positive");
        this.executionCount=executionCount;
    }
    
    public S getState(){
        return(state);
    }

    public StateChangeType getStateChangeType(){
        return(stateChangeType);
    }

    public double getTimeOffsetInSeconds(){
        return(timeOffsetInSeconds);
    }
    
    public Runnable getRunnable(){
        return(runnable);
    }
    
    public int getExecutionCount(){
        return(executionCount);
    }
}
