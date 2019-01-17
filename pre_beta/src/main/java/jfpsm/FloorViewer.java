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

import java.awt.GridLayout;
import java.awt.image.BufferedImage;
import javax.swing.JSplitPane;

/**
 * floor viewer
 * 
 * @author Julien Gouesse
 *
 */
final class FloorViewer extends JFPSMProjectUserObjectViewer {

    private static final long serialVersionUID = 1L;

    private final FloorDrawingPanel[] drawingPanels;

    private final DrawingPanel pathDrawingPanel;

    private final ZoomParameters zoomParams;

    FloorViewer(final Floor floor, final Project project, final ProjectManager projectManager) {
        super(floor, project, projectManager);
        setLayout(new GridLayout(1, 1));
        zoomParams = new ZoomParameters(1, floor.getMap(MapType.CONTAINER_MAP).getWidth(),
                floor.getMap(MapType.CONTAINER_MAP).getHeight());
        drawingPanels = new FloorDrawingPanel[MapType.values().length];
        for (MapType type : MapType.values())
            drawingPanels[type.ordinal()] = new FloorDrawingPanel(floor, type, zoomParams, this);
        JSplitPane leftVerticalSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, true,
                drawingPanels[MapType.CONTAINER_MAP.ordinal()], drawingPanels[MapType.CONTENT_MAP.ordinal()]);
        leftVerticalSplitPane.setOneTouchExpandable(true);
        pathDrawingPanel = new DrawingPanel("path map",
                new BufferedImage(floor.getMap(MapType.CONTAINER_MAP).getWidth(),
                        floor.getMap(MapType.CONTAINER_MAP).getHeight(), BufferedImage.TYPE_INT_ARGB),
                zoomParams, this);
        JSplitPane rightVerticalSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, true,
                drawingPanels[MapType.LIGHT_MAP.ordinal()], pathDrawingPanel);
        rightVerticalSplitPane.setOneTouchExpandable(true);
        JSplitPane horizontalSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, true, leftVerticalSplitPane,
                rightVerticalSplitPane);
        horizontalSplitPane.setOneTouchExpandable(true);
        add(horizontalSplitPane);
        ZoomMouseWheelListener wheelListener = new ZoomMouseWheelListener(this);
        for (MapType type : MapType.values())
            drawingPanels[type.ordinal()].addMouseWheelListener(wheelListener);
        pathDrawingPanel.addMouseWheelListener(wheelListener);
    }

    final void updateZoom(int factorIncrement, int x, int y) {
        // check if the cursor is over the drawable zone
        if (0 <= x && x < zoomParams.getWidth() && 0 <= y && y < zoomParams.getHeight()) {
            int previousFactor = zoomParams.getFactor(), nextFactor;
            if (factorIncrement >= 1)
                nextFactor = Math.min(zoomParams.getFactor() * 2, 32);
            else if (factorIncrement <= -1)
                nextFactor = Math.max(zoomParams.getFactor() / 2, 1);
            else
                nextFactor = previousFactor;
            if (previousFactor != nextFactor) {// the conversion has to be done
                                               // before updating the factor
                int nextX = zoomParams.getAbsoluteXFromRelativeX(x);
                int nextY = zoomParams.getAbsoluteYFromRelativeY(y);
                zoomParams.setFactor(nextFactor);
                zoomParams.setCenterx(nextX);
                zoomParams.setCentery(nextY);
                for (MapType type : MapType.values())
                    drawingPanels[type.ordinal()].repaint();
                pathDrawingPanel.repaint();
            }
        }

    }
}
