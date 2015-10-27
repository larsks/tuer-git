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

/**
 * grid with a finite size
 * @author Julien Gouesse
 *
 */
public abstract class FiniteGrid implements Grid{
    
    
    private int logicalWidth;
    
    private int logicalHeight;
    
    private int logicalDepth;
    
    
    public FiniteGrid(final int logicalWidth,final int logicalHeight,final int logicalDepth){
        this.logicalWidth=logicalWidth;
        this.logicalHeight=logicalHeight;
        this.logicalDepth=logicalDepth;
    }

    
    @Override
    public final int getLogicalDepth(){
        return(logicalDepth);
    }

    @Override
    public final int getLogicalHeight(){
        return(logicalHeight);
    }

    @Override
    public final int getLogicalWidth(){
        return(logicalWidth);
    }
}