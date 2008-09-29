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

import java.awt.Rectangle;
import java.util.Collections;
import java.util.List;
import java.util.Vector;

public final class Cell{
    

    private transient List<PointPair> topWalls;
    
    private transient List<PointPair> bottomWalls;
    
    private transient List<PointPair> topPortals;
    
    private transient List<PointPair> bottomPortals;
    
    private transient List<PointPair> leftWalls;
    
    private transient List<PointPair> rightWalls;
    
    private transient List<PointPair> leftPortals;
    
    private transient List<PointPair> rightPortals;
    
    private transient Rectangle enclosingRectangle;
    
    
    public Cell(){
        topWalls=new Vector<PointPair>();
	    bottomWalls=new Vector<PointPair>();
	    topPortals=new Vector<PointPair>();
	    bottomPortals=new Vector<PointPair>();
	    leftWalls=new Vector<PointPair>();
	    rightWalls=new Vector<PointPair>();
	    leftPortals=new Vector<PointPair>();
	    rightPortals=new Vector<PointPair>();
    }
    
    public final boolean equals(Object o){
        if(o==null||!(o instanceof Cell))
            return(false);   
        Cell c=(Cell)o;
        return(topWalls.equals(c.getTopWalls())&&topPortals.equals(c.getTopPortals())&&
            bottomWalls.equals(c.getBottomWalls())&&bottomPortals.equals(c.getBottomPortals())&&
            leftWalls.equals(c.getLeftWalls())&&leftPortals.equals(c.getLeftPortals())&&
            rightWalls.equals(c.getRightWalls())&&rightPortals.equals(c.getRightPortals())
        );
    }
    
    
    final Rectangle getEnclosingRectangle(){
        double minx=Double.MAX_VALUE,minz=Double.MAX_VALUE,maxx=Double.MIN_VALUE,maxz=Double.MIN_VALUE;
        for(PointPair p:topWalls)
            {if(p.getFirst().getX()<minx) 
                 minx=p.getFirst().getX();
             else
                 if(p.getFirst().getX()>maxx) 
                     maxx=p.getFirst().getX();
             if(p.getLast().getX()<minx) 
                 minx=p.getLast().getX();
             else
                 if(p.getLast().getX()>maxx) 
                     maxx=p.getLast().getX();
             if(p.getFirst().getY()<minz) 
                 minz=p.getFirst().getY();
             else
                 if(p.getFirst().getY()>maxz) 
                     maxz=p.getFirst().getY();
             if(p.getLast().getY()<minz) 
                 minz=p.getLast().getY();
             else
                 if(p.getLast().getY()>maxz) 
                     maxz=p.getLast().getY();
            }
        for(PointPair p:bottomWalls)
            {if(p.getFirst().getX()<minx) 
                 minx=p.getFirst().getX();
             else
                 if(p.getFirst().getX()>maxx) 
                     maxx=p.getFirst().getX();
             if(p.getLast().getX()<minx) 
                 minx=p.getLast().getX();
             else
                 if(p.getLast().getX()>maxx) 
                     maxx=p.getLast().getX();
             if(p.getFirst().getY()<minz) 
                 minz=p.getFirst().getY();
             else
                 if(p.getFirst().getY()>maxz) 
                     maxz=p.getFirst().getY();
             if(p.getLast().getY()<minz) 
                 minz=p.getLast().getY();
             else
                 if(p.getLast().getY()>maxz) 
                     maxz=p.getLast().getY();
            }
        for(PointPair p:leftWalls)
            {if(p.getFirst().getX()<minx) 
                 minx=p.getFirst().getX();
             else
                 if(p.getFirst().getX()>maxx) 
                     maxx=p.getFirst().getX();
             if(p.getLast().getX()<minx) 
                 minx=p.getLast().getX();
             else
                 if(p.getLast().getX()>maxx) 
                     maxx=p.getLast().getX();
             if(p.getFirst().getY()<minz) 
                 minz=p.getFirst().getY();
             else
                 if(p.getFirst().getY()>maxz) 
                     maxz=p.getFirst().getY();
             if(p.getLast().getY()<minz) 
                 minz=p.getLast().getY();
             else
                 if(p.getLast().getY()>maxz) 
                     maxz=p.getLast().getY();
            }
        for(PointPair p:rightWalls)
            {if(p.getFirst().getX()<minx) 
                 minx=p.getFirst().getX();
             else
                 if(p.getFirst().getX()>maxx) 
                     maxx=p.getFirst().getX();
             if(p.getLast().getX()<minx) 
                 minx=p.getLast().getX();
             else
                 if(p.getLast().getX()>maxx) 
                     maxx=p.getLast().getX();
             if(p.getFirst().getY()<minz) 
                 minz=p.getFirst().getY();
             else
                 if(p.getFirst().getY()>maxz) 
                     maxz=p.getFirst().getY();
             if(p.getLast().getY()<minz) 
                 minz=p.getLast().getY();
             else
                 if(p.getLast().getY()>maxz) 
                     maxz=p.getLast().getY();
            }
        for(PointPair p:topPortals)
            {if(p.getFirst().getX()<minx) 
                 minx=p.getFirst().getX();
             else
                 if(p.getFirst().getX()>maxx) 
                     maxx=p.getFirst().getX();
             if(p.getLast().getX()<minx) 
                 minx=p.getLast().getX();
             else
                 if(p.getLast().getX()>maxx) 
                     maxx=p.getLast().getX();
             if(p.getFirst().getY()<minz) 
                 minz=p.getFirst().getY();
             else
                 if(p.getFirst().getY()>maxz) 
                     maxz=p.getFirst().getY();
             if(p.getLast().getY()<minz) 
                 minz=p.getLast().getY();
             else
                 if(p.getLast().getY()>maxz) 
                     maxz=p.getLast().getY();
            }
        for(PointPair p:bottomPortals)
            {if(p.getFirst().getX()<minx) 
                 minx=p.getFirst().getX();
             else
                 if(p.getFirst().getX()>maxx) 
                     maxx=p.getFirst().getX();
             if(p.getLast().getX()<minx) 
                 minx=p.getLast().getX();
             else
                 if(p.getLast().getX()>maxx) 
                     maxx=p.getLast().getX();
             if(p.getFirst().getY()<minz) 
                 minz=p.getFirst().getY();
             else
                 if(p.getFirst().getY()>maxz) 
                     maxz=p.getFirst().getY();
             if(p.getLast().getY()<minz) 
                 minz=p.getLast().getY();
             else
                 if(p.getLast().getY()>maxz) 
                     maxz=p.getLast().getY();
            }
        for(PointPair p:leftPortals)
            {if(p.getFirst().getX()<minx) 
                 minx=p.getFirst().getX();
             else
                 if(p.getFirst().getX()>maxx) 
                     maxx=p.getFirst().getX();
             if(p.getLast().getX()<minx) 
                 minx=p.getLast().getX();
             else
                 if(p.getLast().getX()>maxx) 
                     maxx=p.getLast().getX();
             if(p.getFirst().getY()<minz) 
                 minz=p.getFirst().getY();
             else
                 if(p.getFirst().getY()>maxz) 
                     maxz=p.getFirst().getY();
             if(p.getLast().getY()<minz) 
                 minz=p.getLast().getY();
             else
                 if(p.getLast().getY()>maxz) 
                     maxz=p.getLast().getY();
            }
        for(PointPair p:rightPortals)
            {if(p.getFirst().getX()<minx) 
                 minx=p.getFirst().getX();
             else
                 if(p.getFirst().getX()>maxx) 
                     maxx=p.getFirst().getX();
             if(p.getLast().getX()<minx) 
                 minx=p.getLast().getX();
             else
                 if(p.getLast().getX()>maxx) 
                     maxx=p.getLast().getX();
             if(p.getFirst().getY()<minz) 
                 minz=p.getFirst().getY();
             else
                 if(p.getFirst().getY()>maxz) 
                     maxz=p.getFirst().getY();
             if(p.getLast().getY()<minz) 
                 minz=p.getLast().getY();
             else
                 if(p.getLast().getY()>maxz) 
                     maxz=p.getLast().getY();
            }
        if(enclosingRectangle==null)
            enclosingRectangle=new Rectangle();
        enclosingRectangle.setFrameFromDiagonal(minx,minz,maxx,maxz);
        return(enclosingRectangle);
    }
    
    public final List<PointPair> getTopWalls(){
        return(Collections.unmodifiableList(topWalls));
    }
    
    public final List<PointPair> getBottomWalls(){
        return(Collections.unmodifiableList(bottomWalls));
    }
    
    public final PointPair getTopWall(int index){
        return(topWalls.get(index));
    }
    
    public final PointPair getBottomWall(int index){
        return(bottomWalls.get(index));
    }
         
    public final List<PointPair> getLeftWalls(){
        return(Collections.unmodifiableList(leftWalls));
    }
    
    public final List<PointPair> getRightWalls(){
        return(Collections.unmodifiableList(rightWalls));
    }
    
    public final PointPair getLeftWall(int index){
        return(leftWalls.get(index));
    }
    
    public final PointPair getRightWall(int index){
        return(rightWalls.get(index));
    }   
    
    public final List<PointPair> getTopPortals(){
        return(Collections.unmodifiableList(topPortals));
    }
    
    public final List<PointPair> getBottomPortals(){
        return(Collections.unmodifiableList(bottomPortals));
    }
    
    public final PointPair getTopPortal(int index){
        return(topPortals.get(index));
    }
    
    public final PointPair getBottomPortal(int index){
        return(bottomPortals.get(index));
    }
         
    public final List<PointPair> getLeftPortals(){
        return(Collections.unmodifiableList(leftPortals));
    }
    
    public final List<PointPair> getRightPortals(){
        return(Collections.unmodifiableList(rightPortals));
    }
    
    public final PointPair getLeftPortal(int index){
        return(leftPortals.get(index));
    }
    
    public final PointPair getRightPortal(int index){
        return(rightPortals.get(index));
    }
    
    public final void addTopWall(PointPair topWall){
        topWalls.add(topWall);
    }
    
    public final void addBottomWall(PointPair bottomWall){
        bottomWalls.add(bottomWall);
    }
    
    public final void addTopPortal(PointPair topPortal){
        topPortals.add(topPortal);
    }
    
    public final void addBottomPortal(PointPair bottomPortal){
        bottomPortals.add(bottomPortal);
    }
    
    public final void addLeftWall(PointPair leftWall){
        leftWalls.add(leftWall);
    }
    
    public final void addRightWall(PointPair rightWall){
        rightWalls.add(rightWall);
    }
    
    public final void addLeftPortal(PointPair leftPortal){
        leftPortals.add(leftPortal);
    }
    
    public final void addRightPortal(PointPair rightPortal){
        rightPortals.add(rightPortal);
    }     
    
    public final void addTopWalls(List<PointPair> topWalls){
        this.topWalls.addAll(topWalls);
    }
    
    public final void addBottomWalls(List<PointPair> bottomWalls){
        this.bottomWalls.addAll(bottomWalls);
    }
    
    public final void addTopPortals(List<PointPair> topPortals){
        this.topPortals.addAll(topPortals);
    }
    
    public final void addBottomPortals(List<PointPair> bottomPortals){
        this.bottomPortals.addAll(bottomPortals);
    }
    
    public final void addLeftWalls(List<PointPair> leftWalls){
        this.leftWalls.addAll(leftWalls);
    }
    
    public final void addRightWalls(List<PointPair> rightWalls){
        this.rightWalls.addAll(rightWalls);
    }
    
    public final void addLeftPortals(List<PointPair> leftPortals){
        this.leftPortals.addAll(leftPortals);
    }
    
    public final void addRightPortals(List<PointPair> rightPortals){
        this.rightPortals.addAll(rightPortals);
    }      
    
    public final void removeTopWall(PointPair topWall){
        topWalls.remove(topWall);
    }
    
    public final void removeBottomWall(PointPair bottomWall){
        bottomWalls.remove(bottomWall);
    }
    
    public final void removeTopPortal(PointPair topPortal){
        topPortals.remove(topPortal);
    }
    
    public final void removeBottomPortal(PointPair bottomPortal){
        bottomPortals.remove(bottomPortal);
    }
    
    public final void removeLeftWall(PointPair leftWall){
        leftWalls.remove(leftWall);
    }
    
    public final void removeRightWall(PointPair rightWall){
        rightWalls.remove(rightWall);
    }
    
    public final boolean removeLeftPortal(PointPair leftPortal){
        boolean success;
        if(leftPortals.isEmpty())
            success=false;
        else
            {if(!leftPortals.remove(leftPortal))
                 {success=false;
                  int leftPortalIndex=0;
                  for(PointPair p:leftPortals)
                      if(p.equals(leftPortal))
                          {success=true;                      
                           break;
                          }
                      else
                          leftPortalIndex++;
                  if(success)
                      leftPortals.remove(leftPortalIndex);
                 }
             else
                 success=true;
            }
        return(success);
    }
    
    public final boolean removeRightPortal(PointPair rightPortal){       
        boolean success;
        if(rightPortals.isEmpty())
            success=false;
        else
            {if(!rightPortals.remove(rightPortal))
                 {success=false;
                  int rightPortalIndex=0;
                  for(PointPair p:rightPortals)
                      if(p.equals(rightPortal))
                          {success=true;                      
                           break;
                          }
                      else
                          rightPortalIndex++;
                  if(success)
                      rightPortals.remove(rightPortalIndex);
                 }
             else
                 success=true;
            }
        return(success);
    }     
    
    public final void removeTopWalls(List<PointPair> topWalls){
        this.topWalls.removeAll(topWalls);
    }
    
    public final void removeBottomWalls(List<PointPair> bottomWalls){
        this.bottomWalls.removeAll(bottomWalls);
    }
    
    public final void removeTopPortals(List<PointPair> topPortals){
        this.topPortals.removeAll(topPortals);
    }
    
    public final void removeBottomPortals(List<PointPair> bottomPortals){
        this.bottomPortals.removeAll(bottomPortals);
    }
    
    public final void removeLeftWalls(List<PointPair> leftWalls){
        this.leftWalls.removeAll(leftWalls);
    }
    
    public final void removeRightWalls(List<PointPair> rightWalls){
        this.rightWalls.removeAll(rightWalls);
    }
    
    public final void removeLeftPortals(List<PointPair> leftPortals){
        this.leftPortals.removeAll(leftPortals);
    }
    
    public final void removeRightPortals(List<PointPair> rightPortals){
        this.rightPortals.removeAll(rightPortals);
    }
    
    public final void mergeLeftWalls(){
    	Collections.sort(leftWalls, new PointPairComparator(PointPairComparator.VERTICAL_SORT));
    	mergePointPairList(leftWalls);
    } 
    
    public final void mergeRightWalls(){
    	Collections.sort(rightWalls, new PointPairComparator(PointPairComparator.VERTICAL_SORT));
    	mergePointPairList(rightWalls);
    } 
    
    public final void mergeTopWalls(){
    	Collections.sort(topWalls, new PointPairComparator(PointPairComparator.HORIZONTAL_SORT));
    	mergePointPairList(topWalls);
    } 
    
    public final void mergeBottomWalls(){
    	Collections.sort(bottomWalls, new PointPairComparator(PointPairComparator.HORIZONTAL_SORT));
    	mergePointPairList(bottomWalls);
    }  
    
    public final void mergeLeftPortals(){
    	Collections.sort(leftPortals, new PointPairComparator(PointPairComparator.VERTICAL_SORT));
    	mergePointPairList(leftPortals);
    } 
    
    public final void mergeRightPortals(){
    	Collections.sort(rightPortals, new PointPairComparator(PointPairComparator.VERTICAL_SORT));
    	mergePointPairList(rightPortals);
    } 
    
    public final void mergeTopPortals(){
    	Collections.sort(topPortals, new PointPairComparator(PointPairComparator.HORIZONTAL_SORT));
    	mergePointPairList(topPortals);
    } 
    
    public final void mergeBottomPortals(){
    	Collections.sort(bottomPortals, new PointPairComparator(PointPairComparator.HORIZONTAL_SORT));
    	mergePointPairList(bottomPortals);
    }
    
    /**
     * merge all linked point pairs
     * @param list : list containing SORTED point pairs
     */
    private final static void mergePointPairList(List<PointPair> list){
    	Vector<PointPair> result=new Vector<PointPair>();
    	result.addAll(CellsGenerator.mergeAllWallPieces(list));    	
    	list.clear();
    	list.addAll(result);
    	
    }
    
    public final String toString(){
    	/*final String newLine="\n";//System.getProperty("line.separator");
    	String tmp="";
    	//tmp+=((Object)this).toString()+newLine;
    	tmp+="top walls :"+newLine;
    	for(PointPair p:topWalls)
    	    tmp+=p.toString()+newLine;   	   
    	tmp+="top portals :"+newLine;
    	for(PointPair p:topPortals)
    	    tmp+=p.toString()+newLine;
    	tmp+="bottom walls :"+newLine;
    	for(PointPair p:bottomWalls)
    	    tmp+=p.toString()+newLine;
    	tmp+="bottom portals :"+newLine;
    	for(PointPair p:bottomPortals)
    	    tmp+=p.toString()+newLine;
    	tmp+="left walls :"+newLine;
    	for(PointPair p:leftWalls)
    	    tmp+=p.toString()+newLine;
    	tmp+="left portals :"+newLine;
    	for(PointPair p:leftPortals)
    	    tmp+=p.toString()+newLine;
    	tmp+="right walls :"+newLine;
    	for(PointPair p:rightWalls)
    	    tmp+=p.toString()+newLine;
    	tmp+="right portals :"+newLine;  
    	for(PointPair p:rightPortals)
    	    tmp+=p.toString()+newLine;
    	tmp+=newLine;
    	return(tmp);*/
        return(getEnclosingRectangle().toString());
    }


	public final boolean isValid() {
		if(!leftWalls.isEmpty())
		    {int left=leftWalls.get(0).getFirst().x;
		     for(PointPair p:leftWalls)
			     if(p.getFirst().x!=left || p.getLast().x!=left)
				     return(false);
		     if(!leftPortals.isEmpty())
		    	 for(PointPair p:leftPortals)
		             if(p.getFirst().x!=left || p.getLast().x!=left)
			             return(false);
		    }
		else
		    if(!leftPortals.isEmpty())
		        {int left=leftPortals.get(0).getFirst().x;
	             for(PointPair p:leftPortals)
		             if(p.getFirst().x!=left || p.getLast().x!=left)
			             return(false);
	            }
		    else
		    	return(false);
		if(!rightWalls.isEmpty())
		    {int right=rightWalls.get(0).getFirst().x;
			 for(PointPair p:rightWalls)
				 if(p.getFirst().x!=right || p.getLast().x!=right)
					 return(false);
			 if(!rightPortals.isEmpty())
				 for(PointPair p:rightPortals)
				     if(p.getFirst().x!=right || p.getLast().x!=right)
					     return(false);	
		    }
		else
		    if(!rightPortals.isEmpty())
		        {int right=rightPortals.get(0).getFirst().x;
			     for(PointPair p:rightPortals)
				     if(p.getFirst().x!=right || p.getLast().x!=right)
					     return(false);			 
		        }
		    else
		    	return(false);	
		if(!topWalls.isEmpty())
		    {int top=topWalls.get(0).getFirst().y;
			 for(PointPair p:topWalls)
				 if(p.getFirst().y!=top || p.getLast().y!=top)
					 return(false);	
			 if(!topPortals.isEmpty())
				 for(PointPair p:topPortals)
				     if(p.getFirst().y!=top || p.getLast().y!=top)
					     return(false);
		    }
		else
			if(!topPortals.isEmpty())
			    {int top=topPortals.get(0).getFirst().y;
			     for(PointPair p:topPortals)
				     if(p.getFirst().y!=top || p.getLast().y!=top)
					     return(false);			 
		        }
			else
				return(false);
		
		if(!bottomWalls.isEmpty())
	        {int bottom=bottomWalls.get(0).getFirst().y;
		     for(PointPair p:bottomWalls)
			     if(p.getFirst().y!=bottom || p.getLast().y!=bottom)
				     return(false);	
		     if(!bottomPortals.isEmpty())
			     for(PointPair p:bottomPortals)
			         if(p.getFirst().y!=bottom || p.getLast().y!=bottom)
				         return(false);
	        }
	    else
		    if(!bottomPortals.isEmpty())
		        {int bottom=bottomPortals.get(0).getFirst().y;
		         for(PointPair p:bottomPortals)
			         if(p.getFirst().y!=bottom || p.getLast().y!=bottom)
				         return(false);			 
	            }
		    else
			    return(false);						
		return(true);
	}
}
