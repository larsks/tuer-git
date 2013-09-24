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
package engine.input;

import com.ardor3d.input.InputState;
import com.ardor3d.input.logical.TwoInputStates;
import com.google.common.base.Predicate;

public class MouseWheelMovedDownCondition implements Predicate<TwoInputStates> {
    @Override
	public boolean apply(final TwoInputStates states) {
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
