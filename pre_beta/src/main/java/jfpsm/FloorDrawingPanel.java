/**
 * Copyright (c) 2006-2019 Julien Gouesse
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

import java.awt.Graphics;
import java.awt.image.BufferedImage;

final class FloorDrawingPanel extends DrawingPanel {

    private static final long serialVersionUID = 1L;

    private final Map map;

    FloorDrawingPanel(final Floor floor, final MapType type, final ZoomParameters zoomParams,
            final FloorViewer floorViewer) {
        super(type.getLabel(), floor.getMap(type).getImage(), zoomParams, floorViewer);
        map = floor.getMap(type);
    }

    @Override
    protected final boolean draw(int x1, int y1, int x2, int y2) {
        final boolean success;
        if (success = super.draw(x1, y1, x2, y2))
            map.markDirty();
        return (success);
    }

    @Override
    protected void paintComponent(Graphics g) {
        BufferedImage mapImage = map.getImage();
        // check if the underlying image has been changed (during import)
        if (mapImage != getImage())
            setImage(mapImage);
        super.paintComponent(g);
    }
}
