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
package engine.weaponry;

import engine.abstraction.AbstractFactory;

public class WeaponFactory extends AbstractFactory<Weapon> {

    public WeaponFactory() {
        super();
    }

    public boolean addNewWeapon(final String label, final String identifier, final String resourceName,
            final String pickingUpSoundSamplePath, final String blowOrShotSoundSamplePath,
            final String reloadSoundSamplePath, final boolean twoHanded, final int magazineSize,
            final Ammunition ammunition, final int ammunitionPerShot, final int blowOrShotDurationInMillis,
            final boolean fullyAutomatic) {
        boolean success = identifier != null && !componentMap.containsKey(identifier);
        if (success) {
            final Weapon weapon = new Weapon(label, resourceName, pickingUpSoundSamplePath, blowOrShotSoundSamplePath,
                    reloadSoundSamplePath, twoHanded, magazineSize, ammunition, ammunitionPerShot,
                    blowOrShotDurationInMillis, fullyAutomatic);
            success = add(identifier, weapon);
        }
        return (success);
    }
}
