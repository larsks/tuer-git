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
import java.awt.Frame;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import javax.media.opengl.GL;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.GLCanvas;
import javax.media.opengl.GLCapabilities;
import javax.media.opengl.GLEventListener;
import javax.media.opengl.glu.GLU;


class MD3Viewer{
   

    public static void main(String[] args){
	Frame frame=new Frame("GOUESSE Julien's MD3 loader"); 
	frame.setIgnoreRepaint(true);       
	frame.setResizable(false);
	GLCapabilities capabilities=new GLCapabilities();
	//enables double buffering
	capabilities.setDoubleBuffered(true);
	//enables hardware acceleration
	capabilities.setHardwareAccelerated(true);	 
	GLCanvas canvas = new GLCanvas(capabilities);
	//prevents any auto buffer swapping
	//canvas.setAutoSwapBufferMode(false);
	Renderer r=new Renderer();
	canvas.addGLEventListener(r);
	canvas.addKeyListener(r);
	frame.add(canvas);
	frame.setSize(640,480);         
	Animator animator=new Animator(canvas);
	animator.start();
	frame.addWindowListener(new AnimatedWindowClosingListener(animator));
	frame.setVisible(true);
	canvas.requestFocus();
    }
}
