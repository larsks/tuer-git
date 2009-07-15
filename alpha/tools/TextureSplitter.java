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
import java.util.List;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;

final class TextureSplitter implements Runnable{

    
    private int horizontalSubdivisionCount;
    
    private int verticalSubdivisionCount;
    
    private String sourceTextureFilename;
    
    private String[] destinationTextureFilenames;
    
    private NetworkSet networkSet;
    
    
    TextureSplitter(final String sourceTextureFilename,
            final int horizontalSubdivisionCount,
            final int verticalSubdivisionCount,
            final String[] destinationTextureFilenames,
            NetworkSet networkSet){
        if(destinationTextureFilenames!=null&&
           destinationTextureFilenames.length!=horizontalSubdivisionCount*verticalSubdivisionCount)
            throw new IllegalArgumentException("incorrect count of texture filenames");
        this.destinationTextureFilenames=destinationTextureFilenames;
        this.sourceTextureFilename=sourceTextureFilename;
        this.horizontalSubdivisionCount=horizontalSubdivisionCount;
        this.verticalSubdivisionCount=verticalSubdivisionCount;
        this.networkSet=networkSet;
    }
    
    
    @Override
    public final void run(){
        if(sourceTextureFilename!=null&&destinationTextureFilenames!=null)
            createNewTextures();
        if(networkSet!=null)
            applyDestinationTextureCoordinateTransform();
    }
    
    private final void createNewTextures(){
        ImageIcon icon=new ImageIcon(sourceTextureFilename);
        int destWidth=icon.getIconWidth()/horizontalSubdivisionCount;
        int destHeight=icon.getIconHeight()/verticalSubdivisionCount;
        Image sourceImage=icon.getImage();
        BufferedImage buf=new BufferedImage(destWidth,destHeight,BufferedImage.TYPE_INT_ARGB);
        Graphics g=buf.getGraphics();
        File destTextureFile;
        String filename,path;
        int sx1,sy1,sx2,sy2;
        for(int i=0;i<horizontalSubdivisionCount;i++)
            for(int j=0;j<verticalSubdivisionCount;j++)
                {path=destinationTextureFilenames[i+(j*horizontalSubdivisionCount)];
                 if(path!=null&&!path.equals(""))
                     {sx1=i*destWidth;
                      sy1=j*destHeight;
                      sx2=sx1+destWidth;
                      sy2=sy1+destHeight;
                      g.drawImage(sourceImage,0,0,destWidth,destHeight,sx1,sy1,sx2,sy2,null);
                      destTextureFile=new File(path);
                      filename=destTextureFile.getName();
                      try{if(destTextureFile.exists()||destTextureFile.createNewFile())
                              if(ImageIO.write(buf,filename.substring(filename.indexOf(".")+1),destTextureFile))
                                  System.out.println("texture "+filename+" successfully created");
                              else
                                  System.out.println("no encoder found for the file "+filename);
                          else
                              throw new IOException("file "+destTextureFile.getAbsolutePath()+" not created");
                         }
                      catch(IOException ioe)
                      {ioe.printStackTrace();}
                     }
                }
    }
    
    private final void applyDestinationTextureCoordinateTransform(){
        List<float[]> walls;
        int vertexIndex;
        for(Network network:networkSet.getNetworksList())
            for(Full3DCell cell:network.getCellsList())
                {walls=cell.getBottomWalls();
                 for(vertexIndex=0;vertexIndex<walls.size();vertexIndex+=4)
                     applyDestinationTextureCoordinateTransform(walls.get(vertexIndex),
                     walls.get(vertexIndex+1),walls.get(vertexIndex+2),walls.get(vertexIndex+3));
                 walls=cell.getTopWalls();
                 for(vertexIndex=0;vertexIndex<walls.size();vertexIndex+=4)
                     applyDestinationTextureCoordinateTransform(walls.get(vertexIndex),
                     walls.get(vertexIndex+1),walls.get(vertexIndex+2),walls.get(vertexIndex+3));
                 walls=cell.getCeilWalls();
                 for(vertexIndex=0;vertexIndex<walls.size();vertexIndex+=4)
                     applyDestinationTextureCoordinateTransform(walls.get(vertexIndex),
                     walls.get(vertexIndex+1),walls.get(vertexIndex+2),walls.get(vertexIndex+3));
                 walls=cell.getFloorWalls();
                 for(vertexIndex=0;vertexIndex<walls.size();vertexIndex+=4)
                     applyDestinationTextureCoordinateTransform(walls.get(vertexIndex),
                     walls.get(vertexIndex+1),walls.get(vertexIndex+2),walls.get(vertexIndex+3));
                 walls=cell.getLeftWalls();
                 for(vertexIndex=0;vertexIndex<walls.size();vertexIndex+=4)
                     applyDestinationTextureCoordinateTransform(walls.get(vertexIndex),
                     walls.get(vertexIndex+1),walls.get(vertexIndex+2),walls.get(vertexIndex+3));
                 walls=cell.getRightWalls();
                 for(vertexIndex=0;vertexIndex<walls.size();vertexIndex+=4)
                     applyDestinationTextureCoordinateTransform(walls.get(vertexIndex),
                     walls.get(vertexIndex+1),walls.get(vertexIndex+2),walls.get(vertexIndex+3));
                }
    }
    
    private final void applyDestinationTextureCoordinateTransform(float[] texCoord0,
            float[] texCoord1,float[] texCoord2,float[] texCoord3){
        final float umin=Math.min(texCoord0[0],Math.min(texCoord1[0],Math.min(texCoord2[0],texCoord3[0])));
        final float umax=Math.max(texCoord0[0],Math.max(texCoord1[0],Math.max(texCoord2[0],texCoord3[0])));
        final float vmin=Math.min(texCoord0[1],Math.min(texCoord1[1],Math.min(texCoord2[1],texCoord3[1])));
        final float vmax=Math.max(texCoord0[1],Math.max(texCoord1[1],Math.max(texCoord2[1],texCoord3[1])));
        final float ucenter=(umin+umax)/2;
        final float vcenter=(vmin+vmax)/2;
        final int uindex=(int)Math.floor(ucenter*horizontalSubdivisionCount);
        final int vindex=(int)Math.floor(vcenter*verticalSubdivisionCount);
        final float uoffset=uindex/(float)horizontalSubdivisionCount;
        final float voffset=vindex/(float)verticalSubdivisionCount;
        texCoord0[0]=Math.min(1.0f,Math.max(0.0f,(texCoord0[0]-uoffset)*horizontalSubdivisionCount));
        texCoord0[1]=Math.min(1.0f,Math.max(0.0f,(texCoord0[1]-voffset)*verticalSubdivisionCount));
        texCoord1[0]=Math.min(1.0f,Math.max(0.0f,(texCoord1[0]-uoffset)*horizontalSubdivisionCount));
        texCoord1[1]=Math.min(1.0f,Math.max(0.0f,(texCoord1[1]-voffset)*verticalSubdivisionCount));
        texCoord2[0]=Math.min(1.0f,Math.max(0.0f,(texCoord2[0]-uoffset)*horizontalSubdivisionCount));
        texCoord2[1]=Math.min(1.0f,Math.max(0.0f,(texCoord2[1]-voffset)*verticalSubdivisionCount));
        texCoord3[0]=Math.min(1.0f,Math.max(0.0f,(texCoord3[0]-uoffset)*horizontalSubdivisionCount));
        texCoord3[1]=Math.min(1.0f,Math.max(0.0f,(texCoord3[1]-voffset)*verticalSubdivisionCount));
    }
}
