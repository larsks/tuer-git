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

import java.util.List;

import com.ardor3d.extension.ui.UIButton;
import com.ardor3d.extension.ui.UIFrame;
import com.ardor3d.extension.ui.UIHud;
import com.ardor3d.extension.ui.UILabel;
import com.ardor3d.extension.ui.UIPanel;
import com.ardor3d.extension.ui.event.ActionEvent;
import com.ardor3d.extension.ui.event.ActionListener;
import com.ardor3d.extension.ui.layout.RowLayout;
import com.ardor3d.framework.NativeCanvas;
import com.ardor3d.input.GrabbedState;
import com.ardor3d.input.MouseManager;
import com.ardor3d.input.PhysicalLayer;
import com.ardor3d.math.ColorRGBA;
import com.ardor3d.scenegraph.Node;
import com.ardor3d.scenegraph.controller.SpatialController;
import com.ardor3d.ui.text.BMText;

import engine.data.Objective;
import engine.data.ObjectiveStatus;
import engine.misc.FontStore;
import engine.misc.LocalizedMessageProvider;
import engine.sound.SoundManager;

/**
 * State representing the end of a game, caused by the death of the player, his
 * victory or his abandonment
 * 
 * @author Julien Gouesse
 *
 */
public class GameOverState extends ScenegraphState {

    private final NativeCanvas canvas;

    private final PhysicalLayer physicalLayer;

    private final MouseManager mouseManager;

    private final TransitionTriggerAction<ScenegraphState, String> toUnloadingDisplayTriggerActionForExit;

    private final TransitionTriggerAction<ScenegraphState, String> toUnloadingDisplayTriggerActionForMainMenu;

    private final TransitionTriggerAction<ScenegraphState, String> toUnloadingDisplayTriggerActionForLoadingDisplay;

    private final UIFrame mainFrame;
    
    private final UIHud hud;

    private final UIPanel initialMenuPanel;

    private final UIPanel confirmExitMenuPanel;

    private String latestPlayedLevelIdentifier;

    private String latestNextPlayableLevelIdentifier;

    private final LocalizedMessageProvider localizedMessageProvider;

    private GameStatistics gameStats;

    private List<Objective> objectives;

    private final BMText textNode;

    public GameOverState(final NativeCanvas canvas, final PhysicalLayer physicalLayer, final MouseManager mouseManager,
            final SoundManager soundManager, final FontStore fontStore,
            final LocalizedMessageProvider localizedMessageProvider,
            final TransitionTriggerAction<ScenegraphState, String> toUnloadingDisplayTriggerActionForExit,
            final TransitionTriggerAction<ScenegraphState, String> toUnloadingDisplayTriggerActionForMainMenu,
            final TransitionTriggerAction<ScenegraphState, String> toUnloadingDisplayTriggerActionForLoadingDisplay) {
        super(soundManager);
        this.canvas = canvas;
        this.physicalLayer = physicalLayer;
        this.mouseManager = mouseManager;
        this.toUnloadingDisplayTriggerActionForExit = toUnloadingDisplayTriggerActionForExit;
        this.toUnloadingDisplayTriggerActionForMainMenu = toUnloadingDisplayTriggerActionForMainMenu;
        this.toUnloadingDisplayTriggerActionForLoadingDisplay = toUnloadingDisplayTriggerActionForLoadingDisplay;
        this.latestPlayedLevelIdentifier = null;
        this.latestNextPlayableLevelIdentifier = null;
        this.localizedMessageProvider = localizedMessageProvider;
        initialMenuPanel = createInitialMenuPanel();
        confirmExitMenuPanel = createConfirmExitMenuPanel();
        // creates the main frame
        mainFrame = createMainFrame();
        // creates the head-up display
        hud = createHud();
        hud.add(mainFrame);
        getRoot().attachChild(hud);
        // adds some text
        textNode = new BMText("gameOverNode", localizedMessageProvider.getString("GAME_OVER"),
                fontStore.getFontsList().get(2), BMText.Align.Center, BMText.Justify.Center);
        textNode.setFontScale(10);
        textNode.setTextColor(ColorRGBA.RED);
        textNode.setTranslation(textNode.getTranslation().add(0, 3.3, 0, null));
        getRoot().attachChild(textNode);
    }

    private final UIPanel createInitialMenuPanel() {
        final UIPanel initialMenuPanel = new UIPanel(new RowLayout(false));
        final UIButton nextButton = new UIButton(localizedMessageProvider.getString("NEXT"));
        nextButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                onNextButtonActionPerformed(ae);
            }
        });
        final UIButton retryButton = new UIButton(localizedMessageProvider.getString("RETRY"));
        retryButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                onRetryButtonActionPerformed(ae);
            }
        });
        final UIButton mainMenuButton = new UIButton(localizedMessageProvider.getString("MAIN_MENU"));
        mainMenuButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                onMainMenuButtonActionPerformed(ae);
            }
        });
        final UIButton exitButton = new UIButton(localizedMessageProvider.getString("EXIT"));
        exitButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                onExitButtonActionPerformed(ae);
            }
        });
        initialMenuPanel.add(nextButton);
        initialMenuPanel.add(retryButton);
        initialMenuPanel.add(mainMenuButton);
        initialMenuPanel.add(exitButton);
        return (initialMenuPanel);
    }

    private void onNextButtonActionPerformed(final ActionEvent ae) {
        toUnloadingDisplayTriggerActionForLoadingDisplay.arguments
                .setNextLevelIdentifier(latestNextPlayableLevelIdentifier);
        toUnloadingDisplayTriggerActionForLoadingDisplay.perform(null, null, -1);
    }

    private void onRetryButtonActionPerformed(final ActionEvent ae) {
        toUnloadingDisplayTriggerActionForLoadingDisplay.arguments.setNextLevelIdentifier(latestPlayedLevelIdentifier);
        toUnloadingDisplayTriggerActionForLoadingDisplay.perform(null, null, -1);
    }

    private void onMainMenuButtonActionPerformed(final ActionEvent ae) {
        toUnloadingDisplayTriggerActionForMainMenu.perform(null, null, -1);
    }

    private void onExitButtonActionPerformed(final ActionEvent ae) {
        showPanelInMainFrame(confirmExitMenuPanel);
    }

    private final UIPanel createConfirmExitMenuPanel() {
        final UIPanel confirmExitMenuPanel = new UIPanel(new RowLayout(false));
        final UILabel confirmLabel = new UILabel(localizedMessageProvider.getString("CONFIRM_EXIT"));
        final UIButton yesButton = new UIButton(localizedMessageProvider.getString("YES"));
        yesButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                onYesExitButtonActionPerformed(ae);
            }
        });
        final UIButton noButton = new UIButton(localizedMessageProvider.getString("NO"));
        noButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                onNoExitButtonActionPerformed(ae);
            }
        });
        confirmExitMenuPanel.add(confirmLabel);
        confirmExitMenuPanel.add(yesButton);
        confirmExitMenuPanel.add(noButton);
        return (confirmExitMenuPanel);
    }

    private void onYesExitButtonActionPerformed(final ActionEvent ae) {
        toUnloadingDisplayTriggerActionForExit.perform(null, null, -1);
    }

    private void onNoExitButtonActionPerformed(final ActionEvent ae) {
        showPanelInMainFrame(initialMenuPanel);
    }

    private final UIFrame createMainFrame() {
        final UIFrame mainFrame = new UIFrame("Game Over");
        mainFrame.setUseStandin(false);
        mainFrame.setOpacity(1f);
        mainFrame.setName("Pause Menu");
        mainFrame.setDecorated(false);
        return (mainFrame);
    }

    private final UIHud createHud() {
        final UIHud hud = new UIHud(canvas);
        hud.setupInput(physicalLayer, getLogicalLayer());
        getRoot().addController(new SpatialController<Node>() {
            @Override
            public final void update(final double time, final Node caller) {
                // updates the triggers of the hud
                hud.getLogicalLayer().checkTriggers(time);
            }
        });
        return (hud);
    }

    public void setLatestPlayedLevelIdentifier(final String latestPlayedLevelIdentifier) {
        this.latestPlayedLevelIdentifier = latestPlayedLevelIdentifier;
    }

    public void setLatestNextPlayableLevelIdentifier(final String latestNextPlayableLevelIdentifier) {
        this.latestNextPlayableLevelIdentifier = latestNextPlayableLevelIdentifier;
    }

    public void setGameStatistics(final GameStatistics gameStats) {
        this.gameStats = gameStats;
    }

    public void setObjectives(final List<Objective> objectives) {
        this.objectives = objectives;
    }

    @Override
    public void setEnabled(final boolean enabled) {
        final boolean wasEnabled = isEnabled();
        if (wasEnabled != enabled) {
            super.setEnabled(enabled);
            if (enabled) {
                mouseManager.setGrabbed(GrabbedState.NOT_GRABBED);
                // enables the "next" button if the latest next playable level
                // index seems valid
                // FIXME use the profile data to check if this level is unlocked
                ((UIButton) initialMenuPanel.getChild(0)).setEnabled(latestNextPlayableLevelIdentifier != null);
                // updates the main message
                final String text = computeMissionAndObjectivesText();
                textNode.setText(text);
                showPanelInMainFrame(initialMenuPanel);
            } else
                mouseManager.setGrabbed(GrabbedState.GRABBED);
        }
    }

    /**
     * Returns the text containing the status of the mission and the status of
     * each objective
     * 
     * @return text containing the status of the mission and the status of each
     *         objective
     */
    private String computeMissionAndObjectivesText() {
        final StringBuilder builder = new StringBuilder();
        switch (gameStats.getMissionStatus()) {
        case COMPLETED: {
            builder.append("You win\nMission status: completed");
            break;
        }
        case ABORTED: {
            builder.append("Game over\nMission status: aborted");
            break;
        }
        case DECEASED: {
            builder.append("Game over\nMission status: deceased");
            break;
        }
        case FAILED: {
            builder.append("Game over\nMission status: failed");
            break;
        }
        }
        if (objectives != null && !objectives.isEmpty()) {
            if (objectives.size() == 1)
                builder.append("\nObjective:");
            else
                builder.append("\nObjectives:");
            for (Objective objective : objectives) {
                builder.append("\n");
                builder.append(objective.getDescription());
                builder.append(": ");
                if (objective.getStatus(gameStats) == ObjectiveStatus.COMPLETED)
                    builder.append("COMPLETED");
                else {// the uncompleted objectives are treated as failed as it
                      // is too late to complete them
                    builder.append("FAILED");
                }
            }
        }
        return (builder.toString());
    }

    final void showPanelInMainFrame(final UIPanel panel) {
        mainFrame.setContentPanel(panel);
        mainFrame.updateMinimumSizeFromContents();
        mainFrame.layout();
        mainFrame.pack();
        mainFrame.centerOn(hud);
    }
}
