/**
 * Copyright (c) 2006-2014 Julien Gouesse
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

import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import com.ardor3d.math.ColorRGBA;
import com.ardor3d.math.Vector2;
import com.ardor3d.math.Vector3;
import com.ardor3d.math.type.ReadOnlyColorRGBA;
import com.ardor3d.math.type.ReadOnlyVector2;
import com.ardor3d.math.type.ReadOnlyVector3;
import com.ardor3d.scenegraph.ByteBufferData;
import com.ardor3d.scenegraph.FloatBufferData;
import com.ardor3d.scenegraph.IndexBufferData;
import com.ardor3d.scenegraph.IntBufferData;
import com.ardor3d.scenegraph.Mesh;
import com.ardor3d.scenegraph.MeshData;
import com.ardor3d.scenegraph.ShortBufferData;
import com.ardor3d.util.geom.BufferUtils;
import com.ardor3d.util.geom.GeometryTool;
import com.ardor3d.util.geom.VertGroupData;
import com.ardor3d.util.geom.VertKey;
import com.ardor3d.util.geom.VertMap;
import com.google.common.collect.Maps;

public class GeometryHelper extends GeometryTool{
	
	private static final Logger logger=Logger.getLogger(GeometryHelper.class.getName());

	public GeometryHelper(){
		super();
	}

	/**
	 * Converts an indexed geometry into a non indexed geometry
	 * 
	 * @param meshData mesh data
	 */
	public void convertIndexedGeometryIntoNonIndexedGeometry(final MeshData meshData){
		final IndexBufferData<?> indices=meshData.getIndices();
		if(indices!=null)
		    {final FloatBuffer previousVertexBuffer=meshData.getVertexBuffer();
			 if(previousVertexBuffer!=null)
			     {final int valuesPerVertexTuple=meshData.getVertexCoords().getValuesPerTuple();
			      final FloatBuffer nextVertexBuffer=FloatBuffer.allocate(indices.capacity()*valuesPerVertexTuple);
			      for(int indexIndex=0;indexIndex<indices.capacity();indexIndex++)
			          {final int vertexIndex=indices.get(indexIndex);
				       for(int coordIndex=0;coordIndex<valuesPerVertexTuple;coordIndex++)
				           {final float vertexCoordValue=previousVertexBuffer.get((vertexIndex*valuesPerVertexTuple)+coordIndex);
					        nextVertexBuffer.put((indexIndex*valuesPerVertexTuple)+coordIndex,vertexCoordValue);
				           }
			          }
			      meshData.setVertexCoords(new FloatBufferData(nextVertexBuffer,valuesPerVertexTuple));
			     }
			 final FloatBuffer previousNormalBuffer=meshData.getNormalBuffer();
			 if(previousNormalBuffer!=null)
			     {final int valuesPerNormalTuple=meshData.getNormalCoords().getValuesPerTuple();
			      final FloatBuffer nextNormalBuffer=FloatBuffer.allocate(indices.capacity()*valuesPerNormalTuple);
				  for(int indexIndex=0;indexIndex<indices.capacity();indexIndex++)
			          {final int vertexIndex=indices.get(indexIndex);
			           for(int coordIndex=0;coordIndex<valuesPerNormalTuple;coordIndex++)
				           {final float normalCoordValue=previousNormalBuffer.get((vertexIndex*valuesPerNormalTuple)+coordIndex);
				            nextNormalBuffer.put((indexIndex*valuesPerNormalTuple)+coordIndex,normalCoordValue);
				           }
			          }
				  meshData.setNormalCoords(new FloatBufferData(nextNormalBuffer,valuesPerNormalTuple));
			     }
			 final FloatBuffer previousColorBuffer=meshData.getColorBuffer();
			 if(previousColorBuffer!=null)
			     {final int valuesPerColorTuple=meshData.getColorCoords().getValuesPerTuple();
			      final FloatBuffer nextColorBuffer=FloatBuffer.allocate(indices.capacity()*valuesPerColorTuple);
				  for(int indexIndex=0;indexIndex<indices.capacity();indexIndex++)
			          {final int vertexIndex=indices.get(indexIndex);
			           for(int coordIndex=0;coordIndex<valuesPerColorTuple;coordIndex++)
				           {final float colorCoordValue=previousColorBuffer.get((vertexIndex*valuesPerColorTuple)+coordIndex);
				            nextColorBuffer.put((indexIndex*valuesPerColorTuple)+coordIndex,colorCoordValue);
				           }
			          }
				  meshData.setColorCoords(new FloatBufferData(nextColorBuffer,valuesPerColorTuple));
			     }
			 final FloatBuffer previousFogBuffer=meshData.getFogBuffer();
			 if(previousFogBuffer!=null)
			     {final int valuesPerFogTuple=meshData.getFogCoords().getValuesPerTuple();
			      final FloatBuffer nextFogBuffer=FloatBuffer.allocate(indices.capacity()*valuesPerFogTuple);
				  for(int indexIndex=0;indexIndex<indices.capacity();indexIndex++)
			          {final int vertexIndex=indices.get(indexIndex);
			           for(int coordIndex=0;coordIndex<valuesPerFogTuple;coordIndex++)
				           {final float fogCoordValue=previousFogBuffer.get((vertexIndex*valuesPerFogTuple)+coordIndex);
				            nextFogBuffer.put((indexIndex*valuesPerFogTuple)+coordIndex,fogCoordValue);
				           }
			          }
				  meshData.setFogCoords(new FloatBufferData(nextFogBuffer,valuesPerFogTuple));
			     }
			 final FloatBuffer previousTangentBuffer=meshData.getTangentBuffer();
			 if(previousTangentBuffer!=null)
			     {final int valuesPerTangentTuple=meshData.getTangentCoords().getValuesPerTuple();
			      final FloatBuffer nextTangentBuffer=FloatBuffer.allocate(indices.capacity()*valuesPerTangentTuple);
				  for(int indexIndex=0;indexIndex<indices.capacity();indexIndex++)
			          {final int vertexIndex=indices.get(indexIndex);
			           for(int coordIndex=0;coordIndex<valuesPerTangentTuple;coordIndex++)
				           {final float tangentCoordValue=previousTangentBuffer.get((vertexIndex*valuesPerTangentTuple)+coordIndex);
				            nextTangentBuffer.put((indexIndex*valuesPerTangentTuple)+coordIndex,tangentCoordValue);
				           }
			          }
				  meshData.setTangentCoords(new FloatBufferData(nextTangentBuffer,valuesPerTangentTuple));
			     }
			 final int numberOfUnits=meshData.getNumberOfUnits();
			 if(numberOfUnits>0)
			     {final List<FloatBufferData> previousTextureCoordsList=meshData.getTextureCoords();
			      final List<FloatBufferData> nextTextureCoordsList=new ArrayList<>();
				  for(int unitIndex=0;unitIndex<numberOfUnits;unitIndex++)
				      {final FloatBufferData previousTextureCoords=previousTextureCoordsList.get(unitIndex);
				       if(previousTextureCoords==null)
				    	   nextTextureCoordsList.add(null);
				       else
				           {final FloatBuffer previousTextureBuffer=previousTextureCoords.getBuffer();
				    	    final int valuesPerTextureTuple=previousTextureCoords.getValuesPerTuple();
				    	    final FloatBuffer nextTextureBuffer=FloatBuffer.allocate(indices.capacity()*valuesPerTextureTuple);
				    	    for(int indexIndex=0;indexIndex<indices.capacity();indexIndex++)
					            {final int vertexIndex=indices.get(indexIndex);
					             for(int coordIndex=0;coordIndex<valuesPerTextureTuple;coordIndex++)
						             {final float textureCoordValue=previousTextureBuffer.get((vertexIndex*valuesPerTextureTuple)+coordIndex);
						              nextTextureBuffer.put((indexIndex*valuesPerTextureTuple)+coordIndex,textureCoordValue);
						             }
					            }
				    	    nextTextureCoordsList.add(new FloatBufferData(nextTextureBuffer,valuesPerTextureTuple));
				           }
				      }
				  meshData.setTextureCoords(nextTextureCoordsList);
			     }
			 //removes the index buffer
			 meshData.setIndices(null);
		    }
	}
	
	/**
	 * N.B: the source code below is NOT under GPL, it uses the same license than Ardor3D
	 */
	/**
     * Attempt to collapse duplicate vertex data in a given mesh. Vertices are considered duplicate if they occupy the
     * same place in space and match the supplied conditions. All vertices in the mesh are considered part of the same
     * vertex "group".
     * 
     * @param mesh
     *            the mesh to reduce
     * @param conditions
     *            our match conditions.
     * @return a mapping of old vertex positions to their new positions.
     */
    @Override
	public VertMap minimizeVerts(final Mesh mesh, final EnumSet<MatchCondition> conditions) {
        final VertGroupData groupData = new VertGroupData();
        groupData.setGroupConditions(VertGroupData.DEFAULT_GROUP, conditions);
        return minimizeVerts(mesh, groupData);
    }
	
	/**
	 * 
     * Attempt to collapse duplicate vertex data in a given mesh. Vertices are consider duplicate if they occupy the
     * same place in space and match the supplied conditions. The conditions are supplied per vertex group.
     * 
     * @param mesh
     *            the mesh to reduce
     * @param groupData
     *            grouping data for the vertices in this mesh.
     * @return a mapping of old vertex positions to their new positions.
     */
    @Override
	public VertMap minimizeVerts(final Mesh mesh, final VertGroupData groupData) {
        final long start = System.currentTimeMillis();

        int vertCount = -1;
        final int oldCount = mesh.getMeshData().getVertexCount();
        int newCount = 0;

        final VertMap result = new VertMap(mesh);

        // while we have not run through this optimization and ended up the same...
        // XXX: could optimize this to run all in arrays, then write to buffer after while loop.
        while (vertCount != newCount) {
            vertCount = mesh.getMeshData().getVertexCount();
            // go through each vert...
            final Vector3[] verts = BufferUtils.getVector3Array(mesh.getMeshData().getVertexCoords(), Vector3.ZERO);
            Vector3[] norms = null;
            if (mesh.getMeshData().getNormalBuffer() != null) {
                norms = BufferUtils.getVector3Array(mesh.getMeshData().getNormalCoords(), Vector3.UNIT_Y);
            }

            // see if we have vertex colors
            ColorRGBA[] colors = null;
            if (mesh.getMeshData().getColorBuffer() != null) {
                colors = BufferUtils.getColorArray(mesh.getMeshData().getColorCoords(), ColorRGBA.WHITE);
            }

            // see if we have uv coords
            final Vector2[][] tex = new Vector2[mesh.getMeshData().getNumberOfUnits()][];
            for (int x = 0; x < tex.length; x++) {
                if (mesh.getMeshData().getTextureCoords(x) != null) {
                    tex[x] = BufferUtils.getVector2Array(mesh.getMeshData().getTextureCoords(x), Vector2.ZERO);
                }
            }

            final Map<VertKey, Integer> store = Maps.newHashMap();
            final Map<Integer, Integer> indexRemap = Maps.newHashMap();
            int good = 0;
            long group;
            for (int x = 0, max = verts.length; x < max; x++) {
                group = groupData.getGroupForVertex(x);
                final VertKey vkey = new VertKey(verts[x], norms != null ? norms[x] : null, colors != null ? colors[x]
                        : null, getTexs(tex, x), groupData.getGroupConditions(group), group);
                // if we've already seen it, swap it for the max, and decrease max.
                if (store.containsKey(vkey)) {
                    final int newInd = store.get(vkey);
                    if (indexRemap.containsKey(x)) {
                        indexRemap.put(max, newInd);
                    } else {
                        indexRemap.put(x, newInd);
                    }
                    max--;
                    if (x != max) {
                        indexRemap.put(max, x);
                        verts[x] = verts[max];
                        verts[max] = null;
                        if (norms != null) {
                            norms[newInd].addLocal(norms[x].normalizeLocal());
                            norms[x] = norms[max];
                        }
                        if (colors != null) {
                            colors[x] = colors[max];
                        }
                        for (int y = 0; y < tex.length; y++) {
                            if (mesh.getMeshData().getTextureCoords(y) != null) {
                                tex[y][x] = tex[y][max];
                            }
                        }
                        x--;
                    } else {
                        verts[max] = null;
                    }
                }

                // otherwise just store it
                else {
                    store.put(vkey, x);
                    good++;
                }
            }

            if (norms != null) {
                for (final Vector3 norm : norms) {
                    norm.normalizeLocal();
                }
            }

            mesh.getMeshData().setVertexBuffer(createFloatBufferOnHeap(0, good, verts));
            if (norms != null) {
                mesh.getMeshData().setNormalBuffer(createFloatBufferOnHeap(0, good, norms));
            }
            if (colors != null) {
                mesh.getMeshData().setColorBuffer(createFloatBufferOnHeap(0, good, colors));
            }

            for (int x = 0; x < tex.length; x++) {
                if (tex[x] != null) {
                    mesh.getMeshData().setTextureBuffer(createFloatBufferOnHeap(0, good, tex[x]), x);
                }
            }

            if (mesh.getMeshData().getIndices() == null || mesh.getMeshData().getIndices().getBufferCapacity() == 0) {
                final IndexBufferData<?> indexBuffer = createIndexBufferDataOnHeap(oldCount, oldCount);
                mesh.getMeshData().setIndices(indexBuffer);
                for (int i = 0; i < oldCount; i++) {
                    if (indexRemap.containsKey(i)) {
                        indexBuffer.put(indexRemap.get(i));
                    } else {
                        indexBuffer.put(i);
                    }
                }
            } else {
                final IndexBufferData<?> indexBuffer = mesh.getMeshData().getIndices();
                final int[] inds = BufferUtils.getIntArray(indexBuffer);
                indexBuffer.rewind();
                for (final int i : inds) {
                    if (indexRemap.containsKey(i)) {
                        indexBuffer.put(indexRemap.get(i));
                    } else {
                        indexBuffer.put(i);
                    }
                }
            }
            result.applyRemapping(indexRemap);
            newCount = mesh.getMeshData().getVertexCount();
        }

        logger.info("Vertex reduction complete on: " + mesh + "  old vertex count: " + oldCount + " new vertex count: "
                + newCount + " (in " + (System.currentTimeMillis() - start) + " ms)");

        return result;
    }
    
    private static Vector2[] getTexs(final Vector2[][] tex, final int i) {
        final Vector2[] res = new Vector2[tex.length];
        for (int x = 0; x < tex.length; x++) {
            if (tex[x] != null) {
                res[x] = tex[x][i];
            }
        }
        return res;
    }
    
    /**
     * Generate a new FloatBuffer using the given array of Vector2 objects. The FloatBuffer will be 2 * data.length long
     * and contain the vector data as data[0].x, data[0].y, data[1].x... etc.
     * 
     * @param offset
     *            the starting index to read from in our data array
     * @param length
     *            the number of vectors to read
     * @param data
     *            array of Vector2 objects to place into a new FloatBuffer
     */
    public static FloatBuffer createFloatBufferOnHeap(final int offset, final int length, final ReadOnlyVector2... data) {
        if (data == null) {
            return null;
        }
        final FloatBuffer buff = BufferUtils.createFloatBufferOnHeap(2 * length);
        for (int x = offset; x < length; x++) {
            if (data[x] != null) {
                buff.put(data[x].getXf()).put(data[x].getYf());
            } else {
                buff.put(0).put(0);
            }
        }
        buff.flip();
        return buff;
    }
    
    /**
     * Generate a new FloatBuffer using the given array of Vector3 objects. The FloatBuffer will be 3 * data.length long
     * and contain the vector data as data[0].x, data[0].y, data[0].z, data[1].x... etc.
     * 
     * @param offset
     *            the starting index to read from in our data array
     * @param length
     *            the number of vectors to read
     * @param data
     *            array of Vector3 objects to place into a new FloatBuffer
     */
    public static FloatBuffer createFloatBufferOnHeap(final int offset, final int length, final ReadOnlyVector3... data) {
        if (data == null) {
            return null;
        }
        final FloatBuffer buff = BufferUtils.createFloatBufferOnHeap(3 * length);
        for (int x = offset; x < length; x++) {
            if (data[x] != null) {
                buff.put(data[x].getXf()).put(data[x].getYf()).put(data[x].getZf());
            } else {
                buff.put(0).put(0).put(0);
            }
        }
        buff.flip();
        return buff;
    }
    
    /**
     * Generate a new FloatBuffer using the given array of ColorRGBA objects. The FloatBuffer will be 4 * data.length
     * long and contain the color data as data[0].r, data[0].g, data[0].b, data[0].a, data[1].r... etc.
     * 
     * @param offset
     *            the starting index to read from in our data array
     * @param length
     *            the number of colors to read
     * @param data
     *            array of ColorRGBA objects to place into a new FloatBuffer
     */
    public static FloatBuffer createFloatBufferOnHeap(final int offset, final int length, final ReadOnlyColorRGBA... data) {
        if (data == null) {
            return null;
        }
        final FloatBuffer buff = BufferUtils.createFloatBufferOnHeap(4 * length);
        for (int x = offset; x < length; x++) {
            if (data[x] != null) {
                buff.put(data[x].getRed()).put(data[x].getGreen()).put(data[x].getBlue()).put(data[x].getAlpha());
            } else {
                buff.put(0).put(0).put(0).put(0);
            }
        }
        buff.flip();
        return buff;
    }
    
    /**
     * Create a new IndexBufferData of the specified size. The specific implementation will be chosen based on the max
     * value you need to store in your buffer. If that value is less than 2^8, a ByteBufferData is used. If it is less
     * than 2^16, a ShortBufferData is used. Otherwise an IntBufferData is used.
     * 
     * @param size
     *            required number of values to store.
     * @param maxValue
     *            the largest value you will need to store in your buffer. Often this is equal to
     *            ("size of vertex buffer" - 1).
     * @return the new IndexBufferData
     */
    public static IndexBufferData<?> createIndexBufferDataOnHeap(final int size, final int maxValue) {
        if (maxValue < 256) { // 2^8
            return new ByteBufferData(BufferUtils.createByteBufferOnHeap(size));
        } else if (maxValue < 65536) { // 2^16
            return new ShortBufferData(BufferUtils.createShortBufferOnHeap(size));
        } else {
            return new IntBufferData(BufferUtils.createIntBufferOnHeap(size));
        }
    }
}
