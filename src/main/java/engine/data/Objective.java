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
package engine.data;

import engine.statemachine.GameStatistics;

/**
 * Objective of a mission. It can be uncompleted, completed or failed.
 * 
 * @author Julien Gouesse
 *
 */
public abstract class Objective {

    /** description */
    private final String description;

    /**
     * Constructor
     * 
     * @param description
     *            description
     */
    public Objective(final String description) {
        super();
        this.description = description;
    }

    public abstract ObjectiveStatus getStatus(final GameStatistics gameStats);

    /**
     * Returns the description
     * 
     * @return description
     */
    public String getDescription() {
        return (description);
    }
}
