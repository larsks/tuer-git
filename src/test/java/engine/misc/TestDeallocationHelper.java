/**
 * Copyright (c) 2006-2021 Julien Gouesse This program is free software; you can
 * redistribute it and/or modify it under the terms of the GNU General Public
 * License as published by the Free Software Foundation; either version 2 of the
 * License, or (at your option) any later version. This program is distributed
 * in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even
 * the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details. You should have received
 * a copy of the GNU General Public License along with this program; if not,
 * write to the Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor,
 * Boston, MA 02110-1301, USA.
 */
package engine.misc;

import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.CharBuffer;
import java.nio.DoubleBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.LongBuffer;
import java.nio.ShortBuffer;
import java.util.ArrayList;
import java.util.List;

/**
 * Test of the deallocation helper, it creates all possible kinds of direct NIO
 * buffers.
 * 
 * @author Julien Gouesse
 *
 */
public class TestDeallocationHelper {

    public static void main(String[] args) {
        final ByteBuffer bigEndianReadOnlyDirectByteBuffer = ByteBuffer.allocateDirect(1).order(ByteOrder.BIG_ENDIAN)
                .asReadOnlyBuffer();
        final ByteBuffer bigEndianReadWriteDirectByteBuffer = ByteBuffer.allocateDirect(1).order(ByteOrder.BIG_ENDIAN);
        final ByteBuffer slicedBigEndianReadOnlyDirectByteBuffer = ByteBuffer.allocateDirect(2)
                .order(ByteOrder.BIG_ENDIAN).put((byte) 0).put((byte) 0).position(1).limit(2).slice()
                        .asReadOnlyBuffer();
        final ByteBuffer slicedBigEndianReadWriteDirectByteBuffer = ByteBuffer.allocateDirect(2)
                .order(ByteOrder.BIG_ENDIAN).put((byte) 0).put((byte) 0).position(1).limit(2).slice();
        final CharBuffer bigEndianReadOnlyDirectCharBuffer = ByteBuffer.allocateDirect(1).order(ByteOrder.BIG_ENDIAN)
                .asReadOnlyBuffer().asCharBuffer();
        final CharBuffer bigEndianReadWriteDirectCharBuffer = ByteBuffer.allocateDirect(1).order(ByteOrder.BIG_ENDIAN)
                .asCharBuffer();
        final DoubleBuffer bigEndianReadOnlyDirectDoubleBuffer = ByteBuffer.allocateDirect(1)
                .order(ByteOrder.BIG_ENDIAN).asReadOnlyBuffer().asDoubleBuffer();
        final DoubleBuffer bigEndianReadWriteDirectDoubleBuffer = ByteBuffer.allocateDirect(1)
                .order(ByteOrder.BIG_ENDIAN).asDoubleBuffer();
        final FloatBuffer bigEndianReadOnlyDirectFloatBuffer = ByteBuffer.allocateDirect(1).order(ByteOrder.BIG_ENDIAN)
                .asReadOnlyBuffer().asFloatBuffer();
        final FloatBuffer bigEndianReadWriteDirectFloatBuffer = ByteBuffer.allocateDirect(1).order(ByteOrder.BIG_ENDIAN)
                .asFloatBuffer();
        final IntBuffer bigEndianReadOnlyDirectIntBuffer = ByteBuffer.allocateDirect(1).order(ByteOrder.BIG_ENDIAN)
                .asReadOnlyBuffer().asIntBuffer();
        final IntBuffer bigEndianReadWriteDirectIntBuffer = ByteBuffer.allocateDirect(1).order(ByteOrder.BIG_ENDIAN)
                .asIntBuffer();
        final LongBuffer bigEndianReadOnlyDirectLongBuffer = ByteBuffer.allocateDirect(1).order(ByteOrder.BIG_ENDIAN)
                .asReadOnlyBuffer().asLongBuffer();
        final LongBuffer bigEndianReadWriteDirectLongBuffer = ByteBuffer.allocateDirect(1).order(ByteOrder.BIG_ENDIAN)
                .asLongBuffer();
        final ShortBuffer bigEndianReadOnlyDirectShortBuffer = ByteBuffer.allocateDirect(1).order(ByteOrder.BIG_ENDIAN)
                .asReadOnlyBuffer().asShortBuffer();
        final ShortBuffer bigEndianReadWriteDirectShortBuffer = ByteBuffer.allocateDirect(1).order(ByteOrder.BIG_ENDIAN)
                .asShortBuffer();
        final ByteBuffer littleEndianReadOnlyDirectByteBuffer = ByteBuffer.allocateDirect(1)
                .order(ByteOrder.LITTLE_ENDIAN).asReadOnlyBuffer();
        final ByteBuffer littleEndianReadWriteDirectByteBuffer = ByteBuffer.allocateDirect(1)
                .order(ByteOrder.LITTLE_ENDIAN);
        final ByteBuffer slicedLittleEndianReadOnlyDirectByteBuffer = ByteBuffer.allocateDirect(2)
                .order(ByteOrder.LITTLE_ENDIAN).put((byte) 0).put((byte) 0).position(1).limit(2).slice()
                        .asReadOnlyBuffer();
        final ByteBuffer slicedLittleEndianReadWriteDirectByteBuffer = ByteBuffer.allocateDirect(2)
                .order(ByteOrder.LITTLE_ENDIAN).put((byte) 0).put((byte) 0).position(1).limit(2).slice();
        final CharBuffer littleEndianReadOnlyDirectCharBuffer = ByteBuffer.allocateDirect(1)
                .order(ByteOrder.LITTLE_ENDIAN).asReadOnlyBuffer().asCharBuffer();
        final CharBuffer littleEndianReadWriteDirectCharBuffer = ByteBuffer.allocateDirect(1)
                .order(ByteOrder.LITTLE_ENDIAN).asCharBuffer();
        final DoubleBuffer littleEndianReadOnlyDirectDoubleBuffer = ByteBuffer.allocateDirect(1)
                .order(ByteOrder.LITTLE_ENDIAN).asReadOnlyBuffer().asDoubleBuffer();
        final DoubleBuffer littleEndianReadWriteDirectDoubleBuffer = ByteBuffer.allocateDirect(1)
                .order(ByteOrder.LITTLE_ENDIAN).asDoubleBuffer();
        final FloatBuffer littleEndianReadOnlyDirectFloatBuffer = ByteBuffer.allocateDirect(1)
                .order(ByteOrder.LITTLE_ENDIAN).asReadOnlyBuffer().asFloatBuffer();
        final FloatBuffer littleEndianReadWriteDirectFloatBuffer = ByteBuffer.allocateDirect(1)
                .order(ByteOrder.LITTLE_ENDIAN).asFloatBuffer();
        final IntBuffer littleEndianReadOnlyDirectIntBuffer = ByteBuffer.allocateDirect(1)
                .order(ByteOrder.LITTLE_ENDIAN).asReadOnlyBuffer().asIntBuffer();
        final IntBuffer littleEndianReadWriteDirectIntBuffer = ByteBuffer.allocateDirect(1)
                .order(ByteOrder.LITTLE_ENDIAN).asIntBuffer();
        final LongBuffer littleEndianReadOnlyDirectLongBuffer = ByteBuffer.allocateDirect(1)
                .order(ByteOrder.LITTLE_ENDIAN).asReadOnlyBuffer().asLongBuffer();
        final LongBuffer littleEndianReadWriteDirectLongBuffer = ByteBuffer.allocateDirect(1)
                .order(ByteOrder.LITTLE_ENDIAN).asLongBuffer();
        final ShortBuffer littleEndianReadOnlyDirectShortBuffer = ByteBuffer.allocateDirect(1)
                .order(ByteOrder.LITTLE_ENDIAN).asReadOnlyBuffer().asShortBuffer();
        final ShortBuffer littleEndianReadWriteDirectShortBuffer = ByteBuffer.allocateDirect(1)
                .order(ByteOrder.LITTLE_ENDIAN).asShortBuffer();
        final List<Buffer> buffers = new ArrayList<>();
        buffers.add(bigEndianReadOnlyDirectByteBuffer);
        buffers.add(bigEndianReadWriteDirectByteBuffer);
        buffers.add(slicedBigEndianReadOnlyDirectByteBuffer);
        buffers.add(slicedBigEndianReadWriteDirectByteBuffer);
        buffers.add(bigEndianReadOnlyDirectCharBuffer);
        buffers.add(bigEndianReadWriteDirectCharBuffer);
        buffers.add(bigEndianReadOnlyDirectDoubleBuffer);
        buffers.add(bigEndianReadWriteDirectDoubleBuffer);
        buffers.add(bigEndianReadOnlyDirectFloatBuffer);
        buffers.add(bigEndianReadWriteDirectFloatBuffer);
        buffers.add(bigEndianReadOnlyDirectIntBuffer);
        buffers.add(bigEndianReadWriteDirectIntBuffer);
        buffers.add(bigEndianReadOnlyDirectLongBuffer);
        buffers.add(bigEndianReadWriteDirectLongBuffer);
        buffers.add(bigEndianReadOnlyDirectShortBuffer);
        buffers.add(bigEndianReadWriteDirectShortBuffer);
        buffers.add(littleEndianReadOnlyDirectByteBuffer);
        buffers.add(littleEndianReadWriteDirectByteBuffer);
        buffers.add(slicedLittleEndianReadOnlyDirectByteBuffer);
        buffers.add(slicedLittleEndianReadWriteDirectByteBuffer);
        buffers.add(littleEndianReadOnlyDirectCharBuffer);
        buffers.add(littleEndianReadWriteDirectCharBuffer);
        buffers.add(littleEndianReadOnlyDirectDoubleBuffer);
        buffers.add(littleEndianReadWriteDirectDoubleBuffer);
        buffers.add(littleEndianReadOnlyDirectFloatBuffer);
        buffers.add(littleEndianReadWriteDirectFloatBuffer);
        buffers.add(littleEndianReadOnlyDirectIntBuffer);
        buffers.add(littleEndianReadWriteDirectIntBuffer);
        buffers.add(littleEndianReadOnlyDirectLongBuffer);
        buffers.add(littleEndianReadWriteDirectLongBuffer);
        buffers.add(littleEndianReadOnlyDirectShortBuffer);
        buffers.add(littleEndianReadWriteDirectShortBuffer);
        final DeallocationHelper helper = new DeallocationHelper();
        for (final Buffer buffer : buffers)
            helper.deallocate(buffer);
    }
}
