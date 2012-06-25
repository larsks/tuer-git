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
 * Timer that can be paused
 * 
 * @author Julien Gouesse
 *
 */
public class ApplicativeTimer implements ReadOnlyTimer{
	
	private static final long TIMER_RESOLUTION = 1000000000L;
	
    private static final double INVERSE_TIMER_RESOLUTION = 1.0 / TIMER_RESOLUTION;
    /**internal absolute time reference used as the start of this timer*/
    private long startTime;
    /**internal absolute time reference used as the start of the latest pause*/
    private long latestPauseStartTime;
    /**flag indicating whether the pause was enabled at the previous update of this timer*/
    private boolean pausePreviouslyEnabled;
    /**flag indicating whether the pause is enabled*/
    private boolean pauseEnabled;    
    /**elapsed time in nanoseconds*/
    private long elapsedTime;
    /**elapsed time during all pauses since the start in nanoseconds*/
    private long pauseElapsedTime;
    /**elapsed time during the latest pause in nanoseconds*/
    private long latestPauseElapsedTime;
    /**frequency in Hz (frame per second)*/
    private double frameRate;
    /**period for the latest frame in seconds*/
    private double timePerFrame;
    
    /**
     * Default constructor, starts this timer immediately
     */
    public ApplicativeTimer(){
    	startTime=getSystemNanoTime();
    	pauseEnabled=false;
    	pausePreviouslyEnabled=false;
    	elapsedTime=0L;
    	pauseElapsedTime=0L;
    	latestPauseElapsedTime=0L;
    }
    
    /**
     * Update should be called once per frame to correctly update the variable about the pause(s), the elapsed time, the time per frame and the frame rate
     */
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
    
    /**
     * Enables or disables the pause. When this mechanism is enabled, the time per 
     * frame and the frame rate are equals to zero, the elapsed time is not updated
     * 
     * @param pauseEnabled flag indicating whether the pause has to be enabled
     */
    public final void setPauseEnabled(final boolean pauseEnabled){
    	if(this.pauseEnabled!=pauseEnabled)
    	    {this.pausePreviouslyEnabled=this.pauseEnabled;
    		 this.pauseEnabled=pauseEnabled;   		    		     
    	    }
    }
    
    /**
     * Gets the elapsed time in seconds since this timer was created or reset
     * 
     * @see #getTime()
     * 
     * @return Time in seconds
     */
    public double getElapsedTimeInSeconds(){
        return(elapsedTime*INVERSE_TIMER_RESOLUTION);
    }

    /**
     * Gets the elapsed time in nanoseconds since this timer was created or reset
     * 
     * @see #getResolution()
     * @see #getTimeInSeconds()
     * 
     * @return Time in nanoseconds
     */
    public long getElapsedTimeInNanoseconds(){
        return(elapsedTime);
    }
    
    /**
     * Gets the elapsed time in nanoseconds since this timer was created or reset
     * 
     * @see #getResolution()
     * @see #getTimeInSeconds()
     * 
     * @return Time in nanoseconds
     */
    @Override
    public long getTime(){
        return(getElapsedTimeInNanoseconds());
    }
    
    /**
     * Gets the elapsed time in seconds since this timer was created or reset
     * 
     * @see #getTime()
     * 
     * @return Time in seconds
     */
    @Override
    public double getTimeInSeconds() {
        return(getElapsedTimeInSeconds());
    }
    
    /**
     * Gets the resolution used by this timer (10^9 for nanosecond resolution used by this implementation)
     * 
     * @return Timer resolution
     */
    @Override
    public long getResolution(){
        return TIMER_RESOLUTION;
    }
    
    /**
     * Gets the current number of frames per second
     * 
     * @return Current frames per second
     */
    @Override
    public double getFrameRate() {
        return frameRate;
    }

    /**
     * Gets the time elapsed between the latest two frames, in seconds
     * 
     * @return Time between frames, in seconds
     */
    @Override
    public double getTimePerFrame() {
        return timePerFrame;
    }
    
    /**
     * Resets this timer, so that the elapsed time reflects the time spent from this call
     */
    public void reset(){
    	startTime=getSystemNanoTime();
    	pauseEnabled=false;
    	pausePreviouslyEnabled=false;
    	elapsedTime=0L;
    	pauseElapsedTime=0L;
    	latestPauseElapsedTime=0L;
    }
    
    /**
     * Tells whether the pause is enabled
     * 
     * @return <code>true</code> if the pause is enabled, otherwise <code>false</code>
     */
    public boolean isPauseEnabled(){
        return(pauseEnabled);
    }
    
    private final long getSystemNanoTime(){
    	final long rawSystemNanoTime=System.nanoTime();    	
    	return(rawSystemNanoTime);
    }
}
