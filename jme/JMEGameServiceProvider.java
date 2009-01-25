package jme;

import java.net.URISyntaxException;
import java.net.URL;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.logging.Logger;
import main.ConfigurationDetector;
import com.jme.renderer.Camera;
import com.jme.system.DisplaySystem;
import com.jme.util.GameTaskQueue;
import com.jme.util.GameTaskQueueManager;
import com.jme.util.resource.ResourceLocatorTool;
import com.jme.util.resource.SimpleResourceLocator;
import com.jmex.game.state.GameState;
import com.jmex.game.state.GameStateManager;
import com.jmex.game.state.load.TransitionGameState;

/**
 * 
 * @author Julien Gouesse
 *
 */
public final class JMEGameServiceProvider {

    private static final Logger logger = Logger.getLogger(JMEGameServiceProvider.class.getName());
    
    private JOGLMVCGame game;
    
    private Camera cam;
    
    private JMEGameServiceProvider(){
        this.game=new JOGLMVCGame();
        logger.info("JOGLMVCGame created, creating states...");
        try{ResourceLocatorTool.addResourceLocator(ResourceLocatorTool.TYPE_TEXTURE,new SimpleResourceLocator(JMEGameServiceProvider.class.getResource("/texture/")));} 
        catch(URISyntaxException urise) 
        {urise.printStackTrace();}
        //effectively localize the resource
        URL startingTextureURL=ResourceLocatorTool.locateResource(ResourceLocatorTool.TYPE_TEXTURE,"starting_screen_bis.png");
        TransitionGameState transitionGameState = new TransitionGameState(20,startingTextureURL);
        GameStateManager.getInstance().attachChild(transitionGameState);
        transitionGameState.setActive(true);
        transitionGameState.setProgress(0,"Initializing Game ...");
        DisplaySystem disp=DisplaySystem.getDisplaySystem(); 
        //TODO: use our own parameters
        cam=disp.getRenderer().getCamera();
        cam.setFrustumPerspective( 45.0f,(float) disp.getWidth() / (float) disp.getHeight(), 0.2F, 2000.0F );
        cam.update();
        //NB: each state is responsible of loading its data and updating the progress
        transitionGameState.increment("Initializing GameState: Intro ...");
        //GameStateManager.getInstance().attachChild(new IntroState("Intro",trans));
        transitionGameState.increment("Initializing GameState: Menu ..."); 
        transitionGameState.setProgress(1.0f, "Finished Loading"); 
        GameStateManager.getInstance().attachChild(new MenuState("Main menu",transitionGameState,this,false));       

        //GameStateManager.getInstance().activateChildNamed("Intro");
        //At the end of the introduction (that might be skipped), display the menu
        GameStateManager.getInstance().activateChildNamed("Main menu");
    }
    
    public final ConfigurationDetector getConfigurationDetector(){
        return(game.getConfigurationDetector());
    }
    
    public static final void main(String[] args){
        new JMEGameServiceProvider();
    }
       
    public final GameState getLevelGameState(int index){
        //It is better to rebuild the game state for each level
        //each time to avoid filling the whole memory.
        //However, common objects are loaded once.        
        //URL startingTextureURL=ResourceLocatorTool.locateResource(ResourceLocatorTool.TYPE_TEXTURE,"starting_screen_bis.png");
        //TransitionGameState transitionGameState=new TransitionGameState(10,startingTextureURL);
        //GameStateManager.getInstance().attachChild(transitionGameState);
        //transitionGameState.setActive(true);
        //transitionGameState.setProgress(0,"Initializing Level "+index+" ...");
        //TODO: the level factory loads the common data when used for the first
        //time and the data for a single level at each call
        Future<GameState> task = GameTaskQueueManager.getManager().getQueue(GameTaskQueue.UPDATE).enqueue(new LevelLoadTask(index,/*transitionGameState,*/cam,this));
        //GameState levelGameState=LevelGameState.getInstance(index,transitionGameState,cam);         
        GameTaskQueueManager.getManager().getQueue(GameTaskQueue.UPDATE).execute();
        GameState levelGameState=null;
        try{levelGameState = task.get();} 
        catch(InterruptedException e)
        {e.printStackTrace();} 
        catch(ExecutionException e)
        {e.printStackTrace();}
        //transitionGameState.setProgress(1.0f, "Finished Loading");
        //transitionGameState.setActive(false);
        //GameStateManager.getInstance().detachChild(transitionGameState);       
        return(levelGameState);
    }
    
    private static final class LevelLoadTask implements Callable<GameState>{
        
        
        private int index;
        
        //private TransitionGameState transitionGameState;
        
        private Camera cam;
        
        private JMEGameServiceProvider gameServiceProvider;
        
        
        private LevelLoadTask(int index,/*TransitionGameState transitionGameState,*/
                Camera cam,JMEGameServiceProvider gameServiceProvider){
            this.index=index;
            //this.transitionGameState=transitionGameState;
            this.cam=cam;
            this.gameServiceProvider=gameServiceProvider;
        }
        
        @Override
        public GameState call()throws Exception{
            return(LevelGameState.getInstance(index/*,transitionGameState*/,cam,gameServiceProvider));
        }      
    }

    final void exit(){
        game.cleanup();
    }
    
    /*private static final Logger logger = Logger.getLogger(JMEGameServiceProvider.class
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
//      TODO: prepare resource loading
        try{ResourceLocatorTool.addResourceLocator(ResourceLocatorTool.TYPE_TEXTURE,new SimpleResourceLocator(JMEGameServiceProvider.class.getClassLoader().getResource("/texture/")));

        } 
        catch(URISyntaxException urise) 
        {urise.printStackTrace();}

//      effectively localize the resource
        URL startingTextureURL=ResourceLocatorTool.locateResource(ResourceLocatorTool.TYPE_TEXTURE,"starting_screen_bis.png");
        TransitionGameState transitionGameState = new TransitionGameState(20,startingTextureURL);
        GameStateManager.getInstance().attachChild(transitionGameState);
        transitionGameState.setActive(true);
        transitionGameState.setProgress(0,"Initializing Game ...");
        DisplaySystem disp = DisplaySystem.getDisplaySystem(); 
//      TODO: use our own parameters
        disp.getRenderer().getCamera().setFrustumPerspective( 45.0f,(float) disp.getWidth() / (float) disp.getHeight(), 1.0F, 10000.0F );
        disp.getRenderer().getCamera().update();
//      NB: each state is responsible of loading its data and updating the progress
        transitionGameState.increment("Initializing GameState: Intro ...");
//      GameStateManager.getInstance().attachChild(new IntroState("Intro",trans));
        transitionGameState.increment("Initializing GameState: Menu ..."); 
        transitionGameState.setProgress(1.0f, "Finished Loading"); 
        GameStateManager.getInstance().attachChild(new MenuState("Menu",transitionGameState,this));       

//      GameStateManager.getInstance().activateChildNamed("Intro");
//      At the end of the introduction (that might be skipped), display the menu
        GameStateManager.getInstance().activateChildNamed("Menu");
        ((JOGLAWTCanvas)Frame.getFrames()[0].getComponent(0)).setUpdateInput(true);
    }

    public final StandardGame getGame(){
        return(game);
    }*/
     
}

