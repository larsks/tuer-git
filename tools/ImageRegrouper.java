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

package tools;

import java.awt.Graphics;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;


public class ImageRegrouper{

    private static String imageFiles[]={          
	/*"pic256/wallabs00.png",
	"pic256/wallabs01.png",
	"pic256/wallabs02.png",
	"pic256/wallabs03.png",
	"pic256/wallabs04.png",
	"pic256/wallabs05.png",
	"pic256/wallabs06.png",
	"pic256/wallabs07.png",
	"pic256/wallabs08.png",
	"pic256/wallabs09.png",
	"pic256/wallabs10.png",
	"pic256/wallabs11.png",
	"pic256/wallabs12.png",
	"pic256/wallabs13.png",
	"pic256/wallabs14.png",
	"pic256/wallabs15.png",
	"pic256/wallabs16.png",
	"pic256/wallabs17.png",
	"pic256/wallabs18.png",
	"pic256/wallabs19.png",
	"pic256/wallabs20.png",
	"pic256/wallabs21.png",
	"pic256/wallabs22.png",
	"pic256/wallabs23.png",
	"pic256/wallabs24.png",
	"pic256/wallabs25.png",
	"pic256/wallabs26.png",
        "pic256/wallsur00.png",
	"pic256/wallsur01.png",
	"pic256/wallsur02.png",
	"pic256/wallsur03.png",
	"pic256/wallsur04.png",
	"pic256/wallsur05.png",
	"pic256/wallsur06.png",
	"pic256/wallsur07.png",
	"pic256/wallsur08.png",
	"pic256/wallsur09.png",
	"pic256/wallsur10.png",
	"pic256/wallsur11.png",
	"pic256/wallsur12.png",
	"pic256/wallsur13.png",
	"pic256/wallsur14.png",
	"pic256/wallsur15.png",
	"pic256/wallsur16.png",
	"pic256/wallsur17.png",
	"pic256/wallsur18.png",
	"pic256/wallsur19.png",
	"pic256/wallsur20.png",
	"pic256/wallsur21.png",
	"pic256/wallsur22.png",
	"pic256/wallsur23.png",
	"pic256/wallsur24.png",
	"pic256/wallsur25.png",
	"pic256/wallsur26.png"
        "pic256/obj03.png",
	"pic256/obj0405.png",
	"pic256/obj0403.png",
	"pic256/obj0400.png",
	"pic256/obj0415.png",
	"pic256/obj0402.png",
	"pic256/obj0404.png"
	"pic256/obj0200.png",
	"pic256/obj0201.png",
	"pic256/obj0202.png",
	"pic256/obj0203.png",
	"pic256/obj0204.png",
	"pic256/obj0205.png",
	"pic256/obj0206.png",
	"pic256/obj0207.png",
	"pic256/obj0208.png",
	"pic256/obj0209.png",
	"pic256/obj0210.png"*/
	
	"pic256/obj0211.png",
	"pic256/obj0212.png",
	"pic256/obj0213.png",
	"pic256/obj0214.png",
	"pic256/obj0215.png",
	"pic256/obj0216.png",
	"pic256/obj0217.png",
	"pic256/obj0218.png",
	"pic256/obj0219.png",
	"pic256/obj0220.png",
	"pic256/obj0221.png"
    };

    public static void main(String[] args){
        int size=(int)Math.ceil(Math.sqrt(imageFiles.length));
	int fullSize=1;
	while(fullSize<size)
	    fullSize*=2;
	Image image=null;
	BufferedImage buf=new BufferedImage(256*fullSize,256*fullSize,BufferedImage.TYPE_INT_ARGB);
	Graphics g=buf.getGraphics();
	System.out.println("size : "+size+" length : "+imageFiles.length);
	for(int i=0;i<imageFiles.length;i++)
	    {System.out.println("image n"+i+" start : "+imageFiles[i]+" "+i%size+" "+i/size);
	     image=new ImageIcon(imageFiles[i]).getImage();
	     g.drawImage(image,(i%size)*256,(i/size)*256,null);	     
	     System.out.println("image n"+i+" end");
	    }	
	File f=new File("pic256/bot2.png");	
	try{f.createNewFile();
	    ImageIO.write(buf,"png",f);
	   }
        catch(IOException ioe)
	{ioe.printStackTrace();}
    }
}
