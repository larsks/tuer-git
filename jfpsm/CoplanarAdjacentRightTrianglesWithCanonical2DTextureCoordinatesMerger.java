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

import java.util.EnumSet;

import com.ardor3d.scenegraph.Mesh;
import com.ardor3d.util.geom.GeometryTool;
import com.ardor3d.util.geom.VertMap;
import com.ardor3d.util.geom.GeometryTool.MatchCondition;


/**
 * mesh optimizer, which merges coplanar adjacent right triangles whose 2D texture coordinates 
 * are [0;0], [0;1], [1;0] or [1;1]
 * 
 * @author Julien Gouesse
 *
 */
public class CoplanarAdjacentRightTrianglesWithCanonical2DTextureCoordinatesMerger {

	/**
	 * 
	 * @param mesh using the same set of textures (only on texture per unit) for all vertices
	 * @return
	 */
	public static VertMap minimizeVerts(final Mesh mesh) {
		//FIXME split the mesh into several meshes using different texture states
		EnumSet<MatchCondition> conditions = EnumSet.of(MatchCondition.UVs, MatchCondition.Normal, MatchCondition.Color);
		VertMap result = GeometryTool.minimizeVerts(mesh, conditions);
	    //TODO use VertGroupData vertices groups to sort vertices by textures or pass a mesh using a single texture
	    //TODO use all conditions with GeometryTool (except the group condition if the mesh uses a single texture)
	    //TODO separate right triangles with canonical 2D texture coordinates from the others
	    //TODO sort the triangles of the former set by planes (4D: normalized normal + distance to plane)
	    //TODO subdivide the sets so that each new set contains at most 2 triangles with the same hypotenuse (used to make quads)
	    //TODO merge all sets containing adjacent quads
	    //TODO merge as much triangles of each set as possible if it contains more than 4 triangles (2 quads) by maximizing their areas 
	    //     (update their texture coordinates (use coordinates greater than 1) in order to use texture repeat)
		return result;
	}
}
