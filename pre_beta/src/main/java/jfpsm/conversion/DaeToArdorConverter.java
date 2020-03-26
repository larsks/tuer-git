/**
 * Copyright (c) 2006-2020 Julien Gouesse
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
import com.ardor3d.extension.model.collada.jdom.ColladaImporter;
import com.ardor3d.image.util.jogl.JoglImageLoader;
import com.ardor3d.scenegraph.Node;
import com.ardor3d.util.export.binary.BinaryExporter;
import com.ardor3d.util.geom.GeometryTool;
import com.ardor3d.util.resource.ResourceLocatorTool;
import com.ardor3d.util.resource.ResourceSource;
import com.ardor3d.util.resource.SimpleResourceLocator;
import com.ardor3d.util.resource.URLResourceSource;

public final class DaeToArdorConverter {

    public static final void main(String[] args) throws IOException, URISyntaxException {
        JoglImageLoader.registerLoader();
        BinaryExporter binaryExporter = new BinaryExporter();
        try {
            SimpleResourceLocator srl = new SimpleResourceLocator(DaeToArdorConverter.class.getResource("/images"));
            ResourceLocatorTool.addResourceLocator(ResourceLocatorTool.TYPE_TEXTURE, srl);
            srl = new SimpleResourceLocator(DaeToArdorConverter.class.getResource("/dae"));
            ResourceLocatorTool.addResourceLocator(ResourceLocatorTool.TYPE_MODEL, srl);
        } catch (final URISyntaxException urise) {
            urise.printStackTrace();
        }
        ColladaImporter colladaImporter = new ColladaImporter();
        final GeometryTool geomTool = new GeometryTool(true);
        for (String arg : args) {
            System.out.println("Loading " + arg + " ...");
            final ResourceSource argSrc = ResourceLocatorTool.locateResource(ResourceLocatorTool.TYPE_MODEL, arg);
            final Node colladaNode = colladaImporter.load(argSrc, geomTool).getScene();
            URLResourceSource source = (URLResourceSource) ResourceLocatorTool
                    .locateResource(ResourceLocatorTool.TYPE_MODEL, arg);
            File sourceFile = new File(source.getURL().toURI());
            File destFile = new File(
                    sourceFile.getAbsolutePath().substring(0, sourceFile.getAbsolutePath().lastIndexOf(".dae"))
                            + ".abin");
            if (!destFile.exists())
                if (!destFile.createNewFile()) {
                    System.out.println(destFile.getAbsolutePath() + " cannot be created!");
                    continue;
                }
            System.out.println("Converting " + arg + " ...");
            binaryExporter.save(colladaNode, destFile);
            System.out.println(arg + " successfully converted");
        }
    }
}
