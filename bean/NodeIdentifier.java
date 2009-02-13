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
package bean;

import java.io.ObjectStreamException;
import java.io.Serializable;

public final class NodeIdentifier implements Serializable{

    
    private static final long serialVersionUID=1L;
    
    private static final String levelIDPrefix="level";
    
    private static final String networkIDPrefix="NID";
    
    private static final String cellIDPrefix="CID";
    
    public static final int unknownID=-1;
    
    private int levelID;
    
    private int networkID;
    
    private int cellID;

    private int secondaryCellID;
    
    private transient String name;
    
    
    public NodeIdentifier(){
        this(unknownID,unknownID,unknownID,unknownID);
    }
    
    public NodeIdentifier(int levelID,int networkID,int cellID,int secondaryCellID){
        this.levelID=levelID;
        this.networkID=networkID;
        this.cellID=cellID;
        this.secondaryCellID=secondaryCellID;
    }

    
    @Override
    public boolean equals(Object o){
        boolean result;
        if(o==null || ! ( o instanceof NodeIdentifier ) )
            result=false;
        else
            {NodeIdentifier ni=(NodeIdentifier)o;
             result=levelID==ni.levelID&&networkID==ni.networkID&&cellID==ni.cellID&&secondaryCellID==ni.secondaryCellID;
            }
        return(result);
    }
    
    @Override
    public int hashCode(){
        return(name.hashCode());
    }
    
    public final int getLevelID(){
        return(levelID);
    }

    public final void setLevelID(int levelID){
        this.levelID=levelID;
        updateName();
    }

    public final int getNetworkID(){
        return(networkID);
    }

    public final void setNetworkID(int networkID){
        this.networkID=networkID;
        updateName();
    }

    public final int getCellID(){
        return(cellID);
    }

    public final void setCellID(int cellID){
        this.cellID=cellID;
        updateName();
    }

    public final int getSecondaryCellID(){
        return(secondaryCellID);
    }

    public final void setSecondaryCellID(int secondaryCellID){
        this.secondaryCellID=secondaryCellID;
        updateName();
    }
    
    @Override
    public final String toString(){
        return(name);
    }
    
    private final void updateName(){
        StringBuilder identifierBuilder=new StringBuilder();
        if(levelID!=unknownID)
            {identifierBuilder.append(levelIDPrefix);
             identifierBuilder.append(levelID);
            }
        if(networkID!=unknownID)
            {identifierBuilder.append(networkIDPrefix);
             identifierBuilder.append(networkID);
            }
        if(cellID!=unknownID)
            {identifierBuilder.append(cellIDPrefix);
             identifierBuilder.append(cellID);
            }
        if(secondaryCellID!=unknownID)
            {identifierBuilder.append(cellIDPrefix);
             identifierBuilder.append(secondaryCellID);
            }
        this.name=identifierBuilder.toString();
    }
    
    public Object readResolve() throws ObjectStreamException{
        updateName();
        return(this);
    }
    
    public static final NodeIdentifier getInstance(String name){
        int indexOfLevelIDTag=name.indexOf(levelIDPrefix);
        int indexOfNetworkIDTag=name.indexOf(networkIDPrefix);
        int indexOfFirstCellIDTag=name.indexOf(cellIDPrefix);
        int indexOflastCellIDTag=indexOfFirstCellIDTag!=-1?name.indexOf(cellIDPrefix,indexOfFirstCellIDTag+cellIDPrefix.length()):-1;
        int levelIndex,networkIndex,firstCellIndex,lastCellIndex;
        if(indexOfLevelIDTag==-1)
            {levelIndex=unknownID;
             if(indexOfNetworkIDTag==-1)
                 {networkIndex=unknownID;
                  if(indexOfFirstCellIDTag==-1)
                      {firstCellIndex=unknownID;
                       if(indexOflastCellIDTag==-1)
                           lastCellIndex=unknownID;
                       else
                           lastCellIndex=Integer.parseInt(name.substring(indexOflastCellIDTag+cellIDPrefix.length()));
                      }
                  else
                      {if(indexOflastCellIDTag==-1)
                           {firstCellIndex=Integer.parseInt(name.substring(indexOfFirstCellIDTag+cellIDPrefix.length()));
                            lastCellIndex=unknownID;
                           }
                       else
                           {firstCellIndex=Integer.parseInt(name.substring(indexOfFirstCellIDTag+cellIDPrefix.length(),indexOflastCellIDTag));
                            lastCellIndex=Integer.parseInt(name.substring(indexOflastCellIDTag+cellIDPrefix.length()));
                           }
                      }
                 }
             else
                 {if(indexOfFirstCellIDTag==-1)
                      {firstCellIndex=unknownID;
                       if(indexOflastCellIDTag==-1)
                           {networkIndex=Integer.parseInt(name.substring(indexOfNetworkIDTag+networkIDPrefix.length()));
                            lastCellIndex=unknownID;
                           }
                       else
                           {networkIndex=Integer.parseInt(name.substring(indexOfNetworkIDTag+networkIDPrefix.length(),indexOflastCellIDTag));
                            lastCellIndex=Integer.parseInt(name.substring(indexOflastCellIDTag+cellIDPrefix.length()));
                           }
                      }
                  else
                      {networkIndex=Integer.parseInt(name.substring(indexOfNetworkIDTag+networkIDPrefix.length(),indexOfFirstCellIDTag));
                       if(indexOflastCellIDTag==-1)
                           {firstCellIndex=Integer.parseInt(name.substring(indexOfFirstCellIDTag+cellIDPrefix.length()));
                            lastCellIndex=unknownID;
                           }
                       else
                           {firstCellIndex=Integer.parseInt(name.substring(indexOfFirstCellIDTag+cellIDPrefix.length(),indexOflastCellIDTag));
                            lastCellIndex=Integer.parseInt(name.substring(indexOflastCellIDTag+cellIDPrefix.length()));
                           }
                      }
                 }
            }
        else
            {if(indexOfNetworkIDTag==-1)
                 {networkIndex=unknownID;
                  if(indexOfFirstCellIDTag==-1)
                      {firstCellIndex=unknownID;
                       if(indexOflastCellIDTag==-1)
                           {levelIndex=Integer.parseInt(name.substring(indexOfLevelIDTag+levelIDPrefix.length()));
                            lastCellIndex=unknownID;
                           }
                       else
                           {levelIndex=Integer.parseInt(name.substring(indexOfLevelIDTag+levelIDPrefix.length(),indexOflastCellIDTag));
                            lastCellIndex=Integer.parseInt(name.substring(indexOflastCellIDTag+cellIDPrefix.length()));
                           }
                      }
                  else
                      {levelIndex=Integer.parseInt(name.substring(indexOfLevelIDTag+levelIDPrefix.length(),indexOfFirstCellIDTag));
                       if(indexOflastCellIDTag==-1)
                           {firstCellIndex=Integer.parseInt(name.substring(indexOfFirstCellIDTag+cellIDPrefix.length()));
                            lastCellIndex=unknownID;
                           }
                       else
                           {firstCellIndex=Integer.parseInt(name.substring(indexOfFirstCellIDTag+cellIDPrefix.length(),indexOflastCellIDTag));
                            lastCellIndex=Integer.parseInt(name.substring(indexOflastCellIDTag+cellIDPrefix.length()));
                           }
                      }
                 }
             else
                 {levelIndex=Integer.parseInt(name.substring(indexOfLevelIDTag+levelIDPrefix.length(),indexOfNetworkIDTag));
                  if(indexOfFirstCellIDTag==-1)
                      {firstCellIndex=unknownID;
                       if(indexOflastCellIDTag==-1)
                           {networkIndex=Integer.parseInt(name.substring(indexOfNetworkIDTag+networkIDPrefix.length()));
                            lastCellIndex=unknownID;
                           }
                       else
                           {networkIndex=Integer.parseInt(name.substring(indexOfNetworkIDTag+networkIDPrefix.length(),indexOflastCellIDTag));
                            lastCellIndex=Integer.parseInt(name.substring(indexOflastCellIDTag+cellIDPrefix.length()));
                           }
                      }
                  else
                      {networkIndex=Integer.parseInt(name.substring(indexOfNetworkIDTag+networkIDPrefix.length(),indexOfFirstCellIDTag));
                       if(indexOflastCellIDTag==-1)
                           {firstCellIndex=Integer.parseInt(name.substring(indexOfFirstCellIDTag+cellIDPrefix.length()));
                            lastCellIndex=unknownID;
                           }
                       else
                           {firstCellIndex=Integer.parseInt(name.substring(indexOfFirstCellIDTag+cellIDPrefix.length(),indexOflastCellIDTag));
                            lastCellIndex=Integer.parseInt(name.substring(indexOflastCellIDTag+cellIDPrefix.length()));
                           }
                      }
                     
                 }
            }
        return(new NodeIdentifier(levelIndex,networkIndex,firstCellIndex,lastCellIndex));
    }
}
