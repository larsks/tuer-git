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

/**
 * Object managed by JFPSM, it appears in the tree and in its dedicated viewer
 * if any.
 * 
 * @author Julien Gouesse
 *
 */
public abstract class JFPSMUserObject extends Namable implements Dirtyable, Resolvable {

    private static final long serialVersionUID = 1L;

    public JFPSMUserObject() {
        super("");
    }

    public JFPSMUserObject(String name) {
        super(name);
    }

    abstract boolean isRemovable();

    abstract boolean isOpenable();

    abstract boolean canInstantiateChildren();

    @Override
    public void setName(String name) {
        super.setName(name);
        // mark the entity as dirty when the user renames it
        markDirty();
    }

    @Override
    public void resolve() {
    }
}
