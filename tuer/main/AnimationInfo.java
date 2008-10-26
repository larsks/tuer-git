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
 * This class handles the information concerning an animation.
 * Each animation is composed of one or more frames.
 * @author Julien Gouesse
 */

package main;

import java.io.ObjectStreamException;
import java.io.Serializable;
//JAVABEAN OK
public final class AnimationInfo implements Serializable{
    
    
    private static final long serialVersionUID = 1L;

    private String strName;//This stores the name of the animation
    
    private int startFrame;// This stores the first frame index for this animation
    
    private int endFrame;// This stores the last frame index for this animation
    
    private int frameCount;// This stores the count of frames for this animation
    
    private int framesPerSecond;// This stores the frames per second that this animation runs
    
    private transient int frameDuration;//frame duration in milliseconds
    
    
    public AnimationInfo(){
        this.strName="";
        this.startFrame=0;
        this.endFrame=0;
        this.frameCount=1;
        this.framesPerSecond=1;
    }
    
    AnimationInfo(String strName,int startFrame,int endFrame,int framesPerSecond){
        this.strName=strName;
        this.startFrame=startFrame;
        this.endFrame=endFrame;
        this.frameCount=endFrame-startFrame+1;
        this.framesPerSecond=framesPerSecond;
        updateFrameDuration();
    }
    
    
    public final String getStrName(){
        return(strName);
    }
    
    public final int getStartFrame(){
        return(startFrame);
    }				
    
    public final int getEndFrame(){
        return(endFrame);
    }				
    
    public final int getFrameCount(){
        return(frameCount);
    }			
    
    public final int getFramesPerSecond(){
        return(framesPerSecond);
    }
    
    public final int getFrameDuration(){
        return(frameDuration);
    }
    
    public final void setStrName(String strName){
        this.strName=strName;
    }
    
    public final void setStartFrame(int startFrame){
        this.startFrame=startFrame;
    }				
    
    public final void setEndFrame(int endFrame){
        this.endFrame=endFrame;
    }				
    
    public final void setFrameCount(int frameCount){
        this.frameCount=frameCount;
    }			
    
    public final void setFramesPerSecond(int framesPerSecond){
        this.framesPerSecond=framesPerSecond;
    }
    
    private void updateFrameDuration(){
        this.frameDuration=(this.frameCount*1000)/this.framesPerSecond;
    }
    
    final Object readResolve()throws ObjectStreamException{
        updateFrameDuration();
        return(this);
    }
}
