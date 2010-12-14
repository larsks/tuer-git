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
package engine.taskmanagement;

import java.util.ArrayList;

public final class TaskManager{
    
    
    private static final TaskManager instance=new TaskManager();
    
    private final ArrayList<Runnable> tasksQueue;
     
    
    public TaskManager(){
        tasksQueue=new ArrayList<Runnable>();
    }
    
    
    public final void enqueueTask(final Runnable task){
        tasksQueue.add(task);
    }

    public static final TaskManager getInstance(){
        return(instance);
    }
    
    public final int getTaskCount(){
        return(tasksQueue.size());
    }
    
    public final void executeFirstTask(){
        if(!tasksQueue.isEmpty())
            tasksQueue.remove(0).run();
    }
    
    public final void executeAllTasks(){
        while(getTaskCount()>0)
            executeFirstTask();
    }
}
