/**
 * Copyright (c) 2006-2021 Julien Gouesse
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
package engine.integration;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Logger;

public final class DesktopIntegration {

    private static final Logger LOGGER = Logger.getLogger(DesktopIntegration.class.getName());

    private static final DesktopIntegration instance = new DesktopIntegration();

    private static final String UNDEFINED_COMMAND_TAG = "UNDEFINED_COMMAND_TAG";

    private final String desktopPath;

    public static final boolean useJNLP() {
        final String classloaderClassname = Thread.currentThread().getContextClassLoader().getClass()
                .getCanonicalName();
        return (classloaderClassname.equals("sun.plugin2.applet.JNLP2ClassLoader")
                || classloaderClassname.equals("com.sun.jnlp.JNLPClassLoader"));
    }

    public enum OS {
        Linux("desktop",
                new String[] { "[Desktop Entry]", "Comment=", "Exec=" + UNDEFINED_COMMAND_TAG + " ", "GenericName=",
                        "Icon=", "MimeType=", "Name=", "Path=", "StartupNotify=false", "Terminal=false",
                        "TerminalOptions=", "Type=Application", "X-DBUS-ServiceName=", "X-DBUS-StartupType=",
                        "X-KDE-SubstituteUID=false", "X-KDE-Username=" },
                6, 2),
                // do not use alias file format as it is very complicated to
                // create
                Mac("sh", new String[] { UNDEFINED_COMMAND_TAG + " " }, -1, 0), Unix("desktop",
                        new String[] { "[Desktop Entry]", "Comment=", "Exec=" + UNDEFINED_COMMAND_TAG + " ",
                                "GenericName=", "Icon=", "MimeType=", "Name=", "Path=", "StartupNotify=false",
                                "Terminal=false", "TerminalOptions=", "Type=Application", "X-DBUS-ServiceName=",
                                "X-DBUS-StartupType=", "X-KDE-SubstituteUID=false", "X-KDE-Username=" },
                        6, 2),
                        // do not use LNK file format as it differs depending on
                        // the version
                        Windows("bat",
                                new String[] { "\"" + System.getProperty("java.home")
                                        + System.getProperty("file.separator") + "bin"
                                        + System.getProperty("file.separator") + UNDEFINED_COMMAND_TAG + "\" " },
                                -1, 0);

        private final String desktopShortcutFileExtension;

        private final String[] desktopShortcutFileContent;

        private final int desktopShortcutFileNameLineIndex;

        private final int desktopShortcutFileExecutableCommandLineIndex;

        private OS(final String desktopShortcutFileExtension, final String[] desktopShortcutFileContent,
                final int desktopShortcutFileNameLineIndex, final int desktopShortcutFileExecutableCommandLineIndex) {
            this.desktopShortcutFileExtension = desktopShortcutFileExtension;
            this.desktopShortcutFileContent = desktopShortcutFileContent;
            this.desktopShortcutFileNameLineIndex = desktopShortcutFileNameLineIndex;
            this.desktopShortcutFileExecutableCommandLineIndex = desktopShortcutFileExecutableCommandLineIndex;
        }

        private final String getDesktopShortcutFileExtension() {
            return (desktopShortcutFileExtension);
        }

        private final String[] getDesktopShortcutFileContent() {
            return (desktopShortcutFileContent);
        }

        private final int getDesktopShortcutFileNameLineIndex() {
            return (desktopShortcutFileNameLineIndex);
        }

        private final int getDesktopShortcutFileExecutableCommandLineIndex() {
            return (desktopShortcutFileExecutableCommandLineIndex);
        }
    };

    private final OS operatingSystem;

    private DesktopIntegration() {
        /**
         * finds the desktop path of the current operating system
         */
        final String osName = System.getProperty("os.name").toLowerCase();
        final String userHome = System.getProperty("user.home");
        LOGGER.info("operating system: " + osName);
        if (osName.startsWith("linux"))
            operatingSystem = OS.Linux;
        else if (osName.startsWith("mac"))
            operatingSystem = OS.Mac;
        else if (osName.startsWith("windows") || osName.startsWith("Windows"))
            operatingSystem = OS.Windows;
        else
            operatingSystem = OS.Unix;
        if (operatingSystem.equals(OS.Linux) || operatingSystem.equals(OS.Unix)) {
            if (operatingSystem.equals(OS.Linux))
                LOGGER.info("operating system family: Linux");
            else if (osName.startsWith("solaris") || osName.startsWith("sunos") || osName.startsWith("hp-ux")
                    || osName.startsWith("aix") || osName.startsWith("freebsd") || osName.startsWith("openvms")
                    || osName.startsWith("os") || osName.startsWith("irix") || osName.startsWith("netware")
                    || osName.contains("unix"))
                LOGGER.warning("operating system family: Unix");
            else
                LOGGER.warning("unknown operating system family, maybe Unix");
            // any window manager following the FreeDesktop specification
            // supports the command below
            String XDG_DESKTOP_DIR = null;
            try {
                Process process = Runtime.getRuntime().exec("xdg-user-dir DESKTOP");
                StreamReader reader = new StreamReader(process.getInputStream());
                reader.start();
                process.waitFor();
                reader.join();
                XDG_DESKTOP_DIR = reader.getResult();
                if (XDG_DESKTOP_DIR != null)
                    XDG_DESKTOP_DIR = XDG_DESKTOP_DIR.replace(System.getProperty("line.separator"), "");
            } catch (Exception e) {
                e.printStackTrace();
            }
            final boolean desktopDirSet = XDG_DESKTOP_DIR != null && !XDG_DESKTOP_DIR.isEmpty();
            final boolean desktopDirDenotesExistingDir;
            if (desktopDirSet) {
                final File desktopDirFile = new File(XDG_DESKTOP_DIR);
                desktopDirDenotesExistingDir = desktopDirFile.exists() && desktopDirFile.isDirectory();
            } else
                desktopDirDenotesExistingDir = false;
            if (!desktopDirSet || !desktopDirDenotesExistingDir) {
                if (!desktopDirSet)
                    LOGGER.warning("XDG_DESKTOP_DIR is not set");
                if (!desktopDirDenotesExistingDir)
                    LOGGER.warning("XDG_DESKTOP_DIR doesn't denote an existing directory: " + XDG_DESKTOP_DIR);
                final String defaultDesktopFolderPath = userHome + System.getProperty("file.separator") + "Desktop";
                if (new File(defaultDesktopFolderPath).exists()) {
                    LOGGER.info("use the default desktop folder: " + defaultDesktopFolderPath);
                    desktopPath = defaultDesktopFolderPath;
                } else {
                    LOGGER.warning("the default desktop folder " + defaultDesktopFolderPath + " does not exist");
                    desktopPath = null;
                }
            } else {
                desktopPath = XDG_DESKTOP_DIR;
                LOGGER.info("XDG_DESKTOP_DIR denotes an existing directory: " + XDG_DESKTOP_DIR);
            }
        } else if (operatingSystem.equals(OS.Mac)) {
            LOGGER.info("operating system family: Mac");
            final String oldMacDesktopFolderPath = userHome + System.getProperty("file.separator") + "Desktop Folder";
            final String modernMacDesktopFolderPath = userHome + System.getProperty("file.separator") + "Desktop";
            if (new File(oldMacDesktopFolderPath).exists()) {
                LOGGER.info("use old desktop folder: " + oldMacDesktopFolderPath);
                desktopPath = oldMacDesktopFolderPath;
            } else if (new File(modernMacDesktopFolderPath).exists()) {
                LOGGER.info("use modern desktop folder: " + modernMacDesktopFolderPath);
                desktopPath = modernMacDesktopFolderPath;
            } else {
                LOGGER.warning("The desktop folder does not match with any known pattern. There is no way to find it");
                desktopPath = null;
            }
        } else if (operatingSystem.equals(OS.Windows)) {
            LOGGER.info("operating system family: Windows");
            String specialFolderValue = null;
            File tmpWshFile = null;
            try {
                LOGGER.info("tries to create a temporary file to contain the WSH script...");
                tmpWshFile = File.createTempFile("getDesktopFolder", ".js");
                LOGGER.info("temporary file " + tmpWshFile.getAbsolutePath() + " successfully created");
                try (PrintWriter pw = new PrintWriter(tmpWshFile)) {
                    pw.println("WScript.Echo(WScript.SpecialFolders(\"Desktop\"));");
                    LOGGER.info("temporary file " + tmpWshFile.getAbsolutePath() + " successfully filled");
                }
            } catch (IOException ioe) {
                if (tmpWshFile != null) {
                    tmpWshFile = null;
                    LOGGER.warning("something was wrong while writing the data in the temporary file");
                } else
                    LOGGER.warning("temporary file not created");
                ioe.printStackTrace();
            }
            if (tmpWshFile != null) {// use Windows Scripting Host (supported
                                     // since Windows 98)
                final String wshellCmd = "wscript //NoLogo //B " + tmpWshFile.getAbsolutePath();
                try {
                    Process process = Runtime.getRuntime().exec(wshellCmd);
                    StreamReader reader = new StreamReader(process.getInputStream());
                    reader.start();
                    process.waitFor();
                    reader.join();
                    specialFolderValue = reader.getResult();
                    if (specialFolderValue != null)
                        specialFolderValue = specialFolderValue.replace(System.getProperty("line.separator"), "");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            if (specialFolderValue != null && new File(specialFolderValue).exists()) {
                LOGGER.info("special desktop folder path: " + specialFolderValue);
                desktopPath = specialFolderValue;
            } else {// use Windows registry
                final String REGQUERY_UTIL = "reg query ";
                final String REGSTR_TOKEN = "REG_SZ";
                final String DESKTOP_FOLDER_CMD = REGQUERY_UTIL
                        + "\"HKCU\\Software\\Microsoft\\Windows\\CurrentVersion\\Explorer\\Shell Folders\" /v DESKTOP";
                String registryValue = null;
                try {
                    Process process = Runtime.getRuntime().exec(DESKTOP_FOLDER_CMD);
                    StreamReader reader = new StreamReader(process.getInputStream());
                    reader.start();
                    process.waitFor();
                    reader.join();
                    String result = reader.getResult();
                    result = result.replace(System.getProperty("line.separator"), "");
                    int p = result.indexOf(REGSTR_TOKEN);
                    if (p != -1) {// get the raw value
                        registryValue = result.substring(p + REGSTR_TOKEN.length()).trim();
                        // substitute environment variables by their values
                        Map<String, String> environmentVarsMap = System.getenv();
                        String environmentVariable;
                        int indexOfEnvironmentVariableOccurrence;
                        for (Entry<String, String> entry : environmentVarsMap.entrySet()) {
                            environmentVariable = entry.getKey();
                            /**
                             * It uses indexOf() and substring() because
                             * replaceAll() cannot be used as this method uses
                             * regular expressions and interprets '%' as a
                             * punctuation character which is often in the names
                             * of environment variables.
                             */
                            indexOfEnvironmentVariableOccurrence = registryValue.indexOf(environmentVariable);
                            if (indexOfEnvironmentVariableOccurrence != -1)
                                registryValue = entry.getValue() + registryValue.substring(
                                        indexOfEnvironmentVariableOccurrence + environmentVariable.length(),
                                        registryValue.length());
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (registryValue != null && new File(registryValue).exists()) {
                    LOGGER.info("registry value used as a desktop path: " + registryValue);
                    desktopPath = registryValue;
                } else {// this is the default desktop folder on Windows Vista,
                        // 7 and 8, whatever the language
                    final String modernWindowsDesktopFolderPath = userHome + System.getProperty("file.separator")
                            + "Desktop";
                    if (new File(modernWindowsDesktopFolderPath).exists()) {
                        LOGGER.info("usual default desktop path: " + modernWindowsDesktopFolderPath);
                        desktopPath = modernWindowsDesktopFolderPath;
                    } else {
                        LOGGER.warning("There is no way to find the desktop folder");
                        desktopPath = null;
                    }
                }
            }
        } else
            desktopPath = null;
        if (desktopPath != null) {
            if (operatingSystem.equals(OS.Unix))
                LOGGER.warning("operating system not supported. Desktop path: " + desktopPath);
            else
                LOGGER.info("operating system supported. Desktop path: " + desktopPath);
        } else
            LOGGER.warning("desktop path not found");
    }

    public static final boolean isDesktopShortcutCreationSupported() {
        return (instance.desktopPath != null && instance.operatingSystem.getDesktopShortcutFileContent() != null);
    }

    public static final String getDesktopDirectoryPath() {
        return (instance.desktopPath);
    }

    public static final boolean createLaunchDesktopShortcut(final String desktopShortcutFilenameWithoutExtension,
            final String command) {
        return (createDesktopShortcut(desktopShortcutFilenameWithoutExtension, instance.desktopPath, command));
    }

    public static final boolean createUninstallDesktopShortcut(final String desktopShortcutFilenameWithoutExtension,
            final String command) {
        return (createDesktopShortcut(desktopShortcutFilenameWithoutExtension, instance.desktopPath, command));
    }

    public static final boolean createDesktopShortcut(final String desktopShortcutFilenameWithoutExtension,
            final String desktopShortcutFilepath, final String command) {
        final boolean success;
        if (!isDesktopShortcutCreationSupported()) {
            LOGGER.warning("desktop shortcuts are not supported by this operating system");
            success = false;
        } else if (desktopShortcutFilepath == null) {
            LOGGER.warning("the path of the desktop shortcut file should not be null");
            success = false;
        } else {
            LOGGER.info("desktop shortcuts are supported by this operating system");
            final File desktopShortcutFile = new File(desktopShortcutFilepath + System.getProperty("file.separator")
                    + desktopShortcutFilenameWithoutExtension + "."
                    + instance.operatingSystem.getDesktopShortcutFileExtension());
            // tries to delete the file if it already exists
            if (desktopShortcutFile.exists() && !desktopShortcutFile.delete())
                success = false;
            else {
                boolean fileCreationSuccess = false;
                // (re)creates the file
                try {
                    fileCreationSuccess = desktopShortcutFile.createNewFile();
                    // drives this file executable so that the operating system
                    // does not mark it as untrusted
                    desktopShortcutFile.setExecutable(true, true);
                } catch (IOException ioe) {
                    ioe.printStackTrace();
                }
                if (!fileCreationSuccess) {
                    LOGGER.warning("the desktop shortcut file " + desktopShortcutFile.getAbsolutePath()
                            + " has not been successfully created");
                    success = false;
                } else {
                    LOGGER.info("the desktop shortcut file " + desktopShortcutFile.getAbsolutePath()
                            + " has been successfully created");
                    final String[] src = instance.operatingSystem.getDesktopShortcutFileContent();
                    final String[] desktopShortcutFileContent = new String[src.length];
                    System.arraycopy(src, 0, desktopShortcutFileContent, 0, src.length);
                    // fills the future content of the file with the parameters
                    final int desktopShortcutFileExecutableCommandLineIndex = instance.operatingSystem
                            .getDesktopShortcutFileExecutableCommandLineIndex();
                    desktopShortcutFileContent[desktopShortcutFileExecutableCommandLineIndex] = desktopShortcutFileContent[desktopShortcutFileExecutableCommandLineIndex]
                            .replace("UNDEFINED_COMMAND_TAG", command);
                    final int desktopShortcutFileNameLineIndex = instance.operatingSystem
                            .getDesktopShortcutFileNameLineIndex();
                    if (desktopShortcutFileNameLineIndex != -1)
                        desktopShortcutFileContent[desktopShortcutFileNameLineIndex] = desktopShortcutFileContent[desktopShortcutFileNameLineIndex]
                                + desktopShortcutFilenameWithoutExtension;
                    boolean fileWritingSuccess = true;
                    // writes the content of the file
                    try (PrintWriter pw = new PrintWriter(desktopShortcutFile)) {
                        for (String line : desktopShortcutFileContent)
                            pw.println(line);
                    } catch (FileNotFoundException fnfe) {
                        fileWritingSuccess = false;
                        fnfe.printStackTrace();
                    }
                    if (!fileWritingSuccess) {
                        desktopShortcutFile.delete();
                        LOGGER.info("the desktop shortcut file " + desktopShortcutFile.getAbsolutePath()
                                + " has not been successfully filled");
                        success = false;
                    } else {
                        LOGGER.info("the desktop shortcut file " + desktopShortcutFile.getAbsolutePath()
                                + " has been successfully filled");
                        success = true;
                    }
                }
            }
        }
        return (success);
    }

    public static final OS getOperatingSystem() {
        return instance.operatingSystem;
    }

    private static class StreamReader extends Thread {

        private InputStream is;

        private StringWriter sw;

        private StreamReader(InputStream is) {
            this.is = is;
            sw = new StringWriter();
        }

        @Override
        public void run() {
            try {
                int c;
                while ((c = is.read()) != -1)
                    sw.write(c);
                sw.close();
                is.close();
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }
        }

        private String getResult() {
            return (sw.toString());
        }
    }
}
