package jme;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.jme.bounding.BoundingVolume;
import com.jme.math.Matrix4f;
import com.jme.math.Vector3f;
import com.jme.renderer.AbstractCamera;
import com.jme.renderer.Camera;
import com.jme.renderer.jogl.JOGLCamera;
import com.jme.scene.TriMesh;
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
        
        private Camera prefrustum;
        
        
        private VisibleCellsLocalizerVisitor(Cell firstVisitedCell,Camera initialCamera){
            super(firstVisitedCell);
            this.visibleCellsList=new ArrayList<Cell>();
            this.initialCamera=initialCamera;
            this.cameraList=new ArrayList<Camera>();
            AbstractCamera cam=(AbstractCamera)initialCamera;
            this.prefrustum=new JOGLCamera(cam.getWidth(),cam.getHeight(),true);
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
            BoundingVolume portalWorldBound=portal.getChild(0).getWorldBound();
            //FIX: reset the plane state to perform the culled test correctly
            AbstractCamera cam=((AbstractCamera)currentCamera);
            int previousCamPlaneState=cam.getPlaneState();
            int previousVolumeCheckPlane=portalWorldBound.getCheckPlane();
            cam.setPlaneState(0);
            portalWorldBound.setCheckPlane(0);
            Camera.FrustumIntersect intersectionBetweenPortalAndSubfrustum=currentCamera.contains(portalWorldBound);
            boolean isPortalInSubFrustum=intersectionBetweenPortalAndSubfrustum!=Camera.FrustumIntersect.Outside;            
            //try to detect when the portal is between the near plane and the observer
            Camera subfrustum=null;
            if(!isPortalInSubFrustum)
                {portalWorldBound.setCheckPlane(0);
                 prefrustum.setPlaneState(0);             
                 prefrustum.setFrustum(cam.getFrustumNear(),cam.getFrustumFar(),cam.getFrustumLeft(),cam.getFrustumRight(),cam.getFrustumTop(),cam.getFrustumBottom());
                 prefrustum.setFrame(cam.getLocation().clone(),cam.getLeft()/*.clone()*/,cam.getUp()/*.clone()*/,cam.getDirection()/*.clone()*/);
                 //move back the frustum to the observer's location
                 prefrustum.getLocation().subtractLocal(cam.getDirection().normalize().multLocal(cam.getFrustumNear()));
                 //restrict the prefrustum volume
                 Matrix4f projectionMatrix=cam.getProjectionMatrix();
                 //it uses the center of projection
                 float thalesRatio=-projectionMatrix.m22/(cam.getFrustumNear()-projectionMatrix.m22);
                 prefrustum.setFrustumLeft(thalesRatio*prefrustum.getFrustumLeft());
                 prefrustum.setFrustumRight(thalesRatio*prefrustum.getFrustumRight());
                 prefrustum.setFrustumTop(thalesRatio*prefrustum.getFrustumTop());
                 prefrustum.setFrustumBottom(thalesRatio*prefrustum.getFrustumBottom());    
                 prefrustum.update();
                 //if true, the sub-frustum is the current frustum
                 isPortalInSubFrustum=prefrustum.contains(portalWorldBound)!=Camera.FrustumIntersect.Outside;                 
                 if(isPortalInSubFrustum)
                     subfrustum=currentCamera;
                }
            cam.setPlaneState(previousCamPlaneState);
            portalWorldBound.setCheckPlane(previousVolumeCheckPlane);      
            //if the portal is in the subfrustum,
            //compute another subfrustum from it
            if(isPortalInSubFrustum)
                {if(subfrustum==null)
                     subfrustum=computeSubfrustum(portal,intersectionBetweenPortalAndSubfrustum);
                 if(subfrustum==null)
                     isPortalInSubFrustum=false;
                 else
                     cameraList.add(subfrustum);
                }
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
         * Compute a frustum that contains only what is visible
         * through the portal from the current frustum
         * @param portal restriction of the computed frustum
         * @param intersection status of the intersection between the portal and the current frustum
         * @return
         */
        private final Camera computeSubfrustum(Portal portal,
                                         Camera.FrustumIntersect intersection){
            //TODO: store the vertices of the triangles inside the portals
            //      to decrease the memory usage
            /*Vector3f[] trianglesVertices = ((TriMesh)portal.getChild(0)).getMeshAsTrianglesVertices(null);      
            float left=Float.MAX_VALUE;
            float right=-Float.MAX_VALUE;
            float top=-Float.MAX_VALUE;
            float bottom=Float.MAX_VALUE;
            Vector3f vertex=null;
            for(Vector3f triangleVertex:trianglesVertices)
                {vertex=currentCamera.getFrustumCoordinates(triangleVertex,vertex);                
                 if(vertex.x<left)
                     left=vertex.x;
                 if(vertex.x>right)
                     right=vertex.x;
                 if(vertex.y>top)
                     top=vertex.y;
                 if(vertex.y<bottom)
                     bottom=vertex.y;
                }  
            Camera subFrustum;
            //if the portal is outside the current frustum
            if(bottom>currentCamera.getFrustumTop()||
               top<currentCamera.getFrustumBottom()||
               right<currentCamera.getFrustumLeft()||
               left>currentCamera.getFrustumRight())
                subFrustum=null;
            else
                {left=Math.max(left,currentCamera.getFrustumLeft());
                 right=Math.min(right,currentCamera.getFrustumRight());
                 top=Math.min(top,currentCamera.getFrustumTop());
                 bottom=Math.max(bottom,currentCamera.getFrustumBottom());
                 AbstractCamera cam=((AbstractCamera)currentCamera);
                 subFrustum=new JOGLCamera(cam.getWidth(),cam.getHeight(),true);
                 subFrustum.setFrustum(cam.getFrustumNear(),cam.getFrustumFar(),left,right,top,bottom);
                 subFrustum.setFrame(cam.getLocation(),cam.getLeft(),cam.getUp(),cam.getDirection());
                }           
            //TODO: uncomment this when the code is no more buggy
            return(subFrustum);*/
            //System.out.println(intersection.toString()+" "+left+" "+right+" ");
            return(initialCamera);
        }
    }
}
