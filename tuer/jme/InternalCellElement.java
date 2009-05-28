package jme;

import com.jme.scene.Geometry;
import com.jme.scene.Node;
import com.jme.scene.SharedNode;

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
final class InternalCellElement extends SharedNode{

    
    private static final long serialVersionUID = 1L;
    
    /**
     * unique node that be used to build shared
     * nodes for use in several places in the scenegraph
     */
    private Node sharableNode;
    
    InternalCellElement(Node node){
        this(node.getName(),node);
    }
    
    InternalCellElement(String name,Node node){
        super(name,node);
        if(node instanceof SharedNode)
            throw new IllegalArgumentException("A shared node cannot be shared by an internal cell element");
        else
            sharableNode=node;
    }
    
    InternalCellElement(String name,Geometry geometry){
        super(name,getNodeWithSingleGeometry(geometry));
        sharableNode=geometry.getParent();
    }
    
    
    private static final Node getNodeWithSingleGeometry(Geometry geometry){
        Node node=new Node(geometry.getName());
        node.attachChild(geometry);
        node.updateGeometricState(0.0f,true);
        node.updateRenderState();
        return(node);
    }
    
    final Node getSharableNode(){
        return(sharableNode);
    }
}
