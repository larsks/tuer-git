package jme;

import java.awt.Component;
import java.awt.DisplayMode;
import java.awt.Frame;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Toolkit;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashMap;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import javax.media.opengl.GLAutoDrawable;
import com.jme.bounding.BoundingBox;
import com.jme.image.Texture;
import com.jme.input.InputHandler;
import com.jme.math.FastMath;
import com.jme.math.Quaternion;
import com.jme.math.Vector3f;
import com.jme.renderer.Renderer;
import com.jme.scene.shape.Box;
import com.jme.scene.state.TextureState;
import com.jme.system.DisplaySystem;
import com.jme.system.PreferencesGameSettings;
import com.jme.system.canvas.JMECanvas;
import com.jme.system.canvas.SimpleCanvasImpl;
import com.jme.system.jogl.JOGLSystemProvider;
import com.jme.util.TextureManager;
import com.jme.util.resource.ResourceLocatorTool;
import com.jme.util.resource.SimpleResourceLocator;
import com.jmex.awt.jogl.JOGLAWTCanvasConstructor;
import com.jmex.game.StandardGame;
import com.jmex.game.StandardGame.GameType;
import com.jmex.game.state.GameState;
import com.jmex.game.state.GameStateManager;
import com.jmex.game.state.load.TransitionGameState;
import com.jmex.swt.lwjgl.LWJGLSWTConstants;
import com.sun.opengl.util.Animator;

/**
 * Test for JOGL AWT Canvas implementation. Based upon {@link JMESWTTest}.
 * 
 * @author Joshua Slack
 * @author Steve Vaughan
 * @see JMESWTTest
 */

public final class JMEGameServiceProvider {

    private static final Logger logger = Logger.getLogger(JMEGameServiceProvider.class
            .getName());
    
    private StandardGame game;    

    
    private JMEGameServiceProvider(){       
        PreferencesGameSettings pgs=new PreferencesGameSettings(Preferences.userRoot());
        pgs.setRenderer(JOGLSystemProvider.SYSTEM_IDENTIFIER);
        pgs.setMusic(false);
        pgs.setSFX(false);       
        pgs.setWidth(1280);
        pgs.setHeight(1024);
        pgs.setFullscreen(true);
        this.game=new StandardGame("TUER",GameType.GRAPHICAL,pgs);
        this.game.start();
        //TODO: prepare resource loading
        try{ResourceLocatorTool.addResourceLocator(ResourceLocatorTool.TYPE_TEXTURE,new SimpleResourceLocator(JMEGameServiceProvider.class.getClassLoader().getResource("pic256/")));
            /*ResourceLocatorTool.addResourceLocator(
                    ResourceLocatorTool.TYPE_SHADER,
                    new SimpleResourceLocator(JMEGameServiceProvider.class
                            .getClassLoader().getResource(
                            "com/jmedemos/stardust/data/shader/")));
            ResourceLocatorTool.addResourceLocator(
                    ResourceLocatorTool.TYPE_MODEL,
                    new SimpleResourceLocator(JMEGameServiceProvider.class
                            .getClassLoader().getResource(
                            "com/jmedemos/stardust/data/models/")));
            ResourceLocatorTool.addResourceLocator(
                    ResourceLocatorTool.TYPE_AUDIO,
                    new SimpleResourceLocator(JMEGameServiceProvider.class
                            .getClassLoader().getResource(
                            "com/jmedemos/stardust/data/sounds/")));*/
           } 
        catch(URISyntaxException urise) 
        {urise.printStackTrace();}

        //effectively localize the resource
        URL startingTextureURL=ResourceLocatorTool.locateResource(ResourceLocatorTool.TYPE_TEXTURE,"starting_screen_bis.png");
        TransitionGameState transitionGameState = new TransitionGameState(20,startingTextureURL);
        GameStateManager.getInstance().attachChild(transitionGameState);
        transitionGameState.setActive(true);
        transitionGameState.setProgress(0,"Initializing Game ...");
        DisplaySystem disp = DisplaySystem.getDisplaySystem(); 
        //TODO: use our own parameters
        //disp.getRenderer().getCamera().setFrustumPerspective( 45.0f,(float) disp.getWidth() / (float) disp.getHeight(), 1f,  );
        disp.getRenderer().getCamera().update();
        //NB: each state is responsible of loading its data and updating the progress
        transitionGameState.increment("Initializing GameState: Intro ...");
        //GameStateManager.getInstance().attachChild(new IntroState("Intro",trans));
        transitionGameState.increment("Initializing GameState: Menu ...");       
        GameStateManager.getInstance().attachChild(new MenuState("Menu",transitionGameState,this));       
        transitionGameState.setProgress(1.0f, "Finished Loading");       
        //GameStateManager.getInstance().activateChildNamed("Intro");
        //At the end of the introduction (that might be skipped), display the menu
        //GameStateManager.getInstance().activateChildNamed("Menu");
    }
    
    public final GameState getLevelGameState(int index){
        //It is better to rebuild the game state for each level
        //each time to avoid filling the whole memory.
        //However, common objects are loaded once.        
        URL startingTextureURL=ResourceLocatorTool.locateResource(ResourceLocatorTool.TYPE_TEXTURE,"starting_screen_bis.png");
        TransitionGameState transitionGameState = new TransitionGameState(10,startingTextureURL);
        GameStateManager.getInstance().attachChild(transitionGameState);
        transitionGameState.setActive(true);
        transitionGameState.setProgress(0,"Initializing Level "+index+" ...");
        //TODO: the level factory loads the common data when used for the first
        //time and the data for a single level at each call
        //GameState levelGameState=LevelGameState.getInstance(index,transitionGameState);         
        transitionGameState.setProgress(1.0f, "Finished Loading");
        transitionGameState.setActive(false);
        GameStateManager.getInstance().detachChild(transitionGameState);       
        return(/*levelGameState*/null);
    }
    
    public final StandardGame getGame(){
        return(game);
    }
    
    public static final void main(String[] args){
        new JMEGameServiceProvider();
    }
    /*public static void main(String[] args) {
        DisplaySystem ds = DisplaySystem
                .getDisplaySystem(JOGLSystemProvider.SYSTEM_IDENTIFIER);
        // TODO Shouldn't this be automatic, determined by the SystemProvider?
        ds.registerCanvasConstructor("AWT", JOGLAWTCanvasConstructor.class);

        // TODO Shouldn't DEPTH_BITS be a part of the requested capabilities for
        // the display system? Why would this be specific to the canvas
        // instance?
        HashMap<String, Object> props = new HashMap<String, Object>();
        props.put(LWJGLSWTConstants.DEPTH_BITS, 8);
        Toolkit toolkit=Toolkit.getDefaultToolkit();
        int width=toolkit.getScreenSize().width;
        int height=toolkit.getScreenSize().height;
        // If I'm asking for a canvas, and canvases can be resized, then why
        // specify the width and height? Note the call to shell.setSize in the
        // JMESWTTest class.
        final JMECanvas jmeCanvas = ds
                .createCanvas(width, height, "AWT", props);
        jmeCanvas.setUpdateInput(true);
        jmeCanvas.setTargetRate(60);

        // XXX Note that the canvas can be added to the frame without any prior
        // interaction (such as parameter passing to createCanvas).
        final Frame frame = new Frame("jMonkey Engine JOGL AWT Canvas Test");
        frame.addWindowListener(new WindowAdapter(){
            public void windowClosing(WindowEvent e){
                System.exit(0);
            }
        });
        frame.setAlwaysOnTop(true);
        frame.setIgnoreRepaint(true);
        frame.setUndecorated(true);
        frame.add((Component) jmeCanvas);
        frame.pack();

        // TODO Are we required to use the JMonkey Engine input methods?
        // KeyInput.setProvider(AWTKeyInput.class.getCanonicalName());
        // canvas.addKeyListener((SWTKeyInput) KeyInput.get());

        // TODO Are we required to use the JMonkey Engine input methods?
        // SWTMouseInput.setup(canvas, true);

        // Important! Here is where we add the guts to the panel:
        MyImplementor impl = new MyImplementor(width, height);
        jmeCanvas.setImplementor(impl);

        // TODO Remove when complete (original SWT code).
        // shell.setText("SWT/JME Example");
        // shell.setSize(width, height);
        // shell.open();

        // TODO Remove when complete (original SWT code).
        // canvas.init();
        // canvas.render();

        // FIXME Encapsulate this within the canvas in some fashion?
        Animator animator = new Animator((GLAutoDrawable) jmeCanvas);
        animator.start();

        frame.setVisible(true);

        // TODO Remove when complete (original SWT code).
        // while (!shell.isDisposed()) {
        // if (!display.readAndDispatch())
        // display.sleep();
        // }

        // FIXME Where does this go?
        // display.dispose();
    }*/

    /*static class MyImplementor extends SimpleCanvasImpl {

        private Quaternion rotQuat;

        private float angle = 0;

        private Vector3f axis;

        private Box box;

        long startTime = 0;

        long fps = 0;

        private InputHandler input;

        public MyImplementor(int width, int height) {
            super(width, height);
        }

        public void simpleSetup() {
            // Normal Scene setup stuff...
            rotQuat = new Quaternion();
            axis = new Vector3f(0, 1, 0);
            axis.normalizeLocal();

            Vector3f max = new Vector3f(5, 5, 5);
            Vector3f min = new Vector3f(-5, -5, -5);

            box = new Box("Box", min, max);
            box.setModelBound(new BoundingBox());
            box.updateModelBound();
            box.setLocalTranslation(new Vector3f(0, 0, -10));
            box.setRenderQueueMode(Renderer.QUEUE_SKIP);
            rootNode.attachChild(box);

            TextureState ts = renderer.createTextureState();
            ts.setEnabled(true);
            ts.setTexture(TextureManager.loadTexture(JMEGameServiceProvider.class
                    .getClassLoader().getResource(
                            "pic256/starting_screen_bis.png"),
                    Texture.MinificationFilter.BilinearNoMipMaps,
                    Texture.MagnificationFilter.Bilinear));

            rootNode.setRenderState(ts);
            startTime = System.currentTimeMillis() + 5000;

            //input = new FirstPersonHandler(cam, 50, 1);
            
        }

        public void simpleUpdate() {
            input.update(tpf);

            // Code for rotating the box... no surprises here.
            if (tpf < 1) {
                angle = angle + (tpf * 25);
                if (angle > 360) {
                    angle = 0;
                }
            }
            rotQuat.fromAngleNormalAxis(angle * FastMath.DEG_TO_RAD, axis);
            box.setLocalRotation(rotQuat);

            if (startTime > System.currentTimeMillis()) {
                fps++;
            } else {
                long timeUsed = 5000 + (startTime - System.currentTimeMillis());
                startTime = System.currentTimeMillis() + 5000;
                logger.info(fps + " frames in " + (timeUsed / 1000f)
                        + " seconds = " + (fps / (timeUsed / 1000f))
                        + " FPS (average)");
                fps = 0;
            }
        }
    }*/

}

