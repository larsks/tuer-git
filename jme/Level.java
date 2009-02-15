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
package jme;

import com.jme.scene.Spatial;

import bean.NodeIdentifier;

final class Level extends IdentifiedNode{

    
    private static final long serialVersionUID=1L;
    
    Level(){
        this(NodeIdentifier.unknownID);
    }
    
    Level(int levelID){
        super(levelID);
    }
    
    @Override
    public final int attachChild(Spatial child){
        if(child!=null&&!(child instanceof Network))
            throw new IllegalArgumentException("this child is not an instance of Network");
        return(super.attachChild(child));
    }
    
    @Override
    public int attachChildAt(Spatial child, int index){
        if(child!=null&&!(child instanceof Network))
            throw new IllegalArgumentException("this child is not an instance of Network");
        return(super.attachChildAt(child,index));
    }
}
