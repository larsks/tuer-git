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
package engine.movement;

import java.util.LinkedHashMap;

import misc.SerializationHelper;
import com.ardor3d.math.Vector3;
import com.ardor3d.math.type.ReadOnlyVector3;
import com.ardor3d.scenegraph.Spatial;

public final class UniformlyVariableRectilinearTranslationController extends MovementEquationController{

    
    static{SerializationHelper.forceHandlingOfTransientModifiersForXMLSerialization(UniformlyVariableRectilinearTranslationController.class);}
    
    private static final long serialVersionUID=1L;

    
    public UniformlyVariableRectilinearTranslationController(){
        this(0,0,0,Vector3.ZERO,new LinkedHashMap<Double,Double>());
    }

    public UniformlyVariableRectilinearTranslationController(final double constantAcceleration,
            final double initialSpeed,final double initialTranslationFactor,
            final ReadOnlyVector3 axisVector,final LinkedHashMap<Double,Double> timeWindowsTable){
        super(new UniformlyVariableMovementEquation(constantAcceleration,initialSpeed,initialTranslationFactor),axisVector,timeWindowsTable);
    }

    
    @Override
    protected final void apply(final double translationFactor,final Spatial caller){
        if(getAxis()!=null)
            {Vector3 axisVector=Vector3.fetchTempInstance();
             axisVector.set(getAxis()[0],getAxis()[1],getAxis()[2]);
             if(Vector3.isValid(axisVector))
                 caller.setTranslation(axisVector.multiply(translationFactor,null));
             Vector3.releaseTempInstance(axisVector);
            }
    }
}
