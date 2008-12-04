package jme;

import java.net.URL;
import java.nio.FloatBuffer;
import java.util.logging.Logger;
import com.jme.scene.TexCoords;
import com.jme.scene.state.TextureState;
import com.jme.image.Texture;
import com.jme.input.MouseInput;
import com.jme.math.Vector3f;
import com.jme.renderer.Renderer;
import com.jme.scene.shape.Box;
import com.jme.system.DisplaySystem;
import com.jme.util.TextureManager;
import com.jme.util.geom.BufferUtils;
import com.jme.util.resource.ResourceLocatorTool;
import com.jmex.awt.input.AWTMouseInput;
import com.jmex.game.state.BasicGameState;
import com.jmex.game.state.load.TransitionGameState;

public final class MenuState extends BasicGameState {


    private Box[] menuItemArray;
    
    private static final String[] itemNameArray=new String[]{"New Game","Options","Save Game","Load Game","About","Quit"};
    
    private ExtendedMenuHandler input;
    
    private static final Logger logger = Logger.getLogger(MenuState.class.getName());
    
    
    public MenuState(String name,final TransitionGameState trans,final JMEGameServiceProvider serviceProvider){
        super(name);
        Renderer renderer=DisplaySystem.getDisplaySystem().getRenderer();
        //TODO: use another texture for the menu
        URL quitItemTextureURL=ResourceLocatorTool.locateResource(ResourceLocatorTool.TYPE_TEXTURE,"starting_screen_bis.png");
        TextureState ts=renderer.createTextureState();
        ts.setEnabled(true);
        ts.setTexture(TextureManager.loadTexture(quitItemTextureURL,
                Texture.MinificationFilter.BilinearNoMipMaps,
                Texture.MagnificationFilter.Bilinear));       
        final float itemHeight=2.0f;
        final float verticalInterItemGap=0.5f;
        final float titleHeight=3.0f;
        final float verticalInterSectionGap=1.0f;
        final float xmin=-5.0f;
        final float zmin=-5.0f;
        final float xmax=5.0f;
        final float zmax=5.0f;
        final int itemCount=itemNameArray.length;
        final int faceCount=6;
        menuItemArray=new Box[itemCount];
        float ordinate=(((itemCount*itemHeight)+((itemCount-1)*verticalInterItemGap))/2)-titleHeight-verticalInterSectionGap;
        FloatBuffer buffer;
        for(int itemIndex=0;itemIndex<itemCount;itemIndex++)
            {menuItemArray[itemIndex]=new Box(itemNameArray[itemIndex]+" Menu Item",new Vector3f(xmin,ordinate-itemHeight,zmin),new Vector3f(xmax,ordinate,zmax));
             ordinate-=itemHeight+verticalInterItemGap;
             menuItemArray[itemIndex].setLocalTranslation(0,0,-10);
             menuItemArray[itemIndex].setRenderQueueMode(Renderer.QUEUE_OPAQUE);
             menuItemArray[itemIndex].setRenderState(ts);
             buffer=BufferUtils.createFloatBuffer(8*faceCount);
             for(int faceIndex=0;faceIndex<faceCount;faceIndex++)
                 {buffer.put(faceIndex/(float)faceCount);
                  buffer.put(itemIndex/(float)itemCount);
                  buffer.put((faceIndex+1)/(float)faceCount);
                  buffer.put(itemIndex/(float)itemCount);                 
                  buffer.put((faceIndex+1)/(float)faceCount);
                  buffer.put((itemIndex+1)/(float)itemCount);
                  buffer.put(faceIndex/(float)faceCount);
                  buffer.put((itemIndex+1)/(float)itemCount);
                  //logger.info("TEXCOORDS: "+faceIndex/(float)faceCount+" "+itemIndex/(float)itemCount);
                 }
             buffer.rewind();
             menuItemArray[itemIndex].setTextureCoords(new TexCoords(buffer,2),0);
             menuItemArray[itemIndex].updateRenderState();
             rootNode.attachChild(menuItemArray[itemIndex]);
            }   
        rootNode.updateRenderState();
        //setup the input handler
        input=new ExtendedMenuHandler(serviceProvider,this);
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
        input.getIndex();
        //TODO: draw the menu
    }
    
    @Override
    public final void update(final float tpf) {
        super.update(tpf);
        input.update(tpf);
    }
}
