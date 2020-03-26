/**
 * Copyright (c) 2006-2020 Julien Gouesse
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

public class BasicScheduledTaskConditions {

    private BasicScheduledTaskConditions() {
    }

    public static <S> ScheduledTaskCondition<S> always() {
        return new ScheduledTaskCondition<S>() {
            @Override
            public boolean isSatisfied(final S previousState, final S currentState) {
                return (true);
            }
        };
    }

    public static <S> ScheduledTaskCondition<S> and(final ScheduledTaskCondition<S> first,
            final ScheduledTaskCondition<S> second) {
        return new ScheduledTaskCondition<S>() {
            @Override
            public boolean isSatisfied(final S previousState, final S currentState) {
                return (first.isSatisfied(previousState, currentState)
                        && second.isSatisfied(previousState, currentState));
            }
        };
    }

    public static <S> ScheduledTaskCondition<S> or(final ScheduledTaskCondition<S> first,
            final ScheduledTaskCondition<S> second) {
        return new ScheduledTaskCondition<S>() {
            @Override
            public boolean isSatisfied(final S previousState, final S currentState) {
                return (first.isSatisfied(previousState, currentState)
                        || second.isSatisfied(previousState, currentState));
            }
        };
    }

    public static <S> ScheduledTaskCondition<S> xor(final ScheduledTaskCondition<S> first,
            final ScheduledTaskCondition<S> second) {
        return new ScheduledTaskCondition<S>() {
            @Override
            public boolean isSatisfied(final S previousState, final S currentState) {
                return (first.isSatisfied(previousState, currentState)
                        ^ second.isSatisfied(previousState, currentState));
            }
        };
    }

    public static <S> ScheduledTaskCondition<S> not(final ScheduledTaskCondition<S> condition) {
        return new ScheduledTaskCondition<S>() {
            @Override
            public boolean isSatisfied(final S previousState, final S currentState) {
                return (!condition.isSatisfied(previousState, currentState));
            }
        };
    }
}