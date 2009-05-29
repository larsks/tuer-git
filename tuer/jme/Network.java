package jme;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import com.jme.bounding.BoundingVolume;
import com.jme.math.Matrix4f;
import com.jme.math.Vector3f;
import com.jme.renderer.AbstractCamera;
import com.jme.renderer.Camera;
import com.jme.renderer.Renderer;
import com.jme.renderer.jogl.JOGLCamera;
import com.jme.scene.Node;
import com.jme.scene.Spatial;
import com.jme.scene.TriMesh;
import com.jme.system.DisplaySystem;

import bean.NodeIdentifier;

final class Network extends IdentifiedNode{

    
    private static final long serialVersionUID=1L;
    
    private LocalizerVisitor localizer;
    
    private transient VisibleCellsLocalizerVisitor visibleCellsLocalizer;

    
    Network(){
        this(NodeIdentifier.unknownID,NodeIdentifier.unknownID);
    }

    Network(int levelID,int networkID){
        super(levelID,networkID);
        localizer=new LocalizerVisitor(this,new Vector3f());
        visibleCellsLocalizer=new VisibleCellsLocalizerVisitor(this,DisplaySystem.getDisplaySystem().getRenderer().getCamera());
    }
    
    @Override
    public void draw(Renderer r){
        if(children!=null)
            {Cell child;
             List<Spatial> cellChildren;
             List<Spatial> markedChildren=new ArrayList<Spatial>();
             List<Spatial> hiddenChildren=new ArrayList<Spatial>();
             Node target;
             InternalCellElement internalCellElement;
             for(int i=0,cSize=children.size();i<cSize;i++)
                 {child=(Cell)children.get(i);
                  if(child!=null&&child.getLocalCullHint().equals(CullHint.Never))
                      {cellChildren=child.getChildren();
                       if(cellChildren!=null)
                           {for(Spatial cellChild:cellChildren)
                                {//several  cell elements may represent the same node
                                 internalCellElement=(InternalCellElement)cellChild;
                                 if(internalCellElement.isShared())
                                     {target=internalCellElement.getSharableNode();
                                      if(markedChildren.contains(target))
                                          {hiddenChildren.add(cellChild);
                                           //hide the already drawn object
                                           cellChild.setCullHint(CullHint.Always);
                                          }
                                      else
                                          //mark this object to avoid further drawing
                                          markedChildren.add(target);
                                     }                                     
                                }
                           }
                       child.onDraw(r);
                       if(cellChildren!=null)
                           {//show again hidden children
                            for(Spatial hiddenChild:hiddenChildren)
                                hiddenChild.setCullHint(CullHint.Inherit);
                            hiddenChildren.clear();
                           }
                      }
                 }
            }       
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
    
    final List<Cell> getContainingNodesList(Spatial spatial){       
        Cell currentLocation=locate(spatial.getWorldBound().getCenter());
        List<Cell> containingNodesList=new ArrayList<Cell>();
        if(currentLocation!=null)
            {ContainingCellsLocalizerVisitor visitor=new ContainingCellsLocalizerVisitor(currentLocation,spatial);
             visitor.visit(currentLocation);
             containingNodesList.addAll(visitor.containingNodesList);
            }
        return(containingNodesList);
    }
    
    final List</*VisibleCellsLocalizerVisitor.*/FrustumParameters> getFrustumParametersList(){
        return(visibleCellsLocalizer.storedFrustumParametersList);
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
    
    private static final class ContainingCellsLocalizerVisitor extends BreadthFirstSearchVisitor{

        
        private List<Cell> containingNodesList;
        
        private Spatial spatial;
        
        private boolean hasToAddTheFirstCell;
        
        
        private ContainingCellsLocalizerVisitor(Cell cellLocation,Spatial spatial){
            super(cellLocation);
            this.containingNodesList=new ArrayList<Cell>();
            this.spatial=spatial;
            this.hasToAddTheFirstCell=true;
        }
        
        @Override
        protected final boolean performTaskOnCurrentlyVisitedCell(){
            if(hasToAddTheFirstCell)
                {//add the location into the list
                 containingNodesList.add(getCurrentlyVisitedCell());
                 hasToAddTheFirstCell=false;
                }
            return(true);
        }
        
        @Override
        protected final boolean hasToPush(Cell son,Portal portal){
            //check if the spatial intersects with the portal
            boolean result=spatial.getWorldBound().intersects(portal.getWorldBound());
            if(result)
                containingNodesList.add(son);
            return(result);
        }
        
        @Override
        protected final void clearInternalStorage(){
            super.clearInternalStorage();
            containingNodesList.clear();
            hasToAddTheFirstCell=true;
        }
    }
    
    private static final class VisibleCellsLocalizerVisitor extends BreadthFirstSearchVisitor{

        
        /**
         * if true, the portal culling is enabled, otherwise
         * the view frustum culling is enabled
         */
        private static final boolean enablePortalCulling=false;
        
        private List<Cell> visibleCellsList;       
        /**
         * initial frustum (the whole view frustum)
         */
        private Camera initialCamera;
        /**
         * list of frustum parameters generated
         * from portals
         * (left,right,bottom,top)
         */
        private List<FrustumParameters> frustumParametersList;
        private List<FrustumParameters> storedFrustumParametersList;
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
            this.frustumParametersList=new ArrayList<FrustumParameters>();
            this.storedFrustumParametersList=new ArrayList<FrustumParameters>();
            AbstractCamera cam=(AbstractCamera)initialCamera;
            this.prefrustum=new JOGLCamera(cam.getWidth(),cam.getHeight(),true);                      
            this.currentCamera=new JOGLCamera(cam.getWidth(),cam.getHeight(),true);
            this.currentCamera.setFrame(initialCamera.getLocation(),initialCamera.getLeft(),
                                        initialCamera.getUp(),initialCamera.getDirection());
            this.currentCamera.setFrustum(initialCamera.getFrustumNear(),initialCamera.getFrustumFar(),
                                          initialCamera.getFrustumLeft(),initialCamera.getFrustumRight(),
                                          initialCamera.getFrustumTop(),initialCamera.getFrustumBottom());
            this.currentCamera.update();
        }
        
        private VisibleCellsLocalizerVisitor(Network network,Camera camera){
            this(network.children!=null?(Cell)network.children.get(0):null,camera);
        }
        
        @Override
        protected final boolean performTaskOnCurrentlyVisitedCell(){
            //use the frustum parameters that match with the current cell            
            if(enablePortalCulling)
                frustumParametersList.remove(getNextCellIndex()).update(currentCamera);            
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
            Camera.FrustumIntersect intersectionBetweenPortalAndSubfrustum=cam.contains(portalWorldBound);
            boolean isPortalInSubFrustum=intersectionBetweenPortalAndSubfrustum!=Camera.FrustumIntersect.Outside;            
            boolean isPortalInSubPrefrustum=false;
            //try to detect when the portal is between the near plane and the observer     
            if(!isPortalInSubFrustum)
                {portalWorldBound.setCheckPlane(0);
                 prefrustum.setPlaneState(0);             
                 prefrustum.setFrustum(cam.getFrustumNear(),cam.getFrustumFar(),cam.getFrustumLeft(),cam.getFrustumRight(),cam.getFrustumTop(),cam.getFrustumBottom());
                 prefrustum.setFrame(cam.getLocation().clone(),cam.getLeft(),cam.getUp(),cam.getDirection());
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
                     isPortalInSubPrefrustum=true;
                }
            cam.setPlaneState(previousCamPlaneState);
            portalWorldBound.setCheckPlane(previousVolumeCheckPlane);      
            //if the portal is in the subfrustum,
            //compute another subfrustum from it
            if(isPortalInSubFrustum&&enablePortalCulling)
                {FrustumParameters subFrustumParam=null;
                 if(isPortalInSubPrefrustum)
                     subFrustumParam=new FrustumParameters(currentCamera);
                 else
                     {subFrustumParam=computeSubFrustumParameters(portal,intersectionBetweenPortalAndSubfrustum);
                      if(subFrustumParam==null)
                          isPortalInSubFrustum=false;
                     }
                 if(isPortalInSubFrustum)
                     {frustumParametersList.add(subFrustumParam);
                      storedFrustumParametersList.add(subFrustumParam);
                     }
                }
            return(isPortalInSubFrustum);
        }
        
        @Override
        protected final void clearInternalStorage(){
            super.clearInternalStorage();
            visibleCellsList.clear();            
            currentCamera.setFrame(initialCamera.getLocation(),initialCamera.getLeft(),
                    initialCamera.getUp(),initialCamera.getDirection());           
            if(enablePortalCulling)
                {frustumParametersList.clear();
                 storedFrustumParametersList.clear();
                 //configure the current camera with the setup of the initial camera
                 FrustumParameters fp=new FrustumParameters(initialCamera);
                 fp.update(currentCamera);
                 frustumParametersList.add(fp);
                 storedFrustumParametersList.add(fp);   
                }
        }   
        
        /**
         * Compute a frustum that contains only what is visible
         * through the portal from the current frustum
         * @param portal restriction of the computed frustum
         * @param intersection status of the intersection between the portal and the current frustum
         * @return
         */
        private final FrustumParameters computeSubFrustumParameters(Portal portal,
                                         Camera.FrustumIntersect intersection){
            //TODO: store the vertices of the triangles inside the portals
            //      to decrease the memory usage
            Vector3f[] trianglesVertices = ((TriMesh)portal.getChild(0)).getMeshAsTrianglesVertices(null);      
            float left=Float.MAX_VALUE;
            float right=-Float.MAX_VALUE;
            float top=-Float.MAX_VALUE;
            float bottom=Float.MAX_VALUE;
            Vector3f vertex=null;
            for(Vector3f triangleVertex:trianglesVertices)
                {vertex=currentCamera.getScreenCoordinates(triangleVertex,vertex);                
                 vertex.x=(((vertex.x/((AbstractCamera)currentCamera).getWidth())/(currentCamera.getViewPortRight()-currentCamera.getViewPortLeft()))*(currentCamera.getFrustumRight()-currentCamera.getFrustumLeft()))+currentCamera.getFrustumLeft();
                 vertex.y=(((vertex.y/((AbstractCamera)currentCamera).getHeight())/(currentCamera.getViewPortTop()-currentCamera.getViewPortBottom()))*(currentCamera.getFrustumTop()-currentCamera.getFrustumBottom()))+currentCamera.getFrustumBottom();                
                 if(vertex.x<left)
                     left=vertex.x;
                 if(vertex.x>right)
                     right=vertex.x;
                 if(vertex.y>top)
                     top=vertex.y;
                 if(vertex.y<bottom)
                     bottom=vertex.y;
                }           
            FrustumParameters subFrustumParam;
            //if the portal is outside the current frustum
            if(bottom>currentCamera.getFrustumTop()||
               top<currentCamera.getFrustumBottom()||
               right<currentCamera.getFrustumLeft()||
               left>currentCamera.getFrustumRight())               
                subFrustumParam=null;
            else
                {left=Math.max(left,currentCamera.getFrustumLeft());
                 right=Math.min(right,currentCamera.getFrustumRight());
                 top=Math.min(top,currentCamera.getFrustumTop());
                 bottom=Math.max(bottom,currentCamera.getFrustumBottom());               
                 subFrustumParam=new FrustumParameters(left,right,bottom,top);
                }
            return(subFrustumParam);
        }
    }
    
    /*private*/ static final class FrustumParameters{
        
        
        private final float left;
        
        private final float right;
        
        private final float bottom;
        
        private final float top;
        
        private FrustumParameters(Camera camera){
            this.left=camera.getFrustumLeft();
            this.right=camera.getFrustumRight();
            this.bottom=camera.getFrustumBottom();
            this.top=camera.getFrustumTop();
        }
        
        private FrustumParameters(float left,float right,float bottom,float top){
            this.left=left;
            this.right=right;
            this.bottom=bottom;
            this.top=top;
        }
        
        private final void update(Camera camera){
            camera.setFrustum(camera.getFrustumNear(),camera.getFrustumFar(), left, right, top, bottom);
            camera.update();
        }

        public final float getLeft(){
            return(left);
        }

        public final float getRight(){
            return(right);
        }

        public final float getBottom(){
            return(bottom);
        }

        public final float getTop(){
            return(top);
        }
    }
}
