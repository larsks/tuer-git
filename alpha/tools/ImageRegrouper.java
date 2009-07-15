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
	/*"texture/wallabs00.png",
	"texture/wallabs01.png",
	"texture/wallabs02.png",
	"texture/wallabs03.png",
	"texture/wallabs04.png",
	"texture/wallabs05.png",
	"texture/wallabs06.png",
	"texture/wallabs07.png",
	"texture/wallabs08.png",
	"texture/wallabs09.png",
	"texture/wallabs10.png",
	"texture/wallabs11.png",
	"texture/wallabs12.png",
	"texture/wallabs13.png",
	"texture/wallabs14.png",
	"texture/wallabs15.png",
	"texture/wallabs16.png",
	"texture/wallabs17.png",
	"texture/wallabs18.png",
	"texture/wallabs19.png",
	"texture/wallabs20.png",
	"texture/wallabs21.png",
	"texture/wallabs22.png",
	"texture/wallabs23.png",
	"texture/wallabs24.png",
	"texture/wallabs25.png",
	"texture/wallabs26.png",
        "texture/wallsur00.png",
	"texture/wallsur01.png",
	"texture/wallsur02.png",
	"texture/wallsur03.png",
	"texture/wallsur04.png",
	"texture/wallsur05.png",
	"texture/wallsur06.png",
	"texture/wallsur07.png",
	"texture/wallsur08.png",
	"texture/wallsur09.png",
	"texture/wallsur10.png",
	"texture/wallsur11.png",
	"texture/wallsur12.png",
	"texture/wallsur13.png",
	"texture/wallsur14.png",
	"texture/wallsur15.png",
	"texture/wallsur16.png",
	"texture/wallsur17.png",
	"texture/wallsur18.png",
	"texture/wallsur19.png",
	"texture/wallsur20.png",
	"texture/wallsur21.png",
	"texture/wallsur22.png",
	"texture/wallsur23.png",
	"texture/wallsur24.png",
	"texture/wallsur25.png",
	"texture/wallsur26.png"
        "texture/obj03.png",
	"texture/obj0405.png",
	"texture/obj0403.png",
	"texture/obj0400.png",
	"texture/obj0415.png",
	"texture/obj0402.png",
	"texture/obj0404.png"
	"texture/obj0200.png",
	"texture/obj0201.png",
	"texture/obj0202.png",
	"texture/obj0203.png",
	"texture/obj0204.png",
	"texture/obj0205.png",
	"texture/obj0206.png",
	"texture/obj0207.png",
	"texture/obj0208.png",
	"texture/obj0209.png",
	"texture/obj0210.png"*/
	
	"texture/obj0211.png",
	"texture/obj0212.png",
	"texture/obj0213.png",
	"texture/obj0214.png",
	"texture/obj0215.png",
	"texture/obj0216.png",
	"texture/obj0217.png",
	"texture/obj0218.png",
	"texture/obj0219.png",
	"texture/obj0220.png",
	"texture/obj0221.png"
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
        File f=new File("texture/bot2.png");	
        try{f.createNewFile();
            ImageIO.write(buf,"png",f);
           }
        catch(IOException ioe)
        {ioe.printStackTrace();}
    }
}
