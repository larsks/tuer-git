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
package jfpsm;

import java.io.ObjectStreamException;
import java.util.ArrayList;

/**
 * Container of floors
 * @author Julien Gouesse
 *
 */
public final class FloorSet extends Namable implements Dirtyable{
    
    
	static{SerializationHelper.forceHandlingOfTransientModifiersForXMLSerialization(FloorSet.class);}
	
    private static final long serialVersionUID = 1L;
    
    private ArrayList<Floor> floorsList;
    
    private transient boolean dirty;
    
    
    public FloorSet(){
        this("");
    }
    
    public FloorSet(String name){
        super(name);
        floorsList=new ArrayList<Floor>();
        dirty=true;
    }
    
    @Override
    public final boolean isDirty(){
        boolean dirty=this.dirty;
        if(!dirty)
            for(Floor floor:floorsList)
                if(floor.isDirty())
                    {dirty=true;
                     break;
                    }
        return(dirty);
    }
    
    @Override
    public final void unmarkDirty(){
        dirty=false;
    }
    
    @Override
    public final void markDirty(){
        dirty=true;
    }
    
    public final void addFloor(Floor floor){
        floorsList.add(floor);
        dirty=true;
    }
    
    public final void removeFloor(Floor floor){
        floorsList.remove(floor);
        dirty=true;
    }

    public final ArrayList<Floor> getFloorsList(){
        return(floorsList);
    }

    public final void setFloorsList(ArrayList<Floor> floorsList){
        this.floorsList=floorsList;
        dirty=true;
    }
    
    public final Object readResolve() throws ObjectStreamException{
        //the floor has just been loaded, there is not yet any change
        unmarkDirty();
        return(this);
    }
    
    public final Object writeReplace() throws ObjectStreamException{
        //the floor is being saved, there is no more pending change
        unmarkDirty();
        return(this);
    }
    
    
}