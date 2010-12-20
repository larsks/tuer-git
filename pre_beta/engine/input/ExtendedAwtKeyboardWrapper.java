package engine.input;

import com.ardor3d.input.Key;
import com.ardor3d.input.awt.AwtKey;
import com.ardor3d.input.awt.AwtKeyboardWrapper;
import java.awt.Component;
import java.awt.event.KeyEvent;

public class ExtendedAwtKeyboardWrapper extends AwtKeyboardWrapper{
	
	enum KeyboardLayout{
		AZERTY,QWERTY,UNKNOWN;		
	};
	
	enum AzertyKeymap{
		ZERO(19,KeyEvent.VK_UNDEFINED,-1),
		ONE(10,KeyEvent.VK_AMPERSAND,-1),
		TWO(11,KeyEvent.VK_UNDEFINED,-1),
		THREE(12,KeyEvent.VK_QUOTEDBL,-1),
		FOUR(13,KeyEvent.VK_QUOTE,-1),
		FIVE(14,KeyEvent.VK_LEFT_PARENTHESIS,-1),
		//it differs between the French and the Belgian versions
		//SIX(15,KeyEvent.VK_MINUS,KeyEvent.),
		SEVEN(16,KeyEvent.VK_UNDEFINED,-1),
		//it differs between the French and the Belgian versions
		EIGHT(17,KeyEvent.VK_UNDERSCORE,KeyEvent.VK_EXCLAMATION_MARK),
		NINE(18,KeyEvent.VK_UNDEFINED,-1);
		
		private final int awtRawCode;
		/**key code of the French variant*/
		private final int awtKeyCode;
		/**key code of the Belgian variant*/
		private final int awtSecondaryKeyCode;
		
		private AzertyKeymap(final int awtRawCode,final int awtKeyCode,final int awtSecondaryKeyCode){
			this.awtRawCode=awtRawCode;
			this.awtKeyCode=awtKeyCode;
			this.awtSecondaryKeyCode=awtSecondaryKeyCode;
		}
	};
	
	enum QwertyKeymap{
		ZERO(19,KeyEvent.VK_0),
		ONE(10,KeyEvent.VK_1),
		TWO(11,KeyEvent.VK_2),
		THREE(12,KeyEvent.VK_3),
		FOUR(13,KeyEvent.VK_4),
		FIVE(14,KeyEvent.VK_5),
		SIX(15,KeyEvent.VK_6),
		SEVEN(16,KeyEvent.VK_7),
		EIGHT(17,KeyEvent.VK_8),
		NINE(18,KeyEvent.VK_9);
		
		private final int awtRawCode;
		
		private final int awtKeyCode;
		
		private QwertyKeymap(final int awtRawCode,final int awtKeyCode){
			this.awtRawCode=awtRawCode;
			this.awtKeyCode=awtKeyCode;
		}
	};
	
	private KeyboardLayout keyboardLayout;
	
	public ExtendedAwtKeyboardWrapper(final Component component){
		super(component);
		keyboardLayout=null;
	}
	
	private void detectKeyboardLayout(final KeyEvent e){
		if(this.keyboardLayout==null&&e.getKeyLocation()!=KeyEvent.KEY_LOCATION_NUMPAD)
		    {final int rawcode=getKeyRawCode(e);
		     final int keycode=e.getKeyCode();
			 for(QwertyKeymap keymapValue:QwertyKeymap.values())
	        	 if(keymapValue.awtRawCode==rawcode&&keymapValue.awtKeyCode==keycode)
	        	     {this.keyboardLayout=KeyboardLayout.QWERTY;
	        		  break;
	        	     }
			 if(this.keyboardLayout==null)
			     {for(AzertyKeymap keymapValue:AzertyKeymap.values())
	        	      if(keymapValue.awtRawCode==rawcode&&
	        	        (keymapValue.awtKeyCode==keycode||
	        	         keymapValue.awtSecondaryKeyCode==keycode))
	        	          {this.keyboardLayout=KeyboardLayout.AZERTY;
	        		       break;
	        	          }				  
			     }
		    }
	}

	private final int getKeyRawCode(final KeyEvent e){
		final String paramString=e.paramString();
		final String[] splittedParamString=paramString.split(",");
		int rawcode=-1;
		for(String keyValuePair:splittedParamString)
		    if(keyValuePair.startsWith("rawCode")||keyValuePair.startsWith("rawcode"))
		        {rawcode=Integer.parseInt(keyValuePair.substring(8));
			     break;
		        }
		return(rawcode);
	}
	
	@Override
	public final synchronized Key fromKeyEventToKey(final KeyEvent e){
		if(keyboardLayout==null)
			detectKeyboardLayout(e);
		//if(keyboardLayout!=null)
		//	  System.out.println(keyboardLayout);
		Key key=null;
		if(keyboardLayout==null||keyboardLayout!=KeyboardLayout.QWERTY)
		    {final int rawcode=getKeyRawCode(e);
		     //overrides top-left key codes for numbers
		     if(10<=rawcode&&rawcode<=19)
		         {for(QwertyKeymap keymapValue:QwertyKeymap.values())
		        	  if(keymapValue.awtRawCode==rawcode)
		                  {key=AwtKey.findByCode(keymapValue.awtKeyCode);
		        	       break;
		                  }			      
		         }			 
		    }
		if(key==null)
			key=super.fromKeyEventToKey(e);
		return(key);
	}
}
