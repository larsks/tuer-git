package configuration;

import main.XMLTransportableWrapper;

public final class ApplicationBehaviorBean implements XMLTransportableWrapper<ApplicationBehavior> {

    
    private String levelsConfigurationDirectoryPath;
    
    
    public ApplicationBehaviorBean(){}
    
    
    @Override
    public ApplicationBehavior getWrappedObject() {       
        ApplicationBehavior ab=new ApplicationBehavior();
        ab.setLevelsConfigurationDirectoryPath(levelsConfigurationDirectoryPath);
        return(ab);
    }

    @Override
    public void wrap(ApplicationBehavior ab) {       
        levelsConfigurationDirectoryPath=ab.getLevelsConfigurationDirectoryPath();
    }

}
