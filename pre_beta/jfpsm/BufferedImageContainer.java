/**
 * Copyright (c) 2006-2014 Julien Gouesse
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

import java.awt.image.BufferedImage;
import java.util.ArrayList;

class BufferedImageContainer {

	private final ArrayList<BufferedImage> bufferedImagesList;
	
	BufferedImageContainer(){
		bufferedImagesList=new ArrayList<>();
	}
	
	final void set(final int index,final BufferedImage image){
		while(index>=bufferedImagesList.size())
			bufferedImagesList.add(null);
		bufferedImagesList.set(index,image);
	}
	
	final BufferedImage get(final int index){
		return(index>=0&&index<bufferedImagesList.size()?bufferedImagesList.get(index):null);
	}
	
	@Deprecated
	final int size(){
		return(bufferedImagesList.size());
	}
	
	@Deprecated
	final int getImageCount(){
		int count=0;
		for(BufferedImage image:bufferedImagesList)
			if(image!=null)
				count++;
		return(count);
	}
}
