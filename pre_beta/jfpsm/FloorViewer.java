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
import javax.swing.JSplitPane;

final class FloorViewer extends Viewer{

    
    private static final long serialVersionUID = 1L;
    
    private final DrawingPanel containerDrawingPanel;
    
    private final DrawingPanel contentDrawingPanel;
    
    private final DrawingPanel lightDrawingPanel;
    
    private final DrawingPanel pathDrawingPanel;
    
    private final ZoomParameters zoomParams;
    
    
    FloorViewer(final Floor floor,final Project project,final ProjectManager projectManager){
        super(floor,project,projectManager);
        setLayout(new GridLayout(1,1));
        zoomParams=new ZoomParameters(1,floor.getContainerMap().getWidth(),floor.getContainerMap().getHeight());
        containerDrawingPanel=new DrawingPanel("container map",floor.getContainerMap(),zoomParams,this);
        contentDrawingPanel=new DrawingPanel("content map",floor.getContentMap(),zoomParams,this);
        JSplitPane leftVerticalSplitPane=new JSplitPane(JSplitPane.VERTICAL_SPLIT,true,containerDrawingPanel,contentDrawingPanel);
        leftVerticalSplitPane.setOneTouchExpandable(true);
        lightDrawingPanel=new DrawingPanel("light map",floor.getLightMap(),zoomParams,this);
        pathDrawingPanel=new DrawingPanel("path map",new BufferedImage(256,256,BufferedImage.TYPE_INT_ARGB),zoomParams,this);
        JSplitPane rightVerticalSplitPane=new JSplitPane(JSplitPane.VERTICAL_SPLIT,true,lightDrawingPanel,pathDrawingPanel);
        rightVerticalSplitPane.setOneTouchExpandable(true);
        JSplitPane horizontalSplitPane=new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,true,leftVerticalSplitPane,rightVerticalSplitPane);
        add(horizontalSplitPane);
        containerDrawingPanel.addMouseWheelListener(new ZoomMouseWheelListener(this));
        contentDrawingPanel.addMouseWheelListener(new ZoomMouseWheelListener(this));
        lightDrawingPanel.addMouseWheelListener(new ZoomMouseWheelListener(this));
        pathDrawingPanel.addMouseWheelListener(new ZoomMouseWheelListener(this));   
    }
    
    //FIXME: something is wrong
    final void updateZoom(int factorIncrement,int x,int y){
        int previousFactor=zoomParams.getFactor(),nextFactor;
        if(factorIncrement>=1)
            nextFactor=Math.min(zoomParams.getFactor()*2,32);
        else
            if(factorIncrement<=-1)
                nextFactor=Math.max(zoomParams.getFactor()/2,1);
            else
                nextFactor=previousFactor;
        if(previousFactor!=nextFactor)
            {//the conversion has to be done before updating the factor
             int nextX=zoomParams.getAbsoluteXFromRelativeX(x);
             int nextY=zoomParams.getAbsoluteYFromRelativeY(y);
             zoomParams.setFactor(nextFactor);
             zoomParams.setCenterx(nextX);
             zoomParams.setCentery(nextY);
             containerDrawingPanel.repaint();
             contentDrawingPanel.repaint();
             lightDrawingPanel.repaint();
             pathDrawingPanel.repaint();
            }
    }
}
