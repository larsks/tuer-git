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
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.Serializable;
//JAVABEAN OK
public final class AboutActionListener implements ActionListener,Serializable{
    
    
    private static final long serialVersionUID=1L;

    private String aboutText;
    
    private GameGLView gameView;
    
    
    public AboutActionListener(GameGLView gameView){        
        this.aboutText="";
        this.gameView=gameView;
        String tmp="";
        try{BufferedReader in=new BufferedReader(new InputStreamReader((getClass().getResourceAsStream("/about.txt"))));
            while((tmp=in.readLine())!=null)
                aboutText+=tmp+System.getProperty("line.separator");
            in.close();
           }
        catch(IOException ioe){ioe.printStackTrace();}
    }
    
    
    public final void actionPerformed(ActionEvent e){
        this.gameView.setCycle(GameCycle.ABOUT_SCREEN);
    }
    
    public final String getAboutText(){
        return(aboutText);
    }

    public final GameGLView getGameView(){
        return(gameView);
    }

    public final void setGameView(GameGLView gameView){
        this.gameView=gameView;
    }

    public final void setAboutText(String aboutText){
        this.aboutText=aboutText;
    }
}
