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
/*
import java.nio.FloatBuffer;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.BitSet;
import java.util.List;*/

//TODO: drive all useful classes Serializable and then Remote
interface IGameModel /*extends Remote*/{
    
    
    /*void runEngine()throws RemoteException;
    
    void launchNewGame()throws RemoteException;
    
    void resumeGame()throws RemoteException;
    
    boolean getPlayerWins()throws RemoteException;
    
    boolean getPlayerHit()throws RemoteException;
    
    boolean getBpause()throws RemoteException;
    
    void setBpause(boolean bpause)throws RemoteException;
    
    boolean getBcheat()throws RemoteException;
    
    void setBcheat(boolean bcheat)throws RemoteException;
    
    double getPlayerXpos()throws RemoteException;
    
    double getPlayerYpos()throws RemoteException;
    
    double getPlayerZpos()throws RemoteException;
    
    double getPlayerDirection()throws RemoteException;
    
    //TODO: drive BotModel remote
    List<BotModel> getBotList()throws RemoteException;
    
    //TODO: drive Impact remote
    List<Impact> getImpactList()throws RemoteException;
    
    List<float[]> getRocketList()throws RemoteException;
    
    byte getCollisionMap(int index)throws RemoteException;
    
    boolean isGameRunning()throws RemoteException;
    
    int getHealth()throws RemoteException;
    
    void performAtExit()throws RemoteException;
    
    //TODO: use serializable buffers, implement the both methods:
    //private void writeObject(java.io.ObjectOutputStream out)throws IOException
    //private void readObject(java.io.ObjectInputStream in)throws IOException,ClassNotFoundException
    FloatBuffer getArtCoordinatesBuffer1()throws RemoteException;
    
    FloatBuffer getArtCoordinatesBuffer2()throws RemoteException;
    
    FloatBuffer getArtCoordinatesBuffer3()throws RemoteException;
    
    FloatBuffer getArtCoordinatesBuffer4()throws RemoteException;
    
    FloatBuffer getBonsaiCoordinatesBuffer()throws RemoteException;
    
    FloatBuffer getBotCoordinatesBuffer()throws RemoteException;
    
    FloatBuffer getChairCoordinatesBuffer()throws RemoteException;
    
    FloatBuffer getFlowerCoordinatesBuffer()throws RemoteException;
    
    FloatBuffer getImpactCoordinatesBuffer()throws RemoteException;
    
    FloatBuffer getCrosshairCoordinatesBuffer()throws RemoteException;
    
    FloatBuffer getLampCoordinatesBuffer()throws RemoteException;
    
    FloatBuffer getLevelCoordinatesBuffer()throws RemoteException;
    
    FloatBuffer getRocketCoordinatesBuffer()throws RemoteException;
    
    FloatBuffer getRocketLauncherCoordinatesBuffer()throws RemoteException;
    
    FloatBuffer getTableCoordinatesBuffer()throws RemoteException;
    
    FloatBuffer getUnbreakableObjectCoordinatesBuffer()throws RemoteException;
    
    FloatBuffer getVendingMachineCoordinatesBuffer()throws RemoteException;
    
    void tryLaunchPlayerRocket()throws RemoteException;
    
    void setRunningFast(boolean runningFast)throws RemoteException;
    
    void setRunningForward(boolean runningForward)throws RemoteException;
    
    void setRunningBackward(boolean runningBackward)throws RemoteException;
    
    void setLeftStepping(boolean leftStepping)throws RemoteException;
    
    void setRightStepping(boolean rightStepping)throws RemoteException;
    
    void setTurningLeft(boolean turningLeft)throws RemoteException;
    
    void setTurningRight(boolean turningRight)throws RemoteException;

    BitSet getDataModificationFlagsBitSet()throws RemoteException;*/
}
