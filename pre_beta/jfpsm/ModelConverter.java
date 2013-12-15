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

public class ModelConverter extends JFPSMToolUserObject{

	private static final long serialVersionUID=1L;

	public ModelConverter(){
		this("");
	}
	
	public ModelConverter(final String name){
		super(name);
	}
	
	@Override
    public Viewer createViewer(final ToolManager projectManager){
		//TODO
    	return(null);
    }

	@Override
	public boolean isDirty(){
		return(false);
	}

	@Override
	public void markDirty(){}

	@Override
	public void unmarkDirty(){}

	@Override
	boolean isRemovable(){
		return(true);
	}

	@Override
	boolean isOpenable(){
		return(true);
	}

	@Override
	boolean canInstantiateChildren(){
		return(false);
	}
}