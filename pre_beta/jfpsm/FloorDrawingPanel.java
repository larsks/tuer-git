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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

final class FloorDrawingPanel extends DrawingPanel {


    private static final long serialVersionUID=1L;
    
    private final JPopupMenu popupMenu;
    
    private final Map map;

    
	FloorDrawingPanel(final Floor floor,final MapType type,final ZoomParameters zoomParams,final FloorViewer floorViewer){
		super(type.getLabel(),floor.getMap(type).getImage(),zoomParams,floorViewer);
		map=floor.getMap(type);
		popupMenu=new JPopupMenu();
		final JMenuItem loadMapMenuItem=new JMenuItem("Load");
		loadMapMenuItem.addActionListener(new ActionListener(){       
            @Override
            public void actionPerformed(ActionEvent e){
                floorViewer.openFileAndLoadMap(type);
            }
        });
		popupMenu.add(loadMapMenuItem);
	}
	
	
	protected final JPopupMenu getPopupMenu(){
        return(popupMenu);
    }
	
	protected boolean draw(int x1,int y1,int x2,int y2){
	    final boolean success;
	    if(success=super.draw(x1,y1,x2,y2))
	        map.markDirty();
	    return(success);
	}
	
	final Map getMap(){
	    return(map);
	}
}
