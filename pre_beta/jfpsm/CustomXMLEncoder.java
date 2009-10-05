package jfpsm;

import java.beans.XMLEncoder;
import java.io.OutputStream;

/**
 * XML encoder that sets the dirty flag at false
 * to ensure the object has no pending change
 * @author Julien Gouesse
 *
 */
public final class CustomXMLEncoder extends XMLEncoder{

    public CustomXMLEncoder(OutputStream out){
        super(out);
    }

    @Override
    public final void writeObject(Object o){
        super.writeObject(o);
        if(o!=null&&o instanceof Dirtyable)
            ((Dirtyable)o).unmarkDirty();
    }
}
