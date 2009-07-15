package jme;

import com.jme.input.InputHandler;
import com.jme.input.KeyBindingManager;
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
        // MHenze Fix "cannot stop turning" (Contract JGouesse200903):
        // I copied the implementation from the JME KeyboardLookHandler and just modified the keybinding.
        KeyBindingManager keyboard = KeyBindingManager.getKeyBindingManager();

        keyboard.set( "forward", KeyInput.KEY_W );
        keyboard.add( "forward", KeyInput.KEY_Z );
        keyboard.set( "backward", KeyInput.KEY_S );
        keyboard.set( "strafeLeft", KeyInput.KEY_A );
        keyboard.add( "strafeLeft", KeyInput.KEY_Q );
        keyboard.set( "strafeRight", KeyInput.KEY_D );
        keyboard.set( "lookUp", KeyInput.KEY_UP );
        keyboard.set( "lookDown", KeyInput.KEY_DOWN );
        keyboard.set( "turnRight", KeyInput.KEY_RIGHT );
        keyboard.set( "turnLeft", KeyInput.KEY_LEFT );
        // up and down does not go along well with the azerty mapping, so disable them for now
        //keyboard.set( "elevateUp", KeyInput.KEY_Q);
        //keyboard.set( "elevateDown", KeyInput.KEY_Z);
        // add the Exit binding
        keyboard.set( "exit", KeyInput.KEY_ESCAPE );

        forward = new KeyForwardAction( cam, moveSpeed );
        addAction( forward, "forward", true );
        backward = new KeyBackwardAction( cam, moveSpeed );
        addAction( backward, "backward", true );
        sLeft = new KeyStrafeLeftAction( cam, moveSpeed );
        addAction( sLeft, "strafeLeft", true );
        sRight = new KeyStrafeRightAction( cam, moveSpeed );
        addAction( sRight, "strafeRight", true );
        addAction( new KeyLookUpAction( cam, rotateSpeed ), "lookUp", true );
        addAction( new KeyLookDownAction( cam, rotateSpeed ), "lookDown", true );
        down = new KeyStrafeDownAction(cam, moveSpeed);
        Vector3f upVec = new Vector3f(cam.getUp());
        down.setUpVector( upVec );
        //addAction(down, "elevateDown", true);
        up = new KeyStrafeUpAction( cam, moveSpeed );
        up.setUpVector(upVec);
        //addAction( up, "elevateUp", true);
        right = new KeyRotateRightAction( cam, rotateSpeed );
        right.setLockAxis(new Vector3f(cam.getUp()));
        addAction( right, "turnRight", true );
        left = new KeyRotateLeftAction( cam, rotateSpeed );
        left.setLockAxis(new Vector3f(cam.getUp()));
        addAction( left, "turnLeft", true );
        //TODO: rather go to the pause menu
        addAction( new ExitAction(gameServiceProvider), "exit", false );
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