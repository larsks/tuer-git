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

import java.net.URL;
import java.util.ArrayList;
import java.util.Map.Entry;
import jfpsm.ArrayHelper.OccupancyMap;
import jfpsm.ArrayHelper.Vector2i;
import com.ardor3d.bounding.BoundingBox;
import com.ardor3d.image.Image;
import com.ardor3d.image.util.ImageLoaderUtil;
import com.ardor3d.image.util.ImageUtils;
import com.ardor3d.image.util.jogl.JoglImageLoader;
import com.ardor3d.math.ColorRGBA;
import com.ardor3d.math.Vector3;
import com.ardor3d.util.resource.URLResourceSource;
import engine.data.Level;

public class TestComputeCollisionBoundingVolumes{

	public static void main(String[] args){
		JoglImageLoader.registerLoader();
		final URL mapUrl=Level.class.getResource("/images/containermap.png");
    	final URLResourceSource mapSource=new URLResourceSource(mapUrl);
    	final Image map=ImageLoaderUtil.loadImage(mapSource,false);
    	final Boolean[][] collisionMap=new Boolean[map.getWidth()][map.getHeight()];
    	final boolean[][] primitiveCollisionMap=new boolean[map.getWidth()][map.getHeight()];
    	for(int y=0;y<map.getHeight();y++)
	    	for(int x=0;x<map.getWidth();x++)
	    		{final int argb=ImageUtils.getARGB(map,x,y);
	    		 collisionMap[x][y]=argb==ColorRGBA.BLUE.asIntARGB()?Boolean.TRUE:null;
	    		 primitiveCollisionMap[x][y]=argb==ColorRGBA.BLUE.asIntARGB();
	    		}
    	//System.out.println("[260,3]: "+(imgHelper.getARGB(map,260,3)==ColorRGBA.BLUE.asIntARGB()));//true
    	//System.out.println(map.getWidth()+" "+map.getHeight());//300 256
    	System.out.println("Input: ");
    	final ArrayHelper arrayHelper=new ArrayHelper();
    	System.out.println(arrayHelper.toString(collisionMap,false,null));
    	final OccupancyMap occupancyMap=arrayHelper.createPackedOccupancyMap(collisionMap);
    	System.out.println("rowCount: "+occupancyMap.getRowCount());
    	System.out.println("columnCount: "+occupancyMap.getColumnCount());
    	System.out.println("smallestRowIndex: "+occupancyMap.getSmallestRowIndex());
    	System.out.println("biggestRowIndex: "+occupancyMap.getBiggestRowIndex());
    	System.out.println("smallestColumnIndex: "+occupancyMap.getSmallestColumnIndex());
    	System.out.println("biggestColumnIndex: "+occupancyMap.getBiggestColumnIndex());
    	
    	boolean occupancyMapConsistentWithCollisionMap=true;
    	final boolean[][] array=occupancyMap.getArrayMap();
    	final int smallestColumnIndex=occupancyMap.getSmallestColumnIndex();
    	final int smallestRowIndex=occupancyMap.getSmallestRowIndex();
    	for(int y=0;y<map.getHeight()&&occupancyMapConsistentWithCollisionMap;y++)
	    	for(int x=0;x<map.getWidth()&&occupancyMapConsistentWithCollisionMap;x++)
    	        {final boolean cellInArray=0<=x-smallestColumnIndex&&x-smallestColumnIndex<array.length&&
    	         array[x-smallestColumnIndex]!=null&&0<=y-smallestRowIndex&&y-smallestRowIndex<array[x-smallestColumnIndex].length;
	    		 if(primitiveCollisionMap[x][y])
    	        	 {//checks whether there is a cell at these coordinates and the cell is occupied
	    			  occupancyMapConsistentWithCollisionMap&=cellInArray&&array[x-smallestColumnIndex][y-smallestRowIndex];
    	        	 }
    	         else
    	             {//checks whether there is no cell at these coordinates or the cell isn't occupied
    	        	  occupancyMapConsistentWithCollisionMap&=!cellInArray||!array[x-smallestColumnIndex][y-smallestRowIndex];
    	             }
    	        }
    	System.out.println("Occupancy check: "+(occupancyMapConsistentWithCollisionMap?"OK":"NOK"));
    	final java.util.Map<Vector2i,Boolean[][]> fullArrayMap=arrayHelper.computeFullArraysFromNonFullArray(collisionMap);
    	System.out.println("Output:");
    	System.out.println(arrayHelper.toString(fullArrayMap,map.getHeight(),map.getWidth()));
    	
    	//computes the bounding boxes
    	final ArrayList<BoundingBox> boundingBoxList=new ArrayList<>();
    	final Vector2i zero=new Vector2i(0,0);
    	for(final Entry<Vector2i,Boolean[][]> fullArrayMapEntry:fullArrayMap.entrySet())
    	    {final Vector2i location=fullArrayMapEntry.getKey()==null?zero:fullArrayMapEntry.getKey();
    		 final Boolean[][] fullArray=fullArrayMapEntry.getValue();
    		 if(fullArray!=null)
    		     {final int fullArrayColumnCount=fullArray.length;
    		      //all columns have the same size
    		      final int fullArrayRowCount=fullArrayColumnCount==0?0:fullArray[0].length;
    			  //computes the minimum and maximum coordinates
    		      //abscissa
    		      final int x0=location.getX();
    			  final int x1=x0+fullArrayColumnCount;
    			  //ordinate
    			  final int y0=location.getY();
    			  final int y1=y0+fullArrayRowCount;
    			  //applicate
    			  final int z0=0;
    			  final int z1=1;
    			  //extents
    			  final double xExtent=x1-x0;
    			  final double yExtent=y1-y0;
    			  final double zExtent=z1-z0;
    			  //center
    			  final Vector3 center=new Vector3(xExtent/2.0,yExtent/2.0,zExtent/2.0);
    			  //bounding box
    			  final BoundingBox boundingBox=new BoundingBox(center,xExtent,yExtent,zExtent);
    			  //adds the bounding box into the list
    			  boundingBoxList.add(boundingBox);
    		     }
    	    }
    	System.out.println("Bounding boxes: "+boundingBoxList.size());
	}
}
