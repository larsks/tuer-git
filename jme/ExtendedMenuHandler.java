package jme;


import com.jme.input.InputHandler;
import com.jme.input.KeyBindingManager;
import com.jme.input.KeyInput;
import com.jme.input.action.InputAction;
import com.jme.input.action.InputActionEvent;
import com.jmex.game.StandardGame;
import com.jmex.game.state.GameState;
import com.jmex.game.state.GameStateManager;

public final class ExtendedMenuHandler extends InputHandler{

    private GameState menuState;
    
    private MenuIndex index;
    
    private static enum MenuIndex{NEW_GAME,OPTIONS,LOAD_GAME,SAVE_GAME,ABOUT,QUIT_GAME};
    
    private static final MenuIndex INITIAL_MENU_INDEX=MenuIndex.NEW_GAME;

    private static final int menuIndexCount=MenuIndex.values().length;
    
    
    public ExtendedMenuHandler(JMEGameServiceProvider serviceProvider,GameState menuState){
        KeyBindingManager keyBindingManager=KeyBindingManager.getKeyBindingManager();
        keyBindingManager.set("exit",KeyInput.KEY_ESCAPE);
        keyBindingManager.set("enter",KeyInput.KEY_RETURN);
        keyBindingManager.set("up",KeyInput.KEY_UP);
        keyBindingManager.set("down",KeyInput.KEY_DOWN);
        addAction(new ExitAction(serviceProvider.getGame()),"exit",false);      
        addAction(new EnterAction(serviceProvider),"enter",false);
        addAction(new UpAction(),"up",false);
        addAction(new DownAction(),"down",false);
        this.menuState=menuState;
        this.index=INITIAL_MENU_INDEX;

    }
    
    public final int getIndex(){
        return(index.ordinal());
    }
    
    private final class UpAction extends InputAction{
        
        public final void performAction(InputActionEvent evt){
            index=MenuIndex.values()[(index.ordinal()+1)%menuIndexCount];
        }
    }
    
    private final class DownAction extends InputAction{
        
        public final void performAction(InputActionEvent evt){
            index=MenuIndex.values()[(index.ordinal()+(menuIndexCount-1))%menuIndexCount];
        }
    }

    private static final class ExitAction extends InputAction{
        
        private StandardGame game;
        
        private ExitAction(StandardGame game){
            this.game=game;
        }
        
        public final void performAction(InputActionEvent evt){
            this.game.shutdown();
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
                {this.serviceProvider.getGame().shutdown();
                 break;
                }
            }           
        }
    }
}
