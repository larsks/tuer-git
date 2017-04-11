/**
 * Copyright (c) 2006-2017 Julien Gouesse
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
package engine.input;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Set;
import com.ardor3d.input.Key;
import com.ardor3d.input.MouseButton;
import com.ardor3d.input.logical.KeyPressedCondition;
import com.ardor3d.input.logical.KeyReleasedCondition;
import com.ardor3d.input.logical.MouseButtonPressedCondition;
import com.ardor3d.input.logical.MouseButtonReleasedCondition;
import com.ardor3d.input.logical.TriggerConditions;
import com.ardor3d.input.logical.TwoInputStates;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;

public class ActionMap implements Cloneable {

    private final HashMap<Action, HashSet<Input>> internalActionMap;

    public ActionMap() {
        internalActionMap = new HashMap<>();
    }

    @Override
    public ActionMap clone() {
        final ActionMap clone = new ActionMap();
        clone.set(this);
        return (clone);
    }

    public void set(final ActionMap actionMap) {
        internalActionMap.clear();
        if (actionMap != null) {
            for (Entry<Action, HashSet<Input>> actionInputEntry : actionMap.internalActionMap.entrySet()) {
                final Action action = actionInputEntry.getKey();
                final HashSet<Input> inputs = actionInputEntry.getValue();
                final HashSet<Input> inputsCopy = new HashSet<>(inputs);
                internalActionMap.put(action, inputsCopy);
            }
        }
    }

    @Override
    public int hashCode() {
        return (System.identityHashCode(this));
    }

    @Override
    public boolean equals(Object o) {
        boolean result;
        if (o == null || !(o instanceof ActionMap))
            result = false;
        else {
            result = true;
            final ActionMap that = (ActionMap) o;
            for (Entry<Action, HashSet<Input>> actionInputEntry : internalActionMap.entrySet()) {
                final Action action = actionInputEntry.getKey();
                final HashSet<Input> inputs = actionInputEntry.getValue();
                final int inputCount = inputs == null ? 0 : inputs.size();
                final HashSet<Input> thatInputs = that.internalActionMap.get(action);
                final int thatInputCount = thatInputs == null ? 0 : thatInputs.size();
                if (inputCount == thatInputCount) {
                    if (inputCount > 0 && thatInputs != null && inputs != null) {
                        for (Input input : inputs)
                            if (!thatInputs.contains(input)) {
                                result = false;
                                break;
                            }
                        if (!result)
                            break;
                    }
                } else {
                    result = false;
                    break;
                }
            }
        }
        return (result);
    }

    protected Predicate<TwoInputStates> getCondition(final Input input, final boolean pressed) {
        return (input.getCondition(pressed));
    }

    /**
     * Gets the condition of this action
     * 
     * @param action
     *            action
     * @param pressed
     *            condition valid when the input component is pressed if
     *            <code>true</code>, otherwise condition valid when the input
     *            component is released (if supported)
     * @return
     */
    public Predicate<TwoInputStates> getCondition(final Action action, final boolean pressed) {
        final Set<Input> inputs = getInputs(action);
        final Predicate<TwoInputStates> predicate;
        if (inputs == null || inputs.isEmpty()) {// it should never happen
            predicate = TriggerConditions.alwaysFalse();
        } else {
            if (inputs.size() == 1)
                predicate = getCondition(inputs.iterator().next(), pressed);
            else {
                final ArrayList<Predicate<TwoInputStates>> conditions = new ArrayList<>();
                for (Input input : inputs)
                    conditions.add(getCondition(input, pressed));
                predicate = Predicates.or(conditions);
            }
        }
        return (predicate);
    }

    @SuppressWarnings("unchecked")
    public <T extends Input> Set<T> getInputs(final Action... actions) {
        final Set<T> inputs;
        if (actions == null || actions.length == 0)
            inputs = Collections.<T> emptySet();
        else {
            final Set<T> tmpInputs = new HashSet<>();
            for (Action action : actions)
                if (action != null) {
                    for (Input input : internalActionMap.get(action)) {
                        T tInput = null;
                        try {
                            tInput = (T) input;
                        } catch (ClassCastException cce) {
                        }
                        if (tInput != null)
                            tmpInputs.add(tInput);
                    }
                }
            inputs = Collections.unmodifiableSet(tmpInputs);
        }
        return (inputs);
    }

    public void setKeyActionBinding(final Action action, final Key key) {
        prepareInputActionBinding(action, key);
        final Input input = new KeyInput(key);
        doInputActionBinding(action, input);
    }

    public void setMouseButtonActionBinding(final Action action, final MouseButton mouseButton) {
        prepareInputActionBinding(action, mouseButton);
        final Input input = new MouseButtonInput(mouseButton);
        doInputActionBinding(action, input);
    }

    public void setMouseWheelMoveActionBinding(final Action action, final Boolean wheelUpFlag) {
        prepareInputActionBinding(action, wheelUpFlag);
        final Input input = new MouseWheelMoveInput(wheelUpFlag);
        doInputActionBinding(action, input);
    }

    protected void prepareInputActionBinding(final Action action, final Object inputObject) {
        if (action == null)
            throw new IllegalArgumentException("The action cannot be null");
        if (inputObject == null)
            throw new IllegalArgumentException("The input object cannot be null");
        if (!internalActionMap.containsKey(action))
            internalActionMap.put(action, new HashSet<Input>());
    }

    protected void doInputActionBinding(final Action action, final Input input) {
        final HashSet<Input> inputs = internalActionMap.get(action);
        if (!inputs.contains(input)) {// removes this input from existing sets
                                      // of inputs
            for (Entry<Action, HashSet<Input>> actionInputEntry : internalActionMap.entrySet())
                actionInputEntry.getValue().remove(input);
            // adds this input into the set of inputs for this action
            inputs.add(input);
        }
    }

    public static abstract class Input {

        protected Input(Object o) {
            if (o == null)
                throw new IllegalArgumentException("The object input cannot be null");
        }

        public abstract Object getInputObject();

        public abstract Predicate<TwoInputStates> getCondition(final boolean pressed);

        @Override
        public boolean equals(Object o) {
            final boolean result;
            if (o == null || !(o instanceof Input))
                result = false;
            else
                result = getInputObject().equals(((Input) o).getInputObject());
            return (result);
        }

        @Override
        public int hashCode() {
            return (getInputObject().hashCode());
        }
    }

    public static class KeyInput extends Input {

        private final Key key;

        public KeyInput(final Key key) {
            super(key);
            this.key = key;
        }

        @Override
        public boolean equals(Object o) {
            final boolean result;
            if (o == null || !(o instanceof KeyInput))
                result = false;
            else
                result = key.equals(((KeyInput) o).getInputObject());
            return (result);
        }

        @Override
        public Key getInputObject() {
            return (key);
        }

        @Override
        public Predicate<TwoInputStates> getCondition(final boolean pressed) {
            return (pressed ? new KeyPressedCondition(key) : new KeyReleasedCondition(key));
        }

        @Override
        public String toString() {
            return ("key " + key.name().toUpperCase());
        }
    }

    public static class MouseButtonInput extends Input {

        private final MouseButton mouseButton;

        public MouseButtonInput(final MouseButton mouseButton) {
            super(mouseButton);
            this.mouseButton = mouseButton;
        }

        @Override
        public boolean equals(Object o) {
            final boolean result;
            if (o == null || !(o instanceof MouseButtonInput))
                result = false;
            else
                result = mouseButton.equals(((MouseButtonInput) o).getInputObject());
            return (result);
        }

        @Override
        public MouseButton getInputObject() {
            return (mouseButton);
        }

        @Override
        public Predicate<TwoInputStates> getCondition(final boolean pressed) {
            return (pressed ? new MouseButtonPressedCondition(mouseButton)
                    : new MouseButtonReleasedCondition(mouseButton));
        }

        @Override
        public String toString() {
            return (mouseButton.name().toLowerCase() + " mouse button");
        }
    }

    public static class MouseWheelMoveInput extends Input {

        private final Boolean mouseWheelUpFlag;

        public MouseWheelMoveInput(final Boolean mouseWheelUpFlag) {
            super(mouseWheelUpFlag);
            this.mouseWheelUpFlag = mouseWheelUpFlag;
        }

        @Override
        public boolean equals(Object o) {
            final boolean result;
            if (o == null || !(o instanceof MouseWheelMoveInput))
                result = false;
            else
                result = mouseWheelUpFlag.equals(((MouseWheelMoveInput) o).getInputObject());
            return (result);
        }

        @Override
        public Boolean getInputObject() {
            return (mouseWheelUpFlag);
        }

        @Override
        public Predicate<TwoInputStates> getCondition(final boolean pressed) {
            return (mouseWheelUpFlag ? new MouseWheelMovedUpCondition() : new MouseWheelMovedDownCondition());
        }

        @Override
        public String toString() {
            return (mouseWheelUpFlag.equals(Boolean.TRUE) ? "mouse wheel up" : "mouse wheel down");
        }
    }
}
