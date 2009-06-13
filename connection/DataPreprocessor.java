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

import tools.IBeanProvider;
import tools.ILevelModelBean;
import tools.INodeIdentifier;
import tools.TilesGenerator;

public final class DataPreprocessor implements IBeanProvider{
    
    
    private bean.IBeanProvider delegate;

    
    private DataPreprocessor(bean.IBeanProvider factory,IBeanProvider seeker){
        bindBeanProvider(factory);
        bindBeanProvider(seeker);
    }
    
    
    public final void bindBeanProvider(bean.IBeanProvider provider){
        delegate=provider;     
    }
    
    @Override
    public final void bindBeanProvider(tools.IBeanProvider provider){
         provider.bindBeanProvider(this);      
    }


    @Override
    public final ILevelModelBean getILevelModelBean(float[] initialSpawnPosition,String[] identifiedNodeNames){
        return(new LevelModelBeanConnector(delegate.getILevelModelBean(initialSpawnPosition,identifiedNodeNames)));
    }
    
    @Override
    public final INodeIdentifier getINodeIdentifier(){
        return(new NodeIdentifierConnector(delegate.getINodeIdentifier()));
    }
    
    public static final void main(String[] args){
        DataPreprocessor dp=new DataPreprocessor(bean.BeanProvider.getInstance(),tools.BeanProvider.getInstance());
        if(dp!=null)
            {System.out.println("[INFO] Data processing initialized");
             TilesGenerator tg=TilesGenerator.getInstance(args);
             if(tg!=null)
                 {System.out.println("[INFO] Tiles and cells generator ready");
                  tg.run();
                  System.out.println("[INFO] Tiles and cells generation finished");
                 }
             else
                 System.out.println("[WARNING] Tiles and cells generation failed!");
            }
        else
            System.out.println("[WARNING] Data processing initialization failed!");
    }
}
