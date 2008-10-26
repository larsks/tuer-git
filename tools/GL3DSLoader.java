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

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

public final class GL3DSLoader {
 // These three hold the vertex, texture and normal coordinates
    // There is only one set of verts, textures, normals for entire file
    public ArrayList<float[]> vertices = new ArrayList<float[]>();  // Contains float[3] for each Vertex (XYZ)
    public ArrayList<float[]> normals = new ArrayList<float[]>();     // Contains float[3] for each normal
    public ArrayList<float[]> textureCoords = new ArrayList<float[]>();  // Contains float[3] for each texture map coord (UVW)
    public ArrayList<Face> faces = new ArrayList<Face>();       // Contains Face objects

    private int currentId;
    private int nextOffset;

    private String currentObjectName = null;
    private boolean endOfStream = false;


    public GL3DSLoader() {
    }

    // load an object
    // ASSUMES ONLY ONE object in 3ds file
    public boolean load3DSFromStream(InputStream inStream) {
        System.out.println(">> Importing scene from 3ds stream ...");
        BufferedInputStream in = new BufferedInputStream(inStream);
        try {
            readHeader(in);
            if (currentId != 0x4D4D) {  // 3DS file identifier
                System.out.println("Error: This is not a valid 3ds file.");
                return false;
            }
            while (!endOfStream) {
                readNext(in); // will load into currentObject
            }
        }
        catch (Throwable ignored) {}
        return true;
    }


    private String readString(InputStream in) throws IOException {
        String result = new String();
        byte inByte;
        while ( (inByte = (byte) in.read()) != 0)
            result += (char) inByte;
        return result;
    }

    private int readInt(InputStream in) throws IOException {
        return in.read() | (in.read() << 8) | (in.read() << 16) | (in.read() << 24);
    }

    private int readShort(InputStream in) throws IOException {
        return (in.read() | (in.read() << 8));
    }

    private float readFloat(InputStream in) throws IOException {
        return Float.intBitsToFloat(readInt(in));
    }

    private void readHeader(InputStream in) throws IOException {
        currentId = readShort(in);
        nextOffset = readInt(in);
        endOfStream = currentId < 0;
    }

    private void readNext(InputStream in) throws IOException {
        readHeader(in);
       if (currentId == 0x3D3D) {  // Mesh block
            return;
        }
        if (currentId == 0x4000) {   // Object block
            currentObjectName = readString(in);
            System.out.println("GL_3DS_Reader: " + currentObjectName);
            return;
        }
        if (currentId == 0x4100) { // Triangular polygon object
            System.out.println("GL_3DS_Reader: start mesh object");
            //mesh = new GL_Object();
            return;
        }
        if (currentId == 0x4110) { // Vertex list
            System.out.println("GL_3DS_Reader: read vertex list");
            readVertexList(in);
            return;
        }
        if (currentId == 0x4120) { // Triangle Point list
            System.out.println("GL_3DS_Reader: read triangle list");
            readPointList(in);
            return;
        }
        if (currentId == 0x4140) { // Mapping coordinates
            System.out.println("GL_3DS_Reader: read mapping coords");
            readMappingCoordinates(in);
            return;
        }
        skip(in);
    }

    private void skip(InputStream in) throws IOException, OutOfMemoryError {
        for (int i = 0; (i < nextOffset - 6) && (!endOfStream); i++) {
            endOfStream = in.read() < 0;
        }
    }

    /**
     *  3D Studio Max Models have the Z-Axis pointing up.  For compatibility
     *  with OpenGL we need to flip the y values with the z values, and
     *  negate z.  This gives us z coming toward the viewer, x is horizontal
     *  and y is vertical.
     */
    private void readVertexList(InputStream in) throws IOException {
        float x, y, z, tmpy;
        int numVertices = readShort(in);
        for (int i = 0; i < numVertices; i++) {
            x = readFloat(in);
            y = readFloat(in);
            z = readFloat(in);
            //swap the Y and Z values
            tmpy = y;
            y = z;
            z = -tmpy;
            // add vertex to the list
            vertices.add( new float[] {x, y, z} );
        }
    }

    /**
     * read triangles from file.  A triangle is defined as three indices
     * into the vertex list.  For consistency, I use the same face class
     * that the OBJ file uses, which also stores normal and texture coordinates
     * for each point of the face.  In 3DS format, there is one texture
     * coordinate for each vertex, so the indices into the texture coordinate
     * list will be same as indices into the vertex list.  We're not
     * loading normals so null that out.
     */
    private void readPointList(InputStream in) throws IOException {
        int triangles = readShort(in);
        for (int i = 0; i < triangles; i++) {
            int[] vertexIDs = new int[3];
            vertexIDs[0] = readShort(in);
            vertexIDs[1] = readShort(in);
            vertexIDs[2] = readShort(in);
            readShort(in);
            faces.add( new Face(vertexIDs,vertexIDs,null) );
        }
    }

    private void readMappingCoordinates(InputStream in) throws IOException {
        int numVertices = readShort(in);
        for (int i = 0; i < numVertices; i++) {
            float[] uvw = new float[3];
            uvw[0] = readFloat(in);
            uvw[1] = readFloat(in);
            uvw[2] = 0f;
            textureCoords.add( uvw );
        }
    }
    
    private static final class Face {
        int[] vertexIDs;
        int[] textureIDs;
        int[] normalIDs;

        Face(int[] vertIDs, int[] txtrIDs, int[] normIDs) {
            vertexIDs = new int[vertIDs.length];
            textureIDs = new int[vertIDs.length];
            normalIDs = new int[vertIDs.length];
            if (vertIDs != null)
                System.arraycopy(vertIDs, 0, vertexIDs, 0, vertIDs.length);
            if (txtrIDs != null)
                System.arraycopy(txtrIDs, 0, textureIDs, 0, txtrIDs.length);
            if (normIDs != null)
                System.arraycopy(normIDs, 0, normalIDs, 0, normIDs.length);
        }
    }
}
