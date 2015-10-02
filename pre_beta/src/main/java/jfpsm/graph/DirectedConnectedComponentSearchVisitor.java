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
package jfpsm.graph;

/**
 * Object used to traverse the connected component of a directed graph until a 
 * supplied vertex is found
 * 
 * @author Julien Gouesse
 *
 */
public class DirectedConnectedComponentSearchVisitor<V,E,G extends DirectedGraph<V,E>> extends
		DirectedConnectedComponentVisitor<V,E,G> {

	private final V searchedVertex;
	
	/**
	 * Constructor
	 */
	public DirectedConnectedComponentSearchVisitor(final V searchedVertex){
		super();
		this.searchedVertex=searchedVertex;
	}

	/* (non-Javadoc)
	 * @see jfpsm.graph.Visitor#performOnCurrentlyVisitedVertex(jfpsm.graph.DirectedGraph, java.lang.Object)
	 */
	@Override
	protected boolean performOnCurrentlyVisitedVertex(
			final G graph,final V currentlyVisitedVertex){
		return(!currentlyVisitedVertex.equals(searchedVertex));
	}
}
