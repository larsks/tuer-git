package engine.statemachine;

import engine.data.PlayerData;
import se.hiflyer.fettle.Arguments;
import se.hiflyer.fettle.Condition;


public class ReloadPossibleCondition implements Condition{

    private final PlayerData playerData;

    public ReloadPossibleCondition(final PlayerData playerData){
        this.playerData=playerData;
    }

    @Override
    public boolean isSatisfied(Arguments args){
        final boolean canReload=playerData.getReloadableAmmoCountForPrimaryHandWeapon()>0||playerData.getReloadableAmmoCountForSecondaryHandWeapon()>0;
        return(canReload);
    }

}
