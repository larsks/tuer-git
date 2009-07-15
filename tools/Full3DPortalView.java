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

public final class Full3DPortalView{

    
    private Full3DCellView[] linkedCellsViews;
    
    private Full3DPortalController controller;
    
    
    public Full3DPortalView(Full3DPortalController controller){
        this.controller=controller;
        this.controller.setView(this);
        this.linkedCellsViews=new Full3DCellView[2];
        for(int i=0;i<linkedCellsViews.length;i++)
            linkedCellsViews[i]=controller.getLinkedCellsControllers()[i].getView();
    }
    
    
    public final void setController(Full3DPortalController controller){
        this.controller=controller;
    }

    public final Full3DPortalController getController(){
        return(controller);
    }

    public final Full3DCellView[] getLinkedCellsViews(){
        return(linkedCellsViews);
    }

    public final void setLinkedCellsViews(Full3DCellView[] linkedCellsViews){
        this.linkedCellsViews=linkedCellsViews;
    }
    
    public final float[][] getPortalVertices(){
        return(controller.getPortalVertices());
    }
}
