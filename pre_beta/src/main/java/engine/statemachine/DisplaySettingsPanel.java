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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Callable;

import com.ardor3d.extension.ui.UIButton;
import com.ardor3d.extension.ui.UIComboBox;
import com.ardor3d.extension.ui.UILabel;
import com.ardor3d.extension.ui.UIPanel;
import com.ardor3d.extension.ui.UIRadioButton;
import com.ardor3d.extension.ui.event.ActionEvent;
import com.ardor3d.extension.ui.event.ActionListener;
import com.ardor3d.extension.ui.event.SelectionListener;
import com.ardor3d.extension.ui.layout.RowLayout;
import com.ardor3d.extension.ui.model.DefaultComboBoxModel;
import com.ardor3d.extension.ui.util.ButtonGroup;
import com.ardor3d.framework.CanvasRenderer;
import com.ardor3d.framework.jogl.JoglNewtWindow;
import com.ardor3d.input.logical.TriggerAction;
import com.ardor3d.math.ColorRGBA;
import com.ardor3d.renderer.Camera;
import com.ardor3d.renderer.ContextCapabilities;
import com.ardor3d.renderer.ContextManager;
import com.ardor3d.renderer.RenderContext;
import com.ardor3d.util.GameTaskQueue;
import com.ardor3d.util.GameTaskQueueManager;
import com.jogamp.newt.MonitorDevice;
import com.jogamp.newt.MonitorMode;
import com.jogamp.newt.util.MonitorModeUtil;
import com.jogamp.opengl.JoglVersion;

import engine.misc.LocalizedMessageProvider;
import engine.misc.SettingsProvider;

/**
 * panel of the display settings (vertical synchronization, refresh rate, screen
 * resolution, ...)
 * 
 * @author Julien Gouesse
 *
 */
public class DisplaySettingsPanel extends UIPanel {

    private final MainMenuState mainMenuState;

    private final TriggerAction toggleScreenModeAction;

    private final SettingsProvider settingsProvider;

    private final DefaultComboBoxModel displayModesModel;

    private final UIComboBox displayModesCombo;

    private final HashMap<Integer, List<MonitorMode>> screenModesByRotation;

    public DisplaySettingsPanel(final MainMenuState mainMenuState, final TriggerAction toggleScreenModeAction,
            final LocalizedMessageProvider localizedMessageProvider, final SettingsProvider settingsProvider) {
        super();
        this.mainMenuState = mainMenuState;
        this.toggleScreenModeAction = toggleScreenModeAction;
        this.settingsProvider = settingsProvider;
        setLayout(new RowLayout(false));
        final UILabel screenModesLabel = new UILabel(localizedMessageProvider.getString("DISPLAY_MODE"));
        final MonitorDevice monitor = ((JoglNewtWindow) mainMenuState.canvas).getNewtWindow().getScreen()
                .getPrimaryMonitor();
        final MonitorMode currentScreenMode = monitor.getCurrentMode();
        final List<MonitorMode> screenModes = monitor.getSupportedModes();
        screenModesByRotation = new HashMap<>();
        final int[] rotations = new int[] { 0, 90, 180, 270 };
        final ArrayList<Integer> availableRotations = new ArrayList<>();
        for (int rotation : rotations) {
            final List<MonitorMode> rotatedScreenModes = MonitorModeUtil.filterByRotation(screenModes, rotation);
            if (rotatedScreenModes != null && !rotatedScreenModes.isEmpty()) {
                screenModesByRotation.put(Integer.valueOf(rotation), rotatedScreenModes);
                availableRotations.add(Integer.valueOf(rotation));
            }
        }
        final int selectedScreenRotation = currentScreenMode.getRotation();
        final UIPanel rotationsPanel;
        final UIRadioButton[] rotationsButtons;
        if (availableRotations.size() > 1) {
            rotationsPanel = new UIPanel(new RowLayout(true));
            final UILabel screenRotationsLabel = new UILabel(localizedMessageProvider.getString("SCREEN_ROTATION"));
            rotationsPanel.add(screenRotationsLabel);
            final ButtonGroup rotationsGroup = new ButtonGroup();
            rotationsButtons = new UIRadioButton[availableRotations.size()];
            int availableRotationIndex = 0;
            for (Integer availableRotation : availableRotations) {
                rotationsButtons[availableRotationIndex] = new UIRadioButton(availableRotation.toString());
                rotationsGroup.add(rotationsButtons[availableRotationIndex]);
                rotationsPanel.add(rotationsButtons[availableRotationIndex]);
                if (availableRotation.intValue() == selectedScreenRotation)
                    rotationsButtons[availableRotationIndex].setSelected(true);
                availableRotationIndex++;
            }
        } else {
            rotationsButtons = null;
            rotationsPanel = null;
        }
        int selectedScreenModeIndex = -1, screenModeIndex = 0;
        final List<MonitorMode> currentScreenModes = screenModesByRotation.get(Integer.valueOf(selectedScreenRotation));
        for (MonitorMode screenMode : currentScreenModes) {
            if (screenMode == currentScreenMode) {
                selectedScreenModeIndex = screenModeIndex;
                break;
            }
            screenModeIndex++;
        }
        final Object[] screenModesArray = currentScreenModes.toArray();
        displayModesModel = new DefaultComboBoxModel(screenModesArray);
        displayModesCombo = new UIComboBox(displayModesModel);
        if (selectedScreenModeIndex != -1)
            displayModesCombo.setSelectedIndex(selectedScreenModeIndex, false);
        displayModesCombo.addSelectionListener(new SelectionListener<UIComboBox>() {
            @Override
            public void selectionChanged(final UIComboBox component, final Object newValue) {
                onDisplayModesComboSelectionChanged(component, newValue);
            }
        });
        if (rotationsButtons != null) {
            for (UIRadioButton rotationButton : rotationsButtons) {
                rotationButton.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent ae) {
                        onRotationButtonActionPerformed(ae);
                    }
                });
            }
        }
        final UIButton windowingModeButton = new UIButton(
                localizedMessageProvider.getString("SWITCH_TO_WINDOWED_MODE_OR_FULL_SCREEN_MODE"));
        windowingModeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                onWindowingModeButtonActionPerformed(event);
            }
        });
        final UIPanel vSyncPanel = new UIPanel(new RowLayout(true));
        final ButtonGroup vSyncGroup = new ButtonGroup();
        final UILabel vSyncLabel = new UILabel(localizedMessageProvider.getString("VERTICAL_SYNCHRONIZATION"));
        final UIRadioButton enableVSyncButton = new UIRadioButton(localizedMessageProvider.getString("ON"));
        enableVSyncButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                onEnableVSyncButtonActionPerformed(event);
            }
        });
        final UIRadioButton disableVSyncButton = new UIRadioButton(localizedMessageProvider.getString("OFF"));
        disableVSyncButton.setSelected(true);
        disableVSyncButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                onDisableVSyncButtonActionPerformed(event);
            }
        });
        vSyncGroup.add(enableVSyncButton);
        vSyncGroup.add(disableVSyncButton);
        vSyncPanel.add(vSyncLabel);
        vSyncPanel.add(enableVSyncButton);
        vSyncPanel.add(disableVSyncButton);
        final UIButton backButton = new UIButton(localizedMessageProvider.getString("BACK"));
        backButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                onBackButtonActionPerformed(event);
            }
        });
        final ContextCapabilities caps = ContextManager.getCurrentContext().getCapabilities();
        final UILabel firstEmptyLabel = new UILabel(" ");
        final UILabel crappyDriverWarningLabel = new UILabel(" ");
        // checks whether Microsoft’s generic software emulation driver (OpenGL
        // emulation through Direct3D) is installed
        if (caps.getDisplayVendor().equalsIgnoreCase("Microsoft Corporation")
                && caps.getDisplayRenderer().equalsIgnoreCase("GDI Generic")) {// recommends
                                                                               // to
                                                                               // the
                                                                               // end
                                                                               // user
                                                                               // to
                                                                               // install
                                                                               // a
                                                                               // proper
                                                                               // OpenGL
                                                                               // driver
                                                                               // instead
                                                                               // of
                                                                               // this
                                                                               // crap
            final String warning = "The game might crash or run very slowly with your broken OpenGL driver. To resolve this problem, please download and install the latest version of your graphical card's driver from the your graphical card manufacturer (Nvidia, ATI, Intel).";
            crappyDriverWarningLabel.setText(warning);
            crappyDriverWarningLabel.setForegroundColor(ColorRGBA.RED);
        }
        final UIPanel openglGraphicalCardDriverInfoPanel = new UIPanel(new RowLayout(false));
        final UILabel openglDriverLabel = new UILabel(localizedMessageProvider.getString("OPENGL_DRIVER"));
        final UILabel vendorLabel = new UILabel(
                localizedMessageProvider.getString("VENDOR") + ": " + caps.getDisplayVendor());
        final UILabel rendererLabel = new UILabel(
                localizedMessageProvider.getString("RENDERER") + ": " + caps.getDisplayRenderer());
        final UILabel versionLabel = new UILabel(
                localizedMessageProvider.getString("VERSION") + ": " + caps.getDisplayVersion());
        final UILabel joglVersionLabel = new UILabel(localizedMessageProvider.getString("JOGL_VERSION") + ": "
                + JoglVersion.getInstance().getImplementationVersion());
        openglGraphicalCardDriverInfoPanel.add(firstEmptyLabel);
        openglGraphicalCardDriverInfoPanel.add(openglDriverLabel);
        openglGraphicalCardDriverInfoPanel.add(vendorLabel);
        openglGraphicalCardDriverInfoPanel.add(rendererLabel);
        openglGraphicalCardDriverInfoPanel.add(versionLabel);
        openglGraphicalCardDriverInfoPanel.add(crappyDriverWarningLabel);
        openglGraphicalCardDriverInfoPanel.add(joglVersionLabel);
        add(windowingModeButton);
        add(vSyncPanel);
        add(screenModesLabel);
        add(displayModesCombo);
        if (rotationsPanel != null)
            add(rotationsPanel);
        add(openglGraphicalCardDriverInfoPanel);
        add(backButton);
    }

    private void onRotationButtonActionPerformed(final ActionEvent ae) {
        final Integer selectedRotation = Integer.valueOf(Integer.parseInt(((UIRadioButton) ae.getSource()).getText()));
        displayModesCombo.setSelectedIndex(-1, false);
        displayModesModel.clear();
        final MonitorDevice monitor = ((JoglNewtWindow) mainMenuState.canvas).getNewtWindow().getScreen()
                .getPrimaryMonitor();
        final MonitorMode freshCurrentScreenMode = monitor.getCurrentMode();
        MonitorMode chosenRotatedMonitorMode = null;
        for (MonitorMode rotatedScreenMode : screenModesByRotation.get(selectedRotation)) {
            displayModesModel.addItem(rotatedScreenMode);
            if (rotatedScreenMode.getSurfaceSize().getResolution().getWidth() == freshCurrentScreenMode.getSurfaceSize()
                    .getResolution().getWidth()
                    && rotatedScreenMode.getSurfaceSize().getResolution().getHeight() == freshCurrentScreenMode
                            .getSurfaceSize().getResolution().getHeight()
                    && rotatedScreenMode.getRefreshRate() == monitor.getCurrentMode().getRefreshRate()) {
                chosenRotatedMonitorMode = rotatedScreenMode;
                break;
            }
        }
        if (chosenRotatedMonitorMode != null) {
            setScreenMode(chosenRotatedMonitorMode);
            displayModesCombo.setSelectedIndex(
                    screenModesByRotation.get(selectedRotation).indexOf(monitor.getCurrentMode()), false);
            settingsProvider.setScreenRotation(selectedRotation.intValue());
        }
    }

    private void onWindowingModeButtonActionPerformed(final ActionEvent event) {
        final boolean fullscreenEnabled = !((JoglNewtWindow) mainMenuState.canvas).getNewtWindow().isFullscreen();
        toggleScreenModeAction.perform(mainMenuState.canvas, null, Double.NaN);
        settingsProvider.setFullscreenEnabled(fullscreenEnabled);
    }

    private void onEnableVSyncButtonActionPerformed(final ActionEvent event) {
        onVSyncChanged(true);
    }

    private void onDisableVSyncButtonActionPerformed(final ActionEvent event) {
        onVSyncChanged(false);
    }

    private void onVSyncChanged(final boolean vsyncEnabled) {
        mainMenuState.canvas.setVSyncEnabled(vsyncEnabled);
        settingsProvider.setVerticalSynchronizationEnabled(vsyncEnabled);
    }

    private void onBackButtonActionPerformed(final ActionEvent event) {
        mainMenuState.showPanelInMainFrame(mainMenuState.optionsMenuPanel);
    }

    private void onDisplayModesComboSelectionChanged(final UIComboBox component, final Object newValue) {
        final MonitorMode monitorMode = (MonitorMode) newValue;
        setScreenMode(monitorMode);
        settingsProvider.setScreenWidth(monitorMode.getSurfaceSize().getResolution().getWidth());
        settingsProvider.setScreenHeight(monitorMode.getSurfaceSize().getResolution().getHeight());
    }

    private void setScreenMode(final MonitorMode monitorMode) {
        final MonitorDevice monitor = ((JoglNewtWindow) mainMenuState.canvas).getNewtWindow().getScreen()
                .getPrimaryMonitor();
        if (monitor.setCurrentMode(monitorMode))
            updateUiLocationOnCameraChange(monitorMode.getRotatedWidth(), monitorMode.getRotatedHeight(), 5);
    }

    private void updateUiLocationOnCameraChange(final int width, final int height, final int recurse) {
        // the camera is going to take into account the change of resolution
        // very soon, the update of the UI must be done later
        final CanvasRenderer canvasRenderer = mainMenuState.canvas.getCanvasRenderer();
        final RenderContext renderContext = canvasRenderer.getRenderContext();
        GameTaskQueueManager.getManager(renderContext).getQueue(GameTaskQueue.RENDER).enqueue(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                if (canvasRenderer.getCamera().getWidth() == width
                        && canvasRenderer.getCamera().getHeight() == height) {
                    final Camera cam = mainMenuState.canvas.getCanvasRenderer().getCamera();
                    mainMenuState.mainFrame.setLocationRelativeTo(cam);
                    if (recurse > 0) {// some operating systems (especially
                                      // Microsoft Windows) may require several
                                      // attempts...
                        updateUiLocationOnCameraChange(width, height, recurse - 1);
                    }
                } else
                    updateUiLocationOnCameraChange(width, height, recurse);
                return null;
            }
        });
    }
}
