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
import com.ardor3d.math.Matrix3;
import com.ardor3d.math.Quaternion;
import com.ardor3d.math.Vector3;
import com.ardor3d.math.type.ReadOnlyVector3;
import com.ardor3d.scenegraph.Node;
import com.ardor3d.scenegraph.extension.Skybox;
import com.ardor3d.util.TextureManager;
import com.ardor3d.util.export.binary.BinaryImporter;
import com.ardor3d.util.resource.URLResourceSource;

import engine.data.common.userdata.WeaponUserData;
import engine.misc.ImageHelper;
import engine.weaponry.Weapon;
import engine.weaponry.WeaponFactory;

/**
 * Data model of the level
 * 
 * @author Julien Gouesse
 *
 */
public class Level{
	/**human readable name*/
	private final String label;
	/**name of the resource, i.e the binary file containing the 3D model*/
	private final String resourceName;
	/**unique identifier, must be greater than or equal to zero*/
	private final String identifier;
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
    /**map of the weapons positions sorted by type*/
    private final Map<String,ReadOnlyVector3[]> weaponsPositionsMap;
    //TODO teleporters, ammo
    private final BinaryImporter binaryImporter;
    
    public Level(final String label,final String resourceName,final String identifier,final ReadOnlyVector3[] enemiesPositions,final ReadOnlyVector3[] medikitsPositions,
    		     final Map<String,ReadOnlyVector3[]> weaponsPositionsMap,final Objective... objectives){
    	super();
    	this.binaryImporter=new BinaryImporter();
    	this.label=label;
    	this.resourceName=resourceName;
    	this.identifier=identifier;
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
    
    public List<Node> loadWeaponsModels(final WeaponFactory weaponFactory){
    	final List<Node> weaponsNodes=new ArrayList<>();
    	//N.B: only show working weapons
	    try{/*uziNode.setTranslation(111.5,0.15,219);
            smachNode.setTranslation(112.5,0.15,219);
            pistolNode.setTranslation(113.5,0.1,219);
            duplicatePistolNode.setTranslation(113.5,0.1,217);
            laserNode.setTranslation(116.5,0.1,219);
            shotgunNode.setTranslation(117.5,0.1,219);
            rocketLauncherNode.setTranslation(117.5,0.1,222);*/
	    	//TODO store the template nodes in order to use them during the cleanup
	    	//TODO move the transforms into the binary files and convert them into Wavefront OBJ
	    	final int weaponCount=weaponFactory.getSize();
            for(int weaponIndex=0;weaponIndex<weaponCount;weaponIndex++)
                {final Weapon weapon=weaponFactory.get(weaponIndex);
            	 final String weaponIdentifier=weapon.getIdentifier();
            	 final String weaponResourceName=weapon.getResourceName();
            	 final String weaponLabel=weapon.getLabel();
            	 final ReadOnlyVector3[] weaponsPos=weaponsPositionsMap.get(weaponIdentifier);
            	 if(weaponsPos!=null&&weaponsPos.length!=0)
            	     {final Node weaponTemplateNode=(Node)binaryImporter.load(getClass().getResource(weaponResourceName));
            	      weaponTemplateNode.setName(weaponLabel);
            	      final boolean digitalWatermarkEnabled,primary;
            		  switch(weaponIdentifier)
            		  {
            		      case "PISTOL_9MM":
            		          {//removes the bullet as it is not necessary now
                               ((Node)weaponTemplateNode.getChild(0)).detachChildAt(2);
                               weaponTemplateNode.setScale(0.02);
                               weaponTemplateNode.setRotation(new Quaternion().fromAngleAxis(-Math.PI/2,new Vector3(1,0,0)));
                               digitalWatermarkEnabled=false;
                               primary=true;
           		               break;
           		              }
            		      case "MAG_60":
            		          {weaponTemplateNode.setScale(0.02);
           	                   digitalWatermarkEnabled=false;
           	                   primary=true;
          		               break;
          		              }
            		      case "UZI":
            		          {weaponTemplateNode.setScale(0.2);
            		           digitalWatermarkEnabled=false;
           	                   primary=true;
          		               break;
          		              }
            		      case "SMACH":
            		          {weaponTemplateNode.setScale(0.2);
            		           digitalWatermarkEnabled=false;
              	               primary=true;
          		               break;
          		              }
            		      case "PISTOL_10MM":
            		          {weaponTemplateNode.setScale(0.001);
            		           weaponTemplateNode.setRotation(new Quaternion().fromEulerAngles(Math.PI/2,-Math.PI/4,Math.PI/2));
            		           digitalWatermarkEnabled=false;
              	               primary=true;
          		               break;
          		              }
            		      case "ROCKET_LAUNCHER":
            		          {//removes the scope
            		           weaponTemplateNode.detachChildAt(0);
            		           weaponTemplateNode.setScale(0.08);
            		           weaponTemplateNode.setRotation(new Quaternion().fromAngleAxis(-Math.PI,new Vector3(0,1,0)));
                               digitalWatermarkEnabled=false;
                               primary=true;
            		           break;
            		          }
            		      case "SHOTGUN":
            		          {weaponTemplateNode.setScale(0.1);
            		           digitalWatermarkEnabled=false;
                               primary=true;
            		           break;
            		          }
            		      case "LASER":
            		          {weaponTemplateNode.setScale(0.02);
            		           digitalWatermarkEnabled=false;
                               primary=true;
            		           break;
            		          }
            		      default:
            		          {digitalWatermarkEnabled=false;
                               primary=true;
            		          }
            		  }
            		  for(final ReadOnlyVector3 weaponPos:weaponsPos)
            		      {final Node weaponNode=weaponTemplateNode.makeCopy(false);
            		       weaponNode.setTranslation(weaponPos);
            		       weaponNode.setUserData(new WeaponUserData(weapon,new Matrix3(weaponTemplateNode.getRotation()),PlayerData.NO_UID,digitalWatermarkEnabled,primary));
            		       weaponsNodes.add(weaponNode);
            		      }
            	     }
                }
	       }
	    catch(IOException ioe)
	    {throw new RuntimeException("weapons loading failed",ioe);}
	    return(weaponsNodes);
    }
    
    public Node loadMainModel(){
    	if(mainModel==null)
    	    {//TODO support a custom resource name
    		 try{mainModel=(Node)binaryImporter.load(getClass().getResource(resourceName));}
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
    
    public String getIdentifier(){
    	return(identifier);
    }
    
    public String getLabel(){
    	return(label);
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
    
    public final String getResourceName(){
		return(resourceName);
	}
}
