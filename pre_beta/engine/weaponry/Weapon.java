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
package engine.weaponry;

import java.util.concurrent.atomic.AtomicInteger;

public final class Weapon implements Comparable<Weapon>{
	
    private static final AtomicInteger autoIncrementalIndex=new AtomicInteger(0);
    /**unique name*/
    private final String identifier;
    /**unique identifier*/
    private final int uid;
	/**size of the magazine, -1 for melee weapons*/
	private final int magazineSize;
	/**ammunition (might be null especially for melee weapons)*/
	private final Ammunition ammunition;
	/**ammo per shot, -1 for melee weapons*/
	private final int ammunitionPerShot;
	/**duration of an attack in milliseconds*/
	private final int attackDurationInMillis;
	//TODO store the duration necessary to reload
	//TODO: URL to the binary file
	//TODO: template node for cloning without I/O interruption, lazily instantiated
	    
	Weapon(final String identifier,final int magazineSize,
	        final Ammunition ammunition,final int ammunitionPerShot,
	        final int attackDurationInMillis){
		this.uid=autoIncrementalIndex.getAndIncrement();
		this.identifier=identifier;
		this.magazineSize=magazineSize;
		this.ammunition=ammunition;
		this.ammunitionPerShot=ammunitionPerShot;
		this.attackDurationInMillis=attackDurationInMillis;
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
	
	@Override
	public int hashCode(){
		return(uid);
	}
	
	@Override
	public boolean equals(final Object o){
		final boolean result;
		if(o==null||!(o instanceof Weapon))
		    result=false;
		else
			result=uid==((Weapon)o).uid;
		return(result);
	}
	
	public final int getUid(){
		return(uid);
	}
	
	@Override
	public final int compareTo(final Weapon weapon){
		return(uid-weapon.uid);
	}
	
	@Override
	public final String toString(){
		return(identifier);
	}
	
	public final String getIdentifier(){
		return(identifier);
	}
	
	public final int getAttackDurationInMillis(){
		return(attackDurationInMillis);
	}
}
