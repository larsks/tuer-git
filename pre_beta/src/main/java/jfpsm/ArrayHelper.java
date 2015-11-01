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
import java.util.LinkedHashMap;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.logging.Logger;

/**
 * Helper to manipulate arrays
 * 
 * @author Julien Gouesse
 *
 */
public class ArrayHelper{
	
	private static final Logger logger=Logger.getLogger(ArrayHelper.class.getName());

	/**
	 * Default constructor
	 */
	public ArrayHelper(){
		super();
	}
	
	public static final class Vector2i{
		
		private final int x;
		
		private final int y;
		
		public Vector2i(final int x,final int y){
			super();
			this.x=x;
			this.y=y;
		}
		
		public int getX(){
			return(x);
		}
		
		public int getY(){
			return(y);
		}
		
		@Override
		public int hashCode(){
			final int prime=31;
			int result=1;
			result=prime*result+x;
			result=prime*result+y;
			return result;
		}

		@Override
		public boolean equals(final Object other){
			return(this==other||(other!=null&&other.getClass()==Vector2i.class&&x==((Vector2i)other).x&&y==((Vector2i)other).y));
		}
	}
	
	/**
	 * Occupancy map, structure representing the occupation of an array, can be ragged
	 */
	public static final class OccupancyMap{
		
		/**
		 * array map that indicates which cells are occupied
		 */
		private final boolean[][] arrayMap;
		
		/**
		 * smallest row index, i.e minimum ordinate
		 */
		private final int smallestRowIndex;
		
		/**
		 * biggest row index, i.e maximum ordinate
		 */
		private final int biggestRowIndex;
		
		/**
		 * smallest column index, i.e minimum abscissa
		 */
		private final int smallestColumnIndex;
        
		/**
		 * biggest column index, i.e maximum abscissa
		 */
		private final int biggestColumnIndex;
        
		/**
		 * row count
		 */
		private final int rowCount;
        
		/**
		 * column count
		 */
		private final int columnCount;
		
		/**
		 * Constructor
		 * 
		 * @param arrayMap array map that indicates which cells are occupied
		 * @param smallestRowIndex smallest row index, i.e minimum ordinate
		 * @param biggestRowIndex biggest row index, i.e maximum ordinate
		 * @param smallestColumnIndex smallest column index, i.e minimum abscissa
		 * @param biggestColumnIndex biggest column index, i.e maximum abscissa
		 * @param rowCount row count
		 * @param columnCount column count
		 */
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

		/**
		 * Returns the array map that indicates which cells are occupied
		 * 
		 * @return
		 */
		public final boolean[][] getArrayMap(){
			return(arrayMap);
		}

		/**
		 * Returns the smallest row index, i.e minimum ordinate
		 * 
		 * @return smallest row index, i.e minimum ordinate
		 */
		public final int getSmallestRowIndex(){
			return(smallestRowIndex);
		}

		/**
		 * Returns the biggest row index, i.e maximum ordinate
		 * 
		 * @return biggest row index, i.e maximum ordinate
		 */
		public final int getBiggestRowIndex(){
			return(biggestRowIndex);
		}

		/**
		 * Returns the smallest column index, i.e minimum abscissa
		 * 
		 * @return smallest column index, i.e minimum abscissa
		 */
		public final int getSmallestColumnIndex(){
			return(smallestColumnIndex);
		}

		/**
		 * Returns the biggest column index, i.e maximum abscissa
		 * 
		 * @return biggest column index, i.e maximum abscissa
		 */
		public final int getBiggestColumnIndex(){
			return(biggestColumnIndex);
		}

		/**
		 * Returns the row count
		 * 
		 * @return row count
		 */
		public final int getRowCount(){
			return(rowCount);
		}

		/**
		 * Returns the column count
		 * 
		 * @return column count
		 */
		public final int getColumnCount(){
			return(columnCount);
		}
		
		/**
		 * Tells whether the occupancy array is empty
		 * 
		 * @return <code>true</code> if the occupancy array is empty, otherwise <code>false</code>
		 */
		public final boolean isEmpty(){
			return(rowCount==0||columnCount==0);
		}
	}

	/**
	 * Check of occupancy for a 2D array
	 *
	 * @param <T> type of the value occupying a cell of an array
	 */
	public static interface OccupancyCheck<T>{
		/**
		 * Tells whether the value is considered as occupying a cell array
		 * 
		 * @param value value in the array cell
		 * @return <code>true</code> if the value is considered as occupying a cell array, otherwise <code>false</code>
		 */
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
		//computes the maximum number of rows in the columns (to handle the ragged arrays)
		int maxColumnRowCount=0;
		//for each column, i.e for each abscissa
		for(int x=0;x<array.length;x++)
			if(array[x]!=null)
				maxColumnRowCount=Math.max(maxColumnRowCount,array[x].length);
		//for each row, i.e for each ordinate
		for(int y=0;y<maxColumnRowCount;y++)
		    {//for each column, i.e for each abscissa
			 for(int x=0;x<array.length;x++)
				 {if(array[x]!=null&&y<array[x].length)
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
	 */
	public <T> OccupancyMap createPackedOccupancyMap(final T[][] array){
		//detects empty rows and empty columns in order to skip them later
		int smallestRowIndex=Integer.MAX_VALUE;
		int biggestRowIndex=Integer.MIN_VALUE;
		int smallestColumnIndex=Integer.MAX_VALUE;
		int biggestColumnIndex=Integer.MIN_VALUE;
		//checks if the array has at least one column
		if(array.length>0)
		    {//looks for the biggest column index
			 boolean searchStopped=false;
			 //for each column, i.e for each abscissa
			 for(int x=array.length-1;x>=0&&!searchStopped;x--)
				 if(array[x]!=null&&array[x].length>0)
					 //for each row, i.e for each ordinate
					 for(int y=array[x].length-1;y>=0&&!searchStopped;y--)
						 if(array[x][y]!=null)
			                 {//correct value
							  biggestColumnIndex=x;
							  //candidates
							  smallestColumnIndex=x;
							  smallestRowIndex=y;
							  biggestRowIndex=y;
							  //uses this flag to stop the search
			 		          searchStopped=true;
			                 }
			 //checks if the array has at least one non empty row
			 if(searchStopped)
			     {//looks for the smallest column index
				  searchStopped=false;
				  //for each column, i.e for each abscissa
				  for(int x=0;x<biggestColumnIndex&&!searchStopped;x++)
				      if(array[x]!=null)
				    	  //for each row, i.e for each ordinate
				          for(int y=0;y<array[x].length&&!searchStopped;y++)
				        	  if(array[x][y]!=null)
				                  {//correct value
				                   smallestColumnIndex=x;
				                   //candidates
				                   smallestRowIndex=Math.min(smallestRowIndex,y);
				  			       biggestRowIndex=Math.max(biggestRowIndex,y);
				  			       //uses this flag to stop the search
				  			       searchStopped=true;
				                  }
				  //looks for the biggest row index
				  searchStopped=false;
				  if(biggestRowIndex<Integer.MAX_VALUE)
				      {//for each column, i.e for each abscissa
					   for(int x=biggestColumnIndex-1;x>=smallestColumnIndex&&!searchStopped;x--)
				           if(array[x]!=null&&array[x].length>biggestRowIndex+1)
				               {//for each row, i.e for each ordinate
				        	    for(int y=array[x].length-1;y>biggestRowIndex&&!searchStopped;y--)
				                    if(array[x][y]!=null)
				                    	biggestRowIndex=y;
				                    if(biggestRowIndex==Integer.MAX_VALUE)
				                        searchStopped=true;
                               }
				      }
				  //looks for the smallest row index
				  searchStopped=false;
				  if(smallestRowIndex>0)
				      {//for each column, i.e for each abscissa
					   for(int x=smallestColumnIndex+1;x<=biggestColumnIndex&&!searchStopped;x++)
				           if(array[x]!=null&&array[x].length>0)
				               {//for each row, i.e for each ordinate
				        	    for(int y=0;y<smallestRowIndex&&!searchStopped;y++)
				                    if(array[x][y]!=null)
				                        smallestRowIndex=y;
				                    if(smallestRowIndex==0)
				                        searchStopped=true;
				               }
				      }
			     }
		    }
		final int rowCount=biggestRowIndex>=smallestRowIndex?biggestRowIndex-smallestRowIndex+1:0;
		final int columnCount=biggestColumnIndex>=smallestColumnIndex?biggestColumnIndex-smallestColumnIndex+1:0;
		final boolean[][] occupancyMapArray;
		//if the array is not empty
		if(rowCount>0&&columnCount>0)
		    {//creates an occupancy map of the supplied array but without empty columns and rows
			 occupancyMapArray=new boolean[columnCount][];
			 //for each column, i.e for each abscissa
			 for(int x=0;x<columnCount;x++)
			     {//computes the index in the original array by using the offset
				  final int rawX=x+smallestColumnIndex;
				  if(array[rawX]!=null)
				      {//starts the computation of the biggest index of the current row
					   int localBiggestRowIndex=Integer.MIN_VALUE;
					   //for each row, i.e for each ordinate
					   for(int y=0;y<rowCount;y++)
					       {//computes the index in the original array by using the offset
					    	final int rawY=y+smallestRowIndex;
					        if(array[rawX][rawY]!=null)
					        	localBiggestRowIndex=rawY;
					       }
					   final int localRowCount=localBiggestRowIndex>=smallestRowIndex?localBiggestRowIndex-smallestRowIndex+1:0;
					   //allocates the current column of the occupancy map as tightly as possible
					   occupancyMapArray[x]=new boolean[localRowCount];
					   //fills the occupancy map (true <-> not null)
				       for(int y=0;y<occupancyMapArray[x].length;y++)
					       {final int rawY=y+smallestRowIndex;
					        occupancyMapArray[x][y]=array[rawX][rawY]!=null;
					       }
				      }
			     }
		    }
		else
			occupancyMapArray=new boolean[0][0];
		final OccupancyMap occupancyMap=new OccupancyMap(occupancyMapArray,smallestRowIndex,biggestRowIndex,smallestColumnIndex,biggestColumnIndex,rowCount,columnCount);
		return(occupancyMap);
	}
	
	public <T> String toString(final java.util.Map<Vector2i,T[][]> fullArraysMap){
		return(toString(fullArraysMap,-1,-1));
	}
	
	/**
	 * 
	 * 
	 * @param fullArraysMap
	 * @param maxVisibleRowCount
	 * @param maxVisibleColumnCount
	 * @return
	 */
	public <T> String toString(final java.util.Map<Vector2i,T[][]> fullArraysMap,final int maxVisibleRowCount,final int maxVisibleColumnCount){
		//computes the size of the smallest non full arrays that could contain the full arrays
		int dataMaxRowCount=0;
		int dataMaxColumnCount=0;
		int validValuesCount=0;
		//for each full array of the map
		for(final Entry<Vector2i,T[][]> fullArrayEntry:fullArraysMap.entrySet())
			{final Vector2i location=fullArrayEntry.getKey();
			 final T[][] fullArray=fullArrayEntry.getValue();
			 //a map can support null values
			 if(fullArray!=null)
		         {final int xOffset=location==null?0:location.getX();
		          final int yOffset=location==null?0:location.getY();
				  //uses the location as an offset of the abscissa
				  dataMaxColumnCount=Math.max(dataMaxColumnCount,fullArray.length+xOffset);
		          //for each column
			      for(final T[] fullArrayColumm:fullArray)
			          {//if the column isn't null (Java supports the ragged arrays)
				       if(fullArrayColumm!=null)
					       {//the size of the column is used to compute the maximum row count, uses the location as an offset of the ordinate
					        dataMaxRowCount=Math.max(dataMaxRowCount,fullArrayColumm.length+yOffset);
					       }
			          }
			      validValuesCount++;
		         }
			}
		//computes the size of the visible array
		final int realMaxVisibleRowCount;
		if(maxVisibleRowCount<0)
			realMaxVisibleRowCount=dataMaxRowCount;
		else
			realMaxVisibleRowCount=maxVisibleRowCount;
		final int realMaxVisibleColumnCount;
		if(maxVisibleColumnCount<0)
			realMaxVisibleColumnCount=dataMaxColumnCount;
		else
			realMaxVisibleColumnCount=maxVisibleColumnCount;
		//computes the maximum size of the string used to represent a full array
		final int maxCharCountPerCell=validValuesCount==0?0:Integer.toString(validValuesCount-1).length();
		//builds a 2D array
		final String[][] stringNonFullArray=new String[realMaxVisibleColumnCount][realMaxVisibleRowCount];
		if(validValuesCount>0)
		    {//fills it by looping on the entry set
		     int validFullArrayIndex=0;
		     for(final Entry<Vector2i,T[][]> fullArrayEntry:fullArraysMap.entrySet())
		         {final Vector2i location=fullArrayEntry.getKey();
			      final T[][] fullArray=fullArrayEntry.getValue();
			      //a map can support null values and null keys
			      if(fullArray!=null)
		              {final int xOffset=location==null?0:location.getX();
		               final int yOffset=location==null?0:location.getY();
		               final String validFullArrayIndexString=Integer.toString(validFullArrayIndex);
		               final int validFullArrayIndexStringLength=validFullArrayIndexString.length();
		               //for each column, i.e for each abscissa
		               for(int x=0;x<fullArray.length;x++)
		        	       if(fullArray[x]!=null)
		                       {//for each row, i.e for each ordinate
		        	            for(int y=0;y<fullArray[x].length;y++)
		        	    	        if(fullArray[x][y]!=null&&0<=x+xOffset&&x+xOffset<stringNonFullArray.length&&0<=y+yOffset&&y+yOffset<stringNonFullArray[x+xOffset].length)
		        	                    {//checks whether the cell is already occupied
		        	    		         if(stringNonFullArray[x+xOffset][y+yOffset]!=null&&!stringNonFullArray[x+xOffset][y+yOffset].isEmpty())
		        	    	                 logger.warning("Overlap at ["+(x+xOffset)+"]["+(y+yOffset)+"] between full array n°"+stringNonFullArray[x+xOffset][y+yOffset].trim()+" and full array n°"+validFullArrayIndex);
		        	                     //builds the content of the cell
		        	                     final StringBuilder cellContentBuilder=new StringBuilder();
		        	                     final int leadingWhiteSpaceCount=(maxCharCountPerCell-validFullArrayIndexStringLength)/2;
		        	                     final int trailingWhiteSpaceCount=maxCharCountPerCell-validFullArrayIndexStringLength-leadingWhiteSpaceCount;
		        	                     for(int spaceIndex=0;spaceIndex<leadingWhiteSpaceCount;spaceIndex++)
		        	                    	 cellContentBuilder.append(' ');
		        	                     cellContentBuilder.append(validFullArrayIndexString);
		        	                     for(int spaceIndex=0;spaceIndex<trailingWhiteSpaceCount;spaceIndex++)
		        	                    	 cellContentBuilder.append(' ');
		        	                     //sets the content of the cell
		        	                     stringNonFullArray[x+xOffset][y+yOffset]=cellContentBuilder.toString();
		        	                    }
		                       }
		               validFullArrayIndex++;
		              }
		         }
		    }
		//fills the empty cell with a string containing spaces
		final StringBuilder emptyCellContentBuilder=new StringBuilder();
		for(int spaceIndex=0;spaceIndex<maxCharCountPerCell;spaceIndex++)
			emptyCellContentBuilder.append(' ');
		final String emptyCellContent=emptyCellContentBuilder.toString();
		//for each column, i.e for each abscissa
		for(int x=0;x<realMaxVisibleColumnCount;x++)
			{//for each row, i.e for each ordinate
			 for(int y=0;y<realMaxVisibleRowCount;y++)
				 //if the cell is empty
				 if(stringNonFullArray[x][y]==null||stringNonFullArray[x][y].isEmpty())
		             {//puts a string with empty spaces into it (to preserve the alignment with the other cells)
					  stringNonFullArray[x][y]=emptyCellContent;
		             }
			}
		return(toString(stringNonFullArray,true,null));
	}
	
	/**
	 * Creates a map of full arrays from a potentially non full array. It tries to 
	 * minimize the count of full arrays and to maximize their respective sizes.
	 * 
	 * @param array potentially non full array
	 * @return map of full arrays whose keys are their respective locations
	 */
	public <T> java.util.Map<Vector2i,T[][]> computeFullArraysFromNonFullArray(final T[][] array){
		//creates an occupancy map that will be updated (instead of modifying the supplied array)
		final OccupancyMap occupancyMapObj=createPackedOccupancyMap(array);
		final java.util.Map<Vector2i,T[][]> fullArraysMap=new LinkedHashMap<>();
		//if the array isn't empty (then the occupancy map isn't empty)
		if(!occupancyMapObj.isEmpty())
		    {final int smallestRowIndex=occupancyMapObj.getSmallestRowIndex();
		     final int smallestColumnIndex=occupancyMapObj.getSmallestColumnIndex();
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
		             {//for each column, i.e for each abscissa
		    		  for(int x=0;x<occupancyMap.length;x++)
		    			  if(occupancyMap[x]!=null)
		    			      {//for each row, i.e for each ordinate
		    			       for(int y=0;y<occupancyMap[x].length;y++)
		    		               {//if this element is occupied
		    				        if(occupancyMap[x][y])
		    		                    {//looks for an isolated element
		    				    	     //vertical checks (columns)
		    		    	             if(primarySize+x<=rowCount&&secondarySize<=columnCount&&
		    		    	                isRectangularSubSectionLocallyIsolated(occupancyMap,rowCount,columnCount,x,y,primarySize,secondarySize,true)&&
		    		    		           (y-1<0||!isRectangularSubSectionLocallyIsolated(occupancyMap,rowCount,columnCount,x,y-1,primarySize,1,true))&&
		    		    		           (/*y+secondarySize>=rowCount||*/!isRectangularSubSectionLocallyIsolated(occupancyMap,rowCount,columnCount,x,y+secondarySize,primarySize,1,true))&&
		    		    		            primarySize+x<=columnCount&&secondarySize+y<=rowCount&&
		    		    		            primarySize+x+smallestColumnIndex<=array.length&&array[primarySize+x+smallestColumnIndex-1]!=null&&secondarySize+y+smallestRowIndex<=array[primarySize+x+smallestColumnIndex-1].length)
		    		    	                 {//checks whether the candidate full array doesn't go beyond the occupancy map
		    		    	            	  boolean isOccupable=true;
		    		    	            	  for(int i=0;i<primarySize&&isOccupable;i++)
		    		    	            		  {isOccupable&=occupancyMap[i+x]!=null&&secondarySize+y<=occupancyMap[i+x].length;
		    		    	            		   for(int j=0;j<secondarySize&&isOccupable;j++)
		    		    	            		       isOccupable&=occupancyMap[i+x][j+y];
		    		    	            		  }
		    		    	            	  if(isOccupable)
		    		    	            	      {@SuppressWarnings("unchecked")
										           final T[][] fullArray=(T[][])Array.newInstance(arrayComponentType,primarySize,secondarySize);
		    		    	                       //copies the elements of the chunk into the sub-array and marks them as removed from the occupancy map
		    		    	                       for(int i=0;i<primarySize;i++)
		    		    		                       {for(int j=0;j<secondarySize;j++)
		    		    		                            {fullArray[i][j]=array[i+x+smallestColumnIndex][j+y+smallestRowIndex];
		    		    		                             occupancyMap[i+x][j+y]=false;
		    		    		                            }
		    		    		                       }
		    		    	                       //puts the location of the full array and the array into the map
		    		    	                       if(fullArraysMap.put(new Vector2i(x+smallestColumnIndex,y+smallestRowIndex),fullArray)!=null)
		    		    	                	       logger.warning("Overlap at ["+(x+smallestColumnIndex)+"]["+(y+smallestRowIndex)+"]");
		    		    	            	      }
		    		    	                 }
		    		    	             else
		    		    	                 {//horizontal checks (rows)
		    		    	                  if(primarySize+y<=columnCount&&secondarySize<=rowCount&&
		    		    	                     isRectangularSubSectionLocallyIsolated(occupancyMap,rowCount,columnCount,x,y,primarySize,secondarySize,false)&&
		    		 			 	            (x-1<0||!isRectangularSubSectionLocallyIsolated(occupancyMap,rowCount,columnCount,x-1,y,primarySize,1,false))&&
		    		 			 	            (/*x+secondarySize>=rowCount||*/!isRectangularSubSectionLocallyIsolated(occupancyMap,rowCount,columnCount,x+secondarySize,y,primarySize,1,false))&&
		    		 			 	             secondarySize+x<=columnCount&&primarySize+y<=rowCount&&
		    		 			 	             secondarySize+x+smallestColumnIndex<=array.length&&array[secondarySize+x+smallestColumnIndex-1]!=null&&primarySize+y+smallestRowIndex<=array[secondarySize+x+smallestColumnIndex-1].length)
		    		    	                      {//checks whether the candidate full array doesn't go beyond the occupancy map
			    		    	            	   boolean isOccupable=true;
			    		    	            	   for(int i=0;i<secondarySize&&isOccupable;i++)
			    		    	            		   {isOccupable&=occupancyMap[i+x]!=null&&primarySize+y<=occupancyMap[i+x].length;
			    		    	            		    for(int j=0;j<primarySize&&isOccupable;j++)
			    		    	            		    	isOccupable&=occupancyMap[i+x][j+y];
			    		    	            		   }
			    		    	            	   if(isOccupable)
			    		    	            	       {@SuppressWarnings("unchecked")
		    		    	            	            final T[][] fullArray=(T[][])Array.newInstance(arrayComponentType,secondarySize,primarySize);
		   	 			                                //copies the elements of the chunk into the sub-array and marks them as removed from the occupancy map
		   	 			                                for(int j=0;j<primarySize;j++)
		   	 			                	                {for(int i=0;i<secondarySize;i++)
		   	 			                	        	         {fullArray[i][j]=array[i+x+smallestColumnIndex][j+y+smallestRowIndex];
		   	 			                	        	          if(!occupancyMap[i+x][j+y])
		   	 			                	        	        	  logger.warning("Overlap at ["+(i+x+smallestColumnIndex)+"]["+(j+y+smallestRowIndex)+"]");
		   	 			                	        	          else
		   			                		                          occupancyMap[i+x][j+y]=false;
		   			                		                     }
		   	 			                	                }
		   	 			                                //puts the location of the full array and the array into the map
		   	 			                                if(fullArraysMap.put(new Vector2i(x+smallestColumnIndex,y+smallestRowIndex),fullArray)!=null)
			    		    	                	        logger.warning("Overlap at ["+(x+smallestColumnIndex)+"]["+(y+smallestRowIndex)+"]");
			    		    	            	       }
		    		    	                      }
		    		    	                 }
		    		                    }
		    		               }
		                      }
		             }
		    }
		return(fullArraysMap);
	}

	/**
	 * Tells whether a rectangular subsection of the supplied array is locally isolated, i.e it is full
	 * and its close neighboring is empty
	 * 
	 * @param array array containing the subsection
	 * @param rowCount row count (may be greater than the row count of the supplied array)
	 * @param columnCount column count (may be greater than the column count of the supplied array)
	 * @param localSmallestColumnIndex lowest column index of the subsection 
	 * @param localSmallestRowIndex lowest row index of the subsection
	 * @param primarySize row count of the subsection if testOnColumnIsolationEnabled is true, otherwise column count
	 * @param secondarySize column count of the subsection if testOnColumnIsolationEnabled is true, otherwise row count
	 * @param testOnColumnIsolationEnabled true if the test checks whether the isolation of this subsection is tested as a column, otherwise it is tested as a row
	 * @return
	 */
	public boolean isRectangularSubSectionLocallyIsolated(final boolean[][] array,final int rowCount,final int columnCount,
			final int localSmallestColumnIndex,final int localSmallestRowIndex,final int primarySize,final int secondarySize,
			final boolean testOnColumnIsolationEnabled){
		boolean isolated;
		//checks whether the first cell of the subsection [localSmallestColumnIndex,localSmallestRowIndex] is in the array
	    if(0<=localSmallestColumnIndex&&localSmallestColumnIndex<array.length&&array[localSmallestColumnIndex]!=null&&0<=localSmallestRowIndex&&localSmallestRowIndex<array[localSmallestColumnIndex].length&&array[localSmallestColumnIndex][localSmallestRowIndex])
	        {if(testOnColumnIsolationEnabled)
	             {isolated=true;
	              //for each column, i.e for each abscissa
	              for(int x=Math.max(0,localSmallestColumnIndex-1);x<=localSmallestColumnIndex+primarySize&&x<rowCount&&isolated;x++)
	                  {//for each row, i.e for each ordinate
	            	   for(int y=Math.max(0,localSmallestRowIndex);y<localSmallestRowIndex+secondarySize&&y<columnCount&&isolated;y++)
	            	       if((((x==localSmallestColumnIndex-1)||(x==localSmallestColumnIndex+primarySize))&&(x<array.length&&array[x]!=null&&y<array[x].length&&array[x][y]))||((localSmallestColumnIndex-1<x)&&(x<localSmallestColumnIndex+primarySize)&&(x>=array.length||array[x]==null||y>=array[x].length||!array[x][y])))
	            	    	   isolated=false;
	                  }
	             }
	         else
	             {isolated=true;
	              //for each column, i.e for each abscissa
	              for(int x=Math.max(0,localSmallestColumnIndex);x<localSmallestColumnIndex+secondarySize&&x<rowCount&&isolated;x++)
	                  {//for each row, i.e for each ordinate
	            	   for(int y=Math.max(0,localSmallestRowIndex-1);y<=localSmallestRowIndex+primarySize&&y<columnCount&&isolated;y++)
	            		   if((((y==localSmallestRowIndex-1)||(y==localSmallestRowIndex+primarySize))&&(x<array.length&&array[x]!=null&&y<array[x].length&&array[x][y]))||((localSmallestRowIndex-1<y)&&(y<localSmallestRowIndex+primarySize)&&(x>=array.length||array[x]==null||y>=array[x].length||!array[x][y])))
	            	    	   isolated=false;
	                  }
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
	 * @param localSmallestColumnIndex lowest column index of the subsection 
	 * @param localSmallestRowIndex lowest row index of the subsection
	 * @param primarySize row count of the subsection if testOnColumnIsolationEnabled is true, otherwise column count
	 * @param secondarySize column count of the subsection if testOnColumnIsolationEnabled is true, otherwise row count
	 * @param testOnColumnIsolationEnabled true if the test checks whether the isolation of this subsection is tested as a column, otherwise it is tested as a row
	 * @return
	 */
	public <T> boolean isRectangularSubSectionLocallyIsolated(final T[][] array,final int rowCount,final int columnCount,
			final int localSmallestColumnIndex,final int localSmallestRowIndex,final int primarySize,final int secondarySize,
			final boolean testOnColumnIsolationEnabled){
		boolean isolated;
	    if(0<=localSmallestColumnIndex&&localSmallestColumnIndex<array.length&&array[localSmallestColumnIndex]!=null&&0<=localSmallestRowIndex&&localSmallestRowIndex<array[localSmallestColumnIndex].length&&array[localSmallestColumnIndex][localSmallestRowIndex]!=null)
	        {if(testOnColumnIsolationEnabled)
	             {isolated=true;
	              for(int x=Math.max(0,localSmallestColumnIndex-1);x<=localSmallestColumnIndex+primarySize&&x<rowCount&&isolated;x++)
	                  {for(int y=Math.max(0,localSmallestRowIndex);y<localSmallestRowIndex+secondarySize&&y<columnCount&&isolated;y++)
	            	       if((((x==localSmallestColumnIndex-1)||(x==localSmallestColumnIndex+primarySize))&&(x<array.length&&array[x]!=null&&y<array[x].length&&array[x][y]!=null))||((localSmallestColumnIndex-1<x)&&(x<localSmallestColumnIndex+primarySize)&&(x>=array.length||array[x]==null||y>=array[x].length||array[x][y]==null)))
	            	    	   isolated=false;
	                  }
	             }
	         else
	             {isolated=true;
	              for(int x=Math.max(0,localSmallestColumnIndex);x<localSmallestColumnIndex+secondarySize&&x<rowCount&&isolated;x++)
	                  {for(int y=Math.max(0,localSmallestRowIndex-1);y<=localSmallestRowIndex+primarySize&&y<columnCount&&isolated;y++)
	            		   if((((y==localSmallestRowIndex-1)||(y==localSmallestRowIndex+primarySize))&&(x<array.length&&array[x]!=null&&y<array[x].length&&array[x][y]!=null))||((localSmallestRowIndex-1<y)&&(y<localSmallestRowIndex+primarySize)&&(x>=array.length||array[x]==null||y>=array[x].length||array[x][y]==null)))
	            	    	   isolated=false;
	                  }
	             }
	        }
	    else
	    	isolated=false;
	    return(isolated);
	}
}
