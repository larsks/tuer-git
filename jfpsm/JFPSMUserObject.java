package jfpsm;

public abstract class JFPSMUserObject extends Namable implements Dirtyable,Resolvable{

    
    private static final long serialVersionUID=1L;

    
    public JFPSMUserObject(){
        super("");
    }
    
    public JFPSMUserObject(String name){
        super(name);
    }

    abstract boolean isRemovable();
    
    abstract boolean isOpenable();
    
    abstract boolean canInstantiateChildren();
    
    public Viewer createViewer(final Project project,final ProjectManager projectManager){
    	return(null);
    }
    
    @Override
    public final void setName(String name){
        super.setName(name);
        //mark the entity as dirty when the user renames it
        markDirty();
    }
    
    @Override
    public void resolve(){}
}
