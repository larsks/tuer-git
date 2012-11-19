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
package engine.sound;

import java.net.URL;
import java.util.LinkedList;

import paulscode.sound.SoundSystem;
import paulscode.sound.SoundSystemConfig;
import paulscode.sound.SoundSystemException;
import paulscode.sound.codecs.CodecJOrbis;
import paulscode.sound.libraries.LibraryJOAL;
import paulscode.sound.libraries.LibraryJavaSound;

/**
 * Sound manager that relies on JOAL and Java Sound System written by Paul Lamb.
 * If the sound is not available, it simply does nothing.
 * @author Julien Gouesse
 *
 */
public final class SoundManager{

	private static class ExtendedSoundSystem extends SoundSystem{
		
		@SuppressWarnings("rawtypes")
		private ExtendedSoundSystem(Class libraryClass) throws SoundSystemException{
			super(libraryClass);
		}
		
		private void stop(){
			LinkedList<String> sourcenames=soundLibrary.getAllSourcenames();
			for(String sourcename:sourcenames)
			    stop(sourcename);
			removeTemporarySources();
		}
	}	
	
    /**underlying sound system written by Paul Lamb*/
    private ExtendedSoundSystem soundSystem;
    /**flag indicating whether the sound is enabled*/
    private boolean enabled;
    
    
    public SoundManager(){
        try{try{soundSystem=new ExtendedSoundSystem(LibraryJOAL.class);}
    	    catch(SoundSystemException sseOpenAL)
    	    {System.out.println("The initialization of the sound manager (based on JOAL) failed: "+sseOpenAL);
    	     try{soundSystem=new ExtendedSoundSystem(LibraryJavaSound.class);}
    	     catch(SoundSystemException sseJavaSound)
    	     {System.out.println("The initialization of the sound manager (based on JavaSound) failed: "+sseJavaSound);}
    	    }
    	    if(soundSystem!=null)
                SoundSystemConfig.setCodec("ogg",CodecJOrbis.class);
    	   }
        catch(SoundSystemException sse)
        {System.out.println("The initialization of the sound manager failed: "+sse);}
        finally
        {enabled=true;}
    }
    
    public final boolean isEnabled(){
    	return(enabled);
    }
    
    public final void setEnabled(final boolean enabled){
    	this.enabled=enabled;
    	if(enabled)
    	    {//TODO 
    		 
    	    }
    	else
    	    {//TODO mute
    		 
    	    }
    }
    
    public final String loadSound(final URL url){
    	final String identifier;
        if(soundSystem!=null)
            {final String path=url.getPath();
             identifier=path.substring(path.lastIndexOf("/"));
             soundSystem.loadSound(url,identifier);
            }
        else
        	identifier=null;
        return(identifier);
    }   
    
    public final void play(final boolean backgroundMusic,final boolean toLoop,final String identifier,float x,float y,float z){
    	if(soundSystem!=null)
    	    {final boolean priority;
             final int attenuationModel;
             final float rollOffFactor;
    		 if(backgroundMusic)
                 {priority=true;
                  attenuationModel=SoundSystemConfig.ATTENUATION_NONE;
                  rollOffFactor=0;
                 }
             else
                 {priority=false;
                  attenuationModel=SoundSystemConfig.ATTENUATION_ROLLOFF;
                  rollOffFactor=SoundSystemConfig.getDefaultRolloff();
                 }
    		 soundSystem.quickPlay(priority,null,identifier,toLoop,x,y,z,attenuationModel,rollOffFactor);
    	    }
    }
    
    public final void play(final boolean backgroundMusic,final boolean toLoop,final String identifier){
    	play(backgroundMusic,toLoop,identifier,0,0,0);
    }
    
    public final void stop(){
        if(soundSystem!=null)
        	soundSystem.stop();
    }
    
    public final void cleanup(){
        if(soundSystem!=null)
            soundSystem.cleanup();
    }
}