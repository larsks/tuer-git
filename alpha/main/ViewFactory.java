package main;

import java.util.List;

import com.jogamp.opengl.util.texture.Texture;

abstract class ViewFactory{

    
    private List<IVertexSet> vertexSetsList;
    
    private List<Texture> texturesList;
    
    private List<Texture> secondaryTexturesList;
    
    private List<Integer> texturesIndicesList;
    
    
    
    
    final List<Texture> getTexturesList(){
        return(texturesList);
    }
    
    final List<Texture> getSecondaryTexturesList(){
        return(secondaryTexturesList);
    }
    
    final List<Integer> getTexturesIndicesList(){
        return(texturesIndicesList);
    }

    final List<IVertexSet> getVertexSetsList(){
        return(vertexSetsList);
    }

    final void setVertexSetsList(List<IVertexSet> vertexSetsList){
        this.vertexSetsList=vertexSetsList;
    }

    final void setTexturesList(List<Texture> texturesList){
        this.texturesList=texturesList;
    }

    final void setSecondaryTexturesList(List<Texture> secondaryTexturesList){
        this.secondaryTexturesList=secondaryTexturesList;
    }

    final void setTexturesIndicesList(List<Integer> texturesIndicesList){
        this.texturesIndicesList=texturesIndicesList;
    }
    
    abstract void loadProgressively();
}
