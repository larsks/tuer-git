package main;

final class SphericalBeastView extends Object3DView{
    
    
    SphericalBeastView(){
        super(SphericalBeastViewFactory.getInstance().getTexturesList(),
                SphericalBeastViewFactory.getInstance().getSecondaryTexturesList(),
                SphericalBeastViewFactory.getInstance().getTexturesIndicesList(),
                SphericalBeastViewFactory.getInstance().getVertexSetsList());
    }
}
