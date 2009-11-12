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
package engine;

import com.ardor3d.extension.model.collada.ColladaImporter;
import com.ardor3d.framework.jogl.JoglCanvas;
import com.ardor3d.input.Key;
import com.ardor3d.input.PhysicalLayer;
import com.ardor3d.input.control.FirstPersonControl;
import com.ardor3d.input.logical.InputTrigger;
import com.ardor3d.input.logical.KeyPressedCondition;
import com.ardor3d.input.logical.TriggerAction;
import com.ardor3d.math.Vector3;
import com.ardor3d.scenegraph.Node;

final class GameState extends State{
    
    
    private int levelIndex;

    
    GameState(final JoglCanvas canvas,final PhysicalLayer physicalLayer,final TriggerAction exitAction){
        super();
        final Vector3 worldUp=new Vector3(0,1,0);              
        // drag only at false to remove the need of pressing a button to move
        FirstPersonControl.setupTriggers(getLogicalLayer(),worldUp,false);
        final InputTrigger exitTrigger=new InputTrigger(new KeyPressedCondition(Key.ESCAPE),exitAction);
        final InputTrigger[] triggers=new InputTrigger[]{exitTrigger};
        getLogicalLayer().registerInput(canvas,physicalLayer);
        for(InputTrigger trigger:triggers)
            getLogicalLayer().registerTrigger(trigger);
    }
    
    
    final void setLevelIndex(final int levelIndex){
        this.levelIndex=levelIndex;
    }
    
    @Override
    public final void init(){
        // Remove all previously attached children
        getRoot().detachAllChildren();
        // Load collada model
        try {final Node colladaNode=ColladaImporter.readColladaScene("LID"+levelIndex+".dae");
             getRoot().attachChild(colladaNode); 
            }
        catch(final Exception ex)
        {ex.printStackTrace();} 
    }
}
