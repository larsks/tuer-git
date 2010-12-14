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

public class Ammunition implements Comparable<Ammunition>{
    
	private static final AtomicInteger autoIncrementalIndex=new AtomicInteger(0);
	/**unique name*/
    private final String identifier;
    /**unique identifier*/
    private final int uid;
	
    Ammunition(final String identifier){
    	this.uid=autoIncrementalIndex.getAndIncrement();
    	this.identifier=identifier;
    }
    
    @Override
	public int hashCode(){
		return(uid);
	}
	
	@Override
	public boolean equals(final Object o){
		final boolean result;
		if(o==null||!(o instanceof Ammunition))
		    result=false;
		else
			result=uid==((Ammunition)o).uid;
		return(result);
	}
	
	public final int getUid(){
		return(uid);
	}
	
	@Override
	public final int compareTo(final Ammunition ammunition){
		return(uid-ammunition.uid);
	}
	
	@Override
	public final String toString(){
		return(identifier);
	}
	
	public final String getIdentifier(){
		return(identifier);
	}
}
