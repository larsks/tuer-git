/**
 * Copyright (c) 2006-2016 Julien Gouesse
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

/**
 * equation of a uniformly variable rotation in degrees
 * 
 * @author Julien Gouesse
 *
 */
public final class UniformlyVariableRotationEquation extends UniformlyVariableMovementEquation {

    private static final long serialVersionUID = 1L;

    public UniformlyVariableRotationEquation() {
        this(0, 0, 0);
    }

    public UniformlyVariableRotationEquation(double initialAcceleration, double initialSpeed, double initialValue) {
        super(initialAcceleration, initialSpeed, initialValue);
    }

    @Override
    protected final double validateValue(final double value) {
        double validValue = validateNumber(value);
        if (Math.abs(validValue) > 180)
            validValue -= (validValue > 0 ? 1 : -1) * (Math.floor((validValue - 180) / 360) + 1) * 360;
        return (validValue);
    }
}
