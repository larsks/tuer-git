/**
 * Copyright (c) 2006-2015 Julien Gouesse
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package jfpsm;

import java.awt.Cursor;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.SwingUtilities;

class DrawingMouseAdapter extends MouseAdapter{

    
    private final ZoomParameters zoomParams;
    
    private final DrawingPanel drawingPanel;
    
    private int previousLeftClickX;
    
    private int previousLeftClickY;
    
    private int previousRightClickX;
    
    private int previousRightClickY;
    
    
    DrawingMouseAdapter(final DrawingPanel drawingPanel){
        zoomParams=drawingPanel.getZoomParameters();
        this.drawingPanel=drawingPanel;
    }
    
    
    @Override
    public final void mousePressed(MouseEvent e){
        if(SwingUtilities.isLeftMouseButton(e))
            {previousLeftClickX=e.getX();
             previousLeftClickY=e.getY();
             if(zoomParams!=null)
                 {previousLeftClickX=zoomParams.getAbsoluteXFromRelativeX(previousLeftClickX);
                  previousLeftClickY=zoomParams.getAbsoluteYFromRelativeY(previousLeftClickY);
                 }
            }
        if(SwingUtilities.isRightMouseButton(e))
            {previousRightClickX=e.getX();
             previousRightClickY=e.getY();
             if(zoomParams!=null)
                 {previousRightClickX=zoomParams.getAbsoluteXFromRelativeX(previousRightClickX);
                  previousRightClickY=zoomParams.getAbsoluteYFromRelativeY(previousRightClickY);
                 }
             drawingPanel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            }
    }
    
    @Override
    public final void mouseReleased(MouseEvent e){
        if(SwingUtilities.isRightMouseButton(e))
            drawingPanel.setCursor(Cursor.getDefaultCursor());
    }
    
    @Override
    public final void mouseDragged(MouseEvent e){
        if(SwingUtilities.isLeftMouseButton(e))
            {int x=e.getX(),y=e.getY();
             if(zoomParams!=null)
                 {x=zoomParams.getAbsoluteXFromRelativeX(x);
                  y=zoomParams.getAbsoluteYFromRelativeY(y);                  
                 }
             drawingPanel.draw(previousLeftClickX,previousLeftClickY,x,y);
             previousLeftClickX=x;
             previousLeftClickY=y;
            }
        if(SwingUtilities.isRightMouseButton(e))
            {int x=e.getX(),y=e.getY();
             if(zoomParams!=null)
                 {x=zoomParams.getAbsoluteXFromRelativeX(x);
                  y=zoomParams.getAbsoluteYFromRelativeY(y);                  
                 }
             drawingPanel.move(previousRightClickX,previousRightClickY,x,y);
             previousRightClickX=x;
             previousRightClickY=y;
            }
    }
    
    @Override
    public final void mouseClicked(MouseEvent e){
        if(SwingUtilities.isLeftMouseButton(e))
            {int x=e.getX(),y=e.getY();
             if(zoomParams!=null)
                 {x=zoomParams.getAbsoluteXFromRelativeX(x);
                  y=zoomParams.getAbsoluteYFromRelativeY(y);                  
                 }
             drawingPanel.draw(x,y,x,y);
            }
    }
}
