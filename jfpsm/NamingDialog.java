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
package jfpsm;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;

/**
 * Modal dialog used to name an entity. A single instance can be reused several times.
 * The accepted names are cross-platform filenames only and without file extension.
 * @author Julien Gouesse
 *
 */
public class NamingDialog extends JDialog implements ActionListener,PropertyChangeListener{
    
    
    private static final long serialVersionUID = 1L;
    
    private String typedText;
    
    private final ArrayList<String> names;
    
    private final JTextField textField;
    
    private final JOptionPane optionPane;
    
    private final JButton confirmButton;
        

    /** Creates the reusable naming dialog. */
    public NamingDialog(JFrame aFrame,final ArrayList<String> names,String namedEntity){
        super(aFrame, true);
        this.names=names;
        typedText=null;
        setTitle("New "+namedEntity);
        textField=new JTextField(10);           
        //Create the JOptionPane with an array of the text and components to be displayed.
        optionPane=new JOptionPane(new Object[]{"Enter a "+namedEntity+" name",textField},JOptionPane.QUESTION_MESSAGE,JOptionPane.OK_CANCEL_OPTION);
        //Make this dialog display it.
        setContentPane(optionPane);
        //Handle window closing correctly.
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter(){
            public void windowClosing(WindowEvent we) {
                /*
                 * Instead of directly closing the window,
                 * we're going to change the JOptionPane's
                 * value property.
                 */
                optionPane.setValue(Integer.valueOf(JOptionPane.CLOSED_OPTION));
            }
        });
        //Ensure the text field always gets the first focus.
        addComponentListener(new ComponentAdapter() {
            public void componentShown(ComponentEvent ce) {
                textField.requestFocusInWindow();
            }
        });
        //Register an event handler that puts the text into the option pane.
        textField.addActionListener(this);
        //Register an event handler that reacts to option pane state changes.
        optionPane.addPropertyChangeListener(this);
        confirmButton=(JButton)((JPanel)optionPane.getComponent(1)).getComponent(0);
        textField.getDocument().addDocumentListener(new DocumentListener(){

            @Override
            public final void changedUpdate(DocumentEvent e){
                updateConfirmationAbility();
            }

            @Override
            public final void insertUpdate(DocumentEvent e){
                updateConfirmationAbility();
            }

            @Override
            public final void removeUpdate(DocumentEvent e){
                updateConfirmationAbility();
            }           
        });
        pack();
    }
    
    private static final String REGEXP_INVALID_PUBLISHED_PATH_CHARS = "(\\[|#|\\*|\\?|\"|<|>|\\||!|%|/|\\])+";

    private static final String REGEXP_INVALID_PUBLISHED_PATH_CHARS_LINUX = "(\\[|#|\\*|\\?|\"|<|>|\\||!|%|\\])+";

    private static final String REGEXP_INVALID_FILENAME_CHARS = "(\\[|#|/|\\\\|\\:|\\*|\\?|\"|<|>|\\||\\]|\\s)+";

    
    private final boolean checkNameAvailabilityAndValidity(){
        boolean confirmationEnabled=false;
        Document doc=textField.getDocument();
        try{String text=doc.getText(0,doc.getLength());
            //check if the name is not empty, not used and valid
            confirmationEnabled=text!=null&&!text.equals("")&&!names.contains(text)&&
                                text.replaceAll(REGEXP_INVALID_PUBLISHED_PATH_CHARS,"").equals(text)&& 
                                text.replaceAll(REGEXP_INVALID_PUBLISHED_PATH_CHARS_LINUX,"").equals(text)&&
                                text.replaceAll(REGEXP_INVALID_FILENAME_CHARS,"").equals(text);
           } 
        catch(BadLocationException ble)
        {confirmationEnabled=false;}
        return(confirmationEnabled);
    }
    
    protected boolean checkDataValidity(){
        return(checkNameAvailabilityAndValidity());
    }
    
    protected final boolean updateConfirmationAbility(){
        boolean confirmationEnabled=checkDataValidity();
        confirmButton.setEnabled(confirmationEnabled);
        return(confirmationEnabled);
    }
    
    @Override
    public final void setVisible(boolean visible){
        updateConfirmationAbility();
        super.setVisible(visible);
    }
    
   /**
     * Returns null if the typed string was invalid;
     * otherwise, returns the string as the user entered it.
     */
    public final String getValidatedText() {
        return typedText;
    }
    
    /** This method handles events for the text field. */
    public final void actionPerformed(ActionEvent e){
        if(updateConfirmationAbility())
            optionPane.setValue(Integer.valueOf(JOptionPane.OK_OPTION));
    }

    /** This method reacts to state changes in the option pane. */
    public final void propertyChange(PropertyChangeEvent e){
        String prop = e.getPropertyName();
        if(isVisible()&&(e.getSource()==optionPane) && (JOptionPane.VALUE_PROPERTY.equals(prop) || JOptionPane.INPUT_VALUE_PROPERTY.equals(prop)))
            {Object value = optionPane.getValue();
             if(value==JOptionPane.UNINITIALIZED_VALUE)
                 {//ignore reset
                  return;
                 }
             //Reset the JOptionPane's value.
             //If you don't do this, then if the user
             //presses the same button next time, no
             //property change event will be fired.
             optionPane.setValue(JOptionPane.UNINITIALIZED_VALUE);
             updateValidationData(value.equals(Integer.valueOf(JOptionPane.OK_OPTION)));            
             clearAndHide();
           }
    }
    
    protected void updateValidationData(boolean validate){
        if(validate)
            typedText=textField.getText();
        else
            typedText=null;
    }

    /** This method clears the dialog and hides it. */
    public void clearAndHide(){
        textField.setText(null);
        setVisible(false);
    }
}