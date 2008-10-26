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

package connection;

import drawer.VertexSetFactory;
import java.io.IOException;
import java.nio.FloatBuffer;
import main.GameController;
import main.IDynamicVertexSet;
import main.IStaticVertexSet;
import main.IVertexSet;
import main.IVertexSetProvider;
import main.VertexSetSeeker;

public class GameServiceProvider implements IVertexSetProvider{


    private drawer.IVertexSetProvider delegate;          
	  
    
    private GameServiceProvider(final drawer.IVertexSetProvider factory,final IVertexSetProvider seeker){
        this.bindVertexSetProvider(factory);
	    this.bindVertexSetProvider(seeker);
    }
    

    public void bindVertexSetProvider(final drawer.IVertexSetProvider provider){
         delegate=provider;		
    }
    
    public void bindVertexSetProvider(final main.IVertexSetProvider provider){
         provider.bindVertexSetProvider(this);		
    }
    
    @Override
    public IStaticVertexSet getIStaticVertexSetInstance(final float[] array,int mode){
        return(new StaticVertexSetConnector(delegate.getIStaticVertexSetInstance(array,mode)));
    }
    
    @Override
    public IStaticVertexSet getIStaticVertexSetInstance(final IVertexSet vertexSet,int mode){
        return(new StaticVertexSetConnector(delegate.getIStaticVertexSetInstance(new VertexSetConnector(vertexSet),mode)));
    }
    
    @Override
    public IStaticVertexSet getIStaticVertexSetInstance(FloatBuffer floatBuffer, int mode){       
        return(new StaticVertexSetConnector(delegate.getIStaticVertexSetInstance(floatBuffer,mode)));
    }
    
    @Override
    public IDynamicVertexSet getIDynamicVertexSetInstance(final float[] array,int mode){
        return(new DynamicVertexSetConnector(delegate.getIDynamicVertexSetInstance(array,mode)));
    }
    
    @Override
    public IDynamicVertexSet getIDynamicVertexSetInstance(final IVertexSet vertexSet,int mode){
        return(new DynamicVertexSetConnector(delegate.getIDynamicVertexSetInstance(new VertexSetConnector(vertexSet),mode)));
    }
    
    @Override
    public IDynamicVertexSet getIDynamicVertexSetInstance(final FloatBuffer floatBuffer,int mode){
        return(new DynamicVertexSetConnector(delegate.getIDynamicVertexSetInstance(floatBuffer,mode)));
    }
    
    
    public static void main(final String[] args){
        new GameServiceProvider(VertexSetFactory.getInstance(),VertexSetSeeker.getInstance());
        Runtime.getRuntime().addShutdownHook(new CleanupThread());
        if(System.getProperty("os.name").compareToIgnoreCase("Linux")==0)
            try{Runtime.getRuntime().exec("xset r off");
                System.out.println("xset r off");
               }
            catch(final IOException ioe)
            {ioe.printStackTrace();}
        try{new GameController();}
        catch(Throwable t)
        {t.printStackTrace();}
        //only here, it has nothing to do anywhere else
        System.exit(0);
    }   
}
