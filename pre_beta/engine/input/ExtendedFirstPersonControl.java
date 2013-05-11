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
package engine.input;

import java.util.Set;
import com.ardor3d.framework.Canvas;
import com.ardor3d.input.Key;
import com.ardor3d.input.KeyboardState;
import com.ardor3d.input.MouseState;
import com.ardor3d.input.logical.InputTrigger;
import com.ardor3d.input.logical.LogicalLayer;
import com.ardor3d.input.logical.TriggerAction;
import com.ardor3d.input.logical.TriggerConditions;
import com.ardor3d.input.logical.TwoInputStates;
import com.ardor3d.math.Matrix3;
import com.ardor3d.math.Vector3;
import com.ardor3d.math.type.ReadOnlyVector3;
import com.ardor3d.renderer.Camera;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import engine.input.ActionMap.KeyInput;

/**
 * Adaptation of the class FirstPersonControl in order to handle QWERTY (WSAD) & AZERTY (ZSQD) keyboards correctly. 
 * @author Julien Gouesse
 *
 */
public final class ExtendedFirstPersonControl{

	/**axis headed to up*/
    private final Vector3 upAxis;
    /**turn speed when using the mouse*/
    private double mouseRotateSpeed;
    /**speed of move (front, back ,strafe)*/
    private double moveSpeed;
    /**turn speed when using the arrow keys*/
    private double keyRotateSpeed;
    /**temporary matrix*/
    private final Matrix3 workerMatrix;
    /**temporary vector*/
    private final Vector3 workerStoreA;
    /**flag indicating whether to reserve mouse look up/down*/
    private boolean lookUpDownReversed;
    
    private final Set<KeyInput> moveForwardKeyInputs;
    
    private final Set<KeyInput> moveBackwardKeyInputs;
    
    private final Set<KeyInput> strafeLeftKeyInputs;
    
    private final Set<KeyInput> strafeRightKeyInputs;
    
    private final Set<KeyInput> turnLeftKeyInputs;
    
    private final Set<KeyInput> turnRightKeyInputs;
    
    private final Set<KeyInput> lookUpKeyInputs;
    
    private final Set<KeyInput> lookDownKeyInputs;
    
    private final ActionMap customActionMap;

    public ExtendedFirstPersonControl(final ReadOnlyVector3 upAxis,final ActionMap customActionMap){
    	this.upAxis=new Vector3();
        this.upAxis.set(upAxis);
        mouseRotateSpeed=0.005;
        moveSpeed=5;
        keyRotateSpeed=2.25;
        workerMatrix=new Matrix3();
        workerStoreA=new Vector3();
        this.customActionMap=customActionMap;
        this.moveForwardKeyInputs=customActionMap.getInputs(Action.MOVE_FORWARD);
        this.moveBackwardKeyInputs=customActionMap.getInputs(Action.MOVE_BACKWARD);
        this.strafeLeftKeyInputs=customActionMap.getInputs(Action.STRAFE_LEFT);
        this.strafeRightKeyInputs=customActionMap.getInputs(Action.STRAFE_RIGHT);
        this.turnLeftKeyInputs=customActionMap.getInputs(Action.TURN_LEFT);
        this.turnRightKeyInputs=customActionMap.getInputs(Action.TURN_RIGHT);
        this.lookUpKeyInputs=customActionMap.getInputs(Action.LOOK_UP);
        this.lookDownKeyInputs=customActionMap.getInputs(Action.LOOK_DOWN);
    }

    public ReadOnlyVector3 getUpAxis(){
        return(upAxis);
    }

    public void setUpAxis(final ReadOnlyVector3 upAxis){
    	this.upAxis.set(upAxis);
    }

    public double getMouseRotateSpeed(){
        return(mouseRotateSpeed);
    }

    public void setMouseRotateSpeed(final double speed){
        mouseRotateSpeed=speed;
    }

    public double getMoveSpeed(){
        return(moveSpeed);
    }

    public void setMoveSpeed(final double speed){
        moveSpeed=speed;
    }

    public double getKeyRotateSpeed(){
        return(keyRotateSpeed);
    }

    public void setKeyRotateSpeed(final double speed){
        keyRotateSpeed=speed;
    }

    /**
     * handle all moves performed with the keyboard
     * @param camera
     * @param kb
     * @param tpf
     */
    protected void move(final Camera camera,final KeyboardState kb,final double tpf){
        // MOVEMENT
        int moveFB=0,strafeLR=0;
        for(KeyInput input:moveForwardKeyInputs)
            {final Key key=input.getInputObject();
             if(kb.isDown(key))
            	 moveFB+=1;
            }
        for(KeyInput input:moveBackwardKeyInputs)
            {final Key key=input.getInputObject();
             if(kb.isDown(key))
            	 moveFB-=1;
            }
        for(KeyInput input:strafeLeftKeyInputs)
            {final Key key=input.getInputObject();
             if(kb.isDown(key))
        	     strafeLR+=1;
            }
        for(KeyInput input:strafeRightKeyInputs)
            {final Key key=input.getInputObject();
             if(kb.isDown(key))
            	 strafeLR-=1;
            }
        if(moveFB!=0||strafeLR!=0)
            {final Vector3 loc = workerStoreA.zero();
             if(moveFB==1)
                 loc.addLocal(camera.getDirection());
             else
                 {if(moveFB==-1) 
                      loc.subtractLocal(camera.getDirection());
                 }
             if(strafeLR==1)
                 loc.addLocal(camera.getLeft());
             else
            	 {if(strafeLR==-1)
                      loc.subtractLocal(camera.getLeft());
            	 }
             loc.normalizeLocal().multiplyLocal(moveSpeed*tpf).addLocal(camera.getLocation());
             camera.setLocation(loc);
            }

        // ROTATION
        int rotX = 0, rotY = 0;
        for(KeyInput input:lookUpKeyInputs)
            {final Key key=input.getInputObject();
             if(kb.isDown(key))
            	 rotY-=1;
            }
        for(KeyInput input:lookDownKeyInputs)
            {final Key key=input.getInputObject();
             if(kb.isDown(key))
            	 rotY+=1;
            }
        for(KeyInput input:turnLeftKeyInputs)
            {final Key key=input.getInputObject();
        	 if(kb.isDown(key))
        		 rotX+=1;
            }
        for(KeyInput input:turnRightKeyInputs)
            {final Key key=input.getInputObject();
    	     if(kb.isDown(key))
    		     rotX-=1;
            }
        if((rotX != 0 || rotY != 0) && mouseRotateSpeed != 0 && keyRotateSpeed != 0)
            rotate(camera,rotX * (keyRotateSpeed / mouseRotateSpeed) * tpf,rotY * (keyRotateSpeed / mouseRotateSpeed) * tpf);
    }

    /**
     * handle the rotation with the mouse
     * @param camera
     * @param dx
     * @param dy
     */
    protected void rotate(final Camera camera,final double dx,final double dy){
        if (dx != 0) {
            workerMatrix.fromAngleNormalAxis(mouseRotateSpeed * dx, upAxis != null ? upAxis : camera.getUp());
            workerMatrix.applyPost(camera.getLeft(), workerStoreA);
            camera.setLeft(workerStoreA);
            workerMatrix.applyPost(camera.getDirection(), workerStoreA);
            camera.setDirection(workerStoreA);
            workerMatrix.applyPost(camera.getUp(), workerStoreA);
            camera.setUp(workerStoreA);
        }
        if (dy != 0) {
            workerMatrix.fromAngleNormalAxis(mouseRotateSpeed * dy, camera.getLeft());
            workerMatrix.applyPost(camera.getLeft(), workerStoreA);
            camera.setLeft(workerStoreA);
            workerMatrix.applyPost(camera.getDirection(), workerStoreA);
            camera.setDirection(workerStoreA);
            workerMatrix.applyPost(camera.getUp(), workerStoreA);
            camera.setUp(workerStoreA);
        }
        camera.normalize();
    }

    /**
     * @param layer
     * @param upAxis
     * @param dragOnly
     * @return a new FirstPersonControl object
     */
    public static ExtendedFirstPersonControl setupTriggers(final LogicalLayer layer,final ReadOnlyVector3 upAxis,
            final boolean dragOnly,final ActionMap customActionMap){
        final ExtendedFirstPersonControl control=new ExtendedFirstPersonControl(upAxis,customActionMap);
        control.setupMouseTriggers(layer,dragOnly,control.setupKeyboardTriggers(layer));
        return control;
    }

    public void setupMouseTriggers(final LogicalLayer layer,final boolean dragOnly,final Predicate<TwoInputStates> keysHeld){
        // Mouse look
        final Predicate<TwoInputStates> someMouseDown = Predicates.or(TriggerConditions.leftButtonDown(), Predicates
                .or(TriggerConditions.rightButtonDown(), TriggerConditions.middleButtonDown()));
        final Predicate<TwoInputStates> dragged = Predicates.and(TriggerConditions.mouseMoved(), someMouseDown);
        final TriggerAction dragAction=new TriggerAction(){
            @Override
            public void perform(final Canvas source,final TwoInputStates inputStates,final double tpf){
            	onDragAction(source,inputStates,tpf);
            }
        };
        layer.registerTrigger(new InputTrigger(dragOnly ? dragged : TriggerConditions.mouseMoved(), dragAction));
    }
    
    protected void onDragAction(final Canvas source,final TwoInputStates inputStates,final double tpf){
    	final MouseState mouse = inputStates.getCurrent().getMouseState();
        if (mouse.getDx() != 0 || mouse.getDy() != 0) {
        	rotate(source.getCanvasRenderer().getCamera(), -mouse.getDx(),lookUpDownReversed?mouse.getDy():-mouse.getDy());
        }
    }
    
    public boolean isLookUpDownReversed(){
    	return(lookUpDownReversed);
    }
    
    public void setLookUpDownReversed(final boolean lookUpDownReversed){
    	this.lookUpDownReversed=lookUpDownReversed;
    }
    
    private static final class FirstPersonControlKeyboardTriggersPredicate implements Predicate<TwoInputStates>{
    	
    	private final Set<KeyInput> inputs;
    	
    	private FirstPersonControlKeyboardTriggersPredicate(final ActionMap customActionMap){
    		inputs=customActionMap.getInputs(Action.MOVE_FORWARD,Action.MOVE_BACKWARD,Action.LOOK_UP,Action.LOOK_DOWN,Action.STRAFE_LEFT,Action.STRAFE_RIGHT,Action.TURN_LEFT,Action.TURN_RIGHT);
    	}
    	
    	@Override
    	public boolean apply(final TwoInputStates states){
    		boolean result=false;
    		if(states.getCurrent()!=null)
    		    {for(KeyInput input:inputs)
    		         {final Key key=input.getInputObject();
    		          if(states.getCurrent().getKeyboardState().isDown(key))
    		        	  {result=true;
    		        	   break;
    		        	  }
    		         }
    		     //TODO handle controllers
    		    }
    		return(result);
    	}
    }

    public Predicate<TwoInputStates> setupKeyboardTriggers(final LogicalLayer layer){
        final Predicate<TwoInputStates> keysHeld = new FirstPersonControlKeyboardTriggersPredicate(customActionMap);
        final TriggerAction moveAction=new TriggerAction(){
        	@Override
            public void perform(final Canvas source,final TwoInputStates inputStates,final double tpf){
            	onMoveAction(source,inputStates,tpf);
            }
        };
        layer.registerTrigger(new InputTrigger(keysHeld,moveAction));
        return keysHeld;
    }

    protected void onMoveAction(final Canvas source,final TwoInputStates inputStates,final double tpf){
    	move(source.getCanvasRenderer().getCamera(),inputStates.getCurrent().getKeyboardState(),tpf);
    }
}
