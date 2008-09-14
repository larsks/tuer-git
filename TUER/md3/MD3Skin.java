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

package md3;

// This stores a skin name (We don't use this, just the name of the model to get the texture)
class MD3Skin{


    private String strName;//[68]


    MD3Skin(MD3Model md3Model){
        strName=md3Model.byte2string(68);	
        md3Model.increaseFilePointer(68);
    }
    
    
    String getStrName(){
        return(strName);
    }
} 
