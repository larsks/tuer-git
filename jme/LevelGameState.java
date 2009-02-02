package jme;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.jme.bounding.BoundingBox;
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
    
    private static final String levelPrefix="level";
    
    private static final String networkIDPrefix="NID";
    
    private static final String cellIDPrefix="CID";
    
    
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
        try{URL levelDataDirectoryURL=LevelGameState.class.getResource("/jbin/");
            File levelDataDirectory=new File(levelDataDirectoryURL.toURI());
            FileFilter cellsModelsFilter=new LevelJBINModelsFileFilter(index,true,false,false);
            for(File f:levelDataDirectory.listFiles(cellsModelsFilter))
                {model=(Spatial)BinaryImporter.getInstance().load(f);
                 model.setModelBound(new /*BoundingSphere*/BoundingBox());
                 model.updateModelBound();
                 //Activate back face culling
                 model.setRenderState(DisplaySystem.getDisplaySystem().getRenderer().createCullState());
                 ((CullState)model.getRenderState(RenderState.StateType.Cull)).setCullFace(CullState.Face.Back);
                 model.updateRenderState();
                 //Use VBO if the required extension is available
                 ((TriMesh)model).setVBOInfo(new VBOInfo(gameServiceProvider.getConfigurationDetector().isVBOsupported()));
                 model.lock();
                 //attach it to the root node of the state
                 //TODO: rather parse its model name and put it into the good list,
                 //      one list per network
                 levelState.rootNode.attachChild(model);                
                 levelState.rootNode.updateRenderState();
                }
            //TODO: create a node per network
            //TODO: for each list of models, add their cells as children of 
            //      the correct node
            FileFilter portalsModelsFilter=new LevelJBINModelsFileFilter(index,false,true,false);
            int[] parsingResult;
            for(File f:levelDataDirectory.listFiles(portalsModelsFilter))
                {model=(Spatial)BinaryImporter.getInstance().load(f);
                 model.setModelBound(new BoundingBox());
                 model.updateModelBound();
                 parsingResult=parseModelName(model.getName());
                 //System.out.println("portal: "+Arrays.toString(parsingResult));
                 //TODO: sort the portals by using their network ID
                 //TODO: create a list of tables, one table per network, each table
                 //      associates a portal with its parsing result
                }
            //TODO: for each network, put each portal into the whole scene graph
           } 
        catch(IOException ioe)
        {ioe.printStackTrace();} 
        catch(URISyntaxException urise)
        {urise.printStackTrace();}
        /*System.out.println("vertex count="+model.getVertexCount());
        System.out.println("free memory = "+Runtime.getRuntime().freeMemory());
        System.out.println("total memory = "+Runtime.getRuntime().totalMemory());
        System.out.println("max memory = "+Runtime.getRuntime().maxMemory());*/
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
    
    private static final int[] parseModelName(String modelname){
        int indexOfNIDTag=modelname.indexOf(networkIDPrefix);
        int levelIndex=Integer.parseInt(modelname.substring(levelPrefix.length(),indexOfNIDTag));
        int indexOfFirstCellIDTag=modelname.indexOf(cellIDPrefix);
        int indexOflastCellIDTag=modelname.lastIndexOf(cellIDPrefix);
        int networkIndex=Integer.parseInt(modelname.substring(indexOfNIDTag+networkIDPrefix.length(),indexOfFirstCellIDTag));
        int firstCellIndex;
        int[] result;
        if(indexOfFirstCellIDTag<indexOflastCellIDTag)
            {firstCellIndex=Integer.parseInt(modelname.substring(indexOfFirstCellIDTag+cellIDPrefix.length(),indexOflastCellIDTag));
             int lastCellIndex=Integer.parseInt(modelname.substring(indexOflastCellIDTag+cellIDPrefix.length(),modelname.length()));
             result=new int[]{levelIndex,networkIndex,firstCellIndex,lastCellIndex};
            }
        else
            {firstCellIndex=Integer.parseInt(modelname.substring(indexOfFirstCellIDTag+cellIDPrefix.length(),modelname.length()));
             result=new int[]{levelIndex,networkIndex,firstCellIndex};
            }
        return(result);
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
            result=file.isFile()&&filename.endsWith(".jbin")&&filename.startsWith(levelPrefix+index);
            if(result&&(!includesPortals||!includesCells||!includesNonCellsAndPortals))
                {//Detects the tag used by the cells and the portals
                 int firstIndexOfCIDTag=filename.indexOf(cellIDPrefix);
                 //If it is in the filename
                 if(firstIndexOfCIDTag!=-1)
                     {//If the tag occurs once, then it is a cell, otherwise it is a portal
                      if(firstIndexOfCIDTag==filename.lastIndexOf(cellIDPrefix))
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
