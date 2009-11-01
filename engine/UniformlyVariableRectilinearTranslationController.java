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
import com.ardor3d.math.Vector3;
import com.ardor3d.scenegraph.Spatial;

public final class UniformlyVariableRectilinearTranslationController extends UniformlyVariableMovementController {

    
    static{SerializationHelper.forceHandlingOfTransientModifiersForXMLSerialization(UniformlyVariableRectilinearTranslationController.class);}
    
    private static final long serialVersionUID=1L;

    
    public UniformlyVariableRectilinearTranslationController(){
        super();
    }

    public UniformlyVariableRectilinearTranslationController(final Vector3 axisVector,
            final double constantAcceleration,final double initialSpeed,
            final double initialTranslationFactor){
        super(axisVector,constantAcceleration,initialSpeed,initialTranslationFactor);
    }

    @Override
    protected final void apply(final Spatial caller,final double translationFactor){
        if(getAxis()!=null)
            {Vector3 axisVector=Vector3.fetchTempInstance();
             axisVector.set(getAxis()[0],getAxis()[1],getAxis()[2]);
             if(Vector3.isValid(axisVector))
                 caller.setTranslation(axisVector.multiply(translationFactor,null));
             Vector3.releaseTempInstance(axisVector);
            }
    }

    @Override
    protected final double getMeaningfulValue(double value){
        final double meaningfulValue;
        if(Double.isNaN(value))
            meaningfulValue=0;
        else
            if(Double.isInfinite(value))
                {if(value<0)
                     meaningfulValue=-Double.MAX_VALUE;
                 else
                     meaningfulValue=Double.MAX_VALUE;
                }
            else
                meaningfulValue=value;
        return(meaningfulValue);
    }

    @Override
    protected final boolean isMeaningfulValue(double value){
        return(!Double.isNaN(value)&&!Double.isInfinite(value));
    }

}
