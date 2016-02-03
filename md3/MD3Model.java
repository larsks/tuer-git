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

/*
 * Author: Ron Sullivan (modified by Thomas Hourdel).
 * E-mail: thomas.hourdel@libertysurf.fr
 */


import java.awt.geom.*;
import java.awt.Image;
import java.awt.Graphics2D;
import java.awt.image.*;
import java.io.*;
import java.nio.*;
import java.util.StringTokenizer;
import java.util.ArrayList;

import com.jogamp.opengl.GL;

import com.jogamp.common.nio.Buffers;


class MD3Model{
  		

	  /** This stores the ID for the legs model. **/
	static final int kLower = 0;

	  /** This stores the ID for the torso model. **/
	static final int kUpper = 1;

	  /** This stores the ID for the head model. **/
	static final int kHead = 2;

	  /** This stores the ID for the weapon model. **/
	static final int kWeapon = 3;

	  /** The first twirling death animation. **/
	static final int BOTH_DEATH1 = 0;

	  /** The end of the first twirling death animation. **/
	static final int BOTH_DEAD1 = 1;

	  /** The second twirling death animation. **/
	static final int BOTH_DEATH2 = 2;

	  /** The end of the second twirling death animation. **/
	static final int BOTH_DEAD2 = 3;

	  /** The back flip death animation. **/
	static final int BOTH_DEATH3 = 4;

	  /** The end of the back flip death animation. **/
	static final int BOTH_DEAD3 = 5;

	  /** The torso's gesturing animation. **/
	static final int TORSO_GESTURE = 6;
	
	  /** The torso's attack1 animation. **/
	static final int TORSO_ATTACK = 7;

	  /** The torso's attack2 animation. **/
	static final int TORSO_ATTACK2 = 8;

	  /** The torso's weapon drop animation. **/
	static final int TORSO_DROP = 9;

	  /** The torso's weapon pickup animation. **/
	static final int TORSO_RAISE = 10;

	  /** The torso's idle stand animation. **/
	static final int TORSO_STAND = 11;

	  /** The torso's idle stand2 animation. **/
	static final int TORSO_STAND2 = 12;

	  /** The legs's crouching walk animation. **/
	static final int LEGS_WALKCR = 13;

	  /** The legs's walk animation. **/
	static final int LEGS_WALK = 14;

	  /** The legs's run animation. **/
	static final int LEGS_RUN = 15;

	  /** The legs's running backwards animation. **/
	static final int LEGS_BACK = 16;

	  /** The legs's swimming animation. **/
	static final int LEGS_SWIM = 17;
	
	  /** The legs's jumping animation. **/
	static final int LEGS_JUMP = 18;

	  /** The legs's landing animation. **/
	static final int LEGS_LAND = 19;

	  /** The legs's jumping back animation. **/
	static final int LEGS_JUMPB = 20;

	  /** The legs's landing back animation. **/
	static final int LEGS_LANDB = 21;

	  /** The legs's idle stand animation. **/
	static final int LEGS_IDLE = 22;

	  /** The legs's idle crouching animation. **/
	static final int LEGS_IDLECR = 23;

	  /** The legs's turn animation. **/
	static final int LEGS_TURN = 24;

	  /** The define for the maximum amount of animations. **/
	static final int MAX_ANIMATIONS = 200;

	  /** The header data. **/
	private MD3Header m_Header;

	  /** The skin name data. **/
	private MD3Skin m_pSkins[];

	  /** The texture coordinates. **/
	private MD3TexCoord m_pTexCoords[];

	  /** Face/Triangle data. **/
	private MD3Face m_pTriangles[];

	  /** Vertex/UV indices. **/
	private MD3Triangle m_pVertices[];

	  /** This stores the bone data. **/
	private MD3Bone m_pBones[];

	  /** Model for the character's head. **/
	private Model3D m_Head;

	  /** Model for the character's upper body parts. **/
	private Model3D m_Upper;

	  /** Model for the character's lower body parts. **/
	private Model3D m_Lower;

	  /** This store the players weapon model (optional load). **/
	private Model3D m_Weapon;

	  /** The maximum amount of textures to load. **/
	private final static int MAX_TEXTURES = 20;
	
	  /** All textures. **/
	private int[] m_Textures = new int[MAX_TEXTURES];	
	
	  /** This stores the texture array for each of the textures assigned to this model. **/
	private ArrayList<String> strTextures = new ArrayList<>();

	  /** Global file content. **/
	private byte[] fileContents;
	
	  /** Pointer use for file browsing. **/
	private int m_FilePointer = 0;

        private GL gl;

	/** MD3Model constructor.
	 */

	MD3Model()
	{
		m_Head = new Model3D();
		m_Upper = new Model3D();
		m_Lower = new Model3D();
		m_Weapon = new Model3D();
		this.gl=null;
	}

        void setGL(GL gl){
	    this.gl=gl;
	}


	/** This returns true if the string strSubString is inside of strString.
	 *  @param strString Main string.
	 *  @param strSubString The string to find.
	 *  @return True if the string strSubString is inside of strString.
	 */

	private final boolean IsInString(String strString, String strSubString)
	{
		  // Grab the starting index where the sub string is in the original string
		int index = strString.indexOf(strSubString);

		  // Make sure the index returned was valid
		if(index >= 0 && index < strString.length())
			return true;

		  // The sub string does not exist in strString.
		return false;
	}



	/* File browsing utilities methods.
	 */

	final int byte2byte()
	{
		int b1 = (fileContents[m_FilePointer  ] & 0xFF);
		m_FilePointer += 1;
		return (b1);
	}



	final short byte2short()
	{
		int s1 = (fileContents[m_FilePointer  ] & 0xFF);
		int s2 = (fileContents[m_FilePointer+1] & 0xFF) << 8;
		m_FilePointer += 2;
		return ((short)(s1 | s2));
	}



	final int byte2int()
	{
		int i1 = (fileContents[m_FilePointer  ] & 0xFF);
		int i2 = (fileContents[m_FilePointer+1] & 0xFF) <<  8;
		int i3 = (fileContents[m_FilePointer+2] & 0xFF) << 16;
		int i4 = (fileContents[m_FilePointer+3] & 0xFF) << 24;
		m_FilePointer += 4;
		return (i1 | i2 | i3 | i4);
	}



	final float byte2float()
	{
		return Float.intBitsToFloat(byte2int());
	}



	final String byte2string(int size)
	{
		for(int i = m_FilePointer; i < m_FilePointer + size; i++)
		{
			if((fileContents[i] & 0xFF)== (byte)0)
				return new String(fileContents, m_FilePointer, i - m_FilePointer);
		}

		return new String(fileContents,m_FilePointer, size);
	}

	
        void increaseFilePointer(int offset){
            m_FilePointer +=offset;
        }
	/** This returns a specific model from the character (kLower, kUpper, kHead, kWeapon).
	 *  @param whichPart Wanted part ID.
	 *  @return The wanted model.
	 */

	final Model3D getModel(int whichPart)
	{
		  // Return the legs model if desired
		if(whichPart == kLower) 
			return m_Lower;

		  // Return the torso model if desired
		if(whichPart == kUpper) 
			return m_Upper;

		  // Return the head model if desired
		if(whichPart == kHead) 
			return m_Head;

		  // Return the weapon model
		return m_Weapon;
	}


	
	/** This loads the md3 model from the given path and character name.
	 *  @param strPath Model's path.
	 *  @param strModel Model's name.
	 */

	final void loadModel(String strPath, String strModel)
	{
		String strLowerModel;					// This stores the file name for the lower.md3 model
		String strUpperModel;					// This stores the file name for the upper.md3 model
		String strHeadModel;					// This stores the file name for the head.md3 model
		String strLowerSkin;					// This stores the file name for the lower.md3 skin
		String strUpperSkin;					// This stores the file name for the upper.md3 skin
		String strHeadSkin;						// This stores the file name for the head.md3 skin

		  // Store the correct files names for the .md3 and .skin file for each body part.
		  // We concatinate this on top of the path name to be loaded from.
		strLowerModel = strPath + "/" + strModel + "_lower.md3";
		strUpperModel = strPath + "/" + strModel + "_upper.md3";
		strHeadModel = strPath + "/" + strModel + "_head.md3";
		
		// Get the skin file names with their path
		strLowerSkin = strPath + "/" + strModel + "_lower.skin";
		strUpperSkin = strPath + "/" + strModel + "_upper.skin";
		strHeadSkin = strPath + "/" + strModel + "_head.skin";

		  // Load the head mesh (*_head.md3) and make sure it loaded properly
		if(!importMD3(m_Head, strHeadModel))
		{
			System.out.println("[Error]: unable to load the HEAD part from model \"" + strModel + "\".");
			System.exit(0);
		}

		  // Load the upper mesh (*_head.md3) and make sure it loaded properly
		if(!importMD3(m_Upper, strUpperModel))		
		{
			System.out.println("[Error]: unable to load the UPPER part from model \"" + strModel + "\".");
			System.exit(0);
		}

		  // Load the lower mesh (*_lower.md3) and make sure it loaded properly
		if(!importMD3(m_Lower, strLowerModel))
		{
			System.out.println("[Error]: unable to load the LOWER part from model \"" + strModel + "\".");
			System.exit(0);
		}

		  // Load the lower skin (*_upper.skin) and make sure it loaded properly
		if(!loadSkin(m_Lower, strLowerSkin))
		{
			System.out.println("[Error]: unable to load the LOWER part from model's skin \"" + strModel + "\".");
			System.exit(0);
		}

		  // Load the upper skin (*_upper.skin) and make sure it loaded properly
		if(!loadSkin(m_Upper, strUpperSkin))
		{
			System.out.println("[Error]: unable to load the UPPER part from model's skin \"" + strModel + "\".");
			System.exit(0);
		}

		  // Load the head skin (*_head.skin) and make sure it loaded properly
		if(!loadSkin(m_Head, strHeadSkin))
		{
			System.out.println("[Error]: unable to load the HEAD part from model's skin \"" + strModel + "\".");
			System.exit(0);
		}

		  // Load the lower, upper and head textures.  
		loadModelTextures(m_Lower, strPath);
		loadModelTextures(m_Upper, strPath);
		loadModelTextures(m_Head, strPath);

		  // Add the path and file name prefix to the animation.cfg file
		String strConfigFile = strPath + "/" + strModel + "_animation.cfg";

		  // Load the animation config file (*_animation.config) and make sure it loaded properly
		if(!loadAnimations(strConfigFile))
		{
			System.out.println("[Error]: unable to load the animation config file from model \"" + strModel + "\".");
			System.exit(0);
		}

		  // Link the lower body to the upper body when the tag "tag_torso" is found in our tag array
		linkModel(m_Lower, m_Upper, "tag_torso");

		  // Link the upper body to the head when the tag "tag_head" is found in our tag array
		linkModel(m_Upper, m_Head, "tag_head");		
	}



	/** This loads a md3 weapon model from the given path and weapon name.
	 *  @param strPath Weapon's path.
	 *  @param strModel Weapon's name.
	 */

	final void loadWeapon(String strPath, String strModel)
	{
		String strWeaponModel;					// This stores the file name for the weapon model
		String strWeaponShader;					// This stores the file name for the weapon shader.

		  // Concatenate the path and model name together
		strWeaponModel = strPath + "/" + strModel + ".md3";

		  // Load the weapon mesh (*.md3) and make sure it loaded properly
		if(!importMD3(m_Weapon, strWeaponModel))
		{
			System.out.println("[Error]: unable to load the weapon model \"" + strModel + "\".");
			System.exit(0);
		}

		  // Add the path, file name and .shader extension together to get the file name and path
		strWeaponShader = strPath + "/" + strModel + ".shader";

		  // Load our textures associated with the gun from the weapon shader file
		if(!loadShader(m_Weapon, strWeaponShader))
		{
			System.out.println("[Error]: unable to load the shader for weapon \"" + strModel + "\".");
			System.exit(0);
		}

		  // We should have the textures needed for each weapon part loaded from the weapon's
		  // shader, so let's load them in the given path.
		loadModelTextures(m_Weapon, strPath);

		  // Link the weapon to the model's hand that has the weapon tag
		linkModel(m_Upper, m_Weapon, "tag_weapon");
	}



	/** This loads the textures for the current model passed in with a directory.
	 *  @param pModel Current model.
	 *  @param strPath Source path.
	 */

	private final void loadModelTextures(Model3D pModel, String strPath)
	{
		  // Go through all the materials that are assigned to this model
		for(int i = 0; i < pModel.getNumOfMaterials(); i++)
		{
			  // Check to see if there is a file name to load in this material
			if(pModel.getPMaterials().get(i).getStrFile() != null)
			{
				  // Create a boolean to tell us if we have a new texture to load
				boolean bNewTexture = true;

				  // Go through all the textures in our string list to see if it's already loaded
				for(int j = 0; j < strTextures.size(); j++)
				{
					  // If the texture name is already in our list of texture, don't load it again.
					if(pModel.getPMaterials().get(i).getStrFile().equals(strTextures.get(j)))
					{
						  // We don't need to load this texture since it's already loaded
						bNewTexture = false;

						  // Assign the texture index to our current material textureID.
						pModel.getPMaterials().get(i).setTextureId(j);
					}
				}

				  // Make sure before going any further that this is a new texture to be loaded
				if(bNewTexture == false)
					continue;
				
				String strFullPath;

				  // Add the file name and path together so we can load the texture
				strFullPath = strPath + "/" + pModel.getPMaterials().get(i).getStrFile();

				  // We pass in a reference to an index into our texture array member variable.
				createTexture(m_Textures, strFullPath, strTextures.size());								

				  // Set the texture ID for this material by getting the current loaded texture count
				pModel.getPMaterials().get(i).setTextureId(strTextures.size());

				  // Now we increase the loaded texture count by adding the texture name to our
				  // list of texture names.
				strTextures.add(pModel.getPMaterials().get(i).getStrFile());
			}
		}
	}



	/** This loads the .cfg file that stores all the animation information.
	 *  @param strConfigFile Configuration file's path.
	 */

	private final boolean loadAnimations(String strConfigFile)
	{
		try
		{
			  // Create an animation object for every valid animation in the Quake3 Character
			AnimationInfo[] animations = new AnimationInfo[MAX_ANIMATIONS];

			BufferedReader reader = new BufferedReader(new FileReader(strConfigFile));

			String strWord = "";					// This stores the current word we are reading in
			String strLine = "";					// This stores the current line we read in
			int currentAnim = 0;					// This stores the current animation count
			int torsoOffset = 0;					// The offset between the first torso and leg animation
			StringTokenizer tokenizer;

			  // Here we go through every word in the file until a numeric number is found.
			while((strLine = reader.readLine()) != null)
			{
				  // skip blank lines
				if(strLine.length() == 0)
				{
					continue;
				}
				  // If the first character of the word is NOT a number, we haven't hit an animation line
				if(!Character.isDigit(strLine.charAt(0)))
				{
					continue;
				}

				  // If we get here, we must be on an animation line, so let's parse the data.
				tokenizer = new StringTokenizer(strLine);

				  // Read in the number of frames, the looping frames, then the frames per second
				  // for this current animation we are on.
				int startFrame		= Integer.parseInt(tokenizer.nextToken());
				int numOfFrames		= Integer.parseInt(tokenizer.nextToken());
				int loopingFrames	= Integer.parseInt(tokenizer.nextToken());
				int framesPerSecond = Integer.parseInt(tokenizer.nextToken());
				
				  // Initialize the current animation structure with the data just read in
				animations[currentAnim] = new AnimationInfo();
				animations[currentAnim].setStartFrame(startFrame);
				animations[currentAnim].setEndFrame(startFrame + numOfFrames);
				animations[currentAnim].setLoopingFrames(loopingFrames);
				animations[currentAnim].setFramesPerSecond(framesPerSecond);

				  // Read past the "//" and read in the animation name (I.E. "BOTH_DEATH1").
				  // This might not be how every config file is set up, so make sure.
				tokenizer.nextToken();

				  // Copy the name of the animation to our animation structure
				animations[currentAnim].setStrName(tokenizer.nextToken());

				  // If the animation is for both the legs and the torso, add it to their animation list
				if(IsInString(strLine, "BOTH"))
				{
					  // Add the animation to each of the upper and lower mesh lists
					m_Upper.getPAnimations().add(animations[currentAnim]);
					m_Lower.getPAnimations().add(animations[currentAnim]);
				}
				  // If the animation is for the torso, add it to the torso's list
				else if(IsInString(strLine, "TORSO"))
				{
					m_Upper.getPAnimations().add(animations[currentAnim]);
				}
				  // If the animation is for the legs, add it to the legs's list
				else if(IsInString(strLine, "LEGS"))
				{
					  // If the torso offset hasn't been set, set it
					if(torsoOffset == 0)
						torsoOffset = animations[LEGS_WALKCR].getStartFrame() - animations[TORSO_GESTURE].getStartFrame();

					  // Minus the offset from the legs animation start and end frame.
					animations[currentAnim].setStartFrame(animations[currentAnim].getStartFrame()-torsoOffset);
		                        animations[currentAnim].setEndFrame(animations[currentAnim].getEndFrame()-torsoOffset);

					  // Add the animation to the list of leg animations
					m_Lower.getPAnimations().add(animations[currentAnim]);
				}
			
				  // Increase the current animation count
				currentAnim++;
			}	

			  // Store the number if animations for each list by the size() function
			m_Lower.setNumOfAnimations(m_Lower.getPAnimations().size());
			m_Upper.setNumOfAnimations(m_Upper.getPAnimations().size());
			m_Head.setNumOfAnimations(m_Head.getPAnimations().size());
			m_Weapon.setNumOfAnimations(m_Head.getPAnimations().size());
		}
		catch(Exception e)
		{
			return false;
		}

		  // Return a success
		return true;
	}



	/** This links the body part models to each other, along with the weapon.
	 *  @param pModel Main model.
	 *  @param pLink Model to be linked.
	 *  @param strTagName Tag name.
	 */

	private final void linkModel(Model3D pModel, Model3D pLink, String strTagName)
	{
		  // Go through all of our tags and find which tag contains the strTagName, then link'em
		for(int i = 0; i < pModel.getNumOfTags(); i++)
		{
			  // If this current tag index has the tag name we are looking for
			if(pModel.getPTags()[i].getStrName().equals(strTagName))
			{
				  // Link the model's link index to the link (or model/mesh) and return
				pModel.setPLinks(i,pLink);
				return;
			}
		}
	}



	/** This sets the current frame of animation, depending on it's fps and t.
	 *  @param pModel Current model.
	 */

	private final void updateModel(Model3D pModel){
		  // Initialize a start and end frame, for models with no animation
		int startFrame = 0;
		int endFrame   = 1;

		  // Here we grab the current animation that we are on from our model's animation list
		AnimationInfo pAnim = pModel.getPAnimations().get(pModel.getCurrentAnim());

		  // If there is any animations for this model
		if(pModel.getNumOfAnimations() != 0)
		{
			  // Set the starting and end frame from for the current animation
			startFrame = pAnim.getStartFrame();
			endFrame   = pAnim.getEndFrame();
		}
		
		  // This gives us the next frame we are going to.
		pModel.setNextFrame((pModel.getCurrentFrame() + 1) % endFrame);

		  // If the next frame is zero, that means that we need to start the animation over.
		if(pModel.getNextFrame() == 0) 
			pModel.setNextFrame(startFrame);

		  // Next, we want to get the current time that we are interpolating by.
		setCurrentTime(pModel);
	}



	/** This recursively draws all the character nodes, starting with the legs.
	 */

	final void draw(){
		  // Rotate the model to compensate for the z up orientation that the model was saved
		gl.getGL2().glRotatef(-90, 1, 0, 0);

		  // Update the leg and torso animations
		updateModel(m_Lower);
		updateModel(m_Upper);

		  // Draw the first link, which is the lower body.  This will then recursively go
		  // through the models attached to this model and drawn them.
		drawLink(m_Lower);
	}

        final void analyseModel(){
	    ArrayList<Float> tmpDataBuffer=new ArrayList<>();
	    analyseUpdateModel(m_Lower);
	    analyseUpdateModel(m_Upper);
	    analyseDrawLink(m_Lower,tmpDataBuffer);
	    System.out.println("vertex and texture coordinates count = "+tmpDataBuffer.size()/5);
	}
	
	private final void analyseUpdateModel(Model3D pModel){
	    int startFrame = 0;
	    int endFrame   = 1;

	      // Here we grab the current animation that we are on from our model's animation list
	    AnimationInfo pAnim = pModel.getPAnimations().get(pModel.getCurrentAnim());

	      // If there is any animations for this model
	    if(pModel.getNumOfAnimations() != 0)
	    {
		      // Set the starting and end frame from for the current animation
		    startFrame = pAnim.getStartFrame();
		    endFrame   = pAnim.getEndFrame();
	    }

	      // This gives us the next frame we are going to.
	    pModel.setNextFrame((pModel.getCurrentFrame() + 1) % endFrame);

	      // If the next frame is zero, that means that we need to start the animation over.
	    if(pModel.getNextFrame() == 0) 
		    pModel.setNextFrame(startFrame);
	}
	
	private final void analyseRenderModel(Model3D pModel,ArrayList<Float> list){
	    // Make sure we have valid objects just in case.
	     if(pModel.getPObject() == null)
		     return;

	     // Go through all of the objects stored in this model
	     Object3D pObject = null;
	     for(int i = 0; i < pModel.getNumOfObjects(); i++)
		 {// Get the current object that we are displaying
		  pObject = pModel.getPObject().get(i);

		    // Find the current starting index for the current key frame we are on
		  int currentIndex = pModel.getCurrentFrame() * pObject.getNumOfVerts(); 

		    // Since we are interpolating, we also need the index for the next key frame
		  int nextIndex = pModel.getNextFrame() * pObject.getNumOfVerts();

		    // Grab the texture index from the materialID index into our material list
		  int textureID = pModel.getPMaterials().get(pObject.getMaterialID()).getTextureId();

		    // Bind the texture index that we got from the material textureID
		  //gl.glBindTexture(GL.GL_TEXTURE_2D, m_Textures[textureID]);

		    // Start drawing our model triangles
		  //gl.glBegin(GL.GL_TRIANGLES);

		    // Go through all of the faces (polygons) of the object and draw them
		  for(int j = 0; j < pObject.getNumOfFaces(); j++)
		      {// Go through each vertex of the triangle and draw it.
		       for(int whichVertex = 0; whichVertex < 3; whichVertex++)
		           {// Get the index for the current point in the face list
			    int index = pObject.getPFaces()[j].getVertIndex()[whichVertex];

			    // Make sure there is texture coordinates for this (%99.9 likelyhood)
			    if(pObject.getPTexVerts() != null) 
			        {
				      // Assign the texture coordinate to this vertex
				    //gl.glTexCoord2f(pObject.getPTexVerts()[ index ].getX(), 
				//			     pObject.getPTexVerts()[ index ].getY());
				list.add(new Float(pObject.getPTexVerts()[index].getX()));
				list.add(new Float(pObject.getPTexVerts()[index].getY()));
			        }
                            else
			        {list.add(new Float(0.0f));
				 list.add(new Float(0.0f));
				}
			      // Store the current and next frame's vertex by adding the current
			      // and next index to the initial index given from the face data.
			    Vector3D vPoint1 = pObject.getPVerts()[currentIndex + index];
			    Vector3D vPoint2 = pObject.getPVerts()[nextIndex + index];

			      // By using the equation: p(t) = p0 + t(p1 - p0), with a time t,
			      // we create a new vertex that is closer to the next key frame.
			    //gl.glVertex3f(vPoint1.getX(),vPoint1.getY(),vPoint1.getZ());
			    list.add(new Float(vPoint1.getX()));
			    list.add(new Float(vPoint1.getY()));
			    list.add(new Float(vPoint1.getZ()));
		           }
		      }

		    // Stop drawing polygons
		  //gl.glEnd();
		 }
	}
	
	private final void analyseDrawLink(Model3D pModel,ArrayList<Float> list){
	    // Draw the current model passed in (Initially the legs)
	    analyseRenderModel(pModel,list);

	    // Create some local variables to store all this crazy interpolation data
	    Quaternion qQuat= new Quaternion();
	    Quaternion qNextQuat= new Quaternion();
	    Quaternion qInterpolatedQuat= new Quaternion();
	    float[] pMatrix;
	    float[] pNextMatrix;
	    float[] finalMatrix = new float[16];
	    final FloatBuffer finalMatrixBuffer = Buffers.newDirectFloatBuffer(16);
	    Model3D pLink=null;
	    Vector3D vOldPosition = null;
	    Vector3D vNextPosition = null;
	    Vector3D vPosition = new Vector3D();
	    // Now we need to go through all of this models tags and draw them.
	    for(int i = 0; i < pModel.getNumOfTags(); i++)
		// Get the current link from the models array of links (Pointers to models)			
		// If this link has a valid address, let's draw it!
		if((pLink=pModel.getPLinks()[i])!= null)
		    {// To find the current translation position for this frame of animation, we times
		     // the currentFrame by the number of tags, then add i.
		     vOldPosition = pModel.getPTags()[pModel.getCurrentFrame() * pModel.getNumOfTags() + i].getVPosition();
		     // Grab the next key frame translation position
		     vNextPosition = pModel.getPTags()[pModel.getNextFrame() * pModel.getNumOfTags() + i].getVPosition();
		     // By using the equation: p(t) = p0 + t(p1 - p0), with a time t,
		     // we create a new translation position that is closer to the next key frame.
		     
		     vPosition.setX(vOldPosition.getX());
		     vPosition.setY(vOldPosition.getY());
		     vPosition.setZ(vOldPosition.getZ());

		     // Get a pointer to the start of the 3x3 rotation matrix for the current frame
		     pMatrix = pModel.getPTags()[pModel.getCurrentFrame() * pModel.getNumOfTags() + i].getRotation();

		     // Get a pointer to the start of the 3x3 rotation matrix for the next frame
		     pNextMatrix = pModel.getPTags()[pModel.getNextFrame() * pModel.getNumOfTags() + i].getRotation();

		       // Convert the current and next key frame 3x3 matrix into a quaternion
		     qQuat.createFromMatrix(pMatrix, 3);
		     qNextQuat.createFromMatrix(pNextMatrix, 3);

		       // Using spherical linear interpolation, we find the interpolated quaternion
		     qInterpolatedQuat = qQuat.slerp(qQuat,qNextQuat,0.0f);

		       // Here we convert the interpolated quaternion into a 4x4 matrix
		     qInterpolatedQuat.createMatrix( finalMatrix );

		       // To cut out the need for 2 matrix calls, we can just slip the translation
		       // into the same matrix that holds the rotation.
		     finalMatrix[12] = vPosition.getX();
		     finalMatrix[13] = vPosition.getY();
		     finalMatrix[14] = vPosition.getZ();

		     finalMatrixBuffer.put(finalMatrix).rewind();

		       // Start a new matrix scope
		     //gl.glPushMatrix();

		       // Finally, apply the rotation and translation matrix to the current matrix
		     //gl.glMultMatrixf(finalMatrixBuffer);
		       // Recursively draw the next model that is linked to the current one.
		     drawLink(pLink);
		       // End the current matrix scope
		     //gl.glPopMatrix();
		    }
	}
	
	/** This draws the current mesh with an effected matrix stack from the last mesh.
	 *  @param pModel Current model.
	 */

	private final void drawLink(Model3D pModel){
	    // Draw the current model passed in (Initially the legs)
	    renderModel(pModel);

	    // Create some local variables to store all this crazy interpolation data
	    Quaternion qQuat= new Quaternion();
	    Quaternion qNextQuat= new Quaternion();
	    Quaternion qInterpolatedQuat= new Quaternion();
	    float[] pMatrix;
	    float[] pNextMatrix;
	    float[] finalMatrix = new float[16];
	    final FloatBuffer finalMatrixBuffer = Buffers.newDirectFloatBuffer(16);
	    Model3D pLink=null;
	    Vector3D vOldPosition = null;
	    Vector3D vNextPosition = null;
	    Vector3D vPosition = new Vector3D();
	    // Now we need to go through all of this models tags and draw them.
	    for(int i = 0; i < pModel.getNumOfTags(); i++)
		// Get the current link from the models array of links (Pointers to models)			
		// If this link has a valid address, let's draw it!
		if((pLink=pModel.getPLinks()[i])!= null)
		    {// To find the current translation position for this frame of animation, we times
		     // the currentFrame by the number of tags, then add i.
		     vOldPosition = pModel.getPTags()[pModel.getCurrentFrame() * pModel.getNumOfTags() + i].getVPosition();
		     // Grab the next key frame translation position
		     vNextPosition = pModel.getPTags()[pModel.getNextFrame() * pModel.getNumOfTags() + i].getVPosition();
		     // By using the equation: p(t) = p0 + t(p1 - p0), with a time t,
		     // we create a new translation position that is closer to the next key frame.
		     
		     vPosition.setX(vOldPosition.getX() + pModel.getT() * (vNextPosition.getX() - vOldPosition.getX()));
		     vPosition.setY(vOldPosition.getY() + pModel.getT() * (vNextPosition.getY() - vOldPosition.getY()));
		     vPosition.setZ(vOldPosition.getZ() + pModel.getT() * (vNextPosition.getZ() - vOldPosition.getZ()));

		     // Get a pointer to the start of the 3x3 rotation matrix for the current frame
		     pMatrix = pModel.getPTags()[pModel.getCurrentFrame() * pModel.getNumOfTags() + i].getRotation();

		     // Get a pointer to the start of the 3x3 rotation matrix for the next frame
		     pNextMatrix = pModel.getPTags()[pModel.getNextFrame() * pModel.getNumOfTags() + i].getRotation();

		       // Convert the current and next key frame 3x3 matrix into a quaternion
		     qQuat.createFromMatrix(pMatrix, 3);
		     qNextQuat.createFromMatrix(pNextMatrix, 3);

		       // Using spherical linear interpolation, we find the interpolated quaternion
		     qInterpolatedQuat = qQuat.slerp(qQuat, qNextQuat, pModel.getT());

		       // Here we convert the interpolated quaternion into a 4x4 matrix
		     qInterpolatedQuat.createMatrix( finalMatrix );

		       // To cut out the need for 2 matrix calls, we can just slip the translation
		       // into the same matrix that holds the rotation.
		     finalMatrix[12] = vPosition.getX();
		     finalMatrix[13] = vPosition.getY();
		     finalMatrix[14] = vPosition.getZ();

		     finalMatrixBuffer.put(finalMatrix).rewind();

		       // Start a new matrix scope
		     gl.getGL2().glPushMatrix();

		       // Finally, apply the rotation and translation matrix to the current matrix
		     gl.getGL2().glMultMatrixf(finalMatrixBuffer);
		       // Recursively draw the next model that is linked to the current one.
		     drawLink(pLink);
		       // End the current matrix scope
		     gl.getGL2().glPopMatrix();
		    }
		

	}



	/** This sets time t for the interpolation between the current and next key frame.
	 *  @param pModel Current model.
	 */

	private final void setCurrentTime(Model3D pModel)
	{
		  // Return if there is no animations in this model
		if(pModel.getPAnimations().size() == 0)
			return;

		  // Get the current time in milliseconds
		long time = System.currentTimeMillis();
		
		  // Find the time that has elapsed since the last time that was stored
		long elapsedTime = time - pModel.getLastTime();

		  // Store the animation speed for this animation in a local variable
		int animationSpeed = pModel.getPAnimations().get(pModel.getCurrentAnim()).getFramesPerSecond();

		float t = elapsedTime / (1000.0f / animationSpeed);

		  // If our elapsed time goes over the desired time segment, start over and go 
		  // to the next key frame.
		if(elapsedTime >= (1000.0f / animationSpeed))
		{
			  // Set our current frame to the next key frame (which could be the start of the anim)
			pModel.setCurrentFrame(pModel.getNextFrame());

			  // Set our last time for the model to the current time
			pModel.setLastTime(time);
		}

		  // Set the t for the model to be used in interpolation
		pModel.setT(t);
	}



	/** This renders the model data to the screen.
	 *  @param pModel Current model.
	 */

	private final void renderModel(Model3D pModel){
	     // Make sure we have valid objects just in case.
	     if(pModel.getPObject() == null)
		     return;

	     // Go through all of the objects stored in this model
	     Object3D pObject = null;
	     for(int i = 0; i < pModel.getNumOfObjects(); i++)
		 {// Get the current object that we are displaying
		  pObject = pModel.getPObject().get(i);

		    // Find the current starting index for the current key frame we are on
		  int currentIndex = pModel.getCurrentFrame() * pObject.getNumOfVerts(); 

		    // Since we are interpolating, we also need the index for the next key frame
		  int nextIndex = pModel.getNextFrame() * pObject.getNumOfVerts();

		    // Grab the texture index from the materialID index into our material list
		  int textureID = pModel.getPMaterials().get(pObject.getMaterialID()).getTextureId();

		    // Bind the texture index that we got from the material textureID
		  gl.glBindTexture(GL.GL_TEXTURE_2D, m_Textures[textureID]);

		    // Start drawing our model triangles
		  gl.getGL2().glBegin(GL.GL_TRIANGLES);

		    // Go through all of the faces (polygons) of the object and draw them
		  for(int j = 0; j < pObject.getNumOfFaces(); j++)
		      {// Go through each vertex of the triangle and draw it.
		       for(int whichVertex = 0; whichVertex < 3; whichVertex++)
		           {// Get the index for the current point in the face list
			    int index = pObject.getPFaces()[j].getVertIndex()[whichVertex];

			    // Make sure there is texture coordinates for this (%99.9 likelyhood)
			    if(pObject.getPTexVerts() != null) 
			    {
				      // Assign the texture coordinate to this vertex
				    gl.getGL2().glTexCoord2f(pObject.getPTexVerts()[ index ].getX(), 
							     pObject.getPTexVerts()[ index ].getY());
			    }

			      // Store the current and next frame's vertex by adding the current
			      // and next index to the initial index given from the face data.
			    Vector3D vPoint1 = pObject.getPVerts()[currentIndex + index];
			    Vector3D vPoint2 = pObject.getPVerts()[nextIndex + index];

			      // By using the equation: p(t) = p0 + t(p1 - p0), with a time t,
			      // we create a new vertex that is closer to the next key frame.
			    gl.getGL2().glVertex3f(vPoint1.getX() + pModel.getT() * (vPoint2.getX() - vPoint1.getX()),
					       vPoint1.getY() + pModel.getT() * (vPoint2.getY() - vPoint1.getY()),
					       vPoint1.getZ() + pModel.getT() * (vPoint2.getZ() - vPoint1.getZ()));
		           }
		      }

		    // Stop drawing polygons
		  gl.getGL2().glEnd();
		 }
	}



	/** This sets the current animation that the upper body will be performing.
	 *  @param strAnimation Animation name.
	 */

	final void setTorsoAnimation(String strAnimation)
	{
		  // Go through all of the animations in this model
		for(int i = 0; i < m_Upper.getNumOfAnimations(); i++)
		{
			  // If the animation name passed in is the same as the current animation's name
			if(m_Upper.getPAnimations().get(i).getStrName().equals(strAnimation))
			{
				  // Set the legs animation to the current animation we just found and return
				m_Upper.setCurrentAnim(i);
				m_Upper.setCurrentFrame(m_Upper.getPAnimations().get(m_Upper.getCurrentAnim()).getStartFrame());
				return;
			}
		}
	}



	/** This sets the current animation that the lower body will be performing.
	 *  @param strAnimation Animation name.
	 */

	final void setLegsAnimation(String strAnimation)
	{
		  // Go through all of the animations in this model
		for(int i = 0; i < m_Lower.getNumOfAnimations(); i++)
		{
			  // If the animation name passed in is the same as the current animation's name
			if(m_Lower.getPAnimations().get(i).getStrName().equals(strAnimation))
			{
				  // Set the legs animation to the current animation we just found and return
				m_Lower.setCurrentAnim(i);
				m_Lower.setCurrentFrame(m_Lower.getPAnimations().get(m_Lower.getCurrentAnim()).getStartFrame());
				return;
			}
		}
	}



	/** This is called by the client to open the .Md3 file, read it, then clean up.
	 *  @param pModel Current model.
	 *  @param file MD3 file name.
	 */

	private final boolean importMD3(Model3D pModel, String file)
	{
		try
		{
			File f = new File(file);

			  // Wrap a buffer to make reading more efficient (faster).
			BufferedInputStream bis = new BufferedInputStream(new FileInputStream(f));

			fileContents = new byte[(int)f.length()];

			  // Read the entire file into memory.
			bis.read(fileContents, 0, (int)f.length());

			  // Close the .md3 file that we opened
			bis.close();

			  // Open the MD3 file in binary
			m_FilePointer = 0;

			  // Read the header data and store it in our m_Header member variable
			m_Header = new MD3Header(this);

			  // Get the 4 character ID
			String ID = m_Header.getFileID();

			  // Make sure the ID == IDP3 and the version is this crazy number '15' or else it's a bad egg
			if(!ID.equals("IDP3") || m_Header.getVersion() != 15)
			{
				System.out.println("[Error]: file " + file + " version is not valid.");
				System.exit(0);
			}
			
			  // Read in the model and animation data
			readMD3Data(pModel);
		}
		catch(Exception e)
		{
			System.out.println("[Error]: can't read " + file + " correctly.");
			System.exit(0);
		}

		  // Return a success
		return true;
	}



	/** This function reads in all of the model's data, except the animation frames.
	 *  @param pModel Current model.
	 */

	private final void readMD3Data(Model3D pModel)
	{
		int i = 0;

		  // Here we allocate memory for the bone information and read the bones in.
		m_pBones = new MD3Bone[m_Header.getNumFrames()];

		for (i = 0; i < m_Header.getNumFrames() ; i++)
			m_pBones[i] = new MD3Bone(this);

		  // Free the unused bones
		m_pBones = null;

		  // Next, after the bones are read in, we need to read in the tags.
		pModel.setPTags(new MD3Tag[m_Header.getNumFrames() * m_Header.getNumTags()]);

		for(i = 0; i < m_Header.getNumFrames() * m_Header.getNumTags(); i++)
			pModel.setPTags(i,new MD3Tag(this));

		  // Assign the number of tags to our model
		pModel.setNumOfTags(m_Header.getNumTags());
		
		  // Now we want to initialize our links.
		pModel.setPLinks(new Model3D[m_Header.getNumTags()]);
		
		  // Initilialize our link pointers to NULL
		for(i = 0; i < m_Header.getNumTags(); i++)
			pModel.setPLinks(i,null);

		  // Get the current offset into the file
		int meshOffset = m_FilePointer;

		  // Create a local meshHeader that stores the info about the mesh
		MD3MeshInfo meshHeader = new MD3MeshInfo(this);

		  // Go through all of the sub-objects in this mesh
		for (int j = 0; j < m_Header.getNumMeshes(); j++)
		{
			  // Seek to the start of this mesh and read in it's header
			m_FilePointer = meshOffset;
			meshHeader = new MD3MeshInfo(this);

			  // Here we allocate all of our memory from the header's information
			m_pSkins     = new MD3Skin[meshHeader.getNumSkins()];
			m_pTexCoords = new MD3TexCoord[meshHeader.getNumVertices()];
			m_pTriangles = new MD3Face[meshHeader.getNumTriangles()];
			m_pVertices  = new MD3Triangle[meshHeader.getNumVertices() * meshHeader.getNumMeshFrames()];

			  // Read in the skin information
			for (i = 0; i < meshHeader.getNumSkins() ; i++)
				m_pSkins[i] = new MD3Skin(this);
			
			  // Seek to the start of the triangle/face data, then read it in
			m_FilePointer = meshOffset + meshHeader.getTriStart();

			for (i = 0; i < meshHeader.getNumTriangles(); i++)
				m_pTriangles[i] = new MD3Face(this);

			  // Seek to the start of the UV coordinate data, then read it in
			m_FilePointer = meshOffset + meshHeader.getUvStart();

			for (i = 0; i < meshHeader.getNumVertices(); i++)
				m_pTexCoords[i] = new MD3TexCoord(this);

			  // Seek to the start of the vertex/face index information, then read it in.
			m_FilePointer = meshOffset + meshHeader.getVertexStart();
			for(i = 0; i < meshHeader.getNumMeshFrames() * meshHeader.getNumVertices(); i++)
				m_pVertices[i] = new MD3Triangle(this);

			  // Now that we have the data loaded into the md3 structures, let's convert them to
			  // our data types like Model3D and Object3D.
			convertDataStructures(pModel, meshHeader);

			  // Free all the memory for this mesh since we just converted it to our structures
			m_pSkins = null;    
			m_pTexCoords = null;
			m_pTriangles = null;
			m_pVertices = null;   

			  // Increase the offset into the file
			meshOffset += meshHeader.getMeshSize();
		}
	}



	/** This function converts the .md3 structures to our own model and object structures.
	 *  @param pModel Current model.
	 *  @param meshHeader Current mesh header informations.
	 */

	private final void convertDataStructures(Model3D pModel, MD3MeshInfo meshHeader)
	{
		int i = 0;

		  // Increase the number of objects (sub-objects) in our model since we are loading a new one
		pModel.setNumOfObjects(pModel.getNumOfObjects()+1);
			
		  // Create a empty object structure to store the object's info before we add it to our list
		Object3D currentMesh = new Object3D();

		  // Copy the name of the object to our object structure
		currentMesh.setStrName(meshHeader.getStrName());

		  // Assign the vertex, texture coord and face count to our new structure
		currentMesh.setNumOfVerts(meshHeader.getNumVertices());
		currentMesh.setNumTexVertex(meshHeader.getNumVertices());
		currentMesh.setNumOfFaces(meshHeader.getNumTriangles());

		  // Allocate memory for the vertices, texture coordinates and face data.
		currentMesh.setPVerts(new Vector3D[currentMesh.getNumOfVerts() * meshHeader.getNumMeshFrames()]);
		currentMesh.setPTexVerts(new Vector2D[currentMesh.getNumOfVerts()]);
		currentMesh.setPFaces(new Face[currentMesh.getNumOfFaces()]);

		  // Go through all of the vertices and assign them over to our structure
		for(i = 0; i < currentMesh.getNumOfVerts() * meshHeader.getNumMeshFrames(); i++)
		{
			currentMesh.setPVerts(i,new Vector3D());
			currentMesh.getPVerts()[i].setX(m_pVertices[i].getVertex()[0] / 64.0f);
			currentMesh.getPVerts()[i].setY(m_pVertices[i].getVertex()[1] / 64.0f);
			currentMesh.getPVerts()[i].setZ(m_pVertices[i].getVertex()[2] / 64.0f);
		}

		  // Go through all of the uv coords and assign them over to our structure
		for(i = 0; i < currentMesh.getNumTexVertex(); i++)
		{
			currentMesh.setPTexVerts(i,new Vector2D());
			currentMesh.getPTexVerts()[i].setX(m_pTexCoords[i].getTextureCoord()[0]);
			currentMesh.getPTexVerts()[i].setY(-m_pTexCoords[i].getTextureCoord()[1]);
		}

		  // Go through all of the face data and assign it over to OUR structure
		for(i = 0; i < currentMesh.getNumOfFaces(); i++)
		{
			  // Assign the vertex indices to our face data
			currentMesh.setPFaces(i,new Face());
			currentMesh.getPFaces()[i].setVertIndex(0,m_pTriangles[i].getVertexIndices()[0]);
			currentMesh.getPFaces()[i].setVertIndex(1,m_pTriangles[i].getVertexIndices()[1]);
			currentMesh.getPFaces()[i].setVertIndex(2,m_pTriangles[i].getVertexIndices()[2]);

			  // Assign the texture coord indices to our face data (same as the vertex indices)
			currentMesh.getPFaces()[i].setCoordIndex(0,m_pTriangles[i].getVertexIndices()[0]);
			currentMesh.getPFaces()[i].setCoordIndex(1,m_pTriangles[i].getVertexIndices()[1]);
			currentMesh.getPFaces()[i].setCoordIndex(2,m_pTriangles[i].getVertexIndices()[2]);
		}

		  // Here we add the current object to our list object list
		pModel.getPObject().add(currentMesh);
	}


	/** This loads the texture information for the model from the *.skin file.
	 *  @param pModel Current model.
	 *  @param strSkin Skin path.
	 */

	private final boolean loadSkin(Model3D pModel, String strSkin)
	{
		try
		{
			  // Wrap a buffer to make reading more efficient (faster)
			BufferedReader reader = new BufferedReader(new FileReader(strSkin));

			  // These 2 variables are for reading in each line from the file, then storing
			  // the index of where the bitmap name starts after the last '/' character.
			String strLine;
			int textureNameStart = 0;

			  // Go through every line in the .skin file
			while((strLine = reader.readLine()) != null)
			{
				  // Loop through all of our objects to test if their name is in this line
				for(int i = 0; i < pModel.getNumOfObjects(); i++)
				{
					  // Check if the name of this object appears in this line from the skin file
					if(IsInString(strLine, pModel.getPObject().get(i).getStrName()))			
					{			
						  // To extract the texture name, we loop through the string, starting
						  // at the end of it until we find a '/' character, then save that index + 1.
						textureNameStart = strLine.lastIndexOf("/") + 1;

						  // Create a local material info structure
						MaterialInfo texture = new MaterialInfo();

						  // Copy the name of the file into our texture file name variable.
						texture.setStrFile(strLine.substring(textureNameStart));
						
						  // The tile or scale for the UV's is 1 to 1 
						texture.setUTile(1);
						texture.setVTile(1);

						  // Store the material ID for this object and set the texture boolean to true
						pModel.getPObject().get(i).setMaterialID(pModel.getNumOfMaterials());
						pModel.getPObject().get(i).setBHasTexture(true);

						  // Here we increase the number of materials for the model
						pModel.setNumOfMaterials(pModel.getNumOfMaterials()+1);

						  // Add the local material info structure to our model's material list
						pModel.getPMaterials().add(texture);
					}
				}
			}

			  // Close the file and return a success
			reader.close();
		}
		catch(Exception e)
		{
			return false;
		}

		return true;
	}


	
	/** This loads the basic shader texture info associated with the weapon model.
	 *  @param pModel Current model.
	 *  @param strShader Shader path.
	 */

	private final boolean loadShader(Model3D pModel, String strShader)
	{
		try
		{
			  // Wrap a buffer to make reading more efficient (faster)
			BufferedReader reader = new BufferedReader(new FileReader(strShader));

			  // These variables are used to read in a line at a time from the file, and also
			  // to store the current line being read so that we can use that as an index for the 
			  // textures, in relation to the index of the sub-object loaded in from the weapon model.
			String strLine;
			int currentIndex = 0;
			
			  // Go through and read in every line of text from the file
			while((strLine = reader.readLine()) != null)
			{
				  // Create a local material info structure
				MaterialInfo texture = new MaterialInfo();

				  // Copy the name of the file into our texture file name variable
				texture.setStrFile(strLine);
						
				  // The tile or scale for the UV's is 1 to 1 
				texture.setUTile(1);
				texture.setVTile(1);

				  // Store the material ID for this object and set the texture boolean to true
				pModel.getPObject().get(currentIndex).setMaterialID(pModel.getNumOfMaterials());
				pModel.getPObject().get(currentIndex).setBHasTexture(true);

				  // Here we increase the number of materials for the model
				pModel.setNumOfMaterials(pModel.getNumOfMaterials()+1);

				  // Add the local material info structure to our model's material list
				pModel.getPMaterials().add(texture);

				  // Here we increase the material index for the next texture (if any)
				currentIndex++;
			}

			  // Close the file and return a success
			reader.close();
		}
		catch(Exception e)
		{
			return false;
		}

		return true;
	}



	/** Create a texture.
	 *  @param textureArray The texture array.
	 *  @param strFileName The texture path.
	 *  @param textureID Texture ID.
	 */

	private final void createTexture(int textureArray[], String strFileName, int textureID)
	{
		textureArray[textureID] = loadTexture(strFileName);
	}



	/** Load a texture.
	 *  @param path Texture's path.
	 *  @return The texture ID in OpenGL memory.
	 */
	
	int loadTexture(String path)
	{
		Image image = (new javax.swing.ImageIcon(path)).getImage();
		
		  // Extract The Image
		BufferedImage tex = new BufferedImage(image.getWidth(null), image.getHeight(null), BufferedImage.TYPE_3BYTE_BGR);
		Graphics2D g = (Graphics2D)tex.getGraphics();
		g.drawImage(image, null, null);
		g.dispose();

		  // We flip the image to have a "normal" coordinate system (top-left) instead of
		  // OpenGL one (which is bottom-left) for texture coordinates in the rendering method.
		  // It makes things easier.

		AffineTransform tx = AffineTransform.getScaleInstance(1, -1);
		tx.translate(0, -image.getHeight(null));
		AffineTransformOp op = new AffineTransformOp(tx, AffineTransformOp.TYPE_NEAREST_NEIGHBOR);
		tex = op.filter(tex, null);

		  // Put Image In Memory
		byte data[] = (byte[])tex.getRaster().getDataElements(0, 0, tex.getWidth(), tex.getHeight(), null);
		ByteBuffer buffer2 = Buffers.newDirectByteBuffer(data.length);
		buffer2.put(data);
		buffer2.rewind();

		int[] buffer = new int[1];

		gl.glGenTextures(1,buffer,0);
		gl.glBindTexture(GL.GL_TEXTURE_2D, buffer[0]);

		  // Linear Filtering
		gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MIN_FILTER, GL.GL_LINEAR);
		gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MAG_FILTER, GL.GL_LINEAR);

		  // Generate The Texture
		gl.glTexImage2D(GL.GL_TEXTURE_2D, 0, GL.GL_RGB, tex.getWidth(), tex.getHeight(), 0, GL.GL_RGB, GL.GL_UNSIGNED_BYTE, buffer2);

		buffer2.clear();

		return buffer[0];
	}

}
