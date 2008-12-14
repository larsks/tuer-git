package jme;

import java.net.URL;
import java.nio.FloatBuffer;
import java.util.logging.Logger;
import com.jme.scene.TexCoords;
import com.jme.scene.state.TextureState;
import com.jme.image.Texture;
import com.jme.input.MouseInput;
import com.jme.math.Quaternion;
import com.jme.math.Vector3f;
import com.jme.renderer.Renderer;
import com.jme.scene.shape.Box;
import com.jme.system.DisplaySystem;
import com.jme.util.TextureManager;
import com.jme.util.geom.BufferUtils;
import com.jme.util.jogl.JOGLUtil;
import com.jme.util.resource.ResourceLocatorTool;
import com.jmex.awt.input.AWTMouseInput;
import com.jmex.game.state.BasicGameState;
import com.jmex.game.state.load.TransitionGameState;

public final class MenuState extends BasicGameState {


    private Box[] menuItemArray;
    
    private static final String[] unpausedItemNameArray=new String[]{"New game","Options","Load game","Save game","About","Quit"};
    
    private static final String[] pausedItemNameArray=new String[]{"Resume game","Options","Load game","Save game","About","Main menu"};
    
    private ExtendedMenuHandler input;
    
    private int previouslySelectedIndex;
    
    private int previouslyPressedIndex;
    
    private static final Quaternion enabledItemQuaternion=new Quaternion();
    
    private static final Quaternion selectedItemQuaternion=new Quaternion(new float[]{0.0f,(float)(0.5f*Math.PI),0.0f});
    
    private static final Quaternion enteredItemQuaternion=new Quaternion(new float[]{0.0f,(float)Math.PI,0.0f});
    
    private static final Quaternion disabledItemQuaternion=new Quaternion(new float[]{0.0f,(float)(1.5f*Math.PI),0.0f});
    
    private static final Logger logger=Logger.getLogger(MenuState.class.getName());
    
    
    public MenuState(String name,final TransitionGameState trans,final JMEGameServiceProvider serviceProvider,boolean paused){
        super(name);
        Renderer renderer=DisplaySystem.getDisplaySystem().getRenderer();
        URL quitItemTextureURL=ResourceLocatorTool.locateResource(ResourceLocatorTool.TYPE_TEXTURE,"pic512/menuItems.png");
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
        final int itemCount=unpausedItemNameArray.length;
        final int faceCount=6;
        menuItemArray=new Box[itemCount];
        float ordinate=(((itemCount*itemHeight)+((itemCount-1)*verticalInterItemGap))/2)-titleHeight-verticalInterSectionGap;
        FloatBuffer buffer;
        //final int maxFaceCount=(int)Math.pow(2.0D,Math.floor(Math.log(faceCount/2.0D)/Math.log(2.0D)));
        final int maxFaceCount=JOGLUtil.nearestPower(faceCount);
        final int maxItemCount=JOGLUtil.nearestPower(itemCount);
        logger.info("maxFaceCount = "+maxFaceCount);
        final String[] itemNameArray;
        final int initialItemIndex,lastItemIndex;
        if(paused)
            {itemNameArray=pausedItemNameArray;
             initialItemIndex=itemCount;
             lastItemIndex=itemCount+1;
            }
        else
            {itemNameArray=unpausedItemNameArray;
             initialItemIndex=0;
             lastItemIndex=itemCount-1;
            }               
        for(int itemIndex=0;itemIndex<itemCount;itemIndex++)
            {menuItemArray[itemIndex]=new Box(itemNameArray[itemIndex]+" Menu Item",new Vector3f(xmin,ordinate-itemHeight,zmin),new Vector3f(xmax,ordinate,zmax));
             ordinate-=itemHeight+verticalInterItemGap;
             menuItemArray[itemIndex].setLocalTranslation(0,0,-10);
             menuItemArray[itemIndex].setRenderQueueMode(Renderer.QUEUE_OPAQUE);
             menuItemArray[itemIndex].setRenderState(ts);
             buffer=BufferUtils.createFloatBuffer(8*faceCount);  
             final int currentItemIndex;
             if(itemIndex==0)
                 currentItemIndex=initialItemIndex;
             else
                 if(itemIndex==itemCount-1)
                     currentItemIndex=lastItemIndex;
                 else
                     currentItemIndex=itemIndex;
             for(int faceIndex=0;faceIndex<faceCount;faceIndex++)
                 {buffer.put(1.0f-(faceIndex/(float)maxFaceCount));
                  buffer.put(1.0f-((currentItemIndex+1)/(float)maxItemCount));
                  buffer.put(1.0f-((faceIndex+1)/(float)maxFaceCount));
                  buffer.put(1.0f-((currentItemIndex+1)/(float)maxItemCount));                 
                  buffer.put(1.0f-((faceIndex+1)/(float)maxFaceCount));
                  buffer.put(1.0f-((currentItemIndex)/(float)maxItemCount));
                  buffer.put(1.0f-(faceIndex/(float)maxFaceCount));
                  buffer.put(1.0f-((currentItemIndex)/(float)maxItemCount));
                 }
             buffer.rewind();
             menuItemArray[itemIndex].setTextureCoords(new TexCoords(buffer,2),0);
             menuItemArray[itemIndex].updateRenderState();
             rootNode.attachChild(menuItemArray[itemIndex]);
            }   
        rootNode.updateRenderState();
        //setup the input handler
        input=new ExtendedMenuHandler(serviceProvider,this,paused);
        previouslySelectedIndex=-1;
        previouslyPressedIndex=-1;
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
    }
    
    @Override
    public final void update(final float tpf) {
        super.update(tpf);
        input.update(tpf);
        int currentlySelectedIndex=input.getIndex();       
        if(currentlySelectedIndex!=previouslySelectedIndex)
            {// ( E -> S ) && ( S -> E )
             //unselect the previous index if valid
             if(previouslySelectedIndex!=-1)
                 menuItemArray[previouslySelectedIndex].setLocalRotation(enabledItemQuaternion);
             //select the current index
             menuItemArray[currentlySelectedIndex].setLocalRotation(selectedItemQuaternion);
             //update the previous index
             previouslySelectedIndex=currentlySelectedIndex;
            }
        else
            {if(input.isEntering())
                 {// S -> P
                  if(previouslyPressedIndex==-1)
                      {previouslyPressedIndex=currentlySelectedIndex;
                       //"press"
                       menuItemArray[currentlySelectedIndex].setLocalRotation(enteredItemQuaternion);   
                      }               
                 } 
             else
                 {// P -> S
                  if(previouslyPressedIndex!=-1)
                      {previouslyPressedIndex=-1;
                       menuItemArray[currentlySelectedIndex].setLocalRotation(selectedItemQuaternion);
                      }
                 }
            }
    }
}
