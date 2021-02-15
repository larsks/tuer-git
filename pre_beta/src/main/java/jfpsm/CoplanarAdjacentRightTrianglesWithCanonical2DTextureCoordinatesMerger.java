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
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
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

    static final class RightTriangleInfo {

        private final int primitiveIndex;

        private final int sectionIndex;

        private final int sideIndexOfHypotenuse;
        
        private final Vector3[] vertices;
        
        private final Vector2[] textureCoords;
        
        private final int[] indices;

        RightTriangleInfo(final int primitiveIndex, final int sectionIndex, final int sideIndexOfHypotenuse, final MeshData meshData) {
            super();
            this.primitiveIndex = primitiveIndex;
            this.sectionIndex = sectionIndex;
            this.sideIndexOfHypotenuse = sideIndexOfHypotenuse;
            if (meshData == null) {
                this.vertices = null;
                this.textureCoords = null;
                this.indices = null;
            } else {
                this.vertices = meshData.getPrimitiveVertices(this.primitiveIndex, this.sectionIndex, null);
                this.textureCoords = getPrimitiveTextureCoords(meshData, this.primitiveIndex, this.sectionIndex, 0, null);
                this.indices = meshData.getPrimitiveIndices(this.primitiveIndex, this.sectionIndex, null);
            }
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
            if (o == null || !(o instanceof RightTriangleInfo)) {
                result = false;
            } else if (o == this) {
                result = true;
            } else {
                final RightTriangleInfo r = (RightTriangleInfo) o;
                result = primitiveIndex == r.primitiveIndex && sectionIndex == r.sectionIndex
                            && sideIndexOfHypotenuse == r.sideIndexOfHypotenuse;
            }
            return result;
        }

        @Override
        public int hashCode() {
            return ((sideIndexOfHypotenuse & 0xff) | (sectionIndex & 0xff << 8) | (primitiveIndex & 0xffff << 16));
        }
        
        @Override
        public String toString() {
            return "RightTriangleInfo {primitiveIndex: " + primitiveIndex + " sectionIndex: " + sectionIndex + " sideIndexOfHypotenuse: " + sideIndexOfHypotenuse + "}";
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
     * Returns the triangle side index of the hypotenuse if any, otherwise -1
     * 
     * @param triangleVertices triangle vertices
     * @return triangle side index of the hypotenuse if any, otherwise -1
     */
    private static int getSideIndexOfHypotenuse(final Vector3[] triangleVertices) {
        //TODO rather check whether the dot product is equal to zero
        // computes the squared distances of all sides
        final double[] triangleSideDistancesSquared = IntStream.range(0, triangleVertices.length)
                                                               .mapToDouble((final int triangleSideIndex) -> triangleVertices[triangleSideIndex].distanceSquared(triangleVertices[(triangleSideIndex + 1) % 3]))
                                                               .toArray();
        // uses these squared distances to find the hypotenuse if any by (i.e if it's a right-angled triangle) using the Pythagorean theorem
        return IntStream.range(0, triangleSideDistancesSquared.length)
                        .filter((final int triangleSideIndex) -> triangleSideDistancesSquared[triangleSideIndex] == triangleSideDistancesSquared[(triangleSideIndex + 1) % 3] + triangleSideDistancesSquared[(triangleSideIndex + 2) % 3])
                        .findFirst()
                        .orElse(-1);
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
            final List<RightTriangleInfo> rightTrianglesWithCanonical2DTextureCoordinatesInfos = IntStream.range(0, meshData.getSectionCount())
                // loops on all triangles of each section
                .mapToObj((final int sectionIndex) -> IntStream.range(0, meshData.getPrimitiveCount(sectionIndex))
                // checks whether its texture coordinates are canonical, only considers the first texture index
                .filter((final int trianglePrimitiveIndex) -> hasCanonicalTextureCoords(getPrimitiveTextureCoords(meshData, trianglePrimitiveIndex, sectionIndex, 0, null)))
                .mapToObj((final int trianglePrimitiveIndex) -> {
                    final RightTriangleInfo rightTriangleInfo;
                    // gets the 3 vertices of the triangle
                    final Vector3[] triangleVertices = meshData.getPrimitiveVertices(trianglePrimitiveIndex, sectionIndex, null);
                    final int sideIndexOfHypotenuse = getSideIndexOfHypotenuse(triangleVertices);
                    // if this triangle is right-angled
                    if (sideIndexOfHypotenuse == -1) {
                        rightTriangleInfo = null;
                    } else {
                        // stores the side index of its hypotenuse and several indices allowing to retrieve the required data further
                        rightTriangleInfo = new RightTriangleInfo(trianglePrimitiveIndex, sectionIndex, sideIndexOfHypotenuse, meshData);
                    }
                    return rightTriangleInfo;
                })
                .filter(Objects::nonNull))
                .flatMap(Stream::sequential)
                .collect(Collectors.toList());
            // second step: sorts the triangles of the former set by planes (4D: normal + distance to plane)
            Map<Plane, List<RightTriangleInfo>> mapOfTrianglesByPlanes = rightTrianglesWithCanonical2DTextureCoordinatesInfos.stream()
                    .map((final RightTriangleInfo info) -> {
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
            System.out.println("Number of planes: " + mapOfTrianglesByPlanes.size());
            mapOfTrianglesByPlanes.entrySet().stream().forEach(System.out::println);
            System.out.println("Number of triangles: " + mapOfTrianglesByPlanes.values().stream().mapToInt(List::size).sum());
            // third step: retains only triangles by pairs which could be used to create rectangles
            final Map<Plane, List<RightTriangleInfo[]>> mapOfRightTrianglesWithSameHypotenusesByPairs = mapOfTrianglesByPlanes.entrySet().stream().map((final Entry<Plane, List<RightTriangleInfo>> entry) -> {
                final List<RightTriangleInfo> rightTriangles = entry.getValue();
                final int triCount = rightTriangles.size();
                // for each RightTriangleInfo instance
                return new AbstractMap.SimpleImmutableEntry<>(entry.getKey(), IntStream.range(0, triCount)
                         .mapToObj((final int triIndex1) -> IntStream.range(0, triCount)
                                                                     .filter((final int triIndex2) ->  triIndex1 < triIndex2)
                                                                     .mapToObj((final int triIndex2) -> new int[]{triIndex1, triIndex2}))
                         .flatMap(Stream::sequential)
                         .filter((final int[] triIndices) -> {
                             final RightTriangleInfo tri1 = rightTriangles.get(triIndices[0]);
                             final RightTriangleInfo tri2 = rightTriangles.get(triIndices[1]);
                             final Vector3[] tri1Vertices = tri1.getVertices();
                             final Vector3[] tri2Vertices = tri2.getVertices();
                             final Vector2[] tri1TextureCoords = tri1.getTextureCoords();
                             final Vector2[] tri2TextureCoords = tri2.getTextureCoords();
                             // checks if the vertices of the hypotenuse are the same but in the opposite order (so that both triangles have the same normal)
                             return tri1Vertices[tri1.sideIndexOfHypotenuse].equals(tri2Vertices[(tri2.sideIndexOfHypotenuse + 1) % 3]) &&
                                    tri1Vertices[(tri1.sideIndexOfHypotenuse + 1) % 3].equals(tri2Vertices[tri2.sideIndexOfHypotenuse]) &&
                                    // checks that the texture coordinates of the vertices in the hypotenuse are the same
                                    tri1TextureCoords[tri1.sideIndexOfHypotenuse].equals(tri2TextureCoords[(tri2.sideIndexOfHypotenuse + 1) % 3]) &&
                                    tri1TextureCoords[(tri1.sideIndexOfHypotenuse + 1) % 3].equals(tri2TextureCoords[tri2.sideIndexOfHypotenuse]) &&
                                    // checks that there's a symmetry to go to the vertex outside of the hypotenuse
                                    tri1Vertices[(tri1.sideIndexOfHypotenuse + 2) % 3].subtract(tri1Vertices[(tri1.sideIndexOfHypotenuse + 1) % 3], null).equals(
                                    tri2Vertices[(tri2.sideIndexOfHypotenuse + 1) % 3].subtract(tri2Vertices[(tri2.sideIndexOfHypotenuse + 2) % 3], null)) &&
                                    // checks that all canonical texture coordinates are in the pair of triangles
                                    Stream.of(tri1TextureCoords[tri1.sideIndexOfHypotenuse], tri1TextureCoords[(tri1.sideIndexOfHypotenuse + 1) % 3], tri1TextureCoords[(tri1.sideIndexOfHypotenuse + 2) % 3], tri2TextureCoords[(tri2.sideIndexOfHypotenuse + 2) % 3]).mapToDouble(Vector2::getX).filter((final double u) -> u == 0.0).count() == 2 &&
                                    Stream.of(tri1TextureCoords[tri1.sideIndexOfHypotenuse], tri1TextureCoords[(tri1.sideIndexOfHypotenuse + 1) % 3], tri1TextureCoords[(tri1.sideIndexOfHypotenuse + 2) % 3], tri2TextureCoords[(tri2.sideIndexOfHypotenuse + 2) % 3]).mapToDouble(Vector2::getX).filter((final double u) -> u == 1.0).count() == 2 &&
                                    Stream.of(tri1TextureCoords[tri1.sideIndexOfHypotenuse], tri1TextureCoords[(tri1.sideIndexOfHypotenuse + 1) % 3], tri1TextureCoords[(tri1.sideIndexOfHypotenuse + 2) % 3], tri2TextureCoords[(tri2.sideIndexOfHypotenuse + 2) % 3]).mapToDouble(Vector2::getY).filter((final double v) -> v == 0.0).count() == 2 &&
                                    Stream.of(tri1TextureCoords[tri1.sideIndexOfHypotenuse], tri1TextureCoords[(tri1.sideIndexOfHypotenuse + 1) % 3], tri1TextureCoords[(tri1.sideIndexOfHypotenuse + 2) % 3], tri2TextureCoords[(tri2.sideIndexOfHypotenuse + 2) % 3]).mapToDouble(Vector2::getY).filter((final double v) -> v == 1.0).count() == 2;
                         })
                         .map((final int[] triIndices) -> new RightTriangleInfo[]{rightTriangles.get(triIndices[0]), rightTriangles.get(triIndices[1])})
                         .collect(Collectors.toList()));
            })
                    .collect(Collectors.toMap(AbstractMap.SimpleImmutableEntry::getKey, AbstractMap.SimpleImmutableEntry::getValue));
            System.out.println("Number of planes: " + mapOfRightTrianglesWithSameHypotenusesByPairs.size());
            mapOfRightTrianglesWithSameHypotenusesByPairs.entrySet().stream()
                .map((final Map.Entry<Plane, List<RightTriangleInfo[]>> entry) -> entry.getKey() + "=" + entry.getValue().stream().map(Arrays::stream).flatMap(Stream::sequential).collect(Collectors.toList()))
                .forEach(System.out::println);
            System.out.println("Number of triangles: " + mapOfRightTrianglesWithSameHypotenusesByPairs.values().stream().mapToInt(List::size).sum() * 2);
            // fourth step: creates lists containing all adjacent rectangles in the same planes
            HashMap<Plane, ArrayList<ArrayList<RightTriangleInfo>>> mapOfListsOfTrianglesByPlanes = new HashMap<>();
            HashMap<RightTriangleInfo, ArrayList<Entry<RightTriangleInfo[], int[]>>> commonSidesInfosMap = new HashMap<>();
            RightTriangleInfo tri3, tri4;
            // for each plane of the map
            for (final Entry<Plane, List<RightTriangleInfo[]>> entry : mapOfRightTrianglesWithSameHypotenusesByPairs.entrySet()) {
                List<RightTriangleInfo[]> rightTrianglesByPairs = entry.getValue();
                Plane plane = entry.getKey();
                final int triCount = rightTrianglesByPairs.size();
                for (int triIndex12 = 0; triIndex12 < triCount - 2; triIndex12++) {
                    final RightTriangleInfo tri1 = rightTrianglesByPairs.get(triIndex12)[0];
                    final RightTriangleInfo tri2 = rightTrianglesByPairs.get(triIndex12)[1];
                    ArrayList<ArrayList<RightTriangleInfo>> listOfListsOfTris = mapOfListsOfTrianglesByPlanes
                            .get(plane);
                    ArrayList<RightTriangleInfo> listOfTris = null;
                    // if the list of lists for this plane exists
                    if (listOfListsOfTris != null) {
                        // checks if tri1 and tri2 are already in a list
                        for (ArrayList<RightTriangleInfo> list : listOfListsOfTris)
                            // only looks for tri1 as tri1 and tri2 should be
                            // together
                            if (list.contains(tri1)) {
                                listOfTris = list;
                                break;
                            }
                    }
                    final Vector3[] tri1Vertices = tri1.getVertices();
                    final Vector3[] tri2Vertices = tri2.getVertices();
                    for (int triIndex34 = triIndex12 + 1; triIndex34 < triCount - 1; triIndex34++) {
                        tri3 = rightTrianglesByPairs.get(triIndex34)[0];
                        tri4 = rightTrianglesByPairs.get(triIndex34)[1];
                        final Vector3[] tri3Vertices = tri3.getVertices();
                        final Vector3[] tri4Vertices = tri4.getVertices();
                        Vector3[][] trisVertices = new Vector3[][] { tri1Vertices, tri2Vertices, tri3Vertices, tri4Vertices };
                        RightTriangleInfo[] tris = new RightTriangleInfo[trisVertices.length];
                        Vector2[][] trisTextureCoords = new Vector2[trisVertices.length][];
                        boolean oneCommonSideCorrectVertexOrder2oppositeSidesOfSameLengthAndSameTextureCoordinatesFound = false;
                        boolean oneCommonSideCorrectVertexOrder2oppositeSidesOfSameLength = false;
                        boolean oneCommonSideCorrectVertexOrder = false;
                        boolean oneCommonSide = false;
                        boolean oneCommonVertex = false;
                        /**
                         * checks if both rectangles have exactly one common
                         * side, i.e if one vertex is common to 2 triangles from
                         * 2 different rectangles but not on any hypotenuse and
                         * if another vertex is common to 2 triangles from 2
                         * different rectangles but on the both hypotenuse.
                         * Then, it checks if the vertex order of the rectangles
                         * is the same After that, it checks if the orthogonal
                         * sides adjacent with this common side have the same
                         * length. Finally, it checks if both rectangles have
                         * the same texture coordinates.
                         */
                        tris[0] = tri1;
                        tris[1] = tri2;
                        tris[2] = tri3;
                        tris[3] = tri4;
                        for (int i = 0, ti0, ti1, ti2, ti3; i < 4
                                && !oneCommonSideCorrectVertexOrder2oppositeSidesOfSameLength; i++) {
                            Entry<RightTriangleInfo[], int[]> commonSideInfo = null;
                            // {0;1}
                            ti0 = i / 2;
                            Vector3[] tv0 = trisVertices[ti0];
                            RightTriangleInfo tr0 = tris[ti0];
                            // {2;3}
                            ti1 = 2 + (i % 2);
                            Vector3[] tv1 = trisVertices[ti1];
                            RightTriangleInfo tr1 = tris[ti1];
                            // {1;0}
                            ti2 = ((i / 2) + 1) % 2;
                            RightTriangleInfo tr2 = tris[ti2];
                            // {3;2}
                            ti3 = 2 + ((i + 1) % 2);
                            RightTriangleInfo tr3 = tris[ti3];
                            // checks if both rectangles have exactly one common side
                            for (int j = 0; j < 3 && !oneCommonVertex; j++)
                                if (tv0[(tr0.sideIndexOfHypotenuse + 2) % 3]
                                        .equals(tv1[(tr1.sideIndexOfHypotenuse + j) % 3])) {
                                    oneCommonVertex = true;
                                    if (j != 2) {
                                        for (int k = 0; k < 2 && !oneCommonSide; k++)
                                            if (tv0[(tr0.sideIndexOfHypotenuse + k) % 3]
                                                    .equals(tv1[(tr1.sideIndexOfHypotenuse + 2) % 3])) {
                                                oneCommonSide = true;
                                                // checks if the vertex order is correct
                                                // FIXME this test seems to be wrong
                                                oneCommonSideCorrectVertexOrder = /* j != k */true;
                                                if (oneCommonSideCorrectVertexOrder) {
                                                    // checks if the orthogonal sides adjacent with this common side have the same length
                                                    if (tv0[(tr0.sideIndexOfHypotenuse + ((k + 1) % 2)) % 3]
                                                            .distanceSquared(tv0[(tr0.sideIndexOfHypotenuse + 2)
                                                                    % 3]) == tv1[(tr1.sideIndexOfHypotenuse
                                                                            + ((j + 1) % 2)) % 3].distanceSquared(
                                                                                    tv1[(tr1.sideIndexOfHypotenuse + 2)
                                                                                            % 3])) {
                                                        oneCommonSideCorrectVertexOrder2oppositeSidesOfSameLength = true;
                                                        // checks the texture coordinates
                                                        boolean texCoordsMatch = true;
                                                        // only considers the first texture index
                                                        final int textureIndex = 0;
                                                        // gets all texture coordinates
                                                        for (int l = 0; l < 4; l++)
                                                            trisTextureCoords[l] = getPrimitiveTextureCoords(meshData,
                                                                    tris[l].primitiveIndex, tris[l].sectionIndex,
                                                                    textureIndex, trisTextureCoords[l]);
                                                        // checks if both rectangles have the same texture coordinates
                                                        texCoordsMatch &= trisTextureCoords[ti0][(tr0.sideIndexOfHypotenuse
                                                                + 2) % 3]
                                                                        .equals(trisTextureCoords[ti3][(tr3.sideIndexOfHypotenuse
                                                                                + 2) % 3]);
                                                        texCoordsMatch &= trisTextureCoords[ti1][(tr1.sideIndexOfHypotenuse
                                                                + 2) % 3]
                                                                        .equals(trisTextureCoords[ti2][(tr2.sideIndexOfHypotenuse
                                                                                + 2) % 3]);
                                                        texCoordsMatch &= trisTextureCoords[ti0][(tr0.sideIndexOfHypotenuse
                                                                + ((k + 1) % 2)) % 3]
                                                                        .equals(trisTextureCoords[ti1][(tr1.sideIndexOfHypotenuse
                                                                                + j) % 3]);
                                                        texCoordsMatch &= trisTextureCoords[ti1][(tr1.sideIndexOfHypotenuse
                                                                + ((j + 1) % 2)) % 3]
                                                                        .equals(trisTextureCoords[ti0][(tr0.sideIndexOfHypotenuse
                                                                                + k) % 3]);
                                                        oneCommonSideCorrectVertexOrder2oppositeSidesOfSameLengthAndSameTextureCoordinatesFound = texCoordsMatch;
                                                        if (oneCommonSideCorrectVertexOrder2oppositeSidesOfSameLengthAndSameTextureCoordinatesFound) {
                                                            // stores tr0, tr1, tr2, tr3 and the indices for further uses
                                                            commonSideInfo = new AbstractMap.SimpleEntry<>(
                                                                    new RightTriangleInfo[] { tr0, tr1, tr0, tr1 },
                                                                    new int[] { (tr0.sideIndexOfHypotenuse + 2) % 3,
                                                                            (tr1.sideIndexOfHypotenuse + j) % 3,
                                                                            (tr0.sideIndexOfHypotenuse + k) % 3,
                                                                            (tr1.sideIndexOfHypotenuse + 2) % 3 });
                                                            ArrayList<Entry<RightTriangleInfo[], int[]>> commonSidesInfosEntriesList = commonSidesInfosMap
                                                                    .get(tr0);
                                                            if (commonSidesInfosEntriesList == null) {
                                                                commonSidesInfosEntriesList = new ArrayList<>();
                                                                commonSidesInfosMap.put(tr0,
                                                                        commonSidesInfosEntriesList);
                                                            }
                                                            commonSidesInfosEntriesList.add(commonSideInfo);
                                                            commonSidesInfosEntriesList = commonSidesInfosMap.get(tr1);
                                                            if (commonSidesInfosEntriesList == null) {
                                                                commonSidesInfosEntriesList = new ArrayList<>();
                                                                commonSidesInfosMap.put(tr1,
                                                                        commonSidesInfosEntriesList);
                                                            }
                                                            commonSidesInfosEntriesList.add(commonSideInfo);
                                                        }
                                                    }
                                                }
                                            }
                                    } else {
                                        for (int k = 0; k < 4 && !oneCommonSide; k++)
                                            if (tv0[(tr0.sideIndexOfHypotenuse + (k / 2)) % 3]
                                                    .equals(tv1[(tr1.sideIndexOfHypotenuse + (k % 2)) % 3])) {
                                                oneCommonSide = true;
                                                // checks if the vertex order is correct
                                                oneCommonSideCorrectVertexOrder = (k / 2) != (k % 2);
                                                if (oneCommonSideCorrectVertexOrder) {
                                                    // checks if the orthogonal sides adjacent with this common side have the same length
                                                    if (tv0[(tr0.sideIndexOfHypotenuse + (((k / 2) + 1) % 2)) % 3]
                                                            .distanceSquared(tv0[(tr0.sideIndexOfHypotenuse + 2)
                                                                    % 3]) == tv1[(tr1.sideIndexOfHypotenuse
                                                                            + ((k + 1) % 2)) % 3].distanceSquared(
                                                                                    tv1[(tr1.sideIndexOfHypotenuse + 2)
                                                                                            % 3])) {
                                                        oneCommonSideCorrectVertexOrder2oppositeSidesOfSameLength = true;
                                                        // checks the texture coordinates
                                                        boolean texCoordsMatch = true;
                                                        // only considers the first texture index
                                                        final int textureIndex = 0;
                                                        // gets all texture coordinates
                                                        for (int l = 0; l < 4; l++)
                                                            trisTextureCoords[l] = getPrimitiveTextureCoords(meshData,
                                                                    tris[l].primitiveIndex, tris[l].sectionIndex,
                                                                    textureIndex, trisTextureCoords[l]);
                                                        // checks if both rectangles have the same texture coordinates
                                                        texCoordsMatch &= trisTextureCoords[ti0][(tr0.sideIndexOfHypotenuse
                                                                + (((k / 2) + 1) % 2)) % 3]
                                                                        .equals(trisTextureCoords[ti1][(tr1.sideIndexOfHypotenuse
                                                                                + 2) % 3]);
                                                        texCoordsMatch &= trisTextureCoords[ti1][(tr1.sideIndexOfHypotenuse
                                                                + ((k + 1) % 2)) % 3]
                                                                        .equals(trisTextureCoords[ti0][(tr0.sideIndexOfHypotenuse
                                                                                + 2) % 3]);
                                                        texCoordsMatch &= trisTextureCoords[ti1][(tr1.sideIndexOfHypotenuse
                                                                + (k % 2)) % 3]
                                                                        .equals(trisTextureCoords[ti2][(tr2.sideIndexOfHypotenuse
                                                                                + 2) % 3]);
                                                        texCoordsMatch &= trisTextureCoords[ti0][(tr0.sideIndexOfHypotenuse
                                                                + (k / 2)) % 3]
                                                                        .equals(trisTextureCoords[ti3][(tr3.sideIndexOfHypotenuse
                                                                                + 2) % 3]);
                                                        oneCommonSideCorrectVertexOrder2oppositeSidesOfSameLengthAndSameTextureCoordinatesFound = texCoordsMatch;
                                                        if (oneCommonSideCorrectVertexOrder2oppositeSidesOfSameLengthAndSameTextureCoordinatesFound) {
                                                            // stores tr0, tr1, tr2, tr3 and the indices for further uses
                                                            commonSideInfo = new AbstractMap.SimpleEntry<>(
                                                                    new RightTriangleInfo[] { tr0, tr1, tr0, tr1 },
                                                                    new int[] { (tr0.sideIndexOfHypotenuse + 2) % 3,
                                                                            (tr1.sideIndexOfHypotenuse + j) % 3,
                                                                            (tr0.sideIndexOfHypotenuse + (k / 2)) % 3,
                                                                            (tr1.sideIndexOfHypotenuse + (k % 2))
                                                                                    % 3 });
                                                            ArrayList<Entry<RightTriangleInfo[], int[]>> commonSidesInfosEntriesList = commonSidesInfosMap
                                                                    .get(tr0);
                                                            if (commonSidesInfosEntriesList == null) {
                                                                commonSidesInfosEntriesList = new ArrayList<>();
                                                                commonSidesInfosMap.put(tr0,
                                                                        commonSidesInfosEntriesList);
                                                            }
                                                            commonSidesInfosEntriesList.add(commonSideInfo);
                                                            commonSidesInfosEntriesList = commonSidesInfosMap.get(tr1);
                                                            if (commonSidesInfosEntriesList == null) {
                                                                commonSidesInfosEntriesList = new ArrayList<>();
                                                                commonSidesInfosMap.put(tr1,
                                                                        commonSidesInfosEntriesList);
                                                            }
                                                            commonSidesInfosEntriesList.add(commonSideInfo);
                                                        }
                                                    }
                                                }
                                            }
                                    }
                                }
                        }
                        if (oneCommonSideCorrectVertexOrder2oppositeSidesOfSameLengthAndSameTextureCoordinatesFound) {
                            ArrayList<RightTriangleInfo> previousListOfTris = null;
                            // if the list of lists for this plane does not exist
                            if (listOfListsOfTris == null) {// creates it and puts it into the map
                                listOfListsOfTris = new ArrayList<>();
                                mapOfListsOfTrianglesByPlanes.put(plane, listOfListsOfTris);
                            } else {// checks if tri3 and tri4 are already in a list
                                for (ArrayList<RightTriangleInfo> list : listOfListsOfTris)
                                    // only looks for tri3 as tri3 and tri4 should be together
                                    if (list.contains(tri3)) {
                                        previousListOfTris = list;
                                        break;
                                    }
                            }
                            // if the new list of triangles has not been created
                            if (listOfTris == null) {// creates it, fills it with the 4 triangles and adds it into the list of lists
                                listOfTris = new ArrayList<>();
                                listOfTris.add(tri1);
                                listOfTris.add(tri2);
                                listOfTris.add(tri3);
                                listOfTris.add(tri4);
                                listOfListsOfTris.add(listOfTris);
                            } else {// if tri3 and tri4 are not already in this list, adds them into it
                                if (previousListOfTris != null && previousListOfTris != listOfTris) {
                                    listOfTris.add(tri3);
                                    listOfTris.add(tri4);
                                }
                            }
                            // if tri3 and tri4 are already in another list
                            if (previousListOfTris != null) {
                                // removes all elements already added into the new list from the previous list to keep only elements which are not in the new list
                                previousListOfTris.removeAll(listOfTris);
                                // adds all elements which are not in the new list into it
                                listOfTris.addAll(previousListOfTris);
                            }
                        }
                    }
                }
            }
            // fifth step: creates lists of adjacent rectangles in the same planes usable to make bigger rectangles
            /**
             * Each entry handles the triangles of a plane. Each entry contains several lists of groups of adjacent triangles. Each group of
             * adjacent triangles is a list of arrays of adjacent triangles which could be merged to make bigger rectangles
             */
            HashMap<Plane, ArrayList<ArrayList<RightTriangleInfo[][][]>>> mapOfListsOfListsOfArraysOfMergeableTris = new HashMap<>();
            // for each plane
            for (Entry<Plane, ArrayList<ArrayList<RightTriangleInfo>>> entry : mapOfListsOfTrianglesByPlanes
                    .entrySet()) {
                final Plane plane = entry.getKey();
                // for each list of adjacent triangles
                for (ArrayList<RightTriangleInfo> trisList : entry.getValue())
                    if (!trisList.isEmpty()) {
                        // builds the 2D array from the list of triangles
                        final RightTriangleInfo[][][] adjacentTrisArray = compute2dTrisArrayFromAdjacentTrisList(
                                trisList, commonSidesInfosMap);
                        // computes a list of arrays of adjacent triangles which
                        // could be merged to make bigger rectangles
                        final java.util.Map<Vector2i, RightTriangleInfo[][][]> adjacentTrisArraysMap = computeAdjacentMergeableTrisArraysMap(
                                adjacentTrisArray);
                        // puts the new list into the map
                        ArrayList<ArrayList<RightTriangleInfo[][][]>> adjacentTrisArraysListsList = mapOfListsOfListsOfArraysOfMergeableTris
                                .get(plane);
                        if (adjacentTrisArraysListsList == null) {
                            adjacentTrisArraysListsList = new ArrayList<>();
                            mapOfListsOfListsOfArraysOfMergeableTris.put(plane, adjacentTrisArraysListsList);
                        }
                        adjacentTrisArraysListsList.add(new ArrayList<>(adjacentTrisArraysMap.values()));
                    }
            }
            // sixth step: creates these bigger rectangles with texture coordinates greater than 1 in order to use texture repeat
            HashMap<Plane, HashMap<RightTriangleInfo[][][], NextQuadInfo>> mapOfPreviousAndNextAdjacentTrisMaps = new HashMap<>();
            // for each plane
            for (Entry<Plane, ArrayList<ArrayList<RightTriangleInfo[][][]>>> entry : mapOfListsOfListsOfArraysOfMergeableTris
                    .entrySet()) {
                final Plane plane = entry.getKey();
                final HashMap<RightTriangleInfo[][][], NextQuadInfo> previousAdjacentTrisAndNextQuadInfosMaps = new HashMap<>();
                mapOfPreviousAndNextAdjacentTrisMaps.put(plane, previousAdjacentTrisAndNextQuadInfosMaps);
                // for each list of arrays of adjacent triangles which could be
                // merged to make bigger rectangles
                for (ArrayList<RightTriangleInfo[][][]> adjacentTrisArraysList : entry.getValue())
                    // for each array of adjacent triangles
                    for (RightTriangleInfo[][][] adjacentTrisArray : adjacentTrisArraysList) {
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
                                final RightTriangleInfo[] mergedAdjacentTris = new RightTriangleInfo[2];
                                final Vector3[] mergedAdjacentTrisVertices = new Vector3[4];
                                final Vector2[] mergedAdjacentTrisTextureCoords = new Vector2[4];
                                final int[] tmpLocalIndices = new int[4];
                                final int[] mergedAdjacentTrisVerticesIndices = new int[6];
                                final Vector3[] testedAdjacentTrisVertices = new Vector3[8];
                                final Vector2[] testedAdjacentTrisTextureCoords = new Vector2[4];
                                // for each pair of triangles in a corner of the
                                // array
                                for (int rowIndex = 0; rowIndex <= 1; rowIndex++) {
                                    final int rawRowIndex = rowIndex * (rowCount - 1);
                                    for (int columnIndex = 0; columnIndex <= 1; columnIndex++) {
                                        final int rawColumnIndex = columnIndex * (columnCount - 1);
                                        final RightTriangleInfo tri1 = adjacentTrisArray[rawRowIndex][rawColumnIndex][0];
                                        final RightTriangleInfo tri2 = adjacentTrisArray[rawRowIndex][rawColumnIndex][1];
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
                                                tri3 = adjacentTrisArray[secondaryRawRowIndex][secondaryRawColumnIndex][0];
                                                tri4 = adjacentTrisArray[secondaryRawRowIndex][secondaryRawColumnIndex][1];
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
                                final RightTriangleInfo tri1 = mergedAdjacentTris[0];
                                final RightTriangleInfo tri2 = mergedAdjacentTris[1];
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
            }
            // seventh step: removes the triangles which are no more in the geometry of the mesh
            final ArrayList<Integer> verticesIndicesToRemove = new ArrayList<>();
            // for each plane
            for (Entry<Plane, HashMap<RightTriangleInfo[][][], NextQuadInfo>> mapOfPreviousAndNextAdjacentTrisMapsEntry : mapOfPreviousAndNextAdjacentTrisMaps
                    .entrySet()) {
                // for each couple of old pairs and the new pairs (with some information)
                for (Entry<RightTriangleInfo[][][], NextQuadInfo> previousAdjacentTrisAndNextQuadInfosEntry : mapOfPreviousAndNextAdjacentTrisMapsEntry
                        .getValue().entrySet()) {
                    final RightTriangleInfo[][][] previousAdjacentTrisArray = previousAdjacentTrisAndNextQuadInfosEntry
                            .getKey();
                    for (int rowIndex = 0; rowIndex < previousAdjacentTrisArray.length; rowIndex++)
                        for (int columnIndex = 0; columnIndex < previousAdjacentTrisArray[rowIndex].length; columnIndex++) {
                            // retrieves the vertices
                            final RightTriangleInfo tri1 = previousAdjacentTrisArray[rowIndex][columnIndex][0];
                            final RightTriangleInfo tri2 = previousAdjacentTrisArray[rowIndex][columnIndex][1];
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
            for (HashMap<RightTriangleInfo[][][], NextQuadInfo> previousAndNextAdjacentTrisMap : mapOfPreviousAndNextAdjacentTrisMaps
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
            for (HashMap<RightTriangleInfo[][][], NextQuadInfo> previousAndNextAdjacentTrisMap : mapOfPreviousAndNextAdjacentTrisMaps
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
     * Computes a 2D array of adjacent triangles in the same plane by using
     * their relative location in this plane
     * 
     * @param trisList
     *            list of adjacent triangles
     * @param meshData
     *            mesh data
     * @return
     */
    static RightTriangleInfo[][][] compute2dTrisArrayFromAdjacentTrisList(final ArrayList<RightTriangleInfo> trisList,
            final HashMap<RightTriangleInfo, ArrayList<Entry<RightTriangleInfo[], int[]>>> commonSidesInfosMap) {
        /**
         * computes an overestimated size to be sure not to use an index out of
         * the bounds, uses the list size as all pairs of triangles represent
         * quads and some room is needed in all directions
         */
        final int overestimatedSize = trisList.size();
        // creates the 2D array
        final RightTriangleInfo[][][] adjacentTrisArray = new RightTriangleInfo[overestimatedSize][overestimatedSize][];
        // if this array can contain something
        if (overestimatedSize > 0) {
            /**
             * this initial index ensures there is enough room in all directions
             * for other triangles
             */
            final int initialIndex = (overestimatedSize / 2) - 1;
            adjacentTrisArray[initialIndex][initialIndex] = new RightTriangleInfo[] { trisList.get(0),
                    trisList.get(1) };
            /**
             * uses the following convention: 0 -> left, 1 -> top, 2 -> right, 3
             * -> bottom. Checks whether an edge of the pair of triangles is
             * equal to an edge of adjacentTrisArray[i][j]
             */
            final HashMap<RightTriangleInfo, int[]> arrayMap = new HashMap<>();
            arrayMap.put(trisList.get(0), new int[] { initialIndex, initialIndex });
            arrayMap.put(trisList.get(1), new int[] { initialIndex, initialIndex });
            final ArrayList<Entry<RightTriangleInfo[], int[]>> infosQueue = new ArrayList<>();
            /**
             * reuses the information stored in the previous step, copies them
             * into a list
             */
            for (ArrayList<Entry<RightTriangleInfo[], int[]>> commonSidesInfos : commonSidesInfosMap.values()) {
                for (Entry<RightTriangleInfo[], int[]> commonSideInfo : commonSidesInfos)
                    if (trisList.contains(commonSideInfo.getKey()[0]) || trisList.contains(commonSideInfo.getKey()[1]))
                        infosQueue.add(commonSideInfo);
            }
            int infosQueueIndex = 0;
            // loops while this list is not empty
            while (/* !infosQueue.isEmpty() */arrayMap.size() < trisList.size()) {
                boolean inserted = false;
                // gets the information from the list
                final Entry<RightTriangleInfo[], int[]> info = infosQueue.get(infosQueueIndex);
                final RightTriangleInfo[] tris = info.getKey();
                final int[] commonSidesIndices = info.getValue();
                // if the array already contains the first triangle
                if (arrayMap.containsKey(tris[0])) {// if the array already
                                                    // contains the second
                                                    // triangle
                    if (arrayMap.containsKey(tris[1]))
                        inserted = true;
                    else {// retrieves the indices of the triangle in the 2D
                          // array
                        final int[] arrayIndices = new int[] { arrayMap.get(tris[0])[0], arrayMap.get(tris[0])[1] };
                        // finds which sides are common updates the array
                        final int tri1index = trisList.indexOf(tris[1]);
                        if (tri1index != -1) {
                            if (tri1index % 2 == 0) {
                                if (commonSidesIndices[2] == (tris[0].sideIndexOfHypotenuse + 1) % 3) {// to
                                                                                                       // right
                                    arrayIndices[0]++;
                                } else {// to bottom
                                    arrayIndices[1]++;
                                }
                                adjacentTrisArray[arrayIndices[0]][arrayIndices[1]] = new RightTriangleInfo[] { tris[1],
                                        trisList.get(tri1index + 1) };
                            } else {
                                if (commonSidesIndices[2] == (tris[0].sideIndexOfHypotenuse + 1) % 3) {// to
                                                                                                       // left
                                    arrayIndices[0]--;
                                } else {// to top
                                    arrayIndices[1]--;
                                }
                                adjacentTrisArray[arrayIndices[0]][arrayIndices[1]] = new RightTriangleInfo[] {
                                        trisList.get(tri1index - 1), tris[1] };
                            }
                            // updates the map as tris[1] has been found
                            arrayMap.put(tris[1], arrayIndices);
                            inserted = true;
                        }
                    }
                } else {// if the array already contains the second triangle
                    if (arrayMap.containsKey(tris[1])) {
                        // retrieves the indices of the triangle in the 2D array
                        final int[] arrayIndices = arrayMap.get(tris[1]);
                        // finds which sides are common and updates the array
                        final int tri0index = trisList.indexOf(tris[0]);
                        if (tri0index != -1) {
                            if (tri0index % 2 == 0) {
                                if (commonSidesIndices[2] == (tris[0].sideIndexOfHypotenuse + 1) % 3) {// to
                                                                                                       // left
                                    arrayIndices[0]--;
                                } else {// to top
                                    arrayIndices[1]--;
                                }
                                adjacentTrisArray[arrayIndices[0]][arrayIndices[1]] = new RightTriangleInfo[] { tris[0],
                                        trisList.get(tri0index + 1) };
                            } else {
                                if (commonSidesIndices[2] == (tris[0].sideIndexOfHypotenuse + 1) % 3) {// to
                                                                                                       // right
                                    arrayIndices[0]++;
                                } else {// to bottom
                                    arrayIndices[1]++;
                                }
                                adjacentTrisArray[arrayIndices[0]][arrayIndices[1]] = new RightTriangleInfo[] {
                                        trisList.get(tri0index - 1), tris[0] };
                            }
                            // updates the map as tris[0] has been found
                            arrayMap.put(tris[0], arrayIndices);
                            inserted = true;
                        }
                    } else
                        inserted = false;
                }
                if (inserted) {// removes the information we used
                    /*
                     * infosQueue.remove(infosQueueIndex); //resets the index if
                     * it is out of the bounds
                     * if(infosQueueIndex==infosQueue.size()) infosQueueIndex=0;
                     */
                    final int tri0localIndex = trisList.indexOf(tris[0]);
                    if (tri0localIndex != -1) {
                        final int tri2localIndex;
                        if (tri0localIndex % 2 == 0)
                            tri2localIndex = tri0localIndex + 1;
                        else
                            tri2localIndex = tri0localIndex - 1;
                        final RightTriangleInfo tri = trisList.get(tri2localIndex);
                        if (!arrayMap.containsKey(tri))
                            arrayMap.put(tri, arrayMap.get(tris[0]));
                    }
                    final int tri1localIndex = trisList.indexOf(tris[1]);
                    if (tri1localIndex != -1) {
                        final int tri3localIndex;
                        if (tri1localIndex % 2 == 0)
                            tri3localIndex = tri1localIndex + 1;
                        else
                            tri3localIndex = tri1localIndex - 1;
                        final RightTriangleInfo tri = trisList.get(tri3localIndex);
                        if (!arrayMap.containsKey(tri))
                            arrayMap.put(tri, arrayMap.get(tris[1]));
                    }
                } else {// uses the next index, does not go out of the bounds
                    infosQueueIndex = (infosQueueIndex + 1) % infosQueue.size();
                }
            }
        }
        return (adjacentTrisArray);
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
    static java.util.Map<Vector2i, RightTriangleInfo[][][]> computeAdjacentMergeableTrisArraysMap(
            final RightTriangleInfo[][][] adjacentTrisArray) {
        return (new ArrayHelper().computeFullArraysFromNonFullArray(adjacentTrisArray));
    }
}
