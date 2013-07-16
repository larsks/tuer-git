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

import com.ardor3d.input.logical.LogicalLayer;
import com.ardor3d.math.Vector3;
import com.ardor3d.renderer.Camera;
import com.ardor3d.renderer.ContextManager;
import com.ardor3d.scenegraph.Node;

import engine.sound.SoundManager;

/**
 * Scenegraph state that modifies the parameters of the camera at runtime, especially when entering and exiting this state. It sets them when entering
 * and it resets them to their previous values when exiting.
 * 
 * @author Julien Gouesse
 *
 */
public class ScenegraphStateWithCustomCameraParameters extends ScenegraphState {

	/**previous (before entering this state) frustum near value*/
    private double previousFrustumNear;
    /**previous (before entering this state) frustum far value*/
    private double previousFrustumFar;
    /**previous (before entering this state) orthonormal basis*/
    private final Vector3 previousCamLeft,previousCamUp,previousCamDirection;
    /**previous (before entering this state) location of the camera*/
    private final Vector3 previousCamLocation;
    /**current (when entering and before exiting) frustum near value*/
    protected double currentFrustumNear;
    /**current (when entering and before exiting) frustum far value*/
    protected double currentFrustumFar;
    /**current (when entering and before exiting) orthonormal basis*/
    protected final Vector3 currentCamLeft,currentCamUp,currentCamDirection;
    /**current (when entering and before exiting) location of the camera*/
    protected final Vector3 currentCamLocation;

	public ScenegraphStateWithCustomCameraParameters(final SoundManager soundManager,final LogicalLayer logicalLayer,final Node root,final Camera cam){
		super(soundManager,logicalLayer,root);
		this.previousFrustumNear=cam.getFrustumNear();
        this.previousFrustumFar=cam.getFrustumFar();
        this.currentFrustumNear=this.previousFrustumNear;
        this.currentFrustumFar=this.previousFrustumFar;
        this.previousCamLeft=new Vector3(cam.getLeft());
        this.previousCamUp=new Vector3(cam.getUp());
        this.previousCamDirection=new Vector3(cam.getDirection());
        this.previousCamLocation=new Vector3(cam.getLocation());
        this.currentCamLeft=new Vector3(previousCamLeft);
        this.currentCamUp=new Vector3(previousCamUp);
        this.currentCamDirection=new Vector3(previousCamDirection);
        this.currentCamLocation=new Vector3(previousCamLocation);
	}

	
	public void setEnabled(final boolean enabled){
		final boolean wasEnabled=isEnabled();
        super.setEnabled(enabled);
        if(wasEnabled!=isEnabled())
            {final Camera cam=ContextManager.getCurrentContext().getCurrentCamera();
        	 if(isEnabled())
        	     {previousFrustumNear=cam.getFrustumNear();
                  previousFrustumFar=cam.getFrustumFar();
                  previousCamLeft.set(cam.getLeft());
                  previousCamUp.set(cam.getUp());
                  previousCamDirection.set(cam.getDirection());
                  previousCamLocation.set(cam.getLocation());
                  cam.setFrustumPerspective(cam.getFovY(),(float)cam.getWidth()/(float)cam.getHeight(),currentFrustumNear,currentFrustumFar);
                  cam.setLeft(currentCamLeft);
                  cam.setUp(currentCamUp);
                  cam.setDirection(currentCamDirection);
                  cam.setLocation(currentCamLocation);
        	     }
        	 else
        	     {currentFrustumNear=cam.getFrustumNear();
                  currentFrustumFar=cam.getFrustumFar();
           	      currentCamLeft.set(cam.getLeft());
                  currentCamUp.set(cam.getUp());
                  currentCamDirection.set(cam.getDirection());
           	      currentCamLocation.set(cam.getLocation());
                  cam.setFrustumPerspective(cam.getFovY(),(float)cam.getWidth()/(float)cam.getHeight(),previousFrustumNear,previousFrustumFar);
                  cam.setLeft(previousCamLeft);
                  cam.setUp(previousCamUp);
                  cam.setDirection(previousCamDirection);
                  cam.setLocation(previousCamLocation);
        	     }
            }
	}
}
