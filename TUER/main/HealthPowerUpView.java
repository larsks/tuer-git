package main;

import javax.media.opengl.GL;

final class HealthPowerUpView extends Object3DView {

    HealthPowerUpView(GL gl){
        super(HealthPowerUpViewFactory.getInstance(gl).getTexturesList(),
                HealthPowerUpViewFactory.getInstance(gl).getSecondaryTexturesList(),
                HealthPowerUpViewFactory.getInstance(gl).getTexturesIndicesList(),
                HealthPowerUpViewFactory.getInstance(gl).getVertexSetsList());
    }  
}
