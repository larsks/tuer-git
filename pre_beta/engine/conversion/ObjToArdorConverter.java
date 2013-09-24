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
package engine.conversion;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.Buffer;
import java.nio.FloatBuffer;

import com.ardor3d.extension.model.obj.ObjImporter;
import com.ardor3d.image.util.jogl.JoglImageLoader;
import com.ardor3d.math.type.ReadOnlyColorRGBA;
import com.ardor3d.renderer.state.MaterialState;
import com.ardor3d.renderer.state.RenderState.StateType;
import com.ardor3d.scenegraph.FloatBufferData;
import com.ardor3d.scenegraph.Mesh;
import com.ardor3d.scenegraph.MeshData;
import com.ardor3d.scenegraph.Node;
import com.ardor3d.scenegraph.Spatial;
import com.ardor3d.util.export.binary.BinaryClassObject;
import com.ardor3d.util.export.binary.BinaryExporter;
import com.ardor3d.util.export.binary.BinaryIdContentPair;
import com.ardor3d.util.export.binary.BinaryOutputCapsule;
import com.ardor3d.util.geom.BufferUtils;
import com.ardor3d.util.resource.ResourceLocatorTool;
import com.ardor3d.util.resource.SimpleResourceLocator;
import com.ardor3d.util.resource.URLResourceSource;

/**
 * Converter of WaveFront OBJ files into Ardor3D binary files. As the WaveFront OBJ format doesn't support vertex color, it is optionally 
 * possible to use the ambient colors of the materials as the vertices colors
 * 
 * @author Julien Gouesse
 *
 */
public class ObjToArdorConverter{
	
	//FIXME code duplication, already used in JFPSM
	private static final class DirectBinaryExporter extends BinaryExporter{
        @Override
		protected BinaryIdContentPair generateIdContentPair(final BinaryClassObject bco) {
            final BinaryIdContentPair pair = new BinaryIdContentPair(_idCount++, new BinaryOutputCapsule(this, bco, true));
            return pair;
        }
    }

	private boolean conversionOfAmbientColorsIntoVerticesColorsEnabled;
	
	public ObjToArdorConverter(){
		super();
	}
	
	public boolean getConversionOfAmbientColorsIntoVerticesColorsEnabled(){
		return(conversionOfAmbientColorsIntoVerticesColorsEnabled);
	}
	
	public void setConversionOfAmbientColorsIntoVerticesColorsEnabled(final boolean conversionOfAmbientColorsIntoVerticesColorsEnabled){
		this.conversionOfAmbientColorsIntoVerticesColorsEnabled=conversionOfAmbientColorsIntoVerticesColorsEnabled;
	}
	
	public void run(final String[] args) throws IOException,URISyntaxException{
		JoglImageLoader.registerLoader();
        BinaryExporter binaryExporter=conversionOfAmbientColorsIntoVerticesColorsEnabled?new DirectBinaryExporter():new BinaryExporter();
        try{SimpleResourceLocator srl=new SimpleResourceLocator(ObjToArdorConverter.class.getResource("/images"));
            ResourceLocatorTool.addResourceLocator(ResourceLocatorTool.TYPE_TEXTURE,srl);
            srl=new SimpleResourceLocator(ObjToArdorConverter.class.getResource("/obj"));
            ResourceLocatorTool.addResourceLocator(ResourceLocatorTool.TYPE_MODEL,srl);
           } 
        catch(final URISyntaxException urise)
        {urise.printStackTrace();}
        final ObjImporter objImporter=new ObjImporter();
        try{objImporter.setTextureLocator(new SimpleResourceLocator(ObjToArdorConverter.class.getResource("/images")));
           } 
        catch(final URISyntaxException ex)
        {ex.printStackTrace();}
        Spatial objSpatial;
        for(String arg:args)
            {System.out.println("Loading "+arg+" ...");
             objSpatial=objImporter.load(arg).getScene();
             if(conversionOfAmbientColorsIntoVerticesColorsEnabled)
            	 convertAmbientColorsIntoVerticesColors(objSpatial);
             URLResourceSource source=(URLResourceSource)ResourceLocatorTool.locateResource(ResourceLocatorTool.TYPE_MODEL,arg);
             File sourceFile=new File(source.getURL().toURI());
             File destFile=new File(sourceFile.getAbsolutePath().substring(0,sourceFile.getAbsolutePath().lastIndexOf(".obj"))+".abin");
             if(!destFile.exists())
                 if(!destFile.createNewFile())
                     {System.out.println(destFile.getAbsolutePath()+" cannot be created!");
                      continue;
                     }
             System.out.println("Converting "+arg+" ...");
             binaryExporter.save(objSpatial,destFile);
             System.out.println(arg+" successfully converted");
            }
	}
	
	private void convertAmbientColorsIntoVerticesColors(final Spatial spatial){
		if(spatial instanceof Node)
		    {final Node node=(Node)spatial;
			 for(Spatial child:node.getChildren())
		    	 convertAmbientColorsIntoVerticesColors(child);
		    }
		else
			if(spatial instanceof Mesh)
			    {final Mesh mesh=(Mesh)spatial;
			     final MaterialState materialState=(MaterialState)mesh.getLocalRenderState(StateType.Material);
			     //checks whether there is a material state
			     if(materialState!=null)
			         {final ReadOnlyColorRGBA ambientRgbaColor=materialState.getAmbient();
			          //checks whether there is a material ambient color
			    	  if(ambientRgbaColor!=null)
			    	      {final MeshData meshData=mesh.getMeshData();
			    		   //checks whether there is no color buffer
				           if(meshData!=null&&meshData.getColorBuffer()==null)
				               {final Buffer vertexBuffer=meshData.getVertexBuffer();
				                //checks whether there is a vertex buffer
					            if(vertexBuffer!=null)
					                {final int vertexCount=meshData.getVertexCount();
					                 //creates the new color buffer data of the mesh
					                 final FloatBuffer colorBuffer=BufferUtils.createFloatBufferOnHeap(vertexCount*3);
					            	 final FloatBufferData colorBufferData=new FloatBufferData(colorBuffer,3);
					            	 //fills it with the ambient color
					            	 while(colorBuffer.hasRemaining())
					            	     {colorBuffer.put(ambientRgbaColor.getRed());
					            	      colorBuffer.put(ambientRgbaColor.getGreen());
					            	      colorBuffer.put(ambientRgbaColor.getBlue());
					            	     }
					            	 colorBuffer.rewind();
					            	 //sets the new color buffer data of the mesh
					            	 meshData.setColorCoords(colorBufferData);
					                }
				               }
			    	      }
			         }
			    }
	}
	
	public static final void main(final String[] args){
		try{final ObjToArdorConverter converter=new ObjToArdorConverter();
		    //converter.setConversionOfAmbientColorsIntoVerticesColorsEnabled(true);
		    converter.run(args);
		   }
		catch(Throwable t)
		{t.printStackTrace();}
    }
}
