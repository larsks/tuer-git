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
package engine.abstraction;

import java.util.HashMap;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicInteger;

public abstract class AbstractFactory<T>{
	
	protected final AtomicInteger autoIncrementalIndex=new AtomicInteger(0);
	/**
	 * map containing human readable identifiers
	 * for its components
	 */
	protected final HashMap<String,T> componentMap;
	
	/**
	 * map containing integer identifiers for its 
	 * components
	 */
	protected final HashMap<Integer,T> componentIdentifierMap;

	public AbstractFactory(){
		componentMap=new HashMap<>();
		componentIdentifierMap=new HashMap<>();
	}

	protected boolean add(final String stringId,final T component){
		boolean success=stringId!=null&&component!=null&&!componentMap.containsKey(stringId)&&
				        !componentMap.containsValue(component)&&!componentIdentifierMap.containsValue(component);
		if(success)
		    {final Integer generatedIdObj=Integer.valueOf(autoIncrementalIndex.getAndIncrement());
			 componentMap.put(stringId,component);
		     componentIdentifierMap.put(generatedIdObj,component);
		    }
		return(success);
	}
	
	public int getId(final T component){
		int id=-1;
		if(component!=null)
		    for(Entry<Integer,T> entry:componentIdentifierMap.entrySet())
			    if(entry.getValue().equals(component))
				    id=entry.getKey().intValue();
		return(id);
	}
	
	public T get(final int integerId){
		return(0<=integerId&&integerId<getSize()?componentIdentifierMap.get(Integer.valueOf(integerId)):null);
	}
	
	public T get(final String stringId){
		return(componentMap.get(stringId));
	}
	
	public int getSize(){
		return(componentMap.size());
	}
}
