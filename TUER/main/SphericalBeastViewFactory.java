package main;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.media.opengl.GL;
import javax.media.opengl.GLException;

import com.sun.opengl.util.texture.Texture;
import com.sun.opengl.util.texture.TextureIO;

final class SphericalBeastViewFactory extends ViewFactory {

    
    private static SphericalBeastViewFactory instance=null;
    
    private final static int texturesCount=1;
    
    private final static int vertexSetsCount=102;
    
    private boolean loaded;
    
    private SphericalBeastViewFactory(GL gl,boolean loadFully){
        loaded=false;       
        //vertex set
        List<IVertexSet> vertexSetsList=new ArrayList<IVertexSet>(vertexSetsCount);
        if(loadFully)
            {IStaticVertexSet vertexSet;
             for(int i=0;i<vertexSetsCount;i++)
                 {vertexSet=VertexSetSeeker.getInstance().getIStaticVertexSetInstance(gl,
                         SphericalBeastControllerFactory.getInstance().getCoordinatesBuffersList().get(i),
                         GL.GL_QUADS);
                  vertexSetsList.add(vertexSet);
                 }
            }
        //textures indices
        List<Integer> texturesIndicesList=new ArrayList<Integer>(texturesCount);
        texturesIndicesList.add(Integer.valueOf(0));        
        //textures
        List<Texture> texturesList=new ArrayList<Texture>(texturesCount);
        if(loadFully)
            {try{
                 texturesList.add(TextureIO.newTexture(
                        getClass().getResource("/pic256/sphericalBeast.png"),
                        false,TextureIO.PNG));
                }
             catch(IOException ioe)
             {System.out.println("Problem while initializing the spherical beast view factory");
              ioe.printStackTrace();
             }
             catch(GLException gle)
             {System.out.println("Problem while initializing the spherical beast view factory");
              gle.printStackTrace();
             }
            }
        setVertexSetsList(vertexSetsList);
        setTexturesIndicesList(texturesIndicesList);
        setTexturesList(texturesList); 
    }
    
    @Override
    final void loadProgressively(GL gl) {
        if(!loaded)
            {List<IVertexSet> vertexSetsList=getVertexSetsList();
             List<Texture> texturesList=getTexturesList();            
             if(vertexSetsList.size()<vertexSetsCount)
                 {IStaticVertexSet vertexSet=VertexSetSeeker.getInstance().
                  getIStaticVertexSetInstance(gl,SphericalBeastControllerFactory.getInstance().
                          getCoordinatesBuffersList().get(vertexSetsList.size()),
                          GL.GL_QUADS);
                  vertexSetsList.add(vertexSet);
                 }
             else
                 if(texturesList.size()<texturesCount)
                     {try{
                          texturesList.add(TextureIO.newTexture(
                                 getClass().getResource("/pic256/sphericalBeast.png"),
                                 false,TextureIO.PNG));
                         }
                      catch(IOException ioe)
                      {System.out.println("Problem while initializing the spherical beast view factory");
                       ioe.printStackTrace();
                      }
                      catch(GLException gle)
                      {System.out.println("Problem while initializing the spherical beast view factory");
                       gle.printStackTrace();
                      }                     
                     }
             if(!loaded && vertexSetsList.size()==vertexSetsCount &&
                 texturesList.size()==texturesCount)
                 loaded=true;
            }
    }

    final static SphericalBeastViewFactory getInstance(GL gl,boolean loadFully){
        if(instance==null)
            instance=new SphericalBeastViewFactory(gl,loadFully);
        if(!loadFully&&!instance.loaded)
            instance.loadProgressively(gl);
        return(instance);
    }
    
    final static SphericalBeastViewFactory getInstance(GL gl){
        return(getInstance(gl,true));
    }   
    
    final static int getTexturesCount(){
        return(texturesCount);
    }

    final static int getVertexSetsCount(){
        return(vertexSetsCount);
    }
}
