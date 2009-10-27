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
import java.awt.Toolkit;
import java.awt.image.BufferedImage;

final class Map extends JFPSMUserObject{

	
	static{SerializationHelper.forceHandlingOfTransientModifiersForXMLSerialization(Map.class);}
	
	private static final long serialVersionUID=1L;
	
	private static final int minimumSize=Integer.highestOneBit(Toolkit.getDefaultToolkit().getScreenSize().height/2);
	
	private transient boolean dirty;
	
	private transient BufferedImage image;
	
	
	public Map(){
		this("");
	}
	
	public Map(String name){
		super(name);
		initializeImage();
		markDirty();
	}
	
	
	@Override
	public final boolean isDirty(){
		return(dirty);
	}

	@Override
	public final void markDirty(){
		dirty=true;
	}

	@Override
	public final void unmarkDirty(){
		dirty=false;
	}
	
	final BufferedImage getImage(){
		return(image);
	}
	
	final void setImage(BufferedImage image){
		this.image=image;
		markDirty();
	}
	
	private final void initializeImage(){
		image=new BufferedImage(minimumSize,minimumSize,BufferedImage.TYPE_INT_ARGB);
	    for(int x=0;x<image.getWidth();x++)
            for(int y=0;y<image.getHeight();y++)
            	image.setRGB(x,y,Color.WHITE.getRGB());
	}
	
	public final int getWidth(){
	    return(image.getWidth());
	}
	
	public final int getHeight(){
        return(image.getHeight());
    }

    @Override
    final boolean canInstantiateChildren(){
        return(false);
    }

    @Override
    final boolean isOpenable(){
        return(false);
    }

    @Override
    final boolean isRemovable(){
        return(false);
    }
}
