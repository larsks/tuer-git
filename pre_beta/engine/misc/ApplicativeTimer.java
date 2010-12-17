package engine.misc;

public class ApplicativeTimer {
	
	private static final long TIMER_RESOLUTION = 1000000000L;
	
    private static final double INVERSE_TIMER_RESOLUTION = 1.0 / TIMER_RESOLUTION;

    private long startTime;
    
    private long latestPauseStartTime;
    
    private boolean pausePreviouslyEnabled;
    
    private boolean pauseEnabled;
    
    private long elapsedTime;
    
    private long pauseElapsedTime;
    
    private long latestPauseElapsedTime;
    
    public ApplicativeTimer(){
    	startTime=System.nanoTime();
    	pauseEnabled=false;
    	pausePreviouslyEnabled=false;
    	elapsedTime=0L;
    	pauseElapsedTime=0L;
    	latestPauseElapsedTime=0L;
    }
    
    public final void update(){
    	if(this.pausePreviouslyEnabled!=this.pauseEnabled)
    	    {if(pauseEnabled)
   		         latestPauseStartTime=System.nanoTime();
   		     else   		     
   			     pauseElapsedTime+=latestPauseElapsedTime;   		 
    	    }
    	if(pauseEnabled)
    		latestPauseElapsedTime=System.nanoTime()-latestPauseStartTime;
    	else   		
    	    elapsedTime=System.nanoTime()-startTime-pauseElapsedTime;
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
    
    public void reset(){
    	startTime=System.nanoTime();
    	pauseEnabled=false;
    	pausePreviouslyEnabled=false;
    	elapsedTime=0L;
    	pauseElapsedTime=0L;
    	latestPauseElapsedTime=0L;
    }
}
