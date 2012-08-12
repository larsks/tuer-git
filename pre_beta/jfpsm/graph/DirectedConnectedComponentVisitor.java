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

/**
 * Object used to traverse the connected component of a directed graph
 * 
 * @author Julien Gouesse
 *
 */
public abstract class DirectedConnectedComponentVisitor<V,E>{

	/**
	 * Constructor
	 */
	public DirectedConnectedComponentVisitor(){
		super();
	}
	
	public boolean visit(final DirectedGraph<V,E> graph,
			final V firstElementToVisit,
			final boolean breadthFirstSearchEnabled){
        final ArrayList<V> markedChildrenList=new ArrayList<V>();
        final ArrayList<V> queueOrStack=new ArrayList<V>();
        markedChildrenList.add(firstElementToVisit);
        queueOrStack.add(firstElementToVisit);
        boolean mustGoOn=true;
        while(!queueOrStack.isEmpty())
            {//gets the next element (pop operation)
             final V currentlyVisitedElement=queueOrStack.remove(breadthFirstSearchEnabled?0:queueOrStack.size()-1);
             //performs the main operation and tells whether the traversal must go on
             if(mustGoOn=performOnCurrentlyVisitedElement(currentlyVisitedElement))
                 for(V successor:graph.getSuccessors(currentlyVisitedElement))
                     {if(!markedChildrenList.contains(successor))
                          {//marks the element to avoid traveling it more than once
                           markedChildrenList.add(successor);
                           //adds a new element to traverse (push operation)
                           queueOrStack.add(successor);
                          }
                     }
            }
        return(mustGoOn);
    }

    public abstract boolean performOnCurrentlyVisitedElement(final V currentlyVisitedElement);

}
