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

import java.beans.Transient;

/**
 * A floor is a subsection in a level (downstairs, upstairs, ...). It uses
 * several images (containers, lights, contents) to contain the color codes that
 * match with the tiles.
 * 
 * @author Julien Gouesse
 *
 */
public final class Floor extends JFPSMProjectUserObject {

    private static final long serialVersionUID = 1L;

    /**
     * This flag is necessary as a floor can be renamed
     */
    private transient boolean dirty;

    private Map[] maps;

    public Floor() {
        this("");
    }

    public Floor(String name) {
        super(name);
        initializeMaps();
        markDirty();
    }

    private final void initializeMaps() {
        maps = new Map[MapType.values().length];
        for (MapType type : MapType.values())
            maps[type.ordinal()] = new Map(type.getLabel());
    }

    final Map getMap(MapType type) {
        return (maps[type.ordinal()]);
    }

    @Transient
    @Override
    public final boolean isDirty() {
        boolean dirty = this.dirty;
        if (!dirty)
            for (MapType type : MapType.values())
                if (maps[type.ordinal()].isDirty()) {
                    dirty = true;
                    break;
                }
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

    public final Map[] getMaps() {
        return (maps);
    }

    public final void setMaps(Map[] maps) {
        this.maps = maps;
        markDirty();
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

    @Override
    public Viewer createViewer(final Project project, final ProjectManager projectManager) {
        return (new FloorViewer(this, project, projectManager));
    }
}