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
    
    
    public ExtendedMenuHandler(JMEGameServiceProvider serviceProvider,GameState menuState){      
        addAction(new ExitAction(serviceProvider),InputHandler.DEVICE_KEYBOARD,KeyInput.KEY_ESCAPE,InputHandler.AXIS_NONE,true);
        addAction(new EnterAction(serviceProvider),InputHandler.DEVICE_KEYBOARD,KeyInput.KEY_RETURN,InputHandler.AXIS_NONE,true);
        addAction(new UpAction(),InputHandler.DEVICE_KEYBOARD,KeyInput.KEY_UP,InputHandler.AXIS_NONE,true);
        addAction(new DownAction(),InputHandler.DEVICE_KEYBOARD,KeyInput.KEY_DOWN,InputHandler.AXIS_NONE,true);
        this.menuState=menuState;
        this.index=INITIAL_MENU_INDEX;
    }
    
    public final int getIndex(){
        return(index.ordinal());
    }
    
    private final class UpAction extends InputAction{
        
        public final void performAction(InputActionEvent evt){
            index=MenuIndex.values()[(index.ordinal()+(menuIndexCount-1))%menuIndexCount];
            logger.info("[UP] index "+index.toString());
        }
    }
    
    private final class DownAction extends InputAction{
        
        public final void performAction(InputActionEvent evt){           
            index=MenuIndex.values()[(index.ordinal()+1)%menuIndexCount];
            logger.info("[DOWN] index "+index.toString());
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
        
        private EnterAction(JMEGameServiceProvider serviceProvider){
            this.serviceProvider=serviceProvider;
        }
        
        public final void performAction(InputActionEvent evt){
            switch(index)
            {
                case NEW_GAME:
                {GameState levelGameState=this.serviceProvider.getLevelGameState(0);
                 GameStateManager.getInstance().attachChild(levelGameState);
                 //TODO: detach it in the pause menu when going to the main menu
                 levelGameState.setActive(true);
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
