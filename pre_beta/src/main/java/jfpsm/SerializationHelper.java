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

import java.beans.XMLDecoder;
import java.beans.XMLEncoder;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * helper class to use the XML encoding
 * 
 * @author Julien Gouesse
 *
 */
public final class SerializationHelper {

    public static final Object decodeObjectInXMLFile(String path) {
        Object resultingObject = null;
        try (final InputStream inputStream = SerializationHelper.class.getResourceAsStream(path);
                final BufferedInputStream bis = new BufferedInputStream(inputStream);
                final XMLDecoder decoder = new XMLDecoder(bis)) {
            resultingObject = decoder.readObject();
        } catch (IOException ioe) {
            throw new RuntimeException("Unable to close the file " + path, ioe);
        }
        return (resultingObject);
    }

    public static final void encodeObjectInFile(Object o, String filename) {
        File file = new File(filename);
        try {
            if (!file.exists())
                if (!file.createNewFile())
                    throw new IOException("Unable to create the file " + filename);
            try (final FileOutputStream fos = new FileOutputStream(file);
                    final BufferedOutputStream bos = new BufferedOutputStream(fos);
                    final XMLEncoder encoder = new XMLEncoder(bos)) {
                encoder.writeObject(o);
            }
        } catch (IOException ioe) {
            throw new RuntimeException("Unable to encode the file " + filename, ioe);
        }

    }
}
