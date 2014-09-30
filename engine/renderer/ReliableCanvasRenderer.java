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

import com.ardor3d.framework.Scene;
import com.ardor3d.framework.jogl.CapsUtil;
import com.ardor3d.framework.jogl.JoglCanvasRenderer;
import com.ardor3d.renderer.jogl.JoglContextCapabilities;
import com.ardor3d.renderer.jogl.JoglRenderer;

/**
 * Canvas renderer enhanced for higher frame rates especially 
 * when the vertical synchronization is turned off by avoiding explicit 
 * OpenGL context operations.
 * 
 * @author Julien Gouesse
 *
 */
public class ReliableCanvasRenderer extends JoglCanvasRenderer{

	public ReliableCanvasRenderer(Scene scene){
		super(scene);
	}

	public ReliableCanvasRenderer(Scene scene,boolean useDebug){
		super(scene,useDebug,new CapsUtil(),false);
	}
	
	@Override
	public final JoglRenderer createRenderer(){
		return new ReliableRenderer();
	}

	@Override
	public final JoglContextCapabilities createContextCapabilities(){
		final JoglContextCapabilities defaultCaps = super.createContextCapabilities();
        final ReliableContextCapabilities realCaps = new ReliableContextCapabilities(defaultCaps);
        /**
    	 * TODO Remove this code as JogAmp's Ardor3D Continuation already supports OpenGL 1.1. This game uses no fancy 
    	 * feature, it should work as is. Test it before definitely removing this piece of code
    	 */
        //checks if the operating system is Windows
        /*if(System.getProperty("os.name").toLowerCase().startsWith("windows"))
            {//gets some information about the OpenGL driver
        	 final String vendor=realCaps.getDisplayVendor();
        	 final String renderer=realCaps.getDisplayRenderer();
             //checks whether Microsoft’s generic software emulation driver (OpenGL emulation through Direct3D) is installed
        	 if(vendor!=null&&renderer!=null&&vendor.equalsIgnoreCase("Microsoft Corporation")&&
        		realCaps.getDisplayRenderer().equalsIgnoreCase("GDI Generic"))
                 {if(Platform.AWT_AVAILABLE)
                      {//uses Java Reflection to remove this dependency at runtime
                	   try{final Class<?> jOptionPaneClass=Class.forName("javax.swing.JOptionPane");
                	       final Class<?> componentClass=Class.forName("java.awt.Component");
                	       final Method showMessageDialogMethod=jOptionPaneClass.getMethod("showMessageDialog",componentClass,Object.class,String.class,int.class);
                	       //prevents the use of this crap, recommends to the end user to install a proper OpenGL driver
                	       showMessageDialogMethod.invoke(null,null,
               				    "The game cannot run with your broken OpenGL driver. To resolve this problem, please download and install the latest version of your graphical card's driver from the your graphical card manufacturer (Nvidia, ATI, Intel).",
            				    "OpenGL driver error",0);
					      }
                	   catch(ClassNotFoundException cnfe)
                	   {cnfe.printStackTrace();} 
                	   catch(SecurityException se)
                	   {se.printStackTrace();}
                	   catch(NoSuchMethodException nsme)
                	   {nsme.printStackTrace();}
                	   catch(IllegalArgumentException iae)
                	   {iae.printStackTrace();}
                	   catch(IllegalAccessException e)
                	   {e.printStackTrace();}
                	   catch(InvocationTargetException ite)
                	   {ite.printStackTrace();}
                      }
        		  System.exit(0);
                 }
            }*/
        return(realCaps);
	}
}
