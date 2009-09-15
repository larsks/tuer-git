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
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.AbstractMap.SimpleEntry;

/**
 * This class is a kind of decorator to use a network set as a network. A network set
 * contains several networks, i.e several connected graphs.
 * Rather use this class when you're not sure all cells compose a single network
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
             Full3DCell son;
             while(!fifo.isEmpty())
                 {//Get the first added element as it is a FIFO (pop operation)
                  c=fifo.remove(0);
                  //This is the main treatment, save all connected cells of a single graph
                  subCellsList.add(c);
                  for(int i=0;i<c.getNeighboursCount();i++)
                      {son=c.getNeighbourCell(i);
                       if(!markedCellsList.contains(son))
                           {//Mark the cell to avoid traveling it more than once
                            markedCellsList.add(son);
                            //Add a new cell to travel (push operation)
                            fifo.add(son);
                           }
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
                 currentPositioningCell=network.locate(previousFull3DCell,x,y,z);
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
     * writes the data of the network into files
     * @param filenamepattern 
     * @param textureFilename the path of the texture used for the terrain; 
     *                        if null, textures coordinates are ignored
     * @param grouped if true, then it creates a single OBJ file for the 
     *        whole network set, otherwise it creates a file per cell
     * @param redundant if true, then the redundant vertices are kept, otherwise they are removed
     * @param useTriangles if true, then the quads are cut into triangles, otherwise the quads are kept
     * @param useJOGLTextureCoordinatesVerticalOrder true if the vertical order of texture coordinates is JOGL's one
     * @param writePortals true if the portals have to be written into files too (available only when the parameter "grouped" is at false)
     * @param MTLFilename MTL filename (an MTL file describes the materials used by an OBJ file)
     * @param useOneGroupPerQuad true if one group per quad is created
     * @param smoothing smoothing group (see the description of the OBJ format)
     * @param materialNames name of the materials
     * @param useOneFilePerWall true if it creates one file per wall, otherwise it creates a file per cell
     */
    final void writeObjFiles(final String filenamepattern,
            final String[] textureFilenames,
            final boolean grouped,final boolean redundant,final boolean useTriangles,
            final boolean useJOGLTextureCoordinatesVerticalOrder,
            final boolean writePortals,String MTLFilename,
            final boolean useOneGroupPerQuad,final int smoothing,
            final String[] materialNames,final boolean useOneFilePerWall){
        ArrayList<INodeIdentifier> nodeIDList=new ArrayList<INodeIdentifier>();
        final boolean useTexture=textureFilenames!=null&&textureFilenames.length>0;
        final int slashIndex=filenamepattern.lastIndexOf("/");
        final String directoryname=slashIndex>0?filenamepattern.substring(0,slashIndex):"";
        final String filenamePrefix=slashIndex>0&&slashIndex+1<filenamepattern.length()?filenamepattern.substring(slashIndex+1):filenamepattern;
        if(useTexture)
            TilesGenerator.writeDummyMTLFile(directoryname,MTLFilename,textureFilenames,materialNames);
        BufferedOutputStream bos=null;
        PrintWriter pw=null;
        String objname,objfilename;
        if(grouped)
            {if(writePortals)
                 System.out.println("[WARNING] \"writePortals\" flag ignored as available only in ungrouped mode");
             System.out.println("Starts writing the single OBJ Wavefront file...");
             System.out.println("Writes Wavefront object "+filenamePrefix+".obj");             
             try{bos=TilesGenerator.createNewFileFromLocalPathAndGetBufferedStream(filenamepattern+".obj");}
             catch(IOException ioe)
             {ioe.printStackTrace();return;}
             pw=new PrintWriter(bos);
             //declare the MTL file
             if(useTexture)
                 pw.println("mtllib "+MTLFilename);
             objname=filenamePrefix;
             //write the object name (o objname)
             pw.println("o "+objname);
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
                          }                              
                  if(useTexture)
                      {//write the texture coordinates
                       if(useJOGLTextureCoordinatesVerticalOrder)
                           {for(Network network:networksList)
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
                           }
                       else
                           {for(Network network:networksList)
                               for(Full3DCell cell:network.getCellsList())
                                   {for(float[] wall:cell.getBottomWalls())
                                        pw.println("vt "+wall[0]+" "+(1.0f-wall[1]));
                                    for(float[] wall:cell.getCeilWalls())
                                        pw.println("vt "+wall[0]+" "+(1.0f-wall[1]));
                                    for(float[] wall:cell.getFloorWalls())
                                        pw.println("vt "+wall[0]+" "+(1.0f-wall[1]));
                                    for(float[] wall:cell.getLeftWalls())
                                        pw.println("vt "+wall[0]+" "+(1.0f-wall[1]));
                                    for(float[] wall:cell.getRightWalls())
                                        pw.println("vt "+wall[0]+" "+(1.0f-wall[1]));
                                    for(float[] wall:cell.getTopWalls())
                                        pw.println("vt "+wall[0]+" "+(1.0f-wall[1]));
                                   }
                           }
                      }
                  int faceIndexOffset=0;
                  for(Network network:networksList)
                      for(Full3DCell cell:network.getCellsList())
                          faceIndexOffset+=writeCellFaces(pw,objname,faceIndexOffset,useTexture,useTriangles,useOneGroupPerQuad,materialNames,smoothing,cell);
                 }
             else
                 {//remove this restriction???
                  if(useOneGroupPerQuad)
                      System.out.println("[WARNING] \"useOneGroupPerQuad\" flag ignored as available only in redundant mode");
                  HashMap<Full3DCell,Map.Entry<LinkedHashMap<Integer,Integer>,LinkedHashMap<VertexData,Integer>>> cellularMapsTable=new HashMap<Full3DCell,Map.Entry<LinkedHashMap<Integer,Integer>,LinkedHashMap<VertexData,Integer>>>();
                  int uniqueVerticesIndicesCount=0;                     
                  int duplicateVerticesIndicesCount=0;
                  System.out.println("use NetworkStructuralRedundancyAnalyzer...");
                  for(Network network:networksList)
                      {NetworkStructuralRedundancyAnalyzer nsra = new NetworkStructuralRedundancyAnalyzer(network,cellularMapsTable,uniqueVerticesIndicesCount,duplicateVerticesIndicesCount);
                       nsra.visit();
                       uniqueVerticesIndicesCount=nsra.uniqueVerticesIndicesCount;
                       duplicateVerticesIndicesCount=nsra.duplicateVerticesIndicesCount;
                       System.out.println(cellularMapsTable.size()+" cells "+uniqueVerticesIndicesCount+" unique vertices indices");
                      }             
                  System.out.println("NetworkStructuralRedundancyAnalyzer used");
                  //the network set has been analyzed, we can write the vertex data in the file
                  //use the BFS to write vertex data in the same order than during the analysis
                  System.out.println("use NetworkVertexDataWriter...");
                  for(Network network:networksList)
                      new NetworkVertexDataWriter(network,cellularMapsTable,pw).visit();
                  System.out.println("NetworkVertexDataWriter used");
                  //write the face primitives
                  if(useTexture)
                      {LinkedHashMap<Integer,Integer> duplicateToUniqueIndexationTable=new LinkedHashMap<Integer, Integer>();
                       LinkedHashMap<TextureCoordData,Integer> textureCoordDataToUniqueIndexationTable=new LinkedHashMap<TextureCoordData, Integer>();
                       System.out.println("use NetworkTextureCoordRedundancyAnalyzer...");
                       for(Network network:networksList)
                           new NetworkTextureCoordRedundancyAnalyzer(network,duplicateToUniqueIndexationTable,textureCoordDataToUniqueIndexationTable).visit();
                       System.out.println(duplicateToUniqueIndexationTable.size()+" entries");
                       System.out.println("NetworkTextureCoordRedundancyAnalyzer used");
                       //write texture coordinates
                       System.out.println("Writes texture coordinates...");
                       if(useJOGLTextureCoordinatesVerticalOrder)
                           for(TextureCoordData textureCoordData:textureCoordDataToUniqueIndexationTable.keySet())
                               pw.println("vt "+textureCoordData.textureCoord[0]+" "+textureCoordData.textureCoord[1]);
                       else
                           for(TextureCoordData textureCoordData:textureCoordDataToUniqueIndexationTable.keySet())
                               pw.println("vt "+textureCoordData.textureCoord[0]+" "+(1.0f-textureCoordData.textureCoord[1]));
                       System.out.println("Texture coordinates written"); 
                       writeUseMaterialAndSmoothing(pw,materialNames[0],smoothing);
                       System.out.println("use NetworkTexturedFaceDataWriter...");
                       for(Network network:networksList)
                           new NetworkTexturedFaceDataWriter(network,cellularMapsTable,duplicateToUniqueIndexationTable,pw,useTriangles).visit();
                       System.out.println("NetworkTexturedFaceDataWriter used");
                      }
                  else
                      for(Network network:networksList)
                          new NetworkUntexturedFaceDataWriter(network,cellularMapsTable,pw,useTriangles).visit();                     
                 }
             try{pw.close();
                 bos.close();
                } 
             catch(IOException ioe)
             {ioe.printStackTrace();}
             System.out.println("Ends writing the single OBJ Wavefront file.");
            }
        else
            {System.out.println("Starts writing OBJ Wavefront files...");
             INodeIdentifier distinctNodeID;
             int networkID=0,cellID,levelID=-1;
             int firstDigitIndex=-1;
             for(int i=filenamePrefix.length()-1;i>=0;i--)
                 if(Character.isDigit(filenamePrefix.charAt(i)))
                     firstDigitIndex=i;
                 else
                     break;
             System.out.println("Extracts the level index from \""+filenamePrefix+"\"...");
             if(firstDigitIndex!=-1)
                 levelID=Integer.parseInt(filenamePrefix.substring(firstDigitIndex));
             if(levelID==-1)
                 System.out.println("level index unknown!"); 
             if(useOneFilePerWall)
                 {cellID=0;
                  List<float[]> walls;
                  int elementID;              
                  for(Network network:networksList)
                      {for(Full3DCell cell:network.getCellsList())
                           {elementID=0;
                            walls=cell.getBottomWalls();
                            for(int coordIndex=0;coordIndex<walls.size();coordIndex+=4)
                                {writeCellWallFile(levelID,networkID,cellID,elementID,directoryname,
                                        useTexture,useJOGLTextureCoordinatesVerticalOrder,
                                        useOneGroupPerQuad,smoothing,
                                        new float[][]{walls.get(coordIndex),walls.get(coordIndex+1),
                                                      walls.get(coordIndex+2),walls.get(coordIndex+3)});
                                 elementID++;
                                }
                            walls=cell.getCeilWalls();
                            for(int coordIndex=0;coordIndex<walls.size();coordIndex+=4)
                                {writeCellWallFile(levelID,networkID,cellID,elementID,directoryname,
                                        useTexture,useJOGLTextureCoordinatesVerticalOrder,
                                        useOneGroupPerQuad,smoothing,
                                        new float[][]{walls.get(coordIndex),walls.get(coordIndex+1),
                                                      walls.get(coordIndex+2),walls.get(coordIndex+3)});
                                 elementID++;
                                }
                            walls=cell.getFloorWalls();
                            for(int coordIndex=0;coordIndex<walls.size();coordIndex+=4)
                                {writeCellWallFile(levelID,networkID,cellID,elementID,directoryname,
                                        useTexture,useJOGLTextureCoordinatesVerticalOrder,
                                        useOneGroupPerQuad,smoothing,
                                        new float[][]{walls.get(coordIndex),walls.get(coordIndex+1),
                                                      walls.get(coordIndex+2),walls.get(coordIndex+3)});
                                 elementID++;
                                }
                            walls=cell.getLeftWalls();
                            for(int coordIndex=0;coordIndex<walls.size();coordIndex+=4)
                                {writeCellWallFile(levelID,networkID,cellID,elementID,directoryname,
                                        useTexture,useJOGLTextureCoordinatesVerticalOrder,
                                        useOneGroupPerQuad,smoothing,
                                        new float[][]{walls.get(coordIndex),walls.get(coordIndex+1),
                                                      walls.get(coordIndex+2),walls.get(coordIndex+3)});
                                 elementID++;
                                }
                            walls=cell.getRightWalls();
                            for(int coordIndex=0;coordIndex<walls.size();coordIndex+=4)
                                {writeCellWallFile(levelID,networkID,cellID,elementID,directoryname,
                                        useTexture,useJOGLTextureCoordinatesVerticalOrder,
                                        useOneGroupPerQuad,smoothing,
                                        new float[][]{walls.get(coordIndex),walls.get(coordIndex+1),
                                                      walls.get(coordIndex+2),walls.get(coordIndex+3)});
                                 elementID++;
                                }
                            walls=cell.getTopWalls();
                            for(int coordIndex=0;coordIndex<walls.size();coordIndex+=4)
                                {writeCellWallFile(levelID,networkID,cellID,elementID,directoryname,
                                        useTexture,useJOGLTextureCoordinatesVerticalOrder,
                                        useOneGroupPerQuad,smoothing,
                                        new float[][]{walls.get(coordIndex),walls.get(coordIndex+1),
                                                      walls.get(coordIndex+2),walls.get(coordIndex+3)});
                                 elementID++;
                                }
                            cellID++;
                           }
                       networkID++;
                      }
                 }
             else
                 {ArrayList<String> subObjFilenameList=new ArrayList<String>();             
                  for(Network network:networksList)
                      {cellID=0;
                       for(Full3DCell cell:network.getCellsList())
                           {//create a new file by using the pattern and the identifiers            
                            distinctNodeID=BeanProvider.getInstance().getINodeIdentifier();
                            distinctNodeID.setLevelID(levelID);
                            distinctNodeID.setNetworkID(networkID);
                            distinctNodeID.setCellID(cellID);
                            //FIXME: it has become useless
                            nodeIDList.add(distinctNodeID);
                            objname=distinctNodeID.toString();
                            objfilename=objname+".obj";
                            try{bos=TilesGenerator.createNewFileFromLocalPathAndGetBufferedStream(directoryname+"/"+objfilename);}
                            catch(IOException ioe)
                            {ioe.printStackTrace();}
                            if(bos!=null)
                                {pw=new PrintWriter(bos);
                                 //declare the MTL file
                                 if(useTexture)
                                     pw.println("mtllib "+MTLFilename);
                                 //write the object name (o objname)
                                 pw.println("o "+objname);
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
                                 if(useTexture)
                                     {//for each list of walls
                                          //for each wall
                                              //write texture coordinates (vt x y)
                                      if(useJOGLTextureCoordinatesVerticalOrder)
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
                                      else
                                          {for(float[] wall:cell.getBottomWalls())
                                               pw.println("vt "+wall[0]+" "+(1.0f-wall[1]));
                                           for(float[] wall:cell.getCeilWalls())
                                               pw.println("vt "+wall[0]+" "+(1.0f-wall[1]));
                                           for(float[] wall:cell.getFloorWalls())
                                               pw.println("vt "+wall[0]+" "+(1.0f-wall[1]));
                                           for(float[] wall:cell.getLeftWalls())
                                               pw.println("vt "+wall[0]+" "+(1.0f-wall[1]));
                                           for(float[] wall:cell.getRightWalls())
                                               pw.println("vt "+wall[0]+" "+(1.0f-wall[1]));
                                           for(float[] wall:cell.getTopWalls())
                                               pw.println("vt "+wall[0]+" "+(1.0f-wall[1]));
                                          }
                                     }
                                 writeCellFaces(pw,objname,0,useTexture,useTriangles,useOneGroupPerQuad,materialNames,smoothing,cell);
                                 System.out.println("Writes Wavefront object "+objname);
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
                  System.out.println("Writes Wavefront object "+filenamePrefix);
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
                 }   
             if(writePortals)
                 {//write the portals
                  System.out.println("Starts writing portals...");
                  /* We cannot get all portals by a single visit (BFS) 
                   * because it guarantees that all cells are visited
                   * once but not all portals. All cells can be reached
                   * by using not all portals. That is why we have to 
                   * use this dirty and naive algorithm consisting in
                   * visiting all cells and getting all theirs portals
                   */
                  //The key is built following this rule: NID<nid>CID<cid>CID<cid>
                  //It allows to identify a portal
                  HashMap<String,Full3DPortal> portalsMap=new HashMap<String, Full3DPortal>();
                  networkID=0;
                  String portalKey;
                  Full3DCell[] linkedCells;
                  List<Full3DCell> cellsList;
                  int unknownCIDindex,otherCellID;
                  //for each network
                  for(Network network:networksList)
                      {cellID=0;
                       cellsList=network.getCellsList();
                       //for each cell
                       for(Full3DCell cell:cellsList)
                           {//get all its portals
                            for(Full3DPortal portal:cell.getPortalsList())
                                {linkedCells=portal.getLinkedCells();
                                 if(linkedCells[0]==cell)
                                     unknownCIDindex=1;
                                 else
                                     unknownCIDindex=0;
                                 //find the CID of the other cell
                                 otherCellID=-1;
                                 for(int cellIndex=0;cellIndex<cellsList.size();cellIndex++)
                                     if(linkedCells[unknownCIDindex]==cellsList.get(cellIndex))
                                         {otherCellID=cellIndex;
                                          break;
                                         }
                                 if(otherCellID==-1)
                                     System.out.println("[WARNING] cells of different networks bound in the same portal!!! unknown CID!");
                                 distinctNodeID=BeanProvider.getInstance().getINodeIdentifier();
                                 distinctNodeID.setLevelID(levelID);
                                 distinctNodeID.setNetworkID(networkID);
                                 distinctNodeID.setCellID(cellID);
                                 distinctNodeID.setSecondaryCellID(otherCellID);
                                 portalKey=distinctNodeID.toString();
                                 if(!nodeIDList.contains(distinctNodeID))
                                     {portalsMap.put(portalKey,portal);
                                      nodeIDList.add(distinctNodeID);
                                     }
                                }
                            cellID++;
                           }                      
                       networkID++;
                      }                    
                  //for each portal
                  Full3DPortal portal;
                  int portalVerticesCount;
                  for(Map.Entry<String,Full3DPortal> portalEntry:portalsMap.entrySet())
                      {objname=portalEntry.getKey();
                       objfilename=objname+".obj";
                       try{bos=TilesGenerator.createNewFileFromLocalPathAndGetBufferedStream(directoryname+"/"+objfilename);}
                       catch(IOException ioe)
                       {ioe.printStackTrace();}
                       if(bos!=null)
                           {pw=new PrintWriter(bos);
                            //Do not declare any MTL filebecause it is useless for portals.
                            //A portal contains only vertices but no texture, no light.
                            //Write the object name (o objname)
                            pw.println("o "+objname);
                            portal=portalEntry.getValue();
                            portalVerticesCount=portal.getPortalVertices().length;
                            if(portalVerticesCount!=4)
                                System.out.println("a portal can only be composed of a quad");
                            //write its "v" primitives
                            for(float[] portalVertex:portal.getPortalVertices())
                                pw.println("v "+portalVertex[2]+" "+portalVertex[3]+" "+portalVertex[4]);
                            //write its "f" primitives (ignore textures)
                            writeCellFaces(pw,objname,0,false,useTriangles,false,null,smoothing,null);
                            System.out.println("Writes Wavefront object "+objname);
                            try{pw.close();
                                bos.close();
                               } 
                            catch(IOException ioe)
                            {ioe.printStackTrace();}
                            finally
                            {pw=null;
                             bos=null;                       
                            }
                           }                     
                      }
                  System.out.println("Ends writing portals.");
                 }
             System.out.println("Ends writing OBJ Wavefront files.");
            }
    }
    
    private static final void writeCellWallFile(final int levelID,final int networkID,
                                                final int cellID,final int elementID,
                                                final String directoryname,boolean useTexture,final boolean useJOGLTextureCoordinatesVerticalOrder,
                                                final boolean useOneGroupPerQuad,final int smoothing,
                                                final float[][] cellWall){       
        INodeIdentifier nodeID = BeanProvider.getInstance().getINodeIdentifier();
        nodeID.setLevelID(levelID);
        nodeID.setNetworkID(networkID);
        nodeID.setCellID(cellID);
        //FIXME: standardize the handling of element identifiers
        String objname=nodeID.toString()+"EID"+elementID;
        String objfilename=objname+".obj";
        BufferedOutputStream bos = null;
        try{bos=TilesGenerator.createNewFileFromLocalPathAndGetBufferedStream(directoryname+"/"+objfilename);}
        catch(IOException ioe)
        {ioe.printStackTrace();}
        if(bos!=null)
            {PrintWriter pw=new PrintWriter(bos);           
             //write vertex coordinates
             for(float[] coord:cellWall)
                 pw.println("v "+coord[2]+" "+coord[3]+" "+coord[4]);
             //write texture coordinates
             if(useTexture)
                 {if(useJOGLTextureCoordinatesVerticalOrder)
                      {for(float[] coord:cellWall)
                           pw.println("vt "+coord[0]+" "+coord[1]);
                      }
                  else
                      {for(float[] coord:cellWall)
                           pw.println("vt "+coord[0]+" "+(1.0f-coord[1]));
                      }
                 }
             //TODO: implement it
             //write face coordinates
             //writeCellFaces(pw,objname,0,useTexture,useTriangles,useOneGroupPerQuad,materialNames,smoothing,cell);
             try{pw.close();
                 bos.close();
                } 
             catch(IOException ioe)
             {ioe.printStackTrace();}
             finally
             {pw=null;
              bos=null;                       
             }
            }
    }
    
    private static final int writeCellFaces(PrintWriter pw,final String objname,
            final int faceIndexOffset,final boolean useTexture,
            final boolean useTriangles,final boolean useOneGroupPerQuad,
            final String[] materialNames,final int smoothing,Full3DCell cell){
        final int facePrimitiveCount,bottomFaceCount,topFaceCount;
        final int ceilFaceCount,floorFaceCount,leftFaceCount,rightFaceCount;
        if(cell!=null)
            {bottomFaceCount=cell.getBottomWalls().size()/4;
             topFaceCount=cell.getTopWalls().size()/4;
             ceilFaceCount=cell.getCeilWalls().size()/4;
             floorFaceCount=cell.getFloorWalls().size()/4;
             leftFaceCount=cell.getLeftWalls().size()/4;
             rightFaceCount=cell.getRightWalls().size()/4;
             facePrimitiveCount=bottomFaceCount+ceilFaceCount+floorFaceCount
                                  +leftFaceCount+rightFaceCount+topFaceCount;
            }
        else
            {bottomFaceCount=0;
             topFaceCount=0;
             ceilFaceCount=0;
             floorFaceCount=0;
             leftFaceCount=0;
             rightFaceCount=0;
             facePrimitiveCount=1;
            }
        final int[] faceCounts=new int[]{bottomFaceCount,ceilFaceCount,floorFaceCount,leftFaceCount,rightFaceCount,topFaceCount};
        if(useOneGroupPerQuad)
            {if(useTexture)
                 {if(useTriangles)
                      for(int i=0,eid=0,tmp;i<facePrimitiveCount;i++,eid++)
                          {tmp=4*(i+faceIndexOffset)+1;
                           pw.println("g "+objname+"EID"+eid);
                           writeUseMaterialAndSmoothing(pw,getMaterialName(materialNames,i,faceCounts),smoothing);
                           pw.println("f "+tmp+"/"+tmp+" "+(tmp+1)+"/"+(tmp+1)+" "+(tmp+2)+"/"+(tmp+2));
                           pw.println("f "+(tmp+2)+"/"+(tmp+2)+" "+(tmp+3)+"/"+(tmp+3)+" "+tmp+"/"+tmp);
                          }
                  else
                      for(int i=0,eid=0,tmp;i<facePrimitiveCount;i++,eid++)
                          {tmp=4*(i+faceIndexOffset)+1;
                           pw.println("g "+objname+"EID"+eid);
                           writeUseMaterialAndSmoothing(pw,getMaterialName(materialNames,i,faceCounts),smoothing);
                           pw.println("f "+tmp+"/"+tmp+" "+(tmp+1)+"/"+(tmp+1)+" "+(tmp+2)+"/"+(tmp+2)+" "+(tmp+3)+"/"+(tmp+3));
                          }
                 }
             else
                 {if(useTriangles)
                      for(int i=0,eid=0,tmp;i<facePrimitiveCount;i++,eid++)
                          {tmp=4*(i+faceIndexOffset)+1;
                           pw.println("g "+objname+"EID"+eid);
                           pw.println("f "+tmp+" "+(tmp+1)+" "+(tmp+2));
                           pw.println("f "+(tmp+2)+" "+(tmp+3)+" "+tmp);
                          }
                  else
                      for(int i=0,eid=0,tmp;i<facePrimitiveCount;i++,eid++)
                          {tmp=4*(i+faceIndexOffset)+1;
                           pw.println("g "+objname+"EID"+eid);
                           pw.println("f "+tmp+" "+(tmp+1)+" "+(tmp+2)+" "+(tmp+3));
                          }
                 }

            }
        else
            {if(useTexture)
                 {writeUseMaterialAndSmoothing(pw,getMaterialName(materialNames,0,faceCounts),smoothing);
                  if(useTriangles)
                      for(int i=0,tmp;i<facePrimitiveCount;i++)
                          {tmp=4*(i+faceIndexOffset)+1;
                           pw.println("f "+tmp+"/"+tmp+" "+(tmp+1)+"/"+(tmp+1)+" "+(tmp+2)+"/"+(tmp+2));
                           pw.println("f "+(tmp+2)+"/"+(tmp+2)+" "+(tmp+3)+"/"+(tmp+3)+" "+tmp+"/"+tmp);
                          }
                  else
                      for(int i=0,tmp;i<facePrimitiveCount;i++)
                          {tmp=4*(i+faceIndexOffset)+1;
                           pw.println("f "+tmp+"/"+tmp+" "+(tmp+1)+"/"+(tmp+1)+" "+(tmp+2)+"/"+(tmp+2)+" "+(tmp+3)+"/"+(tmp+3));
                          }
                 }
             else
                 {if(useTriangles)
                      for(int i=0,tmp;i<facePrimitiveCount;i++)
                          {tmp=4*(i+faceIndexOffset)+1;
                           pw.println("f "+tmp+" "+(tmp+1)+" "+(tmp+2));
                           pw.println("f "+(tmp+2)+" "+(tmp+3)+" "+tmp);
                          }
                  else
                      for(int i=0,tmp;i<facePrimitiveCount;i++)
                          {tmp=4*(i+faceIndexOffset)+1;
                           pw.println("f "+tmp+" "+(tmp+1)+" "+(tmp+2)+" "+(tmp+3));
                          }
                 }
            }
        return(facePrimitiveCount);
    }
    
    private static final String getMaterialName(final String[] materialNames,
        final int unshiftedFaceIndex,final int[] faceCounts){        
        int materialIndex=-1;
        for(int faceCountIndex=0,faceCount=0,nonNullMaterialCount=0;faceCountIndex<faceCounts.length;faceCountIndex++)
            {faceCount+=faceCounts[faceCountIndex];
             if(unshiftedFaceIndex<faceCount)
                 {for(int i=0;i<materialNames.length;i++)
                      if(materialNames[i]!=null)
                          if(nonNullMaterialCount==faceCountIndex)
                              {materialIndex=i;
                               break;
                              }
                          else
                              nonNullMaterialCount++;
                  if(materialIndex!=-1)
                      break;
                 }
            }
        return(materialNames[materialIndex]);
    }
    
    private static final void writeUseMaterialAndSmoothing(PrintWriter pw,final String materialName,
            final int smoothing){
        pw.println("usemtl "+materialName);
        //smoothing
        pw.println("s "+smoothing);
    }
    
    private static final class VertexData{
        
        private float[] vertexCoord;
        
        private VertexData(float[] vertexCoord){
            this.vertexCoord=new float[]{vertexCoord[2],vertexCoord[3],vertexCoord[4]};
        }
        
        public final boolean equals(Object o){
            boolean result;
            if(o==null||!(o instanceof VertexData))
                result=false;
            else
                {VertexData v=(VertexData)o;
                 result=vertexCoord[0]==v.vertexCoord[0]&&vertexCoord[1]==v.vertexCoord[1]&&vertexCoord[2]==v.vertexCoord[2];
                }
            return(result);
        }
        
        public final int hashCode(){
            return((int)vertexCoord[2]);
        }
    }
    
    private static final class TextureCoordData{
        
        private float[] textureCoord;
        
        private TextureCoordData(float[] vertexCoord){
            this.textureCoord=new float[]{vertexCoord[0],vertexCoord[1]};
        }
        
        public final boolean equals(Object o){
            boolean result;
            if(o==null||!(o instanceof TextureCoordData))
                result=false;
            else
                {TextureCoordData v=(TextureCoordData)o;
                 result=textureCoord[0]==v.textureCoord[0]&&textureCoord[1]==v.textureCoord[1];
                }
            return(result);
        }
        
        public final int hashCode(){
            return((int)textureCoord[0]);
        }
    }
    
    private static final class NetworkStructuralRedundancyAnalyzer extends Network.BreadthFirstSearchVisitor{

        
        private HashMap<Full3DCell,Map.Entry<LinkedHashMap<Integer,Integer>,LinkedHashMap<VertexData,Integer>>> cellularMapsTable;
        
        private int uniqueVerticesIndicesCount;
        
        private int duplicateVerticesIndicesCount;
        
        
        private NetworkStructuralRedundancyAnalyzer(Network network,HashMap<Full3DCell,Map.Entry<LinkedHashMap<Integer,Integer>,LinkedHashMap<VertexData,Integer>>> cellularMapsTable,int uniqueVerticesIndicesCount,int duplicateVerticesIndicesCount){
            this(network.getRootCell(),cellularMapsTable,uniqueVerticesIndicesCount,duplicateVerticesIndicesCount);
        }
        
        private NetworkStructuralRedundancyAnalyzer(Full3DCell firstVisitedCell,HashMap<Full3DCell,Map.Entry<LinkedHashMap<Integer,Integer>,LinkedHashMap<VertexData,Integer>>> cellularMapsTable,int uniqueVerticesIndicesCount,int duplicateVerticesIndicesCount){
            super(firstVisitedCell);
            this.cellularMapsTable=cellularMapsTable;
            this.uniqueVerticesIndicesCount=uniqueVerticesIndicesCount;
            this.duplicateVerticesIndicesCount=duplicateVerticesIndicesCount;
        }       
        

        @Override
        protected final boolean performTaskOnCurrentlyVisitedCell(){
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
            Integer knownUniqueVertexIndex;
            Full3DCell neighborCell;
            Full3DPortal portal;
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
                     for(int i=0;i<cell.getNeighboursCount()&&portalVertexIndex==-1;i++)
                         {portal=cell.getPortal(i);
                          for(float[] portalVertex:portal.getPortalVertices())
                              //remind: T2_V3 (2 texture coordinates + 3 vertex coordinates)
                              if(portalVertex[2]==wallVertex[2]&&portalVertex[3]==wallVertex[3]&&portalVertex[4]==wallVertex[4])
                                  {portalVertexIndex=currentPortalVertexIndex;
                                   break;
                                  }
                          currentPortalVertexIndex++;
                         }
                     //if the vertex is in a portal
                     if(portalVertexIndex!=-1)
                         {//get the cell that is linked to this portal      
                          neighborCell=cell.getNeighbourCell(portalVertexIndex);
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
                          //else the neighbor cell has not yet been visited 
                          else
                              uniqueVertexIndex=-1;
                         }
                     //else the vertex is not is a portal
                     else
                         uniqueVertexIndex=-1;                         
                     if(uniqueVertexIndex==-1)
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
            Full3DCell cell=getCurrentlyVisitedCell();
            ArrayList<List<float[]>> wallsVerticesListList=new ArrayList<List<float[]>>();
            wallsVerticesListList.add(cell.getBottomWalls());
            wallsVerticesListList.add(cell.getCeilWalls());
            wallsVerticesListList.add(cell.getFloorWalls());
            wallsVerticesListList.add(cell.getLeftWalls());
            wallsVerticesListList.add(cell.getRightWalls());
            wallsVerticesListList.add(cell.getTopWalls());
            Integer knownTextureCoordIndex;
            TextureCoordData currentTextureCoord;
            int uniqueIndex,duplicateIndex=duplicateToUniqueIndexationTable.size();
            for(List<float[]> wallsVerticesList:wallsVerticesListList)
                for(float[] wallVertex:wallsVerticesList)
                    {currentTextureCoord=new TextureCoordData(wallVertex);
                     //if the texture coordinates are already in the second table
                     if((knownTextureCoordIndex=textureCoordDataToUniqueIndexationTable.get(currentTextureCoord))!=null)
                         {//get the unique index
                          uniqueIndex=knownTextureCoordIndex.intValue();
                         }
                     //else
                     else
                         {//get the next unique index
                          uniqueIndex=textureCoordDataToUniqueIndexationTable.size();
                          //put it into the second table
                          textureCoordDataToUniqueIndexationTable.put(currentTextureCoord,Integer.valueOf(uniqueIndex));
                         }                  
                     //put a couple with the duplicate index and the unique index into the first table                     
                     duplicateToUniqueIndexationTable.put(Integer.valueOf(duplicateIndex),Integer.valueOf(uniqueIndex));
                     duplicateIndex++;
                    }    
            //go on visiting
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
        protected final boolean performTaskOnCurrentlyVisitedCell(){
            //As we use a LinkedHashMap, we can benefit of the insertion order
            for(VertexData vertexData:cellularMapsTable.get(getCurrentlyVisitedCell()).getValue().keySet())
                pw.println("v "+vertexData.vertexCoord[0]+" "+vertexData.vertexCoord[1]+" "+vertexData.vertexCoord[2]);
            //go on visiting the network
            return(true);
        }
        
    }
    
    private static final class NetworkTexturedFaceDataWriter extends Network.BreadthFirstSearchVisitor{


        private LinkedHashMap<Integer,Integer> duplicateToUniqueIndexationTable;
        
        private HashMap<Full3DCell,Map.Entry<LinkedHashMap<Integer,Integer>,LinkedHashMap<VertexData,Integer>>> cellularMapsTable;

        private PrintWriter pw;
        
        private boolean useTriangles;
        
        
        private NetworkTexturedFaceDataWriter(Network network,
                HashMap<Full3DCell,Map.Entry<LinkedHashMap<Integer,Integer>,LinkedHashMap<VertexData,Integer>>> cellularMapsTable,
                LinkedHashMap<Integer, Integer> duplicateToUniqueIndexationTable,
                PrintWriter pw,boolean useTriangles){
            super(network);
            this.cellularMapsTable=cellularMapsTable;
            this.duplicateToUniqueIndexationTable=duplicateToUniqueIndexationTable;
            this.pw=pw;
            this.useTriangles=useTriangles;
        }

        
        @Override
        protected final boolean performTaskOnCurrentlyVisitedCell(){     
            int[] uniqueVerticesIndices=new int[4];
            int[] uniqueTextureCoordIndices=new int[4];
            Iterator<Map.Entry<Integer,Integer>> verticesIndicesIterator=cellularMapsTable.get(getCurrentlyVisitedCell()).getKey().entrySet().iterator();
            Map.Entry<Integer,Integer> vertexIndexEntry;
            if(useTriangles)
                {//As we use a LinkedHashMap, we can benefit of the insertion order
                 while(verticesIndicesIterator.hasNext())
                     {for(int i=0;i<4;i++)
                          {vertexIndexEntry=verticesIndicesIterator.next();
                           //The first index is not 0 but 1 in WaveFront OBJ format
                           uniqueTextureCoordIndices[i]=duplicateToUniqueIndexationTable.get(vertexIndexEntry.getKey()).intValue()+1;
                           uniqueVerticesIndices[i]=vertexIndexEntry.getValue().intValue()+1;               
                          }
                      //write the face primitive
                      pw.println("f "+uniqueVerticesIndices[0]+"/"+uniqueTextureCoordIndices[0]+" "+
                                      uniqueVerticesIndices[1]+"/"+uniqueTextureCoordIndices[1]+" "+
                                      uniqueVerticesIndices[2]+"/"+uniqueTextureCoordIndices[2]);
                      pw.println("f "+uniqueVerticesIndices[2]+"/"+uniqueTextureCoordIndices[2]+" "+
                                      uniqueVerticesIndices[3]+"/"+uniqueTextureCoordIndices[3]+" "+
                                      uniqueVerticesIndices[0]+"/"+uniqueTextureCoordIndices[0]);
                     }
                }
            else
                {while(verticesIndicesIterator.hasNext())
                     {for(int i=0;i<4;i++)
                          {vertexIndexEntry=verticesIndicesIterator.next();
                           uniqueTextureCoordIndices[i]=duplicateToUniqueIndexationTable.get(vertexIndexEntry.getKey()).intValue()+1;
                           uniqueVerticesIndices[i]=vertexIndexEntry.getValue().intValue()+1;
                          }
                      pw.println("f "+uniqueVerticesIndices[0]+"/"+uniqueTextureCoordIndices[0]+" "+
                                      uniqueVerticesIndices[1]+"/"+uniqueTextureCoordIndices[1]+" "+
                                      uniqueVerticesIndices[2]+"/"+uniqueTextureCoordIndices[2]+" "+
                                      uniqueVerticesIndices[3]+"/"+uniqueTextureCoordIndices[3]);
                     }
                }
            //go on visiting the network
            return(true);
        }
    }
    
    private static final class NetworkUntexturedFaceDataWriter extends Network.BreadthFirstSearchVisitor{

        
        private HashMap<Full3DCell,Map.Entry<LinkedHashMap<Integer,Integer>,LinkedHashMap<VertexData,Integer>>> cellularMapsTable;
        
        private PrintWriter pw;
        
        private boolean useTriangles;
        
        
        private NetworkUntexturedFaceDataWriter(Network network,HashMap<Full3DCell,Map.Entry<LinkedHashMap<Integer,Integer>,LinkedHashMap<VertexData,Integer>>> cellularMapsTable,PrintWriter pw,boolean useTriangles){
            this(network.getRootCell(),cellularMapsTable,pw,useTriangles);
        }
        
        private NetworkUntexturedFaceDataWriter(Full3DCell firstVisitedCell,HashMap<Full3DCell,Map.Entry<LinkedHashMap<Integer,Integer>,LinkedHashMap<VertexData,Integer>>> cellularMapsTable,PrintWriter pw,boolean useTriangles){
            super(firstVisitedCell);
            this.cellularMapsTable=cellularMapsTable;
            this.pw=pw;
            this.useTriangles=useTriangles;
        }       
        

        @Override
        protected final boolean performTaskOnCurrentlyVisitedCell(){
            int[] uniqueVerticesIndices=new int[4];
            Iterator<Integer> uniqueVerticesIndicesIterator=cellularMapsTable.get(getCurrentlyVisitedCell()).getKey().values().iterator();
            if(useTriangles)
                {//As we use a LinkedHashMap, we can benefit of the insertion order
                 while(uniqueVerticesIndicesIterator.hasNext())
                     {for(int i=0;i<4;i++)
                          //The first index is not 0 but 1 in WaveFront OBJ format
                          uniqueVerticesIndices[i]=uniqueVerticesIndicesIterator.next().intValue()+1;               
                      //write the face primitive
                      pw.println("f "+uniqueVerticesIndices[0]+" "+
                                      uniqueVerticesIndices[1]+" "+
                                      uniqueVerticesIndices[2]);
                      pw.println("f "+uniqueVerticesIndices[2]+" "+
                                      uniqueVerticesIndices[3]+" "+
                                      uniqueVerticesIndices[0]);
                     }
                }
            else
                {while(uniqueVerticesIndicesIterator.hasNext())
                     {for(int i=0;i<4;i++)
                          uniqueVerticesIndices[i]=uniqueVerticesIndicesIterator.next().intValue()+1;               
                      pw.println("f "+uniqueVerticesIndices[0]+" "+
                                      uniqueVerticesIndices[1]+" "+
                                      uniqueVerticesIndices[2]+" "+
                                      uniqueVerticesIndices[3]);
                     }
                }
            //go on visiting the network
            return(true);
        }
        
    }
}
