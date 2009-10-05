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
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import javax.swing.JPanel;

/**
 * Panel that allows the drawing. The current color is the color of the selected tile.
 * @author Julien Gouesse
 *
 */
final class DrawingPanel extends JPanel{

	
	private static final long serialVersionUID=1L;

	private BufferedImage bufferedImage;
	
	private String title;
	
	private ZoomParameters zoomParams;
	
	private Dirtyable entity;
	
	
	DrawingPanel(Dirtyable entity,String title,BufferedImage bufferedImage){
		super();
		this.bufferedImage=bufferedImage;
		this.title=title;
		this.zoomParams=null;
		this.entity=entity;
		final int fontSize=getFontMetrics(getFont()).getHeight();		
		setPreferredSize(new Dimension(this.bufferedImage.getWidth(),this.bufferedImage.getHeight()+fontSize+2));
		MouseAdapter mouseAdapter=new MouseAdapter(){
		    @Override
            public final void mouseDragged(MouseEvent e){
                DrawingPanel.this.draw(e.getX(),e.getY());          
            }
		    
		    @Override
            public final void mouseClicked(MouseEvent e){
                DrawingPanel.this.draw(e.getX(),e.getY());          
            }
		};
		addMouseMotionListener(mouseAdapter);
		addMouseListener(mouseAdapter);
	}
	
	private final void draw(final int x,final int y){
	    if(0<=x&&x<bufferedImage.getWidth()&&0<=y&&y<bufferedImage.getHeight())
            {if(zoomParams==null)
                 bufferedImage.setRGB(x,y,Color.BLACK.getRGB());
             else
                 bufferedImage.setRGB(zoomParams.getAbsoluteXFromRelativeX(x),zoomParams.getAbsoluteYFromRelativeY(y),Color.BLACK.getRGB());
             entity.markDirty();
             repaint();                  
            }
	}
	
	final void setZoomParameters(ZoomParameters zoomParams){
	    this.zoomParams=zoomParams;
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
