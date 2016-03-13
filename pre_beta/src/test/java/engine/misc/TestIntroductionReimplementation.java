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
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;

import com.ardor3d.image.Image;
import com.ardor3d.image.Texture;
import com.ardor3d.image.util.ImageUtils;
import com.ardor3d.image.util.jogl.JoglImageLoader;
import com.ardor3d.scenegraph.FloatBufferData;
import com.ardor3d.scenegraph.Mesh;
import com.ardor3d.scenegraph.MeshData;
import com.ardor3d.scenegraph.controller.ComplexSpatialController;
import com.ardor3d.scenegraph.controller.ComplexSpatialController.RepeatType;
import com.ardor3d.scenegraph.extension.SwitchNode;
import com.ardor3d.util.TextureManager;
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
        final Image[] introImages = loadImages(source);
        computeUntexturedKeyframeSwitchNodeFromImagesAndKeyframeInfos(source, durationInSeconds, framesPerSecond, introImages);
    }

    private static final Image[] loadImages(final URLResourceSource source) {
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
                            Integer.parseInt(name.substring(imageName.length(),name.length() - 4));
                            accepted = true;
                        } catch (NumberFormatException nfe) {
                        }
                    }
                    return accepted;
                }
            });
            final Image[] images;
            if (imageFiles == null) {
                images = null;
            } else {
                images = new Image[imageFiles.length];
                Arrays.sort(imageFiles, new Comparator<File>() {
                    @Override
                    public int compare(File f1, File f2) {
                        final int n1 = Integer.parseInt(f1.getName().substring(imageName.length(),f1.getName().length() - 4));
                        final int n2 = Integer.parseInt(f2.getName().substring(imageName.length(),f2.getName().length() - 4));
                        return Integer.compare(n1, n2);
                    }
                });
                JoglImageLoader.registerLoader();
                System.out.println("[START] Load textures");
                for (int imageIndex = 0 ; imageIndex < imageFiles.length ; imageIndex++ ) {
                    final URLResourceSource imageSource = new URLResourceSource(imageFiles[imageIndex].toURI().toURL());
                    System.out.println("[START] Load texture " + imageFiles[imageIndex].getAbsolutePath());
                    final Texture texture = TextureManager.load(imageSource, Texture.MinificationFilter.Trilinear, false);
                    System.out.println("[ END ] Load texture " + imageFiles[imageIndex].getAbsolutePath());
                    images[imageIndex] = texture.getImage();
                }
                System.out.println("[ END ] Load textures");
            }
            return images;
        } catch (URISyntaxException|MalformedURLException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static final void computeUntexturedKeyframeSwitchNodeFromImagesAndKeyframeInfos(final URLResourceSource source, final int durationInSeconds,
            final int framesPerSecond, final Image[] introImages) {
        final int frameCount = durationInSeconds * framesPerSecond;
        // the generic treatment starts here
        final List<MeshData> meshDataList = new ArrayList<>();
        System.out.println("[START] Compute key frames");
        // creates the array helper
        final ArrayHelper arrayHelper = new ArrayHelper();
        // creates a map to store the full arrays of the occupancy maps
        final Map<ArrayHelper.OccupancyMap, Map<Vector2i, Integer[][]>> pixelsArrayOccupancyMapMap = new HashMap<>();
        // for each frame
        for (int frameIndex = 0; frameIndex < frameCount; frameIndex++) {
            final long startTime = System.currentTimeMillis();
            System.out.println("[START] Compute key frame " + frameIndex);
            // retrieves the image of the frame
            final Image image = introImages[frameIndex];
            // retrieves the pixels of the image and computes the list of colors
            // in this array
            final Integer[][] pixels = new Integer[image.getWidth()][image.getHeight()];
            final Set<Integer> frameColors = new HashSet<>();
            for (int y = 0; y < image.getHeight(); y++) {
                for (int x = 0; x < image.getWidth(); x++) {
                    // gets the ARGB value of the pixel
                    final int argb = ImageUtils.getARGB(image, x, y);
                    final int alpha = (byte) (argb >> 24) & 0xFF;
                    // keeps only non fully transparent pixels
                    if (alpha > 0) {
                        pixels[x][y] = Integer.valueOf(argb);
                        frameColors.add(Integer.valueOf(argb));
                    }
                }
            }
            final Map<Vector2i, Integer[][]> globalDistinctColorsPixelsArraysMap = new HashMap<>();
            // for each color
            for (final Integer frameColor : frameColors) {
                // builds an occupancy check to keep the pixels of a single
                // color
                final OccupancyCheck<Integer> colorFilterOccupancyCheck = new IntegerFilterOccupancyCheck(frameColor);
                // creates the occupancy map
                final ArrayHelper.OccupancyMap occupancyMap = arrayHelper.createPackedOccupancyMap(pixels,
                        colorFilterOccupancyCheck);
                // looks for a full arrays map matching with this occupancy map
                Map<Vector2i, Integer[][]> localDistinctColorsPixelsArraysMap = pixelsArrayOccupancyMapMap
                        .get(occupancyMap);
                if (localDistinctColorsPixelsArraysMap == null) {
                    // uses a clone to avoid using a mutable instance as a key
                    final ArrayHelper.OccupancyMap occupancyMapClone = occupancyMap.clone();
                    // uses it to build some full arrays with distinct colors
                    // (without fully transparent pixels)
                    localDistinctColorsPixelsArraysMap = arrayHelper.computeFullArraysFromNonFullArray(pixels,
                            occupancyMap);
                    // stores the computed full arrays map
                    pixelsArrayOccupancyMapMap.put(occupancyMapClone, localDistinctColorsPixelsArraysMap);
                } else {
                    System.out.println("[ ... ] Reuse a full array map");
                }
                for (final Entry<Vector2i, Integer[][]> localDistinctColorsPixelsArraysEntry : localDistinctColorsPixelsArraysMap
                        .entrySet()) {// retrieves where the pixels come from
                    final Vector2i location = localDistinctColorsPixelsArraysEntry.getKey();
                    // retrieves the non fully transparent pixels
                    final Integer[][] localDistinctColorsPixels = localDistinctColorsPixelsArraysEntry.getValue();
                    // stores the full array with distinct colors
                    globalDistinctColorsPixelsArraysMap.put(location, localDistinctColorsPixels);
                }
            }
            // computes the triangle count
            int triCount = 0;
            // for each array of pixels of the same color
            //for (final Integer[][] globalDictinctColorPixels : globalDistinctColorsPixelsArraysMap.values()) {
                // one pair of triangles per pixel, the array is full (all rows
                // and all columns have respectively the same sizes)
                //triCount += (globalDictinctColorPixels.length * globalDictinctColorPixels[0].length) * 2;
            //}
            triCount = 2 * globalDistinctColorsPixelsArraysMap.size();
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
            System.out.println("[ END ] Compute key frame " + frameIndex);
            final long endTime=System.currentTimeMillis();
            long keyFrameComputationDurationInMillis = endTime - startTime;
            long keyFrameComputationDurationInSeconds = keyFrameComputationDurationInMillis / 1000;
            keyFrameComputationDurationInMillis -= keyFrameComputationDurationInSeconds * 1000;
            long keyFrameComputationDurationInMinutes = keyFrameComputationDurationInSeconds / 60;
            keyFrameComputationDurationInSeconds -= keyFrameComputationDurationInMinutes * 60;
            System.out.println("[     ] Key frame compute time " + keyFrameComputationDurationInMinutes + " m " + keyFrameComputationDurationInSeconds + " s " + keyFrameComputationDurationInMillis);
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

    private static final class BasicKeyframeController extends ComplexSpatialController<SwitchNode> {

        private static final long serialVersionUID = 1L;

        private double startTime;

        private final int framesPerSecond;

        private BasicKeyframeController(final int framesPerSecond) {
            super();
            startTime = Double.NaN;
            this.framesPerSecond = framesPerSecond;
        }

        @Override
        public final void update(final double time, final SwitchNode caller) {
            if (Double.isNaN(startTime))
                startTime = Double.valueOf(time);
            final double elapsedTime = time - startTime;
            final int frameCount = (int) (getMaxTime() * framesPerSecond);
            final int frameIndex;
            switch (getRepeatType()) {
            case CLAMP: {
                frameIndex = Math.min(frameCount - 1, (int) (Math.min(elapsedTime, getMaxTime()) * framesPerSecond));
                break;
            }
            case WRAP: {
                frameIndex = ((int) (elapsedTime * framesPerSecond)) % frameCount;
                break;
            }
            case CYCLE: {
                final int tmpFrameIndex = (int) (elapsedTime * framesPerSecond);
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
