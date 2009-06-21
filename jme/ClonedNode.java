package jme;

import com.jme.math.Quaternion;
import com.jme.math.Vector3f;
import com.jme.scene.Node;
import com.jme.scene.SharedNode;

public class ClonedNode extends SharedNode{

    
    private static final long serialVersionUID = 1L;

    public ClonedNode(Node node){
        this(node.getName(),node);
    }
    
    public ClonedNode(String name,Node node){
        super(name,node);
        //binds the transforms
        super.setLocalRotation(node.getLocalRotation());
        super.setLocalTranslation(node.getLocalTranslation());
        super.setLocalScale(node.getLocalScale());
        super.setLocalRotation(node.getLocalRotation());
    }
    
    
    @Override
    public void setLocalTranslation(Vector3f localTranslation){
        this.localTranslation.set(localTranslation);
        this.worldTranslation.set(this.localTranslation);
    }
    
    @Override
    public void setLocalScale(Vector3f localScale){
        this.localScale.set(localScale);
        this.worldScale.set(this.localScale);
    }
    
    @Override
    public void setLocalRotation(Quaternion quaternion){
        this.localRotation.set(quaternion);
        this.worldRotation.set(this.localRotation);
    }
}
