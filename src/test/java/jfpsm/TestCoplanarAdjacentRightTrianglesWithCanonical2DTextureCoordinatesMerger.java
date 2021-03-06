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

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Paths;

import com.ardor3d.image.util.jogl.JoglImageLoader;
import com.ardor3d.scenegraph.Mesh;
import com.ardor3d.scenegraph.Node;
import com.ardor3d.scenegraph.Spatial;
import com.ardor3d.util.export.binary.BinaryImporter;
import com.ardor3d.util.resource.ResourceLocatorTool;
import com.ardor3d.util.resource.SimpleResourceLocator;

import jfpsm.ArrayHelper.Vector2i;
import jfpsm.CoplanarAdjacentRightTrianglesWithCanonical2DTextureCoordinatesMerger.TriangleInfo;
import jfpsm.service.EngineServiceProvider;

/**
 * Test of a mesh optimizer focused on coplanar adjacent right triangles whose
 * all 2D texture coordinates are canonical ([0;0], [0;1], [1;0] or [1;1]).
 * 
 * @author Julien Gouesse
 *
 */
public class TestCoplanarAdjacentRightTrianglesWithCanonical2DTextureCoordinatesMerger {
    
    private static void testComputeAdjacentMergeableTrisArraysList() {
        final TriangleInfo info = new TriangleInfo(0, 0, null);
        final TriangleInfo[][][] adjacentTrisArray = new TriangleInfo[][][] {
                new TriangleInfo[][] { null, null, null, null, null, null, null, null, null },
                new TriangleInfo[][] { null, null, null, null, new TriangleInfo[] { info, info }, null, null,
                        null, null },
                new TriangleInfo[][] { null, null, new TriangleInfo[] { info, info },
                        new TriangleInfo[] { info, info }, new TriangleInfo[] { info, info },
                        new TriangleInfo[] { info, info }, new TriangleInfo[] { info, info },
                        new TriangleInfo[] { info, info }, new TriangleInfo[] { info, info } },
                new TriangleInfo[][] { null, null, null, new TriangleInfo[] { info, info },
                        new TriangleInfo[] { info, info }, new TriangleInfo[] { info, info },
                        new TriangleInfo[] { info, info }, null, null },
                new TriangleInfo[][] { null, null, new TriangleInfo[] { info, info },
                        new TriangleInfo[] { info, info }, new TriangleInfo[] { info, info },
                        new TriangleInfo[] { info, info }, null, null, null },
                new TriangleInfo[][] { null, null, new TriangleInfo[] { info, info },
                        new TriangleInfo[] { info, info }, new TriangleInfo[] { info, info },
                        new TriangleInfo[] { info, info }, null, null, null },
                new TriangleInfo[][] { null, null, null, new TriangleInfo[] { info, info }, null, null, null,
                        null, null },
                new TriangleInfo[][] { null, null, null, null, null, null, null, null, null } };
        System.out.println("Input:");
        final ArrayHelper arrayHelper = new ArrayHelper();
        final ArrayHelper.OccupancyCheck<TriangleInfo[]> trisOccupancyCheck = new ArrayHelper.OccupancyCheck<>() {

            @Override
            public boolean isOccupied(TriangleInfo[] value) {
                return (value != null && value[0] != null && value[1] != null);
            }

        };
        System.out.println(arrayHelper.toString(adjacentTrisArray, false, trisOccupancyCheck));
        java.util.Map<Vector2i, TriangleInfo[][][]> adjacentTrisArraysMap = new ArrayHelper().computeFullArraysFromNonFullArray(adjacentTrisArray);
        System.out.println("Output:");
        System.out.println(arrayHelper.toString(adjacentTrisArraysMap));
    }

    private static void testOptimize() {
        JoglImageLoader.registerLoader();
        try {
            SimpleResourceLocator srl = new SimpleResourceLocator(
                    TestCoplanarAdjacentRightTrianglesWithCanonical2DTextureCoordinatesMerger.class
                            .getResource("/images"));
            ResourceLocatorTool.addResourceLocator(ResourceLocatorTool.TYPE_TEXTURE, srl);
        } catch (final URISyntaxException urise) {
            urise.printStackTrace();
        }
        try {
            final Node levelNode = (Node) new BinaryImporter()
                    .load(TestCoplanarAdjacentRightTrianglesWithCanonical2DTextureCoordinatesMerger.class.getResource("/abin/LID0.abin"));
            for (final Spatial child : ((Node) levelNode.getChild(0)).getChildren()) {
                if (child instanceof Mesh) {
                    final Mesh mesh = (Mesh) child;
                    System.out.println("Input, vertex count: " + mesh.getMeshData().getVertexCount());
                    CoplanarAdjacentRightTrianglesWithCanonical2DTextureCoordinatesMerger.optimize(mesh);
                    System.out.println("Output, vertex count: " + mesh.getMeshData().getVertexCount());
                }
            }
            final File destFile = new File(Paths.get(TestCoplanarAdjacentRightTrianglesWithCanonical2DTextureCoordinatesMerger.class.getResource("/abin/LID0.abin").toURI()).toFile().getParentFile().getParentFile().getParentFile().getParentFile(), "src/main/resources/abin/LID0.abin");
            new EngineServiceProvider.DirectBinaryExporter().save(levelNode, destFile);
        } catch (IOException|URISyntaxException e) {
            throw new RuntimeException("level loading failed", e);
        }
    }

    public static void main(String[] args) {
        testComputeAdjacentMergeableTrisArraysList();
        testOptimize();
    }

}
