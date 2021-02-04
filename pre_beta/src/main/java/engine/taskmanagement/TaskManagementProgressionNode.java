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
package engine.taskmanagement;

import com.ardor3d.extension.ui.UIFrame;
import com.ardor3d.extension.ui.UIHud;
import com.ardor3d.extension.ui.UILabel;
import com.ardor3d.extension.ui.UIPanel;
import com.ardor3d.extension.ui.UIProgressBar;
import com.ardor3d.extension.ui.layout.RowLayout;
import com.ardor3d.framework.NativeCanvas;
import com.ardor3d.math.Rectangle2;
import com.ardor3d.renderer.Camera;
import com.ardor3d.scenegraph.Node;
import com.ardor3d.scenegraph.Spatial;
import com.ardor3d.scenegraph.controller.SpatialController;

import engine.misc.LocalizedMessageProvider;

public final class TaskManagementProgressionNode extends Node {

    private final UIProgressBar bar;

    private int maxTaskCount;

    private final TaskManager taskManager;

    private final UIFrame frame;

    private final Rectangle2 bounds;

    public TaskManagementProgressionNode(final NativeCanvas canvas, final Camera cam, final TaskManager taskManager, final LocalizedMessageProvider localizedMessageProvider) {
        super("task progression node");
        bounds = new Rectangle2();
        this.taskManager = taskManager;
        maxTaskCount = 0;
        bar = new UIProgressBar("", true);
        bar.setPercentFilled(0);
        // bar.setComponentWidth(250);
        final UIPanel panel = new UIPanel(new RowLayout(false));
        final String text = localizedMessageProvider.getString("LOADING_PLEASE_WAIT");
        panel.add(new UILabel(text));
        panel.add(bar);
        frame = new UIFrame("");
        frame.setDecorated(false);
        frame.setContentPanel(panel);
        frame.updateMinimumSizeFromContents();
        frame.layout();
        frame.pack();
        frame.setUseStandin(false);
        frame.setOpacity(1f);
        frame.setName("task progression frame");
        frame.getRelativeComponentBounds(bounds);
        final UIHud hud = new UIHud(canvas);
        hud.add(frame);
        attachChild(hud);
        addController(new SpatialController<>() {
            @Override
            public final void update(final double time, final Spatial caller) {
                final int taskCount = taskManager.getTaskCount();
                if (maxTaskCount == 0)
                    bar.setPercentFilled(0);
                else
                    bar.setPercentFilled(1 - ((double) taskCount) / maxTaskCount);
            }
        });
        // centers this node by default
        final int x = (cam.getWidth() - getBounds().getWidth()) / 2;
        final int y = (cam.getHeight() - getBounds().getHeight()) / 2;
        setTranslation(x, y, 0);
        updateGeometricState(0);
    }

    public final Rectangle2 getBounds() {
        frame.getRelativeComponentBounds(bounds);
        return (bounds);
    }

    public final void reset() {
        maxTaskCount = taskManager.getTaskCount();
    }
}
