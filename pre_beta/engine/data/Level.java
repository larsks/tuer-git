/**
 * Copyright (c) 2006-2014 Julien Gouesse
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package engine.data;

import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import com.ardor3d.image.Image;
import com.ardor3d.image.util.ImageLoaderUtil;
import com.ardor3d.math.ColorRGBA;
import com.ardor3d.util.resource.URLResourceSource;
import engine.misc.ImageHelper;

/**
 * Data model of the level
 * 
 * @author Julien Gouesse
 *
 */
public class Level{
	/**unique identifier, must be greater than or equal to zero*/
	private final int identifier;
	/**human readable name*/
	private final String name;
	/**@deprecated this collision map is a temporary solution, the real collision system will have to use the 3D mesh instead of a flat 2D array*/
    @Deprecated
    private boolean[][] collisionMap;
    //TODO ammo, health
    /**objectives of the mission*/
    private final List<Objective> objectives;
    //TODO enemies
    /**root node whose hierarchy contains the geometry*/
    //private Node node;
    //TODO weapons, skybox, teleporters
    
    public Level(final int identifier,final String name,Objective... objectives){
    	super();
    	this.identifier=identifier;
    	this.name=name;
    	final List<Objective> localObjectives=new ArrayList<>();
    	if(objectives!=null&&objectives.length>0)
    	    localObjectives.addAll(Arrays.asList(objectives));
    	this.objectives=Collections.unmodifiableList(localObjectives);
    }
    
    @Deprecated
    public final void readCollisionMap(){
    	final URL mapUrl=Level.class.getResource("/images/containermap.png");
    	final URLResourceSource mapSource=new URLResourceSource(mapUrl);
    	final Image map=ImageLoaderUtil.loadImage(mapSource,false);
    	collisionMap=new boolean[map.getWidth()][map.getHeight()];
    	final ImageHelper imgHelper=new ImageHelper();
    	for(int y=0;y<map.getHeight();y++)
	    	for(int x=0;x<map.getWidth();x++)
	    		{final int argb=imgHelper.getARGB(map,x,y);
	    		 collisionMap[x][y]=(argb==ColorRGBA.BLUE.asIntARGB());
	    		}
    }
    
    public int getIdentifier(){
    	return(identifier);
    }
    
    public String getName(){
    	return(name);
    }
    
    @Deprecated
    public boolean[][] getCollisionMap(){
    	return(collisionMap);
    }
    
    public List<Objective> getObjectives(){
    	return(objectives);
    }
}
