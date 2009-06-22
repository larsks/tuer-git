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

import com.jme.scene.Geometry;
import com.jme.scene.Node;

/**
 * Node added into a cell. It is used
 * to ease the share of nodes between cells by being able to retrieve
 * the sharable node used to build a shared node and to make a 
 * difference between nodes that are always linked to a single cell 
 * (often walls or elements composing the structure of the cell, 
 * parts of the container) and nodes that can be linked to several cells
 * (contained elements that can move from a cell to another one or that can
 * overlap several cells) to avoid drawing several times the same 
 * nodes. It has only one child.
 * 
 * @author Julien Gouesse
 *
 */
final class InternalCellElement extends ClonedNode{

    
    private static final long serialVersionUID = 1L;
    
    /**
     * unique node that be used to build shared
     * nodes for use in several places in the scenegraph
     */
    private Node sharableNode;
    
    private boolean shared;
    
    InternalCellElement(Node node,boolean shared){
        this(node.getName(),node,shared);
    }
    
    InternalCellElement(String name,Node node,boolean shared){
        super(name,node);
        this.shared=shared;
        this.sharableNode=node;
        //set its cull hint at INHERIT by default
        //as this node is visible when its parent is visible
        setCullHint(CullHint.Inherit);
    }
    
    InternalCellElement(Geometry geometry,boolean shared){
        this(geometry.getName(),geometry,shared);
    }
    
    InternalCellElement(String name,Geometry geometry,boolean shared){
        super(name,getNodeWithSingleGeometry(geometry));
        this.shared=shared;
        sharableNode=geometry.getParent();
        //set its cull hint at INHERIT by default
        //as this node is visible when its parent is visible
        setCullHint(CullHint.Inherit);
    }
    
    
    private static final Node getNodeWithSingleGeometry(Geometry geometry){
        Node node=new Node(geometry.getName());
        node.attachChild(geometry);
        node.updateGeometricState(0.0f,true);
        node.updateRenderState();
        return(node);
    }
    
    final boolean isShared(){
        return(shared);
    }
    
    final Node getSharableNode(){
        return(sharableNode);
    }
}
