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
import java.awt.Point;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.Serializable;
import java.nio.ByteBuffer;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.concurrent.Callable;

import javax.imageio.ImageIO;
import misc.SerializationHelper;

import com.ardor3d.framework.NativeCanvas;
import com.ardor3d.image.Texture;
import com.ardor3d.image.Texture2D;
import com.ardor3d.image.Image.Format;
import com.ardor3d.image.util.AWTImageLoader;
import com.ardor3d.renderer.RenderContext;
import com.ardor3d.renderer.Renderer;
import com.ardor3d.scenegraph.Spatial;
import com.ardor3d.scenegraph.controller.SpatialController;
import com.ardor3d.util.GameTaskQueueManager;
import com.ardor3d.util.TextureManager;
import com.ardor3d.util.geom.BufferUtils;
import com.ardor3d.util.resource.ResourceLocatorTool;
import com.ardor3d.util.resource.ResourceSource;

public abstract class TextureUpdaterController implements Serializable,SpatialController<Spatial>{

	
    static{SerializationHelper.forceHandlingOfTransientModifiersForXMLSerialization(TextureUpdaterController.class);}
    
	private static final long serialVersionUID=1L;
    
	/**resource name of the image to load it with the resource locator*/
	private String imageResourceName;
	
    /**unchanged image*/
    private transient BufferedImage originalImage;
	
	/**buffer containing the modified data of the image*/
	private transient ByteBuffer imageBuffer;
	
	/**texture modified at runtime*/
	private transient Texture2D texture;
	
	/**equation used to know which pixels can be scanned*/
	private MovementEquation equation;
	
	/**elapsed time in seconds*/
    private transient double elapsedTime;
    
    private transient boolean inited;
    
    /**table matching source colors and destination colors*/
    private HashMap<Color,Color> colorSubstitutionTable;
	
    /**sorted (chronological update order) list of vertices*/
	private transient ArrayList<Entry<Point,Color>> coloredVerticesList;
	
	private transient int updateX,updateY,updateWidth,updateHeight;
	
	private transient int bytesPerPixel;
	
	private transient Renderer renderer;
	
	private transient final RenderContext renderContext;

	
	public TextureUpdaterController(){
	    this(null,null,null,null,null);
	}
	
	public TextureUpdaterController(final String imageResourceName,
	        final MovementEquation equation,
	        final HashMap<Color,Color> colorSubstitutionTable,
	        final Renderer renderer,final RenderContext renderContext){
        this.imageResourceName=imageResourceName;
	    this.equation=equation;
        this.colorSubstitutionTable=colorSubstitutionTable;
        this.renderer=renderer;
        this.renderContext=renderContext;
	    elapsedTime=0;
	    inited=false;
	}
	
	
    private final void init(){
        ResourceSource resourceSource=ResourceLocatorTool.locateResource(ResourceLocatorTool.TYPE_TEXTURE,imageResourceName);
        //create texture from resource name
        texture=(Texture2D)TextureManager.load(imageResourceName,Texture.MinificationFilter.Trilinear,Format.GuessNoCompression,true);
        //load the image
        try{originalImage=ImageIO.read(resourceSource.openStream());}
        catch(IOException ioe)
        {ioe.printStackTrace();}
        //flip the image
        AffineTransform flipVerticallyTr=AffineTransform.getScaleInstance(1,-1);
        flipVerticallyTr.translate(0,-originalImage.getHeight());
        AffineTransformOp flipVerticallyOp=new AffineTransformOp(flipVerticallyTr,AffineTransformOp.TYPE_NEAREST_NEIGHBOR);
        originalImage=flipVerticallyOp.filter(originalImage,null);
        //scale image if needed
        if(originalImage.getWidth()!=texture.getImage().getWidth()||originalImage.getHeight()!=texture.getImage().getHeight())
            {final AffineTransform scaleTr=AffineTransform.getScaleInstance((double)texture.getImage().getWidth()/originalImage.getWidth(),(double)texture.getImage().getHeight()/originalImage.getHeight());
             final AffineTransformOp scaleOp=new AffineTransformOp(scaleTr,AffineTransformOp.TYPE_NEAREST_NEIGHBOR);
             originalImage=scaleOp.filter(originalImage,null);
            }
        //create the buffer
        final byte[] data=AWTImageLoader.asByteArray(originalImage);
        imageBuffer=BufferUtils.createByteBuffer(data.length);
        bytesPerPixel=data.length/(originalImage.getWidth()*originalImage.getHeight());
        //fill the buffer with the data
        imageBuffer.put(data);
        imageBuffer.rewind();
        //compute effect (compute sorted vertices with color substitution)
        coloredVerticesList=new ArrayList<Entry<Point,Color>>();
        //fill
        Color sourceColor,destinationColor;
        for(int y=0;y<originalImage.getHeight();y++)
            for(int x=0;x<originalImage.getWidth();x++)
                {sourceColor=new Color(originalImage.getRGB(x,y));
                 destinationColor=colorSubstitutionTable.get(sourceColor);
                 if(destinationColor!=null)
                     coloredVerticesList.add(new AbstractMap.SimpleEntry<Point,Color>(new Point(x,y),destinationColor));
                }
        //sort
        Collections.sort(coloredVerticesList,getColoredPointComparator());
    }
    
    protected abstract Comparator<Entry<Point,Color>> getColoredPointComparator();

	/**
	 * 
	 * @param elapsedTime
	 * @return
	 */
	protected final int getScannablePixelsCount(final double elapsedTime){
	    return((int)Math.ceil(equation.getValueAtTime(elapsedTime)));
	}
	
	@Override
    public final void update(final double timeSinceLastCall,final Spatial caller){
		if(!inited)
		    {init();
		     inited=true;
		    }
        //get previous elapsed time
        final double previousElapsedTime=elapsedTime;
        final int updatedPixelsCount=getScannablePixelsCount(previousElapsedTime);
        //update elapsed time
        elapsedTime+=timeSinceLastCall;
        //use the movement equation
        final int updatablePixelsCount=Math.max(0,getScannablePixelsCount(elapsedTime)-updatedPixelsCount);		
        if(updatablePixelsCount>0)
            {//modify the buffer
             Point updatedVertex;
             int rgbVal,bufferIndex,minX=originalImage.getWidth(),minY=originalImage.getHeight(),maxX=-1,maxY=-1;
             for(int i=updatedPixelsCount;i<updatedPixelsCount+updatablePixelsCount;i++)
                 {rgbVal=coloredVerticesList.get(i).getValue().getRGB();
                  updatedVertex=coloredVerticesList.get(i).getKey();
                  minX=Math.min(minX,updatedVertex.x);
                  minY=Math.min(minY,updatedVertex.y);
                  maxX=Math.max(maxX,updatedVertex.x);
                  maxY=Math.max(maxY,updatedVertex.y);
                  bufferIndex=bytesPerPixel*(updatedVertex.x+(updatedVertex.y*originalImage.getWidth()));
                  switch(bytesPerPixel)
                  {case 1:
                   {imageBuffer.put(bufferIndex,(byte)(rgbVal&0xFF));
                    break;
                   }
                   case 3:
                   {imageBuffer.put(bufferIndex,(byte)((rgbVal>>16)&0xFF));
                    imageBuffer.put(bufferIndex+1,(byte)((rgbVal>>8)&0xFF));
                    imageBuffer.put(bufferIndex+2,(byte)(rgbVal&0xFF));
                    break;
                   }
                   case 4:
                   {imageBuffer.put(bufferIndex,(byte)((rgbVal>>16)&0xFF));
                    imageBuffer.put(bufferIndex+1,(byte)((rgbVal>>8)&0xFF));
                    imageBuffer.put(bufferIndex+2,(byte)(rgbVal&0xFF));
                    imageBuffer.put(bufferIndex+3,(byte)((rgbVal>>24)&0xFF));
                    break;
                   }
                  }
                 }
             if(minX<originalImage.getWidth())
                 {//compute the zone that needs an update
                  updateX=minX;
                  updateY=minY;
                  updateWidth=maxX-minX+1;
                  updateHeight=maxY-minY+1;
                  //update the texture on the rendering thread
                  GameTaskQueueManager.getManager(renderContext).render(new Callable<Void>(){
                      @Override
                      public Void call() throws Exception{
                          updateTexture();
                          return(null);
                      }
                  });
                 }
             else
                 {updateX=0;
                  updateY=0;
                  updateWidth=0;
                  updateHeight=0;
                 }
            }
	}
	
	/**update the texture*/
	private final void updateTexture(){
	    //modify the texture by using the image data
	    renderer.updateTexture2DSubImage(texture,updateX,updateY,updateWidth,updateHeight,imageBuffer,updateX,updateY,texture.getImage().getWidth());
	}
	
	public final void reset(){
	    elapsedTime=0;
	    //put the data of the original image back into the buffer
	    final byte[] data=AWTImageLoader.asByteArray(originalImage);
	    imageBuffer.rewind();
        imageBuffer.put(data);
        imageBuffer.rewind();
	}

    public final String getImageResourceName(){
        return(imageResourceName);
    }

    public final void setImageResourceName(final String imageResourceName){
        this.imageResourceName=imageResourceName;
    }

    public final MovementEquation getEquation(){
        return(equation);
    }

    public final void setEquation(final MovementEquation equation){
        this.equation=equation;
    }

    public final HashMap<Color, Color> getColorSubstitutionTable(){
        return(colorSubstitutionTable);
    }

    public final void setColorSubstitutionTable(final HashMap<Color,Color> colorSubstitutionTable){
        this.colorSubstitutionTable=colorSubstitutionTable;
    }

    public final int getUpdateX() {
        return(updateX);
    }

    public final void setUpdateX(final int updateX){
        this.updateX=updateX;
    }

    public final int getUpdateY(){
        return(updateY);
    }

    public final void setUpdateY(final int updateY){
        this.updateY=updateY;
    }

    public final int getUpdateWidth(){
        return(updateWidth);
    }

    public final void setUpdateWidth(final int updateWidth){
        this.updateWidth=updateWidth;
    }

    public final int getUpdateHeight(){
        return(updateHeight);
    }

    public final void setUpdateHeight(final int updateHeight){
        this.updateHeight=updateHeight;
    }
}
