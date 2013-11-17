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

public abstract class JFPSMProjectUserObject extends JFPSMUserObject{

	
	private static final long serialVersionUID=1L;

	public JFPSMProjectUserObject(final String name){
		super(name);
	}
	
	/**
     * Creates a dedicated viewer for this object, it returns <code>null</code> if it has no such viewer
     * 
     * @param project project in which this object is, can be null if it does not depend on any project
     * @param projectManager project manager
     * @return
     */
    public Viewer createViewer(final Project project,final ProjectManager projectManager){
    	return(null);
    }
}
