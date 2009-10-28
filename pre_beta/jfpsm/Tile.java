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

import java.awt.Color;

/**
 * A tile associates a color with a textured pattern. This pattern
 * can be a voxel.
 * @author Julien Gouesse
 *
 */
public final class Tile extends JFPSMUserObject{   
    
    
	static{SerializationHelper.forceHandlingOfTransientModifiersForXMLSerialization(Tile.class);}
	
    private static final long serialVersionUID=1L;

    /**
     * color used to identify a tile, appears in the 2D maps
     */
    private Color color;
    
    private VolumeParameters<?> volumeParameters;

    private transient boolean dirty;
    
    
    public Tile(){
    	this("");
    }
    
    public Tile(String name){
        super(name);
        //this tile is being created, this is a pending change
        markDirty();
        color=Color.WHITE;
        volumeParameters=null;
    }
    
    
    public final Color getColor(){
        return(color);
    }

    public final void setColor(Color color){
        this.color=color;
        markDirty();
    }
    
    @Override
    public final boolean isDirty(){
        return(dirty||(volumeParameters!=null&&volumeParameters.isDirty()));
    }
    
    @Override
    public final void unmarkDirty(){
        dirty=false;
    }
    
    @Override
    public final void markDirty(){
        dirty=true;
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

    public final VolumeParameters<?> getVolumeParameters(){
        return(volumeParameters);
    }

    public final void setVolumeParameters(VolumeParameters<?> volumeParameters){
        this.volumeParameters=volumeParameters;
        markDirty();
    }
}