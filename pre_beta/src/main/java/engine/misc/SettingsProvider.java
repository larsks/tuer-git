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
public class SettingsProvider {

    public static final int UNCHANGED_SIZE = 0;

    private static final Logger logger = Logger.getLogger(SettingsProvider.class.getCanonicalName());

    private static final String[] trueStrings = { Boolean.TRUE.toString(), "on", "1", "enabled", "activated" };

    private static final String[] falseStrings = { Boolean.FALSE.toString(), "off", "0", "disabled", "deactivated" };

    private static final int[] screenRotations = new int[] { 0, 90, 180, 270 };

    /**
     * program short name used to name the sub-directory, in the user's home
     * directory
     */
    private final String programShortName;

    /** configuration file */
    private final File configFile;

    /** locale */
    private Locale locale;

    /** vertical synchronization enabled */
    private boolean verticalSynchronizationEnabled;

    /** fullscreen or windowed */
    private boolean fullscreenEnabled;

    /** screen width */
    private int screenWidth;

    /** screen height */
    private int screenHeight;

    /** screen rotation */
    private int screenRotation;

    /** sound enabled */
    private boolean soundEnabled;

    /**
     * Constructor, the configuration file "config" is put into a sub-directory
     * in the user's home directory named .programShortName
     * 
     * @param programShortName
     *            program short name used to name the sub-directory, in the
     *            user's home directory
     */
    public SettingsProvider(final String programShortName) {
        this(programShortName, new File(System.getProperty("user.home") + "/." + programShortName, "config"));
    }

    /**
     * Default constructor
     * 
     * @param programShortName
     *            program short name
     * @param configFile
     *            configuration file
     */
    public SettingsProvider(final String programShortName, final File configFile) {
        super();
        if (programShortName == null || programShortName.isEmpty())
            throw new IllegalArgumentException("programShortName cannot be null or empty");
        if (configFile == null)
            throw new IllegalArgumentException("configFile cannot be null");
        this.programShortName = programShortName;
        this.configFile = configFile;
    }

    /**
     * Loads the settings from the configuration file if any, uses the default
     * values if necessary
     */
    public void load() {
        final Properties properties = new Properties();
        // looks at the file that contains the settings
        if (configFile.exists()) {
            try (final FileReader fileReader = new FileReader(configFile);
                    final BufferedReader bufferedReader = new BufferedReader(fileReader)) {
                properties.load(bufferedReader);
                logger.log(Level.INFO, "Configuration file " + configFile.getAbsolutePath() + " found");
            } catch (IOException ioe) {// something wrong has just happened
                                       // while reading the configuration file
                logger.log(Level.WARNING, "Something wrong has occured while reading the configuration file "
                        + configFile.getAbsolutePath(), ioe);
            }
        } else {// the configuration file is absent
            logger.log(Level.WARNING, "Cannot find the configuration file " + configFile.getAbsolutePath());
        }
        // language property
        locale = readLocalePropertyValue(properties, "LANGUAGE", Locale.getDefault());
        // vertical synchronization
        verticalSynchronizationEnabled = readBooleanPropertyValue(properties, "VSYNC", Boolean.FALSE);
        // fullscreen
        fullscreenEnabled = readBooleanPropertyValue(properties, "FULLSCREEN", Boolean.TRUE);
        // screen width
        screenWidth = readIntPropertyValue(properties, "SCREEN_WIDTH", Integer.valueOf(UNCHANGED_SIZE), null);
        // screen height
        screenHeight = readIntPropertyValue(properties, "SCREEN_HEIGHT", Integer.valueOf(UNCHANGED_SIZE), null);
        // screen rotation
        screenRotation = readIntPropertyValue(properties, "SCREEN_ROTATION", Integer.valueOf(0), screenRotations);
        // sound enabled
        soundEnabled = readBooleanPropertyValue(properties, "SOUND", Boolean.TRUE);
    }

    private Locale readLocalePropertyValue(final Properties properties, final String propertyKey,
            final Locale defaultLocale) {
        final Locale localePropertyValue;
        // tries to read the ISO 639 alpha-2 or alpha-3 language code
        final String languageCode = properties.getProperty(propertyKey);
        if (languageCode != null && !languageCode.isEmpty()) {
            localePropertyValue = new Locale(languageCode);
            logger.log(Level.INFO, "Language code \"" + languageCode + "\" found, uses the language "
                    + localePropertyValue.getDisplayLanguage() + " for the property " + propertyKey);
        } else {
            localePropertyValue = defaultLocale;
            // language not set
            if (localePropertyValue == null)
                logger.log(Level.WARNING,
                        "Language code not found, no default language, uses null for the property " + propertyKey);
            else
                logger.log(Level.INFO, "Language code not found, uses the default language "
                        + localePropertyValue.getDisplayLanguage() + " for the property " + propertyKey);
        }
        return (localePropertyValue);
    }

    private boolean readBooleanPropertyValue(final Properties properties, final String propertyKey,
            final Boolean defaultValue) {
        final String booleanStringValue = properties.getProperty(propertyKey);
        Boolean booleanPropertyValue = null;
        if (booleanStringValue != null && !booleanStringValue.isEmpty()) {
            for (final String trueString : trueStrings)
                if (booleanStringValue.equalsIgnoreCase(trueString)) {
                    booleanPropertyValue = Boolean.TRUE;
                    break;
                }
            if (booleanPropertyValue == null)
                for (final String falseString : falseStrings)
                    if (booleanStringValue.equalsIgnoreCase(falseString)) {
                        booleanPropertyValue = Boolean.FALSE;
                        break;
                    }
        }
        if (booleanPropertyValue == null) {
            if (defaultValue == null) {
                booleanPropertyValue = Boolean.FALSE;
                logger.log(Level.WARNING,
                        "Boolean property " + propertyKey + " not found, no default value, set to false");
            } else {
                booleanPropertyValue = defaultValue;
                logger.log(Level.INFO, "Boolean property " + propertyKey + " not found, set to the default value "
                        + defaultValue.booleanValue());
            }
        } else
            logger.log(Level.INFO,
                    "Boolean property " + propertyKey + " found: " + booleanPropertyValue.booleanValue());
        return (booleanPropertyValue.booleanValue());
    }

    private int readIntPropertyValue(final Properties properties, final String propertyKey, final Integer defaultValue,
            final int[] acceptedValues) {
        final String intStringValue = properties.getProperty(propertyKey);
        Integer intPropertyValue = null;
        if (intStringValue != null && !intStringValue.isEmpty()) {
            try {
                final int resultCandidate = Integer.parseInt(intStringValue);
                if (acceptedValues != null && acceptedValues.length > 0) {
                    for (final int acceptedIntValue : acceptedValues)
                        if (resultCandidate == acceptedIntValue) {
                            intPropertyValue = resultCandidate;
                            break;
                        }
                    if (intPropertyValue == null)
                        logger.log(Level.WARNING,
                                "Value " + resultCandidate + " rejected for the property " + propertyKey);
                } else
                    intPropertyValue = resultCandidate;
            } catch (NumberFormatException nfe) {
                logger.log(Level.WARNING, "A problem occured while reading the property " + propertyKey, nfe);
            }
        }
        if (intPropertyValue == null) {
            if (defaultValue == null) {
                intPropertyValue = Integer.MIN_VALUE;
                logger.log(Level.WARNING, "Integer property " + propertyKey + " not found, no default value, set to "
                        + Integer.MIN_VALUE);
            } else {
                intPropertyValue = defaultValue;
                logger.log(Level.INFO, "Integer property " + propertyKey + " not found, set to the default value "
                        + defaultValue.intValue());
            }
        } else
            logger.log(Level.INFO, "Integer property " + propertyKey + " found: " + intPropertyValue.intValue());
        return (intPropertyValue.intValue());
    }

    /**
     * Tells whether the vertical synchronization is enabled
     * 
     * @return <code>true</code> if the vertical synchronization is enabled,
     *         otherwise <code>false</code>
     */
    public boolean isVerticalSynchronizationEnabled() {
        return (verticalSynchronizationEnabled);
    }

    public void setVerticalSynchronizationEnabled(final boolean verticalSynchronizationEnabled) {
        this.verticalSynchronizationEnabled = verticalSynchronizationEnabled;
    }

    public boolean isFullscreenEnabled() {
        return (fullscreenEnabled);
    }

    public void setFullscreenEnabled(final boolean fullscreenEnabled) {
        this.fullscreenEnabled = fullscreenEnabled;
    }

    /**
     * Returns the locale
     * 
     * @return locale
     */
    public Locale getLocale() {
        return (locale);
    }

    public void setLocale(final Locale locale) {
        if (locale == null)
            throw new IllegalArgumentException("The locale cannot be set to null");
        this.locale = locale;
    }

    public int getScreenRotation() {
        return (screenRotation);
    }

    public void setScreenRotation(final int screenRotation) {
        this.screenRotation = screenRotation;
    }

    public boolean isSoundEnabled() {
        return (soundEnabled);
    }

    public void setSoundEnabled(final boolean soundEnabled) {
        this.soundEnabled = soundEnabled;
    }

    public int getScreenWidth() {
        return (screenWidth);
    }

    public void setScreenWidth(final int screenWidth) {
        this.screenWidth = screenWidth;
    }

    public int getScreenHeight() {
        return (screenHeight);
    }

    public void setScreenHeight(final int screenHeight) {
        this.screenHeight = screenHeight;
    }

    /**
     * Saves the settings in the configuration file
     */
    public void save() {
        final Properties properties = new Properties();
        properties.put("LANGUAGE", locale.getLanguage());
        properties.put("VSYNC", Boolean.toString(verticalSynchronizationEnabled));
        properties.put("FULLSCREEN", Boolean.toString(fullscreenEnabled));
        properties.put("SCREEN_WIDTH", Integer.toString(screenWidth));
        properties.put("SCREEN_HEIGHT", Integer.toString(screenHeight));
        properties.put("SCREEN_ROTATION", Integer.toString(screenRotation));
        properties.put("SOUND", Boolean.toString(soundEnabled));
        try {
            final File parentDir = configFile.getParentFile();
            if (!parentDir.exists())
                parentDir.mkdirs();
            if (!configFile.exists())
                configFile.createNewFile();
            try (final FileWriter fileWriter = new FileWriter(configFile);
                    final BufferedWriter bufferedWriter = new BufferedWriter(fileWriter)) {
                final String comments = programShortName + " configuration file";
                properties.store(bufferedWriter, comments);
            }
            logger.log(Level.INFO, "Settings saved into the configuration file " + configFile.getAbsolutePath() + "\n");
        } catch (Throwable t) {
            logger.log(Level.WARNING,
                    "Something wrong has happened while trying to save the settings into the configuration file "
                            + configFile.getAbsolutePath(),
                    t);
        }
    }
}
