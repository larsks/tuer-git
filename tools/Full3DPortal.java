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
package tools;

import java.util.Arrays;

public final class Full3DPortal{
    
    
    private Full3DCell[] linkedCells;
    
    private float[][] portalVertices;
    
    private Full3DPortalController controller;
    
    
    public Full3DPortal(Full3DCell[] linkedCells,float[][] portalVertices){
        this.linkedCells=linkedCells;
        this.portalVertices=portalVertices;
        this.controller=null;
    }
    
    public Full3DPortal(Full3DCell linkedCell1,Full3DCell linkedCell2,
            float[] portalVertex1,float[] portalVertex2,
            float[] portalVertex3,float[] portalVertex4){
        this(new Full3DCell[]{linkedCell1,linkedCell2},
             new float[][]{portalVertex1,portalVertex2,portalVertex3,portalVertex4});       
    }
    
    public final boolean equals(Object o){
        boolean result;
        if(o==null||!(o instanceof Full3DPortal))
            result=false;
        else
            {Full3DPortal portal=(Full3DPortal)o;
             result=Arrays.equals(linkedCells,portal.linkedCells);
             if(result)
                 {if(portalVertices==null)
                      result=portal.portalVertices==null;
                  else
                      {result=portal.portalVertices!=null&&portalVertices.length==portal.portalVertices.length;
                       for(int i=0;result&&i<portalVertices.length;i++)
                           if(!Arrays.equals(portalVertices[i],portal.portalVertices[i]))
                               result=false;
                      }
                 }
            }
        return(result);
    }

    public final Full3DCell[] getLinkedCells(){
        return(linkedCells);
    }

    public final float[][] getPortalVertices(){
        return(portalVertices);
    }

    public final float[] getPortalVertices(int index){
        return(portalVertices[index]);
    }

    public final void setController(Full3DPortalController controller){
        this.controller=controller;
    }

    public final Full3DPortalController getController(){
        return(controller);
    }
}
