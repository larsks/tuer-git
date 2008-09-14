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

import java.io.Serializable;

public final class Impact implements Serializable {

    
    private static final long serialVersionUID = 1L;
    
    private float x;
    
    private float y;
    
    private float z;
    
    private float nx;
    
    private float ny;
    
    private float nz;
    
    
    public Impact(){}
    
    public Impact(float x,float y,float z,float nx,float ny,float nz){
        this.x=x;
        this.y=y;
        this.z=z;
        this.nx=nx;
        this.ny=ny;
        this.nz=nz;       
    }

    
    public final String toString(){
        return(x+" "+y+" "+z+" "+nx+" "+ny+" "+nz);
    }
    
    public final float getX(){
        return(x);
    }
    
    public final float getY(){
        return(y);
    }
    
    public final float getZ(){
        return(z);
    }
    
    public final float getNx(){
        return(nx);
    }
    
    public final float getNy(){
        return(ny);
    }
    
    public final float getNz(){
        return(nz);
    }   
    
    public final void setX(float x){
        this.x=x;
    }
    
    public final void setY(float y){
        this.y=y;
    }
    
    public final void setZ(float z){
        this.z=z;
    }
    
    public final void setNx(float nx){
        this.nx=nx;
    }
    
    public final void setNy(float ny){
        this.ny=ny;
    }
    
    public final void setNz(float nz){
        this.nz=nz;
    }
    
    /**
     * compute an impact from the bounds of a tarjectory
     * in the simplified case (i.e with orthogonal walls and
     * in the planar surface Oxz)
     * @param x1: abscissa of the previous position of the projected object
     * @param z1: applicate of the previous position of the projected object
     * @param x2: abscissa of the next position of the projected object
     * @param z2: applicate of the next position of the projected object
     * @param wx1: abscissa of the first point of the wall
     * @param wz1: applicate of the first point of the wall
     * @param wx2: abscissa of the second point of the wall
     * @param wz2: applicate of the second point of the wall
     * @param wnx: abscissa of the normal at the impact
     * @param wnz: applicate of the normal at the impact
     * @return the computed impact if found, otherwise null
     */
    public static final Impact computeImpactFromTargetoryBipoint(float x1,float z1,
            float x2,float z2,float wx1,float wz1,float wx2,float wz2,float wnx,
            float wnz){
        boolean isHorizontal=(wnz!=0);
        if(x1==x2)
            {if(!isHorizontal)
                {if(x1==wx1)
                    {if(wz1>wz2)
                         {if(wz2<=z2 && z2<=wz1)
                              {if(z1>z2)
                                   return(new Impact(wx1,0.0f,wz1,wnx,0,wnz));
                               else
                                   return(new Impact(wx1,0.0f,wz2,wnx,0,wnz));
                              }
                          else                              
                              return(null);
                         }
                     else
                         {if(wz1<=z2 && z2<=wz2)
                              {if(z1>z2)
                                   return(new Impact(wx1,0.0f,wz2,wnx,0,wnz));
                               else
                                   return(new Impact(wx1,0.0f,wz1,wnx,0,wnz));                                
                              }
                          else
                              return(null);                         
                         }
                     }
                 else                   
                     return(null);                    
                }
             else
                 {if(wx1>wx2)
                      {if(wx2<=x1 && x1<=wx1)
                           {if(z1>z2)
                                {if(z2<=wz1 && wz1<=z1)
                                     return(new Impact(x1,0.0f,wz1,wnx,0,wnz));
                                 else                                    
                                     return(null);
                                }
                            else
                                {if(z1<=wz1 && wz1<=z2)
                                     return(new Impact(x1,0.0f,wz1,wnx,0,wnz));
                                 else                                    
                                     return(null);
                                }
                           }
                       else                          
                           return(null);
                      }
                  else
                      {if(wx1<=x1 && x1<=wx2)
                           {if(z1>z2)
                                {if(z2<=wz1 && wz1<=z1)
                                     return(new Impact(x1,0.0f,wz1,wnx,0,wnz));
                                 else                                    
                                     return(null);
                                }
                            else
                                {if(z1<=wz1 && wz1<=z2)
                                     return(new Impact(x1,0.0f,wz1,wnx,0,wnz));
                                 else                                    
                                     return(null);
                                }                             
                           }
                       else
                           return(null);
                      }
                 }
            }
        else
            {float a=(z2-z1)/(x2-x1);
             float b=z1-(a*x1);
             float impx,impz;
             if(!isHorizontal)
                 {impx=wx1;
                  impz=(a*impx)+b;
                  if(wz1>wz2)
                      {if(wz2<=impz && impz<=wz1)
                           return(new Impact(impx,0.0f,impz,wnx,0,wnz));
                       else                         
                           return(null);
                      }
                  else
                      {if(wz1<=impz && impz<=wz2)
                           return(new Impact(impx,0.0f,impz,wnx,0,wnz));
                       else                        
                           return(null);
                      }
                 }
             else
                 {if(a!=0)
                      {impz=wz1;
                       impx=(impz-b)/a; 
                       if(wx1>wx2)
                           {if(wx2<=impx && impx<=wx1)
                                return(new Impact(impx,0.0f,impz,wnx,0,wnz));
                            else                              
                                return(null);
                           }
                       else
                           {if(wx1<=impx && impx<=wx2)
                                return(new Impact(impx,0.0f,impz,wnx,0,wnz));
                            else                               
                                return(null);
                           }
                      }
                  else
                      {if(z1==wz1)
                           {if(wx1>wx2)
                               {if(wx2<=x2 && x2<=wx1)
                                    {if(x1>x2)
                                         return(new Impact(wx1,0.0f,wz1,wnx,0,wnz));
                                     else
                                         return(new Impact(wx2,0.0f,wz1,wnx,0,wnz));
                                    }
                                else
                                    return(null);
                               }
                           else
                               {if(wx1<=x2 && x2<=wx2)
                                    {if(x1>x2)
                                         return(new Impact(wx2,0.0f,wz1,wnx,0,wnz));
                                     else
                                         return(new Impact(wx1,0.0f,wz1,wnx,0,wnz));                                
                                    }
                                else
                                    return(null);
                               }
                           }
                       else
                           return(null);                      
                      }
                 }
            }      
    }
}
