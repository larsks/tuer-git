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
package engine.renderer;

import javax.swing.JOptionPane;

import com.ardor3d.framework.Scene;
import com.ardor3d.framework.jogl.JoglCanvasRenderer;
import com.ardor3d.renderer.ContextCapabilities;

/**
 * Canvas renderer enhanced for higher frame rates especially 
 * when the vertical synchronization is turned off by avoiding explicit 
 * OpenGL context operations. It warns the user and quits if he runs 
 * the application with a very bad driver that emulates OpenGL 
 * which is a lot slower than real drivers
 * 
 * @author Julien Gouesse
 *
 */
public class ReliableCanvasRenderer extends JoglCanvasRenderer{

	public ReliableCanvasRenderer(Scene scene){
		super(scene);
	}

	public ReliableCanvasRenderer(Scene scene,boolean useDebug){
		super(scene,useDebug);
	}

	@Override
	public final ContextCapabilities createContextCapabilities(){
		final ContextCapabilities defaultCaps = super.createContextCapabilities();
        final ReliableContextCapabilities realCaps = new ReliableContextCapabilities(defaultCaps);
        //checks if the operating system is Windows
        if(System.getProperty("os.name").toLowerCase().startsWith("windows"))
            {//gets some information about the OpenGL driver
        	 final String vendor=realCaps.getDisplayVendor();
        	 final String renderer=realCaps.getDisplayRenderer();
             //checks whether Microsoft’s generic software emulation driver (OpenGL emulation through Direct3D) is installed
        	 if(vendor!=null&&renderer!=null&&vendor.equalsIgnoreCase("Microsoft Corporation")&&
        		realCaps.getDisplayRenderer().equalsIgnoreCase("GDI Generic"))
                 {//prevents the use of this crap, recommends to the end user to install a proper OpenGL driver
        		  JOptionPane.showMessageDialog(null,
        				    "TUER cannot run with your broken OpenGL driver. To resolve this problem, please download and install the latest version of your graphical card's driver from the your graphical card manufacturer (Nvidia, ATI, Intel).",
        				    "OpenGL driver error",
        				    JOptionPane.ERROR_MESSAGE);
        		  System.exit(0);
                 }
            }
        return(realCaps);
	}
	
	@Override
	public void makeCurrentContext(){}
	
	@Override
	public void releaseCurrentContext(){}
}
