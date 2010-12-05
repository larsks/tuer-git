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
package engine;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Logger;

public final class DesktopIntegration {
	
	private static Logger logger=Logger.getLogger(DesktopIntegration.class.getCanonicalName());
	
	private static final DesktopIntegration instance=new DesktopIntegration();
	
	private final String desktopPath;
	
	private enum OS{
		Linux("desktop",new String[]{"[Desktop Entry]","Comment=","Exec=javaws ","GenericName=","Icon=","MimeType=","Name=","Path=","StartupNotify=false","Terminal=false","TerminalOptions=","Type=Application","X-DBUS-ServiceName=","X-DBUS-StartupType=","X-KDE-SubstituteUID=false","X-KDE-Username="},6,2),
		Mac("",null,-1,-1),
		Other(null,null,-1,-1),
		//do not use LNK file format as it differs depending on the version
		Windows("bat",new String[]{System.getProperty("java.home")+System.getProperty("file.separator")+"bin"+System.getProperty("file.separator")+"javaws.exe "},-1,0);
		
		private final String desktopShortcutFileExtension;
		
		private final String[] desktopShortcutFileContent;
		
		private final int desktopShortcutFileNameLineIndex;
		
		private final int desktopShortcutFileExecutableCommandLineIndex;
		
	    private OS(final String desktopShortcutFileExtension,final String[] desktopShortcutFileContent,final int desktopShortcutFileNameLineIndex,final int desktopShortcutFileExecutableCommandLineIndex){
			this.desktopShortcutFileExtension=desktopShortcutFileExtension;
			this.desktopShortcutFileContent=desktopShortcutFileContent;
			this.desktopShortcutFileNameLineIndex=desktopShortcutFileNameLineIndex;
			this.desktopShortcutFileExecutableCommandLineIndex=desktopShortcutFileExecutableCommandLineIndex;
		}
	    
	    private final String getDesktopShortcutFileExtension(){
	    	return(desktopShortcutFileExtension);
	    }
	    
	    private final String[] getDesktopShortcutFileContent(){
	    	return(desktopShortcutFileContent);
	    }
	    
	    private final int getDesktopShortcutFileNameLineIndex(){
	    	return(desktopShortcutFileNameLineIndex);
	    }
	    
	    private final int getDesktopShortcutFileExecutableCommandLineIndex(){
	    	return(desktopShortcutFileExecutableCommandLineIndex);
	    }
	};
	
	private final OS operatingSystem;
	
	private DesktopIntegration(){
		/**
		 * finds the desktop path of the current operating system
		 */
		final String osName=System.getProperty("os.name").toLowerCase();
		logger.info("operating system: "+osName);
		if(osName.contains("linux"))
		    {operatingSystem=OS.Linux;
			 logger.info("operating system family: Linux");
			 //XDG_DESKTOP_DIR is the environment variable that contains the path of the desktop directory on Linux
			 String XDG_DESKTOP_DIR=System.getenv("XDG_DESKTOP_DIR");
		     /**
		      * workaround of a KDE bug. GNOME fills this environment variable correctly, not KDE.
		      */
		     final String userHome=System.getProperty("user.home");
		     if(XDG_DESKTOP_DIR==null||XDG_DESKTOP_DIR.equals(""))
		         {logger.warning("XDG_DESKTOP_DIR is not set, use a workaround for non-GNOME window managers");
		    	  //this script contains the configuration of the user directories including the directory used by the desktop
		    	  final String configUserDirsScriptPath=userHome+System.getProperty("file.separator")+".config/user-dirs.dirs";
		    	  final File configUserDirsScript=new File(configUserDirsScriptPath);
		    	  //if this script exists
		    	  if(configUserDirsScript.exists())
		    	      {logger.info(configUserDirsScriptPath+" exists, parse it to find XDG_DESKTOP_DIR");
		    		   //N.B: do not source the script to be as neutral as possible (to avoid changing the environment)
		    		   try
		    	          {//reads this file, line by line
		    			   final BufferedReader reader=new BufferedReader(new FileReader(configUserDirsScript));
		    	           String line=null;
		    	           while((line=reader.readLine())!=null)
		    	        	   if(line.startsWith("XDG_DESKTOP_DIR"))
		    	                   {final String[] splitLine=line.split("=");
		    	                    if(splitLine.length==2)
		    	                    	XDG_DESKTOP_DIR=splitLine[1].replaceAll("\"","");
		    	        	        break;
		    	                   }
		    	           reader.close();
					      } 
		    	       catch(FileNotFoundException fnfe) 
					   {fnfe.printStackTrace();}
		    	       catch(IOException ioe)
		    	       {ioe.printStackTrace();}
		    	      }
		    	  else
		    		  {logger.warning(configUserDirsScriptPath+" does not exist, look at the default values of the operating system");
		    		   //this script contains the system-wide default values for XDG user directories
		    		   final String defaultConfigUsersDirsScriptPath="/etc/xdg/user-dirs.defaults";
		    		   final File defaultConfigUsersDirsScript=new File(defaultConfigUsersDirsScriptPath);
		    		   if(defaultConfigUsersDirsScript.exists())
		    		       {logger.info(defaultConfigUsersDirsScript+" exists, parse it to find the DESKTOP tag");
		    			    //N.B: do not source the script to be as neutral as possible (to avoid changing the environment)
			    		    try
			    	           {//reads this file, line by line
			    			    final BufferedReader reader=new BufferedReader(new FileReader(defaultConfigUsersDirsScript));
			    	            String line=null;
			    	            while((line=reader.readLine())!=null)
			    	         	    if(line.startsWith("DESKTOP"))
			    	                    {final String[] splitLine=line.split("=");
			    	                     if(splitLine.length==2)
			    	                         XDG_DESKTOP_DIR=userHome+System.getProperty("file.separator")+splitLine[1].replaceAll("\"","");
			    	        	         break;
			    	                    }
			    	            reader.close();
						       } 
			    	        catch(FileNotFoundException fnfe) 
						    {fnfe.printStackTrace();}
			    	        catch(IOException ioe)
			    	        {ioe.printStackTrace();}		    			    
		    		       }
		    		   else
		    			   logger.warning(defaultConfigUsersDirsScriptPath+" does not exist. There is no way to get the value of XDG_DESKTOP_DIR");
		    		  }
		         }
		     else
		    	 logger.info("XDG_DESKTOP_DIR is set, use its value");
		     if(XDG_DESKTOP_DIR==null||XDG_DESKTOP_DIR.equals(""))
		    	 {logger.warning("XDG_DESKTOP_DIR is not set and the workarounds have failed");
		    	  desktopPath=null;
		    	 }
		     else
		    	 {logger.info("XDG_DESKTOP_DIR raw value: "+XDG_DESKTOP_DIR);
		    	  /**
		    	   * XDG_DESKTOP_DIR can contain some references to other environment variables. These references have 
		    	   * to be replaced by their values to get the absolute path of the desktop directory
		    	   */
		    	  Map<String,String> environmentVarsMap=System.getenv();
		    	  String environmentVariable;
		    	  int indexOfEnvironmentVariableOccurrence;
		    	  for(Entry<String,String> entry:environmentVarsMap.entrySet())
		    	      {environmentVariable=entry.getKey();
		    	       /**
		    	        * It uses indexOf() and substring() because replaceAll() cannot be used as this method uses 
		    	        * regular expressions and interprets '$' as a punctuation character which is often in the names of
		    	        * environment variables.
		    	        */
		    	       indexOfEnvironmentVariableOccurrence=XDG_DESKTOP_DIR.indexOf(environmentVariable);
		    	       if(indexOfEnvironmentVariableOccurrence!=-1)
		    	    	   XDG_DESKTOP_DIR=entry.getValue()+XDG_DESKTOP_DIR.substring(indexOfEnvironmentVariableOccurrence+environmentVariable.length(),XDG_DESKTOP_DIR.length());
		    	      }
		    	  logger.info("XDG_DESKTOP_DIR value: "+XDG_DESKTOP_DIR);
		          desktopPath=XDG_DESKTOP_DIR;
		    	 }
		    }
		else
			if(osName.contains("mac"))
		        {operatingSystem=OS.Mac;
				 logger.info("operating system family: Mac");
				 desktopPath=null;
		        }
		    else
		    	if(osName.contains("windows"))
			        {operatingSystem=OS.Windows;
		    		 logger.info("operating system family: Windows");
		    		 final String REGQUERY_UTIL="reg query ";
			         final String REGSTR_TOKEN="REG_SZ";
			         final String DESKTOP_FOLDER_CMD=REGQUERY_UTIL+"\"HKCU\\Software\\Microsoft\\Windows\\CurrentVersion\\Explorer\\Shell Folders\" /v DESKTOP";
			         String registryValue=null;
			         try
			            {Process process=Runtime.getRuntime().exec(DESKTOP_FOLDER_CMD);
			             StreamReader reader=new StreamReader(process.getInputStream());
			             reader.start();
			             process.waitFor();
			             reader.join();
			             String result=reader.getResult();
			             int p=result.indexOf(REGSTR_TOKEN);
			             if(p==-1)
			            	 registryValue=null;
			             else 
			            	 registryValue=result.substring(p+REGSTR_TOKEN.length()).trim();
			            }
			         catch(Exception e)
			         {e.printStackTrace();}
		    		 desktopPath=registryValue;
			        }
			    else
			    	{operatingSystem=OS.Other;
			    	 logger.warning("unknown operating system family");
			    	 desktopPath=null;
			    	}
		if(desktopPath!=null)
		    {logger.info("desktop path: "+desktopPath);
		     if(operatingSystem.equals(OS.Linux))
		         logger.info("operating system supported");
		     else
		    	 if(operatingSystem.equals(OS.Mac))		    	     
		    	     logger.warning("operating system not yet supported");		    	     
		    	 else
		    		 if(operatingSystem.equals(OS.Windows))		    		     
		    		     logger.warning("operating system not yet supported");		    		     
		    		 else		    			 
		    			 logger.warning("operating system not supported");		    			 
		    }
		else		
			logger.warning("desktop path not found");
	}
	
	public static final boolean isDesktopShortcutCreationSupported(){
		return(instance.desktopPath!=null&&instance.operatingSystem.getDesktopShortcutFileContent()!=null);
	}
	
	public static final boolean createDesktopShortcut(final String desktopShortcutFilenameWithoutExtension,final String javaWebStartJNLPFileUrl){
		final boolean success;
		if(!isDesktopShortcutCreationSupported())
			success=false;
		else
		    {final File desktopShortcutFile=new File(instance.desktopPath+System.getProperty("file.separator")+desktopShortcutFilenameWithoutExtension+"."+instance.operatingSystem.getDesktopShortcutFileExtension());
			 //tries to delete the file if it already exists
		     if(desktopShortcutFile.exists()&&!desktopShortcutFile.delete())
				 success=false;
			 else
			     {boolean fileCreationSuccess=false;
			      //(re)creates the file
			      try{fileCreationSuccess=desktopShortcutFile.createNewFile();
			          //drives this file executable so that the operating system does not mark it as untrusted
			          desktopShortcutFile.setExecutable(true,true);
			         }
			      catch(IOException ioe)
			      {ioe.printStackTrace();}
				  if(!fileCreationSuccess)
				      {logger.warning("the desktop shortcut file"+desktopShortcutFile.getAbsolutePath()+" has not been successfully created");
					   success=false;
				      }
				  else
				      {logger.info("the desktop shortcut file"+desktopShortcutFile.getAbsolutePath()+" has been successfully created");
					   final String[] src=instance.operatingSystem.getDesktopShortcutFileContent();
				       final String[] desktopShortcutFileContent=new String[src.length];
				       System.arraycopy(src,0,desktopShortcutFileContent,0,src.length);
				       //fills the future content of the file with the parameters
				       final int desktopShortcutFileExecutableCommandLineIndex=instance.operatingSystem.getDesktopShortcutFileExecutableCommandLineIndex();
				       desktopShortcutFileContent[desktopShortcutFileExecutableCommandLineIndex]=desktopShortcutFileContent[desktopShortcutFileExecutableCommandLineIndex]+javaWebStartJNLPFileUrl;
				       final int desktopShortcutFileNameLineIndex=instance.operatingSystem.getDesktopShortcutFileNameLineIndex();
				       if(desktopShortcutFileNameLineIndex!=-1)
				           desktopShortcutFileContent[desktopShortcutFileNameLineIndex]=desktopShortcutFileContent[desktopShortcutFileNameLineIndex]+desktopShortcutFilenameWithoutExtension;
				       boolean fileWritingSuccess=true;
					   //writes the content of the file
					   try
					      {PrintWriter pw=new PrintWriter(desktopShortcutFile);
					       for(String line:desktopShortcutFileContent)
					    	   pw.println(line);
					       pw.close();
					      }
					   catch(FileNotFoundException fnfe)
					   {fileWritingSuccess=false;
						fnfe.printStackTrace();
					   }
					   if(!fileWritingSuccess)
					       {desktopShortcutFile.delete();
						    logger.info("the desktop shortcut file "+desktopShortcutFile.getAbsolutePath()+" has not been successfully filled");
						    success=false;
					       }
					   else
					       {logger.info("the desktop shortcut file "+desktopShortcutFile.getAbsolutePath()+" has been successfully filled");
						    success=true;
					       }
				      }
			     }
		    }
		return(success);
	}

	public static final void main(String[] args){
		createDesktopShortcut("TUER","http://tuer.sourceforge.net/very_experimental/tuer.jnlp");
	}
	
	
	private static class StreamReader extends Thread {
		
	    private InputStream is;
	    
	    private StringWriter sw;

	    private StreamReader(InputStream is){
	        this.is=is;
	        sw=new StringWriter();
	    }

	    public void run(){
	        try 
	           {int c;
	            while((c=is.read())!=-1)
	                sw.write(c);
	            sw.close();
	            is.close();
	           }
	        catch(IOException ioe)
	        {ioe.printStackTrace();}
	    }

	    private String getResult(){
	        return(sw.toString());
	    }
	}
}
