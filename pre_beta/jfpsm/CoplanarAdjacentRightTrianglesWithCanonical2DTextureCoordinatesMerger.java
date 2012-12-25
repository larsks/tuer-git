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

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.FloatBuffer;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map.Entry;

import com.ardor3d.image.util.AWTImageLoader;
import com.ardor3d.math.Plane;
import com.ardor3d.math.Triangle;
import com.ardor3d.math.Vector2;
import com.ardor3d.math.Vector3;
import com.ardor3d.math.type.ReadOnlyVector3;
import com.ardor3d.renderer.IndexMode;
import com.ardor3d.scenegraph.FloatBufferData;
import com.ardor3d.scenegraph.Mesh;
import com.ardor3d.scenegraph.MeshData;
import com.ardor3d.scenegraph.Node;
import com.ardor3d.util.export.binary.BinaryImporter;
import com.ardor3d.util.geom.BufferUtils;
import com.ardor3d.util.geom.GeometryTool.MatchCondition;
import com.ardor3d.util.resource.ResourceLocatorTool;
import com.ardor3d.util.resource.SimpleResourceLocator;


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
	
	private static final class NextQuadInfo{
		
		private final Vector3[] vertices;
		
		private final Vector2[] textureCoords;
		
		private final int[] indices;
		
		private NextQuadInfo(final Vector3[] vertices,final Vector2[] textureCoords,final int[] indices){
			this.vertices=vertices;
			this.textureCoords=textureCoords;
			this.indices=indices;
		}
	}
	
	/**
	 * 
	 * @param mesh using only the first texture unit
	 * @return
	 */
	public static void optimize(final Mesh mesh){
		final MeshData meshData=mesh.getMeshData();
		//if there is exactly one texture unit, if there is a texture buffer for this first texture unit
		//if there are 2 texture coordinates per vertex and 3 vertex coordinates per vertex
		if(meshData.getNumberOfUnits()==1&&meshData.getTextureBuffer(0)!=null&&
		   meshData.getTextureCoords(0).getValuesPerTuple()==2&&
		   meshData.getVertexBuffer()!=null&&meshData.getVertexCoords().getValuesPerTuple()==3)
		    {//converts this geometry into non indexed geometry (if necessary) in order to ease further operations
		     final boolean previousGeometryWasIndexed=meshData.getIndexBuffer()!=null;
		     if(previousGeometryWasIndexed)
		         new GeometryHelper().convertIndexedGeometryIntoNonIndexedGeometry(meshData);
			 //first step: separates right triangles with canonical 2D texture coordinates from the others
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
							    //only considers the first texture index
							    final int textureIndex=0;
							    triangleTextureCoords=getPrimitiveTextureCoords(meshData,trianglePrimitiveIndex,sectionIndex,textureIndex,triangleTextureCoords);		    
								for(int triangleTextureCoordsIndex=0;triangleTextureCoordsIndex<3&&hasCanonicalTextureCoords;triangleTextureCoordsIndex++)
								    {u=triangleTextureCoords[triangleTextureCoordsIndex].getX();
								     v=triangleTextureCoords[triangleTextureCoordsIndex].getY();
								     if((u!=0&&u!=1)||(v!=0&&v!=1))
								         hasCanonicalTextureCoords=false;
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
						    	           boolean texCoordsMatch;
						    	           //only considers the first texture index
										   final int textureIndex=0;
						    	           tri1TextureCoords=getPrimitiveTextureCoords(meshData,tri1.primitiveIndex,tri1.sectionIndex,textureIndex,tri1TextureCoords);
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
				                                        //FIXME this test seems to be wrong
				                                        oneCommonSideCorrectVertexOrder=/*j!=k*/true;
				                                        if(oneCommonSideCorrectVertexOrder)
				                                            {//checks if the orthogonal sides adjacent with this common side have the same length
					                                         if(tv0[(tr0.sideIndexOfHypotenuse+((k+1)%2))%3].distanceSquared(tv0[(tr0.sideIndexOfHypotenuse+2)%3])==
									                            tv1[(tr1.sideIndexOfHypotenuse+((j+1)%2))%3].distanceSquared(tv1[(tr1.sideIndexOfHypotenuse+2)%3]))
							                        	         {oneCommonSideCorrectVertexOrder2oppositeSidesOfSameLength=true;	                        	          
							                        	          //checks the texture coordinates
									            				  boolean texCoordsMatch=true;				            				   
									            				  //only considers the first texture index
																  final int textureIndex=0;
									            				  //gets all texture coordinates
									            				  for(int l=0;l<4;l++)
									     				              trisTextureCoords[l]=getPrimitiveTextureCoords(meshData,tris[l].primitiveIndex,tris[l].sectionIndex,textureIndex,trisTextureCoords[l]);
									            				  //checks if both rectangles have the same texture coordinates
									            				  texCoordsMatch&=trisTextureCoords[ti0][(tr0.sideIndexOfHypotenuse+2)%3].equals(trisTextureCoords[ti3][(tr3.sideIndexOfHypotenuse+2)%3]);
									            				  texCoordsMatch&=trisTextureCoords[ti1][(tr1.sideIndexOfHypotenuse+2)%3].equals(trisTextureCoords[ti2][(tr2.sideIndexOfHypotenuse+2)%3]);
									            				  texCoordsMatch&=trisTextureCoords[ti0][(tr0.sideIndexOfHypotenuse+((k+1)%2))%3].equals(trisTextureCoords[ti1][(tr1.sideIndexOfHypotenuse+j)%3]);
									            				  texCoordsMatch&=trisTextureCoords[ti1][(tr1.sideIndexOfHypotenuse+((j+1)%2))%3].equals(trisTextureCoords[ti0][(tr0.sideIndexOfHypotenuse+k)%3]);
									            				  oneCommonSideCorrectVertexOrder2oppositeSidesOfSameLengthAndSameTextureCoordinatesFound=texCoordsMatch;
									            				  if(oneCommonSideCorrectVertexOrder2oppositeSidesOfSameLengthAndSameTextureCoordinatesFound)
								                                      {//stores tr0, tr1, tr2, tr3 and the indices for further uses
									            					   //FIXME consider treating tr2 and tr3, omitting them breaks the first part of the fifth step
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
									            				  //only considers the first texture index
																  final int textureIndex=0;
									            				  //gets all texture coordinates
									            				  for(int l=0;l<4;l++)
									     				              trisTextureCoords[l]=getPrimitiveTextureCoords(meshData,tris[l].primitiveIndex,tris[l].sectionIndex,textureIndex,trisTextureCoords[l]);
									            				  //checks if both rectangles have the same texture coordinates
									            				  texCoordsMatch&=trisTextureCoords[ti0][(tr0.sideIndexOfHypotenuse+(((k/2)+1)%2))%3].equals(trisTextureCoords[ti1][(tr1.sideIndexOfHypotenuse+2)%3]);
									            				  texCoordsMatch&=trisTextureCoords[ti1][(tr1.sideIndexOfHypotenuse+((k+1)%2))%3].equals(trisTextureCoords[ti0][(tr0.sideIndexOfHypotenuse+2)%3]);
									            				  texCoordsMatch&=trisTextureCoords[ti1][(tr1.sideIndexOfHypotenuse+(k%2))%3].equals(trisTextureCoords[ti2][(tr2.sideIndexOfHypotenuse+2)%3]);
									            				  texCoordsMatch&=trisTextureCoords[ti0][(tr0.sideIndexOfHypotenuse+(k/2))%3].equals(trisTextureCoords[ti3][(tr3.sideIndexOfHypotenuse+2)%3]);
									            				  oneCommonSideCorrectVertexOrder2oppositeSidesOfSameLengthAndSameTextureCoordinatesFound=texCoordsMatch;
									            				  if(oneCommonSideCorrectVertexOrder2oppositeSidesOfSameLengthAndSameTextureCoordinatesFound)
									                                  {//stores tr0, tr1, tr2, tr3 and the indices for further uses
									            					   //FIXME consider treating tr2 and tr3, omitting them breaks the first part of the fifth step
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
			     {final Plane plane=entry.getKey();
				  //for each list of adjacent triangles
				  for(ArrayList<RightTriangleInfo> trisList:entry.getValue())
					  if(!trisList.isEmpty())
			              {//builds the 2D array from the list of triangles
						   //FIXME filter the second argument in order to keep only the information about the triangles in the first argument
						   final RightTriangleInfo[][][] adjacentTrisArray=compute2dTrisArrayFromAdjacentTrisList(trisList,commonSidesInfosMap);
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
			 //sixth step: creates these bigger rectangles with texture coordinates greater than 1 in order to use texture repeat
			 HashMap<Plane,HashMap<RightTriangleInfo[][][],NextQuadInfo>> mapOfPreviousAndNextAdjacentTrisMaps=new HashMap<Plane,HashMap<RightTriangleInfo[][][],NextQuadInfo>>();
			 //for each plane
			 for(Entry<Plane,ArrayList<ArrayList<RightTriangleInfo[][][]>>> entry:mapOfListsOfListsOfArraysOfMergeableTris.entrySet())
			     {final Plane plane=entry.getKey();
			      final HashMap<RightTriangleInfo[][][],NextQuadInfo> previousAdjacentTrisAndNextQuadInfosMaps=new HashMap<RightTriangleInfo[][][],NextQuadInfo>();
			      mapOfPreviousAndNextAdjacentTrisMaps.put(plane,previousAdjacentTrisAndNextQuadInfosMaps);
			      //for each list of arrays of adjacent triangles which could be merged to make bigger rectangles
			      for(ArrayList<RightTriangleInfo[][][]> adjacentTrisArraysList:entry.getValue())
			    	  //for each array of adjacent triangles
			    	  for(RightTriangleInfo[][][] adjacentTrisArray:adjacentTrisArraysList)
			    	      {//checks if it contains at least one row and if the first row contains at least one element
			    		   if(adjacentTrisArray.length>=1&&adjacentTrisArray[0]!=null&&adjacentTrisArray[0].length>=1)
		                       {//checks if this array is full and rectangular (i.e all rows contain the same count of elements)
		            	        boolean isFull=true;
		            	        boolean isRectangular=true;
			    			    for(int rowIndex=0;rowIndex<adjacentTrisArray.length&&isRectangular&&isFull;rowIndex++)
		            	        	{if(adjacentTrisArray[rowIndex]==null||adjacentTrisArray[rowIndex].length!=adjacentTrisArray[0].length)
		            	        		 isRectangular=false;
		            	        	 else
			    			    	     for(int columnIndex=0;columnIndex<adjacentTrisArray[rowIndex].length&&isFull;columnIndex++)
		            	        		     if(adjacentTrisArray[rowIndex][columnIndex]==null||adjacentTrisArray[rowIndex][columnIndex].length!=2)
		            	        	             isFull=false;
		            	        	}
			    			    //checks if this array is full, rectangular and if it contains more than one pair of adjacent triangles
			    			    if(isRectangular&&isFull&&(adjacentTrisArray.length>1||adjacentTrisArray[0].length>1))
			    			        {//as this array is rectangular, it has a consistent row count and column count
			    			    	 final int rowCount=adjacentTrisArray.length;
			    			    	 final int columnCount=adjacentTrisArray[0].length;
			    			    	 //computes the new pair of right adjacent triangles
			    			    	 final RightTriangleInfo[] mergedAdjacentTris=new RightTriangleInfo[2];
			    			    	 final Vector3[] mergedAdjacentTrisVertices=new Vector3[4];
			    			    	 final Vector2[] mergedAdjacentTrisTextureCoords=new Vector2[4];
			    			    	 final int[] tmpLocalIndices=new int[4];
			    			    	 final int[] mergedAdjacentTrisVerticesIndices=new int[6];
			    			    	 final Vector3[] testedAdjacentTrisVertices=new Vector3[8];
			    			    	 final Vector2[] testedAdjacentTrisTextureCoords=new Vector2[4];
			    			    	 //for each pair of triangles in a corner of the array
			    			    	 for(int rowIndex=0;rowIndex<=1;rowIndex++)
			    			    		 {final int rawRowIndex=rowIndex*(rowCount-1);
			    			    		  for(int columnIndex=0;columnIndex<=1;columnIndex++)
			    			    		      {final int rawColumnIndex=columnIndex*(columnCount-1);
			    			    			   tri1=adjacentTrisArray[rawRowIndex][rawColumnIndex][0];
			    			    			   tri2=adjacentTrisArray[rawRowIndex][rawColumnIndex][1];
			    			    			   tri1Vertices=meshData.getPrimitiveVertices(tri1.primitiveIndex,tri1.sectionIndex,tri1Vertices);
			    			    			   tri2Vertices=meshData.getPrimitiveVertices(tri2.primitiveIndex,tri2.sectionIndex,tri2Vertices);
			    			    			   //retrieves the distinct vertices of the current corner
			    			    			   //both triangles have reverse vertex orders (see the third step)
			    			    			   testedAdjacentTrisVertices[0]=tri1Vertices[tri1.sideIndexOfHypotenuse];
			    			    			   testedAdjacentTrisVertices[1]=tri2Vertices[tri2.sideIndexOfHypotenuse];
			    			    			   testedAdjacentTrisVertices[2]=tri1Vertices[(tri1.sideIndexOfHypotenuse+2)%3];
			    			    			   testedAdjacentTrisVertices[3]=tri2Vertices[(tri2.sideIndexOfHypotenuse+2)%3];
			    			    			   //retrieves the texture coordinates
			    			    			   tri1TextureCoords=getPrimitiveTextureCoords(meshData,tri1.primitiveIndex,tri1.sectionIndex,0,tri1TextureCoords);
						                       tri2TextureCoords=getPrimitiveTextureCoords(meshData,tri2.primitiveIndex,tri2.sectionIndex,0,tri2TextureCoords);
						                       testedAdjacentTrisTextureCoords[0]=tri1TextureCoords[tri1.sideIndexOfHypotenuse];
						                       testedAdjacentTrisTextureCoords[1]=tri2TextureCoords[tri2.sideIndexOfHypotenuse];
						                       testedAdjacentTrisTextureCoords[2]=tri1TextureCoords[(tri1.sideIndexOfHypotenuse+2)%3];
						                       testedAdjacentTrisTextureCoords[3]=tri2TextureCoords[(tri2.sideIndexOfHypotenuse+2)%3];
			    			    			   //looks for the real vertex of the corner
			    			    			   boolean cornerVertexFound=false;
			    			    			   for(int testedVertexIndex=0;testedVertexIndex<4&&!cornerVertexFound;testedVertexIndex++)
			    			    			       {cornerVertexFound=true;
			    			    				    for(int testedCloseCell1DIndex=1;testedCloseCell1DIndex<=3&&cornerVertexFound;testedCloseCell1DIndex++)
			    			    			            {final int secondaryRawRowIndex=Math.max(0,rawRowIndex+((rowIndex==0?1:-1)*(testedCloseCell1DIndex/2)))%rowCount;
			    			    			             final int secondaryRawColumnIndex=Math.max(0,rawColumnIndex+((columnIndex==0?1:-1)*(testedCloseCell1DIndex%2)))%columnCount;
			    			    				    	 tri3=adjacentTrisArray[secondaryRawRowIndex][secondaryRawColumnIndex][0];
						    			    			 tri4=adjacentTrisArray[secondaryRawRowIndex][secondaryRawColumnIndex][1];
						    			    			 tri3Vertices=meshData.getPrimitiveVertices(tri3.primitiveIndex,tri3.sectionIndex,tri3Vertices);
						    			    			 tri4Vertices=meshData.getPrimitiveVertices(tri4.primitiveIndex,tri4.sectionIndex,tri4Vertices);
						    			    			 testedAdjacentTrisVertices[4]=tri3Vertices[tri3.sideIndexOfHypotenuse];
						    			    			 testedAdjacentTrisVertices[5]=tri4Vertices[tri4.sideIndexOfHypotenuse];
						    			    			 testedAdjacentTrisVertices[6]=tri3Vertices[(tri3.sideIndexOfHypotenuse+2)%3];
						    			    			 testedAdjacentTrisVertices[7]=tri4Vertices[(tri4.sideIndexOfHypotenuse+2)%3];
						    			    			 for(int secondaryTestedVertexIndex=4;secondaryTestedVertexIndex<8&&cornerVertexFound;secondaryTestedVertexIndex++)
			    			    			            	 cornerVertexFound=!testedAdjacentTrisVertices[testedVertexIndex].equals(testedAdjacentTrisVertices[secondaryTestedVertexIndex]);
			    			    			            }
			    			    			        if(cornerVertexFound)
			    			    			            {//checks whether this corner is already in use
			    			    			        	 boolean cornerAlreadyInUse=false;
			    			    			        	 for(int mergedAdjacentTrisVertexIndex=0;mergedAdjacentTrisVertexIndex<4&&!cornerAlreadyInUse;mergedAdjacentTrisVertexIndex++)
			    			    			        		 if(mergedAdjacentTrisVertices[mergedAdjacentTrisVertexIndex]!=null&&
			    			    			        		    mergedAdjacentTrisVertices[mergedAdjacentTrisVertexIndex].equals(testedAdjacentTrisVertices[testedVertexIndex]))
			    			    			        			 cornerAlreadyInUse=true;
			    			    			        	 //if this corner is already in use, the search must go on
			    			    			        	 if(cornerAlreadyInUse)
			    			    			        		 cornerVertexFound=false;
			    			    			        	 else
			    			    			        		 {final int localIndex=(rowIndex/2)+(columnIndex%2);
			    			    			        		  //stores the vertex
			    			    			        		  mergedAdjacentTrisVertices[localIndex]=testedAdjacentTrisVertices[testedVertexIndex];
			    			    			        		  //stores its texture coordinates
			    			    			        		  mergedAdjacentTrisTextureCoords[localIndex]=testedAdjacentTrisTextureCoords[testedVertexIndex];
			    			    			        		  //stores its temporary index in order to know from which triangle it comes and whether it is on the hypotenuse
			    			    			        		  tmpLocalIndices[localIndex]=testedVertexIndex;
			    			    			        		  //if this vertex is not on the hypotenuse
			    			    			        		  if(testedVertexIndex/2==1)
			    			    			        		      {//stores its triangle in order to keep the same orientation
			    			    			        			   if(mergedAdjacentTris[0]==null)
			    			    			        		    	   mergedAdjacentTris[0]=testedVertexIndex==2?tri1:tri2;
			    			    			        		       else
			    			    			        		    	   if(mergedAdjacentTris[1]==null)
				    			    			        		    	   mergedAdjacentTris[1]=testedVertexIndex==2?tri1:tri2;
			    			    			        		    	   else
			    			    			        		    		   System.err.println("there are too much vertices not on the hypotenuse");
			    			    			        		      }
			    			    			        		 }
			    			    			            }
			    			    			       }
			    			    			   if(!cornerVertexFound)
			    			    				   System.err.println("missing corner");
			    			    		      }
			    			    		 }
			    			    	 //keeps the orientation of the previous triangles
			    			    	 Arrays.fill(mergedAdjacentTrisVerticesIndices,-1);
			    			    	 tri1=mergedAdjacentTris[0];
			    			    	 tri2=mergedAdjacentTris[1];
			    			    	 //FIXME the detection of corners is broken
			    			    	 if(tri1!=null&&tri2!=null)
			    			    	     {
			    			    	 tri1TextureCoords=getPrimitiveTextureCoords(meshData,tri1.primitiveIndex,tri1.sectionIndex,0,tri1TextureCoords);
				                     tri2TextureCoords=getPrimitiveTextureCoords(meshData,tri2.primitiveIndex,tri2.sectionIndex,0,tri2TextureCoords);
				                     testedAdjacentTrisTextureCoords[0]=tri1TextureCoords[tri1.sideIndexOfHypotenuse];
				                     testedAdjacentTrisTextureCoords[1]=tri2TextureCoords[tri2.sideIndexOfHypotenuse];
				                     testedAdjacentTrisTextureCoords[2]=tri1TextureCoords[(tri1.sideIndexOfHypotenuse+2)%3];
				                     testedAdjacentTrisTextureCoords[3]=tri2TextureCoords[(tri2.sideIndexOfHypotenuse+2)%3];
				                     //operates on the vertices not on the hypotenuse first
				                     for(int localIndex=0;localIndex<4;localIndex++)
				                    	 {if(mergedAdjacentTrisTextureCoords[localIndex].equals(testedAdjacentTrisTextureCoords[0]))
				                    		  {if(mergedAdjacentTrisVerticesIndices[0]==-1)
				                    		       mergedAdjacentTrisVerticesIndices[0]=localIndex;
				                    		   else
				                    			   if(mergedAdjacentTrisVerticesIndices[4]==-1)
				                    				   mergedAdjacentTrisVerticesIndices[4]=localIndex;
				                    			   else
				                    				   System.err.println("there are too much vertices with the same texture coordinates");
				                    		  }
				                    	  else
				                    		  if(mergedAdjacentTrisTextureCoords[localIndex].equals(testedAdjacentTrisTextureCoords[1]))
				                    			  {if(mergedAdjacentTrisVerticesIndices[1]==-1)
				                    			       mergedAdjacentTrisVerticesIndices[1]=localIndex;
				                    			   else
				                    				   if(mergedAdjacentTrisVerticesIndices[3]==-1)
				                    					   mergedAdjacentTrisVerticesIndices[3]=localIndex;
				                    				   else
				                    					   System.err.println("there are too much vertices with the same texture coordinates");
				                    			  }
				                    	  if(mergedAdjacentTrisTextureCoords[localIndex].equals(testedAdjacentTrisTextureCoords[2]))
				                    		  mergedAdjacentTrisVerticesIndices[2]=localIndex;
				                    	  else
				                    		  if(mergedAdjacentTrisTextureCoords[localIndex].equals(testedAdjacentTrisTextureCoords[3]))
				                    		      mergedAdjacentTrisVerticesIndices[5]=localIndex;
				                    	 }
			    			    	 //updates texture coordinates equal to 1
			    			    	 u=(double)columnCount;
			    			    	 v=(double)rowCount;
			    			    	 for(int localIndex=0;localIndex<4;localIndex++)
			    			    	     {if(mergedAdjacentTrisTextureCoords[localIndex].getX()==1)
			    			    	    	  mergedAdjacentTrisTextureCoords[localIndex].setX(u);
			    			    	      if(mergedAdjacentTrisTextureCoords[localIndex].getY()==1)
			    			    	    	  mergedAdjacentTrisTextureCoords[localIndex].setY(v);
			    			    	     }
			    			    	 //stores the couple of old pairs and the new pairs (with some information) in order to remove the former and to add the latter
			    			    	 final NextQuadInfo quadInfo=new NextQuadInfo(mergedAdjacentTrisVertices,mergedAdjacentTrisTextureCoords,mergedAdjacentTrisVerticesIndices);
			    			    	 previousAdjacentTrisAndNextQuadInfosMaps.put(adjacentTrisArray,quadInfo);
			    			    	     }
			    			        }
		                       }
			    	      }
			     }
			 //seventh step: removes the triangles which are no more in the geometry of the mesh
			 final ArrayList<Integer> verticesIndicesToRemove=new ArrayList<Integer>();
			 int[] tri1Indices=new int[3];
			 int[] tri2Indices=new int[3];
			 //for each plane
			 for(Entry<Plane,HashMap<RightTriangleInfo[][][],NextQuadInfo>> mapOfPreviousAndNextAdjacentTrisMapsEntry:mapOfPreviousAndNextAdjacentTrisMaps.entrySet())
			     {//for each couple of old pairs and the new pairs (with some information)
				  for(Entry<RightTriangleInfo[][][],NextQuadInfo> previousAdjacentTrisAndNextQuadInfosEntry:mapOfPreviousAndNextAdjacentTrisMapsEntry.getValue().entrySet())
					  {final RightTriangleInfo[][][] previousAdjacentTrisArray=previousAdjacentTrisAndNextQuadInfosEntry.getKey();
					   for(int rowIndex=0;rowIndex<previousAdjacentTrisArray.length;rowIndex++)
						   for(int columnIndex=0;columnIndex<previousAdjacentTrisArray[rowIndex].length;columnIndex++)
						       {//retrieves the vertices
							    tri1=previousAdjacentTrisArray[rowIndex][columnIndex][0];
			    			    tri2=previousAdjacentTrisArray[rowIndex][columnIndex][1];
			    			    tri1Vertices=meshData.getPrimitiveVertices(tri1.primitiveIndex,tri1.sectionIndex,tri1Vertices);
			    			    tri2Vertices=meshData.getPrimitiveVertices(tri2.primitiveIndex,tri2.sectionIndex,tri2Vertices);
			    			    tri1Indices=meshData.getPrimitiveIndices(tri1.primitiveIndex,tri1.sectionIndex,tri1Indices);
			    			    tri2Indices=meshData.getPrimitiveIndices(tri2.primitiveIndex,tri2.sectionIndex,tri2Indices);
			    			    //does not keep these vertices, mark them as removable
			    			    for(int triVertexIndex=0;triVertexIndex<tri1Vertices.length;triVertexIndex++)
			    			    	verticesIndicesToRemove.add(Integer.valueOf(tri1Indices[triVertexIndex]));
			    			    for(int triVertexIndex=0;triVertexIndex<tri2Vertices.length;triVertexIndex++)
			    			    	verticesIndicesToRemove.add(Integer.valueOf(tri2Indices[triVertexIndex]));
						       }
					  }
			     }
			 //computes the count of added vertices
			 int addedVerticesCount=0;
			 for(HashMap<RightTriangleInfo[][][],NextQuadInfo> previousAndNextAdjacentTrisMap:mapOfPreviousAndNextAdjacentTrisMaps.values())
				 {//there are (obviously) two triangles by quad and three vertices by triangle
				  addedVerticesCount+=previousAndNextAdjacentTrisMap.size()*6;
				 }
			 //computes the next vertex count
			 final int nextVertexCount=meshData.getVertexCount()-verticesIndicesToRemove.size()+addedVerticesCount;
			 //creates the next vertex buffer
			 final FloatBuffer nextVertexBuffer=FloatBuffer.allocate(nextVertexCount*3);
		     //does not copy the vertices marked as removable into the next vertex buffer, copies the others
			 for(int vertexIndex=0;vertexIndex<meshData.getVertexCount();vertexIndex++)
				 if(!verticesIndicesToRemove.contains(Integer.valueOf(vertexIndex)))
				     {final int vertexCoordinateIndex=vertexIndex*3;
				      final float x=meshData.getVertexBuffer().get(vertexCoordinateIndex);
				      final float y=meshData.getVertexBuffer().get(vertexCoordinateIndex+1);
				      final float z=meshData.getVertexBuffer().get(vertexCoordinateIndex+2);
				      nextVertexBuffer.put(x).put(y).put(z);
				     }
			 //does not modify the position so that this vertex buffer is ready for the addition of the new vertices
			 //computes the next texture coordinate count
			 final int nextTextureCoordsCount=nextVertexCount;
			 //creates the next texture buffer (2D)
			 final FloatBuffer nextTextureBuffer=FloatBuffer.allocate(nextTextureCoordsCount*2);
			 //does not copy the texture coordinates of vertices marked as removable into the next vertex buffer, copies the others
			 for(int vertexIndex=0;vertexIndex<meshData.getVertexCount();vertexIndex++)
				 if(!verticesIndicesToRemove.contains(Integer.valueOf(vertexIndex)))
				     {final int textureCoordinateIndex=vertexIndex*2;
				      final float fu=meshData.getVertexBuffer().get(textureCoordinateIndex);
				      final float fv=meshData.getVertexBuffer().get(textureCoordinateIndex+1);
				      nextTextureBuffer.put(fu).put(fv);
				     }
			 //does not modify the position so that this texture buffer is ready for the addition of the new texture coordinates
			 //eighth step: adds the new triangles into the geometry of the mesh
			 for(HashMap<RightTriangleInfo[][][],NextQuadInfo> previousAndNextAdjacentTrisMap:mapOfPreviousAndNextAdjacentTrisMaps.values())
			     for(NextQuadInfo nextQuadInfo:previousAndNextAdjacentTrisMap.values())
			         {//uses the six indices to know which vertices to use in order to build the two triangles
				      for(int indexIndex=0;indexIndex<nextQuadInfo.indices.length;indexIndex++)
				          {final int vertexIndex=nextQuadInfo.indices[indexIndex];
				    	   final Vector3 vertex=nextQuadInfo.vertices[vertexIndex];
				    	   final Vector2 texCoord=nextQuadInfo.textureCoords[vertexIndex];
				    	   final float x=vertex.getXf();
				    	   final float y=vertex.getYf();
				    	   final float z=vertex.getZf();
				    	   final float fu=texCoord.getXf();
				    	   final float fv=texCoord.getYf();
				    	   nextVertexBuffer.put(x).put(y).put(z);
				    	   nextTextureBuffer.put(fu).put(fv);
				          }
			         }
			 //finally, rewinds the new vertex buffer and sets it
			 nextVertexBuffer.rewind();
			 meshData.setVertexBuffer(nextVertexBuffer);
			 //does the same for texture coordinates
			 nextTextureBuffer.rewind();
			 meshData.setTextureCoords(new FloatBufferData(nextTextureBuffer,2),0);
			 //if the supplied geometry was indexed
		     if(previousGeometryWasIndexed)
		         {//converts the new geometry into an indexed geometry
			      //uses all conditions with GeometryTool
			      final EnumSet<MatchCondition> conditions=EnumSet.of(MatchCondition.UVs,MatchCondition.Normal,MatchCondition.Color);
			      //reduces the geometry to avoid duplication of vertices
			      new GeometryHelper().minimizeVerts(mesh,conditions);
		         }
		    }
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
			final HashMap<RightTriangleInfo,ArrayList<Entry<RightTriangleInfo[],int[]>>> commonSidesInfosMap){
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
			 for(ArrayList<Entry<RightTriangleInfo[],int[]>> commonSidesInfos:commonSidesInfosMap.values())
				 {for(Entry<RightTriangleInfo[],int[]> commonSideInfo:commonSidesInfos)
					  if(trisList.contains(commonSideInfo.getKey()[0])||trisList.contains(commonSideInfo.getKey()[1]))
				          infosQueue.add(commonSideInfo);
				 }
			 int infosQueueIndex=0;
			 //loops while this list is not empty
			 //FIXME rather loop until all triangles of the list supplied in the first parameter are used
			 while(/*!infosQueue.isEmpty()*/arrayMap.size()<trisList.size())
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
					        if(tri1index!=-1)
					            {
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
					  }
				  else
				      {//if the array already contains the second triangle
					   if(arrayMap.containsKey(tris[1]))
			               {//retrieves the indices of the triangle in the 2D array
						    final int[] arrayIndices=arrayMap.get(tris[1]);
				    	    //finds which sides are common and updates the array
			                final int tri0index=trisList.indexOf(tris[0]);
			                if(tri0index!=-1)
			                    {
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
			               }
			           else
			        	   inserted=false;
			          }
			      if(inserted)
			    	  {//removes the information we used
			    	   /*infosQueue.remove(infosQueueIndex);
			    	   //resets the index if it is out of the bounds
			    	   if(infosQueueIndex==infosQueue.size())
			    		   infosQueueIndex=0;*/
			    	   final int tri0localIndex=trisList.indexOf(tris[0]);
			    	   if(tri0localIndex!=-1)
			    	       {final int tri2localIndex;
			    		    if(tri0localIndex%2==0)
			    	            tri2localIndex=tri0localIndex+1;
			    	        else
			    	        	tri2localIndex=tri0localIndex-1;
			    		    final RightTriangleInfo tri=trisList.get(tri2localIndex);
			    		    if(!arrayMap.containsKey(tri))
			    		        arrayMap.put(tri,arrayMap.get(tris[0]));
			    	       }
			    	   final int tri1localIndex=trisList.indexOf(tris[1]);
			    	   if(tri1localIndex!=-1)
			    	       {final int tri3localIndex;
			    		    if(tri1localIndex%2==0)
			    		    	tri3localIndex=tri1localIndex+1;
			    	        else
			    	        	tri3localIndex=tri1localIndex-1;
			    		    final RightTriangleInfo tri=trisList.get(tri3localIndex);
			    		    if(!arrayMap.containsKey(tri))
			    		        arrayMap.put(tri,arrayMap.get(tris[1]));
			    	       }
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
	public static Vector2[] getPrimitiveTextureCoords(final MeshData meshData,final int primitiveIndex,final int section,final int textureIndex,final Vector2[] store){
		Vector2[] result=null;
		if(meshData.getTextureBuffer(textureIndex)!=null)
		    {final int count=meshData.getPrimitiveCount(section);
	         if(primitiveIndex>=count||primitiveIndex<0)
	             throw new IndexOutOfBoundsException("Invalid primitiveIndex '"+primitiveIndex+"'.  Count is "+count);
	         final IndexMode mode = meshData.getIndexMode(section);
	         final int rSize = mode.getVertexCount();
	         result=store;
	         if(result==null||result.length<rSize)
	             result=new Vector2[rSize];
	         for(int i=0;i<rSize;i++)
	             {if(result[i]==null)
	                  result[i]=new Vector2();
	              if(meshData.getIndexBuffer()!=null)
	                  {// indexed geometry
	                   BufferUtils.populateFromBuffer(result[i],meshData.getTextureBuffer(textureIndex),
	                    meshData.getIndices().get(meshData.getVertexIndex(primitiveIndex, i, section)));
	                  }
	              else
	                  {// non-indexed geometry
	                   BufferUtils.populateFromBuffer(result[i],meshData.getTextureBuffer(textureIndex),meshData.getVertexIndex(primitiveIndex,i,section));
	                  }
	             }
	        }
		return result;
	}
	
	@SuppressWarnings("unused")
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
		//testComputeAdjacentMergeableTrisArraysList();
		AWTImageLoader.registerLoader();
		try{SimpleResourceLocator srl=new SimpleResourceLocator(CoplanarAdjacentRightTrianglesWithCanonical2DTextureCoordinatesMerger.class.getResource("/images"));
            ResourceLocatorTool.addResourceLocator(ResourceLocatorTool.TYPE_TEXTURE,srl);
           } 
        catch(final URISyntaxException urise)
        {urise.printStackTrace();}
		try{final Node levelNode=(Node)new BinaryImporter().load(CoplanarAdjacentRightTrianglesWithCanonical2DTextureCoordinatesMerger.class.getResource("/abin/LID0.abin"));
		    final Mesh mesh=(Mesh)((Node)levelNode.getChild(0)).getChild(1);
		    System.out.println("Input: "+mesh.getMeshData().getVertexCount());
		    optimize(mesh);
		    System.out.println("Output: "+mesh.getMeshData().getVertexCount());
	       }
	    catch(IOException ioe)
	    {throw new RuntimeException("level loading failed",ioe);}
	}
	
	/**
	 * Computes a list of arrays of adjacent triangles which could be merged to 
	 * make bigger rectangles
	 * 
	 * @param adjacentTrisArray 2D arrays containing adjacent triangles
	 * @return list of 2D arrays of adjacent mergeable triangles
	 */
	private static ArrayList<RightTriangleInfo[][][]> computeAdjacentMergeableTrisArraysList(final RightTriangleInfo[][][] adjacentTrisArray){
		return(new ArrayHelper().computeFullArraysFromNonFullArray(adjacentTrisArray));
	}
}
