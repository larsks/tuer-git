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
package jfpsm.graph;

/**
 * Graph composed of vertices and directed edges that form a cycle
 * 
 * @author Julien Gouesse
 *
 * @param <V>
 *            vertex class
 * @param <E>
 *            edge class
 */
public class DirectedCyclicGraph<V, E> extends DirectedGraphWithoutMultiEdge<V, E> {

    /**
     * Constructor
     * 
     * @param ordered
     *            flag indicating whether the vertices and the edges are stored
     *            in a way that preserves the order by insertion time
     */
    public DirectedCyclicGraph(final boolean ordered) {
        super(ordered);
    }

    // TODO when adding a vertex, create a synthetic edge to link the first
    // vertex and the last vertex if necessary

    // TODO when adding an edge, create a synthetic edge to link the first
    // vertex and the last vertex if necessary

    // TODO when removing a vertex, create a synthetic edge to link the first
    // vertex and the last vertex if necessary

    // TODO when removing an edge, create a synthetic edge to link the first
    // vertex and the last vertex if necessary
}
