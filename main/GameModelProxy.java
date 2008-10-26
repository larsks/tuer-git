package main;

/*import java.nio.FloatBuffer;
import java.util.BitSet;
import java.util.List;

import com.sun.opengl.util.BufferUtil;*/

final class GameModelProxy /*implements IGameModelProxy*/ {

    //TODO: add some attributes to store the data
    
    /*private FloatBuffer artCoordinatesBuffer1;
    
    private FloatBuffer artCoordinatesBuffer2;
    
    GameModelProxy(){}
    
    
    @Override
    public final void setArtCoordinatesBuffer1(FloatBuffer artCoordinatesBuffer1){
        if(artCoordinatesBuffer1==null)
            this.artCoordinatesBuffer1=null;
        else
            {int size=artCoordinatesBuffer1.capacity();
             artCoordinatesBuffer1.rewind();
             boolean isAllocationNeeded;
             if(this.artCoordinatesBuffer1!=null)
                 {this.artCoordinatesBuffer1.rewind();               
                  isAllocationNeeded=this.artCoordinatesBuffer1.capacity()!=size;                 
                 }
             else
                 isAllocationNeeded=true;
             if(isAllocationNeeded)
                 this.artCoordinatesBuffer1=BufferUtil.copyFloatBuffer(artCoordinatesBuffer1);
             else
                 {this.artCoordinatesBuffer1.put(artCoordinatesBuffer1);
                  this.artCoordinatesBuffer1.rewind();
                  artCoordinatesBuffer1.rewind();
                 }
            }
    }

    @Override
    public final void setArtCoordinatesBuffer2(FloatBuffer artCoordinatesBuffer2) {
        if(artCoordinatesBuffer2==null)
            this.artCoordinatesBuffer2=null;
        else
            {int size=artCoordinatesBuffer2.capacity();
             artCoordinatesBuffer2.rewind();
             boolean isAllocationNeeded;
             if(this.artCoordinatesBuffer2!=null)
                 {this.artCoordinatesBuffer2.rewind();               
                  isAllocationNeeded=this.artCoordinatesBuffer2.capacity()!=size;                 
                 }
             else
                 isAllocationNeeded=true;
             if(isAllocationNeeded)
                 this.artCoordinatesBuffer2=BufferUtil.copyFloatBuffer(artCoordinatesBuffer2);
             else
                 {this.artCoordinatesBuffer2.put(artCoordinatesBuffer2);
                  this.artCoordinatesBuffer2.rewind();
                  artCoordinatesBuffer2.rewind();
                 }
            }

    }

    @Override
    public final void setArtCoordinatesBuffer3(FloatBuffer f) {
        // TODO Auto-generated method stub

    }

    @Override
    public final void setArtCoordinatesBuffer4(FloatBuffer f) {
        // TODO Auto-generated method stub

    }

    @Override
    public final void setBonsaiCoordinatesBuffer(FloatBuffer f) {
        // TODO Auto-generated method stub

    }

    @Override
    public final void setBotCoordinatesBuffer(FloatBuffer f) {
        // TODO Auto-generated method stub

    }

    @Override
    public final void setBotList(List<BotModel> botList) {
        // TODO Auto-generated method stub

    }

    @Override
    public final void setChairCoordinatesBuffer(FloatBuffer f) {
        // TODO Auto-generated method stub

    }

    @Override
    public final void setCollisionMap(byte[] collisionMap) {
        // TODO Auto-generated method stub

    }

    @Override
    public final void setCrosshairCoordinatesBuffer(FloatBuffer f) {
        // TODO Auto-generated method stub

    }

    @Override
    public final void setFlowerCoordinatesBuffer(FloatBuffer f) {
        // TODO Auto-generated method stub

    }

    @Override
    public final void setHealth(int health) {
        // TODO Auto-generated method stub

    }

    @Override
    public final void setImpactCoordinatesBuffer(FloatBuffer f) {
        // TODO Auto-generated method stub

    }

    @Override
    public final void setImpactList(List<Impact> impactList) {
        // TODO Auto-generated method stub

    }

    @Override
    public final void setIsGameRunning(boolean isGameRunning) {
        // TODO Auto-generated method stub

    }

    @Override
    public final void setLampCoordinatesBuffer(FloatBuffer f) {
        // TODO Auto-generated method stub

    }

    @Override
    public final void setLevelCoordinatesBuffer(FloatBuffer f) {
        // TODO Auto-generated method stub

    }

    @Override
    public final void setPlayerDirection(double playerDirection) {
        // TODO Auto-generated method stub

    }

    @Override
    public final void setPlayerHit(boolean playerHit) {
        // TODO Auto-generated method stub

    }

    @Override
    public final void setPlayerWins(boolean playerWins) {
        // TODO Auto-generated method stub

    }

    @Override
    public final void setPlayerXpos(double playerXpos) {
        // TODO Auto-generated method stub

    }

    @Override
    public final void setPlayerYpos(double playerYpos) {
        // TODO Auto-generated method stub

    }

    @Override
    public final void setPlayerZpos(double playerZpos) {
        // TODO Auto-generated method stub

    }

    @Override
    public final void setRocketCoordinatesBuffer(FloatBuffer f) {
        // TODO Auto-generated method stub

    }

    @Override
    public final void setRocketLauncherCoordinatesBuffer(FloatBuffer f) {
        // TODO Auto-generated method stub

    }

    @Override
    public final void setRocketList(List<float[]> rocketList) {
        // TODO Auto-generated method stub

    }

    @Override
    public final void setTableCoordinatesBuffer(FloatBuffer f) {
        // TODO Auto-generated method stub

    }

    @Override
    public final void setUnbreakableObjectCoordinatesBuffer(FloatBuffer f) {
        // TODO Auto-generated method stub

    }

    @Override
    public final void setVendingMachineCoordinatesBuffer(FloatBuffer f) {
        // TODO Auto-generated method stub

    }

    @Override
    public final FloatBuffer getArtCoordinatesBuffer1() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public final FloatBuffer getArtCoordinatesBuffer2() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public final FloatBuffer getArtCoordinatesBuffer3() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public final FloatBuffer getArtCoordinatesBuffer4() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public final boolean getBcheat() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public final FloatBuffer getBonsaiCoordinatesBuffer() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public final FloatBuffer getBotCoordinatesBuffer() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public final List<BotModel> getBotList() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public final boolean getBpause() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public final FloatBuffer getChairCoordinatesBuffer() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public final byte getCollisionMap(int index) {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public final FloatBuffer getCrosshairCoordinatesBuffer() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public final FloatBuffer getFlowerCoordinatesBuffer() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public final int getHealth() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public final FloatBuffer getImpactCoordinatesBuffer() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public final List<Impact> getImpactList() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public final FloatBuffer getLampCoordinatesBuffer() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public final FloatBuffer getLevelCoordinatesBuffer() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public final double getPlayerDirection() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public final boolean getPlayerHit() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public final boolean getPlayerWins() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public final double getPlayerXpos() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public final double getPlayerYpos() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public final double getPlayerZpos() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public final FloatBuffer getRocketCoordinatesBuffer() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public final FloatBuffer getRocketLauncherCoordinatesBuffer() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public final List<float[]> getRocketList() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public final FloatBuffer getTableCoordinatesBuffer() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public final FloatBuffer getUnbreakableObjectCoordinatesBuffer() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public final FloatBuffer getVendingMachineCoordinatesBuffer() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public final boolean isGameRunning() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public final void launchNewGame(){
        throw new UnsupportedOperationException();
    }

    @Override
    public final void performAtExit(){
        throw new UnsupportedOperationException();
    }

    @Override
    public final void resumeGame() {
        throw new UnsupportedOperationException();
    }

    @Override
    public final void runEngine() {
        throw new UnsupportedOperationException();
    }

    @Override
    public final void setBcheat(boolean bcheat) {
        // TODO Auto-generated method stub

    }

    @Override
    public final void setBpause(boolean bpause) {
        // TODO Auto-generated method stub

    }

    @Override
    public final void setLeftStepping(boolean leftStepping) {
        // TODO Auto-generated method stub

    }

    @Override
    public final void setRightStepping(boolean rightStepping) {
        // TODO Auto-generated method stub

    }

    @Override
    public final void setRunningBackward(boolean runningBackward) {
        // TODO Auto-generated method stub

    }

    @Override
    public final void setRunningFast(boolean runningFast) {
        // TODO Auto-generated method stub

    }

    @Override
    public final void setRunningForward(boolean runningForward) {
        // TODO Auto-generated method stub

    }

    @Override
    public final void setTurningLeft(boolean turningLeft) {
        // TODO Auto-generated method stub

    }

    @Override
    public final void setTurningRight(boolean turningRight) {
        // TODO Auto-generated method stub

    }

    @Override
    public final void tryLaunchPlayerRocket() {
        throw new UnsupportedOperationException();
    }


    @Override
    public final void init(IGameModel gameModel){
        //TODO: copy the remote model into the local proxy
    }


    @Override
    public BitSet getDataModificationFlagsBitSet() {
        throw new UnsupportedOperationException();
    }*/
}