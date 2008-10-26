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

import java.util.ArrayList;

import javax.media.opengl.GL;
import javax.media.opengl.TraceGL;

public class GeometryGL extends TraceGL
{
    /*protected ArrayList<float[]> vertices;
    protected ArrayList<float[]> normals;
    protected IntList vertexIndices;
    // the current normal value.
    protected float[] normal;
    protected boolean lines;*/
    //protected int mode=-1;
    /*// the index of the first vertex since the last "begin"
    protected int vFirstIndex;
    // the number of vertices after the last "begin"
    protected int vCount;*/
    private ArrayList<float[]> vertices;

    public GeometryGL(GL downstreamGL)
    {
        // note: we set the stream to null so we know when a method is used which is not
        // overwritten yet; we will get a NullPointerException in this case
        super(downstreamGL, System.out);
        //reset();
        vertices=new ArrayList<float[]>();
    }

    /*public void reset()
    {
        /*vertices=new ArrayList<float[]>();
        normals=new ArrayList<float[]>();

        vertexIndices=new IntList(10);
        //normalIndices=new IntList(10);
        normal=new float[3];

        lines=false;
    }*/
    public ArrayList<float[]> getVertices(){
        return(vertices);
    }
    /*public float[][] getVertices()
    {
        return vertices.toArray(new float[3][vertices.size()]);
    }

    public float[][] getNormals()
    {
        return normals.toArray(new float[3][normals.size()]);
    }

    public int[] getVertexIndices()
    {
        return vertexIndices.trim();
    }

    public int[] getNormalIndices()
    {
        return vertexIndices.trim(); // the lists are identical
    }

    /**
     * Returns true in case there is at least one line geometry.
     */
    /*public boolean isLines()
    {
        return lines;
    }*/

    public void glBegin(int arg0)
    {
        // see http://wiki.delphigl.com/index.php/GlBegin for a good description of modes
        /*mode=arg0;

        vFirstIndex=vertices.size();
        vCount=0;*/
        /*switch(arg0)
        {
            case GL.GL_POINTS:
                throw new IllegalArgumentException("GL_POINTS not supported");
            case GL.GL_LINES: // fall through
            case GL.GL_LINE_STRIP: // fall through
            case GL.GL_LINE_LOOP: 
                //lines=true;
                break;
            case GL.GL_TRIANGLES:
                break;
            case GL.GL_TRIANGLE_STRIP:
                break;
            case GL.GL_TRIANGLE_FAN:
                break;
            case GL.GL_QUADS:
                break;
            case GL.GL_QUAD_STRIP:
                break;
            case GL.GL_POLYGON:
                throw new IllegalArgumentException("GL_POLYGON not supported");
            default: throw new IllegalArgumentException("mode not supported");
        }*/
    }

    public void glEnd()
    {
        /*if(mode==GL.GL_LINE_LOOP)
        {
            // close last line
            if(vCount>1)
            {
                vertexIndices.add(vertices.size()-1);
                vertexIndices.add(vFirstIndex);
            }
        }
        mode=-1;*/
    }
 
    public void glVertex3d(double arg0, double arg1, double arg2)
    {
        glVertex3f((float)arg0, (float)arg1, (float)arg2);
    }

    public void glVertex3dv(double[] arg0, int arg1)
    {
        glVertex3f((float)arg0[arg1], (float)arg0[arg1+1], (float)arg0[arg1+2]);
    }

    public void glVertex3fv(float[] arg0, int arg1)
    {
        glVertex3f(arg0[arg1], arg0[arg1+1], arg0[arg1+2]);
    }

    public void glVertex3f(float arg0, float arg1, float arg2)
    {
        vertices.add(new float[]{arg0, arg1, arg2});/*
        vCount++;

        // apply the normal value as well
        normals.add(normal.clone());*/

        /*switch(mode)
        {
            case GL.GL_POINTS:
                break;
            case GL.GL_LINES:
                vertexIndices.add(vertices.size()-1); // only one vertex added
                break;
            case GL.GL_LINE_STRIP: // fall through
            case GL.GL_LINE_LOOP:
                if(vCount>1)
                {
                    vertexIndices.add(vertices.size()-2);
                    vertexIndices.add(vertices.size()-1);
                }
                break;
            case GL.GL_TRIANGLES:
                vertexIndices.add(vertices.size()-1); // only one vertex added
                break;
            case GL.GL_TRIANGLE_STRIP:
                if(vCount>2)
                {
                    if(vCount%2==0) // even
                    {
                        // one triangle (three vertices) added
                        vertexIndices.add(vertices.size()-3);
                        vertexIndices.add(vertices.size()-2);
                    }
                    else // odd
                    {
                        // one triangle (three vertices) added
                        vertexIndices.add(vertices.size()-2);
                        vertexIndices.add(vertices.size()-3);
                    }
                    vertexIndices.add(vertices.size()-1);
                }
                break;
            case GL.GL_TRIANGLE_FAN:
                if(vCount>2)
                {
                    // one triangle (three vertices) added
                    vertexIndices.add(vFirstIndex);
                    vertexIndices.add(vertices.size()-2);
                    vertexIndices.add(vertices.size()-1);
                }
                break;
            case GL.GL_QUADS:
                if(vCount%4==0) // do it every 4
                {
                    // two triangles (4 vertices) added
                    vertexIndices.add(vertices.size()-4);
                    vertexIndices.add(vertices.size()-3);
                    vertexIndices.add(vertices.size()-1);
                    
                    vertexIndices.add(vertices.size()-1);
                    vertexIndices.add(vertices.size()-3);
                    vertexIndices.add(vertices.size()-2);
                }
                break;
            case GL.GL_QUAD_STRIP:
                if(vCount>2 && vCount%2==0) // do it after the first two vertices and then every two
                {
                    // two triangles (4 vertices) added
                    vertexIndices.add(vertices.size()-3);
                    vertexIndices.add(vertices.size()-4);
                    vertexIndices.add(vertices.size()-2);
                    
                    vertexIndices.add(vertices.size()-3);
                    vertexIndices.add(vertices.size()-2);
                    vertexIndices.add(vertices.size()-1);
                }
                break;
            case GL.GL_POLYGON:
                break;
        }*/
    }

    public void glNormal3d(double arg0, double arg1, double arg2)
    {
        glNormal3f((float)arg0, (float)arg1, (float)arg2);
    }
 
    public void glNormal3dv(double[] arg0, int arg1)
    {
        glNormal3f((float)arg0[arg1], (float)arg0[arg1+1], (float)arg0[arg1+2]);
    }

    public void glNormal3fv(float[] arg0, int arg1)
    {
        glNormal3f(arg0[arg1], arg0[arg1+1], arg0[arg1+2]);
    }

    public void glNormal3f(float arg0, float arg1, float arg2)
    {
        // the normal value will be applied once a new vertex is created
        //normal=new float[]{arg0, arg1, arg2};
    }
}