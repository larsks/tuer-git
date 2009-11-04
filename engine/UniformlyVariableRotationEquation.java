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

/**
 * equation of a uniformly variable rotation in degrees
 * @author Julien Gouesse
 *
 */
public final class UniformlyVariableRotationEquation extends UniformlyVariableMovementEquation{

    
    static{SerializationHelper.forceHandlingOfTransientModifiersForXMLSerialization(UniformlyVariableRotationEquation.class);}
    
    private static final long serialVersionUID=1L;

    
    public UniformlyVariableRotationEquation(){
        this(0,0,0);
    }

    public UniformlyVariableRotationEquation(double initialAcceleration,
            double initialSpeed, double initialValue) {
        super(initialAcceleration,initialSpeed,initialValue);
    }

    
    @Override
    protected final double validateValue(final double value){
        double validValue=validateNumber(value);
        if(Math.abs(validValue)>180)
            validValue-=(validValue>0?1:-1)*(Math.floor((validValue-180)/360)+1)*360;
        return(validValue);
    }
}
