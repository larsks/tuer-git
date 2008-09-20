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

/*This class computes the vertex coordinates and texture coordinates
  of the tiles representing the level by using the map in Stahl's format. 
  It deletes useless "walls", keeps the useful walls and builds the floor 
  and the ceil. 
*/

package tools;

import java.awt.Image;
import java.awt.image.PixelGrabber;
import java.awt.Point;
import java.awt.Toolkit;
import java.beans.XMLDecoder;
import java.beans.XMLEncoder;
import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Vector;

import main.HealthPowerUpModel;
import main.HealthPowerUpModelBean;

public final class TilesGenerator{


    private List<PointPair> topWallsList;
    
    private List<PointPair> bottomWallsList;
    
    private List<PointPair> leftWallsList;
    
    private List<PointPair> rightWallsList;
    
    private List<PointPair> artTopWallsList;
    
    private List<PointPair> artBottomWallsList;
    
    private List<PointPair> artLeftWallsList;
    
    private List<PointPair> artRightWallsList;
    
    private List<Point> ceilTilesList;
    
    private List<Point> floorTilesList;
    
    private List<Point> specialCeilTilesList;
    
    private List<Point> specialFloorTilesList;
    
    private List<Point> unbreakableObjectsTilesList;
    
    private List<Point> lampsTilesList;
    
    private List<Point> chairsTilesList;
    
    private List<Point> flowerPotsTilesList;   
    
    private List<Point> tablesTilesList;
    
    private List<Point> vendingMachinesTilesList;
    
    private List<Point> bonsaiTreesTilesList;
    
    private Point initialPosition;
    
    private int[] worldMap;
    
    static final int tileSize=256;

    private static final int artCount=27;

    private static final int artPerTexture=16;

    private static final int artTextureSize=4;
    
    static final int factor=65536;    
    
    private static final int EMPTY=0;
    
    private static final int FIXED_AND_BREAKABLE_CHAIR=1;
    
    private static final int FIXED_AND_BREAKABLE_LIGHT=2;
    
    private static final int MOVING_AND_BREAKABLE=3;
    
    private static final int AVOIDABLE_AND_UNBREAKABLE=4;
    
    private static final int UNAVOIDABLE_AND_UNBREAKABLE=5;
    
    private static final int UNAVOIDABLE_AND_UNBREAKABLE_DIRTY=6;
    
    private static final int FIXED_AND_BREAKABLE_BIG=7;
    
    private static final int FIXED_AND_BREAKABLE_FLOWER=8;
    
    private static final int FIXED_AND_BREAKABLE_TABLE=9;
    
    private static final int FIXED_AND_BREAKABLE_BONSAI=10;      
    
    private static final int UNAVOIDABLE_AND_UNBREAKABLE_DOWN=11;
    
    private static final int UNAVOIDABLE_AND_UNBREAKABLE_LEFT=12;
    
    private static final int UNAVOIDABLE_AND_UNBREAKABLE_RIGHT=13;
    
    private static final int UNAVOIDABLE_AND_UNBREAKABLE_UP_DOWN=14;
    
    private static final int UNAVOIDABLE_AND_UNBREAKABLE_UP_LEFT=15;
    
    private static final int UNAVOIDABLE_AND_UNBREAKABLE_UP_RIGHT=16;
    
    private static final int UNAVOIDABLE_AND_UNBREAKABLE_UP_DOWN_LEFT=17;
    
    private static final int UNAVOIDABLE_AND_UNBREAKABLE_UP_DOWN_RIGHT=18;
    
    private static final int UNAVOIDABLE_AND_UNBREAKABLE_UP_LEFT_RIGHT=19;
               
    private static final int UNAVOIDABLE_AND_UNBREAKABLE_UP_DOWN_LEFT_RIGHT=20;
       
    private static final int UNAVOIDABLE_AND_UNBREAKABLE_DOWN_LEFT=21;
    
    private static final int UNAVOIDABLE_AND_UNBREAKABLE_DOWN_RIGHT=22;
    
    private static final int UNAVOIDABLE_AND_UNBREAKABLE_DOWN_LEFT_RIGHT=23;
    
    private static final int UNAVOIDABLE_AND_UNBREAKABLE_LEFT_RIGHT=24;
    
    private static final int UNAVOIDABLE_AND_UNBREAKABLE_UP=25;
    
    //dirty walls
    private static final int UNAVOIDABLE_AND_UNBREAKABLE_DOWN_DIRTY=26;
    
    private static final int UNAVOIDABLE_AND_UNBREAKABLE_LEFT_DIRTY=27;
    
    private static final int UNAVOIDABLE_AND_UNBREAKABLE_RIGHT_DIRTY=28;
    
    private static final int UNAVOIDABLE_AND_UNBREAKABLE_UP_DOWN_DIRTY=29;
    
    private static final int UNAVOIDABLE_AND_UNBREAKABLE_UP_LEFT_DIRTY=30;
    
    private static final int UNAVOIDABLE_AND_UNBREAKABLE_UP_RIGHT_DIRTY=31;
    
    private static final int UNAVOIDABLE_AND_UNBREAKABLE_UP_DOWN_LEFT_DIRTY=32;
    
    private static final int UNAVOIDABLE_AND_UNBREAKABLE_UP_DOWN_RIGHT_DIRTY=33;
    
    private static final int UNAVOIDABLE_AND_UNBREAKABLE_UP_LEFT_RIGHT_DIRTY=34;
               
    private static final int UNAVOIDABLE_AND_UNBREAKABLE_UP_DOWN_LEFT_RIGHT_DIRTY=35;
       
    private static final int UNAVOIDABLE_AND_UNBREAKABLE_DOWN_LEFT_DIRTY=36;
    
    private static final int UNAVOIDABLE_AND_UNBREAKABLE_DOWN_RIGHT_DIRTY=37;
    
    private static final int UNAVOIDABLE_AND_UNBREAKABLE_DOWN_LEFT_RIGHT_DIRTY=38;
    
    private static final int UNAVOIDABLE_AND_UNBREAKABLE_LEFT_RIGHT_DIRTY=39;
    
    private static final int UNAVOIDABLE_AND_UNBREAKABLE_UP_DIRTY=40;

    
    public TilesGenerator(String mapFilename,String tilesFilename,
            String rocketLauncherFilename,String binaryMapFilename,
            String botFilename,String unbreakableObjectFilename,
            String vendingMachineFilename,
            String lampFilename,
            String chairFilename,
            String flowerFilename,
            String tableFilename,
            String bonsaiFilename,
            String rocketFilename,
            String explosionFilename,
            String impactFilename,
            String healthPowerUpFilename,
            String healthPowerUpListFilename,
            String crosshairFilename,
            String sphericalBeastFilename){
        topWallsList=new Vector<PointPair>();   
        bottomWallsList=new Vector<PointPair>();
        leftWallsList=new Vector<PointPair>();
        rightWallsList=new Vector<PointPair>();
        artTopWallsList=new Vector<PointPair>();   
        artBottomWallsList=new Vector<PointPair>();
        artLeftWallsList=new Vector<PointPair>();
        artRightWallsList=new Vector<PointPair>();
        ceilTilesList=new Vector<Point>();
        floorTilesList=new Vector<Point>();
        specialCeilTilesList=new Vector<Point>();
        specialFloorTilesList=new Vector<Point>();
        unbreakableObjectsTilesList=new Vector<Point>();
        lampsTilesList=new Vector<Point>();
        chairsTilesList=new Vector<Point>();
        flowerPotsTilesList=new Vector<Point>();
        tablesTilesList=new Vector<Point>();
        vendingMachinesTilesList=new Vector<Point>();
        bonsaiTreesTilesList=new Vector<Point>();
        Image worldMapImage;
        worldMapImage=Toolkit.getDefaultToolkit().getImage(mapFilename);
        worldMap=new int[65536];	
	    try {(new PixelGrabber(worldMapImage,0,0,tileSize,tileSize,worldMap,0,tileSize)).grabPixels();}
        catch(InterruptedException i) 
	    {System.out.println("Pixel grabber interrupted");}
        worldMapImage.flush();
	    int tmp,r,g,b;
	    PointPair pair;	
	    //i : line index
	    //j : column index
	    byte[] collisionMap=new byte[256*256];
	    for(int i=0;i<256;i++)
             {tmp=i*256;
	          for(int j=0;j<256;j++)
	          //handle decorated walls as "special" walls here
	              {r=(worldMap[tmp+j]>>16)&0xFF;
		           g=(worldMap[tmp+j]>>8)&0xFF;
		           b=worldMap[tmp+j]&0xFF;		  
		           if(((r==0x00)&&(g==0x00)&&(b==0xFF))||((r==0xFF)&&(g==0xFF)&&(b==0x00)))
		               {//upper wall
		                pair=new PointPair(new Point(j,i),new Point(j+1,i));
		                if(removePointPair(bottomWallsList,pair))
			                removePointPair(artBottomWallsList,pair);
		                else
		                    if(!removePointPair(artBottomWallsList,pair))
		                        topWallsList.add((PointPair)pair.clone());
		                //use clone()!!!!!
		                //lower wall
		                pair=new PointPair(new Point(j,i+1),new Point(j+1,i+1));
		                if(removePointPair(topWallsList,pair))
		                    removePointPair(artTopWallsList,pair);
		                else
		                    if(!removePointPair(artTopWallsList,pair))			       
		                        bottomWallsList.add((PointPair)pair.clone());
		                //left side wall
		                pair=new PointPair(new Point(j,i),new Point(j,i+1));
		                if(removePointPair(rightWallsList,pair))
		                    removePointPair(artRightWallsList,pair);
		                else
		                    if(!removePointPair(artRightWallsList,pair))			       
		                        leftWallsList.add((PointPair)pair.clone());
		                //right side wall
		                pair=new PointPair(new Point(j+1,i),new Point(j+1,i+1));
		                if(removePointPair(leftWallsList,pair))
		                    removePointPair(artLeftWallsList,pair);
		                else
		                    if(!removePointPair(artLeftWallsList,pair))
		                        rightWallsList.add((PointPair)pair.clone());
		                collisionMap[tmp+j]=UNAVOIDABLE_AND_UNBREAKABLE;		           
		               }
		           else
		               if((r==0x00)&&(g==0x00)&&(b==0x64))
		                   {//upper wall
		                    pair=new PointPair(new Point(j,i),new Point(j+1,i));
		                    if(removePointPair(bottomWallsList,pair))
		                        removePointPair(artBottomWallsList,pair);
		                    else
		                        if(!removePointPair(artBottomWallsList,pair))
		                            artTopWallsList.add((PointPair)pair.clone());
		                    //use clone()!!!!!
		                    //lower wall
		                    pair=new PointPair(new Point(j,i+1),new Point(j+1,i+1));
		                    if(removePointPair(topWallsList,pair))
		                        removePointPair(artTopWallsList,pair);
		                    else
		                        if(!removePointPair(artTopWallsList,pair))			       
		                            artBottomWallsList.add((PointPair)pair.clone());
		                    //left side wall
		                    pair=new PointPair(new Point(j,i),new Point(j,i+1));
		                    if(removePointPair(rightWallsList,pair))
		                        removePointPair(artRightWallsList,pair);
		                    else
		                        if(!removePointPair(artRightWallsList,pair))			       
		                            artLeftWallsList.add((PointPair)pair.clone());
		                    //right side wall
		                    pair=new PointPair(new Point(j+1,i),new Point(j+1,i+1));
		                    if(removePointPair(leftWallsList,pair))
		                        removePointPair(artLeftWallsList,pair);
		                    else
		                        if(!removePointPair(artLeftWallsList,pair))
		                            artRightWallsList.add((PointPair)pair.clone());
		                    collisionMap[tmp+j]=UNAVOIDABLE_AND_UNBREAKABLE;			   	      
		                   }
		               else
			               {if((r==0x00)&&(g==0x00)&&(b==0x00))
		                        {//add floor and ceil
		                         specialFloorTilesList.add(new Point(j,i));
		                         specialFloorTilesList.add(new Point(j,i+1));
		                         specialFloorTilesList.add(new Point(j+1,i+1));
		                         specialFloorTilesList.add(new Point(j+1,i));
		                         specialCeilTilesList.add(new Point(j+1,i));
		                         specialCeilTilesList.add(new Point(j+1,i+1));
		                         specialCeilTilesList.add(new Point(j,i+1));
		                         specialCeilTilesList.add(new Point(j,i));
		                         collisionMap[tmp+j]=EMPTY;
		                        }
			                else
		                        {//add floor and ceil
			                     if((r==0x00)&&(g==0xFF)&&(b==0xFF))
				                     {collisionMap[tmp+j]=MOVING_AND_BREAKABLE;
				                      //collisionMap[tmp+j]=EMPTY;
				                      System.out.println("bot FF");
				                     }
				                 else
				                     if((r==0x00)&&(g==0xC8)&&(b==0xC8))
				                         {collisionMap[tmp+j]=MOVING_AND_BREAKABLE;
					                      //collisionMap[tmp+j]=EMPTY;
					                      System.out.println("bot C8");
					                     }
				                     else
				                         if((r==0x00)&&(g==0x00)&&(b==0xE0))
				                             {unbreakableObjectsTilesList.add(new Point(j,i));
				                             unbreakableObjectsTilesList.add(new Point(j,i+1));
				                             unbreakableObjectsTilesList.add(new Point(j+1,i+1));
				                             unbreakableObjectsTilesList.add(new Point(j+1,i));
				                             collisionMap[tmp+j]=AVOIDABLE_AND_UNBREAKABLE;
				                             System.out.println("unbreakable object");
				                             }
				                         else
				                             if((r==0x99)&&(g==0x00)&&(b==0x99))
				                                 {lampsTilesList.add(new Point(j,i));
				                                 lampsTilesList.add(new Point(j,i+1));
				                                 lampsTilesList.add(new Point(j+1,i+1));
				                                 lampsTilesList.add(new Point(j+1,i));
				                                 collisionMap[tmp+j]=FIXED_AND_BREAKABLE_LIGHT;
				                                 System.out.println("lamp");
				                                 }
				                             else
				                                 if((r==0x97)&&(g==0x00)&&(b==0x97))
				                                     {chairsTilesList.add(new Point(j,i));
				                                     chairsTilesList.add(new Point(j,i+1));
				                                     chairsTilesList.add(new Point(j+1,i+1));
				                                     chairsTilesList.add(new Point(j+1,i));
				                                     collisionMap[tmp+j]=FIXED_AND_BREAKABLE_CHAIR;
				                                     System.out.println("chair");
				                                     }
				                                 else
				                                     if((r==0xC8)&&(g==0x00)&&(b==0xC8))
				                                         {flowerPotsTilesList.add(new Point(j,i));
				                                         flowerPotsTilesList.add(new Point(j,i+1));
				                                         flowerPotsTilesList.add(new Point(j+1,i+1));
				                                         flowerPotsTilesList.add(new Point(j+1,i));
				                                         collisionMap[tmp+j]=FIXED_AND_BREAKABLE_FLOWER;
				                                         System.out.println("flower pot");
				                                         }
				                                     else
				                                         if((r==0x64)&&(g==0x00)&&(b==0x64))
				                                             {tablesTilesList.add(new Point(j,i));
				                                             tablesTilesList.add(new Point(j,i+1));
				                                             tablesTilesList.add(new Point(j+1,i+1));
				                                             tablesTilesList.add(new Point(j+1,i));
				                                             collisionMap[tmp+j]=FIXED_AND_BREAKABLE_TABLE;
				                                             System.out.println("table");
				                                             }
				                                         else
				                                             if((r==0x96)&&(g==0x00)&&(b==0x96))
				                                                 {vendingMachinesTilesList.add(new Point(j,i));
				                                                 vendingMachinesTilesList.add(new Point(j,i+1));
				                                                 vendingMachinesTilesList.add(new Point(j+1,i+1));
				                                                 vendingMachinesTilesList.add(new Point(j+1,i));
				                                                 //collisionMap[tmp+j]=FIXED_AND_BREAKABLE;
				                                                 collisionMap[tmp+j]=FIXED_AND_BREAKABLE_BIG;
				                                                 System.out.println("vending machine");
				                                                 }
				                                             else
				                                                 if((r==0x98)&&(g==0x00)&&(b==0x98))
				                                                     {bonsaiTreesTilesList.add(new Point(j,i));
				                                                     bonsaiTreesTilesList.add(new Point(j,i+1));
				                                                     bonsaiTreesTilesList.add(new Point(j+1,i+1));
				                                                     bonsaiTreesTilesList.add(new Point(j+1,i));
				                                                     collisionMap[tmp+j]=FIXED_AND_BREAKABLE_BONSAI;
				                                                     System.out.println("bonsai tree");
				                                                     }
				                                                 else
				                                                     if((r==0xFF)&&(g==0x00)&&(b==0x00))
				                                                         {initialPosition=new Point(j,i);
				                                                          collisionMap[tmp+j]=EMPTY;
				                                                          System.out.println("initial point");
				                                                         }
				                                                     else
				                                                         if((r==0x64)&&(g==0x64)&&(b==0x64))
				                                                             {collisionMap[tmp+j]=EMPTY;
				                                                              System.out.println("respawn point");
				                                                             }
				                                                         else
				                                                             {//empty area
				                                                              collisionMap[tmp+j]=EMPTY;
				                                                             }
			                     floorTilesList.add(new Point(j,i));
			                     floorTilesList.add(new Point(j,i+1));
			                     floorTilesList.add(new Point(j+1,i+1));
			                     floorTilesList.add(new Point(j+1,i));
			                     ceilTilesList.add(new Point(j+1,i));
			                     ceilTilesList.add(new Point(j+1,i+1));
			                     ceilTilesList.add(new Point(j,i+1));
			                     ceilTilesList.add(new Point(j,i));
		                        }
			               }
	              }
             }	
	    boolean found;
	    for(int i=0;i<256;i++)
	        {tmp=i*256;
	         for(int j=0;j<256;j++)
	             if(collisionMap[tmp+j]==UNAVOIDABLE_AND_UNBREAKABLE)
	                 {found=false;
	                 //upper wall
	                 pair=new PointPair(new Point(j,i),new Point(j+1,i));
	                 for(PointPair po:topWallsList)
	                     if(pair.equals(po))
	                         {found=true;
	                          break;
	                         }
	                if(found)
	                    {switch(collisionMap[tmp+j])
	                        {case UNAVOIDABLE_AND_UNBREAKABLE:
	                        {collisionMap[tmp+j]=UNAVOIDABLE_AND_UNBREAKABLE_UP;
	                        break;
	                        } 			        
	                        case UNAVOIDABLE_AND_UNBREAKABLE_DOWN:
	                        {collisionMap[tmp+j]=UNAVOIDABLE_AND_UNBREAKABLE_UP_DOWN;
	                        break;
	                        }
	                        case UNAVOIDABLE_AND_UNBREAKABLE_LEFT:
	                        {collisionMap[tmp+j]=UNAVOIDABLE_AND_UNBREAKABLE_UP_LEFT;
	                        break;
	                        }
	                        case UNAVOIDABLE_AND_UNBREAKABLE_RIGHT:
	                        {collisionMap[tmp+j]=UNAVOIDABLE_AND_UNBREAKABLE_UP_RIGHT;
	                        break;
	                        }
	                        case UNAVOIDABLE_AND_UNBREAKABLE_DOWN_LEFT:
	                        {collisionMap[tmp+j]=UNAVOIDABLE_AND_UNBREAKABLE_UP_DOWN_LEFT;
	                        break;
	                        }
	                        case UNAVOIDABLE_AND_UNBREAKABLE_DOWN_RIGHT:
	                        {collisionMap[tmp+j]=UNAVOIDABLE_AND_UNBREAKABLE_UP_DOWN_RIGHT;
	                        break;
	                        }
	                        case UNAVOIDABLE_AND_UNBREAKABLE_LEFT_RIGHT:
	                        {collisionMap[tmp+j]=UNAVOIDABLE_AND_UNBREAKABLE_UP_LEFT_RIGHT;
	                        break;
	                        }
	                        case UNAVOIDABLE_AND_UNBREAKABLE_DOWN_LEFT_RIGHT:
	                        {collisionMap[tmp+j]=UNAVOIDABLE_AND_UNBREAKABLE_UP_DOWN_LEFT_RIGHT;
	                        break;
	                        }				   
	                        }
	                    }
	                found=false;  
	                //lower wall
	                pair=new PointPair(new Point(j,i+1),new Point(j+1,i+1));	              
	                for(PointPair po:bottomWallsList)
	                    if(pair.equals(po))
	                        {found=true;
	                        break;
	                        }
		      if(found)
		          {switch(collisionMap[tmp+j])
			       {case UNAVOIDABLE_AND_UNBREAKABLE:
			            {collisionMap[tmp+j]=UNAVOIDABLE_AND_UNBREAKABLE_DOWN;
				     break;
				    } 			        
			        case UNAVOIDABLE_AND_UNBREAKABLE_UP:
				    {collisionMap[tmp+j]=UNAVOIDABLE_AND_UNBREAKABLE_UP_DOWN;
				     break;
				    }
				case UNAVOIDABLE_AND_UNBREAKABLE_LEFT:
				    {collisionMap[tmp+j]=UNAVOIDABLE_AND_UNBREAKABLE_DOWN_LEFT;
				     break;
				    }
				case UNAVOIDABLE_AND_UNBREAKABLE_RIGHT:
				    {collisionMap[tmp+j]=UNAVOIDABLE_AND_UNBREAKABLE_DOWN_RIGHT;
				     break;
				    }
				case UNAVOIDABLE_AND_UNBREAKABLE_UP_LEFT:
				    {collisionMap[tmp+j]=UNAVOIDABLE_AND_UNBREAKABLE_UP_DOWN_LEFT;
				     break;
				    }
				case UNAVOIDABLE_AND_UNBREAKABLE_UP_RIGHT:
				    {collisionMap[tmp+j]=UNAVOIDABLE_AND_UNBREAKABLE_UP_DOWN_RIGHT;
				     break;
				    }
				case UNAVOIDABLE_AND_UNBREAKABLE_LEFT_RIGHT:
				    {collisionMap[tmp+j]=UNAVOIDABLE_AND_UNBREAKABLE_DOWN_LEFT_RIGHT;
				     break;
				    }
				case UNAVOIDABLE_AND_UNBREAKABLE_UP_LEFT_RIGHT:
				    {collisionMap[tmp+j]=UNAVOIDABLE_AND_UNBREAKABLE_UP_DOWN_LEFT_RIGHT;
				     break;
				    }				   
			       }
			  }      			      
		      found=false;
		      //left side wall
		      pair=new PointPair(new Point(j,i),new Point(j,i+1));
		      for(PointPair po:leftWallsList)
		          if(pair.equals(po))
			      {found=true;
			       break;
			      }
		      if(found)
		          {switch(collisionMap[tmp+j])
			       {case UNAVOIDABLE_AND_UNBREAKABLE:
			            {collisionMap[tmp+j]=UNAVOIDABLE_AND_UNBREAKABLE_LEFT;
				     break;
				    } 			        
			        case UNAVOIDABLE_AND_UNBREAKABLE_UP:
				    {collisionMap[tmp+j]=UNAVOIDABLE_AND_UNBREAKABLE_UP_LEFT;
				     break;
				    }
				case UNAVOIDABLE_AND_UNBREAKABLE_DOWN:
				    {collisionMap[tmp+j]=UNAVOIDABLE_AND_UNBREAKABLE_DOWN_LEFT;
				     break;
				    }				
				case UNAVOIDABLE_AND_UNBREAKABLE_RIGHT:
				    {collisionMap[tmp+j]=UNAVOIDABLE_AND_UNBREAKABLE_LEFT_RIGHT;
				     break;
				    }
				case UNAVOIDABLE_AND_UNBREAKABLE_UP_DOWN:
				    {collisionMap[tmp+j]=UNAVOIDABLE_AND_UNBREAKABLE_UP_DOWN_LEFT;
				     break;
				    }
				case UNAVOIDABLE_AND_UNBREAKABLE_UP_RIGHT:
				    {collisionMap[tmp+j]=UNAVOIDABLE_AND_UNBREAKABLE_UP_LEFT_RIGHT;
				     break;
				    }				
				case UNAVOIDABLE_AND_UNBREAKABLE_DOWN_RIGHT:
				    {collisionMap[tmp+j]=UNAVOIDABLE_AND_UNBREAKABLE_DOWN_LEFT_RIGHT;
				     break;
				    }				
				case UNAVOIDABLE_AND_UNBREAKABLE_UP_DOWN_RIGHT:
				    {collisionMap[tmp+j]=UNAVOIDABLE_AND_UNBREAKABLE_UP_DOWN_LEFT_RIGHT;
				     break;
				    }				   
			       }
			  }
		      found=false;
		      //right side wall
		      pair=new PointPair(new Point(j+1,i),new Point(j+1,i+1)); 
		      for(PointPair po:rightWallsList)
		          if(pair.equals(po))
			      {found=true;
			       break;
			      }
		      if(found)
		          {switch(collisionMap[tmp+j])
			       {case UNAVOIDABLE_AND_UNBREAKABLE:
			            {collisionMap[tmp+j]=UNAVOIDABLE_AND_UNBREAKABLE_RIGHT;
				     break;
				    } 			        
			        case UNAVOIDABLE_AND_UNBREAKABLE_UP:
				    {collisionMap[tmp+j]=UNAVOIDABLE_AND_UNBREAKABLE_UP_RIGHT;
				     break;
				    }
				case UNAVOIDABLE_AND_UNBREAKABLE_DOWN:
				    {collisionMap[tmp+j]=UNAVOIDABLE_AND_UNBREAKABLE_DOWN_RIGHT;
				     break;
				    }				
				case UNAVOIDABLE_AND_UNBREAKABLE_LEFT:
				    {collisionMap[tmp+j]=UNAVOIDABLE_AND_UNBREAKABLE_LEFT_RIGHT;
				     break;
				    }
				case UNAVOIDABLE_AND_UNBREAKABLE_UP_DOWN:
				    {collisionMap[tmp+j]=UNAVOIDABLE_AND_UNBREAKABLE_UP_DOWN_RIGHT;
				     break;
				    }
				case UNAVOIDABLE_AND_UNBREAKABLE_UP_LEFT:
				    {collisionMap[tmp+j]=UNAVOIDABLE_AND_UNBREAKABLE_UP_LEFT_RIGHT;
				     break;
				    }				
				case UNAVOIDABLE_AND_UNBREAKABLE_DOWN_LEFT:
				    {collisionMap[tmp+j]=UNAVOIDABLE_AND_UNBREAKABLE_DOWN_LEFT_RIGHT;
				     break;
				    }				
				case UNAVOIDABLE_AND_UNBREAKABLE_UP_DOWN_LEFT:
				    {collisionMap[tmp+j]=UNAVOIDABLE_AND_UNBREAKABLE_UP_DOWN_LEFT_RIGHT;
				     break;
				    }				   
			       }
			  }
		     }
	    }
        /*System.out.println("art count = "+pb);*/
	//write the correct data in the file
	    try 
	        {File file=new File(tilesFilename);
	         if(!file.exists())
	             file.createNewFile();
	         /*for each point, associate the good texture coordinates*/   
	         //use DataOutputStream and writeInt() for the definitive implementation
	         DataOutputStream out=new DataOutputStream(new BufferedOutputStream(new FileOutputStream(file)));
	         //write the number of point of each entity (upper walls, lower walls, left side walls, right side walls, floor and ceil)
	         out.writeInt(topWallsList.size()*4);
	         out.writeInt(bottomWallsList.size()*4);
	         out.writeInt(leftWallsList.size()*4);
	         out.writeInt(rightWallsList.size()*4);
	         out.writeInt(floorTilesList.size()+specialFloorTilesList.size());
	         out.writeInt(ceilTilesList.size()+specialCeilTilesList.size());
	         //dispatch art size so that a texture contains pow(2,n) artworks
	         int totalArtVertexCount=(artTopWallsList.size()*4)+(artBottomWallsList.size()*4)
	         +(artLeftWallsList.size()*4)+(artRightWallsList.size()*4);
	         int maxArtVertexCountPerTexture=(int)Math.pow(2.0d,Math.floor(Math.log(totalArtVertexCount/2)/Math.log(2.0d)));
	         out.writeInt(maxArtVertexCountPerTexture);
             out.writeInt((artLeftWallsList.size()*4)+(artRightWallsList.size()*4)-maxArtVertexCountPerTexture);
             out.writeInt(maxArtVertexCountPerTexture);
             out.writeInt((artBottomWallsList.size()*4)+(artTopWallsList.size()*4)-maxArtVertexCountPerTexture);
	         //use factor
	         //build upper walls (last, first) (y=-0.5 or 0.5)
	         for(PointPair p:topWallsList)
	             {writeQuadPrimitive(out,0.0f,0.25f,(float)(factor*p.getLast().getX()),0.5f*factor,(float)(factor*p.getLast().getY()),
	                     0.0f,0.5f,(float)(factor*p.getLast().getX()),-0.5f*factor,(float)(factor*p.getLast().getY()),
	                     0.25f,0.5f,(float)(factor*p.getFirst().getX()),-0.5f*factor,(float)(factor*p.getFirst().getY()),
	                     0.25f,0.25f,(float)(factor*p.getFirst().getX()),0.5f*factor,(float)(factor*p.getFirst().getY()));	       
	             }
	         //build lower walls (first, last) (y=-0.5 or 0.5)
	         for(PointPair p:bottomWallsList)
	             {writeQuadPrimitive(out,0.0f,0.25f,(float)(factor*p.getFirst().getX()),0.5f*factor,(float)(factor*p.getFirst().getY()),
	                     0.0f,0.5f,(float)(factor*p.getFirst().getX()),-0.5f*factor,(float)(factor*p.getFirst().getY()),
	                     0.25f,0.5f,(float)(factor*p.getLast().getX()),-0.5f*factor,(float)(factor*p.getLast().getY()),
	                     0.25f,0.25f,(float)(factor*p.getLast().getX()),0.5f*factor,(float)(factor*p.getLast().getY()));
	             }
	         //build left side walls (first, last) (y=-0.5 or 0.5)
	         for(PointPair p:leftWallsList)
	             {writeQuadPrimitive(out,0.0f,0.25f,(float)(factor*p.getFirst().getX()),0.5f*factor,(float)(factor*p.getFirst().getY()),
	                     0.0f,0.5f,(float)(factor*p.getFirst().getX()),-0.5f*factor,(float)(factor*p.getFirst().getY()),
	                     0.25f,0.5f,(float)(factor*p.getLast().getX()),-0.5f*factor,(float)(factor*p.getLast().getY()),
	                     0.25f,0.25f,(float)(factor*p.getLast().getX()),0.5f*factor,(float)(factor*p.getLast().getY()));		       
	             }
	         //build right side walls (last, first) (y=-0.5 or 0.5)
	         for(PointPair p:rightWallsList)
	             {writeQuadPrimitive(out,0.0f,0.25f,(float)(factor*p.getLast().getX()),0.5f*factor,(float)(factor*p.getLast().getY()),
	                     0.0f,0.5f,(float)(factor*p.getLast().getX()),-0.5f*factor,(float)(factor*p.getLast().getY()),
	                     0.25f,0.5f,(float)(factor*p.getFirst().getX()),-0.5f*factor,(float)(factor*p.getFirst().getY()),
	                     0.25f,0.25f,(float)(factor*p.getFirst().getX()),0.5f*factor,(float)(factor*p.getFirst().getY()));		       
	             }		    
	         //build floor and ceil (y=-0.5 on the floor, 0.5 on the ceiling)
	         int k=0;
	         for(Point p:floorTilesList)
	             {if(k%4==0)
	                 writeTextureCoord(out,0.0f,0.75f);
	             else
	                 if(k%4==1)
	                     writeTextureCoord(out,0.0f,0.5f);
	                 else
	                     if(k%4==2)
	                         writeTextureCoord(out,0.25f,0.5f);
	                     else			     
	                         writeTextureCoord(out,0.25f,0.75f);
	             k++;
	             //writeNormal(out,0.0f,1.0f,0.0f);
	             writePoint(out,factor*p.getX(),-0.5*factor,factor*p.getY());
	             }
	         k=0;
	         for(Point p:specialFloorTilesList)
	             {if(k%4==0)
	                 writeTextureCoord(out,0.0f,0.75f);
	             else
	                 if(k%4==1)
	                     writeTextureCoord(out,0.0f,0.5f);
	                 else
	                     if(k%4==2)
	                         writeTextureCoord(out,0.25f,0.5f);
	                     else			     
	                         writeTextureCoord(out,0.25f,0.75f);
	             k++;
	             //writeNormal(out,0.0f,1.0f,0.0f);
	             writePoint(out,factor*p.getX(),-0.5*factor,factor*p.getY());
	             }
	         k=0;
	         for(Point p:ceilTilesList)
	             {if(k%4==0)
	                 writeTextureCoord(out,0,1);
	             else
	                 if(k%4==1)
	                     writeTextureCoord(out,0,0.75f);
	                 else
	                     if(k%4==2)
	                         writeTextureCoord(out,0.25f,0.75f);
	                     else			     
	                         writeTextureCoord(out,0.25f,1);
	             k++;
	             //writeNormal(out,0.0f,-1.0f,0.0f);
	             writePoint(out,factor*p.getX(),0.5*factor,factor*p.getY());
	             }		  
	         k=0;
	         for(Point p:specialCeilTilesList)
	             {if(k%4==0)
	                 writeTextureCoord(out,0.25f,1);
	             else
	                 if(k%4==1)
	                     writeTextureCoord(out,0.25f,0.75f);
	                 else
	                     if(k%4==2)
	                         writeTextureCoord(out,0.5f,0.75f);
	                     else			     
	                         writeTextureCoord(out,0.5f,1);
	             k++;
	             //writeNormal(out,0.0f,-1.0f,0.0f);
	             writePoint(out,factor*p.getX(),0.5*factor,factor*p.getY());
	             }	     
	         int artIndex=0;//index of the artworks to display
	         int texturePos=0;//position inside the texture
	         float x1,x2,y1,y2;//texture coordinates
	         float q=1.0f/((float)artTextureSize);
	         //build upper walls (last, first) (y=-0.5 or 0.5)
	         for(PointPair p:artTopWallsList)
	             {x1=texturePos%artTextureSize;
	              y2=artTextureSize-(texturePos/artTextureSize);
	              x2=x1+1;
	              y1=y2-1;                                  		  
	              writeQuadPrimitive(out,x2*q,y2*q,(float)(factor*p.getLast().getX()),0.5f*factor,(float)(factor*p.getLast().getY()),
	                     x2*q,y1*q,(float)(factor*p.getLast().getX()),-0.5f*factor,(float)(factor*p.getLast().getY()),
	                     x1*q,y1*q,(float)(factor*p.getFirst().getX()),-0.5f*factor,(float)(factor*p.getFirst().getY()),
	                     x1*q,y2*q,(float)(factor*p.getFirst().getX()),0.5f*factor,(float)(factor*p.getFirst().getY()));		  		  		  
	              artIndex=(artIndex+1)%artCount;
	              texturePos=artIndex%artPerTexture;	       
	             }
	         //build lower walls (first, last) (y=-0.5 or 0.5)
	         for(PointPair p:artBottomWallsList)
	             {x1=texturePos%artTextureSize;
	              y2=artTextureSize-(texturePos/artTextureSize);
	              x2=x1+1;
	              y1=y2-1;
	              writeQuadPrimitive(out,x1*q,y2*q,(float)(factor*p.getFirst().getX()),0.5f*factor,(float)(factor*p.getFirst().getY()),
	                     x1*q,y1*q,(float)(factor*p.getFirst().getX()),-0.5f*factor,(float)(factor*p.getFirst().getY()),
	                     x2*q,y1*q,(float)(factor*p.getLast().getX()),-0.5f*factor,(float)(factor*p.getLast().getY()),
	                     x2*q,y2*q,(float)(factor*p.getLast().getX()),0.5f*factor,(float)(factor*p.getLast().getY()));

	              artIndex=(artIndex+1)%artCount;
	              texturePos=artIndex%artPerTexture;                    	       
	             }
	         //build left side walls (first, last) (y=-0.5 or 0.5)
	         for(PointPair p:artLeftWallsList)
	             {x1=texturePos%artTextureSize;
	             y2=artTextureSize-(texturePos/artTextureSize);
	             x2=x1+1;
	             y1=y2-1;                                   		  
	             writeQuadPrimitive(out,x1*q,y2*q,(float)(factor*p.getFirst().getX()),0.5f*factor,(float)(factor*p.getFirst().getY()),
	                     x1*q,y1*q,(float)(factor*p.getFirst().getX()),-0.5f*factor,(float)(factor*p.getFirst().getY()),
	                     x2*q,y1*q,(float)(factor*p.getLast().getX()),-0.5f*factor,(float)(factor*p.getLast().getY()),
	                     x2*q,y2*q,(float)(factor*p.getLast().getX()),0.5f*factor,(float)(factor*p.getLast().getY()));		                     
	             artIndex=(artIndex+1)%artCount;
	             texturePos=artIndex%artPerTexture;	       
	             }
	         //build right side walls (last, first) (y=-0.5 or 0.5)
	         for(PointPair p:artRightWallsList)
	             {x1=texturePos%artTextureSize;
	              y2=artTextureSize-(texturePos/artTextureSize);
	              x2=x1+1;
	              y1=y2-1;
	              writeQuadPrimitive(out,x2*q,y2*q,(float)(factor*p.getLast().getX()),0.5f*factor,(float)(factor*p.getLast().getY()),
	                     x2*q,y1*q,(float)(factor*p.getLast().getX()),-0.5f*factor,(float)(factor*p.getLast().getY()),
	                     x1*q,y1*q,(float)(factor*p.getFirst().getX()),-0.5f*factor,(float)(factor*p.getFirst().getY()),
	                     x1*q,y2*q,(float)(factor*p.getFirst().getX()),0.5f*factor,(float)(factor*p.getFirst().getY()));		                       
	              artIndex=(artIndex+1)%artCount;
	              texturePos=artIndex%artPerTexture;	       
	             }       
	         //write the collision map
	         out.write(collisionMap,0,65536);     
	         //put the initial point
	         out.writeInt(initialPosition.x);
	         out.writeInt(initialPosition.y);
	         //don't  put the respawn points because they are no more used    	         
	         out.flush();
	         out.close();
	         writeUnbreakableObject(unbreakableObjectFilename);            
             writeVendingMachine(vendingMachineFilename);                       
             writeLamp(lampFilename);                        
             writeChair(chairFilename);                                  
             writeFlower(flowerFilename);         
             writeTable(tableFilename);                      
             writeBonsai(bonsaiFilename);
	         writeBot(botFilename);	         
             writeRocketLauncher(rocketLauncherFilename);
             writeRocket(rocketFilename);
             writeExplosion(explosionFilename);
             writeImpact(impactFilename);
             writeHealthPowerUp(healthPowerUpFilename);
             writeBinaryMap(binaryMapFilename,worldMap);
             writeHealthPowerUpList(healthPowerUpListFilename);
             writeCrosshair(crosshairFilename);
             writeSphericalBeast(sphericalBeastFilename);
	        }
	     catch(IOException ioe)
	     {ioe.printStackTrace();}
	     //the cells generator uses a cartesian reference mark
	     //whereas the tiles generator inverts left and right
	     //that is why you have to invert it
	     CellsGenerator.generate(topWallsList,bottomWallsList,
	             rightWallsList,leftWallsList,artTopWallsList,
	             artBottomWallsList,artRightWallsList,artLeftWallsList);
    }      
    
    
    public final static void writeNormal(DataOutputStream out,float nx,float ny,float nz)throws IOException{
        out.writeFloat(nx);
	    out.writeFloat(ny);
	    out.writeFloat(nz);	
    }
    
    public final static void writePoint(DataOutputStream out,float x,float y,float z)throws IOException{
        out.writeFloat(x);
	    out.writeFloat(y);
	    out.writeFloat(z);	
    }
    
    private final static void writePoint(DataOutputStream out,double x,double y,double z)throws IOException{
        writePoint(out,(float)x,(float)y,(float)z);
    }
    
    private final static void writeTextureCoord(DataOutputStream out,float u,float v)throws IOException{
        out.writeFloat(u);
	    out.writeFloat(v);		
    }
    
    public final static void writePrimitive(DataOutputStream out,float u,float v,float nx,float ny,float nz,float x,float y,float z)throws IOException{
        writeTextureCoord(out,u,v);
	    //writeNormal(out,nx,ny,nz);
	    writePoint(out,x,y,z);
    }
    
    public final static void writeQuadPrimitive(DataOutputStream out,float u1,float v1,float x1,float y1,float z1,
                                                               float u2,float v2,float x2,float y2,float z2,
							       float u3,float v3,float x3,float y3,float z3,
							       float u4,float v4,float x4,float y4,float z4)throws IOException{
        float[] n=getQuadNormal(x1,y1,z1,x2,y2,z2,x3,y3,z3,x4,y4,z4);	
	    writePrimitive(out,u1,v1,n[0],n[1],n[2],x1,y1,z1);
	    writePrimitive(out,u2,v2,n[0],n[1],n[2],x2,y2,z2);
	    writePrimitive(out,u3,v3,n[0],n[1],n[2],x3,y3,z3);
	    writePrimitive(out,u4,v4,n[0],n[1],n[2],x4,y4,z4);
    }
    
    public static float[] getQuadNormal(float x1,float y1,float z1,float x2,float y2,float z2,float x3,float y3,float z3,float x4,float y4,float z4){
        float[] normal=new float[3];
	    //compute the normal
	
	    //normalize it
        
	    return(normal);
    }
       
    /**
     * write the coordinates to draw a regular octagon centered on zero
     * @param out : stream to write the data
     * @param inradius : inradius of the octagon
     * @param z : applicate of the octagon
     * @param u1
     * @param v1
     * @param u2
     * @param v2
     * @param u3
     * @param v3
     * @param u4
     * @param v4
     * @param u5
     * @param v5
     * @param u6
     * @param v6
     * @param u7
     * @param v7
     * @param u8
     * @param v8
     */
    private final static float[][] writeZeroCenteredRegularOctagonAroundZ(DataOutputStream out,float inradius,float z,
    		float u1,float v1,float u2,float v2,float u3,float v3,float u4,float v4,
    		float u5,float v5,float u6,float v6,float u7,float v7,float u8,float v8)throws IOException{
    	float[][] pArray=computeZeroCenteredRegularOctagonAroundZ(inradius,z);
    	final float[] p1=pArray[0];
    	final float[] p2=pArray[1];
    	final float[] p3=pArray[2];
    	final float[] p4=pArray[3];
    	final float[] p5=pArray[4];
    	final float[] p6=pArray[5];
    	final float[] p7=pArray[6];
    	final float[] p8=pArray[7];
    	writeQuadPrimitive(out,u1,v1,p1[0],p1[1],p1[2],u2,v2,p2[0],p2[1],p2[2],u3,v3,p3[0],p3[1],p3[2],u4,v4,p4[0],p4[1],p4[2]);
    	writeQuadPrimitive(out,u1,v1,p1[0],p1[1],p1[2],u4,v4,p4[0],p4[1],p4[2],u5,v5,p5[0],p5[1],p5[2],u8,v8,p8[0],p8[1],p8[2]);
    	writeQuadPrimitive(out,u5,v5,p5[0],p5[1],p5[2],u6,v6,p6[0],p6[1],p6[2],u7,v7,p7[0],p7[1],p7[2],u8,v8,p8[0],p8[1],p8[2]);
    	return(pArray);
    }
    
    /**
     * write the coordinates to draw a regular octagon centered on zero
     * @param out : stream to write the data
     * @param inradius : inradius of the octagon
     * @param y : ordinate of the octagon
     * @param u1
     * @param v1
     * @param u2
     * @param v2
     * @param u3
     * @param v3
     * @param u4
     * @param v4
     * @param u5
     * @param v5
     * @param u6
     * @param v6
     * @param u7
     * @param v7
     * @param u8
     * @param v8
     */
    private final static float[][] writeZeroCenteredRegularOctagonAroundY(DataOutputStream out,float inradius,float y,
            float u1,float v1,float u2,float v2,float u3,float v3,float u4,float v4,
            float u5,float v5,float u6,float v6,float u7,float v7,float u8,float v8)throws IOException{
        float[][] pArray=computeZeroCenteredRegularOctagonAroundY(inradius,y);
        final float[] p1=pArray[0];
        final float[] p2=pArray[1];
        final float[] p3=pArray[2];
        final float[] p4=pArray[3];
        final float[] p5=pArray[4];
        final float[] p6=pArray[5];
        final float[] p7=pArray[6];
        final float[] p8=pArray[7];
        writeQuadPrimitive(out,u1,v1,p1[0],p1[1],p1[2],u2,v2,p2[0],p2[1],p2[2],u3,v3,p3[0],p3[1],p3[2],u4,v4,p4[0],p4[1],p4[2]);
        writeQuadPrimitive(out,u1,v1,p1[0],p1[1],p1[2],u4,v4,p4[0],p4[1],p4[2],u5,v5,p5[0],p5[1],p5[2],u8,v8,p8[0],p8[1],p8[2]);
        writeQuadPrimitive(out,u5,v5,p5[0],p5[1],p5[2],u6,v6,p6[0],p6[1],p6[2],u7,v7,p7[0],p7[1],p7[2],u8,v8,p8[0],p8[1],p8[2]);
        return(pArray);
    }
       
    /**
     * write the coordinates to draw a regular octagon centered on zero
     * BUT on the reverse order
     * @param out : stream to write the data
     * @param inradius : inradius of the octagon
     * @param y : ordinate of the octagon
     * @param u1
     * @param v1
     * @param u2
     * @param v2
     * @param u3
     * @param v3
     * @param u4
     * @param v4
     * @param u5
     * @param v5
     * @param u6
     * @param v6
     * @param u7
     * @param v7
     * @param u8
     * @param v8
     */
    private final static float[][] writeZeroCenteredReversedRegularOctagonAroundY(DataOutputStream out,float inradius,float y,
            float u1,float v1,float u2,float v2,float u3,float v3,float u4,float v4,
            float u5,float v5,float u6,float v6,float u7,float v7,float u8,float v8)throws IOException{
        float[][] pArray=computeZeroCenteredRegularOctagonAroundY(inradius,y);
        final float[] p1=pArray[7];
        final float[] p2=pArray[6];
        final float[] p3=pArray[5];
        final float[] p4=pArray[4];
        final float[] p5=pArray[3];
        final float[] p6=pArray[2];
        final float[] p7=pArray[1];
        final float[] p8=pArray[0];
        writeQuadPrimitive(out,u1,v1,p1[0],p1[1],p1[2],u2,v2,p2[0],p2[1],p2[2],u3,v3,p3[0],p3[1],p3[2],u4,v4,p4[0],p4[1],p4[2]);
        writeQuadPrimitive(out,u1,v1,p1[0],p1[1],p1[2],u4,v4,p4[0],p4[1],p4[2],u5,v5,p5[0],p5[1],p5[2],u8,v8,p8[0],p8[1],p8[2]);
        writeQuadPrimitive(out,u5,v5,p5[0],p5[1],p5[2],u6,v6,p6[0],p6[1],p6[2],u7,v7,p7[0],p7[1],p7[2],u8,v8,p8[0],p8[1],p8[2]);
        return(pArray);
    }
    
    /**
     * write the coordinates to draw a regular octagon centered on zero
     * BUT on the reverse order
     * @param out : stream to write the data
     * @param inradius : inradius of the octagon
     * @param z : applicate of the octagon
     * @param u1
     * @param v1
     * @param u2
     * @param v2
     * @param u3
     * @param v3
     * @param u4
     * @param v4
     * @param u5
     * @param v5
     * @param u6
     * @param v6
     * @param u7
     * @param v7
     * @param u8
     * @param v8
     */
    private final static float[][] writeZeroCenteredReversedRegularOctagonAroundZ(DataOutputStream out,float inradius,float z,
    		float u1,float v1,float u2,float v2,float u3,float v3,float u4,float v4,
    		float u5,float v5,float u6,float v6,float u7,float v7,float u8,float v8)throws IOException{
    	float[][] pArray=computeZeroCenteredRegularOctagonAroundZ(inradius,z);
    	final float[] p1=pArray[7];
    	final float[] p2=pArray[6];
    	final float[] p3=pArray[5];
    	final float[] p4=pArray[4];
    	final float[] p5=pArray[3];
    	final float[] p6=pArray[2];
    	final float[] p7=pArray[1];
    	final float[] p8=pArray[0];
    	writeQuadPrimitive(out,u1,v1,p1[0],p1[1],p1[2],u2,v2,p2[0],p2[1],p2[2],u3,v3,p3[0],p3[1],p3[2],u4,v4,p4[0],p4[1],p4[2]);
    	writeQuadPrimitive(out,u1,v1,p1[0],p1[1],p1[2],u4,v4,p4[0],p4[1],p4[2],u5,v5,p5[0],p5[1],p5[2],u8,v8,p8[0],p8[1],p8[2]);
    	writeQuadPrimitive(out,u5,v5,p5[0],p5[1],p5[2],u6,v6,p6[0],p6[1],p6[2],u7,v7,p7[0],p7[1],p7[2],u8,v8,p8[0],p8[1],p8[2]);
    	return(pArray);
    }
    
    private final static void writeLinkBetweenTwoZeroCenteredRegularOctagons(DataOutputStream out,float[][] back,float[][] front,
    		float u1,float v1,float u2,float v2,float u3,float v3,float u4,float v4)throws IOException{   	
    	for(int i=0;i<8;i++)
    	    writeQuadPrimitive(out,u1,v1,back[i][0],back[i][1],back[i][2],u2,v2,front[i][0],front[i][1],front[i][2],u3,v3,front[(i+1)%8][0],front[(i+1)%8][1],front[(i+1)%8][2],u4,v4,back[(i+1)%8][0],back[(i+1)%8][1],back[(i+1)%8][2]);
    }
    
    /**
     * 
     * @param inradius
     * @param z
     */
    private final static float[][] computeZeroCenteredRegularOctagonAroundZ(float inradius,float z){
    	final float halfSideLength = inradius/2.4142f;
    	final float[] p1={inradius,halfSideLength,z};
    	final float[] p2={halfSideLength,inradius,z};
    	final float[] p3={-halfSideLength,inradius,z};
    	final float[] p4={-inradius,halfSideLength,z};
    	final float[] p5={-inradius,-halfSideLength,z};
    	final float[] p6={-halfSideLength,-inradius,z};
    	final float[] p7={halfSideLength,-inradius,z};
    	final float[] p8={inradius,-halfSideLength,z};
    	return(new float[][]{p1,p2,p3,p4,p5,p6,p7,p8});
    }
    
    /**
     * 
     * @param inradius
     * @param y
     */
    private final static float[][] computeZeroCenteredRegularOctagonAroundY(float inradius,float y){
        final float halfSideLength = inradius/2.4142f;
        final float[] p1={inradius,y,halfSideLength};
        final float[] p2={halfSideLength,y,inradius};
        final float[] p3={-halfSideLength,y,inradius};
        final float[] p4={-inradius,y,halfSideLength};
        final float[] p5={-inradius,y,-halfSideLength};
        final float[] p6={-halfSideLength,y,-inradius};
        final float[] p7={halfSideLength,y,-inradius};
        final float[] p8={inradius,y,-halfSideLength};
        return(new float[][]{p1,p2,p3,p4,p5,p6,p7,p8});
    }
    
    private final static void writeHeader(DataOutputStream out,int primitiveCount,int valuesPerPrimitive) throws IOException{
        out.writeInt(primitiveCount);
        out.writeInt(valuesPerPrimitive);
    }
    
    private final static void writeBinaryMap(String path,int[] worldMap) throws IOException{
        DataOutputStream out=createNewFileFromLocalPathAndGetDataStream(path);
        for(int i=0;i<worldMap.length;i++)
            out.writeInt(worldMap[i]);
        out.flush();
        out.close();
    }
    
    private final static void writeLamp(String path) throws IOException{
        DataOutputStream out=createNewFileFromLocalPathAndGetDataStream(path);
        //write header       
        //4 bounds * 27 primitive groups * 1 animation * 1 frame * 5 elements in a primitive
        writeHeader(out,108,5);
        writeLampData(out);             
        out.flush();
        out.close();
    }
    
    private final static void writeVendingMachine(String path) throws IOException{
        DataOutputStream out=createNewFileFromLocalPathAndGetDataStream(path);
        //write header
        //4 bounds * 4 primitive groups * 1 animation * 1 frame * 5 elements in a primitive
        writeHeader(out,16,5);
        writeVendingMachineData(out);             
        out.flush();
        out.close();
    }
    
    private final static void writeHealthPowerUp(String path) throws IOException{
        DataOutputStream out=createNewFileFromLocalPathAndGetDataStream(path);
        //write header
        //4 bounds * 6 primitive groups * 1 animation * 1 frame * 5 elements in a primitive
        writeHeader(out,24,5);
        writeHealthPowerUpData(out);             
        out.flush();
        out.close();
    } 

    private final static void writeChair(String path)  throws IOException{
        DataOutputStream out=createNewFileFromLocalPathAndGetDataStream(path);
        //write header
        //4 bounds * 2 primitive groups * 1 animation * 1 frame * 5 elements in a primitive
        writeHeader(out,8,5);
        writeChairData(out);             
        out.flush();
        out.close();
    }
    
    private final static void writeTable(String path)  throws IOException{
        DataOutputStream out=createNewFileFromLocalPathAndGetDataStream(path);   
        //write header       
        //4 bounds * 33 primitive groups * 1 animation * 1 frame * 5 elements in a primitive
        writeHeader(out,132,5);
        writeTableData(out);             
        out.flush();
        out.close();
    }
    
    private final static void writeBonsai(String path)  throws IOException{
        DataOutputStream out=createNewFileFromLocalPathAndGetDataStream(path);
        //write header
        //4 bounds * 2 primitive groups * 1 animation * 1 frame * 5 elements in a primitive
        writeHeader(out,8,5);
        writeBonsaiData(out);             
        out.flush();
        out.close();
    }
    
    private final static void writeExplosion(String path)  throws IOException{
        DataOutputStream out=createNewFileFromLocalPathAndGetDataStream(path);
        //write header
        //4 bounds * 2 primitive groups * 1 animation * 1 frame * 5 elements in a primitive
        writeHeader(out,8,5);
        writeExplosionData(out);             
        out.flush();
        out.close();
    }
    
    private final static void writeImpact(String path)  throws IOException{
        DataOutputStream out=createNewFileFromLocalPathAndGetDataStream(path);
        //write header
        //4 bounds * 2 primitive groups * 1 animation * 1 frame * 5 elements in a primitive
        writeHeader(out,8,5);
        writeImpactData(out);             
        out.flush();
        out.close();
    }
    
    private final static void writeFlower(String path)  throws IOException{
        DataOutputStream out=createNewFileFromLocalPathAndGetDataStream(path);
        //write header
        //4 bounds * 2 primitive groups * 1 animation * 1 frame * 5 elements in a primitive
        writeHeader(out,8,5);
        writeFlowerData(out);             
        out.flush();
        out.close();
    }
    
    private final static void writeUnbreakableObject(String path)  throws IOException{
        DataOutputStream out=createNewFileFromLocalPathAndGetDataStream(path);
        //write header
        //4 bounds * 21 primitive groups * 1 animation * 1 frame * 5 elements in a primitive
        writeHeader(out,84,5);
        writeUnbreakableObjectData(out);             
        out.flush();
        out.close();
    }
    
    private final static void writeBot(String path)  throws IOException{
        DataOutputStream out=createNewFileFromLocalPathAndGetDataStream(path);                     
        //write header
        //4 bounds * 2 animations * 11 frames * 5 elements in a primitive
        writeHeader(out,88,5);
        writeBotData(out);        
        out.flush();
        out.close();   
    }
    
    private final static void writeRocketLauncher(String path) throws IOException{       
        DataOutputStream out=createNewFileFromLocalPathAndGetDataStream(path);
        //write the header
        //1520=((4 octagons * 3 quad primitives calls)+
        //(9 links * 8 quad primitives calls))
        //(4 primitives per quad primitives call*5 elements in a primitive)
        writeHeader(out,336,5);
        writeRocketLauncherData(out,15.0f);                   
        out.flush();
        out.close();
    }
    
    private final static void writeRocket(String path)throws IOException{       
        DataOutputStream out=createNewFileFromLocalPathAndGetDataStream(path);
        //write the header
        //4 bounds * 14 primitive groups * 1 animation * 1 frame * 5 elements in a primitive
        writeHeader(out,56,5);
        writeRocketData(out);                   
        out.flush();
        out.close();
    }
    
    private final static void writeCrosshair(String path)throws IOException{
        DataOutputStream out=createNewFileFromLocalPathAndGetDataStream(path);
        //write the header
        //1 bound * 4 primitive groups * 1 animation * 1 frame * 5 elements in a primitive
        writeHeader(out,4,5);
        writeCrosshairData(out);
        out.flush();
        out.close();
    }
    
    private final static void writeSphericalBeast(String path)throws IOException{
        DataOutputStream out=createNewFileFromLocalPathAndGetDataStream(path);
        //TODO: 7 animations
        
        //write the header
        //? bounds * ? primitive groups * ? animation * ? frame * ? elements in a primitive
        //writeHeader(out,,);
        //writeSphericalBeastData(out,,);
        out.flush();
        out.close();
    }
    
    private final static DataOutputStream createNewFileFromLocalPathAndGetDataStream(String path)throws IOException{      
        return(new DataOutputStream(createNewFileFromLocalPathAndGetBufferedStream(path)));
    }
    
    private final static BufferedOutputStream createNewFileFromLocalPathAndGetBufferedStream(String path)throws IOException{
        BufferedOutputStream out;
        File file=new File(path);       
        //check if the file already exists
        if(!file.exists())
            {//check if the parent directory exists
             File parent=file.getParentFile();
             if(parent!=null && !parent.exists())
                 parent.mkdirs();
             file.createNewFile();
            }
        out=new BufferedOutputStream(new FileOutputStream(file));       
        return(out);
    }
    
    private final static void writeUnbreakableObjectData(DataOutputStream out) throws IOException{       
        final float[] wallTexCoord={0.03f,0.82f,0.22f,0.78f};
        final float[] grassTexCoord={0.08f,0.84f,0.17f,0.83f};
        final float internalHalfSize=0.4f*factor;
        final float externalHalfSize=0.5f*factor;
        final float topHeight=-0.1f*factor;
        final float intermediaryHeight=-0.20f*factor;
        final float bottomHeight=-0.5f*factor;       
        //internal walls
        //animation n 0
        //frame n 0
        //build upper walls              
        writeQuadPrimitive(out,wallTexCoord[0],wallTexCoord[1],-internalHalfSize,intermediaryHeight,-internalHalfSize,
                wallTexCoord[0],wallTexCoord[3],-internalHalfSize,bottomHeight,-internalHalfSize,
                wallTexCoord[2],wallTexCoord[3],internalHalfSize,bottomHeight,-internalHalfSize,
                wallTexCoord[2],wallTexCoord[1],internalHalfSize,intermediaryHeight,-internalHalfSize);
        //build lower walls                  
        writeQuadPrimitive(out,wallTexCoord[0],wallTexCoord[1],internalHalfSize,intermediaryHeight,internalHalfSize,
                wallTexCoord[0],wallTexCoord[3],internalHalfSize,bottomHeight,internalHalfSize,
                wallTexCoord[2],wallTexCoord[3],-internalHalfSize,bottomHeight,internalHalfSize,
                wallTexCoord[2],wallTexCoord[1],-internalHalfSize,intermediaryHeight,internalHalfSize);
        //build left side walls
        writeQuadPrimitive(out,wallTexCoord[0],wallTexCoord[1],-internalHalfSize,intermediaryHeight,internalHalfSize,
                wallTexCoord[0],wallTexCoord[3],-internalHalfSize,bottomHeight,internalHalfSize,
                wallTexCoord[2],wallTexCoord[3],-internalHalfSize,bottomHeight,-internalHalfSize,
                wallTexCoord[2],wallTexCoord[1],-internalHalfSize,intermediaryHeight,-internalHalfSize);              
        //build right side walls               
        writeQuadPrimitive(out,wallTexCoord[0],wallTexCoord[1],internalHalfSize,intermediaryHeight,-internalHalfSize,
                wallTexCoord[0],wallTexCoord[3],internalHalfSize,bottomHeight,-internalHalfSize,
                wallTexCoord[2],wallTexCoord[3],internalHalfSize,bottomHeight,internalHalfSize,
                wallTexCoord[2],wallTexCoord[1],internalHalfSize,intermediaryHeight,internalHalfSize);       
        //external walls
        //build upper walls                
        writeQuadPrimitive(out,wallTexCoord[0],wallTexCoord[1],externalHalfSize,intermediaryHeight,-externalHalfSize,
                wallTexCoord[0],wallTexCoord[3],externalHalfSize,bottomHeight,-externalHalfSize,
                wallTexCoord[2],wallTexCoord[3],-externalHalfSize,bottomHeight,-externalHalfSize,
                wallTexCoord[2],wallTexCoord[1],-externalHalfSize,intermediaryHeight,-externalHalfSize);
        //build lower walls              
        writeQuadPrimitive(out,wallTexCoord[0],wallTexCoord[1],-externalHalfSize,intermediaryHeight,externalHalfSize,
                wallTexCoord[0],wallTexCoord[3],-externalHalfSize,bottomHeight,externalHalfSize,
                wallTexCoord[2],wallTexCoord[3],externalHalfSize,bottomHeight,externalHalfSize,
                wallTexCoord[2],wallTexCoord[1],externalHalfSize,intermediaryHeight,externalHalfSize);
        //build left side walls         
        writeQuadPrimitive(out,wallTexCoord[0],wallTexCoord[1],-externalHalfSize,intermediaryHeight,-externalHalfSize,
                wallTexCoord[0],wallTexCoord[3],-externalHalfSize,bottomHeight,-externalHalfSize,
                wallTexCoord[2],wallTexCoord[3],-externalHalfSize,bottomHeight,externalHalfSize,
                wallTexCoord[2],wallTexCoord[1],-externalHalfSize,intermediaryHeight,externalHalfSize);               
        //build right side walls                  
        writeQuadPrimitive(out,wallTexCoord[0],wallTexCoord[1],externalHalfSize,intermediaryHeight,externalHalfSize,
                wallTexCoord[0],wallTexCoord[3],externalHalfSize,bottomHeight,externalHalfSize,
                wallTexCoord[2],wallTexCoord[3],externalHalfSize,bottomHeight,-externalHalfSize,
                wallTexCoord[2],wallTexCoord[1],externalHalfSize,intermediaryHeight,-externalHalfSize);
        //walls linking the both previous walls, covering the whole architecture          
        //build upper walls                
        writeQuadPrimitive(out,wallTexCoord[0],wallTexCoord[1],-externalHalfSize,intermediaryHeight,-externalHalfSize,
                wallTexCoord[0],wallTexCoord[3],-internalHalfSize,intermediaryHeight,-internalHalfSize,
                wallTexCoord[2],wallTexCoord[3],internalHalfSize,intermediaryHeight,-internalHalfSize,
                wallTexCoord[2],wallTexCoord[1],externalHalfSize,intermediaryHeight,-externalHalfSize);
        //build lower walls
        writeQuadPrimitive(out,wallTexCoord[0],wallTexCoord[1],externalHalfSize,intermediaryHeight,externalHalfSize,
                wallTexCoord[0],wallTexCoord[3],internalHalfSize,intermediaryHeight,internalHalfSize,
                wallTexCoord[2],wallTexCoord[3],-internalHalfSize,intermediaryHeight,internalHalfSize,
                wallTexCoord[2],wallTexCoord[1],-externalHalfSize,intermediaryHeight,externalHalfSize);
        //build left side walls
        writeQuadPrimitive(out,wallTexCoord[0],wallTexCoord[1],-externalHalfSize,intermediaryHeight,externalHalfSize,
                wallTexCoord[0],wallTexCoord[3],-internalHalfSize,intermediaryHeight,internalHalfSize,
                wallTexCoord[2],wallTexCoord[3],-internalHalfSize,intermediaryHeight,-internalHalfSize,
                wallTexCoord[2],wallTexCoord[1],-externalHalfSize,intermediaryHeight,-externalHalfSize);
        //build right side walls 
        writeQuadPrimitive(out,wallTexCoord[0],wallTexCoord[1],externalHalfSize,intermediaryHeight,-externalHalfSize,
                wallTexCoord[0],wallTexCoord[3],internalHalfSize,intermediaryHeight,-internalHalfSize,
                wallTexCoord[2],wallTexCoord[3],internalHalfSize,intermediaryHeight,internalHalfSize,
                wallTexCoord[2],wallTexCoord[1],externalHalfSize,intermediaryHeight,externalHalfSize);
        //green flat part
        writeQuadPrimitive(out,grassTexCoord[0],grassTexCoord[1],-internalHalfSize,intermediaryHeight,-internalHalfSize,
                grassTexCoord[0],grassTexCoord[3],-internalHalfSize,intermediaryHeight,internalHalfSize,
                grassTexCoord[2],grassTexCoord[3],internalHalfSize,intermediaryHeight,internalHalfSize,
                grassTexCoord[2],grassTexCoord[1],internalHalfSize,intermediaryHeight,-internalHalfSize);
        //grass
        //build upper walls              
        writeQuadPrimitive(out,grassTexCoord[0],grassTexCoord[1],-internalHalfSize,topHeight,-internalHalfSize,
                grassTexCoord[0],grassTexCoord[3],-internalHalfSize,intermediaryHeight,-internalHalfSize,
                grassTexCoord[2],grassTexCoord[3],internalHalfSize,intermediaryHeight,-internalHalfSize,
                grassTexCoord[2],grassTexCoord[1],internalHalfSize,topHeight,-internalHalfSize);
        //build lower walls                  
        writeQuadPrimitive(out,grassTexCoord[0],grassTexCoord[1],internalHalfSize,topHeight,internalHalfSize,
                grassTexCoord[0],grassTexCoord[3],internalHalfSize,intermediaryHeight,internalHalfSize,
                grassTexCoord[2],grassTexCoord[3],-internalHalfSize,intermediaryHeight,internalHalfSize,
                grassTexCoord[2],grassTexCoord[1],-internalHalfSize,topHeight,internalHalfSize);
        //build left side walls
        writeQuadPrimitive(out,grassTexCoord[0],grassTexCoord[1],-internalHalfSize,topHeight,internalHalfSize,
                grassTexCoord[0],grassTexCoord[3],-internalHalfSize,intermediaryHeight,internalHalfSize,
                grassTexCoord[2],grassTexCoord[3],-internalHalfSize,intermediaryHeight,-internalHalfSize,
                grassTexCoord[2],grassTexCoord[1],-internalHalfSize,topHeight,-internalHalfSize);              
        //build right side walls               
        writeQuadPrimitive(out,grassTexCoord[0],grassTexCoord[1],internalHalfSize,topHeight,-internalHalfSize,
                grassTexCoord[0],grassTexCoord[3],internalHalfSize,intermediaryHeight,-internalHalfSize,
                grassTexCoord[2],grassTexCoord[3],internalHalfSize,intermediaryHeight,internalHalfSize,
                grassTexCoord[2],grassTexCoord[1],internalHalfSize,topHeight,internalHalfSize);
        //build upper walls                
        writeQuadPrimitive(out,grassTexCoord[0],grassTexCoord[1],internalHalfSize,topHeight,-internalHalfSize,
                grassTexCoord[0],grassTexCoord[3],internalHalfSize,intermediaryHeight,-internalHalfSize,
                grassTexCoord[2],grassTexCoord[3],-internalHalfSize,intermediaryHeight,-internalHalfSize,
                grassTexCoord[2],grassTexCoord[1],-internalHalfSize,topHeight,-internalHalfSize);
        //build lower walls              
        writeQuadPrimitive(out,grassTexCoord[0],grassTexCoord[1],-internalHalfSize,topHeight,internalHalfSize,
                grassTexCoord[0],grassTexCoord[3],-internalHalfSize,intermediaryHeight,internalHalfSize,
                grassTexCoord[2],grassTexCoord[3],internalHalfSize,intermediaryHeight,internalHalfSize,
                grassTexCoord[2],grassTexCoord[1],internalHalfSize,topHeight,internalHalfSize);
        //build left side walls         
        writeQuadPrimitive(out,grassTexCoord[0],grassTexCoord[1],-internalHalfSize,topHeight,-internalHalfSize,
                grassTexCoord[0],grassTexCoord[3],-internalHalfSize,intermediaryHeight,-internalHalfSize,
                grassTexCoord[2],grassTexCoord[3],-internalHalfSize,intermediaryHeight,internalHalfSize,
                grassTexCoord[2],grassTexCoord[1],-internalHalfSize,topHeight,internalHalfSize);               
        //build right side walls                  
        writeQuadPrimitive(out,grassTexCoord[0],grassTexCoord[1],internalHalfSize,topHeight,internalHalfSize,
                grassTexCoord[0],grassTexCoord[3],internalHalfSize,intermediaryHeight,internalHalfSize,
                grassTexCoord[2],grassTexCoord[3],internalHalfSize,intermediaryHeight,-internalHalfSize,
                grassTexCoord[2],grassTexCoord[1],internalHalfSize,topHeight,-internalHalfSize);
    }
    
    private final static void writeBotData(DataOutputStream out) throws IOException{
        //animation n 0
        //frame n 0
        writeQuadPrimitive(out,0.0f,1.0f,-0.5f*factor,0.5f*factor,0.0f,
                0.0f,0.75f,-0.5f*factor,-0.5f*factor,0.0f,
                0.25f,0.75f,0.5f*factor,-0.5f*factor,0.0f,
                0.25f,1.0f,0.5f*factor,0.5f*factor,0.0f);
        //frame n 1
        writeQuadPrimitive(out,0.25f,1.0f,-0.5f*factor,0.5f*factor,0.0f,
                0.25f,0.75f,-0.5f*factor,-0.5f*factor,0.0f,
                0.5f,0.75f,0.5f*factor,-0.5f*factor,0.0f,
                0.5f,1.0f,0.5f*factor,0.5f*factor,0.0f);
        //frame n 2
        writeQuadPrimitive(out,0.5f,1.0f,-0.5f*factor,0.5f*factor,0.0f,
                0.5f,0.75f,-0.5f*factor,-0.5f*factor,0.0f,
                0.75f,0.75f,0.5f*factor,-0.5f*factor,0.0f,
                0.75f,1.0f,0.5f*factor,0.5f*factor,0.0f);
        //frame n 3
        writeQuadPrimitive(out,0.75f,1.0f,-0.5f*factor,0.5f*factor,0.0f,
                0.75f,0.75f,-0.5f*factor,-0.5f*factor,0.0f,
                1.0f,0.75f,0.5f*factor,-0.5f*factor,0.0f,
                1.0f,1.0f,0.5f*factor,0.5f*factor,0.0f);
        //frame n 4
        writeQuadPrimitive(out,0.0f,0.75f,-0.5f*factor,0.5f*factor,0.0f,
                0.0f,0.5f,-0.5f*factor,-0.5f*factor,0.0f,
                0.25f,0.5f,0.5f*factor,-0.5f*factor,0.0f,
                0.25f,0.75f,0.5f*factor,0.5f*factor,0.0f);
        //frame n 5
        writeQuadPrimitive(out,0.25f,0.75f,-0.5f*factor,0.5f*factor,0.0f,
                0.25f,0.5f,-0.5f*factor,-0.5f*factor,0.0f,
                0.5f,0.5f,0.5f*factor,-0.5f*factor,0.0f,
                0.5f,0.75f,0.5f*factor,0.5f*factor,0.0f);
        //frame n 6
        writeQuadPrimitive(out,0.5f,0.75f,-0.5f*factor,0.5f*factor,0.0f,
                0.5f,0.5f,-0.5f*factor,-0.5f*factor,0.0f,
                0.75f,0.5f,0.5f*factor,-0.5f*factor,0.0f,
                0.75f,0.75f,0.5f*factor,0.5f*factor,0.0f);
        //frame n 7
        writeQuadPrimitive(out,0.75f,0.75f,-0.5f*factor,0.5f*factor,0.0f,
                0.75f,0.5f,-0.5f*factor,-0.5f*factor,0.0f,
                1.0f,0.5f,0.5f*factor,-0.5f*factor,0.0f,
                1.0f,0.75f,0.5f*factor,0.5f*factor,0.0f);
        //frame n 8
        writeQuadPrimitive(out,0.0f,0.5f,-0.5f*factor,0.5f*factor,0.0f,
                0.0f,0.25f,-0.5f*factor,-0.5f*factor,0.0f,
                0.25f,0.25f,0.5f*factor,-0.5f*factor,0.0f,
                0.25f,0.5f,0.5f*factor,0.5f*factor,0.0f);
        //frame n 9
        writeQuadPrimitive(out,0.25f,0.5f,-0.5f*factor,0.5f*factor,0.0f,
                0.25f,0.25f,-0.5f*factor,-0.5f*factor,0.0f,
                0.5f,0.25f,0.5f*factor,-0.5f*factor,0.0f,
                0.5f,0.5f,0.5f*factor,0.5f*factor,0.0f);
        //frame n 10
        writeQuadPrimitive(out,0.5f,0.5f,-0.5f*factor,0.5f*factor,0.0f,
                0.5f,0.25f,-0.5f*factor,-0.5f*factor,0.0f,
                0.75f,0.25f,0.5f*factor,-0.5f*factor,0.0f,
                0.75f,0.5f,0.5f*factor,0.5f*factor,0.0f);      
        //animation n 1
        //frame n 0
        writeQuadPrimitive(out,0.0f,1.0f,-0.5f*factor,0.5f*factor,0.0f,
                0.0f,0.75f,-0.5f*factor,-0.5f*factor,0.0f,
                0.25f,0.75f,0.5f*factor,-0.5f*factor,0.0f,
                0.25f,1.0f,0.5f*factor,0.5f*factor,0.0f);
        //frame n 1
        writeQuadPrimitive(out,0.25f,1.0f,-0.5f*factor,0.5f*factor,0.0f,
                0.25f,0.75f,-0.5f*factor,-0.5f*factor,0.0f,
                0.5f,0.75f,0.5f*factor,-0.5f*factor,0.0f,
                0.5f,1.0f,0.5f*factor,0.5f*factor,0.0f);
        //frame n 2
        writeQuadPrimitive(out,0.5f,1.0f,-0.5f*factor,0.5f*factor,0.0f,
                0.5f,0.75f,-0.5f*factor,-0.5f*factor,0.0f,
                0.75f,0.75f,0.5f*factor,-0.5f*factor,0.0f,
                0.75f,1.0f,0.5f*factor,0.5f*factor,0.0f);
        //frame n 3
        writeQuadPrimitive(out,0.75f,1.0f,-0.5f*factor,0.5f*factor,0.0f,
                0.75f,0.75f,-0.5f*factor,-0.5f*factor,0.0f,
                1.0f,0.75f,0.5f*factor,-0.5f*factor,0.0f,
                1.0f,1.0f,0.5f*factor,0.5f*factor,0.0f);
        //frame n 4
        writeQuadPrimitive(out,0.0f,0.75f,-0.5f*factor,0.5f*factor,0.0f,
                0.0f,0.5f,-0.5f*factor,-0.5f*factor,0.0f,
                0.25f,0.5f,0.5f*factor,-0.5f*factor,0.0f,
                0.25f,0.75f,0.5f*factor,0.5f*factor,0.0f);
        //frame n 5
        writeQuadPrimitive(out,0.25f,0.75f,-0.5f*factor,0.5f*factor,0.0f,
                0.25f,0.5f,-0.5f*factor,-0.5f*factor,0.0f,
                0.5f,0.5f,0.5f*factor,-0.5f*factor,0.0f,
                0.5f,0.75f,0.5f*factor,0.5f*factor,0.0f);
        //frame n 6
        writeQuadPrimitive(out,0.5f,0.75f,-0.5f*factor,0.5f*factor,0.0f,
                0.5f,0.5f,-0.5f*factor,-0.5f*factor,0.0f,
                0.75f,0.5f,0.5f*factor,-0.5f*factor,0.0f,
                0.75f,0.75f,0.5f*factor,0.5f*factor,0.0f);
        //frame n 7
        writeQuadPrimitive(out,0.75f,0.75f,-0.5f*factor,0.5f*factor,0.0f,
                0.75f,0.5f,-0.5f*factor,-0.5f*factor,0.0f,
                1.0f,0.5f,0.5f*factor,-0.5f*factor,0.0f,
                1.0f,0.75f,0.5f*factor,0.5f*factor,0.0f);
        //frame n 8
        writeQuadPrimitive(out,0.0f,0.5f,-0.5f*factor,0.5f*factor,0.0f,
                0.0f,0.25f,-0.5f*factor,-0.5f*factor,0.0f,
                0.25f,0.25f,0.5f*factor,-0.5f*factor,0.0f,
                0.25f,0.5f,0.5f*factor,0.5f*factor,0.0f);
        //frame n 9
        writeQuadPrimitive(out,0.25f,0.5f,-0.5f*factor,0.5f*factor,0.0f,
                0.25f,0.25f,-0.5f*factor,-0.5f*factor,0.0f,
                0.5f,0.25f,0.5f*factor,-0.5f*factor,0.0f,
                0.5f,0.5f,0.5f*factor,0.5f*factor,0.0f);
        //frame n 10
        writeQuadPrimitive(out,0.5f,0.5f,-0.5f*factor,0.5f*factor,0.0f,
                0.5f,0.25f,-0.5f*factor,-0.5f*factor,0.0f,
                0.75f,0.25f,0.5f*factor,-0.5f*factor,0.0f,
                0.75f,0.5f,0.5f*factor,0.5f*factor,0.0f);
    }
           
    private final static void writeRocketLauncherData(DataOutputStream out,float inradius)throws IOException{
    	final float r1=inradius,r2=inradius*(2.0f/3.0f);
    	final float r3=inradius*(5.0f/6.0f),r4=inradius/6.0f;
    	final float r5=inradius/2.0f,r6=inradius/3.0f;
    	float[][] part1,part2,part3,part3bis,part4,part5,part6,part7,part8,part9,part10,part11;
    	float[] lightBeige={0.5f,0.5f,0.75f,1.0f};
    	float[] lightestGray={0.75f,0.5f,1.0f,1.0f};   	
    	float[] smallScrew={0.0f,0.0f,0.25f,0.5f};
    	float[] bigScrew={0.25f,0.0f,0.5f,0.5f};
    	float[] lightGray={0.5f,0.0f,0.75f,0.5f};
    	float[] darkGray={0.75f,0.0f,1.0f,0.5f};   	
    	//write the closest octagon in the back of the weapon
    	part1=writeZeroCenteredRegularOctagonAroundZ(out, r1, 0.0f,
    			darkGray[0],darkGray[1],darkGray[0],darkGray[3],
    			darkGray[2],darkGray[3],darkGray[2],darkGray[1],
    			darkGray[0],darkGray[1],darkGray[0],darkGray[3],
    			darkGray[2],darkGray[3],darkGray[2],darkGray[1]);
    	part2=computeZeroCenteredRegularOctagonAroundZ(r1,-inradius*(4.0f/3.0f));
    	//write the closest link
    	writeLinkBetweenTwoZeroCenteredRegularOctagons(out,part1,part2,
    			lightGray[0],lightGray[1],lightGray[0],lightGray[3],
    			lightGray[2],lightGray[3],lightGray[2],lightGray[1]);   	   	
    	part3=computeZeroCenteredRegularOctagonAroundZ(r2, -inradius*(5.0f/3.0f));
    	//write the link
    	writeLinkBetweenTwoZeroCenteredRegularOctagons(out,part2,part3, 
    			bigScrew[0],bigScrew[1],bigScrew[0],bigScrew[3],
    			bigScrew[2],bigScrew[3],bigScrew[2],bigScrew[1]);   	
    	part3bis=computeZeroCenteredRegularOctagonAroundZ(r2, -inradius*(7.0f/3.0f));
    	//write the link
    	writeLinkBetweenTwoZeroCenteredRegularOctagons(out,part3,part3bis, 
    			lightGray[0],lightGray[1],lightGray[0],lightGray[3],
    			lightGray[2],lightGray[3],lightGray[2],lightGray[1]);
    	part4=computeZeroCenteredRegularOctagonAroundZ(r2, -inradius*(8.0f/3.0f));
    	//write the link
    	writeLinkBetweenTwoZeroCenteredRegularOctagons(out,part3bis,part4, 
    			lightBeige[0],lightBeige[1],lightBeige[0],lightBeige[3],
    			lightBeige[2],lightBeige[3],lightBeige[2],lightBeige[1]);
    	part5=computeZeroCenteredRegularOctagonAroundZ(r3, -inradius*3.0f);
    	//write the link
    	writeLinkBetweenTwoZeroCenteredRegularOctagons(out,part4,part5,
    			smallScrew[0],smallScrew[1],smallScrew[0],smallScrew[3],
    			smallScrew[2],smallScrew[3],smallScrew[2],smallScrew[1]);
    	part6=computeZeroCenteredRegularOctagonAroundZ(r3, -inradius*(10.0f/3.0f));
    	//write the link
    	writeLinkBetweenTwoZeroCenteredRegularOctagons(out,part5,part6,
    			bigScrew[0],bigScrew[1],bigScrew[0],bigScrew[3],
    			bigScrew[2],bigScrew[3],bigScrew[2],bigScrew[1]);
    	part7=computeZeroCenteredRegularOctagonAroundZ(r2, -inradius*4.0f);
    	//write the link
    	writeLinkBetweenTwoZeroCenteredRegularOctagons(out,part6,part7, 
    			smallScrew[0],smallScrew[1],smallScrew[0],smallScrew[3],
    			smallScrew[2],smallScrew[3],smallScrew[2],smallScrew[1]);
    	//write an octagon
    	writeZeroCenteredReversedRegularOctagonAroundZ(out, r2, -inradius*4.0f,
    			smallScrew[0],smallScrew[1],smallScrew[0],smallScrew[3],
    			smallScrew[2],smallScrew[3],smallScrew[2],smallScrew[1],
    			smallScrew[0],smallScrew[1],smallScrew[0],smallScrew[3],
    			smallScrew[2],smallScrew[3],smallScrew[2],smallScrew[1]);
    	part8=computeZeroCenteredRegularOctagonAroundZ(r4, -inradius*4.0f);
    	part9=computeZeroCenteredRegularOctagonAroundZ(r4, -inradius*(13.0f/3.0f));
    	//write the link
    	writeLinkBetweenTwoZeroCenteredRegularOctagons(out,part8,part9,
    			smallScrew[0],smallScrew[1],smallScrew[0],smallScrew[3],
    			smallScrew[2],smallScrew[3],smallScrew[2],smallScrew[1]);
    	//write an octagon
    	part10=writeZeroCenteredRegularOctagonAroundZ(out, r5, -inradius*(13.0f/3.0f),
    			smallScrew[0],smallScrew[1],smallScrew[0],smallScrew[3],
    			smallScrew[2],smallScrew[3],smallScrew[2],smallScrew[1],
    			smallScrew[0],smallScrew[1],smallScrew[0],smallScrew[3],
    			smallScrew[2],smallScrew[3],smallScrew[2],smallScrew[1]);
    	part11=computeZeroCenteredRegularOctagonAroundZ(r6, -inradius*(17.0f/3.0f));
    	//write the link
    	writeLinkBetweenTwoZeroCenteredRegularOctagons(out,part10,part11,
    			lightestGray[0],lightestGray[1],lightestGray[0],lightestGray[3],
    			lightestGray[2],lightestGray[3],lightestGray[2],lightestGray[1]);
    	//write an octagon
    	writeZeroCenteredReversedRegularOctagonAroundZ(out, r6, -inradius*(17.0f/3.0f),
    			lightestGray[0],lightestGray[1],lightestGray[0],lightestGray[3],
    			lightestGray[2],lightestGray[3],lightestGray[2],lightestGray[1],
    			lightestGray[0],lightestGray[1],lightestGray[0],lightestGray[3],
    			lightestGray[2],lightestGray[3],lightestGray[2],lightestGray[1]);   	
    }
    
    private final static void writeVendingMachineData(DataOutputStream out) throws IOException {
        //animation n 0
        //frame n 0
        //build upper walls                      
        writeQuadPrimitive(out,0.58f,0.73f,0.2f*factor,0.5f*factor,-0.2f*factor,
                0.58f,0.52f,0.2f*factor,-0.5f*factor,-0.2f*factor,
                0.66f,0.52f,-0.2f*factor,-0.5f*factor,-0.2f*factor,
                0.66f,0.73f,-0.2f*factor,0.5f*factor,-0.2f*factor);
        //build lower walls                          
        writeQuadPrimitive(out,0.58f,0.73f,-0.2f*factor,0.5f*factor,0.2f*factor,
                0.58f,0.52f,-0.2f*factor,-0.5f*factor,0.2f*factor,
                0.66f,0.52f,0.2f*factor,-0.5f*factor,0.2f*factor,
                0.66f,0.73f,0.2f*factor,0.5f*factor,0.2f*factor);
        //build left side walls                                          
        writeQuadPrimitive(out,0.58f,0.73f,-0.2f*factor,0.5f*factor,-0.2f*factor,
                0.58f,0.52f,-0.2f*factor,-0.5f*factor,-0.2f*factor,
                0.66f,0.52f,-0.2f*factor,-0.5f*factor,0.2f*factor,
                0.66f,0.73f,-0.2f*factor,0.5f*factor,0.2f*factor);        
        //build right side walls                                 
        writeQuadPrimitive(out,0.58f,0.73f,0.2f*factor,0.5f*factor,0.2f*factor,
                0.58f,0.52f,0.2f*factor,-0.5f*factor,0.2f*factor,
                0.66f,0.52f,0.2f*factor,-0.5f*factor,-0.2f*factor,
                0.66f,0.73f,0.2f*factor,0.5f*factor,-0.2f*factor);
    }
    
    private final static void writeHealthPowerUpData(DataOutputStream out) throws IOException {
        //animation n 0
        //frame n 0
        //build upper walls                      
        writeQuadPrimitive(out,0.0f,1.0f,0.1f*factor,0.1f*factor,-0.1f*factor,
                0.0f,0.0f,0.1f*factor,-0.1f*factor,-0.1f*factor,
                1.0f,0.0f,-0.1f*factor,-0.1f*factor,-0.1f*factor,
                1.0f,1.0f,-0.1f*factor,0.1f*factor,-0.1f*factor);
        //build lower walls                          
        writeQuadPrimitive(out,0.0f,1.0f,-0.1f*factor,0.1f*factor,0.1f*factor,
                0.0f,0.0f,-0.1f*factor,-0.1f*factor,0.1f*factor,
                1.0f,0.0f,0.1f*factor,-0.1f*factor,0.1f*factor,
                1.0f,1.0f,0.1f*factor,0.1f*factor,0.1f*factor);
        //build left side walls                                          
        writeQuadPrimitive(out,0.0f,1.0f,-0.1f*factor,0.1f*factor,-0.1f*factor,
                0.0f,0.0f,-0.1f*factor,-0.1f*factor,-0.1f*factor,
                1.0f,0.0f,-0.1f*factor,-0.1f*factor,0.1f*factor,
                1.0f,1.0f,-0.1f*factor,0.1f*factor,0.1f*factor);        
        //build right side walls                                 
        writeQuadPrimitive(out,0.0f,1.0f,0.1f*factor,0.1f*factor,0.1f*factor,
                0.0f,0.0f,0.1f*factor,-0.1f*factor,0.1f*factor,
                1.0f,0.0f,0.1f*factor,-0.1f*factor,-0.1f*factor,
                1.0f,1.0f,0.1f*factor,0.1f*factor,-0.1f*factor);
        //build wall above                      
        writeQuadPrimitive(out,0.0f,1.0f,0.1f*factor,0.1f*factor,0.1f*factor,
                0.0f,0.0f,0.1f*factor,0.1f*factor,-0.1f*factor,
                1.0f,0.0f,-0.1f*factor,0.1f*factor,-0.1f*factor,
                1.0f,1.0f,-0.1f*factor,0.1f*factor,0.1f*factor);     
        //build wall below                      
        writeQuadPrimitive(out,0.0f,1.0f,0.1f*factor,-0.1f*factor,-0.1f*factor,
                0.0f,0.0f,0.1f*factor,-0.1f*factor,0.1f*factor,
                1.0f,0.0f,-0.1f*factor,-0.1f*factor,0.1f*factor,
                1.0f,1.0f,-0.1f*factor,-0.1f*factor,-0.1f*factor);
    }
    
    private final static void writeLampData(DataOutputStream out) throws IOException {
        //animation n 0
        //frame n 0
        float[][] part1,part2,part3,part4,part5;
        //bottom
        part1=computeZeroCenteredRegularOctagonAroundY(0.1f*factor,-0.5f*factor);
        part2=writeZeroCenteredReversedRegularOctagonAroundY(out,0.1f*factor,-0.48f*factor,0.36f,0.52f,0.36f,0.51f,0.39f,0.51f,0.39f,0.52f,0.36f,0.52f,0.36f,0.51f,0.39f,0.51f,0.39f,0.52f);
        writeLinkBetweenTwoZeroCenteredRegularOctagons(out,part1,part2,0.36f,0.52f,0.36f,0.51f,0.39f,0.51f,0.39f,0.52f);       
        //middle
        part3=computeZeroCenteredRegularOctagonAroundY(0.01f*factor,-0.48f*factor);
        part4=computeZeroCenteredRegularOctagonAroundY(0.01f*factor,0.20f*factor);
        writeLinkBetweenTwoZeroCenteredRegularOctagons(out,part3,part4,0.36f,0.52f,0.36f,0.51f,0.39f,0.51f,0.39f,0.52f);   
        //top
        part5=computeZeroCenteredRegularOctagonAroundY(0.1f*factor,0.40f*factor);
        writeLinkBetweenTwoZeroCenteredRegularOctagons(out,part4,part5,0.36f,0.52f,0.36f,0.51f,0.39f,0.51f,0.39f,0.52f); 
    }
    
    private final static void writeFlowerData(DataOutputStream out) throws IOException {
        //animation n 0
        //frame n 0                                
        writeQuadPrimitive(out,0.0f,0.75f,-0.5f*factor,0.5f*factor,0.0f,
                0.0f,0.5f,-0.5f*factor,-0.5f*factor,0.0f,
                0.25f,0.5f,0.5f*factor,-0.5f*factor,0.0f,
                0.25f,0.75f,0.5f*factor,0.5f*factor,0.0f);  
        writeQuadPrimitive(out,0.0f,0.75f,0.5f*factor,0.5f*factor,0.0f,
                0.0f,0.5f,0.5f*factor,-0.5f*factor,0.0f,
                0.25f,0.5f,-0.5f*factor,-0.5f*factor,0.0f,
                0.25f,0.75f,-0.5f*factor,0.5f*factor,0.0f);
    }
    
    private final static void writeChairData(DataOutputStream out) throws IOException {
        //animation n 0
        //frame n 0                          
        writeQuadPrimitive(out,0.5f,1.0f,-0.5f*factor,0.5f*factor,0.0f,
                0.5f,0.75f,-0.5f*factor,-0.5f*factor,0.0f,
                0.75f,0.75f,0.5f*factor,-0.5f*factor,0.0f,
                0.75f,1.0f,0.5f*factor,0.5f*factor,0.0f);  
        writeQuadPrimitive(out,0.5f,1.0f,0.5f*factor,0.5f*factor,0.0f,
                0.5f,0.75f,0.5f*factor,-0.5f*factor,0.0f,
                0.75f,0.75f,-0.5f*factor,-0.5f*factor,0.0f,
                0.75f,1.0f,-0.5f*factor,0.5f*factor,0.0f);
    }
    
    private final static void writeTableData(DataOutputStream out) throws IOException {
        //animation n 0
        //frame n 0                                
        //from the bottom to the top      
        //bottom
        float[][] part1,part2,part3,part4,part5,part6;
        part1=computeZeroCenteredRegularOctagonAroundY(0.1f*factor,-0.5f*factor);
        part2=writeZeroCenteredReversedRegularOctagonAroundY(out,0.1f*factor,-0.48f*factor,0.36f,0.52f,0.36f,0.51f,0.39f,0.51f,0.39f,0.52f,0.36f,0.52f,0.36f,0.51f,0.39f,0.51f,0.39f,0.52f);
        writeLinkBetweenTwoZeroCenteredRegularOctagons(out,part1,part2,0.36f,0.52f,0.36f,0.51f,0.39f,0.51f,0.39f,0.52f);       
        //middle
        part3=computeZeroCenteredRegularOctagonAroundY(0.01f*factor,-0.48f*factor);
        part4=computeZeroCenteredRegularOctagonAroundY(0.01f*factor,-0.1f*factor);
        writeLinkBetweenTwoZeroCenteredRegularOctagons(out,part3,part4,0.36f,0.52f,0.36f,0.51f,0.39f,0.51f,0.39f,0.52f);       
        //top
        part5=writeZeroCenteredRegularOctagonAroundY(out,0.1f*factor,-0.1f*factor,0.36f,0.52f,0.36f,0.51f,0.39f,0.51f,0.39f,0.52f,0.36f,0.52f,0.36f,0.51f,0.39f,0.51f,0.39f,0.52f);
        writeZeroCenteredReversedRegularOctagonAroundY(out,0.1f*factor,-0.08f*factor,0.36f,0.52f,0.36f,0.51f,0.39f,0.51f,0.39f,0.52f,0.36f,0.52f,0.36f,0.51f,0.39f,0.51f,0.39f,0.52f);
        part6=computeZeroCenteredRegularOctagonAroundY(0.1f*factor,-0.08f*factor);
        writeLinkBetweenTwoZeroCenteredRegularOctagons(out,part5,part6,0.36f,0.52f,0.36f,0.51f,0.39f,0.51f,0.39f,0.52f);
    }
    
    private final static void writeRocketData(DataOutputStream out)throws IOException{
        //
        //animation n 0
        //frame n 0
        final float radius=0.02f*factor,length=0.05f*factor;
        final float[] lightGray={0.5f,0.0f,0.75f,0.5f};
        float[][] part1,part2;
        part1=computeZeroCenteredRegularOctagonAroundZ(radius,-length/2.0f);
        writeZeroCenteredReversedRegularOctagonAroundZ(out,radius,-length/2.0f,
                lightGray[0],lightGray[1],lightGray[0],lightGray[3],
                lightGray[2],lightGray[3],lightGray[2],lightGray[1],
                lightGray[0],lightGray[1],lightGray[0],lightGray[3],
                lightGray[2],lightGray[3],lightGray[2],lightGray[1]);
        part2=writeZeroCenteredRegularOctagonAroundZ(out,radius,length/2.0f,
                lightGray[0],lightGray[1],lightGray[0],lightGray[3],
                lightGray[2],lightGray[3],lightGray[2],lightGray[1],
                lightGray[0],lightGray[1],lightGray[0],lightGray[3],
                lightGray[2],lightGray[3],lightGray[2],lightGray[1]);
        writeLinkBetweenTwoZeroCenteredRegularOctagons(out,part2,part1,
                lightGray[0],lightGray[1],lightGray[0],lightGray[3],
                lightGray[2],lightGray[3],lightGray[2],lightGray[1]);      
    }
        
    private final static void writeBonsaiData(DataOutputStream out) throws IOException {
        //animation n 0
        //frame n 0                  
        writeQuadPrimitive(out,0.0f,0.5f,-0.5f*factor,0.5f*factor,0.0f,
                0.0f,0.25f,-0.5f*factor,-0.5f*factor,0.0f,
                0.25f,0.25f,0.5f*factor,-0.5f*factor,0.0f,
                0.25f,0.5f,0.5f*factor,0.5f*factor,0.0f);  
        writeQuadPrimitive(out,0.0f,0.5f,0.5f*factor,0.5f*factor,0.0f,
                0.0f,0.25f,0.5f*factor,-0.5f*factor,0.0f,
                0.25f,0.25f,-0.5f*factor,-0.5f*factor,0.0f,
                0.25f,0.5f,-0.5f*factor,0.5f*factor,0.0f); 
    }
    
    private final static void writeExplosionData(DataOutputStream out) throws IOException {
        //animation n 0
        //frame n 0                  
        writeQuadPrimitive(out,0.0f,1.0f,-0.5f*factor,0.5f*factor,0.0f,
                0.0f,0.0f,-0.5f*factor,-0.5f*factor,0.0f,
                1.0f,0.0f,0.5f*factor,-0.5f*factor,0.0f,
                1.0f,1.0f,0.5f*factor,0.5f*factor,0.0f);  
        writeQuadPrimitive(out,0.0f,1.0f,0.5f*factor,0.5f*factor,0.0f,
                0.0f,0.0f,0.5f*factor,-0.5f*factor,0.0f,
                1.0f,0.0f,-0.5f*factor,-0.5f*factor,0.0f,
                1.0f,1.0f,-0.5f*factor,0.5f*factor,0.0f); 
    }
    
    private final static void writeImpactData(DataOutputStream out) throws IOException {
        //animation n 0
        //frame n 0                  
        writeQuadPrimitive(out,0.0f,1.0f,-0.5f*factor,0.5f*factor,0.0f,
                0.0f,0.0f,-0.5f*factor,-0.5f*factor,0.0f,
                1.0f,0.0f,0.5f*factor,-0.5f*factor,0.0f,
                1.0f,1.0f,0.5f*factor,0.5f*factor,0.0f);  
        writeQuadPrimitive(out,0.0f,1.0f,0.5f*factor,0.5f*factor,0.0f,
                0.0f,0.0f,0.5f*factor,-0.5f*factor,0.0f,
                1.0f,0.0f,-0.5f*factor,-0.5f*factor,0.0f,
                1.0f,1.0f,-0.5f*factor,0.5f*factor,0.0f); 
    }
    
    private final static void writeCrosshairData(DataOutputStream out) throws IOException {
        //animation n 0
        //frame n 0
        writePrimitive(out,0.0f,0.0f,0.0f,0.0f,1.0f,1.0f,0.9f,0.0f);
        writePrimitive(out,1.0f,1.0f,0.0f,0.0f,1.0f,1.0f,1.1f,0.0f);
        writePrimitive(out,0.0f,0.0f,0.0f,0.0f,1.0f,0.9f,1.0f,0.0f);
        writePrimitive(out,1.0f,1.0f,0.0f,0.0f,1.0f,1.1f,1.0f,0.0f);
    }
       
    public static boolean removePointPair(List<PointPair> list,PointPair pair){
    	int index=0;
    	for(PointPair p:list)
    		if(p.equals(pair))	        
    			return(list.remove(index)!=null);
    		else
    			index++;
    	return(false);
    }
    
    private final static void writeHealthPowerUpList(String filename){
        List<HealthPowerUpModelBean> healthPowerUpList=new Vector<HealthPowerUpModelBean>();
        HealthPowerUpModel hpum=new HealthPowerUpModel(115*factor,-0.4f*factor,219*factor,0,0,null,"You picked up a medikit",20);
        //fill the list
        healthPowerUpList.add(new HealthPowerUpModelBean(hpum));
        //encode health power up list
        encodeObjectInFile(healthPowerUpList,filename);
    }
    
    private final static void writeEnemyList(String filename){
        //TODO: create an enemy bean list and add some enemies (models)
        
        //encode enemy list
        //encodeObjectInFile(enemyList,filename);
    }
    
    private final static void encodeObjectInFile(Object o,String filename){
        BufferedOutputStream bos=null;
        try{bos=createNewFileFromLocalPathAndGetBufferedStream(filename);
            XMLEncoder encoder = new XMLEncoder(bos);
            encoder.writeObject(o);
            encoder.close();
           }
        catch(IOException ioe)
        {ioe.printStackTrace();
         if(bos!=null)
             try{bos.close();}
             catch(IOException ioe2)
             {ioe2.printStackTrace();}
        }       
    }
    
    public static void main(String[] args){
    	if(args.length!=19)
    	    {System.out.println("Usage: java TilesGenerator map_filename"+
    	            " tiles_filename rocketlauncher_filename"+
    	            " binary_map_filename bot_filename"+
    	            " unbreakable_object_filename"+
    	            " vending_machine_filename"+
    	            " lamp_filename"+
    	            " chair_filename"+
    	            " flower_filename"+
    	            " table_filename"+
    	            " bonsai_filename"+
    	            " rocket_filename"+
    	            " explosion_filename"+
    	            " impact_filename"+
    	            " healthPowerUpFilename"+
    	            " healthPowerUpListFilename"+
    	            " crosshairFilename"+
    	            " sphericalBeastFilename");
    	     System.exit(0);
    	    }
	    new TilesGenerator(args[0],args[1],args[2],args[3],args[4],args[5],
	        args[6],args[7],args[8],args[9],args[10],args[11],args[12],args[13],
	        args[14],args[15],args[16],args[17],args[18]);
    }
    
}
