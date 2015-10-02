/**
 * Copyright (c) 2006-2015 Julien Gouesse
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

import java.beans.Transient;
import java.util.ArrayList;

/**
 * Container of floors => level
 * @author Julien Gouesse
 *
 */
public final class FloorSet extends JFPSMProjectUserObject{
    
    
    private static final long serialVersionUID=1L;
    
    private ArrayList<Floor> floorsList;
    
    private transient boolean dirty;
    
    
    public FloorSet(){
        this("");
    }
    
    public FloorSet(String name){
        super(name);
        floorsList=new ArrayList<>();
        markDirty();
    }
    
    @Transient
    @Override
    public final boolean isDirty(){
        boolean dirty=this.dirty;
        if(!dirty)
            for(Floor floor:floorsList)
                if(floor.isDirty())
                    {dirty=true;
                     break;
                    }
        return(dirty);
    }
    
    @Override
    public final void unmarkDirty(){
        dirty=false;
    }
    
    @Override
    public final void markDirty(){
        dirty=true;
    }
    
    public final void addFloor(Floor floor){
        floorsList.add(floor);
        markDirty();
    }
    
    public final void removeFloor(Floor floor){
        floorsList.remove(floor);
        markDirty();
    }
    
    public final void removeAllFloors(){
        floorsList.clear();
        markDirty();
    }

    public final ArrayList<Floor> getFloorsList(){
        return(floorsList);
    }

    public final void setFloorsList(ArrayList<Floor> floorsList){
        this.floorsList=floorsList;
        markDirty();
    }
    
    @Override
    final boolean canInstantiateChildren(){
        return(true);
    }

    @Override
    final boolean isOpenable(){
        return(true);
    }

    @Override
    final boolean isRemovable(){
        return(true);
    }
    
    @Override
    public Viewer createViewer(final Project project,final ProjectManager projectManager){
    	return(new FloorSetViewer(this,project,projectManager));
    }
}