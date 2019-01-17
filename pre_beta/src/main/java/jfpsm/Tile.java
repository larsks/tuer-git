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
package jfpsm;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.beans.Transient;

/**
 * A tile associates a color with a textured pattern. This pattern can be a
 * voxel.
 * 
 * @author Julien Gouesse
 *
 */
public final class Tile extends JFPSMProjectUserObject {

    private static final long serialVersionUID = 1L;

    /**
     * color used to identify a tile, appears in the 2D maps
     */
    private Color color;

    private VolumeParameters volumeParameters;

    private transient boolean dirty;

    private transient BufferedImageContainer textureContainer;

    public Tile() {
        this("");
    }

    public Tile(String name) {
        super(name);
        // this tile is being created, this is a pending change
        markDirty();
        color = Color.WHITE;
        volumeParameters = null;
        textureContainer = new BufferedImageContainer();
    }

    @Transient
    public final BufferedImage getTexture() {
        return (getTexture(0));
    }

    @Transient
    public final BufferedImage getTexture(final int index) {
        return (textureContainer != null ? textureContainer.get(index) : null);
    }

    public final int getMaxTextureCount() {
        // FIXME: rather use the volume parameters
        return (textureContainer.size());
    }

    @Transient
    public final void setTexture(final BufferedImage texture) {
        setTexture(0, texture);
    }

    @Transient
    public final void setTexture(final int index, final BufferedImage texture) {
        if (textureContainer == null)
            textureContainer = new BufferedImageContainer();
        textureContainer.set(index, texture);
        markDirty();
    }

    public final Color getColor() {
        return (color);
    }

    public final void setColor(Color color) {
        this.color = color;
        markDirty();
    }

    @Transient
    @Override
    public final boolean isDirty() {
        return (dirty || (volumeParameters != null && volumeParameters.isDirty()));
    }

    @Override
    public final void unmarkDirty() {
        dirty = false;
    }

    @Override
    public final void markDirty() {
        dirty = true;
    }

    @Override
    final boolean canInstantiateChildren() {
        return (false);
    }

    @Override
    final boolean isOpenable() {
        return (true);
    }

    @Override
    final boolean isRemovable() {
        return (true);
    }

    public final VolumeParameters getVolumeParameters() {
        return (volumeParameters);
    }

    public final void setVolumeParameters(VolumeParameters volumeParameters) {
        this.volumeParameters = volumeParameters;
        markDirty();
    }

    @Override
    public Viewer createViewer(final Project project, final ProjectManager projectManager) {
        return (new TileViewer(this, project, projectManager));
    }
}