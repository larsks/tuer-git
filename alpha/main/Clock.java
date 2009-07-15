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

import java.io.Serializable;

//JAVABEAN OK
public final class Clock implements Serializable{

    
    private static final long serialVersionUID=1L;

    private long startInstant;
    
    private long pausedDuration;
    
    private long elapsedTime;
    
    private static enum ClockInternalState{OUT_OF_SYNC,STARTED,PAUSED,STOPPED};
    
    private ClockInternalState state;
    
    
    public Clock(){
        pausedDuration = 0L;
        elapsedTime = 0L;
        state = ClockInternalState.OUT_OF_SYNC;
    }
    
    
    final void start(){
        startInstant = System.currentTimeMillis();
        pausedDuration = 0L;
        elapsedTime = 0L;
        state = ClockInternalState.STARTED;       
    }
    
    final void stop(){
        if(isPaused())
            unpause();
        sync();
        state = ClockInternalState.STOPPED;
    }
    
    final void sync(){
        if(state==ClockInternalState.STARTED)
            elapsedTime=System.currentTimeMillis()-startInstant-pausedDuration;
    }
    
    final void pause(){
        if(state==ClockInternalState.STARTED)
            {sync();
             state=ClockInternalState.PAUSED;
            }            
    }
    
    final void unpause(){
        if(state==ClockInternalState.PAUSED)
            {pausedDuration+=System.currentTimeMillis()-getLatestCurrentTimeMillis();
             state=ClockInternalState.STARTED;
            }
    }
    
    final long getLatestCurrentTimeMillis(){
        return(elapsedTime+startInstant+pausedDuration);
    }
    
    public final long getElapsedTime(){
        return(elapsedTime);
    }
    
    final boolean hasNotYetStarted(){
        return(state==ClockInternalState.OUT_OF_SYNC);
    }
    
    final boolean isPaused(){
        return(state==ClockInternalState.PAUSED);
    }
    
    final boolean isStopped(){
        return(state==ClockInternalState.STOPPED);
    }


    public final long getStartInstant(){
        return(startInstant);
    }


    public final void setStartInstant(long startInstant){
        this.startInstant=startInstant;
    }


    public final long getPausedDuration(){
        return(pausedDuration);
    }


    public final void setPausedDuration(long pausedDuration){
        this.pausedDuration=pausedDuration;
    }

    public final ClockInternalState getState(){
        return(state);
    }

    public final void setState(ClockInternalState state){
        this.state=state;
    }

    public final void setElapsedTime(long elapsedTime){
        this.elapsedTime=elapsedTime;
    }
}
