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

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Helper to deallocate memory on the native heap allocated during the creation
 * of a direct byte buffer. It supports numerous virtual machines including
 * OpenJDK, Oracle/Sun Java, Android Dalvik Virtual Machine, Apache Harmony and
 * GNU Classpath. This class uses the syntax of Java 1.7 but it can work
 * correctly with Java 1.4 with a very few minor type changes when using the
 * maps and the collections. It relies on lots of implementation details but
 * it's robust enough to go on working (except when the implementors
 * intentionally use a very general class to store the buffers) despite minor
 * naming changes like those that occurred between Java 1.6 and Java 1.7. It
 * supports Java 1.9 despite the move of the cleaner from the package sun.misc
 * to jdk.internal.ref (in the module java.base). N.B: Releasing the native
 * memory of a sliced direct NIO buffer, the one of a direct NIO buffer created
 * with JNI or the one of any direct NIO buffer created by the virtual machine
 * or by a framework not under your control doesn't prevent the calls to methods
 * attempting to access such buffers. Those calls can throw an exception or
 * crash the virtual machine depending on the implementations.
 * 
 * @author Julien Gouesse
 */
public class DeallocationHelper {

    /**
     * tool responsible for releasing the native memory of a deallocatable byte
     * buffer
     */
    public static abstract class Deallocator {

        public Deallocator() {
            super();
        }

        /**
         * releases the native memory of a deallocatable byte buffer
         * 
         * @param directByteBuffer
         *            deallocatable byte buffer
         * 
         * @return <code>true</code> if the deallocation is successful,
         *         otherwise <code>false</code>
         */
        public abstract boolean run(final ByteBuffer directByteBuffer);
    }

    public static class OracleSunOpenJdkDeallocator extends Deallocator {

        private Method directByteBufferCleanerMethod;

        private Method cleanerCleanOrRunMethod;
        
        private Object unsafeObject;
        
        private Method invokeCleanerMethod;

        public OracleSunOpenJdkDeallocator() {
            super();
            try {
                try {
                    // Java >= 1.9, code path inspired by JogAmp's Gluegen, big kudos to Sven Göthel: https://jogamp.org/cgit/gluegen.git/commit/?h=java11&id=6603026f1bfec02e3486c52270a09a355a1bf914
                    final Class<?> unsafeClass = Class.forName("sun.misc.Unsafe");
                    final Field theUnsafeField = unsafeClass.getDeclaredField("theUnsafe");
                    theUnsafeField.setAccessible(true);
                    unsafeObject = theUnsafeField.get(null);
                    invokeCleanerMethod = unsafeClass.getMethod("invokeCleaner", java.nio.ByteBuffer.class);
                    invokeCleanerMethod.setAccessible(true);
                } catch (final Throwable t) {
                    unsafeObject = null;
                    invokeCleanerMethod = null;
                    final Class<?> directByteBufferClass = Class.forName("java.nio.DirectByteBuffer");
                    final Method localDirectByteBufferCleanerMethod = directByteBufferClass
                            .getDeclaredMethod("cleaner");
                    final Class<?> cleanerClass = localDirectByteBufferCleanerMethod.getReturnType();
                    final Method localCleanerCleanOrRunMethod;
                    if (Runnable.class.isAssignableFrom(cleanerClass)) {
                        /**
                         * The return type is jdk.internal.ref.Cleaner in Java >= 1.9 but it implements the Runnable interface only
                         * in some early access builds of Java 9
                         */
                        localCleanerCleanOrRunMethod = Runnable.class.getDeclaredMethod("run");
                    } else {
                        // The return type is sun.misc.Cleaner in Java <= 1.8
                        localCleanerCleanOrRunMethod = cleanerClass.getDeclaredMethod("clean");
                    }
                    // according to the Java documentation, by default, a reflected object is not accessible
                    localDirectByteBufferCleanerMethod.setAccessible(true);
                    localCleanerCleanOrRunMethod.setAccessible(true);
                    cleanerCleanOrRunMethod = localCleanerCleanOrRunMethod;
                    directByteBufferCleanerMethod = localCleanerCleanOrRunMethod;
                }
            } catch (final Throwable t) {
                LOGGER.log(Level.WARNING, "The initialization of the deallocator for Oracle Java, Sun Java and OpenJDK has failed", t);
            }
        }
       
        @Override
        public boolean run(final ByteBuffer directByteBuffer) {
            boolean success = false;
            if (directByteBufferCleanerMethod != null && cleanerCleanOrRunMethod != null) {
                try {
                    final Object cleaner = directByteBufferCleanerMethod.invoke(directByteBuffer);
                    if (cleaner != null) {
                        cleanerCleanOrRunMethod.invoke(cleaner);
                        success = true;
                    }
                } catch (final IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
                    LOGGER.log(Level.WARNING, "The deallocation of a direct NIO buffer has failed", e);
                }
            } else if (unsafeObject != null && invokeCleanerMethod != null) {
                try {
                    invokeCleanerMethod.invoke(unsafeObject, directByteBuffer);
                } catch (final IllegalAccessException | InvocationTargetException e) {
                    LOGGER.log(Level.WARNING, "The deallocation of a direct NIO buffer has failed", e);
                }
            }
            return (success);
        }
    }

    public static class AndroidDeallocator extends Deallocator {

        private Method directByteBufferFreeMethod;

        public AndroidDeallocator() {
            super();
            try {
                final Class<?> directByteBufferClass = Class.forName("java.nio.DirectByteBuffer");
                final Method localDirectByteBufferFreeMethod = directByteBufferClass.getDeclaredMethod("free");
                localDirectByteBufferFreeMethod.setAccessible(true);
                directByteBufferFreeMethod = localDirectByteBufferFreeMethod;
            } catch (final Throwable t) {
                LOGGER.log(Level.WARNING, "The initialization of the deallocator for Android has failed", t);
            }
        }

        @Override
        public boolean run(final ByteBuffer directByteBuffer) {
            boolean success = false;
            if (directByteBufferFreeMethod != null) {
                try {
                    directByteBufferFreeMethod.invoke(directByteBuffer);
                    success = true;
                } catch (final Throwable t) {
                    LOGGER.log(Level.WARNING, "The deallocation of a direct NIO buffer has failed", t);
                }
            }
            return (success);
        }
    }

    public static class GnuClasspathDeallocator extends Deallocator {

        private Method vmDirectByteBufferFreeMethod;

        private Field bufferAddressField;

        public GnuClasspathDeallocator() {
            super();
            try {
                final Class<?> vmDirectByteBufferClass = Class.forName("java.nio.VMDirectByteBuffer");
                final Class<?> gnuClasspathPointerClass = Class.forName("gnu.classpath.Pointer");
                final Method localVmDirectByteBufferFreeMethod = vmDirectByteBufferClass.getDeclaredMethod("free",
                        gnuClasspathPointerClass);
                localVmDirectByteBufferFreeMethod.setAccessible(true);
                bufferAddressField = Buffer.class.getDeclaredField("address");
                vmDirectByteBufferFreeMethod = localVmDirectByteBufferFreeMethod;
            } catch (final Throwable t) {
                LOGGER.log(Level.WARNING, "The initialization of the deallocator for GNU Classpath has failed", t);
            }
        }

        @Override
        public boolean run(final ByteBuffer directByteBuffer) {
            boolean success = false;
            if (vmDirectByteBufferFreeMethod != null && bufferAddressField != null) {
                try {
                    bufferAddressField.setAccessible(true);
                    final Object address = bufferAddressField.get(directByteBuffer);
                    if (address != null) {
                        vmDirectByteBufferFreeMethod.invoke(null, address);
                        success = true;
                    }
                } catch (final Throwable t) {
                    LOGGER.log(Level.WARNING, "The deallocation of a direct NIO buffer has failed", t);
                }
            }
            return (success);
        }
    }

    public static class ApacheHarmonyDeallocator extends Deallocator {

        private Method directByteBufferFreeMethod;

        public ApacheHarmonyDeallocator() {
            super();
            try {
                final Class<?> directByteBufferClass = Class.forName("java.nio.DirectByteBuffer");
                final Method localDirectByteBufferFreeMethod = directByteBufferClass.getDeclaredMethod("free");
                localDirectByteBufferFreeMethod.setAccessible(true);
                directByteBufferFreeMethod = localDirectByteBufferFreeMethod;
            } catch (final Throwable t) {
                LOGGER.log(Level.WARNING, "The initialization of the deallocator for Apache Harmony has failed", t);
            }
        }

        @Override
        public boolean run(final ByteBuffer directByteBuffer) {
            boolean success = false;
            if (directByteBufferFreeMethod != null) {
                try {
                    directByteBufferFreeMethod.invoke(directByteBuffer);
                    success = true;
                } catch (final Throwable t) {
                    LOGGER.log(Level.WARNING, "The deallocation of a direct NIO buffer has failed", t);
                }
            }
            return (success);
        }
    }

    private static final Logger LOGGER = Logger.getLogger(DeallocationHelper.class.getName());

    private Map<Class<?>, Field> attachmentOrByteBufferFieldMap;

    private Set<Class<?>> deallocatableBufferClassSet;

    private Deallocator deallocator;

    /**
     * Default constructor
     */
    public DeallocationHelper() {
        this(false);
    }

    /**
     * Main constructor
     * 
     * @param ignoreClassesAndFieldsHints
     *            <code>true</code> if the known implementation details should
     *            be ignored when looking for the classes and the fields used
     *            for the native memory of the direct buffers (they are then
     *            fully recomputed at runtime which is slower but safer),
     *            otherwise <code>false</code>
     */
    public DeallocationHelper(final boolean ignoreClassesAndFieldsHints) {
        super();
        final List<Buffer> buffersToDelete = new ArrayList<>();
        /**
         * builds the map used to determine the names of the fields containing
         * the direct byte buffers. The direct read only buffers and the sliced
         * buffers and the direct buffers for other primitive types than bytes
         * store their data into some direct byte buffers. Those direct byte
         * buffers often are the only one accessing directly to the native
         * memory. That's why it's necessary to find them when a developer
         * passes a direct NIO buffer. The code below relies on numerous
         * implementation details found in some classes not available in the
         * public APIs, it's used to find the fields faster in most of the
         * cases. The class names haven't changed since Java 1.4 unlike a few
         * field names.
         */
        final Map<String, String> attachmentOrByteBufferFieldNameMap = new HashMap<>();
        final String javaVendor = System.getProperty("java.vendor");
        final String javaVersion = System.getProperty("java.version");
        if (!ignoreClassesAndFieldsHints) {
            if (javaVendor.equals("AdoptOpenJDK") || javaVendor.equals("Sun Microsystems Inc.") || javaVendor.equals("Oracle Corporation")) {
                final String java14to16DirectBufferAttachmentFieldName = "viewedBuffer";
                final String java17to19DirectBufferAttachmentFieldName = "att";
                final String byteBufferAsNonByteBufferByteBufferFieldName = "bb";
                final String[] directBufferClassnames = new String[] { "java.nio.DirectByteBuffer",
                        "java.nio.DirectByteBufferR", "java.nio.DirectCharBufferRS", "java.nio.DirectCharBufferRU",
                        "java.nio.DirectCharBufferS", "java.nio.DirectCharBufferU", "java.nio.DirectDoubleBufferRS",
                        "java.nio.DirectDoubleBufferRU", "java.nio.DirectDoubleBufferS", "java.nio.DirectDoubleBufferU",
                        "java.nio.DirectFloatBufferRS", "java.nio.DirectFloatBufferRU", "java.nio.DirectFloatBufferS",
                        "java.nio.DirectFloatBufferU", "java.nio.DirectIntBufferRS", "java.nio.DirectIntBufferRU",
                        "java.nio.DirectIntBufferS", "java.nio.DirectIntBufferU", "java.nio.DirectLongBufferRS",
                        "java.nio.DirectLongBufferRU", "java.nio.DirectLongBufferS", "java.nio.DirectLongBufferU",
                        "java.nio.DirectShortBufferRS", "java.nio.DirectShortBufferRU", "java.nio.DirectShortBufferS",
                        "java.nio.DirectShortBufferU" };
                final String[] byteBufferAsNonByteBufferClassnames = new String[] { "java.nio.ByteBufferAsCharBufferB",
                        "java.nio.ByteBufferAsCharBufferL", "java.nio.ByteBufferAsCharBufferRB",
                        "java.nio.ByteBufferAsCharBufferRL", "java.nio.ByteBufferAsDoubleBufferB",
                        "java.nio.ByteBufferAsDoubleBufferL", "java.nio.ByteBufferAsDoubleBufferRB",
                        "java.nio.ByteBufferAsDoubleBufferRL", "java.nio.ByteBufferAsFloatBufferB",
                        "java.nio.ByteBufferAsFloatBufferL", "java.nio.ByteBufferAsFloatBufferRB",
                        "java.nio.ByteBufferAsFloatBufferRL", "java.nio.ByteBufferAsIntBufferB",
                        "java.nio.ByteBufferAsIntBufferL", "java.nio.ByteBufferAsIntBufferRB",
                        "java.nio.ByteBufferAsIntBufferRL", "java.nio.ByteBufferAsLongBufferB",
                        "java.nio.ByteBufferAsLongBufferL", "java.nio.ByteBufferAsLongBufferRB",
                        "java.nio.ByteBufferAsLongBufferRL", "java.nio.ByteBufferAsShortBufferB",
                        "java.nio.ByteBufferAsShortBufferL", "java.nio.ByteBufferAsShortBufferRB",
                        "java.nio.ByteBufferAsShortBufferRL" };
                final String[] javaVersionElements = System.getProperty("java.version").split("\\.");
                final int indexOfEarlyAccessSuffix = javaVersionElements[0].lastIndexOf("-ea");
                if (indexOfEarlyAccessSuffix != -1) {
                    // drops the "-ea" suffix from the major version number for
                    // an early access build
                    javaVersionElements[0] = javaVersionElements[0].substring(0, indexOfEarlyAccessSuffix);
                }
                final int major, minor;
                if (javaVersionElements.length >= 2) {
                    major = Integer.parseInt(javaVersionElements[0]);
                    minor = Integer.parseInt(javaVersionElements[1]);
                } else {
                    major = 1;
                    minor = Integer.parseInt(javaVersionElements[0]);
                }
                final String directBufferAttachmentFieldName;
                if (minor == 1 && major <= 6)
                    directBufferAttachmentFieldName = java14to16DirectBufferAttachmentFieldName;
                else
                    directBufferAttachmentFieldName = java17to19DirectBufferAttachmentFieldName;
                for (final String directBufferClassname : directBufferClassnames)
                    attachmentOrByteBufferFieldNameMap.put(directBufferClassname, directBufferAttachmentFieldName);
                for (final String byteBufferAsNonByteBufferClassname : byteBufferAsNonByteBufferClassnames)
                    attachmentOrByteBufferFieldNameMap.put(byteBufferAsNonByteBufferClassname,
                            byteBufferAsNonByteBufferByteBufferFieldName);
            } else if (javaVendor.equals("The Android Project")) {
                final String byteBufferAsNonByteBufferByteBufferFieldName = "byteBuffer";
                final String[] byteBufferAsNonByteBufferClassnames = new String[] { "java.nio.ByteBufferAsCharBuffer",
                        "java.nio.ByteBufferAsDoubleBuffer", "java.nio.ByteBufferAsFloatBuffer",
                        "java.nio.ByteBufferAsIntBuffer", "java.nio.ByteBufferAsLongBuffer",
                        "java.nio.ByteBufferAsShortBuffer" };
                for (final String byteBufferAsNonByteBufferClassname : byteBufferAsNonByteBufferClassnames)
                    attachmentOrByteBufferFieldNameMap.put(byteBufferAsNonByteBufferClassname,
                            byteBufferAsNonByteBufferByteBufferFieldName);
            } else if (/* javaVendor.equals("Apple Inc.")|| */javaVendor.equals("Free Software Foundation, Inc.")) {
                final String byteBufferAsNonByteBufferByteBufferFieldName = "bb";
                final String[] byteBufferAsNonByteBufferClassnames = new String[] { "java.nio.CharViewBufferImpl",
                        "java.nio.DoubleViewBufferImpl", "java.nio.FloatViewBufferImpl", "java.nio.IntViewBufferImpl",
                        "java.nio.LongViewBufferImpl", "java.nio.ShortViewBufferImpl" };
                for (final String byteBufferAsNonByteBufferClassname : byteBufferAsNonByteBufferClassnames)
                    attachmentOrByteBufferFieldNameMap.put(byteBufferAsNonByteBufferClassname,
                            byteBufferAsNonByteBufferByteBufferFieldName);
            } else if (javaVendor.contains("Apache")) {
                final String byteBufferAsNonByteBufferByteBufferFieldName = "byteBuffer";
                final String[] byteBufferAsNonByteBufferClassnames = new String[] { "java.nio.CharToByteBufferAdapter",
                        "java.nio.DoubleToByteBufferAdapter", "java.nio.FloatToByteBufferAdapter",
                        "java.nio.IntToByteBufferAdapter", "java.nio.LongToByteBufferAdapter",
                        "java.nio.ShortToByteBufferAdapter" };
                for (final String byteBufferAsNonByteBufferClassname : byteBufferAsNonByteBufferClassnames)
                    attachmentOrByteBufferFieldNameMap.put(byteBufferAsNonByteBufferClassname,
                            byteBufferAsNonByteBufferByteBufferFieldName);
            } else if (javaVendor.equals("Jeroen Frijters")) {// TODO IKVM
            } else if (javaVendor.contains("IBM")) {// TODO J9
            }
        }
        // checks if these classes are in the class library
        if (!attachmentOrByteBufferFieldNameMap.isEmpty()) {
            final List<String> classnamesToRemove = new ArrayList<>();
            for (final String classname : attachmentOrByteBufferFieldNameMap.keySet())
                try {
                    Class.forName(classname);
                } catch (ClassNotFoundException cnfe) {
                    classnamesToRemove.add(classname);
                }
            for (final String classnameToRemove : classnamesToRemove)
                attachmentOrByteBufferFieldNameMap.remove(classnameToRemove);
        }
        // builds the map used to determine the fields containing the direct byte buffers
        attachmentOrByteBufferFieldMap = new HashMap<>();
        if (!attachmentOrByteBufferFieldNameMap.isEmpty())
            for (final Entry<String, String> attachmentOrByteBufferFieldNameEntry : attachmentOrByteBufferFieldNameMap
                    .entrySet()) {
                final String classname = attachmentOrByteBufferFieldNameEntry.getKey();
                final String fieldname = attachmentOrByteBufferFieldNameEntry.getValue();
                try {
                    final Class<?> bufferClass = Class.forName(classname);
                    Field bufferField = null;
                    Class<?> bufferIntermediaryClass = bufferClass;
                    final List<Class<?>> intermediaryClassWithoutBufferList = new ArrayList<>();
                    while (bufferIntermediaryClass != null) {
                        try {
                            bufferField = bufferIntermediaryClass.getDeclaredField(fieldname);
                        } catch (NoSuchFieldException nsfe) {
                            if (!bufferIntermediaryClass.equals(Object.class)
                                    && !bufferIntermediaryClass.equals(Buffer.class))
                                intermediaryClassWithoutBufferList.add(bufferIntermediaryClass);
                        }
                        bufferIntermediaryClass = bufferIntermediaryClass.getSuperclass();
                    }
                    if (bufferField == null) {
                        final String superClassesMsg;
                        if (intermediaryClassWithoutBufferList.isEmpty())
                            superClassesMsg = "";
                        else if (intermediaryClassWithoutBufferList.size() == 1)
                            superClassesMsg = " and in its super class "
                                    + intermediaryClassWithoutBufferList.get(0).getName();
                        else {
                            final StringBuilder builder = new StringBuilder();
                            builder.append(" and in its super classes");
                            int classIndex = 0;
                            for (final Class<?> intermediaryClassWithoutBuffer : intermediaryClassWithoutBufferList) {
                                builder.append(' ');
                                builder.append(intermediaryClassWithoutBuffer.getName());
                                if (classIndex < intermediaryClassWithoutBufferList.size() - 1)
                                    builder.append(',');
                                classIndex++;
                            }
                            superClassesMsg = builder.toString();
                        }
                        LOGGER.warning("The field " + fieldname + " hasn't been found in the class " + classname
                                + superClassesMsg);
                    } else {// the field has been found, stores it into the map
                        attachmentOrByteBufferFieldMap.put(bufferClass, bufferField);
                    }
                } catch (ClassNotFoundException cnfe) {
                    // TODO The Java version isn't very useful under Android as it is always zero, rather use android.os.Build.VERSION.RELEASE
                    // to show something meaningful supported since the API level 1
                    final String msg = "The class " + classname
                            + " hasn't been found while initializing the deallocator. Java vendor: " + javaVendor
                            + " Java version: " + javaVersion;
                    LOGGER.log(Level.WARNING, msg, cnfe);
                }
            }
        // if a known implementation has drastically changed or if the current
        // implementation is unknown
        if (attachmentOrByteBufferFieldNameMap.isEmpty()) {
            // detects everything with the reflection API
            // creates all possible kinds of direct NIO buffer that can contain buffers (sliced buffers and views)
            final ByteBuffer slicedBigEndianReadOnlyDirectByteBuffer = ((ByteBuffer) ByteBuffer.allocateDirect(2)
                    .order(ByteOrder.BIG_ENDIAN).put((byte) 0).put((byte) 0).position(1).limit(2)).slice()
                            .asReadOnlyBuffer();
            final ByteBuffer slicedBigEndianReadWriteDirectByteBuffer = ((ByteBuffer) ByteBuffer.allocateDirect(2)
                    .order(ByteOrder.BIG_ENDIAN).put((byte) 0).put((byte) 0).position(1).limit(2)).slice();
            final CharBuffer bigEndianReadOnlyDirectCharBuffer = ByteBuffer.allocateDirect(1)
                    .order(ByteOrder.BIG_ENDIAN).asReadOnlyBuffer().asCharBuffer();
            final CharBuffer bigEndianReadWriteDirectCharBuffer = ByteBuffer.allocateDirect(1)
                    .order(ByteOrder.BIG_ENDIAN).asCharBuffer();
            final DoubleBuffer bigEndianReadOnlyDirectDoubleBuffer = ByteBuffer.allocateDirect(1)
                    .order(ByteOrder.BIG_ENDIAN).asReadOnlyBuffer().asDoubleBuffer();
            final DoubleBuffer bigEndianReadWriteDirectDoubleBuffer = ByteBuffer.allocateDirect(1)
                    .order(ByteOrder.BIG_ENDIAN).asDoubleBuffer();
            final FloatBuffer bigEndianReadOnlyDirectFloatBuffer = ByteBuffer.allocateDirect(1)
                    .order(ByteOrder.BIG_ENDIAN).asReadOnlyBuffer().asFloatBuffer();
            final FloatBuffer bigEndianReadWriteDirectFloatBuffer = ByteBuffer.allocateDirect(1)
                    .order(ByteOrder.BIG_ENDIAN).asFloatBuffer();
            final IntBuffer bigEndianReadOnlyDirectIntBuffer = ByteBuffer.allocateDirect(1).order(ByteOrder.BIG_ENDIAN)
                    .asReadOnlyBuffer().asIntBuffer();
            final IntBuffer bigEndianReadWriteDirectIntBuffer = ByteBuffer.allocateDirect(1).order(ByteOrder.BIG_ENDIAN)
                    .asIntBuffer();
            final LongBuffer bigEndianReadOnlyDirectLongBuffer = ByteBuffer.allocateDirect(1)
                    .order(ByteOrder.BIG_ENDIAN).asReadOnlyBuffer().asLongBuffer();
            final LongBuffer bigEndianReadWriteDirectLongBuffer = ByteBuffer.allocateDirect(1)
                    .order(ByteOrder.BIG_ENDIAN).asLongBuffer();
            final ShortBuffer bigEndianReadOnlyDirectShortBuffer = ByteBuffer.allocateDirect(1)
                    .order(ByteOrder.BIG_ENDIAN).asReadOnlyBuffer().asShortBuffer();
            final ShortBuffer bigEndianReadWriteDirectShortBuffer = ByteBuffer.allocateDirect(1)
                    .order(ByteOrder.BIG_ENDIAN).asShortBuffer();
            final ByteBuffer slicedLittleEndianReadOnlyDirectByteBuffer = ((ByteBuffer) ByteBuffer.allocateDirect(2)
                    .order(ByteOrder.LITTLE_ENDIAN).put((byte) 0).put((byte) 0).position(1).limit(2)).slice()
                            .asReadOnlyBuffer();
            final ByteBuffer slicedLittleEndianReadWriteDirectByteBuffer = ((ByteBuffer) ByteBuffer.allocateDirect(2)
                    .order(ByteOrder.LITTLE_ENDIAN).put((byte) 0).put((byte) 0).position(1).limit(2)).slice();
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
            // gets the fields to access the contained buffers
            for (Buffer buffer : buffers) {
                final Class<?> bufferClass = buffer.getClass();
                if (!attachmentOrByteBufferFieldMap.containsKey(bufferClass)) {
                    Field bufferField = null;
                    Class<?> bufferIntermediaryClass = bufferClass;
                    while (bufferIntermediaryClass != null && bufferField == null) {
                        for (final Field field : bufferIntermediaryClass.getDeclaredFields()) {
                            try {
                                field.setAccessible(true);
                                final Object fieldValue = field.get(buffer);
                                if (fieldValue != null && fieldValue instanceof Buffer) {
                                    bufferField = field;
                                    break;
                                }
                            } catch (final Throwable t) {
                                LOGGER.log(Level.WARNING, "Cannot access the field " + field.getName()
                                        + " of the class " + bufferIntermediaryClass.getName(), t);
                            }
                        }
                        bufferIntermediaryClass = bufferIntermediaryClass.getSuperclass();
                    }
                    if (bufferField != null)
                        attachmentOrByteBufferFieldMap.put(bufferClass, bufferField);
                }
            }
            // cleans the mess
            buffersToDelete.addAll(buffers);
        }
        // builds the set of classes whose instances can be deallocated
        deallocatableBufferClassSet = new HashSet<>();
        if (javaVendor.equals("AdoptOpenJDK") || javaVendor.equals("Sun Microsystems Inc.") || javaVendor.equals("Oracle Corporation")
                || javaVendor.equals("The Android Project")) {
            Class<?> directByteBufferClass = null;
            final String directByteBufferClassName = "java.nio.DirectByteBuffer";
            try {
                directByteBufferClass = Class.forName(directByteBufferClassName);
            } catch (ClassNotFoundException cnfe) {
                final String msg = "The class " + directByteBufferClassName
                        + " hasn't been found while initializing the deallocator. Java vendor: " + javaVendor
                        + " Java version: " + javaVersion;
                LOGGER.log(Level.WARNING, msg, cnfe);
            }
            if (directByteBufferClass != null)
                deallocatableBufferClassSet.add(directByteBufferClass);
        } else if (/* javaVendor.equals("Apple Inc.")|| */javaVendor.equals("Free Software Foundation, Inc.")) {
            Class<?> readOnlyDirectByteBufferClass = null;
            final String readOnlyDirectByteBufferClassName = "java.nio.DirectByteBufferImpl.ReadOnly";
            try {
                readOnlyDirectByteBufferClass = Class.forName(readOnlyDirectByteBufferClassName);
            } catch (ClassNotFoundException cnfe) {
                final String msg = "The class " + readOnlyDirectByteBufferClassName
                        + " hasn't been found while initializing the deallocator. Java vendor: " + javaVendor
                        + " Java version: " + javaVersion;
                LOGGER.log(Level.WARNING, msg, cnfe);
            }
            if (readOnlyDirectByteBufferClass != null)
                deallocatableBufferClassSet.add(readOnlyDirectByteBufferClass);
            Class<?> readWriteDirectByteBufferClass = null;
            final String readWriteDirectByteBufferClassName = "java.nio.DirectByteBufferImpl.ReadWrite";
            try {
                readWriteDirectByteBufferClass = Class.forName(readWriteDirectByteBufferClassName);
            } catch (ClassNotFoundException cnfe) {
                final String msg = "The class " + readWriteDirectByteBufferClassName
                        + " hasn't been found while initializing the deallocator. Java vendor: " + javaVendor
                        + " Java version: " + javaVersion;
                LOGGER.log(Level.WARNING, msg, cnfe);
            }
            if (readWriteDirectByteBufferClass != null)
                deallocatableBufferClassSet.add(readWriteDirectByteBufferClass);
        } else if (javaVendor.contains("Apache")) {
            Class<?> readOnlyDirectByteBufferClass = null;
            final String readOnlyDirectByteBufferClassName = "java.nio.ReadOnlyDirectByteBuffer";
            try {
                readOnlyDirectByteBufferClass = Class.forName(readOnlyDirectByteBufferClassName);
            } catch (ClassNotFoundException cnfe) {
                final String msg = "The class " + readOnlyDirectByteBufferClassName
                        + " hasn't been found while initializing the deallocator. Java vendor: " + javaVendor
                        + " Java version: " + javaVersion;
                LOGGER.log(Level.WARNING, msg, cnfe);
            }
            if (readOnlyDirectByteBufferClass != null)
                deallocatableBufferClassSet.add(readOnlyDirectByteBufferClass);
            Class<?> readWriteDirectByteBufferClass = null;
            final String readWriteDirectByteBufferClassName = "java.nio.ReadWriteDirectByteBuffer";
            try {
                readWriteDirectByteBufferClass = Class.forName(readWriteDirectByteBufferClassName);
            } catch (ClassNotFoundException cnfe) {
                final String msg = "The class " + readWriteDirectByteBufferClassName
                        + " hasn't been found while initializing the deallocator. Java vendor: " + javaVendor
                        + " Java version: " + javaVersion;
                LOGGER.log(Level.WARNING, msg, cnfe);
            }
            if (readWriteDirectByteBufferClass != null)
                deallocatableBufferClassSet.add(readWriteDirectByteBufferClass);
        } else if (javaVendor.equals("Jeroen Frijters")) {// TODO IKVM
        } else if (javaVendor.contains("IBM")) {// TODO J9
        }
        // if there is no known implementation class of the direct byte buffers
        if (deallocatableBufferClassSet.isEmpty()) {// creates a read write direct byte buffer
            final ByteBuffer dummyReadWriteDirectByteBuffer = ByteBuffer.allocateDirect(1);
            // gets its class
            final Class<?> readWriteDirectByteBufferClass = dummyReadWriteDirectByteBuffer.getClass();
            // stores this class
            deallocatableBufferClassSet.add(readWriteDirectByteBufferClass);
            // cleans the mess
            buffersToDelete.add(dummyReadWriteDirectByteBuffer);
            // creates a read only direct byte buffer
            final ByteBuffer dummyReadOnlyDirectByteBuffer = ByteBuffer.allocateDirect(1).asReadOnlyBuffer();
            // gets its class
            final Class<?> readOnlyDirectByteBufferClass = dummyReadOnlyDirectByteBuffer.getClass();
            // stores this class
            deallocatableBufferClassSet.add(readOnlyDirectByteBufferClass);
            // cleans the mess
            buffersToDelete.add(dummyReadOnlyDirectByteBuffer);
        }
        // builds the deallocator responsible for releasing the native memory of
        // a deallocatable byte buffer
        if (javaVendor.equals("AdoptOpenJDK") || javaVendor.equals("Sun Microsystems Inc.") || javaVendor.equals("Oracle Corporation"))
            deallocator = new OracleSunOpenJdkDeallocator();
        else if (javaVendor.equals("The Android Project"))
            deallocator = new AndroidDeallocator();
        else if (/* javaVendor.equals("Apple Inc.")|| */javaVendor.equals("Free Software Foundation, Inc."))
            deallocator = new GnuClasspathDeallocator();
        else if (javaVendor.contains("Apache"))
            deallocator = new ApacheHarmonyDeallocator();
        else if (javaVendor.equals("Jeroen Frijters")) {// TODO IKVM
            deallocator = null;
        } else if (javaVendor.contains("IBM")) {// TODO J9
            deallocator = null;
        } else
            deallocator = null;
        // final cleanup
        for (final Buffer bufferToDelete : buffersToDelete)
            deallocate(bufferToDelete);
    }

    public ByteBuffer findDeallocatableBuffer(Buffer buffer) {
        final ByteBuffer deallocatableDirectByteBuffer;
        // looks only for the direct buffers
        if (buffer != null && buffer.isDirect()) {// looks for any contained
                                                  // buffer in the passed buffer
            final Class<?> bufferClass = buffer.getClass();
            final Field attachmentOrByteBufferField = attachmentOrByteBufferFieldMap == null ? null
                    : attachmentOrByteBufferFieldMap.get(bufferClass);
            final Buffer attachmentBufferOrByteBuffer;
            if (attachmentOrByteBufferField == null)
                attachmentBufferOrByteBuffer = null;
            else {
                Object attachedObjectOrByteBuffer;
                try {
                    attachmentOrByteBufferField.setAccessible(true);
                    attachedObjectOrByteBuffer = attachmentOrByteBufferField.get(buffer);
                } catch (IllegalArgumentException | IllegalAccessException iae) {
                    attachedObjectOrByteBuffer = null;
                }
                if (attachedObjectOrByteBuffer instanceof Buffer)
                    attachmentBufferOrByteBuffer = (Buffer) attachedObjectOrByteBuffer;
                else
                    attachmentBufferOrByteBuffer = null;
            }
            // if there is no buffer inside the buffer given in input
            if (attachmentBufferOrByteBuffer == null) {// if it's a direct byte
                                                       // buffer and if it's an
                                                       // instance of
                                                       // a deallocatable buffer
                                                       // class
                if (buffer instanceof ByteBuffer && deallocatableBufferClassSet.contains(bufferClass))
                    deallocatableDirectByteBuffer = (ByteBuffer) buffer;
                else {// it's not a byte buffer or it's not a
                      // deallocatable buffer
                    deallocatableDirectByteBuffer = null;
                    final String bufferClassName = bufferClass.getName();
                    LOGGER.warning("No deallocatable buffer has been found for an instance of the class "
                            + bufferClassName + " whereas it is a direct NIO buffer");
                }
            } else {// the passed buffer contains another buffer, looks for a
                    // deallocatable buffer inside it
                deallocatableDirectByteBuffer = findDeallocatableBuffer(attachmentBufferOrByteBuffer);
            }
        } else {// there is no need to clean the heap based buffers
            deallocatableDirectByteBuffer = null;
        }
        return deallocatableDirectByteBuffer;
    }

    public void deallocate(final Buffer buffer) {
        if (deallocator != null) {
            final ByteBuffer deallocatableBuffer = findDeallocatableBuffer(buffer);
            if (deallocatableBuffer != null)
                deallocator.run(deallocatableBuffer);
        }
    }

    public Deallocator getDeallocator() {
        return (deallocator);
    }

    public void setDeallocator(Deallocator deallocator) {
        this.deallocator = deallocator;
    }

    public Map<Class<?>, Field> getAttachmentOrByteBufferFieldMap() {
        return (attachmentOrByteBufferFieldMap);
    }

    public void setAttachmentOrByteBufferFieldMap(Map<Class<?>, Field> attachmentOrByteBufferFieldMap) {
        this.attachmentOrByteBufferFieldMap = attachmentOrByteBufferFieldMap;
    }

    public Set<Class<?>> getDeallocatableBufferClassSet() {
        return (deallocatableBufferClassSet);
    }

    public void setDeallocatableBufferClassSet(Set<Class<?>> deallocatableBufferClassSet) {
        this.deallocatableBufferClassSet = deallocatableBufferClassSet;
    }
}
