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
package common;

/**
 * model file format supported by JFPSM
 * 
 * @author Julien Gouesse
 *
 */
public enum ModelFileFormat {
    ARDOR3D_BINARY("Ardor3D Binary", "abin", null), ARDOR3D_XML("Ardor3D XML", "axml", null), COLLADA("Collada", "dae",
            null), MD2("MD2", "md2", null), MD3("MD3", "md3", null), WAVEFRONT_OBJ("WaveFront OBJ", "obj", "mtl");

    private final String description;

    private final String extension;

    private final String secondaryExtension;

    private ModelFileFormat(final String description, final String extension, final String secondaryExtension) {
        this.description = description;
        this.extension = extension;
        this.secondaryExtension = secondaryExtension;
    }

    public final String getDescription() {
        return (description);
    }

    public final String getExtension() {
        return (extension);
    }

    public final String getSecondaryExtension() {
        return (secondaryExtension);
    }

    @Override
    public final String toString() {
        return (getDescription());
    }

    public static ModelFileFormat get(final String filePath) {
        ModelFileFormat modelFileFormat = null;
        if (filePath != null) {
            final int lastIndexOfDot = filePath.lastIndexOf('.');
            if (lastIndexOfDot != -1 && lastIndexOfDot < filePath.length() - 1) {
                final String modelFileExtension = filePath.substring(lastIndexOfDot + 1).toLowerCase();
                for (ModelFileFormat currentModelFileFormat : ModelFileFormat.values())
                    if (modelFileExtension.equals(currentModelFileFormat.getExtension())) {
                        modelFileFormat = currentModelFileFormat;
                        break;
                    }
            }
        }
        return (modelFileFormat);
    }
}