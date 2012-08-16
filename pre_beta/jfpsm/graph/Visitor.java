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

import java.util.ArrayList;
import java.util.Collection;

/**
 * Object used to traverse a graph
 * 
 * @author Julien Gouesse
 *
 */
public abstract class Visitor<V,E>{

	/**
	 * Constructor
	 */
	public Visitor(){
		super();
	}

	/**
	 * Traverses the supplied graph
	 * 
	 * @param graph the traversed graph
	 * @param firstVertexToVisit first traversed vertex
	 * @param breadthFirstSearchEnabled <code>true</code> if the breadth first 
	 * search is enabled, <code>false</code> if the depth first search is 
	 * enabled
	 * @return <code>true</code> if the traversal has not been interrupted 
	 * (see {@link Visitor#performOnCurrentlyVisitedVertex(Object)}), 
	 * otherwise <code>false</code>
	 */
	public boolean visit(final DirectedGraph<V,E> graph,
			final V firstVertexToVisit,
			final boolean breadthFirstSearchEnabled){
        final ArrayList<V> markedChildrenList=new ArrayList<V>();
        final ArrayList<V> queueOrStack=new ArrayList<V>();
        markedChildrenList.add(firstVertexToVisit);
        queueOrStack.add(firstVertexToVisit);
        boolean mustGoOn=true;
        while(!queueOrStack.isEmpty())
            {//gets the next vertex (pop operation)
             final V currentlyVisitedVertex=queueOrStack.remove(breadthFirstSearchEnabled?0:queueOrStack.size()-1);
             //performs the main operation and tells whether the traversal must go on
             if(mustGoOn=performOnCurrentlyVisitedVertex(graph,currentlyVisitedVertex))
                 for(V successor:getNextTraversableVertices(graph,currentlyVisitedVertex))
                     {if(!markedChildrenList.contains(successor))
                          {//marks the vertex to avoid traveling it more than once
                           markedChildrenList.add(successor);
                           //adds a new vertex to traverse (push operation)
                           queueOrStack.add(successor);
                          }
                     }
            }
        return(mustGoOn);
    }
	
	/**
	 * Gets the next vertices linked to the currently visited vertex that are 
	 * going to be traversed
	 * 
	 * @param graph the traversed graph
	 * @param currentlyVisitedVertex the currently visited vertex
	 * @return the next vertices to traverse
	 */
	protected abstract Collection<V> getNextTraversableVertices(
			final DirectedGraph<V,E> graph,final V currentlyVisitedVertex);

	/**
	 * Performs an operation on the currently visited vertex
	 * 
	 * @param graph the traversed graph
	 * @param currentlyVisitedVertex currently visited vertex
	 * @return <code>true</code> if the traversal must go on, 
	 * <code>false</code> if it must be interrupted
	 */
	protected abstract boolean performOnCurrentlyVisitedVertex(
			final DirectedGraph<V,E> graph,final V currentlyVisitedVertex);
}
