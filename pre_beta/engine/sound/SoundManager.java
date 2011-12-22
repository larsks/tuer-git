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

	
    /**underlying sound system written by Paul Lamb*/
    private SoundSystem soundSystem;
    
    
    public SoundManager(){
        try{try{soundSystem=new SoundSystem(LibraryJOAL.class);}
    	    catch(SoundSystemException sseOpenAL)
    	    {System.out.println("The initialization of the sound manager (based on JOAL) failed: "+sseOpenAL);
    	     try{soundSystem=new SoundSystem(LibraryJavaSound.class);}
    	     catch(SoundSystemException sseJavaSound)
    	     {System.out.println("The initialization of the sound manager (based on JavaSound) failed: "+sseJavaSound);}
    	    }
    	    if(soundSystem!=null)
                SoundSystemConfig.setCodec("ogg",CodecJOrbis.class);
    	   }
        catch(SoundSystemException sse)
        {System.out.println("The initialization of the sound manager failed: "+sse);}
    }
    
    public final String preloadSoundSample(final URL url,final boolean backgroundMusic){
        final String path=url.getPath();
        final String identifier=path.substring(path.lastIndexOf("/"));
        final String sourcename=identifier.substring(0,identifier.lastIndexOf("."));
        final boolean priority;
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
        if(soundSystem!=null)
            soundSystem.newSource(priority,sourcename,url,identifier,false,0,0,0,attenuationModel,rollOffFactor);
        return(sourcename);
    }
    
    public final void play(String sourcename){
        if(soundSystem!=null)
            soundSystem.play(sourcename);
    }
    
    public final void stop(String sourcename){
        if(soundSystem!=null)
            soundSystem.stop(sourcename);
    }
    
    public final void cleanup(){
        if(soundSystem!=null)
            soundSystem.cleanup();
    }
}