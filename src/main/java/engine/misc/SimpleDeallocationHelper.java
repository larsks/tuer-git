/**
 * Copyright (c) 2006-2021 Julien Gouesse
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
package engine.misc;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

import jdk.incubator.foreign.MemorySegment;

/**
 * Simple helper to deallocate memory on the native heap allocated during the creation
 * of a direct byte buffer. It supports only OpenJDK and Oracle Java >= 14
 * 
 * @author Julien Gouesse
 */
public class SimpleDeallocationHelper {
    
    private static final Logger LOGGER = Logger.getLogger(SimpleDeallocationHelper.class.getName());

    public SimpleDeallocationHelper() {
        super();
    }
    
    public final ByteBuffer findDeallocatableBuffer(final Buffer buffer) {
        final ByteBuffer result;
        if (buffer == null || !buffer.isDirect()) {
            result = null;
        } else if (buffer.getClass().getName().equals("java.nio.DirectByteBuffer") || Arrays.stream(buffer.getClass().getInterfaces()).map(Class::getName).anyMatch("sun.nio.ch.DirectBuffer"::equals)) {
            Object attachment = null;
            try {
                final Method attachmentMethod = buffer.getClass().getMethod("attachment");
                attachmentMethod.setAccessible(true);
                attachment = attachmentMethod.invoke(buffer);
            } catch (final InvocationTargetException | NoSuchMethodException | IllegalAccessException | IllegalArgumentException e) {
                LOGGER.log(Level.WARNING, "Failed to find the attachment", e);
            }
            if (attachment == null) {
                result = (ByteBuffer) buffer;
            } else if (attachment instanceof Buffer) {
                result = findDeallocatableBuffer((Buffer) attachment);
            } else {
                result = null;
            }
        } else {
            result = null;
        }
        return result;
    }
    
    public void deallocate(final Buffer buffer) {
        final ByteBuffer deallocatableBuffer = findDeallocatableBuffer(buffer);
        if (deallocatableBuffer != null) {
            MemorySegment.ofByteBuffer(deallocatableBuffer).close();
        }
    }
}
