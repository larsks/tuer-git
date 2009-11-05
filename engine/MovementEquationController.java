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
import java.util.LinkedHashMap;
import java.util.Map.Entry;
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
    
    /**elapsed time in seconds outside all time windows*/
    private transient double inertialTime;
    
    /**axis of the movement*/
    private double[] axis;
    
    /**table containing time windows*/
    private LinkedHashMap<Double,Double> timeWindowsTable;
    
    
    public MovementEquationController(){
        this(null,Vector3.ZERO,new LinkedHashMap<Double,Double>());
    }
    
    public MovementEquationController(final MovementEquation movementEquation,final ReadOnlyVector3 axisVector,final LinkedHashMap<Double,Double> timeWindowsTable){
        this(movementEquation,Vector3.isValid(axisVector)?new double[]{axisVector.getX(),axisVector.getY(),axisVector.getZ()}:null,timeWindowsTable);
    }
    
    public MovementEquationController(final MovementEquation movementEquation,final double[] axis,final LinkedHashMap<Double,Double> timeWindowsTable){
        this.movementEquation=movementEquation;
        this.elapsedTime=0;
        this.inertialTime=0;
        setTimeWindowsTable(timeWindowsTable);
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
        inertialTime=0;
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
        final double previousElapsedTime=elapsedTime;
        elapsedTime+=timeSinceLastCall;
        if(caller!=null&&movementEquation!=null)
            {double startTime,endTime;
             double activeElapsedTime=0;
             if(timeWindowsTable!=null)
                 for(Entry<Double,Double> entry:timeWindowsTable.entrySet())
                     {startTime=entry.getKey().doubleValue();
                      endTime=entry.getValue().doubleValue();
                      if(startTime<=elapsedTime&&previousElapsedTime<=endTime)
                          activeElapsedTime+=Math.min(endTime,elapsedTime)-Math.max(startTime,previousElapsedTime);                      
                     }
             inertialTime+=timeSinceLastCall-activeElapsedTime;
             apply(movementEquation.getValueAtTime(elapsedTime-inertialTime),caller);
            }
    }

    public final LinkedHashMap<Double,Double> getTimeWindowsTable(){
        return(timeWindowsTable);
    }

    public final void setTimeWindowsTable(final LinkedHashMap<Double,Double> timeWindowsTable){
        this.timeWindowsTable=timeWindowsTable;
        if(timeWindowsTable!=null)
            {//check if all intervals are valid
             double startTime,endTime;
             for(Entry<Double,Double> entry:timeWindowsTable.entrySet())
                 if(entry.getKey()!=null&&entry.getValue()!=null)
                     {startTime=entry.getKey().doubleValue();
                      endTime=entry.getValue().doubleValue();
                      if(startTime>endTime)
                          throw new IllegalArgumentException("The start time cannot be greater than the end time!");
                     }
                 else
                     throw new IllegalArgumentException("A table of time windows cannot contain null values!");
            }
    }
}
