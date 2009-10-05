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
	
    private static final long serialVersionUID = 1L;
    
    private static final int size=256;
    
    private static final String containerMapFilename="containermap.png";
    
    private static final String lightMapFilename="lightmap.png";
    
    private static final String contentMapFilename="contentmap.png";
    
    private transient boolean dirty;
    
    private transient BufferedImage containerMap,lightMap,contentMap;
    

	public Floor(){
    	this("");
    }
    
    public Floor(String name){
        super(name);
        initializeMaps();
        dirty=true;
    }
    
    
    private final void initializeMaps(){
    	containerMap=new BufferedImage(size,size,BufferedImage.TYPE_INT_ARGB);
    	lightMap=new BufferedImage(size,size,BufferedImage.TYPE_INT_ARGB);
    	contentMap=new BufferedImage(size,size,BufferedImage.TYPE_INT_ARGB);
    	for(int x=0;x<containerMap.getWidth();x++)
            for(int y=0;y<containerMap.getHeight();y++)
                containerMap.setRGB(x,y,Color.WHITE.getRGB());
    	for(int x=0;x<lightMap.getWidth();x++)
            for(int y=0;y<lightMap.getHeight();y++)
                lightMap.setRGB(x,y,Color.WHITE.getRGB());
    	for(int x=0;x<contentMap.getWidth();x++)
            for(int y=0;y<contentMap.getHeight();y++)
                contentMap.setRGB(x,y,Color.WHITE.getRGB());
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
    
    public final String getContainerMapFilename(){
    	return(containerMapFilename);
    }
    
    public final String getLightMapFilename(){
    	return(lightMapFilename);
    }
    
    public final String getContentMapFilename(){
    	return(contentMapFilename);
    }  
    
    final BufferedImage getContainerMap(){
		return(containerMap);
	}

	final void setContainerMap(BufferedImage containerMap){
		this.containerMap=containerMap;
	}

	final BufferedImage getLightMap(){
		return(lightMap);
	}

	final void setLightMap(BufferedImage lightMap){
		this.lightMap=lightMap;
	}

	final BufferedImage getContentMap(){
		return(contentMap);
	}

	final void setContentMap(BufferedImage contentMap){
		this.contentMap=contentMap;
	}
	
	final int getSize(){
		return(size);
	}
}