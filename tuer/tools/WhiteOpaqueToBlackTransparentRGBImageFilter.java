/*This program is free software; you can redistribute it and/or
  modify it under the terms of the GNU General Public License
  as published by the Free Software Foundation, version 2
  of the License.
  This program is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  GNU General Public License for more details.
  You should have received a copy of the GNU General Public License
  along with this program; if not, write to the Free Software
  Foundation, Inc., 59 Temple Place - Suite 330, Boston,
  MA 02111-1307, USA.
*/

/*white opaque :      255 255 255 255
  black transparent : 0   0   0   0
*/

package tools;

import java.awt.Image;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.awt.image.FilteredImageSource;
import java.awt.image.RGBImageFilter;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

public class WhiteOpaqueToBlackTransparentRGBImageFilter extends RGBImageFilter{

    
    public WhiteOpaqueToBlackTransparentRGBImageFilter(){
        canFilterIndexColorModel=true;
    }

    
    public int filterRGB(int x,int y,int rgb){
        return(rgb!=0xFFFFFFFF?rgb:0x00000000);
    }
    
    
    public static void main(String[] args){       
	Image img;
	BufferedImage buf;
	Toolkit toolkit=Toolkit.getDefaultToolkit();
	WhiteOpaqueToBlackTransparentRGBImageFilter filter=new WhiteOpaqueToBlackTransparentRGBImageFilter();
	for(int i=0;i<args.length;i++)
	    {img=toolkit.getImage(args[i]);	     
	     img=toolkit.createImage(new FilteredImageSource(img.getSource(),filter));
	     buf=new BufferedImage(256,256,BufferedImage.TYPE_INT_ARGB);
	     while(!buf.getGraphics().drawImage(img,0,0,null));
	     try{ImageIO.write(buf,args[i].substring(args[i].indexOf(".")+1,args[i].length()),new File(args[i]));}
             catch(IOException ioe)
	     {ioe.printStackTrace();}
            }
    }
}
