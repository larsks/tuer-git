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
package jfpsm;

/**
 * logical spatial abstract data type
 * @author Julien Gouesse
 *
 */
public interface Grid{

    /**
     * get the physical position of a section from its logical position
     * @param i logical abscissa
     * @param j logical ordinate
     * @param k logical applicate
     * @return physical position of a section
     */
    public float[] getSectionPhysicalPosition(int i,int j,int k);
    
    public int[] getSectionLogicalPosition(float x,float y,float z);
    
    public int getLogicalWidth();
    
    public int getLogicalHeight();
    
    public int getLogicalDepth();
    
    public float getSectionPhysicalWidth(int i,int j,int k);
    
    public float getSectionPhysicalHeight(int i,int j,int k);
    
    public float getSectionPhysicalDepth(int i,int j,int k);
}
