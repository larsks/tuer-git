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
package engine.weaponry;

import java.util.Map;

/**
 * Container of ammunition containers, one ammunition container per ammunition
 * type
 * 
 * @author Julien Gouesse
 *
 */
public class AmmunitionContainerContainer {

    private final AmmunitionContainer[] ammunitionContainers;

    private final AmmunitionFactory ammunitionFactory;

    public AmmunitionContainerContainer(final AmmunitionFactory ammunitionFactory,
            final Map<Ammunition, Integer> ammunitionMaxCountMap) {
        this.ammunitionFactory = ammunitionFactory;
        ammunitionContainers = new AmmunitionContainer[ammunitionFactory.getSize()];
        for (int ammoIndex = 0; ammoIndex < ammunitionContainers.length; ammoIndex++) {
            final Ammunition ammo = ammunitionFactory.get(ammoIndex);
            final Integer ammunitionMaxCountObj = ammunitionMaxCountMap.get(ammo);
            if (ammunitionMaxCountObj != null) {
                final int ammunitionMaxCount = ammunitionMaxCountObj.intValue();
                ammunitionContainers[ammoIndex] = new AmmunitionContainer(ammunitionMaxCount);
            }
        }
    }

    public final void empty() {
        for (AmmunitionContainer ammunitionContainer : ammunitionContainers)
            ammunitionContainer.empty();
    }

    public final int getMax(final Ammunition ammunition) {
        final int ammunitionId = ammunitionFactory.getIntIdentifier(ammunition);
        return (ammunitionContainers[ammunitionId].getAmmunitionMaxCount());
    }

    public final int get(final Ammunition ammunition) {
        final int ammunitionId = ammunitionFactory.getIntIdentifier(ammunition);
        return (ammunitionContainers[ammunitionId].getAmmunitionCount());
    }

    public final int add(final Ammunition ammunition, final int ammunitionCountToAdd) {
        final int ammunitionId = ammunitionFactory.getIntIdentifier(ammunition);
        return (ammunitionContainers[ammunitionId].add(ammunitionCountToAdd));
    }

    public final int remove(final Ammunition ammunition, final int ammunitionCountToRemove) {
        final int ammunitionId = ammunitionFactory.getIntIdentifier(ammunition);
        return (ammunitionContainers[ammunitionId].remove(ammunitionCountToRemove));
    }
}
