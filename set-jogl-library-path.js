importClass(java.lang.System)			
var joglLibraryPath=project.getProperty("jogl.library.path")
if(joglLibraryPath==null)
    {var osArch=System.getProperty("os.arch")
     var osName=System.getProperty("os.name")
     var osSubName=osName.substring(0,3).toLowerCase()
     if(osSubName.equals("lin"))
         {if(osArch.equals("amd64"))
              joglLibraryPath="./lib/jogl/native/linux-amd64/"
          else
              if(osArch.equals("i386")||osArch.equals("i586")||osArch.equals("x86"))
                  joglLibraryPath="./lib/jogl/native/linux-i586/"
              else
                  joglLibraryPath="./lib/jogl/native/linux-i586/"
         }
     else
         {if(osSubName.equals("mac"))
              joglLibraryPath="./lib/jogl/native/macosx-universal/"
          else
              if(osSubName.equals("win"))
                  {if(osArch.equals("amd64"))
                       joglLibraryPath="./lib/jogl/native/windows-amd64/"
				       else
				           if(osArch.equals("i386")||osArch.equals("i586")||osArch.equals("x86"))
                           joglLibraryPath="./lib/jogl/native/windows-i586/"
				           else
                           joglLibraryPath="./lib/jogl/native/windows-i586/"
                  }
			}			     
     project.setProperty("jogl.library.path",joglLibraryPath)
    }
System.out.println("jogl.library.path: "+joglLibraryPath)