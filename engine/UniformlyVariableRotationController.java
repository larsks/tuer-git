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

import misc.SerializationHelper;

import com.ardor3d.math.MathUtils;
import com.ardor3d.math.Matrix3;
import com.ardor3d.math.Vector3;
import com.ardor3d.scenegraph.Spatial;

public final class UniformlyVariableRotationController extends UniformlyVariableMovementController{

    
    static{SerializationHelper.forceHandlingOfTransientModifiersForXMLSerialization(UniformlyVariableRotationController.class);}
    
    private static final long serialVersionUID=1L;
    

    public UniformlyVariableRotationController(){
        super();
    }
    
    public UniformlyVariableRotationController(final Vector3 axisVector,
            final double constantAcceleration,final double initialSpeed,
            final double initialAngle,final double terminalAngle){
        super(axisVector,constantAcceleration,initialSpeed,initialAngle,terminalAngle);
    }
    
    
    @Override
    protected final void apply(final Spatial caller,final double angle){
        if(getAxis()!=null)
            {Vector3 axisVector=Vector3.fetchTempInstance();
             axisVector.set(getAxis()[0],getAxis()[1],getAxis()[2]);             
             if(Vector3.isValid(axisVector))
                 {Matrix3 rotationMatrix=Matrix3.fetchTempInstance();
                  rotationMatrix.fromAngleNormalAxis(angle*MathUtils.DEG_TO_RAD,axisVector);             
                  caller.setRotation(rotationMatrix);
                  Matrix3.releaseTempInstance(rotationMatrix);
                 }
             Vector3.releaseTempInstance(axisVector);            
            }        
    }

    @Override
    protected final double getMeaningfulValue(final double angle){
        final double meaningfulValue;
        if(Double.isNaN(angle))
            meaningfulValue=0;
        else
            if(Double.isInfinite(angle))
                {if(angle<0)
                     meaningfulValue=-180;
                 else
                     meaningfulValue=180;
                }
            else
                if(-180<=angle&&angle<=180)
                    meaningfulValue=angle;
                else
                    if(angle>180)
                        meaningfulValue=angle-Math.round((float)Math.floor((angle-180)/360))*360;
                    else
                        meaningfulValue=angle+Math.round((float)Math.floor((angle+180)/-360))*360;       
        return(meaningfulValue);
    }

    @Override
    protected final boolean isMeaningfulValue(final double angle){
        return(!Double.isNaN(angle)&&!Double.isInfinite(angle)&&-180<=angle&&angle<=180);
    }

}
