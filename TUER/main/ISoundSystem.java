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

/**
 * Generic Sound System Interface
 * This is required to allow different implementations
 * of sound systems, and to enable "trial and error"
 * loading of them.
 *
 * @author Vincent Stahl
 */

package main;

public interface ISoundSystem {
   public boolean openSound();
   public void    closeSound();
   public boolean loadSounds();
   public void    stepMusic();
   public void    restartMusic();
   public String  soundInfo();
   public void    playSound(int id,int x,int z);
   public void    playSound(int id,int x,int z,int px,int pz);
   public void    playBotGreeting();
   public void    playBotHit(int x,int z,int px,int pz);
   public void    playAreaCleared();
   public void    playTermSound();
   public void    startMovingSound(int iMask);
   public void    stopMovingSound(int iMask);
   public void    startCarpetSound();
   public void    stopCarpetSound();
   public void    stopAllSounds();
   public void    setSoundOption(String s);
}
