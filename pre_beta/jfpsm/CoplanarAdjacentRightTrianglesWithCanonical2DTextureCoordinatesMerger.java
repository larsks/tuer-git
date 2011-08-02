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
						              boolean sameHypotenuseOppositeSidesOfSameLengthSameWinding=tri1Vertices[tri1.sideIndexOfHypotenuse].equals(tri2Vertices[tri2.sideIndexOfHypotenuse])&&
							             tri1Vertices[(tri1.sideIndexOfHypotenuse+1)%3].equals(tri2Vertices[(tri2.sideIndexOfHypotenuse+1)%3])&&
							             tri1Vertices[tri1.sideIndexOfHypotenuse].distanceSquared(tri1Vertices[(tri1.sideIndexOfHypotenuse+2)%3])==
							             tri2Vertices[(tri2.sideIndexOfHypotenuse+1)%3].distanceSquared(tri2Vertices[(tri2.sideIndexOfHypotenuse+2)%3])&&
							             tri1Vertices[(tri1.sideIndexOfHypotenuse+1)%3].distanceSquared(tri1Vertices[(tri1.sideIndexOfHypotenuse+2)%3])==
							             tri2Vertices[tri2.sideIndexOfHypotenuse].distanceSquared(tri2Vertices[(tri2.sideIndexOfHypotenuse+2)%3]);
						              boolean sameHypotenuseOppositeSidesOfSameLengthReverseWinding=!sameHypotenuseOppositeSidesOfSameLengthSameWinding&&
						    		     (tri1Vertices[tri1.sideIndexOfHypotenuse].equals(tri2Vertices[(tri2.sideIndexOfHypotenuse+1)%3])&&
									      tri1Vertices[(tri1.sideIndexOfHypotenuse+1)%3].equals(tri2Vertices[tri2.sideIndexOfHypotenuse])&&
							              tri1Vertices[tri1.sideIndexOfHypotenuse].distanceSquared(tri1Vertices[(tri1.sideIndexOfHypotenuse+2)%3])==
							              tri2Vertices[tri2.sideIndexOfHypotenuse].distanceSquared(tri2Vertices[(tri2.sideIndexOfHypotenuse+2)%3])&&
							              tri1Vertices[(tri1.sideIndexOfHypotenuse+1)%3].distanceSquared(tri1Vertices[(tri1.sideIndexOfHypotenuse+2)%3])==
							              tri2Vertices[(tri2.sideIndexOfHypotenuse+1)%3].distanceSquared(tri2Vertices[(tri2.sideIndexOfHypotenuse+2)%3]));
						              //checks if their vertices at their right angles are different
						              boolean sameHypotenuseOppositeSidesOfSameLengthDifferentVerticesAtRightAngles=(sameHypotenuseOppositeSidesOfSameLengthSameWinding||
						    		     sameHypotenuseOppositeSidesOfSameLengthReverseWinding)&&
						    		     !tri1Vertices[(tri1.sideIndexOfHypotenuse+2)%3].equals(tri2Vertices[(tri2.sideIndexOfHypotenuse+2)%3]);
						              if(sameHypotenuseOppositeSidesOfSameLengthDifferentVerticesAtRightAngles)
						                  {//checks the texture coordinates
						    	           boolean texCoordsMatch=true;
						    	           for(int textureIndex=0;meshData.getTextureBuffer(textureIndex)!=null&&texCoordsMatch;textureIndex++)
						                       {tri1TextureCoords=getPrimitiveTextureCoords(meshData,tri1.primitiveIndex,tri1.sectionIndex,textureIndex,tri1TextureCoords);
						                        tri2TextureCoords=getPrimitiveTextureCoords(meshData,tri2.primitiveIndex,tri2.sectionIndex,textureIndex,tri2TextureCoords);
						                        //checks if the vertices of the hypotenuse must have the same texture coordinates in both triangles
						                        if(sameHypotenuseOppositeSidesOfSameLengthSameWinding)
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
				            boolean oneCommonSide2oppositeSidesOfSameLengthAndSameTextureCoordinatesFound=false;
				            boolean oneCommonSide2oppositeSidesOfSameLength=false;
				            /**
				             * checks if both rectangles have exactly one common side, i.e if one vertex is common to 2 triangles 
				             * from 2 different rectangles but not on any hypotenuse and if another vertex is common to 2 triangles
				             * from 2 different rectangles but on the both hypotenuse.
				             * Then, it checks if the orthogonal sides adjacent with this common side have the same length.
				             * After that, it checks if both rectangles have the same texture coordinates.
				             * */
				            tris[0]=tri1;
				            tris[1]=tri2;
				            tris[2]=tri3;
				            tris[3]=tri4;				            
				            for(int i=0;i<4&&!oneCommonSide2oppositeSidesOfSameLength;i++)
				            	//checks if both rectangles have exactly one common side
				            	if(trisVertices[i/2][(tris[i/2].sideIndexOfHypotenuse+2)%3].equals(trisVertices[i%2][(tris[i%2].sideIndexOfHypotenuse+2)%3]))
				            		for(int j=0;j<4&&!oneCommonSide2oppositeSidesOfSameLength;j++)
				            			if(trisVertices[i/2][(tris[i/2].sideIndexOfHypotenuse+(((j/2)+1)%2))%3].equals(trisVertices[i%2][(tris[i%2].sideIndexOfHypotenuse+((j+1)%2))%3])&&
				            			   //checks if the orthogonal sides adjacent with this common side have the same length
				            			   trisVertices[i/2][(tris[i/2].sideIndexOfHypotenuse+(j/2))%3].distanceSquared(trisVertices[i/2][(tris[i/2].sideIndexOfHypotenuse+2)%3])==
				            			   trisVertices[i%2][(tris[i%2].sideIndexOfHypotenuse+(j%2))%3].distanceSquared(trisVertices[i%2][(tris[i%2].sideIndexOfHypotenuse+2)%3]))
				            	            {oneCommonSide2oppositeSidesOfSameLength=true;
				            				 //checks the texture coordinates
				            				 boolean texCoordsMatch=true;
				            				 //for each texture unit
				            				 for(int textureIndex=0;meshData.getTextureBuffer(textureIndex)!=null&&texCoordsMatch;textureIndex++)
				            				     {//gets all texture coordinates
				            					  for(int k=0;k<4;k++)
				     				            	  trisTextureCoords[k]=getPrimitiveTextureCoords(meshData,tris[k].primitiveIndex,tris[k].sectionIndex,textureIndex,trisTextureCoords[k]);
				            					  //TODO: checks if both rectangles have the same texture coordinates   					  
				            				     }
				            				 oneCommonSide2oppositeSidesOfSameLengthAndSameTextureCoordinatesFound=texCoordsMatch;
				                            }
				            if(oneCommonSide2oppositeSidesOfSameLengthAndSameTextureCoordinatesFound)
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
			 //TODO: fifth step: create lists of adjacent rectangles in the same planes usable to make bigger rectangles
			 /**
			  * Each entry handles the triangles of a plane. Each entry contains several lists of groups of adjacent triangles.
			  * Each group of adjacent triangles is a list of arrays of adjacent triangles which could be merged to make bigger 
			  * rectangles
			  * */
			 HashMap<Plane,ArrayList<ArrayList<RightTriangleInfo[][]>>> mapOfListsOfListsOfArraysOfMergeableTris=new HashMap<Plane,ArrayList<ArrayList<RightTriangleInfo[][]>>>();
			 //for each plane
			 for(Entry<Plane,ArrayList<ArrayList<RightTriangleInfo>>> entry:mapOfListsOfTrianglesByPlanes.entrySet())
			     {Plane plane=entry.getKey();
				  //for each list of adjacent triangles
				  for(ArrayList<RightTriangleInfo> trisList:entry.getValue())
			          {int width=0;
				       int height=0;
			    	   //TODO compute the maximum size of the 2D array of adjacent triangles
				       if(width>0&&height>0)
				           {RightTriangleInfo[][] adjacentTrisArray=new RightTriangleInfo[width][height];
				            ArrayList<RightTriangleInfo[][]> adjacentTrisArraysList=new ArrayList<RightTriangleInfo[][]>();
				            //TODO compute a list of arrays of adjacent triangles which could be merged to make bigger rectangles
				            //puts the new list into the map
				            ArrayList<ArrayList<RightTriangleInfo[][]>> adjacentTrisArraysListsList=mapOfListsOfListsOfArraysOfMergeableTris.get(plane);
				            if(adjacentTrisArraysListsList==null)
				                {adjacentTrisArraysListsList=new ArrayList<ArrayList<RightTriangleInfo[][]>>();
				                 mapOfListsOfListsOfArraysOfMergeableTris.put(plane,adjacentTrisArraysListsList);
				                }
				            adjacentTrisArraysListsList.add(adjacentTrisArraysList);
				           }
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
