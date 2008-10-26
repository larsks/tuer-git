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

package connection;

import java.io.IOException;

class CleanupThread extends Thread{


    public void run(){
        System.out.println(System.getProperty("os.name")+" "+System.getProperty("os.arch")+" "+System.getProperty("os.version"));
        if(System.getProperty("os.name").compareToIgnoreCase("Linux")==0)
            {try{Runtime.getRuntime().exec("xset r on");
                 System.out.println("xset r on");
                }
             catch(IOException ioe)
             {ioe.printStackTrace();}
            }
        System.out.println("shutdown");
    }
}
