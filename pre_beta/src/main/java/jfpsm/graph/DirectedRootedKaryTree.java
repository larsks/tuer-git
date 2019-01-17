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
package jfpsm.graph;

/**
 * Weakly connected graph composed of directed edges and vertices that have no
 * more than k outgoing (or exiting) vertices and exactly one incoming (or
 * entering) vertex except the root that has no incoming vertex, that does not
 * allow self-loops, parallel edges and cycles, i.e a directed rooted k-ary tree
 * 
 * @author Julien Gouesse
 *
 * @param <V>
 *            vertex class
 * @param <E>
 *            edge class
 */
public class DirectedRootedKaryTree<V, E> extends DirectedRootedTree<V, E> {

    private final int k;

    /**
     * Constructor
     * 
     * @param ordered
     *            flag indicating whether the vertices and the edges are stored
     *            in a way that preserves the order by insertion time
     */
    public DirectedRootedKaryTree(final boolean ordered, final int k) {
        super(ordered);
        if (k < 0)
            throw new IllegalArgumentException("k must be positive");
        this.k = k;
    }

    public final int getK() {
        return (k);
    }

    /*
     * (non-Javadoc)
     * 
     * @see jfpsm.graph.DirectedGraph#isEdgeAdditionValid(java.lang.Object,
     * jfpsm.graph.Pair)
     */
    @Override
    protected boolean isEdgeAdditionValid(E edge, Pair<V> vertices) {
        boolean result = super.isEdgeAdditionValid(edge, vertices);
        if (result) {
            final V firstVertex = vertices.getFirst();
            final int outgoingEdgesCount;
            // vertices cannot have more than k children
            if (containsVertex(firstVertex))
                outgoingEdgesCount = internalGetOutgoingEdges(firstVertex).size();
            else
                outgoingEdgesCount = 0;
            result = outgoingEdgesCount < k;
        }
        return (result);
    }
}
