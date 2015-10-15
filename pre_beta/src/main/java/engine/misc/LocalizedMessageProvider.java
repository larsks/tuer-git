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
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Provider of localized messages. It looks for the language chosen by the end user in the configuration file. It uses the 
 * default language of the operating system as a fallback.
 * 
 * @author Julien Gouesse
 *
 */
public class LocalizedMessageProvider{
	
	private static final Logger logger=Logger.getLogger(LocalizedMessageProvider.class.getCanonicalName());
	
	private ResourceBundle resourceBundle;

	public LocalizedMessageProvider(final String gameFilesSubDirPath){
		super();
		Locale locale=Locale.getDefault();
		//TODO move the reading of the configuration file into a separate class
		//looks at the file that contains the settings as the end user might have chosen another language
		final File configFile=new File(System.getProperty("user.home")+"/"+gameFilesSubDirPath,"config");
		if(configFile.exists())
		    {//tries to read the ISO 639 alpha-2 or alpha-3 language code
			 final Properties properties=new Properties();
			 try(final FileReader fileReader=new FileReader(configFile);final BufferedReader bufferedReader=new BufferedReader(fileReader))
			     {properties.load(bufferedReader);
				  final String languageCode=properties.getProperty("LANGUAGE","en");
				  locale=new Locale(languageCode);
				  logger.log(Level.INFO,"Language code \""+languageCode+"\" found in the configuration file "+configFile.getAbsolutePath());
			     }
			 catch(IOException ioe)
			     {//something wrong has just happened while reading the configuration file
		          logger.log(Level.WARNING,"Cannot read the configuration file "+configFile.getAbsolutePath(),ioe);
			     }
		    }
		else
		    {//the configuration file is absent
			 logger.log(Level.WARNING,"Cannot find the configuration file "+configFile.getAbsolutePath());
		    }
		resourceBundle=ResourceBundle.getBundle("i18n.MessagesBundle",locale);
	}
	
	public String getString(final String key){
		return(resourceBundle.getString(key));
	}
}
