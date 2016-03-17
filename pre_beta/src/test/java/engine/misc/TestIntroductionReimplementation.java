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
package engine.misc;

import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;

import com.ardor3d.image.Image;
import com.ardor3d.image.util.ImageLoaderUtil;
import com.ardor3d.image.util.ImageUtils;
import com.ardor3d.image.util.jogl.JoglImageLoader;
import com.ardor3d.scenegraph.FloatBufferData;
import com.ardor3d.scenegraph.Mesh;
import com.ardor3d.scenegraph.MeshData;
import com.ardor3d.scenegraph.controller.ComplexSpatialController;
import com.ardor3d.scenegraph.controller.ComplexSpatialController.RepeatType;
import com.ardor3d.scenegraph.extension.SwitchNode;
import com.ardor3d.util.export.InputCapsule;
import com.ardor3d.util.export.OutputCapsule;
import com.ardor3d.util.export.binary.BinaryClassObject;
import com.ardor3d.util.export.binary.BinaryExporter;
import com.ardor3d.util.export.binary.BinaryIdContentPair;
import com.ardor3d.util.export.binary.BinaryOutputCapsule;
import com.ardor3d.util.geom.BufferUtils;
import com.ardor3d.util.resource.URLResourceSource;

import jfpsm.ArrayHelper;
import jfpsm.ArrayHelper.OccupancyCheck;
import jfpsm.ArrayHelper.Vector2i;

/**
 * The introduction state shows a map of Europe becoming red, it modifies the
 * texture data at runtime which seems to be slow on some hardware. This test
 * tries to replace the texture data updates by some VBOs.
 * 
 * @author Julien Gouesse
 *
 */
public class TestIntroductionReimplementation {

    private static final String textureFilePath = "/images/introduction.png";

    public static void main(final String[] args) {
        final int durationInSeconds = 10;
        final int framesPerSecond = 30;
        final URLResourceSource source = new URLResourceSource(
                TestIntroductionReimplementation.class.getResource(textureFilePath));
        final File[] imageFiles = findImages(source);
        // final Image[] introImages = loadImages(source);
        computeUntexturedKeyframeSwitchNodeFromImagesAndKeyframeInfos(source, durationInSeconds, framesPerSecond,
                imageFiles);
    }

    private static final File[] findImages(final URLResourceSource source) {
        try {
            final File sourceImageFile = new File(source.getURL().toURI());
            final String imageName = sourceImageFile.getName().substring(0, sourceImageFile.getName().lastIndexOf('.'));
            final File imageParentDir = sourceImageFile.getParentFile();
            final File[] imageFiles = imageParentDir.listFiles(new FilenameFilter() {

                @Override
                public boolean accept(File dir, String name) {
                    boolean accepted = false;
                    if (name.startsWith(imageName) && name.endsWith(".png")) {
                        try {
                            Integer.parseInt(name.substring(imageName.length(), name.length() - 4));
                            accepted = true;
                        } catch (NumberFormatException nfe) {
                        }
                    }
                    return accepted;
                }
            });
            if (imageFiles != null) {
                Arrays.sort(imageFiles, new Comparator<File>() {
                    @Override
                    public int compare(File f1, File f2) {
                        final int n1 = Integer
                                .parseInt(f1.getName().substring(imageName.length(), f1.getName().length() - 4));
                        final int n2 = Integer
                                .parseInt(f2.getName().substring(imageName.length(), f2.getName().length() - 4));
                        return Integer.compare(n1, n2);
                    }
                });
            }
            return imageFiles;
        } catch (URISyntaxException urise) {
            urise.printStackTrace();
        }
        return null;
    }

    private static final String getMemory(final long totalBytes) {
        long bytes = totalBytes;
        long kiloBytes = bytes / 1000;
        bytes -= kiloBytes * 1000;
        long megaBytes = kiloBytes / 1000;
        kiloBytes -= megaBytes * 1000;
        final long gigaBytes = megaBytes / 1000;
        megaBytes -= gigaBytes * 1000;
        return gigaBytes + " GB " + megaBytes + " MB " + kiloBytes + " KB " + bytes + " B";
    }
    
    private static final void computeUntexturedKeyframeSwitchNodeFromImagesAndKeyframeInfos(
            final URLResourceSource source, final int durationInSeconds, final int framesPerSecond,
            final File[] imageFiles) {
        final int frameCount = durationInSeconds * framesPerSecond;
        // the generic treatment starts here
        final List<MeshData> meshDataList = new ArrayList<>();
        System.out.println("[START] Compute key frames");
        // creates the array helper
        final ArrayHelper arrayHelper = new ArrayHelper();
        // creates a map to store the full arrays and the occupancy maps of the
        // previous frame
        final Map<ArrayHelper.OccupancyMap, Map<Vector2i, Integer[][]>> previousFramePixelArrayOccupancyMapMap = new LinkedHashMap<>();
        // prepares the image loaders
        JoglImageLoader.registerLoader();
        ImageLoaderUtil.registerDefaultHandler(new JoglImageLoader());
        // avoids using the memory on the native heap, uses the Java heap
        JoglImageLoader.createOnHeap = true;
        // for each frame
        for (int frameIndex = 0; frameIndex < frameCount; frameIndex++) {
            final long startTime = System.currentTimeMillis();
            final File imageFile = imageFiles[frameIndex];
            System.out.println("[START] Load image " + imageFile.getAbsolutePath());
            // retrieves the image of the frame
            final String imageType = imageFile.getName().substring(imageFile.getName().lastIndexOf('.'));
            Image image = null;
            try (final FileInputStream imageInputStream = new FileInputStream(imageFile)) {
                image = ImageLoaderUtil.loadImage(imageType, imageInputStream, false);
            } catch (IOException ioe) {
                throw new RuntimeException("The image file " + imageFile.getAbsolutePath() + " cannot be found", ioe);
            }
            System.out.println("[ END ] Load image " + imageFile.getAbsolutePath());
            System.out.println("[START] Compute key frame " + frameIndex);
            System.out.println("[     ] Used memory: " + getMemory(Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()));
            // retrieves the pixels of the image and computes the list of colors
            // in this array
            final Integer[][] pixels = new Integer[image.getWidth()][image.getHeight()];
            final Set<Integer> frameColors = new HashSet<>();
            int skippedTransparentPixelCount = 0;
            for (int y = 0; y < image.getHeight(); y++) {
                for (int x = 0; x < image.getWidth(); x++) {
                    // gets the ARGB value of the pixel
                    final int argb = ImageUtils.getARGB(image, x, y);
                    final int alpha = (byte) (argb >> 24) & 0xFF;
                    // keeps only non fully transparent pixels
                    if (alpha > 0) {
                        pixels[x][y] = Integer.valueOf(argb);
                        frameColors.add(Integer.valueOf(argb));
                    } else {
                        skippedTransparentPixelCount++;
                    }
                }
            }
            final Map<Vector2i, Integer[][]> globalDistinctColorsPixelsArraysMap = new HashMap<>();
            int totalOccupiedCellCount = 0;
            // for each color
            for (final Integer frameColor : frameColors) {
                // builds an occupancy check to keep the pixels of a single
                // color
                final OccupancyCheck<Integer> colorFilterOccupancyCheck = new IntegerFilterOccupancyCheck(frameColor);
                // creates the occupancy map
                final ArrayHelper.OccupancyMap occupancyMap = arrayHelper.createPackedOccupancyMap(pixels,
                        colorFilterOccupancyCheck);
                System.out.println("[ ... ] Occupied cell count " + occupancyMap.getOccupiedCellCount());
                totalOccupiedCellCount += occupancyMap.getOccupiedCellCount();
                // looks for a full arrays map matching with this occupancy map
                Map<Vector2i, Integer[][]> localDistinctColorsPixelsArraysMap = previousFramePixelArrayOccupancyMapMap
                        .remove(occupancyMap);
                if (localDistinctColorsPixelsArraysMap == null) {
                    // uses a clone to avoid using a mutable instance as a key
                    final ArrayHelper.OccupancyMap occupancyMapClone = occupancyMap.clone();
                    // uses it to build some full arrays with distinct colors
                    // (without fully transparent pixels)
                    localDistinctColorsPixelsArraysMap = arrayHelper.computeFullArraysFromNonFullArray(pixels,
                            occupancyMap);
                    System.out.println("[ ... ] Occupied cell count " + occupancyMap.getOccupiedCellCount());
                    // stores the computed full arrays map
                    previousFramePixelArrayOccupancyMapMap.put(occupancyMapClone, localDistinctColorsPixelsArraysMap);
                } else {
                    // there is no need to clone the occupancy map as it hasn't
                    // been modified
                    // stores the full arrays map
                    previousFramePixelArrayOccupancyMapMap.put(occupancyMap, localDistinctColorsPixelsArraysMap);
                    System.out.println("[ ... ] Reuse a full array map");
                }
                for (final Entry<Vector2i, Integer[][]> localDistinctColorsPixelsArraysEntry : localDistinctColorsPixelsArraysMap
                        .entrySet()) {
                    // retrieves where the pixels come from
                    final Vector2i location = localDistinctColorsPixelsArraysEntry.getKey();
                    // retrieves the non fully transparent pixels
                    final Integer[][] localDistinctColorsPixels = localDistinctColorsPixelsArraysEntry.getValue();
                    // stores the full array with distinct colors
                    if (globalDistinctColorsPixelsArraysMap.put(location, localDistinctColorsPixels) != null) {
                        System.err.println("[WARN ] Collision at " + location.getX() + " " + location.getY());
                    }
                }
            }
            // removes a full array of the previous frame, keeps only the full
            // arrays of the current frame
            final int currentFrameEntryCount = frameColors.size();
            int previousFrameEntryCount = previousFramePixelArrayOccupancyMapMap.size() - currentFrameEntryCount;
            while (previousFrameEntryCount > 0) {
                previousFramePixelArrayOccupancyMapMap
                        .remove(previousFramePixelArrayOccupancyMapMap.keySet().iterator().next());
                previousFrameEntryCount--;
            }
            System.out.println("[ ... ] Total occupied cell count " + totalOccupiedCellCount);
            int pixelCount = 0;
            // for each array of pixels of the same color
            for (final Integer[][] globalDictinctColorPixels : globalDistinctColorsPixelsArraysMap.values()) {
                // the array is full (all rows and all columns have respectively
                // the same sizes)
                pixelCount += globalDictinctColorPixels.length * globalDictinctColorPixels[0].length;
            }
            final int maxPixelCount = image.getWidth() * image.getHeight();
            if (pixelCount == maxPixelCount) {
                System.out.println("[     ] Key frame " + frameIndex + " total pixel coverage");
            } else {
                final int skippedPixelCount = maxPixelCount - pixelCount;
                System.out.println("[     ] Key frame " + frameIndex + " pixel coverage " + pixelCount + " pixels on "
                        + maxPixelCount + ". " + skippedTransparentPixelCount + " transparent pixels, skipped pixels "
                        + skippedPixelCount);
            }
            // computes the triangle count
            final int triCount = 2 * globalDistinctColorsPixelsArraysMap.size();
            System.out.println("[     ] Key frame " + frameIndex + " triangle count: " + triCount);
            final MeshData meshData = new MeshData();
            // creates the vertex buffer (indirect NIO buffer), triangle count *
            // number of vertices per triangle * floats per vertex (only 2 as
            // we're in 2D)
            final FloatBuffer vertexBuffer = BufferUtils.createFloatBufferOnHeap(triCount * 3 * 2);
            final FloatBufferData vertexBufferData = new FloatBufferData(vertexBuffer, 2);
            // sets this vertex buffer to the mesh data
            meshData.setVertexCoords(vertexBufferData);
            // creates the color buffer (indirect NIO buffer), triangle count *
            // number of vertices per triangle * floats per color (no alpha,
            // RGB)
            final FloatBuffer colorBuffer = BufferUtils.createFloatBufferOnHeap(triCount * 3 * 3);
            final FloatBufferData colorBufferData = new FloatBufferData(colorBuffer, 3);
            // sets this color buffer to the mesh data
            meshData.setColorCoords(colorBufferData);
            // adds the vertices and the colors of the arrays into the vertex
            // buffer and the color buffer
            for (final Entry<Vector2i, Integer[][]> globalDictinctColorPixelsEntry : globalDistinctColorsPixelsArraysMap
                    .entrySet()) {
                // retrieves where the pixels come from
                final Vector2i location = globalDictinctColorPixelsEntry.getKey();
                // retrieves the pixels of the same color
                final Integer[][] globalDictinctColorPixels = globalDictinctColorPixelsEntry.getValue();
                final int height = globalDictinctColorPixels.length;
                final int width = globalDictinctColorPixels[0].length;
                // computes the coordinates of the 4 vertices
                final int x0 = location.getX();
                final int y0 = location.getY();
                final int x1 = location.getX() + width;
                final int y1 = location.getY();
                final int x2 = location.getX() + width;
                final int y2 = location.getY() + height;
                final int x3 = location.getX();
                final int y3 = location.getY() + height;
                // puts the vertices into the vertex buffer to build a pair of
                // triangles
                vertexBuffer.put(x0).put(y0).put(x1).put(y1).put(x2).put(y2).put(x2).put(y2).put(x3).put(y3).put(x0)
                        .put(y0);
                // retrieves the ARGB color
                final int argb = globalDictinctColorPixels[0][0].intValue();
                final int red = (byte) (argb >> 16) & 0xFF;
                final int green = (byte) (argb >> 8) & 0xFF;
                final int blue = (byte) argb & 0xFF;
                // puts the color into the color buffer
                colorBuffer.put(red).put(green).put(blue);
                colorBuffer.put(red).put(green).put(blue);
                colorBuffer.put(red).put(green).put(blue);
                colorBuffer.put(red).put(green).put(blue);
                colorBuffer.put(red).put(green).put(blue);
                colorBuffer.put(red).put(green).put(blue);
            }
            vertexBuffer.rewind();
            colorBuffer.rewind();
            meshDataList.add(meshData);
            System.out.println("[     ] Used memory: " + getMemory(Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()));
            System.out.println("[ END ] Compute key frame " + frameIndex);
            final long endTime = System.currentTimeMillis();
            long keyFrameComputationDurationInMillis = endTime - startTime;
            long keyFrameComputationDurationInSeconds = keyFrameComputationDurationInMillis / 1000;
            keyFrameComputationDurationInMillis -= keyFrameComputationDurationInSeconds * 1000;
            long keyFrameComputationDurationInMinutes = keyFrameComputationDurationInSeconds / 60;
            keyFrameComputationDurationInSeconds -= keyFrameComputationDurationInMinutes * 60;
            System.out.println("[     ] Key frame compute time " + keyFrameComputationDurationInMinutes + " m "
                    + keyFrameComputationDurationInSeconds + " s " + keyFrameComputationDurationInMillis);
        }
        System.out.println("[ END ] Compute key frames");
        System.out.println("[START] Normalize vertex coordinates");
        // computes the minimal and maximal values of the vertex coordinates,
        // then normalize them
        float minx = Float.POSITIVE_INFINITY, miny = Float.POSITIVE_INFINITY, minz = Float.POSITIVE_INFINITY;
        float maxx = Float.NEGATIVE_INFINITY, maxy = Float.NEGATIVE_INFINITY, maxz = Float.NEGATIVE_INFINITY;
        for (final MeshData meshData : meshDataList) {
            final FloatBuffer vertexBuffer = meshData.getVertexBuffer();
            while (vertexBuffer.hasRemaining()) {
                float x = vertexBuffer.get(), y = vertexBuffer.get(), z = vertexBuffer.get();
                minx = Math.min(minx, x);
                miny = Math.min(miny, y);
                minz = Math.min(minz, z);
                maxx = Math.max(maxx, x);
                maxy = Math.max(maxy, y);
                maxz = Math.max(maxz, z);
            }
            vertexBuffer.rewind();
        }
        // normalizes them
        final float xdiff = maxx - minx;
        final float ydiff = maxy - miny;
        final float zdiff = maxz - minz;
        for (final MeshData meshData : meshDataList) {
            final FloatBuffer vertexBuffer = meshData.getVertexBuffer();
            while (vertexBuffer.hasRemaining()) {
                final int pos = vertexBuffer.position();
                float x = vertexBuffer.get(), y = vertexBuffer.get(), z = vertexBuffer.get();
                vertexBuffer.position(pos);
                vertexBuffer.put(xdiff == 0 ? x - minx : (x - minx) / xdiff);
                vertexBuffer.put(ydiff == 0 ? y - miny : (y - miny) / ydiff);
                vertexBuffer.put(zdiff == 0 ? z - minz : (z - minz) / zdiff);
            }
            vertexBuffer.rewind();
        }
        System.out.println("[ END ] Normalize vertex coordinates");
        // creates a SwitchNode containing all meshes of the frames
        System.out.println("[START] Build switch node");
        final SwitchNode switchNode = new SwitchNode();
        int frameIndex = 0;
        for (final MeshData meshData : meshDataList) {
            // creates a Mesh for this frame
            final Mesh mesh = new Mesh("frame n°" + frameIndex);
            mesh.setMeshData(meshData);
            // adds it into the SwitchNode
            switchNode.attachChild(mesh);
            frameIndex++;
        }
        // creates a custom controller that just picks a frame with no
        // interpolation
        final BasicKeyframeController controller = new BasicKeyframeController(framesPerSecond);
        controller.setRepeatType(RepeatType.CLAMP);
        controller.setActive(true);
        controller.setMinTime(0);
        controller.setMaxTime(durationInSeconds);
        switchNode.addController(controller);
        System.out.println("[ END ] Build switch node");
        System.out.println("[START] Save switch node");
        BinaryExporter binaryExporter = new DirectBinaryExporter();
        try {
            final File directory = new File(source.getURL().toURI()).getParentFile();
            final File destFile = new File(directory, "introduction.abin");
            binaryExporter.save(switchNode, destFile);
        } catch (URISyntaxException | IOException e) {
            e.printStackTrace();
        }
        System.out.println("[ END ] Save switch node");
    }

    private static final class DirectBinaryExporter extends BinaryExporter {
        @Override
        protected BinaryIdContentPair generateIdContentPair(final BinaryClassObject bco) {
            final BinaryIdContentPair pair = new BinaryIdContentPair(_idCount++,
                    new BinaryOutputCapsule(this, bco, true));
            return pair;
        }
    }

    public static final class BasicKeyframeController extends ComplexSpatialController<SwitchNode> {

        private static final long serialVersionUID = 1L;

        private double currentTime;

        private int framesPerSecond;

        public BasicKeyframeController() {
            this(0);
        }

        public BasicKeyframeController(final int framesPerSecond) {
            super();
            this.currentTime = 0;
            this.framesPerSecond = framesPerSecond;
        }

        @Override
        public final void update(final double time, final SwitchNode caller) {
            currentTime += time;
            final int frameCount = (int) (getMaxTime() * framesPerSecond);
            final int frameIndex;
            switch (getRepeatType()) {
            case CLAMP: {
                frameIndex = Math.min(frameCount - 1, (int) (Math.min(currentTime, getMaxTime()) * framesPerSecond));
                break;
            }
            case WRAP: {
                frameIndex = ((int) (currentTime * framesPerSecond)) % frameCount;
                break;
            }
            case CYCLE: {
                final int tmpFrameIndex = (int) (currentTime * framesPerSecond);
                if ((tmpFrameIndex / frameCount) % 2 == 0)
                    frameIndex = tmpFrameIndex % frameCount;
                else
                    frameIndex = (frameCount - 1) - tmpFrameIndex % frameCount;
                break;
            }
            default:
                // it should never happen
                frameIndex = 0;
            }
            caller.setSingleVisible(frameIndex);
        }

        public double getCurrentTime() {
            return currentTime;
        }

        public void setCurrentTime(final double currentTime) {
            this.currentTime = currentTime;
        }

        @Override
        public void read(final InputCapsule capsule) throws IOException {
            super.read(capsule);
            framesPerSecond = capsule.readInt("framesPerSecond", 0);
        }

        @Override
        public void write(final OutputCapsule capsule) throws IOException {
            super.write(capsule);
            capsule.write(framesPerSecond, "framesPerSecond", 0);
        }
    }

    private static final class IntegerFilterOccupancyCheck implements OccupancyCheck<Integer> {

        private final Integer integerFilter;

        private IntegerFilterOccupancyCheck(final Integer integerFilter) {
            super();
            this.integerFilter = integerFilter;
        }

        @Override
        public final boolean isOccupied(final Integer value) {
            return (Objects.equals(value, integerFilter));
        }

        @Override
        public boolean equals(Object o) {
            return (o != null && o.getClass() == IntegerFilterOccupancyCheck.class
                    && Objects.equals(((IntegerFilterOccupancyCheck) o).integerFilter, this.integerFilter));
        }

        @Override
        public int hashCode() {
            return (integerFilter == null ? 0 : integerFilter.hashCode());
        }
    }
}
