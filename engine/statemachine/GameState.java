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
import javax.imageio.ImageIO;
import com.ardor3d.bounding.BoundingBox;
import com.ardor3d.bounding.CollisionTree;
import com.ardor3d.bounding.CollisionTreeManager;
import com.ardor3d.framework.Canvas;
import com.ardor3d.framework.NativeCanvas;
import com.ardor3d.image.Texture;
import com.ardor3d.input.Key;
import com.ardor3d.input.MouseButton;
import com.ardor3d.input.PhysicalLayer;
import com.ardor3d.input.logical.InputTrigger;
import com.ardor3d.input.logical.KeyPressedCondition;
import com.ardor3d.input.logical.KeyReleasedCondition;
import com.ardor3d.input.logical.MouseButtonReleasedCondition;
import com.ardor3d.input.logical.MouseWheelMovedCondition;
import com.ardor3d.input.logical.TriggerAction;
import com.ardor3d.input.logical.TwoInputStates;
import com.ardor3d.intersection.BoundingCollisionResults;
import com.ardor3d.intersection.CollisionResults;
import com.ardor3d.intersection.PickingUtil;
import com.ardor3d.math.Matrix3;
import com.ardor3d.math.Quaternion;
import com.ardor3d.math.Vector3;
import com.ardor3d.renderer.Camera;
import com.ardor3d.renderer.state.TextureState;
import com.ardor3d.scenegraph.Mesh;
import com.ardor3d.scenegraph.MeshData;
import com.ardor3d.scenegraph.Node;
import com.ardor3d.scenegraph.Spatial;
import com.ardor3d.scenegraph.controller.SpatialController;
import com.ardor3d.scenegraph.extension.CameraNode;
import com.ardor3d.scenegraph.extension.Skybox;
import com.ardor3d.scenegraph.shape.Box;
import com.ardor3d.ui.text.BasicText;
import com.ardor3d.util.TextureManager;
import com.ardor3d.util.export.binary.BinaryImporter;
import com.ardor3d.util.geom.BufferUtils;
import com.ardor3d.util.resource.URLResourceSource;
import engine.data.CollectibleUserData;
import engine.data.MedikitUserData;
import engine.data.PlayerData;
import engine.data.TeleporterUserData;
import engine.data.WeaponUserData;
import engine.input.ExtendedFirstPersonControl;
import engine.misc.NodeHelper;
import engine.sound.SoundManager;
import engine.weaponry.AmmunitionFactory;
import engine.weaponry.WeaponFactory;

/**
 * State used during the game, the party.
 * @author Julien Gouesse
 *
 */
public final class GameState extends State{
    
    /**index of the level*/
    private int levelIndex;
    /**Our native window, not the gl surface itself*/
    private final NativeCanvas canvas;
    /**source name of the sound played when picking up some ammo*/
    private static String pickupAmmoSourcename;
    /**source name of the sound played when picking up a weapon*/
    private static String pickupWeaponSourcename;
    /**path of the sound sample played when picking up a weapon*/
    private static final String pickupWeaponSoundSamplePath="/sounds/pickup_weapon.ogg";
    /**source name of the sound played when entering a teleporter*/
    private static String teleporterUseSourcename;
    /**path of the sound sample played when entering a teleporter*/
    private static final String teleporterUseSoundSamplePath="/sounds/teleporter_use.ogg";
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
    /**list containing all objects that can be picked up*/
    private final ArrayList<Node> collectibleObjectsList;
    /**list of teleporters*/
    private final ArrayList<Node> teleportersList;
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
    /***/
    private final AmmunitionFactory ammunitionFactory;
    /**instance that creates all weapons*/
    private final WeaponFactory weaponFactory;
    
    public GameState(final NativeCanvas canvas,final PhysicalLayer physicalLayer,final TriggerAction exitAction,final SoundManager soundManager){
        super(soundManager);
        //initialize the factories, the build-in ammo and the build-in weapons       
        ammunitionFactory=new AmmunitionFactory();
        /**American assault rifle*/
        ammunitionFactory.addNewAmmunition("BULLET_5_56MM");
    	/**Russian assault rifle*/
        ammunitionFactory.addNewAmmunition("BULLET_7_62MM");
    	/**American pistols and sub-machine guns*/
        ammunitionFactory.addNewAmmunition("BULLET_9MM");
    	/**Russian pistols*/
        ammunitionFactory.addNewAmmunition("BULLET_10MM");
    	/**cartridge*/
        ammunitionFactory.addNewAmmunition("CARTRIDGE");
    	/**power*/
        ammunitionFactory.addNewAmmunition("ENERGY");
    	/**Russian middle range anti-tank rocket launchers*/
        ammunitionFactory.addNewAmmunition("ANTI_TANK_ROCKET_105MM");
        weaponFactory=new WeaponFactory();                       
        weaponFactory.addNewWeapon("PISTOL_9MM",true,8,ammunitionFactory.getAmmunition("BULLET_9MM"),1);
        weaponFactory.addNewWeapon("PISTOL_10MM",true,10,ammunitionFactory.getAmmunition("BULLET_10MM"),1);
        weaponFactory.addNewWeapon("MAG_60",true,30,ammunitionFactory.getAmmunition("BULLET_9MM"),1);
        weaponFactory.addNewWeapon("UZI",true,20,ammunitionFactory.getAmmunition("BULLET_9MM"),1);
        weaponFactory.addNewWeapon("SMACH",false,35,ammunitionFactory.getAmmunition("BULLET_5_56MM"),1);
        weaponFactory.addNewWeapon("LASER",true,15,ammunitionFactory.getAmmunition("ENERGY"),1);
        weaponFactory.addNewWeapon("SHOTGUN",false,3,ammunitionFactory.getAmmunition("CARTRIDGE"),1);
        readCollisionMap();
        this.canvas=canvas;
        final Camera cam=canvas.getCanvasRenderer().getCamera();
        // create a node that follows the camera
        playerNode=new CameraNode("player",cam);
        playerData=new PlayerData(playerNode,ammunitionFactory,weaponFactory);
        this.previousCamLocation=new Vector3(cam.getLocation());
        this.currentCamLocation=new Vector3();
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
				playerData.selectNextWeapon();
			}
		};
		final TriggerAction previousWeaponAction=new TriggerAction(){		
			@Override
			public void perform(Canvas source, TwoInputStates inputState, double tpf){
				playerData.selectPreviousWeapon();
			}
		};
		final TriggerAction wheelWeaponAction=new TriggerAction(){		
			@Override
			public void perform(Canvas source, TwoInputStates inputState, double tpf){
				//if the mouse wheel has been rotated up/away from the user
				if(inputState.getCurrent().getMouseState().getDwheel()<0)
				    playerData.selectNextWeapon();
				else
					//otherwise the mouse wheel has been rotated down/towards the user
					playerData.selectPreviousWeapon();
			}
		};
		final TriggerAction reloadWeaponAction=new TriggerAction(){
			@Override
			public void perform(Canvas source, TwoInputStates inputState, double tpf){
				playerData.reload();
			}
		};
		final TriggerAction attackAction=new TriggerAction(){
			@Override
			public void perform(Canvas source, TwoInputStates inputState, double tpf){
				playerData.attack();
			}
		};
		final TriggerAction pauseAction=new TriggerAction(){
			@Override
			public void perform(Canvas source, TwoInputStates inputState, double tpf){
				//TODO: pause
			}
		};
		final TriggerAction crouchAction=new TriggerAction(){
			@Override
			public void perform(Canvas source, TwoInputStates inputState, double tpf){
				//TODO: crouch down
			}
		};
		final TriggerAction activateAction=new TriggerAction(){
			@Override
			public void perform(Canvas source, TwoInputStates inputState, double tpf){
				//TODO: activate
			}
		};
		final TriggerAction startRunningAction=new TriggerAction(){
			@Override
			public void perform(Canvas source, TwoInputStates inputState, double tpf){
				//TODO: start running
			}
		};
		final TriggerAction stopRunningAction=new TriggerAction(){
			@Override
			public void perform(Canvas source, TwoInputStates inputState, double tpf){
				//TODO: stop running
			}
		};
		final TriggerAction selectWeaponOneAction=new TriggerAction(){
			@Override
			public void perform(Canvas source, TwoInputStates inputState, double tpf){
				playerData.selectWeapon(0,false);
			}
		};
        //add some triggers to change weapon, reload and shoot
		final InputTrigger weaponMouseWheelTrigger=new InputTrigger(new MouseWheelMovedCondition(),wheelWeaponAction);
        final InputTrigger nextWeaponTrigger=new InputTrigger(new KeyReleasedCondition(Key.L),nextWeaponAction);
        final InputTrigger previousWeaponTrigger=new InputTrigger(new KeyReleasedCondition(Key.M),previousWeaponAction);
        final InputTrigger reloadWeaponTrigger=new InputTrigger(new KeyReleasedCondition(Key.R),reloadWeaponAction);
        final InputTrigger reloadWeaponMouseButtonTrigger=new InputTrigger(new MouseButtonReleasedCondition(MouseButton.RIGHT),reloadWeaponAction);
        final InputTrigger attackTrigger=new InputTrigger(new KeyReleasedCondition(Key.SPACE),attackAction);
        final InputTrigger attackMouseButtonTrigger=new InputTrigger(new MouseButtonReleasedCondition(MouseButton.LEFT),attackAction);
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
        		reloadWeaponMouseButtonTrigger,attackTrigger,attackMouseButtonTrigger,pauseTrigger,crouchTrigger,
        		activateTrigger,startRunningRightTrigger,stopRunningRightTrigger,startRunningLeftTrigger,
        		stopRunningLeftTrigger,selectWeaponOneTrigger};
        getLogicalLayer().registerInput(canvas,physicalLayer);
        for(InputTrigger trigger:triggers)
            getLogicalLayer().registerTrigger(trigger);
        ammoTextLabel=BasicText.createDefaultTextLabel("ammo display","");
        ammoTextLabel.setTranslation(new Vector3(0,80,0));
        ammoTextLabel.addController(new SpatialController<Spatial>(){
            @Override
            public final void update(double time,Spatial caller){
            	if(playerData.isCurrentWeaponAmmunitionCountDisplayable())
            		ammoTextLabel.setText(playerData.getAmmunitionCountInLeftHandedWeapon()+" "+
            				playerData.getAmmunitionCountInRightHandedWeapon()+" "+
            				playerData.getAmmunitionCountInContainer());
            	else
            		ammoTextLabel.setText("N/A");
            }           
        });
        fpsTextLabel=BasicText.createDefaultTextLabel("FPS display","");
        fpsTextLabel.setTranslation(new Vector3(0,20,0));
        fpsTextLabel.addController(new SpatialController<Spatial>(){
            @Override
            public final void update(double time,Spatial caller){
                fpsTextLabel.setText(" "+Math.round(time>0?1/time:0)+" FPS");
            }           
        });
        healthTextLabel=BasicText.createDefaultTextLabel("health display","");
        healthTextLabel.setTranslation(new Vector3(0,60,0));
        healthTextLabel.addController(new SpatialController<Spatial>(){
            @Override
            public final void update(double time,Spatial caller){
            	healthTextLabel.setText("HEALTH: "+playerData.getHealth());
            }           
        });
        headUpDisplayLabel=BasicText.createDefaultTextLabel("Head-up display","");
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
        // configure the collision system
        CollisionTreeManager.getInstance().setTreeType(CollisionTree.Type.AABB);
        final CollisionResults collisionResults=new BoundingCollisionResults();
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
        collectibleObjectsList=new ArrayList<Node>();
        teleportersList=new ArrayList<Node>();
        playerNode.addController(new SpatialController<Spatial>(){
        	
        	private Vector3 previousPosition=new Vector3(115,0.5,223);
        	
        	private boolean wasBeingTeleported=false;
        	
            @Override
            public void update(double timeSinceLastCall,Spatial caller){
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
                Node collectible;
                for(int i=collectibleObjectsList.size()-1;i>=0;i--)
                    {collectible=collectibleObjectsList.get(i);
                	 PickingUtil.findCollisions(collectible,playerNode,collisionResults);
                	 if(collisionResults.getNumber()>0)
                	     {//tries to collect the object (update the player model (MVC))
                		  //if it succeeds, detach the object from the root later
                		  if(playerData.collect(collectible)>0)
                	          {//remove it from the list of collectible objects
                			   collectibleObjectsList.remove(i);
                			   if(collectible.getParent()!=null)
                				   //detach this object from its parent so that it is no more visible
                				   collectible.getParent().detachChild(collectible);
                			   //display a message when the player picked up something
                	           headUpDisplayLabel.setText("picked up "+collectible.getName());
                	           CollectibleUserData collectibleUserData=(CollectibleUserData)collectible.getUserData();
                	           //play a sound if available
                	           if(collectibleUserData.getSourcename()!=null)
                                   getSoundManager().play(collectibleUserData.getSourcename());
                	          }
                	     }
                	 collisionResults.clear();
                    }
                //checks if any teleporter is used
                Node teleporter;
                boolean hasCollision=false;
                for(int i=teleportersList.size()-1;i>=0&&!hasCollision;i--)
                    {teleporter=teleportersList.get(i);
                     PickingUtil.findCollisions(teleporter,playerNode,collisionResults);
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
                               {//the players enters a teleporter                        	    
                      		    wasBeingTeleported=true;
                      		    Vector3 teleporterDestination=((TeleporterUserData)teleporter.getUserData()).getDestination();
                      		    //then move him
                      		    playerNode.setTranslation(teleporterDestination);
                      		    //updates the previous location to avoid any problem when detecting the collisions
                                previousPosition.set(teleporterDestination);
                                //synchronizes the camera with the camera node
                                cam.setLocation(teleporterDestination);
                                //play a sound if available
                 	            if(teleporterUseSourcename!=null)
                                    getSoundManager().play(teleporterUseSourcename);
                	           }
                	      }                          
                    }
                //if the players is not on any teleporter
                if(!hasCollision)
                	wasBeingTeleported=false;
            }           
        });
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
    
    @Override
    public final void init(){
    	// load the sound
        URL sampleUrl=GameState.class.getResource(pickupWeaponSoundSamplePath);
        if(sampleUrl!=null&&pickupWeaponSourcename==null)
            pickupWeaponSourcename=getSoundManager().preloadSoundSample(sampleUrl,true);                     
        else
        	pickupWeaponSourcename=null;           
        pickupAmmoSourcename=pickupWeaponSourcename;
        sampleUrl=GameState.class.getResource(teleporterUseSoundSamplePath);
        if(sampleUrl!=null&&teleporterUseSourcename==null)
        	teleporterUseSourcename=getSoundManager().preloadSoundSample(sampleUrl,true);
        else
        	teleporterUseSourcename=null;
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
        // Load level model
        try {final Node levelNode=(Node)BinaryImporter.getInstance().load(getClass().getResource("/abin/LID"+levelIndex+".abin"));
             NodeHelper.setBackCullState(levelNode);
             getRoot().attachChild(levelNode);
             final Node outdoorPartLevelNode=(Node)BinaryImporter.getInstance().load(getClass().getResource("/abin/wildhouse_action.abin"));
             outdoorPartLevelNode.setTranslation(-128, -6, -128);
             getRoot().attachChild(outdoorPartLevelNode);
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
             final Node teleporterNode=new Node("a teleporter");
             final Box teleporterBox=new Box("a teleporter",new Vector3(0,0,0),0.5,0.05,0.5);
             teleporterBox.setRandomColors();
             teleporterNode.setTranslation(112.5,0,221.5);
             teleporterNode.attachChild(teleporterBox);
             teleporterNode.setUserData(new TeleporterUserData(new Vector3(-132,0.5,-102)));
             teleportersList.add(teleporterNode);
             getRoot().attachChild(teleporterNode);            
             final Node secondTeleporterNode=new Node("another teleporter");
             final Box secondTeleporterBox=new Box("another teleporter",new Vector3(0,0,0),0.5,0.05,0.5);
             secondTeleporterBox.setRandomColors();
             secondTeleporterNode.setTranslation(-132,0,-102);
             secondTeleporterNode.attachChild(secondTeleporterBox);
             secondTeleporterNode.setUserData(new TeleporterUserData(new Vector3(112.5,0.5,221.5)));
             teleportersList.add(secondTeleporterNode);
             getRoot().attachChild(secondTeleporterNode);            
             //only to test the medikit
             //playerData.decreaseHealth(10);
             final Node medikitNode=new Node("a medikit");
             final Box medikitBox=new Box("a medikit",new Vector3(0,0,0),0.1,0.1,0.1);
             final TextureState ts = new TextureState();
             ts.setTexture(TextureManager.load(new URLResourceSource(getClass().getResource("/images/medikit.png")),Texture.MinificationFilter.Trilinear,true));
             medikitBox.setRenderState(ts);
             medikitNode.setTranslation(112.5,0.1,220.5);
             medikitNode.attachChild(medikitBox);
             medikitNode.setUserData(new MedikitUserData(20));
             collectibleObjectsList.add(medikitNode);
             getRoot().attachChild(medikitNode);
             final Node uziNode=(Node)BinaryImporter.getInstance().load(getClass().getResource("/abin/uzi.abin"));
             uziNode.setName("an uzi");
             uziNode.setTranslation(111.5,0.15,219);
             uziNode.setScale(0.2);
             uziNode.setUserData(new WeaponUserData(pickupWeaponSourcename,weaponFactory.getWeapon("UZI"),new Matrix3(uziNode.getRotation()),PlayerData.NO_UID,false));
             //add some bounding boxes for all objects that can be picked up
             collectibleObjectsList.add(uziNode);
             getRoot().attachChild(uziNode);
             final Node smachNode=(Node)BinaryImporter.getInstance().load(getClass().getResource("/abin/smach.abin"));
             smachNode.setName("a smach");
             smachNode.setTranslation(112.5,0.15,219);
             smachNode.setScale(0.2);
             smachNode.setUserData(new WeaponUserData(pickupWeaponSourcename,weaponFactory.getWeapon("SMACH"),new Matrix3(smachNode.getRotation()),PlayerData.NO_UID,false));
             collectibleObjectsList.add(smachNode);
             getRoot().attachChild(smachNode);
             final Node pistolNode=(Node)BinaryImporter.getInstance().load(getClass().getResource("/abin/pistol.abin"));
             pistolNode.setName("a pistol (10mm)");
             pistolNode.setTranslation(113.5,0.1,219);
             pistolNode.setScale(0.001);
             pistolNode.setRotation(new Quaternion().fromEulerAngles(Math.PI/2,-Math.PI/4,Math.PI/2));
             pistolNode.setUserData(new WeaponUserData(pickupWeaponSourcename,weaponFactory.getWeapon("PISTOL_10MM"),new Matrix3(pistolNode.getRotation()),PlayerData.NO_UID,false));
             collectibleObjectsList.add(pistolNode);
             getRoot().attachChild(pistolNode);            
             final Node duplicatePistolNode=pistolNode.makeCopy(false);
             duplicatePistolNode.setUserData(new WeaponUserData(pickupWeaponSourcename,weaponFactory.getWeapon("PISTOL_10MM"),new Matrix3(pistolNode.getRotation()),PlayerData.NO_UID,false));
             duplicatePistolNode.setTranslation(113.5,0.1,217);
             collectibleObjectsList.add(duplicatePistolNode);
             getRoot().attachChild(duplicatePistolNode);
             final Node pistol2Node=(Node)BinaryImporter.getInstance().load(getClass().getResource("/abin/pistol2.abin"));
             pistol2Node.setName("a pistol (9mm)");
             //remove the bullet as it is not necessary now
             ((Node)pistol2Node.getChild(0)).detachChildAt(2);
             pistol2Node.setTranslation(114.5,0.1,219);
             pistol2Node.setScale(0.02);
             pistol2Node.setRotation(new Quaternion().fromAngleAxis(-Math.PI/2,new Vector3(1,0,0)));
             pistol2Node.setUserData(new WeaponUserData(pickupWeaponSourcename,weaponFactory.getWeapon("PISTOL_9MM"),new Matrix3(pistol2Node.getRotation()),PlayerData.NO_UID,false));
             collectibleObjectsList.add(pistol2Node);
             getRoot().attachChild(pistol2Node);
             final Node pistol3Node=(Node)BinaryImporter.getInstance().load(getClass().getResource("/abin/pistol3.abin"));
             pistol3Node.setName("a Mag 60");
             pistol3Node.setTranslation(115.5,0.1,219);
             pistol3Node.setScale(0.02);
             pistol3Node.setUserData(new WeaponUserData(pickupWeaponSourcename,weaponFactory.getWeapon("MAG_60"),new Matrix3(pistol3Node.getRotation()),PlayerData.NO_UID,false));
             collectibleObjectsList.add(pistol3Node);
             getRoot().attachChild(pistol3Node);
             final Node laserNode=(Node)BinaryImporter.getInstance().load(getClass().getResource("/abin/laser.abin"));
             laserNode.setName("a laser");
             laserNode.setTranslation(116.5,0.1,219);
             laserNode.setScale(0.02);
             laserNode.setUserData(new WeaponUserData(pickupWeaponSourcename,weaponFactory.getWeapon("LASER"),new Matrix3(laserNode.getRotation()),PlayerData.NO_UID,false));
             collectibleObjectsList.add(laserNode);
             getRoot().attachChild(laserNode);
             
             final Node shotgunNode=(Node)BinaryImporter.getInstance().load(getClass().getResource("/abin/shotgun.abin"));
             shotgunNode.setName("a shotgun");
             shotgunNode.setTranslation(117.5,0.1,219);
             shotgunNode.setScale(0.1);
             shotgunNode.setUserData(new WeaponUserData(pickupWeaponSourcename,weaponFactory.getWeapon("SHOTGUN"),new Matrix3(shotgunNode.getRotation()),PlayerData.NO_UID,false));
             collectibleObjectsList.add(shotgunNode);
             getRoot().attachChild(shotgunNode);
             
             final Mesh agentNode=(Mesh)BinaryImporter.getInstance().load(getClass().getResource("/abin/agent.abin"));
             agentNode.setName("an agent");
             agentNode.setTranslation(118.5,0.4,219);
             agentNode.setRotation(new Quaternion().fromAngleAxis(-Math.PI/2,new Vector3(1,0,0)));            
             agentNode.setScale(0.015);
             getRoot().attachChild(agentNode);
             
             //TODO: uncomment these lines when we have a texture for this enemy
             /*final Node creatureNode=(Node)BinaryImporter.getInstance().load(getClass().getResource("/abin/creature.abin"));
             creatureNode.setTranslation(118.5,0.7,217);
             creatureNode.setScale(0.0002);
             creatureNode.setRotation(new Quaternion().fromAngleAxis(-Math.PI/2,new Vector3(1,0,0)));
             getRoot().attachChild(creatureNode);*/          
             //add a bounding box to each collectible object
             for(Node collectible:collectibleObjectsList)
            	 NodeHelper.setModelBound(collectible,BoundingBox.class);
             for(Node teleporter:teleportersList)
            	 NodeHelper.setModelBound(teleporter,BoundingBox.class);
            }
        catch(final Exception ex)
        {ex.printStackTrace();}
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
                  cam.setFrustumPerspective(cam.getFovY(),(float)cam.getWidth()/(float)cam.getHeight(),0.2,200);
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
