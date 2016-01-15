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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Weakly connected graph composed of directed edges and vertices that have 
 * exactly one incoming (or entering) vertex except the root that has no 
 * incoming vertex, that does not allow self-loops, parallel edges and cycles, 
 * i.e a directed rooted tree
 * 
 * @author Julien Gouesse
 *
 * @param <V> vertex class
 * @param <E> edge class
 */
public class DirectedRootedTree<V,E> extends DirectedAcyclicGraph<V,E>{

	protected V root;
	
	/**
	 * Constructor
	 * 
	 * @param ordered flag indicating whether the vertices and the edges are 
	 * stored in a way that preserves the order by insertion time
	 */
	public DirectedRootedTree(final boolean ordered){
		super(ordered);
	}
	
	public V getRoot(){
		return(root);
	}
	
	//TODO add a method to create a tree with a subset of vertices
	
	/* (non-Javadoc)
	 * @see jfpsm.graph.DirectedGraph#isEdgeAdditionValid(java.lang.Object, jfpsm.graph.Pair)
	 */
	@Override
	protected boolean isEdgeAdditionValid(E edge,Pair<V> vertices){
		boolean result=super.isEdgeAdditionValid(edge,vertices);
		if(result)
		    {if(getVertexCount()>0)
		    	 {final V firstVertex=vertices.getFirst();
				  final V secondVertex=vertices.getSecond();
		    	  if(getEdgeCount()>0)
		    	      {/**
		    		    * there is no need to check whether the second vertex
		    		    * is already an ancestor of the first vertex as such a 
		    		    * loop would be detected in the super class
		    		    */
		    		   if(containsVertex(secondVertex))
		    		       if(containsVertex(firstVertex))
		    		           {//the new edge would create a loop
		    		            result=false;
		    		           }
		    		       else
		    		           {final int incomingEdgesCount=
		    		            internalGetIncomingEdges(secondVertex).size();
		    	    	        /**
		    	    	         * the vertices (except the root) must have 
		    	    	         * exactly one incoming vertex after the 
		    	    	         * addition, i.e this addition is possible only 
		    	    	         * if the second vertex is the root
		    	    	         */
		    		            result=incomingEdgesCount==0;
		    		            /**
		    		             * if the addition is successful, the first 
		    		             * vertex will become the root
		    		             */
		    		           }
		    		   else
		    			   if(containsVertex(firstVertex))
		    		           {/**
		    				     * the new edge is connected to an existing 
		    				     * vertex
		    				     */
		    				    result=true;
		    		           }
		    			   else
		    			       {
		    				    //the new edge is completely disconnected
		    				    result=false;
		    			       }
		    	      }
		    	  else
		    	      {//there is no edge, there is only the root
		    		   final V vertex=this.vertices.keySet().iterator().next();
		    		   result=firstVertex.equals(vertex)||
		    				  secondVertex.equals(vertex);
		    		   /**
		    		    * if the current first vertex is not yet the root, it 
		    		    * will become the root
		    		    */
		    	      }
		    	 }
		     else
		         {/**
		           * the first vertex becomes the root and the second vertex 
		           * becomes its child
		           */
		    	  result=true;
		         }
		    }
		return(result);
	}
	
	@Override
	public boolean addEdge(E edge,Pair<? extends V> vertices){
        Pair<V> newVertices=getValidatedVertices(edge,vertices);
        final boolean success=newVertices!=null&&
        		isEdgeAdditionValid(edge,newVertices);
        if(success)
            {edges.put(edge,newVertices);
        
             @SuppressWarnings("null")
			 V source = newVertices.getFirst();
             V dest = newVertices.getSecond();

             if(!containsVertex(source))
                 if(super.addVertex(source))
                	 root=source;
        
             if(!containsVertex(dest))
                 super.addVertex(dest);
        
             internalGetIncomingEdges(dest).add(edge);
             internalGetOutgoingEdges(source).add(edge);
            }
        return(success);
	}
	
	/**
	 * Sets the new root of this tree if and only if there isn't one yet
	 * 
	 * @param vertex new root
	 */
	@Override
	public boolean addVertex(V vertex){
		final boolean success;
		if(vertices.isEmpty())
		    {//tries to add this vertex as the graph is empty
			 success=super.addVertex(vertex);
			 /**
			  * if it works, this vertex becomes the root
			  */
			 if(success)
				 root=vertex;
		    }
		else
			{/**
			  * there is already at least one vertex, another disconnected one
			  * cannot be added
			  */
			 success=false;
			}
		return(success);
	}
	
	@Override
	public boolean removeEdge(E edge){
		boolean success=containsEdge(edge);
		if(success)
		    {final Pair<V> vertices=getVertices(edge);
			 final V secondVertex=vertices.getSecond();
			 success=removeVertex(secondVertex);
		    }
		return(success);
	}
	
	@Override
	public boolean removeVertex(V vertex){
    	final boolean success=containsVertex(vertex);
        if(success)
            {Collection<E> incomingEdges=internalGetIncomingEdges(vertex);
        	 if(!incomingEdges.isEmpty())
        	     {//this vertex is not the root
        	      /**
        	       * uses a visitor (BFS) to get its direct and indirect 
        	       * successors
        	       */
        		  final DirectedConnectedComponentVisitorWithHistory<V,E,DirectedRootedTree<V,E>> 
        		  visitor=new DirectedConnectedComponentVisitorWithHistory<>();
        		  visitor.visit(this,vertex,true);
        		  final List<V> directAndIndirectsuccessors=new ArrayList<>();
        		  directAndIndirectsuccessors.addAll(visitor.getVisitedVertices());
        		  /**
        		   * starts using this set by the end, reverses the order of 
        		   * the vertices
        		   */
        		  Collections.reverse(directAndIndirectsuccessors);
        		  //removes all its successors and so on...
        		  for(V vertexToRemove:directAndIndirectsuccessors)
        		      {//gets the incoming edges (there should be only one)
        			   incomingEdges=internalGetIncomingEdges(vertexToRemove);
        			   //gets the edge coming from its parent
        			   final E edgeFromParent=incomingEdges.iterator().next();
        			   //removes the edge coming from its parent
        			   super.removeEdge(edgeFromParent);
        			   //removes the vertex
            	       vertices.remove(vertexToRemove);
        		      }
        	     }
        	 else
        	     {//this vertex is the root, the tree must be emptied
        		  edges.clear();
        		  vertices.clear();
        		  root=null;
        	     }
            }
        return(success);
    }
}
