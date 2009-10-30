package jfpsm;

public final class CuboidParameters extends VolumeParameters{

    
    private static final long serialVersionUID = 1L;

    static{SerializationHelper.forceHandlingOfTransientModifiersForXMLSerialization(CuboidParameters.class);}
    
    private transient boolean dirty;
    
    private float[] offset;
    
    private float[] size;
    
    private boolean[] shownFaces;
    
    
    public CuboidParameters(){
        this(new float[]{0,0,0},new float[]{1,1,1},new boolean[]{true,true,true,true,true,true});
    }
    
    public CuboidParameters(float[] offset,float[] size,boolean[] shownFaces){
        this.offset=offset;
        this.size=size;
        this.shownFaces=shownFaces;
        markDirty();
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
        markDirty();
    }
    
    public final void setOffset(int index,float value){
        this.offset[index]=value;
        markDirty();
    }

    public final float[] getSize(){
        return(size);
    }

    public final void setSize(float[] size){
        this.size=size;
        markDirty();
    }
    
    public final void setSize(int index,float value){
        this.size[index]=value;
        markDirty();
    }

	public final boolean[] getShownFaces(){
		return(shownFaces);
	}

	public final void setShownFaces(boolean[] shownFaces){
		this.shownFaces=shownFaces;
	}
}
