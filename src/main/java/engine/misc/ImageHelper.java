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
package engine.misc;

import java.nio.ByteBuffer;

import com.ardor3d.image.Image;
import com.ardor3d.image.util.ImageUtils;
import com.ardor3d.math.ColorRGBA;

public class ImageHelper{

	public ImageHelper(){
		super();
	}
	
	public ColorRGBA getRGBA(final Image img,final int x,final int y,final ColorRGBA store){
		final ColorRGBA result=store==null?new ColorRGBA():store;
		final int rgba=getRGBA(img,x,y);
		return(result.fromIntRGBA(rgba));
	}
	
	@SuppressWarnings("cast")
	public int getARGB(final Image img,final int x,final int y){
		final ByteBuffer imgData=img.getData(0);
		final int bytesPerPixel=ImageUtils.getPixelByteSize(img.getDataFormat(),img.getDataType());
		final int dataIndex=bytesPerPixel*(x+(y*img.getWidth()));
		final int argb;
		switch(img.getDataFormat())
		{case Alpha:
			 argb=((((int)imgData.get(dataIndex))&0xFF)<<24);
			 break;
		 case Red:
			 argb=(0xFF<<24)|((((int)imgData.get(dataIndex))&0xFF)<<16);
			 break;
		 case Green:
			 argb=(0xFF<<24)|((((int)imgData.get(dataIndex))&0xFF)<<8);
			 break;
		 case Blue:
			 argb=(0xFF<<24)|(((int)imgData.get(dataIndex))&0xFF);
			 break;
		 case RG:
			 argb=(0xFF<<24)|((((int)imgData.get(dataIndex))&0xFF)<<16)|((((int)imgData.get(dataIndex+1))&0xFF)<<8)|(0x00);
			 break;
		 case RGB:
			 argb=(0xFF<<24)|((((int)imgData.get(dataIndex))&0xFF)<<16)|((((int)imgData.get(dataIndex+1))&0xFF)<<8)|(((int)imgData.get(dataIndex+2))&0xFF);
			 break;
		 case BGR:
			 argb=(0xFF<<24)|((((int)imgData.get(dataIndex+2))&0xFF)<<16)|((((int)imgData.get(dataIndex+1))&0xFF)<<8)|(((int)imgData.get(dataIndex))&0xFF);
			 break;
		 case RGBA:
			 argb=((((int)imgData.get(dataIndex+3))&0xFF)<<24)|((((int)imgData.get(dataIndex))&0xFF)<<16)|((((int)imgData.get(dataIndex+1))&0xFF)<<8)|(((int)imgData.get(dataIndex+2))&0xFF);
			 break;
		 case BGRA:
			 argb=((((int)imgData.get(dataIndex+3))&0xFF)<<24)|((((int)imgData.get(dataIndex+2))&0xFF)<<16)|((((int)imgData.get(dataIndex+1))&0xFF)<<8)|(((int)imgData.get(dataIndex))&0xFF);
			 break;
		 default:
			 throw new UnsupportedOperationException("Image data format "+img.getDataFormat()+" not supported!");
		}
		return(argb);
	}
	
	@SuppressWarnings("cast")
	public int getRGBA(final Image img,final int x,final int y){
		final ByteBuffer imgData=img.getData(0);
		final int bytesPerPixel=ImageUtils.getPixelByteSize(img.getDataFormat(),img.getDataType());
		final int dataIndex=bytesPerPixel*(x+(y*img.getWidth()));
		final int rgba;
		switch(img.getDataFormat())
		{case Alpha:
			 rgba=(((int)imgData.get(dataIndex))&0xFF);
			 break;
		 case Red:
			 rgba=(0xFF<<24)|((((int)imgData.get(dataIndex))&0xFF)<<24);
			 break;
		 case Green:
			 rgba=(0xFF<<24)|((((int)imgData.get(dataIndex))&0xFF)<<16);
			 break;
		 case Blue:
			 rgba=(0xFF<<24)|(((int)imgData.get(dataIndex))&0xFF<<8);
			 break;
		 case RG:
			 rgba=((((int)imgData.get(dataIndex))&0xFF)<<24)|((((int)imgData.get(dataIndex+1))&0xFF)<<16)|(0x00<<8)|(0xFF);
			 break;
		 case RGB:
			 rgba=((((int)imgData.get(dataIndex))&0xFF)<<24)|((((int)imgData.get(dataIndex+1))&0xFF)<<16)|((((int)imgData.get(dataIndex+2))&0xFF)<<8)|(0xFF);
			 break;
		 case BGR:
			 rgba=((((int)imgData.get(dataIndex+2))&0xFF)<<24)|((((int)imgData.get(dataIndex+1))&0xFF)<<16)|((((int)imgData.get(dataIndex))&0xFF)<<8)|(0xFF);
			 break;
		 case RGBA:
			 rgba=((((int)imgData.get(dataIndex))&0xFF)<<24)|((((int)imgData.get(dataIndex+1))&0xFF)<<16)|((((int)imgData.get(dataIndex+2))&0xFF)<<8)|(((int)imgData.get(dataIndex+3))&0xFF);
			 break;
		 case BGRA:
			 rgba=((((int)imgData.get(dataIndex+2))&0xFF)<<24)|((((int)imgData.get(dataIndex+1))&0xFF)<<16)|((((int)imgData.get(dataIndex))&0xFF)<<8)|(((int)imgData.get(dataIndex+3))&0xFF);
			 break;
		 default:
			 throw new UnsupportedOperationException("Image data format "+img.getDataFormat()+" not supported!");
		}
		return(rgba);
	}
	
	public void setRGBA(final Image img,final int x,final int y,final ColorRGBA color){
		final int rgba=color.asIntRGBA();
		setRGBA(img,x,y,rgba);
	}
	
	public void setARGB(final Image img,final int x,final int y,final int argb){
		final ByteBuffer imgData=img.getData(0);
		final int bytesPerPixel=ImageUtils.getPixelByteSize(img.getDataFormat(),img.getDataType());
		final int dataIndex=bytesPerPixel*(x+(y*img.getWidth()));
		switch(img.getDataFormat())
		{case Alpha:
			 imgData.put(dataIndex,(byte)((argb>>24)&0xFF));
			 break;
		 case Red:
			 imgData.put(dataIndex,(byte)((argb>>16)&0xFF));
			 break;
		 case Green:
			 imgData.put(dataIndex,(byte)((argb>>8)&0xFF));
			 break;
		 case Blue:
			 imgData.put(dataIndex,(byte)(argb&0xFF));
			 break;
		 case RG:
			 imgData.put(dataIndex,(byte)((argb>>16)&0xFF));
			 imgData.put(dataIndex+1,(byte)((argb>>8)&0xFF));
			 break;
		 case RGB:
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
	
	public void setRGBA(final Image img,final int x,final int y,final int rgba){
		final ByteBuffer imgData=img.getData(0);
		final int bytesPerPixel=ImageUtils.getPixelByteSize(img.getDataFormat(),img.getDataType());
		final int dataIndex=bytesPerPixel*(x+(y*img.getWidth()));
		switch(img.getDataFormat())
		{case Alpha:
			 imgData.put(dataIndex,(byte)((rgba)&0xFF));
			 break;
		 case Red:
			 imgData.put(dataIndex,(byte)((rgba>>24)&0xFF));
			 break;
		 case Green:
			 imgData.put(dataIndex,(byte)((rgba>>16)&0xFF));
			 break;
		 case Blue:
			 imgData.put(dataIndex,(byte)((rgba>>8)&0xFF));
			 break;
		 case RG:
			 imgData.put(dataIndex,(byte)((rgba>>24)&0xFF));
			 imgData.put(dataIndex+1,(byte)((rgba>>16)&0xFF));
			 break;
		 case RGB:
			 imgData.put(dataIndex,(byte)((rgba>>24)&0xFF));
			 imgData.put(dataIndex+1,(byte)((rgba>>16)&0xFF));
			 imgData.put(dataIndex+2,(byte)((rgba>>8)&0xFF));
			 break;
		 case BGR:
			 imgData.put(dataIndex+2,(byte)((rgba>>24)&0xFF));
			 imgData.put(dataIndex+1,(byte)((rgba>>16)&0xFF));
			 imgData.put(dataIndex,(byte)((rgba>>8)&0xFF));
			 break;
		 case RGBA:
			 imgData.put(dataIndex,(byte)((rgba>>24)&0xFF));
			 imgData.put(dataIndex+1,(byte)((rgba>>16)&0xFF));
			 imgData.put(dataIndex+2,(byte)((rgba>>8)&0xFF));
			 imgData.put(dataIndex+3,(byte)(rgba&0xFF));
			 break;
		 case BGRA:
			 imgData.put(dataIndex+2,(byte)((rgba>>24)&0xFF));
			 imgData.put(dataIndex+1,(byte)((rgba>>16)&0xFF));
			 imgData.put(dataIndex,(byte)((rgba>>8)&0xFF));
			 imgData.put(dataIndex+3,(byte)(rgba&0xFF));
			 break;
		 default:
			 throw new UnsupportedOperationException("Image data format "+img.getDataFormat()+" not supported!");
		}
	}
}
