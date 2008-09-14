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

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.StringTokenizer;

/**
 * Based on Object3D.java by Jeremy Adams (elias4444) august 2005
 *
 * Read an OBJ file into ArrayLists.  Populates a "faces" Arraylist with
 * all faces in the mesh.  Also stores face groups in the "groups" Arraylist
 * (an Arraylist that contains Arraylists of faces).
 */
public final class OBJLoader {
    // These three hold the vertex, texture and normal coordinates
    // There is only one set of verts, textures, normals for entire file
    public ArrayList<float[]> vertices = new ArrayList<float[]>();  // Contains float[3] for each Vertex (XYZ)
    public ArrayList<float[]> normals = new ArrayList<float[]>();     // Contains float[3] for each normal
    public ArrayList<float[]> textureCoords = new ArrayList<float[]>();  // Contains float[3] for each texture map coord (UVW)

    // Hold all faces in the mesh
    public ArrayList<Face> faces = new ArrayList<Face>();       // Contains Face objects for entire mesh

    // Holds groups of faces
    public ArrayList<ArrayList<Face>> groups = new ArrayList<ArrayList<Face>>();

    // mesh min and max points
    public float leftpoint = 0;    // x-
    public float rightpoint = 0;   // x+
    public float bottompoint = 0;  // y-
    public float toppoint = 0;     // y+
    public float farpoint = 0;     // z-
    public float nearpoint = 0;    // z+


    public OBJLoader(String ref) {
        loadobject(ref);
    }


    public OBJLoader(InputStream in) {  // Construct from inputstream
        loadobject(in);
    }


    public void loadobject(String ref) {  // load from String filename
        if (ref != null) {
            try {
                // Use these lines if reading from a file
                FileReader fr = new FileReader(ref);
                BufferedReader br = new BufferedReader(fr);

                // Use these lines if reading from within a jar
                //InputStreamReader fr = new InputStreamReader(new BufferedInputStream(getClass().getClassLoader().getResourceAsStream(ref)));
                //BufferedReader br = new BufferedReader(fr);
                loadobject(br);
            }
            catch (IOException e) {
                System.out.println("Failed to read file: " + ref);
            }
        }
    }


    public void loadobject(InputStream in) {  // load from inputStream
        if (in != null) {
            BufferedReader br = new BufferedReader(new InputStreamReader(in));
            loadobject(br);
        }
    }

    /**
     * OBJ file format in a nutshell:
     * First part of file lists vertex data: vert coords, texture coords and normals.
     * These lines start with v, vt and vn respectively.
     * Second part of file lists faces.  These lines start with f.
     * Each face is defined as a set of verts, usually three, but may be more.
     * The face definition line contains triplets: three numbers separated by
     * slashes.  Each number is an index into the vert, texture coord, or normal list.
     * NOTE: these lists are indexed starting with 1, not 0.
     * @param ref
     */
    public void loadobject(BufferedReader br) {
        ArrayList<Face> groupFaces = null;
        String line = "";
        try {
            while ((line = br.readLine()) != null) {
                // remove extra whitespace
                line = line.trim();
                line = line.replaceAll("  ", " ");
                if (line.length() > 0) {
                    if (line.startsWith("v ")) {
                        // vertex coord line looks like: v 2.628657 -5.257312 8.090169 [optional W value]
                        vertices.add(read3Floats(line));
                    }
                    if (line.startsWith("vt")) {
                        // texture coord line looks like: vt 0.187254 0.276553 0.000000
                        textureCoords.add(read3Floats(line));
                    }
                    if (line.startsWith("vn")) {
                        // normal line looks like: vn 0.083837 0.962494 -0.258024
                        normals.add(read3Floats(line));
                    }
                    if (line.startsWith("f ")) {
                        // Face line looks like: f 1/3/1 13/20/13 16/29/16
                        Face f = readFace(line);
                        faces.add(f);           // add to complete face list
                        if (groupFaces != null) {  // add to current group
                            groupFaces.add(f);
                        }
                    }
                    if (line.startsWith("g ")) {
                        // Group line looks like: g someGroupName
                        String groupname = line.substring(2).trim();
                        groupFaces = new ArrayList<Face>(); // start a new group of faces
                        groups.add(groupFaces);
                    }
                }
            }
            // if we never found a group identifier, use the entire face list as the only group
            if (groupFaces == null) {
                groups.add(faces);
            }
        } catch (Exception e) {
            System.out.println("GL_OBJ_Reader.loadObject() failed at line: " + line);
        }
        // find min/max points of mesh
        calcDimensions();
        // debug:
        System.out.println("OBJLoader: read " + numpolygons()
                        + " faces in " + groups.size() + " groups");
    }

    /**
     * Parse three floats from the given input String.  Ignore the
     * first token (the line type identifier, ie. "v", "vn", "vt").
     * Return array: float[3].
     * @param line  contains line from OBJ file
     * @return array of 3 float values
     */
    private float[] read3Floats(String line)
    {
        try
        {
            StringTokenizer st = new StringTokenizer(line, " ");
            st.nextToken();   // throw out line marker (vn, vt, etc.)
            if (st.countTokens() == 2) { // texture uv may have only 2 values
                return new float[] {Float.parseFloat(st.nextToken()),
                                    Float.parseFloat(st.nextToken()),
                                    0};
            }
            else {
                return new float[] {Float.parseFloat(st.nextToken()),
                                    Float.parseFloat(st.nextToken()),
                                    Float.parseFloat(st.nextToken())};
            }
        }
        catch (Exception e)
        {
            System.out.println("OBJLoader.read3Floats(): error on line '" + line + "', " + e);
            return null;
        }
    }

    /**
     * Read a face definition from line and return a Face object.
     * Face line looks like: f 1/3/1 13/20/13 16/29/16
     * Three or more sets of numbers, each set contains vert/txtr/norm
     * references.  A reference is an index into the vert or txtr
     * or normal list.
     * @param line   string from OBJ file with face definition
     * @return       Face object
     */
    private Face readFace(String line) {
        // throw out "f " at start of line, then split
        String[] triplets = line.substring(2).split(" ");
        int[] v = new int[triplets.length];
        int[] vt = new int[triplets.length];
        int[] vn = new int[triplets.length];
        for (int i = 0; i < triplets.length; i++) {
            // triplets look like  13/20/13  and hold
            // vert/txtr/norm indices.  If no texture coord has been
            // assigned, may be 13//13.  Substitute 0 so split works.
            String[] vertTxtrNorm = triplets[i].replaceAll("//", "/0/").split("/");
            if (vertTxtrNorm.length > 0) {
                v[i] = convertIndex(vertTxtrNorm[0],vertices.size());
            }
            if (vertTxtrNorm.length > 1) {
                vt[i] = convertIndex(vertTxtrNorm[1],textureCoords.size());
            }
            if (vertTxtrNorm.length > 2) {
                vn[i] = convertIndex(vertTxtrNorm[2],normals.size());
            }
        }
        return  new Face(v,vt,vn);
    }

    /**
     * Convert a vertex reference number into the correct vertex array index.
     * <BR>
     * Face definitions in the OBJ file refer to verts, texture coords and
     * normals using a reference number. The reference numbers is the position
     * of the vert in the vertex list, in the order read from the OBJ file.
     * Reference numbers start at 1, and can be negative (to refer back into
     * the vert list starting at the bottom, though this seems to be rare). The
     * same approach applies to texture coords and normals.
     * <BR>
     * This function converts reference numbers to an array index starting at 0,
     * and converts negative reference numbers to 0-N array indexes.  It returns
     * -1 if the token is blank, meaning there was no data given (ie. there
     * is no texture coord or normal available).
     * <BR>
     * @param token   a token from the OBJ file containing a numeric value or blank
     * @return idx    will be 0 - N index into vert array, or -1 if token is blank
     */
    public int convertIndex(String token, int numVerts) {
        int idx = Integer.valueOf(token).intValue(); // OBJ file index starts at 1
        idx = (idx < 0) ? (numVerts + idx) : idx-1;  // convert index to start at 0
        return idx;
    }

    /**
     *  Find min/max points of mesh.
     */
    public void calcDimensions() {
        float[] vertex;
        // reset min/max points
        leftpoint = rightpoint = 0;
        bottompoint = toppoint = 0;
        farpoint = nearpoint = 0;
        // loop through all groups of faces (currently only 1)
        for (int g = 0; g < groups.size(); g++) {
            ArrayList<Face> faces = groups.get(g);
            // loop through all faces (ie. triangles or quads)
            for (int f = 0; f < faces.size(); f++) {
                Face face = faces.get(f);
                int[] vertIDs = face.vertexIDs;
                // loop through all vertices in face
                for (int v = 0; v < vertIDs.length; v++) {
                    vertex = vertices.get(vertIDs[v]);
                    if (vertex[0] > rightpoint)  rightpoint = vertex[0];
                    if (vertex[0] < leftpoint)   leftpoint = vertex[0];
                    if (vertex[1] > toppoint)    toppoint = vertex[1];
                    if (vertex[1] < bottompoint) bottompoint = vertex[1];
                    if (vertex[2] > nearpoint)   nearpoint = vertex[2];
                    if (vertex[2] < farpoint)    farpoint = vertex[2];
                }
            }
        }
    }

    public float getXWidth() {
        return rightpoint - leftpoint;
    }

    public float getYHeight() {
        return toppoint - bottompoint;
    }

    public float getZDepth() {
        return nearpoint - farpoint;
    }

    public int numpolygons() {
        int number = 0;
        for (int i = 0; i < groups.size(); i++) {
            number += (groups.get(i)).size();
        }
        return number;
    }

    /**
     * Render the mesh.
     */
    /*public void opengldraw() {
        float[] vertex;
        float[] textureCoord;
        float[] normal;
        // loop through all groups of faces (currently only 1)
        for (int g = 0; g < groups.size(); g++) {
            ArrayList faces = (ArrayList) groups.get(g);
            // loop through all faces (ie. triangles or quads)
            for (int f = 0; f < faces.size(); f++) {
                Face face = (Face) faces.get(f);
                int[] vertIDs = face.vertexIDs;
                int[] txtrIDs = face.textureIDs;
                int[] normIds = face.normalIDs;
                int polytype;
                // is this a triangle, quad or polygon
                if (vertIDs.length == 3) {
                    polytype = GL11.GL_TRIANGLES;
                }
                else if (vertIDs.length == 4) {
                    polytype = GL11.GL_QUADS;
                }
                else {
                    polytype = GL11.GL_POLYGON;
                }
                // Draw the face
                GL11.glBegin(polytype);
                {
                    for (int v = 0; v < vertIDs.length; v++) {
                        if (normIds[v] >= 0) {  // -1 means no normal exists
                            normal = (float[]) normals.get(normIds[v]);
                            GL11.glNormal3f(normal[0], normal[1], normal[2]);
                        }
                        if (txtrIDs[v] >= 0) {    // -1 means no texture coord exists
                            textureCoord = (float[]) textureCoords.get(txtrIDs[v]);
                            GL11.glTexCoord3f(textureCoord[0], textureCoord[1], textureCoord[2]);
                        }
                        vertex = (float[]) vertices.get(vertIDs[v]);
                        GL11.glVertex3f(vertex[0], vertex[1], vertex[2]);
                    }
                }
                GL11.glEnd();
            }
        }
    }*/
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