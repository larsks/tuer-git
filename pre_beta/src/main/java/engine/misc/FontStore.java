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
package engine.misc;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.ardor3d.ui.text.BMFont;
import com.ardor3d.util.resource.URLResourceSource;

import engine.statemachine.ScenegraphStateMachine;

/**
 * 
 * 
 * @author Julien Gouesse
 *
 */
public class FontStore {

    private final List<BMFont> fontsList;

    public FontStore() {
        fontsList = new ArrayList<>();
        try {
            fontsList.add(new BMFont(
                    new URLResourceSource(
                            ScenegraphStateMachine.class.getResource("/fonts/DejaVuSansCondensed-20-bold-regular.fnt")),
                    false));
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
        try {
            fontsList.add(new BMFont(
                    new URLResourceSource(
                            ScenegraphStateMachine.class.getResource("/fonts/Computerfont-35-medium-regular.fnt")),
                    false));
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
        try {
            fontsList.add(new BMFont(
                    new URLResourceSource(ScenegraphStateMachine.class.getResource("/fonts/arial-16-bold.fnt")),
                    false));
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    public final List<BMFont> getFontsList() {
        return (Collections.unmodifiableList(fontsList));
    }
}
