package jme;

import java.util.logging.Logger;

import com.jme.input.InputHandler;
import com.jme.input.KeyInput;
import com.jme.input.action.InputAction;
import com.jme.input.action.InputActionEvent;
import com.jmex.game.state.GameState;
import com.jmex.game.state.GameStateManager;

public final class ExtendedMenuHandler extends InputHandler{

    private static final Logger logger = Logger.getLogger(ExtendedMenuHandler.class.getName());
    
    private GameState menuState;
    
    private MenuIndex index;
    
    private static enum MenuIndex{NEW_GAME,OPTIONS,LOAD_GAME,SAVE_GAME,ABOUT,QUIT_GAME};
    
    private static final MenuIndex INITIAL_MENU_INDEX=MenuIndex.NEW_GAME;

    private static final int menuIndexCount=MenuIndex.values().length;
    
    private static final long canonicalStateChangeTime=200;
    
    
    public ExtendedMenuHandler(JMEGameServiceProvider serviceProvider,GameState menuState,boolean paused){      
        addAction(new ExitAction(serviceProvider),InputHandler.DEVICE_KEYBOARD,KeyInput.KEY_ESCAPE,InputHandler.AXIS_NONE,false);
        addAction(new EnterAction(serviceProvider,paused),InputHandler.DEVICE_KEYBOARD,KeyInput.KEY_RETURN,InputHandler.AXIS_NONE,false);
        addAction(new UpAction(),InputHandler.DEVICE_KEYBOARD,KeyInput.KEY_UP,InputHandler.AXIS_NONE,false);
        addAction(new DownAction(),InputHandler.DEVICE_KEYBOARD,KeyInput.KEY_DOWN,InputHandler.AXIS_NONE,false);
        this.menuState=menuState;
        this.index=INITIAL_MENU_INDEX;
    }
    
    public final int getIndex(){
        return(index.ordinal());
    }
    
    private final class UpAction extends InputAction{
        
        private long latestValidUpAction=-1;
        
        public final void performAction(InputActionEvent evt){
            long latestUpAction=System.currentTimeMillis();
            if(latestUpAction-latestValidUpAction>canonicalStateChangeTime)
                {index=MenuIndex.values()[(index.ordinal()+(menuIndexCount-1))%menuIndexCount];
                 latestValidUpAction=latestUpAction;
                }
            //logger.info("[UP] index "+index.toString());
        }
    }
    
    private final class DownAction extends InputAction{
        
        private long latestValidDownAction=-1;
        
        public final void performAction(InputActionEvent evt){    
            long latestDownAction=System.currentTimeMillis();
            if(latestDownAction-latestValidDownAction>canonicalStateChangeTime)
                {index=MenuIndex.values()[(index.ordinal()+1)%menuIndexCount];
                 latestValidDownAction=latestDownAction;
                }
            //logger.info("[DOWN] index "+index.toString());
        }
    }

    private static final class ExitAction extends InputAction{
        
        private JMEGameServiceProvider serviceProvider;
        
        private ExitAction(JMEGameServiceProvider serviceProvider){
            this.serviceProvider=serviceProvider;
        }
        
        @Override
        public final void performAction(InputActionEvent evt){
            this.serviceProvider.exit();
        }
    }

    private final class EnterAction extends InputAction{
        
        private JMEGameServiceProvider serviceProvider;
        
        private boolean paused;
        
        private EnterAction(JMEGameServiceProvider serviceProvider,boolean paused){
            this.serviceProvider=serviceProvider;
            this.paused=paused;
        }
        
        public final void performAction(InputActionEvent evt){
            switch(index)
            {
                case NEW_GAME:
                {if(paused)
                    {/**TODO: disable the level state
                      *       detach it from the state machine   
                      */  
                     //activate the main menu state
                     GameStateManager.getInstance().activateChildNamed("Main menu");
                    }
                 else
                     {GameState levelGameState=this.serviceProvider.getLevelGameState(0);
                      GameStateManager.getInstance().attachChild(levelGameState);
                      levelGameState.setActive(true);
                     }                
                 menuState.setActive(false);
                 break;
                }
                case LOAD_GAME:
                {
                 break;
                }
                case QUIT_GAME:
                {serviceProvider.exit();
                 break;
                }
            }           
        }
    }
}
