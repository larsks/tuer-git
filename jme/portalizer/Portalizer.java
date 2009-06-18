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
package jme.portalizer;

import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.logging.Logger;
import jme.Level;
import com.jme.math.Triangle;
import com.jme.scene.Node;
import com.jme.scene.Spatial;
import com.jme.scene.TriMesh;

/**
 * This class converts an ordinary Spatial into a forest of 
 * networks. Each network contains some cells. The cells are 
 * linked together by portals. A forest of networks forms a 
 * level. The current implementation is quite naive. It would 
 * be better to use an octal tree to sort the triangles to 
 * find them hugely faster than by using a linear search.
 * @author Julien Gouesse
 *
 */
public final class Portalizer{

    
    private Spatial ordinaryStructure;
    
    private int levelIndex;
    
    private Triangle[] tris;
    
    private static final Logger logger=Logger.getLogger(Portalizer.class.getName());
    
    
    public Portalizer(Spatial ordinaryStructure){
        this(ordinaryStructure,0);
    }
    
    public Portalizer(Spatial ordinaryStructure,int levelIndex){
        this.ordinaryStructure=ordinaryStructure;
        this.levelIndex=levelIndex;
    }
    
    
    /**
     * portalizes the scene graph
     * @return forest of networks that composes a single level
     */
    public final Level portalize(){
        //get the triangles
        initTris();
        //step 1: put the triangles into different networks                       
        //NAIVE SILLY IMPLEMENTATION BELOW
        /*
        TemporaryNetwork foundNetwork,foundNetworkForMerge=null;
        ArrayList<TemporaryNetwork> incompleteNetworksList=new ArrayList<TemporaryNetwork>();
        ArrayList<TemporaryNetwork> completeNetworksList=new ArrayList<TemporaryNetwork>();
        //for each triangle
        for(Triangle t1:tris)
            {foundNetwork=null;
             //check if an existing network can contain this triangle
             for(TemporaryNetwork network:incompleteNetworksList)
                 if(network.add(t1))
                     {foundNetwork=network;
                      break;
                     }
             //if not, create a new network containing it
             if(foundNetwork==null)
                 {foundNetwork=new TemporaryNetwork();
                  foundNetwork.add(t1);
                  incompleteNetworksList.add(foundNetwork);
                 }
             //if the network is not full, try to merge it with another one
             if(!foundNetwork.isComplete())
                 {for(TemporaryNetwork network:incompleteNetworksList)
                      if(network!=foundNetwork && network.merge(foundNetwork))
                          {foundNetworkForMerge=network;
                           break;
                          }
                  //if the merge is successful, remove the useless network
                  if(foundNetworkForMerge!=null)
                      {incompleteNetworksList.remove(foundNetworkForMerge);
                       foundNetworkForMerge=null;
                      }
                 }
             //if the network is full, remove it from the list on incomplete networks
             if(foundNetwork.isComplete())
                 {incompleteNetworksList.remove(foundNetwork);
                  completeNetworksList.add(foundNetwork);
                 }
            }
        */
        //build the adjacency map to avoid doing these computations several times
        ArrayList<Entry<ArrayList<Triangle>,ArrayList<Boolean>>> adjacencyList=new ArrayList<Entry<ArrayList<Triangle>,ArrayList<Boolean>>>();
        HashMap<Triangle,Integer> indexMap=new HashMap<Triangle,Integer>();
        ArrayList<Triangle> triList,triList2;
        int commonVertexFound;
        for(int i=0;i<tris.length;i++)
            {indexMap.put(tris[i],Integer.valueOf(i));
             adjacencyList.add(new SimpleEntry<ArrayList<Triangle>,ArrayList<Boolean>>(new ArrayList<Triangle>(),new ArrayList<Boolean>()));
            }
        boolean isConvex;
        for(int i=0;i<tris.length-1;i++)
            for(int j=i+1;j<tris.length;j++)               
                {//check if the both triangles have a common edge
                 commonVertexFound=0;
                 for(int k=0;commonVertexFound<2 && k<3;k++)
                     for(int l=0;commonVertexFound<2 && l<3;l++)
                         if(tris[i].get(k).equals(tris[j].get(l)))
                             commonVertexFound++;
                 if(commonVertexFound==2)
                     {//update the adjacency map 
                      //TODO: compute the convex flag here
                      isConvex=true;
                      triList=adjacencyList.get(i).getKey();
                      if(!triList.contains(tris[j]))
                          {triList.add(tris[j]);
                           adjacencyList.get(i).getValue().add(Boolean.valueOf(isConvex));
                          }
                      triList2=adjacencyList.get(j).getKey();
                      if(!triList2.contains(tris[i]))
                          {triList2.add(tris[i]);
                           adjacencyList.get(i).getValue().add(Boolean.valueOf(isConvex));
                          }
                     }
                }
        boolean isTriAlreadyContained;
        ArrayList<ArrayList<Triangle>> networksList=new ArrayList<ArrayList<Triangle>>();
        ArrayList<ArrayList<Triangle>> triListList=new ArrayList<ArrayList<Triangle>>(); 
        for(int i=0;i<tris.length;i++)
            {isTriAlreadyContained=false;
             //check if the triangle is already in a network
             for(ArrayList<Triangle> network:networksList)
                 if(network.contains(tris[i]))
                     {isTriAlreadyContained=true;
                      break;
                     }
             if(!isTriAlreadyContained)
                 {//create a new network
                  ArrayList<Triangle> network=new ArrayList<Triangle>();
                  networksList.add(network);
                  network.add(tris[i]);
                  triList=adjacencyList.get(i).getKey();
                  triListList.add(triList);
                  while(!triListList.isEmpty())
                      {triList=triListList.remove(0);
                       for(Triangle tri2:triList)
                           if(!network.contains(tri2))
                               {triList2=adjacencyList.get(indexMap.get(tri2).intValue()).getKey();
                                network.add(tri2);
                                triListList.add(triList2);
                               }
                      }
                 }            
            }
        //step 2: fetch the data about the triangles (texture coordinates, color, fog...)
        //step 3: put the triangles of each network into cells
        /* for(each network n)
         *     compute convex cells by comparing the normals of adjacent triangles
         *     by using the angles (acos(dot_product(v1,v2)/(norm(v1)*norm(v2))))
         * */
        ArrayList<ArrayList<ArrayList<Triangle>>> cellsList=new ArrayList<ArrayList<ArrayList<Triangle>>>();
        ArrayList<ArrayList<Triangle>> cellsForSingleNetworkList;
        ArrayList<Boolean> convexFlagsList;
        ArrayList<Triangle> convexTrisList;
        int index;
        for(ArrayList<Triangle> network:networksList)
            {//create a list that contains all lists of triangles for the cells (one list of triangles per cell)
             cellsForSingleNetworkList=new ArrayList<ArrayList<Triangle>>();
             cellsList.add(cellsForSingleNetworkList);
             for(Triangle tri:network)
                 {isTriAlreadyContained=false;
                  //check if the triangle is already in a cell
                  for(ArrayList<Triangle> cell:cellsForSingleNetworkList)
                      if(cell.contains(tri))
                          {isTriAlreadyContained=true;
                           break;
                          }
                  if(!isTriAlreadyContained)
                      {//create a new cell
                       ArrayList<Triangle> cell=new ArrayList<Triangle>();
                       cellsForSingleNetworkList.add(cell);
                       triList=new ArrayList<Triangle>();
                       triList.add(tri);
                       triListList.add(triList);
                       /*cell.add(tri);
                       index=indexMap.get(tri).intValue();
                       convexFlagsList=adjacencyList.get(index).getValue();
                       triList=adjacencyList.get(index).getKey();
                       convexTrisList=new ArrayList<Triangle>();
                       //use only triangles that form a convex cell
                       for(int m=0;m<triList.size();m++)
                           if(convexFlagsList.get(m).booleanValue())
                               convexTrisList.add(triList.get(m));
                       triListList.add(convexTrisList);*/
                       while(!triListList.isEmpty())
                           {triList=triListList.remove(0);
                            for(Triangle tri2:triList)
                                if(!cell.contains(tri2))
                                    {cell.add(tri2);
                                     index=indexMap.get(tri2).intValue();
                                     //get the list of convex flags
                                     convexFlagsList=adjacencyList.get(index).getValue();
                                     triList2=adjacencyList.get(index).getKey();
                                     convexTrisList=new ArrayList<Triangle>();
                                     //use only triangles that form a convex cell
                                     for(int m=0;m<triList2.size();m++)
                                         if(convexFlagsList.get(m).booleanValue())
                                             convexTrisList.add(triList2.get(m));
                                     triListList.add(convexTrisList);
                                    }
                           }
                      }
                 }
            }
        //step 4: convert the temporary structure into the definitive format
        Level level=new Level(levelIndex);
        //convert temporary networks into real networks
        //convert temporary cells into real cells
        return(level);
    }
    
    /**
     * looks for triangles recursively in the ordinary
     * structure
     */
    private final void initTris(){
        ArrayList<Triangle> trisList=getTris(ordinaryStructure);       
        tris=trisList.toArray(new Triangle[trisList.size()]);
    }
    
    private final ArrayList<Triangle> getTris(Spatial spatial){
        ArrayList<Triangle> trisList=new ArrayList<Triangle>();
        if(spatial instanceof Node)
            {Node node=(Node)spatial;
             if(node.getChildren()!=null)
                 {for(Spatial child:node.getChildren())
                      trisList.addAll(getTris(child));
                 }
            }
        else
            if(spatial instanceof TriMesh)
                trisList.addAll(Arrays.asList(((TriMesh)spatial).getMeshAsTriangles(null)));
                //TODO: store other information (normals, colors, texture coordinates, ...)
            else
                logger.warning("unsupported geometry, only TriMesh instances are supported!");
        return(trisList);
    }
    /*
    private static final class TemporaryNetwork{
        
        
        private ArrayList<Triangle> trisList;
        
        private ArrayList<Map.Entry<Vector3f,Vector3f>> availableEdgesList;
        
        
        private TemporaryNetwork(){
            trisList=new ArrayList<Triangle>();
            availableEdgesList=new ArrayList<Map.Entry<Vector3f,Vector3f>>();
        }
        
        private final boolean add(Triangle triangle){
            boolean success;
            if(trisList.isEmpty())
                {//store its 3 edges in the list
                 availableEdgesList.add(new AbstractMap.SimpleEntry<Vector3f,Vector3f>(triangle.get(0),triangle.get(1)));
                 availableEdgesList.add(new AbstractMap.SimpleEntry<Vector3f,Vector3f>(triangle.get(1),triangle.get(2)));
                 availableEdgesList.add(new AbstractMap.SimpleEntry<Vector3f,Vector3f>(triangle.get(2),triangle.get(0)));
                 success=true;
                }
            else
                {ArrayList<Map.Entry<Vector3f,Vector3f>> commonEdgesList=new ArrayList<Map.Entry<Vector3f,Vector3f>>();
                 boolean found;
                 for(Map.Entry<Vector3f,Vector3f> edge:availableEdgesList)
                     {//check if the both triangle have a common edge
                      found=false;
                      for(int i=0;!found && i<3;i++)
                          if(edge.getKey().equals(triangle.get(i)))
                              found=true;
                      if(found)
                          {found=false;
                           for(int i=0;!found && i<3;i++)
                               if(edge.getValue().equals(triangle.get(i)))
                                   found=true;
                           if(found)
                               commonEdgesList.add(edge);
                          }
                     }
                 if(!commonEdgesList.isEmpty())
                     {//keep all edges of the triangle that are not in commonEdgesList            
                      Map.Entry<Vector3f,Vector3f> triangleEdge;
                      for(int i=0;i<3;i++)
                          {found=false;
                           triangleEdge=new AbstractMap.SimpleEntry<Vector3f,Vector3f>(triangle.get(i),triangle.get((i+1)%3));
                           for(Map.Entry<Vector3f,Vector3f> edge:commonEdgesList)
                               if((edge.getKey().equals(triangleEdge.getKey()) && edge.getValue().equals(triangleEdge.getValue()))||
                                  (edge.getKey().equals(triangleEdge.getValue()) && edge.getValue().equals(triangleEdge.getKey())))
                                   {found=true;
                                    break;
                                   }
                           if(!found)
                               availableEdgesList.add(triangleEdge);
                          }
                      //remove the common edges from the list
                      availableEdgesList.removeAll(commonEdgesList);
                      success=true;
                     }
                 else
                     success=false;                
                }
            if(success)
                trisList.add(triangle);
            return(success);
        }
        
        private final boolean isComplete(){
            return(!trisList.isEmpty()&&availableEdgesList.isEmpty());
        }
        
        private final boolean merge(TemporaryNetwork network){
            boolean success=false;
            ArrayList<Triangle> remainingTris=new ArrayList<Triangle>();
            for(Triangle t:network.trisList)
                if(add(t))
                    {if(!success)
                        success=true;
                    }
                else
                    remainingTris.add(t);
            //if a merge is possible, add the remaining triangles 
            //into the resulting network
            if(success)
                for(Triangle t:remainingTris)
                    if(!add(t))
                        logger.warning("the addition of a triangle already linked to the network failed!");
            network.trisList.clear();
            return(success);
        }
    }*/
}
