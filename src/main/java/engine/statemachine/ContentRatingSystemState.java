/**
 * Copyright (c) 2006-2019 Julien Gouesse
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

import com.ardor3d.framework.NativeCanvas;
import com.ardor3d.input.GrabbedState;
import com.ardor3d.input.MouseManager;
import com.ardor3d.input.PhysicalLayer;
import com.ardor3d.ui.text.BMText;
import engine.misc.FontStore;
import engine.misc.LocalizedMessageProvider;
import engine.sound.SoundManager;

public final class ContentRatingSystemState extends ScenegraphState {

    private MouseManager mouseManager;

    public ContentRatingSystemState(final NativeCanvas canvas, final PhysicalLayer physicalLayer,
            final MouseManager mouseManager, final SoundManager soundManager, final FontStore fontStore,
            final LocalizedMessageProvider localizedMessageProvider) {
        super(soundManager);
        this.mouseManager = mouseManager;
        final String text = localizedMessageProvider.getString("CONTENT_RATING_WARNING");
        final BMText textNode = new BMText("contentSystemRatingNode", text, fontStore.getFontsList().get(2),
                BMText.Align.Center, BMText.Justify.Center);
        getRoot().attachChild(textNode);
    }

    @Override
    public final void init() {
        // do nothing here because this method will be called
        // after the display of this state
    }

    @Override
    public void setEnabled(final boolean enabled) {
        final boolean wasEnabled = isEnabled();
        if (wasEnabled != enabled) {
            super.setEnabled(enabled);
            if (enabled)
                mouseManager.setGrabbed(GrabbedState.GRABBED);
        }
    }
}
