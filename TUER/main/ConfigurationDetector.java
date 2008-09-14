package main;

import java.nio.IntBuffer;

import javax.media.opengl.GL;

import com.sun.opengl.util.BufferUtil;

final class ConfigurationDetector {

    
    private boolean isAlphaTestSupported;
    
    private String openglVersion;
    
    private boolean isVBOsupported;
    
    private boolean isVertexArraySupported;
    
    private boolean isDisplayListSupported;
    
    private boolean isMultiDrawSupported;
    
    private boolean isShaderSupported;
    
    private int maxTextureSize;
    
    
    ConfigurationDetector(GL gl){
        this.isAlphaTestSupported=gl.isFunctionAvailable("glAlphaFunc");
        if(gl.isExtensionAvailable("GL_VERSION_2_1"))
            openglVersion="2.1";
        else
            if(gl.isExtensionAvailable("GL_VERSION_2_0"))
                openglVersion="2.0";
            else
                if(gl.isExtensionAvailable("GL_VERSION_1_5"))
                    openglVersion="1.5";
                else
                    if(gl.isExtensionAvailable("GL_VERSION_1_4"))
                        openglVersion="1.4";
                    else
                        if(gl.isExtensionAvailable("GL_VERSION_1_3"))
                            openglVersion="1.3";
                        else
                            if(gl.isExtensionAvailable("GL_VERSION_1_2"))
                                openglVersion="1.2";
                            else
                                if(gl.isExtensionAvailable("GL_VERSION_1_1"))
                                    openglVersion="1.1";
                                else
                                    openglVersion="unknown";
        isVBOsupported=((gl.isExtensionAvailable("GL_ARB_vertex_buffer_object")
                || gl.isExtensionAvailable("GL_EXT_vertex_buffer_object"))
               && (gl.isFunctionAvailable("glBindBufferARB")
                || gl.isFunctionAvailable("glBindBuffer"))
               && (gl.isFunctionAvailable("glBufferDataARB")
                || gl.isFunctionAvailable("glBufferData"))
               && (gl.isFunctionAvailable("glDeleteBuffersARB")
                || gl.isFunctionAvailable("glDeleteBuffers"))
               && (gl.isFunctionAvailable("glGenBuffersARB")
                || gl.isFunctionAvailable("glGenBuffers")));
        isVertexArraySupported=(gl.isExtensionAvailable("GL_EXT_vertex_array")
                && gl.isFunctionAvailable("glColorPointer")
                && gl.isFunctionAvailable("glDrawArrays")
                && gl.isFunctionAvailable("glDrawElements")
                && gl.isFunctionAvailable("glDrawRangeElements")
                && gl.isFunctionAvailable("glIndexPointer")
                && gl.isFunctionAvailable("glNormalPointer")
                && gl.isFunctionAvailable("glTexCoordPointer")
                && gl.isFunctionAvailable("glVertexPointer"));
        isDisplayListSupported=(gl.isFunctionAvailable("glCallList")
                && gl.isFunctionAvailable("glCallLists")
                && gl.isFunctionAvailable("glDeleteLists")
                && gl.isFunctionAvailable("glGenLists")
                && gl.isFunctionAvailable("glNewList")
                && gl.isFunctionAvailable("glEndList"));
        isMultiDrawSupported=gl.isFunctionAvailable("glMultiDrawArrays");
        isShaderSupported=(gl.isFunctionAvailable("glCreateShader")
                && gl.isFunctionAvailable("glShaderSource")
                && gl.isFunctionAvailable("glCompileShader")
                && gl.isFunctionAvailable("glCreateProgram")
                && gl.isFunctionAvailable("glAttachShader")
                && gl.isFunctionAvailable("glLinkProgram")
                && gl.isFunctionAvailable("glValidateProgram")
                && gl.isFunctionAvailable("glUseProgram"));
        IntBuffer buffer=BufferUtil.newIntBuffer(1);   
        gl.glGetIntegerv(GL.GL_MAX_TEXTURE_SIZE,buffer);
        buffer.position(0);
        maxTextureSize=buffer.get();
    }
    
    
    boolean isAlphaTestSupported(){
        return(isAlphaTestSupported);
    }
    
    boolean isVBOsupported(){
        return(isVBOsupported);
    }
    
    boolean isVertexArraySupported(){
        return(isVertexArraySupported);
    }
    
    boolean isDisplayListSupported(){
        return(isDisplayListSupported);
    }
    
    boolean isMultiDrawSupported(){
        return(isMultiDrawSupported);
    }
    
    boolean isShaderSupported(){
        return(isShaderSupported);
    }
    
    String getOpenGLVersion(){
        return(openglVersion);
    }


    public final int getMaxTextureSize(){
        return(maxTextureSize);
    }
    
    
}
