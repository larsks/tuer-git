package jme;

import java.awt.Component;
import java.awt.Frame;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.net.URL;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.logging.Logger;

import com.jme.scene.state.TextureState;
import com.jme.image.Texture;
import com.jme.input.InputHandler;
import com.jme.input.InputSystem;
import com.jme.input.KeyInput;
import com.jme.input.KeyInputListener;
import com.jme.math.Vector3f;
import com.jme.renderer.Renderer;
import com.jme.scene.shape.Box;
import com.jme.util.GameTaskQueueManager;
import com.jme.util.TextureManager;
import com.jme.util.resource.ResourceLocatorTool;
import com.jmex.game.state.BasicGameState;
import com.jmex.game.state.load.TransitionGameState;

public final class MenuState extends BasicGameState {

    
    private Box resumeBoxItem;
    
    private Box newGameBoxItem;
    
    private Box optionsItem;
    
    private Box loadGameItem;
    
    private Box saveGameItem;
    
    private Box aboutItem;
    
    private Box quitGameItem;
    
    private InputHandler input;
    
    private static final Logger logger = Logger.getLogger(MenuState.class.getName());
    
    
    public MenuState(String name,final TransitionGameState trans,final JMEGameServiceProvider serviceProvider){
        super(name);
        this.quitGameItem=new Box("Quit Game",new Vector3f(-5,-2,-5),new Vector3f(5,2,5));
        this.quitGameItem.setLocalTranslation(0,0,-10);
        this.quitGameItem.setRenderQueueMode(Renderer.QUEUE_OPAQUE);
        this.rootNode.attachChild(this.quitGameItem);      
        Renderer renderer=serviceProvider.getGame().getDisplay().getRenderer(); 
        URL quitItemTextureURL=ResourceLocatorTool.locateResource(ResourceLocatorTool.TYPE_TEXTURE,"starting_screen_bis.png");
        TextureState ts=renderer.createTextureState();
        ts.setEnabled(true);
        ts.setTexture(TextureManager.loadTexture(quitItemTextureURL,
                Texture.MinificationFilter.BilinearNoMipMaps,
                Texture.MagnificationFilter.Bilinear));        
        this.rootNode.setRenderState(ts);
        this.rootNode.updateRenderState();
        //setup the input handler
        this.input=new ExtendedMenuHandler(serviceProvider,this);
        KeyAdapter ka=new KeyAdapter(){
            @Override
            public void keyPressed(KeyEvent e){
                logger.info(e.toString());
            }
        };
        for(Frame frame:Frame.getFrames())
            {frame.addKeyListener(ka);
             for(Component c:frame.getComponents())
                 c.addKeyListener(ka);
            }
        KeyInput.get().addListener(new KeyInputListener(){

            @Override
            public void onKey(char character, int keyCode, boolean pressed) {
                logger.info("[onKey] "+character+" "+keyCode+" "+pressed);
            }
            
        });
    }
    
    /**
     * activate / deactivate Mouse cursor.
     * @param active active yes/no.
     */
    /*@Override
    public final void setActive(final boolean active) {
        super.setActive(active);
        
    }*/

    /**
     * draw the menu.
     * @param tpf time since last frame.
     */
    @Override
    public final void render(final float tpf) {
        super.render(tpf);
        //TODO: use this index to set a different color for the selected menu
        //this.input.getIndex();
        //TODO: draw the menu
    }
    
    @Override
    public final void update(final float tpf) {
        super.update(tpf);
        this.input.update(tpf);
        InputSystem.update();
    }
}
