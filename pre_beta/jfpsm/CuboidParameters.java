package jfpsm;

public final class CuboidParameters extends VolumeParameters{

    
    private static final long serialVersionUID = 1L;

    static{SerializationHelper.forceHandlingOfTransientModifiersForXMLSerialization(CuboidParameters.class);}
    
    private transient boolean dirty;
    
    private float[] offset;
    
    private float[] size;
    
    
    public CuboidParameters(){
        this(new float[]{0,0,0},new float[]{1,1,1});
    }
    
    public CuboidParameters(float[] offset,float[] size){
        this.offset=offset;
        this.size=size;
    }
    
    
    @Override
    final VolumeType getVolumeType(){
        return(VolumeType.CUBOID);
    }

    @Override
    public final boolean isDirty(){
        return(dirty);
    }

    @Override
    public final void markDirty(){
        dirty=true;
    }

    @Override
    public final void unmarkDirty(){
        dirty=false;
    }

    public final float[] getOffset(){
        return(offset);
    }

    public final void setOffset(float[] offset){
        this.offset=offset;
    }
    
    public final void setOffset(int index,float value){
        this.offset[index]=value;
    }

    public final float[] getSize(){
        return(size);
    }

    public final void setSize(float[] size){
        this.size=size;
    }
    
    public final void setSize(int index,float value){
        this.size[index]=value;
    }
}
