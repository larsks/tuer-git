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
package bean;

public final class BeanProvider implements IBeanProvider{
    
    
    private static final BeanProvider instance=new BeanProvider();
    
    
    private BeanProvider(){}
    
    
    public static final BeanProvider getInstance(){
        return(instance);
    }
    
    @Override
    public final ILevelModelBean getILevelModelBean(float[] initialSpawnPosition,String[] identifiedNodeNames){
        LevelModelBean lmb=new LevelModelBean();
        lmb.setInitialSpawnPosition(initialSpawnPosition);
        lmb.setIdentifiedNodeNames(identifiedNodeNames);
        return(lmb);
    }
    
    @Override
    public final INodeIdentifier getINodeIdentifier(){
        return(new NodeIdentifier());
    }
    
    @Override
    public final void bindBeanProvider(IBeanProvider provider){}
}
