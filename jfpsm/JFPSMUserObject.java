package jfpsm;

abstract class JFPSMUserObject extends Namable implements Dirtyable,Resolvable{

    
    private static final long serialVersionUID=1L;

    
    JFPSMUserObject(){
        super("");
    }
    
    JFPSMUserObject(String name){
        super(name);
    }

    abstract boolean isRemovable();
    
    abstract boolean isOpenable();
    
    abstract boolean canInstantiateChildren();
}
