package engine;

import java.awt.Color;
import java.awt.Point;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import javax.imageio.ImageIO;
import com.ardor3d.bounding.BoundingBox;
import com.ardor3d.image.Texture;
import com.ardor3d.image.Texture2D;
import com.ardor3d.image.Image.Format;
import com.ardor3d.image.util.AWTImageLoader;
import com.ardor3d.math.Vector3;
import com.ardor3d.math.type.ReadOnlyVector3;
import com.ardor3d.renderer.Renderer;
import com.ardor3d.renderer.state.TextureState;
import com.ardor3d.renderer.state.RenderState.StateType;
import com.ardor3d.scenegraph.controller.SpatialController;
import com.ardor3d.scenegraph.shape.Box;
import com.ardor3d.util.TextureManager;
import com.ardor3d.util.geom.BufferUtils;
import com.ardor3d.util.resource.ResourceLocatorTool;

/**
 * class that handles specific behaviors of TUER by overriding
 * the JFPSM template
 *
 */
public final class TuerServiceProvider extends Ardor3DGameServiceProvider {

    
    /**
     *  The image that is dynamically modified
     */
    private BufferedImage europeImage;

    /**
     * The image buffer used to update the texture
     */
    private ByteBuffer imageBuffer;
    
    private Box europeBox;
    
    private static final String europeImagePath="images/europe_map.png";
    
    
    protected final void init(){
        super.init();
        europeBox=new Box("Box", Vector3.ZERO, 12, 9, 5);
        europeBox.setModelBound(new BoundingBox());

        // Set its location in space.
        europeBox.setTranslation(new Vector3(0, 0, -75));

        // Add the box to the node that handles the second state in the state machine
        getStateMachine().attachChild(1,europeBox);
        
        // Load the image
        try {europeImage=ImageIO.read(ResourceLocatorTool.locateResource(ResourceLocatorTool.TYPE_TEXTURE,
                europeImagePath).openStream());
        } 
        catch(IOException ioe)
        {ioe.printStackTrace();}
        
        //Set a texture state to the box
        final TextureState ts = new TextureState();
        ts.setEnabled(true);
        ts.setTexture(TextureManager.load(europeImagePath,Texture.MinificationFilter.Trilinear,
                Format.GuessNoCompression,true));
        europeBox.setRenderState(ts);
        //FIXME: resize the image if its size is not valid
        //ts.getTexture().getImage().getWidth();
        // Flip the image vertically (I do not know why it is necessary)
        final AffineTransform tx = AffineTransform.getScaleInstance(1, -1);
        tx.translate(0,-europeImage.getHeight(null));
        final AffineTransformOp op = new AffineTransformOp(tx, AffineTransformOp.TYPE_NEAREST_NEIGHBOR);
        europeImage=op.filter(europeImage,null);
        
        final byte data[] = AWTImageLoader.asByteArray(europeImage);
        imageBuffer=BufferUtils.createByteBuffer(data.length);
        
        //Spread effect
        final ArrayList<Point> verticesList=new ArrayList<Point>();
        for(int i = 0; i < europeImage.getWidth(); i++)
            for(int j = 0; j < europeImage.getHeight(); j++)
                verticesList.add(new Point(i,j));
        final Point center=new Point(europeImage.getWidth()/2,europeImage.getHeight()/2);
        Collections.sort(verticesList,new Comparator<Point>(){
            @Override
            public int compare(Point p1, Point p2) {
                double d1=p1.distance(center);
                double d2=p2.distance(center);
                return(d1==d2?0:d1<d2?-1:1);
            }           
        });
        
        //Set a controller that modifies the image
        europeBox.addController(new SpatialController<Box>(){
            
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
                     red=(europeImage.getRGB(i,j)>>16)&0xFF;
                     green=(europeImage.getRGB(i,j)>>8)&0xFF;
                     blue=europeImage.getRGB(i,j)&0xFF;
                     //Replace blue by red
                     if(red==0&&green==0&&blue==255)
                         {europeImage.setRGB(i,j,Color.RED.getRGB());
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
        // Load collada model

        /*
         * try { final Node colladaNode = ColladaImporter.readColladaScene("collada/duck/duck.dae");
         * _root.attachChild(colladaNode); } catch (final Exception ex) { ex.printStackTrace(); }
         */
    }
    
    public final boolean renderUnto(final Renderer renderer){
        final boolean result;
        if(result=super.renderUnto(renderer))
            {if(getStateMachine().isEnabled(1))
                 {// Update the whole texture so that the display reflects the change
                  // Get the data of the image
                  final byte data[] = AWTImageLoader.asByteArray(europeImage);
                  // Update the buffer
                  imageBuffer.rewind();
                  imageBuffer.put(data);
                  imageBuffer.rewind();
                  // Get the texture
                  final Texture2D texture = (Texture2D) ((TextureState) europeBox.getLocalRenderState(
                        StateType.Texture)).getTexture();
                  // Update the texture (the whole texture is updated)
                  renderer.updateTexture2DSubImage(texture, 0, 0, europeImage.getWidth(), europeImage.getHeight(),
                        imageBuffer, 0, 0, europeImage.getWidth());
                 }
            }
        return(result);
    }
    
    public static void main(final String[] args){
        final TuerServiceProvider application=new TuerServiceProvider();
        application.start();
    }
}
