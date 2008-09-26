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

import com.jcraft.jogg.Packet;
import com.jcraft.jogg.Page;
import com.jcraft.jogg.StreamState;
import com.jcraft.jogg.SyncState;
import com.jcraft.jorbis.Block;
import com.jcraft.jorbis.Comment;
import com.jcraft.jorbis.DspState;
import com.jcraft.jorbis.Info;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.Random;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.Mixer;
import javax.sound.sampled.SourceDataLine;

/**
 * Sound System For JDK 2, providing Art Attack Sounds
 * This will be loaded and used automatically by the
 * game IF the JVM is providing a JDK 2 
 * sound environment.
 *
 * @author Vincent Stahl, Julien Gouesse
 */

public class SoundSystem implements ISoundSystem{
   
   
   private boolean bSound = false;
   
   private Random rg = null;      
   
   private boolean bDistSnd = true;
        
   private byte abSound[][] = null;
   
   private byte abMix[]     = null;         

   // this is the maximum over all distance-dependent
   // sound lengths. it's used both for the channel's
   // internal buffersize, and for abMix.
   private int maxSoundLength = 250000;
   
   /*private int aiPlayList[][] = {
      { 11, 12, 13, 14 },
      { 15, 15,16, 16,15 }
   };*/
    
   
   private AudioFormat   fmtLine  = null;
   
   private AudioFormat   fmtClip  = null;
   
   private DataLine.Info infoLine = null;
   
   private DataLine.Info infoClip = null;
   
   private SourceDataLine asdl[]  = null;
   
   private Clip aclip[] = null;
   
   private Mixer mixer   = null;
   
   private int maxchannels,maxclips;
   
   private int nchannels,nclips,iMusic;
   
   private int playing[];
   
   private long  playstart[];
   
   private String sSoundStatus = "";

   private static final boolean b16bit    = true;
   
   private static final int   nBaseSounds = 12;
   
   private static final int   iClips      = nBaseSounds;
   
   private static final int   nClips      = 9;
   
   private static final int   nMusicClips = 3;
   
   private static final int   nSounds     = nBaseSounds+nClips;
   
   private static final float fSampleRate = 32000.0f;

   private static final String aSoundFiles[] = {

      // BASE SOUNDS. length counts for maxSoundLength.
      "launch",      // 0 immediately
      "hit",         // 1 immediately
      "hit2",        // 2
      "hit3",        // 3
      "hit4",        // 4
      "applause",    // 5, fixed until here

      "bot1",        // 6
      "bot2",        // 7
      "bot3",        // 8
      "bot4",        // 9
      "term3",       // 10
      "rockpass",    // 11 NEW/1.1.0

      // CLIPS. do not influence maxSoundLength.
      "carpet",      // 0, 16 bit to fix loop play
      "walk",        // 1, 16 bit ditto
      "anno",        // 2, 16 bit ditto
      "mus1theone",  // 3, 16 bit ditto
      "mus2evil",    // 4, 16 bit ditto
      "mus3beyond",  // 5, 16 bit ditto
      "botwalk1",    // 6, 16 bit ditto. NEW/1.1.0
      "botwalk2",    // 7, 16 bit ditto. NEW/1.1.0
      "botwalk3",    // 8, 16 bit ditto. NEW/1.1.0
   };

   private void status(String s){
       System.out.println(s);
   }
   
   private int nextRand(int i){ 
       return (rg.nextInt()&65535)%i;
   }
   
   private long currentTime(){
       return System.currentTimeMillis();
   }

   public boolean loadSounds() {
      abSound = new byte[nSounds][];
      for (int i=0;i<nSounds;i++)
         abSound[i] = null;
      try {
         for (int i=0;i<nSounds;i++) 
	     {status("IMMEDIATE loading sounds ("+(i+1)+"/"+nSounds+")");
              if(i<iClips) 
	          abSound[i] = loadOgg(aSoundFiles[i], false);
              else 
	          abSound[i] = loadOgg(aSoundFiles[i], true);
	      /*if(aSoundFiles[i].equals("anno"))
	          playSound(i,0,0);*/
             }         
         abMix = new byte[maxSoundLength+100];
	 status("all sounds loaded.");
         return true;
      }  catch (Throwable e) {
         System.out.println("error loading sounds: "+e);
         e.printStackTrace();
         abSound = null;
         return false;
      }
   }

   // =============== begin ogg vorbis support ======================
   private void hexdump(byte ab[], int iOffset, int iLen)
   {
      String sHexDigits = "0123456789ABCDEF";
      for (int iRemain = iLen; iRemain > 0;) {
         String stmp  = new String(" >");
         String stmp2 = new String("");
         for (int i=0; i<Math.min(iRemain,16); i++) {
            char c = (char)(ab[i+iOffset] & 0xFF);
            stmp += sHexDigits.charAt(((int)c >> 4) & 0xF);
            stmp += sHexDigits.charAt(((int)c     ) & 0xF);
            if (i < 15)
               stmp += ' ';
            if ((i+1)%4==0 && i!=0 && i<13) stmp+=' ';
            if ( (c >=  32 && c <  127) ) stmp2 += c;
            else                          stmp2 += '.';
         }
         stmp += '<';
         iRemain -= 16;
         iOffset += 16;
         while (stmp.length() < 54)
            stmp += ' ';
         stmp += stmp2;
         System.out.println(stmp);
      }
   }
   private byte [] loadOgg(String sName, boolean isClip)throws Throwable{     
      InputStream is1=getClass().getResource("/snd/"+sName+".ogg").openStream();
      ByteArrayOutputStream bSound1 = new ByteArrayOutputStream();
      int r =0, idx = 0;
      byte ab[] = new byte[10240+10];
      while ((r=is1.read(ab, 0, 10240))>=0) {
         bSound1.write(ab,0,r);
         idx+=r;
      }
      is1.close();
      byte ab2[] = bSound1.toByteArray();

      is1 = new ByteArrayInputStream(ab2);

      // - - -

      final int BUFSIZE = 4096 * 1;
      int convsize = BUFSIZE * 1;

      byte[] convbuffer = new byte[convsize]; 
      byte[] buffer = null;

      int bytes=0;

      SyncState   oy = new SyncState();  
      StreamState os = new StreamState();
      Page        og = new Page();       
      Packet      op = new Packet();     
      Info        vi = new Info();       
      Comment     vc = new Comment();    
      DspState    vd = new DspState();   
      Block       vb = new Block(vd);    

      oy.init();

      ByteArrayOutputStream aout = new ByteArrayOutputStream();
      long lWritten=0;
      boolean bhead=false;

    loop:
      while(true) 
      {

      int eos=0;

      int index = oy.buffer(BUFSIZE);
      buffer = oy.data;
      try { 
         bytes = is1.read(buffer, index, BUFSIZE); 
      }  catch(Exception e) {
         System.err.println(e+" (d3s1)");
         return null;
      }
      if (bhead) {
         bhead = false;
         hexdump(buffer,index,bytes);
      }
      oy.wrote(bytes);
    
      if(oy.pageout(og)!=1) {
         if(bytes<BUFSIZE)break;
         System.err.println("Input does not appear to be an Ogg bitstream.");
         return null;
      }

      os.init(og.serialno());
      os.reset();

      vi.init();
      vc.init();

      if(os.pagein(og)<0) {
         System.err.println("Error reading first page of Ogg bitstream data.");
         return null;
      }
      if(os.packetout(op)!=1) {
         System.err.println("Error reading initial header packet.");
         break;
      }
      if(vi.synthesis_headerin(vc, op)<0) {
         System.err.println("This Ogg bitstream does not contain Vorbis audio data.");
         return null;
      }

      int i=0;

      while(i<2) 
      {
         while(i<2) 
         {
            int result = oy.pageout(og);
	         if(result==0) break; // Need more data
	         if(result==1) 
            {
               os.pagein(og);
  	            while(i<2) 
               {
         	      result = os.packetout(op);
	               if(result==0) break;
         	      if(result==-1) {
	                  System.err.println("Corrupt secondary header.  Exiting.");
                     break loop;
	               }
	               vi.synthesis_headerin(vc, op);
	               i++;
	            }
	         }
	      }  // endwhile
         index = oy.buffer(BUFSIZE);
         buffer = oy.data; 
         try { 
            bytes = is1.read(buffer, index, BUFSIZE); 
         }  catch(Exception e) {
            System.err.println(e+" (d3s2)");
            return null;
	      }
         if(bytes == 0 && i<2) {
	         System.err.println("End of file before finding all Vorbis headers!");
            return null;
         }
   	   oy.wrote(bytes);
      }  // endwhile

      {
         byte[][] ptr=vc.user_comments;
         StringBuffer sb=new StringBuffer();
         for(int j=0; j<ptr.length;j++){
            if(ptr[j]==null) break;
            System.err.println("Comment: "+new String(ptr[j], 0, ptr[j].length-1));
            if(sb!=null)sb.append(" "+new String(ptr[j], 0, ptr[j].length-1));
         }  
         // System.err.println("Bitstream is "+vi.channels+" channel, "+vi.rate+"Hz");
         // System.err.println("Encoded by: "+new String(vc.vendor, 0, vc.vendor.length-1)+"\n");
      }

      convsize = BUFSIZE/vi.channels;

      vd.synthesis_init(vi);
      vb.init(vd);
     
      float[][][] _pcmf=new float[1][][];
      int[] _index=new int[vi.channels];

      while(eos==0) 
      {
         while(eos==0) 
         {
      	   int result=oy.pageout(og);
	         if(result==0) break; // need more data
	         if(result==-1) { // missing or corrupt data at this page position
               System.err.println("Corrupt or missing data in bitstream; continuing...");
	         }
	         else 
            {
               os.pagein(og);
	            while(true) 
               {
         	      result=os.packetout(op);
         	      if(result==0) break; // need more data
         	      if(result==-1) { // missing or corrupt data at this page position
		               // no reason to complain; already complained above
                     System.err.println("err2");
	               }
                  else {
                     // we have a packet.  Decode it
	                  int samples;
         	         if(vb.synthesis(op)==0) { // test for success!
		                  vd.synthesis_blockin(vb);
		               }
         	         while((samples=vd.synthesis_pcmout(_pcmf, _index))>0) 
                     {
  	  	                  float[][] pcmf=_pcmf[0];
                        /*boolean clipflag=false;*/
               	      int bout=(samples<convsize?samples:convsize);

                        if (isClip) {
                         // double to 16-bit signed output conversion
 							    for(i=0;i<vi.channels;i++) {
							      int ptr=i;
							      int mono=_index[i];
							      for(int j=0;j<bout;j++) {
							         int val = (int)(pcmf[i][mono+j] * 32767);
							         if(val>32767) {
							            val = 32767;
							            /*clipflag = true;*/
							         }
							         if(val<-32768) {
							            val = -32768;
							            /*clipflag = true;*/
							         }
							         convbuffer[ptr++]=(byte)(val);
							         convbuffer[ptr++]=(byte)(val>>>8);
							      }
							    }
                         aout.write(convbuffer, 0, 2*vi.channels*bout);
                        }
                        else {
                         // double to 8-bit signed output conversion
 							    for(i=0;i<vi.channels;i++) {
							      int ptr=i;
							      int mono=_index[i];
							      for(int j=0;j<bout;j++) {
							         int val = (int)(pcmf[i][mono+j] * 127);
							         if(val>127) {
							            val = 127;
							            /*clipflag = true;*/
							         }
							         if(val<-128) {
							            val = -128;
							            /*clipflag = true;*/
							         }
                              // val += 128; // convert to unsigned
							         convbuffer[ptr]=(byte)(val);
							         ptr += vi.channels;
							      }
							    }
                         aout.write(convbuffer, 0, vi.channels*bout);
                        }
                        lWritten += vi.channels*bout;
			               vd.synthesis_read(bout);
		               }  // endwhile
	               }  // endelse
	            }  // endwhile true
	            if(og.eos()!=0) eos=1;
	         }  // endelse
         }  // endwhile

         if(eos==0) 
         {
            index = oy.buffer(BUFSIZE);
		      buffer = oy.data;
		      try { 
               bytes = is1.read(buffer,index,BUFSIZE); 
            }  catch(Exception e) {
               System.err.println(e+" (d3s3)");
               return null;
		      }
            if(bytes==-1)
               break;
            oy.wrote(bytes);
		      if(bytes==0) eos=1;
         }

      }  // endwhile

      os.clear();
      vb.clear();
      vd.clear();
      vi.clear();

      }  // endwhile loop

      oy.clear();
      is1.close();

      byte bs[] = aout.toByteArray();
      System.out.println(sName+": "+bs.length+" bytes decompressed ("+lWritten+")");
      // array is now SIGNED bytes.

      if (!isClip) 
      {
         // convert to 8 bit UNSIGNED
         for (int i=0;i<bs.length;i++)
            bs[i] = (byte)((128+bs[i])&0xFF);
      }

      // build maximum, return
      if (!isClip) {
         int ioldlen = maxSoundLength;
         maxSoundLength = Math.max(maxSoundLength, bs.length);
         if (maxSoundLength!=ioldlen) {
            // this should NOT happen and could lead
            // to sndbuf overflows, or sounds not played.
            // make sure maxSoundLength is pre-coded correctly.
            System.out.println("WARNING: maxsndlen adapted, "+maxSoundLength);
            abMix = new byte[maxSoundLength+100];
         }
      }
      return bs;
   }
   // =============== end ogg vorbis support ========================   
   
   public String soundInfo(){ 
       return sSoundStatus; 
   }   
   
   public boolean openSound()
   {
      rg = new java.util.Random(863153);

      try {
         // this is done IN CASE sound system fails.
         // the arrays will be re-allocated in case of success.
         asdl  = new SourceDataLine[32];
         aclip = new Clip[32];
         for (int i=0;i<32;i++) {
            asdl[i]  = null;
            aclip[i] = null;
         }

         // prepare search hooks
         fmtLine = new AudioFormat(
            AudioFormat.Encoding.PCM_UNSIGNED,
            fSampleRate, 8, 1, 1, fSampleRate, false
            );
         fmtClip = new AudioFormat(
            b16bit ? AudioFormat.Encoding.PCM_SIGNED : AudioFormat.Encoding.PCM_UNSIGNED,
            fSampleRate, b16bit ? 16 : 8, 1, b16bit ? 2 : 1, fSampleRate, false
            );
         System.out.println("=== checking the Java Sound System: ===");
         infoLine = new DataLine.Info(SourceDataLine.class, fmtLine);
         System.out.println("need: "+infoLine);
         infoClip = new DataLine.Info(Clip.class, fmtClip);
         System.out.println("need: "+infoClip);

         // check the system
         Mixer.Info am[] = AudioSystem.getMixerInfo();
         mixer = null;

         // 1. search mixer by name
         int n;
         for (n=0;n<am.length;n++)
            if (   am[n].getName().indexOf("Java Sound") >= 0
                && (mixer = AudioSystem.getMixer(am[n])) != null)
               break;
         // 1.2. check Java Sound for it's properties
         if (mixer != null) {
            maxchannels = mixer.getMaxLines(infoLine);
            System.out.println(am[n]+": "+maxchannels+" lines supported");
            maxclips = mixer.getMaxLines(infoClip);
            System.out.println(am[n]+": "+maxclips+" clips supported");
            if (maxchannels >= 4 && maxclips >= nclips)
               System.out.println("...match");
            else
               mixer = null;
         }

         if (mixer==null) {
            System.out.println("no matching Java Sound AudioMixer found.\n"
                              +"searching by properties...");
            // 2. search mixer by properties
            for (n=0;n<am.length;n++) {
               mixer = AudioSystem.getMixer(am[n]);
               maxchannels = mixer.getMaxLines(infoLine);
               System.out.println(am[n]+": "+maxchannels+" lines supported");
               maxclips = mixer.getMaxLines(infoClip);
               System.out.println(am[n]+": "+maxclips+" clips supported");
               if (maxchannels >= 4 && maxclips >= nclips) {
                  System.out.println("...match");
                  break;
               }
            }
            if (n==am.length)
               mixer = null;
         }

         if (mixer==null)
            throw new Exception("no Java AudioMixer found. "
               +"Please check your Java+sound system, or use the Simple Edition.");

         // establish lines
         nchannels   = Math.min(8,maxchannels);
         if (nchannels<2)
            throw new Exception("too few lines supported ("+maxchannels+")");
         iMusic = nchannels; // currently NOT used, is done by clips
         asdl = new SourceDataLine[nchannels];
         for (int i=0;i<nchannels;i++) {
            asdl[i] = (SourceDataLine)mixer.getLine(infoLine);
            //FIXME: find a way to detect that the line is unavailable
            if (i >= iMusic)
               asdl[i].open(fmtLine, 500000+64000+4096); // not used
            else
               asdl[i].open(fmtLine, maxSoundLength+4096);
            asdl[i].start();
         }

         // establish clips
         nclips   = Math.min(nClips,maxclips);
         if (nclips<nClips)
            throw new Exception("too few clips supported ("+maxclips+")");

         // create empty clip array
         aclip = new Clip[nclips];
         for (int i=0;i<nclips;i++)
            aclip[i] = null;

         // immediately establish carpet and walk.
         loadSounds();  // loads these 2 only
         // all further clips are created in stepSoundLoading.
         for (int i=0;i<2;i++) {
            aclip[i] = (Clip)mixer.getLine(infoClip);
            aclip[i].open(fmtClip, abSound[iClips+i], 0, abSound[iClips+i].length);
         }
         startCarpetSound();

         // init control channels
         playing = new int[nchannels];
         playstart = new long[nchannels];
         for (int i=0; i<nchannels; i++) {
            playing[i] = -1;
            playstart[i] = currentTime();
         }
         bSound = true;
         System.out.println("using "+nchannels+" lines and "+nclips+" clips.");
         System.out.println("=== Sound System up and running. ===");
         sSoundStatus = "Java 2 Sound with Ogg Vorbis";
         return true;
      }  catch (Throwable t) {
         System.err.println("Error: "+t.getMessage());
         t.printStackTrace();
         // sSystemError = "Error: "+t.getMessage();
         status("");
         System.out.println("=== Sound System startup failed. ===");
         sSoundStatus = "Sound Error - please check console";                  
         return false;
      }
   }
   public void startMovingSound(int iMask) {
      for (int i=0; i<nClips; i++)
      {
         if (i >= 2 && i < 6)
            continue;   // these are no motion sounds
         int lmask = 1<<i;
         if ((iMask & lmask)!=0 && aclip[i]!=null) {
            aclip[i].stop();
            aclip[i].setFramePosition(0);
            aclip[i].loop(Clip.LOOP_CONTINUOUSLY);
         }
      }
   }
   public void stopMovingSound(int iMask) {
      for (int i=0; i<nClips; i++)
      {
         if (i==1 || i>=6) // filter only real motion sounds
         {
            int lmask = 1<<i;
            if ((iMask & lmask)!=0 && aclip[i]!=null) {
               if (aclip[i].isRunning())
                  aclip[i].loop(0);
               else {
                  aclip[i].stop();
                  aclip[i].setFramePosition(0);
               }
            }
         }
      }
   }
   public void startCarpetSound() {
      if (aclip[0]==null)
         return;
      if (aclip[0].isRunning())
         return;
      aclip[0].stop();
      aclip[0].setFramePosition(0);
      aclip[0].loop(Clip.LOOP_CONTINUOUSLY);
   }
   public void stopAllSounds() {
      for (int i=0;i<aclip.length;i++)
         if (aclip[i]!=null)
            aclip[i].stop();
   }
   public void stopCarpetSound() {
      if (aclip[0]==null)
         return;
      aclip[0].stop();
      aclip[0].setFramePosition(0);
   }
   public void playAreaCleared() {
      stopMusic();
      playSound(5, 128<<16, 128<<16); // applause
      int i = 3+nextRand(nMusicClips);
      if (aclip[i]==null)
         return;
      aclip[i].stop();
      aclip[i].setFramePosition(0);
      aclip[i].loop(1);
   }

   // SINGLE SHOT SOUNDS
   public void playBotGreeting() {
      playSound(6, 128<<16, 128<<16);
   }
   public void playBotHit(int x1,int z1,int x2,int z2) {
      int r = nextRand(6);
      if (r <= 1)
         playSound(7,x1,z1,x2,z2);
      else
         playSound(8+nextRand(2),x1,z1,x2,z2);
   }
   public void playTermSound() {
      stopMusic();
      playSound(10, 128<<16, 128<<16);
   }

   public void closeSound() {
      if (bSound) {
         for (int i=0;i<asdl.length;i++) {
            asdl[i].stop();
            asdl[i].close();
         }
         for (int i=0;i<aclip.length;i++) {
            if (aclip[i]!=null) {
               aclip[i].stop();
               aclip[i].close();
            }
         }
         bSound = false;
      }
   }

   public void setSoundOption(String sOpt) {
      if (sOpt.equals("dist-snd:false"))   bDistSnd=false;
      if (sOpt.equals("dist-snd:true"))    bDistSnd=true;
   }

   static int aSoundMap[] = {
      0, 1, 1, 1, 1
   };
   public void playSound(int id,int x,int z) {
      playSound(id,x,z,-1,-1);
   }
   public void playSound(int id,int x,int z,int iPlayerX, int iPlayerZ)
   {
      if (!bSound || abSound==null)
         return;

      byte ab[] = abSound[id];
      if (ab==null) { // not (yet) loaded
         if (id > 4)
            return;
         ab = abSound[aSoundMap[id%aSoundMap.length]];
      }

      try
      {
         // get oldest channel
         int ichan=0,icheck;
         long lnow=currentTime();
         long lmax=0;
         for (icheck=0; icheck<iMusic; icheck++)
            if (lnow-playstart[icheck] > lmax) {
               lmax =lnow-playstart[icheck];
               ichan=icheck;
            }

         playing[ichan]    = id;
         playstart[ichan]  = lnow;

         int dx=0, dz=0;
         if (bDistSnd && iPlayerX != -1) {
            dx = Math.abs((int)iPlayerX-x)>>16;
            dz = Math.abs((int)iPlayerZ-z)>>16;
         }
         if (dx != 0 || dz != 0)
         {
            // adjust loudness to distance of event.
            // how far is the sound away?
            final int fad = 50; // farthest audible distance
            final int fadSquare = ((int)Math.sqrt(fad*fad+fad*fad))+1;
            int ndiv = (((int)Math.sqrt(dx*dx+dz*dz))*50/fadSquare)+10;
            // adapt loudness to distance.
            // REMEMBER this is UNSIGNED sound data,
            // silence is 128!
            for (int i=0; i<ab.length; i++)
               abMix[i] = (byte)((((ab[i]&0xFF)-128)*10/ndiv) + 128);

            if (asdl[ichan].available() < ab.length+100)
                asdl[ichan].flush();
            if (asdl[ichan].available() > ab.length)
                asdl[ichan].write(abMix,0,ab.length);
         }
         else 
         {
            // distance-independent play (default):
            if (asdl[ichan].available() < ab.length+100)
                asdl[ichan].flush();
            if (asdl[ichan].available() > ab.length)
                asdl[ichan].write(ab,0,ab.length);
            else
                System.out.println("warning: sound line overflow");
         }
      }
      catch (Throwable t) {
         System.out.println("playSound error: "+t);
      }
   }   

   // 1 sec == 32000 bytes
   public void stepMusic() { }
   
   public void stopMusic() { }
   
   public void restartMusic() { }  
    
}
