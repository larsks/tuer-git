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
import java.util.Arrays;
import java.util.Collection;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map.Entry;
import jfpsm.graph.DirectedConnectedComponentVisitor;
import jfpsm.graph.DirectedRootedKaryTree;
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
				                {//{0;1}
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
								                                      {//TODO store tr0, tr1, tr2, tr3, ti0, ti1, ti2, ti3 and indices for further uses
								                        	           
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
									                                  {//TODO store tr0, tr1, tr2, tr3, ti0, ti1, ti2, ti3 and indices for further uses
									                        	       
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
						    	      if(previousListOfTris!=listOfTris)
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
			              {/*//builds a quad tree from the list of triangles
				           final LocalQuadTree tree=buildQuaternaryTreeNodeFromTrianglesList(trisList);	   
				           //computes the maximum size of the 2D array of adjacent triangles
				           final int[] tree2dDimension=computeTree2dDimension(tree);
				           final int width=tree2dDimension[0];
				           final int height=tree2dDimension[1];
				           //creates the 2D array with the appropriate size
				           final RightTriangleInfo[][][] adjacentTrisArray=new RightTriangleInfo[width][height][2];
				           //fills this array
				           fill2dArrayFromQuadTree(tree2dDimension[2],tree2dDimension[4],adjacentTrisArray,tree);*/
						   //builds the 2D array from the list of triangles
						   final RightTriangleInfo[][][] adjacentTrisArray=compute2dTrisArrayFromAdjacentTrisList(trisList,meshData);
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
			final ArrayList<RightTriangleInfo> trisList,final MeshData meshData){
		/**
		 * computes an overestimated size to be sure not to use an index out of 
		 * the bounds, uses the list size as all pairs of triangles represent 
		 * quads and some room is needed in all directions
		 */
		final int overestimatedSize=trisList.size();
		//creates the 2D array
		final RightTriangleInfo[][][] adjacentTrisArray=new RightTriangleInfo[overestimatedSize][overestimatedSize][2];
		//if this array can contain something
		if(overestimatedSize>0)
		    {/**
		      * this initial index ensures there is enough room in all directions for 
		      * other triangles
		      */
			 final int initialIndex=(overestimatedSize/2)-1;
			 adjacentTrisArray[initialIndex][initialIndex][0]=trisList.get(0);
			 adjacentTrisArray[initialIndex][initialIndex][1]=trisList.get(1);
			 /**
			  * TODO: use the following convention: 0 -> left, 1 
			  * -> top, 2 -> right, 3 -> bottom. Check whether an 
			  * edge of the pair of triangles is equal to an edge 
			  * of adjacentTrisArray[i][j]
			  */
			 /**
			  * TODO reuse the information stored in the previous step, start 
			  * from the first pair, find it in the supplied information, find 
			  * which side is concerned, add it into the 2D array, mark the 
			  * piece of information and so on...
			  */
			 /*Vector3[] tri0Vertices=new Vector3[3];
			 Vector3[] tri1Vertices=new Vector3[3];
			 Vector3[] tri2Vertices=new Vector3[3];
			 Vector3[] tri3Vertices=new Vector3[3];
			 for(int trisIndex=2;trisIndex<overestimatedSize-1;trisIndex++)
			     {final RightTriangleInfo tri0=trisList.get(trisIndex);
			      final RightTriangleInfo tri1=trisList.get(trisIndex);
			      //gets the vertices of the both triangles
				  tri0Vertices=meshData.getPrimitiveVertices(tri0.primitiveIndex,tri0.sectionIndex,tri0Vertices);
				  tri1Vertices=meshData.getPrimitiveVertices(tri1.primitiveIndex,tri1.sectionIndex,tri1Vertices);
				  final Vector3[][] insertedSides=new Vector3[][]{{tri0Vertices[(tri0.sideIndexOfHypotenuse+1)%3],tri0Vertices[(tri0.sideIndexOfHypotenuse+2)%3]},
						                                          {tri0Vertices[(tri0.sideIndexOfHypotenuse+2)%3],tri0Vertices[(tri0.sideIndexOfHypotenuse+0)%3]},
						                                          {tri1Vertices[(tri1.sideIndexOfHypotenuse+1)%3],tri1Vertices[(tri1.sideIndexOfHypotenuse+2)%3]},
						                                          {tri1Vertices[(tri1.sideIndexOfHypotenuse+2)%3],tri1Vertices[(tri1.sideIndexOfHypotenuse+0)%3]}};
				  boolean trisPairAdded=false;
				  for(int j=0;j<overestimatedSize&&!trisPairAdded;j++)
					  for(int i=0;i<overestimatedSize&&!trisPairAdded;i++)
					      {final RightTriangleInfo tri2=adjacentTrisArray[i][j][0];
					       final RightTriangleInfo tri3=adjacentTrisArray[i][j][1];
					       if(tri2!=null&&tri3!=null)
						       {tri2Vertices=meshData.getPrimitiveVertices(tri2.primitiveIndex,tri2.sectionIndex,tri2Vertices);
						        tri3Vertices=meshData.getPrimitiveVertices(tri3.primitiveIndex,tri3.sectionIndex,tri3Vertices);
						        
						        final Vector3[][] testedSides=new Vector3[][]{{tri2Vertices[(tri2.sideIndexOfHypotenuse+1)%3],tri2Vertices[(tri2.sideIndexOfHypotenuse+2)%3]},
                                                                              {tri2Vertices[(tri2.sideIndexOfHypotenuse+2)%3],tri2Vertices[(tri2.sideIndexOfHypotenuse+0)%3]},
                                                                              {tri3Vertices[(tri3.sideIndexOfHypotenuse+1)%3],tri3Vertices[(tri3.sideIndexOfHypotenuse+2)%3]},
                                                                              {tri3Vertices[(tri3.sideIndexOfHypotenuse+2)%3],tri3Vertices[(tri3.sideIndexOfHypotenuse+0)%3]}};
						        
						       }
					      }
			     }*/
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
	
	private enum QuadTreeElementOrientation{
		LEFT,
		RIGHT,
		TOP,
		BOTTOM
	};
	
	private static final class QuadTreeElementOrientationEdge{
		
		private final QuadTreeElementOrientation orientation;
		
		private QuadTreeElementOrientationEdge(final QuadTreeElementOrientation orientation){
			this.orientation=orientation;
		}
	}
	
	private static final class LocalQuadTree extends DirectedRootedKaryTree<RightTriangleInfo[],QuadTreeElementOrientationEdge>{

		private LocalQuadTree(){
			super(false,4);
		}
	}
	
	private static final class LocalQuadTreeFiller extends DirectedConnectedComponentVisitor<RightTriangleInfo[],QuadTreeElementOrientationEdge,LocalQuadTree>{
		
		private final RightTriangleInfo[] triPairToInsert;
		
		private LocalQuadTreeFiller(final RightTriangleInfo[] triPairToInsert){
			this.triPairToInsert=triPairToInsert;
		}
		
		@Override
		protected final boolean performOnCurrentlyVisitedVertex(
				final LocalQuadTree tree,
				final RightTriangleInfo[] currentlyVisitedVertex){
			boolean triPairInsertionEnabled=false;
			final Collection<QuadTreeElementOrientationEdge> outgoingEdges=tree.getOutgoingEdges(currentlyVisitedVertex);
			for(QuadTreeElementOrientation orientation:QuadTreeElementOrientation.values())
			    {boolean isOrientationAvailable=true;
			     //checks if this orientation is not already in use
			     for(QuadTreeElementOrientationEdge outgoingEdge:outgoingEdges)
			    	 if(outgoingEdge.orientation.equals(orientation))
			             {isOrientationAvailable=false;
			    	      break;
			             }
			     if(isOrientationAvailable)
			         {switch(orientation)
			          {/**
			            * TODO if this edge of the current triangle is equal to 
			            * one edge of triPairToInsert, set 
			            * triPairInsertionEnabled to true
			            */
			           case LEFT:
			               {
			            	break;
			               }
			           case RIGHT:
			               {
			            	break;
			               }
			           case BOTTOM:
			               {
			            	break;
			               }
			           case TOP:
			               {
			            	break;
			               }
			          }
			    	  if(triPairInsertionEnabled)
			    	      {//adds a new edge with the proper orientation into the tree
			    		   final QuadTreeElementOrientationEdge edge=new QuadTreeElementOrientationEdge(orientation);
			    		   tree.addEdge(edge,triPairToInsert);
			    		   break;
			    	      }
			         }
			    }
			return(triPairInsertionEnabled);
		}
	}
	
	private static final LocalQuadTree buildQuaternaryTreeNodeFromTrianglesList(ArrayList<RightTriangleInfo> trisList) {
	    final LocalQuadTree tree=new LocalQuadTree();
	    if(trisList.size()>=2)
	        {//builds a list of pairs
	    	 ArrayList<RightTriangleInfo[]> triPairsList=new ArrayList<RightTriangleInfo[]>();
	    	 for(int triIndex=0,size=trisList.size();triIndex<size-1;triIndex+=2)
	    	     {RightTriangleInfo[] tris=new RightTriangleInfo[]{trisList.get(triIndex),trisList.get(triIndex+1)};
	    	      triPairsList.add(tris);
	    	     }
	    	 //puts the first pair of triangles into the root
	    	 tree.addVertex(triPairsList.remove(0));
	    	 for(RightTriangleInfo[] tris:triPairsList)
	    	     if(!tree.containsVertex(tris))
	    	         {final LocalQuadTreeFiller visitor=new LocalQuadTreeFiller(tris);
	    	          visitor.visit(tree,tree.getRoot(),true);
	    	         }
	        }
	    return(tree);
	}
	
	private static final class LocalQuadTree2dDimensionCalculator extends DirectedConnectedComponentVisitor<RightTriangleInfo[],QuadTreeElementOrientationEdge,LocalQuadTree>{

		private int i,j,leftMostIndex,rightMostIndex,topMostIndex,bottomMostIndex;
		
		@Override
		protected final boolean performOnCurrentlyVisitedVertex(
				final LocalQuadTree tree,
				final RightTriangleInfo[] currentlyVisitedVertex){
			if(tree.getRoot()!=currentlyVisitedVertex)
			    {final Collection<QuadTreeElementOrientationEdge> incomingEdges=tree.getIncomingEdges(currentlyVisitedVertex);
			     final QuadTreeElementOrientationEdge edgeFromParent=incomingEdges.iterator().next();
			     switch(edgeFromParent.orientation)
			     {case LEFT:
			          {i--;
			           leftMostIndex=Math.min(leftMostIndex,i);
			           break;
			          }
			      case RIGHT:
			          {i++;
		               rightMostIndex=Math.max(rightMostIndex,i);
		               break;
		              }
			      case TOP:
			          {j--;
			           topMostIndex=Math.min(topMostIndex,j);
			           break;
			          }
			      case BOTTOM:
			          {j++;
			           bottomMostIndex=Math.max(bottomMostIndex,j);
			           break;
			          }
			     }
			    }
			return(true);
		}

		private final int[] get2dDimension(){
			final int width=Math.abs(rightMostIndex-leftMostIndex)+1;
			final int height=Math.abs(bottomMostIndex-topMostIndex)+1;
			return(new int[]{width,height,leftMostIndex,rightMostIndex,topMostIndex,bottomMostIndex});
		}
	}
	
	private static final int[] computeTree2dDimension(LocalQuadTree tree){
		final int[] dimension;
		if(tree.getVertexCount()>0)
            {final LocalQuadTree2dDimensionCalculator visitor=new LocalQuadTree2dDimensionCalculator();
             visitor.visit(tree,tree.getRoot(),true);
             dimension=visitor.get2dDimension();
            }
		else
			dimension=new int[]{0,0};
		return(dimension);
	}
	
	private static final class LocalQuadTreeTo2dArrayFiller extends DirectedConnectedComponentVisitor<RightTriangleInfo[],QuadTreeElementOrientationEdge,LocalQuadTree>{

		private int i,j;
		
		private final RightTriangleInfo[][][] adjacentTrisArray;
		
		private final int leftMostIndex,topMostIndex;
		
		private LocalQuadTreeTo2dArrayFiller(final int leftMostIndex,
				final int topMostIndex,
				final RightTriangleInfo[][][] adjacentTrisArray){
			this.leftMostIndex=leftMostIndex;
			this.topMostIndex=topMostIndex;
			this.adjacentTrisArray=adjacentTrisArray;
		}
		
		@Override
		protected final boolean performOnCurrentlyVisitedVertex(
				final LocalQuadTree tree,
				final RightTriangleInfo[] currentlyVisitedVertex){
			if(tree.getRoot()!=currentlyVisitedVertex)
			    {final Collection<QuadTreeElementOrientationEdge> incomingEdges=tree.getIncomingEdges(currentlyVisitedVertex);
			     final QuadTreeElementOrientationEdge edgeFromParent=incomingEdges.iterator().next();
			     switch(edgeFromParent.orientation)
			     {case LEFT:
			          {i--;
			           break;
			          }
			      case RIGHT:
			          {i++;
		               break;
		              }
			      case TOP:
			          {j--;
			           break;
			          }
			      case BOTTOM:
			          {j++;
			           break;
			          }
			     }
			    }
			final int zeroBasedI=i-leftMostIndex;
			final int zeroBasedJ=j-topMostIndex;
			adjacentTrisArray[zeroBasedI][zeroBasedJ][0]=currentlyVisitedVertex[0];
			adjacentTrisArray[zeroBasedI][zeroBasedJ][1]=currentlyVisitedVertex[1];
			return(true);
		}
	}
	
	private static void fill2dArrayFromQuadTree(final int leftMostIndex,final int topMostIndex,
			final RightTriangleInfo[][][] adjacentTrisArray,LocalQuadTree tree){
		final LocalQuadTreeTo2dArrayFiller visitor=new LocalQuadTreeTo2dArrayFiller(
				leftMostIndex,topMostIndex,adjacentTrisArray);
		visitor.visit(tree,tree.getRoot(),true);
	}
	
	/**
	 * Compute a list of arrays of adjacent triangles which could be merged to 
	 * make bigger rectangles
	 * 
	 * @param adjacentTrisArray 2D arrays containing adjacent triangles
	 * @return list of 2D arrays of adjacent mergeable triangles
	 */
	private static ArrayList<RightTriangleInfo[][][]> computeAdjacentMergeableTrisArraysList(RightTriangleInfo[][][] adjacentTrisArray){
		ArrayList<RightTriangleInfo[][][]> adjacentTrisArraysList=new ArrayList<RightTriangleInfo[][][]>();
		//TODO
		return(adjacentTrisArraysList);
	}
}
