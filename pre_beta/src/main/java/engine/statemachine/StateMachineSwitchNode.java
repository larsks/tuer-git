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
package engine.statemachine;

import com.ardor3d.bounding.BoundingVolume;
import com.ardor3d.scenegraph.Spatial;
import com.ardor3d.scenegraph.event.DirtyType;
import com.ardor3d.scenegraph.extension.SwitchNode;

/**
 * switch node that updates only the bounding volumes and the transforms of
 * visible children
 */
public final class StateMachineSwitchNode extends SwitchNode {

    public StateMachineSwitchNode() {
        this("StateMachineSwitchNode");
    }

    public StateMachineSwitchNode(final String name) {
        super(name);
        setAllNonVisible();
    }

    @Override
    public void updateWorldTransform(final boolean recurse) {
        // do what a spatial does
        if (_parent != null) {
            _parent.getWorldTransform().multiply(_localTransform, _worldTransform);
        } else {
            _worldTransform.set(_localTransform);
        }
        clearDirty(DirtyType.Transform);

        if (recurse) {
            for (int i = getNumberOfChildren() - 1; i >= 0; i--) {
                if (_childMask.get(i)) {
                    final Spatial child = _children.get(i);
                    if (child != null) {
                        child.updateWorldTransform(true);
                    }
                }
            }
        }
    }

    @Override
    public void updateWorldBound(final boolean recurse) {
        BoundingVolume worldBound = null;
        for (int i = getNumberOfChildren() - 1; i >= 0; i--) {
            // if the child is visible
            if (_childMask.get(i)) {
                final Spatial child = _children.get(i);
                if (child != null) {
                    if (recurse) {
                        child.updateWorldBound(true);
                    }
                    if (worldBound != null) {
                        // merge current world bound with child world bound
                        worldBound.mergeLocal(child.getWorldBound());

                    } else {
                        // set world bound to first non-null child world bound
                        if (child.getWorldBound() != null) {
                            worldBound = child.getWorldBound().clone(_worldBound);
                        }
                    }
                }
            }
        }
        _worldBound = worldBound;
        clearDirty(DirtyType.Bounding);
    }
}
