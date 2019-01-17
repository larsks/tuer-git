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

//import java.awt.BorderLayout;
import java.awt.GridLayout;

public class FloorSetViewer extends JFPSMProjectUserObjectViewer {

    private static final long serialVersionUID = 1L;

    public FloorSetViewer(final FloorSet floorSet, final Project project, final ProjectManager projectManager) {
        super(floorSet, project, projectManager);
        setLayout(new GridLayout());
        // setLayout(new BorderLayout());
        // add(new
        // ImageViewer(floorSet.getFloorsList().get(0).getMap(MapType.CONTAINER_MAP).getImage()),BorderLayout.CENTER);
    }
}
