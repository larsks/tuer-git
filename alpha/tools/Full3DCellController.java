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

import java.awt.Rectangle;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;
import com.jogamp.common.nio.Buffers;

public final class Full3DCellController{
    
    
    //TODO: move it in the view
    private FloatBuffer internalBuffer;
    
    private List<Full3DPortalController> portalsControllersList;
    
    private Full3DCell model;
    
    private Full3DCellView view;
    
    
    public Full3DCellController(Full3DCell full3DCellModel,Full3DCellView full3DCellView){
        this.portalsControllersList=new ArrayList<>();
        this.model=full3DCellModel;
        this.model.setController(this);       
        this.internalBuffer=Buffers.newDirectFloatBuffer(
                (model.getBottomWalls().size()+
                 model.getTopWalls().size()+
                 model.getLeftWalls().size()+
                 model.getRightWalls().size()+
                 model.getCeilWalls().size()+
                 model.getFloorWalls().size())*5);//5 because T2_V3
        for(float[] wall:model.getBottomWalls())
            this.internalBuffer.put(wall);
        for(float[] wall:model.getTopWalls())
            this.internalBuffer.put(wall);
        for(float[] wall:model.getLeftWalls())
            this.internalBuffer.put(wall);
        for(float[] wall:model.getRightWalls())
            this.internalBuffer.put(wall);
        for(float[] wall:model.getCeilWalls())
            this.internalBuffer.put(wall);
        for(float[] wall:model.getFloorWalls())
            this.internalBuffer.put(wall);
        this.internalBuffer.rewind();
        if(full3DCellView!=null)
            {//the view is bound to its controller here because now, the buffer is ready
             this.view=full3DCellView;
             this.view.setController(this);
            }               
    }
    
    public Full3DCellController(Full3DCell full3DCellModel){
        this(full3DCellModel,null);
    }


    public final FloatBuffer getInternalBuffer(){
        return(internalBuffer);
    }

    public final Full3DCellView getView(){
        return(view);
    }
    
    public final void setView(Full3DCellView view){
        this.view=view;
    }
    
    public final void addPortalController(Full3DPortalController portalController){
        portalsControllersList.add(portalController);
    }

    public final Full3DCell getModel(){
        return(model);
    }
    
    public final Rectangle getEnclosingRectangle(){
        return(model.getEnclosingRectangle());
    }

    public final List<float[]> getTopWalls(){
        return(model.getTopWalls());
    }

    public final List<float[]> getBottomWalls(){
        return(model.getBottomWalls());
    }

    public final List<float[]> getTopPortals(){
        return(model.getTopPortals());
    }

    public final List<float[]> getBottomPortals(){
        return(model.getBottomPortals());
    }

    public final List<float[]> getLeftWalls(){
        return(model.getLeftWalls());
    }

    public final List<float[]> getRightWalls(){
        return(model.getRightWalls());
    }

    public final List<float[]> getLeftPortals(){
        return(model.getLeftPortals());
    }

    public final List<float[]> getRightPortals() {
        return(model.getRightPortals());
    }
    
    public final boolean contains(float[] point){
        return(model.contains(point));
    }
    
    public final boolean contains(float x,float y,float z){
        return(model.contains(x,y,z));
    }

    public final List<float[]> getCeilWalls(){
        return(model.getCeilWalls());
    }

    public final List<float[]> getFloorWalls(){
        return(model.getFloorWalls());
    }

    public final List<float[]> getCeilPortals(){
        return(model.getCeilPortals());
    }

    public final List<float[]> getFloorPortals(){
        return(model.getFloorPortals());
    }
    
    public final Full3DPortalController getPortalController(Full3DCellController neighbourCellController){
        Full3DPortalController portalController=null;
        Full3DCellController[] linkedCellsControllers;
        for(Full3DPortalController currentPortalController:portalsControllersList)
            {linkedCellsControllers=currentPortalController.getLinkedCellsControllers();
             if(neighbourCellController==linkedCellsControllers[0]||neighbourCellController==linkedCellsControllers[1])           
                 {portalController=currentPortalController;
                  break;
                 }
            }
        return(portalController);
    }
    
    public final Full3DPortalController getPortalController(int index){
        return(portalsControllersList.get(index));
    }
    
    public final int getNeighboursControllersCount(){
        return(portalsControllersList.size());
    }
    
    public final Full3DCellController getNeighbourCellController(int index){
        Full3DCellController[] linkedCellsControllers=portalsControllersList.get(index).getLinkedCellsControllers();
        return(linkedCellsControllers[0]==this?linkedCellsControllers[1]:linkedCellsControllers[0]); 
    }
}
