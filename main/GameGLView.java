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

//import com.sun.opengl.util.j2d.TextRenderer;
import com.sun.opengl.util.BufferUtil;
import com.sun.opengl.util.texture.Texture;
import com.sun.opengl.util.texture.TextureIO;
import com.sun.opengl.util.Screenshot;
import java.awt.Font;
import java.io.File;
import java.io.IOException;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.List;
import java.util.Vector;
import java.util.Map.Entry;
import javax.media.opengl.GL;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.GLCanvas;
import javax.media.opengl.GLEventListener;
import javax.media.opengl.GLException;
import javax.media.opengl.glu.GLU;
import tools.Full3DCellView;
import tools.GameIO;
import tools.NetworkViewSet;

/**
 * This class has the role of being the GL part of the view
 * (in the meaning of the design pattern "MVC").
 *
 * @author Julien Gouesse
 */

public class GameGLView implements GLEventListener{
    
    
    private GLU glu;  
    
    private ConfigurationDetector configurationDetector;
    
    private GLCanvas canvas;
    
    private GameController gameController;
    
    private int screenWidth;
    
    private int screenHeight;
    
    private List<Object3DView> objectViewList;
    
    private List<String> messageLine;
    
    private List<Integer> messageWidth; 
    
    private List<Integer> messageHeight;
    
    private IStaticVertexSet artVertexSet1;
    
    private IStaticVertexSet artVertexSet2;
    
    private IStaticVertexSet artVertexSet3;
    
    private IStaticVertexSet artVertexSet4;
    
    private IDynamicVertexSet botVertexSet;
    
    private IStaticVertexSet unbreakableObjectVertexSet;
    
    private IStaticVertexSet vendingMachineVertexSet;
        
    private IStaticVertexSet lampVertexSet;
    
    private IStaticVertexSet chairVertexSet;
    
    private IStaticVertexSet flowerVertexSet;
    
    private IStaticVertexSet tableVertexSet;
    
    private IStaticVertexSet bonsaiVertexSet;
    
    private IStaticVertexSet impactVextexSet;
    
    private IStaticVertexSet crosshairVertexSet; 
    
    private IStaticVertexSet rocketLauncherVertexSet;
    
    private IStaticVertexSet rocketVertexSet;
    
    private Texture botTexture1;
    
    private Texture botTexture2;
    
    private Texture levelTexture;
    
    private Texture artTexture1;

    private Texture artTexture2;

    private Texture artTexture3;

    private Texture artTexture4;   
    
    private Texture objectsTexture;
    
    private Texture startingScreenTexture;
    
    private Texture startingMenuTexture;
    
    private Texture rocketLauncherTexture;
    
    private Texture impactTexture;

    private long  lnow;
    
    private long lbefore;
    
    //private long  lStartPhase;

    private boolean recSnapFilm;

    private boolean recSnapShot;      
    
    private VertexSetSeeker vertexSetSeeker;
    
    private TextRenderer textRenderer;
    
    //private int cycle;
    private GameCycle cycle;
    
    private boolean useAlphaTest;
    
    private GLMenu menu;
    
    private int loadProgress;
    
    private GLProgressBar progressBar;
    
    private String[] aboutText;
    
    private File snapDirectory;
    
    private NetworkViewSet networkViewSet;
    
    private Entry<Full3DCellView,Integer> playerPositioning;
    
    private static final float glPolygonOffsetFactor=-20.0f;
    
    private static final float glPolygonOffsetUnit=-20.0f;
    
    private static final int loadableItemCount=27+
    ExplosionViewFactory.getTexturesCount()+
    ExplosionViewFactory.getVertexSetsCount()+
    HealthPowerUpViewFactory.getTexturesCount()+
    HealthPowerUpViewFactory.getVertexSetsCount();
    
    private static int GL_MAX_TEXTURE_SIZE;
    
    private ViewFrustumCullingPerformer softwareViewFrustumCullingPerformer;
    
    private static final float[] neutralColor = {1.0f,1.0f,1.0f};
    
    //private static final int EMPTY=0;
    
    private static final int FIXED_AND_BREAKABLE_CHAIR=1;
    
    private static final int FIXED_AND_BREAKABLE_LIGHT=2;
    
    //private static final int MOVING_AND_BREAKABLE=3;
    
    private static final int AVOIDABLE_AND_UNBREAKABLE=4;
    
    //private static final int UNAVOIDABLE_AND_UNBREAKABLE=5;
    
    //private static final int UNAVOIDABLE_AND_UNBREAKABLE_DIRTY=6;
    
    private static final int FIXED_AND_BREAKABLE_BIG=7;
    
    private static final int FIXED_AND_BREAKABLE_FLOWER=8;
    
    private static final int FIXED_AND_BREAKABLE_TABLE=9;
    
    private static final int FIXED_AND_BREAKABLE_BONSAI=10;
    
    //private static final int UNAVOIDABLE_AND_UNBREAKABLE_DOWN=11;
    
    //private static final int UNAVOIDABLE_AND_UNBREAKABLE_LEFT=12;
    
    //private static final int UNAVOIDABLE_AND_UNBREAKABLE_RIGHT=13;
    
    //private static final int UNAVOIDABLE_AND_UNBREAKABLE_UP_DOWN=14;
    
    //private static final int UNAVOIDABLE_AND_UNBREAKABLE_UP_LEFT=15;
    
    //private static final int UNAVOIDABLE_AND_UNBREAKABLE_UP_RIGHT=16;
    
    //private static final int UNAVOIDABLE_AND_UNBREAKABLE_UP_DOWN_LEFT=17;
    
    //private static final int UNAVOIDABLE_AND_UNBREAKABLE_UP_DOWN_RIGHT=18;
    
    //private static final int UNAVOIDABLE_AND_UNBREAKABLE_UP_LEFT_RIGHT=19;
               
    //private static final int UNAVOIDABLE_AND_UNBREAKABLE_UP_DOWN_LEFT_RIGHT=20;
       
    //private static final int UNAVOIDABLE_AND_UNBREAKABLE_DOWN_LEFT=21;
    
    //private static final int UNAVOIDABLE_AND_UNBREAKABLE_DOWN_RIGHT=22;
    
    //private static final int UNAVOIDABLE_AND_UNBREAKABLE_DOWN_LEFT_RIGHT=23;
    
    //private static final int UNAVOIDABLE_AND_UNBREAKABLE_LEFT_RIGHT=24;
    
    //private static final int UNAVOIDABLE_AND_UNBREAKABLE_UP=25;
    
    //dirty walls
    //private static final int UNAVOIDABLE_AND_UNBREAKABLE_DOWN_DIRTY=26;
    
    //private static final int UNAVOIDABLE_AND_UNBREAKABLE_LEFT_DIRTY=27;
    
    //private static final int UNAVOIDABLE_AND_UNBREAKABLE_RIGHT_DIRTY=28;
    
    //private static final int UNAVOIDABLE_AND_UNBREAKABLE_UP_DOWN_DIRTY=29;
    
    //private static final int UNAVOIDABLE_AND_UNBREAKABLE_UP_LEFT_DIRTY=30;
    
    //private static final int UNAVOIDABLE_AND_UNBREAKABLE_UP_RIGHT_DIRTY=31;
    
    //private static final int UNAVOIDABLE_AND_UNBREAKABLE_UP_DOWN_LEFT_DIRTY=32;
    
    //private static final int UNAVOIDABLE_AND_UNBREAKABLE_UP_DOWN_RIGHT_DIRTY=33;
    
    //private static final int UNAVOIDABLE_AND_UNBREAKABLE_UP_LEFT_RIGHT_DIRTY=34;
               
    //private static final int UNAVOIDABLE_AND_UNBREAKABLE_UP_DOWN_LEFT_RIGHT_DIRTY=35;
       
    //private static final int UNAVOIDABLE_AND_UNBREAKABLE_DOWN_LEFT_DIRTY=36;
    
    //private static final int UNAVOIDABLE_AND_UNBREAKABLE_DOWN_RIGHT_DIRTY=37;
    
    //private static final int UNAVOIDABLE_AND_UNBREAKABLE_DOWN_LEFT_RIGHT_DIRTY=38;
    
    //private static final int UNAVOIDABLE_AND_UNBREAKABLE_LEFT_RIGHT_DIRTY=39;
    
    //private static final int UNAVOIDABLE_AND_UNBREAKABLE_UP_DIRTY=40;
                                      
    
    GameGLView(GameController gameController){
        this.gameController=gameController;	
    	this.glu=new GLU();
    	this.useAlphaTest=false;
    	this.loadProgress=0;
    	this.canvas=gameController.getCanvas();
    	this.screenWidth=gameController.getScreenWidth();
    	this.screenHeight=gameController.getScreenHeight();
    	this.messageLine=new Vector<String>();
    	this.messageHeight=new Vector<Integer>();
    	this.messageWidth=new Vector<Integer>();
    	this.objectViewList=new Vector<Object3DView>();
    	this.artVertexSet1=null;
    	this.artVertexSet2=null;
    	this.artVertexSet3=null;
    	this.artVertexSet4=null;
    	this.botVertexSet=null;
    	this.unbreakableObjectVertexSet=null;
    	this.vendingMachineVertexSet=null;
    	this.lampVertexSet=null;
    	this.chairVertexSet=null;
    	this.flowerVertexSet=null;    
    	this.tableVertexSet=null;
    	this.bonsaiVertexSet=null;
    	this.impactVextexSet=null;
    	this.crosshairVertexSet=null;
    	this.rocketLauncherVertexSet=null;
    	this.rocketVertexSet=null;
    	this.botTexture1=null;
    	this.botTexture2=null;
    	this.levelTexture=null;
    	this.artTexture1=null;
    	this.artTexture2=null;
    	this.artTexture3=null;
    	this.artTexture4=null;
    	this.objectsTexture=null;
    	this.startingScreenTexture=null;
    	this.startingMenuTexture=null;
    	this.impactTexture=null;   
    	this.playerPositioning=null;
    	this.recSnapFilm=false;
    	this.recSnapShot=false;   	
    	this.vertexSetSeeker=VertexSetSeeker.getInstance();
    	this.cycle=GameCycle.START_SCREEN;
    	this.lbefore=System.currentTimeMillis();
    	this.progressBar=new GLProgressBar((screenWidth-500)/2,(int)((screenHeight-25)/2.5),(int)(screenWidth*0.4),screenHeight/40,0,loadableItemCount);
    	this.progressBar.setProgressStringPainted(true);
    	this.aboutText=null;
    	//check if the directory called "snap" already exists, otherwise create it
    	this.snapDirectory=new File(System.getProperty("user.home")+System.getProperty("file.separator")+"snap");
	    try{if(!snapDirectory.exists() || !snapDirectory.isDirectory())
	            {if(!snapDirectory.mkdir())
	                 snapDirectory=null;
	             else
	                 if(!snapDirectory.canWrite())
	                     if(!snapDirectory.setWritable(true))
	                         snapDirectory=null;
	            }
	        else
	            if(!snapDirectory.canWrite())
	                if(!snapDirectory.setWritable(true))
	                    snapDirectory=null;
	       }
	    catch(SecurityException se)
	    {se.printStackTrace();
	     snapDirectory=null;
	    }
	    if(snapDirectory==null)
	        pushMessage("The program can not create a directory to save the screenshots!",(int)(screenWidth*0.35),(int)(screenHeight*0.5)); 
    }
    
    //this method allows to communicate with the model through the controller
    GameController getGameController(){
        return(gameController);
    }

    public final void display(){
    	canvas.display();
    }
    
    public final void display(GLAutoDrawable drawable){  	
        final GL gl=drawable.getGL();
        //long levelDrawTime=0;
        //removeUselessObjectViews();
        //System.out.println(((Runtime.getRuntime().totalMemory()-Runtime.getRuntime().freeMemory())/1048576) +"MB");
        gl.glClear(GL.GL_COLOR_BUFFER_BIT|GL.GL_DEPTH_BUFFER_BIT);          
	    //3D display now
	    switch(cycle)
	        {case START_SCREEN:
	             {break;}
	         case MAIN_MENU:
                 {break;}
	         case GAME:
	             {//if the game is not ready, don't display anything
	              if(loadProgress < loadableItemCount)
	                  break;
	              gl.glEnable(GL.GL_TEXTURE_2D); 
	              gl.glEnable(GL.GL_DEPTH_TEST);
	              gl.glLoadIdentity();
	              glu.gluLookAt(gameController.getPlayerXpos(),gameController.getPlayerYpos(),gameController.getPlayerZpos(),
	                      gameController.getPlayerXpos()+Math.cos(0.5*Math.PI-gameController.getPlayerDirection()),gameController.getPlayerYpos(),gameController.getPlayerZpos()+Math.sin(0.5*Math.PI-gameController.getPlayerDirection()),
	                      0,1,0);
	              //draw the rocket launcher                                  
                  float[] rocketLauncherPos=gameController.getRocketLauncherPos();
                  this.rocketLauncherTexture.bind();
                  gl.glPushMatrix();
                  gl.glTranslatef(rocketLauncherPos[0],rocketLauncherPos[1],rocketLauncherPos[2]);
                  gl.glRotatef(rocketLauncherPos[3]+180,0.0f,1.0f,0.0f);
                  gl.glScalef(0.03f/6.5536f,0.03f/6.5536f,0.03f/6.5536f);
                  this.rocketLauncherVertexSet.draw();                                                
                  gl.glPopMatrix();
                  
                  
	              softwareViewFrustumCullingPerformer.computeViewFrustum();	              
	              //draw here the objects in absolute coordinates	              
	              //draw the levelTextured level	              
	              this.levelTexture.bind();
	              //levelDrawTime=System.currentTimeMillis();
	              this.playerPositioning=networkViewSet.draw((float)gameController.getPlayerXpos(),(float)gameController.getPlayerYpos(),(float)gameController.getPlayerZpos(),(float)gameController.getPlayerDirection(),playerPositioning,softwareViewFrustumCullingPerformer);	              
	              //System.out.println("NETWORK VIEW SET DRAW TIME: "+(System.currentTimeMillis()-levelDrawTime));
	              //levelDrawTime=System.currentTimeMillis()-levelDrawTime;	              
	              //pushMessage("NVSD TIME: "+levelDrawTime,(int)(screenWidth*0.85),(int)(screenHeight*0.05));
	              int i,j,limit,xp,zp;
	              xp=(int)gameController.getPlayerXpos();
	              zp=(int)gameController.getPlayerZpos();
	              //System.out.println("[INFO] xp="+xp+" x="+gameController.getPlayerXpos());
	              //draw the artworks  
	              gl.glEnable(GL.GL_POLYGON_OFFSET_FILL);
	              if(!gameController.getPlayerWins())
	                  {this.artTexture1.bind();	                   
	                   this.artVertexSet1.draw();
	                   this.artVertexSet3.draw();
	                   this.artTexture2.bind();	          
	                   this.artVertexSet2.draw();
                       this.artVertexSet4.draw();
	                  }                          
	              else
	                  {this.artTexture3.bind();	                   
	                   this.artVertexSet1.draw();
                       this.artVertexSet3.draw();
	                   this.artTexture4.bind();	                   
	                   this.artVertexSet2.draw();
                       this.artVertexSet4.draw();
	                  }	
	              gl.glDisable(GL.GL_POLYGON_OFFSET_FILL);
	              //draw the bots
	              limit=0;      
	              FloatBuffer translation=BufferUtil.newFloatBuffer(gameController.getBotList().size()*3);
	              FloatBuffer rotation=BufferUtil.newFloatBuffer(gameController.getBotList().size()*4);
	              IntBuffer first=BufferUtil.newIntBuffer(gameController.getBotList().size());
	              IntBuffer count=BufferUtil.newIntBuffer(gameController.getBotList().size());         
	              for(BotModel bot:gameController.getBotList())
	                  if(bot.getHealth()==BotModel.startingHealth && 
	                 bot.getX()<=gameController.getPlayerXpos()+1638400 &&
	                 bot.getX()>=gameController.getPlayerXpos()-1638400 &&
	                 bot.getZ()<=gameController.getPlayerZpos()+1638400 &&
	                 bot.getZ()>=gameController.getPlayerZpos()-1638400)               
	                      {translation.put((float)bot.getX());
	                       translation.put((float)bot.getY());
	                       translation.put((float)bot.getZ());
	                       rotation.put((float)((Math.PI+gameController.getPlayerDirection())*(180/Math.PI)));
	                       rotation.put(0.0f);
	                       rotation.put(1.0f);
	                       rotation.put(0.0f);
	                       first.put(bot.getFace()*4);
	                       count.put(4);
	                       limit++;
	                      }
	              //draw the bots without damage here
	              //bug fix : allows to display objects on SiS 661 FX  
	              if(this.useAlphaTest)
	                  gl.glEnable(GL.GL_ALPHA_TEST);	                  	              
	              this.botTexture1.bind();	              
	              this.botVertexSet.multiDraw(translation,rotation,first,count,limit,false);      
	              translation.position(0);
	              rotation.position(0);
	              first.position(0);
	              count.position(0);
	              limit=0;
	              for(BotModel bot:gameController.getBotList())
	                  if(bot.isAlive() && bot.getHealth()<BotModel.startingHealth && 
	                 bot.getX()<=gameController.getPlayerXpos()+1638400 &&
	                 bot.getX()>=gameController.getPlayerXpos()-1638400 &&
	                 bot.getZ()<=gameController.getPlayerZpos()+1638400 &&
	                 bot.getZ()>=gameController.getPlayerZpos()-1638400)
	                  {translation.put((float)bot.getX());
	                   translation.put((float)bot.getY());
	                   translation.put((float)bot.getZ());
	                   rotation.put((float)((Math.PI+gameController.getPlayerDirection())*(180/Math.PI)));
	                   rotation.put(0.0f);
	                   rotation.put(1.0f);
	                   rotation.put(0.0f);
	                   first.put(bot.getFace()*4);
	                   count.put(4);
	                   limit++;
	                  }
	              //draw the bots with damage here
	              this.botTexture2.bind();                    
	              this.botVertexSet.multiDraw(translation,rotation,first,count,limit,false);
	              //draw the objects
	              this.objectsTexture.bind();
	              //avoid an ArrayOutOfBoundException in some corners of the level
	              for(i=Math.max(0,zp-25);i<Math.min(256,zp+25);i++)
	                  for(j=Math.max(0,xp-25);j<Math.min(256,xp+25);j++)
	                      {switch(gameController.getCollisionMap(i*256+j))
	                          {case AVOIDABLE_AND_UNBREAKABLE:
	                               {gl.glPushMatrix();
	                                gl.glTranslatef((j+0.5f),0.0f,(i+0.5f));
	                                this.unbreakableObjectVertexSet.draw();
	                                gl.glPopMatrix();
	                                break;
	                               }
	                           case FIXED_AND_BREAKABLE_BIG:  
	                               {gl.glPushMatrix();
	                                gl.glTranslatef((j+0.5f),0.0f,(i+0.5f));
	                                this.vendingMachineVertexSet.draw();
	                                gl.glPopMatrix();
	                                break;
	                               }                        
	                           case FIXED_AND_BREAKABLE_LIGHT:
	                               {gl.glPushMatrix();
	                                gl.glTranslatef((j+0.5f),0.0f,(i+0.5f));
	                                this.lampVertexSet.draw();
	                                gl.glPopMatrix();
	                                break;
	                               }
	                           case FIXED_AND_BREAKABLE_CHAIR:
	                               {gl.glPushMatrix();
	                                gl.glTranslatef((j+0.5f),0.0f,(i+0.5f));
	                                this.chairVertexSet.draw();
	                                gl.glPopMatrix();
	                                break;
	                               }
	                           case FIXED_AND_BREAKABLE_FLOWER:
	                               {gl.glPushMatrix();
	                                gl.glTranslatef((j+0.5f),0.0f,(i+0.5f));
	                                this.flowerVertexSet.draw();
	                                gl.glPopMatrix();
	                                break;
	                               }
	                           case FIXED_AND_BREAKABLE_TABLE:
	                               {gl.glPushMatrix();
	                                gl.glTranslatef((j+0.5f),0.0f,(i+0.5f));
	                                this.tableVertexSet.draw();
	                                gl.glPopMatrix();
	                                break;
	                               }
	                           case FIXED_AND_BREAKABLE_BONSAI:
	                               {gl.glPushMatrix();
	                                gl.glTranslatef((j+0.5f),0.0f,(i+0.5f));
	                                this.bonsaiVertexSet.draw();
	                                gl.glPopMatrix();
	                                break;
	                               }                  
	                          }
	                      }
	              //draw the impacts
	              gl.glEnable(GL.GL_POLYGON_OFFSET_FILL);              
	              impactTexture.bind();
	              for(Impact impact:gameController.getImpactList())
	                  {gl.glPushMatrix();
	                   gl.glTranslatef(impact.getX(),impact.getY(),impact.getZ());
	                   if(impact.getNx()<0)
	                       gl.glRotatef(90.0f,0.0f,1.0f,0.0f);
	                   else
	                       if(impact.getNx()>0)
	                           gl.glRotatef(-90.0f,0.0f,1.0f,0.0f);
	                   this.impactVextexSet.draw();	                   
	                   gl.glPopMatrix();
	                  }	              	                           
	              gl.glDisable(GL.GL_POLYGON_OFFSET_FILL);
	              //draw the rockets
	              this.rocketLauncherTexture.bind();
                  for(float[] rocket:gameController.getRocketList())
                      {gl.glPushMatrix();
                       gl.glTranslatef(rocket[0],rocket[1],rocket[2]);
                       gl.glRotatef(rocket[3],0.0f,1.0f,0.0f);
                       this.rocketVertexSet.draw();             
                       gl.glPopMatrix();                      
                      }
                  //draw the explosions if they are associated to a controller
                  Vector<Object3DView> uselessObjectsList = new Vector<Object3DView>();
                  for(Object3DView o3Dv:this.objectViewList)
                      if(o3Dv.getController()==null)
                          {//System.out.println("explosion [END]");
                           uselessObjectsList.add(o3Dv);
                          }
                      else
                          {o3Dv.draw();
                           //System.out.println("explosion [START]");
                          }
                  //remove useless shared 3D object views
                  this.objectViewList.removeAll(uselessObjectsList);
                  uselessObjectsList.clear();
	              if(this.useAlphaTest)
	                  gl.glDisable(GL.GL_ALPHA_TEST);         
	              gl.glDisable(GL.GL_DEPTH_TEST);
	              gl.glDisable(GL.GL_TEXTURE_2D);  	                                          
	              break;
	             }
	         case ABOUT_SCREEN:
	             {break;}
	         case OPTIONS_MENU:
	             {break;}
	        }	    
	    //2D display (but I save the previous projection matrix)
        gl.glMatrixMode(GL.GL_PROJECTION);
        gl.glPushMatrix();
        gl.glLoadIdentity();
        //2D display using gluOrtho2D
        glu.gluOrtho2D(0,screenWidth,0,screenHeight);       
        gl.glMatrixMode(GL.GL_MODELVIEW);
        gl.glLoadIdentity();       
        //it is not necessary to change the viewport here       
        //draw the panel with the data here : health, map, etc...
        switch(cycle)
            {case START_SCREEN:
                 {if(this.startingScreenTexture!=null)
                      {gl.glEnable(GL.GL_TEXTURE_2D);
                       this.startingScreenTexture.bind();
                       gl.glBegin(GL.GL_QUADS);            
                       gl.glTexCoord2i(0,1);
                       gl.glVertex2i(0,0);
                       gl.glTexCoord2i(1,1);
                       gl.glVertex2i(screenWidth,0);
                       gl.glTexCoord2i(1,0);
                       gl.glVertex2i(screenWidth,screenHeight);
                       gl.glTexCoord2i(0,0);
                       gl.glVertex2i(0,screenHeight);          
                       gl.glEnd();
                       gl.glDisable(GL.GL_TEXTURE_2D);                      
                       progressBar.setValue(loadProgress);
                       progressBar.display(drawable);
                      }
                  break;
                 }
             case MAIN_MENU:
                 {if(this.startingMenuTexture!=null)
                      {gl.glEnable(GL.GL_TEXTURE_2D);
                       this.startingMenuTexture.bind();
                       gl.glBegin(GL.GL_QUADS);             
                       gl.glTexCoord2f(0.0f,0.75f);
                       gl.glVertex2i(0,(int)(screenHeight*0.75));
                       gl.glTexCoord2f(1.0f,0.75f);
                       gl.glVertex2i(screenWidth,(int)(screenHeight*0.75));
                       gl.glTexCoord2f(1.0f,0.25f);
                       gl.glVertex2i(screenWidth,screenHeight);
                       gl.glTexCoord2f(0.0f,0.25f);
                       gl.glVertex2i(0,screenHeight);
                       gl.glEnd();
                       gl.glDisable(GL.GL_TEXTURE_2D);
                       if(!menu.isVisible())
                           menu.setVisible(true);
                       if(gameController.getBpause())
                           {//display something to show the game is paused                     
                            pushMessage("GAME PAUSED",(int)(screenWidth*0.45),(int)(screenHeight*0.7)); 
                            if(!menu.getItem(0).isEnabled())
                                menu.setEnabledIndex(0,true);
                           }
                       else
                           {if(menu.getItem(0).isEnabled())
                                menu.setEnabledIndex(0,false);
                            if(gameController.getPlayerHit())
                                {//display something to show the player died                  
                                 pushMessage("YOU FAILED!!!",(int)(screenWidth*0.45),(int)(screenHeight*0.7));
                                }
                           }
                       menu.display(drawable);
                      }                         
                  break;
                 }
             case GAME:
                 {pushMessage("HEALTH "+gameController.getHealth(),(int)(screenWidth*0.85),(int)(screenHeight*0.1));
                  //bug fix : division by zero
                  long dif=lnow-lbefore;
                  if(dif > 0)
                      {//System.out.println((1000L/(System.currentTimeMillis()-lnow))+" FPS");
                       final int FPS=(int)(1000L/(dif));
                       pushMessage(FPS+" FPS",(int)(screenWidth*0.85),(int)(screenHeight*0.15));
                       /*if(levelDrawTime>2)
                           {System.out.println(FPS+" FPS");
                            System.out.println("NVSD TIME: "+levelDrawTime);
                           }*/
                      }
                  if(gameController.getPlayerWins())
                      {pushMessage("C O N G R A T U L A T I O N S",(int)(screenWidth*0.45),(int)(screenHeight*0.5));
                       pushMessage("DEMO COMPLETED - COOL ART ESTABLISHED!",(int)(screenWidth*0.40),(int)(screenHeight*0.45));                 
                      }
                  else
                      {//draw the crosshair     
                       final int halfWidth=screenWidth/2,halfHeight=screenHeight/2;
                       gl.glColor3f(1.0f,0.0f,0.0f);
                       gl.glPushMatrix();
                       gl.glScalef(halfWidth,halfHeight,1.0f);
                       crosshairVertexSet.draw();
                       gl.glPopMatrix();
                       if(gameController.getPlayerYpos()<0)
                           {//draw the blood
                            //TODO: use dynamic vertex sets instead
                            int bloodHeight=screenHeight-(int)(screenHeight*-gameController.getPlayerYpos()/0.5f);
                            gl.glColor4f(1.0f,0.0f,0.0f,0.5f);
                            gl.glBegin(GL.GL_QUADS);
                            gl.glVertex2i(0,bloodHeight);
                            gl.glVertex2i(screenWidth,bloodHeight);
                            gl.glVertex2i(screenWidth,screenHeight);
                            gl.glVertex2i(0,screenHeight);
                            gl.glEnd();                            
                           }
                       gl.glColor3f(neutralColor[0],neutralColor[1],neutralColor[2]);
                      }
                  if(menu.isVisible())
                      menu.setVisible(false);
                  break;
                 }
             case ABOUT_SCREEN:
                 {pushMessage("ABOUT",(int)(screenWidth*0.45),(int)(screenHeight*0.9));
                  for(int i=0;i<aboutText.length;i++)
                      pushMessage(aboutText[i],(int)(screenWidth*0.05),(int)(screenHeight*(0.85-i*0.02)));
                  pushMessage("PRESS ANY KEY TO CONTINUE",(int)(screenWidth*0.40),(int)(screenHeight*0.05));
                  break;
                 }
             case OPTIONS_MENU:
                 {//display the options menu
                  pushMessage("PRESS ANY KEY TO CONTINUE",(int)(screenWidth*0.40),(int)(screenHeight*0.05));
                  if(configurationDetector!=null)
                      {pushMessage("OpenGL version: "+configurationDetector.getOpenGLVersion(),(int)(screenWidth*0.40),(int)(screenHeight*0.50));
                       pushMessage("alpha test: "+configurationDetector.isAlphaTestSupported(),(int)(screenWidth*0.40),(int)(screenHeight*0.45)); 
                       pushMessage("display list: "+configurationDetector.isDisplayListSupported(),(int)(screenWidth*0.40),(int)(screenHeight*0.40));
                       pushMessage("vertex array: "+configurationDetector.isVertexArraySupported(),(int)(screenWidth*0.40),(int)(screenHeight*0.35));
                       pushMessage("vertex buffer object: "+configurationDetector.isVBOsupported(),(int)(screenWidth*0.40),(int)(screenHeight*0.30));
                       pushMessage("multi draw array: "+configurationDetector.isMultiDrawSupported(),(int)(screenWidth*0.40),(int)(screenHeight*0.25));
                       pushMessage("shaders: "+configurationDetector.isShaderSupported(),(int)(screenWidth*0.40),(int)(screenHeight*0.20));
                       pushMessage("maximum texture size: "+configurationDetector.getMaxTextureSize(),(int)(screenWidth*0.40),(int)(screenHeight*0.15));
                      }
                  break;
                 }
            }
        lbefore=lnow;
        lnow=System.currentTimeMillis();       
        
        if(messageLine.size()>0)
            {textRenderer.begin3DRendering();
             textRenderer.setColor(0.5f,0.2f,0.4f,1.0f);
             for(int i=0;i<messageLine.size();i++)                                 
                 textRenderer.draw(messageLine.get(i),messageWidth.get(i).intValue(),messageHeight.get(i).intValue());
             textRenderer.end3DRendering();
             gl.glColor3f(neutralColor[0],neutralColor[1],neutralColor[2]);
            }
        messageLine.clear();
        messageHeight.clear();
        messageWidth.clear();         
        //restore the matrices for 3D display                            
        gl.glMatrixMode(GL.GL_PROJECTION);
        gl.glPopMatrix();
        gl.glMatrixMode(GL.GL_MODELVIEW);
	    try{canvas.swapBuffers();}
	    catch(GLException glex)
	    {glex.printStackTrace();
	     pushMessage("YOUR HARDWARE IS CURRENTLY NOT SUPPORTED BY THIS GAME",(int)(screenWidth*0.45),(int)(screenHeight*0.7));
	     //disable all items except the last one 
	     for(int i=0;i<menu.getItemCount()-1;i++)
	         menu.getItem(i).setEnabled(false);
	     //come back to the main menu
	     cycle=GameCycle.MAIN_MENU;
	    }
	    if(recSnapFilm || recSnapShot)
	        {if(recSnapShot)
	             recSnapShot=false;
	         //save screenshot in a targa file
	         boolean screenshotSuccessfullyDone;
	         try {final File screenshotFile=getScreenshotFile();
	              if(screenshotFile!=null)
	                  {Screenshot.writeToTargaFile(screenshotFile,screenWidth,screenHeight);
	                   screenshotSuccessfullyDone=true;
	                  }
	              else
	                  screenshotSuccessfullyDone=false;	                  
	             }
	         catch(IOException ioe)
	         {ioe.printStackTrace();
	          screenshotSuccessfullyDone=false;
	         }
	         if(!screenshotSuccessfullyDone)
	             pushMessage("The program can not save the screenshot!",(int)(screenWidth*0.45),(int)(screenHeight*0.5));
	        }
	    if(loadProgress < loadableItemCount)
	        {loadProgress=0;
	         if(this.artVertexSet1==null)
	             {this.artVertexSet1=vertexSetSeeker.getIStaticVertexSetInstance(gameController.getArtCoordinatesBuffer1(),GL.GL_QUADS);
	              return;
	             }
	         else
	             loadProgress+=1;
	         if(this.artVertexSet2==null)
	             {this.artVertexSet2=vertexSetSeeker.getIStaticVertexSetInstance(gameController.getArtCoordinatesBuffer2(),GL.GL_QUADS);
	              return; 
	             }
	         else
	             loadProgress+=1;
	         if(this.artVertexSet3==null)
                 {this.artVertexSet3=vertexSetSeeker.getIStaticVertexSetInstance(gameController.getArtCoordinatesBuffer3(),GL.GL_QUADS);
                  return; 
                 }
             else
                 loadProgress+=1;
	         if(this.artVertexSet4==null)
                 {this.artVertexSet4=vertexSetSeeker.getIStaticVertexSetInstance(gameController.getArtCoordinatesBuffer4(),GL.GL_QUADS);
                  return; 
                 }
             else
                 loadProgress+=1;
	         if(this.botVertexSet==null)
	             {this.botVertexSet=vertexSetSeeker.getIDynamicVertexSetInstance(gameController.getBotCoordinatesBuffer(),GL.GL_QUADS);	              
	              return;
	             }
	         else
                 loadProgress+=1;
	         if(this.unbreakableObjectVertexSet==null)
	             {this.unbreakableObjectVertexSet=vertexSetSeeker.getIStaticVertexSetInstance(gameController.getUnbreakableObjectCoordinatesBuffer(),GL.GL_QUADS);
	              return;
	             }
	         else
                 loadProgress+=1;
	         if(this.vendingMachineVertexSet==null)
	             {this.vendingMachineVertexSet=vertexSetSeeker.getIStaticVertexSetInstance(gameController.getVendingMachineCoordinatesBuffer(),GL.GL_QUADS);
	              return;
	             }
	         else
                 loadProgress+=1;
	         if(this.lampVertexSet==null)
	             {this.lampVertexSet=vertexSetSeeker.getIStaticVertexSetInstance(gameController.getLampCoordinatesBuffer(),GL.GL_QUADS);
	              return;
	             }
	         else
                 loadProgress+=1;
	         if(this.chairVertexSet==null)
	             {this.chairVertexSet=vertexSetSeeker.getIStaticVertexSetInstance(gameController.getChairCoordinatesBuffer(),GL.GL_QUADS);
	              return;
	             }
	         else
                 loadProgress+=1;
	         if(this.flowerVertexSet==null)
	             {this.flowerVertexSet=vertexSetSeeker.getIStaticVertexSetInstance(gameController.getFlowerCoordinatesBuffer(),GL.GL_QUADS);	              
	              return;
	             }
	         else
                 loadProgress+=1;
	         if(this.tableVertexSet==null)
	             {this.tableVertexSet=vertexSetSeeker.getIStaticVertexSetInstance(gameController.getTableCoordinatesBuffer(),GL.GL_QUADS);	              
	              return;
	             }
	         else
                 loadProgress+=1;
	         if(this.bonsaiVertexSet==null)
	             {this.bonsaiVertexSet=vertexSetSeeker.getIStaticVertexSetInstance(gameController.getBonsaiCoordinatesBuffer(),GL.GL_QUADS);
	              return;
	             }
	         else
                 loadProgress+=1;
	         if(this.rocketLauncherVertexSet==null)
	             {this.rocketLauncherVertexSet=vertexSetSeeker.getIStaticVertexSetInstance(gameController.getRocketLauncherCoordinatesBuffer(),GL.GL_QUADS);	              
	              return;
	             }
	         else
                 loadProgress+=1;	         
	         if(this.rocketVertexSet==null)
                 {this.rocketVertexSet=vertexSetSeeker.getIStaticVertexSetInstance(gameController.getRocketCoordinatesBuffer(),GL.GL_QUADS);
                  return;
                 }
             else
                 loadProgress+=1;
	         if(this.impactVextexSet==null)
	             {this.impactVextexSet=vertexSetSeeker.getIStaticVertexSetInstance(gameController.getImpactCoordinatesBuffer(),GL.GL_QUADS);	              
	              return;
	             }
	         else
	             loadProgress+=1;	         
	         if(this.crosshairVertexSet==null)
	             {this.crosshairVertexSet=vertexSetSeeker.getIStaticVertexSetInstance(gameController.getCrosshairCoordinatesBuffer(),GL.GL_LINES);	              
	              return;
	             }
	         else
	             loadProgress+=1;
	         loadProgress+=HealthPowerUpViewFactory.getInstance(false).getVertexSetsList().size();
	         loadProgress+=ExplosionViewFactory.getInstance(false).getVertexSetsList().size();	         
	         try{if(this.levelTexture==null)
	                 {this.levelTexture=GameIO.TextureFactory.getInstance().newTexture(getClass().getResource("/texture/wallTexture.png"),false,TextureIO.PNG);
	                  this.levelTexture.setTexParameteri(GL.GL_TEXTURE_PRIORITY,1);
	                  return;
	                 }
	             else
                     loadProgress+=1;
	             if(this.artTexture1==null)
	                 {this.artTexture1=GameIO.TextureFactory.getInstance().newTexture(getClass().getResource("/texture/wallArt1.png"),false,TextureIO.PNG);
	                  return;
	                 }
	             else
	                 loadProgress+=1;
	             if(this.artTexture2==null)     
	                 {this.artTexture2=GameIO.TextureFactory.getInstance().newTexture(getClass().getResource("/texture/wallArt2.png"),false,TextureIO.PNG);
	                  return;
	                 }
	             else
	                 loadProgress+=1;
	             if(this.artTexture3==null)     
	                 {this.artTexture3=GameIO.TextureFactory.getInstance().newTexture(getClass().getResource("/texture/wallArt3.png"),false,TextureIO.PNG);
	                  return;
	                 }
	             else
	                 loadProgress+=1;
	             if(this.artTexture4==null)     
	                 {this.artTexture4=GameIO.TextureFactory.getInstance().newTexture(getClass().getResource("/texture/wallArt4.png"),false,TextureIO.PNG);
	                  return;
	                 }
	             else
	                 loadProgress+=1;
	             if(this.objectsTexture==null)     
	                 {this.objectsTexture=GameIO.TextureFactory.getInstance().newTexture(getClass().getResource("/texture/objects.png"),false,TextureIO.PNG);
	                  return;
	                 }
	             else
	                 loadProgress+=1;
	             if(this.botTexture1==null)
	                 {this.botTexture1=GameIO.TextureFactory.getInstance().newTexture(getClass().getResource("/texture/bot1.png"),false,TextureIO.PNG);
	                  return;
	                 }
	             else
	                 loadProgress+=1;
	             if(this.botTexture2==null)
	                 {this.botTexture2=GameIO.TextureFactory.getInstance().newTexture(getClass().getResource("/texture/bot2.png"),false,TextureIO.PNG);
	                  return;
	                 }
	             else
	                 loadProgress+=1;
	             if(this.rocketLauncherTexture==null)
	                 {this.rocketLauncherTexture=GameIO.TextureFactory.getInstance().newTexture(getClass().getResource("/texture/rocketLauncher.png"),false,TextureIO.PNG);	                  
                      this.rocketLauncherTexture.setTexParameteri(GL.GL_TEXTURE_PRIORITY,1);
	                  return;
	                 }
	             else
	                 loadProgress+=1;
	             if(this.impactTexture==null)
	                 {this.impactTexture=GameIO.TextureFactory.getInstance().newTexture(getClass().getResource("/texture/wallRocketImpact.png"),false,TextureIO.PNG);
	                  return;
	                 }
	             else
	                 loadProgress+=1;
	             loadProgress+=HealthPowerUpViewFactory.getInstance(false).getTexturesList().size();
	             loadProgress+=ExplosionViewFactory.getInstance(false).getTexturesList().size();
	             if(this.networkViewSet==null)
	                 {this.networkViewSet=gameController.prepareNetworkViewSet();
	                  return; 
	                 }
	             else
	                 loadProgress+=1;	             
	            }
	         catch(IOException ioe)
	         {ioe.printStackTrace();}	         
	        }
	    else
	        {//it allows to display the menu only when both the model and the view are ready
	         if(cycle==GameCycle.START_SCREEN && gameController.isGameRunning())
	             cycle=GameCycle.MAIN_MENU;
	        }	    
    }

    public final void displayChanged(GLAutoDrawable drawable, boolean modeChanged, boolean deviceChanged){}

    public final void init(GLAutoDrawable drawable){
        final GL gl=drawable.getGL();
        configurationDetector=new ConfigurationDetector(gl);
        //get max texture size
        GL_MAX_TEXTURE_SIZE=configurationDetector.getMaxTextureSize();
        GameIO.TextureFactory.createFactory(GL_MAX_TEXTURE_SIZE);
        gl.glClearColor(1.0f,1.0f,1.0f,1.0f);
    	gl.glColor3f(neutralColor[0],neutralColor[1],neutralColor[2]);
    	gl.glHint(GL.GL_PERSPECTIVE_CORRECTION_HINT,GL.GL_NICEST);
    	gl.glHint(GL.GL_LINE_SMOOTH_HINT,GL.GL_NICEST);
    	gl.glHint(GL.GL_POINT_SMOOTH_HINT,GL.GL_NICEST);
    	gl.glHint(GL.GL_POLYGON_SMOOTH_HINT,GL.GL_NICEST);
    	/*gl.glViewport(-50,-50,100,100);*/	
    	/*gl.glClearDepth(1.0);
	    gl.glEnable(GL.GL_DEPTH_TEST);
	    gl.glDepthFunc(GL.GL_LESS);*/   	
    	gl.glEnable(GL.GL_CULL_FACE);
    	gl.glCullFace(GL.GL_BACK);	
    	gl.glMatrixMode(GL.GL_PROJECTION);
    	gl.glLoadIdentity();
    	/*modify the projection matrix only when in 3D full mode*/
    	final float baseSize=50.0f/65536.0f;
    	gl.glFrustum(-baseSize,baseSize,-baseSize,baseSize,baseSize,baseSize*100000);
    	//glu.gluPerspective(45.0f,4.0f/3.0f,0.2f,2000f);
    	//softwareViewFrustumCullingPerformer=new SoftwareViewFrustumCullingPerformer(gl,0);
    	softwareViewFrustumCullingPerformer=new DummyViewFrustumCullingPerformer(gameController);
    	gl.glMatrixMode(GL.GL_MODELVIEW);
    	gl.glLoadIdentity();
    	//this.lStartPhase=System.currentTimeMillis()+15000;
    	this.lnow=System.currentTimeMillis();    	
        this.textRenderer=new TextRenderer(new Font("SansSerif",Font.BOLD,12));
    	try{this.startingScreenTexture=GameIO.TextureFactory.getInstance().newTexture(getClass().getResource("/texture/starting_screen_bis.png"),false,TextureIO.PNG);          	        
    	    this.startingMenuTexture=GameIO.TextureFactory.getInstance().newTexture(getClass().getResource("/texture/starting_menu.png"),false,TextureIO.PNG);   	    
    	   }
        catch(IOException ioe){ioe.printStackTrace();}
    	this.useAlphaTest=configurationDetector.isAlphaTestSupported();
    	gl.setSwapInterval(0);  	
    	gl.glPolygonOffset(glPolygonOffsetFactor,glPolygonOffsetUnit);
    	if(this.useAlphaTest)           
            gl.glAlphaFunc(GL.GL_EQUAL,1);    	  	
    	initMainMenu();
    	
    }
    
    private final void initMainMenu(){
        this.menu=new GLMenu(0.9f*screenWidth/2.0f,1.1f*screenHeight/2.0f,false,new TextRenderer(new Font("SansSerif",Font.BOLD,42)));
        GLMenuItem resumeMenuItem=new GLMenuItem("resume");
        resumeMenuItem.addActionListener(new ResumeGameActionListener(this));
        this.menu.addGLMenuItem(resumeMenuItem);
        GLMenuItem newMenuItem=new GLMenuItem("new game");
        newMenuItem.addActionListener(new NewGameActionListener(this));
        this.menu.addGLMenuItem(newMenuItem);
        GLMenuItem optionsMenuItem=new GLMenuItem("options");
        optionsMenuItem.addActionListener(new OptionsActionListener(this));
        this.menu.addGLMenuItem(optionsMenuItem);
        GLMenuItem loadMenuItem=new GLMenuItem("load game");      
        this.menu.addGLMenuItem(loadMenuItem);
        GLMenuItem saveMenuItem=new GLMenuItem("save game");       
        this.menu.addGLMenuItem(saveMenuItem);
        GLMenuItem aboutMenuItem=new GLMenuItem("about");
        AboutActionListener aboutActionListener=new AboutActionListener(this);
        this.aboutText=aboutActionListener.getAboutText().split(System.getProperty("line.separator"));
        aboutMenuItem.addActionListener(aboutActionListener);
        this.menu.addGLMenuItem(aboutMenuItem);
        GLMenuItem quitMenuItem=new GLMenuItem("quit game");
        quitMenuItem.addActionListener(new QuitGameActionListener(gameController));
        this.menu.addGLMenuItem(quitMenuItem);       
    }

    /*
     * As Yannick wants to play in windowed mode, I have to handle this case
     * (non-Javadoc)
     * @see javax.media.opengl.GLEventListener#reshape(javax.media.opengl.GLAutoDrawable, int, int, int, int)
     */
    public final void reshape(GLAutoDrawable drawable, int x, int y, int width, int height){
        final GL gl=drawable.getGL();
        gl.glMatrixMode(GL.GL_PROJECTION);
        gl.glLoadIdentity();
        float aspect=(float)width/(float)height;
        //gl.glFrustum(-37.5*aspect,37.5*aspect,-50,50,50,5000000);
        //glu.gluPerspective(45.0f,aspect,0.2f,2000f);
        final float baseSize=37.5f*aspect/65536.0f;
        gl.glFrustum(-baseSize,baseSize,-baseSize,baseSize,baseSize,baseSize*100000);
        softwareViewFrustumCullingPerformer.updateProjectionMatrix();
        gl.glMatrixMode(GL.GL_MODELVIEW);
    }
    
    private final void pushMessage(String message,int width,int height){
    	//fill the message queue
    	messageLine.add(message);
    	messageWidth.add(width);
    	messageHeight.add(height);	
    }
    
    public final void pushInfoMessage(String message){
        pushMessage(message,(int)(screenWidth*0.45),(int)(screenHeight*0.9));       
    }
    
    public final File getScreenshotFile(){
        File f=null;
        if(snapDirectory!=null)
            {final String directoryPath=snapDirectory.getAbsolutePath()+System.getProperty("file.separator");
             //try to find an unused abstract pathname
             for(int i=0;i<Integer.MAX_VALUE;i++)
                 if(!(f=new File(directoryPath+"snapshot_"+i+".tga")).exists())
                     {try {//then create a file with this pathname
                           f.createNewFile();                                 
                          }
                      catch(IOException ioe)
                      {ioe.printStackTrace();                     
                       f=null;
                      }
                      break;
                     }
                 else
                     f=null;           
            }   	
    	return(f);
    }
    
    final GameCycle getCycle(){
        return(cycle);
    }
    
    final void setCycle(GameCycle cycle){
        this.cycle=cycle;
    }

    final void setRecSnapFilm(boolean recSnapFilm){
        this.recSnapFilm=recSnapFilm;
    }
    
    final void setRecSnapShot(boolean recSnapShot){
        this.recSnapShot=recSnapShot;
    }

    final boolean getRecSnapFilm(){
        return(recSnapFilm);
    }
    
    final boolean getRecSnapShot(){
        return(recSnapShot);
    }
    
    final GLMenu getGLMenu(){
        return(menu);
    }
    
    final void addNewExplosion(ExplosionView ev){
        this.objectViewList.add(ev);
    }
    
    final void addNewItem(HealthPowerUpView hpuv){
        this.objectViewList.add(hpuv);
    }

    public static final int getGL_MAX_TEXTURE_SIZE(){
        return GL_MAX_TEXTURE_SIZE;
    }
    
    /*private final void removeUselessObjectViews(){
        Vector<Object3DView> removedObjects=new Vector<Object3DView>();
        for(Object3DView o3v:objectViewList)
            if(o3v.getController()==null)
                removedObjects.add(o3v);
        objectViewList.removeAll(removedObjects);
    }*/
}
