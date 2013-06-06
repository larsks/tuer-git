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
package engine.statemachine;

import com.ardor3d.framework.NativeCanvas;
import com.ardor3d.input.GrabbedState;
import com.ardor3d.input.MouseManager;
import com.ardor3d.input.PhysicalLayer;
import com.ardor3d.ui.text.BMText;
import engine.misc.FontStore;
import engine.sound.SoundManager;

public final class ContentRatingSystemState extends ScenegraphState{
	
	
	private final String text="Adults Only (+18)\n\nViolence\n\nBad Language\n\nFear\n\nSex\n\nDrugs\n\nDiscrimination";

	private MouseManager mouseManager;
	
    
	public ContentRatingSystemState(final NativeCanvas canvas,final PhysicalLayer physicalLayer,final MouseManager mouseManager,final SoundManager soundManager,
			final FontStore fontStore){
        super(soundManager);
        this.mouseManager=mouseManager;
        final BMText textNode=new BMText("contentSystemRatingNode",text,fontStore.getFontsList().get(0),BMText.Align.Center,BMText.Justify.Center);
        getRoot().attachChild(textNode);
    }
    
    @Override
    public final void init(){
        //do nothing here because this method will be called
        //after the display of this state
    }
    
    @Override
    public void setEnabled(final boolean enabled){
        final boolean wasEnabled=isEnabled();
        if(wasEnabled!=enabled)
            {super.setEnabled(enabled);
             if(enabled)
                 mouseManager.setGrabbed(GrabbedState.GRABBED);
            }
    }
}
