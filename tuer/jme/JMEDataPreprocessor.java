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

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import bean.NodeIdentifier;
import com.jme.math.FastMath;
import com.jme.math.Quaternion;
import com.jme.math.Vector3f;
import com.jme.system.DisplaySystem;
import com.jme.system.dummy.DummySystemProvider;
import com.jmex.model.converters.MaxToJme;
import com.jmex.model.converters.Md2ToJme;
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
    
    
    private enum Format{ASE,MAX,MD2,MD3,MILK,OBJ,X3D};
    
    private static final ObjToJme objConverter=new ObjToJme();
    
    private static final MaxToJme maxConverter=new MaxToJme();
    
    private static final Md2ToJme md2Converter=new Md2ToJme();
    
    private static final int firstConvertibleObjectIndex=3;
    
    public static final void main(String[] args)throws IOException{
        if(args.length<firstConvertibleObjectIndex)
            {System.out.println();
             return;
            }
        DisplaySystem.getDisplaySystem(DummySystemProvider.DUMMY_SYSTEM_IDENTIFIER);        
        //first step: parse the arguments to get the file names
        List<String[]> conversionGroupsFilenamesList=new ArrayList<String[]>();
        List<Format> formatList=new ArrayList<Format>();
        boolean isFilePatternUsed,isJBINFilePatternUsed,needMtlCheck;
        String jbinPath;
        String levelName=null;
        boolean isSingleObjFile,isSingle3dsFile,isSingleMd2File;
        String tilesFilename=args[0];
        String worldFilename=args[1];
        String levelFilename=args[2];
        List<String> identifiedNodeNameList=new ArrayList<String>();
        for(int i=firstConvertibleObjectIndex;i<args.length;i+=4)
            if(i+2<args.length)
                {isSingleObjFile=args[i].endsWith(".obj")||args[i].endsWith(".OBJ");
                 isSingle3dsFile=args[i].endsWith(".3ds")||args[i].endsWith(".3DS");
                 isSingleMd2File=args[i].endsWith(".md2")||args[i].endsWith(".MD2");
                 isFilePatternUsed=!isSingleObjFile&&!isSingle3dsFile&&!isSingleMd2File;
                 needMtlCheck=isSingleObjFile||isFilePatternUsed;                 
                 if(needMtlCheck&&i+3<args.length)
                     {if(isFilePatternUsed)
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
                             {System.out.println("[INFO] "+args[i+3]+" is not a JME binary file.");
                              jbinPath=args[i+3]+".jbin";
                              System.out.println("[INFO] "+jbinPath+" is going to be used instead.");
                             }
                         else
                             {jbinPath=args[i+3];
                              System.out.println("[INFO] "+jbinPath+" is a JME binary file.");
                             }
                         }
                     if(isFilePatternUsed)
                         {File patternFile=new File(args[i]);
                          if(!jbinPath.substring(jbinPath.length()-1).equals("/"))
                              jbinPath+="/";
                          int slashIndex=args[i].lastIndexOf("/");
                          if(slashIndex!=-1&&args[i].length()>slashIndex+1)
                              levelName=args[i].substring(slashIndex+1);
                          File[] OBJfiles=patternFile.getParentFile().listFiles(new OBJFilenameFilter(patternFile.getName()));
                          String OBJFilePath,JBINFilePath,levelElementName;
                          for(File OBJfile:OBJfiles)
                             {OBJFilePath=OBJfile.getPath();
                              levelElementName=OBJfile.getName().substring(0,Math.max(OBJfile.getName().lastIndexOf(".obj"),OBJfile.getName().lastIndexOf(".OBJ")));
                              identifiedNodeNameList.add(levelElementName);
                              //System.out.println("level element name: "+levelElementName);
                              JBINFilePath=jbinPath+levelElementName+".jbin";
                              formatList.add(Format.OBJ);
                              conversionGroupsFilenamesList.add(new String[]{OBJFilePath,args[i+1],args[i+2],JBINFilePath});
                             }
                         }
                     else
                         {formatList.add(Format.OBJ);
                          conversionGroupsFilenamesList.add(new String[]{args[i],args[i+1],args[i+2],jbinPath});
                         }
                     }
                 else
                     if(!needMtlCheck)
                         {if(isSingle3dsFile)
                              {System.out.println("[INFO] "+args[i]+" is a 3D Studio Max file.");
                               isJBINFilePatternUsed=!args[i+2].endsWith(".jbin")&&!args[i+2].endsWith(".JBIN");
                               if(isJBINFilePatternUsed)
                                   {System.out.println("[INFO] "+args[i+2]+" is not a JME binary file.");
                                    jbinPath=args[i+2]+".jbin";
                                    System.out.println("[INFO] "+jbinPath+" is going to be used instead.");
                                   }
                               else
                                   {jbinPath=args[i+2];
                                    System.out.println("[INFO] "+jbinPath+" is a JME binary file.");
                                   }
                               formatList.add(Format.MAX);
                               conversionGroupsFilenamesList.add(new String[]{args[i],null,args[i+1],jbinPath});                    
                              }
                          else
                              if(isSingleMd2File)
                                  {System.out.println("[INFO] "+args[i]+" is a MD2 file.");
                                   isJBINFilePatternUsed=!args[i+2].endsWith(".jbin")&&!args[i+2].endsWith(".JBIN");
                                   if(isJBINFilePatternUsed)
                                       {System.out.println("[INFO] "+args[i+2]+" is not a JME binary file.");
                                        jbinPath=args[i+2]+".jbin";
                                        System.out.println("[INFO] "+jbinPath+" is going to be used instead.");
                                       }
                                   else
                                       {jbinPath=args[i+2];
                                        System.out.println("[INFO] "+jbinPath+" is a JME binary file.");
                                       }
                                   formatList.add(Format.MD2);
                                   conversionGroupsFilenamesList.add(new String[]{args[i],null,args[i+1],jbinPath});                                   
                                  }
                              else
                                  System.out.println("file "+args[i]+" ignored: unknown file format");
                         }
                     else
                         {for(int j=i;j<args.length;j++)
                              System.out.println("file "+args[j]+" ignored");
                          System.out.println("usage: file_1.obj|file_1.3ds [file_1.mtl] file_1.png file_1.jbin ... file_n.obj file_n.mtl file_n.png file_n.jbin");
                         }
                }
            else
                {for(int j=i;j<args.length;j++)
                     System.out.println("file "+args[j]+" ignored");
                 System.out.println("usage: file_1.obj|file_1.3ds [file_1.mtl] file_1.png file_1.jbin ... file_n.obj file_n.mtl file_n.png file_n.jbin");
                }
        //second step: perform the conversion
        int formatIndex=0;
        for(String[] conversionGroupsFilenames:conversionGroupsFilenamesList)
            {convert(conversionGroupsFilenames[0],conversionGroupsFilenames[1],
                    conversionGroupsFilenames[2],conversionGroupsFilenames[3],formatList.get(formatIndex));
             formatIndex++;
            }
        //build the full world
        HashMap<String,EntityParameters> entityParameterTable=new HashMap<String,EntityParameters>();
        EntityParameters param=new EntityParameters();
        param.setRotation(new Quaternion().fromAngles(FastMath.PI/2.0f,0.0f,-FastMath.PI/4.0f));
        param.setScale(new Vector3f(0.001f,0.001f,0.001f));
        entityParameterTable.put("/jbin/pistol.jbin",param);
        param=new EntityParameters();
        param.setRotation(new Quaternion().fromAngles(0.0f,-FastMath.PI/2.0f,0.0f));
        param.setScale(new Vector3f(0.02f,0.02f,0.02f));    
        entityParameterTable.put("/jbin/pistol2.jbin",param);
        param=new EntityParameters();
        param.setRotation(new Quaternion().fromAngles(0.0f,-FastMath.PI/2.0f,0.0f));
        param.setScale(new Vector3f(0.03f,0.03f,0.03f));
        entityParameterTable.put("/jbin/pistol3.jbin",param);
        param=new EntityParameters();
        param.setRotation(new Quaternion().fromAngles(0.0f,-FastMath.PI/2.0f,0.0f));
        param.setScale(new Vector3f(0.2f,0.2f,0.2f));
        entityParameterTable.put("/jbin/smach.jbin",param);
        param=new EntityParameters();
        param.setRotation(new Quaternion().fromAngles(0.0f,-FastMath.PI/2.0f,0.0f));
        param.setScale(new Vector3f(0.2f,0.2f,0.2f));    
        entityParameterTable.put("/jbin/uzi.jbin",param);
        param=new EntityParameters();
        param.setRotation(new Quaternion().fromAngles(0.0f,-FastMath.PI/2.0f,0.0f));
        param.setScale(new Vector3f(0.03f,0.03f,0.03f));
        entityParameterTable.put("/jbin/laser.jbin",param);
        param=new EntityParameters();
        param.setRotation(new Quaternion().fromAngles(0.0f,-FastMath.PI/2.0f,0.0f));
        param.setScale(new Vector3f(0.018f,0.018f,0.018f));
        param.setTranslation(new Vector3f(0.0f,-0.07f,-0.0f));
        param.setAlternativeTexturePath("agent.png");
        entityParameterTable.put("/jbin/agent.jbin",param);    
        FullWorld world=new FullWorld();
        world.setEntityParameterTable(entityParameterTable);
        Utils.encodeObjectInFile(world,worldFilename);
        if(levelName!=null)
            {float[] spawnPos=null;
             DataInputStream in=new DataInputStream(new BufferedInputStream(JMEDataPreprocessor.class.getResourceAsStream("/"+tilesFilename)));
             //for each artwork, we skip 5 coordinates of 4 bytes (float)
             //we skip the collision map too (256*256 bytes)
             int skipedBytesCount=((in.readInt()+in.readInt()+in.readInt()+in.readInt())*20)+65536;
             if(in.skipBytes(skipedBytesCount)!=skipedBytesCount)
                 throw new IOException("malformed file /data/worldmap.data");
             else
                 {spawnPos=new float[]{in.readInt(),0.0f,in.readInt()};
                  System.out.println("spawn position: "+Arrays.toString(spawnPos));
                 }
             in.close();
             NodeIdentifier[] nodeIdentifiers=new NodeIdentifier[identifiedNodeNameList.size()];
             for(int index=0;index<nodeIdentifiers.length;index++)
                 nodeIdentifiers[index]=NodeIdentifier.getInstance(identifiedNodeNameList.get(index));
             HashMap<Vector3f,String> entityLocationTable=new HashMap<Vector3f, String>();
             //handle the weapons
             entityLocationTable.put(new Vector3f(115.0f,0.0f,220.0f),"/jbin/pistol.jbin");
             entityLocationTable.put(new Vector3f(115.25f,0.0f,220.0f),"/jbin/pistol2.jbin");
             entityLocationTable.put(new Vector3f(114.5f,0.0f,220.0f),"/jbin/pistol3.jbin");
             entityLocationTable.put(new Vector3f(114.0f,0.0f,220.0f),"/jbin/smach.jbin");
             entityLocationTable.put(new Vector3f(113.5f,0.0f,220.0f),"/jbin/uzi.jbin");
             entityLocationTable.put(new Vector3f(114.75f,0.0f,220.0f),"/jbin/laser.jbin");
             //handle the enemy
             entityLocationTable.put(new Vector3f(118.0f,0.0f,220.0f),"/jbin/agent.jbin");                         
             //build the full level model
             FullLevel level=new FullLevel();
             level.setNodeIdentifiers(nodeIdentifiers);
             level.setInitialPlayerPosition(new Vector3f(spawnPos[0],spawnPos[1],spawnPos[2]));
             level.setEntityLocationTable(entityLocationTable);
             Utils.encodeObjectInFile(level,levelFilename);
            }       
    }
    
    private static final void convert(String sourceFilename,String MTLlibFilename,String textureFilename,String destFilename,Format format){
        if(format.equals(Format.OBJ))
            {try{objConverter.setProperty("mtllib",new File(MTLlibFilename).toURI().toURL());
                 objConverter.setProperty("texdir",new File(textureFilename).toURI().toURL());
                } 
             catch(MalformedURLException murle)
             {murle.printStackTrace();}                
             objConverter.attemptFileConvert(new String[]{sourceFilename,destFilename});
            }
        else
            if(format.equals(Format.MAX))
                {try{maxConverter.setProperty("texdir",new File(textureFilename).toURI().toURL());} 
                 catch(MalformedURLException murle)
                 {murle.printStackTrace();}                
                 maxConverter.attemptFileConvert(new String[]{sourceFilename,destFilename});
                }
            else
                if(format.equals(Format.MD2))
                    {try{md2Converter.setProperty("texdir",new File(textureFilename).toURI().toURL());} 
                     catch(MalformedURLException murle)
                     {murle.printStackTrace();}                
                     md2Converter.attemptFileConvert(new String[]{sourceFilename,destFilename});
                    }
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
