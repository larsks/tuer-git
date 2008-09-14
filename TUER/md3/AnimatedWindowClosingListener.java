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

package md3;

import com.sun.opengl.util.Animator;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/**
 *This class allows to exit the program when closing the frame.
 *
 *@author Julien Gouesse
 */

class AnimatedWindowClosingListener extends WindowAdapter{    
    
    
    private Animator animator;
    
    
    AnimatedWindowClosingListener(){
        this.animator=null;
    }
    
    AnimatedWindowClosingListener(Animator animator){
        this.animator=animator;
    }
    
    
    public void windowClosing(WindowEvent we){
        if(animator!=null)
	    animator.stop();
	System.exit(0);
    }   
    
}
