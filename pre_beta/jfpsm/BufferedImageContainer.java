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
