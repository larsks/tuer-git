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
package jme;

import java.io.File;
import java.net.MalformedURLException;
import com.jme.system.DisplaySystem;
import com.jme.system.dummy.DummySystemProvider;
import com.jmex.model.converters.ObjToJme;

/**
 * It allows to convert the data from the format(s) used in order
 * to display the models inside a modeler like Blender into a format
 * easily readable for JMonkeyEngine 2.0, the JME binary format in this 
 * case.
 * 
 * @author Julien Gouesse
 *
 */
class JMEDataPreprocessor{
    
    public static final void main(String[] args){
        DisplaySystem.getDisplaySystem(DummySystemProvider.DUMMY_SYSTEM_IDENTIFIER);
        ObjToJme converter=new ObjToJme();
        for(int i=0;i<args.length;i+=4)
            if(i+3<args.length)
                {if(!args[i].endsWith(".obj")&&!args[i].endsWith(".OBJ"))
                    System.out.println("WARNING: "+args[i]+" is not a WaveFront OBJ file!");
                 if(!args[i+1].endsWith(".mtl")&&!args[i+1].endsWith(".MTL"))
                     System.out.println("WARNING: "+args[i+1]+" is not a MTL file!");
                 if(!args[i+3].endsWith(".jbin")&&!args[i+3].endsWith(".JBIN"))
                     System.out.println("WARNING: "+args[i+3]+" is not a JME binary file!");
                 try{converter.setProperty("mtllib",new File(args[i+1]).toURI().toURL());
                     converter.setProperty("texdir",new File(args[i+2]).toURI().toURL());
                    } 
                 catch(MalformedURLException murle)
                 {murle.printStackTrace();}                
                 converter.attemptFileConvert(new String[]{args[i+0],args[i+3]});
                 //the converter nulls all after a conversion
                }
            else
                {for(int j=i;j<args.length;j++)
                     System.out.println("file "+args[j]+" ignored");
                 System.out.println("usage: file_1.obj file_1.mtl file_1.png file_1.jbin ... file_n.obj file_n.mtl file_n.png file_n.jbin");
                }
    }

}
