/**
 * Copyright (c) 2006-2019 Julien Gouesse
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
package engine.input;

import java.util.function.Predicate;
import com.ardor3d.input.InputState;
import com.ardor3d.input.logical.TwoInputStates;

public class MouseWheelMovedDownCondition implements Predicate<TwoInputStates> {
    @Override
    public boolean test(final TwoInputStates states) {
        final InputState currentState = states.getCurrent();
        final InputState previousState = states.getPrevious();

        if (currentState == null) {
            return false;
        }

        if (currentState.equals(previousState)) {
            return false;
        }

        return currentState.getMouseState().getDwheel() < 0;
    }
}
