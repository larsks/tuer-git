package engine.statemachine;

import engine.data.PlayerData;
import se.hiflyer.fettle.Action;
import se.hiflyer.fettle.Arguments;
import se.hiflyer.fettle.StateMachine;


public class AttackAction implements Action<PlayerState,PlayerTransition>{

    private final PlayerData playerData;

    public AttackAction(final PlayerData playerData){
        this.playerData=playerData;
    }

    @Override
    public void onTransition(PlayerState from,PlayerState to,PlayerTransition event,Arguments args,StateMachine<PlayerState,PlayerTransition> stateMachine){
        playerData.attack();
    }
}
