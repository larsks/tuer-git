/**
 * Copyright (c) 2006-2021 Julien Gouesse
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
package engine.renderer;

import java.nio.Buffer;

import com.ardor3d.image.Image;
import com.ardor3d.image.Texture;
import com.ardor3d.renderer.jogl.JoglRenderer;
import com.ardor3d.scenegraph.AbstractBufferData;
import engine.misc.SimpleDeallocationHelper;

/**
 * Reliable JOGL renderer able to cleanly release all native resources
 * 
 * @author Julien Gouesse
 *
 */
public class ReliableRenderer extends JoglRenderer {

    private final SimpleDeallocationHelper deallocationHelper;

    public ReliableRenderer() {
        this(new SimpleDeallocationHelper());
    }

    public ReliableRenderer(final SimpleDeallocationHelper deallocationHelper) {
        super();
        this.deallocationHelper = deallocationHelper;
    }

    @Override
    public void deleteVBOs(final AbstractBufferData<?> buffer) {
        super.deleteVBOs(buffer);
        final Buffer realNioBuffer = buffer.getBuffer();
        deleteBuffer(realNioBuffer);
    }

    @Override
    public void deleteTexture(final Texture texture) {
        super.deleteTexture(texture);
        final Image image = texture.getImage();
        if (image != null && image.getDataSize() >= 1) {
            for (Buffer data : image.getData()) {
                deleteBuffer(data);
            }
        }
    }

    public void deleteBuffer(final Buffer realNioBuffer) {
        if (deallocationHelper != null) {
            deallocationHelper.deallocate(realNioBuffer);
        }
    }
}
