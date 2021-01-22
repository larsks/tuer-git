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
package jfpsm;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.plaf.basic.BasicButtonUI;

/**
 * Component that displays the viewers of the entities in a tabbed pane
 * 
 * @author Julien Gouesse
 *
 */
public class EntityViewer extends JPanel {

    private static final long serialVersionUID = 1L;

    private final JTabbedPane entityTabbedPane;

    private final HashMap<Namable, JPanel> entityToTabComponentMap;

    private final ProjectManager projectManager;

    private final ToolManager toolManager;

    public EntityViewer(final ProjectManager projectManager, final ToolManager toolManager) {
        this.projectManager = projectManager;
        this.toolManager = toolManager;
        entityToTabComponentMap = new HashMap<>();
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        entityTabbedPane = new JTabbedPane();
        add(entityTabbedPane);
    }

    protected JPanel createEntityViewTabComponent(final JFPSMUserObject entity, final Viewer entityView) {
        entityTabbedPane.addTab(entity.getName(), entityView);
        final JPanel tabComponent = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        entityToTabComponentMap.put(entity, tabComponent);
        tabComponent.setOpaque(false);
        // adds much space to the top of the component
        tabComponent.setBorder(BorderFactory.createEmptyBorder(2, 0, 0, 0));
        final JLabel label = new JLabel(entity.getName());
        // adds much space between the label and the button
        label.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 5));
        tabComponent.add(label);
        // adds a button to close the tab of the viewer
        final CloseButton closeButton = new CloseButton();
        closeButton.addActionListener(new ActionListener() {
            @Override
            public final void actionPerformed(ActionEvent ae) {
                closeEntityView(entity);
            }
        });
        tabComponent.add(closeButton);
        entityTabbedPane.setTabComponentAt(entityTabbedPane.indexOfComponent(entityView), tabComponent);
        return (tabComponent);
    }

    /**
     * Opens the view of an entity. Creates it if it does not exist yet and then
     * selects it
     * 
     * @param entity
     *            entity to view
     * @return
     */
    public boolean openEntityView(final JFPSMToolUserObject entity) {
        final boolean success;
        JPanel tabComponent = entityToTabComponentMap.get(entity);
        final Viewer entityView;
        if (tabComponent == null) {
            entityView = entity.createViewer(toolManager);
            if (entityView != null) {
                tabComponent = createEntityViewTabComponent(entity, entityView);
                success = true;
            } else {// this entity has no dedicated viewer
                success = false;
            }
        } else {// the view of this entity is already open, there is nothing to
                // create
            success = true;
        }
        if (tabComponent != null)
            entityTabbedPane.setSelectedIndex(entityTabbedPane.indexOfTabComponent(tabComponent));
        return (success);
    }

    /**
     * Opens the view of an entity. Creates it if it does not exist yet and then
     * selects it
     * 
     * @param entity
     *            entity to view
     * @param project
     *            project in which this entity is
     * @return
     */
    public boolean openEntityView(final JFPSMProjectUserObject entity, final Project project) {
        final boolean success;
        JPanel tabComponent = entityToTabComponentMap.get(entity);
        final Viewer entityView;
        if (tabComponent == null) {
            entityView = entity.createViewer(project, projectManager);
            if (entityView != null) {
                tabComponent = createEntityViewTabComponent(entity, entityView);
                success = true;
            } else {// this entity has no dedicated viewer
                success = false;
            }
        } else {// the view of this entity is already open, there is nothing to
                // create
            success = true;
        }
        if (tabComponent != null)
            entityTabbedPane.setSelectedIndex(entityTabbedPane.indexOfTabComponent(tabComponent));
        return (success);
    }

    public boolean renameEntityView(final Namable entity) {
        final JPanel tabComponent = entityToTabComponentMap.get(entity);
        final boolean success = tabComponent != null;
        if (tabComponent != null)
            ((JLabel) tabComponent.getComponent(0)).setText(entity.getName());
        return (success);
    }

    public boolean closeEntityView(final Namable entity) {
        final JPanel tabComponent = entityToTabComponentMap.get(entity);
        final boolean success = tabComponent != null;
        if (tabComponent != null) {
            entityTabbedPane.removeTabAt(entityTabbedPane.indexOfTabComponent(tabComponent));
            entityToTabComponentMap.remove(entity);
        }
        return (success);
    }

    /**
     * Close button largely inspired of this Oracle's example:
     * http://docs.oracle.com/javase/tutorial/uiswing/examples/components/
     * TabComponentsDemoProject/src/components/ButtonTabComponent.java
     * 
     * @author Julien Gouesse
     *
     */
    private static final class CloseButton extends JButton {

        private static final long serialVersionUID = 1L;

        private CloseButton() {
            super();
            setPreferredSize(new Dimension(20, 20));
            // uses the most basic UI whatever the look and feel
            setUI(new BasicButtonUI());
            // drives it transparent
            setContentAreaFilled(false);
            // only the rest of the tab should be focusable
            setFocusable(false);
            // sets a border that is never painted
            setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
            setBorderPainted(false);
            // enables the roll over so that we can use it while drawing
            setRolloverEnabled(true);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            final Graphics2D g2 = (Graphics2D) g.create();
            if (getModel().isPressed())
                g2.setStroke(new BasicStroke(2.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER));
            else
                g2.setStroke(new BasicStroke(1.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER));
            if (getModel().isRollover())
                g2.setColor(Color.RED);
            else
                g2.setColor(Color.BLACK);
            final int delta = 3;
            // bias is used as a workaround
            g2.drawLine(delta, delta, getWidth() - delta - 1, getHeight() - delta - 1);
            g2.drawLine(getWidth() - delta - 1, delta, delta, getHeight() - delta - 1);
            g2.dispose();
        }
    }
}
