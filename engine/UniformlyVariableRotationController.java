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

import java.util.LinkedHashMap;

import misc.SerializationHelper;

import com.ardor3d.math.MathUtils;
import com.ardor3d.math.Matrix3;
import com.ardor3d.math.Vector3;
import com.ardor3d.math.type.ReadOnlyVector3;
import com.ardor3d.scenegraph.Spatial;

public final class UniformlyVariableRotationController extends MovementEquationController{

    
    static{SerializationHelper.forceHandlingOfTransientModifiersForXMLSerialization(UniformlyVariableRotationController.class);}
    
    private static final long serialVersionUID=1L;
    

    public UniformlyVariableRotationController(){
        this(0,0,0,Vector3.ZERO,new LinkedHashMap<Double,Double>());
    }
    
    public UniformlyVariableRotationController(final double constantAcceleration,
            final double initialSpeed,final double initialAngle,
            final ReadOnlyVector3 axisVector,
            final LinkedHashMap<Double,Double> timeWindowsTable){
        super(new UniformlyVariableRotationEquation(constantAcceleration,initialSpeed,initialAngle),axisVector,timeWindowsTable);
    }
    
    
    @Override
    protected final void apply(final double angle,final Spatial caller){
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
}
