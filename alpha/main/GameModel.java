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

/**
 * This class performs the main tasks in the game. 
 *
 *@author Julien Gouesse, Vincent Stahl 
 */
/*
         TUER engine (tuer.tuxfamily.org) inspired from:
         
          d3caster, a 3-D java raycasting game engine 
         =============================================
         rel. 1.1.0, Vincent Stahl, www.stahlforce.com

         OPTIMIZED BY JULIEN GOUESSE (no more raycasting, pure power!!!!!)
     
         requires at least Java 1.6 and JOGL 1.1.0
*/

package main;

import java.awt.geom.Rectangle2D;
import java.beans.XMLDecoder;
import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.nio.FloatBuffer;
import java.rmi.RemoteException;
//import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
//import java.util.BitSet;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

import com.jogamp.common.nio.Buffers;

import tools.GameIO;
import tools.NetworkSet;

public class GameModel{
    
    
    private static final long serialVersionUID = 1L;

    private List<GameInfoMessage> gameInfoMessageList;
    
    private GameController gameController;
    
    private NetworkSet networkSet;
    
    private List<BotModel> botList;
    
    private List<Impact> impactList;
    
    private List<ExplosionModel> explosionList;
    
    private List<HealthPowerUpModel> healthPowerUpModelList;
    
    private List<HealthPowerUpModel> initialHealthPowerUpModelList;
    
    private HashMap<Integer,float[]> rocketTable;
    
    //TODO: use a "rocket" class
    private List<float[]> rocketList;
    
    private Clock internalClock;
    
    private float framerateCompensationFactor;

    private static final double hitrange=0.3d;
    
    private static final double minimalRocketLaunchDistance=1.5*hitrange;
    
    private static final double rockrange=0.1d;  
    
    /**
     * size of the edge of the "square" map
     */
    private static final int mapEdgeSize=256;
    
    /**
     * size of the map: voxel count
     */
    static final int mapSize = mapEdgeSize * mapEdgeSize;
    
    private FloatBuffer artCoordinatesBuffer1;//coordinates of the works of art
    
    private FloatBuffer artCoordinatesBuffer2;
    
    private FloatBuffer artCoordinatesBuffer3;
    
    private FloatBuffer artCoordinatesBuffer4;
    
    private FloatBuffer botCoordinatesBuffer;//coordinates of a bot
    
    private FloatBuffer unbreakableObjectCoordinatesBuffer;//coordinates of a unbreakable object
    
    private FloatBuffer vendingMachineCoordinatesBuffer;//coordinates of a vending machine
    
    private FloatBuffer lampCoordinatesBuffer;
    
    private FloatBuffer chairCoordinatesBuffer;
    
    private FloatBuffer flowerCoordinatesBuffer;
    
    private FloatBuffer tableCoordinatesBuffer;
    
    private FloatBuffer bonsaiCoordinatesBuffer;  
    
    private FloatBuffer rocketLauncherCoordinatesBuffer;
    
    private FloatBuffer rocketCoordinatesBuffer;
    
    private FloatBuffer impactCoordinatesBuffer;
    
    private FloatBuffer crosshairCoordinatesBuffer;
    
    private boolean lookingDown;
    
    private boolean lookingUp;
    
    private byte[] collisionMap;
    
    private byte[] initialCollisionMap;
         
    //TODO : put it in a "weapon" class
    private long lastShot;//time of last shot of rocket
    //TODO : put it in a "weapon" class
    /**
     * DIRTY: position of the rocket launcher
     */
    private float[] rocketLauncherPos;
    
    private final static long timeBetweenShots=500;
    
    private boolean gameRunning=false;
    
    private int initialPositionX;
    
    private int initialPositionZ;
    
    private boolean bcheat  = false;

    // basic setup
    private static final double fullCircle = Math.PI*2;
    private static final int numLoWallImages  = 27; // 240804: 20
    private static final int numHiWallImages  = numLoWallImages; // MUST be identical

    // object handling
    private static final int maxBots=200;
    private static final int maxBushes=800;
    private static final int maxDeko=450;
    private static final int numObjects=100+maxBots+maxBushes+maxDeko;
    private static final int ShapeRocket=0; // must add iobjtext for atext[] index!
    private static final int ShapeBot   =2;
    private static final int ShapeBush  =3;
    private static final int ShapeDeko  =4;

    private d3object[] object;

    private static final int IndexPlayerRockets=0;  // plr rockets are on index 0 to 9
    private static final int IndexBotRockets=10;    // bot rockets are on index 10 to 19
    private static final int IndexBots=20;          // bots are on index 20 to 119 (see maxBots)
    //bushes i.e obstacles?
    private static final int IndexBushes=IndexBots+maxBots; // bushes are on index 120 ff.
    private static final int IndexDeko=IndexBushes+maxBushes;
    
    // player handling
    private PlayerModel player;    
    
    private boolean innerLoop;

    private boolean bpause;
    
    private boolean isFalling;//when dying
    
    private long fallStart;
    
    private static final int fallTotalDuration=3000;//in millisecond
    
    private volatile boolean runningForward;
    private volatile boolean runningBackward;
    private volatile boolean rightStepping;
    private volatile boolean leftStepping;
    private volatile boolean playerMoving;
    private volatile boolean turningLeft;
    private volatile boolean turningRight;
    private volatile boolean runningFast;
    private int[] mapData; 
    private int nClBotsWalking = 0;
  
    private long lastBotShotTime;//latency for the bots
    
    private static final int EMPTY=0;
    
    private static final int FIXED_AND_BREAKABLE_CHAIR=1;
    
    private static final int FIXED_AND_BREAKABLE_LIGHT=2;
    
    private static final int MOVING_AND_BREAKABLE=3;
    
    private static final int AVOIDABLE_AND_UNBREAKABLE=4;/*
    
    private static final int UNAVOIDABLE_AND_UNBREAKABLE=5;
    
    private static final int UNAVOIDABLE_AND_UNBREAKABLE_DIRTY=6;*/
    
    private static final int FIXED_AND_BREAKABLE_BIG=7;
    
    private static final int FIXED_AND_BREAKABLE_FLOWER=8;
    
    private static final int FIXED_AND_BREAKABLE_TABLE=9;
    
    private static final int FIXED_AND_BREAKABLE_BONSAI=10;
    
    private static final int UNAVOIDABLE_AND_UNBREAKABLE_DOWN=11;
    
    private static final int UNAVOIDABLE_AND_UNBREAKABLE_LEFT=12;
    
    private static final int UNAVOIDABLE_AND_UNBREAKABLE_RIGHT=13;
    
    private static final int UNAVOIDABLE_AND_UNBREAKABLE_UP_DOWN=14;
    
    private static final int UNAVOIDABLE_AND_UNBREAKABLE_UP_LEFT=15;
    
    private static final int UNAVOIDABLE_AND_UNBREAKABLE_UP_RIGHT=16;
    
    private static final int UNAVOIDABLE_AND_UNBREAKABLE_UP_DOWN_LEFT=17;
    
    private static final int UNAVOIDABLE_AND_UNBREAKABLE_UP_DOWN_RIGHT=18;
    
    private static final int UNAVOIDABLE_AND_UNBREAKABLE_UP_LEFT_RIGHT=19;
               
    private static final int UNAVOIDABLE_AND_UNBREAKABLE_UP_DOWN_LEFT_RIGHT=20;
       
    private static final int UNAVOIDABLE_AND_UNBREAKABLE_DOWN_LEFT=21;
    
    private static final int UNAVOIDABLE_AND_UNBREAKABLE_DOWN_RIGHT=22;
    
    private static final int UNAVOIDABLE_AND_UNBREAKABLE_DOWN_LEFT_RIGHT=23;
    
    private static final int UNAVOIDABLE_AND_UNBREAKABLE_LEFT_RIGHT=24;
    
    private static final int UNAVOIDABLE_AND_UNBREAKABLE_UP=25;
    
    //dirty walls
    /*private static final int UNAVOIDABLE_AND_UNBREAKABLE_DOWN_DIRTY=26;
    
    private static final int UNAVOIDABLE_AND_UNBREAKABLE_LEFT_DIRTY=27;
    
    private static final int UNAVOIDABLE_AND_UNBREAKABLE_RIGHT_DIRTY=28;
    
    private static final int UNAVOIDABLE_AND_UNBREAKABLE_UP_DOWN_DIRTY=29;
    
    private static final int UNAVOIDABLE_AND_UNBREAKABLE_UP_LEFT_DIRTY=30;
    
    private static final int UNAVOIDABLE_AND_UNBREAKABLE_UP_RIGHT_DIRTY=31;
    
    private static final int UNAVOIDABLE_AND_UNBREAKABLE_UP_DOWN_LEFT_DIRTY=32;
    
    private static final int UNAVOIDABLE_AND_UNBREAKABLE_UP_DOWN_RIGHT_DIRTY=33;
    
    private static final int UNAVOIDABLE_AND_UNBREAKABLE_UP_LEFT_RIGHT_DIRTY=34;
               
    private static final int UNAVOIDABLE_AND_UNBREAKABLE_UP_DOWN_LEFT_RIGHT_DIRTY=35;
       
    private static final int UNAVOIDABLE_AND_UNBREAKABLE_DOWN_LEFT_DIRTY=36;
    
    private static final int UNAVOIDABLE_AND_UNBREAKABLE_DOWN_RIGHT_DIRTY=37;
    
    private static final int UNAVOIDABLE_AND_UNBREAKABLE_DOWN_LEFT_RIGHT_DIRTY=38;
    
    private static final int UNAVOIDABLE_AND_UNBREAKABLE_LEFT_RIGHT_DIRTY=39;
    
    private static final int UNAVOIDABLE_AND_UNBREAKABLE_UP_DIRTY=40;*/
    
               
    @SuppressWarnings("unchecked")
    GameModel(GameController gameController)throws RuntimeException,RemoteException{
        this.gameInfoMessageList=new ArrayList<GameInfoMessage>();
        this.gameController=gameController;
        this.collisionMap=new byte[mapSize];
        this.botList=new Vector<BotModel>();
        this.impactList=new Vector<Impact>();
        this.rocketList=new Vector<float[]>();
        this.explosionList=new Vector<ExplosionModel>();
        this.healthPowerUpModelList=new Vector<HealthPowerUpModel>();
        this.initialHealthPowerUpModelList=null;
        this.internalClock=new Clock();
        this.player=new PlayerModel(internalClock);
        this.rocketTable=new HashMap<Integer,float[]>();
        this.isFalling=false;
        this.rocketLauncherPos=new float[4];
        //decode XML items to fill the initial health power up list
        BufferedInputStream bis=null;
        Vector<HealthPowerUpModelBean> beanList=null;
        try{bis=new BufferedInputStream(getClass().getResourceAsStream("/xml/itemList.xml"));
            XMLDecoder decoder = new XMLDecoder(bis);
            beanList=(Vector<HealthPowerUpModelBean>)decoder.readObject();        
            decoder.close();
           } 
        catch(Throwable t)
        {throw new RuntimeException("Unable to decode XML file",t);}
        this.initialHealthPowerUpModelList=new Vector<HealthPowerUpModel>();        
        try{DataInputStream in=new DataInputStream(new BufferedInputStream(getClass().getResourceAsStream("/data/worldmap.data")));
            int i,artWorksCount1,artWorksCount2,artWorksCount3,artWorksCount4;                                           
            //read the data for the works of art
            artWorksCount1=in.readInt();
            artWorksCount2=in.readInt();
            artWorksCount3=in.readInt();
            artWorksCount4=in.readInt();
            //for each point : 2 levelTexture coordinates + 3 vertex coordinates
            final int floatPerPrimitive=5;
            artCoordinatesBuffer1=Buffers.newDirectFloatBuffer(artWorksCount1*floatPerPrimitive);
            for(i=0;i<artWorksCount1;i++)
                {artCoordinatesBuffer1.put(in.readFloat());
                 artCoordinatesBuffer1.put(in.readFloat());
                 //artCoordinatesBuffer1.put(in.readFloat());
                 //artCoordinatesBuffer1.put(in.readFloat());
                 //artCoordinatesBuffer1.put(in.readFloat());
                 artCoordinatesBuffer1.put(in.readFloat());
                 artCoordinatesBuffer1.put(in.readFloat());
                 artCoordinatesBuffer1.put(in.readFloat());
                }
            artCoordinatesBuffer1.rewind();        
            artCoordinatesBuffer2=Buffers.newDirectFloatBuffer(artWorksCount2*floatPerPrimitive);
            for(i=0;i<artWorksCount2;i++)
                {artCoordinatesBuffer2.put(in.readFloat());
                 artCoordinatesBuffer2.put(in.readFloat());
                 //artCoordinatesBuffer2.put(in.readFloat());
                 //artCoordinatesBuffer2.put(in.readFloat());
                 //artCoordinatesBuffer2.put(in.readFloat());
                 artCoordinatesBuffer2.put(in.readFloat());
                 artCoordinatesBuffer2.put(in.readFloat());
                 artCoordinatesBuffer2.put(in.readFloat());
                }
            artCoordinatesBuffer2.rewind();            
            artCoordinatesBuffer3=Buffers.newDirectFloatBuffer(artWorksCount3*floatPerPrimitive);
            for(i=0;i<artWorksCount3;i++)
                {artCoordinatesBuffer3.put(in.readFloat());
                 artCoordinatesBuffer3.put(in.readFloat());
                 //artCoordinatesBuffer3.put(in.readFloat());
                 //artCoordinatesBuffer3.put(in.readFloat());
                 //artCoordinatesBuffer3.put(in.readFloat());
                 artCoordinatesBuffer3.put(in.readFloat());
                 artCoordinatesBuffer3.put(in.readFloat());
                 artCoordinatesBuffer3.put(in.readFloat());
                }
            artCoordinatesBuffer3.rewind();           
            artCoordinatesBuffer4=Buffers.newDirectFloatBuffer(artWorksCount4*floatPerPrimitive);
            for(i=0;i<artWorksCount4;i++)
                {artCoordinatesBuffer4.put(in.readFloat());
                 artCoordinatesBuffer4.put(in.readFloat());
                 //artCoordinatesBuffer4.put(in.readFloat());
                 //artCoordinatesBuffer4.put(in.readFloat());
                 //artCoordinatesBuffer4.put(in.readFloat());
                 artCoordinatesBuffer4.put(in.readFloat());
                 artCoordinatesBuffer4.put(in.readFloat());
                 artCoordinatesBuffer4.put(in.readFloat());
                }
            artCoordinatesBuffer4.rewind();           
            //read the collision map here
            in.read(this.collisionMap,0,mapSize);
            //read the initial position
            initialPositionX=in.readInt();
            initialPositionZ=in.readInt();  
            //initialize a copy of the collision map
            this.initialCollisionMap=new byte[this.collisionMap.length];             
            for(i=0;i<this.collisionMap.length;i++)
                {if(this.collisionMap[i]==MOVING_AND_BREAKABLE)
                     {//detect a bot and add it to the bot container
                      //+32768 -> put the bot at the center of the case
                      this.botList.add(new BotModel((i%mapEdgeSize)+0.5D,0,(i/mapEdgeSize)+0.5D));
                      this.collisionMap[i]=EMPTY;
                     }
                 //copy the collision map
                 this.initialCollisionMap[i]=this.collisionMap[i];
                }                                   
            in.close();              
            unbreakableObjectCoordinatesBuffer=GameIO.readGameFloatDataFile("/data/unbreakableObject.data");          
            vendingMachineCoordinatesBuffer=GameIO.readGameFloatDataFile("/data/vendingMachine.data");
            lampCoordinatesBuffer=GameIO.readGameFloatDataFile("/data/lamp.data");
            chairCoordinatesBuffer=GameIO.readGameFloatDataFile("/data/chair.data");
            flowerCoordinatesBuffer=GameIO.readGameFloatDataFile("/data/flower.data");
            tableCoordinatesBuffer=GameIO.readGameFloatDataFile("/data/table.data");
            bonsaiCoordinatesBuffer=GameIO.readGameFloatDataFile("/data/bonsai.data");            
            botCoordinatesBuffer=GameIO.readGameFloatDataFile("/data/bot.data");
            rocketLauncherCoordinatesBuffer=GameIO.readGameFloatDataFile("/data/rocketLauncher.data");
            rocketCoordinatesBuffer=GameIO.readGameFloatDataFile("/data/rocket.data");
            impactCoordinatesBuffer=GameIO.readGameFloatDataFile("/data/impact.data");
            crosshairCoordinatesBuffer=GameIO.readGameFloatDataFile("/data/crosshair.data");
            //start the factories
            ExplosionModelFactory.getInstance();
            HealthPowerUpModelFactory.getInstance();
            //extract the items from the beans
            for(HealthPowerUpModelBean bean:beanList)
                {HealthPowerUpModel hpum=bean.getWrappedObject();
                 //apply the internal clock on each item
                 hpum.setInternalClock(internalClock);
                 this.initialHealthPowerUpModelList.add(hpum);      
                }
            //read the binary version of the world map built from the pixmap
            mapData=new int[mapSize];
            in=new DataInputStream(new BufferedInputStream(getClass().getResourceAsStream("/data/binaryWorldmap.data")));
            for(i=0;i<mapData.length;i++)
                mapData[i]=in.readInt();
            in.close();
           }
        catch(IOException ioe)
        {throw new RuntimeException("Unable to read the data files",ioe);}      
    }
    
    private final void loadNetworkSet(){
        //read a list of networks
        ObjectInputStream ois=null;
        try{ois=new ObjectInputStream(new BufferedInputStream(getClass().getResourceAsStream("/data/network.data")));
            networkSet=(NetworkSet)ois.readObject();
            ois.close();
           }
        catch(Throwable t)
        {throw new RuntimeException("Unable to read binary network file",t);}
    }
    
    final byte[] getInitialCollisionMap(){
        return(initialCollisionMap);
    }
    
    final byte[] getCollisionMap(){
        return(collisionMap);
    }
    
    public final byte getCollisionMap(int index){
        return(collisionMap[index]);
    }
    
    final void reinitializeCollisionMap(){
        for(int i=0;i<this.collisionMap.length;i++)          
            this.collisionMap[i]=this.initialCollisionMap[i];
    }
    
    final void reinitializeImpactList(){
        this.impactList.clear();
    }
    
    final void reinitializeRocketList(){
        this.rocketList.clear();
        this.rocketTable.clear();
    }
    
    final void reinitializeExplosions(){
        for(ExplosionModel em:explosionList)
            em.dispose();
        this.explosionList.clear();
    }
    
    final void reinitializeItems(){
        //TODO: fill the item lists       
        for(HealthPowerUpModel hpum:healthPowerUpModelList)
            hpum.dispose();
        //clear the list
        this.healthPowerUpModelList.clear();
        //fill the health power up list
        this.healthPowerUpModelList.addAll(this.initialHealthPowerUpModelList);
        //connect them
        for(HealthPowerUpModel hpum:healthPowerUpModelList)
            gameController.addNewItem(hpum);
    }
    
    final List<BotModel> getBotList(){
        return(botList);
    }
    
    final void respawnAllBots(){
        for(BotModel bot:botList)
            bot.respawn();       
    }
    
    public final boolean getBcheat(){
        return(bcheat);
    }
    
    public final FloatBuffer getArtCoordinatesBuffer1(){
        return(artCoordinatesBuffer1);
    }
    
    public final FloatBuffer getArtCoordinatesBuffer2(){
        return(artCoordinatesBuffer2);
    }
    
    public final FloatBuffer getArtCoordinatesBuffer3(){
        return(artCoordinatesBuffer3);
    }
    
    public final FloatBuffer getArtCoordinatesBuffer4(){
        return(artCoordinatesBuffer4);
    }
    
    public final FloatBuffer getBotCoordinatesBuffer(){
        return(botCoordinatesBuffer);
    }
       
    public final FloatBuffer getUnbreakableObjectCoordinatesBuffer(){
        return(unbreakableObjectCoordinatesBuffer);
    }         
       
    public final FloatBuffer getVendingMachineCoordinatesBuffer(){
        return(vendingMachineCoordinatesBuffer);
    }  
                       
    public final FloatBuffer getLampCoordinatesBuffer(){
        return(lampCoordinatesBuffer);
    }
    
    public final FloatBuffer getChairCoordinatesBuffer(){
        return(chairCoordinatesBuffer);
    }
    
    public final FloatBuffer getFlowerCoordinatesBuffer(){
        return(flowerCoordinatesBuffer);
    }
    
    public final FloatBuffer getTableCoordinatesBuffer(){
        return(tableCoordinatesBuffer);
    }
    
    public final FloatBuffer getBonsaiCoordinatesBuffer(){
        return(bonsaiCoordinatesBuffer);
    }
    
    public final FloatBuffer getRocketLauncherCoordinatesBuffer(){
        return(rocketLauncherCoordinatesBuffer);
    }
    
    public final FloatBuffer getRocketCoordinatesBuffer(){
        return(rocketCoordinatesBuffer);
    }
    
    public final FloatBuffer getImpactCoordinatesBuffer(){
        return(impactCoordinatesBuffer);
    }
    
    public final FloatBuffer getCrosshairCoordinatesBuffer(){
        return(crosshairCoordinatesBuffer);
    }
       
    public final boolean getPlayerWins(){
        return(player.isWinning());
    }
    
    final boolean getInnerLoop(){
        return(innerLoop);
    }         
    
    public final double getPlayerXpos(){
        return(player.getX());
    }
    
    public final double getPlayerYpos(){
        return(player.getY());
    }
    
    public final double getPlayerZpos(){
        return(player.getZ());
    }
    
    public final double getPlayerDirection(){
        return(player.getDirection());
    }
    
    public final int getHealth(){
        return(player.getHealth());
    }

    public final boolean getPlayerHit(){
        return(!player.isAlive());
    }
    
    public final boolean getBpause(){
        return(bpause);
    }
    
    public final boolean isGameRunning(){
        return(gameRunning);
    }
    
    public final void setBcheat(boolean bcheat){
        this.bcheat=bcheat;
    } 
    
    public final void setTurningLeft(boolean turningLeft){
        this.turningLeft=turningLeft;       
    }  
      
    public final void setTurningRight(boolean turningRight){
        this.turningRight=turningRight;       
    }
    
    public final void setRunningForward(boolean runningForward){
        this.runningForward=runningForward;
    }
    
    public final void setRunningBackward(boolean runningBackward){
        this.runningBackward=runningBackward;
    }
    
    public final void setRunningFast(boolean runningFast){
        this.runningFast=runningFast;
    }
    
    public final void setRightStepping(boolean rightStepping){
        this.rightStepping=rightStepping;
    }
    
    public final void setLeftStepping(boolean leftStepping){
        this.leftStepping=leftStepping;
    }
    
    public final void setBpause(boolean bpause){
        this.bpause=bpause;
    }
    
    final void setInnerLoop(boolean innerLoop){
        this.innerLoop=innerLoop;
    }  
    
    public final List<Impact> getImpactList(){
        return(impactList);
    }
    
    final Impact getImpact(int index){
        return(impactList.get(index));
    }
    
    final int getImpactsCount(){
        return(impactList.size());
    }
    
    public final List<float[]> getRocketList(){
        return(rocketList);
    }
    
    final NetworkSet getNetworkSet(){
        if(networkSet==null)
            loadNetworkSet();
        return(networkSet);   
    }
    
    public final void launchNewGame(){
        //reinitialize all the bots
        //treat the case of a complete respawn for all (new game)
        respawnAllBots();
        setInnerLoop(false);
        resetPlayerPosition();
        reinitializeCollisionMap();
        reinitializeImpactList();
        reinitializeRocketList();
        reinitializeExplosions();
        reinitializeItems();
        purgeGameInfoMessageList();
    }
    
    public final void resumeGame(){
        setBpause(false);
    }

    //TODO : do not use this method anymore
    private final void genMap(){
        int cx,cz;
        // analyze world map, build objects from it
        int ipix,ired,igrn,iblu,ioff,icode;
        byte ceilstate=0; // initial default: inside
        d3object obj;
        for(cz=0; cz<mapEdgeSize; cz++)
            for(cx=0; cx<mapEdgeSize; cx++)
                {ioff = cz*mapEdgeSize+cx;
                 ipix = mapData[ioff]&0xFFFFFF;
                 ired = (ipix>>16)&0xFF;
                 igrn = (ipix>> 8)&0xFF;
                 iblu = (ipix    )&0xFF;
                 if(ired==0xFF && igrn==0xFF && iblu==0)
                     {ceilstate = (byte)(1-ceilstate); // switch ceiling default
                      // replace yellow by blue (wall)
                      ired=igrn=0; iblu=0xFF;
                      // fall through
                     }
                 if((iblu==0xFF && ired==0xFF && igrn==0)||(ired==100 && igrn==100 && iblu==00)||(ired==0 && igrn==0 && iblu==0))
                     continue;                                    
                 if(ired<=200 && igrn==0 && iblu<=200 && (ired==iblu))
                     {// deko object handling
                      // allocate a new deko object
                      for(icode=IndexDeko; icode<IndexDeko+maxDeko; icode++)
                          if(object[icode].getShape()==-1)
                              break;
                      if(icode==IndexDeko+maxDeko)
                          {System.out.println("x25341353");
                           continue;
                          }
                      object[icode].setX(cx+0.5D);
                      object[icode].setZ(cz+0.5D);
                      object[icode].setShape(ShapeDeko);
                      object[icode].setSpeed(0);                 
                      continue;
                     }
                 if(ired==0 && igrn==0) 
                     {if(iblu==0xE0) 
                          {// bush, obstacle
                           // allocate a new bush
                           for(icode=IndexBushes; icode<IndexBushes+maxBushes; icode++)
                               if (object[icode].getShape()==-1)
                                   break;
                           if(icode==IndexBushes+maxBushes) 
                               {System.out.println("x20342010");
                                continue;
                               }
                           object[icode].setX(cx+0.5D);
                           object[icode].setZ(cz+0.5D);
                           object[icode].setShape(ShapeBush);  // alloc
                           object[icode].setSpeed(0);
                           // have to register bush location in "botmap",
                           // otherwise there would be a hole in the area.                           
                           continue;
                          }
                      if(iblu==0xFF || iblu==100 || iblu==101 || (iblu >= 0xA0 && iblu < 0xF0)) 
                          continue;                                             
                     }
                 if(ired==0xFF && igrn==0 && iblu==0)
                     continue;                     
                 // ----- AREA handling -----
                 if(ired==0 && iblu==0 && (igrn >=0xA0 && igrn <= 0xFE)) 
                     continue;                    
                 if(igrn==0 && iblu==0 && (ired >=0xA0 && ired <= 0xFE)) 
                     continue;  
                 if((ired==50 && igrn==50 && iblu==50)||(ired==100 && igrn==100 && iblu==100)||(ired==150 && igrn==150 && iblu==150)||(ired==255 && igrn==255 && iblu==255))
                     continue;  
                 if((ired==0 && igrn==0xFF && iblu==0xFF)
                         || (ired==0 && igrn==200  && iblu==200)) 
                     {// create bot, auto-detect surrounding area                     
                      if(withinAnArea(cx,cz)>=0) 
                          {// create bot
                           // allocate a new bot
                           for(icode=IndexBots; icode<IndexBots+maxBots; icode++)
                               if(object[icode].getShape()==-1)
                                   break;
                           if(icode==IndexBots+maxBots) 
                               {System.out.println("X20342033");
                                continue;
                               }
                           obj = object[icode];
                           obj.setX(cx+0.5D);
                           obj.setZ(cz+0.5D);                
                           obj.setShape(ShapeBot);   // alloc
                           obj.setSpeed(1.0f/65536.0f);       // pseudo                          
                          }
                      else // bot MUST be within an area
                          System.out.println("X1634234A");
                      continue;
                     }                                                
                }//endfor scan
    }

    // check the 8 pixels around cx,cy if we're standing
    // within or nearby an area. this is used just in genMap(),
    // which is not performance critical.
    private final int withinAnArea(int cx,int cy){
        int sx,sy,ipix2,ired2,iblu2,igrn2;
        if(cx>1 && cx<255 && cy>1 && cy<255)
            for(sx=-1;sx<=1;sx++)
                {for(sy=-1;sy<=1;sy++)
                    {ipix2 = mapData[(cy+sy)*mapEdgeSize+(cx+sx)];
                     ired2 = (ipix2>>16)&0xFF;
                     igrn2 = (ipix2>> 8)&0xFF;
                     iblu2 = (ipix2    )&0xFF;
                     if(igrn2 >= 0xA0 && igrn2 <= 0xFE
                            && ired2 == 0x00 && iblu2 == 0x00)
                         return 0xFE-igrn2;
                    }
                }
        return -1; // not within an area
    }
        
    private final void initialize(){
        if(numLoWallImages!=numHiWallImages)
            System.out.println("X18341622");             
        System.out.println("allocating maps");   
        System.out.println("allocating rest");  
        object=new d3object[numObjects];
        for(int counter=0; counter<numObjects; counter++)
            object[counter] = new d3object();               
    }

    final void reinit(boolean playerHasBeenKilled){
       if(!playerHasBeenKilled)
           player.respawn();
       //respawn him later if he has been killed
       player.setAsLoser();
       bpause=false;
       // however, this is overridden in genMap().
       resetPlayerPosition();
       System.out.println("reinit call");
       // by default, all object slots are passive.
       for(int counter=0; counter<numObjects; counter++) 
           {object[counter].setZ(0);
            object[counter].setX(0);
            object[counter].setDir(0);
            object[counter].setSpeed(0);           
            object[counter].setShape(-1);
            object[counter].setSleep2(0);
            object[counter].setSeenPlayer(false);
           }       
       // init the floor texture with large random patches,
       // to make it look a bit more interesting.         
       genMap();
       //respawn the bots
       for(BotModel bot:botList)       
           bot.respawn();          
       lookingDown=false;
       lookingUp=false;
       rightStepping=false;
       leftStepping=false;
       turningLeft=false;
       turningRight=false;
       runningForward=false;
       runningBackward=false;
       playerMoving=false;
       runningFast=false;

       gameController.stopMovingSound(0xFFFF); // in case it's still playing
       gameController.initBotwalkSound();

       gameController.restartMusic();    // i.e. initial background track
       //FIXME: put a better mechanism to respawn within an area
       //FIXME: maybe this call is now useless as it is done before
       resetPlayerPosition();       
    }

    final void resetPlayerPosition(){
        //if the user wants to start a new game, 
        //it reinitializes the respawn position
        player.setX(initialPositionX+0.5D);
        player.setY(0);
        player.setZ(initialPositionZ+0.5D);
        player.setDirection(fullCircle/2);
        updateRocketLauncherPositionFromPlayerPosition();
    }
    
    private final void updateRocketLauncherPositionFromPlayerPosition(){
        //TODO: put it closer to the player (0.25,0.5)
        rocketLauncherPos[0]=(float)(player.getX()+(Math.sin(player.getDirection())*(player.getBoundingSize()*/*0.0625f*/-0.125f))+Math.sin(player.getDirection()-fullCircle/4)*(player.getBoundingSize()*/*0.125f*/0.5f));
        rocketLauncherPos[1]=-1.0f/8.0f;
        rocketLauncherPos[2]=(float)(player.getZ()+(Math.cos(player.getDirection())*(player.getBoundingSize()*/*0.0625f*/-0.125f))+Math.cos(player.getDirection()-fullCircle/4)*(player.getBoundingSize()*/*0.125f*/0.5f));
        rocketLauncherPos[3]=(float)(player.getDirection()*(180/Math.PI));
    }

    public final long currentTime(){
        if(internalClock!=null && !internalClock.hasNotYetStarted())
            return(internalClock.getElapsedTime());
        else
            return(0L);
    }

    private final void turnLeft(long cycleDuration){       
        double turnspeed;
        if(runningFast) 
            turnspeed=2976;
        else
            turnspeed=5952;       
        player.setDirection(player.getDirection()+((fullCircle*cycleDuration)/turnspeed));
        if(player.getDirection()>=fullCircle)
            player.setDirection(player.getDirection()-fullCircle);       
    }
    
    private final void turnRight(long cycleDuration){       
        double turnspeed;
        if(runningFast) 
            turnspeed=2976;
        else
            turnspeed=5952;       
        player.setDirection(player.getDirection()-((fullCircle*cycleDuration)/turnspeed));
        if(player.getDirection()<0.0)
            player.setDirection(player.getDirection()+fullCircle);
    }

    private final void lookDown(){}
    
    private final void lookUp(){}

    public final void runEngine(){
        lastBotShotTime=currentTime();
        initialize();  // load stuff, raw setup
        gameRunning=true;
        innerLoop=false;
        boolean hasBeenKilled=false;
        Rectangle2D.Float rWallVoxel = new Rectangle2D.Float();
        Rectangle2D.Float rPlayerVoxel = new Rectangle2D.Float();
        final float playerSize=1.0f/4.0f;
        final float wallSize=1.0f;
        rPlayerVoxel.width=playerSize;
        rPlayerVoxel.height=playerSize;
        rWallVoxel.width=wallSize;
        rWallVoxel.height=wallSize;       
        boolean playMenuMusicOnce=true;
        //loop until the player tries to exit the game     
        while(gameRunning)
            {reinit(hasBeenKilled);     // re-set all positions            
             while(gameController.getCycle()!=GameCycle.GAME && gameRunning)
                 {if(playMenuMusicOnce)
                      {gameController.playSound(15);
                       playMenuMusicOnce=false;
                      }
                  //TODO: update a model only used in the menu
                  gameController.display();
                 }
             if(gameRunning)
                 {if(hasBeenKilled)
                      {player.respawn();
                       hasBeenKilled=false;                      
                      }          
                  innerLoop=true;
                  gameController.playSound(14);
                 }
             boolean bmoved;
             double playerXnew,playerZnew;
             float framerateCompensatedSpeed;          
             this.internalClock.start();
             this.lastBotShotTime=currentTime();
             this.lastShot=currentTime();
             this.isFalling=false;
             long cycleDuration;
             boolean walkSoundPlayedOnce=false;
             //loop until the end of a party (even when game paused)
             while(innerLoop)
                 {gameController.display();                              
                  //update the clock if required
                  cycleDuration=internalClock.getElapsedTime();
                  if(bpause) 
                      {if(!internalClock.isPaused())
                           internalClock.pause();
                       continue;
                      }
                  else
                      if(internalClock.isPaused())
                          internalClock.unpause();
                      else
                          internalClock.sync();
                  cycleDuration=internalClock.getElapsedTime()-cycleDuration;
                  framerateCompensationFactor = 16*cycleDuration*10;
                  //uses the time spent between 2 frames to adapt the step to the speed of the machine
                  framerateCompensatedSpeed=framerateCompensationFactor*(runningFast?3:1)/65536.0f;
                  postInfoMessage();
                  updateExplosions();
                  updateItems();
                  hasBeenKilled=stepObjects();   
                  //gameController.stepBotwalkSound();               
                  if(!player.isAlive()&&!isFalling)
                      continue;
                  // step player              
                  if(turningLeft)
                      {turnLeft(cycleDuration);
                      }          
                  if(turningRight)
                      {turnRight(cycleDuration);                   
                      }   
                  if(player.isAlive())
                      {player.setDirection(player.getDirection()-gameController.getDelta().x/1800.0d);
                       updateRocketLauncherPositionFromPlayerPosition();
                      }
                  //System.out.println(player.getDirection()+" "+turningAmount);
                  if(player.getDirection()>=fullCircle)
                      player.setDirection(player.getDirection()-fullCircle);
                  else
                      if(player.getDirection()<0.0)
                          player.setDirection(player.getDirection()+fullCircle);
                  
                  if(lookingDown)
                      {lookDown();
                       lookingDown=false;
                      }
                  if(lookingUp)
                      {lookUp();
                       lookingUp=false;
                      }
                  playerXnew = player.getX();
                  playerZnew = player.getZ();         
                  if(rightStepping)
                      {playerXnew+=Math.sin(player.getDirection()-(fullCircle/4))*framerateCompensatedSpeed;
                       playerZnew+=Math.cos(player.getDirection()-(fullCircle/4))*framerateCompensatedSpeed;
                      }
                  if(leftStepping)
                      {playerXnew+=Math.sin(player.getDirection()+(fullCircle/4))*framerateCompensatedSpeed;
                       playerZnew+=Math.cos(player.getDirection()+(fullCircle/4))*framerateCompensatedSpeed;
                      }
                  if(runningForward) 
                      {playerXnew+=Math.sin(player.getDirection())*framerateCompensatedSpeed;
                       playerZnew+=Math.cos(player.getDirection())*framerateCompensatedSpeed;
                      }
                  if(runningBackward)
                      {playerXnew-=Math.sin(player.getDirection())*framerateCompensatedSpeed;
                       playerZnew-=Math.cos(player.getDirection())*framerateCompensatedSpeed;
                      }                
                  if(playerXnew != player.getX() || playerZnew != player.getZ())
                      bmoved=hasPlayerMoved(player,playerXnew,playerZnew);   
                  else
                      bmoved=false;
                  //System.out.println("player: "+player.getX()+" "+player.getZ());
                  //started player to move?
                  if(!playerMoving && bmoved)
                      {playerMoving = true;
                       //gameController.startMovingSound(2);
                       /*if(!walkSoundPlayedOnce)
                           {*///gameController.loopSound(13);
                            /*walkSoundPlayedOnce=true;
                           }
                       else
                           gameController.resumeSound(13);*/
                      }
                  else
                      if(!bmoved && playerMoving)
                          {// player stopped moving:
                           playerMoving = false;
                           //gameController.stopMovingSound(2);
                           /*if(walkSoundPlayedOnce)
                               gameController.pauseSound(13);*/
                           //gameController.stopSound(13);
                          }               
                 }  // innerloop
             //gameController.stopCarpetSound();      
            }  // outerloop      
    }
    
    /**
     * 
     * @param player
     * @param playerXnew
     * @param playerZnew
     * @return
     */
    private final boolean hasPlayerMoved(PlayerModel player,double playerXnew,
            double playerZnew){
        boolean bmoved,horizontalRecurse,verticalRecurse;       
        horizontalRecurse=(player.getZ()!=playerZnew);
        verticalRecurse=(player.getX()!=playerXnew);
        if(!horizontalRecurse && !verticalRecurse)
            bmoved=false;
        else
            {int xidx1 = (int)Math.round(playerXnew)-1;
             int zidx1 = (int)Math.round(playerZnew)-1;
             Rectangle2D.Double rPlayerVoxel=new Rectangle2D.Double();
             rPlayerVoxel.setRect(player.getVoxel());
             rPlayerVoxel.x=playerXnew-player.getBoundingSize()/2.0d;
             rPlayerVoxel.y=playerZnew-player.getBoundingSize()/2.0d;
             Rectangle2D.Double rWallVoxel=new Rectangle2D.Double();
             rWallVoxel.width=1;
             rWallVoxel.height=1;                               
             boolean isPlayerColliding=false;
             boolean isCellEmpty;
             double cellFactor=1.0D;
             for(int i=0,x,z;!isPlayerColliding && i<4;i++)
                 {x=xidx1+i%2;
                  z=zidx1+i/2;
                  switch(collisionMap[(z*mapEdgeSize)+x])
                  {case EMPTY:
                   {isCellEmpty=true;
                    break;
                   }
                   case FIXED_AND_BREAKABLE_BIG:
                   {isCellEmpty=false;
                    cellFactor=0.4D;                   
                    break;
                   }
                   case FIXED_AND_BREAKABLE_LIGHT:
                   {isCellEmpty=false;
                    cellFactor=0.2D;                   
                    break;
                   }                                    
                   case FIXED_AND_BREAKABLE_TABLE:
                   {isCellEmpty=false;
                    cellFactor=0.2D;                   
                    break;
                   }
                   case FIXED_AND_BREAKABLE_BONSAI:
                   case FIXED_AND_BREAKABLE_CHAIR:
                   case FIXED_AND_BREAKABLE_FLOWER:
                   case AVOIDABLE_AND_UNBREAKABLE:
                   default: 
                   {isCellEmpty=false;
                    cellFactor=1.0D;
                    break;
                   }
                  }  
                  if(!isCellEmpty)
                      {rWallVoxel.width=cellFactor;
                       rWallVoxel.height=cellFactor;
                       rWallVoxel.x=x+(1.0D-cellFactor)/2.0D;
                       rWallVoxel.y=z+(1.0D-cellFactor)/2.0D;
                       if(rPlayerVoxel.intersects(rWallVoxel))
                           isPlayerColliding=true;                      
                      }
                 }
             if(!isPlayerColliding)
                 {player.setX(playerXnew);
                  player.setZ(playerZnew);
                  updateRocketLauncherPositionFromPlayerPosition();
                  bmoved=true;                    
                 }
             else
                 {if(horizontalRecurse && hasPlayerMoved(player,playerXnew,player.getZ()))
                      bmoved=true;
                  else
                      if(verticalRecurse)
                          bmoved=hasPlayerMoved(player,player.getX(),playerZnew);
                      else
                          bmoved=false;
                 }
            }       
        return(bmoved);
    }
 
    public final void performAtExit(){
        innerLoop=false;
        gameRunning=false;
    }

    //TODO: optimize the model to drive this method useless
    private final BotModel getBotModelFromBotObject(d3object botObj){
        for(BotModel bot:botList)
            if(bot.getX()==botObj.getX() && bot.getZ()==botObj.getZ())
                return(bot);                                           
        return(null);
    }
    
    /**walking bot support*/
    private final void tryStepBot(d3object obj,double dxp,double dzp){
        double xnew=obj.getX();
        double znew=obj.getZ();
        boolean bStepLeft=false;
        boolean bStepRight=false;
        boolean bmoved=false;
        final float baseSpeed=1.0f/65536.0f;
        float ispeed=baseSpeed;
        //TODO: implement a mechanism to prevent the bot from getting closer to the rockets
        //and allow it to dodge them
        if (obj.getSleep2() > 0)
            obj.setSleep2(obj.getSleep2()-1);  // suspend walking (after rocket launch)
        else
            {if(bStepLeft)
                 {xnew+=Math.sin(obj.getDir()-(fullCircle/4))*framerateCompensationFactor*obj.getSpeed();
                  znew+=Math.cos(obj.getDir()-(fullCircle/4))*framerateCompensationFactor*obj.getSpeed();
                 }
             if(bStepRight)
                  {xnew+=Math.sin(obj.getDir()+(fullCircle/4))*framerateCompensationFactor*obj.getSpeed();
                   znew+=Math.cos(obj.getDir()+(fullCircle/4))*framerateCompensationFactor*obj.getSpeed();
                  }
             if(obj.getSpeed() >= baseSpeed)
                 {// no matter if speed 1 or more, we always probe
                  if (obj.getSpeed() >= 2*baseSpeed)
                      ispeed = obj.getSpeed()-baseSpeed;
                  xnew+=Math.sin(obj.getDir())*framerateCompensationFactor*ispeed/10;
                  znew+=Math.cos(obj.getDir())*framerateCompensationFactor*ispeed/10;
                 }
            }
        BotModel currentBot=getBotModelFromBotObject(obj);
        if(xnew != obj.getX() || znew != obj.getZ())
           {// how far can the bot walk, if at all?         
            int xidx1 = (((int)xnew)&0xFF);//0<=xidx1<256
            int zidx1 = (((int)znew)&0xFF);
            // about to leave area?
            boolean bstop=false;           
            //force the bot to stop when it is too close to another one and 
            //when the map is occupied           
            //TODO: rather the scene graph
            //if the voxel is not empty
            if(collisionMap[(zidx1*mapEdgeSize)+xidx1]!=EMPTY)
                bstop=true;
            else
                {double dx,dz;
                 for(BotModel bot:botList)
                     {dx=Math.abs(bot.getX()-xnew);
                      dz=Math.abs(bot.getZ()-znew);
                      //if this is another bot and if this bot is too close (at least 2 voxels)
                      if(Math.abs(bot.getX()-obj.getX())>0&&Math.abs(bot.getZ()-obj.getZ())>0&&dx<2.0f&&dz<2.0f) 
                          {bstop=true;
                           break;
                          }
                     }
                 }
            // keep minimum distance also to player
            if(!bstop&&(Math.abs(dxp)<1.0f)&&(Math.abs(dzp)<1.0f))
                bstop=true;
            // check for obstacles and move
            if(!bstop) 
                {//change the position of the bot in the both systems (Vincent's system and mine)
                 obj.setX(xnew);
                 obj.setZ(znew); 
                 currentBot.setX(xnew);
                 currentBot.setZ(znew);                
                 bmoved=true;
                }

           }  // endif position changed
       if(bmoved) 
           {if(obj.getSpeed() < 2/65536.0f)
                {// this determines the bot speeds. the number
                 // gets divided by 10. so 10==1.0, 5==0.5 etc.
                 obj.setSpeed(8.0f/65536.0f);                
                }
            currentBot.setRunning(true); 
            // count walking bots, for stepObjects()
            nClBotsWalking++;
           }  
       else 
           {if(obj.getSpeed() >= 2/65536.0f)
                {obj.setSpeed(1.0f/65536.0f);
                 currentBot.setRunning(false);
                }
           }
    }
    
    private final boolean stepObjects(){
        boolean hasBeenKilled=false,bhit;
        double objectXnew,objectZnew,xdiff,zdiff,deltax,deltaz,maxDelta,interpolationStepX,interpolationStepZ;
        final double minRange=Math.min(hitrange,rockrange);
        float ispeed;
        int iobj,xidx1,zidx1,nOldBotsWalking=nClBotsWalking,interpolationCount;
        d3object obj;
        nClBotsWalking=0;
        //between IndexPlayerRockets and IndexBotRockets, get all active rockets
        boolean[] activeRocket=new boolean[20];
        float[] rocket;
        for(int i=0;i<activeRocket.length;i++)
            {activeRocket[i]=(object[i].getShape()==ShapeRocket);
             if(!activeRocket[i])                 
                 {//attempt to get the rocket in the table
                  rocket=rocketTable.get(Integer.valueOf(i));                      
                  if(rocket!=null)
                      {//remove the rocket from the list
                       rocketList.remove(rocket);
                       //remove the rocket from the table
                       rocketTable.remove(Integer.valueOf(i));
                      }                     
                 }
            }
        // cycle through all object slots
        boolean bfire;
        double dx,dz;
        //loop on all active objects (whose speed is not zero)
        for(int counter=0; counter<numObjects; counter++)
            if((ispeed=object[counter].getSpeed())>0)
                 {obj=object[counter];
                  if(obj.getShape()==ShapeBot) // bot
                      {//check if player is within firing range
                       dx=player.getX()-obj.getX(); // x-distance to player
                       dz=player.getZ()-obj.getZ(); // y-distance to player
                       bfire=true;
                       if(playerVisibleFrom(obj))
                           {if(!obj.getSeenPlayer())
                                {obj.setSeenPlayer(true);
                                 // "NOW..."
                                 gameController.playSound(6,(int)obj.getX(),(int)obj.getZ(),(int)player.getX(),(int)player.getZ());
                                }
                           }
                       else 
                           {//player not visible by bot
                            bfire=false;
                           }                 
                       if(bfire)
                           {// bot wants to fire. calc direction to player.
                            obj.setDir(reverseDir(dx,dz));
                            //set bot direction
                            //TODO: optimize the model to drive this loop useless
                            for(BotModel bot:botList)
                                if(bot.getX()==obj.getX() && bot.getZ()==obj.getZ())
                                    {bot.setDirection(obj.getDir());             
                                     break;
                                    }                 
                            long lTime;
                            if(player.isAlive() && !player.isWinning() && 
                              (lTime=currentTime()) > lastBotShotTime+500)
                                {if(tryLaunchBotRocket(counter, obj.getDir(),counter-IndexBots)) 
                                     {obj.setSleep2(10); // avoid walking into own rocket
                                      lastBotShotTime=lTime;
                                     }
                                }
                            tryStepBot(obj,dx,dz);
                           }
                      }
                  else
                      {//the following is used only for rockets
                       //take into account the frame rate or the main timing
                       deltax=Math.sin(obj.getDir())*framerateCompensationFactor*ispeed;
                       deltaz=Math.cos(obj.getDir())*framerateCompensationFactor*ispeed;              
                       maxDelta=Math.max(deltax,deltaz);
                       //we take the biggest movement and the smallest
                       //range to compute the biggest interpolation count 
                       //in order to avoid forgetting some collisions 
                       //(objects might go through each other then :( )
                       /*interpolationCount=(int)Math.ceil(maxDelta/minRange);
                       //overestimate this count to be sure to perform enough interpolations
                       interpolationCount++;*/
                       //the linear interpolation has been disabled as it was buggy
                       interpolationCount=1;
                       interpolationStepX=deltax/interpolationCount;
                       interpolationStepZ=deltaz/interpolationCount;
                       objectXnew=obj.getX();
                       objectZnew=obj.getZ();
                       bhit=false;
                       for(int m=0;m<interpolationCount&&!bhit;m++)
                           {objectXnew+=interpolationStepX;
                            objectZnew+=interpolationStepZ;
                            //System.out.println("STEP: "+interpolationStepX+" "+interpolationStepZ);
                            // reached a discrete new map position?
                            xidx1=(((int)objectXnew)&0xFF);
                            zidx1=(((int)objectZnew)&0xFF);
                            //bhit=false;
                            if(obj.getShape()==ShapeRocket)
                                {// check for rocket<->object collision
                                 for(iobj=0; iobj<numObjects; iobj++) 
                                     if(object[iobj].getShape()!=-1
                                     && object[iobj].getShape()!=ShapeBush
                                     && iobj!=counter) /*ourselves*/
                                     {xdiff=Math.abs(object[iobj].getX()-objectXnew);
                                      zdiff=Math.abs(object[iobj].getZ()-objectZnew);
                                      if(xdiff<hitrange&&zdiff<hitrange) 
                                          {if(object[iobj].getShape()==ShapeRocket)
                                               {//rocket nearby another rocket. check precise.
                                                if(xdiff<rockrange&&zdiff<rockrange)
                                                    {blastObject(object[iobj]);
                                                     //FIXME: use object[iobj], remove it AS IT IS A ROCKET
                                                     bhit=true;
                                                    }
                                               }
                                           else
                                               {//rocket hit a non-rocket object
                                                blastObject(object[iobj]);
                                                bhit=true;
                                               }
                                          }
                                     }
                                 //check for rocket<->player collision
                                 xdiff=Math.abs(player.getX()-objectXnew);
                                 zdiff=Math.abs(player.getZ()-objectZnew);
                                 if(xdiff<player.getBoundingSize()&&zdiff<player.getBoundingSize()&&player.isAlive()) 
                                     {// player is hit
                                      bhit=true;
                                      if(!bcheat)
                                          player.decreaseHealth(20);
                                      gameController.stopMovingSound(1<<1);
                                      gameController.playSound(2,(int)objectXnew,(int)objectZnew,(int)player.getX(),(int)player.getZ());                      
                                      if(!player.isAlive()&&!isFalling)
                                          {isFalling=true;
                                           //start the time measure   
                                           fallStart=currentTime();
                                          }
                                     }
                                }
                            if(collisionMap[(zidx1*mapEdgeSize)+xidx1]!=EMPTY && 
                               collisionMap[(zidx1*mapEdgeSize)+xidx1]>=UNAVOIDABLE_AND_UNBREAKABLE_DOWN && 
                               collisionMap[(zidx1*mapEdgeSize)+xidx1]<=UNAVOIDABLE_AND_UNBREAKABLE_UP)
                                {// handle a wall impact
                                 bhit=true;
                                 //show impact on plain wall tile
                                 Impact impact=null,impact1=null,impact2=null,impact3=null;
                                 switch(collisionMap[(zidx1*mapEdgeSize)+xidx1])
                                 {case UNAVOIDABLE_AND_UNBREAKABLE_UP:
                                 {impact=Impact.computeImpactFromTargetoryBipoint((float)obj.getX(),(float)obj.getZ(),(float)objectXnew,(float)objectZnew,(float)xidx1,(float)(zidx1),(float)((xidx1+1)),(float)(zidx1),0.0f,-1.0f);                                                                                                    
                                 break;
                                 }
                                 case UNAVOIDABLE_AND_UNBREAKABLE_DOWN:
                                 {impact=Impact.computeImpactFromTargetoryBipoint((float)obj.getX(),(float)obj.getZ(),(float)objectXnew,(float)objectZnew,(float)(xidx1),(float)((zidx1+1)),(float)((xidx1+1)),(float)((zidx1+1)),0.0f,1.0f);                                 
                                 break;
                                 }
                                 case UNAVOIDABLE_AND_UNBREAKABLE_LEFT:
                                 {impact=Impact.computeImpactFromTargetoryBipoint((float)obj.getX(),(float)obj.getZ(),(float)objectXnew,(float)objectZnew,(float)(xidx1),(float)(zidx1),(float)(xidx1),(float)(zidx1+1),-1.0f,0.0f);                                
                                 break;
                                 }
                                 case UNAVOIDABLE_AND_UNBREAKABLE_RIGHT:
                                 {impact=Impact.computeImpactFromTargetoryBipoint((float)obj.getX(),(float)obj.getZ(),(float)objectXnew,(float)objectZnew,(float)(xidx1+1),(float)zidx1,(float)(xidx1+1),(float)(zidx1+1),1.0f,0.0f);                                 
                                 break;
                                 }
                                 case UNAVOIDABLE_AND_UNBREAKABLE_UP_DOWN:
                                 {impact=Impact.computeImpactFromTargetoryBipoint((float)obj.getX(),(float)obj.getZ(),(float)objectXnew,(float)objectZnew,(float)xidx1,(float)zidx1,(float)(xidx1+1),(float)zidx1,0.0f,-1.0f);
                                 impact1=Impact.computeImpactFromTargetoryBipoint((float)obj.getX(),(float)obj.getZ(),(float)objectXnew,(float)objectZnew,(float)xidx1,(float)(zidx1+1),(float)(xidx1+1),(float)(zidx1+1),0.0f,1.0f);
                                 break;
                                 }
                                 case UNAVOIDABLE_AND_UNBREAKABLE_UP_LEFT:
                                 {impact=Impact.computeImpactFromTargetoryBipoint((float)obj.getX(),(float)obj.getZ(),(float)objectXnew,(float)objectZnew,(float)xidx1,(float)zidx1,(float)(xidx1+1),(float)zidx1,0.0f,-1.0f);
                                 impact1=Impact.computeImpactFromTargetoryBipoint((float)obj.getX(),(float)obj.getZ(),(float)objectXnew,(float)objectZnew,(float)xidx1,(float)zidx1,(float)xidx1,(float)(zidx1+1),-1.0f,0.0f);
                                 break;
                                 }
                                 case UNAVOIDABLE_AND_UNBREAKABLE_UP_RIGHT:
                                 {impact=Impact.computeImpactFromTargetoryBipoint((float)obj.getX(),(float)obj.getZ(),(float)objectXnew,(float)objectZnew,(float)xidx1,(float)zidx1,(float)(xidx1+1),(float)zidx1,0.0f,-1.0f);
                                 impact1=Impact.computeImpactFromTargetoryBipoint((float)obj.getX(),(float)obj.getZ(),(float)objectXnew,(float)objectZnew,(float)(xidx1+1),(float)zidx1,(float)(xidx1+1),(float)(zidx1+1),1.0f,0.0f);
                                 break;
                                 }
                                 case UNAVOIDABLE_AND_UNBREAKABLE_DOWN_LEFT:
                                 {impact=Impact.computeImpactFromTargetoryBipoint((float)obj.getX(),(float)obj.getZ(),(float)objectXnew,(float)objectZnew,(float)xidx1,(float)(zidx1+1),(float)(xidx1+1),(float)(zidx1+1),0.0f,1.0f);
                                 impact1=Impact.computeImpactFromTargetoryBipoint((float)obj.getX(),(float)obj.getZ(),(float)objectXnew,(float)objectZnew,(float)xidx1,(float)zidx1,(float)xidx1,(float)(zidx1+1),-1.0f,0.0f);
                                 break;
                                 }
                                 case UNAVOIDABLE_AND_UNBREAKABLE_DOWN_RIGHT:
                                 {impact=Impact.computeImpactFromTargetoryBipoint((float)obj.getX(),(float)obj.getZ(),(float)objectXnew,(float)objectZnew,(float)xidx1,(float)(zidx1+1),(float)(xidx1+1),(float)(zidx1+1),0.0f,1.0f);
                                 impact1=Impact.computeImpactFromTargetoryBipoint((float)obj.getX(),(float)obj.getZ(),(float)objectXnew,(float)objectZnew,(float)(xidx1+1),(float)zidx1,(float)(xidx1+1),(float)(zidx1+1),1.0f,0.0f);
                                 break;
                                 }
                                 case UNAVOIDABLE_AND_UNBREAKABLE_LEFT_RIGHT:
                                 {impact=Impact.computeImpactFromTargetoryBipoint((float)obj.getX(),(float)obj.getZ(),(float)objectXnew,(float)objectZnew,(float)xidx1,(float)zidx1,(float)xidx1,(float)(zidx1+1),-1.0f,0.0f);
                                 impact1=Impact.computeImpactFromTargetoryBipoint((float)obj.getX(),(float)obj.getZ(),(float)objectXnew,(float)objectZnew,(float)(xidx1+1),(float)zidx1,(float)(xidx1+1),(float)(zidx1+1),1.0f,0.0f);
                                 break;
                                 }
                                 case UNAVOIDABLE_AND_UNBREAKABLE_UP_DOWN_LEFT:
                                 {impact=Impact.computeImpactFromTargetoryBipoint((float)obj.getX(),(float)obj.getZ(),(float)objectXnew,(float)objectZnew,(float)xidx1,(float)zidx1,(float)(xidx1+1),(float)zidx1,0.0f,-1.0f);
                                 impact1=Impact.computeImpactFromTargetoryBipoint((float)obj.getX(),(float)obj.getZ(),(float)objectXnew,(float)objectZnew,(float)xidx1,(float)(zidx1+1),(float)(xidx1+1),(float)(zidx1+1),0.0f,1.0f);
                                 impact2=Impact.computeImpactFromTargetoryBipoint((float)obj.getX(),(float)obj.getZ(),(float)objectXnew,(float)objectZnew,(float)xidx1,(float)zidx1,(float)xidx1,(float)(zidx1+1),-1.0f,0.0f);
                                 break;
                                 }
                                 case UNAVOIDABLE_AND_UNBREAKABLE_UP_DOWN_RIGHT:
                                 {impact=Impact.computeImpactFromTargetoryBipoint((float)obj.getX(),(float)obj.getZ(),(float)objectXnew,(float)objectZnew,(float)xidx1,(float)zidx1,(float)(xidx1+1),(float)zidx1,0.0f,-1.0f);
                                 impact1=Impact.computeImpactFromTargetoryBipoint((float)obj.getX(),(float)obj.getZ(),(float)objectXnew,(float)objectZnew,(float)xidx1,(float)(zidx1+1),(float)(xidx1+1),(float)(zidx1+1),0.0f,1.0f);
                                 impact2=Impact.computeImpactFromTargetoryBipoint((float)obj.getX(),(float)obj.getZ(),(float)objectXnew,(float)objectZnew,(float)(xidx1+1),(float)zidx1,(float)(xidx1+1),(float)(zidx1+1),1.0f,0.0f);
                                 break;
                                 }
                                 case UNAVOIDABLE_AND_UNBREAKABLE_UP_LEFT_RIGHT:
                                 {impact=Impact.computeImpactFromTargetoryBipoint((float)obj.getX(),(float)obj.getZ(),(float)objectXnew,(float)objectZnew,(float)xidx1,(float)zidx1,(float)(xidx1+1),(float)zidx1,0.0f,-1.0f);
                                 impact1=Impact.computeImpactFromTargetoryBipoint((float)obj.getX(),(float)obj.getZ(),(float)objectXnew,(float)objectZnew,(float)xidx1,(float)zidx1,(float)xidx1,(float)(zidx1+1),-1.0f,0.0f);
                                 impact2=Impact.computeImpactFromTargetoryBipoint((float)obj.getX(),(float)obj.getZ(),(float)objectXnew,(float)objectZnew,(float)(xidx1+1),(float)zidx1,(float)(xidx1+1),(float)(zidx1+1),1.0f,0.0f);
                                 break;
                                 }
                                 case UNAVOIDABLE_AND_UNBREAKABLE_DOWN_LEFT_RIGHT:
                                 {impact=Impact.computeImpactFromTargetoryBipoint((float)obj.getX(),(float)obj.getZ(),(float)objectXnew,(float)objectZnew,(float)xidx1,(float)(zidx1+1),(float)(xidx1+1),(float)(zidx1+1),0.0f,1.0f);                     
                                 impact1=Impact.computeImpactFromTargetoryBipoint((float)obj.getX(),(float)obj.getZ(),(float)objectXnew,(float)objectZnew,(float)xidx1,(float)zidx1,(float)xidx1,(float)(zidx1+1),-1.0f,0.0f);
                                 impact2=Impact.computeImpactFromTargetoryBipoint((float)obj.getX(),(float)obj.getZ(),(float)objectXnew,(float)objectZnew,(float)(xidx1+1),(float)zidx1,(float)(xidx1+1),(float)(zidx1+1),1.0f,0.0f);                             
                                 break;
                                 }
                                 case UNAVOIDABLE_AND_UNBREAKABLE_UP_DOWN_LEFT_RIGHT:
                                 {impact=Impact.computeImpactFromTargetoryBipoint((float)obj.getX(),(float)obj.getZ(),(float)objectXnew,(float)objectZnew,(float)xidx1,(float)zidx1,(float)(xidx1+1),(float)zidx1,0.0f,-1.0f);                       
                                 impact1=Impact.computeImpactFromTargetoryBipoint((float)obj.getX(),(float)obj.getZ(),(float)objectXnew,(float)objectZnew,(float)xidx1,(float)(zidx1+1),(float)(xidx1+1),(float)(zidx1+1),0.0f,1.0f);
                                 impact2=Impact.computeImpactFromTargetoryBipoint((float)obj.getX(),(float)obj.getZ(),(float)objectXnew,(float)objectZnew,(float)xidx1,(float)zidx1,(float)xidx1,(float)(zidx1+1),-1.0f,0.0f);
                                 impact3=Impact.computeImpactFromTargetoryBipoint((float)obj.getX(),(float)obj.getZ(),(float)objectXnew,(float)objectZnew,(float)(xidx1+1),(float)zidx1,(float)(xidx1+1),(float)(zidx1+1),1.0f,0.0f);
                                 break;
                                 }
                                 default:
                                 {//corner
                                 }
                                 }
                                 if(impact!=null)
                                     impactList.add(impact);                     
                                 if(impact1!=null)
                                     impactList.add(impact1);
                                 if(impact2!=null)
                                     impactList.add(impact2);
                                 if(impact3!=null)
                                     impactList.add(impact3);                                
                                 gameController.playSound(1,(int)objectXnew,(int)objectZnew,(int)player.getX(),(int)player.getZ());
                                }
                            if(bhit)
                                {// no matter what kind of object was hit,
                                 // change its shape now to animated explosion                              
                                 if(obj.getShape()==ShapeRocket)
                                     {//FIXME: use object[counter], remove it AS IT IS A ROCKET (object[counter])

                                     }
                                 //remove the shape from the previous collision system
                                 obj.setSpeed(0);
                                 obj.setShape(-1);
                                 //instanciate a new Explosion object
                                 explode(obj);
                                }
                            else
                                {obj.setX(objectXnew);
                                 obj.setZ(objectZnew);
                                }
                           }
                      }
                 }
        //any changes in the bot walking soundscape?
        if(nClBotsWalking > nOldBotsWalking)
            {if(nOldBotsWalking < 3)
                 {int nDiff = Math.min(3,nClBotsWalking)-nOldBotsWalking;
                  for(int i=0;i<nDiff;i++)
                      {// System.out.println("=> start "+(6+nOldBotsWalking));
                       // startMovingSound(1<<(6+nOldBotsWalking));
                       /*gameController.requestBotwalkSound(nOldBotsWalking);*/
                       nOldBotsWalking++;
                      }
                  // System.out.println(""+nOldBotsWalking+" walkers");
                 }
            }
        else
            if(nClBotsWalking < nOldBotsWalking)
                {if(nClBotsWalking <= 0) 
                     {//just in case we miscounted:
                      /*gameController.unRequestBotwalkSound(0);
                      gameController.unRequestBotwalkSound(1);
                      gameController.unRequestBotwalkSound(2);*/
                     }
                 else
                     if(nOldBotsWalking <= 3) 
                         {int nDiff = nOldBotsWalking-nClBotsWalking;
                          for(int i=0;i<nDiff;i++) 
                              {nOldBotsWalking--;
                              /*gameController.unRequestBotwalkSound(nOldBotsWalking);*/
                              }                         
                         }
                }       
        Vector<float[]> removedRocketList=new Vector<float[]>(20);
        for(int i=0;i<activeRocket.length;i++)
            if(activeRocket[i])
                {rocket=rocketTable.get(Integer.valueOf(i));
                 if(object[i].getShape()==ShapeRocket)
                     {rocket[0]=(float)object[i].getX();
                      rocket[2]=(float)object[i].getZ();                     
                     }
                 else
                     {//remove the rocket from the list
                      rocketList.remove(rocket);
                      //remove the rocket from the table
                      rocketTable.remove(Integer.valueOf(i));                                       
                     }
                }
        for(float[] currentRocket:rocketList)
            if(!rocketTable.containsValue(currentRocket))
                removedRocketList.add(currentRocket);       
        //TODO: rather remove the rockets when exploding
        //remove the broken rocket       
        rocketList.removeAll(removedRocketList);
        if(!player.isAlive()&&isFalling)
            {//if the player has ended to fall
             long fallDuration=currentTime()-fallStart;
             if(fallDuration>fallTotalDuration)   
                 {//go to the menu etc...
                  gameController.setCycle(GameCycle.MAIN_MENU);         
                  innerLoop=false;
                  hasBeenKilled=true;
                  gameController.playTermSound();
                  //unset the flag
                  isFalling=false;
                  fallStart=0L;
                 }
             else                      
                 {float coef=fallDuration/(float)fallTotalDuration;
                  player.setY(-coef*coef*0.5D);         
                 }
            }
        return(hasBeenKilled);
    }

    /**
     * Tells whether the player is visible from the viewpoint
     * of a bot
     * @param obj bot
     */
    private final boolean playerVisibleFrom(d3object obj){
       double x1=obj.getX();
       double z1=obj.getZ();
       double x2=player.getX();
       double z2=player.getZ();
       double ddx=x2-x1;
       double ddz=z2-z1;
       int idx=(int)(ddx*8);
       int idz=(int)(ddz*8);
       int isteps=Math.max(1,Math.max(Math.abs(idx),Math.abs(idz)));// FIX/1.0.3: potential division by zero
       int cx,cz;
       for(int i=0;i<=isteps;i++) 
           {cx=((int)(x1+i*ddx/isteps))&0xFF;
            cz=((int)(z1+i*ddz/isteps))&0xFF;
            if(collisionMap[(cz*mapEdgeSize)+cx]!=EMPTY&&
               collisionMap[(cz*mapEdgeSize)+cx]!=AVOIDABLE_AND_UNBREAKABLE&&
               collisionMap[(cz*mapEdgeSize)+cx]!=FIXED_AND_BREAKABLE_LIGHT&&
               collisionMap[(cz*mapEdgeSize)+cx]!=FIXED_AND_BREAKABLE_TABLE)
                return false;
           }
       return true;
    }
    
    // this is called after a rocket impact
    private final void blastObject(d3object obj){
       if(obj.getShape()==ShapeBot) 
           {// a bot was hit.          
            //lightFlash((int)obj.getX(),(int)obj.getZ(),40,127);           
            gameController.playSound(2,(int)obj.getX(),(int)obj.getZ(),(int)player.getX(),(int)player.getZ());
            //increase the damage of the bot
            BotModel bot=getBotModelFromBotObject(obj);
            if(bot.isAlive())
                bot.decreaseHealth(20);                                    
            //if everybody is dead, you win!!
            //TODO: handle the other enemies too
            if(botList.isEmpty())
                {gameController.playAreaCleared();
                 player.setAsWinner();
                }
            // first hit on this bot?                        
            if(bot.isAlive())
                return;
            // bot is terminated
            gameController.playBotHit((int)obj.getX(),(int)obj.getZ(),(int)player.getX(),(int)player.getZ());
           }  
       else 
           {// a non-bot was hit. this could be
            // another rocket, or a deko         
            if(obj.getShape()==ShapeDeko) 
                {gameController.playSound(3,(int)obj.getX(),(int)obj.getZ(),(int)player.getX(),(int)player.getZ());
                 //TODO: use it for a vending machine or a table
                 //gameController.playSound(4,(int)obj.getX(),(int)obj.getZ(),(int)player.getX(),(int)player.getZ());
                }
            else
                gameController.playSound(1,(int)obj.getX(),(int)obj.getZ(),(int)player.getX(),(int)player.getZ());
           }
       // this spot may have been blocked by the object.
       // re-allow player movement here.
       //update the accelerated model
       //TODO: decorellate
       collisionMap[((int)obj.getZ())*mapEdgeSize+((int)obj.getX())]=EMPTY;
       //remove the shape from the previous collision system
       obj.setSpeed((short)0);
       obj.setShape(-1);
       //instanciate a new Explosion object
       explode(obj);     
    }

    public final void tryLaunchPlayerRocket(){
       //It prevents the player from shooting 6 rockets together instantaneously
       if(currentTime()-lastShot<timeBetweenShots)
           return;
       // a maximum of 3 active rockets applies.
       // if they're all currently active,
       // tryLaunch returns without any action.
       int irocket;
       d3object obj;
       boolean blaunched = false;
       final int maxactive = 6;

       // right rocket
       for (irocket=0; irocket<maxactive; irocket++)
          if (object[IndexPlayerRockets+irocket].getShape()==-1)
             break;
       if(irocket<maxactive)
           {obj = object[irocket];
            //I use something bigger than sqrt(2)*boundingSize to avoid the player
            //from killing himself
            obj.setX(player.getX()+(Math.sin(player.getDirection())*(player.getBoundingSize()*1.5))+Math.sin(player.getDirection()-fullCircle/4)*(player.getBoundingSize()*0.5));
            obj.setZ(player.getZ()+(Math.cos(player.getDirection())*(player.getBoundingSize()*1.5))+Math.cos(player.getDirection()-fullCircle/4)*(player.getBoundingSize()*0.5));
            obj.setShape(ShapeRocket);
            obj.setDir(player.getDirection());           
            obj.setSpeed(3.0f/65536.0f);
            blaunched=true;                   
            float[] rocket=new float[]{(float)obj.getX(),-0.125f,(float)obj.getZ(),
                    (float)(player.getDirection()*(180/Math.PI))};
            rocketList.add(rocket);
            rocketTable.put(Integer.valueOf(irocket),rocket);
           }
       if(blaunched)
           {gameController.playSound(0,(int)player.getX(),(int)player.getZ(),(int)player.getX(),(int)player.getZ());
            lastShot=currentTime();
           }
    }

    private final boolean tryLaunchBotRocket(int ifrom, double ddir, int irocket){
       d3object obj;
       irocket = IndexBotRockets + (irocket % 9);
       if(!bcheat && object[irocket].getShape()==-1)
           {obj=object[irocket];
            obj.setX(object[ifrom].getX() + Math.sin(ddir)*minimalRocketLaunchDistance);
            obj.setZ(object[ifrom].getZ() + Math.cos(ddir)*minimalRocketLaunchDistance);
            obj.setShape(ShapeRocket);
            obj.setDir(ddir);
            obj.setSpeed(3.0f/65536.0f);
            float[] rocket=new float[]{(float)obj.getX(),0.0f,(float)obj.getZ(),
                    (float)(ddir*(180/Math.PI))};
            rocketList.add(rocket);
            //FIXME: sometimes, a key can be overwritten
            rocketTable.put(Integer.valueOf(irocket),rocket);
            gameController.playSound(0,(int)obj.getX(),(int)obj.getZ(),(int)player.getX(),(int)player.getZ());
            return(true);
           }
       else
           return(false);
    }

    // reverse calculate a direction from a position delta
    private final double reverseDir(double dx, double dz){
        int n1;
        final double aquaddelta[]={0.0,fullCircle/2.0,fullCircle/2.0,fullCircle};
        if(dx>0) 
            {if(dz > 0) 
                 n1 = 0;
             else
                 n1 = 1;
            }  
        else  
            {if(dz > 0) 
                 n1 = 3;
             else
                 n1 = 2;
            }
        final double dthresh=0.01d;
        if(Math.abs(dx) < dthresh)
            {if(dz > 0.0)
                 return 0.0;
             else
                 return fullCircle/2.0;
            }
        if(Math.abs(dz) < dthresh)
            {if(dx > 0.0)
                 return fullCircle/4.0;
             else
                 return fullCircle*3.0/4.0;
            }
        double d=Math.atan(dx/dz)+aquaddelta[n1];
        if(d < 0.0) 
            d+=fullCircle;
        return(d);
    }
    
    private final void explode(d3object obj){
        explode((float)obj.getX(),0.0f,(float)obj.getZ(),(float)(obj.getDir()*(180/Math.PI)),0.0f);
    }
    
    private final void explode(float x,float y,float z,float horizontalDirection,
            float verticalDirection){
        ExplosionModel em=new ExplosionModel(x,y,z,horizontalDirection,
                verticalDirection,internalClock);
        explosionList.add(em);
        gameController.addNewExplosion(em);
    }
    
    private final void updateExplosions(){
        Vector<ExplosionModel> finishedExplosionsList=new Vector<ExplosionModel>();       
        for(ExplosionModel em:explosionList)
            {em.updateFrameIndex();
             if(em.isFinished())
                 {finishedExplosionsList.add(em);  
                  em.dispose();
                 }
            }
        explosionList.removeAll(finishedExplosionsList);       
    }
    
    private final void updateItems(){       
        for(HealthPowerUpModel hpum:healthPowerUpModelList)
            {//if the player is near the item
             //FIXME: it doesn't work
             if(player.intersectsWith(hpum))
                 {if(player.collects(hpum))
                      //send a message to the view
                      pushInfoMessage(hpum.getAfterCollectName(),2000);
                  else
                      hpum.updateFrameIndex();
                 }            
            }
        //TODO: update other items           
    }
    
    private final void pushInfoMessage(String message,long duration){
        gameInfoMessageList.add(new GameInfoMessage(message,duration,currentTime()));
    }
    
    private final void purgeGameInfoMessageList(){
        gameInfoMessageList.clear();
    }
    
    private final void postInfoMessage(){
        ArrayList<GameInfoMessage> oldMessagesList=new ArrayList<GameInfoMessage>();
        for(GameInfoMessage gim:gameInfoMessageList)
            if(gim.getCreationTime()+gim.getDuration()>=currentTime())
                gameController.pushInfoMessage(gim.getMessage());
            else
                oldMessagesList.add(gim);
        //remove old messages
        gameInfoMessageList.removeAll(oldMessagesList);
    }

    final float[] getRocketLauncherPos(){
        return(rocketLauncherPos);
    }
}