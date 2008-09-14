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

import com.sun.opengl.util.texture.Texture;
import com.sun.opengl.util.texture.TextureIO;

final class HealthPowerUpViewFactory extends ViewFactory{

    
    private static HealthPowerUpViewFactory instance=null;
    
    private final static int texturesCount=1;
    
    private final static int vertexSetsCount=1;
    
    private boolean loaded;
    
    private IStaticVertexSet vertexSet;
    
    
    private HealthPowerUpViewFactory(GL gl,boolean loadFully){
        loaded=false;
        //vertex set
        List<IVertexSet> vertexSetsList=new ArrayList<IVertexSet>(vertexSetsCount);
        if(loadFully)
            {vertexSet=VertexSetSeeker.getInstance().getIStaticVertexSetInstance(gl,
                     HealthPowerUpControllerFactory.getInstance().getCoordinatesBuffersList().get(0),
                     GL.GL_QUADS);
             vertexSetsList.add(vertexSet);
            }
        //textures indices
        List<Integer> texturesIndicesList=new ArrayList<Integer>(texturesCount);
        texturesIndicesList.add(Integer.valueOf(0));        
        //textures
        List<Texture> texturesList=new ArrayList<Texture>(texturesCount); 
        if(loadFully)
            {try{
                 texturesList.add(TextureIO.newTexture(
                        getClass().getResource("/pic256/healthPowerUp.png"),
                        false,TextureIO.PNG));
                }
             catch(IOException ioe)
             {System.out.println("Problem while initializing the health power up view factory");
              ioe.printStackTrace();
             }
             catch(GLException gle)
             {System.out.println("Problem while initializing the health power up view factory");
              gle.printStackTrace();
             }
             loaded=true;
            }  
        setVertexSetsList(vertexSetsList);
        setTexturesIndicesList(texturesIndicesList);
        setTexturesList(texturesList);       
    }
    
    @Override
    final void loadProgressively(GL gl){
        List<IVertexSet> vertexSetsList=getVertexSetsList();
        List<Texture> texturesList=getTexturesList();
        if(vertexSetsList.size()<vertexSetsCount)
            {if(vertexSet==null)
                 {vertexSet=VertexSetSeeker.getInstance().getIStaticVertexSetInstance(gl,
                         HealthPowerUpControllerFactory.getInstance().getCoordinatesBuffersList().get(0),
                         GL.GL_QUADS);                
                 }
             vertexSetsList.add(vertexSet);
            }
        else
            if(texturesList.size()<texturesCount)
                {try{
                     texturesList.add(TextureIO.newTexture(
                            getClass().getResource("/pic256/healthPowerUp.png"),
                            false,TextureIO.PNG));
                    }
                 catch(IOException ioe)
                 {System.out.println("Problem while initializing the health power up view factory");
                  ioe.printStackTrace();
                 }
                 catch(GLException gle)
                 {System.out.println("Problem while initializing the health power up view factory");
                  gle.printStackTrace();
                 }
                }
        if(loaded==false&&vertexSetsList.size()==vertexSetsCount&&
                texturesList.size()==texturesCount)
            loaded=true;
    }
    
    /**
     * 
     * @param gl
     * @param loadFully: indicates whether the resources are loaded 
     * completely at the first call or rather progressively
     * @return
     */
    final static HealthPowerUpViewFactory getInstance(GL gl,boolean loadFully){
        if(instance==null)
            instance=new HealthPowerUpViewFactory(gl,loadFully);
        if(!loadFully&&!instance.loaded)
            instance.loadProgressively(gl);
        return(instance);
    }
    
    final static HealthPowerUpViewFactory getInstance(GL gl){
        return(getInstance(gl,true));
    }   
    
    final static int getTexturesCount(){
        return(texturesCount);
    }

    final static int getVertexSetsCount(){
        return(vertexSetsCount);
    }
}
