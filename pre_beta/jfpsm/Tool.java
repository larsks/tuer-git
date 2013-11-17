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

public abstract class Tool extends JFPSMToolUserObject{


	private static final long serialVersionUID=1L;
	
	public Tool(final String name){
		super(name);
	}
	
	@Override
    public final boolean isDirty(){
		return(false);
	}
	
	@Override
    public final void unmarkDirty(){}
    
    @Override
    public final void markDirty(){}

	@Override
    final boolean canInstantiateChildren(){
        return(true);
    }

    @Override
    final boolean isOpenable(){
        return(true);
    }

    @Override
    final boolean isRemovable(){
        return(false);
    }
}
