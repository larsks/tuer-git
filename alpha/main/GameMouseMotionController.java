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


import java.awt.AWTException;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.Robot;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

/**
 * This class has the role of being the controller 
 * (in the meaning of the design pattern "MVC") of
 * the keyboard events. It is a tool which is used by the
 * view and the model to communicate together. 
 * It avoids us to modify the model when we modify the view
 * and vice versa. It only handles the events related on
 * the mouse actions, including motions and clicks.
 * It uses a Robot to move the cursor which may be a 
 * problem when using it inside an applet, this kind of
 * action may be stricty restricted by the security manager
 *
 * @author Julien Gouesse & Riven (www.javagaming.org)
 */

public class GameMouseMotionController implements MouseListener,MouseMotionListener{

    
    private Robot robot;
    
    private int centerx;
    
    private int centery;
    
    private GameGLView gameGLEventController;
    
    
    public GameMouseMotionController(GameGLView gameGLEventController){
        this.gameGLEventController=gameGLEventController;       
        try{
            robot=new Robot(gameGLEventController.getGameController().getGraphicsConfiguration().getDevice());
           }
        catch(AWTException e)
        {System.out.println("problem during new Robot() call : "+e);}		
        this.centerx=gameGLEventController.getGameController().getScreenWidth()/2;
        this.centery=gameGLEventController.getGameController().getScreenHeight()/2;
        this.robot.mouseMove(centerx,centery);	
    }
    
    
    public void mouseClicked(MouseEvent me){}
    
    public void mouseEntered(MouseEvent me){}
    
    public void mouseExited(MouseEvent me){}
    
    public void mousePressed(MouseEvent me){       
	    switch(me.getButton())
	        {case MouseEvent.BUTTON1:
	             {if(gameGLEventController.getCycle()==GameCycle.GAME)
	                  {if(!gameGLEventController.getGameController().getPlayerHit())
	                       gameGLEventController.getGameController().tryLaunchPlayerRocket();
	                  }				                   
	              break;
	             }
	         case MouseEvent.BUTTON2:
	             {break;}
	         case MouseEvent.BUTTON3:
	             {break;}
	        }	
    }
    
    public void mouseReleased(MouseEvent e){}
           
    public void mouseMoved(MouseEvent me){}
    
    /**
     * Contribution from Riven
     * @return
     */
    public final Point getDelta(){       
       Point pointer=MouseInfo.getPointerInfo().getLocation();
       int xDelta=pointer.x-centerx;
       int yDelta=pointer.y-centery;
       if(xDelta==0 && yDelta==0) 
           {// robot caused this OR user did not do anything
            return(new Point(0,0));
           }
       else
           {robot.mouseMove(centerx, centery);
            return(new Point(xDelta, yDelta));
           }      
    }
    
    public void mouseDragged(MouseEvent me){
        mouseMoved(me);
    }

}
