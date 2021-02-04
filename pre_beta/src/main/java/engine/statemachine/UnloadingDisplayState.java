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
package engine.statemachine;

import com.ardor3d.bounding.BoundingBox;
import com.ardor3d.framework.NativeCanvas;
import com.ardor3d.image.Texture;
import com.ardor3d.math.Vector3;
import com.ardor3d.renderer.Camera;
import com.ardor3d.renderer.state.TextureState;
import com.ardor3d.renderer.state.RenderState.StateType;
import com.ardor3d.scenegraph.Spatial;
import com.ardor3d.scenegraph.controller.SpatialController;
import com.ardor3d.scenegraph.shape.Box;
import com.ardor3d.ui.text.BMText;
import com.ardor3d.util.TextureManager;

import engine.misc.FontStore;
import engine.misc.LocalizedMessageProvider;
import engine.sound.SoundManager;
import engine.taskmanagement.TaskManagementProgressionNode;
import engine.taskmanagement.TaskManager;

/**
 * State that carefully unloads all resources used by the latest game
 * 
 * @author Julien Gouesse
 *
 */
public class UnloadingDisplayState extends ScenegraphState {

    private final TaskManagementProgressionNode taskNode;

    private final TaskManager taskManager;
    /** box on which the textures are displayed */
    private final Box box;

    private final Texture[] textures;

    private final String[] texturesPaths;

    private final Camera cam;

    private final Runnable gameStateCleanupRunnable;

    public UnloadingDisplayState(final NativeCanvas canvas, final TaskManager taskManager,
            final SoundManager soundManager, final Runnable gameStateCleanupRunnable, final FontStore fontStore,
            final LocalizedMessageProvider localizedMessageProvider) {
        super(soundManager);
        this.taskManager = taskManager;
        this.gameStateCleanupRunnable = gameStateCleanupRunnable;
        taskNode = new TaskManagementProgressionNode(canvas, canvas.getCanvasRenderer().getCamera(), taskManager, localizedMessageProvider);
        cam = canvas.getCanvasRenderer().getCamera();
        getRoot().attachChild(taskNode);
        texturesPaths = new String[] { "communism.png", "venimus_vidimus_vicimus.png" };
        textures = new Texture[texturesPaths.length];
        // executes tasks
        taskNode.addController(new SpatialController<>() {

            private boolean oneSkipDone = false;

            private boolean firstTaskOk = false;

            private int textureIndex = 0;

            private int taskCount = 0;

            @Override
            public final void update(final double time, final Spatial caller) {
                // updates the texture
                final TextureState textureState = (TextureState) box.getLocalRenderState(StateType.Texture);
                textureState.setTexture(textures[textureIndex]);
                // performs the long task only at the second update to display
                // the task node correctly
                if (!oneSkipDone)
                    oneSkipDone = true;
                else {
                    if (taskManager.getTaskCount() > 0) {
                        taskManager.executeFirstTask();
                        if (!firstTaskOk) {
                            firstTaskOk = true;
                            // the first task is used to enqueue all loading
                            // tasks
                            taskNode.reset();
                            taskCount = taskManager.getTaskCount();
                        }
                        // updates the texture index
                        if (taskManager.getTaskCount() > 0)
                            textureIndex = (int) Math.floor(
                                    ((taskCount - taskManager.getTaskCount()) / (double) taskCount) * textures.length);
                        else
                            textureIndex = textures.length - 1;
                    }
                    if (taskManager.getTaskCount() == 0) {
                        oneSkipDone = false;
                        firstTaskOk = false;
                        textureIndex = 0;
                    }
                }
            }
        });
        box = new Box("Level Loading Display Box", Vector3.ZERO, 5, 5, 5);
        box.setModelBound(new BoundingBox());
        box.setTranslation(new Vector3(0, 0, -15));
        getRoot().attachChild(box);
        final BMText unloadingTextLabel = new BMText("Level Index Text", "", fontStore.getFontsList().get(0),
                BMText.Align.Center, BMText.Justify.Center);
        unloadingTextLabel.setTranslation(unloadingTextLabel.getTranslation().add(0, 3.3, 0, null));
        getRoot().attachChild(unloadingTextLabel);
    }

    @Override
    public void init() {
        // puts a texture onto the box
        TextureState ts = new TextureState();
        ts.setEnabled(true);
        box.setRenderState(ts);
        // loads all textures displayed while loading a level
        for (int textureIndex = 0; textureIndex < texturesPaths.length; textureIndex++)
            textures[textureIndex] = TextureManager.load(texturesPaths[textureIndex],
                    Texture.MinificationFilter.Trilinear, true);
    }

    @Override
    public void setEnabled(final boolean enabled) {
        final boolean wasEnabled = isEnabled();
        if (wasEnabled != enabled) {
            super.setEnabled(enabled);
            if (enabled) {
                taskManager.enqueueTask(gameStateCleanupRunnable);
                taskNode.reset();
                // updates the position of the task node (the resolution might
                // have been modified)
                final int x = (cam.getWidth() - taskNode.getBounds().getWidth()) / 2;
                final int y = (cam.getHeight() / 20);
                taskNode.setTranslation(x, y, 0);
                taskNode.updateGeometricState(0);
            }
        }
    }
}
