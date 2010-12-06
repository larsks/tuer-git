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
package engine.weapon;

public enum Weapon{

    PISTOL_9MM(true,8),PISTOL_10MM(true,10),MAG_60(true,30),UZI(true,20),SMACH(false,35),LASER(true,15),SHOTGUN(false,3);
		
	/**flag indicating whether a weapon can be used in both hands*/
	private final boolean twoHanded;
	/**size of the magazine*/
	private final int magazineSize;
	//TODO: Ammunition
	//TODO: ammo per shoot
	//TODO: URL to the binary file
	//TODO: template node for cloning without I/O interruption, lazily instantiated
	    
	private Weapon(final boolean twoHanded,final int magazineSize){
		this.twoHanded=twoHanded;
		this.magazineSize=magazineSize;
	}
	    
	/**
	 * tells whether a weapon can be used in both hands
	 * @return <code>true</code> if a weapon can be used in both hands, otherwise <code>false</code>
	 */
	public final boolean isTwoHanded(){
    	return(twoHanded);
    }
	    
	/**
	 * gets the size of the magazine
	 * @return size of the magazine
	 */
	public final int getMagazineSize(){
	    return(magazineSize);
	}
	
	/**
	 * gets the ammunition used by this weapon
	 * @return ammunition used by this weapon
	 */
	public final Ammunition getAmmunition(){
		return(null);
	}
}
