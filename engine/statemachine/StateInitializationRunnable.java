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
 * Runnable that runs the init() method of a state. N.B: the caller should use this runnable
 * when the OpenGL context is current.
 * 
 * @author Julien Gouesse
 *
 */
public class StateInitializationRunnable implements Runnable{


    protected final State state;


    public StateInitializationRunnable(final State state){
        this.state=state;
    }

    @Override
    public final void run(){
        state.init();
    }  
}