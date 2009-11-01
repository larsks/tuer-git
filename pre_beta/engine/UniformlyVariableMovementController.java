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
import com.ardor3d.scenegraph.Spatial;
import com.ardor3d.scenegraph.controller.SpatialController;

/**
 * controller that allows spatials to perform a uniformly variable
 * movement, idem est a movement with a constant acceleration and
 * variable speed respecting the following formula:
 * Let t be the elapsed time
 * Let x(t) be the position at the instant t
 * Let v(t) be the speed at the instant t
 * Let a(t) be the acceleration at the instant t
 * 
 * x(t)=1/2*a(0)*t²+v(0)*t+x(0)
 * 
 * @author Julien Gouesse
 *
 */
public abstract class UniformlyVariableMovementController implements Serializable,SpatialController<Spatial>{

    
    static{SerializationHelper.forceHandlingOfTransientModifiersForXMLSerialization(UniformlyVariableMovementController.class);}
    
    private static final long serialVersionUID=1L;
    
    private transient boolean checkNeeded;
    
    /**elapsed time in seconds*/
    private transient double elapsedTime;
    
    /**axis of the movement*/
    private double[] axis;
    
    /**constant acceleration*/
    private double initialAcceleration;
    
    /**initial speed*/
    private double initialSpeed;
    
    /**initial value*/
    private double initialValue;
    
    
    public UniformlyVariableMovementController(){
        this((Vector3)Vector3.ZERO,0,0,0);
    }
    
    public UniformlyVariableMovementController(final Vector3 axisVector,
            final double constantAcceleration,final double initialSpeed,
            final double initialValue){
        this.initialAcceleration=constantAcceleration;
        this.initialSpeed=initialSpeed;
        this.initialValue=initialValue;       
        this.axis=getValidAxis(initialSpeed,axisVector);
        checkNeeded=true;
        check();
    }
    
    
    private final void check(){
        if(checkNeeded)
            {initialSpeed=getValidVariationValue(initialSpeed);
             initialAcceleration=getValidVariationValue(initialAcceleration);
             initialValue=getValidMovementValue(initialValue,initialSpeed);
             Vector3 axisVector;
             if(axis==null)
                 axisVector=null;
             else
                 {axisVector=Vector3.fetchTempInstance();
                  axisVector.set(axis[0],axis[1],axis[2]);
                 }
             axis=getValidAxis(initialSpeed,axisVector);
             if(axisVector!=null)
                 Vector3.releaseTempInstance(axisVector);
             checkNeeded=false;
            }       
    }
    
    private static final double getValidVariationValue(final double value){
        final double validValue;
        if(Double.isNaN(value))
            validValue=0;
        else
            if(Double.isInfinite(value))
                {if(value<0)
                     validValue=-Double.MAX_VALUE;
                 else
                     validValue=Double.MAX_VALUE;
                }
            else
                validValue=value;
        return(validValue);
    }
    
    private static final double getValidMovementValue(final double value,
            final double variationPerSecond){
        final double validValue;
        if(Double.isNaN(variationPerSecond)||Double.isInfinite(variationPerSecond))
            throw new IllegalArgumentException("invalid variation per second");
        if(Double.isNaN(value))
            {if(variationPerSecond<0)
                 validValue=-Double.MAX_VALUE;
             else
                 validValue=Double.MAX_VALUE;
            }
        else
            if(Double.isInfinite(value))
                {if(value<0)
                     validValue=-Double.MAX_VALUE;
                 else
                     validValue=Double.MAX_VALUE;
                }
            else
                validValue=value;
        return(validValue);
    }
    
    private static final double[] getValidAxis(final double speed,final Vector3 axisVector){
        double[] axis;
        if(speed!=0&&Vector3.isValid(axisVector))
            {Vector3 currentAxisVector=Vector3.fetchTempInstance();
             currentAxisVector.set(axisVector).normalizeLocal();
             axis=new double[]{currentAxisVector.getX(),currentAxisVector.getY(),currentAxisVector.getZ()};
             Vector3.releaseTempInstance(currentAxisVector);
            }
        else
            axis=null;
        return(axis);
    }

    public final double getInitialSpeed(){
        check();
        return(initialSpeed);
    }

    public final void setInitialSpeed(final double initialSpeed){
        this.initialSpeed=initialSpeed;
        checkNeeded=true;
    }

    public final double[] getAxis(){
        check();
        return(axis!=null?Arrays.copyOf(axis,axis.length):null);
    }

    public final void setAxis(final double[] axis){
        if(axis!=null)
            {Vector3 axisVector=Vector3.fetchTempInstance();
             axisVector.set(axis[0],axis[1],axis[2]).normalizeLocal();
             if(this.axis==null)
                 this.axis=new double[3];
             for(int i=0;i<3;i++)
                 this.axis[i]=axisVector.getValue(i);
             Vector3.releaseTempInstance(axisVector);             
            }
        else
            this.axis=null;
        checkNeeded=true;
    }

    public final double getInitialValue(){
        check();
        return(initialValue);
    }

    public final void setInitialValue(final double initialValue){
        this.initialValue=initialValue;
        checkNeeded=true;
    }
    
    public final void reset(){
        elapsedTime=0;
    }
    
    /**
     * applies the movement on the spatial currently executing this controller
     * @param caller spatial currently executing this controller
     * @param value value of the movement at this time
     * */
    protected abstract void apply(final Spatial caller,final double value);
    
    /**
     * tells whether a value has a meaning for the movement
     * @param value tested value
     * */
    protected abstract boolean isMeaningfulValue(final double value);
    
    /**
     * returns a meaningful value for this movement. If the value is
     * outside the interval of meaningful values, it attempts to return
     * the closest meaningful value
     * @param value tested value
     * @return closest meaningful value
     * */
    protected abstract double getMeaningfulValue(final double value);
    
    @Override
    public final void update(final double timeSinceLastCall,final Spatial caller){
        elapsedTime+=timeSinceLastCall;            
        if(caller!=null)
            apply(caller,getValue());
    }
    
    public final double getElapsedTime(){
        return(elapsedTime);
    }
    
    public final double getValue(){
        return(getValueAtTime(elapsedTime));
    }
    
    public final double getSpeed(){
        return(getSpeedAtTime(elapsedTime));
    }
    
    public final double getAcceleration(){
        return(getAccelerationAtTime(elapsedTime));
    }
    
    public final double getValueAtTime(final double time){
        check();
        return(getMeaningfulValue((initialAcceleration/2)*(time*time)+(time*initialSpeed)+initialValue));
    }
    
    public final double getSpeedAtTime(final double time){
        check();
        return(initialAcceleration*time+initialSpeed);
    }
    
    public final double getAccelerationAtTime(final double time){
        check();
        return(initialAcceleration);
    }

    public final double getInitialAcceleration(){
        check();
        return(initialAcceleration);
    }

    /**
     * sets the acceleration. It should be used only during the deserialization
     * as the acceleration is expected to be constant
     * @param constantAcceleration
     */
    public final void setInitialAcceleration(final double constantAcceleration){
        this.initialAcceleration=constantAcceleration;
        checkNeeded=true;
    }
}
