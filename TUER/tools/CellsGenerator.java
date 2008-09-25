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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Vector;
import java.util.Collections;
import java.awt.Color;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

public final class CellsGenerator{
    
    
    public final static Network generate(List<PointPair> topWallPiecesList,
                                      List<PointPair> bottomWallPiecesList,
                                      List<PointPair> leftWallPiecesList,
			                          List<PointPair> rightWallPiecesList,
			                          List<PointPair> artTopWallPiecesList,
                                      List<PointPair> artBottomWallPiecesList,
                                      List<PointPair> artLeftWallPiecesList,
                                      List<PointPair> artRightWallPiecesList){
        PointPairComparator ppc=new PointPairComparator(PointPairComparator.VERTICAL_HORIZONTAL_SORT);       
        Vector<PointPair> wholeTopWallPiecesList=new Vector<PointPair>();
        wholeTopWallPiecesList.addAll(topWallPiecesList);
        wholeTopWallPiecesList.addAll(artTopWallPiecesList);
        Collections.sort(wholeTopWallPiecesList,ppc);
        Vector<PointPair> wholeBottomWallPiecesList=new Vector<PointPair>();
        wholeBottomWallPiecesList.addAll(bottomWallPiecesList);
        wholeBottomWallPiecesList.addAll(artBottomWallPiecesList);        
        Collections.sort(wholeBottomWallPiecesList,ppc);
        Vector<PointPair> wholeLeftWallPiecesList=new Vector<PointPair>();
        wholeLeftWallPiecesList.addAll(leftWallPiecesList);
        wholeLeftWallPiecesList.addAll(artLeftWallPiecesList);        
        Collections.sort(wholeLeftWallPiecesList,ppc);
        Vector<PointPair> wholeRightWallPiecesList=new Vector<PointPair>();
        wholeRightWallPiecesList.addAll(rightWallPiecesList);
        wholeRightWallPiecesList.addAll(artRightWallPiecesList);        
        Collections.sort(wholeRightWallPiecesList,ppc);       
        Vector<PointPair> topFullWallList=mergeAllWallPieces(wholeTopWallPiecesList);
        Vector<PointPair> bottomFullWallList=mergeAllWallPieces(wholeBottomWallPiecesList);
        Vector<PointPair> leftFullWallList=mergeAllWallPieces(wholeLeftWallPiecesList);
        Vector<PointPair> rightFullWallList=mergeAllWallPieces(wholeRightWallPiecesList);
    	Collections.sort(topFullWallList,ppc);
    	Collections.sort(bottomFullWallList,ppc);
    	Collections.sort(leftFullWallList,ppc);
    	Collections.sort(rightFullWallList,ppc);
    	Vector<Cell> cellsList=new Vector<Cell>();
    	Vector<Portal> portalsList=new Vector<Portal>();
    	generateRawCellsAndPortals(topFullWallList,bottomFullWallList,leftFullWallList,rightFullWallList,cellsList,portalsList);
    	optimizeRawCellsAndPortals(cellsList);  
    	List<Full3DCell> full3DCellsList=convertFromRawTo3DCellsAndPortals(cellsList,
    	        topWallPiecesList,bottomWallPiecesList,
                leftWallPiecesList,rightWallPiecesList,
                TilesGenerator.factor);
    	System.out.println("SIZE : "+cellsList.size());
    	createCellsMap(cellsList);
    	List<Cell> overlappingCellsList=getOverlappingCellsList(cellsList);
    	if(!overlappingCellsList.isEmpty())
    	    {System.out.println(overlappingCellsList.size()+" OVERLAPPING CELLS:");
    	     for(Cell overlappingCell:overlappingCellsList)
    	         System.out.println(overlappingCell);
    	    }
    	else
    	    System.out.println("NO OVERLAPPING CELLS");
    	if(testNetworkIntegrity(cellsList,topFullWallList,
    	        bottomFullWallList,leftFullWallList,rightFullWallList))
    	    {System.out.println("network integrity preserved");
    	     
    	    }
    	else
    	    {System.out.println("network integrity NOT preserved!!!");
    	      
    	    }
    	if(testCellCompleteness(cellsList))
    	    System.out.println("cells integrity preserved");
    	else
    	    System.out.println("cells integrity not preserved!!!");
    	//build the network from the list of cells
    	System.out.println("[start] network construction");
    	Network network=new Network(full3DCellsList);
    	System.out.println("[end] network construction");
    	return(network);
    }
    
    private static final List<Cell> getOverlappingCellsList(List<Cell> cellsList){
        List<Cell> overlappingCellsList=new ArrayList<Cell>();
        for(Cell cell:cellsList)
            for(Cell c2:cellsList)
                if(cell!=c2&&cell.getEnclosingRectangle().intersects(c2.getEnclosingRectangle()))
                    {if(!overlappingCellsList.contains(cell))
                        overlappingCellsList.add(cell);
                     if(!overlappingCellsList.contains(c2))
                         overlappingCellsList.add(c2);
                    }
        return(overlappingCellsList);
    }
    
    //merge all pieces of wall to build the "temporary" full walls
    final static Vector<PointPair> mergeAllWallPieces(List<PointPair> wallPiecesList){
    	Vector<PointPair> temporaryFullWallsList = new Vector<PointPair>();
    	boolean found;
    	for(PointPair currentWallPiece:wallPiecesList)
    	    {//assume that we don't find a linked wall
    		 found = false;
    		 //look for a temporary full wall that can be linked to this piece
    		 for(PointPair currentTempFullWall:temporaryFullWallsList)
    			 {//check if the both pieces are linked together physically
    			  //warning! we assume these pieces are aligned
    			  if(currentTempFullWall.isLinkedTo(currentWallPiece))
    			      {//merge the both pieces and update the temporary full wall
    			       currentTempFullWall.merge(currentWallPiece);
    			       //if the link works, update the flag
    				   found=true;
    				   break;
    			      }
    			 }
    		 if(!found)
    		     {//add this piece as a new temporary full wall
    			  temporaryFullWallsList.add(currentWallPiece);
    		     }
    	    }
    	return(temporaryFullWallsList);
    }
    
    @SuppressWarnings("unused")
    private final static List<PointPair> ALTERNATIVEmergeAllWallPieces(List<PointPair> wallPiecesList){       
        List<PointPair> atomicWallsList = getAtomicWallsList(wallPiecesList);
        Vector<PointPair> fullWallsList = new Vector<PointPair>();
        //merge atomic walls together when possible
        if(atomicWallsList.size()>0)
            {Vector<PointPair> tmpWallsList = new Vector<PointPair>();
             tmpWallsList.add(atomicWallsList.get(0));
             PointPair p;
             for(int i=1;i<atomicWallsList.size();i++)
                 {p=atomicWallsList.get(i);                
                  if(!p.isLinkedTo(tmpWallsList.lastElement()))
                      {fullWallsList.add(new PointPair(tmpWallsList.firstElement().getFirst(),tmpWallsList.lastElement().getLast()));
                       tmpWallsList.clear();                       
                      }
                  tmpWallsList.add(p);
                 }
             if(!tmpWallsList.isEmpty())
                 {//merge them
                  //add them into fullWallsList
                  fullWallsList.add(new PointPair(tmpWallsList.firstElement().getFirst(),tmpWallsList.lastElement().getLast()));
                  tmpWallsList.clear();
                 }
            }
        return(fullWallsList);
    }
    
    private final static List<PointPair> getAtomicWallsList(List<PointPair> wallPiecesList){
        Vector<PointPair> atomicWallsList = new Vector<PointPair>();
      //break all walls into atomic walls
        for(PointPair currentWallPiece:wallPiecesList)
            if(currentWallPiece.getFirst().x==currentWallPiece.getLast().x)
                {int min=Math.min(currentWallPiece.getFirst().y,currentWallPiece.getLast().y);
                 int max=Math.max(currentWallPiece.getFirst().y,currentWallPiece.getLast().y);
                 for(int i=min;i<max;i++)
                     atomicWallsList.add(new PointPair(
                             new Point(currentWallPiece.getFirst().x,i),
                             new Point(currentWallPiece.getFirst().x,i+1)));
                }
            else
                if(currentWallPiece.getFirst().y==currentWallPiece.getLast().y)
                    {int min=Math.min(currentWallPiece.getFirst().x,currentWallPiece.getLast().x);
                     int max=Math.max(currentWallPiece.getFirst().x,currentWallPiece.getLast().x);
                     for(int i=min;i<max;i++)
                         atomicWallsList.add(new PointPair(
                                new Point(i,currentWallPiece.getFirst().y),
                                new Point(i+1,currentWallPiece.getFirst().y)));             
                    }
        Collections.sort(atomicWallsList,new PointPairComparator(PointPairComparator.VERTICAL_HORIZONTAL_SORT));
        return(atomicWallsList);
    }
    
    //create unoptimized 2D cells and portals (cells excessively small and portals excessively wide)
    private final static void generateRawCellsAndPortals(Vector<PointPair> topFullWallList,
                                                         Vector<PointPair> bottomFullWallList,
							 Vector<PointPair> leftFullWallList,
							 Vector<PointPair> rightFullWallList,
							 Vector<Cell> cellsList,
							 Vector<Portal> portalsList){
    	Vector<Cell> fullWallCell=new Vector<Cell>();
    	PointPair topFullWallPiece=new PointPair();	
    	PointPair bottomFullWallPiece;
    	boolean found;
    	//build cells with vertical walls
    	for(PointPair topFullWall:topFullWallList)
    	    {//reorder the point pair to be sure that the first point
    		 //is the "leftmost" point inside the pair
    		 if(topFullWall.getFirst().getX()>topFullWall.getLast().getX())
    			 topFullWall.swap();
    		 topFullWallPiece.set(topFullWall.getFirst(),new Point((int)(topFullWall.getFirst().getX()+1),
    				(int)topFullWall.getFirst().getY()));
    		 //split the full wall into small pieces
    		 for(int i=0;i<topFullWall.getSize();i++)
    		     {//look for the closest piece of the symmetric type
    			  bottomFullWallPiece=null;
                  Vector<PointPair> revertOrderBottomFullWallList=new Vector<PointPair>();
                  for(PointPair bottomFullWall:bottomFullWallList)
                      revertOrderBottomFullWallList.add(0,bottomFullWall);                 
    			  for(PointPair bottomFullWall:revertOrderBottomFullWallList)
    			      {if(bottomFullWall.getFirst().getX()>bottomFullWall.getLast().getX())
    				       bottomFullWall.swap();	       
    			       //check if the abscissa is contained in the bottom full wall
    			       if(topFullWallPiece.getFirst().y > bottomFullWall.getFirst().y &&
    			    	  topFullWallPiece.getFirst().getX()>=bottomFullWall.getFirst().getX() &&
    					  topFullWallPiece.getFirst().getX()<=bottomFullWall.getLast().getX() &&
    					  topFullWallPiece.getLast().getX()>=bottomFullWall.getFirst().getX() &&
    					  topFullWallPiece.getLast().getX()<=bottomFullWall.getLast().getX()
    			         )
    			           {//build the bottom wall piece which matches 
    				        //with the top full wall piece
    				        bottomFullWallPiece=new PointPair();
    				        bottomFullWallPiece.set((int)topFullWallPiece.getFirst().getX(),
    						(int)bottomFullWall.getFirst().getY(),
    						(int)topFullWallPiece.getLast().getX(),
    						(int)bottomFullWall.getFirst().getY());
    				        break;
    			           }
    			      }
    			  if(bottomFullWallPiece!=null)
    			      {//if possible, add these pieces to an existing cell
    				   found=false;
    				   for(Cell currentCell:fullWallCell)
    					   for(int j=0;j<currentCell.getTopWalls().size();j++)
    						   //check if these couples of pieces are aligned
    						   if(currentCell.getTopWall(j).getFirst().getY()==topFullWallPiece.getFirst().getY() &&
    							  currentCell.getBottomWall(j).getFirst().getY()==bottomFullWallPiece.getFirst().getY()
    						     )
    						       if(currentCell.getTopWall(j).isLinkedTo(topFullWallPiece) &&
    						    	  currentCell.getBottomWall(j).isLinkedTo(bottomFullWallPiece)
    						         )
    						           {currentCell.addTopWall(new PointPair(topFullWallPiece));
    						            currentCell.addBottomWall(new PointPair(bottomFullWallPiece));
    						            currentCell.mergeTopWalls();
    						            currentCell.mergeBottomWalls();
    						            found=true;
    						            break;
    						           }
    				           //otherwise, add these pieces to a new cell and add this cell to the list
    				    if(!found)
    				        {Cell cell=new Cell();
    				         cell.addTopWall(new PointPair(topFullWallPiece));
    				         cell.addBottomWall(new PointPair(bottomFullWallPiece));
    				         fullWallCell.add(cell);
    				        }
    			       }
    			   else
    			       {//very problematic case
    				    //what can we do???
    				    //use an handler (cells consider outside?) for orphaned walls??
    				    //use Nan in the coordinates of the "invalid" point pair
    			       }
    			   //update the position of the piece of full wall
    			   topFullWallPiece.translate(1,0);
    		      }
    		  cellsList.addAll(fullWallCell);
    		  fullWallCell.clear();
    	     }
    	 //add horizontal walls to previously build cells
	     for(Cell currentCell:cellsList)
	         {//get the abscissa
	    	  int x=(int)currentCell.getBottomWall(0).getFirst().getX();
	    	  //for all holes at the left side
	    	  for(int i=(int)currentCell.getBottomWall(0).getFirst().getY();
	              i<(int)currentCell.getTopWall(0).getFirst().getY();i++)
	              {PointPair candidate = new PointPair(new Point(x,i),new Point(x,i+1));
	               //look for a candidate
	               found=false;
	               for(PointPair po:leftFullWallList)
	                   {if(po.getFirst().y>po.getLast().y)
	                	    po.swap();
	            	    if(po.getFirst().x==candidate.getFirst().x &&
	            	       candidate.getFirst().y >= po.getFirst().y &&
	            	       candidate.getFirst().y <= po.getLast().y &&
	            	       candidate.getLast().y >= po.getFirst().y &&
	            	       candidate.getLast().y <= po.getLast().y
                          )
	                        {found=true;
            	             break;
                            }
	                   }
	               if(found)
	                   {//add this candidate to the left wall
	            	    currentCell.addLeftWall(candidate);
	            	    //merge it with others if possible
	    			    currentCell.mergeLeftWalls();
	                   }
	    		   else
	    		       {//add this candidate to the left portals
	    			    currentCell.addLeftPortal(candidate);
	    			    //merge it with the others if possible
	    		        currentCell.mergeLeftPortals();
	    		       }
	              }	    	  	    	  	    	      	    	     	    	                   
	    	  x=(int)currentCell.getBottomWall(0).getLast().getX();
	    	  //for all holes at the right side
	    	  for(int i=(int)currentCell.getBottomWall(0).getLast().getY();
              i<(int)currentCell.getTopWall(0).getLast().getY();i++)
                  {PointPair candidate = new PointPair(new Point(x,i),new Point(x,i+1));
                   //look for a candidate
                   found=false;
                   for(PointPair po:rightFullWallList)
                       {if(po.getFirst().y>po.getLast().y)
               	            po.swap();
           	            if(po.getFirst().x==candidate.getFirst().x &&
           	               candidate.getFirst().y >= po.getFirst().y &&
           	               candidate.getFirst().y <= po.getLast().y &&
           	               candidate.getLast().y >= po.getFirst().y &&
           	               candidate.getLast().y <= po.getLast().y
                          )
                           {found=true;
       	                    break;
                           }
                       }
                   if(found)    
                       {//add this candidate to the right wall
            	        currentCell.addRightWall(candidate);
            	        //merge it with others if possible
    			        currentCell.mergeRightWalls();
                       }
    		       else
    		           {//add this candidate to the right portals
    			        currentCell.addRightPortal(candidate);
    			        //merge it with the others if possible
    		            currentCell.mergeRightPortals();
    		           }
                  }	    
	         }
    }
    
    //resize 2D cells and portals so that the average width of the portals decreases 
    //FIXME: when merging some cells, they overlap
    private final static void optimizeRawCellsAndPortals(Vector<Cell> cellsList){
        PointPair leftPortal,rightPortal;
        Cell mergedResultCell=null;
        Vector<Cell> garbageCells=new Vector<Cell>();
        Vector<Cell> additionalMergedCells=new Vector<Cell>();
        boolean found,mustRestart=false,bottomLinkFound,topLinkFound;
    	for(Cell c1:cellsList)
    	    {if(mustRestart)
	    	     break;
    		 for(Cell c2:cellsList)
    		     {if(mustRestart)
		    	      break;
    		      //if the both cells are different, with one wall on the bottom and 
    		      //one wall on the top, aligned and symmetrically "opened"
    			  if(c1!=c2 && c1.getTopWalls().size()>=1 && c2.getTopWalls().size()>=1 &&
    				 c1.getBottomWalls().size()>=1 && c2.getBottomWalls().size()>=1 &&	  
    				 c1.getBottomWall(0).getFirst().y==c2.getBottomWall(0).getFirst().y &&
    		    	 c1.getTopWall(0).getFirst().y==c2.getTopWall(0).getFirst().y)
    			      {if(c1.getTopWall(0).getLast().x<c2.getTopWall(0).getFirst().x && 
       		    		  c1.getRightWalls().isEmpty() && c2.getLeftWalls().isEmpty())
    			           {leftPortal=c1.getRightPortal(0);
    			    	    rightPortal=c2.getLeftPortal(0);
    			           }
    			       else
    			    	   if(c1.getTopWall(0).getLast().x>c2.getTopWall(0).getFirst().x && 
    	    		    	  c1.getLeftWalls().isEmpty() && c2.getRightWalls().isEmpty())
    			    	       {leftPortal=c2.getRightPortal(0);
        			    	    rightPortal=c1.getLeftPortal(0); 			    		   
    			    	       }
    			    	   else
    			    		   continue;
    			       for(Cell c3:cellsList)
    			           {if(mustRestart)
    			    	        break;
    			    	    //check if it is a single different cell contains the both portals
    			            if(c1!=c3 && c2!=c3)
    			                {found=false;
    			                 //check if it contains the left portal
    			                 for(PointPair currentLeftPortal:c3.getLeftPortals())
    			        	         if(currentLeftPortal.equals(leftPortal))
    			        	             {found=true;
    			        	     	      break;
    			        	             }
    			                 if(found)
    			                     {found=false;
    			                      //check if it contains the right portal
        			                  for(PointPair currentRightPortal:c3.getRightPortals())
        			        	          if(currentRightPortal.equals(rightPortal))
        			        	              {found=true;
        			        	    	       break;
        			        	              }
    			            	      if(found)
    			            	          {//check the criteria of decision to merge two cells
    			    			           //we assume that the both portals have the same size
    			            	           //if the size of the portals is bigger than their distance to each other
    			            	           if(leftPortal.getSize()>rightPortal.getFirst().x-leftPortal.getFirst().x)
    			            	               {//erase the both portals in the cell that links the two 
    			            	        	    //others and in the other cells
    			            	        	    c1.removeRightPortal(leftPortal);
    			            	        	    c2.removeRightPortal(leftPortal);
    			            	        	    c1.removeLeftPortal(rightPortal);
    			            	        	    c2.removeLeftPortal(rightPortal);
    			            	        	    c3.removeLeftPortal(leftPortal);
    			            	        	    c3.removeRightPortal(rightPortal);
    			            	        	    //add all the walls and all the portals of the cells that have to be merged
    			            	        	    //in order to make a single new cell replacing the 2 previous cells
    			            	        	    mergedResultCell=new Cell();
    			            	        	    //start with one of them
    			            	        	    mergedResultCell.addTopWalls(c1.getTopWalls());    			            	        	    			    	        	    
     			            	        	    mergedResultCell.addBottomWalls(c1.getBottomWalls());  			            	        	    			            	        	    
    			            	        	    mergedResultCell.addLeftWalls(c1.getLeftWalls());   			            	        	    	        	    
    			            	        	    mergedResultCell.addRightWalls(c1.getRightWalls());   			            	        	        			     	        	    
    			            	        	    mergedResultCell.addTopPortals(c1.getTopPortals());   			            	        	       			    	        	    
    			            	        	    mergedResultCell.addBottomPortals(c1.getBottomPortals());   			            	        	     			            	        	    
    			            	        	    mergedResultCell.addLeftPortals(c1.getLeftPortals());  			            	        	    	        	    
    			            	        	    mergedResultCell.addRightPortals(c1.getRightPortals());
    			            	        	    //the bottom link is the link between c1 and c2 at their bottom level
    			            	        	    PointPair bottomLink = new PointPair(new Point(leftPortal.getFirst().x,c1.getBottomWall(0).getFirst().y),
    			            	        	    		                             new Point(rightPortal.getFirst().x,c1.getBottomWall(0).getFirst().y));   			          	        	    
    			            	        	    //the top link is the link between c1 and c2 at their top level
    			            	        	    PointPair topLink = new PointPair(new Point(leftPortal.getFirst().x,c1.getTopWall(0).getFirst().y),
	        	    		                             new Point(rightPortal.getFirst().x,c1.getTopWall(0).getFirst().y));
    			            	        	    Vector<PointPair> bottomLinkWalls = new Vector<PointPair>();
    			            	        	    Vector<PointPair> bottomLinkPortals = new Vector<PointPair>();
    			            	        	    Vector<PointPair> topLinkWalls = new Vector<PointPair>();
    			            	        	    Vector<PointPair> topLinkPortals = new Vector<PointPair>();
    			            	        	    bottomLinkFound=false;
    			            	        	    for(PointPair p:c3.getBottomWalls())
    			            	        	    	if(p.equals(bottomLink))
    			            	        	    	    {bottomLinkFound=true;
    			            	        	    	     bottomLinkWalls.add(p);
    			            	        	    		 break;
    			            	        	    	    }   			            	        	    	
    			            	        	    //look if the bottom link is a portal of c3 
    			            	        	    if(!bottomLinkFound)
    			            	        	        for(PointPair p:c3.getBottomPortals())
    			            	        	    	    if(p.equals(bottomLink))
    			            	        	    	        {bottomLinkFound=true;
    			            	        	    	         bottomLinkPortals.add(p);
    			            	        	    		     break;
    			            	        	    	        }
    			            	        	    //if required, look for each piece composing the bottom link
    			            	        	    if(!bottomLinkFound && bottomLink.getSize()>1)
    			            	        	        {//check if all walls of c3 are sub-links   
                                                     boolean allSubLinksFound=true;
                                                     for(PointPair p:c3.getBottomWalls())
                                                         if(!bottomLink.contains(p))
                                                             {allSubLinksFound=false;
                                                              break;
                                                             }
                                                     //check if all portals of c3 are sub-links
                                                     if(allSubLinksFound)
                                                         {for(PointPair p:c3.getBottomPortals())
                                                              if(!bottomLink.contains(p))
                                                                  {allSubLinksFound=false;
                                                                   break;
                                                                  }                                         
                                                         }
                                                     //if both conditions are fulfilled
                                                     if(allSubLinksFound)
                                                         {//add all walls of c3 into topLinkWalls
                                                          bottomLinkWalls.addAll(c3.getBottomWalls());
                                                          //add all portals of c3 into topLinkPortals
                                                          bottomLinkPortals.addAll(c3.getBottomPortals());
                                                          bottomLinkFound=true;
                                                         }   
    			            	        	        }    
    			            	        	    topLinkFound=false;    	                                        
    			            	        	    for(PointPair p:c3.getTopWalls())
    			            	        	    	if(p.equals(topLink))
    			            	        	    	    {topLinkFound=true;
    			            	        	    	     topLinkWalls.add(p);
    			            	        	    		 break;
    			            	        	    	    }   			            	        	    	
    			            	        	    if(!topLinkFound)
    			            	        	        for(PointPair p:c3.getTopPortals())
    			            	        	    	    if(p.equals(topLink))
    			            	        	    	        {topLinkFound=true;
    			            	        	    	         topLinkPortals.add(p);
    			            	        	    		     break;
    			            	        	    	        }		            	        	    
    			            	        	    //if required, look for each piece composing the top link
    			            	        	    if(!topLinkFound && topLink.getSize()>1)
    			            	        	        {//check if all walls of c3 are sub-links   
    			            	        	         boolean allSubLinksFound=true;
    			            	        	         for(PointPair p:c3.getTopWalls())
    			            	        	             if(!topLink.contains(p))
    			            	        	                 {allSubLinksFound=false;
    			            	        	                  break;
    			            	        	                 }
                                                     //check if all portals of c3 are sub-links
                                                     if(allSubLinksFound)
                                                         {for(PointPair p:c3.getTopPortals())
                                                              if(!topLink.contains(p))
                                                                  {allSubLinksFound=false;
                                                                   break;
                                                                  }                                         
                                                         }
                                                     //if both conditions are fulfilled
                                                     if(allSubLinksFound)
                                                         {//add all walls of c3 into topLinkWalls
                                                          topLinkWalls.addAll(c3.getTopWalls());
                                                          //add all portals of c3 into topLinkPortals
                                                          topLinkPortals.addAll(c3.getTopPortals());
                                                          topLinkFound=true;
                                                         }                       			            	        	    	 
    			            	        	        }
    			            	        	    //if c3 doesn't disappear
    			            	        	    if(bottomLinkFound!=topLinkFound)
    			            	        	        {//if the bottom link between the portals is a wall
    	                                             if(bottomLinkFound)
    	                                                 {//remove it from the cell containing the portals
    	                                            	  c3.removeBottomWalls(bottomLinkWalls);
    	                                            	  c3.removeBottomPortals(bottomLinkPortals);   	                                            	  
    			            	        	              
    	                                            	  //add it to the final cell
    	                                        	      //mergedResultCell.addBottomWall(bottomLink);
    	                                            	  mergedResultCell.addBottomWalls(bottomLinkWalls);
    	                                            	  mergedResultCell.addBottomPortals(bottomLinkPortals);
    	                                                 }     	        	    
    	                                             else
    	                                                 {//add this bottom link as a portal in the final cell	
    	                                        	      mergedResultCell.addBottomPortal(bottomLink);   	                                            	  
    			            	        	              //add this bottom link as a portal in the cell containing the portals
    	                                        	      c3.addTopPortal(bottomLink);  	                                            	      
    	                                                 }  	                                          	                                            	                                           	                            
    			            	        	         //if the top link between the portals is a wall
    			            	        	         if(topLinkFound)
    			            	        	             {//remove it from the cell containing the portals
    			            	        	        	  c3.removeTopWalls(topLinkWalls);
    			            	        	        	  c3.removeTopPortals(topLinkPortals);   			            	        	        	  
    			            	        	    	      
			            	        	                  //add it to the final cell		
    			            	        	    	      //mergedResultCell.addTopWall(topLink); 
    			            	        	        	  mergedResultCell.addTopWalls(topLinkWalls);
   	                                            	      mergedResultCell.addTopPortals(topLinkPortals);
    			            	        	             }
    			            	        	         else
    			            	        	             {//add this top link as a portal in the final cell	
    			            	        	    	      mergedResultCell.addTopPortal(topLink);   			            	        	        	  
			            	        	                  //add this top link as a portal in the cell containing the portals CHANGE IT	 
    			            	        	    	      c3.addBottomPortal(topLink); 	                                            	      
    			            	        	             }   		                   	 
    			            	        	        }
    			            	        	    else
    			            	        	        {//if c3 disappears
    			            	        	    	 //nothing changes for the main merged cell			            	        	    	 
    			            	        	    	 if(topLinkFound)
    			            	        	    	     {//the main merged cell absorbs c3
    			            	        	    		  //mergedResultCell.addBottomWall(bottomLink);
    			            	        	    	      //mergedResultCell.addTopWall(topLink);    			                 	    	      
    			            	        	    		  mergedResultCell.addTopWalls(topLinkWalls);
 	                                            	      mergedResultCell.addTopPortals(topLinkPortals);
 	                                            	      mergedResultCell.addBottomWalls(bottomLinkWalls);
  	                                            	      mergedResultCell.addBottomPortals(bottomLinkPortals);
    			            	        	    	     }
    			            	        	    	 else
    			            	        	    	     {//c3 is split in 2 new cells
    			            	        	    		  Cell c4=new Cell(),c5=new Cell();
        			            	        	    	  //c5 gets the bottom part of c3
        			            	        	    	  c5.addBottomWalls(c3.getBottomWalls());
        			            	        	    	  c5.addBottomPortals(c3.getBottomPortals());
        			            	        	    	  //c5 gets all the part of c3 below the bottom link
        			            	        	    	  for(PointPair p:c3.getLeftPortals())
        			            	        	    		  if((p.getFirst().y<=bottomLink.getFirst().y &&
        			            	        	    			  p.getLast().y<bottomLink.getFirst().y)||
        			            	        	    		     (p.getFirst().y<bottomLink.getFirst().y &&
        	    			            	        	    	  p.getLast().y<=bottomLink.getFirst().y)
        			            	        	    		    )
        			            	        	    			  c5.addLeftPortal(p);
        			            	        	    	  for(PointPair p:c3.getLeftWalls())
        			            	        	    	 	  if((p.getFirst().y<=bottomLink.getFirst().y &&
            			            	        	     	     p.getLast().y<bottomLink.getFirst().y)||
            			            	        	    	    (p.getFirst().y<bottomLink.getFirst().y &&
            	    			            	        	     p.getLast().y<=bottomLink.getFirst().y)
            			            	        	    	   )
        			            	        	    			 c5.addLeftWall(p);
        			            	        	    	  for(PointPair p:c3.getRightPortals())
        			            	        	    		 if((p.getFirst().y<=bottomLink.getFirst().y &&
            			            	        	    		p.getLast().y<bottomLink.getFirst().y)||
            			            	        	    	   (p.getFirst().y<bottomLink.getFirst().y &&
            	    			            	        	    p.getLast().y<=bottomLink.getFirst().y)
            			            	        	    	  )
        			            	        	    			 c5.addRightPortal(p);
        			            	        	    	  for(PointPair p:c3.getRightWalls())
        			            	        	    		 if((p.getFirst().y<=bottomLink.getFirst().y &&
            			            	        	    		 p.getLast().y<bottomLink.getFirst().y)||
            			            	        	    	   (p.getFirst().y<bottomLink.getFirst().y &&
            	    			            	        	    p.getLast().y<=bottomLink.getFirst().y)
            			            	        	    	  )
        			            	        	    			 c5.addRightWall(p);
        			            	        	    	  //c4 gets the top part of c3
        			            	        	    	  c4.addTopWalls(c3.getTopWalls());
        			            	        	    	  c4.addTopPortals(c3.getTopPortals());
        			            	        	    	  //c4 gets all the part of c3 above the top link
        			            	        	    	  for(PointPair p:c3.getLeftPortals())
        			            	        	    		 if((p.getFirst().y>=topLink.getFirst().y &&
            			            	        	    		p.getLast().y>topLink.getFirst().y)||
            			            	        	    	   (p.getFirst().y>topLink.getFirst().y &&
            	    			            	        	    p.getLast().y>=topLink.getFirst().y)
            			            	        	    	  )
        			            	        	    			 c4.addLeftPortal(p);
        			            	        	    	  for(PointPair p:c3.getLeftWalls())
        			            	        	    		 if((p.getFirst().y>=topLink.getFirst().y &&
                			            	        	    	p.getLast().y>topLink.getFirst().y)||
                			            	        	       (p.getFirst().y>topLink.getFirst().y &&
                	    			            	        	p.getLast().y>=topLink.getFirst().y)
                			            	        	      )
        			            	        	    			 c4.addLeftWall(p);
        			            	        	    	  for(PointPair p:c3.getRightPortals())
        			            	        	    		 if((p.getFirst().y>=topLink.getFirst().y &&
                			            	        	    	p.getLast().y>topLink.getFirst().y)||
                			            	        	       (p.getFirst().y>topLink.getFirst().y &&
                	    			            	        	p.getLast().y>=topLink.getFirst().y)
                			            	        	      )
        			            	        	    			 c4.addRightPortal(p);
        			            	        	    	  for(PointPair p:c3.getRightWalls())
        			            	        	    		 if((p.getFirst().y>=topLink.getFirst().y &&
                			            	        	    	p.getLast().y>topLink.getFirst().y)||
                			            	        	       (p.getFirst().y>topLink.getFirst().y &&
                	    			            	        	p.getLast().y>=topLink.getFirst().y)
                			            	        	      )
        			            	        	    			 c4.addRightWall(p);
    			            	        	    		  mergedResultCell.addBottomPortal(bottomLink);
    			            	        	    	      mergedResultCell.addTopPortal(topLink);       	          	        	    	  
    			            	        	    	      c5.addTopPortal(bottomLink);
    			            	        	    	      c4.addBottomPortal(topLink);   			            	        	    	        			            	        	    	         			            	        	    	         			            	        	    	      
    			            	        	    	      //save the new cells to add them to the main list later
    			            	        	    	      additionalMergedCells.add(c4);
    	    			            	        	      additionalMergedCells.add(c5);
    	    			            	        	      if(!c4.isValid() || !c5.isValid())
    	    			            	        	          {System.out.println("c1 :");
    	    			            	        	           System.out.println(c1);
    	    			            	        	           System.out.println("c2 :");
    	    			            	        	           System.out.println(c2);
    	    			            	        	           System.out.println("c3 :");
    	    			            	        	           System.out.println(c3);
    	    			            	        	           System.out.println("c4 :");
    	    			            	        	           System.out.println(c4);
    	    			            	        	           System.out.println("c5 :");
    	    			            	        	           System.out.println(c5);
    	    			            	        	           System.out.println("mergedResultCell :");
    	    			            	        	           System.out.println(mergedResultCell);
    	    			            	        	          }
    			            	        	    	     }  			            	        	    	 
    			            	        	    	 //c3 mustn't exist anymore and must be destroyed
    			            	        	    	 garbageCells.add(c3);  			                    	    	 
    			            	        	        }        	    
    			            	        	    //handle the second here to keep the lists ordered
    			            	        	    mergedResultCell.addTopWalls(c2.getTopWalls());   
    			            	        	    mergedResultCell.addBottomWalls(c2.getBottomWalls()); 
    			            	        	    mergedResultCell.addLeftWalls(c2.getLeftWalls());
    			            	        	    mergedResultCell.addRightWalls(c2.getRightWalls());
    			            	        	    mergedResultCell.addTopPortals(c2.getTopPortals());
    			            	        	    mergedResultCell.addBottomPortals(c2.getBottomPortals());
    			            	        	    mergedResultCell.addLeftPortals(c2.getLeftPortals());
    			            	        	    mergedResultCell.addRightPortals(c2.getRightPortals());
    			            	        	    //merge all bottom walls of the final cell
    			            	        	    mergedResultCell.mergeBottomWalls();
    			            	        	    //merge all top walls of the final cell
    			            	        	    mergedResultCell.mergeTopWalls();
    			            	        	    //merge all bottom portals of the final cell
    			            	        	    mergedResultCell.mergeBottomPortals();
    			            	        	    //merge all top portals of the final cell
    			            	        	    mergedResultCell.mergeTopPortals();
    			            	        	    //save the cells that must be deleted later
    			            	        	    garbageCells.add(c1);
    			            	        	    garbageCells.add(c2);
    			            	        	    //restart the algorithm (put the flag "mustRestart" at true)
    			            	        	    mustRestart=true;   			            	        	  
    			            	               }
    			            	          }
    			                     }
    			                }
    			           }
    			      }
    		     }
    	    }
    	//if the flag mustRestart is at true
    	if(mustRestart)
    	    {//perform some differed operations here rather than in the loops to avoid causing 
    		 //a ConcurrentModificationException
    		 //add the final cell
    		 cellsList.add(mergedResultCell);
    		 //add the cells resulting of the destruction of c3 if any
    	     cellsList.addAll(additionalMergedCells);
    	     additionalMergedCells.clear();
    	     //remove the cells which have been merged
    		 cellsList.removeAll(garbageCells);
    		 garbageCells.clear();
    	     //perform a recursive call
    		 optimizeRawCellsAndPortals(cellsList);
    	    }   	
    }
    
    //add the third coordinate to each vertex in the cells and in the portals
    private final static List<Full3DCell> convertFromRawTo3DCellsAndPortals(List<Cell> cellsList,
            List<PointPair> topWallPiecesList,
            List<PointPair> bottomWallPiecesList,
            List<PointPair> leftWallPiecesList,
            List<PointPair> rightWallPiecesList,
            float factor){
        List<Full3DCell> full3DCellsList=new ArrayList<Full3DCell>();
        Full3DCell fullCell;
        int xmin,xmax,zmin,zmax;
        float[] texCoord=new float[4];
        final float[] floorTexCoord=new float[]{0.0f,0.75f,0.25f,0.5f};
        //final float[] bottomTexCoord=new float[]{0.0f,0.75f,0.25f,0.5f};
        final float[] ceilTexCoord=new float[]{0.0f,1.0f,0.25f,0.75f};
        for(Cell cell:cellsList)
            {//create a new full 3D cell
             fullCell=new Full3DCell();
             //for each kind of wall
             //compute atomic walls
             //for each atomic walls                     
             for(PointPair p:getAtomicWallsList(cell.getLeftWalls()))
                 {//get the texture coordinates by comparing the current point pair with the previous list of point pairs                                  
                  texCoord[0]=0.0f;
                  texCoord[1]=0.25f;
                  texCoord[2]=0.25f;
                  texCoord[3]=0.5f;                      
                  //create a new full 3D atomic wall (expressed in the base [[0;0;0][255;255;255]])
                  //add it into the list of full cells
                  fullCell.getLeftWalls().add(new float[]{texCoord[0],texCoord[1],(float)p.getLast().getX(),0.5f,(float)p.getLast().getY()});
                  fullCell.getLeftWalls().add(new float[]{texCoord[0],texCoord[3],(float)p.getLast().getX(),-0.5f,(float)p.getLast().getY()});
                  fullCell.getLeftWalls().add(new float[]{texCoord[2],texCoord[3],(float)p.getFirst().getX(),-0.5f,(float)p.getFirst().getY()});
                  fullCell.getLeftWalls().add(new float[]{texCoord[2],texCoord[1],(float)p.getFirst().getX(),0.5f,(float)p.getFirst().getY()});                                                                     
                 }
             //use dummy texture coordinates for portals
             Arrays.fill(texCoord,0);
             for(PointPair p:getAtomicWallsList(cell.getLeftPortals()))
                 {//create a new full 3D atomic wall (expressed in the base [[0;0;0][255;255;255]])
                  //add it into the list of full cells
                  fullCell.getLeftPortals().add(new float[]{texCoord[0],texCoord[1],(float)p.getLast().getX(),0.5f,(float)p.getLast().getY()});
                  fullCell.getLeftPortals().add(new float[]{texCoord[0],texCoord[3],(float)p.getLast().getX(),-0.5f,(float)p.getLast().getY()});
                  fullCell.getLeftPortals().add(new float[]{texCoord[2],texCoord[3],(float)p.getFirst().getX(),-0.5f,(float)p.getFirst().getY()});
                  fullCell.getLeftPortals().add(new float[]{texCoord[2],texCoord[1],(float)p.getFirst().getX(),0.5f,(float)p.getFirst().getY()});                 
                 }
             for(PointPair p:getAtomicWallsList(cell.getRightWalls()))
                 {texCoord[0]=0.0f;
                  texCoord[1]=0.25f;
                  texCoord[2]=0.25f;
                  texCoord[3]=0.5f;                     
                  //create a new full 3D atomic wall (expressed in the base [[0;0;0][255;255;255]])
                  //add it into the list of full cells
                  fullCell.getRightWalls().add(new float[]{texCoord[0],texCoord[1],(float)p.getFirst().getX(),0.5f,(float)p.getFirst().getY()});
                  fullCell.getRightWalls().add(new float[]{texCoord[0],texCoord[3],(float)p.getFirst().getX(),-0.5f,(float)p.getFirst().getY()});
                  fullCell.getRightWalls().add(new float[]{texCoord[2],texCoord[3],(float)p.getLast().getX(),-0.5f,(float)p.getLast().getY()});
                  fullCell.getRightWalls().add(new float[]{texCoord[2],texCoord[1],(float)p.getLast().getX(),0.5f,(float)p.getLast().getY()});                 
                 }
             //use dummy texture coordinates for portals
             Arrays.fill(texCoord,0);
             for(PointPair p:getAtomicWallsList(cell.getRightPortals()))
                 {//create a new full 3D atomic wall (expressed in the base [[0;0;0][255;255;255]])
                  //add it into the list of full cells   
                  fullCell.getRightPortals().add(new float[]{texCoord[0],texCoord[1],(float)p.getFirst().getX(),0.5f,(float)p.getFirst().getY()});
                  fullCell.getRightPortals().add(new float[]{texCoord[0],texCoord[3],(float)p.getFirst().getX(),-0.5f,(float)p.getFirst().getY()});
                  fullCell.getRightPortals().add(new float[]{texCoord[2],texCoord[3],(float)p.getLast().getX(),-0.5f,(float)p.getLast().getY()});
                  fullCell.getRightPortals().add(new float[]{texCoord[2],texCoord[1],(float)p.getLast().getX(),0.5f,(float)p.getLast().getY()});
                 } 
             for(PointPair p:getAtomicWallsList(cell.getTopWalls()))
                 {texCoord[0]=0.0f;
                  texCoord[1]=0.25f;
                  texCoord[2]=0.25f;
                  texCoord[3]=0.5f;
                       
                  //create a new full 3D atomic wall (expressed in the base [[0;0;0][255;255;255]])
                  //add it into the list of full cells
                  fullCell.getTopWalls().add(new float[]{texCoord[0],texCoord[1],(float)p.getLast().getX(),0.5f,(float)p.getLast().getY()});
                  fullCell.getTopWalls().add(new float[]{texCoord[0],texCoord[3],(float)p.getLast().getX(),-0.5f,(float)p.getLast().getY()});
                  fullCell.getTopWalls().add(new float[]{texCoord[2],texCoord[3],(float)p.getFirst().getX(),-0.5f,(float)p.getFirst().getY()});
                  fullCell.getTopWalls().add(new float[]{texCoord[2],texCoord[1],(float)p.getFirst().getX(),0.5f,(float)p.getFirst().getY()});                 
                 }
             //use dummy texture coordinates for portals
             Arrays.fill(texCoord,0);
             for(PointPair p:getAtomicWallsList(cell.getTopPortals()))
                 {//create a new full 3D atomic wall (expressed in the base [[0;0;0][255;255;255]])
                  //add it into the list of full cells
                  fullCell.getTopPortals().add(new float[]{texCoord[0],texCoord[1],(float)p.getLast().getX(),0.5f,(float)p.getLast().getY()});
                  fullCell.getTopPortals().add(new float[]{texCoord[0],texCoord[3],(float)p.getLast().getX(),-0.5f,(float)p.getLast().getY()});
                  fullCell.getTopPortals().add(new float[]{texCoord[2],texCoord[3],(float)p.getFirst().getX(),-0.5f,(float)p.getFirst().getY()});
                  fullCell.getTopPortals().add(new float[]{texCoord[2],texCoord[1],(float)p.getFirst().getX(),0.5f,(float)p.getFirst().getY()});
                 }
             for(PointPair p:getAtomicWallsList(cell.getBottomWalls()))
                 {texCoord[0]=0.0f;
                  texCoord[1]=0.25f;
                  texCoord[2]=0.25f;
                  texCoord[3]=0.5f;
                  //create a new full 3D atomic wall (expressed in the base [[0;0;0][255;255;255]])
                  //add it into the list of full cells
                  fullCell.getBottomWalls().add(new float[]{texCoord[0],texCoord[1],(float)p.getFirst().getX(),0.5f,(float)p.getFirst().getY()});
                  fullCell.getBottomWalls().add(new float[]{texCoord[0],texCoord[3],(float)p.getFirst().getX(),-0.5f,(float)p.getFirst().getY()});
                  fullCell.getBottomWalls().add(new float[]{texCoord[2],texCoord[3],(float)p.getLast().getX(),-0.5f,(float)p.getLast().getY()});
                  fullCell.getBottomWalls().add(new float[]{texCoord[2],texCoord[1],(float)p.getLast().getX(),0.5f,(float)p.getLast().getY()});
                 }
             //use dummy texture coordinates for portals
             Arrays.fill(texCoord,0);
             for(PointPair p:getAtomicWallsList(cell.getBottomPortals()))
                 {//create a new full 3D atomic wall (expressed in the base [[0;0;0][255;255;255]])
                  //add it into the list of full cells
                  fullCell.getBottomPortals().add(new float[]{texCoord[0],texCoord[1],(float)p.getFirst().getX(),0.5f,(float)p.getFirst().getY()});
                  fullCell.getBottomPortals().add(new float[]{texCoord[0],texCoord[3],(float)p.getFirst().getX(),-0.5f,(float)p.getFirst().getY()});
                  fullCell.getBottomPortals().add(new float[]{texCoord[2],texCoord[3],(float)p.getLast().getX(),-0.5f,(float)p.getLast().getY()});
                  fullCell.getBottomPortals().add(new float[]{texCoord[2],texCoord[1],(float)p.getLast().getX(),0.5f,(float)p.getLast().getY()});                 
                 }
             //compute the enclosing rectangle of this full 3D atomic wall
             fullCell.computeEnclosingRectangle();
             //System.out.println("Rectangle: "+fullCell.getEnclosingRectangle());
             xmin=fullCell.getEnclosingRectangle().x;
             zmin=fullCell.getEnclosingRectangle().y;
             xmax=xmin+fullCell.getEnclosingRectangle().width-1;
             zmax=zmin+fullCell.getEnclosingRectangle().height-1;                          
             //for each tile of the rectangle 
             for(int i=xmin;i<=xmax;i++)
                 for(int j=zmin;j<=zmax;j++)
                     {//build a tile for the ceiling
                      fullCell.getCeilWalls().add(new float[]{ceilTexCoord[0],ceilTexCoord[1],i,0.5f,j});
                      fullCell.getCeilWalls().add(new float[]{ceilTexCoord[0],ceilTexCoord[3],i+1,0.5f,j});
                      fullCell.getCeilWalls().add(new float[]{ceilTexCoord[2],ceilTexCoord[3],i+1,0.5f,j+1});
                      fullCell.getCeilWalls().add(new float[]{ceilTexCoord[2],ceilTexCoord[1],i,0.5f,j+1});
                      //build a tile for the floor                     
                      fullCell.getFloorWalls().add(new float[]{floorTexCoord[2],floorTexCoord[1],i,-0.5f,j+1});
                      fullCell.getFloorWalls().add(new float[]{floorTexCoord[2],floorTexCoord[3],i+1,-0.5f,j+1});
                      fullCell.getFloorWalls().add(new float[]{floorTexCoord[0],floorTexCoord[3],i+1,-0.5f,j});
                      fullCell.getFloorWalls().add(new float[]{floorTexCoord[0],floorTexCoord[1],i,-0.5f,j});                      
                     }   
             //convert the elements from the base [[0;0;0][255;255;255]] to the base [[0;0;0][255*65536;255*65536;255*65536]]
             for(float[] wall:fullCell.getLeftWalls())
                 {wall[2]*=factor;
                  wall[3]*=factor;
                  wall[4]*=factor;
                 }
             for(float[] wall:fullCell.getLeftPortals())
                 {wall[2]*=factor;
                  wall[3]*=factor;
                  wall[4]*=factor;
                 }
             for(float[] wall:fullCell.getRightWalls())
                 {wall[2]*=factor;
                  wall[3]*=factor;
                  wall[4]*=factor;                    
                 }
             for(float[] wall:fullCell.getRightPortals())
                 {wall[2]*=factor;
                  wall[3]*=factor;
                  wall[4]*=factor;                   
                 }             
             for(float[] wall:fullCell.getTopWalls())
                 {wall[2]*=factor;
                  wall[3]*=factor;
                  wall[4]*=factor;                    
                 }
             for(float[] wall:fullCell.getTopPortals())
                 {wall[2]*=factor;
                  wall[3]*=factor;
                  wall[4]*=factor;                    
                 }
             for(float[] wall:fullCell.getBottomWalls())
                 {wall[2]*=factor;
                  wall[3]*=factor;
                  wall[4]*=factor;                    
                 }
             for(float[] wall:fullCell.getBottomPortals())
                 {wall[2]*=factor;
                  wall[3]*=factor;
                  wall[4]*=factor;                    
                 }
             for(float[] wall:fullCell.getCeilWalls())
                 {wall[2]*=factor;
                  wall[3]*=factor;
                  wall[4]*=factor;
                 }
             for(float[] wall:fullCell.getFloorWalls())
                 {wall[2]*=factor;
                  wall[3]*=factor;
                  wall[4]*=factor; 
                 }
             //compute the enclosing rectangle again as the coordinates have changed
             fullCell.computeEnclosingRectangle();
             //the full cell is ready, add it into the list
             full3DCellsList.add(fullCell);
            }
        return(full3DCellsList);
    }
    
    /*private static final boolean doesListContainPointpair(List<PointPair> list,PointPair pointpair){
        for(PointPair p:list)
            if(p.equals(pointpair)||p.contains(pointpair)||pointpair.contains(p))
                return(true);
        return(false);
    }*/
    
    //create an image representing the cells on a map
    private final static void createCellsMap(Vector<Cell> cellsList){
    	BufferedImage buffer = new BufferedImage(256,256,BufferedImage.TYPE_INT_ARGB);
    	for(int i=0;i<256;i++)
    		for(int j=0;j<256;j++)
    	        buffer.setRGB(i, j, Color.WHITE.getRGB());
    	Color[] colorArray=initColorArray(cellsList.size());
    	int i=0;
    	for(Cell cell:cellsList)
    		{showCellInSchema(buffer,cell,colorArray[i].getRGB());
    		 i++;
    		}
    	try{
    	    ImageIO.write(buffer,"png",new File("pic256/cellsmap.png"));
    	   }
    	catch(IOException ioe)
    	{ioe.printStackTrace();}
    }

    private final static Color[] initColorArray(int size){ 
        //number of digits: 3 (R,G,B)
        //base: subSampleCount
        //possible combine: subSampleCount ^ 3
        int subSampleCount=(int)Math.ceil(Math.cbrt(size));
        int subSampleCountSquare=subSampleCount*subSampleCount;
        int[] subSample=new int[subSampleCount];
        //compute sub samples between 0 and 255
        double step=256.0D/subSampleCount;
        for(int i=0;i<subSampleCount;i++)
            subSample[i]=(int)Math.rint(step*i);
        Color[] colorArray=new Color[size];
        for(int i=0;i<size;i++)
            colorArray[i]=new Color(subSample[(i/subSampleCountSquare)%subSampleCount],subSample[(i/subSampleCount)%subSampleCount],subSample[i%subSampleCount]);
        return(colorArray);
    }
    
	private final static void showCellInSchema(BufferedImage buffer, Cell cell,int rgb){
		int left,right,top,bottom;		
		Rectangle r=cell.getEnclosingRectangle();
		left=(int)Math.rint(r.getMinX());
		right=(int)Math.rint(r.getMaxX()-1);
		top=(int)Math.rint(r.getMaxY()-1);
		bottom=(int)Math.rint(r.getMinY());
		for(int i=left;i<=right;i++)
		    for(int j=bottom;j<=top;j++)	    	        
		        buffer.setRGB(i,j,rgb);		    	
	}
	
	private final static boolean testNetworkIntegrity(List<Cell> cellsList,
	        Vector<PointPair> topFullWallList,
            Vector<PointPair> bottomFullWallList,
            Vector<PointPair> leftFullWallList,
            Vector<PointPair> rightFullWallList){
        List<PointPair> validWallsList=new ArrayList<PointPair>();
        List<PointPair> missingWallsList=new ArrayList<PointPair>();
        List<PointPair> exceedingWallsList=new ArrayList<PointPair>();
        //list of walls that cannot be put into any cell
        List<PointPair> ignoredWallsList=new ArrayList<PointPair>();
        List<Cell> subnetwork=new ArrayList<Cell>();
        List<PointPair> leftCellNewWallsList=null;
        List<PointPair> rightCellNewWallsList=null;
        List<PointPair> topCellNewWallsList=null;
        List<PointPair> bottomCellNewWallsList=null;
        boolean found;
        for(PointPair p:leftFullWallList)
            {for(Cell c:cellsList)
                 for(PointPair cellWall:c.getLeftWalls())
                     if(cellWall.equals(p))
                         {subnetwork.add(c);
                          break;
                         }
             //then we check if the wall appears only once
             if(subnetwork.isEmpty())
                 {if(leftCellNewWallsList==null)
                      {leftCellNewWallsList=new ArrayList<PointPair>();
                       for(Cell c:cellsList)
                           leftCellNewWallsList.addAll(c.getLeftWalls());
                       leftCellNewWallsList.removeAll(leftFullWallList);
                      }
                  //decompose the missing wall
                  found=true;
                  for(int i=Math.min(p.getFirst().y,p.getLast().y);found&&i<=Math.max(p.getFirst().y,p.getLast().y);i++)
                      {found=false;
                       //look for each piece in each new left wall
                       for(PointPair leftCellNewWall:leftCellNewWallsList)
                           if(p.getFirst().x==leftCellNewWall.getFirst().x)
                               {//decompose the new left wall
                                for(int j=Math.min(leftCellNewWall.getFirst().y,leftCellNewWall.getLast().y);!found&&j<=Math.max(leftCellNewWall.getFirst().y,leftCellNewWall.getLast().y);j++)
                                    if(i==j)
                                        found=true;                                        
                                if(found)
                                    break;
                               }
                      }
                  if(!found)
                      {if(p.getFirst().x==TilesGenerator.tileSize)
                           ignoredWallsList.add(p);
                       else
                           missingWallsList.add(p);
                      }
                  else
                      validWallsList.add(p);
                 }
             else
                 if(subnetwork.size()>1)
                     exceedingWallsList.add(p);
                 else
                     validWallsList.add(p);
             subnetwork.clear();
            }       
        for(PointPair p:rightFullWallList)
            {for(Cell c:cellsList)
                 for(PointPair cellWall:c.getRightWalls())
                     if(cellWall.equals(p))
                         {subnetwork.add(c);
                          break;
                         }
             //then we check if the wall appears only once
             if(subnetwork.isEmpty())
                 {if(rightCellNewWallsList==null)
                      {rightCellNewWallsList=new ArrayList<PointPair>();
                       for(Cell c:cellsList)
                           rightCellNewWallsList.addAll(c.getRightWalls());
                       rightCellNewWallsList.removeAll(rightFullWallList);
                      }
                  //decompose the missing wall
                  found=true;
                  for(int i=Math.min(p.getFirst().y,p.getLast().y);found&&i<=Math.max(p.getFirst().y,p.getLast().y);i++)
                      {found=false;
                       //look for each piece in each new left wall
                       for(PointPair rightCellNewWall:rightCellNewWallsList)
                           if(p.getFirst().x==rightCellNewWall.getFirst().x)
                               {//decompose the new left wall
                                for(int j=Math.min(rightCellNewWall.getFirst().y,rightCellNewWall.getLast().y);!found&&j<=Math.max(rightCellNewWall.getFirst().y,rightCellNewWall.getLast().y);j++)
                                    if(i==j)
                                        found=true;                                        
                                if(found)
                                    break;
                               }
                      }
                  if(!found)
                      {if(p.getFirst().x==0)
                           ignoredWallsList.add(p);
                       else
                           missingWallsList.add(p);
                      }
                  else
                      validWallsList.add(p);              
                 }
             else
                 if(subnetwork.size()>1)
                     exceedingWallsList.add(p);
                 else
                     validWallsList.add(p);
             subnetwork.clear();
            }
        for(PointPair p:topFullWallList)
            {for(Cell c:cellsList)
                 for(PointPair cellWall:c.getTopWalls())
                     if(cellWall.equals(p))
                         {subnetwork.add(c);
                          break;
                         }
             //then we check if the wall appears only once
             if(subnetwork.isEmpty())
                 {if(topCellNewWallsList==null)
                      {topCellNewWallsList=new ArrayList<PointPair>();
                       for(Cell c:cellsList)
                           topCellNewWallsList.addAll(c.getTopWalls());
                       topCellNewWallsList.removeAll(topFullWallList);
                      }
                  //decompose the missing wall
                  found=true;
                  for(int i=Math.min(p.getFirst().x,p.getLast().x);found&&i<=Math.max(p.getFirst().x,p.getLast().x);i++)
                      {found=false;
                       //look for each piece in each new left wall
                       for(PointPair topCellNewWall:topCellNewWallsList)
                           if(p.getFirst().y==topCellNewWall.getFirst().y)
                               {//decompose the new left wall
                                for(int j=Math.min(topCellNewWall.getFirst().x,topCellNewWall.getLast().x);!found&&j<=Math.max(topCellNewWall.getFirst().x,topCellNewWall.getLast().x);j++)
                                    if(i==j)
                                        found=true;                                        
                                if(found)
                                    break;
                               }
                      }
                  if(!found)
                      {if(p.getFirst().y==0)
                           ignoredWallsList.add(p);
                       else
                           missingWallsList.add(p);
                      }
                  else
                      validWallsList.add(p);
                 }
             else
                 if(subnetwork.size()>1)
                     exceedingWallsList.add(p);
                 else
                     validWallsList.add(p);
             subnetwork.clear();
            }
        for(PointPair p:bottomFullWallList)
            {for(Cell c:cellsList)
                 for(PointPair cellWall:c.getBottomWalls())
                     if(cellWall.equals(p))
                         {subnetwork.add(c);
                          break;
                         }
             //then we check if the wall appears only once
             if(subnetwork.isEmpty())
                 {if(bottomCellNewWallsList==null)
                      {bottomCellNewWallsList=new ArrayList<PointPair>();
                       for(Cell c:cellsList)
                           bottomCellNewWallsList.addAll(c.getBottomWalls());
                       bottomCellNewWallsList.removeAll(bottomFullWallList);
                      }
                  //decompose the missing wall
                  found=true;
                  for(int i=Math.min(p.getFirst().x,p.getLast().x);found&&i<=Math.max(p.getFirst().x,p.getLast().x);i++)
                      {found=false;
                       //look for each piece in each new left wall
                       for(PointPair bottomCellNewWall:bottomCellNewWallsList)
                           if(p.getFirst().y==bottomCellNewWall.getFirst().y)
                               {//decompose the new left wall
                                for(int j=Math.min(bottomCellNewWall.getFirst().x,bottomCellNewWall.getLast().x);!found&&j<=Math.max(bottomCellNewWall.getFirst().x,bottomCellNewWall.getLast().x);j++)
                                    if(i==j)
                                        found=true;                                        
                                if(found)
                                    break;
                               }
                      }
                  if(!found)
                      {if(p.getFirst().y==TilesGenerator.tileSize)
                           ignoredWallsList.add(p);
                       else
                           missingWallsList.add(p);
                      }
                  else
                      validWallsList.add(p);
                 }
             else
                 if(subnetwork.size()>1)
                     exceedingWallsList.add(p);
                 else
                     validWallsList.add(p);
             subnetwork.clear();
            }         
	    return(missingWallsList.isEmpty()&&exceedingWallsList.isEmpty()&&
	            validWallsList.size()+ignoredWallsList.size()==(leftFullWallList.size()+rightFullWallList.size()+
	                    topFullWallList.size()+bottomFullWallList.size()));
	}

	private static final boolean testCellCompleteness(List<Cell> cellsList){
	    for(Cell c:cellsList)
	        if(!c.isValid())
	            return(false);
	    return(true);
	}
}
