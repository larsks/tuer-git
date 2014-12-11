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

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import com.ardor3d.image.Image;
import com.ardor3d.image.Texture;
import com.ardor3d.image.util.ImageLoaderUtil;
import com.ardor3d.math.ColorRGBA;
import com.ardor3d.math.type.ReadOnlyVector3;
import com.ardor3d.scenegraph.Node;
import com.ardor3d.scenegraph.extension.Skybox;
import com.ardor3d.util.TextureManager;
import com.ardor3d.util.export.binary.BinaryImporter;
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
    /**objectives of the mission*/
    private final List<Objective> objectives;
    /**positions of the enemies*///TODO handle several kinds of enemy, use a map, load the template nodes
    private final ReadOnlyVector3[] enemiesPositions;
    /**positions of the medikits*///TODO handle several kinds of medikit
    private final ReadOnlyVector3[] medikitsPositions;
    /**root node whose hierarchy contains the geometry of the main model*/
    private Node mainModel;
    /**sky box*/
    private Skybox skybox;
    /**map of the weapons' positions sorted by type*/
    private final Map<String,ReadOnlyVector3[]> weaponsPositionsMap;
    //TODO teleporters, ammo
    private final BinaryImporter binaryImporter;
    
    public Level(final int identifier,final String name,final ReadOnlyVector3[] enemiesPositions,final ReadOnlyVector3[] medikitsPositions,
    		     final Map<String,ReadOnlyVector3[]> weaponsPositionsMap,final Objective... objectives){
    	super();
    	this.binaryImporter=new BinaryImporter();
    	this.identifier=identifier;
    	this.name=name;
    	final List<Objective> localObjectives=new ArrayList<>();
    	if(objectives!=null&&objectives.length>0)
    	    localObjectives.addAll(Arrays.asList(objectives));
    	this.objectives=Collections.unmodifiableList(localObjectives);
    	this.enemiesPositions=enemiesPositions;
    	this.medikitsPositions=medikitsPositions;
    	this.weaponsPositionsMap=weaponsPositionsMap==null?null:Collections.unmodifiableMap(weaponsPositionsMap);
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
    
    public Node loadMainModel(){
    	if(mainModel==null)
    	    {//TODO support a custom resource name
    		 try{mainModel=(Node)binaryImporter.load(getClass().getResource("/abin/LID"+identifier+".abin"));}
	         catch(IOException ioe)
	         {throw new RuntimeException("level loading failed",ioe);}
    	    }
    	return(mainModel);
    }
    
    public Node getMainModel(){
    	return(mainModel);
    }
    
    public Skybox loadSkybox(){
		if(skybox==null)
		    {skybox=new Skybox("skybox",64,64,64);
		     final Texture north=TextureManager.load(new URLResourceSource(getClass().getResource("/images/1.jpg")),Texture.MinificationFilter.BilinearNearestMipMap,true);
		     final Texture south=TextureManager.load(new URLResourceSource(getClass().getResource("/images/3.jpg")),Texture.MinificationFilter.BilinearNearestMipMap,true);
		     final Texture east=TextureManager.load(new URLResourceSource(getClass().getResource("/images/2.jpg")),Texture.MinificationFilter.BilinearNearestMipMap,true);
		     final Texture west=TextureManager.load(new URLResourceSource(getClass().getResource("/images/4.jpg")),Texture.MinificationFilter.BilinearNearestMipMap,true);
		     final Texture up=TextureManager.load(new URLResourceSource(getClass().getResource("/images/6.jpg")),Texture.MinificationFilter.BilinearNearestMipMap,true);
		     final Texture down=TextureManager.load(new URLResourceSource(getClass().getResource("/images/5.jpg")),Texture.MinificationFilter.BilinearNearestMipMap,true);
		     skybox.setTexture(Skybox.Face.North,north);
		     skybox.setTexture(Skybox.Face.West,west);
		     skybox.setTexture(Skybox.Face.South,south);
		     skybox.setTexture(Skybox.Face.East,east);
		     skybox.setTexture(Skybox.Face.Up,up);
		     skybox.setTexture(Skybox.Face.Down,down);
            }
		return(skybox);
    }
    
    public Skybox getSkybox(){
    	return(skybox);
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
    
    public ReadOnlyVector3[] getEnemiesPositions(){
    	return(enemiesPositions);
    }
    
    public ReadOnlyVector3[] getMedikitsPositions(){
    	return(medikitsPositions);
    }
    
    public ReadOnlyVector3[] getWeaponsPositions(final String weaponIdentifier) {
    	return(weaponsPositionsMap.get(weaponIdentifier));
    }
}
