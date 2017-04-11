/**
 * Copyright (c) 2006-2017 Julien Gouesse
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
package jfpsm.conversion;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;

import com.ardor3d.extension.model.obj.ObjExporter;
import com.ardor3d.image.util.jogl.JoglImageLoader;
import com.ardor3d.scenegraph.Mesh;
import com.ardor3d.scenegraph.Node;
import com.ardor3d.scenegraph.Spatial;
import com.ardor3d.util.export.binary.BinaryImporter;
import com.ardor3d.util.resource.ResourceLocatorTool;
import com.ardor3d.util.resource.SimpleResourceLocator;
import com.ardor3d.util.resource.URLResourceSource;

public class ArdorToObjConverter {

    public ArdorToObjConverter() {
        super();
    }

    public void run(final String[] args) throws IOException, URISyntaxException {
        JoglImageLoader.registerLoader();
        try {
            SimpleResourceLocator srl = new SimpleResourceLocator(ArdorToObjConverter.class.getResource("/images"));
            ResourceLocatorTool.addResourceLocator(ResourceLocatorTool.TYPE_TEXTURE, srl);
            srl = new SimpleResourceLocator(ArdorToObjConverter.class.getResource("/abin"));
            ResourceLocatorTool.addResourceLocator(ResourceLocatorTool.TYPE_MODEL, srl);
        } catch (final URISyntaxException urise) {
            urise.printStackTrace();
        }
        final ObjExporter objExporter = new ObjExporter();
        final BinaryImporter binaryImporter = new BinaryImporter();
        Spatial binarySpatial;
        for (String arg : args) {
            System.out.println("Loading " + arg + " ...");
            URLResourceSource source = (URLResourceSource) ResourceLocatorTool
                    .locateResource(ResourceLocatorTool.TYPE_MODEL, arg);
            File sourceFile = new File(source.getURL().toURI());
            binarySpatial = (Spatial) binaryImporter.load(sourceFile);
            final String filenameWithoutExtension = sourceFile.getAbsolutePath().substring(0,
                    sourceFile.getAbsolutePath().lastIndexOf(".abin"));
            File objDestFile = new File(filenameWithoutExtension + ".obj");
            File mtlDestFile = new File(filenameWithoutExtension + ".mtl");
            if (!objDestFile.exists())
                if (!objDestFile.createNewFile()) {
                    System.out.println(objDestFile.getAbsolutePath() + " cannot be created!");
                    continue;
                }
            if (!mtlDestFile.exists())
                if (!mtlDestFile.createNewFile()) {
                    System.out.println(mtlDestFile.getAbsolutePath() + " cannot be created!");
                    continue;
                }
            System.out.println("Converting " + arg + " ...");
            if (binarySpatial instanceof Mesh) {
                final Mesh mesh = (Mesh) binarySpatial;
                objExporter.save(mesh, objDestFile, mtlDestFile);
            } else if (binarySpatial instanceof Node) {
                final Node node = (Node) binarySpatial;
                ArrayList<Mesh> meshes = new ArrayList<>();
                for (Spatial child : node.getChildren())
                    if (child instanceof Mesh)
                        meshes.add((Mesh) child);
                objExporter.save(meshes, objDestFile, mtlDestFile, null);
            }
            System.out.println(arg + " successfully converted");
        }
    }

    public static final void main(final String[] args) {
        try {
            final ArdorToObjConverter converter = new ArdorToObjConverter();
            converter.run(args);
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }
}
