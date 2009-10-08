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

enum MapType{
	
	CONTAINER_MAP("container map","containermap.png"),
	CONTENT_MAP("content map","contentmap.png"),
	LIGHT_MAP("light map","lightmap.png");

	private final String label;
	
	private final String filename;
	
	
	MapType(final String label,final String filename){
		this.label=label;
		this.filename=filename;
	}

	
	final String getLabel(){
		return(label);
	}
	
	final String getFilename(){
		return(filename);
	}
	
	@Override
	public final String toString(){
		return(label);
	}
}
