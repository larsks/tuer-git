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
package main;

import java.io.IOException;
import java.nio.FloatBuffer;
import java.util.Arrays;
import java.util.List;
import java.util.Vector;

import tools.GameIO;

final class ExplosionModelFactory extends ModelFactory {

    private static ExplosionModelFactory instance=null;

    
    private ExplosionModelFactory(){
        FloatBuffer[] coordinatesBuffer=new FloatBuffer[16];       
        List<FloatBuffer> coordinatesBuffersList=new Vector<FloatBuffer>(coordinatesBuffer.length);
        FloatBuffer explosionCoordinatesBuffer=null;
        try{
            explosionCoordinatesBuffer=GameIO.readGameFloatDataFile("/pic256/explosion.data");
           }
        catch(IOException ioe)
        {ioe.printStackTrace();}
        for(int i=0;i<coordinatesBuffer.length;i++)
            coordinatesBuffer[i]=explosionCoordinatesBuffer;            
        coordinatesBuffersList.addAll(Arrays.asList(coordinatesBuffer));
        List<AnimationInfo> animationList=new Vector<AnimationInfo>(1);
        animationList.add(new AnimationInfo("",0,coordinatesBuffer.length-1,8));
        setAnimationList(animationList);
        setCoordinatesBuffersList(coordinatesBuffersList);
    }
    
    final static ExplosionModelFactory getInstance(){      
        if(instance==null)
            instance=new ExplosionModelFactory();
        return(instance);
    }
}
