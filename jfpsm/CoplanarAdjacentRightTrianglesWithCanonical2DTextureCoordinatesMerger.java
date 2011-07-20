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
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map.Entry;

import com.ardor3d.math.Plane;
import com.ardor3d.math.Triangle;
import com.ardor3d.math.Vector2;
import com.ardor3d.math.Vector3;
import com.ardor3d.math.type.ReadOnlyVector3;
import com.ardor3d.renderer.IndexMode;
import com.ardor3d.scenegraph.Mesh;
import com.ardor3d.scenegraph.MeshData;
import com.ardor3d.util.geom.BufferUtils;
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
public class CoplanarAdjacentRightTrianglesWithCanonical2DTextureCoordinatesMerger{

	private static final class RightTriangleInfo{
		
		private final int primitiveIndex;
		
		private final int sectionIndex;
		
		private final int sideIndexOfHypotenuse;
		
		
		private RightTriangleInfo(int primitiveIndex,int sectionIndex,int sideIndexOfHypotenuse){
			this.primitiveIndex=primitiveIndex;
			this.sectionIndex=sectionIndex;
			this.sideIndexOfHypotenuse=sideIndexOfHypotenuse;
		}
	}
	
	/**
	 * 
	 * @param mesh using the same set of textures (only on texture per unit) for all vertices
	 * @return
	 */
	public static VertMap minimizeVerts(final Mesh mesh) {
		//uses all conditions with GeometryTool
		final EnumSet<MatchCondition> conditions=EnumSet.of(MatchCondition.UVs, MatchCondition.Normal, MatchCondition.Color);
		//reduces the geometry to avoid duplication of vertices
		final VertMap result=GeometryTool.minimizeVerts(mesh, conditions);
		
		final MeshData meshData=mesh.getMeshData();
		//if there is a texture buffer for the first texture unit
		if(meshData.getTextureBuffer(0)!=null)
		    {//first step: separate right triangles with canonical 2D texture coordinates from the others
			 final ArrayList<RightTriangleInfo> rightTrianglesWithCanonical2DTextureCoordinatesInfos=new ArrayList<CoplanarAdjacentRightTrianglesWithCanonical2DTextureCoordinatesMerger.RightTriangleInfo>();
			 //loops on all sections of the mesh data
			 Vector3[] triangleVertices=new Vector3[3];
			 int[] triangleIndices=new int[3];
			 Vector2[] triangleTextureCoords=new Vector2[3];
			 final double[] trianglesSidesDistancesSquared=new double[3];
			 double u,v;
			 int sideIndexOfHypotenuse;
			 boolean hasCanonicalTextureCoords;
			 for(int sectionIndex=0,sectionCount=meshData.getSectionCount();sectionIndex<sectionCount;sectionIndex++)
			     //only takes care of sections containing triangles
				 if(meshData.getIndexMode(sectionIndex)==IndexMode.Triangles)
			         {//loops on all triangles of each section
					  for(int trianglePrimitiveIndex=0,triangleCount=meshData.getPrimitiveCount(sectionIndex);trianglePrimitiveIndex<triangleCount;trianglePrimitiveIndex++)
				          {//gets the 3 vertices of the triangle
						   triangleVertices=meshData.getPrimitive(trianglePrimitiveIndex,sectionIndex,triangleVertices);
				    	   //uses Pythagorean theorem to check whether the triangle is right
						   //computes the squared distances of all sides
						   for(int triangleSideIndex=0;triangleSideIndex<3;triangleSideIndex++)
						       trianglesSidesDistancesSquared[triangleSideIndex]=triangleVertices[triangleSideIndex].distanceSquared(triangleVertices[(triangleSideIndex+1)%3]);
						   //uses these squared distances to find the hypotenuse if any
						   sideIndexOfHypotenuse=-1;						   
						   for(int triangleSideIndex=0;triangleSideIndex<3&&sideIndexOfHypotenuse==-1;triangleSideIndex++)
                               if(trianglesSidesDistancesSquared[triangleSideIndex]==trianglesSidesDistancesSquared[(triangleSideIndex+1)%3]+trianglesSidesDistancesSquared[(triangleSideIndex+2)%3])
							       sideIndexOfHypotenuse=triangleSideIndex;						   
						   //if this triangle is right
						   if(sideIndexOfHypotenuse!=-1)
						       {//checks whether its texture coordinates are canonical
							    hasCanonicalTextureCoords=true;
							    for(int textureIndex=0;meshData.getTextureBuffer(textureIndex)!=null&&hasCanonicalTextureCoords;textureIndex++){
							    	triangleTextureCoords=getPrimitiveTextureCoords(meshData,trianglePrimitiveIndex,sectionIndex,textureIndex,triangleTextureCoords);		    
								    for(int triangleTextureCoordsIndex=0;triangleTextureCoordsIndex<3&&hasCanonicalTextureCoords;triangleTextureCoordsIndex++)
								        {u=triangleTextureCoords[triangleTextureCoordsIndex].getX();
								         v=triangleTextureCoords[triangleTextureCoordsIndex].getY();
								         if((u!=0&&u!=1)||(v!=0&&v!=1))
								        	 hasCanonicalTextureCoords=false;
								        }
							    }	    
							    if(hasCanonicalTextureCoords)
							        {//stores the side index of its hypotenuse and several indices allowing to retrieve the required data further 
							         RightTriangleInfo rightTriangleInfo=new RightTriangleInfo(trianglePrimitiveIndex,sectionIndex,sideIndexOfHypotenuse);
							         rightTrianglesWithCanonical2DTextureCoordinatesInfos.add(rightTriangleInfo);
							        }
						       }
				          }
			         }
			 //second step: sorts the triangles of the former set by planes (4D: normal + distance to plane)
			 HashMap<Plane,ArrayList<RightTriangleInfo>> mapOfTrianglesByPlanes=new HashMap<Plane, ArrayList<RightTriangleInfo>>();
			 Triangle tmpTriangle=new Triangle();
			 ReadOnlyVector3 triangleNormal;
			 double distanceToPlane;
			 for(RightTriangleInfo info:rightTrianglesWithCanonical2DTextureCoordinatesInfos)
			     {//gets the 3 vertices of the triangle
				  triangleVertices=meshData.getPrimitive(info.primitiveIndex,info.sectionIndex,triangleVertices);		  
				  //sets the vertices of the temporary triangle
				  for(int vertexInternalIndex=0;vertexInternalIndex<3;vertexInternalIndex++)
				      tmpTriangle.set(vertexInternalIndex,triangleVertices[vertexInternalIndex]);
				  //computes its normal
				  triangleNormal=tmpTriangle.getNormal();
				  //computes its distance to plane d=dot(normal,vertex)
				  distanceToPlane=triangleNormal.dot(tmpTriangle.getCenter());
				  //creates the plane
				  final Plane plane=new Plane(new Vector3(triangleNormal),distanceToPlane);
				  //puts it into a map whose key is a given plane
				  ArrayList<RightTriangleInfo> infoList=mapOfTrianglesByPlanes.get(plane);
				  if(infoList==null)
				      {infoList=new ArrayList<RightTriangleInfo>();
				       mapOfTrianglesByPlanes.put(plane,infoList);
				      }
				  infoList.add(info);		  
			     }			 
			 //third step: retain only triangles by pairs which could be used to create rectangles
			 Vector3[] tri1Vertices=new Vector3[3];
			 Vector3[] tri2Vertices=new Vector3[3];
			 //for each plane of the map
			 for(Entry<Plane, ArrayList<RightTriangleInfo>> entry:mapOfTrianglesByPlanes.entrySet())
			     {ArrayList<RightTriangleInfo> rightTrianglesWithSameHypotenusesByPairs=new ArrayList<CoplanarAdjacentRightTrianglesWithCanonical2DTextureCoordinatesMerger.RightTriangleInfo>();
				  ArrayList<RightTriangleInfo> rightTriangles=entry.getValue();
				  final int triCount=rightTriangles.size();
				  //for each RightTriangleInfo instance
				  for(int triIndex1=0;triIndex1<triCount-1;triIndex1++)
				      {RightTriangleInfo tri1=rightTriangles.get(triIndex1);
					   tri1Vertices=meshData.getPrimitive(tri1.primitiveIndex,tri1.sectionIndex,tri1Vertices);
					   for(int triIndex2=triIndex1+1;triIndex2<triCount;triIndex2++)
					       {RightTriangleInfo tri2=rightTriangles.get(triIndex2);
						    tri2Vertices=meshData.getPrimitive(tri2.primitiveIndex,tri2.sectionIndex,tri2Vertices);
						    //checks if the both triangles have the same hypotenuse, if their opposite side have the same length and if 
						    //their vertices at the right angle are different. It allows to know whether they could be used to create a rectangle
						    //TODO: check the texture coordinates (they need to have a particular form in order to be merged with other triangles)
						    if(((tri1Vertices[tri1.sideIndexOfHypotenuse].equals(tri2Vertices[tri2.sideIndexOfHypotenuse])&&
						        tri1Vertices[(tri1.sideIndexOfHypotenuse+1)%3].equals(tri2Vertices[(tri2.sideIndexOfHypotenuse+1)%3])&&
						        tri1Vertices[tri1.sideIndexOfHypotenuse].distanceSquared(tri1Vertices[(tri1.sideIndexOfHypotenuse+2)%3])==
						        tri2Vertices[(tri2.sideIndexOfHypotenuse+1)%3].distanceSquared(tri2Vertices[(tri2.sideIndexOfHypotenuse+2)%3])&&
						        tri1Vertices[(tri1.sideIndexOfHypotenuse+1)%3].distanceSquared(tri1Vertices[(tri1.sideIndexOfHypotenuse+2)%3])==
						        tri2Vertices[tri2.sideIndexOfHypotenuse].distanceSquared(tri2Vertices[(tri2.sideIndexOfHypotenuse+2)%3]))||
						       (tri1Vertices[tri1.sideIndexOfHypotenuse].equals(tri2Vertices[(tri2.sideIndexOfHypotenuse+1)%3])&&
								tri1Vertices[(tri1.sideIndexOfHypotenuse+1)%3].equals(tri2Vertices[tri2.sideIndexOfHypotenuse])&&
						        tri1Vertices[tri1.sideIndexOfHypotenuse].distanceSquared(tri1Vertices[(tri1.sideIndexOfHypotenuse+2)%3])==
						        tri2Vertices[tri2.sideIndexOfHypotenuse].distanceSquared(tri2Vertices[(tri2.sideIndexOfHypotenuse+2)%3])&&
						        tri1Vertices[(tri1.sideIndexOfHypotenuse+1)%3].distanceSquared(tri1Vertices[(tri1.sideIndexOfHypotenuse+2)%3])==
						        tri2Vertices[(tri2.sideIndexOfHypotenuse+1)%3].distanceSquared(tri2Vertices[(tri2.sideIndexOfHypotenuse+2)%3])))&&
						        !tri1Vertices[(tri1.sideIndexOfHypotenuse+2)%3].equals(tri2Vertices[(tri2.sideIndexOfHypotenuse+2)%3]))
						        {rightTrianglesWithSameHypotenusesByPairs.add(tri1);
						         rightTrianglesWithSameHypotenusesByPairs.add(tri2);
						    	 break;
						        }
					       }
				      }
				  rightTriangles.clear();
				  rightTriangles.addAll(rightTrianglesWithSameHypotenusesByPairs);
			     }
			 //TODO: fourth step: merge all sets containing adjacent quads
		     //TODO create a list of lists of adjacent pairs of triangles inside the same plane
			 //TODO: fifth step: merge as much triangles of each set as possible if it contains more than 4 triangles (2 quads) by maximizing their areas 
		     //     (update their texture coordinates (use coordinates greater than 1) in order to use texture repeat)			 
		    }
		return result;
	}
	
	/**
     * Gets the texture coordinates of the primitive.
     * 
     * @param primitiveIndex
     *            the primitive index
     * @param section
     *            the section
     * @param textureIndex
     *            the texture index
     * @param store
     *            the store
     * 
     * @return the texture coordinates of the primitive
     */
    public static Vector2[] getPrimitiveTextureCoords(final MeshData meshData, final int primitiveIndex, final int section, final int textureIndex, final Vector2[] store) {
    	if (meshData.getTextureBuffer(textureIndex) == null) {
    	    return null;
    	}
    	final int count = meshData.getPrimitiveCount(section);
        if (primitiveIndex >= count || primitiveIndex < 0) {
            throw new IndexOutOfBoundsException("Invalid primitiveIndex '" + primitiveIndex + "'.  Count is " + count);
        }

        final IndexMode mode = meshData.getIndexMode(section);
        final int rSize = mode.getVertexCount();
        Vector2[] result = store;
        if (result == null || result.length < rSize) {
            result = new Vector2[rSize];
        }

        for (int i = 0; i < rSize; i++) {
        	if (result[i] == null) {
        		result[i] = new Vector2();
        	}
        	if (meshData.getIndexBuffer() != null) {
        		// indexed geometry
        		BufferUtils.populateFromBuffer(result[i], meshData.getTextureBuffer(textureIndex),
        				meshData.getIndices().get(meshData.getVertexIndex(primitiveIndex, i, section)));
        	} else {
        		// non-indexed geometry
        		BufferUtils
        		.populateFromBuffer(result[i], meshData.getTextureBuffer(textureIndex), meshData.getVertexIndex(primitiveIndex, i, section));
        	}
        }

        return result;
    }
}
