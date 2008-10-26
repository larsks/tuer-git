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

package md3;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import javax.media.opengl.GL;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.GLCanvas;
import javax.media.opengl.GLCapabilities;
import javax.media.opengl.GLEventListener;
import javax.media.opengl.glu.GLU;

class Renderer implements KeyListener, GLEventListener{


    private boolean[] keys;
    
    private MD3Model g_Model;
    
    private float g_RotateX;	
    		
    private float g_RotationSpeed;
    			
    private float g_TranslationZ;
    			
    private boolean g_RenderMode;
    
    private GLU glu;
    			
    private GL gl;


    Renderer(){
        keys=new boolean[256];
	g_Model=new MD3Model();
	g_RotateX=0.0f;
	g_RotationSpeed=0.1f;	
	g_TranslationZ=-100.0f;
	g_RenderMode=true;
	glu=new GLU();
	gl=null;
	
    }


    public void init(GLAutoDrawable drawable){
        gl=drawable.getGL();
	g_Model.setGL(gl);	        
        //This will clear the background color to Black
        gl.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);  
	gl.glHint(GL.GL_PERSPECTIVE_CORRECTION_HINT,GL.GL_NICEST);
	gl.glHint(GL.GL_LINE_SMOOTH_HINT,GL.GL_NICEST);
	gl.glHint(GL.GL_POINT_SMOOTH_HINT,GL.GL_NICEST);
	gl.glHint(GL.GL_POLYGON_SMOOTH_HINT,GL.GL_NICEST);
        gl.glEnable(GL.GL_TEXTURE_2D);// Enables Texture Mapping
        gl.glEnable(GL.GL_DEPTH_TEST);// Enables Depth Testing                                
        /*try {g_Model.LoadModel(gl,glu,"models/players/warpspider","lower","lower_Red","upper","upper_Red","head","head_Red");
             // Load the gun and attach it to our character
             g_Model.LoadWeapon(gl, glu,"models/players/warpspider" , "railgun");
            }
        catch (IOException ioe)
        {System.err.println("Failed to load model data.");}*/
	/*try {*/g_Model.loadModel("lara","lara");/*
             // Load the gun and attach it to our character
             */
	     g_Model.loadWeapon("lara","railgun");
            /*}
        catch (IOException ioe)
        {System.err.println("Failed to load model data.");}*/
	/*try {*///g_Model.loadModel("harley","harley");
             // Load the gun and attach it to our character
             //g_Model.loadWeapon("harley","plasma");
           /* }
        catch (IOException ioe)
        {System.err.println("Failed to load model data.");}*/
	// Set the standing animation for the torso
	g_Model.setTorsoAnimation("TORSO_STAND");

	// Set the walking animation for the legs
	g_Model.setLegsAnimation("LEGS_WALK");
	//g_Model.analyseModel();
        gl.glEnable(GL.GL_CULL_FACE);// Turn back face culling on
        gl.glCullFace(GL.GL_FRONT);  // Quake3 uses front face culling apparently
        //gl.glEnable(GL.GL_TEXTURE_2D);// Enables Texture Mapping
	gl.glClearDepth(1.0);
	gl.glDepthFunc(GL.GL_LEQUAL);
        //gl.glEnable(GL.GL_DEPTH_TEST);// Enables Depth Testing
    }

    /** Called when the display mode has been changed.  
    * @param gLDrawable The GLDrawable object.
    * @param modeChanged Indicates if the video mode has changed.
    * @param deviceChanged Indicates if the video device has changed.
    */
    public void displayChanged(GLAutoDrawable gLDrawable, boolean modeChanged, boolean deviceChanged){}

    public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height){                    
       if(height <= 0) // avoid a divide by zero error!
          height = 1;
       final float h = (float)width / (float)height;
       gl.glViewport(0, 0, width, height);
       gl.glMatrixMode(GL.GL_PROJECTION);
       gl.glLoadIdentity();
       glu.gluPerspective(45.0f, h, 1.0, 2000.0);
       gl.glMatrixMode(GL.GL_MODELVIEW);
       gl.glLoadIdentity();
    }    

    public void display(GLAutoDrawable drawable){                        
        gl.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT);// Clear The Screen And The Depth Buffer
        gl.glLoadIdentity();// Reset The matrix         
        glu.gluLookAt(0, 5.5f,g_TranslationZ,0, 5.5f,0,0,1,0);                  
        gl.glRotatef(g_RotateX, 0f, 1.0f, 0f);// Rotate the object around the Y-Axis
        g_RotateX += g_RotationSpeed;    // Increase the speed of rotation        
        // Now comes the moment we have all been waiting for!  Below we draw our character.
        gl.glColor3f(1.f, 1.f, 1.f);
        g_Model.draw();//  Render our character to the screen	
        gl.glFlush();
        ProcessKeyboard(gl); 	
    }


    private void ProcessKeyboard(GL gl){
    // Left Arrow Key - Spins the model to the left
    // Right Arrow Key - Spins the model to the right
    // Up Arrow Key - Moves the camera closer to the model
    // Right Arrow Key - Moves the camera farther away from the model
    // W - Changes the Render mode from normal to wireframe.  
    // R - Cycles through the upper (torso) animations
    // L - Cycles through the lower (legs) animations       
       if(keys[KeyEvent.VK_LEFT])
       {			
          g_RotationSpeed -= 0.05f;// Decrease the rotation speed (eventually rotates left)
          keys[KeyEvent.VK_LEFT] = false;
       }

       if(keys[KeyEvent.VK_RIGHT])
       {
          g_RotationSpeed += 0.05f;// Increase the rotation speed (rotates right)
          keys[KeyEvent.VK_RIGHT] = false;
       }

       if(keys[KeyEvent.VK_UP])
       {
          g_TranslationZ += 2;// Move the camera position forward along the Z axis
          keys[KeyEvent.VK_UP] = false;
       }

       if(keys[KeyEvent.VK_DOWN])
       {
          g_TranslationZ -= 2;// Move the camera position back along the Z axis
          keys[KeyEvent.VK_DOWN] = false;
       }

       if(keys['W'])
           {g_RenderMode=!g_RenderMode;// Change the rendering mode
            // Change the rendering mode to and from lines or triangles
            if(g_RenderMode) 				
                // Render the triangles in fill mode		
                gl.glPolygonMode(GL.GL_FRONT_AND_BACK, GL.GL_FILL);	
            else 
                // Render the triangles in wire frame mode
                gl.glPolygonMode(GL.GL_FRONT_AND_BACK, GL.GL_LINE);	
            keys['W'] = false;
           }
       if(keys['L'])
           {increaseCharacterAnimation(g_Model,MD3Model.kLower);
	    keys['L'] = false;
	   }
       if(keys['R'])
           {increaseCharacterAnimation(g_Model,MD3Model.kUpper);
	    keys['R'] = false;	    
	   }
    }

    void increaseCharacterAnimation(MD3Model pCharacter,int whichPart){
	String strWindowTitle="";
	Model3D pModel,pUpper,pLower;

	// This function doesn't have much to do with the character animation, but I
	// created it so that we can cycle through each of the animations to see how
	// they all look.  You can press the right and left mouse buttons to cycle through
	// the torso and leg animations.  If the current animation is the end animation,
	// it cycles back to the first animation.  This function takes the character you
	// want, then the define (kLower, kUpper) that tells which part to change.

	// Here we store pointers to the legs and torso, so we can display their current anim name
	pLower=pCharacter.getModel(MD3Model.kLower);
	pUpper=pCharacter.getModel(MD3Model.kUpper);

	// This line gives us a pointer to the model that we want to change
	pModel=pCharacter.getModel(whichPart);

	// To cycle through the animations, we just increase the model's current animation
	// by 1.  You'll notice that we also mod this result by the total number of
	// animations in our model, to make sure we go back to the beginning once we reach
	// the end of our animation list.  

	// Increase the current animation and mod it by the max animations
	pModel.setCurrentAnim((pModel.getCurrentAnim() + 1) % (pModel.getNumOfAnimations()));

	// Set the current frame to be the starting frame of the new animation
	pModel.setNextFrame(pModel.getPAnimations().get(pModel.getCurrentAnim()).getStartFrame());
        //pModel.setCurrentFrame(pModel.getPAnimations().get(pModel.getCurrentAnim()).getStartFrame());
	// (* NOTE *) Currently when changing animations, the character doesn't immediately
	// change to the next animation, but waits till it finishes the current animation
	// and slowly blends into the next one.  If you want an immediate switch, change
	// the pModel-nextFrame to pModel->currentFrame.

	// Display the current animations in our window's title bar
	/*strWindowTitle="www.GameTutorials.com - Md3 Animation:   Lower: "+
	pLower.getPAnimations().get(pLower.getCurrentAnim()).getStrName()+"  Upper: "+ 
	pUpper.getPAnimations().get(pUpper.getCurrentAnim()).getStrName();*/
        strWindowTitle="Lower: "+
	pLower.getPAnimations().get(pLower.getCurrentAnim()).getStrName()+"  Upper: "+ 
	pUpper.getPAnimations().get(pUpper.getCurrentAnim()).getStrName();
	// Set the window's title bar to our new string of animation names
	//System.out.println("title : "+strWindowTitle);
    }

    // Invoked when a key has been typed. This event occurs when a key press is followed by a key release. 
    public void keyTyped(KeyEvent e){}

    // Invoked when a key has been pressed.
    public void keyPressed(KeyEvent e){
       if(e.getKeyCode()<250)		// only interested in first 250 key codes
          keys[e.getKeyCode()]=true;	
    }

    // Invoked when a key has been released. 
    public void keyReleased(KeyEvent e){
       if(e.getKeyCode()<250)		// only interested in first 250 key codes
          keys[e.getKeyCode()]=false;	
    }
}
