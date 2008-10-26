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

final class AmmunitionStorage {

    /**
     * type of ammo
     */
    private AmmunitionType ammoType;
    
    /**
     * count of ammo
     */
    private int count;
    
    /**
     * maximum count of ammo
     */
    private int size;
    
    AmmunitionStorage(AmmunitionType ammoType,int count,int size){
        assert(ammoType!=null && count >= 0 && size > 0 && count <= size);
        this.ammoType=ammoType;
        this.count=count;
        this.size=size;
    }
    
    /**
     * increase the count of ammunition
     * @param amount: count of ammo added
     * @return: count of ammo really added so that it doesn't exceed the size
     */
    private int fill(int amount){
        int oldCount=count;
        count=Math.min(count+amount,size);
        return(count-oldCount);
    }
    
    /**
     * fill a storage by emptying another one
     * @param ammoStorage
     * @return
     */
    int fill(AmmunitionStorage ammoStorage){
        if(ammoStorage.isEmpty() || isFull())
            return(0);
        else
            {int amount=fill(ammoStorage.count);
             amount=ammoStorage.empty(amount);
             return(amount);            
            }       
    }
    
    /**
     * decrease the count of ammunition
     * @param amount: count of ammo removed
     * @return: count of ammo really removed
     */
    int empty(int amount){
        int oldCount=count;
        count=Math.max(count-amount,0);
        return(oldCount-count);
    }
    
    AmmunitionType getAmmoType(){
        return(ammoType);
    }
    
    boolean isEmpty(){
        return(this.count==0);
    }
    
    boolean isFull(){
        return(this.count==this.size);
    }
}
