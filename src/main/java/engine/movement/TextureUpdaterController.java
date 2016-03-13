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
package engine.movement;

import com.jogamp.nativewindow.util.Point;

import java.io.Serializable;
import java.nio.ByteBuffer;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.concurrent.Callable;

import com.ardor3d.image.Image;
import com.ardor3d.image.Texture;
import com.ardor3d.image.Texture2D;
import com.ardor3d.image.util.ImageUtils;
import com.ardor3d.math.ColorRGBA;
import com.ardor3d.math.type.ReadOnlyColorRGBA;
import com.ardor3d.renderer.RenderContext;
import com.ardor3d.renderer.Renderer;
import com.ardor3d.scenegraph.Spatial;
import com.ardor3d.scenegraph.controller.SpatialController;
import com.ardor3d.util.GameTaskQueueManager;
import com.ardor3d.util.TextureManager;
import com.ardor3d.util.geom.BufferUtils;
import com.ardor3d.util.resource.ResourceSource;
import com.ardor3d.util.resource.URLResourceSource;

public abstract class TextureUpdaterController implements Serializable, SpatialController<Spatial> {

    private static final long serialVersionUID = 1L;

    /** resource name of the image to load it with the resource locator */
    private String imageResourceName;

    /** unchanged image */
    private transient Image originalImage;

    /** modified image */
    private transient Image modifiedImage;

    /** texture modified at runtime */
    private transient Texture2D texture;

    /** equation used to know which pixels can be scanned */
    private MovementEquation equation;

    /** elapsed time in seconds */
    private transient double elapsedTime;

    private transient boolean inited;

    /** table matching source colors and destination colors */
    private HashMap<ReadOnlyColorRGBA, ReadOnlyColorRGBA> colorSubstitutionTable;

    /** sorted (chronological update order) list of vertices */
    private transient ArrayList<Entry<Point, ReadOnlyColorRGBA>> coloredVerticesList;

    private transient int updateX, updateY, updateWidth, updateHeight;

    private transient Renderer renderer;

    private transient final RenderContext renderContext;

    public TextureUpdaterController() {
        this(null, null, null, null, null);
    }

    public TextureUpdaterController(final String imageResourceName, final MovementEquation equation,
            final HashMap<ReadOnlyColorRGBA, ReadOnlyColorRGBA> colorSubstitutionTable, final Renderer renderer,
            final RenderContext renderContext) {
        this.imageResourceName = imageResourceName;
        this.equation = equation;
        this.colorSubstitutionTable = colorSubstitutionTable;
        this.renderer = renderer;
        this.renderContext = renderContext;
        elapsedTime = 0;
        inited = false;
    }

    private final void init() {
        ResourceSource resourceSource = new URLResourceSource(getClass().getResource(imageResourceName));
        // creates texture from resource name
        texture = (Texture2D) TextureManager.load(resourceSource, Texture.MinificationFilter.Trilinear, true);
        // copies the image
        final Image currentImage = texture.getImage();
        final ByteBuffer originalImageData = BufferUtils.createByteBuffer(currentImage.getData(0).capacity());
        originalImageData.put(currentImage.getData(0)).rewind();
        currentImage.getData(0).rewind();
        originalImage = new Image(currentImage.getDataFormat(), currentImage.getDataType(), currentImage.getWidth(),
                currentImage.getHeight(), originalImageData, null);
        // copies the image again, for the modifications
        final ByteBuffer modifiedImageData = BufferUtils.createByteBuffer(currentImage.getData(0).capacity());
        modifiedImageData.put(currentImage.getData(0)).rewind();
        currentImage.getData(0).rewind();
        modifiedImage = new Image(currentImage.getDataFormat(), currentImage.getDataType(), currentImage.getWidth(),
                currentImage.getHeight(), modifiedImageData, null);
        // computes effect (compute sorted vertices with color substitution)
        coloredVerticesList = new ArrayList<>();
        // fills
        ReadOnlyColorRGBA sourceColor, destinationColor;
        for (int y = 0; y < originalImage.getHeight(); y++)
            for (int x = 0; x < originalImage.getWidth(); x++) {
                final int argb = ImageUtils.getARGB(originalImage, x, y);
                sourceColor = new ColorRGBA().fromIntARGB(argb);
                destinationColor = colorSubstitutionTable.get(sourceColor);
                if (destinationColor != null)
                    coloredVerticesList.add(new AbstractMap.SimpleEntry<>(new Point(x, y), destinationColor));
            }
        // sorts
        Collections.sort(coloredVerticesList, getColoredPointComparator());
    }

    protected abstract Comparator<Entry<Point, ReadOnlyColorRGBA>> getColoredPointComparator();

    /**
     * 
     * @param elapsedTime
     * @return
     */
    protected final int getScannablePixelsCount(final double elapsedTime) {
        return ((int) Math.ceil(equation.getValueAtTime(elapsedTime)));
    }

    @Override
    public final void update(final double timeSinceLastCall, final Spatial caller) {
        if (!inited) {
            init();
            inited = true;
        }
        // gets the previous elapsed time
        final double previousElapsedTime = elapsedTime;
        final int updatedPixelsCount = getScannablePixelsCount(previousElapsedTime);
        // updates elapsed time
        elapsedTime += timeSinceLastCall;
        // uses the movement equation
        final int updatablePixelsCount = Math.max(0, getScannablePixelsCount(elapsedTime) - updatedPixelsCount);
        if (updatablePixelsCount > 0) {
            // modifies the buffer
            Point updatedVertex;
            int rgbVal, minX = originalImage.getWidth(), minY = originalImage.getHeight(), maxX = -1, maxY = -1;
            // the maximum updatable pixels count indicated by the equation might exceed the updatable pixels count
            final int maxUpdatedPixelsCount = Math.min(updatedPixelsCount + updatablePixelsCount, coloredVerticesList.size());
            for (int i = updatedPixelsCount; i < maxUpdatedPixelsCount; i++) {
                rgbVal = coloredVerticesList.get(i).getValue().asIntARGB();
                updatedVertex = coloredVerticesList.get(i).getKey();
                minX = Math.min(minX, updatedVertex.getX());
                minY = Math.min(minY, updatedVertex.getY());
                maxX = Math.max(maxX, updatedVertex.getX());
                maxY = Math.max(maxY, updatedVertex.getY());
                ImageUtils.setARGB(modifiedImage, updatedVertex.getX(), updatedVertex.getY(), rgbVal);
            }
            if (minX < originalImage.getWidth()) {
                // computes the zone that needs an update
                updateX = minX;
                updateY = minY;
                updateWidth = maxX - minX + 1;
                updateHeight = maxY - minY + 1;
                // updates the texture on the rendering thread
                GameTaskQueueManager.getManager(renderContext).render(new Callable<Void>() {
                    @Override
                    public Void call() throws Exception {
                        updateTexture();
                        return (null);
                    }
                });
            } else {
                updateX = 0;
                updateY = 0;
                updateWidth = 0;
                updateHeight = 0;
            }
        }
    }

    /** update the texture */
    private final void updateTexture() {
        // modifies the texture by using the image data
        renderer.updateTexture2DSubImage(texture, updateX, updateY, updateWidth, updateHeight, modifiedImage.getData(0),
                updateX, updateY, texture.getImage().getWidth());
    }

    public final void reset() {
        elapsedTime = 0;
        // puts the data of the original image back into the buffer
        final ByteBuffer originalImageData = originalImage.getData(0);
        modifiedImage.getData(0).rewind();
        modifiedImage.getData(0).put(originalImageData).rewind();
        originalImageData.rewind();
    }

    public final String getImageResourceName() {
        return (imageResourceName);
    }

    public final void setImageResourceName(final String imageResourceName) {
        this.imageResourceName = imageResourceName;
    }

    public final MovementEquation getEquation() {
        return (equation);
    }

    public final void setEquation(final MovementEquation equation) {
        this.equation = equation;
    }

    public final HashMap<ReadOnlyColorRGBA, ReadOnlyColorRGBA> getColorSubstitutionTable() {
        return (colorSubstitutionTable);
    }

    public final void setColorSubstitutionTable(
            final HashMap<ReadOnlyColorRGBA, ReadOnlyColorRGBA> colorSubstitutionTable) {
        this.colorSubstitutionTable = colorSubstitutionTable;
    }

    public final int getUpdateX() {
        return (updateX);
    }

    public final void setUpdateX(final int updateX) {
        this.updateX = updateX;
    }

    public final int getUpdateY() {
        return (updateY);
    }

    public final void setUpdateY(final int updateY) {
        this.updateY = updateY;
    }

    public final int getUpdateWidth() {
        return (updateWidth);
    }

    public final void setUpdateWidth(final int updateWidth) {
        this.updateWidth = updateWidth;
    }

    public final int getUpdateHeight() {
        return (updateHeight);
    }

    public final void setUpdateHeight(final int updateHeight) {
        this.updateHeight = updateHeight;
    }
}
