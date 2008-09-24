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
package main;

import java.awt.Frame;
import java.awt.GraphicsConfiguration;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.nio.FloatBuffer;
/*import java.rmi.Naming;
import java.rmi.RemoteException;
import java.util.BitSet;*/
import java.util.ArrayList;
import java.util.List;
import javax.media.opengl.GL;
import javax.media.opengl.GLCanvas;
import javax.media.opengl.GLCapabilities;

import tools.Full3DCell;
import tools.Full3DCellController;
import tools.Full3DCellView;
import tools.Network;
import tools.NetworkController;
import tools.NetworkView;

//TODO: move dependencies with Frame into the view
public final class GameController {

	
	private static final long serialVersionUID = 1L;
	
	private int screenWidth;
	
	private int screenHeight;
	
    private GLCanvas canvas;//canvas for OpenGL rendering
    
    private GL gl;
    
    private transient GameMouseMotionController gameMouseMotionController;//controls the mouse
    
    private GameGLView gameView;//view (referring to the design pattern "MVC")

    private ISoundSystem sif;
    
    //this game model can be remote
    private /*I*/GameModel gameModel;
    
    /*private IGameModelProxy gameModelProxy;
    
    private boolean remote;
    
    private BitSet dataModificationFlagsBitSet;*/
    
    final static int factor=GameModel.factor;
    
    private Frame frame;
    
	
	public GameController(){
	    sif = null;
	    //setRemote(false);//offline game by default	    
		frame = new Frame();		
		try{this.gameModel=new GameModel(this);}
		catch(Throwable t)
		{throw new RuntimeException("Unable to create the game model",t);}
		frame.setLocation(0,0);//sets the window at the top left corner
		frame.setUndecorated(true);//makes the decoration disappear
		frame.setIgnoreRepaint(true);//prevents the system from calling repaint automatically   	
	    //gets the size of the screen    
	    screenWidth=(int)Toolkit.getDefaultToolkit().getScreenSize().getWidth();
	    screenHeight=(int)Toolkit.getDefaultToolkit().getScreenSize().getHeight();	
	    frame.setSize(screenWidth,screenHeight);	
	    //bug fix : under Linux, sometimes the window was drawn below the taskbar
	    frame.setResizable(true);
	    //NOTA BENE: exclusive full screen mode doesn't improve the frame rate
	    //builds a transparent cursor
	    BufferedImage cursor=new BufferedImage(1,1,BufferedImage.TYPE_INT_ARGB);
	    cursor.setRGB(0,0,0);
	    frame.setCursor(Toolkit.getDefaultToolkit().createCustomCursor(cursor,new Point(0,0),"empty cursor"));	
	    GLCapabilities capabilities=new GLCapabilities();
	    capabilities.setDoubleBuffered(true);//enables double buffering
	    capabilities.setHardwareAccelerated(true);//enables hardware acceleration
	    canvas=new GLCanvas(capabilities);								
	    canvas.setAutoSwapBufferMode(false);//prevents any auto buffer swapping	
	    gl=canvas.getGL();
	    canvas.addGLEventListener(gameView=new GameGLView(this));	
	    canvas.addMouseMotionListener(gameMouseMotionController=new GameMouseMotionController(gameView));		
	    canvas.addMouseListener(gameMouseMotionController);	
	    canvas.addKeyListener(new GameKeyboardMonitor(gameView));			    
	    gameView.display();
	    frame.add(canvas);        		
	    frame.setVisible(true);
	    canvas.requestFocus();
	    canvas.requestFocusInWindow();       
	    attachSound();
        openSound();
        //FIXME: this animator doesn't work properly
        //Animator a=new Animator(canvas);
        //a.setRunAsFastAsPossible(true);
        //a.start();
	    /*try{*/this.gameModel.runEngine();/*} 
	    catch(RemoteException re)
        {throw new RuntimeException("Unable to run the engine",re);}*/
	    closeSound();
	}
	
	GraphicsConfiguration getGraphicsConfiguration(){
	    return(frame.getGraphicsConfiguration());
	}
	
	GL getGL(){
        return(gl);
    }        
    
    GLCanvas getCanvas(){
        return(canvas);
    }
    
    int getScreenWidth(){
        return(screenWidth);
    }
    
    final int getScreenHeight(){
        return(screenHeight);
    }
    
    //helpers to respect MVC and ease caching in the future
    final void display(){
        /*if(remote)
            {dataModificationFlagsBitSet.clear();            
             try{//get data modification flags (stored in a single BitSet) from the server
                 dataModificationFlagsBitSet.or(gameModel.getDataModificationFlagsBitSet());
                } 
             catch(Throwable t)
             {throw new RuntimeException("",t);}
            }    */         
    	gameView.display();
    }
    
    final void pushInfoMessage(String message){
    	gameView.pushInfoMessage(message);
    }
    
    final void launchNewGame(){
        try{gameModel.launchNewGame();}
        catch(Throwable t)
        {throw new RuntimeException("",t);}
    }
    
    final void resumeGame(){
        try{gameModel.resumeGame();}
        catch(Throwable t)
        {throw new RuntimeException("",t);}
    }
    
    final boolean getPlayerWins(){
        try{return(gameModel.getPlayerWins());}
        catch(Throwable t)
        {throw new RuntimeException("",t);}
    }
    
    final boolean getPlayerHit(){
        try{return(gameModel.getPlayerHit());}
        catch(Throwable t)
        {throw new RuntimeException("",t);}
    }
    
    final boolean getBpause(){
        try{return(gameModel.getBpause());}
        catch(Throwable t)
        {throw new RuntimeException("",t);}
    }
    
    final void setBpause(boolean bpause){
        try{gameModel.setBpause(bpause);}
        catch(Throwable t)
        {throw new RuntimeException("",t);}
    }
    
    final boolean getBcheat(){
        try{return(gameModel.getBcheat());}
        catch(Throwable t)
        {throw new RuntimeException("",t);}
    }
    
    final void setBcheat(boolean bcheat){
        try{gameModel.setBcheat(bcheat);}
        catch(Throwable t)
        {throw new RuntimeException("",t);}
    }
    
    final double getPlayerXpos(){
        try{return(gameModel.getPlayerXpos());}
        catch(Throwable t)
        {throw new RuntimeException("",t);}
    }
    
    final double getPlayerYpos(){
        try{return(gameModel.getPlayerYpos());}
        catch(Throwable t)
        {throw new RuntimeException("",t);}
    }
    
    final double getPlayerZpos(){
        try{return(gameModel.getPlayerZpos());}
        catch(Throwable t)
        {throw new RuntimeException("",t);}
    }
    
    final double getPlayerDirection(){
        try{return(gameModel.getPlayerDirection());}
        catch(Throwable t)
        {throw new RuntimeException("",t);}
    }
    
    final List<BotModel> getBotList(){
        try{return(gameModel.getBotList());}
        catch(Throwable t)
        {throw new RuntimeException("",t);}
    }
    
    final List<Impact> getImpactList(){
        try{return(gameModel.getImpactList());}
        catch(Throwable t)
        {throw new RuntimeException("",t);}
    }
    
    final List<float[]> getRocketList(){
        try{return(gameModel.getRocketList());}
        catch(Throwable t)
        {throw new RuntimeException("",t);}
    }
    
    final byte getCollisionMap(int index){
        try{return(gameModel.getCollisionMap(index));}
        catch(Throwable t)
        {throw new RuntimeException("",t);}
    }
    
    final boolean isGameRunning(){
        try{return(gameModel.isGameRunning());}
        catch(Throwable t)
        {throw new RuntimeException("",t);}
    }
    
    final int getHealth(){
        try{return(gameModel.getHealth());}
        catch(Throwable t)
        {throw new RuntimeException("",t);}
    }
    
    final void performAtExit(){
        try{gameModel.performAtExit();}
        catch(Throwable t)
        {throw new RuntimeException("",t);}
    }
    
    /*
     * This is an example of method showing how the networking works. 
     * The data are fetched only if they really changed in order to 
     * reduce RMI calls
     * */
    final FloatBuffer getArtCoordinatesBuffer1(){    
        /*try{if(remote)
                {//if the data are no more valid (if the data has changed)
                 if(dataModificationFlagsBitSet.get(0))
                     {//refresh the proxy
                      gameModelProxy.setArtCoordinatesBuffer1(gameModel.getArtCoordinatesBuffer1());
                      //update the data modification flag for these data
                      dataModificationFlagsBitSet.clear(0);
                     }
                 //use the proxy
                 return(gameModelProxy.getArtCoordinatesBuffer1());             
                }
            else*/
                return(gameModel.getArtCoordinatesBuffer1());
           /*}
        catch(Throwable t)
        {throw new RuntimeException("",t);}*/
    }
    
    final FloatBuffer getArtCoordinatesBuffer2(){
        try{return(gameModel.getArtCoordinatesBuffer2());}
        catch(Throwable t)
        {throw new RuntimeException("",t);}
    }
    
    final FloatBuffer getArtCoordinatesBuffer3(){
        try{return(gameModel.getArtCoordinatesBuffer3());}
        catch(Throwable t)
        {throw new RuntimeException("",t);}
    }
    
    final FloatBuffer getArtCoordinatesBuffer4(){
        try{return(gameModel.getArtCoordinatesBuffer4());}
        catch(Throwable t)
        {throw new RuntimeException("",t);}
    }
    
    final FloatBuffer getBonsaiCoordinatesBuffer(){
        try{return(gameModel.getBonsaiCoordinatesBuffer());}
        catch(Throwable t)
        {throw new RuntimeException("",t);}
    }
    
    final FloatBuffer getBotCoordinatesBuffer(){
        try{return(gameModel.getBotCoordinatesBuffer());}
        catch(Throwable t)
        {throw new RuntimeException("",t);}
    }
    
    final FloatBuffer getChairCoordinatesBuffer(){
        try{return(gameModel.getChairCoordinatesBuffer());}
        catch(Throwable t)
        {throw new RuntimeException("",t);}
    }
    
    final FloatBuffer getFlowerCoordinatesBuffer(){
        try{return(gameModel.getFlowerCoordinatesBuffer());}
        catch(Throwable t)
        {throw new RuntimeException("",t);}
    }
    
    final FloatBuffer getImpactCoordinatesBuffer(){
        try{return(gameModel.getImpactCoordinatesBuffer());}
        catch(Throwable t)
        {throw new RuntimeException("",t);}
    }
    
    final FloatBuffer getCrosshairCoordinatesBuffer(){
        try{return(gameModel.getCrosshairCoordinatesBuffer());}
        catch(Throwable t)
        {throw new RuntimeException("",t);}
    }
    
    final FloatBuffer getLampCoordinatesBuffer(){
        try{return(gameModel.getLampCoordinatesBuffer());}
        catch(Throwable t)
        {throw new RuntimeException("",t);}
    }
    
    final FloatBuffer getLevelCoordinatesBuffer(){
        try{return(gameModel.getLevelCoordinatesBuffer());}
        catch(Throwable t)
        {throw new RuntimeException("",t);}
    }
    
    final FloatBuffer getRocketCoordinatesBuffer(){
        try{return(gameModel.getRocketCoordinatesBuffer());}
        catch(Throwable t)
        {throw new RuntimeException("",t);}
    }
    
    final FloatBuffer getRocketLauncherCoordinatesBuffer(){
        try{return(gameModel.getRocketLauncherCoordinatesBuffer());}
        catch(Throwable t)
        {throw new RuntimeException("",t);}
    }
    
    final FloatBuffer getTableCoordinatesBuffer(){
        try{return(gameModel.getTableCoordinatesBuffer());}
        catch(Throwable t)
        {throw new RuntimeException("",t);}
    }
    
    final FloatBuffer getUnbreakableObjectCoordinatesBuffer(){
        try{return(gameModel.getUnbreakableObjectCoordinatesBuffer());}
        catch(Throwable t)
        {throw new RuntimeException("",t);}
    }
    
    final FloatBuffer getVendingMachineCoordinatesBuffer(){
        try{return(gameModel.getVendingMachineCoordinatesBuffer());}
        catch(Throwable t)
        {throw new RuntimeException("",t);}
    }   
    
    final GameCycle getCycle(){
    	return(gameView.getCycle());
    }
    
    final void setCycle(GameCycle cycle){
    	gameView.setCycle(cycle);
    }
    
    final void tryLaunchPlayerRocket(){
        try{gameModel.tryLaunchPlayerRocket();}
        catch(Throwable t)
        {throw new RuntimeException("",t);}
    }
    
    final Point getDelta(){
        return(gameMouseMotionController.getDelta());
    }
    
    final void setRunningFast(boolean runningFast){
        try{gameModel.setRunningFast(runningFast);}
        catch(Throwable t)
        {throw new RuntimeException("",t);}
    }
    
    final void setRunningForward(boolean runningForward){
        try{gameModel.setRunningForward(runningForward);}
        catch(Throwable t)
        {throw new RuntimeException("",t);}
    }
    
    final void setRunningBackward(boolean runningBackward){
        try{gameModel.setRunningBackward(runningBackward);}
        catch(Throwable t)
        {throw new RuntimeException("",t);}
    }
    
    final void setLeftStepping(boolean leftStepping){
        try{gameModel.setLeftStepping(leftStepping);}
        catch(Throwable t)
        {throw new RuntimeException("",t);}
    }
    
    final void setRightStepping(boolean rightStepping){
        try{gameModel.setRightStepping(rightStepping);}
        catch(Throwable t)
        {throw new RuntimeException("",t);}
    }
    
    final void setTurningLeft(boolean turningLeft){
        try{gameModel.setTurningLeft(turningLeft);}
        catch(Throwable t)
        {throw new RuntimeException("",t);}
    }
    
    final void setTurningRight(boolean turningRight){
        try{gameModel.setTurningRight(turningRight);}
        catch(Throwable t)
        {throw new RuntimeException("",t);}
    }
    
    //SOUND SYSTEM
    public final void attachSound(){
        System.out.println("trying Java 2 Sound:");
        try {sif = new SoundSystem();}  
        catch(Throwable e)
        {sif=null;
         System.out.println("Java 2 sound failed : "+e.getMessage());
        }
     }
     
    public final void openSound(){  
         try {if(sif!=null)
                  {if(!sif.openSound())         
                       {// Java 2 sound startup failed.
                        System.out.println("Java 2 sound startup failed.");
                        sif=null;
                       } 
                  }
              else
                  System.out.println("Java 2 sound startup failed.");
             }
         catch(Throwable t)
         {System.out.println("Problem : "+t.getMessage());
          sif=null;
         }
     }    

     public final void closeSound(){ 
         if(sif!=null) 
             sif.closeSound();
     }
     
     public final boolean loadSounds(){ 
         if(sif!=null) 
             return(sif.loadSounds()); 
         return(false); 
     }
     
     public final void stepMusic(){ 
         if(sif!=null) 
             sif.stepMusic();         
     }
     
     public final void restartMusic(){ 
         if(sif!=null) 
             sif.restartMusic();      
     }
     
     public String soundInfo(){
         if(sif!=null) 
             return sif.soundInfo(); 
         return ""; 
     }
     
     public final void playSound(int id,int x,int z,int playerx,int playerz){ 
         if(sif!=null) 
             sif.playSound(id,x,z,playerx,playerz);
     }
     
     public final void playBotGreeting(){ 
         if(sif!=null) 
             sif.playBotGreeting();   
     }
     
     public final void playBotHit(int x1,int z1,int x2,int z2){ 
         if(sif!=null) 
             sif.playBotHit(x1,z1,x2,z2); 
     }
     
     public final void playAreaCleared(){ 
         if(sif!=null) 
             sif.playAreaCleared();   
     }
     
     public final void playTermSound(){ 
         if(sif!=null) 
             sif.playTermSound();     
     }
     
     public final void startMovingSound(int iMask){ 
         if(sif!=null) 
             sif.startMovingSound(iMask);  
     }
     
     public final void stopMovingSound(int iMask){ 
         if(sif!=null) 
             sif.stopMovingSound(iMask);
     }
     
     //TODO : call it when the player gets close to an area
     public final void startCarpetSound(){ 
         if(sif!=null) 
             sif.startCarpetSound();  
     }
     
     public final void stopCarpetSound(){ 
         if(sif!=null) 
             sif.stopCarpetSound();
     }
     
     public final void stopAllSounds(){ 
         if(sif!=null) 
             sif.stopAllSounds();
     }
     
     /*final void setRemote(boolean remote){
         this.remote=remote;
         if(this.remote)
             {if(gameModelProxy==null)
                  gameModelProxy=new GameModelProxy();
              //get the remote game model (host:port/object)
              try{gameModel=(IGameModel)Naming.lookup("rmi://localhost:12345/arena0");} 
              catch(Throwable t)
              {throw new RuntimeException("Unable to get the remote game model",t);}
              //fill the proxy for the first time
              gameModelProxy.init(gameModel);
              if(dataModificationFlagsBitSet==null)
                  dataModificationFlagsBitSet=new BitSet();
             }
     }*/
     
     final void addNewExplosion(ExplosionModel em){
         //create an explosion view
         ExplosionView ev=new ExplosionView(gl);
         //create a controller that binds the model and the view
         /*Object3DController ec=*/new Object3DController(em,ev);
         //add this explosion to the view
         gameView.addNewExplosion(ev);
     }
     
     final void addNewItem(HealthPowerUpModel hpum){
         //create a medikit view
         HealthPowerUpView hpuv=new HealthPowerUpView(gl);
         //create a controller that binds the model and the view
         /*Object3DController ec=*/new Object3DController(hpum,hpuv);
         //add this medikit to the view
         gameView.addNewItem(hpuv);
     }
     
     final void registerSoftwareViewFrustumCullingPerformerAndPrepareNetwork(SoftwareViewFrustumCullingPerformer frustumView){
         new SoftwareViewFrustumCullingPerformerController(gameModel.getSvfcpModel(),frustumView);
         //bind all full cells models to their controllers and their views
         List<Full3DCellController> cellsControllersList=new ArrayList<Full3DCellController>();
         List<Full3DCellView> cellsViewsList=new ArrayList<Full3DCellView>();
         Full3DCellView cellView;
         Full3DCellController cellController;
         for(Full3DCell cellModel:gameModel.getCellsList())
             {cellView=new Full3DCellView(gl);
              cellController=new Full3DCellController(cellModel,cellView);
              cellsControllersList.add(cellController);
              cellsViewsList.add(cellView);
             }
         //build the network view
         NetworkView networkView=new NetworkView(cellsViewsList);
         //build the network controller
         new NetworkController(gameModel.getNetwork(),networkView,cellsControllersList);
         gameView.setNetworkView(networkView);
     }
     
     
     /*final List<Full3DCellView> getVisibleCellsList(){
         List<Full3DCell> tmpFull3DCellsList=gameModel.getVisibleCellsList();
         List<Full3DCellView> full3DCellsList=new ArrayList<Full3DCellView>();
         for(Full3DCell cell:tmpFull3DCellsList)
             full3DCellsList.add(cell.getController().getView());
         return(full3DCellsList);
     }*/
}
