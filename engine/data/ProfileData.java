/**
 * Copyright (c) 2006-2014 Julien Gouesse
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

/**
 * Data of the profile, manages the unlocked levels. It will be used to store the other achievements too. It should contain the player's 
 * statistics (the latest GameStatistics instance should transfer its data into it when ending a mission).
 * 
 * @author Julien Gouesse
 *
 */
public class ProfileData {
	
	/**
	 * list of the levels indices that the player unlocks (to which he can go)
	 * */
	final Set<Integer> unlockedLevelsIndices;
	
	/**
	 * Constructor
	 * 
	 * @param firstUnlockedLevelIndex level index of the unlocked level when the player runs the game for the very first time
	 */
	public ProfileData(final int firstUnlockedLevelIndex){
		super();
		this.unlockedLevelsIndices=new HashSet<>();
		this.unlockedLevelsIndices.add(Integer.valueOf(firstUnlockedLevelIndex));
	}

	/**
	 * Unlocks a level
	 * 
	 * @param levelIndex index of the level to add
	 * @return <code>true</code> if the index points to a level that wasn't already unlocked before this call, otherwise <code>false</code>
	 */
	public boolean addUnlockedLevelIndex(final int levelIndex){
		return(unlockedLevelsIndices.add(Integer.valueOf(levelIndex)));
	}
	
	/**
	 * Tells whether a level is unlocked
	 * 
	 * @param levelIndex index of the level
	 * @return <code>true</code> if the index points to an unlocked level, otherwise <code>false</code>
	 */
	public boolean containsUnlockedLevelIndex(final int levelIndex){
		return(unlockedLevelsIndices.contains(Integer.valueOf(levelIndex)));
	}
}
