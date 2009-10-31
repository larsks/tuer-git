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
package engine;

import java.awt.Color;
import java.awt.DisplayMode;
import java.awt.GraphicsEnvironment;
import java.awt.Point;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import javax.imageio.ImageIO;
import paulscode.sound.SoundSystem;
import paulscode.sound.SoundSystemConfig;
import paulscode.sound.SoundSystemException;
import paulscode.sound.codecs.CodecJOrbis;
import paulscode.sound.libraries.LibraryJavaSound;
import com.ardor3d.bounding.BoundingBox;
import com.ardor3d.framework.Canvas;
import com.ardor3d.framework.DisplaySettings;
import com.ardor3d.framework.Scene;
import com.ardor3d.framework.jogl.JoglCanvas;
import com.ardor3d.framework.jogl.JoglCanvasRenderer;
import com.ardor3d.image.Image;
import com.ardor3d.image.Texture;
import com.ardor3d.image.Texture2D;
import com.ardor3d.image.Image.Format;
import com.ardor3d.image.util.AWTImageLoader;
import com.ardor3d.input.GrabbedState;
import com.ardor3d.input.Key;
import com.ardor3d.input.MouseManager;
import com.ardor3d.input.PhysicalLayer;
import com.ardor3d.input.awt.AwtFocusWrapper;
import com.ardor3d.input.awt.AwtKeyboardWrapper;
import com.ardor3d.input.awt.AwtMouseManager;
import com.ardor3d.input.awt.AwtMouseWrapper;
import com.ardor3d.input.control.FirstPersonControl;
import com.ardor3d.input.logical.InputTrigger;
import com.ardor3d.input.logical.KeyPressedCondition;
import com.ardor3d.input.logical.TriggerAction;
import com.ardor3d.input.logical.TwoInputStates;
import com.ardor3d.intersection.PickResults;
import com.ardor3d.math.MathUtils;
import com.ardor3d.math.Matrix3;
import com.ardor3d.math.Ray3;
import com.ardor3d.math.Vector3;
import com.ardor3d.math.type.ReadOnlyVector3;
import com.ardor3d.renderer.Renderer;
import com.ardor3d.renderer.state.TextureState;
import com.ardor3d.renderer.state.ZBufferState;
import com.ardor3d.renderer.state.RenderState.StateType;
import com.ardor3d.scenegraph.Node;
import com.ardor3d.scenegraph.controller.SpatialController;
import com.ardor3d.scenegraph.shape.Box;
import com.ardor3d.util.ContextGarbageCollector;
import com.ardor3d.util.ReadOnlyTimer;
import com.ardor3d.util.TextureManager;
import com.ardor3d.util.Timer;
import com.ardor3d.util.geom.BufferUtils;
import com.ardor3d.util.resource.ResourceLocatorTool;
import com.ardor3d.util.resource.ResourceSource;
import com.ardor3d.util.resource.SimpleResourceLocator;

/**
 * General entry point of an application created by JFPSM
 * 
 */
public class Ardor3DGameServiceProvider implements Scene{

    /**Our native window, not the gl surface itself*/
    private final JoglCanvas canvas;

    private final PhysicalLayer physicalLayer;

    private final MouseManager mouseManager;

    private final Vector3 worldUp;

    /**Our timer*/
    private final Timer timer;

    /**boolean allowing us to "pull the plug" from anywhere*/
    private boolean exit;

    /**root of our scene*/
    private final Node root;
    
    enum Step{/*RATING,*/
              INITIALIZATION,
              INTRODUCTION,
              /*MAIN_MENU,
              LOADING_DISPLAY,*/
              GAME,
              GAME_OVER,
              PAUSE_MENU,
              END_LEVEL_DISPLAY,
              END_GAME_DISPLAY};
              
    private SoundSystem soundSystem;

    private final StateMachine stateMachine;
    
    /**images used to modify textures at runtime (null if the texture should not be modified)*/
    private final BufferedImage[] textureImages;
    
    private final ByteBuffer[] imageBuffers;
    
    private final Box[] illustrationBox;
    
    private final boolean[] modifiableTextureFlags;
    
    
    public static void main(final String[] args){
    	//Disable DirectDraw under Windows in order to avoid conflicts with OpenGL
    	System.setProperty("sun.java2d.noddraw","true");
        final Ardor3DGameServiceProvider application = new Ardor3DGameServiceProvider();
        application.start();
    }

    public Ardor3DGameServiceProvider(){
        this(new boolean[]{false,true,false,false,false,false,false});
    }
    
    /**
     * Constructs the example class, also creating the native window and GL surface.
     */
    public Ardor3DGameServiceProvider(final boolean[] modifiableTextureFlags){
        exit=false;
        this.modifiableTextureFlags=modifiableTextureFlags;
        textureImages=new BufferedImage[Step.values().length];
        imageBuffers=new ByteBuffer[Step.values().length];
        illustrationBox=new Box[Step.values().length];
        worldUp=new Vector3(0, 1, 0);
        timer=new Timer();
        root=new Node();
        stateMachine=new StateMachine(root);
        //Initialize the sound
        try{soundSystem=new SoundSystem(LibraryJavaSound.class);
            SoundSystemConfig.setCodec("ogg",CodecJOrbis.class);
           }
        catch(SoundSystemException sse)
        {sse.printStackTrace();}       
        // Setup a jogl canvas and canvas renderer
        final JoglCanvasRenderer canvasRenderer = new JoglCanvasRenderer(this);
        // Get the default display mode
        final DisplayMode defaultMode = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice()
                .getDisplayMode();
        // Choose the full-screen mode
        final DisplaySettings settings=new DisplaySettings(defaultMode.getWidth(), defaultMode.getHeight(),
                defaultMode.getBitDepth(), 0, 0, 8, 0, 0, true, false);
        canvas=new JoglCanvas(canvasRenderer,settings);
        canvas.init();
        mouseManager=new AwtMouseManager(canvas);
        // remove the mouse cursor
        mouseManager.setGrabbed(GrabbedState.GRABBED);
        physicalLayer=new PhysicalLayer(new AwtKeyboardWrapper(canvas), new AwtMouseWrapper(canvas),
                new AwtFocusWrapper(canvas));
    }

    
    final StateMachine getStateMachine(){
        return(stateMachine);
    }
    
    /**
     * Kicks off the example logic, first setting up the scene, then continuously updating and rendering it until exit
     * is flagged. Afterwards, the scene and gl surface are cleaned up.
     */
    protected final void start(){
        init();

        // Run in this same thread.
        while(!exit)
            {if(canvas.isClosing())
                 {exit=true;
                  return;
                 }
             updateLogicalLayer(timer);
             timer.update();
             if(stateMachine.isEnabled(Step.INITIALIZATION.ordinal()) && timer.getTimeInSeconds() > 15)
                 {stateMachine.setEnabled(Step.INITIALIZATION.ordinal(),false);
                  stateMachine.setEnabled(Step.INTRODUCTION.ordinal(),true);
                  if(soundSystem!=null)
                      soundSystem.backgroundMusic("Internationale",getClass().getResource("/sounds/internationale.ogg"),"internationale.ogg",true);
                 }
             //update controllers/render states/transforms/bounds for rootNode.
             root.updateGeometricState(timer.getTimePerFrame(), true);
             canvas.draw(null);
             //Thread.yield();
            }
        canvas.getCanvasRenderer().setCurrentContext();

        // Done, do cleanup
        ContextGarbageCollector.doFinalCleanup(canvas.getCanvasRenderer().getRenderer());
        canvas.close();
        if(soundSystem!=null)
            soundSystem.cleanup();
        //necessary for Java Webstart
        System.exit(0);
    }

    /**
     * Initialize our scene.
     */
    protected void init(){
        canvas.setTitle("Ardor3DGameServiceProvider - close window to exit");

        // Make a box...
        illustrationBox[Step.INITIALIZATION.ordinal()]=new Box(Step.INITIALIZATION.toString()+"Box",Vector3.ZERO,5,5,5);

        // Setup a bounding box for it.
        illustrationBox[Step.INITIALIZATION.ordinal()].setModelBound(new BoundingBox());

        // Set its location in space.
        illustrationBox[Step.INITIALIZATION.ordinal()].setTranslation(new Vector3(0,0,-15));       
        
        stateMachine.addState();
        // Add the box to the initial state
        stateMachine.attachChild(Step.INITIALIZATION.ordinal(),illustrationBox[Step.INITIALIZATION.ordinal()]);
        //Enable the first state
        stateMachine.setEnabled(Step.INITIALIZATION.ordinal(),true);
        
        // Add a second state to the state machine
        stateMachine.addState();
        // bind the physical layer to the logical layer
        stateMachine.getLogicalLayer(Step.INITIALIZATION.ordinal()).registerInput(canvas, physicalLayer);
        stateMachine.getLogicalLayer(Step.INTRODUCTION.ordinal()).registerInput(canvas, physicalLayer);

        final InputTrigger escTrigger=new InputTrigger(new KeyPressedCondition(Key.ESCAPE), new TriggerAction() {
            public void perform(final Canvas source, final TwoInputStates inputState, final double tpf) {
                exit = true;
            }
        });
        stateMachine.getLogicalLayer(Step.INITIALIZATION.ordinal()).registerTrigger(escTrigger);
        stateMachine.getLogicalLayer(Step.INTRODUCTION.ordinal()).registerTrigger(escTrigger);
        
        //Add a state for the game itself
        stateMachine.addState();
        stateMachine.getLogicalLayer(Step.GAME.ordinal()).registerInput(canvas, physicalLayer);
        stateMachine.getLogicalLayer(Step.GAME.ordinal()).registerTrigger(escTrigger);
        
        // drag only at false to remove the need of pressing a button to move
        FirstPersonControl.setupTriggers(stateMachine.getLogicalLayer(Step.GAME.ordinal()), worldUp, false);
        //FIXME: this is a temporary trigger to enter the game, remove it, put a true menu
        final InputTrigger returnTrigger=new InputTrigger(new KeyPressedCondition(Key.RETURN), new TriggerAction() {
            public void perform(final Canvas source, final TwoInputStates inputState, final double tpf) {
                stateMachine.setEnabled(Step.INTRODUCTION.ordinal(),false);
                if(soundSystem!=null)
                    soundSystem.stop("Internationale");
                stateMachine.setEnabled(Step.GAME.ordinal(),true);
            }
        });
        stateMachine.getLogicalLayer(Step.INTRODUCTION.ordinal()).registerTrigger(returnTrigger);
        // set it to rotate:
        illustrationBox[Step.INITIALIZATION.ordinal()].addController(new SpatialController<Box>(){
            private static final long serialVersionUID=1L;
            private final Vector3 axis=new Vector3(0, 1, 0).normalizeLocal();
            private final Matrix3 rotate=new Matrix3();
            private double angle = 0;

            public void update(final double time, final Box caller){
                // update our rotation
                angle+=(timer.getTimePerFrame()*25);
                while(angle>180)
                    angle-=360;
                rotate.fromAngleNormalAxis(angle*MathUtils.DEG_TO_RAD,axis);
                illustrationBox[Step.INITIALIZATION.ordinal()].setRotation(rotate);
            }
        });

        // Add our awt based image loader.
        AWTImageLoader.registerLoader();

        // Set the location of our example resources.
        try{SimpleResourceLocator srl = new SimpleResourceLocator(getClass().getResource("/images"));
            ResourceLocatorTool.addResourceLocator(ResourceLocatorTool.TYPE_TEXTURE, srl);
            //srl = new SimpleResourceLocator(getClass().getResource("/"));
            //ResourceLocatorTool.addResourceLocator(ResourceLocatorTool.TYPE_MODEL, srl);
           } 
        catch(final URISyntaxException urise)
        {urise.printStackTrace();}
        
        AffineTransform flipVerticallyTr;
        AffineTransformOp flipVerticallyOp;
        //Load images used to update the textures at runtime
        for(Step step:Step.values())
            if(modifiableTextureFlags[step.ordinal()])
                {try{ResourceSource resourceSource=ResourceLocatorTool.locateResource(ResourceLocatorTool.TYPE_TEXTURE,getIllustrationImagePathFromStep(step));
                     if(resourceSource!=null)
                         {textureImages[step.ordinal()]=ImageIO.read(resourceSource.openStream());
                          //flip the image vertically
                          flipVerticallyTr=AffineTransform.getScaleInstance(1,-1);                    
                          flipVerticallyTr.translate(0,-textureImages[step.ordinal()].getHeight());
                          flipVerticallyOp=new AffineTransformOp(flipVerticallyTr,AffineTransformOp.TYPE_NEAREST_NEIGHBOR);
                          textureImages[step.ordinal()]=flipVerticallyOp.filter(textureImages[step.ordinal()],null);                              
                         }                         
                    } 
                 catch(IOException ioe)
                 {ioe.printStackTrace();}
                }
        // Load collada model
        /*
         * try { final Node colladaNode = ColladaImporter.readColladaScene("collada/duck/duck.dae");
         * _root.attachChild(colladaNode); } catch (final Exception ex) { ex.printStackTrace(); }
         */

        // Create a ZBuffer to display pixels closest to the camera above farther ones.
        final ZBufferState buf=new ZBufferState();
        buf.setEnabled(true);
        buf.setFunction(ZBufferState.TestFunction.LessThanOrEqualTo);
        root.setRenderState(buf);

        final TextureState[] textureStates=new TextureState[Step.values().length];
        // Create a texture with the initial logo
        textureStates[Step.INITIALIZATION.ordinal()]=new TextureState();
        textureStates[Step.INITIALIZATION.ordinal()].setEnabled(true);
        textureStates[Step.INITIALIZATION.ordinal()].setTexture(TextureManager.load(getIllustrationImagePathFromStep(Step.INITIALIZATION),Texture.MinificationFilter.Trilinear,
                Format.GuessNoCompression,true));
        illustrationBox[Step.INITIALIZATION.ordinal()].setRenderState(textureStates[Step.INITIALIZATION.ordinal()]);
        
        illustrationBox[Step.INTRODUCTION.ordinal()]=new Box(Step.INTRODUCTION.toString()+"Box",Vector3.ZERO,12,9,5);
        illustrationBox[Step.INTRODUCTION.ordinal()].setModelBound(new BoundingBox());
        illustrationBox[Step.INTRODUCTION.ordinal()].setTranslation(new Vector3(0, 0, -75));
        stateMachine.attachChild(Step.INTRODUCTION.ordinal(),illustrationBox[Step.INTRODUCTION.ordinal()]);
        //Set a texture state to the box
        textureStates[Step.INTRODUCTION.ordinal()]=new TextureState();
        textureStates[Step.INTRODUCTION.ordinal()].setEnabled(true);
        textureStates[Step.INTRODUCTION.ordinal()].setTexture(TextureManager.load(getIllustrationImagePathFromStep(Step.INTRODUCTION),Texture.MinificationFilter.Trilinear,
                Format.GuessNoCompression,true));
        illustrationBox[Step.INTRODUCTION.ordinal()].setRenderState(textureStates[Step.INTRODUCTION.ordinal()]);
        //check if each image has a valid size, otherwise resize it
        int index;
        byte[] data;
        for(Step step:Step.values())
            {index=step.ordinal();
             if(modifiableTextureFlags[index]&&textureImages[index]!=null&&textureStates[index]!=null)
                {Image image=textureStates[index].getTexture().getImage();
                 if(textureImages[index].getWidth()!=image.getWidth()||textureImages[index].getHeight()!=image.getHeight())
                     {final AffineTransform scaleTr=AffineTransform.getScaleInstance((double)image.getWidth()/textureImages[index].getWidth(),(double)image.getHeight()/textureImages[index].getHeight());
                      final AffineTransformOp scaleOp=new AffineTransformOp(scaleTr,AffineTransformOp.TYPE_NEAREST_NEIGHBOR);
                      textureImages[index]=scaleOp.filter(textureImages[index],null);
                     }
                 data=AWTImageLoader.asByteArray(textureImages[index]);
                 imageBuffers[index]=BufferUtils.createByteBuffer(data.length);
                }
            }
        //Spread effect
        final ArrayList<Point> verticesList=new ArrayList<Point>();
        for(int i=0;i<textureImages[Step.INTRODUCTION.ordinal()].getWidth();i++)
            for(int j=0;j<textureImages[Step.INTRODUCTION.ordinal()].getHeight();j++)
                verticesList.add(new Point(i,j));
        final Point center=new Point(textureImages[Step.INTRODUCTION.ordinal()].getWidth()/2,textureImages[Step.INTRODUCTION.ordinal()].getHeight()/2);
        Collections.sort(verticesList,new Comparator<Point>(){
            @Override
            public int compare(Point p1, Point p2) {
                double d1=p1.distance(center);
                double d2=p2.distance(center);
                return(d1==d2?0:d1<d2?-1:1);
            }           
        });
        //set a controller that modifies the image
        illustrationBox[Step.INTRODUCTION.ordinal()].addController(new SpatialController<Box>(){
            
            private int index=0;
            
            public void update(final double time, final Box caller){
                // modify the underlying image
                int red, green, blue;
                int modifiedPixelsCount = 0;
                final int maxModifiedPixelsCount = (int) (20000 * time);
                int i,j;
                Point vertex;
                for(;index<verticesList.size();index++)
                    {vertex=verticesList.get(index);
                     i=vertex.x;
                     j=vertex.y;
                     red=(textureImages[Step.INTRODUCTION.ordinal()].getRGB(i,j)>>16)&0xFF;
                     green=(textureImages[Step.INTRODUCTION.ordinal()].getRGB(i,j)>>8)&0xFF;
                     blue=textureImages[Step.INTRODUCTION.ordinal()].getRGB(i,j)&0xFF;
                     //Replace blue by red
                     if(red==0&&green==0&&blue==255)
                         {textureImages[Step.INTRODUCTION.ordinal()].setRGB(i,j,Color.RED.getRGB());
                          modifiedPixelsCount++;
                         }
                     if(modifiedPixelsCount > maxModifiedPixelsCount)
                         break;
                    }
                //Move the box to the front
                ReadOnlyVector3 translation=caller.getTranslation();
                double z;
                if((z=translation.getZ())<-15)
                    {z=Math.min(z+time*10,-15);
                     caller.setTranslation(translation.getX(),translation.getY(),z);
                    }
            }
        });
    }
    
    private static final String getIllustrationImagePathFromStep(Step step){
        return(step.toString().toLowerCase()+".png");
    }

    private void updateLogicalLayer(final ReadOnlyTimer timer) {
        // check and execute any input triggers, if we are concerned with input
        stateMachine.updateLogicalLayer(timer);
    }

    // ------ Scene methods ------

    public boolean renderUnto(final Renderer renderer){
        final boolean isOpen=!canvas.isClosing();
        if(isOpen)
            {// Draw the root and all its children.
             renderer.draw(root);
             if(stateMachine.isEnabled(Step.INTRODUCTION.ordinal()))
                 {// Update the whole texture so that the display reflects the change
                  // Get the data of the image
                  final byte data[] = AWTImageLoader.asByteArray(textureImages[Step.INTRODUCTION.ordinal()]);
                  // Update the buffer
                  imageBuffers[Step.INTRODUCTION.ordinal()].rewind();
                  imageBuffers[Step.INTRODUCTION.ordinal()].put(data);
                  imageBuffers[Step.INTRODUCTION.ordinal()].rewind();
                  // Get the texture
                  final Texture2D texture=(Texture2D)((TextureState) illustrationBox[Step.INTRODUCTION.ordinal()].getLocalRenderState(
                        StateType.Texture)).getTexture();
                  // Update the texture (the whole texture is updated)
                  renderer.updateTexture2DSubImage(texture, 0, 0,textureImages[Step.INTRODUCTION.ordinal()].getWidth(),textureImages[Step.INTRODUCTION.ordinal()].getHeight(),
                          imageBuffers[Step.INTRODUCTION.ordinal()],0,0,textureImages[Step.INTRODUCTION.ordinal()].getWidth());
                 }
            }
        return(isOpen);
    }

    public PickResults doPick(final Ray3 pickRay){
        // Ignore
        return(null);
    }
}