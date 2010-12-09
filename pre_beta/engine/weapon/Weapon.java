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

    PISTOL_9MM(true,8,Ammunition.BULLET_9MM,1),
    PISTOL_10MM(true,10,Ammunition.BULLET_10MM,1),
    MAG_60(true,30,Ammunition.BULLET_9MM,1),
    UZI(true,20,Ammunition.BULLET_9MM,1),
    SMACH(false,35,null,1),
    LASER(true,15,Ammunition.ENERGY,1),
    SHOTGUN(false,3,Ammunition.CARTRIDGE,1);
		
	/**flag indicating whether a weapon can be used in both hands*/
	private final boolean twoHanded;
	/**size of the magazine, -1 for melee weapons*/
	private final int magazineSize;
	/**ammunition (might be null especially for melee weapons)*/
	private final Ammunition ammunition;
	/**ammo per shot, -1 for melee weapons*/
	private final int ammunitionPerShot;
	//TODO: URL to the binary file
	//TODO: template node for cloning without I/O interruption, lazily instantiated
	    
	private Weapon(final boolean twoHanded,final int magazineSize,final Ammunition ammunition,final int ammunitionPerShot){
		this.twoHanded=twoHanded;
		this.magazineSize=magazineSize;
		this.ammunition=ammunition;
		this.ammunitionPerShot=ammunitionPerShot;
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
	 * @return ammunition used by this weapon if it is not a contact weapon, otherwise null (typically for knives, swords, etc..)
	 */
	public final Ammunition getAmmunition(){
		return(ammunition);
	}
	
	public boolean isForMelee(){
		return(ammunition==null);
	}
	
	public final int getAmmunitionPerShot(){
		return(ammunitionPerShot);
	}
}
