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

import java.io.File;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

public interface I3DServiceSeeker{
    public void bind3DServiceSeeker(I3DServiceSeeker seeker);
    public boolean writeSavableInstanceIntoFile(final Object savable,final File file);
    public void attachChildToNode(final Object parent,final Object child);
    public Object createNode(final String name);
    public Object createMeshFromBuffers(final String name,
    		final FloatBuffer vertexBuffer,final IntBuffer indexBuffer,
    		final FloatBuffer normalBuffer,final FloatBuffer texCoordBuffer);
    public void attachTextureToSpatial(final Object spatial,final String path);
}
