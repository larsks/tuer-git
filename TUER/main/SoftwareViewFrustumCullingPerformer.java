package main;

import java.nio.FloatBuffer;
import java.util.Arrays;

import javax.media.opengl.GL;

import com.sun.opengl.util.BufferUtil;

public final class SoftwareViewFrustumCullingPerformer{
    
    
    private FloatBuffer projectionMatrix;
    
    private FloatBuffer frustumMatrix;
    
    private GL gl;
    
    private float tolerance;
    
    
    SoftwareViewFrustumCullingPerformer(GL gl,float tolerance){
        this.gl=gl;
        this.tolerance=tolerance;
        this.projectionMatrix=BufferUtil.newFloatBuffer(16);
        this.frustumMatrix=BufferUtil.newFloatBuffer(24);
        updateProjectionMatrix();
    }

    final void updateProjectionMatrix(){
        gl.glPushAttrib(GL.GL_TRANSFORM_BIT);
        gl.glMatrixMode(GL.GL_PROJECTION);
        gl.glPushMatrix();
        gl.glScalef(1+tolerance,1+tolerance,1+tolerance);
        this.projectionMatrix.rewind();
        gl.glGetFloatv(GL.GL_PROJECTION_MATRIX,projectionMatrix);
        this.projectionMatrix.rewind();
        gl.glPopMatrix();
        gl.glPopAttrib();
    }
    
    /**
     * Cyberboy2054 inspired me. I optimized his method to let OpenGL perform
     * the multiplication for me as I'm lazy. Call this method after gluLookAt
     */
    final void computeViewFrustum(){
        FloatBuffer clipMatrix=BufferUtil.newFloatBuffer(16);
        float normalizationFactor;
        //We assume that the model view matrix is selected
        gl.glPushMatrix();
        //Use glMultTransposeMatrix if it doesn't work
        gl.glMultMatrixf(projectionMatrix);
        gl.glGetFloatv(GL.GL_MODELVIEW_MATRIX,clipMatrix);
        gl.glPopMatrix();
        projectionMatrix.rewind();
        clipMatrix.rewind();
        frustumMatrix.rewind();
        //right planar surface
        frustumMatrix.put(clipMatrix.get(3)-clipMatrix.get(0));
        frustumMatrix.put(clipMatrix.get(7)-clipMatrix.get(4));
        frustumMatrix.put(clipMatrix.get(11)-clipMatrix.get(8));
        frustumMatrix.put(clipMatrix.get(15)-clipMatrix.get(12));
        normalizationFactor=(float)Math.sqrt((double)(frustumMatrix.get(0)*frustumMatrix.get(0)+frustumMatrix.get(1)*frustumMatrix.get(1)+frustumMatrix.get(2)*frustumMatrix.get(2)));
        frustumMatrix.put(0,frustumMatrix.get(0)/normalizationFactor);
        frustumMatrix.put(1,frustumMatrix.get(1)/normalizationFactor);
        frustumMatrix.put(2,frustumMatrix.get(2)/normalizationFactor);
        frustumMatrix.put(3,frustumMatrix.get(3)/normalizationFactor);
        //left planar surface
        frustumMatrix.put(clipMatrix.get(3)+clipMatrix.get(0));
        frustumMatrix.put(clipMatrix.get(7)+clipMatrix.get(4));
        frustumMatrix.put(clipMatrix.get(11)+clipMatrix.get(8));
        frustumMatrix.put(clipMatrix.get(15)+clipMatrix.get(12));
        normalizationFactor=(float)Math.sqrt((double)(frustumMatrix.get(4)*frustumMatrix.get(4)+frustumMatrix.get(5)*frustumMatrix.get(5)+frustumMatrix.get(6)*frustumMatrix.get(6)));
        frustumMatrix.put(4,frustumMatrix.get(4)/normalizationFactor);
        frustumMatrix.put(5,frustumMatrix.get(5)/normalizationFactor);
        frustumMatrix.put(6,frustumMatrix.get(6)/normalizationFactor);
        frustumMatrix.put(7,frustumMatrix.get(7)/normalizationFactor);
        //bottom planar surface
        frustumMatrix.put(clipMatrix.get(3)+clipMatrix.get(1));
        frustumMatrix.put(clipMatrix.get(7)+clipMatrix.get(5));
        frustumMatrix.put(clipMatrix.get(11)+clipMatrix.get(9));
        frustumMatrix.put(clipMatrix.get(15)+clipMatrix.get(13));
        normalizationFactor=(float)Math.sqrt((double)(frustumMatrix.get(8)*frustumMatrix.get(8)+frustumMatrix.get(9)*frustumMatrix.get(9)+frustumMatrix.get(10)*frustumMatrix.get(10)));
        frustumMatrix.put(8,frustumMatrix.get(8)/normalizationFactor);
        frustumMatrix.put(9,frustumMatrix.get(9)/normalizationFactor);
        frustumMatrix.put(10,frustumMatrix.get(10)/normalizationFactor);
        frustumMatrix.put(11,frustumMatrix.get(11)/normalizationFactor);
        //top planar surface
        frustumMatrix.put(clipMatrix.get(3)-clipMatrix.get(1));
        frustumMatrix.put(clipMatrix.get(7)-clipMatrix.get(5));
        frustumMatrix.put(clipMatrix.get(11)-clipMatrix.get(9));
        frustumMatrix.put(clipMatrix.get(15)-clipMatrix.get(13));
        normalizationFactor=(float)Math.sqrt((double)(frustumMatrix.get(12)*frustumMatrix.get(12)+frustumMatrix.get(13)*frustumMatrix.get(13)+frustumMatrix.get(14)*frustumMatrix.get(14)));
        frustumMatrix.put(12,frustumMatrix.get(12)/normalizationFactor);
        frustumMatrix.put(13,frustumMatrix.get(13)/normalizationFactor);
        frustumMatrix.put(14,frustumMatrix.get(14)/normalizationFactor);
        frustumMatrix.put(15,frustumMatrix.get(15)/normalizationFactor);
        //far planar surface
        frustumMatrix.put(clipMatrix.get(3)-clipMatrix.get(2));
        frustumMatrix.put(clipMatrix.get(7)-clipMatrix.get(6));
        frustumMatrix.put(clipMatrix.get(11)-clipMatrix.get(10));
        frustumMatrix.put(clipMatrix.get(15)-clipMatrix.get(14));
        normalizationFactor=(float)Math.sqrt((double)(frustumMatrix.get(16)*frustumMatrix.get(16)+frustumMatrix.get(17)*frustumMatrix.get(17)+frustumMatrix.get(18)*frustumMatrix.get(18)));
        frustumMatrix.put(16,frustumMatrix.get(16)/normalizationFactor);
        frustumMatrix.put(17,frustumMatrix.get(17)/normalizationFactor);
        frustumMatrix.put(18,frustumMatrix.get(18)/normalizationFactor);
        frustumMatrix.put(19,frustumMatrix.get(19)/normalizationFactor);
        //near planar surface
        frustumMatrix.put(clipMatrix.get(3)+clipMatrix.get(2));
        frustumMatrix.put(clipMatrix.get(7)+clipMatrix.get(6));
        frustumMatrix.put(clipMatrix.get(11)+clipMatrix.get(10));
        frustumMatrix.put(clipMatrix.get(15)+clipMatrix.get(14));
        normalizationFactor=(float)Math.sqrt((double)(frustumMatrix.get(20)*frustumMatrix.get(20)+frustumMatrix.get(21)*frustumMatrix.get(21)+frustumMatrix.get(22)*frustumMatrix.get(22)));
        frustumMatrix.put(20,frustumMatrix.get(20)/normalizationFactor);
        frustumMatrix.put(21,frustumMatrix.get(21)/normalizationFactor);
        frustumMatrix.put(22,frustumMatrix.get(22)/normalizationFactor);
        frustumMatrix.put(23,frustumMatrix.get(23)/normalizationFactor);
        frustumMatrix.rewind();
    }
    
    /**
     * 
     * @param p1
     * @param p2
     * @param p3
     * @param p4
     * @return true if the quad is in the frustum or clipped by it
     */
    public final boolean isQuadInViewFrustum(float[] p1,float[] p2,float[] p3,float[] p4,int dataOffset){
        //an out flag per vertex, indicate whether a point is inside the frustum
        int[] outFlag=new int[4];
        //contains the coefficients of the planar surface equation
        float[] frustumPlane=new float[4];
        Arrays.fill(outFlag,0);
        frustumMatrix.rewind();
        int[] shiftedIndicesTable=new int[]{0+dataOffset,1+dataOffset,2+dataOffset};
        //iterate on each planar surface of the frustum
        for(int i=0; i<6; i++)
            {frustumMatrix.get(frustumPlane);
             //the dot product allows to get the distance from plane
             if(dot(p1[shiftedIndicesTable[0]],p1[shiftedIndicesTable[1]],p1[shiftedIndicesTable[2]],1,frustumPlane[0],frustumPlane[1],frustumPlane[2],frustumPlane[3])<=0)
                 outFlag[0]|=(1<<i);
             if(dot(p2[shiftedIndicesTable[0]],p2[shiftedIndicesTable[1]],p2[shiftedIndicesTable[2]],1,frustumPlane[0],frustumPlane[1],frustumPlane[2],frustumPlane[3])<=0)
                 outFlag[1]|=(1<<i);
             if(dot(p3[shiftedIndicesTable[0]],p3[shiftedIndicesTable[1]],p3[shiftedIndicesTable[2]],1,frustumPlane[0],frustumPlane[1],frustumPlane[2],frustumPlane[3])<=0)
                 outFlag[2]|=(1<<i);
             if(dot(p4[shiftedIndicesTable[0]],p4[shiftedIndicesTable[1]],p4[shiftedIndicesTable[2]],1,frustumPlane[0],frustumPlane[1],frustumPlane[2],frustumPlane[3])<=0)
                 outFlag[3]|=(1<<i);
            }
        frustumMatrix.rewind();
        //if all vertices are out and if they are all in the wrong side of the planes
        return(!((outFlag[0]!=0&&outFlag[1]!=0&&outFlag[2]!=0&&outFlag[3]!=0)&&
                ((outFlag[0]&outFlag[1]&outFlag[2]&outFlag[3])!=0)));
    }
    
    final boolean isBoxInViewFrustum(float[] p1,float[] p2,float[] p3,float[] p4,
            float[] p5,float[] p6,float[] p7,float[] p8){
        return(isSolidInViewFrustum(p1,p2,p3,p4,p5,p6,p7,p8));
    }
    
    /*
     * Pass this method the convex hull of a solid
     */
    final boolean isSolidInViewFrustum(float[] ... point){       
        //an out flag per vertex, indicate whether a point is inside the frustum
        int[] outFlag=new int[point.length];
        //contains the coefficients of the planar surface equation
        float[] frustumPlane=new float[4];
        Arrays.fill(outFlag,0);
        frustumMatrix.rewind();
        //iterate on each planar surface of the frustum
        for(int i=0; i<6; i++)
            {frustumMatrix.get(frustumPlane);
             //the dot product allows to get the distance from plane
             for(int j=0;j<outFlag.length;j++)
                 if(dot(point[j][0],point[j][1],point[j][2],1,frustumPlane[0],frustumPlane[1],frustumPlane[2],frustumPlane[3])<=0)
                     outFlag[j]|=(1<<i);
            }
        frustumMatrix.rewind();
        //if all vertices are out and if they are all in the wrong side of the planes
        boolean out=true;
        //3E in hexadecimal = 111111 in binary, there are 6 surfaces
        int outResultingFlag=0x3E;
        for(int j=0;j<outFlag.length;j++)
            {out&=(outFlag[j]!=0);
             outResultingFlag&=outFlag[j];
            }       
        return(!out&&outResultingFlag!=0);
    }
    
    final boolean isSphereInViewFrustum(float[] center,float radius){
        float[] frustumPlane=new float[4];
        for(int i=0; i<6; i++)
            {frustumMatrix.get(frustumPlane);
             if(dot(center[0],center[1],center[2],1,frustumPlane[0],frustumPlane[1],frustumPlane[2],frustumPlane[3])<=-radius)
                 return(false);
            }
        return(true);
    }
    
    final boolean isPointInViewFrustum(float[] point){
        float[] frustumPlane=new float[4];
        for(int i=0; i<6; i++)
            {frustumMatrix.get(frustumPlane);
             if(dot(point[0],point[1],point[2],1,frustumPlane[0],frustumPlane[1],frustumPlane[2],frustumPlane[3])<=0)
                 return(false);
            }
        return(true);
    }
    
    static final float dot(float x1,float y1,float z1,float w1,float x2,float y2,float z2,float w2){
        //NB: avoid using w=0 for vertices because it is not mathematically correct
        return(x1*x2+y1*y2+z1*z2+w1*w2);
    }
}
