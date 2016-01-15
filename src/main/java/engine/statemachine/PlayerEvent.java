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
package engine.statemachine;

/**
 * Event of the player's state machine
 * 
 * @author Julien Gouesse
 *
 */
public enum PlayerEvent{
	/**
	 * event that drives the entity available
	 */
	AVAILABLE,
	/**
	 * event that may allow to come back to a state allowing to start any operation
	 */
	IDLE,
	/**
	 * event that may lead to press the trigger of the current weapon
	 */
	PRESSING_TRIGGER,
	/**
	 * event that may lead to wait for the trigger release when the magazine of the current 
	 * weapon is empty or the attack with a non fully automatic weapon has ended)
	 */
	WAITING_FOR_TRIGGER_RELEASE,
	/**
	 * event that may lead to release the trigger of the current weapon
	 */
	RELEASING_TRIGGER,
	/**
	 * event that may run an attack
	 */
	ATTACKING,
	/**
	 * event that may run a reload of the current weapon
	 */
	RELOADING,
	/**
	 * event that may lead to a switch to the previous weapon but that runs the "put back" first
	 */
	PUTTING_BACK_BEFORE_SELECTING_PREVIOUS,
	/**
	 * event that may lead to a switch to the next weapon but that runs the "put back" first
	 */
	PUTTING_BACK_BEFORE_SELECTING_NEXT,
	/**
	 * event that may lead to a reload of the current weapon but that runs the "put back" first
	 */
	PUTTING_BACK_BEFORE_RELOADING,
	/**
	 * event that runs the "pull out" of the current weapon
	 */
	PULLING_OUT,
	/**
	 * event that may run a switch to the previous weapon
	 */
	SELECTING_PREVIOUS,
	/**
	 * event that may run a switch to the next weapon
	 */
	SELECTING_NEXT
}
