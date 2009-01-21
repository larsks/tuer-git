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

/**
 * This class is the facade of the component called "tools". 
 * It provides some static methods to manipulate textures.
 *@author Julien Gouesse
 */

package tools;

import com.sun.opengl.util.BufferUtil;
import com.sun.opengl.util.texture.Texture;
import com.sun.opengl.util.texture.TextureIO;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import main.GameGLView;

public final class GameIO{

    private static final int HEADER_SIZE = 2;
    
    private static final int PRIMITIVE_COUNT_HEADER_INDEX = 0;
    
    private static final int VALUE_COUNT_PER_PRIMITIVE_HEADER_INDEX = 1;
    
    
    public static final FloatBuffer readGameFloatDataFile(String path) throws IOException{
        DataInputStream in;
        FloatBuffer coordinatesBuffer;
        in=new DataInputStream(new BufferedInputStream(GameIO.class.getResourceAsStream(path)));
        int[] headerData=readGameFloatDataFileHeader(in);
        //? bounds * ? animations * ? frames * ? elements in a primitive
        coordinatesBuffer=BufferUtil.newFloatBuffer(headerData[PRIMITIVE_COUNT_HEADER_INDEX]*headerData[VALUE_COUNT_PER_PRIMITIVE_HEADER_INDEX]);
        for(int i=0;i<coordinatesBuffer.capacity();i++)
            coordinatesBuffer.put(in.readFloat());
        coordinatesBuffer.position(0);
        in.close();
        return(coordinatesBuffer);
    }
    
    public static final List<FloatBuffer> readGameMultiBufferFloatDataFile(String path) throws IOException{
        List<FloatBuffer> coordinatesBufferList=new ArrayList<FloatBuffer>();
        //read several buffers from a single file
        DataInputStream in=new DataInputStream(new BufferedInputStream(GameIO.class.getResourceAsStream(path)));
        int[] headerData;
        FloatBuffer coordinatesBuffer;
        while(in.available()>0)
            {//read the header to know the amount of data to read
             headerData=readGameFloatDataFileHeader(in);
             //create a buffer
             coordinatesBuffer=BufferUtil.newFloatBuffer(headerData[PRIMITIVE_COUNT_HEADER_INDEX]*headerData[VALUE_COUNT_PER_PRIMITIVE_HEADER_INDEX]);
             //fill the buffer
             for(int i=0;i<coordinatesBuffer.capacity();i++)
                 coordinatesBuffer.put(in.readFloat());
             //rewind it to avoid problems later
             coordinatesBuffer.rewind();
             //add it to the list
             coordinatesBufferList.add(coordinatesBuffer);
            }
        in.close();
        return(coordinatesBufferList);
    }
    
    private static final int[] readGameFloatDataFileHeader(DataInputStream in) throws IOException{
        int[] result=new int[HEADER_SIZE];
        result[PRIMITIVE_COUNT_HEADER_INDEX]=in.readInt();
        //read the count of values per primitive: 
        //3 for the vertices, 2 for the texture coordinates for example
        result[VALUE_COUNT_PER_PRIMITIVE_HEADER_INDEX]=in.readInt();
        return(result);
    }
    
    public static final Texture newTexture(String path,boolean useMipmap,String format)throws IOException{
        return(newTexture(GameIO.class.getResource(path),useMipmap,format));
    }
    
    public static final Texture newTexture(URL path,boolean useMipmap,String format)throws IOException{
        ImageIcon imageIcon=new ImageIcon(path);
        int sourceWidth=imageIcon.getIconWidth();
        int sourceHeight=imageIcon.getIconHeight();
        float xScaleFactor=1.0f,yScaleFactor=1.0f;
        //TODO: compute the scale factors
        //square image
        //TODO: check if we support non power of 2 textures
        if(sourceWidth==sourceHeight)
            {if(sourceWidth>GameGLView.getGL_MAX_TEXTURE_SIZE())
                 {xScaleFactor=GameGLView.getGL_MAX_TEXTURE_SIZE()/(float)sourceWidth;
                  yScaleFactor=xScaleFactor;
                 }
            }
        else
            {//TODO: check if we support non square textures
             //non square image
             if(sourceWidth>GameGLView.getGL_MAX_TEXTURE_SIZE())
                {if(sourceHeight>GameGLView.getGL_MAX_TEXTURE_SIZE())
                    {
                      
                    }
                else
                    {
                     
                    }
               }
           else
               {if(sourceHeight>GameGLView.getGL_MAX_TEXTURE_SIZE())
                    {
                    }
               }            
            }
        Texture texture;
        if(xScaleFactor!=1.0f||yScaleFactor!=1.0f)
            {BufferedImage bsrc=ImageIO.read(path);
             BufferedImage bdest=new BufferedImage((int)(bsrc.getWidth()*xScaleFactor),(int)(bsrc.getHeight()*yScaleFactor),BufferedImage.TYPE_INT_ARGB);
             Graphics2D g=bdest.createGraphics();
             AffineTransform at=AffineTransform.getScaleInstance((double)xScaleFactor,(double)yScaleFactor);
             g.drawRenderedImage(bsrc,at);              
             texture=TextureIO.newTexture(bdest,useMipmap);
             g.dispose();
            }
        else
            texture=TextureIO.newTexture(path,useMipmap,format);
        return(texture);
    }
    
    /*private static final int nearestPower(int value){
        int i=1;
        if(value==0)
            return(-1);
        while(true) 
            {if(value==1)
                 return(i);
             else 
                 if(value==3)
                     return(i*4);
             value>>=1;
             i*=2;
            }
    }*/
}
