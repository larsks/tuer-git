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
package engine.service;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.Properties;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLRunnable;
import com.ardor3d.annotation.MainThread;
import com.ardor3d.framework.Canvas;
import com.ardor3d.framework.CanvasRenderer;
import com.ardor3d.framework.DisplaySettings;
import com.ardor3d.framework.NativeCanvas;
import com.ardor3d.framework.Scene;
import com.ardor3d.framework.jogl.JoglCanvasRenderer;
import com.ardor3d.framework.jogl.JoglNewtWindow;
import com.ardor3d.image.util.jogl.JoglImageLoader;
import com.ardor3d.input.GrabbedState;
import com.ardor3d.input.MouseManager;
import com.ardor3d.input.PhysicalLayer;
import com.ardor3d.input.jogl.JoglNewtFocusWrapper;
import com.ardor3d.input.jogl.JoglNewtKeyboardWrapper;
import com.ardor3d.input.jogl.JoglNewtMouseManager;
import com.ardor3d.input.jogl.JoglNewtMouseWrapper;
import com.ardor3d.input.logical.TriggerAction;
import com.ardor3d.input.logical.TwoInputStates;
import com.ardor3d.intersection.PickResults;
import com.ardor3d.math.Ray3;
import com.ardor3d.renderer.Renderer;
import com.ardor3d.renderer.state.ZBufferState;
import com.ardor3d.scenegraph.Node;
import com.ardor3d.util.GameTaskQueue;
import com.ardor3d.util.GameTaskQueueManager;
import com.ardor3d.util.ReadOnlyTimer;
import com.ardor3d.util.Timer;
import com.ardor3d.util.resource.ResourceLocatorTool;
import com.ardor3d.util.resource.SimpleResourceLocator;
import com.jogamp.newt.Screen;
import com.jogamp.newt.event.WindowAdapter;
import com.jogamp.newt.event.WindowEvent;
import com.jogamp.newt.opengl.GLWindow;
import engine.integration.DesktopIntegration;
import engine.integration.DesktopIntegration.OS;
import engine.misc.LocalizedMessageProvider;
import engine.misc.SettingsProvider;
import engine.renderer.ReliableCanvasRenderer;
import engine.statemachine.ScenegraphStateMachine;

/**
 * General entry point of a game created by the editor
 * 
 * @author Julien Gouesse
 * 
 */
public final class Ardor3DGameServiceProvider implements Scene {

    /** path of the branding property file */
    private final String brandingPropertyFilePath;

    /** storage of branding properties */
    private Properties brandingProperties;

    /** short name of the game */
    private final String gameShortName;

    private final SettingsProvider settingsProvider;

    /** provider of localized messages */
    private final LocalizedMessageProvider localizedMessageProvider;

    /** full name of the game */
    private final String gameLongName;

    /** subtitle displayed in the introduction of the game */
    private final String gameIntroductionSubtitle;

    /** recommended URL to download the game */
    private final String gameRecommendedDownloadUrl;

    /** game title, visible only in windowed mode */
    private final String gameTitle;

    /** native window, not the GL surface itself */
    private final NativeCanvas canvas;

    /** physical layer of the input system */
    private final PhysicalLayer physicalLayer;

    /** mouse manager used to handle the cursor */
    private final MouseManager mouseManager;

    /** main timer */
    private final Timer timer;

    /** root of our scene */
    private final Node root;

    /** state machine of the scenegraph */
    private ScenegraphStateMachine scenegraphStateMachine;

    /**
     * Main method to run the game
     * 
     * @param args
     */
    public static void main(final String[] args) {
        // there is no need of hardware acceleration for Java2D as I don't use
        // it anymore
        System.setProperty("sun.java2d.opengl", "false");
        // disables OpenGL-ES
        System.setProperty("jogl.disable.opengles", "true");
        if (DesktopIntegration.getOperatingSystem().equals(OS.Windows)) {// Windows-specific
                                                                         // workarounds
            /**
             * Forces the use of the high precision timer on Windows. See
             * http://bugs.sun.com/view_bug.do?bug_id=6435126
             */
            new Thread(new Runnable() {
                @Override
                public void run() {
                    while (true) {
                        try {
                            Thread.sleep(Integer.MAX_VALUE);
                        } catch (InterruptedException ie) {
                            ie.printStackTrace();
                        }
                    }
                }
            }, "Microsoft Windows Sleeper (see bug 6435126)") {
                {
                    this.setDaemon(true);
                }
            }.start();
            // sets the default maximum direct memory size (to work around a
            // regression in Java 1.7 u9 and Java 1.6 u37 in JNLP parsing)
            System.setProperty("-XX:MaxDirectMemorySize", "128M");
            System.setProperty("sun.nio.MaxDirectMemorySize", "128M");
            // disables DirectDraw under Windows in order to avoid conflicts
            // with OpenGL
            System.setProperty("sun.java2d.noddraw", "true");
            // disables Direct3D under Windows in order to avoid conflicts with
            // OpenGL
            System.setProperty("sun.java2d.d3d", "false");
        }
        final Ardor3DGameServiceProvider application = new Ardor3DGameServiceProvider();
        application.start();
    }

    private abstract static class DesktopShortcutCreationRunnable implements Runnable {

        protected final String filenameWithoutExtension;

        protected final String alternativeCommand;

        private DesktopShortcutCreationRunnable(final String filenameWithoutExtension,
                final String alternativeCommand) {
            this.filenameWithoutExtension = filenameWithoutExtension;
            this.alternativeCommand = alternativeCommand;
        }
    }

    private static final class LaunchDesktopShortcutCreationRunnable extends DesktopShortcutCreationRunnable {

        private LaunchDesktopShortcutCreationRunnable(final String filenameWithoutExtension,
                final String alternativeCommand) {
            super(filenameWithoutExtension, alternativeCommand);
        }

        @Override
        public void run() {
            DesktopIntegration.createLaunchDesktopShortcut(filenameWithoutExtension, alternativeCommand);
        }
    }

    private static final class UninstallDesktopShortcutCreationRunnable extends DesktopShortcutCreationRunnable {

        private UninstallDesktopShortcutCreationRunnable(final String filenameWithoutExtension,
                final String alternativeCommand) {
            super(filenameWithoutExtension, alternativeCommand);
        }

        @Override
        public void run() {
            DesktopIntegration.createUninstallDesktopShortcut(filenameWithoutExtension, alternativeCommand);
        }
    }

    private static final class ToggleScreenModeAction implements TriggerAction {

        private static GLRunnable resizer = new GLRunnable() {
            @Override
            public boolean run(GLAutoDrawable glAutoDrawable) {
                final GLWindow window = (GLWindow) glAutoDrawable;
                final Screen screen = window.getScreen();
                final int screenWidth = screen.getWidth();
                final int screenHeight = screen.getHeight();
                final int topLevelWidth, topLevelHeight;
                if (window.isFullscreen()) {
                    topLevelWidth = window.getWidth();
                    topLevelHeight = window.getHeight();
                } else {
                    topLevelWidth = window.getWidth() + window.getInsets().getTotalWidth();
                    topLevelHeight = window.getHeight() + window.getInsets().getTopHeight();
                }
                if (topLevelWidth != screenWidth || topLevelHeight != screenHeight) {// modifies
                                                                                     // the
                                                                                     // size
                                                                                     // of
                                                                                     // the
                                                                                     // window
                                                                                     // only
                                                                                     // when
                                                                                     // it
                                                                                     // is
                                                                                     // necessary
                    window.setTopLevelSize(screenWidth, screenHeight);
                }
                return true;
            }
        };

        @Override
        @MainThread
        public void perform(Canvas source, TwoInputStates inputStates, double tpf) {
            final JoglNewtWindow joglNewtWindow = (JoglNewtWindow) source;
            final GLWindow glWindow = joglNewtWindow.getNewtWindow();
            final boolean fullscreenOn = glWindow.isFullscreen();
            final int screenWidth = glWindow.getScreen().getWidth();
            final int screenHeight = glWindow.getScreen().getHeight();
            glWindow.setFullscreen(!fullscreenOn);
            glWindow.setUndecorated(!fullscreenOn);
            glWindow.setTopLevelSize(screenWidth, screenHeight);
            glWindow.setTopLevelPosition(0, 0);
            /**
             * The very first switch to another mode just after a modification
             * of the resolution may cause a reset to the previous one (which is
             * the default behavior). The desired size must be set later in
             * order to override this mechanism.
             */
            glWindow.invoke(false, resizer);
        }

    }

    /**
     * Constructs an instance of this class, also creating the native window and
     * GL surface.
     */
    private Ardor3DGameServiceProvider() {
        super();
        // branding stuff, naming, settings, internationalization (i18n)
        brandingPropertyFilePath = "/branding.properties";
        brandingProperties = new Properties();
        try (InputStream stream = Ardor3DGameServiceProvider.class.getResourceAsStream(brandingPropertyFilePath)) {
            brandingProperties.load(stream);
        } catch (IOException ioe) {
            throw new RuntimeException("Failed in loading the property file " + brandingPropertyFilePath, ioe);
        }
        gameShortName = getPropertyValue("game-short-name");
        settingsProvider = new SettingsProvider(gameShortName);
        settingsProvider.load();
        localizedMessageProvider = new LocalizedMessageProvider(settingsProvider.getLocale());
        gameLongName = getPropertyValue("game-long-name");
        gameIntroductionSubtitle = localizedMessageProvider.getString("END_OF_THE_INTERNATIONALE_COMMUNIST_ANTHEM");
        gameRecommendedDownloadUrl = getPropertyValue("game-recommended-download-url");
        gameTitle = gameShortName + ": " + gameLongName;
        // scene stuff
        timer = new Timer();
        root = new Node("root node of the game");
        final int primaryMonitorRequestedWidth = settingsProvider.getScreenWidth();
        final int primaryMonitorRequestedHeight = settingsProvider.getScreenHeight();
        final int primaryMonitorRequestedRotation = settingsProvider.getScreenRotation();
        /**
         * do not use 32 bits for the depth buffer as 24 bits are enough and
         * forcing 32 bits might lead to pick a slow software renderer with a
         * bad support of OpenGL
         */
        final int depthBits = 24;
        final boolean fullscreen = settingsProvider.isFullscreenEnabled();
        final DisplaySettings settings = new DisplaySettings(primaryMonitorRequestedWidth,
                primaryMonitorRequestedHeight, 0, 0, 0, depthBits, 0, 0, fullscreen, false, null,
                primaryMonitorRequestedRotation);
        // setups the canvas renderer
        final JoglCanvasRenderer canvasRenderer = new ReliableCanvasRenderer(this);
        // creates a canvas
        canvas = new JoglNewtWindow(canvasRenderer, settings);
        canvas.init();
        // enables or disables the vertical synchronization
        final boolean verticalSynchronizationEnabled = settingsProvider.isVerticalSynchronizationEnabled();
        canvas.setVSyncEnabled(verticalSynchronizationEnabled);
        mouseManager = new JoglNewtMouseManager((JoglNewtWindow) canvas);
        // removes the mouse cursor
        mouseManager.setGrabbed(GrabbedState.GRABBED);
        final JoglNewtKeyboardWrapper keyboardWrapper = new JoglNewtKeyboardWrapper((JoglNewtWindow) canvas);
        keyboardWrapper.setSkipAutoRepeatEvents(true);
        final JoglNewtMouseWrapper mouseWrapper = new JoglNewtMouseWrapper((JoglNewtWindow) canvas, mouseManager);
        mouseWrapper.setSkipAutoRepeatEvents(true);
        final JoglNewtFocusWrapper focusWrapper = new JoglNewtFocusWrapper((JoglNewtWindow) canvas);
        physicalLayer = new PhysicalLayer(keyboardWrapper, mouseWrapper, focusWrapper);
    }

    /**
     * Kicks off the logic, first setting up the scene, then continuously
     * updating and rendering it until the exit state is used. Afterwards, the
     * scene and GL surface are cleaned up.
     */
    private final void start() {
        init();

        // runs in this same thread
        while (!canvas.isClosing()) {
            timer.update();
            updateLogicalLayer(timer);
            // updates controllers/render states/transforms/bounds for rootNode
            root.updateGeometricState(timer.getTimePerFrame(), true);
            canvas.draw(null);
            // Thread.yield();
        }
    }

    /**
     * Initializes our scene.
     */
    private final void init() {
        canvas.setTitle(gameTitle);
        // refreshes the frustum when the window is resized
        ((JoglNewtWindow) canvas).addWindowListener(new WindowAdapter() {
            @Override
            public void windowResized(final WindowEvent e) {
                final GLWindow newtWindow = ((JoglNewtWindow) canvas).getNewtWindow();
                final CanvasRenderer canvasRenderer = canvas.getCanvasRenderer();
                newtWindow.invoke(true, new GLRunnable() {
                    @Override
                    public boolean run(GLAutoDrawable glAutoDrawable) {
                        canvasRenderer.getCamera().resize(newtWindow.getWidth(), newtWindow.getHeight());
                        canvasRenderer.getCamera().setFrustumPerspective(canvasRenderer.getCamera().getFovY(),
                                (float) newtWindow.getWidth() / (float) newtWindow.getHeight(),
                                canvasRenderer.getCamera().getFrustumNear(),
                                canvasRenderer.getCamera().getFrustumFar());
                        return true;
                    }
                });
            }
        });
        // disables vertical synchronization for tests
        canvas.setVSyncEnabled(false);
        // creates a ZBuffer to display pixels closest to the camera above
        // farther ones.
        final ZBufferState buf = new ZBufferState();
        buf.setEnabled(true);
        buf.setFunction(ZBufferState.TestFunction.LessThanOrEqualTo);
        root.setRenderState(buf);
        // adds our image loader.
        JoglImageLoader.registerLoader();
        // sets the default font
        // UIComponent.setDefaultFont(getFontsList().get(2));
        // sets the location of our resources.
        try {
            SimpleResourceLocator srl = new SimpleResourceLocator(getClass().getResource("/images"));
            ResourceLocatorTool.addResourceLocator(ResourceLocatorTool.TYPE_TEXTURE, srl);
        } catch (final URISyntaxException urise) {
            urise.printStackTrace();
        }
        final LaunchDesktopShortcutCreationRunnable launchRunnable;
        final UninstallDesktopShortcutCreationRunnable uninstallRunnable;
        if (DesktopIntegration.isDesktopShortcutCreationSupported() && !DesktopIntegration.useJNLP()) {
            launchRunnable = /*
                              * new
                              * LaunchDesktopShortcutCreationRunnable("????",
                              * "????")
                              */null;
            uninstallRunnable = null;
        } else {
            launchRunnable = null;
            uninstallRunnable = null;
        }
        final String readmeContent = localizedMessageProvider.getString("README_FULL_TEXT");
        final TriggerAction toggleScreenModeAction = new ToggleScreenModeAction();
        scenegraphStateMachine = new ScenegraphStateMachine(root, canvas, physicalLayer, mouseManager,
                toggleScreenModeAction, launchRunnable, uninstallRunnable, gameShortName, gameLongName,
                gameIntroductionSubtitle, gameRecommendedDownloadUrl, readmeContent, null, null, 0,
                localizedMessageProvider, settingsProvider);
    }

    private final void updateLogicalLayer(final ReadOnlyTimer timer) {
        // checks and executes any input triggers, if we are concerned with
        // input
        scenegraphStateMachine.updateLogicalLayer(timer);
    }

    @Override
    public final boolean renderUnto(final Renderer renderer) {
        final boolean isOpen = !canvas.isClosing();
        if (isOpen) {// draws the root and all its children
            renderer.draw(root);
            // executes all update tasks queued by the controllers
            GameTaskQueueManager.getManager(canvas.getCanvasRenderer().getRenderContext())
                    .getQueue(GameTaskQueue.UPDATE).execute(renderer);
            // executes all rendering tasks queued by the controllers
            GameTaskQueueManager.getManager(canvas.getCanvasRenderer().getRenderContext())
                    .getQueue(GameTaskQueue.RENDER).execute(renderer);
        }
        return (isOpen);
    }

    @Override
    public final PickResults doPick(final Ray3 pickRay) {
        // ignores
        return (null);
    }

    private final String getPropertyValue(final String propertyKey) {
        if (propertyKey == null)
            throw new IllegalArgumentException("Cannot find a property whose key is null");
        final String propertyValue = brandingProperties.getProperty(propertyKey);
        if (propertyValue == null)
            throw new RuntimeException("Property " + propertyKey + " not found");
        return (propertyValue);
    }
}