package jfpsm;

public final class EngineServiceSeeker implements I3DServiceSeeker{

    
    private static final EngineServiceSeeker instance=new EngineServiceSeeker();
    
    private I3DServiceSeeker delegate;
    
    
    @Override
    public void bind3DServiceSeeker(I3DServiceSeeker seeker){
        delegate=seeker;
    }

    public static final EngineServiceSeeker getInstance(){
        return(instance);
    }
    
    public final void dummyTest(){
        delegate.dummyTest();
    }
}
