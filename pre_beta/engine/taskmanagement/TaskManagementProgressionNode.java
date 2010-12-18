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
package engine.taskmanagement;

import com.ardor3d.extension.ui.UIFrame;
import com.ardor3d.extension.ui.UIHud;
import com.ardor3d.extension.ui.UILabel;
import com.ardor3d.extension.ui.UIPanel;
import com.ardor3d.extension.ui.UIProgressBar;
import com.ardor3d.extension.ui.layout.RowLayout;
import com.ardor3d.renderer.Camera;
import com.ardor3d.scenegraph.Node;
import com.ardor3d.scenegraph.Spatial;
import com.ardor3d.scenegraph.controller.SpatialController;

public final class TaskManagementProgressionNode extends Node{

    
    private final UIProgressBar bar;
    
    private int maxTaskCount;
    
    private final TaskManager taskManager;
    
    
    public TaskManagementProgressionNode(final Camera cam,final TaskManager taskManager){
        super("task progression node");
        this.taskManager=taskManager;
        maxTaskCount=0;
        bar=new UIProgressBar("",true);
        bar.setPercentFilled(0);
        //bar.setComponentWidth(250);
        final UIPanel panel=new UIPanel(new RowLayout(false));
        panel.add(new UILabel("Loading... Please wait"));
        panel.add(bar);
        final UIFrame frame=new UIFrame("");
        frame.setDecorated(false);
        frame.setContentPanel(panel);
        frame.updateMinimumSizeFromContents();
        frame.layout();
        frame.pack();
        frame.setUseStandin(false);
        frame.setOpacity(1f);
        frame.setName("task progression frame");
        frame.setLocationRelativeTo(cam);
        final UIHud hud=new UIHud();
        hud.add(frame);
        attachChild(hud);
        addController(new SpatialController<Spatial>(){
            @Override
            public final void update(final double time,final Spatial caller){
                final int taskCount=taskManager.getTaskCount();
                if(maxTaskCount==0)
                    bar.setPercentFilled(0);
                else
                    bar.setPercentFilled(1-((double)taskCount)/maxTaskCount);
            }
        });
    }
    
    public final void reset(){
        maxTaskCount=taskManager.getTaskCount();
    }
}
