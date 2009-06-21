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

import java.io.IOException;
import java.util.HashMap;
import com.jme.scene.Node;
import com.jme.util.CloneImportExport;
import com.jme.util.export.binary.BinaryImporter;

final class NodeFactory{
    
    
    private HashMap<String,CloneImportExport> clonerTable;

    private static final NodeFactory instance=new NodeFactory();
    
    
    private NodeFactory(){
        clonerTable=new HashMap<String, CloneImportExport>();
    }
   
    
    static final NodeFactory getInstance(){
        return(instance);
    }   
    
    final Node getNode(String path){
        CloneImportExport cloner=clonerTable.get(path);
        if(cloner==null)
            {cloner=new CloneImportExport();
             Node node=null;
             try{node=(Node)BinaryImporter.getInstance().load(NodeFactory.class.getResource(path));} 
             catch(IOException ioe)
             {ioe.printStackTrace();}
             if(node!=null)
                 {cloner.saveClone(node);
                  clonerTable.put(path,cloner);
                 }
            }
        return(cloner==null?null:(Node)cloner.loadClone());
    }
}
