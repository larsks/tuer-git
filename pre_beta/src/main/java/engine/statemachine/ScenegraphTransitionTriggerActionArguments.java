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

import java.util.List;

import com.ardor3d.math.Vector3;

import engine.data.Objective;
import se.hiflyer.fettle.Arguments;

/**
 * Arguments used by scenegraph transitions triggers
 * 
 * @author Julien Gouesse
 *
 */
public class ScenegraphTransitionTriggerActionArguments extends Arguments {

    /**
     * Constructor with no tag
     */
    public ScenegraphTransitionTriggerActionArguments() {
        this(null);
    }

    /**
     * Constructor
     * 
     * @param tag
     *            hint used during the transition
     */
    @SuppressWarnings("cast")
    public ScenegraphTransitionTriggerActionArguments(final String tag) {
        super(new String[] { null, null }, tag, (Object) new GameStatistics[1], (Object) new List[1],
                (Object) new Vector3[2]);
    }

    /**
     * 
     * @return
     */
    public String getPreviousLevelIdentifier() {
        return (((String[]) getFirst())[0]);
    }

    /**
     * 
     * @param previousLevelIdentifier
     */
    public void setPreviousLevelIdentifier(final String previousLevelIdentifier) {
        ((String[]) getFirst())[0] = previousLevelIdentifier;
    }

    /**
     * 
     * @return
     */
    public String getNextLevelIdentifier() {
        return (((String[]) getFirst())[1]);
    }

    /**
     * 
     * @param nextLevelIdentifier
     */
    public void setNextLevelIdentifier(final String nextLevelIdentifier) {
        ((String[]) getFirst())[1] = nextLevelIdentifier;
    }

    /**
     * 
     * @return
     */
    public String getTag() {
        return ((String) getArgument(1));
    }

    /**
     * 
     * @return
     */
    public GameStatistics getGameStatistics() {
        return (((GameStatistics[]) getArgument(2))[0]);
    }

    /**
     * 
     * @param gameStats
     */
    public void setGameStatistics(final GameStatistics gameStats) {
        ((GameStatistics[]) getArgument(2))[0] = gameStats;
    }

    /**
     * 
     * @return
     */
    @SuppressWarnings("unchecked")
    public List<Objective> getObjectives() {
        return ((((List[]) getArgument(3))[0]));
    }

    /**
     * 
     * @param objectives
     */
    public void setObjectives(final List<Objective> objectives) {
        ((List[]) getArgument(3))[0] = objectives;
    }

    /**
     * 
     * @return
     */
    public Vector3 getPreviousLocation() {
        return (((Vector3[]) getArgument(4))[0]);
    }

    /**
     * 
     * @param previousLocation
     */
    public void setPreviousLocation(final Vector3 previousLocation) {
        ((Vector3[]) getArgument(4))[0] = previousLocation;
    }

    /**
     * 
     * @return
     */
    public Vector3 getNextLocation() {
        return (((Vector3[]) getArgument(4))[1]);
    }

    /**
     * 
     * @param nextLocation
     */
    public void setNextLocation(final Vector3 nextLocation) {
        ((Vector3[]) getArgument(4))[1] = nextLocation;
    }
}
