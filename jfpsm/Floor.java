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
import java.awt.image.BufferedImage;

/**
 * A floor is a subsection in a level (downstairs, upstairs, ...). 
 * It uses several images (containers, lights, contents) to contain the 
 * color codes that match with the tiles.
 * @author Julien Gouesse
 *
 */
public final class Floor extends Namable implements Dirtyable,Resolvable{
    
    
	static{SerializationHelper.forceHandlingOfTransientModifiersForXMLSerialization(Floor.class);}
	
    private static final long serialVersionUID=1L;
    
    private static final int defaultSize=256;
    
    private transient boolean dirty;
    
    private transient BufferedImage[] maps;
    

	public Floor(){
    	this("");
    }
    
    public Floor(String name){
        super(name);
        initializeMaps();
        dirty=true;
    }
    
    
    private final void initializeMaps(){  	
    	maps=new BufferedImage[MapType.values().length];
    	for(int i=0;i<maps.length;i++)
    	    {maps[i]=new BufferedImage(defaultSize,defaultSize,BufferedImage.TYPE_INT_ARGB);
    	     for(int x=0;x<maps[i].getWidth();x++)
                 for(int y=0;y<maps[i].getHeight();y++)
                	 maps[i].setRGB(x,y,Color.WHITE.getRGB());
    	    }
    }
    
    final BufferedImage getMap(MapType type){
    	return(maps[type.ordinal()]);
    }
    
    final void setMap(MapType type,BufferedImage map){
    	maps[type.ordinal()]=map;
    }
    
    @Override
    public final boolean isDirty(){
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
    
    @Override
    public final void resolve(){
    	initializeMaps();
    }
}