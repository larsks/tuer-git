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
package jfpsm;

import java.awt.BorderLayout;
import java.awt.Color;
import java.util.ArrayList;
import javax.swing.JColorChooser;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

final class TileCreationDialog extends NamingDialog {

    private static final long serialVersionUID = 1L;

    private final ArrayList<Color> colors;

    private final JColorChooser colorChooser;

    private Color color;

    TileCreationDialog(JFrame frame, final ArrayList<String> names, final ArrayList<Color> colors) {
        super(frame, names, "tile");
        this.colors = colors;
        color = null;
        colorChooser = new JColorChooser();
        ((JPanel) getContentPane().getComponent(0)).add(colorChooser, BorderLayout.SOUTH);
        pack();
        // update the confirm button state when selecting a color
        colorChooser.getSelectionModel().addChangeListener(new ChangeListener() {
            @Override
            public final void stateChanged(ChangeEvent e) {
                updateConfirmationAbility();
            }
        });
        setResizable(false);
    }

    @Override
    protected final boolean checkDataValidity() {
        Color currentColor = colorChooser.getColor();
        return (super.checkDataValidity() && currentColor != null && !colors.contains(currentColor));
    }

    final Color getValidatedColor() {
        return (color);
    }

    @Override
    protected final void updateValidationData(boolean validate) {
        super.updateValidationData(validate);
        if (validate)
            color = colorChooser.getColor();
        else
            color = null;
    }

    @Override
    public final void clearAndHide() {
        super.clearAndHide();
        colorChooser.setColor(null);
    }
}
