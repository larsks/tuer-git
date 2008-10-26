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

/**
 * This class has the role of the controller
 * (in the meaning of the design pattern "MVC") 
 * of a 3D object
 * @author Julien Gouesse
 */

package main;

import java.nio.FloatBuffer;
import java.util.List;

class Object3DController{

    
    private Object3DModel model;
    
    private Object3DView view;
    
    
    Object3DController(){
        this.model=null;
        this.view=null;
    }
    
    Object3DController(Object3DModel model,Object3DView view){
        this.model=model;
        this.model.setController(this);
        this.view=view;
        this.view.setController(this);
    }
    
    
    void dispose(){
        if(this.model!=null)
            {this.model.setController(null);
             this.model=null;            
            }
        if(this.view!=null)
            {this.view.setController(null);
             this.view=null;            
            }       
    }
    
    int getCurrentFrameIndex(){
        return(this.model.getCurrentFrameIndex());
    }
    
    float getX(){
        return(this.model.getX());
    }
    
    float getY(){
        return(this.model.getY());
    }
    
    float getZ(){
        return(this.model.getZ());
    }
    
    float getHorizontalDirection(){
        return(this.model.getHorizontalDirection());
    }
    
    float getVerticalDirection(){
        return(this.model.getVerticalDirection());
    }
    
    List<FloatBuffer> getCoordinatesBuffersList(){
        return(this.model.getCoordinatesBuffersList());
    }
}
