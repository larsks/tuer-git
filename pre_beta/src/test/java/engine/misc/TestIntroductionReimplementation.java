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
		final int[][] argbArray=new int[introImage.getHeight()][introImage.getWidth()];
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
                 //just copies the pixels of the image into the array, it's going to be used for the very first frame
                 argbArray[y][x]=argb;
                 sourceColor=new ColorRGBA().fromIntARGB(argb);
                 destinationColor=colorSubstitutionTable.get(sourceColor);
                 if(destinationColor==null)
                     {//TODO use the array helper to build several smaller full arrays only for the white pixels as their color doesn't change
                     }
                 else
                     coloredVerticesList.add(new AbstractMap.SimpleEntry<>(new Point(x,y),destinationColor));
                }
        //creates the vertex buffer (indirect NIO buffer), 2 * 6 * w * h if unoptimized
        final FloatBufferData vertexBufferData=new FloatBufferData(BufferUtils.createFloatBufferOnHeap(introImage.getWidth()*introImage.getHeight()*2*6),2);
        for(int y=0;y<introImage.getHeight();y++)
            for(int x=0;x<introImage.getWidth();x++)
                {//TODO fill it, its data won't change
            	 //TODO six vertices
            	 //TODO first triangle
            	 //TODO second triangle
                }
        //sorts
        Collections.sort(coloredVerticesList,new CenteredColoredPointComparator(spreadCenter));
		//for each frame
		for(int frameIndex=0;frameIndex<frameCount;frameIndex++)
		    {final double previousElapsedTime=frameIndex==0?0:(frameIndex-1)/(double)framesPerSecond;
			 final double elapsedTime=frameIndex/(double)framesPerSecond;
			 final int updatedPixelsCount=getScannablePixelsCount(equation,previousElapsedTime);
			 final int updatablePixelsCount=Math.max(0,getScannablePixelsCount(equation,elapsedTime)-updatedPixelsCount);
			 //if it isn't the very first frame
			 if(frameIndex!=0)
			     {//if there are some pixels to update
				  if(updatablePixelsCount>0)
			          {//updates the pixels (incrementally)
			           for(int i=updatedPixelsCount;i<updatedPixelsCount+updatablePixelsCount;i++)
		                   {final int rgbVal=coloredVerticesList.get(i).getValue().asIntARGB();
		                    final Point updatedVertex=coloredVerticesList.get(i).getKey();
		                    argbArray[updatedVertex.getY()][updatedVertex.getX()]=rgbVal;
		                   }
			          }
			     }
			 final MeshData meshData=new MeshData();
			 //uses the shared vertex buffer (all frames use the same one)
			 meshData.setVertexCoords(vertexBufferData);
		     //creates the color buffer (indirect NIO buffer), 4 * 6 * w * h in the worst case
			 final FloatBufferData colorBufferData=new FloatBufferData(BufferUtils.createFloatBufferOnHeap(introImage.getWidth()*introImage.getHeight()*4*6),4);
		     //sets this color buffer to the mesh data
			 meshData.setColorCoords(colorBufferData);
             //TODO add the colors of the arrays into the color buffer
			 
			 meshDataList.add(meshData);
		    }
		//TODO create a Mesh
		//TODO add the first MeshData into it
		//TODO create a KeyframeController
		//TODO add all MeshData instances into it
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
