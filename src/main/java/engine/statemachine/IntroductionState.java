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

import com.jogamp.nativewindow.util.Point;

import java.net.URL;
import java.util.HashMap;
import java.util.LinkedHashMap;

import com.ardor3d.bounding.BoundingBox;
import com.ardor3d.framework.NativeCanvas;
import com.ardor3d.image.Texture;
import com.ardor3d.input.Key;
import com.ardor3d.input.PhysicalLayer;
import com.ardor3d.input.logical.InputTrigger;
import com.ardor3d.input.logical.KeyPressedCondition;
import com.ardor3d.input.logical.TriggerAction;
import com.ardor3d.math.ColorRGBA;
import com.ardor3d.math.Vector3;
import com.ardor3d.math.type.ReadOnlyColorRGBA;
import com.ardor3d.renderer.state.TextureState;
import com.ardor3d.scenegraph.shape.Box;
import com.ardor3d.ui.text.BMText;
import com.ardor3d.util.TextureManager;
import com.ardor3d.util.resource.URLResourceSource;

import engine.misc.FontStore;
import engine.movement.CircularSpreadTextureUpdaterController;
import engine.movement.MovementEquation;
import engine.movement.UniformlyVariableMovementEquation;
import engine.movement.UniformlyVariableRectilinearTranslationController;
import engine.sound.SoundManager;

public final class IntroductionState extends ScenegraphState {

    private static final String soundSamplePath = "/sounds/introduction.ogg";

    private static final String textureFilePath = "/images/introduction.png";

    private String soundIdentifier;

    private final Box box;

    /**
     * Constructor
     * 
     * @param canvas
     * @param physicalLayer
     * @param toExitGameTriggerAction
     * @param toMainMenuAction
     * @param soundManager
     * @param fontStore
     * @param gameShortName
     */
    public IntroductionState(final NativeCanvas canvas, final PhysicalLayer physicalLayer,
            final TransitionTriggerAction<ScenegraphState, String> toExitGameTriggerAction,
            final TriggerAction toMainMenuAction, final SoundManager soundManager, final FontStore fontStore,
            final String gameShortName, final String gameIntroductionSubtitle) {
        super(soundManager);
        box = new Box("Introduction Box", Vector3.ZERO, 12, 9, 5);
        box.setModelBound(new BoundingBox());
        box.setTranslation(new Vector3(0, 0, -75));
        // configures the spread effect
        final Point spreadCenter = new Point(205, 265);
        HashMap<ReadOnlyColorRGBA, ReadOnlyColorRGBA> colorSubstitutionTable = new HashMap<>();
        colorSubstitutionTable.put(ColorRGBA.BLUE, ColorRGBA.RED);
        MovementEquation equation = new UniformlyVariableMovementEquation(0, 10500, 0);
        // sets a controller that modifies the image
        /**
         * TODO: replace this controller by another one that simply switches the
         * textures because the current one modifies the unique texture at
         * runtime which is very slow on Intel graphics cards and on-board chips
         */
        box.addController(new CircularSpreadTextureUpdaterController(textureFilePath, equation, colorSubstitutionTable,
                spreadCenter, canvas.getCanvasRenderer().getRenderer(), canvas.getCanvasRenderer().getRenderContext()));
        // sets a controller that moves the image
        LinkedHashMap<Double, Double> timeWindowsTable = new LinkedHashMap<>();
        timeWindowsTable.put(Double.valueOf(0), Double.valueOf(6));
        box.addController(new UniformlyVariableRectilinearTranslationController(0, 10, -75, new Vector3(0, 0, 1),
                timeWindowsTable));
        getRoot().attachChild(box);
        // shows the game title as text
        final BMText textNode = new BMText("gameTitleNode", gameShortName, fontStore.getFontsList().get(1),
                BMText.Align.Center, BMText.Justify.Center);
        textNode.setFontScale(6);
        textNode.setTextColor(ColorRGBA.BLACK);
        textNode.setTranslation(0, 0, -75);
        timeWindowsTable = new LinkedHashMap<>();
        timeWindowsTable.put(Double.valueOf(0), Double.valueOf(8));
        textNode.addController(new UniformlyVariableRectilinearTranslationController(0, 10, -75, new Vector3(0, 0, 1),
                timeWindowsTable));
        getRoot().attachChild(textNode);
        // shows the subtitle if any
        if (gameIntroductionSubtitle != null && !gameIntroductionSubtitle.isEmpty()) {
            final BMText subtitleNode = new BMText("subtitleNode", gameIntroductionSubtitle,
                    fontStore.getFontsList().get(2), BMText.Align.Center, BMText.Justify.Center);
            subtitleNode.setFontScale(6);
            subtitleNode.setTextColor(ColorRGBA.ORANGE);
            subtitleNode.setTranslation(0, -5, -5);
            getRoot().attachChild(subtitleNode);
        }
        // adds the triggers
        final InputTrigger toMainMenuTrigger = new InputTrigger(new KeyPressedCondition(Key.RETURN), toMainMenuAction);
        final InputTrigger exitTrigger = new InputTrigger(new KeyPressedCondition(Key.ESCAPE), toExitGameTriggerAction);
        final InputTrigger[] triggers = new InputTrigger[] { exitTrigger, toMainMenuTrigger };
        // binds the physical layer to the logical layer
        getLogicalLayer().registerInput(canvas, physicalLayer);
        for (InputTrigger trigger : triggers)
            getLogicalLayer().registerTrigger(trigger);
    }

    @Override
    public final void init() {
        // load the music
        final URL sampleUrl = IntroductionState.class.getResource(soundSamplePath);
        if (sampleUrl != null)
            soundIdentifier = getSoundManager().loadSound(sampleUrl);
        else
            soundIdentifier = null;
        // puts a texture onto the box
        final TextureState ts = new TextureState();
        ts.setEnabled(true);
        ts.setTexture(TextureManager.load(new URLResourceSource(getClass().getResource(textureFilePath)),
                Texture.MinificationFilter.Trilinear, true));
        box.setRenderState(ts);
    }

    @Override
    public void setEnabled(final boolean enabled) {
        final boolean wasEnabled = isEnabled();
        if (wasEnabled != enabled) {
            super.setEnabled(enabled);
            if (enabled) {
                if (soundIdentifier != null)
                    getSoundManager().play(true, false, soundIdentifier);
            } else {
                if (soundIdentifier != null)
                    getSoundManager().stop();
            }
        }
    }
}
