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
package engine.renderer;

import java.nio.Buffer;

import com.ardor3d.renderer.jogl.JoglRenderer;
import com.ardor3d.scenegraph.AbstractBufferData;

import sun.misc.Cleaner;
import sun.nio.ch.DirectBuffer;

/**
 * Reliable JOGL renderer able to cleanly release all native resources. However, it requires manual interventions.
 * 
 * @author Julien Gouesse
 *
 */
@SuppressWarnings("restriction")
public class ReliableRenderer extends JoglRenderer{

	public ReliableRenderer(){
		super();
	}
	
	@Override
	public void deleteVBOs(final AbstractBufferData<?> buffer){
		super.deleteVBOs(buffer);
		final Buffer realNioBuffer=buffer.getBuffer();
		if(realNioBuffer.isDirect())
		    {final DirectBuffer nioDirectBuffer=(DirectBuffer)realNioBuffer;
			 final Cleaner cleaner=nioDirectBuffer.cleaner();
			 if(cleaner!=null)
				 cleaner.clean();
		    }
	}
}
