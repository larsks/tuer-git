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
                final Triangle tmpTriangle = new Triangle();
                // gets the 3 vertices of the triangle
                final Vector3[] triangleVertices = info.getVertices();
                // sets the vertices of the temporary triangle
                IntStream.range(0, triangleVertices.length)
                         .forEach((final int vertexInternalIndex) -> tmpTriangle.set(vertexInternalIndex, triangleVertices[vertexInternalIndex]));
                // computes its normal, normalizes it in order to avoid creating useless planes for triangles of different sizes
                final ReadOnlyVector3 triangleNormal = new Vector3(tmpTriangle.getNormal()).normalizeLocal();
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
                        final TriangleInfo srcTri0, srcTri1, srcTri2, srcTri3, srcTri4, srcTri5, srcTri6, srcTri7, srcTri8, srcTri9;
                // looks for the 6 vertices composing the triangle pair to create//FIXME
                if ((adjacentTriArray.length == 1 || (!adjacentTriArray[0][0][0].getVertices()[adjacentTriArray[0][0][0].rightAngleVertexIndex].equals(adjacentTriArray[1][0][0].getVertices()[(adjacentTriArray[1][0][0].rightAngleVertexIndex + 1) % 3]) && 
                                                      !adjacentTriArray[0][0][0].getVertices()[adjacentTriArray[0][0][0].rightAngleVertexIndex].equals(adjacentTriArray[1][0][1].getVertices()[(adjacentTriArray[1][0][1].rightAngleVertexIndex + 1) % 3]) )) && 
                    (adjacentTriArray[0].length == 1 || (!adjacentTriArray[0][0][0].getVertices()[adjacentTriArray[0][0][0].rightAngleVertexIndex].equals(adjacentTriArray[0][1][0].getVertices()[(adjacentTriArray[0][1][0].rightAngleVertexIndex + 1) % 3]) && 
                                                         !adjacentTriArray[0][0][0].getVertices()[adjacentTriArray[0][0][0].rightAngleVertexIndex].equals(adjacentTriArray[0][1][1].getVertices()[(adjacentTriArray[0][1][1].rightAngleVertexIndex + 1) % 3]) ))) {
                    srcTri0 = adjacentTriArray[0][0][0];
                    srcTri1 = adjacentTriArray[0][0][0];
                    srcTri2 = adjacentTriArray[0][0][0];
                    srcTri3 = adjacentTriArray[adjacentTriArray.length - 1][0][0];
                    srcTri4 = adjacentTriArray[0][adjacentTriArray[0].length - 1][0];
                    srcTri5 = adjacentTriArray[adjacentTriArray.length - 1][adjacentTriArray[0].length - 1][1];
                    srcTri6 = adjacentTriArray[adjacentTriArray.length - 1][adjacentTriArray[0].length - 1][1];
                    srcTri7 = adjacentTriArray[adjacentTriArray.length - 1][adjacentTriArray[0].length - 1][1];
                    srcTri8 = adjacentTriArray[adjacentTriArray.length - 1][0][1];
                    srcTri9 = adjacentTriArray[0][adjacentTriArray[0].length - 1][1];
                } else if ((adjacentTriArray.length == 1 || (!adjacentTriArray[0][0][1].getVertices()[adjacentTriArray[0][0][1].rightAngleVertexIndex].equals(adjacentTriArray[1][0][0].getVertices()[(adjacentTriArray[1][0][0].rightAngleVertexIndex + 1) % 3]) && 
                                                             !adjacentTriArray[0][0][1].getVertices()[adjacentTriArray[0][0][1].rightAngleVertexIndex].equals(adjacentTriArray[1][0][1].getVertices()[(adjacentTriArray[1][0][1].rightAngleVertexIndex + 1) % 3]) )) && 
                           (adjacentTriArray[0].length == 1 || (!adjacentTriArray[0][0][1].getVertices()[adjacentTriArray[0][0][1].rightAngleVertexIndex].equals(adjacentTriArray[0][1][0].getVertices()[(adjacentTriArray[0][1][0].rightAngleVertexIndex + 1) % 3]) && 
                                                                !adjacentTriArray[0][0][1].getVertices()[adjacentTriArray[0][0][1].rightAngleVertexIndex].equals(adjacentTriArray[0][1][1].getVertices()[(adjacentTriArray[0][1][1].rightAngleVertexIndex + 1) % 3]) ))) {
                    srcTri0 = adjacentTriArray[0][0][1];
                    srcTri1 = adjacentTriArray[0][0][1];
                    srcTri2 = adjacentTriArray[0][0][1];
                    srcTri3 = adjacentTriArray[adjacentTriArray.length - 1][0][1];
                    srcTri4 = adjacentTriArray[0][adjacentTriArray[0].length - 1][1];
                    srcTri5 = adjacentTriArray[adjacentTriArray.length - 1][adjacentTriArray[0].length - 1][0];
                    srcTri6 = adjacentTriArray[adjacentTriArray.length - 1][adjacentTriArray[0].length - 1][0];
                    srcTri7 = adjacentTriArray[adjacentTriArray.length - 1][adjacentTriArray[0].length - 1][0];
                    srcTri8 = adjacentTriArray[adjacentTriArray.length - 1][0][0];
                    srcTri9 = adjacentTriArray[0][adjacentTriArray[0].length - 1][0];
                } else if ((adjacentTriArray.length == 1 || (!adjacentTriArray[0][0][0].getVertices()[(adjacentTriArray[0][0][0].rightAngleVertexIndex + 1) % 3].equals(adjacentTriArray[1][0][0].getVertices()[adjacentTriArray[1][0][0].rightAngleVertexIndex]) &&
                                                             !adjacentTriArray[0][0][0].getVertices()[(adjacentTriArray[0][0][0].rightAngleVertexIndex + 1) % 3].equals(adjacentTriArray[1][0][1].getVertices()[adjacentTriArray[1][0][1].rightAngleVertexIndex]) )) && 
                           (adjacentTriArray[0].length == 1 || (!adjacentTriArray[0][0][0].getVertices()[(adjacentTriArray[0][0][0].rightAngleVertexIndex + 1) % 3].equals(adjacentTriArray[0][1][0].getVertices()[adjacentTriArray[0][1][0].rightAngleVertexIndex]) &&
                                                                !adjacentTriArray[0][0][0].getVertices()[(adjacentTriArray[0][0][0].rightAngleVertexIndex + 1) % 3].equals(adjacentTriArray[0][1][1].getVertices()[adjacentTriArray[0][1][1].rightAngleVertexIndex]) ))) {
                    srcTri0 = adjacentTriArray[0][0][0];
                    srcTri1 = adjacentTriArray[adjacentTriArray.length - 1][0][0];
                    srcTri2 = adjacentTriArray[0][adjacentTriArray[0].length - 1][0];
                    srcTri3 = adjacentTriArray[0][0][0];
                    srcTri4 = adjacentTriArray[0][0][0];
                    srcTri5 = adjacentTriArray[adjacentTriArray.length - 1][adjacentTriArray[0].length - 1][1];
                    srcTri6 = adjacentTriArray[0][adjacentTriArray[0].length - 1][1];
                    srcTri7 = adjacentTriArray[adjacentTriArray.length - 1][0][1];
                    srcTri8 = adjacentTriArray[adjacentTriArray.length - 1][adjacentTriArray[0].length - 1][1];
                    srcTri9 = adjacentTriArray[adjacentTriArray.length - 1][adjacentTriArray[0].length - 1][1];
                } else if ((adjacentTriArray.length == 1 || (!adjacentTriArray[0][0][0].getVertices()[(adjacentTriArray[0][0][1].rightAngleVertexIndex + 1) % 3].equals(adjacentTriArray[1][0][0].getVertices()[adjacentTriArray[1][0][0].rightAngleVertexIndex]) &&
                                                             !adjacentTriArray[0][0][0].getVertices()[(adjacentTriArray[0][0][1].rightAngleVertexIndex + 1) % 3].equals(adjacentTriArray[1][0][1].getVertices()[adjacentTriArray[1][0][1].rightAngleVertexIndex]) )) && 
                           (adjacentTriArray[0].length == 1 || (!adjacentTriArray[0][0][0].getVertices()[(adjacentTriArray[0][0][1].rightAngleVertexIndex + 1) % 3].equals(adjacentTriArray[0][1][0].getVertices()[adjacentTriArray[0][1][0].rightAngleVertexIndex]) &&
                                                                !adjacentTriArray[0][0][0].getVertices()[(adjacentTriArray[0][0][1].rightAngleVertexIndex + 1) % 3].equals(adjacentTriArray[0][1][1].getVertices()[adjacentTriArray[0][1][1].rightAngleVertexIndex])  ))) {
                    srcTri0 = adjacentTriArray[0][0][1];
                    srcTri1 = adjacentTriArray[adjacentTriArray.length - 1][0][1];
                    srcTri2 = adjacentTriArray[0][adjacentTriArray[0].length - 1][1];
                    srcTri3 = adjacentTriArray[0][0][1];
                    srcTri4 = adjacentTriArray[0][0][1];
                    srcTri5 = adjacentTriArray[adjacentTriArray.length - 1][adjacentTriArray[0].length - 1][0];
                    srcTri6 = adjacentTriArray[0][adjacentTriArray[0].length - 1][0];
                    srcTri7 = adjacentTriArray[adjacentTriArray.length - 1][0][0];
                    srcTri8 = adjacentTriArray[adjacentTriArray.length - 1][adjacentTriArray[0].length - 1][0];
                    srcTri9 = adjacentTriArray[adjacentTriArray.length - 1][adjacentTriArray[0].length - 1][0];
                } else {
                    srcTri0 = null;
                    srcTri1 = null;
                    srcTri2 = null;
                    srcTri3 = null;
                    srcTri4 = null;
                    srcTri5 = null;
                    srcTri6 = null;
                    srcTri7 = null;
                    srcTri8 = null;
                    srcTri9 = null;
                }
                final Map.Entry<TriangleInfo[][][], TriangleInfo[]> mergeableToMergeEntry;
                if (srcTri0 != null && srcTri1 != null && srcTri2 != null && srcTri3 != null && srcTri4 != null && srcTri5 != null && srcTri6 != null && srcTri7 != null && srcTri8 != null && srcTri9 != null) {
                    final Vector3[] tri0Vertices = new Vector3[3];
                    final Vector2[] tri0TexCoords = new Vector2[3];
                    final Vector3[] tri0Normals = meshData.getNormalBuffer() == null ? null : new Vector3[3];
                    final Vector3[] tri1Vertices = new Vector3[3];
                    final Vector2[] tri1TexCoords = new Vector2[3];
                    final Vector3[] tri1Normals = meshData.getNormalBuffer() == null ? null : new Vector3[3];
                    if (srcTri0.getTextureCoords()[srcTri0.rightAngleVertexIndex].equals(srcTri1.getTextureCoords()[srcTri1.rightAngleVertexIndex])) {
                        tri0Vertices[srcTri0.rightAngleVertexIndex] = srcTri1.getVertices()[srcTri1.rightAngleVertexIndex];
                        tri0TexCoords[srcTri0.rightAngleVertexIndex] = srcTri1.getTextureCoords()[srcTri1.rightAngleVertexIndex];
                        if (tri0Normals != null) {
                            tri0Normals[srcTri0.rightAngleVertexIndex] = srcTri1.getNormals()[srcTri1.rightAngleVertexIndex];
                        }
                    } else {
                        tri0Vertices[srcTri0.rightAngleVertexIndex] = srcTri2.getVertices()[srcTri2.rightAngleVertexIndex];
                        tri0TexCoords[srcTri0.rightAngleVertexIndex] = srcTri2.getTextureCoords()[srcTri2.rightAngleVertexIndex];
                        if (tri0Normals != null) {
                            tri0Normals[srcTri0.rightAngleVertexIndex] = srcTri1.getNormals()[srcTri2.rightAngleVertexIndex];
                        }
                    }
                    if (srcTri0.getTextureCoords()[(srcTri0.rightAngleVertexIndex + 1) % 3].equals(srcTri3.getTextureCoords()[(srcTri3.rightAngleVertexIndex + 1) % 3])) {
                        tri0Vertices[(srcTri0.rightAngleVertexIndex + 1) % 3] = srcTri3.getVertices()[(srcTri3.rightAngleVertexIndex + 1) % 3];
                        tri0TexCoords[(srcTri0.rightAngleVertexIndex + 1) % 3] = srcTri3.getTextureCoords()[(srcTri3.rightAngleVertexIndex + 1) % 3];
                        if (tri0Normals != null) {
                            tri0Normals[(srcTri0.rightAngleVertexIndex + 1) % 3] = srcTri3.getNormals()[(srcTri3.rightAngleVertexIndex + 1) % 3];
                        }
                    } else {
                        tri0Vertices[(srcTri0.rightAngleVertexIndex + 1) % 3] = srcTri4.getVertices()[(srcTri4.rightAngleVertexIndex + 1) % 3];
                        tri0TexCoords[(srcTri0.rightAngleVertexIndex + 1) % 3] = srcTri4.getTextureCoords()[(srcTri4.rightAngleVertexIndex + 1) % 3];
                        if (tri0Normals != null) {
                            tri0Normals[(srcTri0.rightAngleVertexIndex + 1) % 3] = srcTri4.getNormals()[(srcTri4.rightAngleVertexIndex + 1) % 3];
                        }
                    }
                    if (srcTri0.getTextureCoords()[(srcTri0.rightAngleVertexIndex + 2) % 3].equals(srcTri3.getTextureCoords()[(srcTri3.rightAngleVertexIndex + 2) % 3])) {
                        tri0Vertices[(srcTri0.rightAngleVertexIndex + 2) % 3] = srcTri3.getVertices()[(srcTri3.rightAngleVertexIndex + 2) % 3];
                        tri0TexCoords[(srcTri0.rightAngleVertexIndex + 2) % 3] = srcTri3.getTextureCoords()[(srcTri3.rightAngleVertexIndex + 2) % 3];
                        if (tri0Normals != null) {
                            tri0Normals[(srcTri0.rightAngleVertexIndex + 2) % 3] = srcTri3.getNormals()[(srcTri3.rightAngleVertexIndex + 2) % 3];
                        }
                    } else {
                        tri0Vertices[(srcTri0.rightAngleVertexIndex + 2) % 3] = srcTri4.getVertices()[(srcTri4.rightAngleVertexIndex + 2) % 3];
                        tri0TexCoords[(srcTri0.rightAngleVertexIndex + 2) % 3] = srcTri4.getTextureCoords()[(srcTri4.rightAngleVertexIndex + 2) % 3];
                        if (tri0Normals != null) {
                            tri0Normals[(srcTri0.rightAngleVertexIndex + 2) % 3] = srcTri4.getNormals()[(srcTri4.rightAngleVertexIndex + 2) % 3];
                        }
                    }
                    if (srcTri5.getTextureCoords()[srcTri5.rightAngleVertexIndex].equals(srcTri6.getTextureCoords()[srcTri6.rightAngleVertexIndex])) {
                        tri1Vertices[srcTri5.rightAngleVertexIndex] = srcTri6.getVertices()[srcTri6.rightAngleVertexIndex];
                        tri1TexCoords[srcTri5.rightAngleVertexIndex] = srcTri6.getTextureCoords()[srcTri6.rightAngleVertexIndex];
                        if (tri1Normals != null) {
                            tri1Normals[srcTri5.rightAngleVertexIndex] = srcTri6.getNormals()[srcTri6.rightAngleVertexIndex];
                        }
                    } else {
                        tri1Vertices[srcTri5.rightAngleVertexIndex] = srcTri7.getVertices()[srcTri7.rightAngleVertexIndex];
                        tri1TexCoords[srcTri5.rightAngleVertexIndex] = srcTri7.getTextureCoords()[srcTri7.rightAngleVertexIndex];
                        if (tri1Normals != null) {
                            tri1Normals[srcTri5.rightAngleVertexIndex] = srcTri7.getNormals()[srcTri7.rightAngleVertexIndex];
                        }
                    }
                    if (srcTri5.getTextureCoords()[(srcTri5.rightAngleVertexIndex + 1) % 3].equals(srcTri8.getTextureCoords()[(srcTri8.rightAngleVertexIndex + 1) % 3])) {
                        tri1Vertices[(srcTri5.rightAngleVertexIndex + 1) % 3] = srcTri8.getVertices()[(srcTri8.rightAngleVertexIndex + 1) % 3];
                        tri1TexCoords[(srcTri5.rightAngleVertexIndex + 1) % 3] = srcTri8.getTextureCoords()[(srcTri8.rightAngleVertexIndex + 1) % 3];
                        if (tri1Normals != null) {
                            tri1Normals[(srcTri5.rightAngleVertexIndex + 1) % 3] = srcTri8.getNormals()[(srcTri8.rightAngleVertexIndex + 1) % 3];
                        }
                    } else {
                        tri1Vertices[(srcTri5.rightAngleVertexIndex + 1) % 3] = srcTri9.getVertices()[(srcTri9.rightAngleVertexIndex + 1) % 3];
                        tri1TexCoords[(srcTri5.rightAngleVertexIndex + 1) % 3] = srcTri9.getTextureCoords()[(srcTri9.rightAngleVertexIndex + 1) % 3];
                        if (tri1Normals != null) {
                            tri1Normals[(srcTri5.rightAngleVertexIndex + 1) % 3] = srcTri9.getNormals()[(srcTri9.rightAngleVertexIndex + 1) % 3];
                        }
                    }
                    if (srcTri5.getTextureCoords()[(srcTri5.rightAngleVertexIndex + 2) % 3].equals(srcTri8.getTextureCoords()[(srcTri8.rightAngleVertexIndex + 2) % 3])) {
                        tri1Vertices[(srcTri5.rightAngleVertexIndex + 2) % 3] = srcTri8.getVertices()[(srcTri8.rightAngleVertexIndex + 2) % 3];
                        tri1TexCoords[(srcTri5.rightAngleVertexIndex + 2) % 3] = srcTri8.getTextureCoords()[(srcTri8.rightAngleVertexIndex + 2) % 3];
                        if (tri1Normals != null) {
                            tri1Normals[(srcTri5.rightAngleVertexIndex + 2) % 3] = srcTri8.getNormals()[(srcTri8.rightAngleVertexIndex + 2) % 3];
                        }
                    } else {
                        tri1Vertices[(srcTri5.rightAngleVertexIndex + 2) % 3] = srcTri9.getVertices()[(srcTri9.rightAngleVertexIndex + 2) % 3];
                        tri1TexCoords[(srcTri5.rightAngleVertexIndex + 2) % 3] = srcTri9.getTextureCoords()[(srcTri9.rightAngleVertexIndex + 2) % 3];
                        if (tri1Normals != null) {
                            tri1Normals[(srcTri5.rightAngleVertexIndex + 2) % 3] = srcTri9.getNormals()[(srcTri9.rightAngleVertexIndex + 2) % 3];
                        }
                    }
                    // modifies the texture coordinates to repeat the textures
                    IntStream.range(0, 3)
                        .mapToObj((final int texCoordIndex) -> Stream.of(tri0TexCoords[texCoordIndex], tri1TexCoords[texCoordIndex]))
                        .flatMap(Stream::sequential)
                        .forEachOrdered((final Vector2 texCoord) -> {
                            if (texCoord.getX() == 1.0) {
                                texCoord.setX(adjacentTriArray.length);
                            }
                            if (texCoord.getY() == 1.0) {
                                texCoord.setY(adjacentTriArray[0].length);
                            }
                        });
                    mergeableToMergeEntry = new AbstractMap.SimpleImmutableEntry<>(adjacentTriArray, new TriangleInfo[] {new TriangleInfo(srcTri0.sectionIndex, tri0Vertices, tri0TexCoords, tri0Normals), new TriangleInfo(srcTri5.sectionIndex, tri1Vertices, tri1TexCoords, tri1Normals)});
                } else {
                    mergeableToMergeEntry = null;
                    System.err.println("Missing corners");
                }
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
