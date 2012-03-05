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

import se.hiflyer.fettle.Arguments;
import se.hiflyer.fettle.Condition;
import engine.taskmanagement.TaskManager;

/**
 * Condition satisfied when the task manager has no pending task.
 * 
 * @author Julien Gouesse
 *
 */
public class NoPendingTaskCondition implements Condition {

    protected final TaskManager taskManager;

    public NoPendingTaskCondition(TaskManager taskManager){
        this.taskManager=taskManager;
    }

    @Override
    public boolean isSatisfied(Arguments args){
        return taskManager.getTaskCount()==0;
    }
    
}