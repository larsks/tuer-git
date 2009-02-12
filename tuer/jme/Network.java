package jme;

final class Network extends IdentifiedNode{

    
    private static final long serialVersionUID = 1L;

    
    Network(){
        this(unknownID,unknownID);
    }

    Network(int levelID,int networkID){
        super(levelID,networkID);
        
    }

}
