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
package tools;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class Network implements Serializable{
    
    
    private static final long serialVersionUID = 1L;
    
    private Full3DCell rootCell;
    
    private List<Full3DCell> cellsList;
    
    private transient NetworkController controller;
    
    
    public Network(){}
    
    /**
     * Build a connected graph
     * @param full3DCellsList list of connected cells
     */
    public Network(List<Full3DCell> full3DCellsList){
        buildGraphFromList(full3DCellsList);
    }
    
    
    private final void writeObject(java.io.ObjectOutputStream out)throws IOException{
        out.writeObject(cellsList);
    }

    private final void readObject(java.io.ObjectInputStream in)throws IOException, ClassNotFoundException{
        //read a list of cells
        List<Full3DCell> full3DCellsList=(List<Full3DCell>)in.readObject();
        buildGraphFromList(full3DCellsList);       
    }   
    
    private final void buildGraphFromList(List<Full3DCell> full3DCellsList){
        cellsList=full3DCellsList;              
        //the first cell becomes the root cell
        if(!full3DCellsList.isEmpty())
            {rootCell=full3DCellsList.get(0);
             connectCellsTogether(full3DCellsList);
            }
    }
    
    static final void connectCellsTogether(List<Full3DCell> full3DCellsList){
        float[] portal0,portal1,portal2,portal3;
        float[] c2portal0,c2portal1,c2portal2,c2portal3;
        Full3DCell neighbourCell;
        for(Full3DCell cell:full3DCellsList)
            {//compare its left portals to other right portals
             for(int i=0;i<cell.getLeftPortals().size();i+=4)
                {//get each portal
                 portal0=cell.getLeftPortals().get(i);
                 portal1=cell.getLeftPortals().get(i+1);
                 portal2=cell.getLeftPortals().get(i+2);
                 portal3=cell.getLeftPortals().get(i+3);
                 //copy it into the list of portals
                 cell.addNeighbourPortal(portal0);
                 cell.addNeighbourPortal(portal1);
                 cell.addNeighbourPortal(portal2);
                 cell.addNeighbourPortal(portal3);
                 //look for the neighbor cell
                 neighbourCell=null;
                 for(Full3DCell cell2:full3DCellsList)
                     if(cell!=cell2)
                         {for(int j=0;j<cell2.getRightPortals().size()&&neighbourCell==null;j+=4)
                              {//get each portal
                               c2portal0=cell2.getRightPortals().get(j);
                               c2portal1=cell2.getRightPortals().get(j+1);
                               c2portal2=cell2.getRightPortals().get(j+2);
                               c2portal3=cell2.getRightPortals().get(j+3);
                               if((Arrays.equals(portal0,c2portal0)||
                                   Arrays.equals(portal0,c2portal1)||
                                   Arrays.equals(portal0,c2portal2)||
                                   Arrays.equals(portal0,c2portal3))&&
                                  (Arrays.equals(portal1,c2portal0)||
                                   Arrays.equals(portal1,c2portal1)||
                                   Arrays.equals(portal1,c2portal2)||
                                   Arrays.equals(portal1,c2portal3)&&
                                  (Arrays.equals(portal2,c2portal0)||
                                   Arrays.equals(portal2,c2portal1)||
                                   Arrays.equals(portal2,c2portal2)||
                                   Arrays.equals(portal2,c2portal3)&&
                                  (Arrays.equals(portal3,c2portal0)||
                                   Arrays.equals(portal3,c2portal1)||
                                   Arrays.equals(portal3,c2portal2)||
                                   Arrays.equals(portal3,c2portal3)))))
                                   neighbourCell=cell2;                                 
                              }
                          if(neighbourCell!=null)
                              break;
                         }
                 if(neighbourCell!=null)
                     cell.addNeighbourCell(neighbourCell);
                     //don't apply the reciprocal operation because it would cause
                     //a problem in the indexing of portals with cells
                 else
                     System.out.println("ORPHANED PORTAL");
                }
             //compare its right portals to other left portals
             for(int i=0;i<cell.getRightPortals().size();i+=4)
                 {//get each portal
                  portal0=cell.getRightPortals().get(i);
                  portal1=cell.getRightPortals().get(i+1);
                  portal2=cell.getRightPortals().get(i+2);
                  portal3=cell.getRightPortals().get(i+3);
                  //copy it into the list of portals
                  cell.addNeighbourPortal(portal0);
                  cell.addNeighbourPortal(portal1);
                  cell.addNeighbourPortal(portal2);
                  cell.addNeighbourPortal(portal3);
                  //look for the neighbor cell
                  neighbourCell=null;
                  for(Full3DCell cell2:full3DCellsList)
                      if(cell!=cell2)
                          {for(int j=0;j<cell2.getLeftPortals().size()&&neighbourCell==null;j+=4)
                               {//get each portal
                                c2portal0=cell2.getLeftPortals().get(j);
                                c2portal1=cell2.getLeftPortals().get(j+1);
                                c2portal2=cell2.getLeftPortals().get(j+2);
                                c2portal3=cell2.getLeftPortals().get(j+3);
                                if((Arrays.equals(portal0,c2portal0)||
                                    Arrays.equals(portal0,c2portal1)||
                                    Arrays.equals(portal0,c2portal2)||
                                    Arrays.equals(portal0,c2portal3))&&
                                   (Arrays.equals(portal1,c2portal0)||
                                    Arrays.equals(portal1,c2portal1)||
                                    Arrays.equals(portal1,c2portal2)||
                                    Arrays.equals(portal1,c2portal3)&&
                                   (Arrays.equals(portal2,c2portal0)||
                                    Arrays.equals(portal2,c2portal1)||
                                    Arrays.equals(portal2,c2portal2)||
                                    Arrays.equals(portal2,c2portal3)&&
                                   (Arrays.equals(portal3,c2portal0)||
                                    Arrays.equals(portal3,c2portal1)||
                                    Arrays.equals(portal3,c2portal2)||
                                    Arrays.equals(portal3,c2portal3)))))
                                    neighbourCell=cell2;                                 
                               }
                           if(neighbourCell!=null)
                               break;
                          }
                  if(neighbourCell!=null)
                      cell.addNeighbourCell(neighbourCell);
                      //don't apply the reciprocal operation because it would cause
                      //a problem in the indexing of portals with cells
                  else
                      System.out.println("ORPHANED PORTAL");
                 }
             //compare its top portals to other bottom portals
             for(int i=0;i<cell.getTopPortals().size();i+=4)
                 {//get each portal
                  portal0=cell.getTopPortals().get(i);
                  portal1=cell.getTopPortals().get(i+1);
                  portal2=cell.getTopPortals().get(i+2);
                  portal3=cell.getTopPortals().get(i+3);
                  //copy it into the list of portals
                  cell.addNeighbourPortal(portal0);
                  cell.addNeighbourPortal(portal1);
                  cell.addNeighbourPortal(portal2);
                  cell.addNeighbourPortal(portal3);
                  //look for the neighbor cell
                  neighbourCell=null;
                  for(Full3DCell cell2:full3DCellsList)
                      if(cell!=cell2)
                          {for(int j=0;j<cell2.getBottomPortals().size()&&neighbourCell==null;j+=4)
                               {//get each portal
                                c2portal0=cell2.getBottomPortals().get(j);
                                c2portal1=cell2.getBottomPortals().get(j+1);
                                c2portal2=cell2.getBottomPortals().get(j+2);
                                c2portal3=cell2.getBottomPortals().get(j+3);
                                if((Arrays.equals(portal0,c2portal0)||
                                    Arrays.equals(portal0,c2portal1)||
                                    Arrays.equals(portal0,c2portal2)||
                                    Arrays.equals(portal0,c2portal3))&&
                                   (Arrays.equals(portal1,c2portal0)||
                                    Arrays.equals(portal1,c2portal1)||
                                    Arrays.equals(portal1,c2portal2)||
                                    Arrays.equals(portal1,c2portal3)&&
                                   (Arrays.equals(portal2,c2portal0)||
                                    Arrays.equals(portal2,c2portal1)||
                                    Arrays.equals(portal2,c2portal2)||
                                    Arrays.equals(portal2,c2portal3)&&
                                   (Arrays.equals(portal3,c2portal0)||
                                    Arrays.equals(portal3,c2portal1)||
                                    Arrays.equals(portal3,c2portal2)||
                                    Arrays.equals(portal3,c2portal3)))))
                                    neighbourCell=cell2;                                 
                               }
                           if(neighbourCell!=null)
                               break;
                          }
                  if(neighbourCell!=null)
                      cell.addNeighbourCell(neighbourCell);
                      //don't apply the reciprocal operation because it would cause
                      //a problem in the indexing of portals with cells
                  else
                      System.out.println("ORPHANED PORTAL");
                 }
             //compare its bottom portals to other top portals
             for(int i=0;i<cell.getBottomPortals().size();i+=4)
                 {//get each portal
                  portal0=cell.getBottomPortals().get(i);
                  portal1=cell.getBottomPortals().get(i+1);
                  portal2=cell.getBottomPortals().get(i+2);
                  portal3=cell.getBottomPortals().get(i+3);
                  //copy it into the list of portals
                  cell.addNeighbourPortal(portal0);
                  cell.addNeighbourPortal(portal1);
                  cell.addNeighbourPortal(portal2);
                  cell.addNeighbourPortal(portal3);
                  //look for the neighbor cell
                  neighbourCell=null;
                  for(Full3DCell cell2:full3DCellsList)
                      if(cell!=cell2)
                          {for(int j=0;j<cell2.getTopPortals().size()&&neighbourCell==null;j+=4)
                               {//get each portal
                                c2portal0=cell2.getTopPortals().get(j);
                                c2portal1=cell2.getTopPortals().get(j+1);
                                c2portal2=cell2.getTopPortals().get(j+2);
                                c2portal3=cell2.getTopPortals().get(j+3);
                                if((Arrays.equals(portal0,c2portal0)||
                                    Arrays.equals(portal0,c2portal1)||
                                    Arrays.equals(portal0,c2portal2)||
                                    Arrays.equals(portal0,c2portal3))&&
                                   (Arrays.equals(portal1,c2portal0)||
                                    Arrays.equals(portal1,c2portal1)||
                                    Arrays.equals(portal1,c2portal2)||
                                    Arrays.equals(portal1,c2portal3)&&
                                   (Arrays.equals(portal2,c2portal0)||
                                    Arrays.equals(portal2,c2portal1)||
                                    Arrays.equals(portal2,c2portal2)||
                                    Arrays.equals(portal2,c2portal3)&&
                                   (Arrays.equals(portal3,c2portal0)||
                                    Arrays.equals(portal3,c2portal1)||
                                    Arrays.equals(portal3,c2portal2)||
                                    Arrays.equals(portal3,c2portal3)))))
                                    neighbourCell=cell2;                                 
                               }
                           if(neighbourCell!=null)
                               break;
                          }
                  if(neighbourCell!=null)
                      cell.addNeighbourCell(neighbourCell);
                      //don't apply the reciprocal operation because it would cause
                      //a problem in the indexing of portals with cells
                  else
                      System.out.println("ORPHANED PORTAL");
                 }
            }
    }
    
    public final List<Full3DCell> getCellsList(){
        return(cellsList);
    }
    
    final Full3DCell locate(float x,float y,float z){
        return(locate(x,y,z,rootCell));
    }
    
    /*
     * Breadth First Search to locate the cell in which the point is.
     * BFS has been chosen because it is faster when we know that the player has gone 
     * to a close neighbor of the previous occupied cell
     */
    final Full3DCell locate(float x,float y,float z,Full3DCell firstTraveledCell){
        Full3DCell c;
        //First In First Out abstract data type used to store the sons of the current cell
        List<Full3DCell> fifo=new ArrayList<Full3DCell>();
        //Each cell that has been seen has to be marked to avoid an infinite loop
        List<Full3DCell> markedCellsList=new ArrayList<Full3DCell>();
        //We use the first traveled cell suggested by the user
        markedCellsList.add(firstTraveledCell);
        fifo.add(firstTraveledCell);
        while(!fifo.isEmpty())
            {//Get the first added element as it is a FIFO (pop operation)
             c=fifo.remove(0);
             //This is the main treatment; if the point is in the cell, the travel ends            
             if(c.contains(x,y,z))
                 return(c);
             else
                 {for(Full3DCell son:c.getNeighboursCellsList())
                      if(!markedCellsList.contains(son))
                          {//Mark the cell to avoid traveling it more than once
                           markedCellsList.add(son);
                           //Add a new cell to travel (push operation)
                           fifo.add(son);
                          }
                 }
            }
        //It means that you are completely outside the network
        return(null);
    } 
    
    public final Full3DCell getRootCell(){
        return(rootCell);
    }

    public final void setRootCell(Full3DCell rootCell){
        this.rootCell=rootCell;
    }

    public final NetworkController getController(){
        return(controller);
    }

    public final void setController(NetworkController controller){
        this.controller=controller;
    }

    public final void setCellsList(List<Full3DCell> cellsList){
        this.cellsList=cellsList;
    }  
}
