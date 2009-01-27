package jme;

import com.jme.input.InputHandler;
import com.jme.input.KeyInput;
import com.jme.input.action.InputAction;
import com.jme.input.action.InputActionEvent;
import com.jme.input.action.KeyBackwardAction;
import com.jme.input.action.KeyForwardAction;
import com.jme.input.action.KeyLookDownAction;
import com.jme.input.action.KeyLookUpAction;
import com.jme.input.action.KeyRotateLeftAction;
import com.jme.input.action.KeyRotateRightAction;
import com.jme.input.action.KeyStrafeDownAction;
import com.jme.input.action.KeyStrafeLeftAction;
import com.jme.input.action.KeyStrafeRightAction;
import com.jme.input.action.KeyStrafeUpAction;
import com.jme.math.Vector3f;
import com.jme.renderer.Camera;

/**
 * Supports AZERTY and QWERTY keyboards
 * @author Julien Gouesse
 *
 */
public final class ExtendedKeyboardLookHandler extends InputHandler{
    private KeyForwardAction forward;
    private KeyBackwardAction backward;
    private KeyStrafeLeftAction sLeft;
    private KeyStrafeRightAction sRight;
    private KeyRotateRightAction right;
    private KeyRotateLeftAction left;
    private KeyStrafeDownAction down;
    private KeyStrafeUpAction up;

    private float moveSpeed;
    
    public ExtendedKeyboardLookHandler(Camera cam,float moveSpeed,float rotateSpeed,
            JMEGameServiceProvider gameServiceProvider){
        this.moveSpeed=moveSpeed;     
        forward=new KeyForwardAction(cam,moveSpeed);
        addAction(forward,InputHandler.DEVICE_KEYBOARD,KeyInput.KEY_W,InputHandler.AXIS_NONE,true);
        addAction(forward,InputHandler.DEVICE_KEYBOARD,KeyInput.KEY_Z,InputHandler.AXIS_NONE,true);
        backward=new KeyBackwardAction(cam,moveSpeed);
        addAction(backward,InputHandler.DEVICE_KEYBOARD,KeyInput.KEY_S,InputHandler.AXIS_NONE,true);
        sLeft = new KeyStrafeLeftAction(cam,moveSpeed);
        addAction(sLeft,InputHandler.DEVICE_KEYBOARD,KeyInput.KEY_Q,InputHandler.AXIS_NONE,true);
        addAction(sLeft,InputHandler.DEVICE_KEYBOARD,KeyInput.KEY_A,InputHandler.AXIS_NONE,true);
        sRight = new KeyStrafeRightAction(cam,moveSpeed);
        addAction(sRight,InputHandler.DEVICE_KEYBOARD,KeyInput.KEY_D,InputHandler.AXIS_NONE,true);
        addAction(new KeyLookUpAction(cam,rotateSpeed),InputHandler.DEVICE_KEYBOARD,KeyInput.KEY_UP,InputHandler.AXIS_NONE,true);
        addAction(new KeyLookDownAction(cam,rotateSpeed),InputHandler.DEVICE_KEYBOARD,KeyInput.KEY_DOWN,InputHandler.AXIS_NONE,true);
        down=new KeyStrafeDownAction(cam,moveSpeed);
        Vector3f upVec=new Vector3f(cam.getUp());
        down.setUpVector(upVec);
        //addAction(down,InputHandler.DEVICE_KEYBOARD,KeyInput.KEY_C,InputHandler.AXIS_NONE,true);
        up=new KeyStrafeUpAction(cam,moveSpeed);
        up.setUpVector(upVec);
        //addAction(up,InputHandler.DEVICE_KEYBOARD,KeyInput.KEY_J,InputHandler.AXIS_NONE,true);
        right = new KeyRotateRightAction(cam,rotateSpeed);
        right.setLockAxis(new Vector3f(cam.getUp()));
        addAction(right,InputHandler.DEVICE_KEYBOARD,KeyInput.KEY_RIGHT,InputHandler.AXIS_NONE,true);
        left = new KeyRotateLeftAction(cam,rotateSpeed);
        left.setLockAxis(new Vector3f(cam.getUp()));
        addAction(left,InputHandler.DEVICE_KEYBOARD,KeyInput.KEY_LEFT,InputHandler.AXIS_NONE,true);
        //TODO: rather go to the pause menu
        addAction(new ExitAction(gameServiceProvider),InputHandler.DEVICE_KEYBOARD,KeyInput.KEY_ESCAPE,InputHandler.AXIS_NONE,false);
    }
    
    private static final class ExitAction extends InputAction{
        
        private JMEGameServiceProvider gameServiceProvider;

        private ExitAction(JMEGameServiceProvider gameServiceProvider){
            this.gameServiceProvider=gameServiceProvider;
        }

        @Override
        public final void performAction(InputActionEvent evt){
            gameServiceProvider.exit();
        }
    }
    
    public void setLockAxis(Vector3f lock) {
        right.setLockAxis(new Vector3f(lock));
        left.setLockAxis(new Vector3f(lock));
    }
    
    public void setUpAxis(Vector3f upAxis) {
        up.setUpVector(upAxis);
        down.setUpVector(upAxis);
    }

    public float getMoveSpeed() {
        return moveSpeed;
    }

    public void setMoveSpeed(float moveSpeed) {
        if(moveSpeed < 0) {
            moveSpeed = 0;
        }
        this.moveSpeed = moveSpeed;
        
        forward.setSpeed(moveSpeed);
        backward.setSpeed(moveSpeed);
        sLeft.setSpeed(moveSpeed);
        sRight.setSpeed(moveSpeed);
        down.setSpeed(moveSpeed);
        up.setSpeed(moveSpeed);
    }
}