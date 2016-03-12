/**
 * Copyright (c) 2006-2016 Julien Gouesse
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
import engine.misc.FontStore;
import engine.misc.LocalizedMessageProvider;
import engine.sound.SoundManager;

/**
 * State for the in-game pause menu
 * 
 * @author Julien Gouesse
 *
 */
public class PauseMenuState extends ScenegraphState {

    private final NativeCanvas canvas;

    private final PhysicalLayer physicalLayer;

    private final MouseManager mouseManager;

    private final TransitionTriggerAction<ScenegraphState, String> toGameTriggerAction;

    private final TransitionTriggerAction<ScenegraphState, String> toGameOverTriggerAction;

    private final TransitionTriggerAction<ScenegraphState, String> toUnloadingDisplayTriggerAction;

    private final UIFrame mainFrame;

    private final UIPanel initialMenuPanel;
    // TODO move this panel into a separate internal class
    private final UIPanel objectivesMenuPanel;

    private final UIButton objectivesButton;

    private final UIButton objectivesBackButton;

    private final UIPanel confirmAbortMenuPanel;

    private final UIPanel confirmExitMenuPanel;

    private boolean openedForExitConfirm = false;

    private String latestPlayedLevelIdentifier;

    private String latestNextPlayableLevelIdentifier;

    private final LocalizedMessageProvider localizedMessageProvider;

    private GameStatistics gameStats;

    private List<Objective> objectives;

    public PauseMenuState(final NativeCanvas canvas, final PhysicalLayer physicalLayer, final MouseManager mouseManager,
            final TransitionTriggerAction<ScenegraphState, String> toGameTriggerAction,
            final TransitionTriggerAction<ScenegraphState, String> toGameOverTriggerAction,
            final TransitionTriggerAction<ScenegraphState, String> toUnloadingDisplayTriggerAction,
            final SoundManager soundManager, final FontStore fontStore,
            final LocalizedMessageProvider localizedMessageProvider) {
        super(soundManager);
        this.canvas = canvas;
        this.physicalLayer = physicalLayer;
        this.mouseManager = mouseManager;
        this.toGameTriggerAction = toGameTriggerAction;
        this.toGameOverTriggerAction = toGameOverTriggerAction;
        this.toUnloadingDisplayTriggerAction = toUnloadingDisplayTriggerAction;
        this.latestPlayedLevelIdentifier = null;
        this.latestNextPlayableLevelIdentifier = null;
        this.localizedMessageProvider = localizedMessageProvider;
        objectivesMenuPanel = createObjectivesMenuPanel();
        initialMenuPanel = createInitialMenuPanel();
        objectivesButton = (UIButton) initialMenuPanel.getChild(0);
        objectivesBackButton = (UIButton) objectivesMenuPanel.getChild(0);
        confirmAbortMenuPanel = createConfirmAbortMenuPanel();
        confirmExitMenuPanel = createConfirmExitMenuPanel();
        // creates the main frame
        mainFrame = createMainFrame();
        // creates the head-up display
        final UIHud hud = createHud();
        hud.add(mainFrame);
        getRoot().attachChild(hud);
        // adds some text
        final BMText textNode = new BMText("gamePauseNode", localizedMessageProvider.getString("GAME_PAUSED"),
                fontStore.getFontsList().get(2), BMText.Align.Center, BMText.Justify.Center);
        textNode.setFontScale(10);
        textNode.setTextColor(ColorRGBA.RED);
        textNode.setTranslation(textNode.getTranslation().add(0, 3.3, 0, null));
        getRoot().attachChild(textNode);
    }

    private final UIPanel createObjectivesMenuPanel() {
        final UIPanel objectivesMenuPanel = new UIPanel(new RowLayout(false));
        final UIButton backButton = new UIButton(localizedMessageProvider.getString("BACK"));
        backButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                showPanelInMainFrame(initialMenuPanel);
            }
        });
        objectivesMenuPanel.add(backButton);
        return (objectivesMenuPanel);
    }

    private final UIPanel createInitialMenuPanel() {
        final UIPanel initialMenuPanel = new UIPanel(new RowLayout(false));
        final UIButton objectivesButton = new UIButton(localizedMessageProvider.getString("OBJECTIVES"));
        objectivesButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                onObjectivesButtonActionPerformed(ae);
            }
        });
        final UIButton resumeButton = new UIButton(localizedMessageProvider.getString("RESUME"));
        resumeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                onResumeButtonActionPerformed(ae);
            }
        });
        final UIButton abortButton = new UIButton(localizedMessageProvider.getString("ABORT"));
        abortButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                onAbortButtonActionPerformed(ae);
            }
        });
        final UIButton exitButton = new UIButton(localizedMessageProvider.getString("EXIT"));
        exitButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                onExitButtonActionPerformed(ae);
            }
        });
        initialMenuPanel.add(objectivesButton);
        initialMenuPanel.add(resumeButton);
        initialMenuPanel.add(abortButton);
        initialMenuPanel.add(exitButton);
        return (initialMenuPanel);
    }

    private final UIPanel createConfirmAbortMenuPanel() {
        final UIPanel confirmAbortMenuPanel = new UIPanel(new RowLayout(false));
        final UILabel confirmLabel = new UILabel(localizedMessageProvider.getString("CONFIRM_ABORT"));
        final UIButton yesButton = new UIButton(localizedMessageProvider.getString("YES"));
        yesButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                onYesAbortButtonActionPerformed(ae);
            }
        });
        final UIButton noButton = new UIButton(localizedMessageProvider.getString("NO"));
        noButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                onNoAbortButtonActionPerformed(ae);
            }
        });
        confirmAbortMenuPanel.add(confirmLabel);
        confirmAbortMenuPanel.add(yesButton);
        confirmAbortMenuPanel.add(noButton);
        return (confirmAbortMenuPanel);
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

    private void onObjectivesButtonActionPerformed(final ActionEvent ae) {
        showPanelInMainFrame(objectivesMenuPanel);
    }

    private void onResumeButtonActionPerformed(final ActionEvent ae) {
        toGameTriggerAction.perform(null, null, -1);
    }

    private void onAbortButtonActionPerformed(final ActionEvent ae) {
        showPanelInMainFrame(confirmAbortMenuPanel);
    }

    private void onExitButtonActionPerformed(final ActionEvent ae) {
        showPanelInMainFrame(confirmExitMenuPanel);
    }

    private void onYesAbortButtonActionPerformed(final ActionEvent ae) {
        // passes the objectives but the mission is aborted anyway
        gameStats.setMissionStatus(MissionStatus.ABORTED);
        toGameOverTriggerAction.arguments.setPreviousLevelIdentifier(latestPlayedLevelIdentifier);
        toGameOverTriggerAction.arguments.setNextLevelIdentifier(latestNextPlayableLevelIdentifier);
        toGameOverTriggerAction.arguments.setGameStatistics(gameStats);
        toGameOverTriggerAction.arguments.setObjectives(objectives);
        toGameOverTriggerAction.perform(null, null, -1);
    }

    private void onNoAbortButtonActionPerformed(final ActionEvent ae) {
        showPanelInMainFrame(initialMenuPanel);
    }

    private void onYesExitButtonActionPerformed(final ActionEvent ae) {
        toUnloadingDisplayTriggerAction.perform(null, null, -1);
    }

    private void onNoExitButtonActionPerformed(final ActionEvent ae) {
        showPanelInMainFrame(initialMenuPanel);
    }

    private final UIHud createHud() {
        final UIHud hud = new UIHud();
        hud.setupInput(canvas, physicalLayer, getLogicalLayer());
        getRoot().addController(new SpatialController<Node>() {
            @Override
            public final void update(final double time, final Node caller) {
                // updates the triggers of the hud
                hud.getLogicalLayer().checkTriggers(time);
            }
        });
        return (hud);
    }

    private final UIFrame createMainFrame() {
        final UIFrame mainFrame = new UIFrame("Pause Menu");
        mainFrame.setUseStandin(false);
        mainFrame.setOpacity(1f);
        mainFrame.setName("Pause Menu");
        mainFrame.setDecorated(false);
        return (mainFrame);
    }

    public void setOpenedForExitConfirm(final boolean openedForExitConfirm) {
        this.openedForExitConfirm = openedForExitConfirm;
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
                // removes all components from the objectives panel
                objectivesMenuPanel.removeAllComponents();
                if (objectives == null || objectives.isEmpty()) {// disables
                                                                 // this button
                                                                 // as there is
                                                                 // no objective
                                                                 // to show
                    objectivesButton.setEnabled(false);
                } else {
                    objectivesButton.setEnabled(true);
                    // updates the objectives
                    final String objectivesText = computeObjectivesText();
                    objectivesMenuPanel.add(new UILabel(objectivesText));
                }
                // adds the back button into the objectives button after the
                // objective(s)
                objectivesMenuPanel.add(objectivesBackButton);
                // if the end user arrives here after pressing ESC, it shows the
                // exit confirm panel
                if (openedForExitConfirm) {
                    showPanelInMainFrame(confirmExitMenuPanel);
                    openedForExitConfirm = false;
                } else
                    showPanelInMainFrame(initialMenuPanel);
            } else
                mouseManager.setGrabbed(GrabbedState.GRABBED);
        }
    }

    private String computeObjectivesText() {
        final StringBuilder builder = new StringBuilder();
        if (objectives != null && !objectives.isEmpty()) {
            if (objectives.size() == 1)
                builder.append("Objective:");
            else
                builder.append("Objectives:");
            for (Objective objective : objectives) {
                builder.append("\n");
                builder.append(objective.getDescription());
                builder.append(": ");
                builder.append(objective.getStatus(gameStats).toString());
            }
        }
        return (builder.toString());
    }

    final void showPanelInMainFrame(final UIPanel panel) {
        mainFrame.setContentPanel(panel);
        mainFrame.updateMinimumSizeFromContents();
        mainFrame.layout();
        mainFrame.pack();
        mainFrame.setLocationRelativeTo(canvas.getCanvasRenderer().getCamera());
    }
}
