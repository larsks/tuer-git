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
package jme;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

public final class TransientMarkerForXMLSerialization{

    
    public static final void updateTransientModifierForXMLSerialization(Class<?> myClass){
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
}
