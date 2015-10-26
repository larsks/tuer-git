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
package jfpsm;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Objects;

/**
 * Helpers to manipulate arrays
 * 
 * @author Julien Gouesse
 *
 */
public class ArrayHelper{

	public ArrayHelper(){
		super();
	}
	
	/**
	 * Occupancy map, structure representing the occupation of an array, can be ragged
	 * 
	 * @author gouessej
	 *
	 */
	public static final class OccupancyMap{
		
		private final boolean[][] arrayMap;
		
		private final int smallestRowIndex;
		
		private final int biggestRowIndex;
		
		private final int smallestColumnIndex;
        
		private final int biggestColumnIndex;
        
		private final int rowCount;
        
		private final int columnCount;
		
		public OccupancyMap(final boolean[][] arrayMap,final int smallestRowIndex,final int biggestRowIndex,
				            final int smallestColumnIndex,final int biggestColumnIndex,
				            final int rowCount,final int columnCount){
			this.arrayMap=arrayMap;
			this.smallestRowIndex=smallestRowIndex;
			this.biggestRowIndex=biggestRowIndex;
			this.smallestColumnIndex=smallestColumnIndex;
			this.biggestColumnIndex=biggestColumnIndex;
			this.rowCount=rowCount;
			this.columnCount=columnCount;
		}

		public final boolean[][] getArrayMap(){
			return(arrayMap);
		}

		public final int getSmallestRowIndex(){
			return(smallestRowIndex);
		}

		public final int getBiggestRowIndex(){
			return(biggestRowIndex);
		}

		public final int getSmallestColumnIndex(){
			return(smallestColumnIndex);
		}

		public final int getBiggestColumnIndex(){
			return(biggestColumnIndex);
		}

		public final int getRowCount(){
			return(rowCount);
		}

		public final int getColumnCount(){
			return(columnCount);
		}
		
		public final boolean isEmpty(){
			return(rowCount==0||columnCount==0);
		}
	}

	public static interface OccupancyCheck<T>{
		public boolean isOccupied(T value);
	}
	
	/**
	 * Returns a textual representation of a 2D array, can be ragged
	 * 
	 * @param array 2D array
	 * @param useObjectToString indicates whether to use Object.toString() to build the textual representation of an object, uses 'X' and ' ' if <code>false</code>
	 * @param occupancyCheck occupancy check, tells whether the object "occupies" the array cell, can be null. If <code>null</code>, the cell isn't occupied if it contains <code>null</code>
	 * @return textual representation of a 2D array
	 */
	public <T> String toString(final T[][] array,final boolean useObjectToString,final OccupancyCheck<T> occupancyCheck){
		final StringBuilder builder=new StringBuilder();
		//computes the maximum number of rows in the columns (to handle the jagged arrays)
		int maxColumnRowCount=0;
		//for each column, i.e for each abscissa
		for(int x=0;x<array.length;x++)
			if(array[x]!=null)
				maxColumnRowCount=Math.max(maxColumnRowCount,array[x].length);
		//for each row, i.e for each ordinate
		for(int y=0;y<maxColumnRowCount;y++)
		    {//for each column, i.e for each abscissa
			 for(int x=0;x<array.length;x++)
				 if(array[x]!=null&&y<array[x].length)
			         {final T value=array[x][y];
			          if(useObjectToString)
			              {if(value==null||(occupancyCheck!=null&&!occupancyCheck.isOccupied(value)))
				    	       builder.append("[null]");
				           else
				    	       builder.append('[').append(Objects.toString(value)).append(']');
			              }
			          else
				          {if(value==null||(occupancyCheck!=null&&!occupancyCheck.isOccupied(value)))
				    	       builder.append("[ ]");
				           else
				    	       builder.append("[X]");
				          }
			         }
			 //end of row
			 builder.append('\n');
		    }
		return(builder.toString());
	}
	
	/**
	 * Creates the occupancy map of an array
	 * 
	 * @param array array whose occupancy has to be computed
	 * @return occupancy map of an array
	 * 
	 * TODO support OccupancyCheck
	 * FIXME rowCount and columnCount are wrong, rows and columns are in the wrong order even though the occupancy array is correct
	 */
	public <T> OccupancyMap createPackedOccupancyMap(final T[][] array){
		//detects empty rows and empty columns in order to skip them later
		int smallestI=Integer.MAX_VALUE;
		int biggestI=Integer.MIN_VALUE;
		int smallestJ=Integer.MAX_VALUE;
		int biggestJ=Integer.MIN_VALUE;
		//checks if the array has at least one row
		if(array.length>0)
			{//looks for biggestI
			 boolean searchStopped=false;
			 for(int i=array.length-1;i>=0&&!searchStopped;i--)
			     if(array[i]!=null&&array[i].length>0)
			 		 for(int j=array[i].length-1;j>=0&&!searchStopped;j--)
			 			 if(array[i][j]!=null)
			 		         {//correct value
			 			      biggestI=i;
			 			      //candidates
			 		          smallestI=i;
			 		          smallestJ=j;
			 		          biggestJ=j;
			 		          //uses this flag to stop the search
			 		          searchStopped=true;
			 		         }
			 //checks if the array has at least one non empty row
			 if(searchStopped)
			     {//looks for smallestI
				  searchStopped=false;
				  for(int i=0;i<biggestI&&!searchStopped;i++)
				      if(array[i]!=null)
				          for(int j=0;j<array[i].length&&!searchStopped;j++)
				        	  if(array[i][j]!=null)
				                  {//correct value
				                   smallestI=i;
				                   //candidates
				                   smallestJ=Math.min(smallestJ,j);
				  			       biggestJ=Math.max(biggestJ,j);
				  			       //uses this flag to stop the search
				  			       searchStopped=true;
				                  }
				  //looks for biggestJ
				  searchStopped=false;
				  if(biggestJ<Integer.MAX_VALUE)
				      {for(int i=biggestI-1;i>=smallestI&&!searchStopped;i--)
				           if(array[i]!=null&&array[i].length>biggestJ+1)
				               {for(int j=array[i].length-1;j>biggestJ&&!searchStopped;j--)
				                    if(array[i][j]!=null)
				                        biggestJ=j;
				                    if(biggestJ==Integer.MAX_VALUE)
				                        searchStopped=true;
                               }
				      }
				  //looks for smallestJ
				  searchStopped=false;
				  if(smallestJ>0)
				      {for(int i=smallestI+1;i<=biggestI&&!searchStopped;i++)
				           if(array[i]!=null&&array[i].length>0)
				               {for(int j=0;j<smallestJ&&!searchStopped;j++)
				                    if(array[i][j]!=null)
				                        smallestJ=j;
				                    if(smallestJ==0)
				                        searchStopped=true;
				               }
				      }
			     }
		    }
		//N.B: row-major convention
		final int rowCount=biggestI>=smallestI?biggestI-smallestI+1:0;//this is equal to the "length" of the occupancy map
		final int columnCount=biggestJ>=smallestJ?biggestJ-smallestJ+1:0;
		final boolean[][] occupancyMapArray;
		//if the array is not empty
		if(rowCount>0&&columnCount>0)
		    {//creates an occupancy map of the supplied array but without empty columns and rows
		     occupancyMapArray=new boolean[rowCount][];
		     //for each row
			 for(int i=0;i<occupancyMapArray.length;i++)
				 {//computes the index in the original array by using the offset
				  final int rawI=i+smallestI;
				  if(array[rawI]!=null)
				      {//starts the computation of the biggest index of the current column
					   int localBiggestJ=Integer.MIN_VALUE;
					   for(int j=0;j<columnCount;j++)
					       {//computes the index in the original array by using the offset
					    	final int rawJ=j+smallestJ;
					        if(array[rawI][rawJ]!=null)
					        	localBiggestJ=rawJ;
					       }
					   final int localColumnCount=localBiggestJ>=smallestJ?localBiggestJ-smallestJ+1:0;
					   //allocates the current column of the occupancy map as tightly as possible
					   occupancyMapArray[i]=new boolean[localColumnCount];
					   //fills the occupancy map (true <-> not null)
				       for(int j=0;j<occupancyMapArray[i].length;j++)
					       {final int rawJ=j+smallestJ;
					        occupancyMapArray[i][j]=array[rawI][rawJ]!=null;
					       }
				      }
                 }
			}
		else
			occupancyMapArray=new boolean[0][0];
		final OccupancyMap occupancyMap=new OccupancyMap(occupancyMapArray,smallestI,biggestI,smallestJ,biggestJ,rowCount,columnCount);
		return(occupancyMap);
	}
	
	/**
	 * Creates a list of full arrays from a potentially non full array. It tries to 
	 * minimize the count of full arrays and to maximize their size.
	 * 
	 * @param array potentially non full array
	 * @return list of full arrays
	 */
	public <T> ArrayList<T[][]> computeFullArraysFromNonFullArray(final T[][] array){
		//creates an occupancy map that will be updated (instead of modifying the supplied array)
		final OccupancyMap occupancyMapObj=createPackedOccupancyMap(array);
		final ArrayList<T[][]> adjacentTrisArraysList=new ArrayList<>();
		//if the array isn't empty (then the occupancy map isn't empty)
		if(!occupancyMapObj.isEmpty())
		    {final int smallestI=occupancyMapObj.getSmallestRowIndex();
		     final int smallestJ=occupancyMapObj.getSmallestColumnIndex();
		     final int rowCount=occupancyMapObj.getRowCount();
		     final int columnCount=occupancyMapObj.getColumnCount();
			 final boolean[][] occupancyMap=occupancyMapObj.getArrayMap();
		     /**
		      * As Java is unable to create a generic array by directly using the generic type, 
		      * it is necessary to retrieve it thanks to the reflection
		      */
		     final Class<?> arrayComponentType=array.getClass().getComponentType().getComponentType();
		     //finds the isolated sets of adjacent triangles that could be used to create quads
		     //the secondary size is the least important size of the chunk
		     for(int secondarySize=1;secondarySize<=Math.max(rowCount,columnCount);secondarySize++)
		    	 //the primary size is the most important size of the chunk
		    	 for(int primarySize=1;primarySize<=Math.max(rowCount,columnCount);primarySize++)
		             {//for each row
		    		  for(int i=0;i<occupancyMap.length;i++)
		    			  if(occupancyMap[i]!=null)
		    		          //for each column
		    			      for(int j=0;j<occupancyMap[i].length;j++)
		    		              {//if this element is occupied
		    				       if(occupancyMap[i][j])
		    		                   {//looks for an isolated element
		    				    	    //vertical checks (columns)
		    		    	            if(primarySize+i<=rowCount&&secondarySize<=columnCount&&
		    		    	               isRectangularSubSectionLocallyIsolated(occupancyMap,rowCount,columnCount,i,j,primarySize,secondarySize,true)&&
		    		    		          (j-1<0||!isRectangularSubSectionLocallyIsolated(occupancyMap,rowCount,columnCount,i,j-1,primarySize,1,true))&&
		    		    		          (j+secondarySize>=columnCount||!isRectangularSubSectionLocallyIsolated(occupancyMap,rowCount,columnCount,i,j+secondarySize,primarySize,1,true)))
		    		    	                {@SuppressWarnings("unchecked")
										     final T[][] adjacentTrisSubArray=(T[][])Array.newInstance(arrayComponentType,primarySize,secondarySize);
		    		    	                 //adds it into the returned list
		    		    	                 adjacentTrisArraysList.add(adjacentTrisSubArray);
		    		    	                 //copies the elements of the chunk into the sub-array and marks them as removed from the occupancy map
		    		    	                 for(int ii=0;ii<primarySize;ii++)
		    		    		                 for(int jj=0;jj<secondarySize;jj++)
		    		    		                     {adjacentTrisSubArray[ii][jj]=array[ii+i+smallestI][jj+j+smallestJ];
		    		    		                      occupancyMap[ii+i][jj+j]=false;
		    		    		                     }
		    		    	                }
		    		    	            else
		    		    	                {//horizontal checks (rows)
		    		    	                 if(primarySize+j<=columnCount&&secondarySize<=rowCount&&
		    		    	                    isRectangularSubSectionLocallyIsolated(occupancyMap,rowCount,columnCount,i,j,primarySize,secondarySize,false)&&
		    		 			 	           (i-1<0||!isRectangularSubSectionLocallyIsolated(occupancyMap,rowCount,columnCount,i-1,j,primarySize,1,false))&&
		    		 			 	           (i+secondarySize>=rowCount||!isRectangularSubSectionLocallyIsolated(occupancyMap,rowCount,columnCount,i+secondarySize,j,primarySize,1,false)))
		    		    	                     {@SuppressWarnings("unchecked")
		    		    	            	      final T[][] adjacentTrisSubArray=(T[][])Array.newInstance(arrayComponentType,secondarySize,primarySize);
		   	 			                          //adds it into the returned list
		   	 			                          adjacentTrisArraysList.add(adjacentTrisSubArray);
		   	 			                          //copies the elements of the chunk into the sub-array and marks them as removed from the occupancy map
		   	 			                          for(int jj=0;jj<primarySize;jj++)
		   	 			                	          for(int ii=0;ii<secondarySize;ii++)
		   	 			                	        	  try
		   	 			                		              {adjacentTrisSubArray[ii][jj]=array[ii+i+smallestI][jj+j+smallestJ];
		   			                		                   occupancyMap[ii+i][jj+j]=false;
		   			                		                  }
		   	 			                                  catch(final ArrayIndexOutOfBoundsException aioobe)
		   	 			                                      {aioobe.printStackTrace();}
		    		    	                     }
		    		    	                }
		    		                   }
		    		              }
		             }
		    }
		return(adjacentTrisArraysList);
	}

	/**
	 * Tells whether a rectangular subsection of the supplied array is locally isolated, i.e it is full
	 * and its close neighboring is empty
	 * 
	 * @param array array containing the subsection
	 * @param rowCount row count (may be greater than the row count of the supplied array)
	 * @param columnCount column count (may be greater than the column count of the supplied array)
	 * @param i lowest row index of the subsection 
	 * @param j lowest column index of the subsection
	 * @param primarySize row count of the subsection if testOnColumnIsolationEnabled is true, otherwise column count
	 * @param secondarySize column count of the subsection if testOnColumnIsolationEnabled is true, otherwise row count
	 * @param testOnColumnIsolationEnabled true if the test checks whether the isolation of this subsection is tested as a column, otherwise it is tested as a row
	 * @return
	 */
	public boolean isRectangularSubSectionLocallyIsolated(final boolean[][] array,final int rowCount,final int columnCount,
			final int i,final int j,final int primarySize,final int secondarySize,
			final boolean testOnColumnIsolationEnabled){
		boolean isolated;
	    if(0<=i&&i<array.length&&array[i]!=null&&0<=j&&j<array[i].length&&array[i][j])
	        {if(testOnColumnIsolationEnabled)
	             {isolated=true;
	              for(int ii=Math.max(0,i-1);ii<=i+primarySize&&ii<rowCount&&isolated;ii++)
	                  for(int jj=Math.max(0,j);jj<j+secondarySize&&jj<columnCount&&isolated;jj++)
	            	      if((((ii==i-1)||(ii==i+primarySize))&&(ii<array.length&&array[ii]!=null&&jj<array[ii].length&&array[ii][jj]))||((i-1<ii)&&(ii<i+primarySize)&&(ii>=array.length||array[ii]==null||jj>=array[ii].length||!array[ii][jj])))
	            	    	  isolated=false;
	             }
	         else
	             {isolated=true;
	              for(int ii=Math.max(0,i);ii<i+secondarySize&&ii<rowCount&&isolated;ii++)
	                  for(int jj=Math.max(0,j-1);jj<=j+primarySize&&jj<columnCount&&isolated;jj++)
	            		  if((((jj==j-1)||(jj==j+primarySize))&&(ii<array.length&&array[ii]!=null&&jj<array[ii].length&&array[ii][jj]))||((j-1<jj)&&(jj<j+primarySize)&&(ii>=array.length||array[ii]==null||jj>=array[ii].length||!array[ii][jj])))
	            	    	  isolated=false;
	             }
	        }
	    else
	    	isolated=false;
	    return(isolated);
	}
	
	/**
	 * Tells whether a rectangular subsection of the supplied array is locally isolated, i.e it is full
	 * and its close neighboring is empty
	 * 
	 * @param array array containing the subsection
	 * @param rowCount row count (may be greater than the row count of the supplied array)
	 * @param columnCount column count (may be greater than the column count of the supplied array)
	 * @param i lowest row index of the subsection 
	 * @param j lowest column index of the subsection
	 * @param primarySize row count of the subsection if testOnColumnIsolationEnabled is true, otherwise column count
	 * @param secondarySize column count of the subsection if testOnColumnIsolationEnabled is true, otherwise row count
	 * @param testOnColumnIsolationEnabled true if the test checks whether the isolation of this subsection is tested as a column, otherwise it is tested as a row
	 * @return
	 */
	public <T> boolean isRectangularSubSectionLocallyIsolated(final T[][] array,final int rowCount,final int columnCount,
			final int i,final int j,final int primarySize,final int secondarySize,
			final boolean testOnColumnIsolationEnabled){
		boolean isolated;
	    if(0<=i&&i<array.length&&array[i]!=null&&0<=j&&j<array[i].length&&array[i][j]!=null)
	        {if(testOnColumnIsolationEnabled)
	             {isolated=true;
	              for(int ii=Math.max(0,i-1);ii<=i+primarySize&&ii<rowCount&&isolated;ii++)
	                  for(int jj=Math.max(0,j);jj<j+secondarySize&&jj<columnCount&&isolated;jj++)
	            	      if((((ii==i-1)||(ii==i+primarySize))&&(ii<array.length&&array[ii]!=null&&jj<array[ii].length&&array[ii][jj]!=null))||((i-1<ii)&&(ii<i+primarySize)&&(ii>=array.length||array[ii]==null||jj>=array[ii].length||array[ii][jj]==null)))
	            	    	  isolated=false;
	             }
	         else
	             {isolated=true;
	              for(int ii=Math.max(0,i);ii<i+secondarySize&&ii<rowCount&&isolated;ii++)
	                  for(int jj=Math.max(0,j-1);jj<=j+primarySize&&jj<columnCount&&isolated;jj++)
	            		  if((((jj==j-1)||(jj==j+primarySize))&&(ii<array.length&&array[ii]!=null&&jj<array[ii].length&&array[ii][jj]!=null))||((j-1<jj)&&(jj<j+primarySize)&&(ii>=array.length||array[ii]==null||jj>=array[ii].length||array[ii][jj]==null)))
	            	    	  isolated=false;
	             }
	        }
	    else
	    	isolated=false;
	    return(isolated);
	}
}
