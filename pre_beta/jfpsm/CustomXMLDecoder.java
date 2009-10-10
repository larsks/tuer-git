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
