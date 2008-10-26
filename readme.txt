
TUER: Truly Unusual Experience of Revolution: 3D JAVA-POWERED HARDWARE ACCELERATED FPS
         ===========================================
               Julien GOUESSE, tuer.tuxfamily.org
               Vincent Stahl, www.stahlforce.com
          Read CHANGES.RTF for current version info

   Supplied in form of a demo game, called "Art Attack".

   Purpose of this package:
   -  to give a powerful and simple 3D engine using both 
      Java and OpenGL through JOGL 
   -  to provide a basis for modifications (simply
      called "mod's") by changing the code, and/or 
      graphics and sound. you are free to publish
      such modified games on your own website.
   -  to promote the New Computer Artworks.

   Prerequisites:
   -  you need to have a Sun Java 2 SDK installed, 
      at least JDK 1.6 + JOGL 1.1.1

   Intended audience:
   -  if you intend to change the code, you should have
      a good knowledge of Java programming, and the
      ability to learn by examples. I tried to add 
      as much source comments as possible, but you
      will have to find out many things by yourself.
   -  but you may also create new games just by
      changing the map, graphics and sounds.
      you are free to publish such modifications (mods)
      on your own website, but make sure that you
      include only artworks that are either made
      by yourself, or which are free for distribution
      (like the artworks supplied in this package).

   Credits:
   -  The 3-D graphics part is based on "java maze",
      created by Jonathan Thomas, www.shinelife.co.uk.
   -  Ogg Vorbis compression classes were downloaded from
      http://www.jcraft.com/jorbis/

   Features (added, since java maze):
   -  support for different screen and texture resolutions;
      now, from 64x64 to 512x512, everything's possible.
      256x256 proved to be the optimum compromise.
   -  high-color (15-bit) JPG based textures.
      truecolor was too slow, at least on a Pentium III 900 Mhz.
   -  dynamic texture and image loading:
      the stuff is loaded before playing.
   -  correct object drawing with depth sorting
   -  objects: rocket, robots, decoration and obstacles
   -  object/wall and object/rocket collision detection
      (and even rocket/rocket collision, sometimes)
   -  everywhere-compatible sound system, for JDK1 and JDK2
   -  Ogg Vorbis sound compression support (JDK2 only)
   -  loadable world map (worldmap.gif), with color-based
      object encoding. to be created/edited by paint program.
   -  area handling. an "area" is a special zone where robots
      are located, they attack the player once he steps in.
   -  text display 
   -  a stable timing system
   -  smoke on walls after rocket hit
   -  wall art images, to make propaganda
      for the futuristic computer art
   -  go to http://tuer.tuxfamily.org/project.html#requests

   Bug fixes:
   -  There was a crash when standing close to a wall.
      As far as I remember, this was a division by 0, due to some
      number over- or underflow. Search the source for "FIX for
      division by zero". I may have also added further value checks
      or modulo clippings, in any way, It didn't crash again.
   -  Fixed clipping of floor and ceiling.
   -  read CHANGES.RTF for more.
   -  go to http://tuer.tuxfamily.org/project.html#bugs

   Technical issues concerning the engine:
   -  in general, avoid that the player walks through
      wide open space (i.e., outside the building), 
      as this slows down the rendering.
   -  the sound system isn't very generic, it provides
      exactly the sounds needed for the supplied demo game,
      so replacing or extending the sounds can be a bit tricky.
      watch out for some hard-coded maxium sound lenghts
      or playback timings within d3sound*.java. 


   Contact and feedback:
   -  go to www.javagaming.org, my pseudo is gouessej.
      I have a little time to answer you

   Special note for those who wish to reuse and publish my code
   -  please don't not use the name "TUER". The license doesn't
      prevent you from doing it but it would be nice not to mix 
      everything.
   -  please, if your game made from mine is no more fully 
      cross-platform, precise it and precise that it is not the 
      case of TUER. 


   License and distribution
   ------------------------
   
   This program is free software; you can redistribute it and/or
   modify it under the terms of the GNU General Public License
   as published by the Free Software Foundation; either version 2
   of the License, or (at your option) any later version.
   This program is distributed in the hope that it will be useful,
   but WITHOUT ANY WARRANTY; without even the implied warranty of
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
   GNU General Public License for more details.
   You should have received a copy of the GNU General Public License
   along with this program; if not, write to the Free Software
   Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.

   Furthermore, all graphics (except some starting screens) 
   and music supplied in this package were created and 
   composed 2004 by Vincent Stahl, and they are also free 
   for distribution, either as they are, or in modified form.

   - - - - - - - - - - - - - - - - - - - - - - - - - - - -

            VINCENT STAHL - THE NEW COMPUTER ART

     If you ever have a minute, check my online gallery:

     http://www.StahlWorks.com

     providing free desktop backgrounds, mobile phone
     wallpapers, animations, music and more.
     And maybe you're interested in a state-of-the-art
     photoprint, mug or mousepad.

   - - - - - - - - - - - - - - - - - - - - - - - - - - - -
   
   	Julien GOUESSE - THE DEFENDER OF THE FREE ACCESS TO GAMING
	
	http://tuer.tuxfamily.org
