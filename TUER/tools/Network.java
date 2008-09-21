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
import java.util.List;

public final class Network implements Serializable{
    
    
    private static final long serialVersionUID = 1L;
    
    private Full3DCell rootCell;
    
    
    public Network(){}
    
    public Network(List<Full3DCell> full3DCellsList){
        buildGraphFromList(full3DCellsList);
    }
    
    
    private final void writeObject(java.io.ObjectOutputStream out)throws IOException{
        //use BFS, write a list of cells
        if(rootCell!=null)
            {Full3DCell c;
             //First In First Out abstract data type used to store the sons of the current cell
             List<Full3DCell> fifo=new ArrayList<Full3DCell>();
             //Each cell that has been seen has to be marked to avoid an infinite loop
             List<Full3DCell> markedCellsList=new ArrayList<Full3DCell>();
             //We use the first traveled cell suggested by the user
             markedCellsList.add(rootCell);
             fifo.add(rootCell);
             while(!fifo.isEmpty())
                 {//Get the first added element as it is a FIFO (pop operation)
                  c=fifo.remove(0);
                  //This is the main treatment, write each cell
                  out.writeObject(c);
                  for(Full3DCell son:c.getNeighboursCellsList())
                      if(!markedCellsList.contains(son))
                          {//Mark the cell to avoid traveling it more than once
                           markedCellsList.add(c);
                           //Add a new cell to travel (push operation)
                           fifo.add(c);
                          }
                 }
            }
    }

    private final void readObject(java.io.ObjectInputStream in)throws IOException, ClassNotFoundException{
        //read a list of cells
        List<Full3DCell> full3DCellsList=(List<Full3DCell>)in.readObject();
        buildGraphFromList(full3DCellsList);       
    }   
    
    private final void buildGraphFromList(List<Full3DCell> full3DCellsList){
        //the first cell becomes the root cell
        rootCell=full3DCellsList.get(0);
        //compute the neighbors list of each cell in order to rebuild the network
        int i=0,j;
        //for each cell, we look for its sons
        for(Full3DCell fatherCell:full3DCellsList)
            {j=0;
             for(Full3DCell fullCell:full3DCellsList)
                 {//if the current cell is not the father cell and if it is a son of the father cell
                  if(i!=j&&Full3DCell.testNeighbourhood(fatherCell,fullCell))
                      //add the son cell into the neighbors cells list of the father cell
                      fatherCell.getNeighboursCellsList().add(fullCell);
                  j++;
                 }
             i++;
            }
    }
    
    public final Full3DCell locate(float[] point){
        return(locate(point,getRootCell()));
    }
    
    /*
     * Breadth First Search to locate the cell in which the point is.
     * BFS has been chosen because it is faster when we know that the player has gone 
     * to a close neighbor of the previous occupied cell
     */
    public static final Full3DCell locate(float[] point,Full3DCell firstTraveledCell){
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
             if(c.contains(point))
                 return(c);
             else
                 {for(Full3DCell son:c.getNeighboursCellsList())
                      if(!markedCellsList.contains(son))
                          {//Mark the cell to avoid traveling it more than once
                           markedCellsList.add(c);
                           //Add a new cell to travel (push operation)
                           fifo.add(c);
                          }
                 }
            }
        //It should NEVER HAPPEN
        //It means that you are completely outside the network
        return(null);
    }

    public final Full3DCell getRootCell(){
        return(rootCell);
    }  
}
