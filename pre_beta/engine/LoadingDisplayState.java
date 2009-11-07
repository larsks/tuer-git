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

import com.ardor3d.framework.jogl.JoglCanvas;
import com.ardor3d.input.Key;
import com.ardor3d.input.PhysicalLayer;
import com.ardor3d.input.logical.InputTrigger;
import com.ardor3d.input.logical.KeyPressedCondition;
import com.ardor3d.input.logical.TriggerAction;
import com.ardor3d.ui.text.BMText;

final class LoadingDisplayState extends State {

    
    LoadingDisplayState(final JoglCanvas canvas,final PhysicalLayer physicalLayer,final TriggerAction exitAction){
        super();
        final BMText textNode=new BMText("loadingGameNode","Feature not yet implemented!",Ardor3DGameServiceProvider.getFontsList().get(0),BMText.Align.Center,BMText.Justify.Center);
        getRoot().attachChild(textNode);
        final InputTrigger exitTrigger=new InputTrigger(new KeyPressedCondition(Key.ESCAPE),exitAction);
        final InputTrigger[] triggers=new InputTrigger[]{exitTrigger};
        getLogicalLayer().registerInput(canvas,physicalLayer);
        for(InputTrigger trigger:triggers)
            getLogicalLayer().registerTrigger(trigger);
    }
}
