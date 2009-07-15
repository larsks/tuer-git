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

import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JOptionPane;

class DisplayActionListener implements ActionListener{

    private GameGLView gameView;
    
    private String text;
    
    private DisplayActionListener(){}
    
    DisplayActionListener(GameGLView gameView){        
        this.text="";		
        this.gameView=gameView;
        //Toolkit toolkit=Toolkit.getDefaultToolkit();
        /*this.text+="available accelerated memory : ";
	    this.text+=gameView.getGameModel().getGraphicsConfiguration().getDevice().getAvailableAcceleratedMemory()/1048576+"";		
	    this.text+=" MB VRAM";
	    this.text+=System.getProperty("line.separator");*/	
    }
    
    
    public void actionPerformed(ActionEvent e){
        JOptionPane.showMessageDialog(null,text,"Screen and sound configuration",JOptionPane.INFORMATION_MESSAGE);
    }
}
