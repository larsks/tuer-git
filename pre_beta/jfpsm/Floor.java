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

/**
 * A floor is a subsection in a level (downstairs, upstairs, ...). 
 * It uses several images (containers, lights, contents) to contain the 
 * color codes that match with the tiles.
 * @author Julien Gouesse
 *
 */
public final class Floor extends JFPSMUserObject{
    
    
	static{SerializationHelper.forceHandlingOfTransientModifiersForXMLSerialization(Floor.class);}
	
    private static final long serialVersionUID=1L;
    
    private Map[] maps;
    

	public Floor(){
    	this("");
    }
    
    public Floor(String name){
        super(name);
        initializeMaps();
        markDirty();
    }
    
    
    private final void initializeMaps(){
    	maps=new Map[MapType.values().length];
    	for(MapType type:MapType.values())
    		maps[type.ordinal()]=new Map(type.getLabel());
    }
    
    final Map getMap(MapType type){
    	return(maps[type.ordinal()]);
    }
    
    @Override
    public final boolean isDirty(){
        boolean dirty=false;
        for(MapType type:MapType.values())
            if(maps[type.ordinal()].isDirty())
                {dirty=true;
                 break;
                }
        return(dirty);
    }
    
    @Override
    public final void unmarkDirty(){}
    
    @Override
    public final void markDirty(){}
    
    @Override
    public final void resolve(){
    	initializeMaps();
    	unmarkDirty();
    }

    public final Map[] getMaps(){
        return(maps);
    }

    public final void setMaps(Map[] maps){
        this.maps=maps;
        markDirty();
    }
    
    @Override
    final boolean canInstantiateChildren(){
        return(false);
    }

    @Override
    final boolean isOpenable(){
        return(true);
    }

    @Override
    final boolean isRemovable(){
        return(true);
    }
}