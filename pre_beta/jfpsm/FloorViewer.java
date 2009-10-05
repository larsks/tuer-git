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
package jfpsm;

import java.awt.GridLayout;
import java.awt.image.BufferedImage;
import javax.swing.JPanel;
import javax.swing.JSplitPane;

final class FloorViewer extends JPanel{

    
    private static final long serialVersionUID = 1L;
    
    private DrawingPanel containerDrawingPanel;
    
    private DrawingPanel contentDrawingPanel;
    
    private DrawingPanel lightDrawingPanel;
    
    private DrawingPanel pathDrawingPanel;
    
    private ZoomParameters zoomParams;
    

    FloorViewer(Floor floor){
        super(new GridLayout(1,1));
        zoomParams=new ZoomParameters(1,floor.getContainerMap().getWidth(),floor.getContainerMap().getHeight());
        containerDrawingPanel=new DrawingPanel(floor,"container map",floor.getContainerMap());
        contentDrawingPanel=new DrawingPanel(floor,"content map",floor.getContentMap());
        JSplitPane leftVerticalSplitPane=new JSplitPane(JSplitPane.VERTICAL_SPLIT,true,containerDrawingPanel,contentDrawingPanel);
        leftVerticalSplitPane.setOneTouchExpandable(true);
        lightDrawingPanel=new DrawingPanel(floor,"light map",floor.getLightMap());
        pathDrawingPanel=new DrawingPanel(floor,"path map",new BufferedImage(256,256,BufferedImage.TYPE_INT_ARGB));
        JSplitPane rightVerticalSplitPane=new JSplitPane(JSplitPane.VERTICAL_SPLIT,true,lightDrawingPanel,pathDrawingPanel);
        rightVerticalSplitPane.setOneTouchExpandable(true);
        JSplitPane horizontalSplitPane=new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,true,leftVerticalSplitPane,rightVerticalSplitPane);
        add(horizontalSplitPane);
        containerDrawingPanel.addMouseWheelListener(new ZoomMouseWheelListener(this));
        contentDrawingPanel.addMouseWheelListener(new ZoomMouseWheelListener(this));
        lightDrawingPanel.addMouseWheelListener(new ZoomMouseWheelListener(this));
        pathDrawingPanel.addMouseWheelListener(new ZoomMouseWheelListener(this));
        containerDrawingPanel.setZoomParameters(zoomParams);
        contentDrawingPanel.setZoomParameters(zoomParams);
        lightDrawingPanel.setZoomParameters(zoomParams);
        pathDrawingPanel.setZoomParameters(zoomParams);      
    }
    
    final void updateZoom(int factorIncrement,int x,int y){
        int previousFactor=zoomParams.getFactor();
        if(factorIncrement>=1)
            zoomParams.setFactor(Math.min(zoomParams.getFactor()+1,32));
        else
            if(factorIncrement<=-1)
                zoomParams.setFactor(Math.max(zoomParams.getFactor()-1,1));
        if(previousFactor!=zoomParams.getFactor())
            {//convert it into the correct base
             zoomParams.setCenterx(zoomParams.getAbsoluteXFromRelativeX(x));
             zoomParams.setCentery(zoomParams.getAbsoluteYFromRelativeY(y));
             containerDrawingPanel.repaint();
             contentDrawingPanel.repaint();
             lightDrawingPanel.repaint();
             pathDrawingPanel.repaint();
            }
    }
}
