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

enum VolumeType {

    CUBOID("cuboid"),
    PARALLELEPIPED("parallelepiped"),
    QUADRILATERAL_FRUSTUM("quadrilateral frustum"),
    DISPLACEMENT_TELEPORTER("displacement teleporter"),
    FLOOR_LINK("floor link");
    
    private final String label;
    
    
    VolumeType(final String label){
        this.label=label;
    }
    
    
    final String getLabel(){
        return(label);
    }
    
    @Override
    public final String toString(){
        return(label);
    }
}
