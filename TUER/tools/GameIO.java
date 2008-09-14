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

/**
 * This class is the facade of the component called "tools". 
 * It provides some static methods to manipulate textures.
 *@author Julien Gouesse
 */

package tools;

import com.sun.opengl.util.BufferUtil;
import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;

public class GameIO{

    private static final int HEADER_SIZE = 2;
    
    private static final int PRIMITIVE_COUNT_HEADER_INDEX = 0;
    
    private static final int VALUE_COUNT_PER_PRIMITIVE_HEADER_INDEX = 1;
    
    
    public static FloatBuffer readGameFloatDataFile(String path) throws IOException{
        DataInputStream in;
        FloatBuffer coordinatesBuffer;
        in=new DataInputStream(new BufferedInputStream(GameIO.class.getResourceAsStream(path)));
        int[] headerData=readGameFloatDataFileHeader(in);
        //? bounds * ? animations * ? frames * ? elements in a primitive
        coordinatesBuffer=BufferUtil.newFloatBuffer(headerData[PRIMITIVE_COUNT_HEADER_INDEX]*headerData[VALUE_COUNT_PER_PRIMITIVE_HEADER_INDEX]);
        for(int i=0;i<coordinatesBuffer.capacity();i++)
            coordinatesBuffer.put(in.readFloat());
        coordinatesBuffer.position(0);
        in.close();
        return(coordinatesBuffer);
    }
    
    public static final List<FloatBuffer> readGameMultiBufferFloatDataFile(String path) throws IOException{
        List<FloatBuffer> coordinatesBufferList=new ArrayList<FloatBuffer>();
        //TODO: read several buffers from a single file
        DataInputStream in=new DataInputStream(new BufferedInputStream(GameIO.class.getResourceAsStream(path)));
        int[] headerData;
        FloatBuffer coordinatesBuffer;
        while(in.available()>0)
            {//read the header to know the amount of data to read
             headerData=readGameFloatDataFileHeader(in);
             //create a buffer
             coordinatesBuffer=BufferUtil.newFloatBuffer(headerData[PRIMITIVE_COUNT_HEADER_INDEX]*headerData[VALUE_COUNT_PER_PRIMITIVE_HEADER_INDEX]);
             //fill the buffer
             for(int i=0;i<coordinatesBuffer.capacity();i++)
                 coordinatesBuffer.put(in.readFloat());
             //rewind it to avoid problems later
             coordinatesBuffer.rewind();
             //add it to the list
             coordinatesBufferList.add(coordinatesBuffer);
            }
        in.close();
        return(coordinatesBufferList);
    }
    
    private static final int[] readGameFloatDataFileHeader(DataInputStream in) throws IOException{
        int[] result=new int[HEADER_SIZE];
        result[PRIMITIVE_COUNT_HEADER_INDEX]=in.readInt();
        //read the count of values per primitive: 
        //3 for the vertices, 2 for the texture coordinates for example
        result[VALUE_COUNT_PER_PRIMITIVE_HEADER_INDEX]=in.readInt();
        return(result);
    }
}
