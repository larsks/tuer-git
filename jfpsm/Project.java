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
 * Instance of a game, it contains a container of floors and a container of tiles.
 * It is saved as a ZIP archive that contains an XML file for most of the data and the image files.
 * @author Julien Gouesse
 *
 */
public final class Project extends Namable implements Dirtyable{
    
    
	static{SerializationHelper.forceHandlingOfTransientModifiersForXMLSerialization(Project.class);}
	
    private static final long serialVersionUID = 1L;
    
    private static final String fileExtension = ".jfpsm.zip";
    
    private FloorSet floorSet;

    private TileSet tileSet;
    
    public Project(){
    	this("");
    }
    
    public Project(String name){
        super(name);
        floorSet=new FloorSet("Floor Set");
        tileSet=new TileSet("Tile Set");
    }
    
    
    @Override
    public final boolean equals(Object o){
        boolean result;
        if(o==null||!(o instanceof Project))
            result=false;
        else
            {String name=getName();
             String otherName=((Project)o).getName();
             if(name==null)
                 result=otherName==null;
             else
                 result=name.equals(otherName);
            }
        return(result);
    }
    
    @Override
    public final boolean isDirty(){
        return(floorSet.isDirty()||tileSet.isDirty());
    }
    
    @Override
    public final void unmarkDirty(){}
    
    @Override
    public final void markDirty(){}
    
    public final FloorSet getFloorSet(){
        return(floorSet);
    }

    public final void setFloorSet(FloorSet floorSet){
        this.floorSet=floorSet;
    }

    public final TileSet getTileSet(){
        return(tileSet);
    }

    public final void setTileSet(TileSet tileSet){
        this.tileSet=tileSet;
    }
    
    public static final String getFileExtension(){
    	return(fileExtension);
    }
}