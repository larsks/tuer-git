/**
 * Copyright (c) 2006-2016 Julien Gouesse
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

import java.util.Locale;
import java.util.ResourceBundle;

/**
 * Provider of localized messages. It looks for the language chosen by the end
 * user in the configuration file. It uses the default language of the operating
 * system as a fallback.
 * 
 * @author Julien Gouesse
 *
 */
public class LocalizedMessageProvider {

    private ResourceBundle resourceBundle;

    public LocalizedMessageProvider(final Locale locale) {
        super();
        if (locale == null)
            throw new IllegalArgumentException("The locale cannot be null");
        resourceBundle = ResourceBundle.getBundle("i18n.MessagesBundle", locale);
    }

    public String getString(final String key) {
        return (resourceBundle.getString(key));
    }
}
