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
import java.util.List;
import java.util.Vector;
import java.util.Collections;
import java.awt.Color;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

public final class CellsGenerator{
    
    
    public final static void generate(List<PointPair> topWallPiecesList,
                                      List<PointPair> bottomWallPiecesList,
                                      List<PointPair> leftWallPiecesList,
			              List<PointPair> rightWallPiecesList){
    	Vector<PointPair> topFullWallList=mergeAllWallPieces(topWallPiecesList);
    	Vector<PointPair> bottomFullWallList=mergeAllWallPieces(bottomWallPiecesList);
    	Vector<PointPair> leftFullWallList=mergeAllWallPieces(leftWallPiecesList);
    	Vector<PointPair> rightFullWallList=mergeAllWallPieces(rightWallPiecesList);
    	Collections.sort(topFullWallList,new PointPairComparator(PointPairComparator.VERTICAL_SORT));
    	Collections.sort(bottomFullWallList,new PointPairComparator(PointPairComparator.VERTICAL_SORT));
    	Collections.sort(leftFullWallList,new PointPairComparator(PointPairComparator.HORIZONTAL_SORT));
    	Collections.sort(rightFullWallList,new PointPairComparator(PointPairComparator.HORIZONTAL_SORT));
    	Vector<Cell> cellsList=new Vector<Cell>();
    	Vector<Portal> portalsList=new Vector<Portal>();
    	generateRawCellsAndPortals(topFullWallList,bottomFullWallList,leftFullWallList,rightFullWallList,cellsList,portalsList);
    	optimizeRawCellsAndPortals(cellsList);  
    	List<Full3DCell> full3DCellsList=convertFromRawTo3DCellsAndPortals(cellsList);
    	addCeilAndFloorToFull3DCells(full3DCellsList);   	
    	addTexturesToCells(full3DCellsList);
    	System.out.println("SIZE : "+cellsList.size());
    	createCellsMap(cellsList);
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
    private final static Vector<PointPair> ALTERNATIVEmergeAllWallPieces(List<PointPair> wallPiecesList){       
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
    		 //FIXME: this line might be responsible of the problem
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
    	    {//perform some differed operations here rather than in the loops to avoid to cause 
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
    	else
    	    {//save the portals in a distinct list
    		 
    	    }
    }
    
    
    //complete cells by adding them a ceiling and a floor
    private final static void addCeilAndFloorToFull3DCells(List<Full3DCell> full3DCellsList){       
        for(Full3DCell cell:full3DCellsList)
            {//TODO 
             //for each left wall
                 //for each top wall
                     //build a tile for the ceiling
                     //build a tile for the floor
             //for each left portal
                 //for each top wall
                     //build a tile for the ceiling
                     //build a tile for the floor
             //for each left wall
                 //for each top portal
                     //build a tile for the ceiling
                     //build a tile for the floor
             //for each left portal
                 //for each top portal
                     //build a tile for the ceiling
                     //build a tile for the floor
            }
    }
    
    //add the third coordinate to each vertex in the cells and in the portals
    private final static List<Full3DCell> convertFromRawTo3DCellsAndPortals(List<Cell> cellsList){
        List<Full3DCell> full3DCellsList=new ArrayList<Full3DCell>();
        for(Cell cell:cellsList)
            {//TODO
             //for each kind of wall
                 //for each wall
                     //compute atomic walls
                     //for each atomic walls
                         //create a new full 3D atomic wall
                         //compute the enclosing rectangle of this full 3D atomic wall
                         //fullcell.computeEnclosingRectangle();
                         //add it into the list of full cells
            }
        return(full3DCellsList);
    }
    
    //add the textures coordinates and the textures to the cells
    private final static void addTexturesToCells(List<Full3DCell> full3DCellsList){
        
    }
    
    //create an image representing the cells on a map
    private final static void createCellsMap(Vector<Cell> cellsList){
    	BufferedImage buffer = new BufferedImage(256,256,BufferedImage.TYPE_INT_ARGB);
    	Vector<Cell> overlappingCellsList=new Vector<Cell>();
    	for(int i=0;i<256;i++)
    		for(int j=0;j<256;j++)
    	        buffer.setRGB(i, j, Color.WHITE.getRGB());
    	Color[] colorArray=initColorArray(cellsList.size());
    	int i=0;
    	for(Cell cell:cellsList)
    		{showCellInSchema(buffer,cell,overlappingCellsList,colorArray[i].getRGB());
    		 i++;
    		}
    	try{
    	    ImageIO.write(buffer,"png",new File("pic256/cellsmap.png"));
    	   }
    	catch(IOException ioe)
    	{ioe.printStackTrace();}
    	System.out.println("OVERLAPPING CELLS COUNT = "+overlappingCellsList.size());
    	for(Cell cell:overlappingCellsList)
    	    {System.out.println("OVERLAPPING CELL");
	         System.out.println(cell);  		 
    	    }
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
    
	private final static void showCellInSchema(BufferedImage buffer, Cell cell,Vector<Cell> overlappingCellsList,int rgb){
		int left,right,top,bottom;		
		if(cell.isValid())
		    {if(!cell.getLeftWalls().isEmpty())
		    	 left=cell.getLeftWall(0).getFirst().x;
		     else
		    	 left=cell.getLeftPortal(0).getFirst().x;
		     if(!cell.getRightWalls().isEmpty())
		    	 right=cell.getRightWall(0).getFirst().x-1;
		     else
		    	 right=cell.getRightPortal(0).getFirst().x-1;
		     if(!cell.getTopWalls().isEmpty())
		         top=cell.getTopWall(0).getFirst().y-1;
		     else
		    	 top=cell.getTopPortal(0).getFirst().y-1;
		     if(!cell.getBottomWalls().isEmpty())
		    	 bottom=cell.getBottomWall(0).getFirst().y;
		     else
		    	 bottom=cell.getBottomPortal(0).getFirst().y;		     
			 for(int i=left;i<=right;i++)
	    		for(int j=bottom;j<=top;j++)
	    	        if(buffer.getRGB(i,j)==Color.WHITE.getRGB())
	    	        	buffer.setRGB(i,j,rgb);
	    	        else
	    	            {buffer.setRGB(i,j,rgb);
	    	        	 if(!overlappingCellsList.contains(cell))
	    	        		 if(cell.getBottomPortals().isEmpty() &&
	    	        		    cell.getTopPortals().isEmpty() &&
	    	        		    cell.getLeftPortals().isEmpty() &&
	    	        		    cell.getRightPortals().isEmpty()
	    	        	       )
	    	        	         overlappingCellsList.add(cell);
	    	        		 else
	    	        		     {//if the overlap comes from
	    	        			  //a wall, add this cell to the overlap list???
	    	        		     }
	   		            }
		    }
		else
		    {System.out.println("INVALID CELL");
		     System.out.println(cell);
		    }		
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
