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

class AnimationInfo{
    
    
    private String strName;			// This stores the name of the animation (I.E. "TORSO_STAND")
    
    private int startFrame;				// This stores the first frame number for this animation
    
    private int endFrame;				// This stores the last frame number for this animation
    
    private int loopingFrames;			// This stores the looping frames for this animation (not used)
    
    private int framesPerSecond;		// This stores the frames per second that this animation runs
    
    AnimationInfo(){}
    
    String getStrName(){
        return(strName);
    }
    
    int getStartFrame(){
        return(startFrame);
    }				
    
    int getEndFrame(){
        return(endFrame);
    }				
    
    int getLoopingFrames(){
        return(loopingFrames);
    }			
    
    int getFramesPerSecond(){
        return(framesPerSecond);
    }
    
    void setStrName(String strName){
        this.strName=strName;
    }
    
    void setStartFrame(int startFrame){
        this.startFrame=startFrame;
    }				
    
    void setEndFrame(int endFrame){
        this.endFrame=endFrame;
    }				
    
    void setLoopingFrames(int loopingFrames){
        this.loopingFrames=loopingFrames;
    }			
    
    void setFramesPerSecond(int framesPerSecond){
        this.framesPerSecond=framesPerSecond;
    }
}
