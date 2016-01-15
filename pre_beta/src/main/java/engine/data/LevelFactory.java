/**
 * Copyright (c) 2006-2016 Julien Gouesse
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

import java.util.Map;
import java.util.Map.Entry;
import com.ardor3d.math.type.ReadOnlyVector3;
import engine.abstraction.AbstractFactory;

public class LevelFactory extends AbstractFactory<Level>{

	public LevelFactory(){
		super();
	}
	
	public boolean addNewLevel(final String label,final String identifier,final String resourceName,final String boundingBoxListResourceName,final Map<String,ReadOnlyVector3[]> enemyPositionsMap,
		     final Map<String,ReadOnlyVector3[]> medikitPositionsMap,final Map<String,ReadOnlyVector3[]> weaponPositionsMap,
		     final Map<String,ReadOnlyVector3[]> ammoBoxPositionsMap,final String skyboxIdentifier,final Map<String,Entry<String,ReadOnlyVector3[]>> teleporterPositionsMap,final Objective... objectives){
		boolean success=identifier!=null&&!componentMap.containsKey(identifier);
		if(success)
			{final Level level=new Level(label,identifier,resourceName,boundingBoxListResourceName,enemyPositionsMap,medikitPositionsMap,weaponPositionsMap,ammoBoxPositionsMap,skyboxIdentifier,teleporterPositionsMap,objectives);
			 success=add(identifier,level);
			}
		return(success);
	}
}
