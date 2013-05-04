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
package engine.renderer;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.util.logging.Level;
import java.util.logging.Logger;
import com.ardor3d.renderer.jogl.JoglRenderer;
import com.ardor3d.scenegraph.AbstractBufferData;

/**
 * Reliable JOGL renderer able to cleanly release all native resources. However, it requires manual interventions.
 * 
 * @author Julien Gouesse
 *
 */
public class ReliableRenderer extends JoglRenderer{
	
	private static final Logger logger = Logger.getLogger(ReliableRenderer.class.getName());
	
	private static final boolean directByteBufferCleanerCleanCallable;
	
	private static final Method directByteBufferCleanerMethod;
	
	private static final Method cleanerCleanMethod;
	
	static{
		boolean tmpDirectByteBufferCleanerCleanCallable;
		Method tmpDirectByteBufferCleanerMethod;
		Method tmpCleanerCleanMethod;
		try{final Class<?> directByteBufferClass=Class.forName("java.nio.DirectByteBuffer");
		    tmpDirectByteBufferCleanerMethod=directByteBufferClass.getDeclaredMethod("cleaner");
		    tmpDirectByteBufferCleanerMethod.setAccessible(true);
		    final Class<?> cleanerClass=Class.forName("sun.misc.Cleaner");
		    tmpCleanerCleanMethod=cleanerClass.getDeclaredMethod("clean");
		    tmpCleanerCleanMethod.setAccessible(true);
		    tmpDirectByteBufferCleanerCleanCallable=true;
		   } 
		catch(Throwable t){
			tmpDirectByteBufferCleanerCleanCallable=false;
			tmpDirectByteBufferCleanerMethod=null;
			tmpCleanerCleanMethod=null;
			logger.logp(Level.WARNING,ReliableRenderer.class.getName(),"<static initializer>","Impossible to retrieve the required methods to release the native resources of direct NIO buffers",t);
		}
		directByteBufferCleanerCleanCallable=tmpDirectByteBufferCleanerCleanCallable;
		directByteBufferCleanerMethod=tmpDirectByteBufferCleanerMethod;
		cleanerCleanMethod=tmpCleanerCleanMethod;
		if(directByteBufferCleanerCleanCallable)
			logger.info("the deallocator has been successfully initialized");
	}

	public ReliableRenderer(){
		super();
	}
	
	@Override
	public void deleteVBOs(final AbstractBufferData<?> buffer){
		super.deleteVBOs(buffer);
		final Buffer realNioBuffer=buffer.getBuffer();
		if(realNioBuffer.isDirect())
		    {//This buffer is direct. Then, it uses some native memory. Therefore, it's up to the programmer to release it.
			 if(directByteBufferCleanerCleanCallable)
		         {//The mechanism is working, tries to use it
				  Object directByteBuffer=null;
		          if(realNioBuffer instanceof ByteBuffer)
		        	  {//this buffer is a direct byte buffer
		        	   directByteBuffer=realNioBuffer;
		        	  }
		          else
		              {//this buffer is a view on a direct byte buffer, gets this viewed buffer
		        	   for(Field field:realNioBuffer.getClass().getDeclaredFields())
		                   {final boolean wasAccessible=field.isAccessible();
		        		    if(!wasAccessible)
		                	    field.setAccessible(true);
							try{final Object fieldValue = field.get(realNioBuffer);
								if(fieldValue!=null&&fieldValue instanceof ByteBuffer&&((ByteBuffer)fieldValue).isDirect())
		            	    	    {directByteBuffer=fieldValue;
		            	    	     break;
		            	    	    }
							   }
							catch(Throwable t)
							{logger.logp(Level.WARNING,ReliableRenderer.class.getName(),"deleteVBOs","Failed to get the value of a byte buffer's field",t);}
		                    finally
		                    {if(!wasAccessible)
		                	     field.setAccessible(false);
		                    }
		                   }
		              }
		    	  if(directByteBuffer!=null)
		              {Object cleaner;
				       try{cleaner=directByteBufferCleanerMethod.invoke(directByteBuffer);
					       if(cleaner!=null)
		    		           cleanerCleanMethod.invoke(cleaner);
				          }
				       catch(Throwable t)
				       {logger.logp(Level.WARNING,ReliableRenderer.class.getName(),"deleteVBOs","Failed to use the cleaner of a byte buffer",t);}
		              }
		         }
		    }
	}
}
