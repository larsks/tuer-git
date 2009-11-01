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
package misc;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.beans.XMLDecoder;
import java.beans.XMLEncoder;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;


/**
 * helper class to use the XML encoding
 * @author Julien Gouesse
 *
 */
public final class SerializationHelper{

	/**
	 * force the XML encoder/decoder to ignore transient members like the binary serialization
	 * @param myClass
	 */
    public static final void forceHandlingOfTransientModifiersForXMLSerialization(Class<?> myClass){
        BeanInfo beanInfo = null;
        try{beanInfo=Introspector.getBeanInfo(myClass);} 
        catch(IntrospectionException ie)
        {ie.printStackTrace();}
        if(beanInfo!=null)
            {PropertyDescriptor[] propertyDescriptors=beanInfo.getPropertyDescriptors();
             String fieldName;
             //for each non inherited declared field
             for(Field field:myClass.getDeclaredFields())
                 //if this field is transient for binary serialization
                 if(Modifier.isTransient(field.getModifiers()))
                     {fieldName=field.getName();
                      for(PropertyDescriptor propertyDesc:propertyDescriptors)
                          if(propertyDesc.getName().equals(fieldName))
                              {//set this field to transient for the XML serialization too
                               propertyDesc.setValue("transient",Boolean.TRUE);
                               break;
                              }
                     }
            }
    }
    
    public static final Object decodeObjectInXMLFile(String path){
        BufferedInputStream bis=new BufferedInputStream(SerializationHelper.class.getResourceAsStream(path));
        XMLDecoder decoder=new XMLDecoder(bis);
        Object resultingObject=decoder.readObject();
        decoder.close();
        try{bis.close();}
        catch(IOException ioe)
        {throw new RuntimeException("Unable to close the file "+path,ioe);}
        return(resultingObject);
    }

    public static final void encodeObjectInFile(Object o,String filename){
        BufferedOutputStream bos=null;
        File file=new File(filename);   
        try{if(!file.exists())
                if(!file.createNewFile())
                    throw new IOException("Unable to create the file "+filename);
            bos=new BufferedOutputStream(new FileOutputStream(file));
            XMLEncoder encoder=new XMLEncoder(bos);
            encoder.writeObject(o);
            encoder.close();
           }
        catch(IOException ioe)
        {throw new RuntimeException("Unable to encode the file "+filename,ioe);}
        finally
        {if(bos!=null)
             try{bos.close();}
             catch(IOException ioe)
             {throw new RuntimeException("Unable to close the file "+filename,ioe);}           
        }
    }
}
