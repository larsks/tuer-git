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
package engine;

import java.io.Serializable;
import java.util.Arrays;

import misc.SerializationHelper;

import com.ardor3d.math.Vector3;
import com.ardor3d.math.type.ReadOnlyVector3;
import com.ardor3d.scenegraph.Spatial;
import com.ardor3d.scenegraph.controller.SpatialController;

public abstract class MovementEquationController implements Serializable,SpatialController<Spatial>{
    
    
    static{SerializationHelper.forceHandlingOfTransientModifiersForXMLSerialization(MovementEquationController.class);}
    
    private static final long serialVersionUID=1L;
    
    /**movement equation*/
    private MovementEquation movementEquation;
    
    /**elapsed time in seconds*/
    private transient double elapsedTime;
    
    /**axis of the movement*/
    private double[] axis;
    
    
    public MovementEquationController(){
        this(null,Vector3.ZERO);
    }
    
    public MovementEquationController(final MovementEquation movementEquation,
            final ReadOnlyVector3 axisVector){
        this(movementEquation,Vector3.isValid(axisVector)?new double[]{axisVector.getX(),axisVector.getY(),axisVector.getZ()}:null);
    }
    
    public MovementEquationController(final MovementEquation movementEquation,
            final double[] axis){
        this.movementEquation=movementEquation;
        setAxis(axis);
    }
    

    public final double[] getAxis(){
        return(axis!=null?Arrays.copyOf(axis,axis.length):null);
    }

    public final void setAxis(final double[] axis){
        if(axis!=null)
            {Vector3 axisVector=Vector3.fetchTempInstance();
             axisVector.set(axis[0],axis[1],axis[2]);
             if(Vector3.isValid(axisVector))
                 {axisVector.normalizeLocal();
                  if(this.axis==null)
                      this.axis=new double[3];
                  for(int i=0;i<3;i++)
                      this.axis[i]=axisVector.getValue(i);
                 }
             else
                 this.axis=null;
             Vector3.releaseTempInstance(axisVector);             
            }
        else
            this.axis=null;
    }
    
    public final void reset(){
        elapsedTime=0;
    }
    
    /**
     * applies the movement on the spatial currently executing this controller
     * @param value value of the movement at this time
     * @param caller spatial currently executing this controller
     * */
    protected abstract void apply(final double value,final Spatial caller);
    
    public final MovementEquation getMovementEquation(){
        return(movementEquation);
    }

    public final void setMovementEquation(final MovementEquation movementEquation){
        this.movementEquation=movementEquation;
    }
    
    @Override
    public final void update(final double timeSinceLastCall,final Spatial caller){
        elapsedTime+=timeSinceLastCall;
        if(caller!=null&&movementEquation!=null)
            apply(movementEquation.getValueAtTime(elapsedTime),caller);
    }
}
