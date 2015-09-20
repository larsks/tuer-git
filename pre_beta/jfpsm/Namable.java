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

import java.io.Serializable;

public abstract class Namable implements Serializable{
    
    
    private static final long serialVersionUID = 1L;
    
    private String name;
    
    
    public Namable(){}
    
    public Namable(String name){
        this.name=name;
    }
    
    @Override
    public String toString(){
        return(getName());
    }

    public String getName(){
        return(name);
    }

    public void setName(String name){
        this.name=name;
    }
}