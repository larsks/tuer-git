/*This program is free software; you can redistribute it and/or
  modify it under the terms of the GNU General Public License
  as published by the Free Software Foundation, version 2
  of the License.
  This program is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  GNU General Public License for more details.
  You should have received a copy of the GNU General Public License
  along with this program; if not, write to the Free Software
  Foundation, Inc., 59 Temple Place - Suite 330, Boston,
  MA 02111-1307, USA.
*/
package jme;

import java.awt.Toolkit;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import com.jme.math.Vector3f;
import com.jme.renderer.Camera;
import com.jme.system.DisplaySystem;
import com.jme.system.PreferencesGameSettings;
import com.jme.system.jogl.JOGLSystemProvider;
import com.jme.util.GameTaskQueue;
import com.jme.util.GameTaskQueueManager;
import com.jme.util.resource.ResourceLocatorTool;
import com.jme.util.resource.SimpleResourceLocator;
import com.jmex.game.StandardGame;
import com.jmex.game.StandardGame.GameType;
import com.jmex.game.state.GameState;
import com.jmex.game.state.GameStateManager;
import com.jmex.game.state.load.TransitionGameState;

/**
 * 
 * @author Julien Gouesse
 *
 */
public final class JMEGameServiceProvider {

    private static final Logger logger=Logger.getLogger(JMEGameServiceProvider.class.getName());
    
    private Camera cam;
    
    private StandardGame standardGame;
    
    
    private JMEGameServiceProvider(){
        PreferencesGameSettings pgs=new PreferencesGameSettings(Preferences.userRoot());
        pgs.setRenderer(JOGLSystemProvider.SYSTEM_IDENTIFIER);
        Toolkit toolkit=Toolkit.getDefaultToolkit();
        final int width=toolkit.getScreenSize().width;
        final int height=toolkit.getScreenSize().height;
        pgs.setMusic(false);
        pgs.setSFX(false);       
        pgs.setWidth(width);
        pgs.setHeight(height);
        pgs.setFullscreen(true);
        this.standardGame=new StandardGame("TUER",GameType.GRAPHICAL,pgs);
        this.standardGame.start();       
        try{ResourceLocatorTool.addResourceLocator(ResourceLocatorTool.TYPE_TEXTURE,new SimpleResourceLocator(JMEGameServiceProvider.class.getResource("/texture/")));} 
        catch(URISyntaxException urise) 
        {urise.printStackTrace();}
        //effectively localize the resource
        URL startingTextureURL=ResourceLocatorTool.locateResource(ResourceLocatorTool.TYPE_TEXTURE,"starting_screen_bis.png");
        TransitionGameState transitionGameState = new TransitionGameState(20,startingTextureURL);
        GameStateManager.getInstance().attachChild(transitionGameState);
        transitionGameState.setActive(true);
        transitionGameState.setProgress(0,"Initializing Game ...");
        final DisplaySystem disp=DisplaySystem.getDisplaySystem(); 
        //use our own parameters
        cam=disp.getRenderer().getCamera();
        cam.setFrustumPerspective( 45.0f,(float) disp.getWidth() / (float) disp.getHeight(), 0.2F, 2000.0F );
        Vector3f loc = new Vector3f(0.0f,0.0f,25.0f);
        Vector3f left = new Vector3f(-1.0f,0.0f,0.0f);
        Vector3f up = new Vector3f(0.0f,1.0f,0.0f);
        Vector3f dir = new Vector3f(0.0f,0.0f,-1.0f);
        cam.setFrame(loc,left,up,dir);
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
        standardGame.shutdown();           
    }   
}

