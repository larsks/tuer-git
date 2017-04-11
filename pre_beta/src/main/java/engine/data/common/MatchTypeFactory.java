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
package engine.data.common;

import engine.abstraction.AbstractFactory;

public class MatchTypeFactory extends AbstractFactory<MatchType> {

    public MatchTypeFactory() {
    }

    public boolean addNewMatchType(final String identifier, final String label,
            final String noLimitObjectiveDescriptionLabel) {
        boolean success = identifier != null && label != null && !componentMap.containsKey(identifier);
        if (success) {
            final MatchType matchType = new MatchType(label, noLimitObjectiveDescriptionLabel);
            success = add(identifier, matchType);
        }
        return (success);
    }

    public String getFormattedStringForCombo(MatchType matchType) {
        int maxMatchTypeLabelLength = 0;
        for (int matchTypeIndex = 0; matchTypeIndex < getSize(); matchTypeIndex++)
            maxMatchTypeLabelLength = Math.max(maxMatchTypeLabelLength, get(matchTypeIndex).getLabel().trim().length());
        final String formattedStringForCombo;
        if (maxMatchTypeLabelLength > 0) {
            final String basicCurrentTrimmedLabel = matchType.getLabel().trim();
            if (basicCurrentTrimmedLabel.length() == maxMatchTypeLabelLength)
                formattedStringForCombo = basicCurrentTrimmedLabel;
            else {
                final StringBuilder builder = new StringBuilder();
                final int leadingSpacesCount = (maxMatchTypeLabelLength - basicCurrentTrimmedLabel.length()) / 2;
                final int trailingSpacesCount = maxMatchTypeLabelLength - basicCurrentTrimmedLabel.length()
                        - leadingSpacesCount;
                final String space = " ";
                for (int i = 0; i < leadingSpacesCount; i++)
                    builder.append(space);
                builder.append(basicCurrentTrimmedLabel);
                for (int i = 0; i < trailingSpacesCount; i++)
                    builder.append(space);
                formattedStringForCombo = builder.toString();
            }
        } else
            formattedStringForCombo = "";
        return (formattedStringForCombo);
    }
}
