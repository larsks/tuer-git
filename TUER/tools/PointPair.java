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

import java.awt.Point;
import java.awt.geom.Point2D;

public final class PointPair implements Cloneable{


    private Point first;
    
    private Point last;
    

    public PointPair(){
        this(new Point(),new Point());
    }

    public PointPair(Point first,Point last){
        this.first=first;
        this.last=last;
    }

    public PointPair(PointPair p){
        this(new Point(p.first),new Point(p.last));
    }

    public final Point getFirst(){
        return(first);
    }

    public final Point getLast(){
        return(last);
    }
    
    public final boolean equals(Object o){
        if(o==null||!(o instanceof PointPair))
            return(false);
        else
            {PointPair p=(PointPair)o;
             return((first.equals(p.getFirst())&&last.equals(p.getLast()))
                    ||(first.equals(p.getLast())&&last.equals(p.getFirst())));
            }
    }

    public final Object clone(){
        return(new PointPair(new Point((int)this.first.getX(),(int)this.first.getY()),
                new Point((int)this.last.getX(),(int)this.last.getY())));
    }    

    public final boolean isLinkedTo(PointPair p){
        return(first.equals(p.getFirst())||last.equals(p.getLast())
                ||first.equals(p.getLast())||last.equals(p.getFirst()));
    }

    public final void merge(PointPair p){
        set(getMergedPointPair(this,p));  
    }

    public final static PointPair getMergedPointPair(PointPair p1,PointPair p2){
        if(p1.getFirst().equals(p2.getFirst()))
            return(new PointPair(p1.getLast(),p2.getLast()));
        else
            if(p1.getFirst().equals(p2.getLast()))
                return(new PointPair(p1.getLast(),p2.getFirst()));
            else
                if(p1.getLast().equals(p2.getFirst()))
                    return(new PointPair(p1.getFirst(),p2.getLast()));
                else
                    if(p1.getLast().equals(p2.getLast()))
                        return(new PointPair(p1.getFirst(),p2.getFirst()));
                    else
                        return(null);
    }

    public final void set(PointPair p){
        set(p.getFirst(),p.getLast());
    }

    public final void set(Point first,Point last){
        this.first.setLocation(first);
        this.last.setLocation(last);
    }

    public final void set(int x1,int y1,int x2,int y2){
        this.first.setLocation(x1,y1);
        this.last.setLocation(x2,y2);
    }

    public final void translate(int dx,int dy){
        this.first.translate(dx,dy);
        this.last.translate(dx,dy);
    }

    public final void swap(){
        Point tmp=new Point();
        tmp.setLocation(first);
        first.setLocation(last);
        last.setLocation(tmp);
    }

    public String toString(){
        StringBuffer tmp=new StringBuffer("");
        tmp.append("[");
        tmp.append("[");      
        tmp.append("[");
        tmp.append(first.x);
        tmp.append("]");       
        tmp.append("[");
        tmp.append(first.y);
        tmp.append("]");              
        tmp.append("]");
        tmp.append("[");
        tmp.append("[");
        tmp.append(last.x);
        tmp.append("]");
        tmp.append("[");
        tmp.append(last.y);
        tmp.append("]");        
        tmp.append("]");
        tmp.append("]");
    	return(tmp.toString());
    }
    
    public final int getSize(){
        return((int)Point2D.distance(first.getX(),first.getY(),last.getX(),last.getY()));
    }
    
    public final boolean contains(PointPair p){
        boolean result=false;
        if(first.y==last.y)
            {if(p.first.y==first.y && p.first.y==p.last.y)
                 if(first.x<=last.x)
                     {if(first.x<=p.first.x && p.first.x<=last.x && 
                         first.x<=p.last.x && p.last.x<=last.x)
                          result=true;
                     }   
                 else
                     {if(last.x<=p.first.x && p.first.x<=first.x && 
                         last.x<=p.last.x && p.last.x<=first.x)
                          result=true;
                     }
            }
        else
            if(first.x==last.x)
                {if(p.first.x==first.x && p.first.x==p.last.x)
                     if(first.y<=last.y)
                         {if(first.y<=p.first.y && p.first.y<=last.y && 
                             first.y<=p.last.y && p.last.y<=last.y)
                              result=true;
                         }   
                     else
                         {if(last.y<=p.first.y && p.first.y<=first.y && 
                             last.y<=p.last.y && p.last.y<=first.y)
                              result=true;
                         }                
                }
        return(result);
    }
}
