/*This program is free software; you can redistribute it and/or
  modify it under the terms of the GNU General Public License
  as published by the Free Software Foundation, version 2
  of the License.
  This program is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  GNU General Public License for more details.
  You should have received a copy of the GNU General Public License
  along with this program; if not, write to the Free Software
  Foundation, Inc., 59 Temple Place - Suite 330, Boston,
  MA 02111-1307, USA.
*/
package tools;

import java.nio.FloatBuffer;

import com.jogamp.opengl.glu.GLU;
import com.jogamp.opengl.glu.GLUquadric;

import com.jogamp.common.nio.Buffers;

import jogamp.opengl.glu.GLUquadricImpl;



public final class ExtendedGLU{

    
    public final static FloatBuffer computeCylinder(GLUquadric quad,float baseRadius, 
            float topRadius, float height, int slices, int stacks){
        GLUquadricImpl quadImpl=(GLUquadricImpl)quad;
        return(computeCylinder(baseRadius,topRadius,height,slices,stacks,
                quadImpl.getOrientation(),quadImpl.getDrawStyle(),
                quadImpl.getNormals()!=GLU.GLU_NONE,quadImpl.getTextureFlag()));               
    }
    
    /**
     * computes a cylinder oriented along the z axis. The base of the
     * cylinder is placed at z = 0, and the top at z=height. Like a sphere, a
     * cylinder is subdivided around the z axis into slices, and along the z axis
     * into stacks.
     *
     * Note that if topRadius is set to zero, then this routine will generate a
     * cone.
     *
     * If the orientation is set to GLU.OUTSIDE (with glu.quadricOrientation), then
     * any generated normals point away from the z axis. Otherwise, they point
     * toward the z axis.
     *
     * If texturing is turned on (with glu.quadricTexture), then texture
     * coordinates are generated so that t ranges linearly from 0.0 at z = 0 to
     * 1.0 at z = height, and s ranges from 0.0 at the +y axis, to 0.25 at the +x
     * axis, to 0.5 at the -y axis, to 0.75 at the -x axis, and back to 1.0 at the
     * +y axis.
     *
     * @param baseRadius     Specifies the radius of the cylinder at z = 0.
     * @param topRadius      Specifies the radius of the cylinder at z = height.
     * @param height         Specifies the height of the cylinder.
     * @param slices         Specifies the number of subdivisions around the z axis.
     * @param stacks         Specifies the number of subdivisions along the z axis.
     * @param orientation    GLU.OUTSIDE or GLU.INSIDE
     * @param drawStyle      GLU.GLU_POINT, GLU.GLU_LINE, GLU.GLU_SILHOUETTE or GLU.GLU_FILL
     * @param computeNormal  Indicates whether the normals are computed
     * @param computeTexture Indicates whether the texture coordinates are computed
     */
    private final static FloatBuffer computeCylinder(float baseRadius, 
            float topRadius, float height, int slices, int stacks,int orientation,
            int drawStyle,boolean computeNormal,boolean computeTexture){
        float da, r, dr, dz;
        float x, y, z, nz;
        final float nsign=(orientation == GLU.GLU_INSIDE?-1.0f:1.0f);
        int i, j;
        FloatBuffer buffer=null;     
        da = 2.0f * (float)Math.PI / slices;
        dr = (topRadius - baseRadius) / stacks;
        dz = height / stacks;
        nz = (baseRadius - topRadius) / height;
        // Z component of normal vectors
        if(drawStyle == GLU.GLU_POINT) 
            {//3 normal coordinates + 3 vertex coordinates
             //use it with GL.GL_POINTS
             buffer=Buffers.newDirectFloatBuffer(slices*(stacks+1)*(3+(computeNormal?3:0)));          
             for(i = 0; i < slices; i++) 
                 {x = (float) Math.cos((float)(i * da));
                  y = (float) Math.sin((float)(i * da));                            
                  z = 0.0f;
                  r = baseRadius;
                  for(j = 0; j <= stacks; j++)
                      {if(computeNormal)
                           {buffer.put(x*nsign);
                            buffer.put(y*nsign);
                            buffer.put(nz*nsign);                       
                           }
                       buffer.put(x*r);
                       buffer.put(y*r);
                       buffer.put(z);
                       z += dz;
                       r += dr;
                      }
                 }
             buffer.position(0);
            } 
        else 
            if(drawStyle == GLU.GLU_LINE || drawStyle == GLU.GLU_SILHOUETTE)
                {// Draw rings
                 if(drawStyle == GLU.GLU_LINE)
                     {z = 0.0f;
                      r = baseRadius;
                      for(j = 0; j <= stacks; j++) 
                          {//gl.glBegin(GL.GL_LINE_LOOP);
                           for(i = 0; i < slices; i++) 
                               {x = (float) Math.cos((float)(i * da));
                                y = (float) Math.sin((float)(i * da));
                                //normal3f(gl, x * nsign, y * nsign, nz * nsign);
                                //gl.glVertex3f((x * r), (y * r), z);
                               }
                            //gl.glEnd();
                            z += dz;
                            r += dr;
                           }
                     }
                 else
                     {// draw one ring at each end
                      if(baseRadius != 0.0) 
                          {//gl.glBegin(GL.GL_LINE_LOOP);
                           for(i = 0; i < slices; i++)
                               {x = (float) Math.cos((float)(i * da));
                                y = (float) Math.sin((float)(i * da));
                                //normal3f(gl, x * nsign, y * nsign, nz * nsign);
                                //gl.glVertex3f((x * baseRadius), (y * baseRadius), 0.0f);
                               }
                           //gl.glEnd();
                           //gl.glBegin(GL.GL_LINE_LOOP);
                           for(i = 0; i < slices; i++)
                               {x = (float) Math.cos((float)(i * da));
                                y = (float) Math.sin((float)(i * da));
                                //normal3f(gl, x * nsign, y * nsign, nz * nsign);
                                //gl.glVertex3f((x * topRadius), (y * topRadius), height);
                               }
                           //gl.glEnd();
                          }
                     }
                 // draw length lines
                 //gl.glBegin(GL.GL_LINES);
                 for(i = 0; i < slices; i++)
                     {x = (float) Math.cos((float)(i * da));
                      y = (float) Math.sin((float)(i * da));
                      //normal3f(gl, x * nsign, y * nsign, nz * nsign);
                      //gl.glVertex3f((x * baseRadius), (y * baseRadius), 0.0f);
                      //gl.glVertex3f((x * topRadius), (y * topRadius), (height));
                     }
                 //gl.glEnd();
                }
            else 
                if(drawStyle == GLU.GLU_FILL)
                    {//2 texture coordinates + 3 normal coordinates + 3 vertex coordinates
                     final int floatPerPrimitive=3+(computeTexture?2:0)+(computeNormal?3:0);
                     buffer=Buffers.newDirectFloatBuffer(stacks*slices*(4*(floatPerPrimitive)));
                     //use it with GL.GL_QUAD
                     float ds = 1.0f / slices;
                     float dt = 1.0f / stacks;
                     float t = 0.0f;
                     z = 0.0f;
                     r = baseRadius;
                     for(j = 0; j < stacks; j++)
                         {float s = 0.0f;
                          float v0x=0,v0y=0,v0z=0,v1x=0,v1y=0,v1z=0,n0x=0,n0y=0,n0z=0,t0x=0,t0y=0,t1x=0,t1y=0;
                          for(i = 0; i <= slices; i++)
                              {if(i == slices) 
                                   {x = (float) Math.sin(0.0f);
                                    y = (float) Math.cos(0.0f);
                                   } 
                               else 
                                   {x = (float) Math.sin((float)(i * da));
                                    y = (float) Math.cos((float)(i * da));
                                   }
                               if(i>0)
                                   {//use the most recently inserted values
                                    if(computeTexture)
                                        buffer.put(t0x).put(t0y);
                                    if(computeNormal)
                                        buffer.put(n0x).put(n0y).put(n0z);                                     
                                    buffer.put(v0x).put(v0y).put(v0z);
                                    if(computeTexture)
                                        buffer.put(t1x).put(t1y);                      
                                    if(computeNormal)
                                        buffer.put(n0x).put(n0y).put(n0z);                                                  
                                    buffer.put(v1x).put(v1y).put(v1z); 
                                   }
                             
                               //compute the new primitives
                               if(computeTexture)
                                   {t0x=s;
                                    t0y=t;
                                   }
                               if(computeNormal)
                                   {n0x=x*nsign;
                                    n0y=y*nsign;
                                    n0z=nz*nsign;                                      
                                   }                                 
                               v0x=x*r;
                               v0y=y*r;
                               v0z=z; 
                               if(computeTexture)
                                   {t1x=s;
                                    t1y=t + dt;                                         
                                   }                                                                  
                               v1x=x*(r + dr);
                               v1y=y*(r + dr);
                               v1z=z+dz;                              
                               
                               if(i>0)
                                   {if(computeTexture)
                                        buffer.put(t1x).put(t1y);                      
                                   if(computeNormal)
                                       buffer.put(n0x).put(n0y).put(n0z);                                                  
                                   buffer.put(v1x).put(v1y).put(v1z);
                                   if(computeTexture)
                                       buffer.put(t0x).put(t0y);
                                   if(computeNormal)
                                       buffer.put(n0x).put(n0y).put(n0z);                                     
                                   buffer.put(v0x).put(v0y).put(v0z);
                                 }                                                     
                               s += ds;
                              } // for slices                       
                          r += dr;
                          t += dt;
                          z += dz;
                         } 
                     // for stacks
                     buffer.position(0);
                    }
        return(buffer);
    }
    
    /**
     * renders a disk on the z = 0  plane.  The disk has a radius of
     * outerRadius, and contains a concentric circular hole with a radius of
     * innerRadius. If innerRadius is 0, then no hole is generated. The disk is
     * subdivided around the z axis into slices (like pizza slices), and also
     * about the z axis into rings (as specified by slices and loops,
     * respectively).
     *
     * With respect to orientation, the +z side of the disk is considered to be
     * "outside" (see glu.quadricOrientation).  This means that if the orientation
     * is set to GLU.OUTSIDE, then any normals generated point along the +z axis.
     * Otherwise, they point along the -z axis.
     *
     * If texturing is turned on (with glu.quadricTexture), texture coordinates are
     * generated linearly such that where r=outerRadius, the value at (r, 0, 0) is
     * (1, 0.5), at (0, r, 0) it is (0.5, 1), at (-r, 0, 0) it is (0, 0.5), and at
     * (0, -r, 0) it is (0.5, 0).
     */
    /*private final void computeDisk(float innerRadius, float outerRadius, int slices, 
            int loops,int orientation,int drawStyle,boolean computeNormal,
            boolean computeTexture)
    {
      float da, dr;
      if (computeNormal) {
        if (orientation == GLU.GLU_OUTSIDE) {
          //gl.glNormal3f(0.0f, 0.0f, +1.0f);
        }
        else {
          //gl.glNormal3f(0.0f, 0.0f, -1.0f);
        }
      }
      
      da = 2.0f * (float)Math.PI / slices;
      dr = (outerRadius - innerRadius) /  loops;
      
      switch (drawStyle) {
      case GLU.GLU_FILL:
        {
          // texture of a gluDisk is a cut out of the texture unit square
          // x, y in [-outerRadius, +outerRadius]; s, t in [0, 1]
          // (linear mapping)
          //
          float dtc = 2.0f * outerRadius;
          float sa, ca;
          float r1 = innerRadius;
          int l;
          for (l = 0; l < loops; l++) {
            float r2 = r1 + dr;
            if (orientation == GLU.GLU_OUTSIDE) {
              int s;
              //gl.glBegin(gl.GL_QUAD_STRIP);
              for (s = 0; s <= slices; s++) {
                float a;
                if (s == slices)
                  a = 0.0f;
                else
                  a = s * da;
                sa = (float)Math.sin((float)a);
                ca = (float)Math.cos((float)a);
                //TXTR_COORD(gl, 0.5f + sa * r2 / dtc, 0.5f + ca * r2 / dtc);
                //gl.glVertex2f(r2 * sa, r2 * ca);
                //TXTR_COORD(gl, 0.5f + sa * r1 / dtc, 0.5f + ca * r1 / dtc);
                //gl.glVertex2f(r1 * sa, r1 * ca);
              }
              //gl.glEnd();
            }
            else {
              int s;
              //gl.glBegin(GL.GL_QUAD_STRIP);
              for (s = slices; s >= 0; s--) {
                float a;
                if (s == slices)
                  a = 0.0f;
                else
                  a = s * da;
                sa = (float)Math.sin((float)a);
                ca = (float)Math.cos((float)a);
                //TXTR_COORD(gl, 0.5f - sa * r2 / dtc, 0.5f + ca * r2 / dtc);
                //gl.glVertex2f(r2 * sa, r2 * ca);
                //TXTR_COORD(gl, 0.5f - sa * r1 / dtc, 0.5f + ca * r1 / dtc);
                //gl.glVertex2f(r1 * sa, r1 * ca);
              }
              //gl.glEnd();
            }
            r1 = r2;
          }
          break;
        }
      case GLU.GLU_LINE:
        {
          int l, s;

          for (l = 0; l <= loops; l++) {
            float r = innerRadius + l * dr;
            //gl.glBegin(GL.GL_LINE_LOOP);
            for (s = 0; s < slices; s++) {
              float a = s * da;
              //gl.glVertex2f(r * sin(a), r * cos(a));
            }
            //gl.glEnd();
          }

          for (s = 0; s < slices; s++) {
            float a = s * da;
            float x = (float)Math.sin((float)a);
            float y = (float)Math.cos((float)a);
            //gl.glBegin(GL.GL_LINE_STRIP);
            for (l = 0; l <= loops; l++) {
              float r = innerRadius + l * dr;
              //gl.glVertex2f(r * x, r * y);
            }
            //gl.glEnd();
          }
          break;
        }
      case GLU.GLU_POINT:
        {
          int s;
          //gl.glBegin(GL.GL_POINTS);
          for (s = 0; s < slices; s++) {
            float a = s * da;
            float x = (float)Math.sin((float)a);
            float y = (float)Math.cos((float)a);
            int l;
            for (l = 0; l <= loops; l++) {
              float r = innerRadius * l * dr;
              //gl.glVertex2f(r * x, r * y);
            }
          }
          //gl.glEnd();
          break;
        }
      case GLU.GLU_SILHOUETTE:
        {
          if (innerRadius != 0.0) {
            float a;
            //gl.glBegin(GL.GL_LINE_LOOP);
            for (a = 0.0f; a < 2.0 * Math.PI; a += da) {
              float x = innerRadius * (float)Math.sin((float)a);
              float y = innerRadius * (float)Math.cos((float)a);
              //gl.glVertex2f(x, y);
            }
            //gl.glEnd();
          }
          {
            float a;
            //gl.glBegin(GL.GL_LINE_LOOP);
            for (a = 0; a < 2.0f * Math.PI; a += da) {
              float x = outerRadius * (float)Math.sin((float)a);
              float y = outerRadius * (float)Math.cos((float)a);
              //gl.glVertex2f(x, y);
            }
            //gl.glEnd();
          }
          break;
        }
      default:
        return;
      }
    }*/
}
