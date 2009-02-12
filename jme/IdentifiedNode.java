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

import com.jme.scene.Node;

/**
 * node identified by 4 identifiers for the level, the network, 
 * the first cell and optionally the second cell
 * @author Julien Gouesse
 *
 */
abstract class IdentifiedNode extends Node{
    
    
    private static final long serialVersionUID=1L;
    
    //TODO: create a static class that handles the node identifier
    //TODO: use an immutable instance as an attribute of an identified node
    private static final String levelIDPrefix="level";
    
    private static final String networkIDPrefix="NID";
    
    private static final String cellIDPrefix="CID";
    
    protected static final int unknownID=-1;
    
    protected int levelID;
    
    protected int networkID;
    
    protected int cellID;

    protected int secondaryCellID;
    
    
    IdentifiedNode(){
        this(unknownID,unknownID,unknownID,unknownID);
    }
    
    IdentifiedNode(int levelID){
        this(levelID,unknownID,unknownID,unknownID);
    }
    
    IdentifiedNode(int levelID,int networkID){
        this(levelID,networkID,unknownID,unknownID);
    }
    
    IdentifiedNode(int levelID,int networkID,int cellID){
        this(levelID,networkID,cellID,unknownID);
    }
    
    IdentifiedNode(int levelID,int networkID,int cellID,int secondaryCellID){
        super(createNodeIdentifier(levelID,networkID,cellID,secondaryCellID));
        this.levelID=levelID;
        this.networkID=networkID;
        this.cellID=cellID;
    }
    
    /*private static final String createNodeIdentifier(int levelID,int networkID,int cellID){
        return(createNodeIdentifier(levelID,networkID,cellID,unknownID));
    }*/
    
    boolean isIdentifiedBy(int levelID,int networkID){
        return(isIdentifiedBy(levelID,networkID,unknownID,unknownID));
    }
    
    boolean isIdentifiedBy(int levelID,int networkID,int cellID,int secondaryCellID){
        return(this.levelID==levelID&&this.networkID==networkID&&this.cellID==cellID&&this.secondaryCellID==secondaryCellID);
    }
    
    @Override
    public boolean equals(Object o){
        boolean result;
        if(o==null || ! ( o instanceof IdentifiedNode ) )
            result=false;
        else
            {IdentifiedNode in=(IdentifiedNode)o;
             result=isIdentifiedBy(in.levelID,in.networkID,in.cellID,in.secondaryCellID);
            }
        return(result);
    }
    
    @Override
    public int hashCode(){
        return(cellID);
    }
    
    private static final String createNodeIdentifier(int levelID,int networkID,int cellID,int secondaryCellID){
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
        return(identifierBuilder.toString());
    }
    
    /**
     * is it really useful???
     * @return
     */
    String getNodeIdentifier(){
        return(name);
    }
}
