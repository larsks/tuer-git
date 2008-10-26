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

/*

*/

package tools;

import java.io.Serializable;
import java.util.Comparator;

public final class PointPairComparator implements Comparator<PointPair>,Serializable {

    
    private static final long serialVersionUID = 1L;

    public static final int VERTICAL_SORT=1;
    
    public static final int HORIZONTAL_SORT=2;
    
    public static final int VERTICAL_HORIZONTAL_SORT=3;
    
    public static final int HORIZONTAL_VERTICAL_SORT=4;
    
    private int sortType;
    
    
    public PointPairComparator(int sortType){
        if(sortType!=VERTICAL_SORT && sortType!=HORIZONTAL_SORT && 
                sortType!=VERTICAL_HORIZONTAL_SORT && sortType!=HORIZONTAL_VERTICAL_SORT)
	        throw new IllegalArgumentException("sort type "+sortType+" unknown");
	    this.sortType=sortType;
    }
    
    public int compare(PointPair p1,PointPair p2){
        switch(sortType)
        {case VERTICAL_SORT:
             {if(p1.getFirst().getY()>p2.getFirst().getY())
                  return(1);
              else
                  if(p1.getFirst().getY()<p2.getFirst().getY())
                      return(-1);
              return(0);
             }
         case HORIZONTAL_SORT:
             {if(p1.getFirst().getX()>p2.getFirst().getX())
                  return(1);
              else
                  if(p1.getFirst().getX()<p2.getFirst().getX())
                      return(-1);
              return(0);
             }
         case VERTICAL_HORIZONTAL_SORT:
             {if(p1.getFirst().getY()>p2.getFirst().getY())
                 return(1);
              else
                  if(p1.getFirst().getY()<p2.getFirst().getY())
                      return(-1);
              if(p1.getFirst().getX()>p2.getFirst().getX())
                  return(1);
              else
                  if(p1.getFirst().getX()<p2.getFirst().getX())
                     return(-1);
              return(0);
             }
         case HORIZONTAL_VERTICAL_SORT:
             {if(p1.getFirst().getX()>p2.getFirst().getX())
                  return(1);
              else
                  if(p1.getFirst().getX()<p2.getFirst().getX())
                      return(-1);
              if(p1.getFirst().getY()>p2.getFirst().getY())
                  return(1);
              else
                  if(p1.getFirst().getY()<p2.getFirst().getY())
                      return(-1);
              return(0);
             }
         default:
             {throw new UnsupportedOperationException("sort type "+sortType+" unknown");}
             }
    }
    
    public boolean equals(Object comparator){
        if(comparator==null || !(comparator instanceof PointPairComparator))
	        return(false);
	    return(this.sortType==((PointPairComparator)comparator).getSortType());
    }
    
    public final int hashCode(){
        return(this.sortType);
    }
    
    public final int getSortType(){
        return(sortType);
    }
}
