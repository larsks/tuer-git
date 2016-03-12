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

import java.beans.Transient;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map.Entry;

public final class CuboidParameters extends VolumeParameters {

    private static final long serialVersionUID = 1L;

    private transient boolean dirty;

    private transient boolean buffersRecomputationNeeded;

    private float[] offset;

    private float[] size;
    /** texture coordinates sorted by side */
    private float[][] texCoord;

    public enum Side {
        BACK, RIGHT, FRONT, LEFT, TOP, BOTTOM
    };

    public enum Orientation {
        OUTWARDS, INWARDS, NONE
    };

    private Orientation[] faceOrientation;

    private transient FloatBuffer vertexBuffer;

    private transient FloatBuffer normalBuffer;

    private transient IntBuffer indexBuffer;

    private transient IntBuffer mergeableIndexBuffer;

    private transient FloatBuffer texCoordBuffer;

    private transient int[][][] verticesIndicesOfPotentiallyIdenticalFaces;

    private transient int[][][] verticesIndicesOfAdjacentMergeableFaces;

    public CuboidParameters() {
        this(new float[] { 0, 0, 0 }, new float[] { 1, 1, 1 },
                new Orientation[] { Orientation.OUTWARDS, Orientation.OUTWARDS, Orientation.OUTWARDS,
                        Orientation.OUTWARDS, Orientation.OUTWARDS, Orientation.OUTWARDS },
                new float[][] { new float[] { 0, 1, 0, 1 }, new float[] { 0, 1, 0, 1 }, new float[] { 0, 1, 0, 1 },
                        new float[] { 0, 1, 0, 1 }, new float[] { 0, 1, 0, 1 }, new float[] { 0, 1, 0, 1 } });
    }

    public CuboidParameters(final float[] offset, final float[] size, final Orientation[] faceOrientation,
            final float[][] texCoord) {
        this.offset = offset;
        this.size = size;
        this.texCoord = texCoord;
        this.faceOrientation = faceOrientation;
        buffersRecomputationNeeded = true;
        markDirty();
    }

    @Override
    final VolumeType getVolumeType() {
        return (VolumeType.CUBOID);
    }

    @Transient
    @Override
    public final boolean isDirty() {
        return (dirty);
    }

    @Override
    public final void markDirty() {
        dirty = true;
    }

    @Override
    public final void unmarkDirty() {
        dirty = false;
    }

    public final float[] getOffset() {
        return (offset);
    }

    public final void setOffset(float[] offset) {
        this.offset = offset;
        buffersRecomputationNeeded = true;
        markDirty();
    }

    public final void setOffset(int index, float value) {
        this.offset[index] = value;
        buffersRecomputationNeeded = true;
        markDirty();
    }

    public final float[] getSize() {
        return (size);
    }

    public final void setSize(float[] size) {
        this.size = size;
        buffersRecomputationNeeded = true;
        markDirty();
    }

    public final void setSize(int index, float value) {
        this.size[index] = value;
        buffersRecomputationNeeded = true;
        markDirty();
    }

    private final void recomputeBuffersIfNeeded() {
        if (buffersRecomputationNeeded) {
            int visibleFacesCount = 0;
            for (int i = 0; i < 6; i++)
                if (faceOrientation[i] != Orientation.NONE)
                    visibleFacesCount++;
            if (vertexBuffer == null || vertexBuffer.capacity() != visibleFacesCount * 12) {// 6
                                                                                            // faces
                                                                                            // *
                                                                                            // 4
                                                                                            // vertices
                                                                                            // *
                                                                                            // 3
                                                                                            // coordinates
                vertexBuffer = FloatBuffer.allocate(visibleFacesCount * 12);
            }
            float[] center = new float[3];
            for (int i = 0; i < 3; i++)
                center[i] = (size[i] / 2) + offset[i];
            float[][] vertices = new float[8][3];
            vertices[0] = new float[] { center[0] - size[0] / 2, center[1] - size[1] / 2, center[2] - size[2] / 2 };
            vertices[1] = new float[] { center[0] + size[0] / 2, center[1] - size[1] / 2, center[2] - size[2] / 2 };
            vertices[2] = new float[] { center[0] + size[0] / 2, center[1] + size[1] / 2, center[2] - size[2] / 2 };
            vertices[3] = new float[] { center[0] - size[0] / 2, center[1] + size[1] / 2, center[2] - size[2] / 2 };
            vertices[4] = new float[] { center[0] + size[0] / 2, center[1] - size[1] / 2, center[2] + size[2] / 2 };
            vertices[5] = new float[] { center[0] - size[0] / 2, center[1] - size[1] / 2, center[2] + size[2] / 2 };
            vertices[6] = new float[] { center[0] + size[0] / 2, center[1] + size[1] / 2, center[2] + size[2] / 2 };
            vertices[7] = new float[] { center[0] - size[0] / 2, center[1] + size[1] / 2, center[2] + size[2] / 2 };
            // fill the vertex buffer
            vertexBuffer.rewind();
            // Back
            if (faceOrientation[Side.BACK.ordinal()] != Orientation.NONE) {
                vertexBuffer.put(vertices[0]);
                vertexBuffer.put(vertices[1]);
                vertexBuffer.put(vertices[2]);
                vertexBuffer.put(vertices[3]);
            }
            // Right
            if (faceOrientation[Side.RIGHT.ordinal()] != Orientation.NONE) {
                vertexBuffer.put(vertices[1]);
                vertexBuffer.put(vertices[4]);
                vertexBuffer.put(vertices[6]);
                vertexBuffer.put(vertices[2]);
            }
            // Front
            if (faceOrientation[Side.FRONT.ordinal()] != Orientation.NONE) {
                vertexBuffer.put(vertices[4]);
                vertexBuffer.put(vertices[5]);
                vertexBuffer.put(vertices[7]);
                vertexBuffer.put(vertices[6]);
            }
            // Left
            if (faceOrientation[Side.LEFT.ordinal()] != Orientation.NONE) {
                vertexBuffer.put(vertices[5]);
                vertexBuffer.put(vertices[0]);
                vertexBuffer.put(vertices[3]);
                vertexBuffer.put(vertices[7]);
            }
            // Top
            if (faceOrientation[Side.TOP.ordinal()] != Orientation.NONE) {
                vertexBuffer.put(vertices[2]);
                vertexBuffer.put(vertices[6]);
                vertexBuffer.put(vertices[7]);
                vertexBuffer.put(vertices[3]);
            }
            // Bottom
            if (faceOrientation[Side.BOTTOM.ordinal()] != Orientation.NONE) {
                vertexBuffer.put(vertices[0]);
                vertexBuffer.put(vertices[5]);
                vertexBuffer.put(vertices[4]);
                vertexBuffer.put(vertices[1]);
            }
            vertexBuffer.rewind();
            if (indexBuffer == null || indexBuffer.capacity() != visibleFacesCount * 6) {// 6
                                                                                         // faces
                                                                                         // *
                                                                                         // 2
                                                                                         // triangles
                                                                                         // *
                                                                                         // 3
                                                                                         // indices
                indexBuffer = IntBuffer.allocate(visibleFacesCount * 6);
            }
            if (mergeableIndexBuffer == null || mergeableIndexBuffer.capacity() != visibleFacesCount * 6) {// 6
                                                                                                           // faces
                                                                                                           // *
                                                                                                           // 2
                                                                                                           // triangles
                                                                                                           // *
                                                                                                           // 3
                                                                                                           // indices
                mergeableIndexBuffer = IntBuffer.allocate(visibleFacesCount * 6);
            }
            // fill the index buffer
            indexBuffer.rewind();
            mergeableIndexBuffer.rewind();
            int indexOffset = 0;
            final int[] firstVerticesIndicesInIndexBufferBySide = new int[Side.values().length];
            final boolean[] changeDiagonal = new boolean[] { false, true, true, false, true, false };
            for (Side side : Side.values()) {
                if (faceOrientation[side.ordinal()] == Orientation.OUTWARDS) {
                    firstVerticesIndicesInIndexBufferBySide[side.ordinal()] = indexBuffer.position();
                    indexBuffer.put(2 + indexOffset).put(1 + indexOffset).put(0 + indexOffset).put(3 + indexOffset)
                            .put(2 + indexOffset).put(0 + indexOffset);
                    if (changeDiagonal[side.ordinal()])
                        mergeableIndexBuffer.put(3 + indexOffset).put(0 + indexOffset).put(1 + indexOffset)
                                .put(2 + indexOffset).put(3 + indexOffset).put(1 + indexOffset);
                    else
                        mergeableIndexBuffer.put(2 + indexOffset).put(1 + indexOffset).put(0 + indexOffset)
                                .put(3 + indexOffset).put(2 + indexOffset).put(0 + indexOffset);
                    indexOffset += 4;
                } else if (faceOrientation[side.ordinal()] == Orientation.INWARDS) {
                    firstVerticesIndicesInIndexBufferBySide[side.ordinal()] = indexBuffer.position();
                    indexBuffer.put(0 + indexOffset).put(1 + indexOffset).put(2 + indexOffset).put(0 + indexOffset)
                            .put(2 + indexOffset).put(3 + indexOffset);
                    // use normals to outwards instead of inwards to ease the
                    // merge
                    if (changeDiagonal[side.ordinal()])
                        mergeableIndexBuffer.put(3 + indexOffset).put(0 + indexOffset).put(1 + indexOffset)
                                .put(2 + indexOffset).put(3 + indexOffset).put(1 + indexOffset);
                    else
                        mergeableIndexBuffer.put(2 + indexOffset).put(1 + indexOffset).put(0 + indexOffset)
                                .put(3 + indexOffset).put(2 + indexOffset).put(0 + indexOffset);
                    indexOffset += 4;
                } else
                    firstVerticesIndicesInIndexBufferBySide[side.ordinal()] = -1;
            }
            indexBuffer.rewind();
            mergeableIndexBuffer.rewind();
            if (normalBuffer == null || normalBuffer.capacity() != visibleFacesCount * 12) {// 6
                                                                                            // faces
                                                                                            // *
                                                                                            // 4
                                                                                            // vertices
                                                                                            // *
                                                                                            // 3
                                                                                            // coordinates
                normalBuffer = FloatBuffer.allocate(visibleFacesCount * 12);
            }
            // fill the normal buffer
            normalBuffer.rewind();
            int value;
            if (faceOrientation[Side.BACK.ordinal()] != Orientation.NONE) {
                value = faceOrientation[Side.BACK.ordinal()] == Orientation.OUTWARDS ? -1 : 1;
                normalBuffer.put(0).put(0).put(value);
                normalBuffer.put(0).put(0).put(value);
                normalBuffer.put(0).put(0).put(value);
                normalBuffer.put(0).put(0).put(value);
            }
            if (faceOrientation[Side.RIGHT.ordinal()] != Orientation.NONE) {
                value = faceOrientation[Side.RIGHT.ordinal()] == Orientation.OUTWARDS ? 1 : -1;
                normalBuffer.put(value).put(0).put(0);
                normalBuffer.put(value).put(0).put(0);
                normalBuffer.put(value).put(0).put(0);
                normalBuffer.put(value).put(0).put(0);
            }
            if (faceOrientation[Side.FRONT.ordinal()] != Orientation.NONE) {
                value = faceOrientation[Side.FRONT.ordinal()] == Orientation.OUTWARDS ? 1 : -1;
                normalBuffer.put(0).put(0).put(value);
                normalBuffer.put(0).put(0).put(value);
                normalBuffer.put(0).put(0).put(value);
                normalBuffer.put(0).put(0).put(value);
            }
            if (faceOrientation[Side.LEFT.ordinal()] != Orientation.NONE) {
                value = faceOrientation[Side.LEFT.ordinal()] == Orientation.OUTWARDS ? -1 : 1;
                normalBuffer.put(value).put(0).put(0);
                normalBuffer.put(value).put(0).put(0);
                normalBuffer.put(value).put(0).put(0);
                normalBuffer.put(value).put(0).put(0);
            }
            if (faceOrientation[Side.TOP.ordinal()] != Orientation.NONE) {
                value = faceOrientation[Side.TOP.ordinal()] == Orientation.OUTWARDS ? 1 : -1;
                normalBuffer.put(0).put(value).put(0);
                normalBuffer.put(0).put(value).put(0);
                normalBuffer.put(0).put(value).put(0);
                normalBuffer.put(0).put(value).put(0);
            }
            if (faceOrientation[Side.BOTTOM.ordinal()] != Orientation.NONE) {
                value = faceOrientation[Side.BOTTOM.ordinal()] == Orientation.OUTWARDS ? -1 : 1;
                normalBuffer.put(0).put(value).put(0);
                normalBuffer.put(0).put(value).put(0);
                normalBuffer.put(0).put(value).put(0);
                normalBuffer.put(0).put(value).put(0);
            }
            normalBuffer.rewind();
            if (texCoordBuffer == null || texCoordBuffer.capacity() != visibleFacesCount * 8) {// 6
                                                                                               // faces
                                                                                               // *
                                                                                               // 4
                                                                                               // vertices
                                                                                               // *
                                                                                               // 2
                                                                                               // coordinates
                texCoordBuffer = FloatBuffer.allocate(visibleFacesCount * 8);
            }
            texCoordBuffer.rewind();
            // fill the texture coord buffer
            float u0, u1, v0, v1;
            for (Side side : Side.values())
                if (faceOrientation[side.ordinal()] != Orientation.NONE) {
                    u0 = texCoord[side.ordinal()][0];
                    u1 = texCoord[side.ordinal()][1];
                    v0 = texCoord[side.ordinal()][2];
                    v1 = texCoord[side.ordinal()][3];
                    texCoordBuffer.put(u1).put(v0);
                    texCoordBuffer.put(u0).put(v0);
                    texCoordBuffer.put(u0).put(v1);
                    texCoordBuffer.put(u1).put(v1);
                }
            texCoordBuffer.rewind();
            ArrayList<int[][]> verticesIndicesOfPotentiallyIdenticalFacesList = new ArrayList<>();
            int fvi0, fvi1;
            if (faceOrientation[Side.BACK.ordinal()] != Orientation.NONE
                    && faceOrientation[Side.FRONT.ordinal()] != Orientation.NONE) {
                fvi0 = firstVerticesIndicesInIndexBufferBySide[Side.BACK.ordinal()];
                fvi1 = firstVerticesIndicesInIndexBufferBySide[Side.FRONT.ordinal()];
                verticesIndicesOfPotentiallyIdenticalFacesList.add(new int[][] {
                        new int[] { mergeableIndexBuffer.get(fvi0), mergeableIndexBuffer.get(fvi0 + 1),
                                mergeableIndexBuffer.get(fvi0 + 2) },
                        new int[] { mergeableIndexBuffer.get(fvi1), mergeableIndexBuffer.get(fvi1 + 1),
                                mergeableIndexBuffer.get(fvi1 + 2) } });
                verticesIndicesOfPotentiallyIdenticalFacesList.add(new int[][] {
                        new int[] { mergeableIndexBuffer.get(fvi0 + 3), mergeableIndexBuffer.get(fvi0 + 4),
                                mergeableIndexBuffer.get(fvi0 + 5) },
                        new int[] { mergeableIndexBuffer.get(fvi1 + 3), mergeableIndexBuffer.get(fvi1 + 4),
                                mergeableIndexBuffer.get(fvi1 + 5) } });
            }
            if (faceOrientation[Side.LEFT.ordinal()] != Orientation.NONE
                    && faceOrientation[Side.RIGHT.ordinal()] != Orientation.NONE) {
                fvi0 = firstVerticesIndicesInIndexBufferBySide[Side.LEFT.ordinal()];
                fvi1 = firstVerticesIndicesInIndexBufferBySide[Side.RIGHT.ordinal()];
                verticesIndicesOfPotentiallyIdenticalFacesList.add(new int[][] {
                        new int[] { mergeableIndexBuffer.get(fvi0), mergeableIndexBuffer.get(fvi0 + 1),
                                mergeableIndexBuffer.get(fvi0 + 2) },
                        new int[] { mergeableIndexBuffer.get(fvi1), mergeableIndexBuffer.get(fvi1 + 1),
                                mergeableIndexBuffer.get(fvi1 + 2) } });
                verticesIndicesOfPotentiallyIdenticalFacesList.add(new int[][] {
                        new int[] { mergeableIndexBuffer.get(fvi0 + 3), mergeableIndexBuffer.get(fvi0 + 4),
                                mergeableIndexBuffer.get(fvi0 + 5) },
                        new int[] { mergeableIndexBuffer.get(fvi1 + 3), mergeableIndexBuffer.get(fvi1 + 4),
                                mergeableIndexBuffer.get(fvi1 + 5) } });
            }
            if (faceOrientation[Side.BOTTOM.ordinal()] != Orientation.NONE
                    && faceOrientation[Side.TOP.ordinal()] != Orientation.NONE) {
                fvi0 = firstVerticesIndicesInIndexBufferBySide[Side.BOTTOM.ordinal()];
                fvi1 = firstVerticesIndicesInIndexBufferBySide[Side.TOP.ordinal()];
                verticesIndicesOfPotentiallyIdenticalFacesList.add(new int[][] {
                        new int[] { mergeableIndexBuffer.get(fvi0), mergeableIndexBuffer.get(fvi0 + 1),
                                mergeableIndexBuffer.get(fvi0 + 2) },
                        new int[] { mergeableIndexBuffer.get(fvi1), mergeableIndexBuffer.get(fvi1 + 1),
                                mergeableIndexBuffer.get(fvi1 + 2) } });
                verticesIndicesOfPotentiallyIdenticalFacesList.add(new int[][] {
                        new int[] { mergeableIndexBuffer.get(fvi0 + 3), mergeableIndexBuffer.get(fvi0 + 4),
                                mergeableIndexBuffer.get(fvi0 + 5) },
                        new int[] { mergeableIndexBuffer.get(fvi1 + 3), mergeableIndexBuffer.get(fvi1 + 4),
                                mergeableIndexBuffer.get(fvi1 + 5) } });
            }
            // 6 faces * 2 triangles * 3 indices
            verticesIndicesOfPotentiallyIdenticalFaces = verticesIndicesOfPotentiallyIdenticalFacesList
                    .toArray(new int[verticesIndicesOfPotentiallyIdenticalFacesList.size()][2][3]);
            ArrayList<int[][]> verticesIndicesOfAdjacentMergeableFacesList = new ArrayList<>();
            int indexIndex = 0;
            for (Side side : Side.values())
                if (faceOrientation[side.ordinal()] != Orientation.NONE) {
                    verticesIndicesOfAdjacentMergeableFacesList.add(new int[][] {
                            new int[] { indexBuffer.get(indexIndex), indexBuffer.get(indexIndex + 1),
                                    indexBuffer.get(indexIndex + 2) },
                            new int[] { indexBuffer.get(indexIndex + 3), indexBuffer.get(indexIndex + 4),
                                    indexBuffer.get(indexIndex + 5) } });
                    indexIndex += 6;
                }
            verticesIndicesOfAdjacentMergeableFaces = verticesIndicesOfAdjacentMergeableFacesList
                    .toArray(new int[verticesIndicesOfPotentiallyIdenticalFacesList.size()][2][3]);
            buffersRecomputationNeeded = false;
        }
    }

    @Transient
    @Override
    public final int[][][] getVerticesIndicesOfPotentiallyIdenticalFaces() {
        recomputeBuffersIfNeeded();
        return (verticesIndicesOfPotentiallyIdenticalFaces);
    }

    @Transient
    @Override
    public final int[][][] getVerticesIndicesOfAdjacentMergeableFaces() {
        recomputeBuffersIfNeeded();
        return (verticesIndicesOfAdjacentMergeableFaces);
    }

    @Override
    public final Entry<int[][][], int[][]> getVerticesIndicesOfAdjacentMergeableFacesAndAdjacencyCoordIndices(
            RegularGrid grid) {
        recomputeBuffersIfNeeded();
        ArrayList<int[][]> verticesIndicesOfAdjacentMergeableFacesList = new ArrayList<>();
        ArrayList<int[]> adjacencyCoordIndicesList = new ArrayList<>();
        int[] adjacentCoordsCount = new int[6];
        ArrayList<Integer> adjacencyCoordIndices = new ArrayList<>();
        int[] adjacencyCoordIndicesArray;
        boolean canMerge;
        ArrayList<Integer> indices = new ArrayList<>();
        int index, adjacencyCoordIndex;
        float coord, dot;
        float[] vec0 = new float[3], vec1 = new float[3], vec2 = new float[3], vec3 = new float[3],
                adjacencyLinkVec = new float[3];
        // for each face
        for (int faceIndex = 0; faceIndex < verticesIndicesOfAdjacentMergeableFaces.length; faceIndex++) {
            Arrays.fill(adjacentCoordsCount, 0);
            indices.clear();
            // for each triangle
            for (int triangleIndex = 0; triangleIndex < verticesIndicesOfAdjacentMergeableFaces[faceIndex].length; triangleIndex++)
                // for each index
                for (int indexIndex = 0; indexIndex < verticesIndicesOfAdjacentMergeableFaces[faceIndex][triangleIndex].length; indexIndex++) {// get
                                                                                                                                               // the
                                                                                                                                               // index
                    index = verticesIndicesOfAdjacentMergeableFaces[faceIndex][triangleIndex][indexIndex];
                    // if the index has not already been tested
                    if (!indices.contains(Integer.valueOf(index))) {// mark it
                                                                    // as tested
                        indices.add(Integer.valueOf(index));
                        // for each coordinate
                        for (int coordIndex = 0; coordIndex < 3; coordIndex++) {// get
                                                                                // the
                                                                                // coordinate
                            coord = vertexBuffer.get((index * 3) + coordIndex);
                            // if the coordinate matches with any of the
                            // coordinate of the grid section, increment the
                            // counter
                            if (coord == 0)
                                adjacentCoordsCount[coordIndex * 2]++;
                            else if (coord == grid.getSectionPhysicalSize(coordIndex))
                                adjacentCoordsCount[(coordIndex * 2) + 1]++;
                        }
                    }
                }
            adjacencyCoordIndices.clear();
            for (int coordIndex = 0; coordIndex < 3; coordIndex++)
                // if 2 coordinates match with each coordinate of the grid
                // section for this coordinate index
                if ((adjacentCoordsCount[coordIndex * 2] == 2 && adjacentCoordsCount[(coordIndex * 2) + 1] == 2))
                    adjacencyCoordIndices.add(Integer.valueOf(coordIndex));
            canMerge = false;
            if (adjacencyCoordIndices.size() >= 1 && indices.size() == 4) {// check
                                                                           // that
                                                                           // the
                                                                           // shape
                                                                           // is
                                                                           // rectangular
                                                                           // compute
                                                                           // the
                                                                           // 4
                                                                           // vectors
                for (int coordIndex = 0; coordIndex < 3; coordIndex++) {
                    vec0[coordIndex] = vertexBuffer.get((indices.get(1).intValue() * 3) + coordIndex)
                            - vertexBuffer.get((indices.get(0).intValue() * 3) + coordIndex);
                    vec1[coordIndex] = vertexBuffer.get((indices.get(2).intValue() * 3) + coordIndex)
                            - vertexBuffer.get((indices.get(1).intValue() * 3) + coordIndex);
                    vec2[coordIndex] = vertexBuffer.get((indices.get(3).intValue() * 3) + coordIndex)
                            - vertexBuffer.get((indices.get(2).intValue() * 3) + coordIndex);
                    vec3[coordIndex] = vertexBuffer.get((indices.get(0).intValue() * 3) + coordIndex)
                            - vertexBuffer.get((indices.get(3).intValue() * 3) + coordIndex);
                }
                dot = 0;
                // dot product
                for (int coordIndex = 0; coordIndex < 3; coordIndex++)
                    dot += vec0[coordIndex] * vec1[coordIndex];
                if (dot == 0) {// another dot product
                    for (int coordIndex = 0; coordIndex < 3; coordIndex++)
                        dot += vec1[coordIndex] * vec2[coordIndex];
                    if (dot == 0) {// the shape is rectangular
                                   // check if it is possible to link 2 adjacent
                                   // faces together
                        for (int adjacencyCoordIndexIndex = adjacencyCoordIndices.size()
                                - 1; adjacencyCoordIndexIndex >= 0; adjacencyCoordIndexIndex--) {
                            adjacencyCoordIndex = adjacencyCoordIndices.get(adjacencyCoordIndexIndex);
                            Arrays.fill(adjacencyLinkVec, 0);
                            adjacencyLinkVec[adjacencyCoordIndex] = grid.getSectionPhysicalSize(adjacencyCoordIndex);
                            if (!Arrays.equals(adjacencyLinkVec, vec0) && !Arrays.equals(adjacencyLinkVec, vec1)
                                    && !Arrays.equals(adjacencyLinkVec, vec2) && !Arrays.equals(adjacencyLinkVec, vec3))
                                adjacencyCoordIndices.remove(adjacencyCoordIndexIndex);
                        }
                        if (adjacencyCoordIndices.size() > 0)
                            canMerge = true;
                    }
                }
            }
            if (canMerge) {// sort the coordinate indices
                Collections.sort(adjacencyCoordIndices);
                verticesIndicesOfAdjacentMergeableFacesList.add(verticesIndicesOfAdjacentMergeableFaces[faceIndex]);
                adjacencyCoordIndicesArray = new int[adjacencyCoordIndices.size()];
                for (int adjacencyCoordIndexIndex = 0; adjacencyCoordIndexIndex < adjacencyCoordIndices
                        .size(); adjacencyCoordIndexIndex++)
                    adjacencyCoordIndicesArray[adjacencyCoordIndexIndex] = adjacencyCoordIndices
                            .get(adjacencyCoordIndexIndex).intValue();
                adjacencyCoordIndicesList.add(adjacencyCoordIndicesArray);
            }
        }
        int[][][] verticesIndicesOfAdjacentMergeableFacesForThisGrid = verticesIndicesOfAdjacentMergeableFacesList
                .toArray(new int[verticesIndicesOfAdjacentMergeableFacesList.size()][2][3]);
        int[][] adjacencyCoordIndicesPerFaceArray = adjacencyCoordIndicesList
                .toArray(new int[adjacencyCoordIndicesList.size()][]);
        Entry<int[][][], int[][]> entry = new AbstractMap.SimpleImmutableEntry<>(
                verticesIndicesOfAdjacentMergeableFacesForThisGrid, adjacencyCoordIndicesPerFaceArray);
        return (entry);
    }

    @Override
    public float[][][] getAxisAlignedBoundingBoxesExtremeCoordinates(final float[][] containerBoundingBox) {
        // TODO: implement it
        return (null);
    }

    public final void setOrientation(Side side, Orientation orientation) {
        faceOrientation[side.ordinal()] = orientation;
        buffersRecomputationNeeded = true;
        markDirty();
    }

    public final Orientation getOrientation(Side side) {
        return (faceOrientation[side.ordinal()]);
    }

    public final Orientation[] getFaceOrientation() {
        return (faceOrientation);
    }

    public final void setFaceOrientation(final Orientation[] faceOrientation) {
        this.faceOrientation = faceOrientation;
        buffersRecomputationNeeded = true;
        markDirty();
    }

    public final float[][] getTexCoord() {
        return (texCoord);
    }

    public final float getTexCoord(final Side side, final int texCoordIndex) {
        return (texCoord[side.ordinal()][texCoordIndex]);
    }

    public final void setTexCoord(final Side side, final int texCoordIndex, final float value) {
        this.texCoord[side.ordinal()][texCoordIndex] = value;
        buffersRecomputationNeeded = true;
        markDirty();
    }

    public final void setTexCoord(final float[][] texCoord) {
        this.texCoord = texCoord;
        buffersRecomputationNeeded = true;
        markDirty();
    }

    @Transient
    @Override
    public final IntBuffer getIndexBuffer() {
        recomputeBuffersIfNeeded();
        return (indexBuffer);
    }

    @Transient
    @Override
    public final IntBuffer getMergeableIndexBuffer() {
        recomputeBuffersIfNeeded();
        return (mergeableIndexBuffer);
    }

    @Transient
    @Override
    public final FloatBuffer getNormalBuffer() {
        recomputeBuffersIfNeeded();
        return (normalBuffer);
    }

    @Transient
    @Override
    public final FloatBuffer getVertexBuffer() {
        recomputeBuffersIfNeeded();
        return (vertexBuffer);
    }

    @Transient
    @Override
    public final FloatBuffer getTexCoordBuffer() {
        recomputeBuffersIfNeeded();
        return (texCoordBuffer);
    }
}