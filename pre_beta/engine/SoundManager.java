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
package engine;

import java.net.URL;
import paulscode.sound.SoundSystem;
import paulscode.sound.SoundSystemConfig;
import paulscode.sound.SoundSystemException;
import paulscode.sound.codecs.CodecJOrbis;
import paulscode.sound.libraries.LibraryJavaSound;

public final class SoundManager{

    
    private static final SoundManager instance=new SoundManager();
    
    private SoundSystem soundSystem;
    
    
    private SoundManager(){
        try{soundSystem=new SoundSystem(LibraryJavaSound.class);
            SoundSystemConfig.setCodec("ogg",CodecJOrbis.class);
           }
        catch(SoundSystemException sse)
        {System.out.println("The initialization of the sound manager failed: "+sse);}
    }
    
    public final String preloadSoundSample(final URL url){
        final String path=url.getPath();
        final String identifier=path.substring(path.lastIndexOf("/"));
        final String sourcename=identifier.substring(0,identifier.lastIndexOf("."));
        if(soundSystem!=null)
            soundSystem.newSource(false,sourcename,url,identifier,false,0,0,0,SoundSystemConfig.ATTENUATION_ROLLOFF,SoundSystemConfig.getDefaultRolloff());
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
    
    public static final SoundManager getInstance(){
        return(instance);
    }
    
    public final void cleanup(){
        if(soundSystem!=null)
            soundSystem.cleanup();
    }
}
