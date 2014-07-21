/**
 * Copyright (c) 2006-2014 Julien Gouesse
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
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;

/**
 * image viewer supporting the scroll (with the scroll bars), the drag (on right click) and the zoom (work in progress)
 * 
 * @author Julien Gouesse
 *
 */
public class ImageViewer extends JScrollPane{

	private static final long serialVersionUID=1L;
	
	private boolean zoomEnabled;
	
	private transient Point lastDragPoint;
	
	public ImageViewer(final BufferedImage image){
		super();
		setViewportView(new DummyImagePanel(image));
		final MouseAdapter mouseAdapter=new MouseAdapter(){
			
			@Override
			public void mouseClicked(MouseEvent me){
				ImageViewer.this.mouseClicked(me);
			}
			
			@Override
			public void mousePressed(MouseEvent me){
				ImageViewer.this.mousePressed(me);
			}
			
			@Override
		    public void mouseDragged(MouseEvent me){
				ImageViewer.this.mouseDragged(me);
			}
			
			@Override
			public void mouseReleased(MouseEvent me){
				ImageViewer.this.mouseReleased(me);
			}
		};
		addMouseListener(mouseAdapter);
		addMouseMotionListener(mouseAdapter);
	}
	
	public void setZoomEnabled(final boolean zoomEnabled){
		if(this.zoomEnabled!=zoomEnabled)
		    {this.zoomEnabled=zoomEnabled;
			 if(zoomEnabled)
				 setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
			 else
				 setCursor(Cursor.getDefaultCursor());
		    }
	}
	
	private void mouseClicked(MouseEvent me){
		if(SwingUtilities.isLeftMouseButton(me)&&zoomEnabled)
		    {//TODO
			 
		    }
	}
	
	private void mousePressed(MouseEvent me){
		if(SwingUtilities.isRightMouseButton(me))
		    {lastDragPoint=me.getPoint();
		     setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
		    }
	}
	
	private void mouseDragged(MouseEvent me){
		if(SwingUtilities.isRightMouseButton(me))
	        {final int deltaX=me.getPoint().x-lastDragPoint.x;
	         final int deltaY=me.getPoint().y-lastDragPoint.y;
		     if(getHorizontalScrollBar().isVisible())
			     getHorizontalScrollBar().setValue(getHorizontalScrollBar().getValue()+deltaX);
		     if(getVerticalScrollBar().isVisible())
			     getVerticalScrollBar().setValue(getVerticalScrollBar().getValue()+deltaY);
	         lastDragPoint=me.getPoint();
	        }
	}
	
	private void mouseReleased(MouseEvent me){
		if(SwingUtilities.isRightMouseButton(me))
		    {lastDragPoint=null;
		     setCursor(Cursor.getDefaultCursor());
		    }
	}
	
	/**
	 * Dummy image panel, responsible for painting the image as is. Its preferred size is the preferred size of the image
	 */
    private static final class DummyImagePanel extends JPanel{
		
		private static final long serialVersionUID=1L;
		
		private final BufferedImage image;
		
		private DummyImagePanel(final BufferedImage image){
			super();
			this.image=image;
			setPreferredSize(new Dimension(image.getWidth(),image.getHeight()));
		}
		
		@Override
		protected void paintComponent(Graphics g){
			super.paintComponent(g);
			g.drawImage(image,0,0,null);
		}
	}
}
