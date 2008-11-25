package jme;

import com.jme.math.Vector3f;
import com.jme.renderer.Renderer;
import com.jme.scene.shape.Box;
import com.jme.scene.state.TextureState;
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
    
    //private StandardGame game;
    
    private ExtendedMenuHandler input;
    
    
    public MenuState(String name,TransitionGameState trans,JMEGameServiceProvider serviceProvider){
        super(name);
        //this.game=game;
        this.quitGameItem=new Box("Quit Game",new Vector3f(-5,-5,-5),new Vector3f(5,5,5));
        this.quitGameItem.setLocalTranslation(0,0,-10);
        this.quitGameItem.setRenderQueueMode(Renderer.QUEUE_OPAQUE);
        this.rootNode.attachChild(this.quitGameItem);
        Renderer renderer=serviceProvider.getGame().getDisplay().getRenderer();   
        TextureState ts=renderer.createTextureState();
        ts.setEnabled(true);
        this.rootNode.setRenderState(ts);
        //setup the input handler
        this.input=new ExtendedMenuHandler(serviceProvider,this);
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
        this.input.getIndex();
        //TODO: draw the menu
    }
    
    @Override
    public final void update(final float tpf) {
        super.update(tpf);
        this.input.update(tpf);
    }
}
