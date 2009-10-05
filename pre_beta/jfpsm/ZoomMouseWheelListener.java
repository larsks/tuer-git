/**
 * 
 */
package jfpsm;

import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;

final class ZoomMouseWheelListener implements MouseWheelListener{

    
    private final FloorViewer floorViewer;
    
    
    ZoomMouseWheelListener(FloorViewer floorViewer){
        this.floorViewer=floorViewer;
    }
    
    @Override
    public final void mouseWheelMoved(MouseWheelEvent e){
        floorViewer.updateZoom(e.getWheelRotation()*-1,e.getX(),e.getY());
    }
}