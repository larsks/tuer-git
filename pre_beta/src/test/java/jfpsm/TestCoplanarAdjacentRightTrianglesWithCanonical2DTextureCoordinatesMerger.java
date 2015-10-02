/**
 * Copyright (c) 2006-2015 Julien Gouesse
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
package jfpsm;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;

import com.ardor3d.image.util.jogl.JoglImageLoader;
import com.ardor3d.scenegraph.Mesh;
import com.ardor3d.scenegraph.Node;
import com.ardor3d.util.export.binary.BinaryImporter;
import com.ardor3d.util.resource.ResourceLocatorTool;
import com.ardor3d.util.resource.SimpleResourceLocator;

import jfpsm.CoplanarAdjacentRightTrianglesWithCanonical2DTextureCoordinatesMerger.RightTriangleInfo;

/**
 * Test of a mesh optimizer focused on coplanar adjacent right triangles whose all 2D texture coordinates 
 * are canonical ([0;0], [0;1], [1;0] or [1;1]).
 * 
 * @author Julien Gouesse
 *
 */
public class TestCoplanarAdjacentRightTrianglesWithCanonical2DTextureCoordinatesMerger{

	private static void testComputeAdjacentMergeableTrisArraysList(){
		final RightTriangleInfo info=new RightTriangleInfo(0,0,0);
		final RightTriangleInfo[][][] adjacentTrisArray=new RightTriangleInfo[][][]{new RightTriangleInfo[][]{null,null,null,null,null,null,null,null},
				                                                                    new RightTriangleInfo[][]{null,null,null,new RightTriangleInfo[]{info,info},null,null,null,null},
				                                                                    new RightTriangleInfo[][]{null,new RightTriangleInfo[]{info,info},new RightTriangleInfo[]{info,info},new RightTriangleInfo[]{info,info},new RightTriangleInfo[]{info,info},new RightTriangleInfo[]{info,info},new RightTriangleInfo[]{info,info},new RightTriangleInfo[]{info,info}},
				                                                                    new RightTriangleInfo[][]{null,null,new RightTriangleInfo[]{info,info},new RightTriangleInfo[]{info,info},new RightTriangleInfo[]{info,info},new RightTriangleInfo[]{info,info},null,null},
				                                                                    new RightTriangleInfo[][]{null,new RightTriangleInfo[]{info,info},new RightTriangleInfo[]{info,info},new RightTriangleInfo[]{info,info},new RightTriangleInfo[]{info,info},null,null,null},
				                                                                    new RightTriangleInfo[][]{null,new RightTriangleInfo[]{info,info},new RightTriangleInfo[]{info,info},new RightTriangleInfo[]{info,info},new RightTriangleInfo[]{info,info},null,null,null},
				                                                                    new RightTriangleInfo[][]{null,null,new RightTriangleInfo[]{info,info},null,null,null,null,null},
				                                                                    new RightTriangleInfo[][]{null,null,null,null,null,null,null,null}};
		System.out.println("Input:");
		//for each row
		for(int i=0;i<adjacentTrisArray.length;i++)
			{//for each column
			 for(int j=0;j<adjacentTrisArray[i].length;j++)
				 if(adjacentTrisArray[i][j]!=null&&adjacentTrisArray[i][j][0]!=null&&adjacentTrisArray[i][j][1]!=null)
		             System.out.print("[X]");
				 else
					 System.out.print("[ ]");
			 System.out.println("");
			}
		System.out.println("");
		ArrayList<RightTriangleInfo[][][]> adjacentTrisArraysList=CoplanarAdjacentRightTrianglesWithCanonical2DTextureCoordinatesMerger.computeAdjacentMergeableTrisArraysList(adjacentTrisArray);
		System.out.println("Output:");
		for(RightTriangleInfo[][][] resultingAdjacentTrisArray:adjacentTrisArraysList)
			{for(int i=0;i<resultingAdjacentTrisArray.length;i++)
			     {for(int j=0;j<resultingAdjacentTrisArray[i].length;j++)
				      if(resultingAdjacentTrisArray[i][j]!=null&&resultingAdjacentTrisArray[i][j][0]!=null&&resultingAdjacentTrisArray[i][j][1]!=null)
		                  System.out.print("[X]");
				      else
					      System.out.print("[ ]");
			      System.out.println("");
			     }
			 System.out.println("");
			}
	}
	
	private static void testOptimize(){
		JoglImageLoader.registerLoader();
		try{SimpleResourceLocator srl=new SimpleResourceLocator(CoplanarAdjacentRightTrianglesWithCanonical2DTextureCoordinatesMerger.class.getResource("/images"));
            ResourceLocatorTool.addResourceLocator(ResourceLocatorTool.TYPE_TEXTURE,srl);
           } 
        catch(final URISyntaxException urise)
        {urise.printStackTrace();}
		try{final Node levelNode=(Node)new BinaryImporter().load(CoplanarAdjacentRightTrianglesWithCanonical2DTextureCoordinatesMerger.class.getResource("/abin/LID0.abin"));
		    final Mesh mesh=(Mesh)((Node)levelNode.getChild(0)).getChild(1);
		    System.out.println("Input: "+mesh.getMeshData().getVertexCount());
		    CoplanarAdjacentRightTrianglesWithCanonical2DTextureCoordinatesMerger.optimize(mesh);
		    System.out.println("Output: "+mesh.getMeshData().getVertexCount());
	       }
	    catch(IOException ioe)
	    {throw new RuntimeException("level loading failed",ioe);}
	}
	
	public static void main(String[] args){
		testComputeAdjacentMergeableTrisArraysList();
		testOptimize();
	}

}
