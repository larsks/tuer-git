package configuration;

import java.beans.XMLDecoder;
import java.io.BufferedInputStream;

public final class ApplicationBehavior {

    private static final String CONFIG_XML_DEFAULT_PATH="/xml/ApplicationBehavior.xml";
    
    private static ApplicationBehavior instance=null;
    
    private String levelsConfigurationDirectoryPath;//ends with a file separator
    
    
    ApplicationBehavior(){}
    
    
    public static final ApplicationBehavior getInstance(){       
        if(instance==null)
            {ApplicationBehaviorBean abb=null;
             //use CONFIG_XML_DEFAULT_PATH to decode the bean
             BufferedInputStream bis=null;
             try{bis=new BufferedInputStream(ApplicationBehavior.class.getResourceAsStream(CONFIG_XML_DEFAULT_PATH));
                 XMLDecoder decoder = new XMLDecoder(bis);
                 abb=(ApplicationBehaviorBean)decoder.readObject();        
                 decoder.close();
                } 
             catch(Exception e)
             {throw new RuntimeException("Unable to decode XML file",e);}
             //use the bean to instantiate the useful object
             if(abb!=null)
                 instance=abb.getWrappedObject();
            }        
        return(instance);
    }

    public final String getLevelsConfigurationDirectoryPath(){
        return(levelsConfigurationDirectoryPath);
    }

    final void setLevelsConfigurationDirectoryPath(String levelsConfigurationDirectoryPath){
        if(levelsConfigurationDirectoryPath.endsWith("/"))
            levelsConfigurationDirectoryPath+="/";
        this.levelsConfigurationDirectoryPath=levelsConfigurationDirectoryPath;
    }        
}
