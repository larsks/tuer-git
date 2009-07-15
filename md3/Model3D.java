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

import java.util.ArrayList;

// This holds our model information.  This should also turn into a robust class.
// We use STL's (Standard Template Library) vector class to ease our link list burdens. :)
class Model3D{


    private int numOfObjects;	// The number of objects in the model
    
    private int numOfMaterials;	// The number of materials for the model
    
    private ArrayList<MaterialInfo> pMaterials;	// The list of material information (Textures and colors)
    
    private ArrayList<Object3D> pObject;	// The object list for our model NEW
        
    private int numOfAnimations;	// The number of animations in this model 
    
    private int currentAnim;	// The current index into pAnimations list 
    
    private int currentFrame;	// The current frame of the current animation 
    
    private int nextFrame;		// The next frame of animation to interpolate too
    
    private float t;		// The ratio of 0.0f to 1.0f between each key frame
    
    private long lastTime;		// This stores the last time that was stored
    
    private ArrayList<AnimationInfo> pAnimations; // The list of animations    
    
    private int numOfTags; // This stores the number of tags in the model
    
    private Model3D[] pLinks;	// This stores a list of pointers that are linked to this model
    
    private MD3Tag[] pTags;	// This stores all the tags for the model animations


    Model3D(){
        pMaterials = new ArrayList<MaterialInfo>();
	pObject = new ArrayList<Object3D>();
	pAnimations = new ArrayList<AnimationInfo>();
    }
    
    
    int getNumOfObjects(){
        return(numOfObjects);
    }
    
    int getNumOfMaterials(){
        return(numOfMaterials);
    }
    
    ArrayList<MaterialInfo> getPMaterials(){
        return(pMaterials);
    }
    
    ArrayList<Object3D> getPObject(){
        return(pObject);
    }
    
    int getNumOfAnimations(){
        return(numOfAnimations);
    }
    
    int getCurrentAnim(){
        return(currentAnim);
    }
    
    int getCurrentFrame(){
        return(currentFrame);
    }
    
    int getNextFrame(){
        return(nextFrame);
    }
    
    float getT(){
        return(t);
    }
    
    long getLastTime(){
        return(lastTime);
    }
    
    ArrayList<AnimationInfo> getPAnimations(){
        return(pAnimations);
    }
    
    int getNumOfTags(){
        return(numOfTags);
    }
    
    Model3D[] getPLinks(){
        return(pLinks);
    }
    
    MD3Tag[] getPTags(){
        return(pTags);
    }
    
    void setNumOfObjects(int numOfObjects){
        this.numOfObjects=numOfObjects;
    }
    
    void setNumOfMaterials(int numOfMaterials){
        this.numOfMaterials=numOfMaterials;
    }
    
    void setPMaterials(ArrayList<MaterialInfo> pMaterials){
        this.pMaterials=pMaterials;
    }
    
    void setPObject(ArrayList<Object3D> pObject){
        this.pObject=pObject;
    }
    
    void setNumOfAnimations(int numOfAnimations){
        this.numOfAnimations=numOfAnimations;
    }
    
    void setCurrentAnim(int currentAnim){
        this.currentAnim=currentAnim;
    }
    
    void setCurrentFrame(int currentFrame){
        this.currentFrame=currentFrame;
    }
    
    void setNextFrame(int nextFrame){
        this.nextFrame=nextFrame;
    }
    
    void setT(float t){
        this.t=t;
    }
    
    void setLastTime(long lastTime){
        this.lastTime=lastTime;
    }
    
    void setPAnimations(ArrayList<AnimationInfo> pAnimations){
        this.pAnimations=pAnimations;
    }    
  
    void setNumOfTags(int numOfTags){
        this.numOfTags=numOfTags;
    }
    
    void setPLinks(Model3D[] pLinks){
        this.pLinks=pLinks;
    }
    
    void setPLinks(int index,Model3D pLinks){
        this.pLinks[index]=pLinks;
    }
    
    void setPTags(MD3Tag[] pTags){
        this.pTags=pTags;
    }
    
    void setPTags(int index,MD3Tag pTags){
        this.pTags[index]=pTags;
    }
}
