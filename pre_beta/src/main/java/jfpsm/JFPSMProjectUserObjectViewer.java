/**
 * Copyright (c) 2006-2019 Julien Gouesse
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

import java.awt.Color;

public abstract class JFPSMProjectUserObjectViewer extends Viewer {

    private static final long serialVersionUID = 1L;

    private final ProjectManager projectManager;

    private final Project project;

    public JFPSMProjectUserObjectViewer(final JFPSMProjectUserObject entity, final Project project,
            final ProjectManager projectManager) {
        super(entity);
        this.project = project;
        this.projectManager = projectManager;
    }

    final Color getSelectedTileColor() {
        return (projectManager.getSelectedTileColor(project));
    }

    /*
     * final BufferedImage openFileAndLoadImage(){
     * return(projectManager.openFileAndLoadImage()); }
     */
}
