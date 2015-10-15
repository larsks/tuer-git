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
import java.util.Date;
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
	
	private static final Logger logger=Logger.getLogger(SettingsProvider.class.getCanonicalName());
	
	private static final String[] trueStrings={Boolean.TRUE.toString(),"on","1","enabled","activated"};
	
	private static final String[] falseStrings={Boolean.FALSE.toString(),"off","0","disabled","deactivated"};
	
	/**program short name used to name the sub-directory, in the user's home directory*/
	private final String programShortName;
	
	/**locale*/
	private Locale locale;
	
	/**configuration file*/
	private final File configFile;
	
	private boolean verticalSynchronizationEnabled;
	
	private boolean fullscreenEnabled;

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
	
	public void save(){
		final Properties properties=new Properties();
		properties.put("LANGUAGE",locale.getLanguage());
		properties.put("VSYNC",Boolean.toString(verticalSynchronizationEnabled));
		properties.put("FULLSCREEN",Boolean.toString(fullscreenEnabled));
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
