package jme;

import bean.NodeIdentifier;

final class Network extends IdentifiedNode{

    
    private static final long serialVersionUID = 1L;

    
    Network(){
        this(NodeIdentifier.unknownID,NodeIdentifier.unknownID);
    }

    Network(int levelID,int networkID){
        super(levelID,networkID);
        
    }

}
