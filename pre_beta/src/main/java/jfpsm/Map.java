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
package jfpsm;

import java.awt.Color;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.beans.Transient;

public final class Map extends JFPSMProjectUserObject {

    private static final long serialVersionUID = 1L;

    private static final int minimumSize = Integer
            .highestOneBit(Toolkit.getDefaultToolkit().getScreenSize().height / 2);

    private transient boolean dirty;

    private transient BufferedImage image;

    public Map() {
        this("");
    }

    public Map(String name) {
        super(name);
        initializeImage();
        markDirty();
    }

    @Transient
    @Override
    public final boolean isDirty() {
        return (dirty);
    }

    @Override
    public final void markDirty() {
        dirty = true;
    }

    @Override
    public final void unmarkDirty() {
        dirty = false;
    }

    @Transient
    public final BufferedImage getImage() {
        return (image);
    }

    @Transient
    public final void setImage(BufferedImage image) {
        this.image = image;
        markDirty();
    }

    private final void initializeImage() {
        image = new BufferedImage(minimumSize, minimumSize, BufferedImage.TYPE_INT_ARGB);
        for (int x = 0; x < image.getWidth(); x++)
            for (int y = 0; y < image.getHeight(); y++)
                image.setRGB(x, y, Color.WHITE.getRGB());
    }

    public final int getWidth() {
        return (image.getWidth());
    }

    public final int getHeight() {
        return (image.getHeight());
    }

    @Override
    final boolean canInstantiateChildren() {
        return (false);
    }

    @Override
    final boolean isOpenable() {
        return (false);
    }

    @Override
    final boolean isRemovable() {
        return (false);
    }
}
