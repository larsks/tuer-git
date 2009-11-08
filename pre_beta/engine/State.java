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
package engine;

import com.ardor3d.input.logical.LogicalLayer;
import com.ardor3d.scenegraph.Node;
import com.ardor3d.scenegraph.extension.SwitchNode;

public class State{

    
    /**
     * layer used to handle the input
     */
    private LogicalLayer logicalLayer;
    
    /**
     * root node
     */
    private final Node root;
    
    
    public State(){
        this.logicalLayer=new LogicalLayer();
        root=new Node();
    }
    
    
    public final boolean isEnabled(){
        final int index=getStateIndex();
        return(index==-1?false:getSwitchNode().getVisible(index));
    }
    
    public void setEnabled(final boolean enabled){
        final boolean wasEnabled=isEnabled();
        if(wasEnabled!=enabled)
            {final int index=getStateIndex();
             if(index!=-1)
                 getSwitchNode().setVisible(index,enabled);
            }       
    }
    
    public void init(){}
    
    private final SwitchNode getSwitchNode(){
        return((SwitchNode)root.getParent());
    }
    
    private final int getStateIndex(){
        return(getSwitchNode().getChildIndex(root));
    }
    
    final Node getRoot(){
        return(root);
    }
    
    final LogicalLayer getLogicalLayer(){
        return(logicalLayer);
    }
}
