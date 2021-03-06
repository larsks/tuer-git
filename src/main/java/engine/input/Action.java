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
 * Default actions that the player can do, bounded to a key mapping
 * 
 * @author Julien Gouesse
 *
 */
public enum Action {

    /***/
    MOVE_FORWARD,
    /***/
    MOVE_BACKWARD,
    /***/
    STRAFE_LEFT,
    /***/
    STRAFE_RIGHT,
    /***/
    TURN_LEFT,
    /***/
    TURN_RIGHT,
    /***/
    LOOK_UP,
    /***/
    LOOK_DOWN,
    /** crouch */
    CROUCH,
    /** pause the game */
    PAUSE,
    /** reload the current weapon */
    RELOAD,
    /** activate, i.e use a switch */
    ACTIVATE,
    /***/
    RUN,
    /** attack, i.e shoot or beat */
    ATTACK,
    /** select the next weapon */
    NEXT_WEAPON,
    /** select the previous weapon */
    PREVIOUS_WEAPON,
    /** enable/disable the wireframe mode, mainly for debug purposes */
    TOGGLE_WIREFRAME_MODE,
    /** leave the game */
    QUIT;
}
