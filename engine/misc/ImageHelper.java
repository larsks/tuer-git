package engine.misc;

import java.nio.ByteBuffer;

import com.ardor3d.image.Image;

public class ImageHelper {

	public ImageHelper(){}
	
	public int getARGB(final Image img,final int x,final int y){
		final ByteBuffer imgData=img.getData(0);
		final int bytesPerPixel=imgData.capacity()/(img.getWidth()*img.getHeight());
		final int dataIndex=bytesPerPixel*(x+(y*img.getWidth()));
		final int argb;
		switch(img.getDataFormat())
		{case RGB:
			 argb=((((int)imgData.get(dataIndex))&0xFF)<<16)|((((int)imgData.get(dataIndex+1))&0xFF)<<8)|(((int)imgData.get(dataIndex+2))&0xFF)|(0xFF<<24);
			 break;
		 case BGR:
			 argb=(((int)imgData.get(dataIndex))&0xFF)|((((int)imgData.get(dataIndex+1))&0xFF)<<8)|((((int)imgData.get(dataIndex+2))&0xFF)<<16)|(0xFF<<24);
			 break;
		 case RGBA:
			 argb=((((int)imgData.get(dataIndex))&0xFF)<<16)|((((int)imgData.get(dataIndex+1))&0xFF)<<8)|(((int)imgData.get(dataIndex+2))&0xFF)|((((int)imgData.get(dataIndex+3))&0xFF)<<24);
			 break;
		 case BGRA:
			 argb=(((int)imgData.get(dataIndex))&0xFF)|((((int)imgData.get(dataIndex+1))&0xFF)<<8)|((((int)imgData.get(dataIndex+2))&0xFF)<<16)|((((int)imgData.get(dataIndex+3))&0xFF)<<24);
			 break;
		 default:
			 throw new UnsupportedOperationException("Image data format "+img.getDataFormat()+" not supported!");
		}
		return(argb);
	}
	
	public void setARGB(final Image img,final int x,final int y,final int argb){
		final ByteBuffer imgData=img.getData(0);
		final int bytesPerPixel=imgData.capacity()/(img.getWidth()*img.getHeight());
		final int dataIndex=bytesPerPixel*(x+(y*img.getWidth()));
		switch(img.getDataFormat())
		{case RGB:
			 imgData.put(dataIndex,(byte)((argb>>16)&0xFF));
			 imgData.put(dataIndex+1,(byte)((argb>>8)&0xFF));
			 imgData.put(dataIndex+2,(byte)(argb&0xFF));
			 break;
		 case BGR:
			 imgData.put(dataIndex+2,(byte)((argb>>16)&0xFF));
			 imgData.put(dataIndex+1,(byte)((argb>>8)&0xFF));
			 imgData.put(dataIndex,(byte)(argb&0xFF));
			 break;
		 case RGBA:
			 imgData.put(dataIndex,(byte)((argb>>16)&0xFF));
			 imgData.put(dataIndex+1,(byte)((argb>>8)&0xFF));
			 imgData.put(dataIndex+2,(byte)(argb&0xFF));
			 imgData.put(dataIndex+3,(byte)((argb>>24)&0xFF));
			 break;
		 case BGRA:
			 imgData.put(dataIndex+2,(byte)((argb>>16)&0xFF));
			 imgData.put(dataIndex+1,(byte)((argb>>8)&0xFF));
			 imgData.put(dataIndex,(byte)(argb&0xFF));
			 imgData.put(dataIndex+3,(byte)((argb>>24)&0xFF));
			 break;
		 default:
			 throw new UnsupportedOperationException("Image data format "+img.getDataFormat()+" not supported!");
		}
	}
}
