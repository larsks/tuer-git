package jme;

import bean.NodeIdentifier;

final class Portal extends IdentifiedNode{

    
    private static final long serialVersionUID=1L;

    private Cell[] linkedCells;
    
    
    Portal(){
        this(NodeIdentifier.unknownID,NodeIdentifier.unknownID,NodeIdentifier.unknownID,NodeIdentifier.unknownID,null,null);
    }
    
    Portal(int levelID,int networkID,int cellID,int secondaryCellID,Cell c1,Cell c2){
        super(levelID,networkID,cellID,secondaryCellID);
        linkedCells=new Cell[]{c1,c2};
    }
    
    
    Cell getCellAt(int index){
        return(linkedCells[index]);
    }
}
