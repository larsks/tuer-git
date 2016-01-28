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

import java.util.ArrayList;
import java.util.List;

import com.ardor3d.image.Image;
import com.ardor3d.image.Texture;
import com.ardor3d.image.util.ImageUtils;
import com.ardor3d.image.util.jogl.JoglImageLoader;
import com.ardor3d.scenegraph.MeshData;
import com.ardor3d.util.TextureManager;
import com.ardor3d.util.resource.URLResourceSource;

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
		//for each frame
		for(int frameIndex=0;frameIndex<frameCount;frameIndex++)
		    {//for each ordinate
			 for(int y=0;y<introImage.getHeight();y++)
                 //for each abscissa
				 for(int x=0;x<introImage.getWidth();x++)
                     {//retrieves the color of the pixel
                	  final int argb=ImageUtils.getARGB(introImage,x,y);
                	  //TODO use the frame index and the equation to modify the color
                	  argbArray[y][x]=argb;
                     }
			 //TODO use the array helper to build several smaller full arrays
			 final MeshData meshData=new MeshData();
			 //TODO create the vertex buffer (indirect NIO buffer), 3 * 6 * w * h in the worst case
		     //TODO create the color buffer (indirect NIO buffer), 4 * 6 * w * h in the worst case
		     //TODO add these buffers into the mesh data
			 //TODO add the vertices of the arrays into the vertex buffer
             //TODO add the colors of the arrays into the color buffer
			 meshDataList.add(meshData);
		    }
		//TODO create a Mesh
		//TODO add the first MeshData into it
		//TODO create a KeyframeController
		//TODO add all MeshData instances into it
		//TODO show the result
	}
}
