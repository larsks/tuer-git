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

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.AbstractMap.SimpleEntry;

/**
 * This class is a kind of decorator to use a network set as a network. A network set
 * contains several networks, i.e several connected graphs
 * @author Julien Gouesse
 *
 */
public final class NetworkSet implements Serializable{
    
    
    private static final long serialVersionUID = 1L;
    
    private List<Network> networksList;
    
    public NetworkSet(){
        networksList=new ArrayList<Network>();
    }
    
    public NetworkSet(List<Full3DCell> full3DCellsList){
        networksList=new ArrayList<Network>();
        List<Full3DCell> cellsList=new ArrayList<Full3DCell>();
        cellsList.addAll(full3DCellsList);
        //connect each cell to its neighbors
        Network.connectCellsTogether(cellsList);       
        Full3DCell c;
        List<Full3DCell> subCellsList;
        Network network;
        //Each cell that has been seen has to be marked to avoid an infinite loop
        List<Full3DCell> markedCellsList=new ArrayList<Full3DCell>();
        //First In First Out abstract data type used to store the sons of the current cell
        List<Full3DCell> fifo=new ArrayList<Full3DCell>();
        while(!cellsList.isEmpty())
            {network=new Network();
             network.setRootCell(cellsList.get(0));  
             //now we use the BFS to get all connected cells of a single graph
             subCellsList=new ArrayList<Full3DCell>();
             markedCellsList.clear();
             //We use the first traveled cell suggested by the user
             markedCellsList.add(network.getRootCell());
             fifo.add(network.getRootCell());
             while(!fifo.isEmpty())
                 {//Get the first added element as it is a FIFO (pop operation)
                  c=fifo.remove(0);
                  //This is the main treatment, save all connected cells of a single graph
                  subCellsList.add(c);
                  for(Full3DCell son:c.getNeighboursCellsList())
                      if(!markedCellsList.contains(son))
                          {//Mark the cell to avoid traveling it more than once
                           markedCellsList.add(son);
                           //Add a new cell to travel (push operation)
                           fifo.add(son);
                          }
                 }
             network.setCellsList(subCellsList);
             networksList.add(network);
             //we remove the cells already used by the most recently created network
             cellsList.removeAll(subCellsList);            
            }
    }
    
    private final void writeObject(java.io.ObjectOutputStream out)throws IOException{
        out.writeObject(networksList);
    }
    
    private final void readObject(java.io.ObjectInputStream in)throws IOException, ClassNotFoundException{
        networksList=(List<Network>)in.readObject();       
    }

    public final Entry<Full3DCell,Integer> locate(float x,float y,float z){
        return(locate(x,y,z,null));
    }
    
    /**
     * 
     * @param x
     * @param y
     * @param z
     * @param previousPositioning: a couple composed of a cell and the index of the network
     * @return the new positioning
     */
    public final Entry<Full3DCell,Integer> locate(float x,float y,float z,Entry<Full3DCell,Integer> previousPositioning){
        int previousNetworkIndex;
        Full3DCell previousFull3DCell;
        if(previousPositioning!=null)
            {previousNetworkIndex=previousPositioning.getValue().intValue();
             previousFull3DCell=previousPositioning.getKey();
            }
        else
            {previousNetworkIndex=0;
             previousFull3DCell=networksList.get(0).getRootCell();
            }
        Network network;
        Full3DCell currentPositioningCell=null;
        final int networkCount=networksList.size();
        int currentNetworkIndex=-1;
        for(int networkIndex=previousNetworkIndex,j=0;j<networkCount&&currentPositioningCell==null;j++,networkIndex=(networkIndex+1)%networkCount)
            {network=networksList.get(networkIndex);
             if(networkIndex==previousNetworkIndex)
                 currentPositioningCell=Network.locate(x,y,z,previousFull3DCell);
             else
                 currentPositioningCell=network.locate(x,y,z);  
             currentNetworkIndex=networkIndex;
            }
        Entry<Full3DCell,Integer> currentPositioning;
        if(currentPositioningCell!=null)
            currentPositioning=new SimpleEntry<Full3DCell,Integer>(currentPositioningCell,currentNetworkIndex);
        else
            //this should never happen, it means that you are outside all networks
            currentPositioning=null;
        return(currentPositioning);
    }

    public final List<Network> getNetworksList(){
        return(networksList);
    }

    public final void setNetworksList(List<Network> networksList){
        this.networksList=networksList;
    }
    
    /**
     * 
     * @param filenamepattern
     * @param textureFilename the path of the texture used for the terrain; 
     *                        if null, textures coordinates are ignored
     * @param grouped
     * @param redundant
     */
    final void writeObjFiles(String filenamepattern,String textureFilename,boolean grouped,boolean redundant){
        final boolean useTexture=(textureFilename!=null&&!textureFilename.equals(""));
        final int slashIndex=filenamepattern.lastIndexOf("/");
        final String directoryname=slashIndex>0?filenamepattern.substring(0,slashIndex):"";
        final String filenamePrefix=slashIndex>0&&slashIndex+1<filenamepattern.length()?filenamepattern.substring(slashIndex+1):filenamepattern;       
        final String MTLFilename="terrain.mtl";
        BufferedOutputStream bos=null;
        PrintWriter pw=null;
        if(useTexture)
            {System.out.println("Starts writing MTL file used for the terrain...");
             try{bos=TilesGenerator.createNewFileFromLocalPathAndGetBufferedStream(directoryname+"/"+MTLFilename);}
             catch(IOException ioe)
             {ioe.printStackTrace();return;}
             pw=new PrintWriter(bos);
             //write a MTL file
             pw.println("newmtl terrain");
             pw.println("Ns 0");
             pw.println("Ka 0.000000 0.000000 0.000000");
             pw.println("Kd 0.8 0.8 0.8");
             pw.println("Ks 0.8 0.8 0.8");
             pw.println("d 1");
             pw.println("illum 2");
             pw.println("map_Kd "+textureFilename.substring(textureFilename.lastIndexOf("/")+1));
             System.out.println("Ends writing MTL file used for the terrain.");
             try{pw.close();
                 bos.close();
                } 
             catch(IOException ioe)
             {ioe.printStackTrace();}
             finally
             {bos=null;
              pw=null;
             }   
            }
        int facePrimitiveCount=0;
        if(grouped)
            {System.out.println("Starts writing the single OBJ Wavefront file...");
             System.out.println("Writes Wavefront object "+filenamePrefix+".obj");
             try{bos=TilesGenerator.createNewFileFromLocalPathAndGetBufferedStream(filenamepattern+".obj");}
             catch(IOException ioe)
             {ioe.printStackTrace();return;}
             pw=new PrintWriter(bos);
             //declare the MTL file
             if(useTexture)
                 pw.println("mtllib "+MTLFilename);            
             if(redundant)
                 {for(Network network:networksList)
                      for(Full3DCell cell:network.getCellsList())
                          {for(float[] wall:cell.getBottomWalls())
                               pw.println("v "+wall[2]+" "+wall[3]+" "+wall[4]);
                           for(float[] wall:cell.getCeilWalls())
                               pw.println("v "+wall[2]+" "+wall[3]+" "+wall[4]);
                           for(float[] wall:cell.getFloorWalls())
                               pw.println("v "+wall[2]+" "+wall[3]+" "+wall[4]);
                           for(float[] wall:cell.getLeftWalls())
                               pw.println("v "+wall[2]+" "+wall[3]+" "+wall[4]);
                           for(float[] wall:cell.getRightWalls())
                               pw.println("v "+wall[2]+" "+wall[3]+" "+wall[4]);
                           for(float[] wall:cell.getTopWalls())
                               pw.println("v "+wall[2]+" "+wall[3]+" "+wall[4]);
                           //count the total amount of face primitives by 
                           //summing the face primitives per cell
                           facePrimitiveCount+=(cell.getBottomWalls().size()+
                                   cell.getCeilWalls().size()+
                                   cell.getFloorWalls().size()+
                                   cell.getLeftWalls().size()+
                                   cell.getRightWalls().size()+
                                   cell.getTopWalls().size())/4;
                          }                              
                  if(useTexture)
                      {//write the texture coordinates
                       for(Network network:networksList)
                           for(Full3DCell cell:network.getCellsList())
                               {for(float[] wall:cell.getBottomWalls())
                                    pw.println("vt "+wall[0]+" "+wall[1]);
                                for(float[] wall:cell.getCeilWalls())
                                    pw.println("vt "+wall[0]+" "+wall[1]);
                                for(float[] wall:cell.getFloorWalls())
                                    pw.println("vt "+wall[0]+" "+wall[1]);
                                for(float[] wall:cell.getLeftWalls())
                                    pw.println("vt "+wall[0]+" "+wall[1]);
                                for(float[] wall:cell.getRightWalls())
                                    pw.println("vt "+wall[0]+" "+wall[1]);
                                for(float[] wall:cell.getTopWalls())
                                    pw.println("vt "+wall[0]+" "+wall[1]);
                               }
                       pw.println("usemtl terrain");
                       //smoothing
                       pw.println("s 1");
                       //write faces (we already know the count of face primitives)
                       for(int i=0,tmp;i<facePrimitiveCount;i++)
                           {tmp=4*i+1;
                            pw.println("f "+tmp+"/"+tmp+" "+(tmp+1)+"/"+(tmp+1)+" "+(tmp+2)+"/"+(tmp+2)+" "+(tmp+3)+"/"+(tmp+3));
                           }
                      }
                  else
                      {//write faces (we already know the count of face primitives)
                       for(int i=0,tmp;i<facePrimitiveCount;i++)
                           {tmp=4*i+1;
                            pw.println("f "+tmp+" "+(tmp+1)+" "+(tmp+2)+" "+(tmp+3));
                           }
                      }
                 }
             else
                 {final boolean useNaiveImplementation=true;
                  if(!useNaiveImplementation)
                      {HashMap<Full3DCell,Map.Entry<LinkedHashMap<Integer,Integer>,LinkedHashMap<VertexData,Integer>>> cellularMapsTable=new HashMap<Full3DCell,Map.Entry<LinkedHashMap<Integer,Integer>,LinkedHashMap<VertexData,Integer>>>();
                       for(Network network:networksList)
                           new NetworkStructuralRedundancyAnalyzer(network,cellularMapsTable);
                       //the network set has been analyzed, we can write the vertex data in the file
                       //use the BFS to write vertex data in the same order than during the analysis
                       for(Network network:networksList)
                           new NetworkVertexDataWriter(network,cellularMapsTable,pw);
                       //write the face primitives
                       if(useTexture)
                           {LinkedHashMap<Integer,Integer> duplicateToUniqueIndexationTable=new LinkedHashMap<Integer, Integer>();
                            LinkedHashMap<TextureCoordData,Integer> textureCoordDataToUniqueIndexationTable=new LinkedHashMap<TextureCoordData, Integer>();
                            for(Network network:networksList)
                                new NetworkTextureCoordRedundancyAnalyzer(network,duplicateToUniqueIndexationTable,textureCoordDataToUniqueIndexationTable);
                            for(Network network:networksList)
                                new NetworkTexturedFaceDataWriter(network,duplicateToUniqueIndexationTable,textureCoordDataToUniqueIndexationTable,pw);
                           }
                       else
                           for(Network network:networksList)
                               new NetworkUntexturedFaceDataWriter(network,cellularMapsTable,pw);                   
                      }
                  else
                      {LinkedHashMap<float[],ArrayList<Integer>> verticesIndirectionTable=new LinkedHashMap<float[], ArrayList<Integer>>();
                       Map.Entry<float[],ArrayList<Integer>> foundVertexEntry;
                       int compactVertexIndex=0;
                       int uncompactVertexIndex=0;
                       ArrayList<List<float[]>> wallsListList=new ArrayList<List<float[]>>();
                       for(Network network:networksList)                 
                           for(Full3DCell cell:network.getCellsList())
                               {wallsListList.add(cell.getBottomWalls());
                                wallsListList.add(cell.getCeilWalls());
                                wallsListList.add(cell.getFloorWalls());
                                wallsListList.add(cell.getLeftWalls());
                                wallsListList.add(cell.getRightWalls());
                                wallsListList.add(cell.getTopWalls());
                                for(List<float[]> wallsList:wallsListList)
                                    for(float[] wall:wallsList)
                                        {foundVertexEntry=null;                     
                                         //look sequentially for the vertex
                                         for(Map.Entry<float[],ArrayList<Integer>> verticesEntry:verticesIndirectionTable.entrySet())
                                             if(verticesEntry.getKey()[2]==wall[2]&&
                                                 verticesEntry.getKey()[3]==wall[3]&&
                                                 verticesEntry.getKey()[4]==wall[4])
                                                 {foundVertexEntry=verticesEntry;
                                                  break;
                                                 }                                  
                                         //if unknown
                                         if(foundVertexEntry==null)
                                             {//write it
                                              pw.println("v "+wall[2]+" "+wall[3]+" "+wall[4]);
                                              ArrayList<Integer> indicesList=new ArrayList<Integer>();
                                              //This list contains the vertex index 
                                              //of the order in the file first and 
                                              //the indices of the redundant vertices
                                              indicesList.add(Integer.valueOf(compactVertexIndex));
                                              indicesList.add(Integer.valueOf(uncompactVertexIndex));
                                              //add it into the table
                                              verticesIndirectionTable.put(wall,indicesList);
                                              //update the index of the vertex so 
                                              //that it matches with the order of 
                                              //the vertices as they are written 
                                              //in the file
                                              compactVertexIndex++;
                                             }              
                                         //else
                                         else
                                             {//update the value in the table
                                              foundVertexEntry.getValue().add(Integer.valueOf(uncompactVertexIndex));
                                             }  
                                         uncompactVertexIndex++;
                                        }           
                                //count the total amount of face primitives by 
                                //summing the face primitives per cell
                                facePrimitiveCount+=(cell.getBottomWalls().size()+
                                      cell.getCeilWalls().size()+
                                      cell.getFloorWalls().size()+
                                      cell.getLeftWalls().size()+
                                      cell.getRightWalls().size()+
                                      cell.getTopWalls().size())/4;
                                wallsListList.clear();
                               }               
                       if(useTexture)
                           {//write the texture coordinates
                            int compactTextureCoordIndex=0;
                            int uncompactTextureCoordIndex=0;
                            Map.Entry<float[],ArrayList<Integer>> foundTextureCoordEntry;
                            LinkedHashMap<float[],ArrayList<Integer>> textureCoordIndirectionTable=new LinkedHashMap<float[], ArrayList<Integer>>();
                            for(Network network:networksList)
                                for(Full3DCell cell:network.getCellsList())
                                    {wallsListList.add(cell.getBottomWalls());
                                     wallsListList.add(cell.getCeilWalls());
                                     wallsListList.add(cell.getFloorWalls());
                                     wallsListList.add(cell.getLeftWalls());
                                     wallsListList.add(cell.getRightWalls());
                                     wallsListList.add(cell.getTopWalls());
                                     for(List<float[]> wallsList:wallsListList)
                                         for(float[] wall:wallsList)
                                             {foundTextureCoordEntry=null;
                                              for(Map.Entry<float[],ArrayList<Integer>> textureCoordEntry:textureCoordIndirectionTable.entrySet())
                                                  if(textureCoordEntry.getKey()[0]==wall[0]&&
                                                     textureCoordEntry.getKey()[1]==wall[1])
                                                      {foundTextureCoordEntry=textureCoordEntry;
                                                       break;
                                                      }                                  
                                              //if unknown
                                              if(foundTextureCoordEntry==null)
                                                  {//write it
                                                   pw.println("vt "+wall[0]+" "+wall[1]);
                                                   ArrayList<Integer> indicesList=new ArrayList<Integer>();
                                                   indicesList.add(Integer.valueOf(compactTextureCoordIndex));
                                                   indicesList.add(Integer.valueOf(uncompactTextureCoordIndex));
                                                   //add it into the table
                                                   textureCoordIndirectionTable.put(wall,indicesList);
                                                   compactTextureCoordIndex++;
                                                  }              
                                              //else
                                              else
                                                  {//update the value in the table
                                                   foundTextureCoordEntry.getValue().add(Integer.valueOf(uncompactTextureCoordIndex));
                                                  }  
                                              uncompactTextureCoordIndex++;                           
                                             }
                                     wallsListList.clear();
                                    }
                            //write faces (we already know the count of face primitives)
                            //but use the indirection tables
                            for(int i=0,tmp;i<facePrimitiveCount;i++)
                                {tmp=4*i+1;
                                 pw.print("f");
                                 for(int j=tmp;j<tmp+4;j++)
                                     {uncompactVertexIndex=-1;
                                      for(ArrayList<Integer> verticesIndicesList:verticesIndirectionTable.values())
                                          {//look at not compacted indices
                                           for(int k=1;k<verticesIndicesList.size()&&uncompactVertexIndex==-1;k++)
                                               if(verticesIndicesList.get(k).intValue()==j)
                                                   uncompactVertexIndex=verticesIndicesList.get(0);
                                           if(uncompactVertexIndex!=-1)
                                               break;
                                          }
                                      if(uncompactVertexIndex!=-1)
                                          pw.print(" "+uncompactVertexIndex);
                                      else
                                          System.out.println("[warning]: no compact vertex index found for "+j);
                                      uncompactTextureCoordIndex=-1;
                                      for(ArrayList<Integer> textureCoordIndicesList:textureCoordIndirectionTable.values())
                                          {//look at not compacted indices
                                           for(int k=1;k<textureCoordIndicesList.size()&&uncompactTextureCoordIndex==-1;k++)
                                               if(textureCoordIndicesList.get(k).intValue()==j)
                                                   uncompactTextureCoordIndex=textureCoordIndicesList.get(0);
                                           if(uncompactTextureCoordIndex!=-1)
                                               break;
                                          }
                                      if(uncompactTextureCoordIndex!=-1)
                                          pw.print("/"+uncompactTextureCoordIndex);
                                      else
                                          System.out.println("[warning]: no compact texture coord index found for "+j);
                                     }
                                 pw.println();
                                }
                           }
                       else
                           {//write faces (we already know the count of face primitives)
                            //but use the indirection table
                            for(int i=0,tmp;i<facePrimitiveCount;i++)
                                {tmp=4*i+1;
                                 pw.print("f");
                                 for(int j=tmp;j<tmp+4;j++)
                                     {uncompactVertexIndex=-1;
                                      for(ArrayList<Integer> verticesIndicesList:verticesIndirectionTable.values())
                                          {//look at not compacted indices
                                           for(int k=1;k<verticesIndicesList.size()&&uncompactVertexIndex==-1;k++)
                                               if(verticesIndicesList.get(k).intValue()==j)
                                                   uncompactVertexIndex=verticesIndicesList.get(0);
                                           if(uncompactVertexIndex!=-1)
                                               break;
                                          }
                                      if(uncompactVertexIndex!=-1)
                                          pw.print(" "+uncompactVertexIndex);
                                      else
                                          System.out.println("[warning]: no compact index found for "+j);
                                     }
                                    pw.println();
                                   }
                           }
                      }
                 }
             try{pw.close();
                 bos.close();
                } 
             catch(IOException ioe)
             {ioe.printStackTrace();}
             System.out.println("Ends writing OBJ Wavefront files.");
             System.out.println("Ends writing the single OBJ Wavefront file.");
            }
        else
            {System.out.println("Starts writing OBJ Wavefront files...");
             int networkID=0,cellID;
             ArrayList<String> subObjFilenameList=new ArrayList<String>();
             for(Network network:networksList)
                 {cellID=0;
                  for(Full3DCell cell:network.getCellsList())
                      {//create a new file by using the pattern and the identifiers
                       final String objfilename=filenamePrefix+"NID"+networkID+"CID"+cellID+".obj";
                       try{bos=TilesGenerator.createNewFileFromLocalPathAndGetBufferedStream(directoryname+"/"+objfilename);}
                       catch(IOException ioe)
                       {ioe.printStackTrace();}
                       if(bos!=null)
                           {pw=new PrintWriter(bos);
                            //declare the MTL file
                            if(useTexture)
                                pw.println("mtllib "+MTLFilename);
                            //write the object name (o objname)
                            pw.println("o "+objfilename);
                            //for each list of walls
                                //for each wall
                                    //write vertices (v x y z)
                            for(float[] wall:cell.getBottomWalls())
                                pw.println("v "+wall[2]+" "+wall[3]+" "+wall[4]);
                            for(float[] wall:cell.getCeilWalls())
                                pw.println("v "+wall[2]+" "+wall[3]+" "+wall[4]);
                            for(float[] wall:cell.getFloorWalls())
                                pw.println("v "+wall[2]+" "+wall[3]+" "+wall[4]);
                            for(float[] wall:cell.getLeftWalls())
                                pw.println("v "+wall[2]+" "+wall[3]+" "+wall[4]);
                            for(float[] wall:cell.getRightWalls())
                                pw.println("v "+wall[2]+" "+wall[3]+" "+wall[4]);
                            for(float[] wall:cell.getTopWalls())
                                pw.println("v "+wall[2]+" "+wall[3]+" "+wall[4]);
                            facePrimitiveCount=(cell.getBottomWalls().size()+
                                    cell.getCeilWalls().size()+
                                    cell.getFloorWalls().size()+
                                    cell.getLeftWalls().size()+
                                    cell.getRightWalls().size()+
                                    cell.getTopWalls().size())/4;
                            if(useTexture)
                                {//for each list of walls
                                     //for each wall
                                         //write texture coordinates (vt x y)
                                 for(float[] wall:cell.getBottomWalls())
                                     pw.println("vt "+wall[0]+" "+wall[1]);
                                 for(float[] wall:cell.getCeilWalls())
                                     pw.println("vt "+wall[0]+" "+wall[1]);
                                 for(float[] wall:cell.getFloorWalls())
                                     pw.println("vt "+wall[0]+" "+wall[1]);
                                 for(float[] wall:cell.getLeftWalls())
                                     pw.println("vt "+wall[0]+" "+wall[1]);
                                 for(float[] wall:cell.getRightWalls())
                                     pw.println("vt "+wall[0]+" "+wall[1]);
                                 for(float[] wall:cell.getTopWalls())
                                     pw.println("vt "+wall[0]+" "+wall[1]);
                                 pw.println("usemtl terrain");
                                 //smoothing
                                 pw.println("s 1");
                                 //for each list of walls
                                     //for each wall
                                         //write faces (f v1/vt1)        
                                 for(int i=0,tmp;i<facePrimitiveCount;i++)
                                     {tmp=4*i+1;
                                      pw.println("f "+tmp+"/"+tmp+" "+(tmp+1)+"/"+(tmp+1)+" "+(tmp+2)+"/"+(tmp+2)+" "+(tmp+3)+"/"+(tmp+3));
                                     }
                                }
                            else
                                {//for each list of walls
                                     //for each wall
                                         //write faces (f v1)        
                                 for(int i=0,tmp;i<facePrimitiveCount;i++)
                                     {tmp=4*i+1;
                                      pw.println("f "+tmp+" "+(tmp+1)+" "+(tmp+2)+" "+(tmp+3));
                                     }                           
                                }     
                            System.out.println("Writes Wavefront object "+objfilename);
                            try{pw.close();
                                bos.close();
                               } 
                            catch(IOException ioe)
                            {ioe.printStackTrace();}
                            finally
                            {pw=null;
                             bos=null;                       
                            }
                            subObjFilenameList.add(objfilename);
                           }
                       cellID++;
                      }
                  networkID++;         
                 }
             //write the main OBJ file that calls the others
             System.out.println("Writes Wavefront object "+filenamePrefix+".obj");
             try{bos=TilesGenerator.createNewFileFromLocalPathAndGetBufferedStream(filenamepattern+".obj");}
             catch(IOException ioe)
             {ioe.printStackTrace();return;}
             pw=new PrintWriter(bos);
             for(String subObjFilename:subObjFilenameList)
                 pw.println("call "+subObjFilename);
             try{pw.close();
                 bos.close();
                } 
             catch(IOException ioe)
             {ioe.printStackTrace();}
             System.out.println("Ends writing OBJ Wavefront files.");
            }
    }
    
    private static final class VertexData{
        
        private float[] vertexCoord;
        
        private VertexData(float[] vertexCoord){
            this.vertexCoord=vertexCoord;
        }
        
        public final boolean equals(Object o){
            boolean result;
            if(o==null||!(o instanceof VertexData))
                result=false;
            else
                {VertexData v=(VertexData)o;
                 result=(vertexCoord==v.vertexCoord)||(vertexCoord[2]==v.vertexCoord[2]&&vertexCoord[3]==v.vertexCoord[3]&&vertexCoord[4]==v.vertexCoord[4]);
                }
            return(result);
        }
        
        public final int hashCode(){
            return((int)vertexCoord[2]);
        }
    }
    
    private static final class TextureCoordData{
        
        private float[] vertexCoord;
        
        private TextureCoordData(float[] vertexCoord){
            this.vertexCoord=vertexCoord;
        }
        
        public final boolean equals(Object o){
            boolean result;
            if(o==null||!(o instanceof VertexData))
                result=false;
            else
                {VertexData v=(VertexData)o;
                 result=(vertexCoord==v.vertexCoord)||(vertexCoord[0]==v.vertexCoord[0]&&vertexCoord[1]==v.vertexCoord[1]);
                }
            return(result);
        }
        
        public final int hashCode(){
            return((int)vertexCoord[0]);
        }
    }
    
    private static final class NetworkStructuralRedundancyAnalyzer extends Network.BreadthFirstSearchVisitor{

        
        private HashMap<Full3DCell,Map.Entry<LinkedHashMap<Integer,Integer>,LinkedHashMap<VertexData,Integer>>> cellularMapsTable;
        
        
        private NetworkStructuralRedundancyAnalyzer(Network network,HashMap<Full3DCell,Map.Entry<LinkedHashMap<Integer,Integer>,LinkedHashMap<VertexData,Integer>>> cellularMapsTable){
            this(network.getRootCell(),cellularMapsTable);
        }
        
        private NetworkStructuralRedundancyAnalyzer(Full3DCell firstVisitedCell,HashMap<Full3DCell,Map.Entry<LinkedHashMap<Integer,Integer>,LinkedHashMap<VertexData,Integer>>> cellularMapsTable){
            super(firstVisitedCell);
            this.cellularMapsTable=cellularMapsTable;
        }       
        

        @Override
        protected boolean performTaskOnCurrentlyVisitedCell(){
            Full3DCell cell=getCurrentlyVisitedCell();
            ArrayList<List<float[]>> wallsVerticesListList=new ArrayList<List<float[]>>();
            wallsVerticesListList.add(cell.getBottomWalls());
            wallsVerticesListList.add(cell.getCeilWalls());
            wallsVerticesListList.add(cell.getFloorWalls());
            wallsVerticesListList.add(cell.getLeftWalls());
            wallsVerticesListList.add(cell.getRightWalls());
            wallsVerticesListList.add(cell.getTopWalls());
            //create the both tables
            LinkedHashMap<Integer,Integer> duplicateToUniqueIndexationTable=new LinkedHashMap<Integer,Integer>();
            LinkedHashMap<VertexData,Integer> vertexDataToUniqueIndexationTable=new LinkedHashMap<VertexData,Integer>();         
            //local variables used in the loop
            int portalVertexIndex,currentPortalVertexIndex,uniqueVertexIndex;
            int uniqueVerticesIndicesCount=0;
            int duplicateVerticesIndicesCount=0;
            Integer knownUniqueVertexIndex;
            Full3DCell neighborCell;
            Map.Entry<LinkedHashMap<Integer,Integer>,LinkedHashMap<VertexData,Integer>> neighborCellEntry;
            LinkedHashMap<VertexData,Integer> neighBorVertexDataToUniqueIndexationTable;
            //for each vertex
            for(List<float[]> wallsVerticesList:wallsVerticesListList)
                for(float[] wallVertex:wallsVerticesList)
                    {currentPortalVertexIndex=0;
                     //-1 is used to know that no vertex equal with this 
                     //vertex of wall has been found
                     portalVertexIndex=-1;
                     //look for this vertex in the vertices contained in the portals
                     for(float[] portalVertex:cell.getNeighboursPortalsList())
                         {//remind: T2_V3 (2 texture coordinates + 3 vertex coordinates)
                          if(portalVertex[2]==wallVertex[2]&&portalVertex[3]==wallVertex[3]&&portalVertex[4]==wallVertex[4])
                              {portalVertexIndex=currentPortalVertexIndex;
                               break;
                              }
                          currentPortalVertexIndex++;
                         }
                     //if the vertex is in a portal
                     if(portalVertexIndex!=-1)
                         {//get the cell that is linked to this portal
                          neighborCell=cell.getNeighboursCellsList().get(portalVertexIndex/4);
                          //use the table of cellular maps to get the tables of the neighbor cell if any
                          neighborCellEntry=cellularMapsTable.get(neighborCell);       
                          //if these tables exist
                          if(neighborCellEntry!=null)
                              {//get the second table of the neighbor cell
                               neighBorVertexDataToUniqueIndexationTable=neighborCellEntry.getValue();              
                               //get the unique index by using this second table
                               knownUniqueVertexIndex=neighBorVertexDataToUniqueIndexationTable.get(new VertexData(wallVertex));
                               //store the unique index
                               uniqueVertexIndex=knownUniqueVertexIndex.intValue();
                              }
                          //else (in this case, the neighbor cell has not yet been visited)
                          else
                              {//compute and store this new unique index
                               uniqueVertexIndex=uniqueVerticesIndicesCount;
                               //increment it in order to ensure the attributed value is really unique 
                               uniqueVerticesIndicesCount++;
                               //put it into the second table
                               vertexDataToUniqueIndexationTable.put(new VertexData(wallVertex),Integer.valueOf(uniqueVertexIndex));
                              }                            
                         }
                     //else
                     else
                         {//if the second table already contains this vertex
                          if((knownUniqueVertexIndex=vertexDataToUniqueIndexationTable.get(new VertexData(wallVertex)))!=null)
                              {//store the unique index
                               uniqueVertexIndex=knownUniqueVertexIndex.intValue();
                              }        
                          //else
                          else
                              {//compute and store this new unique index
                               uniqueVertexIndex=uniqueVerticesIndicesCount;
                               //increment it in order to ensure the attributed value is really unique 
                               uniqueVerticesIndicesCount++;
                               //put it into the second table
                               vertexDataToUniqueIndexationTable.put(new VertexData(wallVertex),Integer.valueOf(uniqueVertexIndex));
                              }               
                         }          
                     //fill the first table with the new duplicate index and the unique index
                     duplicateToUniqueIndexationTable.put(Integer.valueOf(duplicateVerticesIndicesCount),Integer.valueOf(uniqueVertexIndex));
                     //update the count of duplicate vertices
                     duplicateVerticesIndicesCount++;
                    }               
            //put the both tables in the table of cellular maps
            cellularMapsTable.put(cell,new SimpleEntry<LinkedHashMap<Integer,Integer>,LinkedHashMap<VertexData,Integer>>(duplicateToUniqueIndexationTable,vertexDataToUniqueIndexationTable));
            //go on visiting the network
            return(true);
        }
        
    }
    
    private static final class NetworkTextureCoordRedundancyAnalyzer extends Network.BreadthFirstSearchVisitor{
        
        
        private LinkedHashMap<Integer,Integer> duplicateToUniqueIndexationTable;
        
        private LinkedHashMap<TextureCoordData,Integer> textureCoordDataToUniqueIndexationTable;
        
        
        private NetworkTextureCoordRedundancyAnalyzer(Network network,LinkedHashMap<Integer,Integer> duplicateToUniqueIndexationTable,LinkedHashMap<TextureCoordData,Integer> textureCoordDataToUniqueIndexationTable){
            super(network);
            this.duplicateToUniqueIndexationTable=duplicateToUniqueIndexationTable;
            this.textureCoordDataToUniqueIndexationTable=textureCoordDataToUniqueIndexationTable;
        }

        
        @Override
        protected final boolean performTaskOnCurrentlyVisitedCell(){
            //TODO: implement it
            return(true);
        }       
    }
    
    private static final class NetworkVertexDataWriter extends Network.BreadthFirstSearchVisitor{

        
        private HashMap<Full3DCell,Map.Entry<LinkedHashMap<Integer,Integer>,LinkedHashMap<VertexData,Integer>>> cellularMapsTable;
        
        private PrintWriter pw;
        
        
        private NetworkVertexDataWriter(Network network,HashMap<Full3DCell,Map.Entry<LinkedHashMap<Integer,Integer>,LinkedHashMap<VertexData,Integer>>> cellularMapsTable,PrintWriter pw){
            this(network.getRootCell(),cellularMapsTable,pw);
        }
        
        private NetworkVertexDataWriter(Full3DCell firstVisitedCell,HashMap<Full3DCell,Map.Entry<LinkedHashMap<Integer,Integer>,LinkedHashMap<VertexData,Integer>>> cellularMapsTable,PrintWriter pw){
            super(firstVisitedCell);
            this.cellularMapsTable=cellularMapsTable;
            this.pw=pw;
        }       
        

        @Override
        protected boolean performTaskOnCurrentlyVisitedCell(){
            //As we use a LinkedHashMap, we can benefit of the insertion order
            for(VertexData vertexData:cellularMapsTable.get(getCurrentlyVisitedCell()).getValue().keySet())
                pw.println("v "+vertexData.vertexCoord[2]+" "+vertexData.vertexCoord[3]+" "+vertexData.vertexCoord[4]);
            //go on visiting the network
            return(true);
        }
        
    }
    
    private static final class NetworkTexturedFaceDataWriter extends Network.BreadthFirstSearchVisitor{


        private LinkedHashMap<Integer,Integer> duplicateToUniqueIndexationTable;

        private LinkedHashMap<TextureCoordData,Integer> textureCoordDataToUniqueIndexationTable;

        private PrintWriter pw;
        
        
        public NetworkTexturedFaceDataWriter(Network network,
                LinkedHashMap<Integer, Integer> duplicateToUniqueIndexationTable,
                LinkedHashMap<TextureCoordData, Integer> textureCoordDataToUniqueIndexationTable,
                PrintWriter pw){
            super(network);
            this.duplicateToUniqueIndexationTable=duplicateToUniqueIndexationTable;
            this.textureCoordDataToUniqueIndexationTable=textureCoordDataToUniqueIndexationTable;
            this.pw=pw;
        }

        
        @Override
        protected boolean performTaskOnCurrentlyVisitedCell(){
            //TODO
            //go on visiting the network
            return(true);
        }
    }
    
    private static final class NetworkUntexturedFaceDataWriter extends Network.BreadthFirstSearchVisitor{

        
        private HashMap<Full3DCell,Map.Entry<LinkedHashMap<Integer,Integer>,LinkedHashMap<VertexData,Integer>>> cellularMapsTable;
        
        private PrintWriter pw;
        
        
        private NetworkUntexturedFaceDataWriter(Network network,HashMap<Full3DCell,Map.Entry<LinkedHashMap<Integer,Integer>,LinkedHashMap<VertexData,Integer>>> cellularMapsTable,PrintWriter pw){
            this(network.getRootCell(),cellularMapsTable,pw);
        }
        
        private NetworkUntexturedFaceDataWriter(Full3DCell firstVisitedCell,HashMap<Full3DCell,Map.Entry<LinkedHashMap<Integer,Integer>,LinkedHashMap<VertexData,Integer>>> cellularMapsTable,PrintWriter pw){
            super(firstVisitedCell);
            this.cellularMapsTable=cellularMapsTable;
            this.pw=pw;
        }       
        

        @Override
        protected boolean performTaskOnCurrentlyVisitedCell(){
            ArrayList<Integer> uniqueVertexIndices=new ArrayList<Integer>();           
            //As we use a LinkedHashMap, we can benefit of the insertion order
            for(Integer uniqueVertexIndex:cellularMapsTable.get(getCurrentlyVisitedCell()).getKey().values())
                {//add the current index
                 uniqueVertexIndices.add(uniqueVertexIndex);
                 //if the list is full
                 if(uniqueVertexIndices.size()==4)
                     {//write the face primitive
                      pw.print("f "+uniqueVertexIndices.get(0).intValue()+" "+
                                    uniqueVertexIndices.get(1).intValue()+" "+
                                    uniqueVertexIndices.get(2).intValue()+" "+
                                    uniqueVertexIndices.get(3).intValue());
                      //empty the list
                      uniqueVertexIndices.clear();
                     }
                }
            //go on visiting the network
            return(true);
        }
        
    }
}
