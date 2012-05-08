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
 * Task scheduled to be executed at least one time immediately or after an optional delay when a condition gets satisfied
 * 
 * @author Julien Gouesse
 *
 */
public class ScheduledTask<S>{

    protected final double timeOffsetInSeconds;
    
    protected final Runnable runnable;
    
    protected final int executionCount;
    
    protected final ScheduledTaskCondition<S> condition;
    
    public ScheduledTask(final double timeOffsetInSeconds,final Runnable runnable,
    		final int executionCount,final ScheduledTaskCondition<S> condition){
    	if(timeOffsetInSeconds<0)
            throw new IllegalArgumentException("The time offset must be positive");
        this.timeOffsetInSeconds=timeOffsetInSeconds;
        this.runnable=runnable;
        if(executionCount<=0)
            throw new IllegalArgumentException("The execution count must be strictly positive");
        this.executionCount=executionCount;
        if(condition==null)
        	throw new IllegalArgumentException("The condition must not be null");
        this.condition=condition;
    }
    
    public boolean isSatisfied(S previousState,S currentState){
    	return(condition.isSatisfied(previousState,currentState));
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
