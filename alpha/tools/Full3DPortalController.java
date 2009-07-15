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
package tools;

public final class Full3DPortalController{
    
    
    private Full3DCellController[] linkedCellsControllers;
    
    private Full3DPortal model;
    
    private Full3DPortalView view;

    
    public Full3DPortalController(Full3DPortal model,Full3DPortalView view){
        this.model=model;
        this.model.setController(this);
        this.linkedCellsControllers=new Full3DCellController[2];
        for(int i=0;i<linkedCellsControllers.length;i++)
            linkedCellsControllers[i]=model.getLinkedCells()[i].getController();      
        if(view!=null)
            {this.view=view;
             this.view.setController(this);
            }
    }
    
    public Full3DPortalController(Full3DPortal model){
        this(model,null);
    }
    
    public final void setView(Full3DPortalView view){
        this.view=view;
    }

    public final Full3DCellController[] getLinkedCellsControllers(){
        return(linkedCellsControllers);
    }
    
    public final float[][] getPortalVertices(){
        return(model.getPortalVertices());
    }
}
