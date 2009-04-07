package jme;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import com.jme.math.Vector3f;
import com.jme.renderer.Camera;
import com.jme.system.DisplaySystem;

import bean.NodeIdentifier;

final class Network extends IdentifiedNode{

    
    private static final long serialVersionUID=1L;
    
    private LocalizerVisitor localizer;
    
    private VisibleCellsLocalizerVisitor visibleCellsLocalizer;

    
    Network(){
        this(NodeIdentifier.unknownID,NodeIdentifier.unknownID);
    }

    Network(int levelID,int networkID){
        super(levelID,networkID);
        localizer=new LocalizerVisitor(this,new Vector3f());
        visibleCellsLocalizer=new VisibleCellsLocalizerVisitor(this,DisplaySystem.getDisplaySystem().getRenderer().getCamera());
    }
    
    Cell locate(Vector3f position){
        Cell firstVisitedCell;
        if(getChildren()!=null&&getChildren().size()>0)
            firstVisitedCell=(Cell)getChild(0);
        else
            firstVisitedCell=null;
        return(locate(position,firstVisitedCell));
    }

    Cell locate(Vector3f position,Cell previousLocation){
        return(localizer.visit(previousLocation,position)?null:localizer.getCurrentlyVisitedCell());
    }
    
    final List<Cell> getVisibleNodesList(Cell currentLocation){
        visibleCellsLocalizer.visit(currentLocation);      
        return(visibleCellsLocalizer.visibleCellsList);
    }
    
    private static abstract class Visitor{
        
        /**
         * Cell that is visited at first
         */
        private Cell firstVisitedCell;
        /**
         * Cell that is currently visited
         */
        private Cell currentlyVisitedCell;
        /**
         * abstract data type used to store the sons of the current cell
         */
        private List<Cell> cellsList;
        /**
         * Each cell that has been seen has to be marked to avoid an infinite loop
         * (by visiting the same cell more than once)
         */
        private List<Cell> markedCellsList;
        
        
        /**
         * Prepare the visit of the whole network by beginning with the root cell
         * @param network: visited network
         */
        private Visitor(Network network){
            this(network.children!=null?(Cell)network.children.get(0):null);
        }
        
        /**
         * Prepare the visit of the whole network by beginning with the provided cell
         * @param firstVisitedCell: cell that is visited at first
         */
        private Visitor(Cell firstVisitedCell){
            this.firstVisitedCell=firstVisitedCell;
            this.currentlyVisitedCell=null;
            this.cellsList=new ArrayList<Cell>();
            this.markedCellsList=new ArrayList<Cell>();           
        }
        
        /**
         * Starts the visit (not thread-safe)
         */
        final boolean visit(){
            return(visit(this.firstVisitedCell));
        }
        
        /**
         * 
         * @param firstVisitedCell
         * @return true if the network has been fully visited, otherwise false
         */
        final boolean visit(Cell firstVisitedCell){
            clearInternalStorage();
            this.firstVisitedCell=firstVisitedCell;
            if(firstVisitedCell!=null)
                {markedCellsList.add(firstVisitedCell);
                 cellsList.add(firstVisitedCell);
                }
            boolean hasToContinue=true;
            Portal portal;
            Cell son;
            while(!cellsList.isEmpty())
                {//Get the next element (pop operation)
                 currentlyVisitedCell=cellsList.remove(getNextCellIndex());
                 //This is the main treatment
                 if(hasToContinue=performTaskOnCurrentlyVisitedCell())
                     for(int i=0;i<currentlyVisitedCell.getPortalCount();i++)
                         {portal=currentlyVisitedCell.getPortalAt(i);
                          son=portal.getCellAt(0).equals(currentlyVisitedCell)?portal.getCellAt(1):portal.getCellAt(0);      
                          if(!markedCellsList.contains(son))
                              {//Mark the cell to avoid traveling it more than once
                               markedCellsList.add(son);
                               if(hasToPush(son,portal))
                                   {//Add a new cell to travel (push operation)
                                    cellsList.add(son);
                                   }                              
                              }
                         }
                 else
                     break;
                }
            return(hasToContinue);
        }
        
        protected void clearInternalStorage(){
            this.currentlyVisitedCell=null;
            this.cellsList.clear();
            this.markedCellsList.clear();
        }
        
        protected abstract int getNextCellIndex();
        
        /**
         * Allows to perform a task on the currently visited cell.
         * Each cell is visited at most once per visit.
         * @return true if the visit has to go on, otherwise the visit is stopped
         */
        protected abstract boolean performTaskOnCurrentlyVisitedCell();     
        /**
         * Allows to perform a test on the currently visited son of the visited cell. 
         * @return true if this son has to be pushed
         */
        protected boolean hasToPush(Cell son,Portal portal){
            return(true);
        }
        
        protected final Cell getCurrentlyVisitedCell(){
            return(currentlyVisitedCell);
        }
        protected final List<Cell> getCellsList(){
            return(Collections.unmodifiableList(cellsList));
        }

        protected final List<Cell> getMarkedCellsList(){
            return(Collections.unmodifiableList(markedCellsList));
        }      
    }
    
    private static abstract class BreadthFirstSearchVisitor extends Visitor{
        
        
        private BreadthFirstSearchVisitor(Network network){
            super(network.children!=null?(Cell)network.children.get(0):null);
        }
        

        private BreadthFirstSearchVisitor(Cell firstVisitedCell){
            super(firstVisitedCell);
        }
        
        /**
         * The internal list is used as a FIFO
         */
        @Override
        protected final int getNextCellIndex(){
            return(0);
        }
    }
    
    private static final class LocalizerVisitor extends BreadthFirstSearchVisitor{

        
        private Vector3f point;
        
        
        private LocalizerVisitor(Network network,Vector3f point){
            this(network.children!=null?(Cell)network.children.get(0):null,point);
        }
        

        private LocalizerVisitor(Cell firstVisitedCell,Vector3f point){
            super(firstVisitedCell);
            this.point=point;
        }
        
        
        @SuppressWarnings("unused")
        private final boolean visit(Vector3f point){
            this.point=point;
            return(visit());
        }
        
        private final boolean visit(Cell firstVisitedCell,Vector3f point){
            this.point=point;
            return(visit(firstVisitedCell));
        }
        
        @Override
        protected final boolean performTaskOnCurrentlyVisitedCell(){
            return(!getCurrentlyVisitedCell().contains(point));
        }
        
    }
    
    private static final class VisibleCellsLocalizerVisitor extends BreadthFirstSearchVisitor{

        
        private List<Cell> visibleCellsList;       
        /**
         * initial frustum (the whole view frustum)
         */
        private Camera initialCamera;
        /**
         * list of frustum (generated subfrustums)
         */
        private List<Camera> cameraList;
        /**
         * frustum in use 
         * (subfrustum used for the view frustum culling)
         */
        private Camera currentCamera;
        
        
        private VisibleCellsLocalizerVisitor(Cell firstVisitedCell,Camera initialCamera){
            super(firstVisitedCell);
            this.visibleCellsList=new ArrayList<Cell>();
            this.initialCamera=initialCamera;
            this.cameraList=new ArrayList<Camera>();
        }
        
        private VisibleCellsLocalizerVisitor(Network network,Camera camera){
            this(network.children!=null?(Cell)network.children.get(0):null,camera);
        }
        
        @Override
        protected final boolean performTaskOnCurrentlyVisitedCell(){
            //use the camera that matches with the current cell
            currentCamera=cameraList.remove(getNextCellIndex());
            visibleCellsList.add(getCurrentlyVisitedCell());
            return(true);
        }
        
        @Override
        protected final boolean hasToPush(Cell son,Portal portal){
            Camera.FrustumIntersect intersectionBetweenPortalAndSubfrustum=currentCamera.contains(portal.getWorldBound());
            boolean isPortalInSubFrustum=intersectionBetweenPortalAndSubfrustum!=Camera.FrustumIntersect.Outside;
            //if the portal is in the subfrustum,
            //compute another subfrustum from it
            if(isPortalInSubFrustum)
                cameraList.add(computeSubfrustum(portal,intersectionBetweenPortalAndSubfrustum));               
            return(isPortalInSubFrustum);
        }
        
        @Override
        protected final void clearInternalStorage(){
            super.clearInternalStorage();
            visibleCellsList.clear();
            cameraList.clear();
            //add the first fresh frustum in the list (the frustum of the camera)
            cameraList.add(initialCamera);
        }   
        
        /**
         * 
         * @param portal
         * @param intersection status of the intersection between the portal and the current subfrustum
         * @return
         */
        private final Camera computeSubfrustum(Portal portal,
                                         Camera.FrustumIntersect intersection){
            //FIXME: compute the subfrustum by projecting
            //the portal onto the near plane (use getScreenCoordinates() to achieve this)
            return(initialCamera);
        }
    }
}
