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
 * Set of tools
 * 
 * @author Julien Gouesse
 *
 */
public final class ToolSet extends JFPSMUserObject{

	private static final long serialVersionUID=1L;
	
	//TODO add a list of tools
	
	public ToolSet(){
		this("");
	}
	
	public ToolSet(final String name){
		super(name);
	}

	@Override
	public boolean isDirty(){
		return(false);
	}

	@Override
	public void markDirty(){
	}

	@Override
	public void unmarkDirty(){
	}

	@Override
    final boolean isOpenable(){
        //it is always open and it cannot be closed
        return(false);
    }

    @Override
    final boolean isRemovable(){
        return(false);
    }

	@Override
	boolean canInstantiateChildren(){
		return(false);
	}
}
