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

final class ZoomParameters{
    
    
    private int factor;
    
    private int width;
    
    private int height;
    
    /**
     * center abscissa in the absolute reference
     */
    private int centerx;
    
    private int centery;

    
    ZoomParameters(int factor,int width,int height){
        this.factor=factor;
        this.width=width;
        this.height=height;
        this.centerx=width/2;
        this.centery=height/2;
    }
    
    
    final void setFactor(int factor){
        this.factor=factor;
    }
    
    final int getFactor(){
        return(factor);
    }

    final int getCenterx(){
        return(centerx);
    }
    
    final int getWidth(){
        return(width);
    }
    
    final int getHeight(){
        return(height);
    }
    
    final int getAbsoluteXFromRelativeX(int relativeX){
        return(centerx-(width/(2*factor))+(relativeX/factor));
    }
    
    final int getAbsoluteYFromRelativeY(int relativeY){
        return(centery-(height/(2*factor))+(relativeY/factor));
    }

    /**
     * Set the center abscissa in the absolute reference
     * @param centerx center abscissa in the absolute reference
     */
    final void setCenterx(int centerx){
        this.centerx=Math.max(((width/factor)/2),Math.min(centerx,width-1-((width/factor)/2)));
    }

    final int getCentery(){
        return(centery);
    }

    final void setCentery(int centery){
        this.centery=Math.max(((height/factor)/2),Math.min(centery,height-1-((height/factor)/2)));
    }
}
