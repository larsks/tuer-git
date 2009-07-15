package jme;

import com.jme.input.InputHandler;
import com.jme.input.MouseLookHandler;
import com.jme.renderer.Camera;

/**
 * first person handler that takes into account the case of AZERTY 
 * keyboards unlike FirstPersonHandler that takes into account only
 * the QWERTY keyboards
 * 
 * @author Julien Gouesse
 *
 */
public final class ExtendedFirstPersonHandler extends InputHandler {
    private MouseLookHandler mouseLookHandler;
    private ExtendedKeyboardLookHandler keyboardLookHandler;

    /**
     * @return handler for keyboard controls
     */
    public ExtendedKeyboardLookHandler getKeyboardLookHandler() {
        return keyboardLookHandler;
    }

    /**
     * @return handler for mouse controls
     */
    public MouseLookHandler getMouseLookHandler() {
        return mouseLookHandler;
    }
    
    public void setButtonPressRequired(boolean value) {
        mouseLookHandler.requireButtonPress(value);
    }

    /**
     * Creates a first person handler.
     * @param cam The camera to move by this handler.
     */
    public ExtendedFirstPersonHandler(Camera cam,
            JMEGameServiceProvider gameServiceProvider){
        mouseLookHandler = new MouseLookHandler( cam, 1 );
        addToAttachedHandlers( mouseLookHandler );
        keyboardLookHandler = new ExtendedKeyboardLookHandler(cam,0.5f,0.01f,gameServiceProvider);
        addToAttachedHandlers( keyboardLookHandler );
    }

    /**
     * Creates a first person handler.
     * @param cam The camera to move by this handler.
     * @param moveSpeed action speed for move actions
     * @param turnSpeed action speed for rotating actions
     */
    public ExtendedFirstPersonHandler(Camera cam,float moveSpeed,float turnSpeed,
            JMEGameServiceProvider gameServiceProvider){
        mouseLookHandler = new MouseLookHandler( cam, turnSpeed );
        addToAttachedHandlers( mouseLookHandler );
        keyboardLookHandler = new ExtendedKeyboardLookHandler(cam,moveSpeed,turnSpeed,gameServiceProvider);
        addToAttachedHandlers( keyboardLookHandler );
    }
}