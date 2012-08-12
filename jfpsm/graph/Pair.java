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
package jfpsm.graph;

/**
 * Pair of objects
 * 
 * @author Julien Gouesse
 *
 */
public class Pair<E>{

	private final E first;
	
	private final E second;
	
	/**
	 * Constructor
	 * 
	 * @param first first element
	 * @param second second element
	 */
	public Pair(final E first,final E second){
		if(first==null||second==null)
		    throw new IllegalArgumentException(
		    		"A pair cannot contain null values");
		this.first=first;
		this.second=second;
	}

	public final E getFirst(){
		return(first);
	}
	
	public final E getSecond(){
		return(second);
	}
	
	public boolean equals(Object o){
		boolean result;
		if(o==null||!(o instanceof Pair))
			result=false;
		else
			if(o==this)
			    result=true;
			else
		        {Pair<?> p=(Pair<?>)o;
			     if(first==null&&p.getFirst()!=null)
				     result=false;
			     else
				     if(second==null&&p.getSecond()!=null)
					     result=false;
				     else
					     if(first!=null&&p.getFirst()==null)
						     result=false;
					     else
						     if(second!=null&&p.getSecond()==null)
							     result=false;
						     else
							     {final boolean firstEquals,secondEquals;
							      if(first==p.getFirst())
								      firstEquals=true;
							      else
								      firstEquals=first.equals(p.getFirst());
							      if(second==p.getSecond())
							          secondEquals=true;
							      else
						              secondEquals=second.equals(p.getSecond());
						          result=firstEquals&&secondEquals;
							     }
		    }
		return(result);
	}
	
	@Override
    public int hashCode(){
    	int hashCode = 1;
	    hashCode = 31*hashCode + (first==null ? 0 : first.hashCode());
	    hashCode = 31*hashCode + (second==null ? 0 : second.hashCode());
    	return(hashCode);
    }
	
	@Override
	public String toString(){
		final String firstString=(first==null)?"null":first.toString();
		final String secondString=(second==null)?"null":second.toString();
		return("<"+firstString+";"+secondString+">");
	}
}
