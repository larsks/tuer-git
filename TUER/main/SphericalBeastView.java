package main;

import javax.media.opengl.GL;

final class SphericalBeastView extends Object3DView{
    
    
    SphericalBeastView(GL gl){
        super(SphericalBeastViewFactory.getInstance(gl).getTexturesList(),
                SphericalBeastViewFactory.getInstance(gl).getSecondaryTexturesList(),
                SphericalBeastViewFactory.getInstance(gl).getTexturesIndicesList(),
                SphericalBeastViewFactory.getInstance(gl).getVertexSetsList());
    }
}
