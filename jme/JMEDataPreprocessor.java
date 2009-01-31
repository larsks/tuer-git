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
import java.io.FilenameFilter;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;

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
    
    private static final ObjToJme converter=new ObjToJme();
    
    public static final void main(String[] args){
        DisplaySystem.getDisplaySystem(DummySystemProvider.DUMMY_SYSTEM_IDENTIFIER);        
        //first step: parse the arguments to get the file names
        List<String[]> conversionGroupsFilenamesList=new ArrayList<String[]>();
        boolean isFilePatternUsed,isJBINFilePatternUsed;
        String jbinPath;
        for(int i=0;i<args.length;i+=4)
            if(i+3<args.length)
                {isFilePatternUsed=!args[i].endsWith(".obj")&&!args[i].endsWith(".OBJ");
                 if(isFilePatternUsed)
                     {System.out.println("[INFO] "+args[i]+" is not a WaveFront OBJ file.");
                      System.out.println("[INFO] "+args[i]+" is going to be treated as a pattern (directory+file prefix).");                      
                     }
                 else
                     System.out.println("[INFO] "+args[i]+" is a WaveFront OBJ file.");
                 if(!args[i+1].endsWith(".mtl")&&!args[i+1].endsWith(".MTL"))
                     System.out.println("[WARNING] "+args[i+1]+" is not a MTL file!");
                 isJBINFilePatternUsed=!args[i+3].endsWith(".jbin")&&!args[i+3].endsWith(".JBIN");
                 if(isFilePatternUsed)
                     {if(isJBINFilePatternUsed)
                          {System.out.println("[INFO] "+args[i+3]+" is not a JME binary file.");
                           jbinPath=args[i+3];
                           System.out.println("[INFO] "+jbinPath+" is going to be treated as a pattern (directory+file prefix).");     
                          }
                      else
                          {System.out.println("[INFO] "+args[i+3]+" is a JME binary file.");
                           jbinPath=new File(args[i+3]).getParent();
                           System.out.println("[INFO] "+jbinPath+" is going to be used as a directory.");
                          }
                     }
                 else
                     {if(isJBINFilePatternUsed)
                          {System.out.println("[INFO]: "+args[i+3]+" is not a JME binary file.");
                           jbinPath=args[i+3]+".jbin";
                           System.out.println("[INFO]: "+jbinPath+" is going to be used instead.");
                          }
                      else
                          {jbinPath=args[i+3];
                           System.out.println("[INFO]: "+jbinPath+" is a JME binary file.");
                          }
                     }
                 if(isFilePatternUsed)
                     {File patternFile=new File(args[i]);
                      if(!jbinPath.substring(jbinPath.length()-1).equals("/"))
                          jbinPath+="/";
                      File[] OBJfiles=patternFile.getParentFile().listFiles(new OBJFilenameFilter(patternFile.getName()));
                      String OBJFilePath,JBINFilePath;
                      for(File OBJfile:OBJfiles)
                          {OBJFilePath=OBJfile.getPath();
                           JBINFilePath=jbinPath+OBJfile.getName().substring(0,Math.max(OBJfile.getName().lastIndexOf(".obj"),OBJfile.getName().lastIndexOf(".OBJ")))+".jbin";
                           conversionGroupsFilenamesList.add(new String[]{OBJFilePath,args[i+1],args[i+2],JBINFilePath});
                          }
                     }
                 else
                     conversionGroupsFilenamesList.add(new String[]{args[i],args[i+1],args[i+2],jbinPath});
                }
            else
                {for(int j=i;j<args.length;j++)
                     System.out.println("file "+args[j]+" ignored");
                 System.out.println("usage: file_1.obj file_1.mtl file_1.png file_1.jbin ... file_n.obj file_n.mtl file_n.png file_n.jbin");
                }
        //second step: perform the conversion
        for(String[] conversionGroupsFilenames:conversionGroupsFilenamesList)
            convert(conversionGroupsFilenames[0],conversionGroupsFilenames[1],
                    conversionGroupsFilenames[2],conversionGroupsFilenames[3]);
    }
    
    private static final void convert(String OBJFilename,String MTLlibFilename,String textureFilename,String JBINFilename){
        try{converter.setProperty("mtllib",new File(MTLlibFilename).toURI().toURL());
            converter.setProperty("texdir",new File(textureFilename).toURI().toURL());
           } 
        catch(MalformedURLException murle)
        {murle.printStackTrace();}                
        converter.attemptFileConvert(new String[]{OBJFilename,JBINFilename});
        //the converter nulls all after a conversion
    }

    private static final class OBJFilenameFilter implements FilenameFilter{

        
        private String pattern;
        
        private String[] excludedNames;
        
        
        private OBJFilenameFilter(String pattern){
            this.pattern=pattern;
            //remove this file because it only contains "call" primitives
            //that are useless for JME 2.0
            this.excludedNames=new String[]{pattern+".obj",pattern+".OBJ"};
        }
        
        @Override
        public boolean accept(File dir, String name){
            return(name.startsWith(pattern)&&(name.endsWith(".obj")||name.endsWith(".OBJ"))&&!name.equals(excludedNames[0])&&!name.equals(excludedNames[1]));
        }
        
    }
}
