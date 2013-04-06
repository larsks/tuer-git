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
package engine.movement;

import javax.media.nativewindow.util.Point;
import java.io.Serializable;
import java.nio.ByteBuffer;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.concurrent.Callable;
import misc.SerializationHelper;
import com.ardor3d.image.Image;
import com.ardor3d.image.ImageDataFormat;
import com.ardor3d.image.Texture;
import com.ardor3d.image.Texture2D;
import com.ardor3d.math.ColorRGBA;
import com.ardor3d.math.type.ReadOnlyColorRGBA;
import com.ardor3d.renderer.RenderContext;
import com.ardor3d.renderer.Renderer;
import com.ardor3d.scenegraph.Spatial;
import com.ardor3d.scenegraph.controller.SpatialController;
import com.ardor3d.util.GameTaskQueueManager;
import com.ardor3d.util.TextureManager;
import com.ardor3d.util.geom.BufferUtils;
import com.ardor3d.util.resource.ResourceSource;
import com.ardor3d.util.resource.URLResourceSource;

public abstract class TextureUpdaterController implements Serializable,SpatialController<Spatial>{

	
    static{SerializationHelper.forceHandlingOfTransientModifiersForXMLSerialization(TextureUpdaterController.class);}
    
	private static final long serialVersionUID=1L;
    
	/**resource name of the image to load it with the resource locator*/
	private String imageResourceName;
	
    /**unchanged image*/
    private transient Image originalImage;
	
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
    private HashMap<ReadOnlyColorRGBA,ReadOnlyColorRGBA> colorSubstitutionTable;
	
    /**sorted (chronological update order) list of vertices*/
	private transient ArrayList<Entry<Point,ReadOnlyColorRGBA>> coloredVerticesList;
	
	private transient int updateX,updateY,updateWidth,updateHeight;
	
	private transient int bytesPerPixel;
	
	private transient Renderer renderer;
	
	private transient final RenderContext renderContext;

	
	public TextureUpdaterController(){
	    this(null,null,null,null,null);
	}
	
	public TextureUpdaterController(final String imageResourceName,
	        final MovementEquation equation,
	        final HashMap<ReadOnlyColorRGBA,ReadOnlyColorRGBA> colorSubstitutionTable,
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
        ResourceSource resourceSource=new URLResourceSource(getClass().getResource(imageResourceName));
        //creates texture from resource name
        texture=(Texture2D)TextureManager.load(resourceSource,Texture.MinificationFilter.Trilinear,true);
        //copies the image
        final Image currentImage = texture.getImage();
        final ByteBuffer originalImageData = BufferUtils.createByteBuffer(currentImage.getData(0).capacity());
        originalImageData.put(currentImage.getData(0)).rewind();
        currentImage.getData(0).rewind();
        originalImage=new Image(currentImage.getDataFormat(),currentImage.getDataType(),currentImage.getWidth(),currentImage.getHeight(),originalImageData,null);
        //creates the buffer
        imageBuffer=BufferUtils.createByteBuffer(originalImageData.capacity());
        bytesPerPixel=imageBuffer.capacity()/(texture.getImage().getWidth()*texture.getImage().getHeight());
        //fills the buffer with the data
        imageBuffer.put(originalImageData).rewind();
        originalImageData.rewind();
        //computes effect (compute sorted vertices with color substitution)
        coloredVerticesList=new ArrayList<Entry<Point,ReadOnlyColorRGBA>>();
        //fills
        ReadOnlyColorRGBA sourceColor,destinationColor;
        for(int y=0;y<originalImage.getHeight();y++)
            for(int x=0;x<originalImage.getWidth();x++)
                {final int index=bytesPerPixel*(x+(y*originalImage.getWidth()));
                 sourceColor=new ColorRGBA();
                 final int argb;
                 switch(originalImage.getDataFormat())
                 {case RGB:
                	  argb=(imageBuffer.get(index)<<16)|(imageBuffer.get(index+1)<<8)|(imageBuffer.get(index+2))|(0xFF<<24);
               	      break;
                  case BGR:
                	  argb=(imageBuffer.get(index))|(imageBuffer.get(index+1)<<8)|(imageBuffer.get(index+2)<<16)|(0xFF<<24);
                	  break;
                  case RGBA:
                	  argb=(imageBuffer.get(index)<<16)|(imageBuffer.get(index+1)<<8)|(imageBuffer.get(index+2))|(imageBuffer.get(index+3)<<24);
                	  break;
                  case BGRA:
                	  argb=(imageBuffer.get(index))|(imageBuffer.get(index+1)<<8)|(imageBuffer.get(index+2)<<16)|(imageBuffer.get(index+3)<<24);
                	  break;
                  default:
                	  argb=0;
                 }
                 sourceColor=new ColorRGBA().fromIntARGB(argb);
                 ColorRGBA.BLUE.asIntARGB();
                 destinationColor=colorSubstitutionTable.get(sourceColor);
                 if(destinationColor!=null)
                     coloredVerticesList.add(new AbstractMap.SimpleEntry<Point,ReadOnlyColorRGBA>(new Point(x,y),destinationColor));
                }
        //sorts
        Collections.sort(coloredVerticesList,getColoredPointComparator());
    }
    
    protected abstract Comparator<Entry<Point,ReadOnlyColorRGBA>> getColoredPointComparator();

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
                 {rgbVal=coloredVerticesList.get(i).getValue().asIntARGB();
                  updatedVertex=coloredVerticesList.get(i).getKey();
                  minX=Math.min(minX,updatedVertex.getX());
                  minY=Math.min(minY,updatedVertex.getY());
                  maxX=Math.max(maxX,updatedVertex.getX());
                  maxY=Math.max(maxY,updatedVertex.getY());
                  bufferIndex=bytesPerPixel*(updatedVertex.getX()+(updatedVertex.getY()*originalImage.getWidth()));
                  switch(bytesPerPixel)
                  {case 1:
                   {imageBuffer.put(bufferIndex,(byte)(rgbVal&0xFF));
                    break;
                   }
                   case 3:
                   {if(texture.getImage().getDataFormat()==ImageDataFormat.RGB)
                	    {imageBuffer.put(bufferIndex,(byte)((rgbVal>>16)&0xFF));
                         imageBuffer.put(bufferIndex+1,(byte)((rgbVal>>8)&0xFF));
                         imageBuffer.put(bufferIndex+2,(byte)(rgbVal&0xFF));
                	    }
                    else
                        {if(texture.getImage().getDataFormat()==ImageDataFormat.BGR)
                             {imageBuffer.put(bufferIndex+2,(byte)((rgbVal>>16)&0xFF));
                              imageBuffer.put(bufferIndex+1,(byte)((rgbVal>>8)&0xFF));
                              imageBuffer.put(bufferIndex,(byte)(rgbVal&0xFF));
                             }
                        }
                    break;
                   }
                   case 4:
                   {if(texture.getImage().getDataFormat()==ImageDataFormat.RGBA)
                        {imageBuffer.put(bufferIndex,(byte)((rgbVal>>16)&0xFF));
                         imageBuffer.put(bufferIndex+1,(byte)((rgbVal>>8)&0xFF));
                         imageBuffer.put(bufferIndex+2,(byte)(rgbVal&0xFF));
                         imageBuffer.put(bufferIndex+3,(byte)((rgbVal>>24)&0xFF));
                        }
                    else
                        {if(texture.getImage().getDataFormat()==ImageDataFormat.BGRA)
                            {imageBuffer.put(bufferIndex+2,(byte)((rgbVal>>16)&0xFF));
                             imageBuffer.put(bufferIndex+1,(byte)((rgbVal>>8)&0xFF));
                             imageBuffer.put(bufferIndex,(byte)(rgbVal&0xFF));
                             imageBuffer.put(bufferIndex+3,(byte)((rgbVal>>24)&0xFF));
                            }
                        }
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
	    final ByteBuffer originalImageData=originalImage.getData(0);
	    imageBuffer.rewind();
        imageBuffer.put(originalImageData).rewind();
        originalImageData.rewind();
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

    public final HashMap<ReadOnlyColorRGBA,ReadOnlyColorRGBA> getColorSubstitutionTable(){
        return(colorSubstitutionTable);
    }

    public final void setColorSubstitutionTable(final HashMap<ReadOnlyColorRGBA,ReadOnlyColorRGBA> colorSubstitutionTable){
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
