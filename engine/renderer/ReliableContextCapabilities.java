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

import com.ardor3d.renderer.ContextCapabilities;

public class ReliableContextCapabilities extends ContextCapabilities {

	public ReliableContextCapabilities(final ContextCapabilities defaultCaps){
		super(defaultCaps);
        //System.err.println(defaultCaps.getDisplayRenderer());
        //System.err.println(defaultCaps.getDisplayVendor());
        //System.err.println(defaultCaps.getDisplayVersion());
        if(defaultCaps.getDisplayRenderer().startsWith("Mesa DRI R200 "))
      	    /**
      	     * Some very old ATI Radeon graphics cards do not support 2048*2048 textures
      	     * despite their specifications.
      	     */
            _maxTextureSize=defaultCaps.getMaxTextureSize()/2;
        //FIXME R300 and R500 drivers on Mac OS X sometimes return absurd values
	}
}
