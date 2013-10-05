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
package engine.statemachine;

import java.io.IOException;
import java.net.URL;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.Callable;
import com.ardor3d.bounding.BoundingBox;
import com.ardor3d.bounding.CollisionTree;
import com.ardor3d.bounding.CollisionTreeManager;
import com.ardor3d.extension.model.util.KeyframeController;
import com.ardor3d.extension.model.util.KeyframeController.PointInTime;
import com.ardor3d.framework.Canvas;
import com.ardor3d.framework.CanvasRenderer;
import com.ardor3d.framework.NativeCanvas;
import com.ardor3d.framework.jogl.JoglNewtWindow;
import com.ardor3d.image.Image;
import com.ardor3d.image.Texture;
import com.ardor3d.image.util.ImageLoaderUtil;
import com.ardor3d.input.GrabbedState;
import com.ardor3d.input.MouseManager;
import com.ardor3d.input.PhysicalLayer;
import com.ardor3d.input.logical.InputTrigger;
import com.ardor3d.input.logical.LogicalLayer;
import com.ardor3d.input.logical.TriggerAction;
import com.ardor3d.input.logical.TwoInputStates;
import com.ardor3d.intersection.BoundingCollisionResults;
import com.ardor3d.intersection.BoundingPickResults;
import com.ardor3d.intersection.CollisionResults;
import com.ardor3d.intersection.PickingUtil;
import com.ardor3d.math.ColorRGBA;
import com.ardor3d.math.Matrix3;
import com.ardor3d.math.Quaternion;
import com.ardor3d.math.Ray3;
import com.ardor3d.math.Vector3;
import com.ardor3d.renderer.Camera;
import com.ardor3d.renderer.RenderContext;
import com.ardor3d.renderer.Renderer;
import com.ardor3d.renderer.queue.RenderBucketType;
import com.ardor3d.renderer.state.RenderState.StateType;
import com.ardor3d.renderer.state.TextureState;
import com.ardor3d.renderer.state.WireframeState;
import com.ardor3d.scenegraph.FloatBufferData;
import com.ardor3d.scenegraph.IndexBufferData;
import com.ardor3d.scenegraph.Mesh;
import com.ardor3d.scenegraph.MeshData;
import com.ardor3d.scenegraph.Node;
import com.ardor3d.scenegraph.Spatial;
import com.ardor3d.scenegraph.controller.ComplexSpatialController.RepeatType;
import com.ardor3d.scenegraph.controller.SpatialController;
import com.ardor3d.scenegraph.event.DirtyType;
import com.ardor3d.scenegraph.extension.CameraNode;
import com.ardor3d.scenegraph.extension.Skybox;
import com.ardor3d.scenegraph.shape.Box;
import com.ardor3d.scenegraph.visitor.Visitor;
import com.ardor3d.ui.text.BasicText;
import com.ardor3d.util.GameTaskQueue;
import com.ardor3d.util.GameTaskQueueManager;
import com.ardor3d.util.ReadOnlyTimer;
import com.ardor3d.util.TextureManager;
import com.ardor3d.util.export.binary.BinaryImporter;
import com.ardor3d.util.geom.BufferUtils;
import com.ardor3d.util.resource.URLResourceSource;
import engine.data.EnemyData;
import engine.data.PlayerData;
import engine.data.ProjectileController;
import engine.data.ProjectileData;
import engine.data.common.Medikit;
import engine.data.common.Teleporter;
import engine.data.common.userdata.AmmunitionUserData;
import engine.data.common.userdata.CollectibleUserData;
import engine.data.common.userdata.MedikitUserData;
import engine.data.common.userdata.TeleporterUserData;
import engine.data.common.userdata.WeaponUserData;
import engine.input.Action;
import engine.input.ActionMap;
import engine.input.ExtendedFirstPersonControl;
import engine.input.MouseAndKeyboardSettings;
import engine.misc.ApplicativeTimer;
import engine.misc.ImageHelper;
import engine.misc.MD2FrameSet;
import engine.misc.NodeHelper;
import engine.sound.SoundManager;
import engine.taskmanagement.TaskManager;
import engine.weaponry.Ammunition;
import engine.weaponry.AmmunitionFactory;
import engine.weaponry.Weapon;
import engine.weaponry.WeaponFactory;

/**
 * State used during the game, the party.
 * 
 * @author Julien Gouesse
 *
 */
public final class GameState extends ScenegraphStateWithCustomCameraParameters{
    
    /**index of the level*/
    private int levelIndex;
    /**Our native window, not the OpenGL surface itself*/
    private final NativeCanvas canvas;
    /**node of the player*/
    private final CameraNode playerNode;
    /**data of the player*/
    private final PlayerData playerData;
    /**player object that relies on a state machine*/
    private final LogicalPlayer playerWithStateMachine;
    /**player's game statistics*/
    private GameStatistics gameStats;
    /**list containing all objects that can be picked up*/
    private final ArrayList<Node> collectibleObjectsList;
    /**list of teleporters*/
    private final ArrayList<Node> teleportersList;
    /**typical teleporter*/
    private final Teleporter teleporter;
    /**typical medical kit*/
    private final Medikit medikit;
    /**text label showing the ammunition*/
    private final BasicText ammoTextLabel;
    /**text label showing the frame rate*/
    private final BasicText fpsTextLabel;
    /**text label showing the health*/
    private final BasicText healthTextLabel;
    /**text label of the head-up display*/
    private final BasicText headUpDisplayLabel;
    /**@deprecated this collision map is a temporary solution, the real collision system will have to use the 3D mesh instead of a flat 2D array*/
    @Deprecated
    private boolean[][] collisionMap;
    /**instance that creates all ammunitions*/
    private final AmmunitionFactory ammunitionFactory;
    /**instance that creates all weapons*/
    private final WeaponFactory weaponFactory;
    /**timer that can be paused and used to measure the elapsed time*/
    private final ApplicativeTimer timer;
    
    private final TaskManager taskManager;
    
    private final BinaryImporter binaryImporter;
    
    private final Map<Node,ProjectileData> projectilesMap;
    
    private final Random random;
    
    private final HashMap<Mesh,EnemyData> enemiesDataMap;
    
    private final HashMap<EnemyData,Long> enemiesLatestDetection;
    
    private Long latestPlayerDeath;
    
    private final ProjectileDataOpponentsComparator projectileDataOpponentsComparator;
    
    private ExtendedFirstPersonControl fpsc;
    
    private final PhysicalLayer physicalLayer;
    
    private final MouseManager mouseManager;
    
    private final TransitionTriggerAction<ScenegraphState,String> toPauseMenuTriggerAction;
    
    private final TransitionTriggerAction<ScenegraphState,String> toPauseMenuTriggerActionForExitConfirm;
    
    private final TransitionTriggerAction<ScenegraphState,String> toGameOverTriggerAction;
    
    private final TriggerAction toggleScreenModeAction;
    
    private final ActionMap defaultActionMap;
    
    private final ActionMap customActionMap;
    
    private final MouseAndKeyboardSettings defaultMouseAndKeyboardSettings;
    
    private final MouseAndKeyboardSettings customMouseAndKeyboardSettings;
    
    private static final String gameoverSoundSamplePath="/sounds/gameover.ogg";
    
    private static String gameoverSoundSampleIdentifier=null;
    
    private static final String pain1soundSamplePath="/sounds/pain1.ogg";
    
    private static final String pain2soundSamplePath="/sounds/pain2.ogg";
    
    private static final String pain3soundSamplePath="/sounds/pain3.ogg";
    
    private static final String pain4soundSamplePath="/sounds/pain4.ogg";
    
    private static final String pain5soundSamplePath="/sounds/pain5.ogg";
    
    private static final String pain6soundSamplePath="/sounds/pain6.ogg";
    
    private static String pain1soundSampleIdentifier=null;
    
    private static String pain2soundSampleIdentifier=null;
    
    private static String pain3soundSampleIdentifier=null;
    
    private static String pain4soundSampleIdentifier=null;
    
    private static String pain5soundSampleIdentifier=null;
    
    private static String pain6soundSampleIdentifier=null;
    
    private static final String enemyShotgunShotSamplePath="/sounds/shotgun_shot.ogg";
    
    private static String enemyShotgunShotSampleIdentifier = null;
    
    private WireframeState wireframeState;
    
    private Skybox skyboxNode;
    
    private Node levelNode;
    
    public GameState(final NativeCanvas canvas,final PhysicalLayer physicalLayer,
    		         final TransitionTriggerAction<ScenegraphState,String> toPauseMenuTriggerAction,
    		         final TransitionTriggerAction<ScenegraphState,String> toPauseMenuTriggerActionForExitConfirm,
    		         final TransitionTriggerAction<ScenegraphState,String> toGameOverTriggerAction,
    		         final TriggerAction toggleScreenModeAction,final SoundManager soundManager,final TaskManager taskManager,
    		         final MouseManager mouseManager,final ActionMap defaultActionMap,final ActionMap customActionMap,
    		         final MouseAndKeyboardSettings defaultMouseAndKeyboardSettings,final MouseAndKeyboardSettings customMouseAndKeyboardSettings){
        super(soundManager,new LogicalLayer(),new Node(),canvas.getCanvasRenderer().getCamera());
        this.mouseManager=mouseManager;
        this.physicalLayer=physicalLayer;
        this.toPauseMenuTriggerAction=toPauseMenuTriggerAction;
        this.toPauseMenuTriggerActionForExitConfirm=toPauseMenuTriggerActionForExitConfirm;
        this.toGameOverTriggerAction=toGameOverTriggerAction;
        this.toggleScreenModeAction=toggleScreenModeAction;
        this.defaultActionMap=defaultActionMap;
        this.customActionMap=customActionMap;
        this.defaultMouseAndKeyboardSettings=defaultMouseAndKeyboardSettings;
        this.customMouseAndKeyboardSettings=customMouseAndKeyboardSettings;
        random=new Random();
        projectileDataOpponentsComparator=new ProjectileDataOpponentsComparator();
        enemiesDataMap=new HashMap<>();
        enemiesLatestDetection=new HashMap<>();
        this.binaryImporter=new BinaryImporter();
        this.taskManager=taskManager;
        timer=new ApplicativeTimer();
        collectibleObjectsList=new ArrayList<>();
        projectilesMap=new HashMap<>();
        teleportersList=new ArrayList<>();        
        teleporter=initializeTeleporter();
        medikit=initializeMedikit();
        //initializes the factories, the build-in ammo and the build-in weapons       
        ammunitionFactory=initializeAmmunitionFactory();
        weaponFactory=initializeWeaponFactory();
        readCollisionMap();
        this.canvas=canvas;
        final Camera cam=canvas.getCanvasRenderer().getCamera();
        //creates a node that follows the camera
        /**
         * draws the content of the player node at last just after clearing the 
         * depth buffer in order to prevent the weapons from being clipped into 
         * 3D objects
         * FIXME detect whether the weapon is close to another 3D object and 
         * update its position
         */
        playerNode=new CameraNode("player",cam){
        	@Override
            public void draw(final Renderer renderer){
        		/**
        		 * Ardor3D doesn't put nodes without render delegate 
        		 * into render queues by default, it must be done here
        		 */
        		final boolean queued;
        		if(!renderer.isProcessingQueue())
        			queued=renderer.checkAndAdd(this);
        		else
        			queued=false;
        		if(!queued)
                    {//clears the depth buffer
        			 renderer.clearBuffers(Renderer.BUFFER_DEPTH);
        			 //renders
                     super.draw(renderer);
                    }
            }
        };
        playerNode.getSceneHints().setRenderBucketType(RenderBucketType.PostBucket);
        //attaches a node for the crosshair to the player node
        final Node crosshairNode=createCrosshairNode();
        playerNode.attachChild(crosshairNode);
        //builds the player data
        playerData=new PlayerData(playerNode,ammunitionFactory,weaponFactory,true){
        	
        	private final FloatBuffer projectileVertexBuffer;
        	
        	{
        		projectileVertexBuffer=BufferUtils.createFloatBuffer(6);
        		projectileVertexBuffer.put(-0.1f).put(-0.1f).put(-0.1f).put(0.1f).put(0.1f).put(0.1f).rewind();
        	}
        	
        	@Override
        	public Map.Entry<Integer,Integer> attack(){
        		final Map.Entry<Integer,Integer> consumedAmmunitionOrKnockCounts=super.attack();
        		final String identifier=getCurrentWeaponBlowOrShotSoundSampleIdentifier();
        		//primary hand
        		for(int index=0;index<consumedAmmunitionOrKnockCounts.getKey().intValue();index++)
        		    {if(isCurrentWeaponAmmunitionCountDisplayable())
        		    	 {//creates a new projectile launched by the primary hand
        		    	  createProjectile(cameraNode.getChild(0));
        		    	 }
        		     if(identifier!=null)
        		    	 soundManager.play(false,false,identifier);
        		    }
        		//secondary hand
        		for(int index=0;index<consumedAmmunitionOrKnockCounts.getValue().intValue();index++)
    		        {if(isCurrentWeaponAmmunitionCountDisplayable())
    		    	     {//creates a new projectile launched by the secondary hand
    		        	  createProjectile(cameraNode.getChild(0));
    		    	     }
    		         if(identifier!=null)
    		    	     soundManager.play(false,false,identifier);
    		        }
        		return(consumedAmmunitionOrKnockCounts);
        	}
        	
        	private final void createProjectile(final Spatial weaponSpatial){
        		//uses the world bound of the primary weapon to compute the initial position of the shot
		    	final Vector3 initialLocation=new Vector3(weaponSpatial.getWorldBound().getCenter());
		    	final String originator=playerNode.getName();
		    	final double initialSpeed=350.0/1000000000.0;
		    	final double initialAcceleration=0;
		    	final Vector3 initialDirection=weaponSpatial.getWorldTransform().getMatrix().getColumn(2,null);
		    	final long initialTimeInNanos=timer.getElapsedTimeInNanoseconds();
		    	final ProjectileData projectileData=new ProjectileData(originator,initialLocation,initialSpeed,initialAcceleration,
		    	initialDirection,initialTimeInNanos);
		    	final Node projectileNode=new Node(projectileData.toString());
		    	NodeHelper.setModelBound(projectileNode,BoundingBox.class);
		    	projectileNode.setTransform(weaponSpatial.getWorldTransform());
		    	projectileNode.setTranslation(initialLocation);
		    	//TODO use the correct node
		    	//final WeaponUserData weaponUserData=(WeaponUserData)((Node)weaponSpatial).getUserData();
		        //final Weapon weapon=weaponUserData.getWeapon();
		    	Mesh projectileMesh=createProjectileMesh(null,projectileData);
		        projectileNode.attachChild(projectileMesh);
		    	projectileNode.addController(new ProjectileController(timer,projectileData));
		    	//stores it for a further use
		    	projectilesMap.put(projectileNode,projectileData);
		    	getRoot().attachChild(projectileNode);
        	}
        	
        	private Mesh createProjectileMesh(final Weapon weapon,final ProjectileData projectileData){
        		//TODO support several kinds of projectile with different sizes
        		Mesh projectileMesh=new Mesh("Mesh@"+projectileData.toString());
        		MeshData projectileMeshData=new MeshData();
        		projectileMeshData.setVertexBuffer(projectileVertexBuffer);
        		projectileMesh.setMeshData(projectileMeshData);
        		return(projectileMesh);
        	}
        	
        	@Override
        	public int reload(){
        		final int reloadedAmmoCount=super.reload();
        		if(reloadedAmmoCount>0)
    		        {final String identifier=getCurrentWeaponReloadSoundSampleIdentifier();
    		         if(identifier!=null)
   			             soundManager.play(false,false,identifier);
    		        }
        		return(reloadedAmmoCount);
        	}
        };
        latestPlayerDeath=null;
        playerWithStateMachine=new LogicalPlayer(playerData);
        //initializes all text displays
        ammoTextLabel=initializeAmmunitionTextLabel();
        fpsTextLabel=initializeFpsTextLabel();
        healthTextLabel=initializeHealthTextLabel();
        headUpDisplayLabel=initializeHeadUpDisplayLabel();
        initializeCollisionSystem(cam);
        wireframeState=new WireframeState();
        wireframeState.setEnabled(false);
        getRoot().setRenderState(wireframeState);
    }
    
    @SuppressWarnings("cast")
	private final Node createCrosshairNode() {
    	final Node crosshairNode=new Node("crosshair");
        final Mesh crosshairMesh=new Mesh();
        final MeshData crosshairMeshData=new MeshData();
        final Camera cam=playerNode.getCamera();
        final FloatBuffer crosshairVertexBuffer=BufferUtils.createFloatBuffer(36);
        final float halfSmallWidth=0.0004f*1920.0f/((float)cam.getWidth());
		final float halfSmallHeight=0.0004f*1080.0f/((float)cam.getHeight());
	    final float halfBigWidth=0.005f*1920.0f/((float)cam.getWidth());
	    final float halfBigHeight=0.005f*1080.0f/((float)cam.getHeight());
        final float z=1.0f;
        crosshairVertexBuffer.put(-halfSmallWidth).put(-halfBigHeight).put(z);
	    crosshairVertexBuffer.put(-halfSmallWidth).put(+halfBigHeight).put(z);
	    crosshairVertexBuffer.put(+halfSmallWidth).put(+halfBigHeight).put(z);
	    crosshairVertexBuffer.put(+halfSmallWidth).put(+halfBigHeight).put(z);
	    crosshairVertexBuffer.put(+halfSmallWidth).put(-halfBigHeight).put(z);
	    crosshairVertexBuffer.put(-halfSmallWidth).put(-halfBigHeight).put(z);
	    crosshairVertexBuffer.put(-halfBigWidth).put(-halfSmallHeight).put(z);
	    crosshairVertexBuffer.put(-halfBigWidth).put(+halfSmallHeight).put(z);
	    crosshairVertexBuffer.put(+halfBigWidth).put(+halfSmallHeight).put(z);
	    crosshairVertexBuffer.put(+halfBigWidth).put(+halfSmallHeight).put(z);
	    crosshairVertexBuffer.put(+halfBigWidth).put(-halfSmallHeight).put(z);
	    crosshairVertexBuffer.put(-halfBigWidth).put(-halfSmallHeight).put(z);
        crosshairVertexBuffer.rewind();
        crosshairMeshData.setVertexBuffer(crosshairVertexBuffer);
        final FloatBuffer crosshairColorBuffer=BufferUtils.createFloatBuffer(48);
        for(int vertexIndex=0;vertexIndex<12;vertexIndex++)
		     crosshairColorBuffer.put(1.0f).put(0.0f).put(0.0f).put(1.0f);
        crosshairColorBuffer.rewind();
        crosshairMeshData.setColorBuffer(crosshairColorBuffer);
        crosshairMesh.setMeshData(crosshairMeshData);
        crosshairNode.attachChild(crosshairMesh);
        return(crosshairNode);
    }
    
    @SuppressWarnings("cast")
	private final void updateCrosshairNode(){
    	final Node crosshairNode=(Node)playerNode.getChild("crosshair");
    	if(crosshairNode!=null)
    	    {final Mesh crosshairMesh=(Mesh)crosshairNode.getChild(0);
             final MeshData crosshairMeshData=crosshairMesh.getMeshData();
    		 final FloatBuffer crosshairVertexBuffer=crosshairMeshData.getVertexBuffer();
    		 final Camera cam=playerNode.getCamera();
    		 final float halfSmallWidth=0.0004f*1920.0f/((float)cam.getWidth());
    		 final float halfSmallHeight=0.0004f*1080.0f/((float)cam.getHeight());
    	     final float halfBigWidth=0.005f*1920.0f/((float)cam.getWidth());
    	     final float halfBigHeight=0.005f*1080.0f/((float)cam.getHeight());
    	     final float z=1.0f;
    	     crosshairVertexBuffer.rewind();
    	     crosshairVertexBuffer.put(-halfSmallWidth).put(-halfBigHeight).put(z);
    	     crosshairVertexBuffer.put(-halfSmallWidth).put(+halfBigHeight).put(z);
    	     crosshairVertexBuffer.put(+halfSmallWidth).put(+halfBigHeight).put(z);
    	     crosshairVertexBuffer.put(+halfSmallWidth).put(+halfBigHeight).put(z);
    	     crosshairVertexBuffer.put(+halfSmallWidth).put(-halfBigHeight).put(z);
    	     crosshairVertexBuffer.put(-halfSmallWidth).put(-halfBigHeight).put(z);
    	     crosshairVertexBuffer.put(-halfBigWidth).put(-halfSmallHeight).put(z);
    	     crosshairVertexBuffer.put(-halfBigWidth).put(+halfSmallHeight).put(z);
    	     crosshairVertexBuffer.put(+halfBigWidth).put(+halfSmallHeight).put(z);
    	     crosshairVertexBuffer.put(+halfBigWidth).put(+halfSmallHeight).put(z);
    	     crosshairVertexBuffer.put(+halfBigWidth).put(-halfSmallHeight).put(z);
    	     crosshairVertexBuffer.put(-halfBigWidth).put(-halfSmallHeight).put(z);
    	     crosshairVertexBuffer.rewind();
    		 final FloatBuffer crosshairColorBuffer=crosshairMeshData.getColorBuffer();
    		 crosshairColorBuffer.rewind();
    		 for(int vertexIndex=0;vertexIndex<12;vertexIndex++)
    		     crosshairColorBuffer.put(1.0f).put(0.0f).put(0.0f).put(1.0f);
    	     crosshairColorBuffer.rewind();
    	     crosshairMeshData.getVertexCoords().setNeedsRefresh(true);
    	     crosshairMeshData.getColorCoords().setNeedsRefresh(true);
    	    }
    }
    
    private final void initializeCollisionSystem(final Camera cam){
    	//configures the collision system
        CollisionTreeManager.getInstance().setTreeType(CollisionTree.Type.AABB);       
        //adds a mesh with an invisible mesh data
        final Mesh playerMesh=new Mesh("player");
        final MeshData playerMeshData=new MeshData();
        final FloatBuffer playerVertexBuffer=BufferUtils.createFloatBuffer(6);
        playerVertexBuffer.put(-0.5f).put(-0.9f).put(-0.5f).put(0.5f).put(0.9f).put(0.5f).rewind();
        playerMeshData.setVertexBuffer(playerVertexBuffer);
        playerMesh.setMeshData(playerMeshData);
        playerNode.attachChild(playerMesh);
        //adds a bounding box to the camera node
        NodeHelper.setModelBound(playerNode,BoundingBox.class);
        playerNode.addController(new SpatialController<Spatial>(){
        	
            private final FloatBuffer projectileVertexBuffer;
        	
        	{
        		projectileVertexBuffer=BufferUtils.createFloatBuffer(6);
        		projectileVertexBuffer.put(-0.1f).put(-0.1f).put(-0.1f).put(0.1f).put(0.1f).put(0.1f).rewind();
        	}
        	
        	private final CollisionResults collisionResults=new BoundingCollisionResults();
        	
        	private Vector3 previousPosition=new Vector3(115,0.5,223);
        	
        	private boolean wasBeingTeleported=false;
        	
        	//private long previouslyMeasuredElapsedTime=-1;
        	
            @SuppressWarnings({ "unchecked", "cast" })
			@Override
            public void update(double timeSinceLastCall,Spatial caller){
            	//updates the timer
            	timer.update();
            	final long absoluteElapsedTimeInNanoseconds=timer.getElapsedTimeInNanoseconds();
            	/*final long elapsedTimeSinceLatestCallInNanos=previouslyMeasuredElapsedTime==-1?0:absoluteElapsedTimeInNanoseconds-previouslyMeasuredElapsedTime;
            	previouslyMeasuredElapsedTime=absoluteElapsedTimeInNanoseconds;*/
                //synchronizes the camera node with the camera
                playerNode.updateFromCamera();
                //temporary avoids to move on Y
                playerNode.addTranslation(0,0.5-playerNode.getTranslation().getY(),0);
                //synchronizes the camera with the camera node
                cam.setLocation(playerNode.getTranslation());
                //FIXME remove this temporary system
                double playerStartX=previousPosition.getX();
                double playerStartZ=previousPosition.getZ();
                double playerEndX=playerNode.getTranslation().getX();
                double playerEndZ=playerNode.getTranslation().getZ();
                double playerX,playerZ;
                double distance=previousPosition.distance(playerNode.getTranslation());
                int stepCount=(int)Math.ceil(distance/0.2);
                double stepX=stepCount==0?0:(playerEndX-playerStartX)/stepCount;
                double stepZ=stepCount==0?0:(playerEndZ-playerStartZ)/stepCount;
                boolean collisionFound=false;
                double correctX=playerStartX,correctZ=playerStartZ;
                int tmpX,tmpZ;
                //temporary hack to disable collisions in some levels
                final boolean useCollisionMap=levelIndex==0||levelIndex==1;
                for(int i=1;i<=stepCount&&!collisionFound;i++)
                    {playerX=playerStartX+(stepX*i);
                	 playerZ=playerStartZ+(stepZ*i);
                	 for(int z=0;z<2&&!collisionFound;z++)
             	    	for(int x=0;x<2&&!collisionFound;x++)
             	    	    {tmpX=(int)(playerX-0.2+(x*0.4));
             	    	     tmpZ=(int)(playerZ-0.2+(z*0.4));
             	    	     if(useCollisionMap&&0<=tmpX&&tmpX<collisionMap.length&&0<=tmpZ&&tmpZ<collisionMap[tmpX].length)
             	    		     collisionFound=collisionMap[tmpX][tmpZ];
             	    	     else
             	    	    	 collisionFound=false;
             	    	    }
                	 if(!collisionFound)
                		 {correctX=playerX;
                		  correctZ=playerZ;
                		 }
                	 else
                		 if(stepX!=0&&stepZ!=0)
                	         {collisionFound=false;
                	          playerZ=playerStartZ+(stepZ*(i-1));
                	          for(int z=0;z<2&&!collisionFound;z++)
                       	    	  for(int x=0;x<2&&!collisionFound;x++)
                       	    	      {tmpX=(int)(playerX-0.2+(x*0.4));
                       	    	       tmpZ=(int)(playerZ-0.2+(z*0.4));
                       	    	       if(useCollisionMap&&0<=tmpX&&tmpX<collisionMap.length&&0<=tmpZ&&tmpZ<collisionMap[tmpX].length)
                       	    		       collisionFound=collisionMap[tmpX][tmpZ];
                       	    	       else
                       	    	    	   collisionFound=false;
                       	    	      }
                	          if(!collisionFound)
                     		      {correctX=playerX;
                     		       correctZ=playerZ;
                     		      }
                     	      else
                     	          {collisionFound=false;
                     	    	   playerX=playerStartX+(stepX*(i-1));
                     	    	   playerZ=playerStartZ+(stepZ*i);
                     	    	   for(int z=0;z<2&&!collisionFound;z++)
                           	    	   for(int x=0;x<2&&!collisionFound;x++)
                           	    	       {tmpX=(int)(playerX-0.2+(x*0.4));
                           	    	        tmpZ=(int)(playerZ-0.2+(z*0.4));
                           	    	        if(useCollisionMap&&0<=tmpX&&tmpX<collisionMap.length&&0<=tmpZ&&tmpZ<collisionMap[tmpX].length)
                           	    		        collisionFound=collisionMap[tmpX][tmpZ];
                           	    	        else
                           	    	    	    collisionFound=false;
                           	    	       }
                     	    	   if(!collisionFound)
                     		           {correctX=playerX;
                     		            correctZ=playerZ;
                     		           }
                     	          }
                	         }
                    }
                //updates the current location
                playerNode.setTranslation(correctX,0.5,correctZ);
                //updates the previous location and the camera
                previousPosition.set(playerNode.getTranslation());
                cam.setLocation(playerNode.getTranslation());
                if(skyboxNode!=null)
                    skyboxNode.setTranslation(playerNode.getTranslation());
                //checks if any object is collected
                Node collectibleNode;
                String subElementName;
                for(int i=collectibleObjectsList.size()-1,collectedSubElementsCount;i>=0;i--)
                    {collectibleNode=collectibleObjectsList.get(i);
                	 PickingUtil.findCollisions(collectibleNode,playerNode,collisionResults);
                	 if(collisionResults.getNumber()>0)
                	     {//tries to collect the object (update the player model (MVC))
                		  collectedSubElementsCount=playerData.collect(collectibleNode);
                		  //if it succeeds, detach the object from the root later
                		  if(collectedSubElementsCount>0)
                	          {//removes it from the list of collectible objects
                			   collectibleObjectsList.remove(i);
                			   if(collectibleNode.getParent()!=null)
                				   //detach this object from its parent so that it is no more visible
                				   collectibleNode.getParent().detachChild(collectibleNode);
                			   CollectibleUserData<?> collectibleUserData=(CollectibleUserData<?>)collectibleNode.getUserData();
                			   //displays a message when the player picked up something
                			   subElementName=collectibleUserData.getSubElementName();
                			   if(subElementName!=null && !subElementName.equals(""))
                				   headUpDisplayLabel.setText("picked up "+collectedSubElementsCount+" "+subElementName+(collectedSubElementsCount>1?"s":""));
                			   else
                			       headUpDisplayLabel.setText("picked up "+collectibleNode.getName());               	           
                	           //plays a sound if available
                	           if(collectibleUserData.getPickingUpSoundSampleIdentifier()!=null)
                                   getSoundManager().play(false,false,collectibleUserData.getPickingUpSoundSampleIdentifier());
                	          }
                	     }
                	 collisionResults.clear();
                    }
                //checks if any teleporter is used
                Node teleporterNode;
                boolean hasCollision=false;
                for(int i=teleportersList.size()-1;i>=0&&!hasCollision;i--)
                    {teleporterNode=teleportersList.get(i);
                     PickingUtil.findCollisions(teleporterNode,playerNode,collisionResults);
                     hasCollision=collisionResults.getNumber()>0;
                     collisionResults.clear();
                     //if the current position is inside a teleporter
                     if(hasCollision)
                         {/**
                           * The teleporter can be bi-directional. A player who was being teleported
                           * in a direction should not be immediately teleported in the opposite
                           * direction. I use a flag to avoid this case because applying naively 
                           * the algorithm would be problematic as the previous position is 
                           * outside the teleporter and the current position is inside the 
                           * teleporter.
                           */
                    	   //if the previous position is not on any teleporter
                      	   if(!wasBeingTeleported)
                               {//the player enters a teleporter                        	    
                      		    wasBeingTeleported=true;
                      		    TeleporterUserData teleporterUserData=(TeleporterUserData)teleporterNode.getUserData();
                      		    Vector3 teleporterDestination=teleporterUserData.getDestination();
                      		    //then moves him
                      		    playerNode.setTranslation(teleporterDestination);
                      		    //updates the previous location to avoid any problem when detecting the collisions
                                previousPosition.set(teleporterDestination);
                                //synchronizes the camera with the camera node
                                cam.setLocation(teleporterDestination);
                                //play a sound if available
                 	            if(teleporterUserData.getPickingUpSoundSampleIdentifier()!=null)
                                    getSoundManager().play(false,false,teleporterUserData.getPickingUpSoundSampleIdentifier());
                	           }
                	      }                          
                    }
                //if the player is not on any teleporter
                if(!hasCollision)
                	wasBeingTeleported=false;
                //handles the collisions between enemies and projectiles
                HashSet<Node> projectilesToRemove=new HashSet<>();
                ArrayList<EnemyData> editedEnemiesData=new ArrayList<>();
                //filters data to keep only the valid opponents
                final List<Spatial> reachableOpponents=new ArrayList<>();
                for(Spatial child:getRoot().getChildren())
            	    {if((enemiesDataMap.keySet().contains(child)||child.equals(playerNode)))
               		     reachableOpponents.add(child);
            	    }
                for(Entry<Node,ProjectileData> projectileEntry:projectilesMap.entrySet())
                    {final Node projectileNode=projectileEntry.getKey();
                	 final ProjectileData projectileData=projectileEntry.getValue();
                	 hasCollision=false;
                	 //prevents the originator from committing a suicide
                	 final Spatial originator=getRoot().getChild(projectileData.getOriginator());
                	 reachableOpponents.remove(originator);
                	 //sorts the opponents to perform a collision check on the closest one
                	 projectileDataOpponentsComparator.setProjectileData(projectileData);
                	 Collections.sort(reachableOpponents,projectileDataOpponentsComparator);
                	 projectileDataOpponentsComparator.setProjectileData(null);
                	 for(Spatial child:reachableOpponents)
                	     {if(enemiesDataMap.keySet().contains(child))
                              {Ray3 ray=new Ray3(projectileNode.getTranslation(),projectileNode.getTransform().getMatrix().getColumn(2,null));
                               BoundingPickResults results=new BoundingPickResults();
                               PickingUtil.findPick(child,ray,results);
                               hasCollision=results.getNumber()>0;
                               results.clear();
                               if(hasCollision)
                                   {/**
                                     * TODO - Create a data model (for the enemy) containing the current state, the health, the ammunition, ...
                                     *      As a first step, it should be very limited. On the long term, it will have to be homogeneous with the 
                                     *      data model used for the player so that any enemy can behave like a bot in the arena mode
                                     *      - Create another controller to modify the view depending on the changes in the data model
                                     */
                            	    //attempts to kill this enemy
                            	    final EnemyData soldierData=enemiesDataMap.get(child);
                            	    editedEnemiesData.add(soldierData);
                            	    final KeyframeController<Mesh> soldierKeyframeController=(KeyframeController<Mesh>)child.getController(0);
                            	    if(soldierData.isAlive())
                            	  	    {soldierData.decreaseHealth(25);
                            	         //stops at the last frame of the set in the supplied time frame
                                         soldierKeyframeController.setRepeatType(RepeatType.CLAMP);
                                         //selects randomly the death kind
                                         final int localFrameIndex;
                                         if(soldierData.isAlive())
                                             localFrameIndex=3+random.nextInt(3);
                             	         else
                             	             localFrameIndex=random.nextInt(3);
                                         final MD2FrameSet frameSet;
                                         switch(localFrameIndex)
                                         {
                                             case 0:
                                   	             frameSet=MD2FrameSet.DEATH_FALLFORWARD;
                                   	             break;
                                             case 1:
                                   	             frameSet=MD2FrameSet.DEATH_FALLBACK;
                                   	             break;
                                             case 2:
                                   	             frameSet=MD2FrameSet.DEATH_FALLBACKSLOW;
                                   	             break;
                                             case 3:
                                                 frameSet=MD2FrameSet.PAIN_A;
                                                 break;
                                             case 4:
                                                 frameSet=MD2FrameSet.PAIN_B;
                                                 break;
                                             case 5:
                                                 frameSet=MD2FrameSet.PAIN_C;
                                                 break;
                                             default:
                                                 frameSet=null;	  
                                         }
                                         if(frameSet!=null)
                                             {soldierKeyframeController.setSpeed(frameSet.getFramesPerSecond());
                                              soldierKeyframeController.setCurTime(frameSet.getFirstFrameIndex());
                                              soldierKeyframeController.setMinTime(frameSet.getFirstFrameIndex());
                                              soldierKeyframeController.setMaxTime(frameSet.getLastFrameIndex());
                                              final Mesh soldierWeaponMesh=(Mesh)getRoot().getChild((getRoot().getChildren().indexOf(child)+1));
                                              if(localFrameIndex>=3)
                                                  {final KeyframeController<Mesh> soldierWeaponKeyframeController=(KeyframeController<Mesh>)soldierWeaponMesh.getController(0);
                                                   soldierWeaponKeyframeController.setSpeed(frameSet.getFramesPerSecond());
                                                   soldierWeaponKeyframeController.setCurTime(frameSet.getFirstFrameIndex());
                                                   soldierWeaponKeyframeController.setMinTime(frameSet.getFirstFrameIndex());
                                                   soldierWeaponKeyframeController.setMaxTime(frameSet.getLastFrameIndex());
                                                  }
                                              else
                                            	  {//there are only 173 frames for weapons
                                            	   soldierWeaponMesh.setVisible(false);
                                            	  }
                                             }
                                         //plays a sound if the enemy is not dead
                                         if(soldierData.getHealth()<=17)
                                             getSoundManager().play(false,false,pain6soundSampleIdentifier);
                                         else
                                      	     if(soldierData.getHealth()<=33)
                                                 getSoundManager().play(false,false,pain5soundSampleIdentifier);
                                      	     else
                                       		     if(soldierData.getHealth()<=50)
                                                     getSoundManager().play(false,false,pain4soundSampleIdentifier);
                                       		     else
                                       			     if(soldierData.getHealth()<=67)
                                                         getSoundManager().play(false,false,pain3soundSampleIdentifier);
                                   	                 else
                                   	            	     if(soldierData.getHealth()<=83)
                                                             getSoundManager().play(false,false,pain2soundSampleIdentifier);
                                                         else
                                                       	     getSoundManager().play(false,false,pain1soundSampleIdentifier);
                                        }
                            	    //FIXME only remove the projectile if it doesn't pass through the enemy
                                    projectilesToRemove.add(projectileNode);
                            	    break;
                                   }
                               }
                		   else
                			   if(child.equals(playerNode))
                				   {Ray3 ray=new Ray3(projectileNode.getTranslation(),projectileNode.getTransform().getMatrix().getColumn(2,null));
                                    BoundingPickResults results=new BoundingPickResults();
                                    PickingUtil.findPick(child,ray,results);
                                    hasCollision=results.getNumber()>0;
                                    results.clear();                                   
                                    if(hasCollision)
                                        {if(playerData.isAlive())
                                             {playerData.decreaseHealth(10);
                                              if(playerData.getHealth()<=17)
                                                  getSoundManager().play(false,false,pain6soundSampleIdentifier);
                                              else
                                                  if(playerData.getHealth()<=33)
                                                      getSoundManager().play(false,false,pain5soundSampleIdentifier);
                                      	          else
                                      		          if(playerData.getHealth()<=50)
                                                          getSoundManager().play(false,false,pain4soundSampleIdentifier);
                                      		          else
                                      			          if(playerData.getHealth()<=67)
                                                              getSoundManager().play(false,false,pain3soundSampleIdentifier);
                                  	                      else
                                  	                          if(playerData.getHealth()<=83)
                                                                  getSoundManager().play(false,false,pain2soundSampleIdentifier);
                                                              else
                                                      	          getSoundManager().play(false,false,pain1soundSampleIdentifier);
                                             }
                                         //FIXME only remove the projectile if it doesn't pass through the player
                                         projectilesToRemove.add(projectileNode);
                               	         break;
                                        }
                				   }
                	     }
                	 //resets the list of opponents
                	 reachableOpponents.add(originator);
                    }
                //FIXME only remove "infinite" rays
                //as all projectiles are designed with rays, they shouldn't stay in the data model any longer
                projectilesToRemove.addAll(projectilesMap.keySet());
                //FIXME move this logic into a state machine
                for(Entry<Mesh,EnemyData> enemyEntry:enemiesDataMap.entrySet())
                	{EnemyData enemyData=enemyEntry.getValue();
                	 if(!editedEnemiesData.contains(enemyData)&&enemyData.isAlive())
                	     {final Mesh enemyMesh=enemyEntry.getKey();
                	      final Mesh enemyWeaponMesh=(Mesh)getRoot().getChild((getRoot().getChildren().indexOf(enemyMesh)+1));
                		  final KeyframeController<Mesh> enemyKeyframeController=(KeyframeController<Mesh>)enemyMesh.getController(0);
                		  final KeyframeController<Mesh> enemyWeaponKeyframeController=(KeyframeController<Mesh>)enemyWeaponMesh.getController(0);
                		  //if this enemy is not yet idle and if he has finished his latest animation
                		  if(/*enemyKeyframeController.isRepeatTypeClamp()&&*/
                			 enemyKeyframeController.getMaxTime()!=MD2FrameSet.STAND.getLastFrameIndex())
                		      {if(enemyKeyframeController.getCurTime()>enemyKeyframeController.getMaxTime())
                			       {enemyKeyframeController.setRepeatType(RepeatType.WRAP);
                			        //uses the "stand" animation
                			        enemyKeyframeController.setSpeed(MD2FrameSet.STAND.getFramesPerSecond());
                			        enemyKeyframeController.setCurTime(MD2FrameSet.STAND.getFirstFrameIndex());
                			        enemyKeyframeController.setMinTime(MD2FrameSet.STAND.getFirstFrameIndex());
                			        enemyKeyframeController.setMaxTime(MD2FrameSet.STAND.getLastFrameIndex());
                			        
                			        enemyWeaponKeyframeController.setRepeatType(RepeatType.WRAP);
                			        enemyWeaponKeyframeController.setSpeed(MD2FrameSet.STAND.getFramesPerSecond());
                			        enemyWeaponKeyframeController.setCurTime(MD2FrameSet.STAND.getFirstFrameIndex());
                			        enemyWeaponKeyframeController.setMinTime(MD2FrameSet.STAND.getFirstFrameIndex());
                			        enemyWeaponKeyframeController.setMaxTime(MD2FrameSet.STAND.getLastFrameIndex());
                			       }
                		      }
                		  else
                		      {Long latestDetectionObj=enemiesLatestDetection.get(enemyData);
                			   if((latestDetectionObj==null||absoluteElapsedTimeInNanoseconds-latestDetectionObj.longValue()>=1000000000)&&playerData.isAlive())
                		           {enemiesLatestDetection.put(enemyData,Long.valueOf(absoluteElapsedTimeInNanoseconds));
                		            //checks whether the player is in front of this enemy (defensive behavior)
                		            Ray3 ray=new Ray3(playerNode.getTranslation(),playerNode.getTransform().getMatrix().getColumn(2,null));
                                    BoundingPickResults results=new BoundingPickResults();
                                    PickingUtil.findPick(enemyMesh,ray,results);
                                    hasCollision=results.getNumber()>0;
                                    results.clear();
                                    if(hasCollision)
                                        {enemyKeyframeController.setRepeatType(RepeatType.CLAMP);
                    			         //uses the "attack" animation
                    			         enemyKeyframeController.setSpeed(MD2FrameSet.ATTACK.getFramesPerSecond());
                    			         enemyKeyframeController.setCurTime(MD2FrameSet.ATTACK.getFirstFrameIndex());
                    			         enemyKeyframeController.setMinTime(MD2FrameSet.ATTACK.getFirstFrameIndex());
                    			         enemyKeyframeController.setMaxTime(MD2FrameSet.ATTACK.getLastFrameIndex());
                    			         
                    			         enemyWeaponKeyframeController.setRepeatType(RepeatType.CLAMP);
                    			         enemyWeaponKeyframeController.setSpeed(MD2FrameSet.ATTACK.getFramesPerSecond());
                    			         enemyWeaponKeyframeController.setCurTime(MD2FrameSet.ATTACK.getFirstFrameIndex());
                    			         enemyWeaponKeyframeController.setMinTime(MD2FrameSet.ATTACK.getFirstFrameIndex());
                    			         enemyWeaponKeyframeController.setMaxTime(MD2FrameSet.ATTACK.getLastFrameIndex());
                    			         
                    			         //creates a new projectile
                    			         createEnemyProjectile(enemyData,enemyMesh,enemyWeaponMesh);
                    			         getSoundManager().play(false,false,enemyShotgunShotSampleIdentifier);
                                        }
                		           }
                		      }
                	     }
                	}
                for(Node projectileToRemove:projectilesToRemove)
                    {projectilesMap.remove(projectileToRemove);
                	 getRoot().detachChild(projectileToRemove);
                    }
                if(playerData.isAlive())
                    {fpsc.setKeyRotateSpeed(customMouseAndKeyboardSettings.getKeyRotateSpeed());
               	     fpsc.setMouseRotateSpeed(customMouseAndKeyboardSettings.getMouseRotateSpeed());
               	     fpsc.setMoveSpeed(customMouseAndKeyboardSettings.getMoveSpeed());
                    }
                else
                    {if(latestPlayerDeath==null)
                         {gameStats.setMissionStatus(MissionStatus.DECEASED);
                    	  fpsc.setKeyRotateSpeed(0);
                	      fpsc.setMouseRotateSpeed(0);
                	      fpsc.setMoveSpeed(0);
                    	  latestPlayerDeath=Long.valueOf(absoluteElapsedTimeInNanoseconds);
                    	  getSoundManager().play(false,false,gameoverSoundSampleIdentifier);
                         }
                     else
                         {final long latestDeathDuration=absoluteElapsedTimeInNanoseconds-latestPlayerDeath.longValue();
                    	  final double y=0.4d-((((double)Math.max(0,Math.min(latestDeathDuration,500000000)))/500000000.0d)*0.4d)+0.1d;
                    	  playerNode.setTranslation(playerNode.getTranslation().getX(),y,playerNode.getTranslation().getZ());
                    	  playerNode.getCamera().setLocation(playerNode.getTranslation());
                    	  /**
                    	   * TODO add a mechanism into the entry action of this state to handle the figures of the player.
                    	   * It would help to know why he enters this state and which level(s) should be available
                    	   */
                    	  if(latestDeathDuration>500000000)
                    	      {((int[])toGameOverTriggerAction.arguments.getFirst())[0]=levelIndex;
                    	       //the player can't go to the next level when he dies
                    	       ((int[])toGameOverTriggerAction.arguments.getFirst())[1]=-1;
                    		   toGameOverTriggerAction.perform(null,null,-1);
                    	      }
                         }
                    }
                //updates the state machine of the player
                playerWithStateMachine.updateLogicalLayer(timer);
            }

            private void createEnemyProjectile(EnemyData enemyData,Mesh enemyMesh,Mesh enemyWeaponMesh){
            	final Vector3 initialLocation=new Vector3(enemyWeaponMesh.getWorldBound().getCenter());
		    	final String originator=enemyMesh.getName();
		    	final double initialSpeed=350.0/1000000000.0;
		    	final double initialAcceleration=0;
		    	final Vector3 initialDirection=enemyWeaponMesh.getWorldTransform().getMatrix().getColumn(2,null);
		    	final long initialTimeInNanos=timer.getElapsedTimeInNanoseconds();
		    	final ProjectileData projectileData=new ProjectileData(originator,initialLocation,initialSpeed,initialAcceleration,
		    	initialDirection,initialTimeInNanos);
		    	final Node projectileNode=new Node(projectileData.toString());
		    	NodeHelper.setModelBound(projectileNode,BoundingBox.class);
		    	projectileNode.setTransform(enemyWeaponMesh.getWorldTransform());
		    	projectileNode.setTranslation(initialLocation);        		    	  
		    	final Mesh projectileMesh=createProjectileMesh(null,projectileData);
		        projectileNode.attachChild(projectileMesh);
		    	projectileNode.addController(new ProjectileController(timer,projectileData));
		    	//stores it for a further use
		    	projectilesMap.put(projectileNode,projectileData);
		    	getRoot().attachChild(projectileNode);
            }
            
            private Mesh createProjectileMesh(final Weapon weapon,final ProjectileData projectileData){
        		//TODO support several kinds of projectile with different sizes
        		Mesh projectileMesh=new Mesh("Mesh@"+projectileData.toString());
        		MeshData projectileMeshData=new MeshData();
        		projectileMeshData.setVertexBuffer(projectileVertexBuffer);
        		projectileMesh.setMeshData(projectileMeshData);
        		return(projectileMesh);
        	}
        });
    }
    
    private static final class ProjectileDataOpponentsComparator implements Comparator<Spatial>{

		 private ProjectileData projectileData;
		 
		 @Override
		 public int compare(final Spatial opponent0,final Spatial opponent1){
			 final double distance0=opponent0.getWorldTranslation().distance(projectileData.getInitialLocation());
			 final double distance1=opponent1.getWorldTranslation().distance(projectileData.getInitialLocation());
			 final int compareFactor=distance0==distance1?0:distance0<distance1?-1:1;
		     return(compareFactor);
		 }
		 
		 public void setProjectileData(final ProjectileData projectileData){
			 this.projectileData=projectileData;
		 }
	 }
    
    /**
     * logical entity allowing to manipulate a player used as a link between
     * the state machine and the player data
     * 
     * @author Julien Gouesse
     *
     */
    public static final class LogicalPlayer{
    	
    	private final PlayerStateMachine stateMachine;
    	
    	private final PlayerData playerData;
    	
    	private double elapsedTimeSinceLatestTransitionInSeconds=0;
    	
    	private double initialLatestPutBackProgress=0,initialEndAttackProgress=0;
    	
    	public LogicalPlayer(final PlayerData playerData){
    		this.playerData=playerData;
    		this.stateMachine=new PlayerStateMachine(playerData);
    	}
    	
    	public void updateLogicalLayer(final ReadOnlyTimer timer){
    		//gets the previous state of the player
            final PlayerState previousPlayerState=getPreviousState();
            //updates its state
    		stateMachine.updateLogicalLayer(timer);
    		//gets its current state (as is after the update)
            final PlayerState currentPlayerState=getPreviousState();
            //updates the amount of time since the latest transition
            if(previousPlayerState!=currentPlayerState)
            	elapsedTimeSinceLatestTransitionInSeconds=0;
            else
            	elapsedTimeSinceLatestTransitionInSeconds+=timer.getTimePerFrame();
            //updates the player data from its state machine and the elapsed time since the latest transition
            switch(currentPlayerState)
                {case NOT_YET_AVAILABLE:
                     {//there is nothing to do
                      break;
                     }
                 case IDLE:
                     {
                      break;
                     }
                 case PRESS_TRIGGER:
                     {playerData.pressTrigger(elapsedTimeSinceLatestTransitionInSeconds);
                      break;
                     }
                 case ATTACK:
                     {playerData.attack(elapsedTimeSinceLatestTransitionInSeconds);
                      break;
                     }
                 case WAIT_FOR_ATTACK_END:
                     {if(elapsedTimeSinceLatestTransitionInSeconds==0)
                    	 initialEndAttackProgress=playerData.computeEndAttackProgress();
                      playerData.waitForAttackEnd(elapsedTimeSinceLatestTransitionInSeconds,initialEndAttackProgress);                      
                     }
                 case RELEASE_TRIGGER:
                     {playerData.releaseTrigger(elapsedTimeSinceLatestTransitionInSeconds);
                      break;
                     }
                 case WAIT_FOR_TRIGGER_RELEASE:
                     {playerData.waitForTriggerRelease(elapsedTimeSinceLatestTransitionInSeconds);
                      break;
                     }
                 case RELOAD:
                     {
                      break;
                     }
                 case PULL_OUT:
                     {playerData.pullOut(elapsedTimeSinceLatestTransitionInSeconds);
                      break;
                     }
                 case PUT_BACK:
                     {if(elapsedTimeSinceLatestTransitionInSeconds==0)
                    	  initialLatestPutBackProgress=playerData.computePutBackProgress();
                      playerData.putBack(elapsedTimeSinceLatestTransitionInSeconds,initialLatestPutBackProgress);
                      break;
                     }
                 case SELECT_NEXT:
                     {
                      break;
                     }
                 case SELECT_PREVIOUS:
                     {
                      break;
                     }
                 default:
                      //it should never happen
                }
    	}
    	
    	public void tryReload(){
    		if(playerData.isAlive())
    		    stateMachine.fireEvent(PlayerEvent.PUTTING_BACK_BEFORE_RELOADING);
    	}
    	
    	public void trySelectNextWeapon(){
    		if(playerData.isAlive())
    		    stateMachine.fireEvent(PlayerEvent.PUTTING_BACK_BEFORE_SELECTING_NEXT);
    	}
    	
        public void trySelectPreviousWeapon(){
        	if(playerData.isAlive())
        	    stateMachine.fireEvent(PlayerEvent.PUTTING_BACK_BEFORE_SELECTING_PREVIOUS);
    	}
        
        public PlayerState getPreviousState(){
        	return(stateMachine.previousState);
        }
        
        public void tryStartAttacking(){
        	if(playerData.isAlive())
        	    stateMachine.fireEvent(PlayerEvent.PRESSING_TRIGGER);
        }
        
        public void tryStopAttacking(){
        	stateMachine.fireEvent(PlayerEvent.RELEASING_TRIGGER);
        }
    }
    
    private final void initializeInput(final TransitionTriggerAction<ScenegraphState,String> toPauseMenuTriggerAction,
    		                           final TransitionTriggerAction<ScenegraphState,String> toPauseMenuTriggerActionForExitConfirm,
    		                           final TriggerAction toggleScreenModeAction,final Camera cam,final PhysicalLayer physicalLayer){
    	//deregisters all triggers
        if(!getLogicalLayer().getTriggers().isEmpty())
            {final Set<InputTrigger> triggers=new HashSet<>(getLogicalLayer().getTriggers());
        	 for(InputTrigger trigger:triggers)
        		 getLogicalLayer().deregisterTrigger(trigger);
            }
    	final Vector3 worldUp=new Vector3(0,1,0);
        //sets "drag only" to false to remove the need of pressing a button to move
        fpsc=ExtendedFirstPersonControl.setupTriggers(getLogicalLayer(),worldUp,false,customActionMap);
        //applies the mouse and keyboard settings
        fpsc.setKeyRotateSpeed(customMouseAndKeyboardSettings.getKeyRotateSpeed());
        fpsc.setLookUpDownReversed(customMouseAndKeyboardSettings.isLookUpDownReversed());
        fpsc.setMouseRotateSpeed(customMouseAndKeyboardSettings.getMouseRotateSpeed());
        fpsc.setMoveSpeed(customMouseAndKeyboardSettings.getMoveSpeed());
        final InputTrigger exitPromptTrigger=new InputTrigger(customActionMap.getCondition(Action.QUIT,false),new TriggerAction(){
        	@Override
			public void perform(Canvas source, TwoInputStates inputState, double tpf){
        		toPauseMenuTriggerActionForExitConfirm.perform(null,null,-1);
			}
		});
        final TriggerAction nextWeaponAction=new TriggerAction(){		
			@Override
			public void perform(Canvas source, TwoInputStates inputState, double tpf){
				playerWithStateMachine.trySelectNextWeapon();
			}
		};
		final TriggerAction previousWeaponAction=new TriggerAction(){		
			@Override
			public void perform(Canvas source, TwoInputStates inputState, double tpf){
				playerWithStateMachine.trySelectPreviousWeapon();
			}
		};
		final TriggerAction reloadWeaponAction=new TriggerAction(){
			@Override
			public void perform(Canvas source, TwoInputStates inputState, double tpf){
				playerWithStateMachine.tryReload();
			}
		};
		final TriggerAction startAttackAction=new TriggerAction(){
			@Override
			public void perform(Canvas source, TwoInputStates inputState, double tpf){
				playerWithStateMachine.tryStartAttacking();
			}
		};
		final TriggerAction stopAttackAction=new TriggerAction(){
			@Override
			public void perform(Canvas source, TwoInputStates inputState, double tpf){
				playerWithStateMachine.tryStopAttacking();
			}
		};
		/**
		 * TODO implement these actions when the state machine is ready to handle them
		 */
		final TriggerAction pauseAction=new TriggerAction(){
			@Override
			public void perform(Canvas source, TwoInputStates inputState, double tpf){
				//TODO: pause
				//TODO: pause the timer
				//timer.setPauseEnabled(true);
				//timer.update();
				toPauseMenuTriggerAction.perform(null,null,-1);
			}
		};
		final TriggerAction crouchAction=new TriggerAction(){
			@Override
			public void perform(Canvas source, TwoInputStates inputState, double tpf){
			}
		};
		final TriggerAction activateAction=new TriggerAction(){
			@Override
			public void perform(Canvas source, TwoInputStates inputState, double tpf){
			}
		};
		final TriggerAction startRunningAction=new TriggerAction(){
			@Override
			public void perform(Canvas source, TwoInputStates inputState, double tpf){
			}
		};
		final TriggerAction stopRunningAction=new TriggerAction(){
			@Override
			public void perform(Canvas source, TwoInputStates inputState, double tpf){
			}
		};
		final TriggerAction toggleWireframeModeAction=new TriggerAction() {
            @Override
			public void perform(final Canvas source, final TwoInputStates inputState, final double tpf) {
                wireframeState.setEnabled(!wireframeState.isEnabled());
                getRoot().markDirty(DirtyType.RenderState);
            }
        };
		/*final TriggerAction selectWeaponOneAction=new TriggerAction(){
			@Override
			public void perform(Canvas source, TwoInputStates inputState, double tpf){
				//playerData.selectWeapon(0,false);
			}
		};*/
        //add some triggers to change weapon, reload and shoot
		final InputTrigger nextWeaponTrigger=new InputTrigger(customActionMap.getCondition(Action.NEXT_WEAPON,false),nextWeaponAction);
		final InputTrigger previousWeaponTrigger=new InputTrigger(customActionMap.getCondition(Action.PREVIOUS_WEAPON,false),previousWeaponAction);
        final InputTrigger reloadWeaponTrigger=new InputTrigger(customActionMap.getCondition(Action.RELOAD,false),reloadWeaponAction);
        final InputTrigger startAttackTrigger=new InputTrigger(customActionMap.getCondition(Action.ATTACK,true),startAttackAction);
        final InputTrigger stopAttackTrigger=new InputTrigger(customActionMap.getCondition(Action.ATTACK,false),stopAttackAction);
        final InputTrigger pauseTrigger=new InputTrigger(customActionMap.getCondition(Action.PAUSE,false),pauseAction);
        final InputTrigger crouchTrigger=new InputTrigger(customActionMap.getCondition(Action.CROUCH,false),crouchAction);
        final InputTrigger activateTrigger=new InputTrigger(customActionMap.getCondition(Action.ACTIVATE,false),activateAction);
        final InputTrigger startRunningRightTrigger=new InputTrigger(customActionMap.getCondition(Action.RUN,true),startRunningAction);
        final InputTrigger stopRunningRightTrigger=new InputTrigger(customActionMap.getCondition(Action.RUN,false),stopRunningAction);
        final InputTrigger toggleWireframeModeTrigger=new InputTrigger(customActionMap.getCondition(Action.TOGGLE_WIREFRAME_MODE,false),toggleWireframeModeAction);
        final InputTrigger[] triggers=new InputTrigger[]{exitPromptTrigger,
        		nextWeaponTrigger,previousWeaponTrigger,reloadWeaponTrigger,
        		startAttackTrigger,pauseTrigger,crouchTrigger,
        		activateTrigger,startRunningRightTrigger,stopRunningRightTrigger,
        		stopAttackTrigger,toggleWireframeModeTrigger};
        getLogicalLayer().registerInput(canvas,physicalLayer);
        for(InputTrigger trigger:triggers)
            getLogicalLayer().registerTrigger(trigger);
    }
    
    private final BasicText initializeAmmunitionTextLabel(){
    	final BasicText ammoTextLabel=BasicText.createDefaultTextLabel("ammo display","");
    	ammoTextLabel.setTranslation(new Vector3(0,80,0));
        ammoTextLabel.addController(new SpatialController<Spatial>(){
            @Override
            public final void update(double time,Spatial caller){
            	if(playerData.isCurrentWeaponAmmunitionCountDisplayable())
            		{final StringBuffer text=new StringBuffer("AMMO: ");
            		 if(playerData.isDualWeaponUseEnabled())
            			 {text.append(playerData.getAmmunitionCountInSecondaryHandedWeapon());
            			  text.append(" ");
            			 }
            		 text.append(playerData.getAmmunitionCountInPrimaryHandedWeapon());
            		 text.append(" ");
            		 text.append(playerData.getAmmunitionCountInContainer());
            		 ammoTextLabel.setText(text.toString());
            		}
            	else
            		ammoTextLabel.setText("N/A");
            }           
        });
        return(ammoTextLabel);
    }
    
    private final BasicText initializeFpsTextLabel(){
    	final BasicText fpsTextLabel=BasicText.createDefaultTextLabel("FPS display","");
    	fpsTextLabel.setTranslation(new Vector3(0,20,0));
        fpsTextLabel.addController(new SpatialController<Spatial>(){
        	
        	private double period;
        	
        	private int frameCount;
        	
            @Override
            public final void update(double timePerFrame,Spatial caller){
            	if(period>1)
            	    {final int framesPerSecond=(int)Math.round(frameCount>0&&period>0?frameCount/period:0);
            	     fpsTextLabel.setText(" "+ framesPerSecond +" FPS");
            		 period=timePerFrame;
            		 frameCount=1;
            	    }
            	else
            		{period+=timePerFrame;
            		 frameCount++;
            		}
            }           
        });
    	return(fpsTextLabel);
    }
    
    private final BasicText initializeHealthTextLabel(){
    	final BasicText healthTextLabel=BasicText.createDefaultTextLabel("health display","");
    	healthTextLabel.setTranslation(new Vector3(0,60,0));
        healthTextLabel.addController(new SpatialController<Spatial>(){
            @Override
            public final void update(double time,Spatial caller){
            	healthTextLabel.setText("HEALTH: "+playerData.getHealth());
            }           
        });
        return(healthTextLabel);
    }
    
    private final BasicText initializeHeadUpDisplayLabel(){    	       
    	final BasicText headUpDisplayLabel=BasicText.createDefaultTextLabel("Head-up display","");           
        headUpDisplayLabel.setTranslation(new Vector3(0,40,0));
        headUpDisplayLabel.addController(new SpatialController<Spatial>(){
        	
        	private String latestText="";
        	
        	private double duration=0;
        	
            @Override
            public final void update(double time,Spatial caller){
            	//if the HUD label contains anything
            	if(!headUpDisplayLabel.getText().isEmpty())
            	    {//if it contains the same text
            		 if(latestText.equals(headUpDisplayLabel.getText()))
                		 {//increase the display time
            			  duration+=time;
            			  //if it has been displayed for a too long time
                	      if(duration>3)
                	          {//remove it
                	    	   headUpDisplayLabel.setText("");
                	           duration=0;
                	          }
                		 }
            	     else
            	    	 {//otherwise update the latest text
            	    	  latestText=headUpDisplayLabel.getText();
            	    	  duration=0;
            	    	 }
            	    }
            }           
        });
        return(headUpDisplayLabel);
    }
    
    private final Teleporter initializeTeleporter(){
    	final Teleporter teleporter=new Teleporter("/sounds/teleporter_use.ogg");
    	return(teleporter);
    }

    private final Medikit initializeMedikit(){
    	//TODO set the path of the sound sample
    	final Medikit medikit=new Medikit(null,20);
    	return(medikit);
    }
    
    private final AmmunitionFactory initializeAmmunitionFactory(){
    	final AmmunitionFactory ammunitionFactory=new AmmunitionFactory();
        /**American assault rifle*/
        ammunitionFactory.addNewAmmunition("/sounds/pickup_weapon.ogg","BULLET_5_56MM","5.56mm bullet");
    	/**Russian assault rifle*/
        ammunitionFactory.addNewAmmunition("/sounds/pickup_weapon.ogg","BULLET_7_62MM","7.62mm bullet");
    	/**American pistols and sub-machine guns*/
        ammunitionFactory.addNewAmmunition("/sounds/pickup_weapon.ogg","BULLET_9MM","9mm bullet");
    	/**Russian pistols*/
        ammunitionFactory.addNewAmmunition("/sounds/pickup_weapon.ogg","BULLET_10MM","10mm bullet");
    	/**cartridge*/
        ammunitionFactory.addNewAmmunition("/sounds/pickup_weapon.ogg","CARTRIDGE","cartridge");
    	/**power*/
        ammunitionFactory.addNewAmmunition("/sounds/pickup_weapon.ogg","ENERGY_CELL","energy cell");
    	/**Russian middle range anti-tank rocket launchers*/
        ammunitionFactory.addNewAmmunition("/sounds/pickup_weapon.ogg","ANTI_TANK_ROCKET_105MM","105mm anti tank rocket");
        return(ammunitionFactory);
    }
    
    private final WeaponFactory initializeWeaponFactory(){
    	final WeaponFactory weaponFactory=new WeaponFactory();                       
        weaponFactory.addNewWeapon("/sounds/pickup_weapon.ogg","/sounds/pistol9mn_shot.ogg","/sounds/pistol9mn_reload.ogg","PISTOL_9MM",true,8,ammunitionFactory.get("BULLET_9MM"),1,500,true);
        weaponFactory.addNewWeapon("/sounds/pickup_weapon.ogg",null,null,"PISTOL_10MM",true,10,ammunitionFactory.get("BULLET_10MM"),1,500,true);
        weaponFactory.addNewWeapon("/sounds/pickup_weapon.ogg","/sounds/mag60_shot.ogg","/sounds/mag60_reload.ogg","MAG_60",true,30,ammunitionFactory.get("BULLET_9MM"),1,100,true);
        weaponFactory.addNewWeapon("/sounds/pickup_weapon.ogg",null,null,"UZI",true,20,ammunitionFactory.get("BULLET_9MM"),1,100,true);
        weaponFactory.addNewWeapon("/sounds/pickup_weapon.ogg",null,null,"SMACH",true,35,ammunitionFactory.get("BULLET_5_56MM"),1,100,true);
        weaponFactory.addNewWeapon("/sounds/pickup_weapon.ogg",null,null,"LASER",true,15,ammunitionFactory.get("ENERGY_CELL"),1,1000,false);
        weaponFactory.addNewWeapon("/sounds/pickup_weapon.ogg",null,null,"SHOTGUN",false,3,ammunitionFactory.get("CARTRIDGE"),1,1500,false);
        weaponFactory.addNewWeapon("/sounds/pickup_weapon.ogg",null,null,"ROCKET_LAUNCHER",false,1,ammunitionFactory.get("ANTI_TANK_ROCKET_105MM"),1,2000,false);
        return(weaponFactory);
    }
    
    @Deprecated
    private final void readCollisionMap(){
    	final URL mapUrl=GameState.class.getResource("/images/containermap.png");
    	final URLResourceSource mapSource=new URLResourceSource(mapUrl);
    	final Image map=ImageLoaderUtil.loadImage(mapSource,false);
    	collisionMap=new boolean[map.getWidth()][map.getHeight()];
    	final ImageHelper imgHelper=new ImageHelper();
    	for(int y=0;y<map.getHeight();y++)
	    	for(int x=0;x<map.getWidth();x++)
	    		{final int argb=imgHelper.getARGB(map,x,y);
	    		 collisionMap[x][y]=(argb==ColorRGBA.BLUE.asIntARGB());
	    		}
    }
    
    protected void setLevelIndex(final int levelIndex){
        this.levelIndex=levelIndex;
    }
    
    public int getLevelIndex(){
    	return(levelIndex);
    }
    
    /**
     * loads the sound samples
     */
    private final void loadSounds(){
        final String teleporterSoundSamplePath=teleporter.getPickingUpSoundSamplePath();
  	    if(teleporterSoundSamplePath!=null)
  	        {final URL teleporterSoundSampleUrl=GameState.class.getResource(teleporterSoundSamplePath);
  		     if(teleporterSoundSampleUrl!=null)
  		         {final String teleporterSoundSampleIdentifier=getSoundManager().loadSound(teleporterSoundSampleUrl);
  		          if(teleporterSoundSampleIdentifier!=null)
  			     	  teleporter.setPickingUpSoundSampleIdentifier(teleporterSoundSampleIdentifier);
  		         }
  	        }
        final int ammoCount=ammunitionFactory.getSize();
        for(int ammoIndex=0;ammoIndex<ammoCount;ammoIndex++)
            {final Ammunition ammo=ammunitionFactory.get(ammoIndex);
             final String pickingUpSoundSamplePath=ammo.getPickingUpSoundSamplePath();
       	     if(pickingUpSoundSamplePath!=null)
       	         {final URL pickingUpSoundSampleUrl=GameState.class.getResource(pickingUpSoundSamplePath);
       		      if(pickingUpSoundSampleUrl!=null)
       		          {final String pickingUpSoundSampleIdentifier=getSoundManager().loadSound(pickingUpSoundSampleUrl);
       			       if(pickingUpSoundSampleIdentifier!=null)
       				       ammo.setPickingUpSoundSampleIdentifier(pickingUpSoundSampleIdentifier);
       		          }
       	         }
            }
        final int weaponCount=weaponFactory.getSize();
        for(int weaponIndex=0;weaponIndex<weaponCount;weaponIndex++)
            {final Weapon weapon=weaponFactory.get(weaponIndex);
        	 final String pickingUpSoundSamplePath=weapon.getPickingUpSoundSamplePath();
        	 if(pickingUpSoundSamplePath!=null)
        	     {final URL pickingUpSoundSampleUrl=GameState.class.getResource(pickingUpSoundSamplePath);
        		  if(pickingUpSoundSampleUrl!=null)
        		      {final String pickingUpSoundSampleSourcename=getSoundManager().loadSound(pickingUpSoundSampleUrl);
        			   if(pickingUpSoundSampleSourcename!=null)
        				   weapon.setPickingUpSoundSampleIdentifier(pickingUpSoundSampleSourcename);
        		      }
        	     }
        	 final String blowOrShotSoundSamplePath=weapon.getBlowOrShotSoundSamplePath();
        	 if(blowOrShotSoundSamplePath!=null)
        	     {final URL blowOrShotSoundSampleUrl=GameState.class.getResource(blowOrShotSoundSamplePath);
        		  if(blowOrShotSoundSampleUrl!=null)
        		      {final String blowOrShotSoundSampleIdentifier=getSoundManager().loadSound(blowOrShotSoundSampleUrl);
        			   if(blowOrShotSoundSampleIdentifier!=null)
        				   weapon.setBlowOrShotSoundSampleIdentifier(blowOrShotSoundSampleIdentifier);
        		      }
        	     }
        	 final String reloadSoundSamplePath=weapon.getReloadSoundSamplePath();
        	 if(reloadSoundSamplePath!=null)
        	     {final URL reloadSoundSampleUrl=GameState.class.getResource(reloadSoundSamplePath);
        		  if(reloadSoundSampleUrl!=null)
        		      {final String reloadSoundSampleIdentifier=getSoundManager().loadSound(reloadSoundSampleUrl);
        			   if(reloadSoundSampleIdentifier!=null)
        				   weapon.setReloadSoundSampleIdentifier(reloadSoundSampleIdentifier);
        		      }
        	     }
            }
        if(pain1soundSamplePath!=null&&pain1soundSampleIdentifier==null)
            {final URL pain1SoundSampleUrl=GameState.class.getResource(pain1soundSamplePath);
        	 if(pain1SoundSampleUrl!=null)
        	     pain1soundSampleIdentifier=getSoundManager().loadSound(pain1SoundSampleUrl);
            }
        if(pain2soundSamplePath!=null&&pain2soundSampleIdentifier==null)
            {final URL pain1SoundSampleUrl=GameState.class.getResource(pain2soundSamplePath);
    	     if(pain1SoundSampleUrl!=null)
    	         pain2soundSampleIdentifier=getSoundManager().loadSound(pain1SoundSampleUrl);
            }
        if(pain3soundSamplePath!=null&&pain3soundSampleIdentifier==null)
            {final URL pain1SoundSampleUrl=GameState.class.getResource(pain3soundSamplePath);
    	     if(pain1SoundSampleUrl!=null)
    	         pain3soundSampleIdentifier=getSoundManager().loadSound(pain1SoundSampleUrl);
            }
        if(pain4soundSamplePath!=null&&pain4soundSampleIdentifier==null)
            {final URL pain1SoundSampleUrl=GameState.class.getResource(pain4soundSamplePath);
    	     if(pain1SoundSampleUrl!=null)
    	         pain4soundSampleIdentifier=getSoundManager().loadSound(pain1SoundSampleUrl);
            }
        if(pain5soundSamplePath!=null&&pain5soundSampleIdentifier==null)
            {final URL pain1SoundSampleUrl=GameState.class.getResource(pain5soundSamplePath);
	         if(pain1SoundSampleUrl!=null)
	             pain5soundSampleIdentifier=getSoundManager().loadSound(pain1SoundSampleUrl);
            }
        if(pain6soundSamplePath!=null&&pain6soundSampleIdentifier==null)
            {final URL pain1SoundSampleUrl=GameState.class.getResource(pain6soundSamplePath);
	         if(pain1SoundSampleUrl!=null)
	             pain6soundSampleIdentifier=getSoundManager().loadSound(pain1SoundSampleUrl);
            }
        if(enemyShotgunShotSamplePath!=null&&enemyShotgunShotSampleIdentifier==null)
            {final URL enemyShotgunShotSampleUrl=GameState.class.getResource(enemyShotgunShotSamplePath);
        	 if(enemyShotgunShotSampleUrl!=null)
        		 enemyShotgunShotSampleIdentifier=getSoundManager().loadSound(enemyShotgunShotSampleUrl);
            }
        if(gameoverSoundSamplePath!=null&&gameoverSoundSampleIdentifier==null)
            {final URL gameoverSoundSampleUrl=GameState.class.getResource(gameoverSoundSamplePath);
        	 if(gameoverSoundSampleUrl!=null)
        		 gameoverSoundSampleIdentifier=getSoundManager().loadSound(gameoverSoundSampleUrl);
            }
    }
    
    /**
     * unloads the sound samples
     */
    private final void unloadSounds(){
        final String teleporterSoundSamplePath=teleporter.getPickingUpSoundSamplePath();
  	    if(teleporterSoundSamplePath!=null)
  	        {final URL teleporterSoundSampleUrl=GameState.class.getResource(teleporterSoundSamplePath);
  		     if(teleporterSoundSampleUrl!=null)
  		         {getSoundManager().unloadSound(teleporterSoundSampleUrl);
  		          if(teleporter.getPickingUpSoundSampleIdentifier()!=null)
  			     	  teleporter.setPickingUpSoundSampleIdentifier(null);
  		         }
  	        }
        final int ammoCount=ammunitionFactory.getSize();
        for(int ammoIndex=0;ammoIndex<ammoCount;ammoIndex++)
            {final Ammunition ammo=ammunitionFactory.get(ammoIndex);
             final String pickingUpSoundSamplePath=ammo.getPickingUpSoundSamplePath();
       	     if(pickingUpSoundSamplePath!=null)
       	         {final URL pickingUpSoundSampleUrl=GameState.class.getResource(pickingUpSoundSamplePath);
       		      if(pickingUpSoundSampleUrl!=null)
       		          {getSoundManager().unloadSound(pickingUpSoundSampleUrl);
       			       if(ammo.getPickingUpSoundSampleIdentifier()!=null)
       				       ammo.setPickingUpSoundSampleIdentifier(null);
       		          }
       	         }
            }
        final int weaponCount=weaponFactory.getSize();
        for(int weaponIndex=0;weaponIndex<weaponCount;weaponIndex++)
            {final Weapon weapon=weaponFactory.get(weaponIndex);
        	 final String pickingUpSoundSamplePath=weapon.getPickingUpSoundSamplePath();
        	 if(pickingUpSoundSamplePath!=null)
        	     {final URL pickingUpSoundSampleUrl=GameState.class.getResource(pickingUpSoundSamplePath);
        		  if(pickingUpSoundSampleUrl!=null)
        		      {getSoundManager().unloadSound(pickingUpSoundSampleUrl);
        			   if(weapon.getPickingUpSoundSampleIdentifier()!=null)
        				   weapon.setPickingUpSoundSampleIdentifier(null);
        		      }
        	     }
        	 final String blowOrShotSoundSamplePath=weapon.getBlowOrShotSoundSamplePath();
        	 if(blowOrShotSoundSamplePath!=null)
        	     {final URL blowOrShotSoundSampleUrl=GameState.class.getResource(blowOrShotSoundSamplePath);
        		  if(blowOrShotSoundSampleUrl!=null)
        		      {getSoundManager().unloadSound(blowOrShotSoundSampleUrl);
        			   if(weapon.getBlowOrShotSoundSampleIdentifier()!=null)
        				   weapon.setBlowOrShotSoundSampleIdentifier(null);
        		      }
        	     }
        	 final String reloadSoundSamplePath=weapon.getReloadSoundSamplePath();
        	 if(reloadSoundSamplePath!=null)
        	     {final URL reloadSoundSampleUrl=GameState.class.getResource(reloadSoundSamplePath);
        		  if(reloadSoundSampleUrl!=null)
        		      {getSoundManager().unloadSound(reloadSoundSampleUrl);
        			   if(weapon.getReloadSoundSampleIdentifier()!=null)
        				   weapon.setReloadSoundSampleIdentifier(null);
        		      }
        	     }
            }
        if(pain1soundSamplePath!=null&&pain1soundSampleIdentifier!=null)
            {final URL pain1SoundSampleUrl=GameState.class.getResource(pain1soundSamplePath);
        	 if(pain1SoundSampleUrl!=null)
        	     getSoundManager().unloadSound(pain1SoundSampleUrl);
        	 pain1soundSampleIdentifier=null;
            }
        if(pain2soundSamplePath!=null&&pain2soundSampleIdentifier!=null)
            {final URL pain1SoundSampleUrl=GameState.class.getResource(pain2soundSamplePath);
    	     if(pain1SoundSampleUrl!=null)
    	         getSoundManager().unloadSound(pain1SoundSampleUrl);
    	     pain2soundSampleIdentifier=null;
            }
        if(pain3soundSamplePath!=null&&pain3soundSampleIdentifier!=null)
            {final URL pain1SoundSampleUrl=GameState.class.getResource(pain3soundSamplePath);
    	     if(pain1SoundSampleUrl!=null)
    	         getSoundManager().unloadSound(pain1SoundSampleUrl);
    	     pain3soundSampleIdentifier=null;
            }
        if(pain4soundSamplePath!=null&&pain4soundSampleIdentifier!=null)
            {final URL pain1SoundSampleUrl=GameState.class.getResource(pain4soundSamplePath);
    	     if(pain1SoundSampleUrl!=null)
    	         getSoundManager().unloadSound(pain1SoundSampleUrl);
    	     pain4soundSampleIdentifier=null;
            }
        if(pain5soundSamplePath!=null&&pain5soundSampleIdentifier!=null)
            {final URL pain1SoundSampleUrl=GameState.class.getResource(pain5soundSamplePath);
	         if(pain1SoundSampleUrl!=null)
	             getSoundManager().unloadSound(pain1SoundSampleUrl);
	         pain5soundSampleIdentifier=null;
            }
        if(pain6soundSamplePath!=null&&pain6soundSampleIdentifier!=null)
            {final URL pain1SoundSampleUrl=GameState.class.getResource(pain6soundSamplePath);
	         if(pain1SoundSampleUrl!=null)
	             getSoundManager().unloadSound(pain1SoundSampleUrl);
	         pain6soundSampleIdentifier=null;
            }
        if(enemyShotgunShotSamplePath!=null&&enemyShotgunShotSampleIdentifier!=null)
            {final URL enemyShotgunShotSampleUrl=GameState.class.getResource(enemyShotgunShotSamplePath);
        	 if(enemyShotgunShotSampleUrl!=null)
        		 getSoundManager().unloadSound(enemyShotgunShotSampleUrl);
        	 enemyShotgunShotSampleIdentifier=null;
            }
        if(gameoverSoundSamplePath!=null&&gameoverSoundSampleIdentifier!=null)
            {final URL gameoverSoundSampleUrl=GameState.class.getResource(gameoverSoundSamplePath);
        	 if(gameoverSoundSampleUrl!=null)
        		 getSoundManager().unloadSound(gameoverSoundSampleUrl);
        	 gameoverSoundSampleIdentifier=null;
            }
    }
    
    private final void loadLevelModel(){
    	try{levelNode=(Node)binaryImporter.load(getClass().getResource("/abin/LID"+levelIndex+".abin"));
            getRoot().attachChild(levelNode);
    	   }
    	catch(IOException ioe)
    	{throw new RuntimeException("level loading failed",ioe);}
    }
    
    private final void loadOutdoor(){
    	//TODO load separately the outdoor if any
    }
    
    private final void performInitialBasicSetup(){
        //FIXME it should not be hard-coded
    	switch(levelIndex)
    	{case 0:
    	 case 1:
    	     {currentCamLeft.set(-1,0,0);
    		  currentCamUp.set(0,1,0);
    		  currentCamDirection.set(0,0,-1);
    	      currentCamLocation.set(115,0.5,223);
    	      break;
    	     }
    	 case 2:
    	     {currentCamLeft.set(1,0,0);
   		      currentCamUp.set(0,1,0);
   		      currentCamDirection.set(0,0,1);
    	      currentCamLocation.set(0,0,0);
    	      break;
    	     }
    	 case 3:
    	     {currentCamLeft.set(1,0,0);
  		      currentCamUp.set(0,1,0);
  		      currentCamDirection.set(0,0,1);
    	      currentCamLocation.set(0,0,5);
    	      break;
    	     }
    	}
        currentFrustumNear=0.1;
        currentFrustumFar=200;
        //attaches the player itself
        getRoot().attachChild(playerNode);
        //attaches the ammunition display node
        getRoot().attachChild(ammoTextLabel);
        //attaches the FPS display node
        getRoot().attachChild(fpsTextLabel);
        //attaches the health display node
        getRoot().attachChild(healthTextLabel);
        //attaches the HUD node
        getRoot().attachChild(headUpDisplayLabel);
        //resets the latest player's death
        latestPlayerDeath=null;
        //resets player's stats
        gameStats=new GameStatistics();
        //sets the statistics of each action
		((GameStatistics[])toPauseMenuTriggerAction.arguments.getArgument(2))[0]=gameStats;
		((GameStatistics[])toPauseMenuTriggerActionForExitConfirm.arguments.getArgument(2))[0]=gameStats;
		((GameStatistics[])toGameOverTriggerAction.arguments.getArgument(1))[0]=gameStats;
		((int[])toPauseMenuTriggerAction.arguments.getFirst())[0]=levelIndex;
		((int[])toPauseMenuTriggerActionForExitConfirm.arguments.getFirst())[0]=levelIndex;
		//the player cannot go to the next level when leaving or aborting
		((int[])toPauseMenuTriggerAction.arguments.getFirst())[1]=-1;
		((int[])toPauseMenuTriggerActionForExitConfirm.arguments.getFirst())[1]=-1;
        //resurrects the player
        playerData.respawn();
        //TODO resets the parameters to the latest saved values
    }
    
    private final void performTerminalBasicCleanup(){
    	if(levelNode!=null)
    	    {levelNode.detachAllChildren();
    		 levelNode=null;
    	    }
    	if(skyboxNode!=null)
    	    {skyboxNode.detachAllChildren();
    		 skyboxNode=null;
    	    }
    	//clears the list of objects that can be picked up
    	collectibleObjectsList.clear();
    	//clears the list of teleporters
    	teleportersList.clear();
    	//clears the data model used for the enemies
    	enemiesDataMap.clear();
    	enemiesLatestDetection.clear();
        //removes all previously attached children
        getRoot().detachAllChildren();
    }
    
    private final void performTerminalBasicSetup(){
    	//adds a bounding box to each collectible object
        for(Node collectible:collectibleObjectsList)
       	    NodeHelper.setModelBound(collectible,BoundingBox.class);
        for(Node currentTeleporter:teleportersList)
       	    NodeHelper.setModelBound(currentTeleporter,BoundingBox.class);
        //resets the timer at the end of all long operations performed while loading
        timer.reset();
        if(skyboxNode!=null)
        	skyboxNode.setTranslation(currentCamLocation);
    }
    
    private final void performInitialBasicCleanup(){
    	//detaches the player itself
        getRoot().detachChild(playerNode);
        //detaches the ammunition display node
        getRoot().detachChild(ammoTextLabel);
        //detaches the FPS display node
        getRoot().detachChild(fpsTextLabel);
        //detaches the health display node
        getRoot().detachChild(healthTextLabel);
        //detaches the HUD node
        getRoot().detachChild(headUpDisplayLabel);
    	//resets the timer at the beginning of all long operations performed while unloading
        timer.reset();
        //detaches some nodes from the root to prevent Java from using them while releasing their resources
        if(levelNode!=null)
        	getRoot().detachChild(levelNode);
        if(skyboxNode!=null)
            getRoot().detachChild(skyboxNode);
    }
    
    private static final class VBODeleterVisitor implements Visitor{
    	
    	private final Renderer renderer;
    	
    	private VBODeleterVisitor(final Renderer renderer){
    		super();
    		this.renderer=renderer;
    	}
    	
    	@Override
    	public void visit(final Spatial spatial){
    		if(spatial instanceof Mesh)
    			deleteVBOs((Mesh)spatial);
    	}
    	
    	private final void deleteVBOs(final Mesh disposableMesh){
            final MeshData meshData=disposableMesh.getMeshData();
    	    if(meshData!=null)
    	        {//deletes the OpenGL identifier of the VBOs and releases the native memory of their direct NIO buffers
    	         final FloatBufferData vertexBufferData=meshData.getVertexCoords();
    	         if(vertexBufferData!=null)
    	      	     {renderer.deleteVBOs(vertexBufferData);
    	              meshData.setVertexCoords(null);
    	             }
    	         final FloatBufferData colorBufferData=meshData.getColorCoords();
    	         if(colorBufferData!=null)
    	             {renderer.deleteVBOs(colorBufferData);
    	              meshData.setColorCoords(null);
    	             }
    	         final IndexBufferData<?> indexBufferData=meshData.getIndices();
    	         if(indexBufferData!=null)
    	             {renderer.deleteVBOs(indexBufferData);
    	              meshData.setIndices(null);
    	             }
    	         final FloatBufferData fogBufferData=meshData.getFogCoords();
    	         if(fogBufferData!=null)
	                 {renderer.deleteVBOs(fogBufferData);
	                  meshData.setFogCoords(null);
	                 }
    	         final FloatBufferData intervealedBufferData=meshData.getInterleavedData();
    	         if(intervealedBufferData!=null)
                     {renderer.deleteVBOs(intervealedBufferData);
            	      meshData.setInterleavedData(null);
            	     }
    	         final FloatBufferData normalBufferData=meshData.getNormalCoords();
    	         if(normalBufferData!=null)
        	         {renderer.deleteVBOs(normalBufferData);
        	          meshData.setNormalCoords(null);
        	         }
    	         final FloatBufferData tangentBufferData=meshData.getTangentCoords();
    	         if(tangentBufferData!=null)
    	             {renderer.deleteVBOs(tangentBufferData);
    	              meshData.setTangentCoords(null);
    	             }
    	         final List<FloatBufferData> textureCoordsList=meshData.getTextureCoords();
    	         if(textureCoordsList!=null&&!textureCoordsList.isEmpty())
    	             {for(FloatBufferData textureCoords:textureCoordsList)
    	                  if(textureCoords!=null)
    	                      renderer.deleteVBOs(textureCoords);
    	              textureCoordsList.clear();
    	             }
    	         disposableMesh.setMeshData(null);
    	        }
        }
    }
    
    /**
     * unloads direct NIO buffers
     */
    private final void performDirectNioBuffersCleanup(){
    	//stores the spatials whose direct NIO buffers need to be disposed
    	final HashSet<Spatial> disposableSpatials=new HashSet<>();
    	//gets the renderer
    	final Renderer renderer=canvas.getCanvasRenderer().getRenderer();
    	//TODO destroy the morph meshes and the template meshes of enemies
    	//TODO use templates to create weapons and do the same than above with them (get them from the list of collectible objects and from the camera node)
    	if(levelNode!=null)
	        disposableSpatials.add(levelNode);
    	if(skyboxNode!=null)
    	    disposableSpatials.add(skyboxNode);
    	//performs the destruction with a single callable
    	GameTaskQueueManager.getManager(canvas.getCanvasRenderer().getRenderContext()).getQueue(GameTaskQueue.RENDER).enqueue(new Callable<Void>(){
		      @Override
		      public Void call()throws Exception{
		    	  //builds the visitor
    	          final VBODeleterVisitor deleter=new VBODeleterVisitor(renderer);
    	          //runs it on all disposable spatials
    	          for(Spatial spatial:disposableSpatials)
    	        	  spatial.acceptVisitor(deleter,false);
    	          //clears the list of disposable spatials as it is now useless
    	          disposableSpatials.clear();
			      return null;
		      }
	    });
    }
    
    private static final class TextureDeleterVisitor implements Visitor{
    	
    	private final Renderer renderer;
    	
    	private TextureDeleterVisitor(final Renderer renderer){
    		super();
    		this.renderer=renderer;
    	}
    	
    	@Override
    	public void visit(final Spatial spatial){
    		deleteTextures(spatial);
    	}
    	
    	private void deleteTextures(final Spatial disposableSpatial){
    		final TextureState textureState=(TextureState)disposableSpatial.getLocalRenderState(StateType.Texture);
    		if(textureState!=null)
    		    {//loops on all texture units
    			 for(int textureUnit=0;textureUnit<textureState.getMaxTextureIndexUsed();textureUnit++)
    		         {final Texture texture=textureState.getTexture(textureUnit);
    		    	  if(texture!=null)
    		    	      {//deletes the OpenGL identifier of the texture and releases the native memory of its direct NIO buffer
    		    		   renderer.deleteTexture(texture);
    		    	      }
    		         }
    		     //removes the textures from the texture state
    			 textureState.clearTextures();
    			 //removes the texture state from this spatial
    			 disposableSpatial.clearRenderState(StateType.Texture);
    		    }
    	}
    }
    
    /**
     * unloads the textures
     */
    private final void performTexturesDataCleanup(){
    	//stores the spatials whose textures need to be disposed
    	final HashSet<Spatial> disposableSpatials=new HashSet<>();
    	//gets the renderer
    	final Renderer renderer=canvas.getCanvasRenderer().getRenderer();
    	if(levelNode!=null)
    		disposableSpatials.add(levelNode);
    	if(skyboxNode!=null)
	        disposableSpatials.add(skyboxNode);
    	//performs the destruction with a single callable
    	GameTaskQueueManager.getManager(canvas.getCanvasRenderer().getRenderContext()).getQueue(GameTaskQueue.RENDER).enqueue(new Callable<Void>(){
		      @Override
		      public Void call()throws Exception{
		    	  //builds the visitor
    	          final TextureDeleterVisitor deleter=new TextureDeleterVisitor(renderer);
    	          //runs it on all disposable spatials
    	          for(Spatial spatial:disposableSpatials)
    	        	  spatial.acceptVisitor(deleter,false);
    	          //clears the list of disposable spatials as it is now useless
    	          disposableSpatials.clear();
			      return null;
		      }
	    });
    }
    
    private final void loadSkybox(){
    	if(levelIndex==2||levelIndex==3)
    	    {skyboxNode=new Skybox("skybox",64,64,64);
    	     final Texture north=TextureManager.load(new URLResourceSource(getClass().getResource("/images/1.jpg")),Texture.MinificationFilter.BilinearNearestMipMap,true);
    	     final Texture south=TextureManager.load(new URLResourceSource(getClass().getResource("/images/3.jpg")),Texture.MinificationFilter.BilinearNearestMipMap,true);
    	     final Texture east=TextureManager.load(new URLResourceSource(getClass().getResource("/images/2.jpg")),Texture.MinificationFilter.BilinearNearestMipMap,true);
    	     final Texture west=TextureManager.load(new URLResourceSource(getClass().getResource("/images/4.jpg")),Texture.MinificationFilter.BilinearNearestMipMap,true);
    	     final Texture up=TextureManager.load(new URLResourceSource(getClass().getResource("/images/6.jpg")),Texture.MinificationFilter.BilinearNearestMipMap,true);
    	     final Texture down=TextureManager.load(new URLResourceSource(getClass().getResource("/images/5.jpg")),Texture.MinificationFilter.BilinearNearestMipMap,true);            
    	     skyboxNode.setTexture(Skybox.Face.North,north);
    	     skyboxNode.setTexture(Skybox.Face.West,west);
    	     skyboxNode.setTexture(Skybox.Face.South,south);
    	     skyboxNode.setTexture(Skybox.Face.East,east);
    	     skyboxNode.setTexture(Skybox.Face.Up,up);
    	     skyboxNode.setTexture(Skybox.Face.Down,down);
    	     getRoot().attachChild(skyboxNode);
    	    }
    }
    
    private final void loadTeleporters(){
    	/*if(levelIndex==0)
    	    {final Node teleporterNode=new Node("a teleporter");
             final Box teleporterBox=new Box("a teleporter",new Vector3(0,0,0),0.5,0.05,0.5);
             teleporterBox.setRandomColors();
             teleporterNode.setTranslation(112.5,0,221.5);
             teleporterNode.attachChild(teleporterBox);
             teleporterNode.setUserData(new TeleporterUserData(teleporter,new Vector3(-132,0.5,-102)));
             teleportersList.add(teleporterNode);
             getRoot().attachChild(teleporterNode);            
             final Node secondTeleporterNode=new Node("another teleporter");
             final Box secondTeleporterBox=new Box("another teleporter",new Vector3(0,0,0),0.5,0.05,0.5);
             secondTeleporterBox.setRandomColors();
             secondTeleporterNode.setTranslation(-132,0,-102);
             secondTeleporterNode.attachChild(secondTeleporterBox);
             secondTeleporterNode.setUserData(new TeleporterUserData(teleporter,new Vector3(112.5,0.5,221.5)));
             teleportersList.add(secondTeleporterNode);
             getRoot().attachChild(secondTeleporterNode);
    	    }*/
    }
    
    private final void loadMedikits(){
    	if(levelIndex==0||levelIndex==1)
    	    {final Node medikitNode=new Node("a medikit");
             final Box medikitBox=new Box("a medikit",new Vector3(0,0,0),0.1,0.1,0.1);
             final TextureState ts = new TextureState();
             ts.setTexture(TextureManager.load(new URLResourceSource(getClass().getResource("/images/medikit.png")),Texture.MinificationFilter.Trilinear,true));
             medikitBox.setRenderState(ts);
             medikitNode.setTranslation(112.5,0.1,220.5);
             medikitNode.attachChild(medikitBox);
             medikitNode.setUserData(new MedikitUserData(medikit));
             collectibleObjectsList.add(medikitNode);
             getRoot().attachChild(medikitNode);
    	    }
    }
    
    private final void loadWeapons(){
    	if(levelIndex==0||levelIndex==1)
    	//N.B: only show working weapons
	    try{/*final Node uziNode=(Node)binaryImporter.load(getClass().getResource("/abin/uzi.abin"));
            uziNode.setName("an uzi");
            uziNode.setTranslation(111.5,0.15,219);
            uziNode.setScale(0.2);
            uziNode.setUserData(new WeaponUserData(weaponFactory.getWeapon("UZI"),new Matrix3(uziNode.getRotation()),PlayerData.NO_UID,false,true));
            //adds some bounding boxes for all objects that can be picked up
            collectibleObjectsList.add(uziNode);
            getRoot().attachChild(uziNode);
            final Node smachNode=(Node)binaryImporter.load(getClass().getResource("/abin/smach.abin"));
            smachNode.setName("a smach");
            smachNode.setTranslation(112.5,0.15,219);
            smachNode.setScale(0.2);
            smachNode.setUserData(new WeaponUserData(weaponFactory.getWeapon("SMACH"),new Matrix3(smachNode.getRotation()),PlayerData.NO_UID,false,true));
            collectibleObjectsList.add(smachNode);
            getRoot().attachChild(smachNode);
            final Node pistolNode=(Node)binaryImporter.load(getClass().getResource("/abin/pistol.abin"));
            pistolNode.setName("a pistol (10mm)");
            pistolNode.setTranslation(113.5,0.1,219);
            pistolNode.setScale(0.001);
            pistolNode.setRotation(new Quaternion().fromEulerAngles(Math.PI/2,-Math.PI/4,Math.PI/2));
            pistolNode.setUserData(new WeaponUserData(weaponFactory.getWeapon("PISTOL_10MM"),new Matrix3(pistolNode.getRotation()),PlayerData.NO_UID,false,true));
            collectibleObjectsList.add(pistolNode);
            getRoot().attachChild(pistolNode);            
            final Node duplicatePistolNode=pistolNode.makeCopy(false);
            duplicatePistolNode.setUserData(new WeaponUserData(weaponFactory.getWeapon("PISTOL_10MM"),new Matrix3(pistolNode.getRotation()),PlayerData.NO_UID,false,false));
            duplicatePistolNode.setTranslation(113.5,0.1,217);
            collectibleObjectsList.add(duplicatePistolNode);
            getRoot().attachChild(duplicatePistolNode);*/
            final Node pistol2Node=(Node)binaryImporter.load(getClass().getResource("/abin/pistol2.abin"));
            pistol2Node.setName("a pistol (9mm)");
            //removes the bullet as it is not necessary now
            ((Node)pistol2Node.getChild(0)).detachChildAt(2);
            pistol2Node.setTranslation(114.5,0.1,219);
            pistol2Node.setScale(0.02);
            pistol2Node.setRotation(new Quaternion().fromAngleAxis(-Math.PI/2,new Vector3(1,0,0)));
            pistol2Node.setUserData(new WeaponUserData(weaponFactory.get("PISTOL_9MM"),new Matrix3(pistol2Node.getRotation()),PlayerData.NO_UID,false,true));
            collectibleObjectsList.add(pistol2Node);
            getRoot().attachChild(pistol2Node);
            final Node pistol3Node=(Node)binaryImporter.load(getClass().getResource("/abin/pistol3.abin"));
            pistol3Node.setName("a Mag 60");
            pistol3Node.setTranslation(115.5,0.1,219);
            pistol3Node.setScale(0.02);
            pistol3Node.setUserData(new WeaponUserData(weaponFactory.get("MAG_60"),new Matrix3(pistol3Node.getRotation()),PlayerData.NO_UID,false,true));
            collectibleObjectsList.add(pistol3Node);
            getRoot().attachChild(pistol3Node);
            /*final Node laserNode=(Node)binaryImporter.load(getClass().getResource("/abin/laser.abin"));
            laserNode.setName("a laser");
            laserNode.setTranslation(116.5,0.1,219);
            laserNode.setScale(0.02);
            laserNode.setUserData(new WeaponUserData(weaponFactory.getWeapon("LASER"),new Matrix3(laserNode.getRotation()),PlayerData.NO_UID,false,true));
            collectibleObjectsList.add(laserNode);
            getRoot().attachChild(laserNode);
            final Node shotgunNode=(Node)binaryImporter.load(getClass().getResource("/abin/shotgun.abin"));
            shotgunNode.setName("a shotgun");
            shotgunNode.setTranslation(117.5,0.1,219);
            shotgunNode.setScale(0.1);
            shotgunNode.setUserData(new WeaponUserData(weaponFactory.getWeapon("SHOTGUN"),new Matrix3(shotgunNode.getRotation()),PlayerData.NO_UID,false,true));
            collectibleObjectsList.add(shotgunNode);
            getRoot().attachChild(shotgunNode);	  
            
            final Node rocketLauncherNode=(Node)binaryImporter.load(getClass().getResource("/abin/rocketlauncher.abin"));
            //removes the scope
            rocketLauncherNode.detachChildAt(0);
            rocketLauncherNode.setName("a rocket launcher");
            rocketLauncherNode.setTranslation(117.5,0.1,222);
            rocketLauncherNode.setScale(0.08);
            rocketLauncherNode.setRotation(new Quaternion().fromAngleAxis(-Math.PI,new Vector3(0,1,0)));
            rocketLauncherNode.setUserData(new WeaponUserData(weaponFactory.getWeapon("ROCKET_LAUNCHER"),new Matrix3(rocketLauncherNode.getRotation()),PlayerData.NO_UID,false,true));
            collectibleObjectsList.add(rocketLauncherNode);
            getRoot().attachChild(rocketLauncherNode);*/
	       }
	    catch(IOException ioe)
	    {throw new RuntimeException("weapons loading failed",ioe);}
    }
    
    private final void loadAmmunitions(){
    	if(levelIndex==0||levelIndex==1)
	        {final Node bullet9mmAmmoNode=new Node("some 9mm bullets");
             final Box bullet9mmAmmoBox=new Box("some 9mm bullets",new Vector3(0,0,0),0.1,0.1,0.1);
             final TextureState ts = new TextureState();
             ts.setTexture(TextureManager.load(new URLResourceSource(getClass().getResource("/images/ammo.png")),Texture.MinificationFilter.Trilinear,true));
             bullet9mmAmmoBox.setRenderState(ts);
             bullet9mmAmmoNode.setTranslation(112.5,0.1,222.5);
             bullet9mmAmmoNode.attachChild(bullet9mmAmmoBox);
             bullet9mmAmmoNode.setUserData(new AmmunitionUserData(ammunitionFactory.get("BULLET_9MM"),30));
             collectibleObjectsList.add(bullet9mmAmmoNode);
             getRoot().attachChild(bullet9mmAmmoNode);
	        }
    }
    
    @SuppressWarnings("unchecked")
	private final void loadEnemies(){
    	if(levelIndex==0||levelIndex==1)
	    try{final Mesh weaponNodeTemplate=(Mesh)binaryImporter.load(getClass().getResource("/abin/weapon.abin"));
	        weaponNodeTemplate.setRotation(new Quaternion().fromEulerAngles(-Math.PI/2,0,-Math.PI/2));
	        weaponNodeTemplate.setScale(0.015);
            //the transform of the mesh mustn't be polluted by the initial rotation and scale as it is used to know the orientation of the weapon
            NodeHelper.applyTransformToMeshData(weaponNodeTemplate);
            weaponNodeTemplate.updateModelBound();
            weaponNodeTemplate.updateWorldBound(true);
            final KeyframeController<Mesh> weaponKeyframeControllerTemplate=(KeyframeController<Mesh>)weaponNodeTemplate.getController(0);
            for(PointInTime pit:weaponKeyframeControllerTemplate._keyframes)
                {pit._newShape.setScale(0.015);
                 pit._newShape.setRotation(new Quaternion().fromEulerAngles(-Math.PI/2,0,-Math.PI/2));
                 NodeHelper.applyTransformToMeshData(pit._newShape);
                 pit._newShape.updateModelBound();
                 pit._newShape.updateWorldBound(true);
                }
	    	final Mesh soldierNodeTemplate=(Mesh)binaryImporter.load(getClass().getResource("/abin/soldier.abin"));
	    	soldierNodeTemplate.setRotation(new Quaternion().fromEulerAngles(-Math.PI/2,0,-Math.PI/2));
	    	soldierNodeTemplate.setScale(0.015);
	    	NodeHelper.applyTransformToMeshData(soldierNodeTemplate);
	    	soldierNodeTemplate.updateModelBound();
	    	soldierNodeTemplate.updateWorldBound(true);
	    	final KeyframeController<Mesh> soldierKeyframeControllerTemplate=(KeyframeController<Mesh>)soldierNodeTemplate.getController(0);
	    	for(PointInTime pit:soldierKeyframeControllerTemplate._keyframes)
	    	    {pit._newShape.setScale(0.015);
                 pit._newShape.setRotation(new Quaternion().fromEulerAngles(-Math.PI/2,0,-Math.PI/2));
                 NodeHelper.applyTransformToMeshData(pit._newShape);
                 pit._newShape.updateModelBound();
                 pit._newShape.updateWorldBound(true);
	    	    }
	        final Vector3[] soldiersPos=new Vector3[]{new Vector3(118.5,0.4,219),new Vector3(117.5,0.4,219)};
	        for(Vector3 soldierPos:soldiersPos)
	            {final Mesh soldierNode=NodeHelper.makeCopy(soldierNodeTemplate,true);
	        	 soldierNode.setName("enemy@"+soldierNode.hashCode());
                 soldierNode.setTranslation(soldierPos);
                 final KeyframeController<Mesh> soldierKeyframeController=(KeyframeController<Mesh>)soldierNode.getController(0);
                 soldierKeyframeController.setUpdateBounding(true);
                 //loops on all frames of the set in the supplied time frame
                 soldierKeyframeController.setRepeatType(RepeatType.WRAP);
                 //uses the "stand" animation
                 soldierKeyframeController.setSpeed(MD2FrameSet.STAND.getFramesPerSecond());
                 soldierKeyframeController.setCurTime(MD2FrameSet.STAND.getFirstFrameIndex());
                 soldierKeyframeController.setMinTime(MD2FrameSet.STAND.getFirstFrameIndex());
                 soldierKeyframeController.setMaxTime(MD2FrameSet.STAND.getLastFrameIndex());
                 getRoot().attachChild(soldierNode);
                 final EnemyData soldierData=new EnemyData();
                 enemiesDataMap.put(soldierNode,soldierData);
                 final Mesh weaponNode=NodeHelper.makeCopy(weaponNodeTemplate,true);
                 weaponNode.setName("weapon of "+soldierNode.getName());
                 weaponNode.setTranslation(soldierPos);
                 final KeyframeController<Mesh> weaponKeyframeController=(KeyframeController<Mesh>)weaponNode.getController(0);
                 //loops on all frames of the set in the supplied time frame
                 weaponKeyframeController.setRepeatType(RepeatType.WRAP);
                 //uses the "stand" animation
                 weaponKeyframeController.setSpeed(MD2FrameSet.STAND.getFramesPerSecond());
                 weaponKeyframeController.setCurTime(MD2FrameSet.STAND.getFirstFrameIndex());
                 weaponKeyframeController.setMinTime(MD2FrameSet.STAND.getFirstFrameIndex());
                 weaponKeyframeController.setMaxTime(MD2FrameSet.STAND.getLastFrameIndex());
                 getRoot().attachChild(weaponNode);
	            }
	       }
	    catch(IOException ioe)
	    {throw new RuntimeException("enemies loading failed",ioe);}
    }
    
    private final void preloadTextures(){
    	final CanvasRenderer canvasRenderer=canvas.getCanvasRenderer();
    	final RenderContext renderContext=canvasRenderer.getRenderContext();
    	final Renderer renderer=canvasRenderer.getRenderer();
    	GameTaskQueueManager.getManager(renderContext).getQueue(GameTaskQueue.RENDER).enqueue(new Callable<Void>(){
			@Override
			public Void call() throws Exception {
				TextureManager.preloadCache(renderer);
				return null;
			}
    	});
    }
    
    public final void cleanup(){
    	taskManager.enqueueTask(new Runnable(){			
			@Override
			public final void run(){
    	        unloadSounds();
			}
    	});
    	taskManager.enqueueTask(new Runnable(){			
			@Override
			public final void run(){
    	        performInitialBasicCleanup();
			}
    	});
    	taskManager.enqueueTask(new Runnable(){			
			@Override
			public final void run(){
    	        performDirectNioBuffersCleanup();
			}
    	});
    	taskManager.enqueueTask(new Runnable(){			
			@Override
			public final void run(){
    	        performTexturesDataCleanup();
			}
    	});
    	taskManager.enqueueTask(new Runnable(){			
			@Override
			public final void run(){
    	        performTerminalBasicCleanup();
			}
    	});
    }
    
    @Override
    public final void init(){
    	taskManager.enqueueTask(new Runnable(){			
			@Override
			public final void run(){
				loadSounds();
			}
		}); 	
    	taskManager.enqueueTask(new Runnable(){			
			@Override
			public final void run(){
				performInitialBasicSetup();
			}
		});
        // Load level model
        taskManager.enqueueTask(new Runnable(){			
			@Override
			public final void run(){
				loadLevelModel();
			}
		});
        taskManager.enqueueTask(new Runnable(){			
			@Override
			public final void run(){
				loadOutdoor();
			}
		});
        taskManager.enqueueTask(new Runnable(){			
			@Override
			public final void run(){
				loadSkybox();
			}
		});
        taskManager.enqueueTask(new Runnable(){			
			@Override
			public final void run(){
				loadTeleporters();
			}
		});
        taskManager.enqueueTask(new Runnable(){			
			@Override
			public final void run(){
				loadMedikits();
			}
		});
        taskManager.enqueueTask(new Runnable(){			
			@Override
			public final void run(){
				loadWeapons();
			}
		});
        taskManager.enqueueTask(new Runnable(){			
			@Override
			public final void run(){
				loadAmmunitions();
			}
		});
        taskManager.enqueueTask(new Runnable(){			
			@Override
			public final void run(){
				loadEnemies();
			}
		});
        taskManager.enqueueTask(new Runnable(){			
			@Override
			public final void run(){
				preloadTextures();
			}
	    });
        taskManager.enqueueTask(new Runnable(){			
			@Override
			public final void run(){
				performTerminalBasicSetup();
			}
		});
    }
    
    @Override
    public void setEnabled(final boolean enabled){
        final boolean wasEnabled=isEnabled();
        super.setEnabled(enabled);
        if(wasEnabled!=isEnabled())
            {final Camera cam=canvas.getCanvasRenderer().getCamera();
             if(isEnabled())
                 {//FIXME this is a source of bugs, rather do that during the initialization
            	  mouseManager.setGrabbed(GrabbedState.NOT_GRABBED);
                  mouseManager.setPosition(cam.getWidth()/2,cam.getHeight()/2);
            	  mouseManager.setGrabbed(GrabbedState.GRABBED);
         		  if(customMouseAndKeyboardSettings.isMousePointerNeverHidden())
      		          {GameTaskQueueManager.getManager(canvas.getCanvasRenderer().getRenderContext()).getQueue(GameTaskQueue.RENDER).enqueue(new Callable<Void>(){
      				       @Override
      				       public Void call() throws Exception {
      					       ((JoglNewtWindow)canvas).getNewtWindow().setPointerVisible(true);
      					       return null;
      				       }
      	    	       });
      		          }
                  //the resolution of the screen might have been modified in the graphical user interface
                  //updates all elements whose positions should be bound to the resolution of the screen
                  updateCrosshairNode();
          		  /*
          		   * checks whether the custom action map has been modified by comparing it to the default one or 
          		   * if the input triggers have never been initialized
          		   * */
          		  if(fpsc==null||!customActionMap.equals(defaultActionMap)||!customMouseAndKeyboardSettings.equals(defaultMouseAndKeyboardSettings))
          		      {//(re)initializes input triggers
          			   initializeInput(toPauseMenuTriggerAction,toPauseMenuTriggerActionForExitConfirm,toggleScreenModeAction,cam,physicalLayer);
          		      }
                 }
            }
    }
}
