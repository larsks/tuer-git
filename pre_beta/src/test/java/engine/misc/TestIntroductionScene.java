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
package engine.misc;


import java.io.IOException;
import java.net.URISyntaxException;

import com.ardor3d.framework.CanvasRenderer;
import com.ardor3d.framework.DisplaySettings;
import com.ardor3d.framework.Scene;
import com.ardor3d.framework.jogl.JoglCanvasRenderer;
import com.ardor3d.framework.jogl.JoglNewtWindow;
import com.ardor3d.image.util.ImageLoaderUtil;
import com.ardor3d.image.util.jogl.JoglImageLoader;
import com.ardor3d.intersection.PickResults;
import com.ardor3d.math.Ray3;
import com.ardor3d.renderer.Renderer;
import com.ardor3d.renderer.state.ZBufferState;
import com.ardor3d.scenegraph.Node;
import com.ardor3d.scenegraph.extension.SwitchNode;
import com.ardor3d.util.ContextGarbageCollector;
import com.ardor3d.util.Timer;
import com.ardor3d.util.export.binary.BinaryImporter;
import com.ardor3d.util.resource.ResourceLocatorTool;
import com.ardor3d.util.resource.SimpleResourceLocator;
import com.jogamp.newt.event.WindowAdapter;
import com.jogamp.newt.event.WindowEvent;

public class TestIntroductionScene implements Scene {

    // Our native window, not the gl surface itself.
    private final JoglNewtWindow _canvas;

    // Our timer.
    private final Timer _timer = new Timer();

    // A boolean allowing us to "pull the plug" from anywhere.
    private boolean _exit = false;

    // The root of our scene
    private final Node _root = new Node();

    public static void main(final String[] args) {
        final TestIntroductionScene example = new TestIntroductionScene();
        example.start();
    }

    /**
     * Constructs the example class, also creating the native window and GL surface.
     */
    public TestIntroductionScene() {
        final JoglCanvasRenderer canvasRenderer = new JoglCanvasRenderer(this);
        final DisplaySettings settings = new DisplaySettings(800, 600, 24, 0, 0, 8, 0, 0, false, false);
        _canvas = new JoglNewtWindow(canvasRenderer, settings);
        _canvas.init();
    }

    /**
     * Kicks off the example logic, first setting up the scene, then continuously updating and rendering it until exit
     * is flagged. Afterwards, the scene and gl surface are cleaned up.
     */
    private void start() {
        _canvas.setTitle(" - close window to exit");
        
        final BinaryImporter importer = new BinaryImporter();
        try {
            SwitchNode introNode = (SwitchNode) importer.load(getClass().getResourceAsStream("/abin/introduction.abin"));
            //introNode.removeController(0);
            introNode.setTranslation(-0.5, -0.5, 8.5);
            _root.attachChild(introNode);
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }

        // Add our newt based image loader.
        JoglImageLoader.registerLoader();
        ImageLoaderUtil.registerDefaultHandler(new JoglImageLoader());

        // Set the location of our example resources.
        try {
            final SimpleResourceLocator srl = new SimpleResourceLocator(getClass().getResource("/images"));
            ResourceLocatorTool.addResourceLocator(ResourceLocatorTool.TYPE_TEXTURE, srl);
        } catch (final URISyntaxException ex) {
            ex.printStackTrace();
        }

        // Create a ZBuffer to display pixels closest to the camera above farther ones.
        final ZBufferState buf = new ZBufferState();
        buf.setEnabled(true);
        buf.setFunction(ZBufferState.TestFunction.LessThanOrEqualTo);
        _root.setRenderState(buf);

        _canvas.addWindowListener(new WindowAdapter() {
            @Override
            public void windowDestroyNotify(final WindowEvent e) {
                final CanvasRenderer cr = _canvas.getCanvasRenderer();
                cr.makeCurrentContext();
                // Done, do cleanup
                ContextGarbageCollector.doFinalCleanup(cr.getRenderer());
                cr.releaseCurrentContext();
            }
        });

        // Run in this same thread.
        while (!_exit) {
            if (_canvas.isClosing()) {
                _exit = true;
            } else {
                _timer.update();

                // Update controllers/render states/transforms/bounds for
                // rootNode.
                _root.updateGeometricState(_timer.getTimePerFrame(), true);
            }
            _canvas.draw(null);
            Thread.yield();
        }
    }

    // ------ Scene methods ------

    @Override
    public boolean renderUnto(final Renderer renderer) {
        if (!_canvas.isClosing()) {

            // Draw the root and all its children.
            renderer.draw(_root);

            return true;
        }
        return false;
    }

    @Override
    public PickResults doPick(final Ray3 pickRay) {
        // Ignore
        return null;
    }
}
