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

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.Map;

import javax.imageio.ImageIO;
import com.ardor3d.bounding.BoundingBox;
import com.ardor3d.bounding.CollisionTree;
import com.ardor3d.bounding.CollisionTreeManager;
import com.ardor3d.extension.model.util.KeyframeController;
import com.ardor3d.framework.Canvas;
import com.ardor3d.framework.CanvasRenderer;
import com.ardor3d.framework.NativeCanvas;
import com.ardor3d.image.Texture;
import com.ardor3d.input.Key;
import com.ardor3d.input.MouseButton;
import com.ardor3d.input.PhysicalLayer;
import com.ardor3d.input.logical.InputTrigger;
import com.ardor3d.input.logical.KeyPressedCondition;
import com.ardor3d.input.logical.KeyReleasedCondition;
import com.ardor3d.input.logical.MouseButtonPressedCondition;
import com.ardor3d.input.logical.MouseButtonReleasedCondition;
import com.ardor3d.input.logical.MouseWheelMovedCondition;
import com.ardor3d.input.logical.TriggerAction;
import com.ardor3d.input.logical.TwoInputStates;
import com.ardor3d.intersection.BoundingCollisionResults;
import com.ardor3d.intersection.CollisionResults;
import com.ardor3d.intersection.PickingUtil;
import com.ardor3d.math.ColorRGBA;
import com.ardor3d.math.Matrix3;
import com.ardor3d.math.Quaternion;
import com.ardor3d.math.Vector3;
import com.ardor3d.renderer.Camera;
import com.ardor3d.renderer.Renderer;
import com.ardor3d.renderer.state.TextureState;
import com.ardor3d.scenegraph.Mesh;
import com.ardor3d.scenegraph.MeshData;
import com.ardor3d.scenegraph.Node;
import com.ardor3d.scenegraph.Spatial;
import com.ardor3d.scenegraph.controller.ComplexSpatialController.RepeatType;
import com.ardor3d.scenegraph.controller.SpatialController;
import com.ardor3d.scenegraph.extension.CameraNode;
import com.ardor3d.scenegraph.extension.Skybox;
import com.ardor3d.scenegraph.shape.Box;
import com.ardor3d.ui.text.BasicText;
import com.ardor3d.util.ReadOnlyTimer;
import com.ardor3d.util.TextureManager;
import com.ardor3d.util.export.binary.BinaryImporter;
import com.ardor3d.util.geom.BufferUtils;
import com.ardor3d.util.resource.URLResourceSource;
import engine.data.PlayerData;
import engine.data.common.Medikit;
import engine.data.common.Teleporter;
import engine.data.common.userdata.AmmunitionUserData;
import engine.data.common.userdata.CollectibleUserData;
import engine.data.common.userdata.MedikitUserData;
import engine.data.common.userdata.TeleporterUserData;
import engine.data.common.userdata.WeaponUserData;
import engine.input.ExtendedFirstPersonControl;
import engine.misc.ApplicativeTimer;
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
 * @author Julien Gouesse
 *
 */
public final class GameState extends ScenegraphState{
    
    /**index of the level*/
    private int levelIndex;
    /**Our native window, not the gl surface itself*/
    private final NativeCanvas canvas;
    /**previous (before entering this state) frustum far value*/
    private double previousFrustumNear;
    /**previous (before entering this state) frustum near value*/
    private double previousFrustumFar;
    /**previous (before entering this state) location of the camera*/
    private final Vector3 previousCamLocation;
    /**current location of the camera*/
    private final Vector3 currentCamLocation;
    /**node of the player*/
    private final CameraNode playerNode;
    /**data of the player*/
    private final PlayerData playerData;
    /**player object that relies on a state machine*/
    private final LogicalPlayer playerWithStateMachine;
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
    
    public GameState(final NativeCanvas canvas,final PhysicalLayer physicalLayer,final TriggerAction exitAction,final SoundManager soundManager,final TaskManager taskManager){
        super(soundManager);
        this.binaryImporter=new BinaryImporter();
        this.taskManager=taskManager;
        timer=new ApplicativeTimer();
        collectibleObjectsList=new ArrayList<Node>();
        teleportersList=new ArrayList<Node>();        
        teleporter=initializeTeleporter();
        medikit=initializeMedikit();
        //initialize the factories, the build-in ammo and the build-in weapons       
        ammunitionFactory=initializeAmmunitionFactory();
        weaponFactory=initializeWeaponFactory();
        readCollisionMap();
        this.canvas=canvas;
        final Camera cam=canvas.getCanvasRenderer().getCamera();
        // create a node that follows the camera
        playerNode=new CameraNode("player",cam);
        //FIXME handle collision detection between bullet(s) and enemies here too
        playerData=new PlayerData(playerNode,ammunitionFactory,weaponFactory,true){
        	@Override
        	public Map.Entry<Integer,Integer> attack(){
        		final Map.Entry<Integer,Integer> consumedAmmunitionOrKnockCounts=super.attack();
        		final String identifier=getCurrentWeaponBlowOrShotSoundSampleIdentifier();
        		//primary hand
        		for(int index=0;index<consumedAmmunitionOrKnockCounts.getKey().intValue();index++)
        		    {if(isCurrentWeaponAmmunitionCountDisplayable())
        		    	 {//FIXME use the world bound(s) of the weapon(s) to compute the initial position(s) of the shot(s)
        		    	  cameraNode.getChild(0).getWorldBound().getCenter();
        		    	  //      store them for further use
        		    	 }
        		     if(identifier!=null)
        		    	 soundManager.play(false,identifier);
        		    }
        		//secondary hand
        		for(int index=0;index<consumedAmmunitionOrKnockCounts.getValue().intValue();index++)
    		        {if(isCurrentWeaponAmmunitionCountDisplayable())
    		    	     {//FIXME use the world bound(s) of the weapon(s) to compute the initial position(s) of the shot(s)
    		    	      cameraNode.getChild(1).getWorldBound().getCenter();
    		    	      //      store them for further use
    		    	     }
    		         if(identifier!=null)
    		    	     soundManager.play(false,identifier);
    		        }
        		return(consumedAmmunitionOrKnockCounts);
        	}
        	
        	@Override
        	public int reload(){
        		final int reloadedAmmoCount=super.reload();
        		if(reloadedAmmoCount>0)
    		        {final String identifier=getCurrentWeaponReloadSoundSampleIdentifier();
    		         if(identifier!=null)
   			             soundManager.play(false,identifier);
    		        }
        		return(reloadedAmmoCount);
        	}
        };
        playerWithStateMachine=new LogicalPlayer(playerData);
        this.previousCamLocation=new Vector3(cam.getLocation());
        this.currentCamLocation=new Vector3(previousCamLocation);
        initializeInput(exitAction,cam,physicalLayer);
        //initialize all text displays
        ammoTextLabel=initializeAmmunitionTextLabel();
        fpsTextLabel=initializeFpsTextLabel();
        healthTextLabel=initializeHealthTextLabel();
        headUpDisplayLabel=initializeHeadUpDisplayLabel();
        initializeCollisionSystem(cam);
    }
    
    private final void initializeCollisionSystem(final Camera cam){
    	// configure the collision system
        CollisionTreeManager.getInstance().setTreeType(CollisionTree.Type.AABB);       
        //add a mesh with an invisible mesh data
        Mesh playerMesh=new Mesh("player");
        MeshData playerMeshData=new MeshData();
        FloatBuffer playerVertexBuffer=BufferUtils.createFloatBuffer(6);
        playerVertexBuffer.put(-0.5f).put(-0.9f).put(-0.5f).put(0.5f).put(0.9f).put(0.5f).rewind();
        playerMeshData.setVertexBuffer(playerVertexBuffer);
        playerMesh.setMeshData(playerMeshData);
        playerNode.attachChild(playerMesh);
        //add a bounding box to the camera node
        NodeHelper.setModelBound(playerNode,BoundingBox.class);
        playerNode.addController(new SpatialController<Spatial>(){
        	
        	private final CollisionResults collisionResults=new BoundingCollisionResults();
        	
        	private Vector3 previousPosition=new Vector3(115,0.5,223);
        	
        	private boolean wasBeingTeleported=false;
        	
        	//private long previouslyMeasuredElapsedTime=-1;
        	
            @Override
            public void update(double timeSinceLastCall,Spatial caller){
            	//update the timer
            	timer.update();
            	/*final long absoluteElapsedTimeInNanoseconds=timer.getElapsedTimeInNanoseconds();
            	final long elapsedTimeSinceLatestCallInNanos=previouslyMeasuredElapsedTime==-1?0:absoluteElapsedTimeInNanoseconds-previouslyMeasuredElapsedTime;
            	previouslyMeasuredElapsedTime=absoluteElapsedTimeInNanoseconds;*/
                //synchronizes the camera node with the camera
                playerNode.updateFromCamera();
                //temporary avoids to move on Y
                playerNode.addTranslation(0,0.5-playerNode.getTranslation().getY(),0);
                //synchronizes the camera with the camera node
                cam.setLocation(playerNode.getTranslation());
                //FIXME: remove this temporary system
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
                for(int i=1;i<=stepCount&&!collisionFound;i++)
                    {playerX=playerStartX+(stepX*i);
                	 playerZ=playerStartZ+(stepZ*i);
                	 for(int z=0;z<2&&!collisionFound;z++)
             	    	for(int x=0;x<2&&!collisionFound;x++)
             	    	    {tmpX=(int)(playerX-0.2+(x*0.4));
             	    	     tmpZ=(int)(playerZ-0.2+(z*0.4));
             	    	     if(0<=tmpX && tmpX<collisionMap.length && 0<=tmpZ && tmpZ<collisionMap[tmpX].length)
             	    		     collisionFound=collisionMap[tmpX][tmpZ];
             	    	     else
             	    	    	 collisionFound=false;
             	    	    }
                	 if(!collisionFound)
                		 {correctX=playerX;
                		  correctZ=playerZ;
                		 }
                    }
                //updates the current location
                playerNode.setTranslation(correctX,0.5,correctZ);
                //updates the previous location and the camera
                previousPosition.set(playerNode.getTranslation());
                cam.setLocation(playerNode.getTranslation());
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
                	          {//remove it from the list of collectible objects
                			   collectibleObjectsList.remove(i);
                			   if(collectibleNode.getParent()!=null)
                				   //detach this object from its parent so that it is no more visible
                				   collectibleNode.getParent().detachChild(collectibleNode);
                			   CollectibleUserData<?> collectibleUserData=(CollectibleUserData<?>)collectibleNode.getUserData();
                			   //display a message when the player picked up something
                			   subElementName=collectibleUserData.getSubElementName();
                			   if(subElementName!=null && !subElementName.equals(""))
                				   headUpDisplayLabel.setText("picked up "+collectedSubElementsCount+" "+subElementName+(collectedSubElementsCount>1?"s":""));
                			   else
                			       headUpDisplayLabel.setText("picked up "+collectibleNode.getName());               	           
                	           //play a sound if available
                	           if(collectibleUserData.getPickingUpSoundSampleIdentifier()!=null)
                                   getSoundManager().play(false,collectibleUserData.getPickingUpSoundSampleIdentifier());
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
                      		    //then move him
                      		    playerNode.setTranslation(teleporterDestination);
                      		    //updates the previous location to avoid any problem when detecting the collisions
                                previousPosition.set(teleporterDestination);
                                //synchronizes the camera with the camera node
                                cam.setLocation(teleporterDestination);
                                //play a sound if available
                 	            if(teleporterUserData.getPickingUpSoundSampleIdentifier()!=null)
                                    getSoundManager().play(false,teleporterUserData.getPickingUpSoundSampleIdentifier());
                	           }
                	      }                          
                    }
                //if the player is not on any teleporter
                if(!hasCollision)
                	wasBeingTeleported=false;
                
                playerWithStateMachine.updateLogicalLayer(timer);
            }           
        });
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
    		stateMachine.fireEvent(PlayerEvent.PUTTING_BACK_BEFORE_RELOADING);
    	}
    	
    	public void trySelectNextWeapon(){
    		stateMachine.fireEvent(PlayerEvent.PUTTING_BACK_BEFORE_SELECTING_NEXT);
    	}
    	
        public void trySelectPreviousWeapon(){
        	stateMachine.fireEvent(PlayerEvent.PUTTING_BACK_BEFORE_SELECTING_PREVIOUS);
    	}
        
        public PlayerState getPreviousState(){
        	return(stateMachine.previousState);
        }
        
        public void tryStartAttacking(){
        	stateMachine.fireEvent(PlayerEvent.PRESSING_TRIGGER);
        }
        
        public void tryStopAttacking(){
        	stateMachine.fireEvent(PlayerEvent.RELEASING_TRIGGER);
        }
    }
    
    private final void initializeInput(final TriggerAction exitAction,final Camera cam,final PhysicalLayer physicalLayer){
    	final Vector3 worldUp=new Vector3(0,1,0);              
        // drag only at false to remove the need of pressing a button to move
        ExtendedFirstPersonControl fpsc=ExtendedFirstPersonControl.setupTriggers(getLogicalLayer(),worldUp,false);
        fpsc.setMoveSpeed(fpsc.getMoveSpeed()/10);
        //create a text node that asks the user to confirm or not the exit
        final BasicText exitPromptTextLabel=BasicText.createDefaultTextLabel("Confirm Exit","Confirm Exit? Y/N");
        exitPromptTextLabel.setTranslation(new Vector3(cam.getWidth()/2,cam.getHeight()/2,0));
        final InputTrigger exitPromptTrigger=new InputTrigger(new KeyReleasedCondition(Key.ESCAPE),new TriggerAction(){
        	@Override
			public void perform(Canvas source, TwoInputStates inputState, double tpf){
				//if the player has not been prompted
        		if(!getRoot().hasChild(exitPromptTextLabel))
        		    getRoot().attachChild(exitPromptTextLabel);
			}
		});
        final InputTrigger exitConfirmTrigger=new InputTrigger(new KeyReleasedCondition(Key.Y),new TriggerAction(){     	
        	@Override
			public void perform(Canvas source, TwoInputStates inputState, double tpf){
				//if the player has just been prompted
        		if(getRoot().hasChild(exitPromptTextLabel))
				    {//remove the prompt message
        			 getRoot().detachChild(exitPromptTextLabel);
        			 //quit the program
        			 exitAction.perform(source,inputState,tpf);
				    }
			}
		});
        final InputTrigger exitInfirmTrigger=new InputTrigger(new KeyReleasedCondition(Key.N),new TriggerAction(){     	
        	@Override
			public void perform(Canvas source, TwoInputStates inputState, double tpf){
				//if the player has just been prompted
        		if(getRoot().hasChild(exitPromptTextLabel))
				    {//remove the prompt message
        			 getRoot().detachChild(exitPromptTextLabel);
				    }
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
		final TriggerAction wheelWeaponAction=new TriggerAction(){		
			@Override
			public void perform(Canvas source, TwoInputStates inputState, double tpf){
				//if the mouse wheel has been rotated up/away from the user
				if(inputState.getCurrent().getMouseState().getDwheel()>0)
					playerWithStateMachine.trySelectNextWeapon();
				else
					//otherwise the mouse wheel has been rotated down/towards the user
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
				timer.setPauseEnabled(true);
				timer.update();
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
		final TriggerAction selectWeaponOneAction=new TriggerAction(){
			@Override
			public void perform(Canvas source, TwoInputStates inputState, double tpf){
				//playerData.selectWeapon(0,false);
			}
		};
        //add some triggers to change weapon, reload and shoot
		final InputTrigger weaponMouseWheelTrigger=new InputTrigger(new MouseWheelMovedCondition(),wheelWeaponAction);
        final InputTrigger nextWeaponTrigger=new InputTrigger(new KeyReleasedCondition(Key.L),nextWeaponAction);
        final InputTrigger previousWeaponTrigger=new InputTrigger(new KeyReleasedCondition(Key.M),previousWeaponAction);
        final InputTrigger reloadWeaponTrigger=new InputTrigger(new KeyReleasedCondition(Key.R),reloadWeaponAction);
        final InputTrigger reloadWeaponMouseButtonTrigger=new InputTrigger(new MouseButtonReleasedCondition(MouseButton.RIGHT),reloadWeaponAction);
        final InputTrigger startAttackTrigger=new InputTrigger(new KeyPressedCondition(Key.SPACE),startAttackAction);
        final InputTrigger stopAttackTrigger=new InputTrigger(new KeyReleasedCondition(Key.SPACE),stopAttackAction);
        final InputTrigger startAttackMouseButtonTrigger=new InputTrigger(new MouseButtonPressedCondition(MouseButton.LEFT),startAttackAction);
        final InputTrigger stopAttackMouseButtonTrigger=new InputTrigger(new MouseButtonReleasedCondition(MouseButton.LEFT),stopAttackAction);
        final InputTrigger pauseTrigger=new InputTrigger(new KeyReleasedCondition(Key.P),pauseAction);
        final InputTrigger crouchTrigger=new InputTrigger(new KeyReleasedCondition(Key.C),crouchAction);
        final InputTrigger activateTrigger=new InputTrigger(new KeyReleasedCondition(Key.RETURN),activateAction);
        final InputTrigger startRunningRightTrigger=new InputTrigger(new KeyPressedCondition(Key.RSHIFT),startRunningAction);
        final InputTrigger stopRunningRightTrigger=new InputTrigger(new KeyReleasedCondition(Key.RSHIFT),stopRunningAction);
        final InputTrigger startRunningLeftTrigger=new InputTrigger(new KeyPressedCondition(Key.LSHIFT),startRunningAction);
        final InputTrigger stopRunningLeftTrigger=new InputTrigger(new KeyReleasedCondition(Key.LSHIFT),stopRunningAction);
        final InputTrigger selectWeaponOneTrigger=new InputTrigger(new KeyReleasedCondition(Key.ONE),selectWeaponOneAction);       
        final InputTrigger[] triggers=new InputTrigger[]{exitPromptTrigger,exitConfirmTrigger,exitInfirmTrigger,
        		nextWeaponTrigger,previousWeaponTrigger,weaponMouseWheelTrigger,reloadWeaponTrigger,
        		reloadWeaponMouseButtonTrigger,startAttackTrigger,startAttackMouseButtonTrigger,pauseTrigger,crouchTrigger,
        		activateTrigger,startRunningRightTrigger,stopRunningRightTrigger,startRunningLeftTrigger,
        		stopRunningLeftTrigger,selectWeaponOneTrigger,stopAttackTrigger,stopAttackMouseButtonTrigger};
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
            		{final StringBuffer text=new StringBuffer();
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
            @Override
            public final void update(double timePerFrame,Spatial caller){
                fpsTextLabel.setText(" "+Math.round(timePerFrame>0?1/timePerFrame:0)+" FPS");
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
        weaponFactory.addNewWeapon("/sounds/pickup_weapon.ogg","/sounds/pistol9mn_shot.ogg","/sounds/pistol9mn_reload.ogg","PISTOL_9MM",true,8,ammunitionFactory.getAmmunition("BULLET_9MM"),1,500,true);
        weaponFactory.addNewWeapon("/sounds/pickup_weapon.ogg",null,null,"PISTOL_10MM",true,10,ammunitionFactory.getAmmunition("BULLET_10MM"),1,500,true);
        weaponFactory.addNewWeapon("/sounds/pickup_weapon.ogg","/sounds/mag60_shot.ogg","/sounds/mag60_reload.ogg","MAG_60",true,30,ammunitionFactory.getAmmunition("BULLET_9MM"),1,100,true);
        weaponFactory.addNewWeapon("/sounds/pickup_weapon.ogg",null,null,"UZI",true,20,ammunitionFactory.getAmmunition("BULLET_9MM"),1,100,true);
        weaponFactory.addNewWeapon("/sounds/pickup_weapon.ogg",null,null,"SMACH",true,35,ammunitionFactory.getAmmunition("BULLET_5_56MM"),1,100,true);
        weaponFactory.addNewWeapon("/sounds/pickup_weapon.ogg",null,null,"LASER",true,15,ammunitionFactory.getAmmunition("ENERGY_CELL"),1,1000,false);
        weaponFactory.addNewWeapon("/sounds/pickup_weapon.ogg",null,null,"SHOTGUN",false,3,ammunitionFactory.getAmmunition("CARTRIDGE"),1,1500,false);
        weaponFactory.addNewWeapon("/sounds/pickup_weapon.ogg",null,null,"ROCKET_LAUNCHER",false,1,ammunitionFactory.getAmmunition("ANTI_TANK_ROCKET_105MM"),1,2000,false);
        return(weaponFactory);
    }
    
    @Deprecated
    private final void readCollisionMap(){
    	try{BufferedImage map=ImageIO.read(GameState.class.getResource("/images/containermap.png"));
    	    collisionMap=new boolean[map.getWidth()][map.getHeight()];
    	    for(int y=0;y<map.getHeight();y++)
    	    	for(int x=0;x<map.getWidth();x++)
    	    		collisionMap[x][y]=(map.getRGB(x, y)==Color.BLUE.getRGB());
    	   }
    	catch(IOException ioe)
		{ioe.printStackTrace();}
    }
    
    final void setLevelIndex(final int levelIndex){
        this.levelIndex=levelIndex;
    }
    
    private final void loadSounds(){
    	//loads the sound samples
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
            {final Ammunition ammo=ammunitionFactory.getAmmunition(ammoIndex);
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
            {final Weapon weapon=weaponFactory.getWeapon(weaponIndex);
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
    }
    
    private final void loadLevelModel(){
    	try{final Node levelNode=(Node)binaryImporter.load(getClass().getResource("/abin/LID"+levelIndex+".abin"));
            NodeHelper.setBackCullState(levelNode);
            getRoot().attachChild(levelNode);
    	   }
    	catch(IOException ioe)
    	{throw new RuntimeException("level loading failed",ioe);}
    }
    
    private final void loadOutdoor(){
    	try{final Node outdoorPartLevelNode=(Node)binaryImporter.load(getClass().getResource("/abin/wildhouse_action.abin"));
            outdoorPartLevelNode.setTranslation(-128, -6, -128);
            getRoot().attachChild(outdoorPartLevelNode);   		
    	   }
    	catch(IOException ioe)
    	{throw new RuntimeException("outdoor loading failed",ioe);}
    }
    
    private final void performInitialBasicSetup(){
    	// clear the list of objects that can be picked up
    	collectibleObjectsList.clear();
        // Remove all previously attached children
        getRoot().detachAllChildren();
        //FIXME: it should not be hard-coded
        currentCamLocation.set(115,0.5,223);
        //attach the player itself
        getRoot().attachChild(playerNode);
        //attach the ammunition display node
        getRoot().attachChild(ammoTextLabel);
        //attach the FPS display node
        getRoot().attachChild(fpsTextLabel);
        //attach the health display node
        getRoot().attachChild(healthTextLabel);
        //attach the HUD node
        getRoot().attachChild(headUpDisplayLabel);
    }
    
    private final void performTerminalBasicSetup(){
    	//add a bounding box to each collectible object
        for(Node collectible:collectibleObjectsList)
       	 NodeHelper.setModelBound(collectible,BoundingBox.class);
        for(Node teleporter:teleportersList)
       	 NodeHelper.setModelBound(teleporter,BoundingBox.class);
        //reset the timer at the end of all long operations performed while loading
        timer.reset();
    }
    
    private final void loadSkybox(){
	    final Skybox skyboxNode=new Skybox("skybox",64,64,64);
	    skyboxNode.setTranslation(-128,0,-128);
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
    
    private final void loadTeleporters(){
	    final Node teleporterNode=new Node("a teleporter");
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
    }
    
    private final void loadMedikits(){
    	final Node medikitNode=new Node("a medikit");
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
    
    private final void loadWeapons(){
	    try{final Node uziNode=(Node)binaryImporter.load(getClass().getResource("/abin/uzi.abin"));
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
            getRoot().attachChild(duplicatePistolNode);
            final Node pistol2Node=(Node)binaryImporter.load(getClass().getResource("/abin/pistol2.abin"));
            pistol2Node.setName("a pistol (9mm)");
            //removes the bullet as it is not necessary now
            ((Node)pistol2Node.getChild(0)).detachChildAt(2);
            pistol2Node.setTranslation(114.5,0.1,219);
            pistol2Node.setScale(0.02);
            pistol2Node.setRotation(new Quaternion().fromAngleAxis(-Math.PI/2,new Vector3(1,0,0)));
            pistol2Node.setUserData(new WeaponUserData(weaponFactory.getWeapon("PISTOL_9MM"),new Matrix3(pistol2Node.getRotation()),PlayerData.NO_UID,false,true));
            collectibleObjectsList.add(pistol2Node);
            getRoot().attachChild(pistol2Node);
            final Node pistol3Node=(Node)binaryImporter.load(getClass().getResource("/abin/pistol3.abin"));
            pistol3Node.setName("a Mag 60");
            pistol3Node.setTranslation(115.5,0.1,219);
            pistol3Node.setScale(0.02);
            pistol3Node.setUserData(new WeaponUserData(weaponFactory.getWeapon("MAG_60"),new Matrix3(pistol3Node.getRotation()),PlayerData.NO_UID,false,true));
            collectibleObjectsList.add(pistol3Node);
            getRoot().attachChild(pistol3Node);
            final Node laserNode=(Node)binaryImporter.load(getClass().getResource("/abin/laser.abin"));
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
            getRoot().attachChild(rocketLauncherNode);	
	       }
	    catch(IOException ioe)
	    {throw new RuntimeException("weapons loading failed",ioe);}
    }
    
    private final void loadAmmunitions(){
    	final Node bullet9mmAmmoNode=new Node("some 9mm bullets");
        final Box bullet9mmAmmoBox=new Box("some 9mm bullets",new Vector3(0,0,0),0.1,0.1,0.1);
        bullet9mmAmmoBox.setDefaultColor(ColorRGBA.GREEN);
        bullet9mmAmmoNode.setTranslation(112.5,0.1,222.5);
        bullet9mmAmmoNode.attachChild(bullet9mmAmmoBox);
        bullet9mmAmmoNode.setUserData(new AmmunitionUserData(ammunitionFactory.getAmmunition("BULLET_9MM"),30));
        collectibleObjectsList.add(bullet9mmAmmoNode);
        getRoot().attachChild(bullet9mmAmmoNode);
    }
    
    @SuppressWarnings("unchecked")
	private final void loadEnemies(){
	    try{final Mesh soldierNode=(Mesh)binaryImporter.load(getClass().getResource("/abin/soldier.abin"));
            soldierNode.setName("a soldier");
            soldierNode.setTranslation(118.5,0.4,219);
            soldierNode.setRotation(new Quaternion().fromEulerAngles(-Math.PI/2,0,-Math.PI/2));
            soldierNode.setScale(0.015);
            final KeyframeController<Mesh> soldierKeyframeController=(KeyframeController<Mesh>)soldierNode.getController(0);
            //loops on all frames of the set in the supplied time frame
            soldierKeyframeController.setRepeatType(RepeatType.WRAP);
            //uses the "stand" animation
            soldierKeyframeController.setSpeed(MD2FrameSet.STAND.getFramesPerSecond());
            soldierKeyframeController.setMinTime(MD2FrameSet.STAND.getFirstFrameIndex());
            soldierKeyframeController.setMaxTime(MD2FrameSet.STAND.getLastFrameIndex());
            getRoot().attachChild(soldierNode);
	       }
	    catch(IOException ioe)
	    {throw new RuntimeException("enemies loading failed",ioe);}
    }
    
    private final void preloadTextures(){
    	final CanvasRenderer canvasRenderer=canvas.getCanvasRenderer();
    	final Renderer renderer=canvasRenderer.getRenderer();   	
    	canvasRenderer.makeCurrentContext();
    	TextureManager.preloadCache(renderer);
    	canvasRenderer.releaseCurrentContext();
    }
    
    @Override
    public final void init(){
    	taskManager.enqueueTask(new Runnable(){			
			@Override
			public final void run() {
				loadSounds();
			}
		}); 	
    	taskManager.enqueueTask(new Runnable(){			
			@Override
			public final void run() {
				performInitialBasicSetup();
			}
		});
        // Load level model
        taskManager.enqueueTask(new Runnable(){			
			@Override
			public final void run() {
				loadLevelModel();
			}
		});
        taskManager.enqueueTask(new Runnable(){			
			@Override
			public final void run() {
				loadOutdoor();
			}
		});
        taskManager.enqueueTask(new Runnable(){			
			@Override
			public final void run() {
				loadSkybox();
			}
		});
        taskManager.enqueueTask(new Runnable(){			
			@Override
			public final void run() {
				loadTeleporters();
			}
		});
        taskManager.enqueueTask(new Runnable(){			
			@Override
			public final void run() {
				loadMedikits();
			}
		});
        taskManager.enqueueTask(new Runnable(){			
			@Override
			public final void run() {
				loadWeapons();
			}
		});
        taskManager.enqueueTask(new Runnable(){			
			@Override
			public final void run() {
				loadAmmunitions();
			}
		});
        taskManager.enqueueTask(new Runnable(){			
			@Override
			public final void run() {
				loadEnemies();
			}
		});
        taskManager.enqueueTask(new Runnable(){			
			@Override
			public final void run() {
				preloadTextures();
			}
	    });
        taskManager.enqueueTask(new Runnable(){			
			@Override
			public final void run() {
				performTerminalBasicSetup();
			}
		});
    }
    
    @Override
    public void setEnabled(final boolean enabled){
        final boolean wasEnabled=isEnabled();
        if(wasEnabled!=enabled)
            {super.setEnabled(enabled);
             final Camera cam=canvas.getCanvasRenderer().getCamera();
             if(enabled)
                 {previousFrustumNear=cam.getFrustumNear();
                  previousFrustumFar=cam.getFrustumFar();
                  previousCamLocation.set(cam.getLocation());
                  cam.setFrustumPerspective(cam.getFovY(),(float)cam.getWidth()/(float)cam.getHeight(),0.1,200);
                  cam.setLocation(currentCamLocation);
                 }
             else
                 {currentCamLocation.set(cam.getLocation());                  
                  cam.setFrustumPerspective(cam.getFovY(),(float)cam.getWidth()/(float)cam.getHeight(),previousFrustumNear,previousFrustumFar);
                  cam.setLocation(previousCamLocation);
                 }
            }
    }
}
