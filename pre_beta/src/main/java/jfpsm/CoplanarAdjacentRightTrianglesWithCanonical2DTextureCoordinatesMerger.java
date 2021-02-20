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
package jfpsm;

import java.nio.FloatBuffer;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import jfpsm.ArrayHelper.Vector2i;

import com.ardor3d.math.Plane;
import com.ardor3d.math.Triangle;
import com.ardor3d.math.Vector2;
import com.ardor3d.math.Vector3;
import com.ardor3d.math.type.ReadOnlyVector3;
import com.ardor3d.renderer.IndexMode;
import com.ardor3d.scenegraph.FloatBufferData;
import com.ardor3d.scenegraph.Mesh;
import com.ardor3d.scenegraph.MeshData;
import com.ardor3d.util.geom.BufferUtils;
import com.ardor3d.util.geom.GeometryTool;
import com.ardor3d.util.geom.GeometryTool.MatchCondition;

/**
 * mesh optimizer, which merges coplanar adjacent right triangles whose all 2D
 * texture coordinates are canonical ([0;0], [0;1], [1;0] or [1;1])
 * 
 * @author Julien Gouesse
 *
 */
public class CoplanarAdjacentRightTrianglesWithCanonical2DTextureCoordinatesMerger {

    static final class TriangleInfo {

        private final int primitiveIndex;

        private final int sectionIndex;

        private final int sideIndexOfHypotenuse;
        
        private final int rightAngleVertexIndex;
        
        private final Vector3[] vertices;
        
        private final Vector2[] textureCoords;
        
        private final int[] indices;

        TriangleInfo(final int primitiveIndex, final int sectionIndex, final MeshData meshData) {
            super();
            this.primitiveIndex = primitiveIndex;
            this.sectionIndex = sectionIndex;
            if (meshData == null) {
                this.vertices = null;
                this.textureCoords = null;
                this.indices = null;
                this.rightAngleVertexIndex = -1;
                this.sideIndexOfHypotenuse = -1;
            } else {
                this.vertices = meshData.getPrimitiveVertices(this.primitiveIndex, this.sectionIndex, null);
                this.textureCoords = getPrimitiveTextureCoords(meshData, this.primitiveIndex, this.sectionIndex, 0, null);
                this.indices = meshData.getPrimitiveIndices(this.primitiveIndex, this.sectionIndex, null);
                this.rightAngleVertexIndex = IntStream.range(0, 3)
                        // computes the dot product of two vectors to check whether there's a right angle at their common vertex
                        .filter((final int triIndex) -> this.vertices[triIndex].subtract(this.vertices[(triIndex + 1) % 3], null).dot(this.vertices[triIndex].subtract(this.vertices[(triIndex + 2) % 3], null)) == 0.0)
                        .findFirst()
                        .orElse(-1);
                this.sideIndexOfHypotenuse = this.rightAngleVertexIndex == -1 ? -1 : (this.rightAngleVertexIndex + 1) % 3;
            }
        }
        
        boolean isRightAngled() {
            return this.rightAngleVertexIndex != -1;
        }
        
        Vector3[] getVertices() {
            return vertices;
        }
        
        Vector2[] getTextureCoords() {
            return textureCoords;
        }
        
        int[] getIndices() {
            return indices;
        }

        @Override
        public final boolean equals(final Object o) {
            final boolean result;
            if (o == null || !(o instanceof TriangleInfo)) {
                result = false;
            } else if (o == this) {
                result = true;
            } else {
                final TriangleInfo r = (TriangleInfo) o;
                result = primitiveIndex == r.primitiveIndex && sectionIndex == r.sectionIndex
                            && rightAngleVertexIndex == r.rightAngleVertexIndex;
            }
            return result;
        }

        @Override
        public int hashCode() {
            return ((rightAngleVertexIndex & 0xff) | (sectionIndex & 0xff << 8) | (primitiveIndex & 0xffff << 16));
        }
        
        @Override
        public String toString() {
            return "TriangleInfo {primitiveIndex: " + primitiveIndex + " sectionIndex: " + sectionIndex + " rightAngleVertexIndex: " + rightAngleVertexIndex + "}";
        }
    }

    static final class NextQuadInfo {

        private final Vector3[] vertices;

        private final Vector2[] textureCoords;

        private final int[] indices;

        NextQuadInfo(final Vector3[] vertices, final Vector2[] textureCoords, final int[] indices) {
            this.vertices = vertices;
            this.textureCoords = textureCoords;
            this.indices = indices;
        }
    }
    
    /**
     * Tells whether the passed texture coordinates are canonical (i.e if they're equal to 0 or 1)
     * 
     * @param textureCoords texture coordinates
     * @return <code>true</code> if the passed texture coordinates are canonical, otherwise <code>false</code>
     */
    private static boolean hasCanonicalTextureCoords(final Vector2[] textureCoords) {
        return Arrays.stream(textureCoords)
                     .flatMapToDouble((final Vector2 textureCoord) -> DoubleStream.of(textureCoord.getX(), textureCoord.getY()))
                     .allMatch((final double uv) -> uv == 0 || uv == 1);
    }
    
    /**
     * 
     * @param mesh
     *            using only the first texture unit
     * @return
     */
    public static void optimize(final Mesh mesh) {
        final MeshData meshData = mesh.getMeshData();
        // if there is exactly one texture unit, if there is a texture buffer for this first texture unit
        if (meshData.getNumberOfUnits() == 1 && meshData.getTextureBuffer(0) != null && 
            // if there are 2 texture coordinates per vertex and 3 vertex coordinates per vertex
            meshData.getTextureCoords(0).getValuesPerTuple() == 2 && meshData.getVertexBuffer() != null && 
            meshData.getVertexCoords().getValuesPerTuple() == 3 &&
            // if all sections contains triangles
            IntStream.range(0, meshData.getSectionCount()).mapToObj(meshData::getIndexMode).allMatch(IndexMode.Triangles::equals)) {
            // converts this geometry into non indexed geometry (if necessary) in order to ease further operations
            final boolean previousGeometryWasIndexed = meshData.getIndexBuffer() != null;
            if (previousGeometryWasIndexed) {
                new GeometryTool(true).convertIndexedGeometryIntoNonIndexedGeometry(meshData);
                System.out.println("Non indexed input: " + meshData.getVertexCount());
            }
            // first step: separates right triangles with canonical 2D texture coordinates from the others, loops on all sections of the mesh data
            final List<TriangleInfo> rightTrianglesWithCanonical2DTextureCoordinatesInfos = IntStream.range(0, meshData.getSectionCount())
                // loops on all triangles of each section
                .mapToObj((final int sectionIndex) -> IntStream.range(0, meshData.getPrimitiveCount(sectionIndex))
                // checks whether its texture coordinates are canonical, only considers the first texture index
                .filter((final int trianglePrimitiveIndex) -> hasCanonicalTextureCoords(getPrimitiveTextureCoords(meshData, trianglePrimitiveIndex, sectionIndex, 0, null)))
                .mapToObj((final int trianglePrimitiveIndex) -> new TriangleInfo(trianglePrimitiveIndex, sectionIndex, meshData))
                .filter(TriangleInfo::isRightAngled))
                .flatMap(Stream::sequential)
                .collect(Collectors.toList());
            rightTrianglesWithCanonical2DTextureCoordinatesInfos.forEach(System.out::println);
            System.out.println("[1] Number of triangles: " + rightTrianglesWithCanonical2DTextureCoordinatesInfos.size());
            // second step: sorts the triangles of the former set by planes (4D: normal + distance to plane)
            Map<Plane, List<TriangleInfo>> mapOfTrianglesByPlanes = rightTrianglesWithCanonical2DTextureCoordinatesInfos.stream()
                    .map((final TriangleInfo info) -> {
                final Triangle tmpTriangle = new Triangle();
                // gets the 3 vertices of the triangle
                final Vector3[] triangleVertices = info.getVertices();
                // sets the vertices of the temporary triangle
                IntStream.range(0, triangleVertices.length)
                         .forEach((final int vertexInternalIndex) -> tmpTriangle.set(vertexInternalIndex, triangleVertices[vertexInternalIndex]));
                // computes its normal
                final ReadOnlyVector3 triangleNormal = tmpTriangle.getNormal();
                // computes its distance to plane d=dot(normal,vertex)
                final double distanceToPlane = triangleNormal.dot(tmpTriangle.getCenter());
                // creates the plane
                final Plane plane = new Plane(triangleNormal, distanceToPlane);
                return new AbstractMap.SimpleImmutableEntry<>(plane, info);
            }).collect(Collectors.groupingBy(AbstractMap.SimpleImmutableEntry::getKey, 
                       Collectors.mapping(AbstractMap.SimpleImmutableEntry::getValue, Collectors.toList())));
            System.out.println("[2] Number of planes: " + mapOfTrianglesByPlanes.size());
            mapOfTrianglesByPlanes.entrySet().stream().forEach(System.out::println);
            System.out.println("[2] Number of triangles: " + mapOfTrianglesByPlanes.values().stream().mapToInt(List::size).sum());
            // third step: retains only triangles by pairs which could be used to create rectangles
            final Map<Plane, List<TriangleInfo[]>> mapOfRightTrianglesWithSameHypotenusesByPairs = mapOfTrianglesByPlanes.entrySet().stream().map((final Map.Entry<Plane, List<TriangleInfo>> entry) -> {
                final List<TriangleInfo> rightTriangles = entry.getValue();
                final int triCount = rightTriangles.size();
                // for each RightTriangleInfo instance
                return new AbstractMap.SimpleImmutableEntry<>(entry.getKey(), IntStream.range(0, triCount)
                         .mapToObj((final int triIndex1) -> IntStream.range(0, triCount)
                                                                     .filter((final int triIndex2) ->  triIndex1 < triIndex2)
                                                                     .mapToObj((final int triIndex2) -> new int[]{triIndex1, triIndex2}))
                         .flatMap(Stream::sequential)
                         .filter((final int[] triIndices) -> {
                             final TriangleInfo tri1 = rightTriangles.get(triIndices[0]);
                             final TriangleInfo tri2 = rightTriangles.get(triIndices[1]);
                             final Vector3[] tri1Vertices = tri1.getVertices();
                             final Vector3[] tri2Vertices = tri2.getVertices();
                             final Vector2[] tri1TextureCoords = tri1.getTextureCoords();
                             final Vector2[] tri2TextureCoords = tri2.getTextureCoords();
                             // checks if the vertices of the hypotenuse are the same but in the opposite order (so that both triangles have the same normal)
                             return tri1Vertices[(tri1.rightAngleVertexIndex + 1) % 3].equals(tri2Vertices[(tri2.rightAngleVertexIndex + 2) % 3]) &&
                                    tri1Vertices[(tri1.rightAngleVertexIndex + 2) % 3].equals(tri2Vertices[(tri2.rightAngleVertexIndex + 1) % 3]) &&
                                    // checks that the texture coordinates of the vertices in the hypotenuse are the same
                                    tri1TextureCoords[(tri1.rightAngleVertexIndex + 1) % 3].equals(tri2TextureCoords[(tri2.rightAngleVertexIndex + 2) % 3]) &&
                                    tri1TextureCoords[(tri1.rightAngleVertexIndex + 2) % 3].equals(tri2TextureCoords[(tri2.rightAngleVertexIndex + 1) % 3]) &&
                                    // checks that there's a symmetry to go to the vertex outside of the hypotenuse
                                    tri1Vertices[tri1.rightAngleVertexIndex].subtract(tri1Vertices[(tri1.rightAngleVertexIndex + 2) % 3], null).equals(
                                    tri2Vertices[(tri2.rightAngleVertexIndex + 2) % 3].subtract(tri2Vertices[tri2.rightAngleVertexIndex], null)) &&
                                    // checks that all canonical texture coordinates are in the pair of triangles
                                    IntStream.of(Stream.of(tri1TextureCoords[tri1.rightAngleVertexIndex], tri1TextureCoords[(tri1.rightAngleVertexIndex + 1) % 3], tri1TextureCoords[(tri1.rightAngleVertexIndex + 2) % 3], tri2TextureCoords[tri2.rightAngleVertexIndex])
                                                       .map((final Vector2 textureCoord) -> new int[]{textureCoord.getX() == 0.0 ? 1 : 0, textureCoord.getX() == 1.0 ? 1 : 0, textureCoord.getY() == 0.0 ? 1 : 0, textureCoord.getY() == 1.0 ? 1 : 0})
                                                       .reduce((final int[] v0, final int[] v1) -> new int[] {v0[0] + v1[0], v0[1] + v1[1], v0[2] + v1[2], v0[3] + v1[3]}).get())
                                             .allMatch((final int texCoordCount) -> texCoordCount == 2);
                         })
                         .map((final int[] triIndices) -> new TriangleInfo[]{rightTriangles.get(triIndices[0]), rightTriangles.get(triIndices[1])})
                         .collect(Collectors.toList()));
            })
                    .filter((final AbstractMap.SimpleImmutableEntry<Plane, List<TriangleInfo[]>> entry) -> !entry.getValue().isEmpty())
                    .collect(Collectors.toMap(AbstractMap.SimpleImmutableEntry::getKey, AbstractMap.SimpleImmutableEntry::getValue));
            System.out.println("[3] Number of planes: " + mapOfRightTrianglesWithSameHypotenusesByPairs.size());
            mapOfRightTrianglesWithSameHypotenusesByPairs.entrySet().stream()
                .map((final Map.Entry<Plane, List<TriangleInfo[]>> entry) -> entry.getKey() + "=" + entry.getValue().stream().flatMap(Arrays::stream).collect(Collectors.toList()))
                .forEach(System.out::println);
            System.out.println("[3] Number of triangles: " + mapOfRightTrianglesWithSameHypotenusesByPairs.values().stream().flatMap(List::stream).flatMap(Arrays::stream).count());
            // fourth step: creates lists containing all adjacent rectangles in the same planes
            // for each plane of the map
            final Map<Plane, List<Map.Entry<Integer, TriangleInfo[]>>> mapOfRightTrianglesWithSameHypotenusesByQuartets = mapOfRightTrianglesWithSameHypotenusesByPairs.entrySet().stream().map((final Map.Entry<Plane, List<TriangleInfo[]>> entry) -> {
                final List<TriangleInfo[]> rightTrianglePairList = entry.getValue();
                final int triPairCount = rightTrianglePairList.size();
                return new AbstractMap.SimpleImmutableEntry<>(entry.getKey(), IntStream.range(0, triPairCount)
                         .mapToObj((final int triPairIndex1) -> IntStream.range(0, triPairCount)
                                                                     .filter((final int triPairIndex2) ->  triPairIndex1 < triPairIndex2)
                                                                     .mapToObj((final int triPairIndex2) -> new int[]{triPairIndex1, triPairIndex2}))
                         .flatMap(Stream::sequential)
                         .map((final int[] triPairIndices) -> {
                             final TriangleInfo tri1 = rightTrianglePairList.get(triPairIndices[0])[0];
                             final TriangleInfo tri2 = rightTrianglePairList.get(triPairIndices[0])[1];
                             final TriangleInfo tri3 = rightTrianglePairList.get(triPairIndices[1])[0];
                             final TriangleInfo tri4 = rightTrianglePairList.get(triPairIndices[1])[1];
                             final Map.Entry<Integer, TriangleInfo[]> orientationAndTriQuartet;
                             // checks that the texture coordinates matches so that there is a chance to merge them
                             if (Arrays.equals(tri1.getTextureCoords(), tri3.getTextureCoords()) && Arrays.equals(tri2.getTextureCoords(), tri4.getTextureCoords())) {
                                 // looks for a quartet that matches by looking for a common side and a projection of a vertex on the vector of the hypotenuse
                                 if (tri1.getVertices()[(tri1.rightAngleVertexIndex + 2)% 3].equals(tri4.getVertices()[tri4.rightAngleVertexIndex]) && 
                                     tri1.getVertices()[tri1.rightAngleVertexIndex].equals(tri4.getVertices()[(tri4.rightAngleVertexIndex + 2)% 3]) &&
                                     tri1.getVertices()[tri1.rightAngleVertexIndex].add(tri1.getVertices()[(tri1.rightAngleVertexIndex + 2)% 3].subtract(tri1.getVertices()[(tri1.rightAngleVertexIndex + 1)% 3], null), null).equals(tri4.getVertices()[(tri4.rightAngleVertexIndex + 1)% 3])) {
                                     orientationAndTriQuartet = new AbstractMap.SimpleImmutableEntry<>(Integer.valueOf(0), new TriangleInfo[] {tri1, tri2, tri3, tri4});
                                 } else if (tri1.getVertices()[tri1.rightAngleVertexIndex].equals(tri4.getVertices()[(tri4.rightAngleVertexIndex + 1)% 3]) && 
                                            tri1.getVertices()[(tri1.rightAngleVertexIndex + 1)% 3].equals(tri4.getVertices()[tri4.rightAngleVertexIndex]) &&
                                            tri1.getVertices()[tri1.rightAngleVertexIndex].add(tri1.getVertices()[(tri1.rightAngleVertexIndex + 1)% 3].subtract(tri1.getVertices()[(tri1.rightAngleVertexIndex + 2)% 3], null), null).equals(tri4.getVertices()[(tri4.rightAngleVertexIndex + 2)% 3])) {
                                         orientationAndTriQuartet = new AbstractMap.SimpleImmutableEntry<>(Integer.valueOf(1), new TriangleInfo[] {tri1, tri2, tri3, tri4});
                                 } else if (tri2.getVertices()[(tri2.rightAngleVertexIndex + 2)% 3].equals(tri3.getVertices()[tri3.rightAngleVertexIndex]) && 
                                            tri2.getVertices()[tri2.rightAngleVertexIndex].equals(tri3.getVertices()[(tri3.rightAngleVertexIndex + 2)% 3]) &&
                                            tri2.getVertices()[tri2.rightAngleVertexIndex].add(tri2.getVertices()[(tri2.rightAngleVertexIndex + 2)% 3].subtract(tri2.getVertices()[(tri2.rightAngleVertexIndex + 1)% 3], null), null).equals(tri3.getVertices()[(tri3.rightAngleVertexIndex + 1)% 3])) {
                                         orientationAndTriQuartet = new AbstractMap.SimpleImmutableEntry<>(Integer.valueOf(2), new TriangleInfo[] {tri1, tri2, tri3, tri4});
                                 } else if (tri2.getVertices()[tri2.rightAngleVertexIndex].equals(tri3.getVertices()[(tri3.rightAngleVertexIndex + 1)% 3]) && 
                                            tri2.getVertices()[(tri2.rightAngleVertexIndex + 1)% 3].equals(tri3.getVertices()[tri3.rightAngleVertexIndex]) &&
                                            tri2.getVertices()[tri2.rightAngleVertexIndex].add(tri2.getVertices()[(tri2.rightAngleVertexIndex + 1)% 3].subtract(tri2.getVertices()[(tri2.rightAngleVertexIndex + 2)% 3], null), null).equals(tri3.getVertices()[(tri3.rightAngleVertexIndex + 2)% 3])) {
                                         orientationAndTriQuartet = new AbstractMap.SimpleImmutableEntry<>(Integer.valueOf(3), new TriangleInfo[] {tri1, tri2, tri3, tri4});
                                 } else {
                                     orientationAndTriQuartet = null;
                                 }
                             } else {
                                 orientationAndTriQuartet = null;
                             }
                             return orientationAndTriQuartet;
                         })
                         .filter(Objects::nonNull)
                         .collect(Collectors.toList()));
            })
                    .filter((final AbstractMap.SimpleImmutableEntry<Plane, List<Map.Entry<Integer, TriangleInfo[]>>> entry) -> !entry.getValue().isEmpty())
                    .collect(Collectors.toMap(AbstractMap.SimpleImmutableEntry::getKey, AbstractMap.SimpleImmutableEntry::getValue));
            System.out.println("[4] Number of planes: " + mapOfRightTrianglesWithSameHypotenusesByQuartets.size());
            mapOfRightTrianglesWithSameHypotenusesByQuartets.entrySet().stream()
                .map((final Map.Entry<Plane, List<Map.Entry<Integer, TriangleInfo[]>>> entry) -> entry.getKey() + "=" + entry.getValue().stream().map(Map.Entry::getValue).flatMap(Arrays::stream).collect(Collectors.toList()))
                .forEach(System.out::println);
            System.out.println("[4] Number of triangles: " + mapOfRightTrianglesWithSameHypotenusesByQuartets.values().stream().flatMap(List::stream).map(Map.Entry::getValue).flatMap(Arrays::stream).distinct().count());
            // fifth step: creates lists of adjacent rectangles in the same planes usable to make bigger rectangles
            final Map<Plane, List<TriangleInfo[][][]>> mapOfAdjacentRightTriangles = mapOfRightTrianglesWithSameHypotenusesByQuartets.entrySet().stream().map((final Map.Entry<Plane, List<Map.Entry<Integer, TriangleInfo[]>>> entry) -> {
                final List<List<Map.Entry<Integer, TriangleInfo[]>>> rightTrianglesWithSameHypotenusesByQuartetList = new ArrayList<>();
                // splits the list of quartets into lists of adjacent quartets
                entry.getValue().stream().forEachOrdered((final Map.Entry<Integer, TriangleInfo[]> orientationAndTriQuartet) -> rightTrianglesWithSameHypotenusesByQuartetList.stream()
                       .filter((final List<Map.Entry<Integer, TriangleInfo[]>> adjacentTriQuartetList) -> 
                                  adjacentTriQuartetList.stream()
                                      .map(Map.Entry::getValue)
                                      .flatMap(Arrays::stream)
                                      .anyMatch((final TriangleInfo quartetMember) -> Arrays.stream(orientationAndTriQuartet.getValue()).anyMatch(quartetMember::equals)))
                       .findFirst()
                       .orElseGet(() -> {
                           final List<Map.Entry<Integer, TriangleInfo[]>> quartetList = new ArrayList<>();
                           rightTrianglesWithSameHypotenusesByQuartetList.add(quartetList);
                           return quartetList;
                           }).add(orientationAndTriQuartet));
                // converts each sublist into a 3D array
                return new AbstractMap.SimpleImmutableEntry<>(entry.getKey(), rightTrianglesWithSameHypotenusesByQuartetList.stream().map((final List<Map.Entry<Integer, TriangleInfo[]>> adjacentTriQuartetList) -> {
                    // computes the length of the largest path in the same direction
                    final long maxDirection = adjacentTriQuartetList.stream()
                            .map(Map.Entry::getKey)
                            .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()))
                            .values()
                            .stream()
                            .mapToLong(Long::longValue)
                            .max()
                            .getAsLong();
                    // computes the size
                    final int size = (2 * (int) maxDirection) + 1;
                    final TriangleInfo[][][] triPairArray = new TriangleInfo[size][size][];
                    final List<Map.Entry<Integer, TriangleInfo[]>> adjacentTriQuartetListToTreat = new ArrayList<>(adjacentTriQuartetList);
                    Map.Entry<Integer, TriangleInfo[]> adjacentEntry = null;
                    for (int x, y;!adjacentTriQuartetListToTreat.isEmpty();) {
                        if (adjacentEntry == null) {
                            adjacentEntry = adjacentTriQuartetListToTreat.remove(0);
                            x = (int) maxDirection;
                            y = (int) maxDirection;
                        } else {
                            // finds a quartet whose two first triangles are already in the array
                            final Map.Entry<Map.Entry<Integer, TriangleInfo[]>, int[]> adjacentEntryWithLocation = adjacentTriQuartetListToTreat.stream()
                                .map((final Map.Entry<Integer, TriangleInfo[]> currentAdjacentEntry) -> new AbstractMap.SimpleImmutableEntry<>(currentAdjacentEntry, IntStream.range(0, size)
                                        .mapToObj((final int currentX) -> IntStream.range(0, size)
                                                .mapToObj((final int currentY) -> new int[]{currentX, currentY}))
                                        .flatMap(Stream::sequential)
                                        .filter((final int[] xy) -> triPairArray[xy[0]][xy[1]] != null)
                                        .filter((final int[] xy) -> Objects.equals(triPairArray[xy[0]][xy[1]][0], currentAdjacentEntry.getValue()[0]) && Objects.equals(triPairArray[xy[0]][xy[1]][1], currentAdjacentEntry.getValue()[1]))
                                        .findFirst()
                                        .orElse(new int[] {-1, -1})  ))
                                .filter((final Map.Entry<Map.Entry<Integer, TriangleInfo[]>, int[]> currentEntryWithLocation) -> currentEntryWithLocation.getValue()[0] != -1 && currentEntryWithLocation.getValue()[1] != -1)
                                .findFirst()
                                .orElse(null);
                            if (adjacentEntryWithLocation == null) {
                                x = -1;
                                y = -1;
                                adjacentEntry = null;
                                System.err.println("Failed to treat some quartets: " + adjacentTriQuartetListToTreat);
                                break;
                            } else {
                                x = adjacentEntryWithLocation.getValue()[0];
                                y = adjacentEntryWithLocation.getValue()[1];
                                adjacentEntry = adjacentEntryWithLocation.getKey();
                                adjacentTriQuartetListToTreat.remove(adjacentEntry);
                            }
                        }
                        triPairArray[x][y] = new TriangleInfo[2];
                        triPairArray[x][y][0] = adjacentEntry.getValue()[0];
                        triPairArray[x][y][1] = adjacentEntry.getValue()[1];
                        // updates x and y depending on the value of the key
                        switch(adjacentEntry.getKey().intValue()) {
                            case 0:
                                y--;
                                break;
                            case 1:
                                x--;
                                break;
                            case 2:
                                y++;
                                break;
                            case 3:
                                x++;
                                break;
                        }
                        triPairArray[x][y] = new TriangleInfo[2];
                        triPairArray[x][y][0] = adjacentEntry.getValue()[2];
                        triPairArray[x][y][1] = adjacentEntry.getValue()[3];
                    }
                    return triPairArray;
                }).collect(Collectors.toList()));
            }).collect(Collectors.toMap(AbstractMap.SimpleImmutableEntry::getKey, AbstractMap.SimpleImmutableEntry::getValue));
            System.out.println("[5.0] Number of planes: " + mapOfAdjacentRightTriangles.size());
            mapOfAdjacentRightTriangles.entrySet().stream()
                .map((final Map.Entry<Plane, List<TriangleInfo[][][]>> entry) -> entry.getKey() + "=" + entry.getValue().stream().flatMap(Arrays::stream).flatMap(Arrays::stream).filter(Objects::nonNull).flatMap(Arrays::stream).filter(Objects::nonNull).collect(Collectors.toList()))
                .forEach(System.out::println);
            System.out.println("[5.0] Number of triangles: " + mapOfAdjacentRightTriangles.values().stream().flatMap(List::stream).flatMap(Arrays::stream).flatMap(Arrays::stream).filter(Objects::nonNull).flatMap(Arrays::stream).filter(Objects::nonNull).count());
            // compute the sets of mergeable triangles
            final Map<Plane, List<TriangleInfo[][][]>> mapOfMergeableTris = mapOfAdjacentRightTriangles.entrySet().stream().map((final Map.Entry<Plane, List<TriangleInfo[][][]>> entry) -> new AbstractMap.SimpleImmutableEntry<>(entry.getKey(), 
                entry.getValue().stream().map(CoplanarAdjacentRightTrianglesWithCanonical2DTextureCoordinatesMerger::computeAdjacentMergeableTrisArraysMap).map(Map::values).flatMap(Collection::stream).collect(Collectors.toList())))
                .collect(Collectors.toMap(AbstractMap.SimpleImmutableEntry::getKey, AbstractMap.SimpleImmutableEntry::getValue));
            System.out.println("[5.1] Number of planes: " + mapOfMergeableTris.size());
            mapOfMergeableTris.entrySet().stream()
                .map((final Map.Entry<Plane, List<TriangleInfo[][][]>> entry) -> entry.getKey() + "=" + entry.getValue().stream().flatMap(Arrays::stream).flatMap(Arrays::stream).filter(Objects::nonNull).flatMap(Arrays::stream).filter(Objects::nonNull).collect(Collectors.toList()))
                .forEach(System.out::println);
            System.out.println("[5.1] Number of triangles: " + mapOfMergeableTris.values().stream().flatMap(List::stream).flatMap(Arrays::stream).flatMap(Arrays::stream).filter(Objects::nonNull).flatMap(Arrays::stream).filter(Objects::nonNull).count());
            // sixth step: creates these bigger rectangles with texture coordinates greater than 1 in order to use texture repeat
            /*mapOfMergeableTris.entrySet().stream().map((final Map.Entry<Plane, List<TriangleInfo[][][]>> entry) -> 
                entry.getValue().stream().map((final TriangleInfo[][][] adjacentTriArray) -> {
                //TODO
                System.out.println(adjacentTriArray);
                return null;
            }));*/
            HashMap<Plane, HashMap<TriangleInfo[][][], NextQuadInfo>> mapOfPreviousAndNextAdjacentTrisMaps = new HashMap<>();
            // for each plane
            for (Map.Entry<Plane, List<TriangleInfo[][][]>> entry : mapOfMergeableTris.entrySet()) {
                final Plane plane = entry.getKey();
                final HashMap<TriangleInfo[][][], NextQuadInfo> previousAdjacentTrisAndNextQuadInfosMaps = new HashMap<>();
                mapOfPreviousAndNextAdjacentTrisMaps.put(plane, previousAdjacentTrisAndNextQuadInfosMaps);
                // for each array of adjacent triangles which could be merged to make bigger rectangles
                for (final TriangleInfo[][][] adjacentTrisArray : entry.getValue()) {
                    // checks if it contains at least one row and if the first row contains at least one element
                    if (adjacentTrisArray.length >= 1 && adjacentTrisArray[0] != null
                        && adjacentTrisArray[0].length >= 1) {
                        // checks if this array is full and rectangular (i.e all rows contain the same count of elements)
                        boolean isFull = true;
                        boolean isRectangular = true;
                        for (int rowIndex = 0; rowIndex < adjacentTrisArray.length && isRectangular
                                && isFull; rowIndex++) {
                            if (adjacentTrisArray[rowIndex] == null
                                    || adjacentTrisArray[rowIndex].length != adjacentTrisArray[0].length)
                                isRectangular = false;
                            else
                                for (int columnIndex = 0; columnIndex < adjacentTrisArray[rowIndex].length
                                        && isFull; columnIndex++)
                                    if (adjacentTrisArray[rowIndex][columnIndex] == null
                                            || adjacentTrisArray[rowIndex][columnIndex].length != 2)
                                        isFull = false;
                        }
                        // FIXME this test never passes
                        // checks if this array is full, rectangular and if
                        // it contains more than one pair of adjacent
                        // triangles
                        if (isRectangular && isFull
                                && (adjacentTrisArray.length > 1 || adjacentTrisArray[0].length > 1)) {
                            // as this array is rectangular, it has a consistent row count and column count
                            final int rowCount = adjacentTrisArray.length;
                            final int columnCount = adjacentTrisArray[0].length;
                            // computes the new pair of right adjacent
                            // triangles
                            final TriangleInfo[] mergedAdjacentTris = new TriangleInfo[2];
                            final Vector3[] mergedAdjacentTrisVertices = new Vector3[4];
                            final Vector2[] mergedAdjacentTrisTextureCoords = new Vector2[4];
                            final int[] tmpLocalIndices = new int[4];
                            final int[] mergedAdjacentTrisVerticesIndices = new int[6];
                            final Vector3[] testedAdjacentTrisVertices = new Vector3[8];
                            final Vector2[] testedAdjacentTrisTextureCoords = new Vector2[4];
                            // for each pair of triangles in a corner of the array
                            for (int rowIndex = 0; rowIndex <= 1; rowIndex++) {
                                final int rawRowIndex = rowIndex * (rowCount - 1);
                                for (int columnIndex = 0; columnIndex <= 1; columnIndex++) {
                                    final int rawColumnIndex = columnIndex * (columnCount - 1);
                                    final TriangleInfo tri1 = adjacentTrisArray[rawRowIndex][rawColumnIndex][0];
                                    final TriangleInfo tri2 = adjacentTrisArray[rawRowIndex][rawColumnIndex][1];
                                    if (tri1 == null || tri2 == null) {
                                        continue;
                                    }
                                    final Vector3[] tri1Vertices = tri1.getVertices();
                                    final Vector3[] tri2Vertices = tri2.getVertices();
                                    // retrieves the distinct vertices of
                                    // the current corner
                                    // both triangles have reverse vertex
                                    // orders (see the third step)
                                    testedAdjacentTrisVertices[0] = tri1Vertices[tri1.sideIndexOfHypotenuse];
                                    testedAdjacentTrisVertices[1] = tri2Vertices[tri2.sideIndexOfHypotenuse];
                                    testedAdjacentTrisVertices[2] = tri1Vertices[(tri1.sideIndexOfHypotenuse + 2)
                                            % 3];
                                    testedAdjacentTrisVertices[3] = tri2Vertices[(tri2.sideIndexOfHypotenuse + 2)
                                            % 3];
                                    // retrieves the texture coordinates
                                    final Vector2[] tri1TextureCoords = tri1.getTextureCoords();
                                    final Vector2[] tri2TextureCoords = tri2.getTextureCoords();
                                    testedAdjacentTrisTextureCoords[0] = tri1TextureCoords[tri1.sideIndexOfHypotenuse];
                                    testedAdjacentTrisTextureCoords[1] = tri2TextureCoords[tri2.sideIndexOfHypotenuse];
                                    testedAdjacentTrisTextureCoords[2] = tri1TextureCoords[(tri1.sideIndexOfHypotenuse
                                            + 2) % 3];
                                    testedAdjacentTrisTextureCoords[3] = tri2TextureCoords[(tri2.sideIndexOfHypotenuse
                                            + 2) % 3];
                                    // looks for the real vertex of the corner
                                    boolean cornerVertexFound = false;
                                    for (int testedVertexIndex = 0; testedVertexIndex < 4
                                            && !cornerVertexFound; testedVertexIndex++) {
                                        cornerVertexFound = true;
                                        for (int testedCloseCell1DIndex = 1; testedCloseCell1DIndex <= 3
                                                && cornerVertexFound; testedCloseCell1DIndex++) {
                                            final int secondaryRawRowIndex = Math.max(0, rawRowIndex
                                                    + ((rowIndex == 0 ? 1 : -1) * (testedCloseCell1DIndex / 2)))
                                                    % rowCount;
                                            final int secondaryRawColumnIndex = Math.max(0, rawColumnIndex
                                                    + ((columnIndex == 0 ? 1 : -1) * (testedCloseCell1DIndex % 2)))
                                                    % columnCount;
                                            final TriangleInfo tri3 = adjacentTrisArray[secondaryRawRowIndex][secondaryRawColumnIndex][0];
                                            final TriangleInfo tri4 = adjacentTrisArray[secondaryRawRowIndex][secondaryRawColumnIndex][1];
                                            final Vector3[] tri3Vertices = tri3.getVertices();
                                            final Vector3[] tri4Vertices = tri4.getVertices();
                                            testedAdjacentTrisVertices[4] = tri3Vertices[tri3.sideIndexOfHypotenuse];
                                            testedAdjacentTrisVertices[5] = tri4Vertices[tri4.sideIndexOfHypotenuse];
                                            testedAdjacentTrisVertices[6] = tri3Vertices[(tri3.sideIndexOfHypotenuse
                                                    + 2) % 3];
                                            testedAdjacentTrisVertices[7] = tri4Vertices[(tri4.sideIndexOfHypotenuse
                                                    + 2) % 3];
                                            for (int secondaryTestedVertexIndex = 4; secondaryTestedVertexIndex < 8
                                                    && cornerVertexFound; secondaryTestedVertexIndex++)
                                                cornerVertexFound = !testedAdjacentTrisVertices[testedVertexIndex]
                                                        .equals(testedAdjacentTrisVertices[secondaryTestedVertexIndex]);
                                        }
                                        if (cornerVertexFound) {
                                            // checks whether this corner is already in use
                                            boolean cornerAlreadyInUse = false;
                                            for (int mergedAdjacentTrisVertexIndex = 0; mergedAdjacentTrisVertexIndex < 4
                                                    && !cornerAlreadyInUse; mergedAdjacentTrisVertexIndex++)
                                                if (mergedAdjacentTrisVertices[mergedAdjacentTrisVertexIndex] != null
                                                        && mergedAdjacentTrisVertices[mergedAdjacentTrisVertexIndex]
                                                                .equals(testedAdjacentTrisVertices[testedVertexIndex]))
                                                    cornerAlreadyInUse = true;
                                            // if this corner is already in
                                            // use, the search must go on
                                            if (cornerAlreadyInUse)
                                                cornerVertexFound = false;
                                            else {
                                                final int localIndex = (rowIndex / 2) + (columnIndex % 2);
                                                // stores the vertex
                                                mergedAdjacentTrisVertices[localIndex] = testedAdjacentTrisVertices[testedVertexIndex];
                                                // stores its texture
                                                // coordinates
                                                mergedAdjacentTrisTextureCoords[localIndex] = testedAdjacentTrisTextureCoords[testedVertexIndex];
                                                // stores its temporary
                                                // index in order to know
                                                // from which triangle it
                                                // comes and whether it is
                                                // on the hypotenuse
                                                tmpLocalIndices[localIndex] = testedVertexIndex;
                                                // if this vertex is not on
                                                // the hypotenuse
                                                if (testedVertexIndex / 2 == 1) {
                                                    // stores its triangle in order to keep the same orientation
                                                    if (mergedAdjacentTris[0] == null)
                                                        mergedAdjacentTris[0] = testedVertexIndex == 2 ? tri1
                                                                : tri2;
                                                    else if (mergedAdjacentTris[1] == null)
                                                        mergedAdjacentTris[1] = testedVertexIndex == 2 ? tri1
                                                                : tri2;
                                                    else
                                                        System.err.println(
                                                                "there are too much vertices not on the hypotenuse");
                                                }
                                            }
                                        }
                                    }
                                    if (!cornerVertexFound)
                                        System.err.println("missing corner");
                                }
                            }
                            // keeps the orientation of the previous triangles
                            Arrays.fill(mergedAdjacentTrisVerticesIndices, -1);
                            final TriangleInfo tri1 = mergedAdjacentTris[0];
                            final TriangleInfo tri2 = mergedAdjacentTris[1];
                            // FIXME the detection of corners is broken
                            if (tri1 != null && tri2 != null) {
                                final Vector2[] tri1TextureCoords = tri1.getTextureCoords();
                                final Vector2[] tri2TextureCoords = tri2.getTextureCoords();
                                testedAdjacentTrisTextureCoords[0] = tri1TextureCoords[tri1.sideIndexOfHypotenuse];
                                testedAdjacentTrisTextureCoords[1] = tri2TextureCoords[tri2.sideIndexOfHypotenuse];
                                testedAdjacentTrisTextureCoords[2] = tri1TextureCoords[(tri1.sideIndexOfHypotenuse
                                        + 2) % 3];
                                testedAdjacentTrisTextureCoords[3] = tri2TextureCoords[(tri2.sideIndexOfHypotenuse
                                        + 2) % 3];
                                // operates on the vertices not on the hypotenuse first
                                for (int localIndex = 0; localIndex < 4; localIndex++) {
                                    if (mergedAdjacentTrisTextureCoords[localIndex]
                                            .equals(testedAdjacentTrisTextureCoords[0])) {
                                        if (mergedAdjacentTrisVerticesIndices[0] == -1)
                                            mergedAdjacentTrisVerticesIndices[0] = localIndex;
                                        else if (mergedAdjacentTrisVerticesIndices[4] == -1)
                                            mergedAdjacentTrisVerticesIndices[4] = localIndex;
                                        else
                                            System.err.println(
                                                    "there are too much vertices with the same texture coordinates");
                                    } else if (mergedAdjacentTrisTextureCoords[localIndex]
                                            .equals(testedAdjacentTrisTextureCoords[1])) {
                                        if (mergedAdjacentTrisVerticesIndices[1] == -1)
                                            mergedAdjacentTrisVerticesIndices[1] = localIndex;
                                        else if (mergedAdjacentTrisVerticesIndices[3] == -1)
                                            mergedAdjacentTrisVerticesIndices[3] = localIndex;
                                        else
                                            System.err.println(
                                                    "there are too much vertices with the same texture coordinates");
                                    }
                                    if (mergedAdjacentTrisTextureCoords[localIndex]
                                            .equals(testedAdjacentTrisTextureCoords[2]))
                                        mergedAdjacentTrisVerticesIndices[2] = localIndex;
                                    else if (mergedAdjacentTrisTextureCoords[localIndex]
                                            .equals(testedAdjacentTrisTextureCoords[3]))
                                        mergedAdjacentTrisVerticesIndices[5] = localIndex;
                                }
                                // updates texture coordinates equal to 1
                                final double u = columnCount;
                                final double v = rowCount;
                                for (int localIndex = 0; localIndex < 4; localIndex++) {
                                    if (mergedAdjacentTrisTextureCoords[localIndex].getX() == 1)
                                        mergedAdjacentTrisTextureCoords[localIndex].setX(u);
                                    if (mergedAdjacentTrisTextureCoords[localIndex].getY() == 1)
                                        mergedAdjacentTrisTextureCoords[localIndex].setY(v);
                                }
                                // stores the couple of old pairs and the new pairs (with some information) in order to remove the former and to add the latter
                                final NextQuadInfo quadInfo = new NextQuadInfo(mergedAdjacentTrisVertices,
                                        mergedAdjacentTrisTextureCoords, mergedAdjacentTrisVerticesIndices);
                                previousAdjacentTrisAndNextQuadInfosMaps.put(adjacentTrisArray, quadInfo);
                            }
                        }
                    }
                }
                if (previousAdjacentTrisAndNextQuadInfosMaps.isEmpty()) {
                    mapOfPreviousAndNextAdjacentTrisMaps.remove(plane);
                }
            }
            System.out.println("[6] Number of planes: " + mapOfPreviousAndNextAdjacentTrisMaps.size());
            mapOfPreviousAndNextAdjacentTrisMaps.entrySet().stream()
                .map((final Map.Entry<Plane, HashMap<TriangleInfo[][][], NextQuadInfo>> entry) -> entry.getKey() + "=" + entry.getValue().keySet().stream().flatMap(Arrays::stream).flatMap(Arrays::stream).filter(Objects::nonNull).flatMap(Arrays::stream).filter(Objects::nonNull).collect(Collectors.toList()))
                .forEach(System.out::println);
            System.out.println("[6] Number of triangles: " + mapOfPreviousAndNextAdjacentTrisMaps.values().stream().map(Map::keySet).flatMap(Collection::stream).flatMap(Arrays::stream).flatMap(Arrays::stream).filter(Objects::nonNull).flatMap(Arrays::stream).filter(Objects::nonNull).count());
            // seventh step: removes the triangles which are no more in the geometry of the mesh
            final ArrayList<Integer> verticesIndicesToRemove = new ArrayList<>();
            // for each plane
            for (Map.Entry<Plane, HashMap<TriangleInfo[][][], NextQuadInfo>> mapOfPreviousAndNextAdjacentTrisMapsEntry : mapOfPreviousAndNextAdjacentTrisMaps
                    .entrySet()) {
                // for each couple of old pairs and the new pairs (with some information)
                for (Map.Entry<TriangleInfo[][][], NextQuadInfo> previousAdjacentTrisAndNextQuadInfosEntry : mapOfPreviousAndNextAdjacentTrisMapsEntry
                        .getValue().entrySet()) {
                    final TriangleInfo[][][] previousAdjacentTrisArray = previousAdjacentTrisAndNextQuadInfosEntry
                            .getKey();
                    for (int rowIndex = 0; rowIndex < previousAdjacentTrisArray.length; rowIndex++)
                        for (int columnIndex = 0; columnIndex < previousAdjacentTrisArray[rowIndex].length; columnIndex++) {
                            // retrieves the vertices
                            final TriangleInfo tri1 = previousAdjacentTrisArray[rowIndex][columnIndex][0];
                            final TriangleInfo tri2 = previousAdjacentTrisArray[rowIndex][columnIndex][1];
                            final Vector3[] tri1Vertices = tri1.getVertices();
                            final Vector3[] tri2Vertices = tri2.getVertices();
                            final int[] tri1Indices = tri1.getIndices();
                            final int[] tri2Indices = tri2.getIndices();
                            // does not keep these vertices, mark them as removable
                            for (int triVertexIndex = 0; triVertexIndex < tri1Vertices.length; triVertexIndex++)
                                verticesIndicesToRemove.add(Integer.valueOf(tri1Indices[triVertexIndex]));
                            for (int triVertexIndex = 0; triVertexIndex < tri2Vertices.length; triVertexIndex++)
                                verticesIndicesToRemove.add(Integer.valueOf(tri2Indices[triVertexIndex]));
                        }
                }
            }
            // computes the count of added vertices
            int addedVerticesCount = 0;
            for (HashMap<TriangleInfo[][][], NextQuadInfo> previousAndNextAdjacentTrisMap : mapOfPreviousAndNextAdjacentTrisMaps
                    .values()) {
                // there are (obviously) two triangles by quad and three vertices by triangle
                addedVerticesCount += previousAndNextAdjacentTrisMap.size() * 6;
            }
            // computes the next vertex count
            final int nextVertexCount = meshData.getVertexCount() - verticesIndicesToRemove.size() + addedVerticesCount;
            // creates the next vertex buffer
            final FloatBuffer nextVertexBuffer = FloatBuffer.allocate(nextVertexCount * 3);
            // does not copy the vertices marked as removable into the next vertex buffer, copies the others
            for (int vertexIndex = 0; vertexIndex < meshData.getVertexCount(); vertexIndex++)
                if (!verticesIndicesToRemove.contains(Integer.valueOf(vertexIndex))) {
                    final int vertexCoordinateIndex = vertexIndex * 3;
                    final float x = meshData.getVertexBuffer().get(vertexCoordinateIndex);
                    final float y = meshData.getVertexBuffer().get(vertexCoordinateIndex + 1);
                    final float z = meshData.getVertexBuffer().get(vertexCoordinateIndex + 2);
                    nextVertexBuffer.put(x).put(y).put(z);
                }
            // does not modify the position so that this vertex buffer is ready for the addition of the new vertices computes the next texture coordinate count
            final int nextTextureCoordsCount = nextVertexCount;
            // creates the next texture buffer (2D)
            final FloatBuffer nextTextureBuffer = FloatBuffer.allocate(nextTextureCoordsCount * 2);
            // does not copy the texture coordinates of vertices marked as
            // removable into the next vertex buffer, copies the others
            for (int vertexIndex = 0; vertexIndex < meshData.getVertexCount(); vertexIndex++)
                if (!verticesIndicesToRemove.contains(Integer.valueOf(vertexIndex))) {
                    final int textureCoordinateIndex = vertexIndex * 2;
                    final float fu = meshData.getVertexBuffer().get(textureCoordinateIndex);
                    final float fv = meshData.getVertexBuffer().get(textureCoordinateIndex + 1);
                    nextTextureBuffer.put(fu).put(fv);
                }
            // does not modify the position so that this texture buffer is ready for the addition of the new texture coordinates
            // eighth step: adds the new triangles into the geometry of the mesh
            for (HashMap<TriangleInfo[][][], NextQuadInfo> previousAndNextAdjacentTrisMap : mapOfPreviousAndNextAdjacentTrisMaps
                    .values())
                for (NextQuadInfo nextQuadInfo : previousAndNextAdjacentTrisMap.values()) {
                    // uses the six indices to know which vertices to use in order to build the two triangles
                    for (int indexIndex = 0; indexIndex < nextQuadInfo.indices.length; indexIndex++) {
                        final int vertexIndex = nextQuadInfo.indices[indexIndex];
                        final Vector3 vertex = nextQuadInfo.vertices[vertexIndex];
                        final Vector2 texCoord = nextQuadInfo.textureCoords[vertexIndex];
                        final float x = vertex.getXf();
                        final float y = vertex.getYf();
                        final float z = vertex.getZf();
                        final float fu = texCoord.getXf();
                        final float fv = texCoord.getYf();
                        nextVertexBuffer.put(x).put(y).put(z);
                        nextTextureBuffer.put(fu).put(fv);
                    }
                }
            // finally, rewinds the new vertex buffer and sets it
            nextVertexBuffer.rewind();
            meshData.setVertexBuffer(nextVertexBuffer);
            // does the same for texture coordinates
            nextTextureBuffer.rewind();
            meshData.setTextureCoords(new FloatBufferData(nextTextureBuffer, 2), 0);
            // if the supplied geometry was indexed
            if (previousGeometryWasIndexed) {
                // converts the new geometry into an indexed geometry uses all conditions with GeometryTool
                final EnumSet<MatchCondition> conditions = EnumSet.of(MatchCondition.UVs, MatchCondition.Normal,
                        MatchCondition.Color);
                // reduces the geometry to avoid duplication of vertices
                new GeometryTool(true).minimizeVerts(mesh, conditions);
            }
        }
    }

    /**
     * Gets the texture coordinates of the primitive.
     * 
     * @param primitiveIndex
     *            the primitive index
     * @param section
     *            the section
     * @param textureIndex
     *            the texture index
     * @param store
     *            the store
     * 
     * @return the texture coordinates of the primitive
     */
    public static Vector2[] getPrimitiveTextureCoords(final MeshData meshData, final int primitiveIndex,
            final int section, final int textureIndex, final Vector2[] store) {
        Vector2[] result = null;
        if (meshData.getTextureBuffer(textureIndex) != null) {
            final int count = meshData.getPrimitiveCount(section);
            if (primitiveIndex >= count || primitiveIndex < 0)
                throw new IndexOutOfBoundsException(
                        "Invalid primitiveIndex '" + primitiveIndex + "'.  Count is " + count);
            final IndexMode mode = meshData.getIndexMode(section);
            final int rSize = mode.getVertexCount();
            result = store;
            if (result == null || result.length < rSize)
                result = new Vector2[rSize];
            for (int i = 0; i < rSize; i++) {
                if (result[i] == null)
                    result[i] = new Vector2();
                if (meshData.getIndexBuffer() != null) {// indexed geometry
                    BufferUtils.populateFromBuffer(result[i], meshData.getTextureBuffer(textureIndex),
                            meshData.getIndices().get(meshData.getVertexIndex(primitiveIndex, i, section)));
                } else {// non-indexed geometry
                    BufferUtils.populateFromBuffer(result[i], meshData.getTextureBuffer(textureIndex),
                            meshData.getVertexIndex(primitiveIndex, i, section));
                }
            }
        }
        return result;
    }

    /**
     * Computes a list of arrays of adjacent triangles which could be merged to
     * make bigger rectangles
     * 
     * @param adjacentTrisArray
     *            2D arrays containing adjacent triangles
     * @return map of 2D arrays of adjacent mergeable triangles
     */
    static Map<Vector2i, TriangleInfo[][][]> computeAdjacentMergeableTrisArraysMap(final TriangleInfo[][][] adjacentTrisArray) {
        return new ArrayHelper().computeFullArraysFromNonFullArray(adjacentTrisArray);
    }
}
