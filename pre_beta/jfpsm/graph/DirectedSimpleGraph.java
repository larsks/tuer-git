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

/**
 * Graph composed of vertices and directed edges, that does not allow 
 * self-loops and parallel edges
 * 
 * @author Julien Gouesse
 *
 * @param <V> vertex class
 * @param <E> edge class
 */
public class DirectedSimpleGraph<V,E> extends DirectedGraph<V,E>{

	/**
	 * Constructor
	 * 
	 * @param ordered flag indicating whether the vertices and the edges are 
	 * stored in a way that preserves the order by insertion time
	 */
	public DirectedSimpleGraph(final boolean ordered){
		super(ordered);
	}

}
