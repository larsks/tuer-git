/**
 * Copyright (c) 2006-2015 Julien Gouesse
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

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.List;

/**
 * Interface of services seeker
 * 
 * @author Julien Gouesse
 *
 * @param <S> class of encodable or serializable objects
 * @param <T> class of scenegraph nodes
 * @param <U> class of scenegraph objects
 * @param <V> class of leaf nodes managing the geometry
 * @param <W> class of bounding box
 */
public interface I3DServiceSeeker<S,T,U,V,W>{
    public void bind3DServiceSeeker(I3DServiceSeeker<S,T,U,V,W> seeker);
    public boolean writeSavableInstanceIntoFile(final S savable,final File file);
    public boolean writeSavableInstancesListIntoFile(final List<S> savablesList,final File file);
    public void attachChildToNode(final T parent,final U child);
    public T createNode(final String name);
    public V createMeshFromBuffers(final String name,
    		final FloatBuffer vertexBuffer,final IntBuffer indexBuffer,
    		final FloatBuffer normalBuffer,final FloatBuffer texCoordBuffer);
    public void attachTextureToSpatial(final U spatial,final URL url);
    public W createBoundingBox(final double xCenter,final double yCenter,final double zCenter,final double xExtent,final double yExtent,final double zExtent);
    public U load(final File inputModelFile,final String inputModelFileFormat)throws IOException,UnsupportedOperationException;
    public void save(final File outputModelFile,final String outputModelFileFormat,final File secondaryOutputModelFile,final U convertible)throws IOException,UnsupportedOperationException;
}
