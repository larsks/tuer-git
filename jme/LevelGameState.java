package jme;

import com.jmex.game.state.BasicGameState;
import com.jmex.game.state.GameState;
import com.jmex.game.state.load.TransitionGameState;

public final class LevelGameState extends BasicGameState {

    //TODO: add the key handling
    
    public LevelGameState(String name){
        super(name);       
    }

    public static GameState getInstance(int index,
            TransitionGameState transitionGameState){
        //TODO: update the transition game state
        //TODO: return a true level game state
        return(new LevelGameState(""));
    }
    
    

}
