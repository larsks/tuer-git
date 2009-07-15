package main;

public final class AnimationInfoBean implements XMLTransportableWrapper<AnimationInfo>{

    
    private String strName;
    
    private int startFrame;
    
    private int endFrame;
    
    private int frameCount;
    
    private int framesPerSecond;
    
    
    public AnimationInfoBean(){}
    
    @Override
    public AnimationInfo getWrappedObject() {
        return(new AnimationInfo(strName,startFrame,endFrame,framesPerSecond));
    }

    @Override
    public void wrap(AnimationInfo ai) {
        strName=ai.getStrName();
        startFrame=ai.getStartFrame();
        endFrame=ai.getEndFrame();
        frameCount=ai.getFrameCount();
        framesPerSecond=ai.getFramesPerSecond();
    }

    public final String getStrName() {
        return strName;
    }

    public final void setStrName(String strName) {
        this.strName = strName;
    }

    public final int getStartFrame() {
        return startFrame;
    }

    public final void setStartFrame(int startFrame) {
        this.startFrame = startFrame;
    }

    public final int getEndFrame() {
        return endFrame;
    }

    public final void setEndFrame(int endFrame) {
        this.endFrame = endFrame;
    }

    public final int getFrameCount() {
        return frameCount;
    }

    public final void setFrameCount(int frameCount) {
        this.frameCount = frameCount;
    }

    public final int getFramesPerSecond() {
        return framesPerSecond;
    }

    public final void setFramesPerSecond(int framesPerSecond) {
        this.framesPerSecond = framesPerSecond;
    }
}
