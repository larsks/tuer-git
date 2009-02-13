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

import bean.NodeIdentifier;
import com.jme.scene.Node;

/**
 * node identified by 4 identifiers for the level, the network, 
 * the first cell and optionally the second cell
 * @author Julien Gouesse
 *
 */
abstract class IdentifiedNode extends Node{
    
    
    private static final long serialVersionUID=1L;
    
    private NodeIdentifier nodeIdentifier;
    
    
    IdentifiedNode(){
        this(NodeIdentifier.unknownID,NodeIdentifier.unknownID,NodeIdentifier.unknownID,NodeIdentifier.unknownID);
    }
    
    IdentifiedNode(int levelID){
        this(levelID,NodeIdentifier.unknownID,NodeIdentifier.unknownID,NodeIdentifier.unknownID);
    }
    
    IdentifiedNode(int levelID,int networkID){
        this(levelID,networkID,NodeIdentifier.unknownID,NodeIdentifier.unknownID);
    }
    
    IdentifiedNode(int levelID,int networkID,int cellID){
        this(levelID,networkID,cellID,NodeIdentifier.unknownID);
    }
    
    IdentifiedNode(int levelID,int networkID,int cellID,int secondaryCellID){
        this.nodeIdentifier=new NodeIdentifier(levelID,networkID,cellID,secondaryCellID);
        this.name=this.nodeIdentifier.toString();
    }
    
    boolean isIdentifiedBy(int levelID,int networkID){
        return(isIdentifiedBy(levelID,networkID,NodeIdentifier.unknownID,NodeIdentifier.unknownID));
    }
    
    boolean isIdentifiedBy(int levelID,int networkID,int cellID){
        return(isIdentifiedBy(levelID,networkID,cellID,NodeIdentifier.unknownID));
    }
    
    boolean isIdentifiedBy(int levelID,int networkID,int cellID,int secondaryCellID){
        return(getLevelID()==levelID&&
               nodeIdentifier.getNetworkID()==networkID&&
               nodeIdentifier.getCellID()==cellID&&
               nodeIdentifier.getSecondaryCellID()==secondaryCellID);
    }
    
    @Override
    public boolean equals(Object o){
        boolean result;
        if(o==null || ! ( o instanceof IdentifiedNode ) )
            result=false;
        else
            {IdentifiedNode in=(IdentifiedNode)o;
             result=this.nodeIdentifier.equals(in.nodeIdentifier);
            }
        return(result);
    }
    
    final int getLevelID(){
        return(nodeIdentifier.getLevelID());
    }
    
    final int getNetworkID(){
        return(nodeIdentifier.getNetworkID());
    }
    
    final int getCellID(){
        return(nodeIdentifier.getCellID());
    }
    
    final int getSecondaryCellID(){
        return(nodeIdentifier.getSecondaryCellID());
    }
    
    @Override
    public int hashCode(){
        return(name.hashCode());
    }
    
    public final String toString(){
        return(name);
    }
}
