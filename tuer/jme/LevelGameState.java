package jme;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import com.jme.bounding.BoundingSphere;
import com.jme.math.Vector3f;
import com.jme.renderer.Camera;
import com.jme.scene.Spatial;
import com.jme.scene.TriMesh;
import com.jme.scene.VBOInfo;
import com.jme.scene.state.CullState;
import com.jme.scene.state.RenderState;
import com.jme.system.DisplaySystem;
import com.jme.util.export.binary.BinaryImporter;
import com.jmex.game.state.BasicGameState;
import com.jmex.game.state.GameState;

public final class LevelGameState extends BasicGameState {


    private ExtendedFirstPersonHandler input;
    
    private long previousTime;
    
    
    public LevelGameState(String name,Camera cam,JMEGameServiceProvider gameServiceProvider){
        super(name);
        input=new ExtendedFirstPersonHandler(cam,10,1,gameServiceProvider);
        //FIXME: dirty thing to set the first position
        cam.setLocation(new Vector3f(115.0f,0.0f,223.0f));
        previousTime=System.currentTimeMillis();
    }

    public static GameState getInstance(int index,
            /*TransitionGameState transitionGameState,*/Camera cam,
            JMEGameServiceProvider gameServiceProvider){
        LevelGameState levelState=new LevelGameState("",cam,gameServiceProvider);
        //update the transition game state
        //transitionGameState.setProgress(0.5f,"Loading WaveFront OBJ "+index+" ...");
        //load the data
        Spatial model;
        try{//TODO: load the hierarchical data
            URL levelDataDirectoryURL=LevelGameState.class.getResource("/jbin/");
            File levelDataDirectory=new File(levelDataDirectoryURL.toURI());
            FileFilter cellsModelsFilter=new LevelJBINModelsFileFilter(index,true,false,false);
            for(File f:levelDataDirectory.listFiles(cellsModelsFilter))
                {//System.out.println("model: "+f.getName());
                 //TODO: analyze the name to extract the position in the 
                 //forest of graphs
                 model=(Spatial)BinaryImporter.getInstance().load(f);
                 model.setModelBound(new BoundingSphere());
                 model.updateModelBound();
                 //Activate back face culling
                 model.setRenderState(DisplaySystem.getDisplaySystem().getRenderer().createCullState());
                 ((CullState)model.getRenderState(RenderState.StateType.Cull)).setCullFace(CullState.Face.Back);
                 model.updateRenderState();
                 //Use VBO if the required extension is available
                 ((TriMesh)model).setVBOInfo(new VBOInfo(gameServiceProvider.getConfigurationDetector().isVBOsupported()));
                 model.lock();
                 //TODO: use the position in the forest of graphs to 
                 //determine the location of insertion the tree of JME 2.0
                 //attach it to the root node of the state
                 levelState.rootNode.attachChild(model);                
                 levelState.rootNode.updateRenderState();
                }
            /*System.out.println("vertex count="+model.getVertexCount());
            System.out.println("free memory = "+Runtime.getRuntime().freeMemory());
            System.out.println("total memory = "+Runtime.getRuntime().totalMemory());
            System.out.println("max memory = "+Runtime.getRuntime().maxMemory());*/
           } 
        catch(IOException ioe)
        {ioe.printStackTrace();} 
        catch(URISyntaxException urise)
        {urise.printStackTrace();}
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

    private static final class LevelJBINModelsFileFilter implements FileFilter{
        
        
        private int index;       
        
        private boolean includesCells;
        
        private boolean includesPortals;
        
        private boolean includesNonCellsAndPortals;
        
        
        private LevelJBINModelsFileFilter(int index,boolean includesCells,boolean includesPortals,boolean includesNonCellsAndPortals){
            this.index=index;
            this.includesCells=includesCells;
            this.includesPortals=includesPortals;
            this.includesNonCellsAndPortals=includesNonCellsAndPortals;
        }


        @Override
        public boolean accept(File file){
            String filename=file.getName();
            boolean result;
            result=file.isFile()&&filename.endsWith(".jbin")&&filename.startsWith("level"+index);
            if(result&&(!includesPortals||!includesCells||!includesNonCellsAndPortals))
                {//Detects the tag used by the cells and the portals
                 int firstIndexOfCIDTag=filename.indexOf("CID");
                 //If it is in the filename
                 if(firstIndexOfCIDTag!=-1)
                     {//If the tag occurs once, then it is a cell, otherwise it is a portal
                      if(firstIndexOfCIDTag==filename.lastIndexOf("CID"))
                          {if(!includesCells)
                               result=false;
                          }
                      else
                          {if(!includesPortals)
                               result=false;
                          }
                     }
                 else
                     {if(!includesNonCellsAndPortals)
                          result=false;
                     }
                }                       
            return(result);
        }       
    }
}
