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

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * Graph composed of vertices and directed edges (inspired of JUNG 2.0)
 * 
 * @author Julien Gouesse
 *
 * @param <V>
 *            vertex class
 * @param <E>
 *            edge class
 */
public abstract class DirectedGraph<V, E> {

    /**
     * map of vertices with their adjacency sets (incoming, outgoing)
     */
    protected final Map<V, Pair<Set<E>>> vertices;
    /**
     * map of edges with their incident vertices (start point, end point)
     */
    protected final Map<E, Pair<V>> edges;

    /**
     * Constructor
     * 
     * @param ordered
     *            flag indicating whether the vertices and the edges are stored
     *            in a way that preserves the order by insertion time
     */
    public DirectedGraph(final boolean ordered) {
        if (ordered) {
            vertices = new LinkedHashMap<>();
            edges = new LinkedHashMap<>();
        } else {
            vertices = new HashMap<>();
            edges = new HashMap<>();
        }
    }

    /**
     * Adds a self-loop to this graph
     * 
     * @param e
     *            edge
     * @param v
     *            vertex
     * @return <code>true</code> if the addition is successful, otherwise
     *         <code>false</code>
     */
    public boolean addEdge(E e, V v) {
        return (addEdge(e, new Pair<>(v, v)));
    }

    /**
     * 
     * @param e
     * @param v1
     * @param v2
     * @return
     */
    public boolean addEdge(E e, V v1, V v2) {
        return (addEdge(e, new Pair<>(v1, v2)));
    }

    /**
     * 
     * @param edge
     * @param vertices
     * @return
     */
    public boolean addEdge(E edge, Pair<? extends V> vertices) {
        if (edge == null)
            throw new IllegalArgumentException("edge must not be null");
        Pair<V> newVertices = getValidatedVertices(edge, vertices);
        final boolean success = newVertices != null && isEdgeAdditionValid(edge, newVertices);
        if (success) {
            edges.put(edge, newVertices);

            @SuppressWarnings("null")
            V source = newVertices.getFirst();
            V dest = newVertices.getSecond();

            if (!containsVertex(source))
                this.addVertex(source);

            if (!containsVertex(dest))
                this.addVertex(dest);

            internalGetIncomingEdges(dest).add(edge);
            internalGetOutgoingEdges(source).add(edge);
        }
        return (success);
    }

    /**
     * 
     * @param vertex
     * @return
     */
    public boolean addVertex(V vertex) {
        if (vertex == null)
            throw new IllegalArgumentException("vertex must not be null");
        final boolean success = !containsVertex(vertex);
        if (success)
            vertices.put(vertex, new Pair<Set<E>>(new HashSet<E>(), new HashSet<E>()));
        return (success);
    }

    /**
     * 
     * @return
     */
    public Collection<E> getEdges() {
        return (Collections.unmodifiableCollection(edges.keySet()));
    }

    /**
     * Checks that the supplied edge is not already in this graph and returns a
     * validated pair of the appropriate type
     * 
     * @param edge
     * @param vertices
     * @return
     */
    protected Pair<V> getValidatedVertices(E edge, Pair<? extends V> vertices) {
        if (edge == null)
            throw new IllegalArgumentException("input edge must not be null");

        if (vertices == null)
            throw new IllegalArgumentException("endpoints must not be null");

        Pair<V> newVertices = new Pair<>(vertices.getFirst(), vertices.getSecond());
        if (containsEdge(edge)) {
            Pair<V> existingVertices = getVertices(edge);
            if (!existingVertices.equals(newVertices))
                throw new IllegalArgumentException("edge " + edge + " already exists in this graph with vertices "
                        + existingVertices + " and cannot be added with vertices " + vertices);
            else
                newVertices = null;
        }
        return (newVertices);
    }

    /**
     * 
     * @return
     */
    public Collection<V> getVertices() {
        return (Collections.unmodifiableCollection(vertices.keySet()));
    }

    /**
     * 
     * @param vertex
     * @return
     */
    public boolean containsVertex(V vertex) {
        return (vertices.keySet().contains(vertex));
    }

    /**
     * 
     * @param edge
     * @return
     */
    public boolean containsEdge(E edge) {
        return (edges.keySet().contains(edge));
    }

    /**
     * 
     * @param edge
     * @return
     */
    public Pair<V> getVertices(E edge) {
        return (edges.get(edge));
    }

    /**
     * 
     * @return
     */
    public int getEdgeCount() {
        return (edges.size());
    }

    /**
     * 
     * @return
     */
    public int getVertexCount() {
        return (vertices.size());
    }

    /**
     * 
     * @param vertex
     * @return
     */
    protected Collection<E> internalGetIncomingEdges(V vertex) {
        return (vertices.get(vertex).getFirst());
    }

    /**
     * 
     * @param vertex
     * @return
     */
    protected Collection<E> internalGetOutgoingEdges(V vertex) {
        return (vertices.get(vertex).getSecond());
    }

    /**
     * 
     * @param vertex
     * @return
     */
    public Collection<E> getIncomingEdges(V vertex) {
        final Collection<E> result;
        if (!containsVertex(vertex))
            result = null;
        else
            result = Collections.unmodifiableCollection(internalGetIncomingEdges(vertex));
        return (result);
    }

    /**
     * 
     * @param vertex
     * @return
     */
    public Collection<E> getOutgoingEdges(V vertex) {
        final Collection<E> result;
        if (!containsVertex(vertex))
            result = null;
        else
            result = Collections.unmodifiableCollection(internalGetOutgoingEdges(vertex));
        return (result);
    }

    /**
     * 
     * @param vertex
     * @return
     */
    public Collection<V> getPredecessors(V vertex) {
        final Collection<V> result;
        if (!containsVertex(vertex))
            result = null;
        else {
            Set<V> preds = new HashSet<>();
            for (E edge : internalGetIncomingEdges(vertex))
                preds.add(this.getSource(edge));
            result = Collections.unmodifiableCollection(preds);
        }
        return (result);
    }

    /**
     * 
     * @param vertex
     * @return
     */
    public Collection<V> getSuccessors(V vertex) {
        final Collection<V> result;
        if (!containsVertex(vertex))
            result = null;
        else {
            Set<V> succs = new HashSet<>();
            for (E edge : internalGetOutgoingEdges(vertex))
                succs.add(this.getDest(edge));
            result = Collections.unmodifiableCollection(succs);
        }
        return (result);
    }

    /**
     * 
     * @param vertex
     * @return
     */
    public Collection<V> getNeighbors(V vertex) {
        final Collection<V> result;
        if (!containsVertex(vertex))
            result = null;
        else {
            Collection<V> neighbors = new HashSet<>();
            for (E edge : internalGetIncomingEdges(vertex))
                neighbors.add(this.getSource(edge));
            for (E edge : internalGetOutgoingEdges(vertex))
                neighbors.add(this.getDest(edge));
            result = Collections.unmodifiableCollection(neighbors);
        }
        return (result);
    }

    /**
     * 
     * @param vertex
     * @return
     */
    public Collection<E> getIncidentEdges(V vertex) {
        final Collection<E> incident;
        if (!containsVertex(vertex))
            incident = null;
        else {
            incident = new HashSet<>();
            incident.addAll(internalGetIncomingEdges(vertex));
            incident.addAll(internalGetOutgoingEdges(vertex));
        }
        return incident;
    }

    /**
     * 
     * @param v1
     * @param v2
     * @return
     */
    public E findEdge(V v1, V v2) {
        E foundEdge = null;
        if (containsVertex(v1) && containsVertex(v2))
            for (E edge : internalGetOutgoingEdges(v1))
                if (this.getDest(edge).equals(v2)) {
                    foundEdge = edge;
                    break;
                }
        return (foundEdge);
    }

    /**
     * 
     * @param edge
     * @return
     */
    public V getSource(E edge) {
        final V sourceVertex;
        if (!containsEdge(edge))
            sourceVertex = null;
        else
            sourceVertex = this.getVertices(edge).getFirst();
        return (sourceVertex);
    }

    /**
     * 
     * @param edge
     * @return
     */
    public V getDest(E edge) {
        final V destinationVertex;
        if (!containsEdge(edge))
            destinationVertex = null;
        else
            destinationVertex = this.getVertices(edge).getSecond();
        return (destinationVertex);
    }

    /**
     * 
     * @param vertex
     * @param edge
     * @return
     */
    public boolean isSource(V vertex, E edge) {
        final boolean result = containsEdge(edge) && containsVertex(vertex)
                && vertex.equals(this.getVertices(edge).getFirst());
        return (result);
    }

    /**
     * 
     * @param vertex
     * @param edge
     * @return
     */
    public boolean isDest(V vertex, E edge) {
        final boolean result = containsEdge(edge) && containsVertex(vertex)
                && vertex.equals(this.getVertices(edge).getSecond());
        return (result);
    }

    /**
     * Tells whether this edge addition is valid for this kind of graph
     * 
     * @param edge
     * @param vertices
     * @return
     */
    protected abstract boolean isEdgeAdditionValid(E edge, Pair<V> vertices);

    public boolean removeEdge(E edge) {
        final boolean success = containsEdge(edge);
        if (success) {
            Pair<V> vertices = this.getVertices(edge);
            V source = vertices.getFirst();
            V dest = vertices.getSecond();

            // removes edge from incident vertices' adjacency sets
            internalGetOutgoingEdges(source).remove(edge);
            internalGetIncomingEdges(dest).remove(edge);

            edges.remove(edge);
        }
        return (success);
    }

    /**
     * 
     * @param vertex
     * @return
     */
    public boolean removeVertex(V vertex) {
        final boolean success = containsVertex(vertex);
        if (success) {// copies to avoid concurrent modification in removeEdge
            Set<E> incident = new HashSet<>(internalGetIncomingEdges(vertex));
            incident.addAll(internalGetOutgoingEdges(vertex));

            for (E edge : incident)
                removeEdge(edge);

            vertices.remove(vertex);
        }
        return (success);
    }
}
