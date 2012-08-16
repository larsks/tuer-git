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

/**
 * Object used to traverse the connected component of a directed graph
 * 
 * @author Julien Gouesse
 *
 */
public abstract class DirectedConnectedComponentVisitor<V,E,G extends DirectedGraph<V,E>> extends 
                      Visitor<V,E,G>{

	/**
	 * Constructor
	 */
	public DirectedConnectedComponentVisitor(){
		super();
	}
	
	@Override
	protected Collection<V> getNextTraversableVertices(final G graph,final V currentlyVisitedElement){
		final Collection<V> successors=graph.getSuccessors(currentlyVisitedElement);
		return(successors);
	}
}
