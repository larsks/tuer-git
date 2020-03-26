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
package jfpsm;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Set of tools
 * 
 * @author Julien Gouesse
 *
 */
public final class ToolSet extends JFPSMToolUserObject {

    private static final long serialVersionUID = 1L;

    private final ArrayList<Tool> toolsList;

    public ToolSet() {
        this("");
    }

    public ToolSet(final String name) {
        super(name);
        toolsList = new ArrayList<>();
    }

    @Override
    public boolean isDirty() {
        return (false);
    }

    @Override
    public void markDirty() {
    }

    @Override
    public void unmarkDirty() {
    }

    final void addTool(final Tool tool) {
        toolsList.add(tool);
        markDirty();
    }

    final List<Tool> getToolsList() {
        // it should be unmodifiable if and only if it is not serialized
        return (Collections.unmodifiableList(toolsList));
    }

    @Override
    final boolean isOpenable() {
        // it is always open and it cannot be closed
        return (false);
    }

    @Override
    final boolean isRemovable() {
        return (false);
    }

    @Override
    boolean canInstantiateChildren() {
        return (false);
    }
}
