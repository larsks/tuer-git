/**
 * Copyright (c) 2006-2016 Julien Gouesse
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
package jfpsm.graph;

/**
 * Graph composed of vertices and directed edges, that allows self-loops and 
 * parallel edges
 * 
 * @author Julien Gouesse
 *
 * @param <V> vertex class
 * @param <E> edge class
 */
public class DirectedMultiGraph<V,E> extends DirectedGraph<V,E>{

	/**
	 * Constructor
	 * 
	 * @param ordered flag indicating whether the vertices and the edges are 
	 * stored in a way that preserves the order by insertion time
	 */
	public DirectedMultiGraph(final boolean ordered){
		super(ordered);
	}
	
	/* (non-Javadoc)
	 * @see jfpsm.graph.DirectedGraph#isEdgeAdditionValid(java.lang.Object, jfpsm.graph.Pair)
	 */
	@Override
	protected boolean isEdgeAdditionValid(E edge,Pair<V> vertices){
		return(true);
	}
}
