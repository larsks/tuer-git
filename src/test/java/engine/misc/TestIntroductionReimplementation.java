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
import java.nio.FloatBuffer;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;

import com.ardor3d.image.Image;
import com.ardor3d.image.Texture;
import com.ardor3d.image.util.ImageUtils;
import com.ardor3d.image.util.jogl.JoglImageLoader;
import com.ardor3d.math.ColorRGBA;
import com.ardor3d.math.type.ReadOnlyColorRGBA;
import com.ardor3d.scenegraph.FloatBufferData;
import com.ardor3d.scenegraph.Mesh;
import com.ardor3d.scenegraph.MeshData;
import com.ardor3d.scenegraph.controller.ComplexSpatialController;
import com.ardor3d.scenegraph.controller.ComplexSpatialController.RepeatType;
import com.ardor3d.scenegraph.extension.SwitchNode;
import com.ardor3d.util.TextureManager;
import com.ardor3d.util.geom.BufferUtils;
import com.ardor3d.util.resource.URLResourceSource;
import com.jogamp.nativewindow.util.Point;

import engine.movement.MovementEquation;
import engine.movement.UniformlyVariableMovementEquation;
import jfpsm.ArrayHelper;
import jfpsm.ArrayHelper.OccupancyCheck;
import jfpsm.ArrayHelper.Vector2i;

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
        System.out.println("[START] Fill color table");
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
        System.out.println("[ END ] Fill color table");
        //sorts
        Collections.sort(coloredVerticesList,new CenteredColoredPointComparator(spreadCenter));
        System.out.println("[START] Compute key frames images");
        //creates one image per frame
        final Image[] introImages=new Image[frameCount];
        //for each frame
      	for(int frameIndex=0;frameIndex<frameCount;frameIndex++)
      	    {System.out.println("[START] Compute key frames image "+frameIndex);
      		 //gets the image of the previous frame
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
      		 System.out.println("[ END ] Compute key frames image "+frameIndex);
      	    }
      	System.out.println("[ END ] Compute key frames images");
      	//the generic treatment starts here
      	System.out.println("[START] Compute key frames");
		//for each frame
		for(int frameIndex=0;frameIndex<frameCount;frameIndex++)
		    {System.out.println("[START] Compute key frame "+frameIndex);
			 //retrieves the image of the frame
			 final Image image=introImages[frameIndex];
			 //retrieves the pixels of the image
			 final Integer[][] pixels=new Integer[image.getHeight()][image.getWidth()];
			 for(int y=0;y<introImage.getHeight();y++)
		            for(int x=0;x<introImage.getWidth();x++)
		            	{//gets the ARGB value of the pixel
		            	 final int argb=ImageUtils.getARGB(introImage,x,y);
		            	 final int alpha=(byte)(argb>>24)&0xFF;
		            	 //keeps only non fully transparent pixels
		            	 if(alpha>0)
		            	     pixels[y][x]=Integer.valueOf(argb);
		            	}
			 //creates the array helper
			 final ArrayHelper arrayHelper=new ArrayHelper();
			 //uses the array helper to compute full arrays (without fully transparent pixels)
			 final Map<Vector2i,Integer[][]> fullPixelsArraysMap=arrayHelper.computeFullArraysFromNonFullArray(pixels);
			 final Map<Vector2i,Integer[][]> globalDistinctColorsPixelsArraysMap=new HashMap<>();
			 //loops on all full arrays, there is no risk of having any holes
			 for(final Entry<Vector2i,Integer[][]> fullPixelsArrayEntry:fullPixelsArraysMap.entrySet())
			     {//retrieves where the pixels come from
				  final Vector2i location=fullPixelsArrayEntry.getKey();
				  //retrieves the non fully transparent pixels
				  final Integer[][] nonFullyTransparentPixels=fullPixelsArrayEntry.getValue();
				  //computes the list of colors in this array
				  final Set<Integer> colors=new HashSet<>();
				  for(int y=0;y<nonFullyTransparentPixels.length;y++)
			          for(int x=0;x<nonFullyTransparentPixels[y].length;x++)
			        	  colors.add(nonFullyTransparentPixels[y][x]);
				  //for each color
				  for(final Integer color:colors)
				      {//builds an occupancy check to keep the pixels of a single color, 
					   final OccupancyCheck<Integer> colorFilterOccupancyCheck=new IntegerFilterOccupancyCheck(color);
					   //uses it to build some full arrays with distinct colors
					   final Map<Vector2i,Integer[][]> localDistinctColorsPixelsArraysMap=arrayHelper.computeFullArraysFromNonFullArray(nonFullyTransparentPixels,colorFilterOccupancyCheck);
					   for(final Entry<Vector2i,Integer[][]> localDistinctColorsPixelsArraysEntry:localDistinctColorsPixelsArraysMap.entrySet())
					       {//retrieves where the pixels come from
							final Vector2i subLocation=localDistinctColorsPixelsArraysEntry.getKey();
							//computes the right location
							final Vector2i globalLocation=new Vector2i(subLocation.getX()+location.getX(),subLocation.getY()+location.getY());
							//retrieves the non fully transparent pixels
							final Integer[][] localDistinctColorsPixels=localDistinctColorsPixelsArraysEntry.getValue();
							//stores the full array with distinct colors
							globalDistinctColorsPixelsArraysMap.put(globalLocation,localDistinctColorsPixels);
					       }
				      }
			     }
			 //computes the triangle count
			 int triCount=0;
			 //for each array of pixels of the same color
			 for(final Integer[][] globalDictinctColorPixels:globalDistinctColorsPixelsArraysMap.values())
				 {//one pair of triangles per pixel, the array is full (all rows and all columns have respectively the same sizes)
				  triCount+=(globalDictinctColorPixels.length*globalDictinctColorPixels[0].length)*2;
				 }
			 final MeshData meshData=new MeshData();
			 //creates the vertex buffer (indirect NIO buffer), triangle count * number of vertices per triangle * floats per vertex (only 2 as we're in 2D)
			 final FloatBuffer vertexBuffer=BufferUtils.createFloatBufferOnHeap(triCount*3*2);
             final FloatBufferData vertexBufferData=new FloatBufferData(vertexBuffer,2);
			 //sets this vertex buffer to the mesh data
			 meshData.setVertexCoords(vertexBufferData);
		     //creates the color buffer (indirect NIO buffer), triangle count * number of vertices per triangle * floats per color (no alpha, RGB)
			 final FloatBuffer colorBuffer=BufferUtils.createFloatBufferOnHeap(triCount*3*3);
			 final FloatBufferData colorBufferData=new FloatBufferData(colorBuffer,3);
		     //sets this color buffer to the mesh data
			 meshData.setColorCoords(colorBufferData);
             //adds the vertices and the colors of the arrays into the vertex buffer and the color buffer
			 for(final Entry<Vector2i,Integer[][]> globalDictinctColorPixelsEntry:globalDistinctColorsPixelsArraysMap.entrySet())
			     {//retrieves where the pixels come from
				  final Vector2i location=globalDictinctColorPixelsEntry.getKey();
				  //retrieves the pixels of the same color
				  final Integer[][] globalDictinctColorPixels=globalDictinctColorPixelsEntry.getValue();
				  final int height=globalDictinctColorPixels.length;
				  final int width=globalDictinctColorPixels[0].length;
				  //computes the coordinates of the 4 vertices
				  final int x0=location.getX();
				  final int y0=location.getY();
				  final int x1=location.getX()+width;
				  final int y1=location.getY();
				  final int x2=location.getX()+width;
				  final int y2=location.getY()+height;
				  final int x3=location.getX();
				  final int y3=location.getY()+height;
				  //puts the vertices into the vertex buffer to build a pair of triangles
				  vertexBuffer.put(x0).put(y0).put(x1).put(y1).put(x2).put(y2).put(x2).put(y2).put(x3).put(y3).put(x0).put(y0);
				  //retrieves the ARGB color
				  final int argb=globalDictinctColorPixels[0][0].intValue();
				  final int red=(byte) (argb >> 16) & 0xFF;
				  final int green=(byte) (argb >> 8) & 0xFF;
				  final int blue=(byte) argb & 0xFF;
				  //puts the color into the color buffer
				  colorBuffer.put(red).put(green).put(blue);
				  colorBuffer.put(red).put(green).put(blue);
				  colorBuffer.put(red).put(green).put(blue);
				  colorBuffer.put(red).put(green).put(blue);
				  colorBuffer.put(red).put(green).put(blue);
				  colorBuffer.put(red).put(green).put(blue);
			     }
			 vertexBuffer.rewind();
			 colorBuffer.rewind();
			 meshDataList.add(meshData);
			 System.out.println("[ END ] Compute key frame "+frameIndex);
		    }
		System.out.println("[END] Compute key frames");
		System.out.println("[START] Normalize vertex coordinates");
		//computes the minimal and maximal values of the vertex coordinates, then normalize them
		float minx=Float.POSITIVE_INFINITY,miny=Float.POSITIVE_INFINITY,minz=Float.POSITIVE_INFINITY;
		float maxx=Float.NEGATIVE_INFINITY,maxy=Float.NEGATIVE_INFINITY,maxz=Float.NEGATIVE_INFINITY;
		for(final MeshData meshData:meshDataList)
		    {final FloatBuffer vertexBuffer=meshData.getVertexBuffer();
			 while(vertexBuffer.hasRemaining())
			     {float x=vertexBuffer.get(),y=vertexBuffer.get(),z=vertexBuffer.get();
				  minx=Math.min(minx,x);
			      miny=Math.min(miny,y);
			      minz=Math.min(minz,z);
			      maxx=Math.max(maxx,x);
			      maxy=Math.max(maxy,y);
			      maxz=Math.max(maxz,z);
			     }
			 vertexBuffer.rewind();
		    }
		//normalizes them
		final float xdiff=maxx-minx;
		final float ydiff=maxy-miny;
		final float zdiff=maxz-minz;
		for(final MeshData meshData:meshDataList)
	        {final FloatBuffer vertexBuffer=meshData.getVertexBuffer();
		     while(vertexBuffer.hasRemaining())
		         {final int pos=vertexBuffer.position();
		    	  float x=vertexBuffer.get(),y=vertexBuffer.get(),z=vertexBuffer.get();
		    	  vertexBuffer.position(pos);
		    	  vertexBuffer.put(xdiff==0?x-minx:(x-minx)/xdiff);
		    	  vertexBuffer.put(ydiff==0?y-miny:(y-miny)/ydiff);
		    	  vertexBuffer.put(zdiff==0?z-minz:(z-minz)/zdiff);
		         }
		     vertexBuffer.rewind();
	        }
		System.out.println("[ END ] Normalize vertex coordinates");
		//creates a SwitchNode containing all meshes of the frames
		System.out.println("[START] Build switch node");
		final SwitchNode switchNode=new SwitchNode();
		int frameIndex=0;
		for(final MeshData meshData:meshDataList)
		    {//creates a Mesh for this frame
			 final Mesh mesh=new Mesh("frame n°"+frameIndex);
			 mesh.setMeshData(meshData);
			 //adds it into the SwitchNode
			 switchNode.attachChild(mesh);
			 frameIndex++;
		    }
		//creates a custom controller that just picks a frame with no interpolation
		final BasicKeyframeController controller=new BasicKeyframeController(framesPerSecond);
		controller.setRepeatType(RepeatType.CLAMP);
		controller.setActive(true);
		controller.setMinTime(0);
		controller.setMaxTime(durationInSeconds);
		switchNode.addController(controller);
		System.out.println("[ END ] Build switch node");
		//TODO show the result
	}
	
	private static final class BasicKeyframeController extends ComplexSpatialController<SwitchNode>{

		private static final long serialVersionUID = 1L;

		private double startTime;
		
		private final int framesPerSecond;
		
		private BasicKeyframeController(final int framesPerSecond){
			super();
			startTime=Double.NaN;
			this.framesPerSecond=framesPerSecond;
		}
		
		@Override
		public final void update(final double time,final SwitchNode caller){
			if(Double.isNaN(startTime))
				startTime=Double.valueOf(time);
			final double elapsedTime=time-startTime;
			final int frameCount=(int)(getMaxTime()*framesPerSecond);
			final int frameIndex;
			switch(getRepeatType())
			    {case CLAMP:
			    	 {frameIndex=Math.min(frameCount-1,(int)(Math.min(elapsedTime,getMaxTime())*framesPerSecond));
			          break;
			    	 }
			     case WRAP:
			         {frameIndex=((int)(elapsedTime*framesPerSecond))%frameCount;
			          break;
			         }
			     case CYCLE:
			         {final int tmpFrameIndex=(int)(elapsedTime*framesPerSecond);
			          if((tmpFrameIndex/frameCount)%2==0)
			        	  frameIndex=tmpFrameIndex%frameCount;
			          else
			        	  frameIndex=(frameCount-1)-tmpFrameIndex%frameCount;
			          break;
			         }
			     default:
			    	 //it should never happen
			    	 frameIndex=0;
			    }
			caller.setSingleVisible(frameIndex);
		}
		
	}
	
	private static final class IntegerFilterOccupancyCheck implements OccupancyCheck<Integer>{

		private final Integer integerFilter;
		
		private IntegerFilterOccupancyCheck(final Integer integerFilter){
			super();
			this.integerFilter=integerFilter;
		}
		
		@Override
		public final boolean isOccupied(final Integer value){
			return(Objects.equals(value,integerFilter));
		}
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
