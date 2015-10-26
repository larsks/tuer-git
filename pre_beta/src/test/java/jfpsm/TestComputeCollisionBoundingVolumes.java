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
import java.util.Arrays;

import jfpsm.ArrayHelper.OccupancyMap;

import com.ardor3d.image.Image;
import com.ardor3d.image.util.ImageLoaderUtil;
import com.ardor3d.image.util.jogl.JoglImageLoader;
import com.ardor3d.math.ColorRGBA;
import com.ardor3d.util.resource.URLResourceSource;

import engine.data.Level;
import engine.misc.ImageHelper;

public class TestComputeCollisionBoundingVolumes {

	public static void main(String[] args) {
		JoglImageLoader.registerLoader();
		final URL mapUrl=Level.class.getResource("/images/containermap.png");
    	final URLResourceSource mapSource=new URLResourceSource(mapUrl);
    	final Image map=ImageLoaderUtil.loadImage(mapSource,false);
    	final Boolean[][] collisionMap=new Boolean[map.getWidth()][map.getHeight()];
    	final boolean[][] primitiveCollisionMap=new boolean[map.getWidth()][map.getHeight()];
    	final ImageHelper imgHelper=new ImageHelper();
    	for(int y=0;y<map.getHeight();y++)
	    	for(int x=0;x<map.getWidth();x++)
	    		{final int argb=imgHelper.getARGB(map,x,y);
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
    	System.out.println("Occupancy map check: "+(Arrays.deepEquals(primitiveCollisionMap,occupancyMap.getArrayMap())?"OK":"NOK"));
    	final ArrayList<Boolean[][]> arrayList=arrayHelper.computeFullArraysFromNonFullArray(collisionMap);
    	System.out.println("Output:");
    	for(final Boolean[][] collisionArray:arrayList)
    	    {System.out.println(arrayHelper.toString(collisionArray,false,null));
    	    }
	}
}
