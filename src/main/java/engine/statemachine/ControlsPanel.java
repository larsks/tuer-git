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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Predicate;

import com.ardor3d.extension.ui.Orientation;
import com.ardor3d.extension.ui.UIButton;
import com.ardor3d.extension.ui.UICheckBox;
import com.ardor3d.extension.ui.UILabel;
import com.ardor3d.extension.ui.UIPanel;
import com.ardor3d.extension.ui.UISlider;
import com.ardor3d.extension.ui.event.ActionEvent;
import com.ardor3d.extension.ui.event.ActionListener;
import com.ardor3d.extension.ui.layout.RowLayout;
import com.ardor3d.framework.Canvas;
import com.ardor3d.input.Key;
import com.ardor3d.input.MouseButton;
import com.ardor3d.input.logical.AnyKeyCondition;
import com.ardor3d.input.logical.InputTrigger;
import com.ardor3d.input.logical.MouseButtonClickedCondition;
import com.ardor3d.input.logical.TriggerAction;
import com.ardor3d.input.logical.TwoInputStates;

import engine.input.Action;
import engine.input.ActionMap;
import engine.input.MouseAndKeyboardSettings;
import engine.input.MouseWheelMovedDownCondition;
import engine.input.MouseWheelMovedUpCondition;
import engine.misc.LocalizedMessageProvider;

/**
 * Panel used to modify the controls
 * 
 * @author Julien Gouesse
 *
 */
public final class ControlsPanel extends UIPanel {

    private Action latestEditedAction;

    private final HashSet<InputTrigger> previousTriggers;

    private final MainMenuState mainMenuState;

    private final ActionMap defaultActionMap;

    private final ActionMap customActionMap;

    private final MouseAndKeyboardSettings defaultMouseAndKeyboardSettings;

    private final MouseAndKeyboardSettings customMouseAndKeyboardSettings;

    private final HashMap<Action, UILabel> actionsLabelsMap;

    public ControlsPanel(final MainMenuState mainMenuState, final ActionMap defaultActionMap,
            final ActionMap customActionMap, final MouseAndKeyboardSettings defaultMouseAndKeyboardSettings,
            final MouseAndKeyboardSettings customMouseAndKeyboardSettings,
            final LocalizedMessageProvider localizedMessageProvider) {
        super();
        setLayout(new RowLayout(false));
        previousTriggers = new HashSet<>();
        this.mainMenuState = mainMenuState;
        this.defaultActionMap = defaultActionMap;
        this.customActionMap = customActionMap;
        this.defaultMouseAndKeyboardSettings = defaultMouseAndKeyboardSettings;
        this.customMouseAndKeyboardSettings = customMouseAndKeyboardSettings;
        latestEditedAction = null;
        actionsLabelsMap = new HashMap<>();
        add(new UILabel(localizedMessageProvider.getString("CONTROLS")));
        final UIPanel actionPanel = new UIPanel(new RowLayout(true));
        final UIPanel actionsButtonsPanel = new UIPanel(new RowLayout(false));
        final UIPanel actionsLabelsPanel = new UIPanel(new RowLayout(false));
        for (Action action : Action.values()) {
            final String actionName = action.name().replace('_', ' ').toLowerCase();
            final UIButton actionButton = new UIButton(actionName);
            actionButton.setUserData(action);
            actionButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent ae) {
                    actionButtonActionPerformed(ae);
                }
            });
            final UILabel actionLabel = new UILabel("");
            actionsLabelsMap.put(action, actionLabel);
            actionsButtonsPanel.add(actionButton);
            actionsLabelsPanel.add(actionLabel);
        }
        actionPanel.add(actionsButtonsPanel);
        actionPanel.add(actionsLabelsPanel);
        add(actionPanel);
        final UIPanel mouseRotateSpeedPanel = new UIPanel(new RowLayout(true));
        final int initialMouseRotateSliderValue = fromMouseRotationModelToSliderModel(
                customMouseAndKeyboardSettings.getMouseRotateSpeed());
        final UISlider mouseRotateSpeedSlider = new UISlider(Orientation.Horizontal, 1, 100,
                initialMouseRotateSliderValue);
        mouseRotateSpeedSlider.setSnapToValues(true);
        mouseRotateSpeedSlider.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                onMouseRotateSpeedSliderActionPerformed(ae);
            }
        });
        final UILabel mouseRotateSpeedLabel = new UILabel(localizedMessageProvider.getString("MOUSE_ROTATION_SPEED"));
        mouseRotateSpeedPanel.add(mouseRotateSpeedSlider);
        mouseRotateSpeedPanel.add(mouseRotateSpeedLabel);
        add(mouseRotateSpeedPanel);
        final UIPanel keyRotateSpeedPanel = new UIPanel(new RowLayout(true));
        final int initialkeyRotateSliderValue = fromKeyRotationModelToSliderModel(
                customMouseAndKeyboardSettings.getKeyRotateSpeed());
        final UISlider keyRotateSpeedSlider = new UISlider(Orientation.Horizontal, 1, 100, initialkeyRotateSliderValue);
        keyRotateSpeedSlider.setSnapToValues(true);
        keyRotateSpeedSlider.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                onKeyRotateSpeedSliderActionPerformed(ae);
            }
        });
        final UILabel keyRotateSpeedLabel = new UILabel(localizedMessageProvider.getString("KEY_ROTATION_SPEED"));
        keyRotateSpeedPanel.add(keyRotateSpeedSlider);
        keyRotateSpeedPanel.add(keyRotateSpeedLabel);
        add(keyRotateSpeedPanel);

        final UICheckBox lookUpDownReversedCheckBox = new UICheckBox(
                localizedMessageProvider.getString("REVERSE_LOOK_UP_DOWN"));
        lookUpDownReversedCheckBox.setSelected(customMouseAndKeyboardSettings.isLookUpDownReversed());
        lookUpDownReversedCheckBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                onLookUpDownReversedCheckBoxActionPerformed(ae);
            }
        });
        add(lookUpDownReversedCheckBox);
        final UICheckBox mousePointerNeverHiddenCheckBox = new UICheckBox(
                localizedMessageProvider.getString("NEVER_HIDE_THE_MOUSE_POINTER_FOR_DEBUG_PURPOSES_ONLY"));
        mousePointerNeverHiddenCheckBox.setSelected(customMouseAndKeyboardSettings.isMousePointerNeverHidden());
        mousePointerNeverHiddenCheckBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                onMousePointerNeverHiddenCheckBoxActionPerformed(ae);
            }
        });
        add(mousePointerNeverHiddenCheckBox);
        update();
        final UIButton resetToDefaultsButton = new UIButton(localizedMessageProvider.getString("RESET_TO_DEFAULTS"));
        resetToDefaultsButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                resetToDefaultsButtonActionPerformed(ae);
                update();
            }
        });
        final UIButton backButton = new UIButton(localizedMessageProvider.getString("BACK"));
        backButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                backButtonActionPerformed(ae);
            }
        });
        add(backButton);
    }

    protected double fromSliderModelToMouseRotationModel(final int value) {
        final double mouseRotationModelValue = value / 1000.0d;
        return (mouseRotationModelValue);
    }

    protected int fromMouseRotationModelToSliderModel(final double value) {
        final int sliderModelValue = (int) Math.round(value * 1000.0d);
        return (sliderModelValue);
    }

    protected void onMouseRotateSpeedSliderActionPerformed(final ActionEvent ae) {
        final int sliderModelValue = ((UISlider) ae.getSource()).getValue();
        final double mouseRotationModelValue = fromSliderModelToMouseRotationModel(sliderModelValue);
        customMouseAndKeyboardSettings.setMouseRotateSpeed(mouseRotationModelValue);
    }

    protected double fromSliderModelToKeyRotationModel(final int value) {
        final double mouseRotationModelValue = value / 10.0d;
        return (mouseRotationModelValue);
    }

    protected int fromKeyRotationModelToSliderModel(final double value) {
        final int sliderModelValue = (int) Math.round(value * 10.0d);
        return (sliderModelValue);
    }

    protected void onKeyRotateSpeedSliderActionPerformed(final ActionEvent ae) {
        final int sliderModelValue = ((UISlider) ae.getSource()).getValue();
        final double keyRotationModelValue = fromSliderModelToKeyRotationModel(sliderModelValue);
        customMouseAndKeyboardSettings.setKeyRotateSpeed(keyRotationModelValue);
    }

    protected void onLookUpDownReversedCheckBoxActionPerformed(final ActionEvent ae) {
        customMouseAndKeyboardSettings.setLookUpDownReversed(((UICheckBox) ae.getSource()).isSelected());
    }

    protected void onMousePointerNeverHiddenCheckBoxActionPerformed(final ActionEvent ae) {
        customMouseAndKeyboardSettings.setMousePointerNeverHidden(((UICheckBox) ae.getSource()).isSelected());
    }

    private void update() {
        final StringBuilder controlsContentBuilder = new StringBuilder("");
        for (Action action : Action.values()) {
            final Set<ActionMap.Input> inputs = customActionMap.getInputs(action);
            for (ActionMap.Input input : inputs)
                controlsContentBuilder.append(input).append(", ");
            if (controlsContentBuilder.charAt(controlsContentBuilder.length() - 2) == ',')
                controlsContentBuilder.delete(controlsContentBuilder.length() - 2, controlsContentBuilder.length());
            // this is the only way of forcing a minimum size whatever the
            // content of the label
            while (controlsContentBuilder.length() < 100)
                controlsContentBuilder.append(' ');
            final String actionText = controlsContentBuilder.toString();
            final UILabel actionLabel = actionsLabelsMap.get(action);
            actionLabel.setText(actionText);
            controlsContentBuilder.delete(0, controlsContentBuilder.length());
        }
    }

    private void actionButtonActionPerformed(ActionEvent ae) {
        final Action action = (Action) ae.getSource().getUserData();
        latestEditedAction = action;
    }

    private void resetToDefaultsButtonActionPerformed(ActionEvent ae) {
        customActionMap.set(defaultActionMap);
        customMouseAndKeyboardSettings.set(defaultMouseAndKeyboardSettings);
    }

    @Override
    public void attachedToHud() {
        super.attachedToHud();
        latestEditedAction = null;
        previousTriggers.clear();
        // saves all triggers for further use
        previousTriggers.addAll(mainMenuState.getLogicalLayer().getTriggers());
        // unregisters all triggers of this logical layer
        for (InputTrigger trigger : previousTriggers)
            mainMenuState.getLogicalLayer().deregisterTrigger(trigger);
        final TriggerAction changeKeyControlAction = new TriggerAction() {
            @Override
            public void perform(Canvas source, TwoInputStates inputState, double tpf) {
                final Key key = inputState.getCurrent().getKeyboardState().getKeyEvent().getKey();
                addKeyBinding(key);
            }
        };
        final InputTrigger changeKeyBindingTrigger = new InputTrigger(new AnyKeyCondition(), changeKeyControlAction);
        final TriggerAction changeMouseButtonControlAction = new TriggerAction() {
            @Override
            public void perform(Canvas source, TwoInputStates inputState, double tpf) {
                final MouseButton mouseButton = inputState.getCurrent().getMouseState().getButtonsClicked().iterator()
                        .next();
                addMouseButtonBinding(mouseButton);
            }
        };
        final Predicate<TwoInputStates> anyMouseButtonClickedCondition = 
                new MouseButtonClickedCondition(MouseButton.LEFT).or(new MouseButtonClickedCondition(MouseButton.RIGHT)).or(
                        new MouseButtonClickedCondition(MouseButton.MIDDLE));
        final InputTrigger changeMouseButtonBindingTrigger = new InputTrigger(anyMouseButtonClickedCondition,
                changeMouseButtonControlAction);
        final TriggerAction changeMouseWheelControlAction = new TriggerAction() {
            @Override
            public void perform(Canvas source, TwoInputStates inputState, double tpf) {
                final Boolean mouseWheelUpFlag = inputState.getCurrent().getMouseState().getDwheel() > 0 ? Boolean.TRUE
                        : Boolean.FALSE;
                addMouseWheelBinding(mouseWheelUpFlag);
            }
        };
        final Predicate<TwoInputStates> mouseWheelUpOrDownCondition = new MouseWheelMovedUpCondition().or(new MouseWheelMovedDownCondition());
        final InputTrigger changeMouseWheelBindingTrigger = new InputTrigger(mouseWheelUpOrDownCondition,
                changeMouseWheelControlAction);
        // registers the triggers of this panel in the logical layer
        mainMenuState.getLogicalLayer().registerTrigger(changeKeyBindingTrigger);
        mainMenuState.getLogicalLayer().registerTrigger(changeMouseButtonBindingTrigger);
        mainMenuState.getLogicalLayer().registerTrigger(changeMouseWheelBindingTrigger);
    }

    private void addKeyBinding(final Key key) {
        if (latestEditedAction != null) {
            customActionMap.setKeyActionBinding(latestEditedAction, key);
            // updates the UI
            update();
        }
    }

    private void addMouseButtonBinding(final MouseButton mouseButton) {
        if (latestEditedAction != null) {
            customActionMap.setMouseButtonActionBinding(latestEditedAction, mouseButton);
            // updates the UI
            update();
        }
    }

    private void addMouseWheelBinding(final Boolean mouseWheelUpFlag) {
        if (latestEditedAction != null) {
            customActionMap.setMouseWheelMoveActionBinding(latestEditedAction, mouseWheelUpFlag);
            // updates the UI
            update();
        }
    }

    @Override
    public void detachedFromHud() {
        super.detachedFromHud();
        latestEditedAction = null;
        // unregisters all triggers of this logical layer
        final Set<InputTrigger> triggers = new HashSet<>(mainMenuState.getLogicalLayer().getTriggers());
        for (InputTrigger trigger : triggers)
            mainMenuState.getLogicalLayer().deregisterTrigger(trigger);
        // registers all triggers that were in this logical layer before the
        // attachment of this panel to the current HUD
        for (InputTrigger trigger : previousTriggers)
            mainMenuState.getLogicalLayer().registerTrigger(trigger);
        previousTriggers.clear();
    }

    private void backButtonActionPerformed(ActionEvent ae) {
        latestEditedAction = null;
        mainMenuState.showPanelInMainFrame(mainMenuState.optionsMenuPanel);
    }
}