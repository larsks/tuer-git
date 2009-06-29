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
package jme;

import java.io.Serializable;
import java.util.HashMap;

public final class FullWorld implements Serializable{
    
    
    private static final long serialVersionUID=1L;

    static{TransientMarkerForXMLSerialization.updateTransientModifierForXMLSerialization(FullWorld.class);}

    private HashMap<String,EntityParameters> entityParameterTable;
    
    
    public FullWorld(){}


    public final HashMap<String,EntityParameters> getEntityParameterTable(){
        return(entityParameterTable);
    }


    public final void setEntityParameterTable(HashMap<String,EntityParameters> entityParameterTable){
        this.entityParameterTable=entityParameterTable;
    }  
}
