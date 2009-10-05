package jfpsm;

import java.io.File;
import java.io.FileFilter;

public final class ProjectFileFilter implements FileFilter{

    
    @Override
    public final boolean accept(File file) {
        return(file.isFile()&&file.canRead()&&file.getName().endsWith(Project.getFileExtension()));
    }
}
