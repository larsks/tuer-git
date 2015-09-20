/**
 * Copyright (c) 2006-2015 Julien Gouesse
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package engine.movement;

public class UniformlyVariableMovementEquation implements MovementEquation{

    
    private static final long serialVersionUID=1L;
    
    /**initial acceleration*/
    private double initialAcceleration;
    
    /**initial speed*/
    private double initialSpeed;
    
    /**initial value*/
    private double initialValue;

    
    public UniformlyVariableMovementEquation(){
        this(0,0,0);
    }
    
    public UniformlyVariableMovementEquation(final double initialAcceleration,
            final double initialSpeed,final double initialValue){
        this.initialAcceleration=validateNumber(initialAcceleration);
        this.initialSpeed=validateNumber(initialSpeed);
        this.initialValue=validateNumber(initialValue);
    }
    
    /**
     * Replaces infinite and "not a number" number
     * by a valid finite number 
     * @param number ordinary number
     * @return valid finite value
     */
    protected final double validateNumber(final double number){
        final double validNumber;
        if(Double.isNaN(number))
            validNumber=0;
        else
            if(Double.isInfinite(number))
                {if(number<0)
                     validNumber=-Double.MAX_VALUE;
                 else
                     validNumber=Double.MAX_VALUE;
                }
            else
                validNumber=number;
        return(validNumber);
    }
    
    /**
     * Restricts a value to a user-defined sub-interval
     * 
     * @param value ordinary value (can be infinite or NaN)
     * @return valid finite value in a user-defined sub-interval
     */
    protected double validateValue(final double value){
        return(validateNumber(value));
    }
    
    @Override
    public final double getValueAtTime(final double elapsedTime){        
        return(validateNumber(validateValue((initialAcceleration/2)*(elapsedTime*elapsedTime)+(elapsedTime*initialSpeed)+initialValue)));
    }
    
    public final double getSpeedAtTime(final double elapsedTime){
        return(validateNumber(initialAcceleration*elapsedTime+initialSpeed));
    }
    
    public final double getAccelerationAtTime(final double elapsedTime){
        return(initialAcceleration);
    }

    public final double getInitialAcceleration(){
        return(initialAcceleration);
    }

    public final void setInitialAcceleration(final double initialAcceleration){
        this.initialAcceleration=validateNumber(initialAcceleration);
    }

    public final double getInitialSpeed(){
        return(initialSpeed);
    }

    public final void setInitialSpeed(final double initialSpeed){
        this.initialSpeed=validateNumber(initialSpeed);
    }

    public final double getInitialValue(){
        return(initialValue);
    }

    public final void setInitialValue(final double initialValue){
        this.initialValue=validateNumber(validateValue(initialValue));
    }

}
