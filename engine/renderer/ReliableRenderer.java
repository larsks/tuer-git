/**
 * Copyright (c) 2006-2014 Julien Gouesse
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package engine.renderer;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.ardor3d.image.Image;
import com.ardor3d.image.Texture;
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
	
	private static final Class<?> directByteBufferClass;
	
	private static final boolean directByteBufferCleanerCleanCallable;
	
	private static final Method directByteBufferCleanerMethod;
	
	private static final Method cleanerCleanMethod;
	
	private static final Method viewedBufferMethod;
	
	static{
		boolean tmpDirectByteBufferCleanerCleanCallable;
		Method tmpDirectByteBufferCleanerMethod;
		Method tmpCleanerCleanMethod;
		Method tmpViewedBufferMethod=null;
		Class<?> tmpDirectByteBufferClass=null;
		try{tmpDirectByteBufferClass=Class.forName("java.nio.DirectByteBuffer");
		    tmpDirectByteBufferCleanerMethod=tmpDirectByteBufferClass.getDeclaredMethod("cleaner");
		    tmpDirectByteBufferCleanerMethod.setAccessible(true);
		    final Class<?> cleanerClass=Class.forName("sun.misc.Cleaner");
		    tmpCleanerCleanMethod=cleanerClass.getDeclaredMethod("clean");
		    tmpCleanerCleanMethod.setAccessible(true);
		    final Class<?> directBufferInterface=Class.forName("sun.nio.ch.DirectBuffer");
		    try{//Java 1.6
		    	tmpViewedBufferMethod=directBufferInterface.getDeclaredMethod("viewedBuffer");
		       }
		    catch(NoSuchMethodException nsme)
		    {try{//Java 1.7 and later
		         tmpViewedBufferMethod=directBufferInterface.getDeclaredMethod("attachment");
		        }
		     catch(NoSuchMethodException nsme2)
		     {//it should never happen (but I haven't tested with AvianVM)
		     }
		    }
		    if(tmpViewedBufferMethod!=null)
		    	tmpViewedBufferMethod.setAccessible(true);
		    tmpDirectByteBufferCleanerCleanCallable=true;
		   } 
		catch(Throwable t){
			tmpDirectByteBufferCleanerCleanCallable=false;
			tmpDirectByteBufferClass=null;
			tmpDirectByteBufferCleanerMethod=null;
			tmpCleanerCleanMethod=null;
			tmpViewedBufferMethod=null;
			logger.logp(Level.WARNING,ReliableRenderer.class.getName(),"<static initializer>","Impossible to retrieve the required methods to release the native resources of direct NIO buffers",t);
		}
		directByteBufferCleanerCleanCallable=tmpDirectByteBufferCleanerCleanCallable;
		directByteBufferClass=tmpDirectByteBufferClass;
		directByteBufferCleanerMethod=tmpDirectByteBufferCleanerMethod;
		cleanerCleanMethod=tmpCleanerCleanMethod;
		viewedBufferMethod=tmpViewedBufferMethod;
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
		deleteBuffer(realNioBuffer);
	}
	
	@Override
	public void deleteTexture(final Texture texture){
		super.deleteTexture(texture);
		final Image image=texture.getImage();
		if(image!=null&&image.getDataSize()>=1)
		    {for(Buffer data:image.getData())
		    	 deleteBuffer(data);
		    }
	}
	
	public void deleteBuffer(final Buffer realNioBuffer){
		if(realNioBuffer!=null&&realNioBuffer.isDirect())
	        {//this buffer is direct. Then, it uses some native memory. Therefore, it's up to the programmer to release it.
		     if(directByteBufferCleanerCleanCallable)
	             {//the mechanism is working, tries to use it
			      Object directByteBuffer=getCleanableDirectByteBufferFromDirectByteBuffer(realNioBuffer);
	              if(directByteBuffer==null)
	                  {//this buffer is a view on a direct byte buffer, gets this viewed buffer
	            	   //first attempt (inspired of com.jme3.util.BufferUtils.destroyByteBuffer(Buffer) from JMonkeyEngine 3: http://www.jmonkeyengine.org)
	            	   if(viewedBufferMethod!=null)
	            		   {try{directByteBuffer=viewedBufferMethod.invoke(realNioBuffer);}
	            		    catch(Throwable t)
	            		    {logger.logp(Level.WARNING,ReliableRenderer.class.getName(),"deleteBuffer","Failed to get the viewed buffer",t);}
	            		   }
	            	   //last attempt, less straightforward, looks at each field
	            	   if(directByteBuffer==null)
	            	       {for(Field field:realNioBuffer.getClass().getDeclaredFields())
	                            {final boolean wasAccessible=field.isAccessible();
	        		             if(!wasAccessible)
	                	             field.setAccessible(true);
						         try{final Object fieldValue=field.get(realNioBuffer);
							         if(fieldValue!=null&&fieldValue instanceof Buffer)
	            	    	             {directByteBuffer=getCleanableDirectByteBufferFromDirectByteBuffer((Buffer)fieldValue);
	            	    	              if(directByteBuffer!=null)
	            	    	                  break;
	            	    	             }
						            }
						         catch(Throwable t)
						         {logger.logp(Level.WARNING,ReliableRenderer.class.getName(),"deleteBuffer","Failed to get the value of a byte buffer's field",t);}
	                             finally
	                             {if(!wasAccessible)
	                	              field.setAccessible(false);
	                             }
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
			           {logger.logp(Level.WARNING,ReliableRenderer.class.getName(),"deleteBuffer","Failed to use the cleaner of a byte buffer",t);}
	                  }
	             }
	        }
	}
	
	private ByteBuffer getCleanableDirectByteBufferFromDirectByteBuffer(final Buffer buffer){
		ByteBuffer cleanableByteBuffer=null;
		if(buffer!=null&&buffer.isDirect()&&buffer instanceof ByteBuffer&&directByteBufferClass.isAssignableFrom(buffer.getClass()))
		    {//this buffer is a direct byte buffer
 	         if(buffer.isReadOnly())
                 {//this buffer is in read only mode, the real direct buffer that contains the data is the viewed buffer
    	          if(viewedBufferMethod!=null)
		              {try{cleanableByteBuffer=(ByteBuffer)viewedBufferMethod.invoke(buffer);}
		               catch(Throwable t)
		               {logger.logp(Level.WARNING,ReliableRenderer.class.getName(),"deleteBuffer","Failed to get the viewed buffer",t);}
		              }
                 }
             else
    	         cleanableByteBuffer=(ByteBuffer)buffer;
 	        }
	    return(cleanableByteBuffer);
	}
}
