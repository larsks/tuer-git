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
import java.io.File;
import java.io.FileReader;
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
	
	private static final Logger logger=Logger.getLogger(SettingsProvider.class.getCanonicalName());
	
	private static final String[] trueStrings={Boolean.TRUE.toString(),"on","1","enabled","activated"};
	
	private static final String[] falseStrings={Boolean.FALSE.toString(),"off","0","disabled","deactivated"};
	
	/**locale*/
	private Locale locale;
	
	private boolean verticalSynchronizationEnabled;
	
	private boolean fullscreenEnabled;

	/**
	 * Default constructor
	 * 
	 * @param gameFilesSubDir sub-directory of the game, in the user's home directory
	 */
	public SettingsProvider(final String gameFilesSubDir){
		super();
		//default values
		locale=Locale.getDefault();//locale of the system
		verticalSynchronizationEnabled=false;
		fullscreenEnabled=true;
		//looks at the file that contains the settings
		final File configFile=new File(System.getProperty("user.home")+"/"+gameFilesSubDir,"config");
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
	
	public boolean isFullscreenEnabled(){
		return(fullscreenEnabled);
	}
	
	/**
	 * Returns the locale
	 * 
	 * @return locale
	 */
	public Locale getLocale(){
		return(locale);
	}
}
