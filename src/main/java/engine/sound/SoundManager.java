/**
 * Copyright (c) 2006-2015 Julien Gouesse
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
package engine.sound;

import java.net.URL;
import java.util.LinkedList;

import paulscode.sound.SoundSystem;
import paulscode.sound.SoundSystemConfig;
import paulscode.sound.SoundSystemException;
import paulscode.sound.codecs.CodecJOrbis;
import paulscode.sound.libraries.LibraryJOAL;

/**
 * Sound manager that relies on JOAL based on Paul Lamb's library.
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
    
    private float latestMasterVolume;
    
    
    public SoundManager(){
        try{try{soundSystem=new ExtendedSoundSystem(LibraryJOAL.class);}
    	    catch(SoundSystemException sseOpenAL)
    	    {System.out.println("The initialization of the sound manager (based on JOAL) failed: "+sseOpenAL);}
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
    	    {//resets the volume to its latest non null value
    		 soundSystem.setMasterVolume(latestMasterVolume);
    	    }
    	else
    	    {//gets the current volume before muting
    		 latestMasterVolume=soundSystem.getMasterVolume();
    		 soundSystem.setMasterVolume(0);
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
    
    public final void unloadSound(final URL url){
        if(soundSystem!=null)
            {final String path=url.getPath();
             soundSystem.unloadSound(path);
            }
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
            {soundSystem.cleanup();
             //prevents the manager from playing another sound
             soundSystem=null;
            }
    }
}