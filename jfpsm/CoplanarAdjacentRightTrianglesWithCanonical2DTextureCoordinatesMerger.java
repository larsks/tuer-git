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

import java.lang.reflect.Array;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Arrays;
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
 * mesh optimizer, which merges coplanar adjacent right triangles whose all 2D texture coordinates 
 * are canonical ([0;0], [0;1], [1;0] or [1;1])
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
		
		@Override
		public final boolean equals(Object o){
			final boolean result;
			if(o==null||!(o instanceof RightTriangleInfo))
			    result=false;
			else
			    {RightTriangleInfo r=(RightTriangleInfo)o;
				 if(r==this)
					 result=true;
				 else
				     result=primitiveIndex==r.primitiveIndex&&
				            sectionIndex==r.sectionIndex&&
				            sideIndexOfHypotenuse==r.sideIndexOfHypotenuse;
			    }
			return(result);
		}
		
		@Override
		public int hashCode(){
			return((sideIndexOfHypotenuse&0xff)|(sectionIndex&0xff<<8)|(primitiveIndex&0xffff<<16));
		}
	}
	
	/**
	 * 
	 * @param mesh using the same set of textures (only on texture per unit) for all vertices
	 * @return
	 */
	public static VertMap minimizeVerts(final Mesh mesh){
		//uses all conditions with GeometryTool
		final EnumSet<MatchCondition> conditions=EnumSet.of(MatchCondition.UVs, MatchCondition.Normal, MatchCondition.Color);
		//reduces the geometry to avoid duplication of vertices
		final VertMap result=GeometryTool.minimizeVerts(mesh, conditions);
		
		final MeshData meshData=mesh.getMeshData();
		//if there is a texture buffer for the first texture unit
		if(meshData.getTextureBuffer(0)!=null)
		    {//first step: separates right triangles with canonical 2D texture coordinates from the others
			 final ArrayList<RightTriangleInfo> rightTrianglesWithCanonical2DTextureCoordinatesInfos=new ArrayList<RightTriangleInfo>();
			 //loops on all sections of the mesh data
			 Vector3[] triangleVertices=new Vector3[3];
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
						   triangleVertices=meshData.getPrimitiveVertices(trianglePrimitiveIndex,sectionIndex,triangleVertices);
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
							    for(int textureIndex=0;meshData.getTextureBuffer(textureIndex)!=null&&hasCanonicalTextureCoords;textureIndex++)
							        {triangleTextureCoords=getPrimitiveTextureCoords(meshData,trianglePrimitiveIndex,sectionIndex,textureIndex,triangleTextureCoords);		    
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
				  triangleVertices=meshData.getPrimitiveVertices(info.primitiveIndex,info.sectionIndex,triangleVertices);		  
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
			 //third step: retains only triangles by pairs which could be used to create rectangles
			 Vector3[] tri1Vertices=new Vector3[3];
			 Vector3[] tri2Vertices=new Vector3[3];
			 Vector2[] tri1TextureCoords=new Vector2[3];
			 Vector2[] tri2TextureCoords=new Vector2[3];
			 RightTriangleInfo tri1,tri2;
			 final boolean[][] canonicalTexCoordsFound=new boolean[2][2];
			 //for each plane of the map
			 for(Entry<Plane,ArrayList<RightTriangleInfo>> entry:mapOfTrianglesByPlanes.entrySet())
			     {ArrayList<RightTriangleInfo> rightTrianglesWithSameHypotenusesByPairs=new ArrayList<RightTriangleInfo>();
				  ArrayList<RightTriangleInfo> rightTriangles=entry.getValue();
				  final int triCount=rightTriangles.size();
				  //for each RightTriangleInfo instance
				  for(int triIndex1=0;triIndex1<triCount-1;triIndex1++)
				      {tri1=rightTriangles.get(triIndex1);
				       if(!rightTrianglesWithSameHypotenusesByPairs.contains(tri1))
				           {tri1Vertices=meshData.getPrimitiveVertices(tri1.primitiveIndex,tri1.sectionIndex,tri1Vertices);
					        for(int triIndex2=triIndex1+1;triIndex2<triCount;triIndex2++)
					            {tri2=rightTriangles.get(triIndex2);
					             if(!rightTrianglesWithSameHypotenusesByPairs.contains(tri2))
					                 {tri2Vertices=meshData.getPrimitiveVertices(tri2.primitiveIndex,tri2.sectionIndex,tri2Vertices);
						              /**
						               * checks if the both triangles have the same hypotenuse (i.e it is a quadrilateral with 2 right angles) 
						               * and if their opposite sides have the same length (i.e it is a parallelogram).
						               * As a parallelogram with 2 right angles is necessarily a rectangle, such a shape is a rectangle.
						               * */
						              boolean sameHypotenuseOppositeSidesOfSameLengthSameVertexOrder=tri1Vertices[tri1.sideIndexOfHypotenuse].equals(tri2Vertices[tri2.sideIndexOfHypotenuse])&&
							             tri1Vertices[(tri1.sideIndexOfHypotenuse+1)%3].equals(tri2Vertices[(tri2.sideIndexOfHypotenuse+1)%3])&&
							             tri1Vertices[tri1.sideIndexOfHypotenuse].distanceSquared(tri1Vertices[(tri1.sideIndexOfHypotenuse+2)%3])==
							             tri2Vertices[(tri2.sideIndexOfHypotenuse+1)%3].distanceSquared(tri2Vertices[(tri2.sideIndexOfHypotenuse+2)%3])&&
							             tri1Vertices[(tri1.sideIndexOfHypotenuse+1)%3].distanceSquared(tri1Vertices[(tri1.sideIndexOfHypotenuse+2)%3])==
							             tri2Vertices[tri2.sideIndexOfHypotenuse].distanceSquared(tri2Vertices[(tri2.sideIndexOfHypotenuse+2)%3]);
						              boolean sameHypotenuseOppositeSidesOfSameLengthReverseVertexOrder=!sameHypotenuseOppositeSidesOfSameLengthSameVertexOrder&&
						    		     (tri1Vertices[tri1.sideIndexOfHypotenuse].equals(tri2Vertices[(tri2.sideIndexOfHypotenuse+1)%3])&&
									      tri1Vertices[(tri1.sideIndexOfHypotenuse+1)%3].equals(tri2Vertices[tri2.sideIndexOfHypotenuse])&&
							              tri1Vertices[tri1.sideIndexOfHypotenuse].distanceSquared(tri1Vertices[(tri1.sideIndexOfHypotenuse+2)%3])==
							              tri2Vertices[tri2.sideIndexOfHypotenuse].distanceSquared(tri2Vertices[(tri2.sideIndexOfHypotenuse+2)%3])&&
							              tri1Vertices[(tri1.sideIndexOfHypotenuse+1)%3].distanceSquared(tri1Vertices[(tri1.sideIndexOfHypotenuse+2)%3])==
							              tri2Vertices[(tri2.sideIndexOfHypotenuse+1)%3].distanceSquared(tri2Vertices[(tri2.sideIndexOfHypotenuse+2)%3]));
						              //checks if their vertices at their right angles are different
						              boolean sameHypotenuseOppositeSidesOfSameLengthDifferentVerticesAtRightAngles=(sameHypotenuseOppositeSidesOfSameLengthSameVertexOrder||
						            		  sameHypotenuseOppositeSidesOfSameLengthReverseVertexOrder)&&
						    		     !tri1Vertices[(tri1.sideIndexOfHypotenuse+2)%3].equals(tri2Vertices[(tri2.sideIndexOfHypotenuse+2)%3]);
						              //N.B: triangles must have different vertex orders to have the same normal
						              if(sameHypotenuseOppositeSidesOfSameLengthDifferentVerticesAtRightAngles&&
						            	 sameHypotenuseOppositeSidesOfSameLengthReverseVertexOrder)
						                  {//checks the texture coordinates
						    	           boolean texCoordsMatch=true;
						    	           for(int textureIndex=0;meshData.getTextureBuffer(textureIndex)!=null&&texCoordsMatch;textureIndex++)
						                       {tri1TextureCoords=getPrimitiveTextureCoords(meshData,tri1.primitiveIndex,tri1.sectionIndex,textureIndex,tri1TextureCoords);
						                        tri2TextureCoords=getPrimitiveTextureCoords(meshData,tri2.primitiveIndex,tri2.sectionIndex,textureIndex,tri2TextureCoords);
						                        //checks if the vertices of the hypotenuse must have the same texture coordinates in both triangles
						                        if(sameHypotenuseOppositeSidesOfSameLengthSameVertexOrder)
						                            {texCoordsMatch=tri1TextureCoords[tri1.sideIndexOfHypotenuse].equals(tri2TextureCoords[tri2.sideIndexOfHypotenuse])&&
						                		                    tri1TextureCoords[(tri1.sideIndexOfHypotenuse+1)%3].equals(tri2TextureCoords[(tri2.sideIndexOfHypotenuse+1)%3]);
						                            }
						                        else
						                            {texCoordsMatch=tri1TextureCoords[tri1.sideIndexOfHypotenuse].equals(tri2TextureCoords[(tri2.sideIndexOfHypotenuse+1)%3])&&
		                		                                    tri1TextureCoords[(tri1.sideIndexOfHypotenuse+1)%3].equals(tri2TextureCoords[tri2.sideIndexOfHypotenuse]);						            	   
						                            }
						                        if(texCoordsMatch)
						                            {/**
						                              * checks if the rectangle contains all possible pairs of canonical texture coordinates
						                              * i.e [0;0], [0;1], [1;0] and [1;1]
						                              * */
						            	             //resets the array of flags
						            	             for(int abscissaIndex=0;abscissaIndex<canonicalTexCoordsFound.length;abscissaIndex++)
						            	                 Arrays.fill(canonicalTexCoordsFound[abscissaIndex],false);
						            	             //checks the texture coordinates of the vertices of the right angles
						            	             for(int abscissaIndex=0;abscissaIndex<canonicalTexCoordsFound.length;abscissaIndex++)
						            		             for(int ordinateIndex=0;ordinateIndex<canonicalTexCoordsFound[abscissaIndex].length;ordinateIndex++)
						            	                     if(tri1TextureCoords[(tri1.sideIndexOfHypotenuse+2)%3].getX()==abscissaIndex&&
						            	        	            tri1TextureCoords[(tri1.sideIndexOfHypotenuse+2)%3].getY()==ordinateIndex)
						            	        	             canonicalTexCoordsFound[abscissaIndex][ordinateIndex]=true;
						            	             for(int abscissaIndex=0;abscissaIndex<canonicalTexCoordsFound.length;abscissaIndex++)
						            		             for(int ordinateIndex=0;ordinateIndex<canonicalTexCoordsFound[abscissaIndex].length;ordinateIndex++)
						            	                     if(tri2TextureCoords[(tri2.sideIndexOfHypotenuse+2)%3].getX()==abscissaIndex&&
						            	        	            tri2TextureCoords[(tri2.sideIndexOfHypotenuse+2)%3].getY()==ordinateIndex)
						            	        	             canonicalTexCoordsFound[abscissaIndex][ordinateIndex]=true;
						            	             /**
						            	              * checks the texture coordinates of the vertices of the hypotenuse. Looking at the first
						            	              * triangle is enough as I have already tested that the vertices of the hypotenuse in both 
						            	              * triangles have the same texture coordinates.
						            	              * */
						            	             for(int abscissaIndex=0;abscissaIndex<canonicalTexCoordsFound.length;abscissaIndex++)
						            		             for(int ordinateIndex=0;ordinateIndex<canonicalTexCoordsFound[abscissaIndex].length;ordinateIndex++)
						            	                     if(tri1TextureCoords[tri1.sideIndexOfHypotenuse].getX()==abscissaIndex&&
						            	        	            tri1TextureCoords[tri1.sideIndexOfHypotenuse].getY()==ordinateIndex)
						            	        	             canonicalTexCoordsFound[abscissaIndex][ordinateIndex]=true;
						            	             for(int abscissaIndex=0;abscissaIndex<canonicalTexCoordsFound.length;abscissaIndex++)
						            		             for(int ordinateIndex=0;ordinateIndex<canonicalTexCoordsFound[abscissaIndex].length;ordinateIndex++)
						            	                     if(tri1TextureCoords[(tri1.sideIndexOfHypotenuse+1)%3].getX()==abscissaIndex&&
						            	        	            tri1TextureCoords[(tri1.sideIndexOfHypotenuse+1)%3].getY()==ordinateIndex)
						            	        	             canonicalTexCoordsFound[abscissaIndex][ordinateIndex]=true;
						            	             //checks all possible pairs of canonical texture coordinates have been found
						            	             for(int abscissaIndex=0;abscissaIndex<canonicalTexCoordsFound.length&&texCoordsMatch;abscissaIndex++)
						            		             for(int ordinateIndex=0;ordinateIndex<canonicalTexCoordsFound[abscissaIndex].length&&texCoordsMatch;ordinateIndex++)
						            			             if(!canonicalTexCoordsFound[abscissaIndex][ordinateIndex])
						            				             texCoordsMatch=false;
						                            }
						                       }
						    	           if(texCoordsMatch)
						    	               {rightTrianglesWithSameHypotenusesByPairs.add(tri1);
						                        rightTrianglesWithSameHypotenusesByPairs.add(tri2);
						                        break;
						    	               }						    	           
						                  }
					                 }
					            }
				           }					   
				      }
				  rightTriangles.clear();
				  rightTriangles.addAll(rightTrianglesWithSameHypotenusesByPairs);
			     }
			 //fourth step: creates lists containing all adjacent rectangles in the same planes
			 HashMap<Plane,ArrayList<ArrayList<RightTriangleInfo>>> mapOfListsOfTrianglesByPlanes=new HashMap<Plane, ArrayList<ArrayList<RightTriangleInfo>>>();
			 HashMap<RightTriangleInfo,ArrayList<Entry<RightTriangleInfo[],int[]>>> commonSidesInfosMap=new HashMap<RightTriangleInfo,ArrayList<Entry<RightTriangleInfo[],int[]>>>();
			 Vector3[] tri3Vertices=new Vector3[3];
			 Vector3[] tri4Vertices=new Vector3[3];
			 RightTriangleInfo tri3,tri4;
			 Vector3[][] trisVertices=new Vector3[][]{tri1Vertices,tri2Vertices,tri3Vertices,tri4Vertices};
			 RightTriangleInfo[] tris=new RightTriangleInfo[trisVertices.length];
			 Vector2[][] trisTextureCoords=new Vector2[trisVertices.length][];
			 //for each plane of the map
			 for(Entry<Plane,ArrayList<RightTriangleInfo>> entry:mapOfTrianglesByPlanes.entrySet())
			     {ArrayList<RightTriangleInfo> rightTrianglesByPairs=entry.getValue();
			      Plane plane=entry.getKey();
			      final int triCount=rightTrianglesByPairs.size();
			      for(int triIndex12=0;triIndex12<triCount-3;triIndex12+=2)
			          {tri1=rightTrianglesByPairs.get(triIndex12);
			           tri2=rightTrianglesByPairs.get(triIndex12+1);
			           ArrayList<ArrayList<RightTriangleInfo>> listOfListsOfTris=mapOfListsOfTrianglesByPlanes.get(plane);
			           ArrayList<RightTriangleInfo> listOfTris=null;
			           //if the list of lists for this plane exists
			           if(listOfListsOfTris!=null)
			               {//checks if tri1 and tri2 are already in a list
			        	    for(ArrayList<RightTriangleInfo> list:listOfListsOfTris)
		    	    	        //only looks for tri1 as tri1 and tri2 should be together
			            	    if(list.contains(tri1))
	    	    	                {listOfTris=list;
	    	    	                 break;
	    	    	                }			        	    
			               }
			           tri1Vertices=meshData.getPrimitiveVertices(tri1.primitiveIndex,tri1.sectionIndex,tri1Vertices);
			           tri2Vertices=meshData.getPrimitiveVertices(tri2.primitiveIndex,tri2.sectionIndex,tri2Vertices);
			           for(int triIndex34=triIndex12+2;triIndex34<triCount-1;triIndex34+=2)
			               {tri3=rightTrianglesByPairs.get(triIndex34);
				            tri4=rightTrianglesByPairs.get(triIndex34+1);				            
				            tri3Vertices=meshData.getPrimitiveVertices(tri3.primitiveIndex,tri3.sectionIndex,tri3Vertices);
				            tri4Vertices=meshData.getPrimitiveVertices(tri4.primitiveIndex,tri4.sectionIndex,tri4Vertices);
				            boolean oneCommonSideCorrectVertexOrder2oppositeSidesOfSameLengthAndSameTextureCoordinatesFound=false;				            
				            boolean oneCommonSideCorrectVertexOrder2oppositeSidesOfSameLength=false;
				            boolean oneCommonSideCorrectVertexOrder=false;
				            boolean oneCommonSide=false;
				            boolean oneCommonVertex=false;
				            /**
				             * checks if both rectangles have exactly one common side, i.e if one vertex is common to 2 triangles 
				             * from 2 different rectangles but not on any hypotenuse and if another vertex is common to 2 triangles
				             * from 2 different rectangles but on the both hypotenuse.
				             * Then, it checks if the vertex order of the rectangles is the same
				             * After that, it checks if the orthogonal sides adjacent with this common side have the same length.
				             * Finally, it checks if both rectangles have the same texture coordinates.
				             * */
				            tris[0]=tri1;
				            tris[1]=tri2;
				            tris[2]=tri3;
				            tris[3]=tri4;
				            for(int i=0,ti0,ti1,ti2,ti3;i<4&&!oneCommonSideCorrectVertexOrder2oppositeSidesOfSameLength;i++)
				                {Entry<RightTriangleInfo[],int[]> commonSideInfo=null;
				            	 //{0;1}
				            	 ti0=i/2;
				            	 Vector3[] tv0=trisVertices[ti0];
				            	 RightTriangleInfo tr0=tris[ti0];
				            	 //{2;3}
				                 ti1=2+(i%2);				                 
				                 Vector3[] tv1=trisVertices[ti1];
				                 RightTriangleInfo tr1=tris[ti1];
				                 //{1;0}
				                 ti2=((i/2)+1)%2;
				                 RightTriangleInfo tr2=tris[ti2];
				                 //{3;2}
				                 ti3=2+((i+1)%2);
				                 RightTriangleInfo tr3=tris[ti3];
				                 //checks if both rectangles have exactly one common side
				                 for(int j=0;j<3&&!oneCommonVertex;j++)
				                     if(tv0[(tr0.sideIndexOfHypotenuse+2)%3].equals(tv1[(tr1.sideIndexOfHypotenuse+j)%3]))
				                         {oneCommonVertex=true;
				                          if(j!=2)
				                              {for(int k=0;k<2&&!oneCommonSide;k++)
				                        	       if(tv0[(tr0.sideIndexOfHypotenuse+k)%3].equals(tv1[(tr1.sideIndexOfHypotenuse+2)%3]))
				                                       {oneCommonSide=true;
				                                        //checks if the vertex order is correct
				                                        oneCommonSideCorrectVertexOrder=j!=k;
				                                        if(oneCommonSideCorrectVertexOrder)
				                                            {//checks if the orthogonal sides adjacent with this common side have the same length
					                                         if(tv0[(tr0.sideIndexOfHypotenuse+((k+1)%2))%3].distanceSquared(tv0[(tr0.sideIndexOfHypotenuse+2)%3])==
									                            tv1[(tr1.sideIndexOfHypotenuse+((j+1)%2))%3].distanceSquared(tv1[(tr1.sideIndexOfHypotenuse+2)%3]))
							                        	         {oneCommonSideCorrectVertexOrder2oppositeSidesOfSameLength=true;	                        	          
							                        	          //checks the texture coordinates
									            				  boolean texCoordsMatch=true;				            				   
									            				  //for each texture unit
									            				  for(int textureIndex=0;meshData.getTextureBuffer(textureIndex)!=null&&texCoordsMatch;textureIndex++)
									            				      {//gets all texture coordinates
									            					   for(int l=0;l<4;l++)
									     				            	   trisTextureCoords[l]=getPrimitiveTextureCoords(meshData,tris[l].primitiveIndex,tris[l].sectionIndex,textureIndex,trisTextureCoords[l]);
									            					   //checks if both rectangles have the same texture coordinates
									            					   texCoordsMatch&=trisTextureCoords[ti0][(tr0.sideIndexOfHypotenuse+2)%3].equals(trisTextureCoords[ti3][(tr3.sideIndexOfHypotenuse+2)%3]);
									            					   texCoordsMatch&=trisTextureCoords[ti1][(tr1.sideIndexOfHypotenuse+2)%3].equals(trisTextureCoords[ti2][(tr2.sideIndexOfHypotenuse+2)%3]);
									            					   texCoordsMatch&=trisTextureCoords[ti0][(tr0.sideIndexOfHypotenuse+((k+1)%2))%3].equals(trisTextureCoords[ti1][(tr1.sideIndexOfHypotenuse+j)%3]);
									            					   texCoordsMatch&=trisTextureCoords[ti1][(tr1.sideIndexOfHypotenuse+((j+1)%2))%3].equals(trisTextureCoords[ti0][(tr0.sideIndexOfHypotenuse+k)%3]);
									            				      }
									            				  oneCommonSideCorrectVertexOrder2oppositeSidesOfSameLengthAndSameTextureCoordinatesFound=texCoordsMatch;
									            				  if(oneCommonSideCorrectVertexOrder2oppositeSidesOfSameLengthAndSameTextureCoordinatesFound)
								                                      {//stores tr0, tr1, tr2, tr3 and the indices for further uses
									            					   commonSideInfo=new SimpleEntry<RightTriangleInfo[],int[]>(new RightTriangleInfo[]{tr0,tr1,tr0,tr1},
									            							   new int[]{(tr0.sideIndexOfHypotenuse+2)%3,
									            							   (tr1.sideIndexOfHypotenuse+j)%3,
									            							   (tr0.sideIndexOfHypotenuse+k)%3,
									            							   (tr1.sideIndexOfHypotenuse+2)%3});
									            					   ArrayList<Entry<RightTriangleInfo[],int[]>> commonSidesInfosEntriesList=commonSidesInfosMap.get(tr0);
									            					   if(commonSidesInfosEntriesList==null)
									            						   {commonSidesInfosEntriesList=new ArrayList<Entry<RightTriangleInfo[],int[]>>();
									            						    commonSidesInfosMap.put(tr0,commonSidesInfosEntriesList);
									            						   }
									            					   commonSidesInfosEntriesList.add(commonSideInfo);
									            					   commonSidesInfosEntriesList=commonSidesInfosMap.get(tr1);
									            					   if(commonSidesInfosEntriesList==null)
									            						   {commonSidesInfosEntriesList=new ArrayList<Entry<RightTriangleInfo[],int[]>>();
									            						    commonSidesInfosMap.put(tr1,commonSidesInfosEntriesList);
									            						   }
									            					   commonSidesInfosEntriesList.add(commonSideInfo);
								                                      }
							                        	         }				                                        	 
				                                            }				                                        
				                                       }
				                              }
				                          else
				                              {for(int k=0;k<4&&!oneCommonSide;k++)
				                        	       if(tv0[(tr0.sideIndexOfHypotenuse+(k/2))%3].equals(tv1[(tr1.sideIndexOfHypotenuse+(k%2))%3]))
				                        	           {oneCommonSide=true;
				                        	            //checks if the vertex order is correct
				                        	            oneCommonSideCorrectVertexOrder=(k/2)!=(k%2);
				                        	            if(oneCommonSideCorrectVertexOrder)
				                        	                {//checks if the orthogonal sides adjacent with this common side have the same length
					                                         if(tv0[(tr0.sideIndexOfHypotenuse+(((k/2)+1)%2))%3].distanceSquared(tv0[(tr0.sideIndexOfHypotenuse+2)%3])==
							                        	        tv1[(tr1.sideIndexOfHypotenuse+((k+1)%2))%3].distanceSquared(tv1[(tr1.sideIndexOfHypotenuse+2)%3]))
							                        	         {oneCommonSideCorrectVertexOrder2oppositeSidesOfSameLength=true;
							                        	          //checks the texture coordinates
									            				  boolean texCoordsMatch=true;				            				   
									            				  //for each texture unit
									            				  for(int textureIndex=0;meshData.getTextureBuffer(textureIndex)!=null&&texCoordsMatch;textureIndex++)
									            				      {//gets all texture coordinates
									            					   for(int l=0;l<4;l++)
									     				            	   trisTextureCoords[l]=getPrimitiveTextureCoords(meshData,tris[l].primitiveIndex,tris[l].sectionIndex,textureIndex,trisTextureCoords[l]);
									            					   //checks if both rectangles have the same texture coordinates
									            					   texCoordsMatch&=trisTextureCoords[ti0][(tr0.sideIndexOfHypotenuse+(((k/2)+1)%2))%3].equals(trisTextureCoords[ti1][(tr1.sideIndexOfHypotenuse+2)%3]);
									            					   texCoordsMatch&=trisTextureCoords[ti1][(tr1.sideIndexOfHypotenuse+((k+1)%2))%3].equals(trisTextureCoords[ti0][(tr0.sideIndexOfHypotenuse+2)%3]);
									            					   texCoordsMatch&=trisTextureCoords[ti1][(tr1.sideIndexOfHypotenuse+(k%2))%3].equals(trisTextureCoords[ti2][(tr2.sideIndexOfHypotenuse+2)%3]);
									            					   texCoordsMatch&=trisTextureCoords[ti0][(tr0.sideIndexOfHypotenuse+(k/2))%3].equals(trisTextureCoords[ti3][(tr3.sideIndexOfHypotenuse+2)%3]);
									            				      }
									            				  oneCommonSideCorrectVertexOrder2oppositeSidesOfSameLengthAndSameTextureCoordinatesFound=texCoordsMatch;
									            				  if(oneCommonSideCorrectVertexOrder2oppositeSidesOfSameLengthAndSameTextureCoordinatesFound)
									                                  {//stores tr0, tr1, tr2, tr3 and the indices for further uses
									            					   commonSideInfo=new SimpleEntry<RightTriangleInfo[],int[]>(new RightTriangleInfo[]{tr0,tr1,tr0,tr1},
									            							   new int[]{(tr0.sideIndexOfHypotenuse+2)%3,
									            							   (tr1.sideIndexOfHypotenuse+j)%3,
									            							   (tr0.sideIndexOfHypotenuse+(k/2))%3,
									            							   (tr1.sideIndexOfHypotenuse+(k%2))%3});
									            					   ArrayList<Entry<RightTriangleInfo[],int[]>> commonSidesInfosEntriesList=commonSidesInfosMap.get(tr0);
									            					   if(commonSidesInfosEntriesList==null)
									            						   {commonSidesInfosEntriesList=new ArrayList<Entry<RightTriangleInfo[],int[]>>();
									            						    commonSidesInfosMap.put(tr0,commonSidesInfosEntriesList);
									            						   }
									            					   commonSidesInfosEntriesList.add(commonSideInfo);
									            					   commonSidesInfosEntriesList=commonSidesInfosMap.get(tr1);
									            					   if(commonSidesInfosEntriesList==null)
									            						   {commonSidesInfosEntriesList=new ArrayList<Entry<RightTriangleInfo[],int[]>>();
									            						    commonSidesInfosMap.put(tr1,commonSidesInfosEntriesList);
									            						   }
									            					   commonSidesInfosEntriesList.add(commonSideInfo);
									                                  }
							                        	         }				                        	            	 
				                        	                }				                        	            
				                        	           }
				                              }
				                         }
			                    }
				            if(oneCommonSideCorrectVertexOrder2oppositeSidesOfSameLengthAndSameTextureCoordinatesFound)
			        	        {ArrayList<RightTriangleInfo> previousListOfTris=null;
				                 //if the list of lists for this plane does not exist
						    	 if(listOfListsOfTris==null)
						    	     {//creates it and puts it into the map
						    	      listOfListsOfTris=new ArrayList<ArrayList<RightTriangleInfo>>();
						    	      mapOfListsOfTrianglesByPlanes.put(plane,listOfListsOfTris);
						    	     }
						    	 else
						    	     {//checks if tri3 and tri4 are already in a list
						    	      for(ArrayList<RightTriangleInfo> list:listOfListsOfTris)
						    	          //only looks for tri3 as tri3 and tri4 should be together
						    	          if(list.contains(tri3))
					    	    	          {previousListOfTris=list;
					    	    	           break;
					    	    	          }
						    	     }
						    	 //if the new list of triangles has not been created
						         if(listOfTris==null)
						    	     {//creates it, fills it with the 4 triangles and adds it into the list of lists
						    	      listOfTris=new ArrayList<RightTriangleInfo>();
						    	      listOfTris.add(tri1);			        	    	 
						    	      listOfTris.add(tri2);
						    	      listOfTris.add(tri3);
								      listOfTris.add(tri4);
						    	      listOfListsOfTris.add(listOfTris);
						    	     }
						    	 else
						    	     {//if tri3 and tri4 are not already in this list, adds them into it
						    	      if(previousListOfTris!=null&&previousListOfTris!=listOfTris)
						    	          {listOfTris.add(tri3);
									       listOfTris.add(tri4);
						    	          }
						    	     }
						    	 //if tri3 and tri4 are already in another list
						    	 if(previousListOfTris!=null)
						    	     {/**
						    	       * removes all elements already added into the new list from the previous list
						    	       * to keep only elements which are not in the new list
						    	       * */
						    	      previousListOfTris.removeAll(listOfTris);
						    	      //adds all elements which are not in the new list into it
						    	      listOfTris.addAll(previousListOfTris);
						    	     }
						    	}			        	        
			               }
			          }
			     }
			 //fifth step: creates lists of adjacent rectangles in the same planes usable to make bigger rectangles
			 /**
			  * Each entry handles the triangles of a plane. Each entry contains several lists of groups of adjacent triangles.
			  * Each group of adjacent triangles is a list of arrays of adjacent triangles which could be merged to make bigger 
			  * rectangles
			  * */
			 HashMap<Plane,ArrayList<ArrayList<RightTriangleInfo[][][]>>> mapOfListsOfListsOfArraysOfMergeableTris=new HashMap<Plane,ArrayList<ArrayList<RightTriangleInfo[][][]>>>();
			 //for each plane
			 for(Entry<Plane,ArrayList<ArrayList<RightTriangleInfo>>> entry:mapOfListsOfTrianglesByPlanes.entrySet())
			     {Plane plane=entry.getKey();
				  //for each list of adjacent triangles
				  for(ArrayList<RightTriangleInfo> trisList:entry.getValue())
					  if(!trisList.isEmpty())
			              {//builds the 2D array from the list of triangles
						   final RightTriangleInfo[][][] adjacentTrisArray=compute2dTrisArrayFromAdjacentTrisList(trisList,commonSidesInfosMap,meshData);
				           //computes a list of arrays of adjacent triangles which could be merged to make bigger rectangles
				           final ArrayList<RightTriangleInfo[][][]> adjacentTrisArraysList=computeAdjacentMergeableTrisArraysList(adjacentTrisArray);
				           //puts the new list into the map
				           ArrayList<ArrayList<RightTriangleInfo[][][]>> adjacentTrisArraysListsList=mapOfListsOfListsOfArraysOfMergeableTris.get(plane);
				           if(adjacentTrisArraysListsList==null)
				               {adjacentTrisArraysListsList=new ArrayList<ArrayList<RightTriangleInfo[][][]>>();
				                mapOfListsOfListsOfArraysOfMergeableTris.put(plane,adjacentTrisArraysListsList);
				               }
				           adjacentTrisArraysListsList.add(adjacentTrisArraysList);
			              }		  
			     }
			 //TODO: sixth step: create these bigger rectangles (update their texture coordinates (use coordinates greater 
			 //than 1) in order to use texture repeat)
			 //TODO: seventh step: remove the triangles which are no more in the geometry of the mesh
			 //TODO: eighth step: add the new triangles into the geometry of the mesh
		    }
		return result;
	}
	
	/**
	 * Computes a 2D array of adjacent triangles in the same plane by using 
	 * their relative location in this plane
	 * 
	 * @param trisList list of adjacent triangles
	 * @param meshData mesh data
	 * @return
	 */
	private static RightTriangleInfo[][][] compute2dTrisArrayFromAdjacentTrisList(
			final ArrayList<RightTriangleInfo> trisList,
			final HashMap<RightTriangleInfo,ArrayList<Entry<RightTriangleInfo[],int[]>>> commonSidesInfosMap,
			final MeshData meshData){
		/**
		 * computes an overestimated size to be sure not to use an index out of 
		 * the bounds, uses the list size as all pairs of triangles represent 
		 * quads and some room is needed in all directions
		 */
		final int overestimatedSize=trisList.size();
		//creates the 2D array
		final RightTriangleInfo[][][] adjacentTrisArray=new RightTriangleInfo[overestimatedSize][overestimatedSize][];
		//if this array can contain something
		if(overestimatedSize>0)
		    {/**
		      * this initial index ensures there is enough room in all directions for 
		      * other triangles
		      */
			 final int initialIndex=(overestimatedSize/2)-1;
			 adjacentTrisArray[initialIndex][initialIndex]=new RightTriangleInfo[]{trisList.get(0),trisList.get(1)};
			 /**
			  * uses the following convention: 0 -> left, 1 
			  * -> top, 2 -> right, 3 -> bottom. Checks whether an 
			  * edge of the pair of triangles is equal to an edge 
			  * of adjacentTrisArray[i][j]
			  */
			 final HashMap<RightTriangleInfo,int[]> arrayMap=new HashMap<RightTriangleInfo,int[]>();
			 arrayMap.put(trisList.get(0),new int[]{initialIndex,initialIndex});
			 arrayMap.put(trisList.get(1),new int[]{initialIndex,initialIndex});
			 final ArrayList<Entry<RightTriangleInfo[],int[]>> infosQueue=new ArrayList<Entry<RightTriangleInfo[],int[]>>();
			 /**
			  * reuses the information stored in the previous step, copies them 
			  * into a list
			  */
			 for(ArrayList<Entry<RightTriangleInfo[], int[]>> commonSidesInfos:commonSidesInfosMap.values())
				 infosQueue.addAll(commonSidesInfos);
			 int infosQueueIndex=0;
			 //loops while this list is not empty
			 while(!infosQueue.isEmpty())
			     {boolean inserted=false;
			      //gets the information from the list
			      final Entry<RightTriangleInfo[],int[]> info=infosQueue.get(infosQueueIndex);
			      final RightTriangleInfo[] tris=info.getKey();
			      final int[] commonSidesIndices=info.getValue();
			      //if the array already contains the first triangle
				  if(arrayMap.containsKey(tris[0]))
					  {//if the array already contains the second triangle
					   if(arrayMap.containsKey(tris[1]))
						   inserted=true;
					   else
					       {//retrieves the indices of the triangle in the 2D array
						    final int[] arrayIndices=new int[]{arrayMap.get(tris[0])[0],arrayMap.get(tris[0])[1]};
						    //finds which sides are common updates the array
					        final int tri1index=trisList.indexOf(tris[1]);
					        if(tri1index%2==0)
					            {if(commonSidesIndices[2]==(tris[0].sideIndexOfHypotenuse+1)%3)
			                         {//to right
				            	      arrayIndices[0]++;
			                         }
			                     else
			                         {//to bottom
			                	      arrayIndices[1]++;
			                         }
					        	 adjacentTrisArray[arrayIndices[0]][arrayIndices[1]]=new RightTriangleInfo[]{tris[1],trisList.get(tri1index+1)};
					            }
					        else
					            {if(commonSidesIndices[2]==(tris[0].sideIndexOfHypotenuse+1)%3)
				                     {//to left
				            	      arrayIndices[0]--;
				                     }
				                 else
				                     {//to top
				            	      arrayIndices[1]--;
				                     }
					        	 adjacentTrisArray[arrayIndices[0]][arrayIndices[1]]=new RightTriangleInfo[]{trisList.get(tri1index-1),tris[1]};
					            }
						    //updates the map as tris[1] has been found
						    arrayMap.put(tris[1],arrayIndices);
						    inserted=true;
					       }
					  }
				  else
				      {//if the array already contains the second triangle
					   if(arrayMap.containsKey(tris[1]))
			               {//retrieves the indices of the triangle in the 2D array
						    final int[] arrayIndices=arrayMap.get(tris[1]);
				    	    //finds which sides are common and updates the array
			                final int tri0index=trisList.indexOf(tris[0]);
					        if(tri0index%2==0)
					            {if(commonSidesIndices[2]==(tris[0].sideIndexOfHypotenuse+1)%3)
					                 {//to left
					            	  arrayIndices[0]--;
					                 }
					             else
					                 {//to top
					            	  arrayIndices[1]--;
					                 }
					        	 adjacentTrisArray[arrayIndices[0]][arrayIndices[1]]=new RightTriangleInfo[]{tris[0],trisList.get(tri0index+1)};
					            }
					        else
					            {if(commonSidesIndices[2]==(tris[0].sideIndexOfHypotenuse+1)%3)
				                     {//to right
					            	  arrayIndices[0]++;
				                     }
				                 else
				                     {//to bottom
				                	  arrayIndices[1]++;
				                     }
					        	 adjacentTrisArray[arrayIndices[0]][arrayIndices[1]]=new RightTriangleInfo[]{trisList.get(tri0index-1),tris[0]};
					            }
				    	    //updates the map as tris[0] has been found
				    	    arrayMap.put(tris[0],arrayIndices);
				    	    inserted=true;
			               }
			           else
			        	   inserted=false;
			          }
			      if(inserted)
			    	  {//removes the information we used
			    	   infosQueue.remove(infosQueueIndex);
			    	   //resets the index if it is out of the bounds
			    	   if(infosQueueIndex==infosQueue.size())
			    		   infosQueueIndex=0;
			    	  }
			      else
			    	  {//uses the next index, does not go out of the bounds
			    	   infosQueueIndex=(infosQueueIndex+1)%infosQueue.size();
			    	  }
			     }
		    }
		return(adjacentTrisArray);
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
	
	private static void testComputeAdjacentMergeableTrisArraysList(){
		final RightTriangleInfo info=new RightTriangleInfo(0,0,0);
		final RightTriangleInfo[][][] adjacentTrisArray=new RightTriangleInfo[][][]{new RightTriangleInfo[][]{null,null,null,null,null,null,null,null},
				                                                                    new RightTriangleInfo[][]{null,null,null,new RightTriangleInfo[]{info,info},null,null,null,null},
				                                                                    new RightTriangleInfo[][]{null,new RightTriangleInfo[]{info,info},new RightTriangleInfo[]{info,info},new RightTriangleInfo[]{info,info},new RightTriangleInfo[]{info,info},new RightTriangleInfo[]{info,info},new RightTriangleInfo[]{info,info},new RightTriangleInfo[]{info,info}},
				                                                                    new RightTriangleInfo[][]{null,null,new RightTriangleInfo[]{info,info},new RightTriangleInfo[]{info,info},new RightTriangleInfo[]{info,info},new RightTriangleInfo[]{info,info},null,null},
				                                                                    new RightTriangleInfo[][]{null,new RightTriangleInfo[]{info,info},new RightTriangleInfo[]{info,info},new RightTriangleInfo[]{info,info},new RightTriangleInfo[]{info,info},null,null,null},
				                                                                    new RightTriangleInfo[][]{null,new RightTriangleInfo[]{info,info},new RightTriangleInfo[]{info,info},new RightTriangleInfo[]{info,info},new RightTriangleInfo[]{info,info},null,null,null},
				                                                                    new RightTriangleInfo[][]{null,null,new RightTriangleInfo[]{info,info},null,null,null,null,null},
				                                                                    new RightTriangleInfo[][]{null,null,null,null,null,null,null,null}};
		System.out.println("Input:");
		//for each row
		for(int i=0;i<adjacentTrisArray.length;i++)
			{//for each column
			 for(int j=0;j<adjacentTrisArray[i].length;j++)
				 if(adjacentTrisArray[i][j]!=null&&adjacentTrisArray[i][j][0]!=null&&adjacentTrisArray[i][j][1]!=null)
		             System.out.print("[X]");
				 else
					 System.out.print("[ ]");
			 System.out.println("");
			}
		System.out.println("");
		ArrayList<RightTriangleInfo[][][]> adjacentTrisArraysList=computeAdjacentMergeableTrisArraysList(adjacentTrisArray);
		System.out.println("Output:");
		for(RightTriangleInfo[][][] resultingAdjacentTrisArray:adjacentTrisArraysList)
			{for(int i=0;i<resultingAdjacentTrisArray.length;i++)
			     {for(int j=0;j<resultingAdjacentTrisArray[i].length;j++)
				      if(resultingAdjacentTrisArray[i][j]!=null&&resultingAdjacentTrisArray[i][j][0]!=null&&resultingAdjacentTrisArray[i][j][1]!=null)
		                  System.out.print("[X]");
				      else
					      System.out.print("[ ]");
			      System.out.println("");
			     }
			 System.out.println("");
			}
	}
	
	public static final void main(String[] args){
		testComputeAdjacentMergeableTrisArraysList();
	}
	
	/**
	 * Computes a list of arrays of adjacent triangles which could be merged to 
	 * make bigger rectangles
	 * 
	 * @param adjacentTrisArray 2D arrays containing adjacent triangles
	 * @return list of 2D arrays of adjacent mergeable triangles
	 */
	private static ArrayList<RightTriangleInfo[][][]> computeAdjacentMergeableTrisArraysList(final RightTriangleInfo[][][] adjacentTrisArray){
		return(computeFullArraysFromNonFullArray(adjacentTrisArray));
	}
	
	/**
	 * Creates a list of full arrays from a potentially non full array
	 * 
	 * @param array potentially non full array
	 * @return list of full arrays
	 */
	public static <T> ArrayList<T[][]> computeFullArraysFromNonFullArray(final T[][] array){
		//detects empty rows and empty columns in order to skip them later
		int smallestI=Integer.MAX_VALUE;
		int biggestI=Integer.MIN_VALUE;
		int smallestJ=Integer.MAX_VALUE;
		int biggestJ=Integer.MIN_VALUE;
		for(int i=0;i<array.length;i++)
			for(int j=0;j<array[i].length;j++)
				if(array[i][j]!=null)
			        {smallestI=Math.min(smallestI,i);
			         biggestI=Math.max(biggestI,i);
			         smallestJ=Math.min(smallestJ,j);
			         biggestJ=Math.max(biggestJ,j);
			        }
		//N.B: row-major convention
		final int rowCount=biggestI>=smallestI?biggestI-smallestI+1:0;//this is equal to the "length" of the array
		final int columnCount=biggestJ>=smallestJ?biggestJ-smallestJ+1:0;
		final ArrayList<T[][]> adjacentTrisArraysList=new ArrayList<T[][]>();
		//if the array is not empty
		if(rowCount>0&&columnCount>0)
		    {//creates an occupancy map of the supplied array but without empty columns and rows
			 final boolean[][] useFlagsArray=new boolean[rowCount][];
			 //for each row
		     for(int i=0;i<useFlagsArray.length;i++)
			     {//computes the index in the original array by using the offset
		    	  final int rawI=i+smallestI;
		    	  //starts the computation of the biggest index of the current column
			      int localBiggestJ=Integer.MIN_VALUE;
			      for(int j=0;j<columnCount;j++)
			          {//computes the index in the original array by using the offset
			    	   final int rawJ=j+smallestJ;
			           if(array[rawI][rawJ]!=null)
			        	   localBiggestJ=rawJ;
			          }
			      //allocates the current column of the occupancy map as tightly as possible
			      useFlagsArray[i]=new boolean[localBiggestJ>=smallestJ?localBiggestJ-smallestJ+1:0];
			      //fills the occupancy map (true <-> not null)
		    	  for(int j=0;j<useFlagsArray[i].length;j++)
				      {final int rawJ=j+smallestJ;
					   useFlagsArray[i][j]=array[rawI][rawJ]!=null;
				      }
			     }
		     /**
		      * As Java is unable to create a generic array by directly using the generic type, 
		      * it is necessary to retrieve it thanks to the reflection
		      */
		     final Class<?> arrayComponentType=array.getClass().getComponentType().getComponentType();
		     //finds the isolated sets of adjacent triangles that could be used to create quads
		     //the secondary size is the least important size of the chunk
		     for(int secondarySize=1;secondarySize<=Math.max(rowCount,columnCount);secondarySize++)
		    	 //the primary size is the most important size of the chunk
		    	 for(int primarySize=1;primarySize<=Math.max(rowCount,columnCount);primarySize++)
		             {//for each row
		    		  for(int i=0;i<useFlagsArray.length;i++)
		    		      //for each column
		    			  for(int j=0;j<useFlagsArray[i].length;j++)
		    		          {//if this element is occupied
		    				   if(useFlagsArray[i][j])
		    		               {//looks for an isolated element
		    		    	        //horizontal checks (rows)
		    		    	        if(primarySize+i<=rowCount&&secondarySize<=columnCount&&
		    		    	           areTrianglesLocallyIsolated(useFlagsArray,rowCount,columnCount,i,j,primarySize,secondarySize,true)&&
		    		    		      (j-1<0||!areTrianglesLocallyIsolated(useFlagsArray,rowCount,columnCount,i,j-1,primarySize,1,true))&&
		    		    		      (j+secondarySize>=columnCount||!areTrianglesLocallyIsolated(useFlagsArray,rowCount,columnCount,i,j+secondarySize,primarySize,1,true)))
		    		    	            {@SuppressWarnings("unchecked")
										 final T[][] adjacentTrisSubArray=(T[][])Array.newInstance(arrayComponentType,primarySize,secondarySize);
		    		    	             //adds it into the returned list
		    		    	             adjacentTrisArraysList.add(adjacentTrisSubArray);
		    		    	             //copies the elements of the chunk into the sub-array and marks them as removed from the occupancy map
		    		    	             for(int ii=0;ii<primarySize;ii++)
		    		    		             for(int jj=0;jj<secondarySize;jj++)
		    		    		                 {adjacentTrisSubArray[ii][jj]=array[ii+i+smallestI][jj+j+smallestJ];
		    		    		                  useFlagsArray[ii+i][jj+j]=false;
		    		    		                 }
		    		    	            }
		    		    	        else
		    		    	            {//vertical checks (columns)
		    		    	             if(primarySize+j<=columnCount&&secondarySize<=rowCount&&
		    		    	                areTrianglesLocallyIsolated(useFlagsArray,rowCount,columnCount,i,j,primarySize,secondarySize,false)&&
		    		 			 	       (i-1<0||!areTrianglesLocallyIsolated(useFlagsArray,rowCount,columnCount,i-1,j,primarySize,1,false))&&
		    		 			 	       (i+secondarySize>=rowCount||!areTrianglesLocallyIsolated(useFlagsArray,rowCount,columnCount,i+secondarySize,j,primarySize,1,false)))
		    		    	                 {@SuppressWarnings("unchecked")
		    		    	            	  final T[][] adjacentTrisSubArray=(T[][])Array.newInstance(arrayComponentType,secondarySize,primarySize);
		   	 			                      //adds it into the returned list
		   	 			                      adjacentTrisArraysList.add(adjacentTrisSubArray);
		   	 			                      //copies the elements of the chunk into the sub-array and marks them as removed from the occupancy map
		   	 			                      for(int jj=0;jj<primarySize;jj++)
		   	 			                	      for(int ii=0;ii<secondarySize;ii++)
		   	 			                		      {adjacentTrisSubArray[ii][jj]=array[ii+i+smallestI][jj+j+smallestJ];
		   			                		           useFlagsArray[ii+i][jj+j]=false;
		   			                		          }
		    		    	                 }
		    		    	            }
		    		               }
		    		          }
		             }
		    }
		return(adjacentTrisArraysList);
	}
	
	private static boolean areTrianglesLocallyIsolated(final boolean[][] useFlagsArray,final int rowCount,final int columnCount,
			final int i,final int j,final int primarySize,final int secondarySize,
			final boolean testOnRowIsolationEnabled){
		boolean isolated;
        if(0<=i&&i<useFlagsArray.length&&0<=j&&j<useFlagsArray[i].length&&useFlagsArray[i][j])
            {if(testOnRowIsolationEnabled)
                 {isolated=true;
                  for(int ii=Math.max(0,i-1);ii<=i+primarySize&&ii<rowCount&&isolated;ii++)
                      for(int jj=Math.max(0,j);jj<j+secondarySize&&jj<columnCount&&isolated;jj++)
                	      if((((ii==i-1)||(ii==i+primarySize))&&(ii<useFlagsArray.length&&jj<useFlagsArray[ii].length&&useFlagsArray[ii][jj]))||((i-1<ii)&&(ii<i+primarySize)&&(ii>=useFlagsArray.length||jj>=useFlagsArray[ii].length||!useFlagsArray[ii][jj])))
                	    	  isolated=false;
                 }
             else
                 {isolated=true;
                  for(int ii=Math.max(0,i);ii<i+secondarySize&&ii<rowCount&&isolated;ii++)
                      for(int jj=Math.max(0,j-1);jj<=j+primarySize&&jj<columnCount&&isolated;jj++)
                		  if((((jj==j-1)||(jj==j+primarySize))&&(ii<useFlagsArray.length&&jj<useFlagsArray[ii].length&&useFlagsArray[ii][jj]))||((j-1<jj)&&(jj<j+primarySize)&&(ii>=useFlagsArray.length||jj>=useFlagsArray[ii].length||!useFlagsArray[ii][jj])))
                	    	  isolated=false;
                 }
            }
        else
        	isolated=false;
        return(isolated);
	}
}
