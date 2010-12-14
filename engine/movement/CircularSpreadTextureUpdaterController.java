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
package engine.movement;

import java.awt.Color;
import java.awt.Point;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map.Entry;
import com.ardor3d.renderer.RenderContext;
import com.ardor3d.renderer.Renderer;

public final class CircularSpreadTextureUpdaterController extends TextureUpdaterController{

    private static final long serialVersionUID=1L;

    /**location from where we start spreading*/
    private Point spreadCenter;
    
    
    public CircularSpreadTextureUpdaterController(){}

    public CircularSpreadTextureUpdaterController(String imageResourceName,
            MovementEquation equation,
            HashMap<Color, Color> colorSubstitutionTable,
            final Point spreadCenter,final Renderer renderer,final RenderContext renderContext){
        super(imageResourceName,equation,colorSubstitutionTable,renderer,renderContext);
        this.spreadCenter=spreadCenter;
    }

    @Override
    protected Comparator<Entry<Point, Color>> getColoredPointComparator() {
        return(new CenteredColoredPointComparator(spreadCenter));
    }
    
    private static final class CenteredColoredPointComparator implements Comparator<Entry<Point,Color>>{
        
        private final Point spreadCenter;
        
        private CenteredColoredPointComparator(final Point spreadCenter){
            this.spreadCenter=spreadCenter;
        }
        
        
        @Override
        public final int compare(final Entry<Point, Color> o1,
                                 final Entry<Point, Color> o2){
            final Point p1=o1.getKey();
            final Point p2=o2.getKey();
            double d1=p1.distance(spreadCenter);
            double d2=p2.distance(spreadCenter);
            return(d1==d2?0:d1<d2?-1:1);
        } 
    }

    public final Point getSpreadCenter(){
        return(spreadCenter);
    }

    public final void setSpreadCenter(final Point spreadCenter){
        this.spreadCenter=spreadCenter;
    }
}
