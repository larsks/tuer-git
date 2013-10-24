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

import jfpsm.GeometryHelper;

import com.ardor3d.extension.model.obj.ObjGeometryStore;
import com.ardor3d.extension.model.obj.ObjImporter;
import com.ardor3d.image.Texture;
import com.ardor3d.image.util.jogl.JoglImageLoader;
import com.ardor3d.math.Vector2;
import com.ardor3d.math.Vector3;
import com.ardor3d.math.type.ReadOnlyColorRGBA;
import com.ardor3d.renderer.IndexMode;
import com.ardor3d.renderer.state.MaterialState;
import com.ardor3d.renderer.state.RenderState.StateType;
import com.ardor3d.renderer.state.TextureState;
import com.ardor3d.scenegraph.FloatBufferData;
import com.ardor3d.scenegraph.Mesh;
import com.ardor3d.scenegraph.MeshData;
import com.ardor3d.scenegraph.Spatial;
import com.ardor3d.scenegraph.visitor.Visitor;
import com.ardor3d.util.TextureManager;
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

	/**
	 * flag indicating whether to convert ambient colors (of materials, when the lighting is enabled) into vertices colors 
	 * (with no material, when the lighting is disabled)
	 */
	private boolean conversionOfAmbientColorsIntoVerticesColorsEnabled;
	
	/**
	 * flag indicating whether to convert ambient colors into texture coordinates
	 */
	private boolean conversionOfAmbientColorsIntoTexCoordsEnabled;
	
	public ObjToArdorConverter(){
		super();
	}
	
	public boolean isConversionOfAmbientColorsIntoVerticesColorsEnabled(){
		return(conversionOfAmbientColorsIntoVerticesColorsEnabled);
	}
	
	public void setConversionOfAmbientColorsIntoVerticesColorsEnabled(final boolean conversionOfAmbientColorsIntoVerticesColorsEnabled){
		this.conversionOfAmbientColorsIntoVerticesColorsEnabled=conversionOfAmbientColorsIntoVerticesColorsEnabled;
	}
	
	public boolean isConversionOfAmbientColorsIntoTexCoordsEnabled() {
		return conversionOfAmbientColorsIntoTexCoordsEnabled;
	}

	public void setConversionOfAmbientColorsIntoTexCoordsEnabled(
			boolean conversionOfAmbientColorsIntoTexCoordsEnabled) {
		this.conversionOfAmbientColorsIntoTexCoordsEnabled = conversionOfAmbientColorsIntoTexCoordsEnabled;
	}

	public void run(final String[] args) throws IOException,URISyntaxException{
		JoglImageLoader.registerLoader();
        BinaryExporter binaryExporter=conversionOfAmbientColorsIntoVerticesColorsEnabled||conversionOfAmbientColorsIntoTexCoordsEnabled?new DirectBinaryExporter():new BinaryExporter();
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
             final ObjGeometryStore geomStore=objImporter.load(arg);
             objSpatial=geomStore.getScene();
             if(isConversionOfAmbientColorsIntoTexCoordsEnabled()||conversionOfAmbientColorsIntoVerticesColorsEnabled)
            	 deindex(objSpatial);
             if(conversionOfAmbientColorsIntoVerticesColorsEnabled)
            	 convertAmbientColorsIntoVerticesColors(objSpatial);
             if(isConversionOfAmbientColorsIntoTexCoordsEnabled())
            	 convertAmbientColorsIntoTexCoords(objSpatial,geomStore);
             if(isConversionOfAmbientColorsIntoTexCoordsEnabled()||conversionOfAmbientColorsIntoVerticesColorsEnabled)
                 removeMaterialStates(objSpatial);
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
	
	private static final class MaterialStateDeleter implements Visitor{
		
		@Override
    	public void visit(final Spatial spatial){
			if(spatial instanceof Mesh)
			    {final Mesh mesh=(Mesh)spatial;
			     mesh.clearRenderState(StateType.Material);
			    }
		}
	}
	
	private void removeMaterialStates(final Spatial spatial){
		spatial.acceptVisitor(new MaterialStateDeleter(),false);
	}
	
	private static final class Deindexer implements Visitor{
		
		private final GeometryHelper geometryHelper;
		
		private Deindexer(){
			super();
			this.geometryHelper=new GeometryHelper();
		}
		
		@Override
    	public void visit(final Spatial spatial){
			if(spatial instanceof Mesh)
			    {final Mesh mesh=(Mesh)spatial;
			     final MeshData meshData=mesh.getMeshData();
			     if(meshData!=null)
			    	 geometryHelper.convertIndexedGeometryIntoNonIndexedGeometry(meshData);
			    }
		}
	}
	
	private void deindex(final Spatial spatial){
		spatial.acceptVisitor(new Deindexer(),false);
	}
	
    private static final class AmbientColorsIntoTexCoordsConverter implements Visitor{
		
    	private final ObjGeometryStore geomStore;
    	
    	private AmbientColorsIntoTexCoordsConverter(final ObjGeometryStore geomStore){
    		super();
    		this.geomStore=geomStore;
    	}
    	
		@Override
    	public void visit(final Spatial spatial){
			if(spatial instanceof Mesh)
			    {final Mesh mesh=(Mesh)spatial;
			     final MaterialState materialState=(MaterialState)mesh.getLocalRenderState(StateType.Material);
			     //checks whether there is a material state
			     if(materialState!=null)
			         {final ReadOnlyColorRGBA ambientRgbaColor=materialState.getAmbient();
			          //checks whether there is a material ambient color
			    	  if(ambientRgbaColor!=null)
			    	      {final MeshData meshData=mesh.getMeshData();
			    		   //checks whether there is no texture coordinate buffer
				           if(meshData!=null&&meshData.getTextureCoords(0)==null)
				               {//gets the vertex buffer
				        	    final Buffer vertexBuffer=meshData.getVertexBuffer();
				                //checks whether there is a vertex buffer
					            if(vertexBuffer!=null)
					                {final int vertexCount=meshData.getVertexCount();
					                 //creates the new texture coordinate buffer data of the mesh
					                 final FloatBuffer texCoordBuffer=BufferUtils.createFloatBufferOnHeap(vertexCount*2);
					            	 final FloatBufferData texCoordBufferData=new FloatBufferData(texCoordBuffer,2);
					                 final Vector3[] triangleVertices=new Vector3[]{new Vector3(),new Vector3(),new Vector3()};
					                 final Vector2[] triangleTexCoords=new Vector2[]{new Vector2(),new Vector2(),new Vector2()};
					                 int tmpDummyTexCoordIndex=0;
					                 for(int sectionIndex=0,sectionCount=meshData.getSectionCount();sectionIndex<sectionCount;sectionIndex++)
					                	 //only takes care of sections containing triangles
					    				 if(meshData.getIndexMode(sectionIndex)==IndexMode.Triangles)
					                         {//loops on all triangles of each section
					   					      for(int trianglePrimitiveIndex=0,triangleCount=meshData.getPrimitiveCount(sectionIndex);trianglePrimitiveIndex<triangleCount;trianglePrimitiveIndex++)
								                  {//gets the 3 vertices of the triangle
										           meshData.getPrimitiveVertices(trianglePrimitiveIndex,sectionIndex,triangleVertices);
										           //TODO use a better algorithm to compute the texture coordinates
										           for(Vector2 triTexCoord:triangleTexCoords)
										               {triTexCoord.set(tmpDummyTexCoordIndex/2,tmpDummyTexCoordIndex%2);
										        	    tmpDummyTexCoordIndex=(tmpDummyTexCoordIndex+1)%4;
										               }
										           //puts the texture coordinates into the buffer
										           for(Vector2 triTexCoord:triangleTexCoords)
										               {texCoordBuffer.put(triTexCoord.getXf());
										                texCoordBuffer.put(triTexCoord.getYf());
										               }
								                  }
					                         }
					                 //TODO remove this loop
					            	 //fills it with texture coordinates
					            	 /*int vertexIndex=0;
					            	 while(texCoordBuffer.hasRemaining())
					            	     {texCoordBuffer.put(vertexIndex/2);
					            	      texCoordBuffer.put(vertexIndex%2);
					            	      vertexIndex=(vertexIndex+1)%4;
					            	     }*/
					            	 texCoordBuffer.rewind();
					            	 //sets the new texture coordinate buffer data of the mesh
					            	 meshData.setTextureCoords(texCoordBufferData,0);
					                }
				               }
			    	      }
			         }
			     final String materialName=geomStore.getMaterialMap().get(mesh);
			     //modifies its texture state
			     switch(materialName)
			     {
			         case "ASPHALT":
			        	 Texture texture=TextureManager.load("asphalt.png",Texture.MinificationFilter.Trilinear,true);
			        	 TextureState textureState=(TextureState)mesh.getLocalRenderState(StateType.Texture);
			        	 if(textureState==null)
			                 {textureState=new TextureState();
			    	          mesh.setRenderState(textureState);
			                 }
			        	 textureState.setTexture(texture,0);
			        	 textureState.setEnabled(true);
			        	 //removes the colors
			        	 mesh.getMeshData().setColorCoords(null);
			        	 break;
			         case "BRICK":
			         case "BRIDGE_DEFAULT":
			         case "BUILDING_DEFAULT":
			         case "BUS_STOP_SIGN":
			         case "COBBLESTONE":
			         case "CONCRETE":
			         case "EARTH":
			         case "FENCE_DEFAULT":
			         case "GLASS":
			         case "GRASS":
			         case "MAT_0":
			         case "MAT_1":
			         case "RED_ROAD_MARKING":
			         case "ROAD_MARKING_DASHED":
			         case "ROOF_DEFAULT":
			         case "STEEL":
			         case "STEPS_DEFAULT":
			         case "TERRAIN_DEFAULT":
			         case "TUNNEL_DEFAULT":
			         case "WATER":
			         
			     }
			    }
		}
	}
	
	private void convertAmbientColorsIntoTexCoords(final Spatial spatial,final ObjGeometryStore geomStore){
		spatial.acceptVisitor(new AmbientColorsIntoTexCoordsConverter(geomStore),false);
	}
	
	private static final class AmbientColorsIntoVerticesColorsConverter implements Visitor{
		
		@Override
    	public void visit(final Spatial spatial){
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
	}
	
	private void convertAmbientColorsIntoVerticesColors(final Spatial spatial){
		spatial.acceptVisitor(new AmbientColorsIntoVerticesColorsConverter(),false);
	}
	
	public static final void main(final String[] args){
		try{final ObjToArdorConverter converter=new ObjToArdorConverter();
		    //only for OSM WaveFront OBJ files
		    converter.setConversionOfAmbientColorsIntoVerticesColorsEnabled(true);
		    converter.setConversionOfAmbientColorsIntoTexCoordsEnabled(true);
		    converter.run(args);
		   }
		catch(Throwable t)
		{t.printStackTrace();}
    }
}
