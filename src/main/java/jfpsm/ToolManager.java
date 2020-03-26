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
package jfpsm;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.AbstractMap.SimpleEntry;
import javax.swing.JOptionPane;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import common.EngineServiceProviderInterface;

/**
 * Panel that allows to manipulate the tools in a tree containing their use
 * cases
 * 
 * @author Julien Gouesse
 *
 */
public final class ToolManager extends EntityManager {

    private static final long serialVersionUID = 1L;

    public ToolManager(final MainWindow mainWindow, final EngineServiceProviderInterface<?, ?, ?, ?, ?> seeker) {
        super(mainWindow, new DefaultTreeModel(new DefaultMutableTreeNode(new ToolSet("Tool Set"))), seeker);
        final DefaultTreeModel treeModel = (DefaultTreeModel) tree.getModel();
        final DefaultMutableTreeNode toolsRoot = (DefaultMutableTreeNode) treeModel.getRoot();
        final ToolSet toolSet = (ToolSet) toolsRoot.getUserObject();
        toolSet.addTool(new ModelConverterSet("Model Converter Set"));
        for (final Tool tool : toolSet.getToolsList()) {
            final DefaultMutableTreeNode toolNode = new DefaultMutableTreeNode(tool);
            treeModel.insertNodeInto(toolNode, toolsRoot, toolsRoot.getChildCount());
        }
        // expands the tree path from the root
        tree.expandPath(new TreePath(treeModel.getPathToRoot((TreeNode) treeModel.getRoot())));
        tree.addMouseListener(new MouseAdapter() {
            @Override
            public final void mousePressed(MouseEvent e) {
                handleMouseEvent(e);
            }

            @Override
            public final void mouseReleased(MouseEvent e) {
                handleMouseEvent(e);
            }
        });
    }

    private void handleMouseEvent(MouseEvent me) {
        if (me.isPopupTrigger()) {// gets all selected paths
            TreePath[] paths = tree.getSelectionPaths();
            TreePath mouseOverPath = tree.getClosestPathForLocation(me.getX(), me.getY());
            if (mouseOverPath != null) {// if no node is selected or if the node
                                        // under the mouse pointer is not
                                        // selected
                                        // then selects only the node under the
                                        // mouse pointer and invalidate the
                                        // previous selection
                boolean found = false;
                if (paths != null)
                    for (TreePath path : paths)
                        if (path.equals(mouseOverPath)) {
                            found = true;
                            break;
                        }
                if (!found) {// selects the path on right click if none was
                             // already selected
                    tree.setSelectionPath(mouseOverPath);
                    paths = new TreePath[] { mouseOverPath };
                }
            }
            if (paths != null) {
                final boolean singleSelection = paths.length == 1;
                // gets the first selected tree node
                final TreePath path = tree.getSelectionPath();
                final DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode) path.getLastPathComponent();
                final JFPSMUserObject userObject = (JFPSMUserObject) selectedNode.getUserObject();
                final boolean showNew = singleSelection && userObject.canInstantiateChildren();
                boolean showOpenAndClose = false;
                JFPSMUserObject currentUserObject;
                for (TreePath currentPath : paths) {
                    currentUserObject = (JFPSMUserObject) ((DefaultMutableTreeNode) currentPath.getLastPathComponent())
                            .getUserObject();
                    if (currentUserObject.isOpenable()) {
                        showOpenAndClose = true;
                        break;
                    }
                }
                boolean showDelete = false;
                for (TreePath currentPath : paths) {
                    currentUserObject = (JFPSMUserObject) ((DefaultMutableTreeNode) currentPath.getLastPathComponent())
                            .getUserObject();
                    if (currentUserObject.isRemovable()) {
                        showDelete = true;
                        break;
                    }
                }
                newMenuItem.setVisible(showNew);
                openMenuItem.setVisible(showOpenAndClose);
                closeMenuItem.setVisible(showOpenAndClose);
                deleteMenuItem.setVisible(showDelete);
                if (showNew || showOpenAndClose || showDelete)
                    treePopupMenu.show(mainWindow.getApplicativeFrame(), me.getXOnScreen(), me.getYOnScreen());
            }
        } else
        // double-click
        if (me.getClickCount() == 2) {
            final TreePath path = tree.getSelectionPath();
            if (path != null) {
                final DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode) path.getLastPathComponent();
                final JFPSMToolUserObject userObject = (JFPSMToolUserObject) selectedNode.getUserObject();
                if (userObject != null && userObject.isOpenable())
                    mainWindow.getEntityViewer().openEntityView(userObject);
            }
        }
    }

    @Override
    protected SimpleEntry<JFPSMToolUserObject, DefaultMutableTreeNode> createNewEntityFromSelectedEntity() {
        final TreePath path = tree.getSelectionPath();
        final DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode) path.getLastPathComponent();
        final JFPSMToolUserObject userObject = (JFPSMToolUserObject) selectedNode.getUserObject();
        final JFPSMToolUserObject newlyCreatedEntity;
        final DefaultMutableTreeNode treeNode;
        if (userObject.canInstantiateChildren()) {
            if (userObject instanceof ModelConverterSet) {
                final ModelConverterSet modelConverterSet = (ModelConverterSet) userObject;
                // creates the model converter
                final ModelConverter modelConverter = new ModelConverter("Model Converter");
                // adds it into the set
                modelConverterSet.addModelConverter(modelConverter);
                // creates and adds the node into the tree
                final DefaultMutableTreeNode modelConverterNode = new DefaultMutableTreeNode(modelConverter);
                ((DefaultTreeModel) tree.getModel()).insertNodeInto(modelConverterNode, selectedNode,
                        selectedNode.getChildCount());
                // expands the path of the set node
                final TreePath modelConverterSetPath = new TreePath(
                        ((DefaultTreeModel) tree.getModel()).getPathToRoot(selectedNode));
                if (!tree.isExpanded(modelConverterSetPath))
                    tree.expandPath(modelConverterSetPath);
                newlyCreatedEntity = modelConverter;
                treeNode = modelConverterNode;
            } else {
                newlyCreatedEntity = null;
                treeNode = null;
            }
        } else {
            newlyCreatedEntity = null;
            treeNode = null;
        }
        final SimpleEntry<JFPSMToolUserObject, DefaultMutableTreeNode> entry;
        if (newlyCreatedEntity == null && treeNode == null)
            entry = null;
        else
            entry = new SimpleEntry<>(newlyCreatedEntity, treeNode);
        return (entry);
    }

    @Override
    protected void openEntity(final TreePath path, final DefaultMutableTreeNode node,
            final JFPSMUserObject userObject) {
        super.openEntity(path, node, userObject);
        if (userObject.isOpenable()) {
            if (userObject instanceof ModelConverter) {// opens a tab view for
                                                       // this entity
                mainWindow.getEntityViewer().openEntityView((JFPSMToolUserObject) userObject);
            }
        }
    }

    @Override
    protected void closeSelectedEntities() {
        super.closeSelectedEntities();
        final TreePath[] paths = tree.getSelectionPaths();
        for (TreePath path : paths) {
            final DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode) path.getLastPathComponent();
            final JFPSMUserObject userObject = (JFPSMUserObject) selectedNode.getUserObject();
            if (userObject.isOpenable()) {// closes the tab views of its
                                          // children
                if (userObject instanceof ModelConverterSet) {
                    for (final ModelConverter modelConverter : ((ModelConverterSet) userObject)
                            .getModelConvertersList())
                        mainWindow.getEntityViewer().closeEntityView(modelConverter);
                }
                if (userObject instanceof ModelConverter) {// closes the tab
                                                           // view of this
                                                           // entity
                    mainWindow.getEntityViewer().closeEntityView(userObject);
                }
            }
        }
    }

    @Override
    protected void deleteSelectedEntities() {
        final TreePath[] paths = tree.getSelectionPaths();
        final ArrayList<DefaultMutableTreeNode> modelConvertersNodesTrash = new ArrayList<>();
        for (TreePath path : paths) {
            final DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode) path.getLastPathComponent();
            final JFPSMToolUserObject userObject = (JFPSMToolUserObject) selectedNode.getUserObject();
            if (userObject.isRemovable()) {
                if (userObject instanceof ModelConverter)
                    modelConvertersNodesTrash.add(selectedNode);
            }
        }
        final int elementsCount = modelConvertersNodesTrash.size();
        if (elementsCount >= 1) {
            final StringBuilder entitiesBuilder = new StringBuilder();
            final boolean noModelConverter = modelConvertersNodesTrash.isEmpty();
            String questionStart;
            if (noModelConverter)
                questionStart = "Delete element";
            else
                questionStart = "Delete model converter";
            // checks if a plural is needed
            if (elementsCount > 1)
                questionStart += "s";
            entitiesBuilder.append(questionStart);
            entitiesBuilder.append(" ");
            for (final DefaultMutableTreeNode modelConverterNode : modelConvertersNodesTrash)
                entitiesBuilder.append("\"" + modelConverterNode.getUserObject().toString() + "\", ");
            // deletes the useless string ", " at the end
            entitiesBuilder.delete(entitiesBuilder.length() - 2, entitiesBuilder.length());
            entitiesBuilder.append("?");
            final String windowTitle = "Confirm " + questionStart.toLowerCase();
            if (JOptionPane.showConfirmDialog(mainWindow.getApplicativeFrame(), entitiesBuilder.toString(), windowTitle,
                    JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
                final DefaultTreeModel treeModel = (DefaultTreeModel) tree.getModel();
                for (DefaultMutableTreeNode node : modelConvertersNodesTrash) {
                    final ModelConverterSet modelConverterSet = (ModelConverterSet) ((DefaultMutableTreeNode) node
                            .getParent()).getUserObject();
                    final ModelConverter modelConverter = (ModelConverter) node.getUserObject();
                    // removes the model converter from the entity viewer by
                    // closing its tab view
                    mainWindow.getEntityViewer().closeEntityView(modelConverter);
                    // removes the model converter from the workspace
                    modelConverterSet.removeModelConverter(modelConverter);
                    // removes the model converter from the tree
                    treeModel.removeNodeFromParent(node);
                }
            }
        }
    }
}
