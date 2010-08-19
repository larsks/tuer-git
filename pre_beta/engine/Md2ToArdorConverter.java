package engine;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import com.ardor3d.extension.model.md2.Md2Importer;
import com.ardor3d.image.Texture;
import com.ardor3d.image.util.AWTImageLoader;
import com.ardor3d.renderer.state.TextureState;
import com.ardor3d.scenegraph.Spatial;
import com.ardor3d.util.TextureManager;
import com.ardor3d.util.export.binary.BinaryExporter;
import com.ardor3d.util.resource.ResourceLocatorTool;
import com.ardor3d.util.resource.SimpleResourceLocator;
import com.ardor3d.util.resource.URLResourceSource;

public final class Md2ToArdorConverter{

    
    public static final void main(String[] args) throws IOException, URISyntaxException{
        AWTImageLoader.registerLoader();
        try{SimpleResourceLocator srl=new SimpleResourceLocator(Md2ToArdorConverter.class.getResource("/images"));
            ResourceLocatorTool.addResourceLocator(ResourceLocatorTool.TYPE_TEXTURE,srl);
            srl=new SimpleResourceLocator(Md2ToArdorConverter.class.getResource("/md2"));
            ResourceLocatorTool.addResourceLocator(ResourceLocatorTool.TYPE_MODEL,srl);
           } 
        catch(final URISyntaxException urise)
        {urise.printStackTrace();}
        Md2Importer md2Importer=new Md2Importer();
        URLResourceSource imageSource;
        URLResourceSource source;
        Spatial spatial;
        TextureState ts;
        File sourceFile,imageSourceFile,destFile;
        for(String arg:args)
            {System.out.println("Loading "+arg+" ...");
             spatial=md2Importer.load(arg).getScenegraph();
             source=(URLResourceSource)ResourceLocatorTool.locateResource(ResourceLocatorTool.TYPE_MODEL,arg);
             sourceFile=new File(source.getURL().toURI());
             destFile=new File(sourceFile.getAbsolutePath().substring(0,sourceFile.getAbsolutePath().lastIndexOf(".md2"))+".abin");
             if(!destFile.exists())
                 if(!destFile.createNewFile())
                     {System.out.println(destFile.getAbsolutePath()+" cannot be created!");
                      continue;
                     }
             imageSourceFile=new File(sourceFile.getName().substring(0,sourceFile.getName().lastIndexOf(".md2"))+".png");
             imageSource=(URLResourceSource)ResourceLocatorTool.locateResource(ResourceLocatorTool.TYPE_TEXTURE,imageSourceFile.getName());
             if(imageSource!=null){
            	 ts=new TextureState();
                 ts.setEnabled(true);
                 ts.setTexture(TextureManager.load(new URLResourceSource(imageSource.getURL()),Texture.MinificationFilter.Trilinear,true));
                 spatial.setRenderState(ts);
             }
             System.out.println("Converting "+arg+" ...");
             BinaryExporter.getInstance().save(spatial,destFile);
             System.out.println(arg+" successfully converted");
            }
    }
}