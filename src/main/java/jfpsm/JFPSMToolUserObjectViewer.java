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

public abstract class JFPSMToolUserObjectViewer extends Viewer {

    private static final long serialVersionUID = 1L;

    protected final ToolManager toolManager;

    public JFPSMToolUserObjectViewer(final JFPSMToolUserObject entity, final ToolManager toolManager) {
        super(entity);
        this.toolManager = toolManager;
    }

    @Override
    public JFPSMToolUserObject getEntity() {
        return ((JFPSMToolUserObject) super.getEntity());
    }
}
