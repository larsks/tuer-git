/**
 * Copyright (c) 2006-2020 Julien Gouesse
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

import engine.abstraction.AbstractFactory;

/**
 * 
 * @author Julien Gouesse
 *
 */
public class SkyboxFactory extends AbstractFactory<Skybox> {

    public SkyboxFactory() {
        super();
    }

    public boolean addNewSkybox(final String label, final String identifier, final String[] textureResourceNames) {
        boolean success = identifier != null && !componentMap.containsKey(identifier);
        if (success) {
            final Skybox skybox = new Skybox(label, identifier, textureResourceNames);
            success = add(identifier, skybox);
        }
        return (success);
    }
}
