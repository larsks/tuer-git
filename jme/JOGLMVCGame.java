package jme;

import java.awt.Component;
import java.awt.Frame;
import java.awt.Toolkit;
import java.util.concurrent.Callable;
import java.util.logging.Logger;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.glu.GLU;
import main.ConfigurationDetector;
import com.jme.input.KeyInput;
import com.jme.input.MouseInput;
import com.jme.input.joystick.JoystickInput;
import com.jme.system.DisplaySystem;
import com.jme.system.canvas.JMECanvas;
import com.jme.system.canvas.JMECanvasImplementor;
import com.jme.system.jogl.JOGLSystemProvider;
import com.jme.util.GameTaskQueue;
import com.jme.util.GameTaskQueueManager;
import com.jme.util.NanoTimer;
import com.jme.util.TextureManager;
import com.jme.util.Timer;
import com.jmex.audio.AudioSystem;
import com.jmex.awt.jogl.JOGLAWTCanvasConstructor;
import com.jmex.game.state.GameStateManager;
import com.sun.opengl.util.Animator;

public final class JOGLMVCGame{  

    private static final Logger logger = Logger.getLogger(JOGLMVCGame.class.getName());
    
    private Animator animator;
    
    private ConfigurationDetector configurationDetector;
    
    public JOGLMVCGame(){
        //initialize game state machine
        GameStateManager.create();
        //force JOGL/AWT parameters
        KeyInput.setProvider(KeyInput.INPUT_AWT);
        MouseInput.setProvider(MouseInput.INPUT_AWT);
        DisplaySystem ds = DisplaySystem.getDisplaySystem(JOGLSystemProvider.SYSTEM_IDENTIFIER);
        ds.registerCanvasConstructor("AWT", JOGLAWTCanvasConstructor.class);
        Toolkit toolkit=Toolkit.getDefaultToolkit();
        int width=toolkit.getScreenSize().width;
        int height=toolkit.getScreenSize().height;
        final JMECanvas jmeCanvas=ds.createCanvas(width, height,"AWT",null);       
        jmeCanvas.setUpdateInput(true);
        //jmeCanvas.setTargetRate(60);
        final Frame frame = new Frame();
        frame.setAlwaysOnTop(true);
        frame.setIgnoreRepaint(true);
        frame.setUndecorated(true);
        frame.add((Component) jmeCanvas);
        frame.pack();
        //the implementor mainly delegates lots of work to the states
        GenericImplementor impl = new GenericImplementor(width, height);
        jmeCanvas.setImplementor(impl);
        //the animator updates the display
        animator=new Animator((GLAutoDrawable) jmeCanvas);
        animator.start();
        //The frame is ready to be shown
        frame.setVisible(true);
        //kludge to force the creation of the OpenGL context      
        try{GameTaskQueueManager.getManager().getQueue(GameTaskQueue.RENDER).enqueue(new Callable<Object>(){
                @Override
                public Object call() throws Exception{
                    //load the configuration
                    configurationDetector=new ConfigurationDetector(GLU.getCurrentGL());
                    logger.info("JOGLMVCGame ready to be used, context initialized");
                    return(null);
                }}).get();
           } 
        catch(Exception e)
        {e.printStackTrace();}
        GameTaskQueueManager.getManager().getQueue(GameTaskQueue.RENDER).execute();
        /*new Thread(new Runnable(){
            @Override
            public void run(){
                while(true)
                    InputSystem.update();
                    ((GLAutoDrawable) jmeCanvas).display();
            }
        }).start();   */    
    }
    
    /**
     * This method must be called on the OpenGL thread
     */
    final synchronized void cleanup(){  
        //cleanup each state
        GameStateManager.getInstance().cleanup();
        //cleanup the JOGL renderer       
        DisplaySystem.getDisplaySystem().getRenderer().cleanup();    
        //cleanup the textures
        TextureManager.doTextureCleanup();
        TextureManager.clearCache();
        //cleanup the joystick handling
        JoystickInput.destroyIfInitalized();                 
        //cleanup the sound system
        if(AudioSystem.isCreated())
            AudioSystem.getSystem().cleanup();
        //finally close the display system
        DisplaySystem.getDisplaySystem().close();
        animator.stop();
        System.exit(0);
    }
    
    public final ConfigurationDetector getConfigurationDetector(){
        return(configurationDetector);
    }
    
    private static final class GenericImplementor extends JMECanvasImplementor{

        private Timer timer;

        private float tpf;
        
        private GenericImplementor(int width,int height){
            this.width=width;
            this.height=height;
        }
        
        @Override
        public final void doSetup(){
            renderer=DisplaySystem.getDisplaySystem().getRenderer();
            timer=new NanoTimer();
            setup=true;
        }
        
        @Override
        public final void doUpdate(){
            timer.update();
            tpf = timer.getTimePerFrame();
            GameStateManager.getInstance().update(tpf);
        }
        
        @Override
        public final void doRender(){
            renderer.clearBuffers();
            GameStateManager.getInstance().render(tpf);
            renderer.displayBackBuffer();
        }
    }
}
