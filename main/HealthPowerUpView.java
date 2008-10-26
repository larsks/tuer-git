package main;

final class HealthPowerUpView extends Object3DView {

    HealthPowerUpView(){
        super(HealthPowerUpViewFactory.getInstance().getTexturesList(),
                HealthPowerUpViewFactory.getInstance().getSecondaryTexturesList(),
                HealthPowerUpViewFactory.getInstance().getTexturesIndicesList(),
                HealthPowerUpViewFactory.getInstance().getVertexSetsList());
    }  
}
