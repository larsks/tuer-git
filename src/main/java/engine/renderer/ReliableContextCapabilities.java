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
package engine.renderer;

import com.ardor3d.renderer.jogl.JoglContextCapabilities;

/**
 * Context capabilities with some management of wrong values returned by some
 * OpenGL drivers
 * 
 * @author Julien Gouesse
 *
 */
public class ReliableContextCapabilities extends JoglContextCapabilities {

    public ReliableContextCapabilities(final JoglContextCapabilities defaultCaps) {
        super(defaultCaps);
        // System.err.println(defaultCaps.getDisplayRenderer());
        // System.err.println(defaultCaps.getDisplayVendor());
        // System.err.println(defaultCaps.getDisplayVersion());
        if (defaultCaps.getDisplayRenderer().startsWith("Mesa DRI R200 "))
            /**
             * Some very old ATI Radeon graphics cards do not support 2048*2048
             * textures despite their specifications.
             */
            _maxTextureSize = defaultCaps.getMaxTextureSize() / 2;
        // FIXME R300 and R500 drivers on Mac OS X sometimes return absurd
        // values
    }
}
