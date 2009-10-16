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

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Toolkit;
import java.awt.event.MouseAdapter;
import java.awt.image.BufferedImage;
import javax.swing.Box;
import javax.swing.JPanel;

/**
 * Panel that allows the drawing. The current color is the color of the selected tile.
 * @author Julien Gouesse
 *
 */
class DrawingPanel extends JPanel{

	
	private static final long serialVersionUID=1L;

	private BufferedImage bufferedImage;
	
	private final String title;
	
	private final ZoomParameters zoomParams;
	
	private Graphics graphics;
	
	private final Viewer viewer;
	
	private final int fontSize;
	
	private final Box.Filler filler;
	
	
	/**
	 * 
	 * @param title title of the panel
	 * @param bufferedImage image used to draw
	 * @param zoomParams zoom parameters (zoom disabled if null)
	 * @param viewer viewer that displays this panel
	 */
	DrawingPanel(String title,BufferedImage bufferedImage,ZoomParameters zoomParams,Viewer viewer){
		super();
		this.viewer=viewer;
		this.title=title;
		this.zoomParams=zoomParams;		
		fontSize=getFontMetrics(getFont()).getHeight();
		Dimension prefDim=new Dimension(bufferedImage.getWidth(),bufferedImage.getHeight());
		filler=new Box.Filler(prefDim,prefDim,prefDim);
		setImage(bufferedImage);
		add(filler);
		MouseAdapter mouseAdapter=new DrawingMouseAdapter(this);
		addMouseMotionListener(mouseAdapter);
		addMouseListener(mouseAdapter);
	}
	
	
	protected boolean draw(int x1,int y1,int x2,int y2){
		//get the color of the selected tile
	    Color color=viewer.getSelectedTileColor();
	    final boolean success;
	    if(success=color!=null)
	        {graphics.setColor(color);
	         graphics.drawLine(x1,y1,x2,y2);
	         repaint();
	        }
	    return(success);
	}
	
	final void move(int x1,int y1,int x2,int y2){
	    zoomParams.setCenterx(zoomParams.getCenterx()+x2-x1);
	    zoomParams.setCentery(zoomParams.getCentery()+y2-y1);
	    viewer.repaint();
	}
	
	final ZoomParameters getZoomParameters(){
	    return(zoomParams);
	}
	
	final void setImage(BufferedImage bufferedImage){
		this.bufferedImage=bufferedImage;
		graphics=bufferedImage.createGraphics();
		final int min=Integer.highestOneBit(Toolkit.getDefaultToolkit().getScreenSize().height/2);
		final int width=bufferedImage.getWidth();
		final int height=bufferedImage.getHeight();
		Dimension prefSize=new Dimension(width,height+fontSize+2);
		setPreferredSize(prefSize);
		filler.setMinimumSize(new Dimension(min,min));
		filler.setPreferredSize(prefSize);
		filler.setMaximumSize(prefSize);
		//reset the zoom
        zoomParams.setFactor(1);
        zoomParams.setWidth(width);
        zoomParams.setHeight(height);
        zoomParams.setCenterx(width/2);
        zoomParams.setCentery(height/2);
	}
	
	final BufferedImage getImage(){
	    return(bufferedImage);
	}
	
	@Override
	protected void paintComponent(Graphics g){
		super.paintComponent(g);
		if(zoomParams==null)
            g.drawImage(bufferedImage,0,0,this);
        else
            {int w=bufferedImage.getWidth(),h=bufferedImage.getHeight();
		     int factor=zoomParams.getFactor();
		     int cx=zoomParams.getCenterx(),cy=zoomParams.getCentery();
		     int halfDw=(w/factor)/2,halfDh=(h/factor)/2;
		     g.drawImage(bufferedImage,0,0,w-1,h-1,cx-halfDw,cy-halfDh,cx+halfDw,cy+halfDh,this);
            }
		g.drawString(title,0,bufferedImage.getHeight()+g.getFontMetrics().getHeight());
	}
}
