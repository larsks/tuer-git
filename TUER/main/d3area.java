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

/**
 *This class represents a piece of level linked with lights and 3D objects
 *
 *@author Vincent Stahl, Julien Gouesse
 */


public final class d3area{


   private int nmembers;       // no. of objects linked to the area
   private d3object amember[]; // fast and simple object table.
   // this table may contain HOLE entries (null's), to avoid
   // permanent re-sorting. remember this when accessing.
   private int nimages;        // no. of images in the area
   private int aimagex[];      // and their locations in map coords
   private int aimagez[];      // (0..255 for x and z)
   private boolean announced;  // this area was reached+announced.
   private int iname;          // internal area ID (number)
   private int light;          // area's light level, upto 255
   private int lightsUpCnt;    // counter for lightening things up
   private int foglevel;       // area fog intensity
   private int lightlevel;     // soft darkening
   private int spawnx;         // for restart within area
   private int spawnz;         // ""
   private double playerdir;   // ""
   private int nlights;        // NEW/1.1.0: light sources.
   private int alightx[];      // this table contains HOLES
   private int alightz[];      // where lightx==0.
   
   
   public d3area(int idx) {
      nmembers    = 0;
      amember     = new d3object[40];
      iname       = idx;
      nimages     = 0;
      aimagex     = new int[40];
      aimagez     = new int[40];
      announced   = false;
      light       = 255;
      lightsUpCnt = 0;
      nlights     = 0;
      foglevel    = 100;
      lightlevel  = 256;
      spawnx      = -1;
      spawnz      = -1;
      playerdir   = Math.PI;  // upwards
      nlights     = 0;
      alightx     = new int[40];
      alightz     = new int[40];
   }
   
   
   int getNmembers(){return(nmembers);}
   
   d3object[] getAmember(){return(amember);} 
   
   d3object getAmember(int i){return(amember[i]);}
   
   int getNimages(){return(nimages);} 
          
   int[] getAimagex(){return(aimagex);} 
        
   int[] getAimagez(){return(aimagez);}  
       
   boolean getAnnounced(){return(announced);} 
   
   int getIname(){return(iname);}  
           
   int getLight(){return(light);}   
                
   int getLightsUpCnt(){return(lightsUpCnt);} 
      
   int getFoglevel(){return(foglevel);} 
         
   int getLightlevel(){return(lightlevel);} 
       
   int getSpawnx(){return(spawnx);} 
           
   int getSpawnz(){return(spawnz);} 
           
   double getPlayerdir(){return(playerdir);} 
     
   int getNlights(){return(nlights);}  
         
   int[] getAlightx(){return(alightx);} 
        
   int[] getAlightz(){return(alightz);}      
   
   void setAnnounced(boolean announced){this.announced=announced;}
   
   void setLight(int light){this.light=light;}
   
   void setLightsUpCnt(int lightsUpCnt){this.lightsUpCnt=lightsUpCnt;}
   
   void setFoglevel(int foglevel){this.foglevel=foglevel;}
   
   void setLightlevel(int lightlevel){this.lightlevel=lightlevel;}
   
   void setSpawnx(int spawnx){this.spawnx=spawnx;}
   
   void setSpawnz(int spawnz){this.spawnz=spawnz;}
   
   void setPlayerdir(double playerdir){this.playerdir=playerdir;}
   
   void setNlights(int nlights){this.nlights=nlights;}
   
   void addMember(d3object obj) {
      if (nmembers < amember.length-1)
         amember[nmembers++] = obj;
      else
      System.out.println("WARNING: too many bots in area "+iname);
   }
   
   void removeMember(d3object obj) {
      int i;
      for (i=0;i<amember.length;i++)
         if (amember[i]==obj) {
            amember[i]=null;
            nmembers--;
            break;
         }
      if (i==amember.length) System.out.println("X1634953");
   }
   
   void addImage(int x,int z) {
      if (nimages < aimagex.length-1) {
         aimagex[nimages]=x;
         aimagez[nimages]=z;
         nimages++;
      }
      else
      System.out.println("WARNING: too many images in area "+iname);
   }
   
   void setSpawnPoint(int x,int z) {
      spawnx = x;
      spawnz = z;
   }
   
   static final String anames[] = {
      "first contact",
      "double trouble",
      "ambush",
      "junction",
      "sub junction",
      "oops",
      "waterloo",
      "stereo",
      "smallfield",
      "battlefield",
      "bad area",
      "area11",
      "area12",      // color code 242
      "area13",      // color code 241
      "area14",      // color code 240
      "area15",      // color code 239
      "area16",      // color code 238
      "area17",      // color code 237
      "area18",      // color code 236
      "",            // goal, code 235
      "",            // start point, code 234
   };
   
   String name() {
      return anames[iname%anames.length];
   }
   
   void addLight(int x,int z) {
      if (nlights<alightx.length-1) {
         alightx[nlights]=x;
         alightz[nlights]=z;
         nlights++;
      }
      else
      System.out.println("WARNING: too many lights in area "+iname);
   }
   
   void tryRemoveLight(int x,int z) {
      for (int i=0;i<nlights;i++)
       if (alightx[i]==x && alightz[i]==z) {
         alightx[i]=0;  // create a HOLE,
         alightz[i]=0;  // don't bother resorting.
         return;
       }
   }
}
