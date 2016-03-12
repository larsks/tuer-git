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
package jfpsm;

import java.util.ArrayList;

/**
 * Container of levels
 * 
 * @author Julien Gouesse
 *
 */
public final class LevelSet extends JFPSMProjectUserObject {

    private static final long serialVersionUID = 1L;

    private ArrayList<FloorSet> floorSetsList;

    public LevelSet() {
        this("");
    }

    public LevelSet(String name) {
        super(name);
        floorSetsList = new ArrayList<>();
        markDirty();
    }

    @Override
    public final boolean isDirty() {
        boolean dirty = false;
        if (!dirty)
            for (FloorSet floorset : floorSetsList)
                if (floorset.isDirty()) {
                    dirty = true;
                    break;
                }
        return (dirty);
    }

    @Override
    public final void unmarkDirty() {
    }

    @Override
    public final void markDirty() {
    }

    public final void addFloorSet(FloorSet floorSet) {
        floorSetsList.add(floorSet);
        markDirty();
    }

    public final void removeFloorSet(FloorSet floorSet) {
        floorSetsList.remove(floorSet);
        markDirty();
    }

    public final ArrayList<FloorSet> getFloorSetsList() {
        return (floorSetsList);
    }

    public final void setFloorSetsList(ArrayList<FloorSet> floorSetsList) {
        this.floorSetsList = floorSetsList;
        markDirty();
    }

    @Override
    final boolean canInstantiateChildren() {
        return (true);
    }

    @Override
    final boolean isOpenable() {
        return (true);
    }

    @Override
    final boolean isRemovable() {
        return (false);
    }
}