package jme;

import java.io.IOException;
import tools.TilesGenerator;
import com.jme.bounding.BoundingSphere;
import com.jme.renderer.Camera;
import com.jme.scene.Spatial;
import com.jme.util.export.binary.BinaryImporter;
import com.jmex.game.state.BasicGameState;
import com.jmex.game.state.GameState;

public final class LevelGameState extends BasicGameState {


    private ExtendedFirstPersonHandler input;
    
    private long previousTime;
    
    
    public LevelGameState(String name,Camera cam){
        super(name);
        input=new ExtendedFirstPersonHandler(cam,50,1);
        previousTime=System.currentTimeMillis();
    }

    public static GameState getInstance(int index,
            /*TransitionGameState transitionGameState,*/Camera cam){
        LevelGameState levelState=new LevelGameState("",cam);
        //update the transition game state
        //transitionGameState.setProgress(0.5f,"Loading WaveFront OBJ "+index+" ...");
        //load the data
        try{Spatial model=(Spatial)BinaryImporter.getInstance().load(
                    TilesGenerator.class.getResource("/jbin/level"+index+".jbin"));
            //This line below solved my problem
            //model.setLocalScale(1.0f/65536.0f);
            model.setModelBound(new BoundingSphere());
            model.updateModelBound();
            model.updateRenderState();
            //attach it to the root node of the state
            levelState.rootNode.attachChild(model);
            System.out.println("vertex count="+model.getVertexCount());
            System.out.println("free memory = "+Runtime.getRuntime().freeMemory());
            System.out.println("total memory = "+Runtime.getRuntime().totalMemory());
            System.out.println("max memory = "+Runtime.getRuntime().maxMemory());
            levelState.rootNode.updateRenderState();
           } 
        catch(IOException ioe)
        {ioe.printStackTrace();}
        //return a true level game state
        return(levelState);
    }
    
    @Override
    public final void update(final float tpf) {
        super.update(tpf);
        input.update(tpf);
    }
    
    @Override
    public final void render(final float tpf) {
        super.render(tpf);
        long currentTime=System.currentTimeMillis();
        long period=currentTime-previousTime;
        float framePerSecond=(period==0)?0:1000f/period;
        System.out.println("frame rate = "+framePerSecond);
        previousTime=currentTime;
    }

}
