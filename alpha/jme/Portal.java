package jme;

import com.jme.scene.Spatial;

import bean.NodeIdentifier;

final class Portal extends IdentifiedNode{

    
    private static final long serialVersionUID=1L;

    private Cell[] linkedCells;
    
    
    Portal(){
        this(NodeIdentifier.unknownID,NodeIdentifier.unknownID,NodeIdentifier.unknownID,NodeIdentifier.unknownID,null,null,null);
    }
    
    Portal(int levelID,int networkID,int cellID,int secondaryCellID,Cell c1,Cell c2,Spatial model){
        super(levelID,networkID,cellID,secondaryCellID);
        linkedCells=new Cell[]{c1,c2};
        if(model!=null)
            {//TODO: store the vertices of the triangles inside a local variable
             attachChild(model);
            }
    }
    
    
    Cell getCellAt(int index){
        return(linkedCells[index]);
    }
}
