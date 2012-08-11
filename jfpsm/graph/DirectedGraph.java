/*This program is free software; you can redistribute it and/or
  modify it under the terms of the GNU General Public License
  as published by the Free Software Foundation, version 2
  of the License.
  This program is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  GNU General Public License for more details.
  You should have received a copy of the GNU General Public License
  along with this program; if not, write to the Free Software
  Foundation, Inc., 59 Temple Place - Suite 330, Boston,
  MA 02111-1307, USA.
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
 * Graph composed of vertices and directed edges
 * 
 * @author Julien Gouesse
 *
 * @param <V> vertex class
 * @param <E> edge class
 */
public abstract class DirectedGraph<V,E>{
	
	/**
	 * map of vertices with their adjacency sets (incoming, outgoing)
	 */
	protected Map<V,Pair<Set<E>>> vertices;
	/**
	 * map of edges with their incident vertices (start point, end point)
	 */
	protected Map<E,Pair<V>> edges;
    
	/**
	 * Constructor
	 * 
	 * @param ordered flag indicating whether the vertices and the edges are 
	 * stored in a way that preserves the order by insertion time
	 */
	public DirectedGraph(final boolean ordered){
		if(ordered)
		    {vertices=new LinkedHashMap<V,Pair<Set<E>>>();
		     edges=new LinkedHashMap<E,Pair<V>>();
		    }
		else
		    {vertices=new HashMap<V,Pair<Set<E>>>();
		     edges=new HashMap<E,Pair<V>>();
		    }
	}
	
	public Collection<E> getEdges(){
        return(Collections.unmodifiableCollection(edges.keySet()));
    }

    public Collection<V> getVertices(){
        return(Collections.unmodifiableCollection(vertices.keySet()));
    }

    public boolean containsVertex(V vertex){
    	return(vertices.keySet().contains(vertex));
    }
    
    public boolean containsEdge(E edge){
    	return(edges.keySet().contains(edge));
    }
    
    public Pair<V> getVertices(E edge){
    	return(edges.get(edge));
    }
    
    public int getEdgeCount(){
		return(edges.size());
	}

	public int getVertexCount(){
		return(vertices.size());
	}
	
	protected Collection<E> internalGetIncomingEdges(V vertex){
        return(vertices.get(vertex).getFirst());
    }
    
    protected Collection<E> getInternalOutgoingEdges(V vertex){
        return(vertices.get(vertex).getSecond());
    }
    
    public Collection<E> getIncomingEdges(V vertex) {
    	final Collection<E> result;
    	if(!containsVertex(vertex))
    		result=null;
    	else
    		result=Collections.unmodifiableCollection(internalGetIncomingEdges(
    				vertex));
    	return(result);
    }

    public Collection<E> getOutgoingEdges(V vertex) {
    	final Collection<E> result;
    	if (!containsVertex(vertex))
    		result=null;
    	else
    		result=Collections.unmodifiableCollection(getInternalOutgoingEdges(
    				vertex));
    	return(result);
    }

    public Collection<V> getPredecessors(V vertex){
    	final Collection<V> result;
        if(!containsVertex(vertex))
            result=null;
        else
            {Set<V> preds=new HashSet<V>();
             for(E edge:internalGetIncomingEdges(vertex))
                 preds.add(this.getSource(edge));
        	 result=Collections.unmodifiableCollection(preds);
            }
        return(result);
    }
    
    public Collection<V> getSuccessors(V vertex){
    	final Collection<V> result;
        if(!containsVertex(vertex))
        	result=null;
        else
            {Set<V> succs=new HashSet<V>();
             for(E edge:getInternalOutgoingEdges(vertex))
                 succs.add(this.getDest(edge));
             result=Collections.unmodifiableCollection(succs);
            }
        return(result);
    }

    public Collection<V> getNeighbors(V vertex){
    	final Collection<V> result;
        if(!containsVertex(vertex))
        	result=null;
        else
            {Collection<V> neighbors=new HashSet<V>();
             for(E edge:internalGetIncomingEdges(vertex))
                 neighbors.add(this.getSource(edge));
             for(E edge:getInternalOutgoingEdges(vertex))
                 neighbors.add(this.getDest(edge));
             result=Collections.unmodifiableCollection(neighbors);
            }
        return(result);
    }

    public Collection<E> getIncidentEdges(V vertex){
    	final Collection<E> incident;
    	if(!containsVertex(vertex))
    		incident=null;
    	else
    	    {incident = new HashSet<E>();
             incident.addAll(internalGetIncomingEdges(vertex));
             incident.addAll(getInternalOutgoingEdges(vertex));
    	    }
        return incident;
    }

    public E findEdge(V v1,V v2){
    	E foundEdge=null;
        if(containsVertex(v1)&&containsVertex(v2))
            for(E edge:getInternalOutgoingEdges(v1))
                if(this.getDest(edge).equals(v2))
            	    {foundEdge=edge;
            	     break;
            	    }
        return(foundEdge);
    }
    
    public V getSource(E edge){
    	final V sourceVertex;
        if(!containsEdge(edge))
        	sourceVertex=null;
        else
        	sourceVertex=this.getVertices(edge).getFirst();
        return(sourceVertex);
    }

    public V getDest(E edge){
    	final V destinationVertex;
        if(!containsEdge(edge))
        	destinationVertex=null;
        else
        	destinationVertex=this.getVertices(edge).getSecond();
        return(destinationVertex);
    }

    public boolean isSource(V vertex,E edge){
    	final boolean result=containsEdge(edge)&&containsVertex(vertex)&&
    			vertex.equals(this.getVertices(edge).getFirst());
        return(result);
    }

    public boolean isDest(V vertex,E edge){
    	final boolean result=containsEdge(edge)&&containsVertex(vertex)&&
    			vertex.equals(this.getVertices(edge).getSecond());
        return(result);
    }
}
