package engine;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import com.ardor3d.extension.model.obj.ObjImporter;
import com.ardor3d.image.util.AWTImageLoader;
import com.ardor3d.scenegraph.Spatial;
import com.ardor3d.util.export.binary.BinaryExporter;
import com.ardor3d.util.resource.ResourceLocatorTool;
import com.ardor3d.util.resource.SimpleResourceLocator;
import com.ardor3d.util.resource.URLResourceSource;

public class ObjToArdorConverter {

	
	public static final void main(String[] args) throws IOException, URISyntaxException{
        AWTImageLoader.registerLoader();
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
             objSpatial=objImporter.load(arg).getScenegraph();
             URLResourceSource source=(URLResourceSource)ResourceLocatorTool.locateResource(ResourceLocatorTool.TYPE_MODEL,arg);
             File sourceFile=new File(source.getURL().toURI());
             File destFile=new File(sourceFile.getAbsolutePath().substring(0,sourceFile.getAbsolutePath().lastIndexOf(".obj"))+".abin");
             if(!destFile.exists())
                 if(!destFile.createNewFile())
                     {System.out.println(destFile.getAbsolutePath()+" cannot be created!");
                      continue;
                     }
             System.out.println("Converting "+arg+" ...");
             BinaryExporter.getInstance().save(objSpatial,destFile);
             System.out.println(arg+" successfully converted");
            }
    }
}
