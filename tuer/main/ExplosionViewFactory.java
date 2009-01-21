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
package main;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.media.opengl.GL;
import javax.media.opengl.GLException;

import tools.GameIO;

import com.sun.opengl.util.texture.Texture;
import com.sun.opengl.util.texture.TextureIO;

public final class ExplosionViewFactory extends ViewFactory{
    
    
    private static ExplosionViewFactory instance=null;
    
    private final static int texturesCount=16;
    
    private final static int vertexSetsCount=16;
    
    private boolean loaded;
    
    private IStaticVertexSet vertexSet;
    
    
    private ExplosionViewFactory(boolean loadFully){
        loaded=false;
        //vertex set
        List<IVertexSet> vertexSetsList=new ArrayList<IVertexSet>(vertexSetsCount);
        if(loadFully)
            {vertexSet=VertexSetSeeker.getInstance().getIStaticVertexSetInstance(
                     ExplosionControllerFactory.getInstance().getCoordinatesBuffersList().get(0),
                     GL.GL_QUADS);                   
             for(int i=0;i<vertexSetsCount;i++)
                 vertexSetsList.add(vertexSet);            
            }             
        //textures indices
        //in this case, there are as much textures as frames
        List<Integer> texturesIndicesList=new ArrayList<Integer>(texturesCount);
        for(int i=0;i<texturesCount;i++)
            texturesIndicesList.add(Integer.valueOf(i));
        //textures
        List<Texture> texturesList=new ArrayList<Texture>(texturesCount); 
        if(loadFully)
            {String strIndex;
             for(int i=0;i<texturesCount;i++)
                 {strIndex = (i<10?"0":"") + Integer.toString(i);
                  try{
                      texturesList.add(GameIO.newTexture(
                             getClass().getResource("/texture/obj01"+strIndex+".png"),
                             false,TextureIO.PNG));
                     }
                  catch(IOException ioe)
                  {System.out.println("Problem while initializing the explosion view factory");
                   ioe.printStackTrace();
                  }
                  catch(GLException gle)
                  {System.out.println("Problem while initializing the explosion view factory");
                   gle.printStackTrace();
                  }
                 }
             loaded=true;
            }
        setVertexSetsList(vertexSetsList);
        setTexturesIndicesList(texturesIndicesList);
        setTexturesList(texturesList);
    }
    
    @Override
    final void loadProgressively(){
        List<IVertexSet> vertexSetsList=getVertexSetsList();
        List<Texture> texturesList=getTexturesList();
        if(vertexSetsList.size()<vertexSetsCount)
            {if(vertexSet==null)
                {vertexSet=VertexSetSeeker.getInstance().getIStaticVertexSetInstance(
                        ExplosionControllerFactory.getInstance().getCoordinatesBuffersList().get(0),
                        GL.GL_QUADS);                                 
                }
             vertexSetsList.add(vertexSet);
            }
        else
            if(texturesList.size()<texturesCount)
                {String strIndex = (texturesList.size()<10?"0":"")+
                    Integer.toString(texturesList.size());
                 try{
                     texturesList.add(GameIO.newTexture(
                           getClass().getResource("/texture/obj01"+strIndex+".png"),
                           false,TextureIO.PNG));
                    }
                 catch(IOException ioe)
                 {System.out.println("Problem while initializing the explosion view factory");
                  ioe.printStackTrace();
                 }
                 catch(GLException gle)
                 {System.out.println("Problem while initializing the explosion view factory");
                  gle.printStackTrace();
                 }
                }
        if(loaded==false&&vertexSetsList.size()==vertexSetsCount&&
                texturesList.size()==texturesCount)
            loaded=true;
    }
    
    /**
     * 
     *
     * @param loadFully: indicates whether the resources are loaded 
     * completely at the first call or rather progressively
     * @return
     */
    final static ExplosionViewFactory getInstance(boolean loadFully){
        if(instance==null)
            instance=new ExplosionViewFactory(loadFully);
        if(!loadFully&&!instance.loaded)
            instance.loadProgressively();
        return(instance);
    }
    
    final static ExplosionViewFactory getInstance(){
        return(getInstance(true));
    }
    
    final static int getTexturesCount(){
        return(texturesCount);
    }

    final static int getVertexSetsCount(){
        return(vertexSetsCount);
    }
}
