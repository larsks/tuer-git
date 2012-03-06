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
package engine.misc;

import com.ardor3d.util.ReadOnlyTimer;

/**
 * Timer that can be paused. N.B: the frame rate is equal to zero during a pause
 * 
 * @author Julien Gouesse
 *
 */
public class ApplicativeTimer implements ReadOnlyTimer{
	
	private static final long TIMER_RESOLUTION = 1000000000L;
	
    private static final double INVERSE_TIMER_RESOLUTION = 1.0 / TIMER_RESOLUTION;

    private long startTime;
    
    private long latestPauseStartTime;
    
    private boolean pausePreviouslyEnabled;
    
    private boolean pauseEnabled;
    
    private long elapsedTime;
    
    private long pauseElapsedTime;
    
    private long latestPauseElapsedTime;
    
    private double frameRate;
    
    private double timePerFrame;
    
    public ApplicativeTimer(){
    	startTime=getSystemNanoTime();
    	pauseEnabled=false;
    	pausePreviouslyEnabled=false;
    	elapsedTime=0L;
    	pauseElapsedTime=0L;
    	latestPauseElapsedTime=0L;
    }
    
    public final void update(){
    	final long systemNanoTime=getSystemNanoTime();
    	if(this.pausePreviouslyEnabled!=this.pauseEnabled)
    	    {if(pauseEnabled)
   		         latestPauseStartTime=systemNanoTime;
   		     else   		     
   			     pauseElapsedTime+=latestPauseElapsedTime;   		 
    	    }
    	if(pauseEnabled)
    		{latestPauseElapsedTime=systemNanoTime-latestPauseStartTime;
    		 timePerFrame=0;
    		 frameRate=0;
    		}
    	else   		
    	    {final long previousElapsedTime=elapsedTime;
    	     elapsedTime=systemNanoTime-startTime-pauseElapsedTime;
    	     timePerFrame=(elapsedTime-previousElapsedTime)*INVERSE_TIMER_RESOLUTION;
    	     frameRate=1.0/timePerFrame;
    	    }
    }
    
    public final void setPauseEnabled(final boolean pauseEnabled){
    	if(this.pauseEnabled!=pauseEnabled)
    	    {this.pausePreviouslyEnabled=this.pauseEnabled;
    		 this.pauseEnabled=pauseEnabled;   		    		     
    	    }
    }
    
    public double getElapsedTimeInSeconds() {
        return(elapsedTime*INVERSE_TIMER_RESOLUTION);
    }

    public long getElapsedTimeInNanoseconds() {
        return(elapsedTime);
    }
    
    @Override
    public long getTime(){
        return(getElapsedTimeInNanoseconds());
    }
    
    @Override
    public double getTimeInSeconds() {
        return(getElapsedTimeInSeconds());
    }
    
    @Override
    public long getResolution(){
        return TIMER_RESOLUTION;
    }
    
    @Override
    public double getFrameRate() {
        return frameRate;
    }

    @Override
    public double getTimePerFrame() {
        return timePerFrame;
    }
    
    public void reset(){
    	startTime=getSystemNanoTime();
    	pauseEnabled=false;
    	pausePreviouslyEnabled=false;
    	elapsedTime=0L;
    	pauseElapsedTime=0L;
    	latestPauseElapsedTime=0L;
    }
    
    public boolean isPauseEnabled(){
        return(pauseEnabled);
    }
    
    private final long getSystemNanoTime(){
    	final long rawSystemNanoTime=System.nanoTime();    	
    	return(rawSystemNanoTime);
    }
}
