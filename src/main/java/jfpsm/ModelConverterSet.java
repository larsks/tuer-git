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
import java.util.Collections;
import java.util.List;

public class ModelConverterSet extends Tool {

    private static final long serialVersionUID = 1L;

    private ArrayList<ModelConverter> modelConvertersList;

    public ModelConverterSet() {
        this("");
    }

    public ModelConverterSet(final String name) {
        super(name);
        modelConvertersList = new ArrayList<>();
        markDirty();
    }

    public final void addModelConverter(final ModelConverter modelConverter) {
        modelConvertersList.add(modelConverter);
        markDirty();
    }

    public final void removeModelConverter(final ModelConverter modelConverter) {
        modelConvertersList.remove(modelConverter);
        markDirty();
    }

    public final void removeAllModelConverters() {
        modelConvertersList.clear();
        markDirty();
    }

    public final List<ModelConverter> getModelConvertersList() {
        return (Collections.unmodifiableList(modelConvertersList));
    }

    public final void setModelConvertersList(final ArrayList<ModelConverter> modelConvertersList) {
        this.modelConvertersList = modelConvertersList;
        markDirty();
    }
}
