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

import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;

/**
 * image viewer supporting the scroll (with the scroll bars), the drag (on right click) and the zoom (work in progress)
 * 
 * @author Julien Gouesse
 *
 */
public class ImageViewer extends JPanel{

	private static final long serialVersionUID=1L;
	
	private final JScrollPane scrollPane;
	
	private boolean zoomEnabled;
	
	private Point lastDragPoint;
	
	private final DummyImagePanel dummyImagePanel;
	
	public ImageViewer(final BufferedImage image){
		super();
		setLayout(new BorderLayout());
		this.scrollPane=new JScrollPane();
		this.dummyImagePanel=new DummyImagePanel(image);
		this.scrollPane.setViewportView(dummyImagePanel);
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
		add(scrollPane,BorderLayout.CENTER);
		final JToolBar toolBar=new JToolBar(JToolBar.VERTICAL);
		final JButton normalModeButton=new JButton(" ");
		normalModeButton.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent ae) {
				normalModeButtonActionPerformed(ae);
			}
		});
		final JButton zoomModeButton=new JButton("Z");
		zoomModeButton.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent ae) {
				zoomModeButtonActionPerformed(ae);
			}
		});
		toolBar.add(normalModeButton);
		toolBar.add(zoomModeButton);
		add(toolBar,BorderLayout.WEST);
		this.scrollPane.addMouseListener(mouseAdapter);
		this.scrollPane.addMouseMotionListener(mouseAdapter);
	}
	
	private void normalModeButtonActionPerformed(ActionEvent ae){
		setZoomEnabled(false);
	}
	
	private void zoomModeButtonActionPerformed(ActionEvent ae){
		setZoomEnabled(true);
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
			 //dummyImagePanel.transform=;
		    }
	}
	
	private void mousePressed(MouseEvent me){
		try{if(SwingUtilities.isRightMouseButton(me))
		        setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
		    if(SwingUtilities.isLeftMouseButton(me)&&zoomEnabled)
		    	setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
		   }
		finally
		{lastDragPoint=me.getPoint();}
	}
	
	private void mouseDragged(MouseEvent me){
		try{if(SwingUtilities.isRightMouseButton(me))
	            {final int deltaX=me.getPoint().x-lastDragPoint.x;
	             final int deltaY=me.getPoint().y-lastDragPoint.y;
		         if(scrollPane.getHorizontalScrollBar().isVisible())
		    	     scrollPane.getHorizontalScrollBar().setValue(scrollPane.getHorizontalScrollBar().getValue()+deltaX);
		         if(scrollPane.getVerticalScrollBar().isVisible())
		    	     scrollPane.getVerticalScrollBar().setValue(scrollPane.getVerticalScrollBar().getValue()+deltaY);
	            }
		   }
		finally
		{lastDragPoint=me.getPoint();}
	}
	
	private void mouseReleased(MouseEvent me){
		try{
			
		   }
		finally
		{lastDragPoint=null;
		 if(zoomEnabled)
			 setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
		 else
		     setCursor(Cursor.getDefaultCursor());
		}
	}
	
	/**
	 * Dummy image panel, responsible for painting the image as is. Its preferred size is the preferred size of the image
	 */
    private static final class DummyImagePanel extends JPanel{
		
		private static final long serialVersionUID=1L;
		
		private final BufferedImage image;
		
		private AffineTransform transform;
		
		private DummyImagePanel(final BufferedImage image){
			super();
			this.image=image;
			this.transform=new AffineTransform();
			setPreferredSize(new Dimension(image.getWidth(),image.getHeight()));
		}
		
		AffineTransform getTransform(){
			return(transform);
		}
		
		void setTransform(final AffineTransform transform){
			this.transform=transform;
		}
		
		@Override
		protected void paintComponent(Graphics g){
			super.paintComponent(g);
			((Graphics2D)g).drawImage(image,transform,null);
		}
	}
}
