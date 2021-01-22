/**
 * Copyright (c) 2006-2021 Julien Gouesse
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

/**
 * Additional settings of the mouse and the keyboard
 * 
 * @author Julien Gouesse
 *
 */
public class MouseAndKeyboardSettings implements Cloneable {

    /** turn speed when using the mouse */
    private double mouseRotateSpeed;
    /** speed of move (front, back ,strafe) */
    private double moveSpeed;
    /** turn speed when using the arrow keys */
    private double keyRotateSpeed;
    /** flag indicating whether to reserve mouse look up/down */
    private boolean lookUpDownReversed;
    /**
     * flag indicating whether the mouse pointer is never hidden (mainly for
     * debug purposes)
     */
    private boolean mousePointerNeverHidden;

    public MouseAndKeyboardSettings() {
        super();
    }

    @Override
    public MouseAndKeyboardSettings clone() {
        final MouseAndKeyboardSettings clone = new MouseAndKeyboardSettings();
        clone.set(this);
        return (clone);
    }

    public void set(final MouseAndKeyboardSettings mouseAndKeyboardSettings) {
        keyRotateSpeed = mouseAndKeyboardSettings.keyRotateSpeed;
        lookUpDownReversed = mouseAndKeyboardSettings.lookUpDownReversed;
        mousePointerNeverHidden = mouseAndKeyboardSettings.mousePointerNeverHidden;
        mouseRotateSpeed = mouseAndKeyboardSettings.mouseRotateSpeed;
        moveSpeed = mouseAndKeyboardSettings.moveSpeed;
    }

    @Override
    public boolean equals(Object o) {
        final boolean result;
        if (o == null || !(o instanceof MouseAndKeyboardSettings))
            result = false;
        else {
            final MouseAndKeyboardSettings that = (MouseAndKeyboardSettings) o;
            result = this.lookUpDownReversed == that.lookUpDownReversed
                    && this.mousePointerNeverHidden == that.mousePointerNeverHidden
                    && Double.compare(this.keyRotateSpeed, that.keyRotateSpeed) == 0
                    && Double.compare(this.mouseRotateSpeed, that.mouseRotateSpeed) == 0
                    && Double.compare(this.moveSpeed, that.moveSpeed) == 0;
        }
        return (result);
    }

    public double getMouseRotateSpeed() {
        return (mouseRotateSpeed);
    }

    public void setMouseRotateSpeed(final double mouseRotateSpeed) {
        this.mouseRotateSpeed = mouseRotateSpeed;
    }

    public double getMoveSpeed() {
        return (moveSpeed);
    }

    public void setMoveSpeed(final double moveSpeed) {
        this.moveSpeed = moveSpeed;
    }

    public double getKeyRotateSpeed() {
        return (keyRotateSpeed);
    }

    public void setKeyRotateSpeed(final double keyRotateSpeed) {
        this.keyRotateSpeed = keyRotateSpeed;
    }

    public boolean isLookUpDownReversed() {
        return (lookUpDownReversed);
    }

    public void setLookUpDownReversed(final boolean lookUpDownReversed) {
        this.lookUpDownReversed = lookUpDownReversed;
    }

    public boolean isMousePointerNeverHidden() {
        return (mousePointerNeverHidden);
    }

    public void setMousePointerNeverHidden(final boolean mousePointerNeverHidden) {
        this.mousePointerNeverHidden = mousePointerNeverHidden;
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }
}
