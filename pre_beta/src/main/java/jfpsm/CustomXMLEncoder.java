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

import java.beans.XMLEncoder;
import java.io.OutputStream;

/**
 * XML encoder that sets the dirty flag at false to ensure the object has no
 * pending change
 * 
 * @author Julien Gouesse
 *
 */
public final class CustomXMLEncoder extends XMLEncoder {

    public CustomXMLEncoder(OutputStream out) {
        super(out);
    }

    @Override
    public final void writeObject(Object o) {
        super.writeObject(o);
        if (o != null && o instanceof Dirtyable)
            ((Dirtyable) o).unmarkDirty();
        // FIXME: call these methods on all attributes
    }
}
