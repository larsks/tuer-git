package engine.input;

import com.ardor3d.input.Key;
import com.ardor3d.input.awt.AwtKeyboardWrapper;
import java.awt.Component;
import java.awt.event.KeyEvent;
import java.util.Locale;

public class ExtendedAwtKeyboardWrapper extends AwtKeyboardWrapper{
	
	public ExtendedAwtKeyboardWrapper(final Component component) {
		super(component);
	}

	@Override
	public final synchronized Key fromKeyEventToKey(final KeyEvent e){
		final Locale defaultLocale = Locale.getDefault();
        final String country = defaultLocale.getCountry();
        Key key = null;
        if(e.getKeyLocation() != KeyEvent.KEY_LOCATION_NUMPAD && 
        		defaultLocale.getLanguage().equals("fr")
                && (country.equals("FR") || country.equals("BE")))
            switch(e.getKeyChar())
                {case '1':
                     if(e.getKeyCode() == KeyEvent.VK_AMPERSAND)
                    	 key=Key.ONE;                  
                     break;
                 case '&':
                	 key=Key.ONE;
                	 break;
                 case '2':
                     if(e.getKeyCode() == KeyEvent.VK_UNDEFINED)
                         key=Key.TWO;                   
                     break;
                 case 'é':
                	 key=Key.TWO;
                	 break;
                 case '3':
                     if(e.getKeyCode() == KeyEvent.VK_QUOTEDBL)
                    	 key=Key.THREE;                   
                     break;
                 case '\"':
                	 key=Key.THREE;
                	 break;
                 case '4':
                     if(e.getKeyCode() == KeyEvent.VK_QUOTE)
                    	 key=Key.FOUR;                   
                     break;                   
                 case '\'':
                	 key=Key.FOUR;
                	 break;
                 case '5':
                     if(e.getKeyCode() == KeyEvent.VK_LEFT_PARENTHESIS)
                    	 key=Key.FIVE;
                     break;
                 case '(':
                	 key=Key.FIVE;
                	 break;
                 case '6':
                     if(e.getKeyCode() == KeyEvent.VK_MINUS)
                    	 key=Key.SIX;                   
                     break;                   
                 case '-':
                	 key=Key.SIX;
                	 break;
                 case '7':
                     if(e.getKeyCode() == KeyEvent.VK_UNDEFINED)
                    	 key=Key.SEVEN;                  
                     break;
                 case 'è':
                	 key=Key.SEVEN;
                	 break;
                 case '8':
                     if(e.getKeyCode() == KeyEvent.VK_UNDERSCORE)
                    	 key=Key.EIGHT;
                     break;                    
                 case '_':
                	 key=Key.EIGHT;
                	 break;
                 case '9':
                     if(e.getKeyCode() == KeyEvent.VK_UNDEFINED)
                    	 key=Key.NINE;
                     break;                    
                 case 'ç':
                	 key=Key.NINE;
                	 break;
                 case '0':
                     if(e.getKeyCode() == KeyEvent.VK_UNDEFINED)
                    	 key=Key.ZERO;
                     break;                    
                 case 'à':
                	 key=Key.ZERO;
                	 break;
                }          
        if(key==null)
            key=super.fromKeyEventToKey(e);
		return(key);
	}
}
