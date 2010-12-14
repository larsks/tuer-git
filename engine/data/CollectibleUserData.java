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
package engine.data;

public abstract class CollectibleUserData{
	/**source name of the sound played when picking up this kind of object*/
	private final String sourcename;
	
	public CollectibleUserData(final String sourcename){
		this.sourcename=sourcename;
	}
	
	public final String getSourcename(){
		return(sourcename);
	}
}