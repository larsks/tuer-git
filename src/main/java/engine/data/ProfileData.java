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
package engine.data;

import java.util.HashSet;
import java.util.Set;

import engine.statemachine.GameStatistics;

/**
 * Data of the profile, manages the unlocked levels. It will be used to store
 * the other achievements too. It should contain the player's statistics (the
 * latest GameStatistics instance should transfer its data into it when ending a
 * mission).
 * 
 * @author Julien Gouesse
 *
 */
public class ProfileData {

    /**
     * list of the level identifiers that the player unlocks (to which he can
     * go)
     */
    final Set<String> unlockedLevelIdentifiers;

    /**
     * Constructor
     */
    public ProfileData() {
        super();
        this.unlockedLevelIdentifiers = new HashSet<>();
    }

    /**
     * Unlocks a level
     * 
     * @param levelIdentifier
     *            identifier of the level to add
     * @return <code>true</code> if the identifier points to a level that wasn't
     *         already unlocked before this call, otherwise <code>false</code>
     */
    public boolean addUnlockedLevelIdentifier(final String levelIdentifier) {
        return (unlockedLevelIdentifiers.add(levelIdentifier));
    }

    /**
     * Tells whether a level is unlocked
     * 
     * @param levelIdentifier
     *            identifier of the level
     * @return <code>true</code> if the identifier points to an unlocked level,
     *         otherwise <code>false</code>
     */
    public boolean containsUnlockedLevelIdentifier(final String levelIdentifier) {
        return (unlockedLevelIdentifiers.contains(levelIdentifier));
    }

    /**
     * Updates the games statistics of the player with the statistics of a game
     * 
     * @param gameStats
     *            statistics of a game
     */
    public void updateGamesStatistics(final GameStatistics gameStats) {
        // TODO
    }

    public void save() {
        // TODO
    }

    public void load() {
        // unlocks at least the first level
        // FIXME the identifier of the first level shouldn't be hardcoded
        unlockedLevelIdentifiers.add("0");
        // TODO if the file exists, use it to get the unlocked levels
    }
}
