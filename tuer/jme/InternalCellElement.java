package jme;

import com.jme.scene.Node;
import com.jme.scene.SharedNode;
import com.jme.scene.Spatial;

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
final class InternalCellElement extends Node{

    
    private static final long serialVersionUID = 1L;
    
    /**
     * unique node that be used to build shared
     * nodes for use in several places in the scenegraph
     */
    private Node sharableNode;
    
    
    /**
     * 
     * @param spatial JME spatial that has to be wrapped
     * @param share   indicates whether this element is only inside a 
     *                single cell or can be shared (only geometric 
     *                elements composing the fixed structure of a cell
     *                should not be shared)
     */
    InternalCellElement(Spatial spatial,boolean share){
        this(spatial.getName(),spatial,share);
    }
    
    /**
     * 
     * @param name    name of the element
     * @param spatial JME spatial that has to be wrapped
     * @param share   indicates whether this element is only inside a 
     *                single cell or can be shared (only geometric 
     *                elements composing the fixed structure of a cell
     *                should not be shared)
     */
    InternalCellElement(String name,Spatial spatial,boolean share){
        super(name);
        if(share)
            {if(!(spatial instanceof Node))
                 {//FIXME: support TriMesh?
                  throw new IllegalArgumentException("Only a node cannot be shared by an internal cell element");
                 }
             else
                 {Node node=(Node)spatial;
                  if(node instanceof SharedNode)
                      throw new IllegalArgumentException("A shared node cannot be shared by an internal cell element");
                  else
                      {sharableNode=node;
                       attachChild(new SharedNode(name,node));
                      }
                 }
            }
        else
            {sharableNode=null;
             attachChild(spatial);
            }
    }
    
    final Node getSharableNode(){
        return(sharableNode);
    }
    
    @Override
    public final int attachChild(Spatial child){
        if(child!=null&&getChildren()!=null&&getChildren().size()==1)
            throw new IllegalArgumentException("an internal cell element can contain only one child");
        return(super.attachChild(child));
    }
    
    @Override
    public final int attachChildAt(Spatial child, int index){
        if(child!=null&&((getChildren()!=null&&getChildren().size()==1)||index>0))
            throw new IllegalArgumentException("an internal cell element can contain only one child");
        return(super.attachChildAt(child,index));
    }
}
