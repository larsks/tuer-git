package jme;

import java.net.URL;
//import java.util.logging.Logger;
import com.jme.scene.TexCoords;
import com.jme.scene.state.TextureState;
import com.jme.image.Texture;
import com.jme.input.InputHandler;
import com.jme.input.MouseInput;
import com.jme.math.Vector3f;
import com.jme.renderer.Renderer;
import com.jme.scene.shape.Box;
import com.jme.system.DisplaySystem;
import com.jme.util.TextureManager;
import com.jme.util.resource.ResourceLocatorTool;
import com.jmex.awt.input.AWTMouseInput;
import com.jmex.game.state.BasicGameState;
import com.jmex.game.state.load.TransitionGameState;

public final class MenuState extends BasicGameState {

    
    private Box newGameBoxItem;
    
    private Box optionsItem;
    
    private Box loadGameItem;
    
    private Box saveGameItem;
    
    private Box aboutItem;
    
    private Box quitGameItem;
    
    private ExtendedMenuHandler input;
    
    //private static final Logger logger = Logger.getLogger(MenuState.class.getName());
    
    
    public MenuState(String name,final TransitionGameState trans,final JMEGameServiceProvider serviceProvider){
        super(name);
        final float itemHeight=2.0f;
        final float verticalInterItemGap=0.5f;
        final float titleHeight=3.0f;
        final float verticalInterSectionGap=1.0f;
        float ordinate=(((6*itemHeight)+(5*verticalInterItemGap))/2)-titleHeight-verticalInterSectionGap;
        this.newGameBoxItem=new Box("New Game Menu Item",new Vector3f(-5,ordinate-itemHeight,-5),new Vector3f(5,ordinate,5));
        ordinate-=itemHeight+verticalInterItemGap;
        this.optionsItem=new Box("Options Menu Item",new Vector3f(-5,ordinate-itemHeight,-5),new Vector3f(5,ordinate,5));
        ordinate-=itemHeight+verticalInterItemGap;
        this.saveGameItem=new Box("Save Game Menu Item",new Vector3f(-5,ordinate-itemHeight,-5),new Vector3f(5,ordinate,5));
        ordinate-=itemHeight+verticalInterItemGap;
        this.loadGameItem=new Box("About Menu Item",new Vector3f(-5,ordinate-itemHeight,-5),new Vector3f(5,ordinate,5));
        ordinate-=itemHeight+verticalInterItemGap;
        this.aboutItem=new Box("About Menu Item",new Vector3f(-5,ordinate-itemHeight,-5),new Vector3f(5,ordinate,5));
        ordinate-=itemHeight+verticalInterItemGap;
        this.quitGameItem=new Box("Quit Game Menu Item",new Vector3f(-5,ordinate-itemHeight,-5),new Vector3f(5,ordinate,5));
        this.newGameBoxItem.setLocalTranslation(0,0,-10);
        this.optionsItem.setLocalTranslation(0,0,-10);
        this.saveGameItem.setLocalTranslation(0,0,-10);
        this.loadGameItem.setLocalTranslation(0,0,-10);
        this.aboutItem.setLocalTranslation(0,0,-10);
        this.quitGameItem.setLocalTranslation(0,0,-10);
        //TODO: add texture coordinates
        //this.newGameBoxItem.addTextureCoordinates(new TexCoords());
        this.newGameBoxItem.setRenderQueueMode(Renderer.QUEUE_OPAQUE);
        this.optionsItem.setRenderQueueMode(Renderer.QUEUE_OPAQUE);
        this.saveGameItem.setRenderQueueMode(Renderer.QUEUE_OPAQUE);
        this.loadGameItem.setRenderQueueMode(Renderer.QUEUE_OPAQUE);
        this.aboutItem.setRenderQueueMode(Renderer.QUEUE_OPAQUE);        
        this.quitGameItem.setRenderQueueMode(Renderer.QUEUE_OPAQUE);
        this.rootNode.attachChild(this.newGameBoxItem);
        this.rootNode.attachChild(this.optionsItem);
        this.rootNode.attachChild(this.saveGameItem);
        this.rootNode.attachChild(this.loadGameItem);
        this.rootNode.attachChild(this.aboutItem);
        this.rootNode.attachChild(this.quitGameItem);
        Renderer renderer=DisplaySystem.getDisplaySystem().getRenderer();
        //TODO: use another texture for the menu
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
    }
    
    /**
     * 
     * @param active active yes/no.
     */
    @Override
    public final void setActive(final boolean active) {
        super.setActive(active);
        if(active)
            ((AWTMouseInput) MouseInput.get()).setCursorVisible(false);
    }

    /**
     * draw the menu.
     * @param tpf time since last frame.
     */
    @Override
    public final void render(final float tpf) {
        super.render(tpf);
        //TODO: use this index to set a different color for the selected menu
        this.input.getIndex();
        //TODO: draw the menu
    }
    
    @Override
    public final void update(final float tpf) {
        super.update(tpf);
        this.input.update(tpf);
    }
}
