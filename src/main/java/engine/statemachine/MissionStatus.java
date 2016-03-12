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
 * Status at the end of a game
 * 
 * @author Julien Gouesse
 *
 */
public enum MissionStatus {

    /**
     * the objectives of the mission have been achieved and the player is still
     * alive
     */
    COMPLETED,
    /**
     * the player is still alive and has reached the end of the mission but some
     * objectives haven't been achieved
     */
    FAILED,
    /**
     * the player has just died during the mission
     */
    DECEASED,
    /**
     * the player has just aborted the mission
     */
    ABORTED;
}
