package jfpsm;

import java.beans.XMLDecoder;
import java.io.InputStream;

/**
 * XML decoder that sets the dirty flag at false
 * to ensure the object has no pending change
 * @author Julien Gouesse
 *
 */
public final class CustomXMLDecoder extends XMLDecoder{

    public CustomXMLDecoder(InputStream in){
        super(in);
    }

    @Override
    public final Object readObject(){
        Object o=super.readObject();
        if(o!=null)
        	{if(o instanceof Resolvable)
       		     ((Resolvable)o).resolve();
        	 if(o instanceof Dirtyable)
                 ((Dirtyable)o).unmarkDirty();       	 
        	}
        return(o);
    }
}
