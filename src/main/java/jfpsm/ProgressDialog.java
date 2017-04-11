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

import java.awt.Dimension;
import java.awt.Toolkit;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.border.TitledBorder;

public final class ProgressDialog extends JDialog {

    private static final long serialVersionUID = 1L;

    private final JLabel label;

    private final JProgressBar progressBar;

    public ProgressDialog(JFrame owner, String title) {
        // set as modal
        super(owner, title, true);
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        final TitledBorder border = BorderFactory.createTitledBorder(getTitle());
        border.setTitleJustification(TitledBorder.CENTER);
        panel.setBorder(border);
        setResizable(false);
        setUndecorated(true);
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        setLocation(((int) screenSize.getWidth() / 2) - 200, ((int) screenSize.getHeight() / 2) - 150);
        label = new JLabel("");
        progressBar = new JProgressBar(0, 100);
        panel.add(progressBar);
        panel.add(label);
        add(panel);
        setSize(400, 300);
    }

    public final void reset() {
        progressBar.setValue(0);
        label.setText("");
    }

    public final void setText(final String text) {
        label.setText(text);
    }

    public final void setValue(final int value) {
        progressBar.setValue(value);
    }
}
