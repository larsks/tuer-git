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
package weapon;

import java.util.HashMap;

public final class ProjectileModelFactory {

    private HashMap<AmmunitionType,ProjectileModel> projectileModelTable;
    
    private final static ProjectileModelFactory instance=new ProjectileModelFactory();
    
    
    private ProjectileModelFactory(){
        this.projectileModelTable=new HashMap<AmmunitionType, ProjectileModel>();
        this.projectileModelTable.put(AmmunitionType.CALIBER_9MM,new ProjectileModel(AmmunitionType.CALIBER_9MM));
        this.projectileModelTable.put(AmmunitionType.ROCKET,new ProjectileModel(AmmunitionType.ROCKET));
    }
    
    
    static final ProjectileModel getInstance(AmmunitionType ammoType){
        return(instance.projectileModelTable.get(ammoType));
    }
}
