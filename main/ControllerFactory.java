package main;

import java.nio.FloatBuffer;
import java.util.List;

abstract class ControllerFactory {
    

    abstract List<FloatBuffer> getCoordinatesBuffersList();
}
