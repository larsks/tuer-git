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

import java.util.ArrayList;

/**
 * Container of tiles
 * @author Julien Gouesse
 *
 */
public final class TileSet extends JFPSMUserObject{
    
    
	static{SerializationHelper.forceHandlingOfTransientModifiersForXMLSerialization(TileSet.class);}
	
    private static final long serialVersionUID = 1L;
    
    private ArrayList<Tile> tilesList;
    
    private transient boolean dirty;
    
    
    public TileSet(){
        this("");
    }
    
    public TileSet(String name){
        super(name);
        tilesList=new ArrayList<Tile>();
        dirty=true;
    }
    
    @Override
    public final boolean isDirty(){
        boolean dirty=this.dirty;
        if(!dirty)
            for(Tile tile:tilesList)
                if(tile.isDirty())
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

    public final void addTile(Tile tile){
        tilesList.add(tile);
        dirty=true;
    }
    
    public final void removeTile(Tile tile){
        tilesList.remove(tile);
        dirty=true;
    }
    
    public final ArrayList<Tile> getTilesList(){
        return(tilesList);
    }

    public final void setTilesList(ArrayList<Tile> tilesList){
        this.tilesList=tilesList;
        dirty=true;
    }
    
    @Override
    public final void resolve(){
        unmarkDirty();
    }
    
    @Override
    final boolean canInstantiateChildren(){
        return(true);
    }

    @Override
    final boolean isOpenable(){
        return(true);
    }

    @Override
    final boolean isRemovable(){
        return(false);
    }
}