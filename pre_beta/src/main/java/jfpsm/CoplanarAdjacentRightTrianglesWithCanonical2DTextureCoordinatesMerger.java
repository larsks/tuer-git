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
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import jfpsm.ArrayHelper.Vector2i;

import com.ardor3d.image.Texture.WrapMode;
import com.ardor3d.math.Plane;
import com.ardor3d.math.Triangle;
import com.ardor3d.math.Vector2;
import com.ardor3d.math.Vector3;
import com.ardor3d.math.type.ReadOnlyVector3;
import com.ardor3d.renderer.IndexMode;
import com.ardor3d.renderer.state.TextureState;
import com.ardor3d.renderer.state.RenderState.StateType;
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
        
        private final int rightAngleVertexIndex;
        
        private final Vector3[] vertices;
        
        private final Vector2[] textureCoords;
        
        private final Vector3[] normals;
        
        private final boolean hasCanonicalTextureCoordinates;
        
        private final ReadOnlyVector3 normalizedNormal;
        
        private final ReadOnlyVector3 center;

        TriangleInfo(final int primitiveIndex, final int sectionIndex, final MeshData meshData) {
            super();
            this.primitiveIndex = primitiveIndex;
            this.sectionIndex = sectionIndex;
            if (meshData == null) {
                this.vertices = null;
                this.textureCoords = null;
                this.normals = null;
            } else {
                this.vertices = meshData.getPrimitiveVertices(this.primitiveIndex, this.sectionIndex, null);
                if (meshData.getNumberOfUnits() == 0 || meshData.getTextureCoords().isEmpty()) {
                    this.textureCoords = null;
                } else {
                    this.textureCoords = getPrimitiveTextureCoords(meshData, this.primitiveIndex, this.sectionIndex, 0, null);
                }
                this.normals = getPrimitiveNormals(meshData, this.primitiveIndex, this.sectionIndex, null);
            }
            this.rightAngleVertexIndex = computeRightAngleVertexIndex();
            this.hasCanonicalTextureCoordinates = computeHasCanonicalTextureCoordinates();
            final Map.Entry<ReadOnlyVector3, ReadOnlyVector3> normalAndCenterEntry = computeNormalizedNormalAndCenter();
            this.normalizedNormal = normalAndCenterEntry.getKey();
            this.center = normalAndCenterEntry.getValue();
        }
        
        TriangleInfo(final int sectionIndex, final Vector3[] vertices, final Vector2[] textureCoords, final Vector3[] normals) {
            super();
            this.primitiveIndex = -1;
            this.sectionIndex = sectionIndex;
            this.vertices = vertices;
            this.textureCoords = textureCoords;
            this.normals = normals;
            this.rightAngleVertexIndex = computeRightAngleVertexIndex();
            this.hasCanonicalTextureCoordinates = computeHasCanonicalTextureCoordinates();
            final Map.Entry<ReadOnlyVector3, ReadOnlyVector3> normalAndCenterEntry = computeNormalizedNormalAndCenter();
            this.normalizedNormal = normalAndCenterEntry.getKey();
            this.center = normalAndCenterEntry.getValue();
        }
        
        private final int computeRightAngleVertexIndex() {
            return this.vertices == null || this.vertices.length < 3 ? -1 : IntStream.range(0, 3)
                    // computes the dot product of two vectors to check whether there's a right angle at their common vertex
                    .filter((final int triIndex) -> this.vertices[triIndex].subtract(this.vertices[(triIndex + 1) % 3], null).dot(this.vertices[triIndex].subtract(this.vertices[(triIndex + 2) % 3], null)) == 0.0)
                    .findFirst()
                    .orElse(-1);
        }
        
        private final boolean computeHasCanonicalTextureCoordinates() {
            return textureCoords != null && Arrays.stream(textureCoords)
                    .flatMapToDouble((final Vector2 textureCoord) -> DoubleStream.of(textureCoord.getX(), textureCoord.getY()))
                    .allMatch((final double uv) -> uv == 0 || uv == 1) && Arrays.stream(textureCoords).distinct().count() == 3;
        }
        
        private final Map.Entry<ReadOnlyVector3, ReadOnlyVector3> computeNormalizedNormalAndCenter() {
            final Map.Entry<ReadOnlyVector3, ReadOnlyVector3> result;
            if (this.vertices == null || this.vertices.length < 3) {
                result = new AbstractMap.SimpleImmutableEntry<>(null, null);
            } else {
                final Triangle tmpTriangle = new Triangle(this.vertices[0], this.vertices[1], this.vertices[2]);
                // computes its normal, normalizes it in order to avoid creating useless planes for triangles of different sizes
                result = new AbstractMap.SimpleImmutableEntry<>(new Vector3(tmpTriangle.getNormal()).normalizeLocal(), tmpTriangle.getCenter());
            }
            return result;
        }
        
        boolean isRightAngled() {
            return this.rightAngleVertexIndex != -1;
        }
        
        boolean hasCanonicalTextureCoordinates() {
            return hasCanonicalTextureCoordinates;
        }
        
        Vector3[] getVertices() {
            return vertices;
        }
        
        Vector2[] getTextureCoords() {
            return textureCoords;
        }
        
        Vector3[] getNormals() {
            return normals;
        }
        
        ReadOnlyVector3 getNormalizedNormal() {
            return normalizedNormal;
        }
        
        ReadOnlyVector3 getCenter() {
            return center;
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
            // first step: separates right triangles with canonical 2D texture coordinates from the others, loops on all sections of the mesh data
            final List<TriangleInfo> triangleInfoList = IntStream.range(0, meshData.getSectionCount())
                // loops on all triangles of each section
                .mapToObj((final int sectionIndex) -> IntStream.range(0, meshData.getPrimitiveCount(sectionIndex))
                .mapToObj((final int trianglePrimitiveIndex) -> new TriangleInfo(trianglePrimitiveIndex, sectionIndex, meshData)))
                .flatMap(Stream::sequential)
                .collect(Collectors.toList());
            triangleInfoList.forEach(System.out::println);
            System.out.println("[1.0] Number of triangles: " + triangleInfoList.size());
            final List<TriangleInfo> trianglesWithCanonical2DTextureCoordinatesInfos = triangleInfoList.stream()
                    // checks whether its texture coordinates are canonical
                    .filter(TriangleInfo::hasCanonicalTextureCoordinates)
                    .collect(Collectors.toList());
            trianglesWithCanonical2DTextureCoordinatesInfos.forEach(System.out::println);
            System.out.println("[1.1] Number of triangles: " + trianglesWithCanonical2DTextureCoordinatesInfos.size());
            final List<TriangleInfo> rightTrianglesWithCanonical2DTextureCoordinatesInfos = trianglesWithCanonical2DTextureCoordinatesInfos.stream().filter(TriangleInfo::isRightAngled).collect(Collectors.toList());
            rightTrianglesWithCanonical2DTextureCoordinatesInfos.forEach(System.out::println);
            System.out.println("[1.2] Number of triangles: " + rightTrianglesWithCanonical2DTextureCoordinatesInfos.size());
            // second step: sorts the triangles of the former set by planes (4D: normal + distance to plane)
            Map<Plane, List<TriangleInfo>> mapOfTrianglesByPlanes = rightTrianglesWithCanonical2DTextureCoordinatesInfos.stream()
                    .map((final TriangleInfo info) -> {
                // uses its normal, normalizes it in order to avoid creating useless planes for triangles of different sizes
                final ReadOnlyVector3 triangleNormal = info.getNormalizedNormal();
                // computes its distance to plane d=dot(normal,vertex)
                final double distanceToPlane = triangleNormal.dot(info.getCenter());
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
                entry.getValue().stream()
                    .map(CoplanarAdjacentRightTrianglesWithCanonical2DTextureCoordinatesMerger::computeAdjacentMergeableTrisArraysMap)
                    .map(Map::values)
                    .flatMap(Collection::stream)
                    .filter((final TriangleInfo[][][] adjacentTriArray) -> adjacentTriArray.length >= 1 && adjacentTriArray[0].length >= 1 && (adjacentTriArray.length >= 2 || adjacentTriArray[0].length >= 2))
                    .collect(Collectors.toList())))
                .collect(Collectors.toMap(AbstractMap.SimpleImmutableEntry::getKey, AbstractMap.SimpleImmutableEntry::getValue));
            System.out.println("[5.1] Number of planes: " + mapOfMergeableTris.size());
            mapOfMergeableTris.entrySet().stream()
                .map((final Map.Entry<Plane, List<TriangleInfo[][][]>> entry) -> entry.getKey() + "=" + entry.getValue().stream().flatMap(Arrays::stream).flatMap(Arrays::stream).filter(Objects::nonNull).flatMap(Arrays::stream).filter(Objects::nonNull).collect(Collectors.toList()))
                .forEach(System.out::println);
            System.out.println("[5.1] Number of triangles: " + mapOfMergeableTris.values().stream().flatMap(List::stream).flatMap(Arrays::stream).flatMap(Arrays::stream).filter(Objects::nonNull).flatMap(Arrays::stream).filter(Objects::nonNull).count());
            // sixth step: creates these bigger rectangles with texture coordinates greater than 1 in order to use texture repeat
            final Map<Plane, List<Map.Entry<TriangleInfo[][][], TriangleInfo[]>>> mergedTriPairListMap = mapOfMergeableTris.entrySet().stream().map((final Map.Entry<Plane, List<TriangleInfo[][][]>> entry) -> 
                new AbstractMap.SimpleImmutableEntry<>(entry.getKey(), entry.getValue().stream()
                    .map((final TriangleInfo[][][] adjacentTriArray) -> {
                        // loops on 2 or 4 triangle pairs in the corners
                        final List<int[]> mergeCandidateInfoList = Stream.of(new AbstractMap.SimpleImmutableEntry<>(Integer.valueOf(0), Integer.valueOf(0)), 
                                  new AbstractMap.SimpleImmutableEntry<>(Integer.valueOf(adjacentTriArray.length - 1), Integer.valueOf(0)), 
                                  new AbstractMap.SimpleImmutableEntry<>(Integer.valueOf(adjacentTriArray.length - 1), Integer.valueOf(adjacentTriArray[0].length - 1)),
                                  new AbstractMap.SimpleImmutableEntry<>(Integer.valueOf(0), Integer.valueOf(adjacentTriArray[0].length - 1)))
                              .distinct()
                              .map((final Map.Entry<Integer, Integer> cornerCoordinateEntry) -> {
                                  final int cornerX = cornerCoordinateEntry.getKey().intValue();
                                  final int cornerY = cornerCoordinateEntry.getValue().intValue();
                                  final Vector3 cornerFirstRightAngleVertex = adjacentTriArray[cornerX][cornerY][0].getVertices()[adjacentTriArray[cornerX][cornerY][0].rightAngleVertexIndex];
                                  final Vector3 cornerSecondRightAngleVertex = adjacentTriArray[cornerX][cornerY][1].getVertices()[adjacentTriArray[cornerX][cornerY][1].rightAngleVertexIndex];
                                  final Vector3 cornerFirstHypotenuseVertex = adjacentTriArray[cornerX][cornerY][0].getVertices()[(adjacentTriArray[cornerX][cornerY][0].rightAngleVertexIndex + 1) % 3];
                                  final Vector3 cornerSecondHypotenuseVertex = adjacentTriArray[cornerX][cornerY][1].getVertices()[(adjacentTriArray[cornerX][cornerY][1].rightAngleVertexIndex + 1) % 3];
                                  // loops on 1 or 2 adjacent triangle pairs
                                  return Stream.of(new AbstractMap.SimpleImmutableEntry<>(Integer.valueOf(cornerX - 1), Integer.valueOf(cornerY)),
                                            new AbstractMap.SimpleImmutableEntry<>(Integer.valueOf(cornerX), Integer.valueOf(cornerY + 1)),
                                            new AbstractMap.SimpleImmutableEntry<>(Integer.valueOf(cornerX), Integer.valueOf(cornerY - 1)),
                                            new AbstractMap.SimpleImmutableEntry<>(Integer.valueOf(cornerX + 1), Integer.valueOf(cornerY)))
                                        .filter((final Map.Entry<Integer, Integer> neighbourCoordinateEntry) -> 
                                            0 <= neighbourCoordinateEntry.getKey().intValue() && neighbourCoordinateEntry.getKey().intValue() <= adjacentTriArray.length - 1 &&
                                            0 <= neighbourCoordinateEntry.getValue().intValue() && neighbourCoordinateEntry.getValue().intValue() <= adjacentTriArray[0].length - 1)
                                        .map((final Map.Entry<Integer, Integer> neighbourCoordinateEntry) -> {
                                            final int neighbourX = neighbourCoordinateEntry.getKey().intValue();
                                            final int neighbourY = neighbourCoordinateEntry.getValue().intValue();
                                            final Vector3 neighbourFirstRightAngleVertex = adjacentTriArray[neighbourX][neighbourY][0].getVertices()[adjacentTriArray[neighbourX][neighbourY][0].rightAngleVertexIndex];
                                            final Vector3 neighbourSecondRightAngleVertex = adjacentTriArray[neighbourX][neighbourY][1].getVertices()[adjacentTriArray[neighbourX][neighbourY][1].rightAngleVertexIndex];
                                            final Vector3 neighbourFirstHypotenuseVertex = adjacentTriArray[neighbourX][neighbourY][0].getVertices()[(adjacentTriArray[neighbourX][neighbourY][0].rightAngleVertexIndex + 1) % 3];
                                            final Vector3 neighbourSecondHypotenuseVertex = adjacentTriArray[neighbourX][neighbourY][1].getVertices()[(adjacentTriArray[neighbourX][neighbourY][1].rightAngleVertexIndex + 1) % 3];
                                            final Stream.Builder<int[]> candidateInfoStreamBuilder = Stream.builder();
                                            // looks for the vertices of the (next) merged rectangle
                                            if (Stream.of(neighbourFirstHypotenuseVertex, neighbourSecondHypotenuseVertex).noneMatch(cornerFirstRightAngleVertex::equals)) {
                                                candidateInfoStreamBuilder.add(new int[] {cornerX, cornerY, 0, adjacentTriArray[cornerX][cornerY][0].rightAngleVertexIndex});
                                            }
                                            if (Stream.of(neighbourFirstHypotenuseVertex, neighbourSecondHypotenuseVertex).noneMatch(cornerSecondRightAngleVertex::equals)) {
                                                candidateInfoStreamBuilder.add(new int[] {cornerX, cornerY, 1, adjacentTriArray[cornerX][cornerY][1].rightAngleVertexIndex});
                                            }
                                            if (Stream.of(neighbourFirstRightAngleVertex, neighbourSecondRightAngleVertex).noneMatch(cornerFirstHypotenuseVertex::equals)) {
                                                candidateInfoStreamBuilder.add(new int[] {cornerX, cornerY, 0, (adjacentTriArray[cornerX][cornerY][0].rightAngleVertexIndex + 1) % 3});
                                            }
                                            if (Stream.of(neighbourFirstRightAngleVertex, neighbourSecondRightAngleVertex).noneMatch(cornerSecondHypotenuseVertex::equals)) {
                                                candidateInfoStreamBuilder.add(new int[] {cornerX, cornerY, 1, (adjacentTriArray[cornerX][cornerY][1].rightAngleVertexIndex + 1) % 3});
                                            }
                                            return candidateInfoStreamBuilder.build();
                                        })
                                        .flatMap(Stream::sequential);
                              })
                              .flatMap(Stream::sequential)
                              .collect(Collectors.toList());
                        // looks for the vertices at the right angles of the triangles composing the merged rectangle
                        final int[] firstRightAngleMergeCandidateInfo = mergeCandidateInfoList.stream()
                                .filter((final int[] mergeCandidateInfo) -> adjacentTriArray[mergeCandidateInfo[0]][mergeCandidateInfo[1]][mergeCandidateInfo[2]].rightAngleVertexIndex == mergeCandidateInfo[3])
                                .findFirst()
                                .get();
                        mergeCandidateInfoList.remove(firstRightAngleMergeCandidateInfo);
                        final int[] secondRightAngleMergeCandidateInfo = mergeCandidateInfoList.stream()
                                .filter((final int[] mergeCandidateInfo) -> adjacentTriArray[mergeCandidateInfo[0]][mergeCandidateInfo[1]][mergeCandidateInfo[2]].rightAngleVertexIndex == mergeCandidateInfo[3])
                                .findFirst()
                                .get();
                        mergeCandidateInfoList.remove(secondRightAngleMergeCandidateInfo);
                        final int[] firstHypotenuseMergeCandidateInfo = mergeCandidateInfoList.remove(0);
                        final int[] secondHypotenuseMergeCandidateInfo = mergeCandidateInfoList.remove(0);
                        final TriangleInfo cornerFirstRightAngleTri = adjacentTriArray[firstRightAngleMergeCandidateInfo[0]][firstRightAngleMergeCandidateInfo[1]][firstRightAngleMergeCandidateInfo[2]];
                        final Vector3 cornerFirstRightAngleVertex = cornerFirstRightAngleTri.getVertices()[firstRightAngleMergeCandidateInfo[3]];
                        final TriangleInfo cornerSecondRightAngleTri = adjacentTriArray[secondRightAngleMergeCandidateInfo[0]][secondRightAngleMergeCandidateInfo[1]][secondRightAngleMergeCandidateInfo[2]];
                        final Vector3 cornerSecondRightAngleVertex = cornerSecondRightAngleTri.getVertices()[secondRightAngleMergeCandidateInfo[3]];
                        final Vector3 cornerFirstHypotenuseVertex = adjacentTriArray[firstHypotenuseMergeCandidateInfo[0]][firstHypotenuseMergeCandidateInfo[1]][firstHypotenuseMergeCandidateInfo[2]].getVertices()[firstHypotenuseMergeCandidateInfo[3]];
                        final Vector3 cornerSecondHypotenuseVertex = adjacentTriArray[secondHypotenuseMergeCandidateInfo[0]][secondHypotenuseMergeCandidateInfo[1]][secondHypotenuseMergeCandidateInfo[2]].getVertices()[secondHypotenuseMergeCandidateInfo[3]];
                        final Vector3 normalizedNormal0 = new Vector3(new Triangle(cornerFirstRightAngleVertex, cornerFirstHypotenuseVertex, cornerSecondHypotenuseVertex).getNormal()).normalizeLocal();
                        final Vector3 normalizedNormal1 = new Vector3(new Triangle(cornerSecondRightAngleVertex, cornerFirstHypotenuseVertex, cornerSecondHypotenuseVertex).getNormal()).normalizeLocal();
                        final Vector3[] tri0Vertices;
                        final Vector2[] tri0TexCoords;
                        final Vector3[] tri0Normals;
                        final Vector3[] tri1Vertices;
                        final Vector2[] tri1TexCoords;
                        final Vector3[] tri1Normals;
                        if (cornerFirstRightAngleTri.getNormalizedNormal().equals(normalizedNormal0)) {
                            tri0Vertices = new Vector3[] {cornerFirstRightAngleVertex, cornerFirstHypotenuseVertex, cornerSecondHypotenuseVertex};
                            tri0TexCoords = new Vector2[] {adjacentTriArray[firstRightAngleMergeCandidateInfo[0]][firstRightAngleMergeCandidateInfo[1]][firstRightAngleMergeCandidateInfo[2]].getTextureCoords()[firstRightAngleMergeCandidateInfo[3]],
                                                           adjacentTriArray[firstHypotenuseMergeCandidateInfo[0]][firstHypotenuseMergeCandidateInfo[1]][firstHypotenuseMergeCandidateInfo[2]].getTextureCoords()[firstHypotenuseMergeCandidateInfo[3]],
                                                           adjacentTriArray[secondHypotenuseMergeCandidateInfo[0]][secondHypotenuseMergeCandidateInfo[1]][secondHypotenuseMergeCandidateInfo[2]].getTextureCoords()[secondHypotenuseMergeCandidateInfo[3]]};
                            if (meshData.getNormalBuffer() == null) {
                                tri0Normals = null;
                            } else {
                                tri0Normals = new Vector3[] {adjacentTriArray[firstRightAngleMergeCandidateInfo[0]][firstRightAngleMergeCandidateInfo[1]][firstRightAngleMergeCandidateInfo[2]].getNormals()[firstRightAngleMergeCandidateInfo[3]],
                                                             adjacentTriArray[firstHypotenuseMergeCandidateInfo[0]][firstHypotenuseMergeCandidateInfo[1]][firstHypotenuseMergeCandidateInfo[2]].getNormals()[firstHypotenuseMergeCandidateInfo[3]],
                                                             adjacentTriArray[secondHypotenuseMergeCandidateInfo[0]][secondHypotenuseMergeCandidateInfo[1]][secondHypotenuseMergeCandidateInfo[2]].getNormals()[secondHypotenuseMergeCandidateInfo[3]]};
                            }
                        } else {
                            tri0Vertices = new Vector3[] {cornerFirstRightAngleVertex, cornerSecondHypotenuseVertex, cornerFirstHypotenuseVertex};
                            tri0TexCoords = new Vector2[] {adjacentTriArray[firstRightAngleMergeCandidateInfo[0]][firstRightAngleMergeCandidateInfo[1]][firstRightAngleMergeCandidateInfo[2]].getTextureCoords()[firstRightAngleMergeCandidateInfo[3]],
                                                           adjacentTriArray[secondHypotenuseMergeCandidateInfo[0]][secondHypotenuseMergeCandidateInfo[1]][secondHypotenuseMergeCandidateInfo[2]].getTextureCoords()[secondHypotenuseMergeCandidateInfo[3]],
                                                           adjacentTriArray[firstHypotenuseMergeCandidateInfo[0]][firstHypotenuseMergeCandidateInfo[1]][firstHypotenuseMergeCandidateInfo[2]].getTextureCoords()[firstHypotenuseMergeCandidateInfo[3]]};
                            if (meshData.getNormalBuffer() == null) {
                                tri0Normals = null;
                            } else {
                                tri0Normals = new Vector3[] {adjacentTriArray[firstRightAngleMergeCandidateInfo[0]][firstRightAngleMergeCandidateInfo[1]][firstRightAngleMergeCandidateInfo[2]].getNormals()[firstRightAngleMergeCandidateInfo[3]],
                                                             adjacentTriArray[secondHypotenuseMergeCandidateInfo[0]][secondHypotenuseMergeCandidateInfo[1]][secondHypotenuseMergeCandidateInfo[2]].getNormals()[secondHypotenuseMergeCandidateInfo[3]],
                                                             adjacentTriArray[firstHypotenuseMergeCandidateInfo[0]][firstHypotenuseMergeCandidateInfo[1]][firstHypotenuseMergeCandidateInfo[2]].getNormals()[firstHypotenuseMergeCandidateInfo[3]]};
                            }
                        }
                        if (cornerSecondRightAngleTri.getNormalizedNormal().equals(normalizedNormal1)) {
                            tri1Vertices = new Vector3[] {cornerSecondRightAngleVertex, cornerFirstHypotenuseVertex, cornerSecondHypotenuseVertex};
                            tri1TexCoords = new Vector2[] {adjacentTriArray[secondRightAngleMergeCandidateInfo[0]][secondRightAngleMergeCandidateInfo[1]][secondRightAngleMergeCandidateInfo[2]].getTextureCoords()[secondRightAngleMergeCandidateInfo[3]],
                                                           adjacentTriArray[firstHypotenuseMergeCandidateInfo[0]][firstHypotenuseMergeCandidateInfo[1]][firstHypotenuseMergeCandidateInfo[2]].getTextureCoords()[firstHypotenuseMergeCandidateInfo[3]],
                                                           adjacentTriArray[secondHypotenuseMergeCandidateInfo[0]][secondHypotenuseMergeCandidateInfo[1]][secondHypotenuseMergeCandidateInfo[2]].getTextureCoords()[secondHypotenuseMergeCandidateInfo[3]]};
                            if (meshData.getNormalBuffer() == null) {
                                tri1Normals = null;
                            } else {
                                tri1Normals = new Vector3[] {adjacentTriArray[secondRightAngleMergeCandidateInfo[0]][secondRightAngleMergeCandidateInfo[1]][secondRightAngleMergeCandidateInfo[2]].getNormals()[secondRightAngleMergeCandidateInfo[3]],
                                                             adjacentTriArray[firstHypotenuseMergeCandidateInfo[0]][firstHypotenuseMergeCandidateInfo[1]][firstHypotenuseMergeCandidateInfo[2]].getNormals()[firstHypotenuseMergeCandidateInfo[3]],
                                                             adjacentTriArray[secondHypotenuseMergeCandidateInfo[0]][secondHypotenuseMergeCandidateInfo[1]][secondHypotenuseMergeCandidateInfo[2]].getNormals()[secondHypotenuseMergeCandidateInfo[3]]};
                            }
                        } else {
                            tri1Vertices = new Vector3[] {cornerSecondRightAngleVertex, cornerSecondHypotenuseVertex, cornerFirstHypotenuseVertex};
                            tri1TexCoords = new Vector2[] {adjacentTriArray[secondRightAngleMergeCandidateInfo[0]][secondRightAngleMergeCandidateInfo[1]][secondRightAngleMergeCandidateInfo[2]].getTextureCoords()[secondRightAngleMergeCandidateInfo[3]],
                                                           adjacentTriArray[secondHypotenuseMergeCandidateInfo[0]][secondHypotenuseMergeCandidateInfo[1]][secondHypotenuseMergeCandidateInfo[2]].getTextureCoords()[secondHypotenuseMergeCandidateInfo[3]],
                                                           adjacentTriArray[firstHypotenuseMergeCandidateInfo[0]][firstHypotenuseMergeCandidateInfo[1]][firstHypotenuseMergeCandidateInfo[2]].getTextureCoords()[firstHypotenuseMergeCandidateInfo[3]]};
                            if (meshData.getNormalBuffer() == null) {
                                tri1Normals = null;
                            } else {
                                tri1Normals = new Vector3[] {adjacentTriArray[secondRightAngleMergeCandidateInfo[0]][secondRightAngleMergeCandidateInfo[1]][secondRightAngleMergeCandidateInfo[2]].getNormals()[secondRightAngleMergeCandidateInfo[3]],
                                                             adjacentTriArray[secondHypotenuseMergeCandidateInfo[0]][secondHypotenuseMergeCandidateInfo[1]][secondHypotenuseMergeCandidateInfo[2]].getNormals()[secondHypotenuseMergeCandidateInfo[3]],
                                                             adjacentTriArray[firstHypotenuseMergeCandidateInfo[0]][firstHypotenuseMergeCandidateInfo[1]][firstHypotenuseMergeCandidateInfo[2]].getNormals()[firstHypotenuseMergeCandidateInfo[3]]};
                            }
                        }
                        // performs some sanity checks
                        if (Stream.concat(Arrays.stream(tri0Vertices), Arrays.stream(tri1Vertices)).distinct().count() != 4) {
                            System.err.println("malformed vertices: " + Arrays.toString(tri0Vertices) + " " + Arrays.toString(tri1Vertices));
                        }
                        if (Stream.concat(Arrays.stream(tri0TexCoords), Arrays.stream(tri1TexCoords)).distinct().count() != 4) {
                            System.err.println("malformed texture coordinates: " + Arrays.toString(tri0TexCoords) + " " + Arrays.toString(tri1TexCoords));
                        }
                        if (!Arrays.equals(tri0Normals, tri1Normals)) {
                            System.err.println("inconsistent normals: " + tri0Normals + " " + tri1Normals);
                        }
                        // modifies the texture coordinates to repeat the textures
                        IntStream.range(0, 3)
                            .mapToObj((final int texCoordIndex) -> Stream.of(tri0TexCoords[texCoordIndex], tri1TexCoords[texCoordIndex]))
                            .flatMap(Stream::sequential)
                            .forEachOrdered((final Vector2 texCoord) -> {
                                if (texCoord.getX() == 1.0) {
                                    texCoord.setX(adjacentTriArray[0].length);
                                }
                                if (texCoord.getY() == 1.0) {
                                    texCoord.setY(adjacentTriArray.length);
                                }
                            });
                final Map.Entry<TriangleInfo[][][], TriangleInfo[]> mergeableToMergeEntry = new AbstractMap.SimpleImmutableEntry<>(adjacentTriArray, new TriangleInfo[] {new TriangleInfo(cornerFirstRightAngleTri.sectionIndex, tri0Vertices, tri0TexCoords, tri0Normals), new TriangleInfo(cornerSecondRightAngleTri.sectionIndex, tri1Vertices, tri1TexCoords, tri1Normals)});
                return mergeableToMergeEntry;
            }).filter(Objects::nonNull)
              .collect(Collectors.toList())))
                    .filter((final AbstractMap.SimpleImmutableEntry<Plane, List<Map.Entry<TriangleInfo[][][], TriangleInfo[]>>> entry) -> !entry.getValue().isEmpty())
                    .collect(Collectors.toMap(AbstractMap.SimpleImmutableEntry::getKey, AbstractMap.SimpleImmutableEntry::getValue));
            System.out.println("[6] Number of planes: " + mergedTriPairListMap.size());
            mergedTriPairListMap.entrySet().stream()
                .map((final Map.Entry<Plane, List<Map.Entry<TriangleInfo[][][], TriangleInfo[]>>> entry) -> entry.getKey() + "=" + entry.getValue().stream().map(Map.Entry::getValue).filter(Objects::nonNull).flatMap(Arrays::stream).collect(Collectors.toList()))
                .forEach(System.out::println);
            System.out.println("[6] Number of triangles: " + mergedTriPairListMap.values().stream().flatMap(List::stream).map(Map.Entry::getValue).filter(Objects::nonNull).flatMap(Arrays::stream).count());            
            if (!mergedTriPairListMap.isEmpty()) {
                // seventh step: rebuild the mesh data
                // builds a stream supplier providing the triangles to put into the next mesh data
                final Supplier<Stream<TriangleInfo>> nextTriangleInfoStreamSupplier = () -> Stream.concat(triangleInfoList.stream().filter((final TriangleInfo oldTri) -> mergedTriPairListMap.values().stream().flatMap(List::stream).map(Map.Entry::getKey).flatMap(Arrays::stream).flatMap(Arrays::stream).flatMap(Arrays::stream).noneMatch(oldTri::equals)), 
                                                                                                          mergedTriPairListMap.values().stream().flatMap(List::stream).map(Map.Entry::getValue).filter(Objects::nonNull).flatMap(Arrays::stream));
                // computes the next vertex count
                final int nextVertexCount = 3 * (int) nextTriangleInfoStreamSupplier.get().count();
                // creates the next vertex buffer
                final FloatBuffer nextVertexBuffer = FloatBuffer.allocate(nextVertexCount * 3);
                // creates the next texture buffer (2D)
                final FloatBuffer nextTextureBuffer = FloatBuffer.allocate(nextVertexCount * 2);
                // creates the next normal buffer
                final FloatBuffer nextNormalBuffer = meshData.getNormalBuffer() == null ? null : FloatBuffer.allocate(nextVertexCount * 3);
                nextTriangleInfoStreamSupplier.get().forEachOrdered((final TriangleInfo tri) -> {
                    Arrays.stream(tri.getVertices()).forEachOrdered((final Vector3 vertex) -> IntStream.range(0, 3).mapToDouble(vertex::getValue)
                        .forEachOrdered((final double vertexCoord) -> nextVertexBuffer.put((float) vertexCoord)));
                    Arrays.stream(tri.getTextureCoords()).forEachOrdered((final Vector2 textureCoords) -> IntStream.range(0, 2).mapToDouble(textureCoords::getValue)
                        .forEachOrdered((final double textureCoord) -> nextTextureBuffer.put((float) textureCoord)));
                    if (nextNormalBuffer != null) {
                        Arrays.stream(tri.getNormals()).forEachOrdered((final Vector3 normal) -> IntStream.range(0, 3).mapToDouble(normal::getValue)
                            .forEachOrdered((final double normalCoord) -> nextNormalBuffer.put((float) normalCoord)));
                    }
                });
                final MeshData nextMeshData = new MeshData();
                // finally, rewinds the new vertex buffer and sets it
                nextVertexBuffer.rewind();
                nextMeshData.setVertexBuffer(nextVertexBuffer);
                // does the same for texture coordinates
                nextTextureBuffer.rewind();
                nextMeshData.setTextureCoords(new FloatBufferData(nextTextureBuffer, 2), 0);
                if (nextNormalBuffer != null) {
                    nextNormalBuffer.rewind();
                    nextMeshData.setNormalBuffer(nextNormalBuffer);
                }
                // assigns the next mesh data to the mesh
                mesh.setMeshData(nextMeshData);
                // gets the texture state
                final TextureState textureState = (TextureState) mesh.getLocalRenderState(StateType.Texture);
                // sets the repeat wrap mode so that the renderer takes into account the texture coordinates beyond 1.0
                textureState.getTexture().setWrap(WrapMode.Repeat);
                // if the supplied geometry was indexed
                if (meshData.getIndexBuffer() != null) {
                    // converts the new geometry into an indexed geometry uses
                    // all conditions with GeometryTool
                    final EnumSet<MatchCondition> conditions = EnumSet.of(MatchCondition.UVs, MatchCondition.Normal/* , MatchCondition.Color */);
                    // reduces the geometry to avoid duplication of vertices
                    new GeometryTool(true).minimizeVerts(mesh, conditions);
                }
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
    public static Vector2[] getPrimitiveTextureCoords(final MeshData meshData, final int primitiveIndex, final int section, final int textureIndex, final Vector2[] store) {
        Vector2[] result = null;
        if (meshData.getTextureBuffer(textureIndex) != null) {
            final int count = meshData.getPrimitiveCount(section);
            if (primitiveIndex >= count || primitiveIndex < 0)
                throw new IndexOutOfBoundsException("Invalid primitiveIndex '" + primitiveIndex + "'.  Count is " + count);
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

    public static Vector3[] getPrimitiveNormals(final MeshData meshData, final int primitiveIndex, final int section, final Vector3[] store) {
        Vector3[] result = null;
        if (meshData.getNormalBuffer() != null) {
            final int count = meshData.getPrimitiveCount(section);
            if (primitiveIndex >= count || primitiveIndex < 0)
                throw new IndexOutOfBoundsException("Invalid primitiveIndex '" + primitiveIndex + "'.  Count is " + count);
            final IndexMode mode = meshData.getIndexMode(section);
            final int rSize = mode.getVertexCount();
            result = store;
            if (result == null || result.length < rSize)
                result = new Vector3[rSize];
            for (int i = 0; i < rSize; i++) {
                if (result[i] == null)
                    result[i] = new Vector3();
                if (meshData.getIndexBuffer() != null) {// indexed geometry
                    BufferUtils.populateFromBuffer(result[i], meshData.getNormalBuffer(),
                            meshData.getIndices().get(meshData.getVertexIndex(primitiveIndex, i, section)));
                } else {// non-indexed geometry
                    BufferUtils.populateFromBuffer(result[i], meshData.getNormalBuffer(),
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
