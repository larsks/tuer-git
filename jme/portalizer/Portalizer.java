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
import jme.Network;
import jme.Cell;
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
 * TODO:
 *       - use the broadest surface including the edge delimitating 
 *       2 triangles that don't form a convex polyedron in order to
 *       make a portal
 *       - find a good way to check the convexity (use both cos and sin?)
 *       - instantiate the portals
 *       - handle other data (textures, normals, ...)
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
        //build the networks
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
        //TODO: fill the portals as the edges that link triangles that cannot compose a convex polyedron
        //must be linked to some other edges to form complete portals
        //step 2: fetch the data about the triangles (texture coordinates, color, fog...)
        //step 3: put the triangles of each network into cells
        /* for(each network n)
         *     compute convex cells by comparing the normals of adjacent triangles
         *     by using the angles (acos(dot_product(v1,v2)/(norm(v1)*norm(v2))))
         * */
        //build the cells
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
                                     //TODO: else
                                     //          portalTrisList.add(triList2.get(m));
                                     triListList.add(convexTrisList);
                                     //TODO
                                     //for(Triangle portalTri:portalTrisList)
                                     //    if this portal is not in the list of portals of this network
                                     //        create it
                                     //        add it into the list of portals of this network
                                     //    if this cell is not in this portal
                                     //        add this cell to this portal
                                     //portalTrisList.clear();
                                    }
                           }
                      }
                 }
            }
        //TODO: check all portals; if any portal lacks a linked cell, attach a void cell to it
        //step 4: convert the temporary structure into the definitive format
        Level level=new Level(levelIndex);
        int networkIndex=0,cellIndex;
        Network network;
        Cell cell;
        TriMesh cellMesh=null;
        //convert temporary networks into real networks
        for(ArrayList<ArrayList<Triangle>> cellsForOneNetworkList:cellsList)
            {network=new Network(levelIndex,networkIndex);
             level.attachChild(network);
             cellIndex=0;
             for(ArrayList<Triangle> trianglesForOneCellList:cellsForOneNetworkList)
                 {//TODO: recompose the TriMesh instance
                  cell=new Cell(levelIndex,networkIndex,cellIndex,cellMesh);
                  network.attachChild(cell);
                  //TODO: add the portals
                  cellIndex++;
                 }
             networkIndex++;
            }
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
}
