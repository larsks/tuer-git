/**
 * Copyright (c) 2006-2015 Julien Gouesse
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package engine.misc;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Locale;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Provider of the settings read from the configuration file (if any).
 * 
 * @author Julien Gouesse
 *
 */
public class SettingsProvider{
	
	public static final int UNCHANGED_SIZE=-1;
	
	private static final Logger logger=Logger.getLogger(SettingsProvider.class.getCanonicalName());
	
	private static final String[] trueStrings={Boolean.TRUE.toString(),"on","1","enabled","activated"};
	
	private static final String[] falseStrings={Boolean.FALSE.toString(),"off","0","disabled","deactivated"};
	
	private static final int[] screenRotations=new int[]{0,90,180,270};
	
	/**program short name used to name the sub-directory, in the user's home directory*/
	private final String programShortName;
	
	/**locale*/
	private Locale locale;
	
	/**configuration file*/
	private final File configFile;
	
	private boolean verticalSynchronizationEnabled;
	
	/**fullscreen or windowed*/
	private boolean fullscreenEnabled;
	
	/**screen width*/
	private int screenWidth;
	
	/**screen height*/
	private int screenHeight;
	
	/**screen rotation*/
	private int screenRotation;
	
	/**sound enabled*/
	private boolean soundEnabled;

	/**
	 * Default constructor
	 * 
	 * @param programShortName program short name used to name the sub-directory, in the user's home directory
	 */
	public SettingsProvider(final String programShortName){
		super();
		this.programShortName=programShortName;
		//default values
		locale=Locale.getDefault();//locale of the system
		verticalSynchronizationEnabled=false;
		fullscreenEnabled=true;
		screenWidth=UNCHANGED_SIZE;
		screenHeight=UNCHANGED_SIZE;
		screenRotation=0;
		soundEnabled=true;
		//looks at the file that contains the settings
		configFile=new File(System.getProperty("user.home")+"/."+programShortName,"config");
		if(configFile.exists())
	        {final Properties properties=new Properties();
	         try(final FileReader fileReader=new FileReader(configFile);final BufferedReader bufferedReader=new BufferedReader(fileReader))
		         {properties.load(bufferedReader);
		          logger.log(Level.INFO,"Configuration file "+configFile.getAbsolutePath()+" found");
		          //language property
		          //tries to read the ISO 639 alpha-2 or alpha-3 language code
			      final String languageCode=properties.getProperty("LANGUAGE");
			      if(languageCode!=null&&!languageCode.isEmpty())
			          {locale=new Locale(languageCode);
			           logger.log(Level.INFO,"Language code \""+languageCode+"\" found, uses the language "+locale.getDisplayLanguage());
			          }
			      else
			          {//language not set
			    	   logger.log(Level.INFO,"Language code not found, uses the default language "+locale.getDisplayLanguage());
			          }
			      //vertical synchronization
			      final String vSyncString=properties.getProperty("VSYNC");
			      verticalSynchronizationEnabled=parseBoolean(vSyncString,Boolean.valueOf(verticalSynchronizationEnabled));
			      if(vSyncString!=null&&!vSyncString.isEmpty())
			    	  logger.log(Level.INFO,"Vertical synchronization flag \""+vSyncString+"\" found, vertical synchronization "+verticalSynchronizationEnabled);
			      else
			    	  logger.log(Level.INFO,"Vertical synchronization flag not found, vertical synchronization "+verticalSynchronizationEnabled);
			      //fullscreen
			      final String fullscreenString=properties.getProperty("FULLSCREEN");
			      fullscreenEnabled=parseBoolean(fullscreenString,Boolean.valueOf(fullscreenEnabled));
			      if(fullscreenString!=null&&!fullscreenString.isEmpty())
			    	  logger.log(Level.INFO,"Fullscreen flag \""+fullscreenString+"\" found, fullscreen "+fullscreenEnabled);
			      else
			    	  logger.log(Level.INFO,"Fullscreen flag not found, fullscreen "+fullscreenEnabled);
			      //screen width
			      final String screenWidthString=properties.getProperty("SCREEN_WIDTH");
			      screenWidth=parseInt(screenWidthString,Integer.valueOf(screenWidth),null);
			      if(screenWidthString!=null&&!screenWidthString.isEmpty())
			    	  logger.log(Level.INFO,"Screen width flag \""+screenWidthString+"\" found, screen width "+screenWidth);
			      else
			    	  logger.log(Level.INFO,"Screen width flag not found, screen width "+screenWidth);
			      //screen height
			      final String screenHeightString=properties.getProperty("SCREEN_HEIGHT");
			      screenHeight=parseInt(screenHeightString,Integer.valueOf(screenHeight),null);
			      if(screenHeightString!=null&&!screenHeightString.isEmpty())
			    	  logger.log(Level.INFO,"Screen height flag \""+screenHeightString+"\" found, screen height "+screenHeight);
			      else
			    	  logger.log(Level.INFO,"Screen height flag not found, screen height "+screenHeight);
			      //screen rotation
			      final String screenRotationString=properties.getProperty("SCREEN_ROTATION");
			      screenRotation=parseInt(screenRotationString,Integer.valueOf(screenRotation),screenRotations);
			      if(screenRotationString!=null&&!screenRotationString.isEmpty())
			    	  logger.log(Level.INFO,"Screen rotation flag \""+screenRotationString+"\" found, screen rotation "+screenRotation);
			      else
			    	  logger.log(Level.INFO,"Screen rotation flag not found, screen rotation "+screenRotation);
			      //sound enabled
			      final String soundEnabledString=properties.getProperty("SOUND_ENABLED");
			      soundEnabled=parseBoolean(soundEnabledString,Boolean.valueOf(soundEnabled));
			      if(soundEnabledString!=null&&!soundEnabledString.isEmpty())
			    	  logger.log(Level.INFO,"Sound enabled flag \""+soundEnabledString+"\" found, sound enabled "+soundEnabled);
			      else
			    	  logger.log(Level.INFO,"Sound enabled flag not found, sound enabled "+soundEnabled);
		         }
		     catch(IOException ioe)
		         {//something wrong has just happened while reading the configuration file
	              logger.log(Level.WARNING,"Something wrong has occured while reading the configuration file "+configFile.getAbsolutePath(),ioe);
		         }
	        }
		else
			{//the configuration file is absent
			 logger.log(Level.WARNING,"Cannot find the configuration file "+configFile.getAbsolutePath());
			}
	}
	
	private boolean parseBoolean(final String string,final Boolean defaultValue){
		boolean result=defaultValue==null?false:defaultValue.booleanValue();
		if(string!=null&&!string.isEmpty())
		    {for(final String trueString:trueStrings)
			     if(string.equalsIgnoreCase(trueString))
			         {result=true;
			    	  break;
			         }
		     for(final String falseString:falseStrings)
			     if(string.equalsIgnoreCase(falseString))
			         {result=false;
			    	  break;
			         }
		    }
		return(result);
	}
	
	private int parseInt(final String string,final Integer defaultValue,final int[] acceptedValues){
		int result=defaultValue==null?0:defaultValue.intValue();
		if(string!=null&&!string.isEmpty())
		    {final int resultCandidate=Integer.parseInt(string);
		     if(acceptedValues!=null&&acceptedValues.length>0)
			     {for(final int acceptedIntValue:acceptedValues)
		              if(resultCandidate==acceptedIntValue)
		                  {result=resultCandidate;
		        	       break;
		                  }
			     }
		     else
		    	 result=resultCandidate;
		    }
		return(result);
	}
	
	/**
	 * Tells whether the vertical synchronization is enabled
	 * 
	 * @return <code>true</code> if the vertical synchronization is enabled, otherwise <code>false</code>
	 */
	public boolean isVerticalSynchronizationEnabled(){
		return(verticalSynchronizationEnabled);
	}
	
	public void setVerticalSynchronizationEnabled(final boolean verticalSynchronizationEnabled){
		this.verticalSynchronizationEnabled=verticalSynchronizationEnabled;
	}
	
	public boolean isFullscreenEnabled(){
		return(fullscreenEnabled);
	}
	
	public void setFullscreenEnabled(final boolean fullscreenEnabled){
		this.fullscreenEnabled=fullscreenEnabled;
	}
	
	/**
	 * Returns the locale
	 * 
	 * @return locale
	 */
	public Locale getLocale(){
		return(locale);
	}
	
	
	public void setLocale(final Locale locale){
		if(locale==null)
		    throw new IllegalArgumentException("The locale cannot be set to null");
		this.locale=locale;
	}
	
	public int getScreenRotation(){
		return(screenRotation);
	}
	
	public void setScreenRotation(final int screenRotation){
		this.screenRotation=screenRotation;
	}
	
	public boolean isSoundEnabled(){
		return(soundEnabled);
	}
	
	public void setSoundEnabled(final boolean soundEnabled){
		this.soundEnabled=soundEnabled;
	}
	
	public int getScreenWidth(){
		return(screenWidth);
	}
	
	public void setScreenWidth(final int screenWidth){
		this.screenWidth=screenWidth;
	}
	
	public int getScreenHeight(){
		return(screenHeight);
	}
	
	public void setScreenHeight(final int screenHeight){
		this.screenHeight=screenHeight;
	}
	
	public void save(){
		final Properties properties=new Properties();
		properties.put("LANGUAGE",locale.getLanguage());
		properties.put("VSYNC",Boolean.toString(verticalSynchronizationEnabled));
		properties.put("FULLSCREEN",Boolean.toString(fullscreenEnabled));
		properties.put("SCREEN_WIDTH",Integer.toString(screenWidth));
		properties.put("SCREEN_HEIGHT",Integer.toString(screenHeight));
		properties.put("SCREEN_ROTATION",Integer.toString(screenRotation));
		properties.put("SOUND_ENABLED",Boolean.toString(soundEnabled));
		try
		    {final File parentDir=configFile.getParentFile();
			 if(!parentDir.exists())
				 parentDir.mkdirs();
			 if(!configFile.exists())
				 configFile.createNewFile();
			 try(final FileWriter fileWriter=new FileWriter(configFile);final BufferedWriter bufferedWriter=new BufferedWriter(fileWriter))
			     {final String comments=programShortName+" configuration file";
				  properties.store(bufferedWriter, comments);
				 }
			 logger.log(Level.INFO,"Settings saved into the configuration file "+configFile.getAbsolutePath());
		    }
		catch(Throwable t)
		{logger.log(Level.WARNING,"Something wrong has happened while trying to save the settings into the configuration file "+configFile.getAbsolutePath(),t);}
	}
}
