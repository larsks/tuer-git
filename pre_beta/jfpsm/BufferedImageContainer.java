package jfpsm;

import java.awt.image.BufferedImage;
import java.util.ArrayList;

class BufferedImageContainer {

	private ArrayList<BufferedImage> bufferedImagesList;
	
	BufferedImageContainer(){
		bufferedImagesList=new ArrayList<BufferedImage>();
	}
	
	void set(final int index,final BufferedImage image){
		while(index>=bufferedImagesList.size())
			bufferedImagesList.add(null);
		bufferedImagesList.set(index,image);
	}
	
	BufferedImage get(final int index){
		return(index>=0&&index<bufferedImagesList.size()?bufferedImagesList.get(index):null);
	}
}
