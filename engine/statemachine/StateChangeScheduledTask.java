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
 * Scheduled task whose condition is on a state change. It is not focused on a given 
 * transition but rather on the entry or the exit from a state.
 * 
 * @author Julien Gouesse
 *
 * @param <S> state class
 */
public class StateChangeScheduledTask<S> extends ScheduledTask<S>{
    
    public StateChangeScheduledTask(final S state,final StateChangeType stateChangeType,
            final double timeOffsetInSeconds,final Runnable runnable,final int executionCount){
        super(timeOffsetInSeconds,runnable,executionCount,new StateChangeScheduledTaskCondition<S>(state,stateChangeType));
    }
}
