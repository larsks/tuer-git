package main;

final class GameInfoMessage {

    private String message;
    
    private long duration;
    
    private long creationTime;
    
    GameInfoMessage(String message,long duration,long creationTime){
        this.message=message;
        this.duration=duration;
        this.creationTime=creationTime;
    }
    
    String getMessage(){
        return(message);
    }
    
    long getDuration(){
        return(duration);
    }
    
    long getCreationTime(){
        return(creationTime);
    }
}
