/**
 * Copyright (c) 2006-2017 Julien Gouesse
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
package engine.statemachine;

/**
 * State of player's state machine
 * 
 * @author Julien Gouesse
 *
 */
public enum PlayerState {
    /**
     * the entity is not yet really available (only used very early as an
     * initial state)
     */
    NOT_YET_AVAILABLE,
    /**
     * no operation currently run
     */
    IDLE,
    /**
     * the current weapon is being put back
     */
    PUT_BACK,
    /**
     * the current weapon is being pulled out
     */
    PULL_OUT,
    /**
     * the trigger of the current weapon is being pressed
     */
    PRESS_TRIGGER,
    /**
     * the magazine of the current weapon is empty or the attack with a non
     * fully automatic weapon has ended (therefore the trigger should be
     * released)
     */
    WAIT_FOR_TRIGGER_RELEASE,
    /**
     * the trigger of the current weapon is being released
     */
    RELEASE_TRIGGER,
    /**
     * the current weapon is being used to attack
     */
    ATTACK,
    /**
     * the latest attack is ending
     */
    WAIT_FOR_ATTACK_END,
    /**
     * the current weapon is being reloaded
     */
    RELOAD,
    /**
     * the previous weapon is going to be selected very soon
     */
    SELECT_PREVIOUS,
    /**
     * the next weapon is going to be selected very soon
     */
    SELECT_NEXT
}