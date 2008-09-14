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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

class QuitGameActionListener implements ActionListener{        
       
    
    private GameController gameController;
    
    /**
     * 
     * @param gameModel: model associated to this listener
     */       
    QuitGameActionListener(GameController gameController){
        this.gameController=gameController;
    }
    
    /**
     * called when the user wishes to quit the game, shuts down the game
     * @param ae: event received by the listener
     */
    public void actionPerformed(ActionEvent ae){
        this.gameController.performAtExit();
    }
}
