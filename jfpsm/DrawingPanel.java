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
import java.awt.Graphics2D;
import java.awt.event.MouseAdapter;
import java.awt.image.BufferedImage;
import javax.swing.JPanel;

/**
 * Panel that allows the drawing. The current color is the color of the selected tile.
 * @author Julien Gouesse
 *
 */
final class DrawingPanel extends JPanel{

	
	private static final long serialVersionUID=1L;

	private final BufferedImage bufferedImage;
	
	private final String title;
	
	private final ZoomParameters zoomParams;
	
	private final Graphics graphics;
	
	
	/**
	 * 
	 * @param entity displayed entity
	 * @param title title of the panel
	 * @param bufferedImage image used to draw
	 * @param zoomParams zoom parameters (zoom disabled if null)
	 */
	DrawingPanel(Dirtyable entity,String title,BufferedImage bufferedImage,ZoomParameters zoomParams){
		super();
		this.bufferedImage=bufferedImage;
		graphics=bufferedImage.createGraphics();
		graphics.setColor(Color.BLACK);
		this.title=title;
		this.zoomParams=zoomParams;
		final int fontSize=getFontMetrics(getFont()).getHeight();		
		setPreferredSize(new Dimension(this.bufferedImage.getWidth(),this.bufferedImage.getHeight()+fontSize+2));
		MouseAdapter mouseAdapter=new DrawingMouseAdapter(this);
		addMouseMotionListener(mouseAdapter);
		addMouseListener(mouseAdapter);
	}
	
	final void draw(int x1,int y1,int x2,int y2){
	    graphics.drawLine(x1,y1,x2,y2);
	    repaint();
	}
	
	final ZoomParameters getZoomParameters(){
	    return(zoomParams);
	}
	
	@Override
	protected final void paintComponent(Graphics g){
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
