/**
 * Copyright (c) 2006-2021 Julien Gouesse
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
import java.util.Arrays;
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
public class ArrayHelper {

    private static final Logger LOGGER = Logger.getLogger(ArrayHelper.class.getName());

    /**
     * Default constructor
     */
    public ArrayHelper() {
        super();
    }

    public static final class Vector2i {

        private final int x;

        private final int y;

        public Vector2i(final int x, final int y) {
            super();
            this.x = x;
            this.y = y;
        }

        public int getX() {
            return x;
        }

        public int getY() {
            return y;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + x;
            result = prime * result + y;
            return result;
        }

        @Override
        public boolean equals(final Object other) {
            return (this == other || (other != null && other.getClass() == Vector2i.class && x == ((Vector2i) other).x
                    && y == ((Vector2i) other).y));
        }
    }

    /**
     * Occupancy map, structure representing the occupation of an array, can be
     * ragged. This class isn't thread safe and the passed array shouldn't be
     * modified outside of this class. It's not copied in order to avoid
     * increasing the memory footprint.
     */
    public static final class OccupancyMap implements Cloneable {

        /**
         * array map that indicates which cells are occupied
         */
        private final boolean[][] arrayMap;

        /**
         * smallest row index or minimum ordinate of an occupied cell in the
         * array used to compute the array map
         */
        private final int smallestRowIndex;

        /**
         * biggest row index or maximum ordinate of an occupied cell in the
         * array used to compute the array map
         */
        private final int biggestRowIndex;

        /**
         * smallest column index or minimum abscissa of an occupied cell in the
         * array used to compute the array map
         */
        private final int smallestColumnIndex;

        /**
         * biggest column index or maximum abscissa of an occupied cell in the
         * array used to compute the array map
         */
        private final int biggestColumnIndex;

        /**
         * maximum row count that can be found in the array map knowing that the
         * columns may have different row counts
         */
        private final int rowCount;

        /**
         * count of occupied cells, i.e count of cells set to true
         */
        private int occupiedCellCount;

        /**
         * Constructor
         * 
         * @param arrayMap
         *            array map that indicates which cells are occupied
         * @param smallestRowIndex
         *            smallest row index or minimum ordinate of an occupied cell
         *            in the array used to compute the array map
         * @param biggestRowIndex
         *            biggest row index or maximum ordinate of an occupied cell
         *            in the array used to compute the array map
         * @param smallestColumnIndex
         *            smallest column index or minimum abscissa of an occupied
         *            cell in the array used to compute the array map
         * @param biggestColumnIndex
         *            biggest column index or maximum abscissa of an occupied
         *            cell in the array used to compute the array map
         * @param rowCount
         *            row count maximum row count of a column, a column of the
         *            array map mustn't have a greater row count
         */
        protected OccupancyMap(final boolean[][] arrayMap, final int smallestRowIndex, final int biggestRowIndex,
                final int smallestColumnIndex, final int biggestColumnIndex, final int rowCount) {
            super();
            if (rowCount < 0)
                throw new IllegalArgumentException("The row count must be positive or equal to zero");
            this.occupiedCellCount = 0;
            // computes the count of occupied cells
            // for each column, i.e for each abscissa
            for (int x = 0; x < arrayMap.length; x++) {
                // if there is a non empty column
                if (arrayMap[x] != null && arrayMap[x].length > 0) {
                    if (arrayMap[x].length > rowCount)
                        throw new IllegalArgumentException(
                                "The row count must be less than or equal to the row count of each column. "
                                        + arrayMap[x].length + ">" + rowCount);
                    // doesn't visit any cell beyond the the row count
                    final int localRowCount = arrayMap[x].length;
                    // for each row, i.e for each ordinate
                    for (int y = 0; y < localRowCount; y++)
                        if (arrayMap[x][y]) {
                            // the cell is occupied
                            this.occupiedCellCount++;
                        }
                }
            }
            this.arrayMap = arrayMap;
            this.smallestRowIndex = smallestRowIndex;
            this.biggestRowIndex = biggestRowIndex;
            this.smallestColumnIndex = smallestColumnIndex;
            this.biggestColumnIndex = biggestColumnIndex;
            this.rowCount = rowCount;
        }

        /**
         * Constructor only used to clone occupancy maps
         * 
         * @param arrayMap
         *            array map that indicates which cells are occupied
         * @param smallestRowIndex
         *            smallest row index or minimum ordinate of an occupied cell
         *            in the array used to compute the array map
         * @param biggestRowIndex
         *            biggest row index or maximum ordinate of an occupied cell
         *            in the array used to compute the array map
         * @param smallestColumnIndex
         *            smallest column index or minimum abscissa of an occupied
         *            cell in the array used to compute the array map
         * @param biggestColumnIndex
         *            biggest column index or maximum abscissa of an occupied
         *            cell in the array used to compute the array map
         * @param rowCount
         *            row count maximum row count of a column, a column of the
         *            array map mustn't have a greater row count
         * @param occupiedCellCount
         *            count of occupied cells, i.e count of cells set to true
         */
        private OccupancyMap(final boolean[][] arrayMap, final int smallestRowIndex, final int biggestRowIndex,
                final int smallestColumnIndex, final int biggestColumnIndex, final int rowCount,
                final int occupiedCellCount) {
            super();
            this.arrayMap = arrayMap;
            this.smallestRowIndex = smallestRowIndex;
            this.biggestRowIndex = biggestRowIndex;
            this.smallestColumnIndex = smallestColumnIndex;
            this.biggestColumnIndex = biggestColumnIndex;
            this.rowCount = rowCount;
            this.occupiedCellCount = occupiedCellCount;
        }

        @Override
        public OccupancyMap clone() {
            final boolean[][] arrayMapCopy = new boolean[arrayMap.length][];
            // for each column, i.e for each abscissa
            for (int x = 0; x < arrayMap.length; x++) {
                // skips the column if null
                if (arrayMap[x] != null) {
                    // creates the new column
                    arrayMapCopy[x] = new boolean[arrayMap[x].length];
                    // copies the data of the column into the new column
                    System.arraycopy(arrayMap[x], 0, arrayMapCopy[x], 0, arrayMap[x].length);
                }
            }
            final OccupancyMap copy = new OccupancyMap(arrayMapCopy, smallestRowIndex, biggestRowIndex,
                    smallestColumnIndex, biggestColumnIndex, rowCount, occupiedCellCount);
            return copy;
        }

        /**
         * Tells whether the column is non null
         * 
         * @param columnIndex
         *            column index
         * 
         * @return <code>true</code> if the column is non null
         */
        public boolean hasNonNullColumn(final int columnIndex) {
            return arrayMap[columnIndex] != null;
        }

        /**
         * Returns the row count of the column
         * 
         * @param columnIndex
         *            column index
         * 
         * @return row count of the column
         */
        public int getRowCount(final int columnIndex) {
            return arrayMap[columnIndex].length;
        }

        /**
         * Returns the value at the given position
         * 
         * @param columnIndex
         *            column index
         * @param rowIndex
         *            row index
         * 
         * @return value at the given position
         */
        public boolean getValue(final int columnIndex, final int rowIndex) {
            return arrayMap[columnIndex][rowIndex];
        }

        /**
         * Sets the value at the given position and updates the count of cells
         * set to true
         * 
         * @param columnIndex
         *            column index
         * @param rowIndex
         *            row index
         * @param value
         *            value to set
         */
        public void setValue(final int columnIndex, final int rowIndex, boolean value) {
            if (arrayMap[columnIndex][rowIndex] != value) {
                /**
                 * updates the count of cells, tries to avoid setting an absurd
                 * value even though a modification of the array map not
                 * performed in this setter isn't tracked and can drive the
                 * count of occupied cells completely wrong
                 */
                if (value) {
                    /**
                     * increases the count of cells set to true, it can't be
                     * greater than the maximum cell count
                     */
                    this.occupiedCellCount = Math.min(this.occupiedCellCount + 1, arrayMap.length * rowCount);
                } else {
                    /**
                     * decreases the count of cells set to true, it can't be
                     * negative
                     */
                    this.occupiedCellCount = Math.max(this.occupiedCellCount - 1, 0);
                }
                arrayMap[columnIndex][rowIndex] = value;
            }
        }

        /**
         * Returns the smallest row index or minimum ordinate of an occupied
         * cell in the array used to compute the array map
         * 
         * @return smallest row index or minimum ordinate of an occupied cell in
         *         the array used to compute the array map
         */
        public final int getSmallestRowIndex() {
            return smallestRowIndex;
        }

        /**
         * Returns the biggest row index or maximum ordinate of an occupied cell
         * in the array used to compute the array map
         * 
         * @return biggest row index, i.e or maximum ordinate of an occupied
         *         cell in the array used to compute the array map
         */
        public final int getBiggestRowIndex() {
            return biggestRowIndex;
        }

        /**
         * Returns the smallest column index or minimum abscissa of an occupied
         * cell in the array used to compute the array map
         * 
         * @return smallest column index or minimum abscissa of an occupied cell
         *         in the array used to compute the array map
         */
        public final int getSmallestColumnIndex() {
            return smallestColumnIndex;
        }

        /**
         * Returns the biggest column index or maximum abscissa of an occupied
         * cell in the array used to compute the array map
         * 
         * @return biggest column index or maximum abscissa of an occupied cell
         *         in the array used to compute the array map
         */
        public final int getBiggestColumnIndex() {
            return biggestColumnIndex;
        }

        /**
         * Returns the maximum row count that can be found in the array map
         * 
         * @return maximum row count that can be found in the array map
         */
        public final int getRowCount() {
            return rowCount;
        }

        /**
         * Returns the column count
         * 
         * @return column count
         */
        public final int getColumnCount() {
            return arrayMap.length;
        }

        /**
         * Tells whether the occupancy array is empty, i.e if it has no row, no
         * column or no cell set to true
         * 
         * @return <code>true</code> if the occupancy array is empty, otherwise
         *         <code>false</code>
         */
        public final boolean isEmpty() {
            return rowCount == 0 || arrayMap.length == 0 || occupiedCellCount == 0;
        }

        public final int getOccupiedCellCount() {
            return occupiedCellCount;
        }

        /**
         * Tells whether a rectangular subsection of the occupancy map is
         * locally isolated, i.e it is full and its close neighboring is empty
         * 
         * @param localSmallestColumnIndex
         *            lowest column index of the subsection
         * @param localSmallestRowIndex
         *            lowest row index of the subsection
         * @param subsectionRowCount
         *            row count of the subsection
         * @param subsectionColumnCount
         *            column count of the subsection
         * @param testOnRowIsolationEnabled
         *            true if the test checks whether the isolation of this
         *            subsection is tested as a row, otherwise it is tested as a
         *            column
         * 
         * @return <code>true</code> if the subsection is isolated, otherwise
         *         <code>false</code>
         */
        public boolean isRectangularSubSectionLocallyIsolated(final int localSmallestColumnIndex,
                final int localSmallestRowIndex, final int subsectionRowCount, final int subsectionColumnCount,
                final boolean testOnRowIsolationEnabled) {
            boolean isolated;
            /**
             * checks whether the first cell of the subsection
             * [localSmallestColumnIndex,localSmallestRowIndex] is in the array
             * and is occupied
             */
            if (0 <= localSmallestColumnIndex && localSmallestColumnIndex < arrayMap.length
                    && arrayMap[localSmallestColumnIndex] != null && 0 <= localSmallestRowIndex
                    && localSmallestRowIndex < arrayMap[localSmallestColumnIndex].length
                    && arrayMap[localSmallestColumnIndex][localSmallestRowIndex]) {
                isolated = true;
                final int xOffset = testOnRowIsolationEnabled ? 1 : 0;
                final int yOffset = testOnRowIsolationEnabled ? 0 : 1;
                final int minX = localSmallestColumnIndex - xOffset;
                final int maxX = localSmallestColumnIndex + subsectionColumnCount - 1 + xOffset;
                final int minY = localSmallestRowIndex - yOffset;
                final int maxY = localSmallestRowIndex + subsectionRowCount - 1 + yOffset;
                final int cappedMinX = Math.max(0, minX);
                final int cappedMaxX = Math.min(maxX, arrayMap.length - 1);
                final int cappedMinY = Math.max(0, minY);
                final int cappedMaxY = Math.min(maxY, rowCount - 1);
                if (testOnRowIsolationEnabled) {
                    // for each column, i.e for each abscissa
                    for (int x = cappedMinX; x <= cappedMaxX && isolated; x++) {
                        if (x == minX || x == maxX) {
                            /**
                             * looks at the closest columns outside of the
                             * subsection on its left and on its right for each
                             * row, i.e for each ordinate
                             */
                            for (int y = cappedMinY; y <= cappedMaxY && isolated; y++) {
                                // if the cell is in the array and if it's
                                // occupied
                                if (x < arrayMap.length && arrayMap[x] != null && y < arrayMap[x].length
                                        && arrayMap[x][y])
                                    isolated = false;
                            }
                        } else {
                            // looks at the cells inside the subsection
                            // for each row, i.e for each ordinate
                            for (int y = cappedMinY; y <= cappedMaxY && isolated; y++) {
                                // if the cell isn't in the array or if it isn't
                                // occupied
                                if (x >= arrayMap.length || arrayMap[x] == null || y >= arrayMap[x].length
                                        || !arrayMap[x][y])
                                    isolated = false;
                            }
                        }
                    }
                } else {// for each row, i.e for each ordinate
                    for (int y = cappedMinY; y <= cappedMaxY && isolated; y++) {
                        if (y == minY || y == maxY) {
                            // looks at the closest rows outside of the
                            // subsection above and below
                            // for each column, i.e for each abscissa
                            for (int x = cappedMinX; x <= cappedMaxX && isolated; x++) {
                                // if the cell is in the array and if it's
                                // occupied
                                if (x < arrayMap.length && arrayMap[x] != null && y < arrayMap[x].length
                                        && arrayMap[x][y])
                                    isolated = false;
                            }
                        } else {// looks at the cells inside the subsection
                                // for each column, i.e for each abscissa
                            for (int x = cappedMinX; x <= cappedMaxX && isolated; x++) {
                                // if the cell isn't in the array or if it isn't
                                // occupied
                                if (x >= arrayMap.length || arrayMap[x] == null || y >= arrayMap[x].length
                                        || !arrayMap[x][y])
                                    isolated = false;
                            }
                        }
                    }

                }
            } else
                isolated = false;
            return (isolated);
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + Arrays.deepHashCode(arrayMap);
            result = prime * result + biggestColumnIndex;
            result = prime * result + biggestRowIndex;
            result = prime * result + occupiedCellCount;
            result = prime * result + rowCount;
            result = prime * result + smallestColumnIndex;
            result = prime * result + smallestRowIndex;
            return (result);
        }

        @Override
        public boolean equals(Object o) {
            return (this == o || (o != null && o.getClass() == OccupancyMap.class
                    && ((OccupancyMap) o).biggestColumnIndex == biggestColumnIndex
                    && ((OccupancyMap) o).biggestRowIndex == biggestRowIndex
                    && ((OccupancyMap) o).occupiedCellCount == occupiedCellCount
                    && ((OccupancyMap) o).smallestColumnIndex == smallestColumnIndex
                    && ((OccupancyMap) o).smallestRowIndex == smallestRowIndex
                    && Arrays.deepEquals(((OccupancyMap) o).arrayMap, arrayMap)));
        }
    }

    /**
     * Check of occupancy for a 2D array
     *
     * @param <T>
     *            type of the value occupying a cell of an array
     */
    public static interface OccupancyCheck<T> {
        /**
         * Tells whether the value is considered as occupying a cell array
         * 
         * @param value
         *            value in the array cell
         * @return <code>true</code> if the value is considered as occupying a
         *         cell array, otherwise <code>false</code>
         */
        public boolean isOccupied(T value);
    }

    /**
     * Returns a textual representation of a 2D array, can be ragged
     * 
     * @param array
     *            2D array
     * @param useObjectToString
     *            indicates whether to use Object.toString() to build the
     *            textual representation of an object, uses 'X' and ' ' if
     *            <code>false</code>
     * @param occupancyCheck
     *            occupancy check, tells whether the object "occupies" the array
     *            cell, can be null. If <code>null</code>, the cell isn't
     *            occupied if it contains <code>null</code>
     * @return textual representation of a 2D array
     */
    public <T> String toString(final T[][] array, final boolean useObjectToString,
            final OccupancyCheck<T> occupancyCheck) {
        final StringBuilder builder = new StringBuilder();
        // computes the maximum number of rows in the columns (to handle the
        // ragged arrays)
        int maxColumnRowCount = 0;
        // for each column, i.e for each abscissa
        for (int x = 0; x < array.length; x++)
            if (array[x] != null)
                maxColumnRowCount = Math.max(maxColumnRowCount, array[x].length);
        // for each row, i.e for each ordinate
        for (int y = 0; y < maxColumnRowCount; y++) {// for each column, i.e for
                                                     // each abscissa
            for (int x = 0; x < array.length; x++) {
                if (array[x] != null && y < array[x].length) {
                    final T value = array[x][y];
                    if (useObjectToString) {
                        if ((occupancyCheck == null && value == null)
                                || (occupancyCheck != null && !occupancyCheck.isOccupied(value)))
                            builder.append("[null]");
                        else
                            builder.append('[').append(Objects.toString(value)).append(']');
                    } else {
                        if ((occupancyCheck == null && value == null)
                                || (occupancyCheck != null && !occupancyCheck.isOccupied(value)))
                            builder.append("[ ]");
                        else
                            builder.append("[X]");
                    }
                }
            }
            // end of row
            builder.append('\n');
        }
        return (builder.toString());
    }

    /**
     * Creates the occupancy map of an array
     * 
     * @param array
     *            array whose occupancy has to be computed
     * 
     * @return occupancy map of an array
     */
    public <T> OccupancyMap createPackedOccupancyMap(final T[][] array) {
        return (createPackedOccupancyMap(array, null));
    }

    /**
     * Creates the occupancy map of an array
     * 
     * @param array
     *            array whose occupancy has to be computed
     * @param occupancyCheck
     *            occupancy check, tells whether the object "occupies" the array
     *            cell, can be null. If <code>null</code>, the cell isn't
     *            occupied if it contains <code>null</code>
     * 
     * @return occupancy map of an array
     */
    public <T> OccupancyMap createPackedOccupancyMap(final T[][] array, final OccupancyCheck<T> occupancyCheck) {
        // detects empty rows and empty columns in order to skip them later
        int smallestRowIndex = Integer.MAX_VALUE;
        int biggestRowIndex = Integer.MIN_VALUE;
        int smallestColumnIndex = Integer.MAX_VALUE;
        int biggestColumnIndex = Integer.MIN_VALUE;
        // checks if the array has at least one column
        if (array.length > 0) {// looks for the biggest column index
            boolean searchStopped = false;
            // for each column, i.e for each abscissa
            for (int x = array.length - 1; x >= 0 && !searchStopped; x--)
                if (array[x] != null && array[x].length > 0)
                    // for each row, i.e for each ordinate
                    for (int y = array[x].length - 1; y >= 0 && !searchStopped; y--)
                        if ((occupancyCheck == null && array[x][y] != null)
                                || (occupancyCheck != null && occupancyCheck.isOccupied(array[x][y]))) {
                            // correct value
                            biggestColumnIndex = x;
                            // candidates
                            smallestColumnIndex = x;
                            smallestRowIndex = y;
                            biggestRowIndex = y;
                            // uses this flag to stop the search
                            searchStopped = true;
                        }
            // checks if the array has at least one non empty row
            if (searchStopped) {
                // looks for the smallest column index
                searchStopped = false;
                // for each column, i.e for each abscissa
                for (int x = 0; x <= biggestColumnIndex && !searchStopped; x++)
                    if (array[x] != null)
                        // for each row, i.e for each ordinate
                        for (int y = 0; y < array[x].length && !searchStopped; y++)
                            if ((occupancyCheck == null && array[x][y] != null)
                                    || (occupancyCheck != null && occupancyCheck.isOccupied(array[x][y]))) {
                                // correct value
                                smallestColumnIndex = x;
                                // candidates
                                smallestRowIndex = Math.min(smallestRowIndex, y);
                                biggestRowIndex = Math.max(biggestRowIndex, y);
                                // uses this flag to stop the search
                                searchStopped = true;
                            }
                // looks for the biggest row index
                searchStopped = false;
                if (biggestRowIndex < Integer.MAX_VALUE) {
                    // for each column, i.e for each abscissa
                    for (int x = biggestColumnIndex; x >= smallestColumnIndex && !searchStopped; x--)
                        if (array[x] != null && array[x].length > biggestRowIndex + 1) {
                            // for each row, i.e for each ordinate
                            for (int y = array[x].length - 1; y > biggestRowIndex && !searchStopped; y--)
                                if ((occupancyCheck == null && array[x][y] != null)
                                        || (occupancyCheck != null && occupancyCheck.isOccupied(array[x][y])))
                                    biggestRowIndex = y;
                            if (biggestRowIndex == Integer.MAX_VALUE)
                                searchStopped = true;
                        }
                }
                // looks for the smallest row index
                searchStopped = false;
                if (smallestRowIndex > 0) {
                    // for each column, i.e for each abscissa
                    for (int x = smallestColumnIndex; x <= biggestColumnIndex && !searchStopped; x++)
                        if (array[x] != null && array[x].length > 0) {
                            // for each row, i.e for each ordinate
                            for (int y = 0; y < smallestRowIndex && !searchStopped; y++)
                                if ((occupancyCheck == null && array[x][y] != null)
                                        || (occupancyCheck != null && occupancyCheck.isOccupied(array[x][y])))
                                    smallestRowIndex = y;
                            if (smallestRowIndex == 0)
                                searchStopped = true;
                        }
                }
            }
        }
        final int tmpRowCount = biggestRowIndex >= smallestRowIndex ? biggestRowIndex - smallestRowIndex + 1 : 0;
        final int tmpColumnCount = biggestColumnIndex >= smallestColumnIndex ? biggestColumnIndex - smallestColumnIndex + 1 : 0;
        final int rowCount = tmpRowCount == 0 || tmpColumnCount == 0 ? 0 : tmpRowCount;
        final int columnCount = tmpRowCount == 0 || tmpColumnCount == 0 ? 0 : tmpColumnCount;
        final boolean[][] occupancyMapArray;
        // if the array is not empty
        if (rowCount > 0 && columnCount > 0) {
            // creates an occupancy map of the supplied array but without empty
            // columns and rows
            occupancyMapArray = new boolean[columnCount][];
            // for each column, i.e for each abscissa
            for (int x = 0; x < columnCount; x++) {
                // computes the index in the original array by using the offset
                final int rawX = x + smallestColumnIndex;
                if (array[rawX] != null) {
                    // starts the computation of the biggest index of the
                    // current row
                    int localBiggestRowIndex = Integer.MIN_VALUE;
                    // for each row, i.e for each ordinate
                    for (int y = 0; y < rowCount; y++) {
                        // computes the index in the original array by using the
                        // offset
                        final int rawY = y + smallestRowIndex;
                        if ((occupancyCheck == null && array[rawX][rawY] != null)
                                || (occupancyCheck != null && occupancyCheck.isOccupied(array[rawX][rawY])))
                            localBiggestRowIndex = rawY;
                    }
                    final int localRowCount = localBiggestRowIndex >= smallestRowIndex
                            ? localBiggestRowIndex - smallestRowIndex + 1 : 0;
                    // allocates the current column of the occupancy map as
                    // tightly as possible
                    occupancyMapArray[x] = new boolean[localRowCount];
                    // fills the occupancy map (true <-> not null)
                    for (int y = 0; y < occupancyMapArray[x].length; y++) {
                        final int rawY = y + smallestRowIndex;
                        occupancyMapArray[x][y] = (occupancyCheck == null && array[rawX][rawY] != null)
                                || (occupancyCheck != null && occupancyCheck.isOccupied(array[rawX][rawY]));
                    }
                }
            }
        } else {
            // the array is empty
            occupancyMapArray = new boolean[0][0];
        }
        final OccupancyMap occupancyMap = new OccupancyMap(occupancyMapArray, smallestRowIndex, biggestRowIndex,
                smallestColumnIndex, biggestColumnIndex, rowCount);
        return occupancyMap;
    }

    public <T> String toString(final java.util.Map<Vector2i, T[][]> fullArraysMap) {
        return toString(fullArraysMap, -1, -1);
    }

    /**
     * Returns a textual representation of a full array map subset
     * 
     * @param fullArraysMap
     *            full array map
     * @param maxVisibleRowCount
     *            maximum visible row count or -1 if none
     * @param maxVisibleColumnCount
     *            maximum visible column count or -1 if none
     * @return
     */
    public <T> String toString(final java.util.Map<Vector2i, T[][]> fullArraysMap, final int maxVisibleRowCount,
            final int maxVisibleColumnCount) {
        // computes the size of the smallest non full arrays that could contain
        // the full arrays
        int dataMaxRowCount = 0;
        int dataMaxColumnCount = 0;
        int validValuesCount = 0;
        // for each full array of the map
        for (final Entry<Vector2i, T[][]> fullArrayEntry : fullArraysMap.entrySet()) {
            final Vector2i location = fullArrayEntry.getKey();
            final T[][] fullArray = fullArrayEntry.getValue();
            // a map can support null values
            if (fullArray != null) {
                final int xOffset = location == null ? 0 : location.getX();
                final int yOffset = location == null ? 0 : location.getY();
                // uses the location as an offset of the abscissa
                dataMaxColumnCount = Math.max(dataMaxColumnCount, fullArray.length + xOffset);
                // for each column
                for (final T[] fullArrayColumm : fullArray) {
                    // if the column isn't null (Java supports the ragged
                    // arrays)
                    if (fullArrayColumm != null) {
                        // the size of the column is used to compute the maximum
                        // row count, uses the location as an offset of the
                        // ordinate
                        dataMaxRowCount = Math.max(dataMaxRowCount, fullArrayColumm.length + yOffset);
                    }
                }
                validValuesCount++;
            }
        }
        // computes the size of the visible array
        final int realMaxVisibleRowCount;
        if (maxVisibleRowCount < 0)
            realMaxVisibleRowCount = dataMaxRowCount;
        else
            realMaxVisibleRowCount = maxVisibleRowCount;
        final int realMaxVisibleColumnCount;
        if (maxVisibleColumnCount < 0)
            realMaxVisibleColumnCount = dataMaxColumnCount;
        else
            realMaxVisibleColumnCount = maxVisibleColumnCount;
        // computes the maximum size of the string used to represent a full
        // array
        final int maxCharCountPerCell = validValuesCount == 0 ? 0 : Integer.toString(validValuesCount - 1).length();
        // builds a 2D array
        final String[][] stringNonFullArray = new String[realMaxVisibleColumnCount][realMaxVisibleRowCount];
        if (validValuesCount > 0) {
            // fills it by looping on the entry set
            int validFullArrayIndex = 0;
            for (final Entry<Vector2i, T[][]> fullArrayEntry : fullArraysMap.entrySet()) {
                final Vector2i location = fullArrayEntry.getKey();
                final T[][] fullArray = fullArrayEntry.getValue();
                // a map can support null values and null keys
                if (fullArray != null) {
                    final int xOffset = location == null ? 0 : location.getX();
                    final int yOffset = location == null ? 0 : location.getY();
                    final String validFullArrayIndexString = Integer.toString(validFullArrayIndex);
                    final int validFullArrayIndexStringLength = validFullArrayIndexString.length();
                    // for each column, i.e for each abscissa
                    for (int x = 0; x < fullArray.length; x++)
                        if (fullArray[x] != null) {
                            // for each row, i.e for each ordinate
                            for (int y = 0; y < fullArray[x].length; y++)
                                if (fullArray[x][y] != null && 0 <= x + xOffset
                                        && x + xOffset < stringNonFullArray.length && 0 <= y + yOffset
                                        && y + yOffset < stringNonFullArray[x + xOffset].length) {
                                    // checks whether the cell is already
                                    // occupied
                                    if (stringNonFullArray[x + xOffset][y + yOffset] != null
                                            && !stringNonFullArray[x + xOffset][y + yOffset].isEmpty())
                                        LOGGER.warning("Overlap at [" + (x + xOffset) + "][" + (y + yOffset)
                                                + "] between full array n°"
                                                + stringNonFullArray[x + xOffset][y + yOffset].trim()
                                                + " and full array n°" + validFullArrayIndex);
                                    // builds the content of the cell
                                    final StringBuilder cellContentBuilder = new StringBuilder();
                                    final int leadingWhiteSpaceCount = (maxCharCountPerCell
                                            - validFullArrayIndexStringLength) / 2;
                                    final int trailingWhiteSpaceCount = maxCharCountPerCell
                                            - validFullArrayIndexStringLength - leadingWhiteSpaceCount;
                                    for (int spaceIndex = 0; spaceIndex < leadingWhiteSpaceCount; spaceIndex++)
                                        cellContentBuilder.append(' ');
                                    cellContentBuilder.append(validFullArrayIndexString);
                                    for (int spaceIndex = 0; spaceIndex < trailingWhiteSpaceCount; spaceIndex++)
                                        cellContentBuilder.append(' ');
                                    // sets the content of the cell
                                    stringNonFullArray[x + xOffset][y + yOffset] = cellContentBuilder.toString();
                                }
                        }
                    validFullArrayIndex++;
                }
            }
        }
        // fills the empty cell with a string containing spaces
        final StringBuilder emptyCellContentBuilder = new StringBuilder();
        for (int spaceIndex = 0; spaceIndex < maxCharCountPerCell; spaceIndex++)
            emptyCellContentBuilder.append(' ');
        final String emptyCellContent = emptyCellContentBuilder.toString();
        // for each column, i.e for each abscissa
        for (int x = 0; x < realMaxVisibleColumnCount; x++) {
            // for each row, i.e for each ordinate
            for (int y = 0; y < realMaxVisibleRowCount; y++)
                // if the cell is empty
                if (stringNonFullArray[x][y] == null || stringNonFullArray[x][y].isEmpty()) {
                    // puts a string with empty spaces into it (to preserve the
                    // alignment with the other cells)
                    stringNonFullArray[x][y] = emptyCellContent;
                }
        }
        return toString(stringNonFullArray, true, null);
    }

    /**
     * Creates a map of full arrays from a potentially non full array. It tries
     * to minimize the count of full arrays and to maximize their respective
     * sizes.
     * 
     * @param array
     *            potentially non full array
     * @return map of full arrays whose keys are their respective locations, the
     *         insertion order is preserved
     */
    public <T> LinkedHashMap<Vector2i, T[][]> computeFullArraysFromNonFullArray(final T[][] array) {
        return computeFullArraysFromNonFullArray(array, (OccupancyCheck<T>) null);
    }

    /**
     * Creates a map of full arrays from a potentially non full array. It tries
     * to minimize the count of full arrays and to maximize their respective
     * sizes.
     * 
     * @param array
     *            potentially non full array
     * @param occupancyCheck
     *            occupancy check, tells whether the object "occupies" the array
     *            cell, can be null. If <code>null</code>, the cell isn't
     *            occupied if it contains <code>null</code>
     * @return map of full arrays whose keys are their respective locations, the
     *         insertion order is preserved
     */
    public <T> LinkedHashMap<Vector2i, T[][]> computeFullArraysFromNonFullArray(final T[][] array,
            final OccupancyCheck<T> occupancyCheck) {
        // creates an occupancy map that will be updated (instead of modifying
        // the supplied array)
        final OccupancyMap occupancyMapObj = createPackedOccupancyMap(array, occupancyCheck);
        return computeFullArraysFromNonFullArray(array, occupancyMapObj);
    }

    /**
     * Creates a map of full arrays from a potentially non full array. It tries
     * to minimize the count of full arrays and to maximize their respective
     * sizes.
     * 
     * @param array
     *            potentially non full array
     * @param occupancyMapObj
     *            occupancy map reflecting the occupancy of the passed array
     * @return map of full arrays whose keys are their respective locations, the
     *         insertion order is preserved
     */
    public <T> LinkedHashMap<Vector2i, T[][]> computeFullArraysFromNonFullArray(final T[][] array,
            final OccupancyMap occupancyMapObj) {
        final LinkedHashMap<Vector2i, T[][]> fullArraysMap = new LinkedHashMap<>();
        // if the array isn't empty (then the occupancy map isn't empty)
        while (!occupancyMapObj.isEmpty()) {
            final int smallestRowIndex = occupancyMapObj.getSmallestRowIndex();
            final int smallestColumnIndex = occupancyMapObj.getSmallestColumnIndex();
            final int rowCount = occupancyMapObj.getRowCount();
            final int columnCount = occupancyMapObj.getColumnCount();
            /**
             * As Java is unable to create a generic array by directly using the
             * generic type, it is necessary to retrieve it thanks to the
             * reflection
             */
            final Class<?> arrayComponentType = array.getClass().getComponentType().getComponentType();
            // finds the isolated sets of adjacent triangles that could be used
            // to create quads
            // the secondary size is the least important size of the chunk
            for (int secondarySize = 1; secondarySize <= Math.max(rowCount, columnCount)
                    && !occupancyMapObj.isEmpty(); secondarySize++) {
                // the primary size is the most important size of the chunk
                for (int primarySize = 1; primarySize <= Math.max(rowCount, columnCount)
                        && !occupancyMapObj.isEmpty(); primarySize++) {
                    // checks whether there are enough cells to occupy. This
                    // test drastically improves the performance
                    if (primarySize * secondarySize <= occupancyMapObj.getOccupiedCellCount()) {
                        // for each column, i.e for each abscissa
                        for (int x = 0; x < columnCount && !occupancyMapObj.isEmpty(); x++) {
                            if (occupancyMapObj.hasNonNullColumn(x)) {
                                // for each row, i.e for each ordinate
                                for (int y = 0; y < occupancyMapObj.getRowCount(x) && !occupancyMapObj.isEmpty(); y++) {
                                    // if this element is occupied
                                    if (occupancyMapObj.getValue(x, y)) {
                                        // looks for an isolated element
                                        // horizontal checks (rows)
                                        int subsectionRowCount = secondarySize;
                                        int subsectionColumnCount = primarySize;
                                        if (// avoids to go beyond the occupancy
                                            // map
                                        subsectionColumnCount + x <= columnCount && subsectionRowCount + y <= rowCount
                                                &&
                                                // avoids to go beyond the
                                                // passed array
                                        subsectionColumnCount + x + smallestColumnIndex <= array.length
                                                && array[subsectionColumnCount + x + smallestColumnIndex - 1] != null
                                                && subsectionRowCount + y
                                                        + smallestRowIndex <= array[subsectionColumnCount + x
                                                                + smallestColumnIndex - 1].length
                                                &&
                                                // checks if the current set of
                                                // rows is isolated
                                        occupancyMapObj.isRectangularSubSectionLocallyIsolated(x, y, subsectionRowCount,
                                                subsectionColumnCount, true) &&
                                                // checks if there is no row
                                                // above the current set of rows
                                                // or if this row isn't isolated
                                        (y - 1 < 0 || !occupancyMapObj.isRectangularSubSectionLocallyIsolated(x, y - 1,
                                                1, subsectionColumnCount, true)) &&
                                                // checks if there is no row
                                                // below the current set of rows
                                                // or if this row isn't isolated
                                        (y + subsectionRowCount >= rowCount
                                                || !occupancyMapObj.isRectangularSubSectionLocallyIsolated(x,
                                                        y + subsectionRowCount, 1, subsectionColumnCount, true))) {
                                            // checks whether the candidate full
                                            // array doesn't go beyond the
                                            // occupancy map
                                            boolean isSubsectionFullyOccupied = true;
                                            for (int i = 0; i < subsectionColumnCount
                                                    && isSubsectionFullyOccupied; i++) {
                                                isSubsectionFullyOccupied &= occupancyMapObj.hasNonNullColumn(i + x)
                                                        && subsectionRowCount + y <= occupancyMapObj.getRowCount(i + x);
                                                for (int j = 0; j < subsectionRowCount
                                                        && isSubsectionFullyOccupied; j++)
                                                    isSubsectionFullyOccupied &= occupancyMapObj.getValue(i + x, j + y);
                                            }
                                            if (isSubsectionFullyOccupied) {
                                                @SuppressWarnings("unchecked")
                                                final T[][] fullArray = (T[][]) Array.newInstance(arrayComponentType,
                                                        subsectionColumnCount, subsectionRowCount);
                                                // copies the elements of the
                                                // chunk into the sub-array and
                                                // marks them as removed from
                                                // the occupancy map
                                                for (int i = 0; i < subsectionColumnCount; i++) {
                                                    System.arraycopy(array[i + x + smallestColumnIndex],
                                                            y + smallestRowIndex, fullArray[i], 0, subsectionRowCount);
                                                    for (int j = 0; j < subsectionRowCount; j++) {
                                                        if (!occupancyMapObj.getValue(i + x, j + y))
                                                            LOGGER.warning(
                                                                    "Overlap at [" + (i + x + smallestColumnIndex)
                                                                            + "][" + (j + y + smallestRowIndex) + "]");
                                                        else
                                                            occupancyMapObj.setValue(i + x, j + y, false);
                                                    }
                                                }
                                                // puts the location of the full
                                                // array and the array into the
                                                // map
                                                if (fullArraysMap.put(
                                                        new Vector2i(x + smallestColumnIndex, y + smallestRowIndex),
                                                        fullArray) != null)
                                                    LOGGER.warning("Overlap at [" + (x + smallestColumnIndex) + "]["
                                                            + (y + smallestRowIndex) + "]");
                                            }
                                        } else {// vertical checks (columns)
                                            subsectionRowCount = primarySize;
                                            subsectionColumnCount = secondarySize;
                                            if (// avoids to go beyond the
                                                // occupancy map
                                            subsectionColumnCount + x <= columnCount
                                                    && subsectionRowCount + y <= rowCount &&
                                                    // avoids to go beyond the
                                                    // passed array
                                            subsectionColumnCount + x + smallestColumnIndex <= array.length
                                                    && array[subsectionColumnCount + x + smallestColumnIndex
                                                            - 1] != null
                                                    && subsectionRowCount + y
                                                            + smallestRowIndex <= array[subsectionColumnCount + x
                                                                    + smallestColumnIndex - 1].length
                                                    &&
                                                    // checks if the current set
                                                    // of columns is isolated
                                            occupancyMapObj.isRectangularSubSectionLocallyIsolated(x, y,
                                                    subsectionRowCount, subsectionColumnCount, false) &&
                                                    // checks if there is no
                                                    // column above the current
                                                    // set of columns or if this
                                                    // column isn't isolated
                                            (x - 1 < 0 || !occupancyMapObj.isRectangularSubSectionLocallyIsolated(x - 1,
                                                    y, subsectionRowCount, 1, false)) &&
                                                    // checks if there is no
                                                    // column below the current
                                                    // set of columns or if this
                                                    // column isn't isolated
                                            (x + subsectionColumnCount >= columnCount || !occupancyMapObj
                                                    .isRectangularSubSectionLocallyIsolated(x + subsectionColumnCount,
                                                            y, subsectionRowCount, 1, false))) {
                                                // checks whether the candidate
                                                // full array doesn't go beyond
                                                // the occupancy map
                                                boolean isSubsectionFullyOccupied = true;
                                                for (int i = 0; i < subsectionColumnCount
                                                        && isSubsectionFullyOccupied; i++) {
                                                    isSubsectionFullyOccupied &= occupancyMapObj.hasNonNullColumn(i + x)
                                                            && subsectionRowCount + y <= occupancyMapObj
                                                                    .getRowCount(i + x);
                                                    for (int j = 0; j < subsectionRowCount
                                                            && isSubsectionFullyOccupied; j++)
                                                        isSubsectionFullyOccupied &= occupancyMapObj.getValue(i + x,
                                                                j + y);
                                                }
                                                if (isSubsectionFullyOccupied) {
                                                    @SuppressWarnings("unchecked")
                                                    final T[][] fullArray = (T[][]) Array.newInstance(
                                                            arrayComponentType, subsectionColumnCount,
                                                            subsectionRowCount);
                                                    // copies the elements of
                                                    // the chunk into the
                                                    // sub-array and marks them
                                                    // as removed from the
                                                    // occupancy map
                                                    for (int i = 0; i < subsectionColumnCount; i++) {
                                                        System.arraycopy(array[i + x + smallestColumnIndex],
                                                                y + smallestRowIndex, fullArray[i], 0,
                                                                subsectionRowCount);
                                                        for (int j = 0; j < subsectionRowCount; j++) {
                                                            if (!occupancyMapObj.getValue(i + x, j + y))
                                                                LOGGER.warning("Overlap at ["
                                                                        + (i + x + smallestColumnIndex) + "]["
                                                                        + (j + y + smallestRowIndex) + "]");
                                                            else
                                                                occupancyMapObj.setValue(i + x, j + y, false);
                                                        }
                                                    }
                                                    // puts the location of the
                                                    // full array and the array
                                                    // into the map
                                                    if (fullArraysMap.put(
                                                            new Vector2i(x + smallestColumnIndex, y + smallestRowIndex),
                                                            fullArray) != null)
                                                        LOGGER.warning("Overlap at [" + (x + smallestColumnIndex) + "]["
                                                                + (y + smallestRowIndex) + "]");
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        return (fullArraysMap);
    }
}
