/**
 * Copyright (c) 2006-2016 Julien Gouesse
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package engine.misc;

import java.nio.ByteBuffer;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import com.ardor3d.image.Image;
import com.ardor3d.image.Texture;
import com.ardor3d.image.util.ImageUtils;
import com.ardor3d.image.util.jogl.JoglImageLoader;
import com.ardor3d.math.ColorRGBA;
import com.ardor3d.math.type.ReadOnlyColorRGBA;
import com.ardor3d.scenegraph.FloatBufferData;
import com.ardor3d.scenegraph.MeshData;
import com.ardor3d.util.TextureManager;
import com.ardor3d.util.geom.BufferUtils;
import com.ardor3d.util.resource.URLResourceSource;
import com.jogamp.nativewindow.util.Point;

import engine.movement.MovementEquation;
import engine.movement.UniformlyVariableMovementEquation;

/**
 * The introduction state shows a map of Europe becoming red, it modifies the texture data at runtime which seems to be slow on some hardware.
 * This test tries to replace the texture data updates by some VBOs.
 * 
 * @author Julien Gouesse
 *
 */
public class TestIntroductionReimplementation{

	private static final String textureFilePath="/images/introduction.png";
	
	public static void main(final String[] args){
		JoglImageLoader.registerLoader();
		final Texture introTexture=TextureManager.load(new URLResourceSource(TestIntroductionReimplementation.class.getResource(textureFilePath)),Texture.MinificationFilter.Trilinear,true);
		final Image introImage=introTexture.getImage();
		final int durationInSeconds=10;
		final int framesPerSecond=30;
		final int frameCount=durationInSeconds*framesPerSecond;
		final List<MeshData> meshDataList=new ArrayList<>();
		final Point spreadCenter=new Point(205,265);
		final MovementEquation equation=new UniformlyVariableMovementEquation(0,10000,0);
		HashMap<ReadOnlyColorRGBA,ReadOnlyColorRGBA> colorSubstitutionTable=new HashMap<>();
        colorSubstitutionTable.put(ColorRGBA.BLUE,ColorRGBA.RED);
		final ArrayList<Entry<Point,ReadOnlyColorRGBA>> coloredVerticesList=new ArrayList<>();
		//fills
        ReadOnlyColorRGBA sourceColor,destinationColor;
        for(int y=0;y<introImage.getHeight();y++)
            for(int x=0;x<introImage.getWidth();x++)
                {final int argb=ImageUtils.getARGB(introImage,x,y);
                 sourceColor=new ColorRGBA().fromIntARGB(argb);
                 destinationColor=colorSubstitutionTable.get(sourceColor);
                 if(destinationColor!=null)
                     coloredVerticesList.add(new AbstractMap.SimpleEntry<>(new Point(x,y),destinationColor));
                }
        //sorts
        Collections.sort(coloredVerticesList,new CenteredColoredPointComparator(spreadCenter));
        //creates one image per frame
        final Image[] introImages=new Image[frameCount];
        //for each frame
      	for(int frameIndex=0;frameIndex<frameCount;frameIndex++)
      	    {//gets the image of the previous frame
      		 final Image previousImage=frameIndex==0?introImage:introImages[frameIndex-1];
      		 //copies its image data
      		 final ByteBuffer data=BufferUtils.clone(previousImage.getData(0));
      	     data.rewind();
      	     //creates the image by copying this previous image
      		 final Image image=new Image(previousImage.getDataFormat(),previousImage.getDataType(),previousImage.getWidth(),previousImage.getHeight(),data,null);
      	     //performs the modification of this image for this frame
      		 final double previousElapsedTime=frameIndex==0?0:(frameIndex-1)/(double)framesPerSecond;
			 final double elapsedTime=frameIndex/(double)framesPerSecond;
			 final int updatedPixelsCount=getScannablePixelsCount(equation,previousElapsedTime);
			 final int updatablePixelsCount=Math.max(0,getScannablePixelsCount(equation,elapsedTime)-updatedPixelsCount);
			 //if there are some pixels to update
			 if(updatablePixelsCount>0)
			     {//updates the pixels (incrementally)
			      for(int i=updatedPixelsCount;i<updatedPixelsCount+updatablePixelsCount;i++)
		              {final int argb=coloredVerticesList.get(i).getValue().asIntARGB();
		               final Point updatedVertex=coloredVerticesList.get(i).getKey();
		               ImageUtils.setARGB(image,updatedVertex.getX(),updatedVertex.getY(),argb);
		              }
			     }
      		 //stores the new image into the array
      		 introImages[frameIndex]=image;
      	    }
      	//the generic treatment starts here
      	//detects the pixels whose color doesn't change
      	final int[][] argbUnchangedPixelsArray=new int[introImage.getHeight()][introImage.getWidth()];
      	//for each ordinate
      	for(int y=0;y<introImage.getHeight();y++)
            //for each abscissa
      		for(int x=0;x<introImage.getWidth();x++)
            	{//assumes the pixel's color doesn't change
            	 boolean identical=true;
            	 //for each frame
            	 for(int frameIndex=1;frameIndex<frameCount&&identical;frameIndex++)
                     {//retrieves the pixels at this coordinate in the image of the current frame and in the image of the previous frame
            		  final Image previousIntroImage=introImages[frameIndex-1];
            		  final Image currentIntroImage=introImages[frameIndex];
            		  //compares them
            		  identical=ImageUtils.getARGB(previousIntroImage,x,y)==ImageUtils.getARGB(currentIntroImage,x,y);
                     }
            	 //if the pixel is identical at this coordinate in all frames
            	 if(identical)
            	     {//saves the color if and only if it's not a fully transparent color
            		  if(ImageUtils.getRGBA(introImage,x,y,null).getAlpha()==0)
            			  argbUnchangedPixelsArray[y][x]=ColorRGBA.BLACK_NO_ALPHA.asIntARGB();
            		  else
            		      argbUnchangedPixelsArray[y][x]=ImageUtils.getARGB(introImage,x,y);
            		  //for each image
            		  for(final Image frameIntroImage:introImages)
            		      {//marks the constant pixel fully transparent so that it's not treated with the non constant pixels
            			   ImageUtils.setARGB(frameIntroImage,x,y,ColorRGBA.BLACK_NO_ALPHA.asIntARGB());
            		      }
            	     }
            	 else
            	     {//marks the non constant pixel fully transparent in this array so that it's not treated with the constant pixels
            		  argbUnchangedPixelsArray[y][x]=ColorRGBA.BLACK_NO_ALPHA.asIntARGB();
            	     }
            	}
      	//TODO use the array helper to compute full arrays
        
        /*for(int y=0;y<introImage.getHeight();y++)
            for(int x=0;x<introImage.getWidth();x++)
                {//TODO fill it, its data won't change
            	 //TODO six vertices
            	 //TODO first triangle
            	 //TODO second triangle
                }*/
		//for each frame
		for(int frameIndex=0;frameIndex<frameCount;frameIndex++)
		    {//TODO computes the triangle count from the full arrays
			 final int triCount=introImage.getWidth()*introImage.getHeight()*2;//w * h * number of triangles per cell in the worst case
			 final MeshData meshData=new MeshData();
			 //creates the vertex buffer (indirect NIO buffer), triangle count * number of vertices per triangle * floats per vertex (only 2 as we're in 2D)
             final FloatBufferData vertexBufferData=new FloatBufferData(BufferUtils.createFloatBufferOnHeap(triCount*3*2),2);
			 //sets this vertex buffer to the mesh data
			 meshData.setVertexCoords(vertexBufferData);
		     //creates the color buffer (indirect NIO buffer), triangle count * number of vertices per triangle * floats per color (no alpha, RGB)
			 final FloatBufferData colorBufferData=new FloatBufferData(BufferUtils.createFloatBufferOnHeap(triCount*3*3),3);
		     //sets this color buffer to the mesh data
			 meshData.setColorCoords(colorBufferData);
             //TODO add the vertices and the colors of the arrays into the vertex buffer and the color buffer
			 
			 meshDataList.add(meshData);
		    }
		//TODO create a SwitchNode
		//TODO create a Node per frame
		//TODO add a MeshData into each Node
		//TODO create a custom controller that just picks a frame with no interpolation
		//TODO show the result
	}
	
	private static final int getScannablePixelsCount(final MovementEquation equation,final double elapsedTime){
	    return((int)Math.ceil(equation.getValueAtTime(elapsedTime)));
	}
	
    private static final class CenteredColoredPointComparator implements Comparator<Entry<Point,ReadOnlyColorRGBA>>{
        
        private final Point spreadCenter;
        
        private CenteredColoredPointComparator(final Point spreadCenter){
            this.spreadCenter=spreadCenter;
        }
        
        
        @Override
        public final int compare(final Entry<Point,ReadOnlyColorRGBA> o1,
                                 final Entry<Point,ReadOnlyColorRGBA> o2){
            final Point p1=o1.getKey();
            final Point p2=o2.getKey();
            double d1=distance(p1,spreadCenter);
            double d2=distance(p2,spreadCenter);
            return(d1==d2?0:d1<d2?-1:1);
        } 
    }
    
    private static double distance(final Point p1,final Point p2) {
    	double abscissaSub=p2.getX()-p1.getX();
    	double ordinateSub=p2.getY()-p1.getY();
    	return Math.sqrt((abscissaSub*abscissaSub)+(ordinateSub*ordinateSub));
    }
}
