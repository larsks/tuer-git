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

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map.Entry;

import javax.imageio.ImageIO;

import com.ardor3d.image.Image;
import com.ardor3d.image.Texture;
import com.ardor3d.image.util.ImageUtils;
import com.ardor3d.image.util.jogl.JoglImageLoader;
import com.ardor3d.math.ColorRGBA;
import com.ardor3d.math.type.ReadOnlyColorRGBA;
import com.ardor3d.util.TextureManager;
import com.ardor3d.util.geom.BufferUtils;
import com.ardor3d.util.resource.URLResourceSource;
import com.jogamp.nativewindow.util.Point;

import engine.movement.MovementEquation;
import engine.movement.UniformlyVariableMovementEquation;

/**
 * Tests the creation of the images used in the introduction state
 * 
 * @author Julien Gouesse
 *
 */
public class TestIntroductionStateImageCreation {
    
    public static void main(String[] args) {
        final String textureFilePath = "/images/introduction.png";
        final int durationInSeconds = 10;
        final int framesPerSecond = 30;
        final URLResourceSource source = new URLResourceSource(
                TestIntroductionReimplementation.class.getResource(textureFilePath));
        final Image[] introImages = generateImages(source, durationInSeconds, framesPerSecond);
        // stores the images into some files
        if (introImages != null) {
            File introImageParentDir = null;
            String introImageSimpleFilenameWithoutExtension = null;
            try {
                final File introImageFile = new File(source.getURL().toURI());
                introImageSimpleFilenameWithoutExtension = introImageFile.getName().substring(0, introImageFile.getName().lastIndexOf('.'));
                introImageParentDir = introImageFile.getParentFile();
            } catch (URISyntaxException urise) {
                urise.printStackTrace();
            }
            if (introImageParentDir != null && introImageSimpleFilenameWithoutExtension != null) {
                System.out.println("[START] Write images");
                int imageIndex = 0;
                for (final Image introImage : introImages) {
                    final BufferedImage bufferedImage = new BufferedImage(introImage.getWidth(), introImage.getHeight(),
                            BufferedImage.TYPE_INT_ARGB);
                    for (int x = 0; x < bufferedImage.getWidth(); x++) {
                        for (int y = 0; y < bufferedImage.getHeight(); y++) {
                            final int argb = ImageUtils.getARGB(introImage, x, y);
                            bufferedImage.setRGB(x, y, argb);
                        }
                    }
                    final String imageFilename = introImageSimpleFilenameWithoutExtension + imageIndex + ".png";
                    final File imageFile = new File(introImageParentDir, imageFilename);
                    if (imageFile.exists()) {
                        imageFile.delete();
                    }
                    try {
                        System.out.println("[START] Write image " + imageFile.getAbsolutePath());
                        imageFile.createNewFile();
                        ImageIO.write(bufferedImage, "png", imageFile);
                        System.out.println("[ END ] Write image " + imageFile.getAbsolutePath());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    imageIndex++;
                }
                System.out.println("[ END ] Write images");
            }
        }
    }

    private static final Image[] generateImages(final URLResourceSource source, final int durationInSeconds,
            final int framesPerSecond) {
        JoglImageLoader.registerLoader();
        System.out.println("[START] Load texture");
        final Texture introTexture = TextureManager.load(source, Texture.MinificationFilter.Trilinear, false);
        System.out.println("[ END ] Load texture");
        final Image introImage = introTexture.getImage();
        final int frameCount = durationInSeconds * framesPerSecond;
        final Point spreadCenter = new Point(205, 265);
        final MovementEquation equation = new UniformlyVariableMovementEquation(0, 10500, 0);
        HashMap<ReadOnlyColorRGBA, ReadOnlyColorRGBA> colorSubstitutionTable = new HashMap<>();
        colorSubstitutionTable.put(ColorRGBA.BLUE, ColorRGBA.RED);
        System.out.println("[START] Fill color table");
        final ArrayList<Entry<Point, ReadOnlyColorRGBA>> coloredVerticesList = new ArrayList<>();
        // fills
        ReadOnlyColorRGBA sourceColor, destinationColor;
        for (int y = 0; y < introImage.getHeight(); y++)
            for (int x = 0; x < introImage.getWidth(); x++) {
                final int argb = ImageUtils.getARGB(introImage, x, y);
                sourceColor = new ColorRGBA().fromIntARGB(argb);
                destinationColor = colorSubstitutionTable.get(sourceColor);
                if (destinationColor != null)
                    coloredVerticesList.add(new AbstractMap.SimpleEntry<>(new Point(x, y), destinationColor));
            }
        System.out.println("[ END ] Fill color table");
        // sorts
        Collections.sort(coloredVerticesList, new CenteredColoredPointComparator(spreadCenter));
        System.out.println("[START] Compute key frames images");
        // creates one image per frame
        final Image[] introImages = new Image[frameCount];
        // for each frame
        for (int frameIndex = 0; frameIndex < frameCount; frameIndex++) {
            System.out.println("[START] Compute key frames image " + frameIndex);
            // gets the image of the previous frame
            final Image previousImage = frameIndex == 0 ? introImage : introImages[frameIndex - 1];
            // copies its image data
            final ByteBuffer data = BufferUtils.clone(previousImage.getData(0));
            data.rewind();
            // creates the image by copying this previous image
            final Image image = new Image(previousImage.getDataFormat(), previousImage.getDataType(),
                    previousImage.getWidth(), previousImage.getHeight(), data, null);
            // performs the modification of this image for this frame
            final double previousElapsedTime = frameIndex == 0 ? 0 : (frameIndex - 1) / (double) framesPerSecond;
            final double elapsedTime = frameIndex / (double) framesPerSecond;
            final int updatedPixelsCount = getScannablePixelsCount(equation, previousElapsedTime);
            final int updatablePixelsCount = Math.max(0,
                    getScannablePixelsCount(equation, elapsedTime) - updatedPixelsCount);
            // if there are some pixels to update
            if (updatablePixelsCount > 0) {
                System.out.println("[     ] " + updatablePixelsCount + " updatable pixels");
                // updates the pixels (incrementally)
                final int maxUpdatedPixelsCount;
                if (updatedPixelsCount + updatablePixelsCount > coloredVerticesList.size()) {
                    maxUpdatedPixelsCount = coloredVerticesList.size();
                    final int nonUpdatedPixelsCount = updatedPixelsCount + updatablePixelsCount - coloredVerticesList.size();
                    System.out.println("[     ] " + nonUpdatedPixelsCount + " non updated pixels (updatable pixels according to the equation > updatable pixels)");
                } else {
                    maxUpdatedPixelsCount = updatedPixelsCount + updatablePixelsCount;
                }
                for (int i = updatedPixelsCount; i < maxUpdatedPixelsCount; i++) {
                    final int argb = coloredVerticesList.get(i).getValue().asIntARGB();
                    final Point updatedVertex = coloredVerticesList.get(i).getKey();
                    ImageUtils.setARGB(image, updatedVertex.getX(), updatedVertex.getY(), argb);
                }
            } else {
                System.out.println("[     ] No pixel to update");
            }
            // stores the new image into the array
            introImages[frameIndex] = image;
            System.out.println("[ END ] Compute key frames image " + frameIndex);
        }
        System.out.println("[ END ] Compute key frames images");
        return introImages;
    }
    
    private static final int getScannablePixelsCount(final MovementEquation equation, final double elapsedTime) {
        return ((int) Math.ceil(equation.getValueAtTime(elapsedTime)));
    }

    private static final class CenteredColoredPointComparator implements Comparator<Entry<Point, ReadOnlyColorRGBA>> {

        private final Point spreadCenter;

        private CenteredColoredPointComparator(final Point spreadCenter) {
            this.spreadCenter = spreadCenter;
        }

        @Override
        public final int compare(final Entry<Point, ReadOnlyColorRGBA> o1, final Entry<Point, ReadOnlyColorRGBA> o2) {
            final Point p1 = o1.getKey();
            final Point p2 = o2.getKey();
            double d1 = distance(p1, spreadCenter);
            double d2 = distance(p2, spreadCenter);
            return (d1 == d2 ? 0 : d1 < d2 ? -1 : 1);
        }
    }

    private static double distance(final Point p1, final Point p2) {
        double abscissaSub = p2.getX() - p1.getX();
        double ordinateSub = p2.getY() - p1.getY();
        return Math.sqrt((abscissaSub * abscissaSub) + (ordinateSub * ordinateSub));
    }
}
