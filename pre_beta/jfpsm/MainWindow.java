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

import java.awt.BorderLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Locale;
import javax.swing.AbstractAction;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JSplitPane;
import javax.swing.KeyStroke;
import javax.swing.UIManager;
import javax.swing.WindowConstants;

/**
 * main class of the application, it handles several components: the projects manager and the viewer.
 * It contains the entry point of the program. 
 * 
 * @author Julien Gouesse
 */
public final class MainWindow{

    
    private static final long serialVersionUID=1L;
    
    private JFrame applicativeFrame;
    
    private ProjectManager projectManager;
    
    private EntityViewer entityViewer;

    /**
     * create the most robust part of the application
     * @param applicativeFrame frame that contains the whole GUI
     */
    public MainWindow(JFrame applicativeFrame){
    	this.applicativeFrame=applicativeFrame;
    	applicativeFrame.setTitle("JFPSM: Java First Person Shooter Maker");
        //force the use of English in the whole application
        JComponent.setDefaultLocale(Locale.ENGLISH);
        UIManager.getDefaults().setDefaultLocale(Locale.ENGLISH);
        //this is the only solution to have the good localization for color choosers
        UIManager.getDefaults().put("ColorChooser.cancelText","Cancel");
        UIManager.getDefaults().put("ColorChooser.resetText","Reset");
        UIManager.getDefaults().put("ColorChooser.previewText","Preview");
        UIManager.getDefaults().put("ColorChooser.sampleText","Sample");
        UIManager.getDefaults().put("ColorChooser.swatchesNameText","Swatches");
        UIManager.getDefaults().put("ColorChooser.swatchesRecentText","Recent:");
        UIManager.getDefaults().put("ColorChooser.hsbNameText","HSB");
        UIManager.getDefaults().put("ColorChooser.hsbHueText","H");
        UIManager.getDefaults().put("ColorChooser.hsbSaturationText","S");
        UIManager.getDefaults().put("ColorChooser.hsbBrightnessText","B");
        UIManager.getDefaults().put("ColorChooser.hsbRedText","R");
        UIManager.getDefaults().put("ColorChooser.hsbGreenText","G");
        UIManager.getDefaults().put("ColorChooser.hsbBlueText","B");
        UIManager.getDefaults().put("ColorChooser.rgbNameText","RGB");
        UIManager.getDefaults().put("ColorChooser.rgbRedText","Red");
        UIManager.getDefaults().put("ColorChooser.rgbGreenText","Green");
        UIManager.getDefaults().put("ColorChooser.rgbBlueText","Blue");
        UIManager.getDefaults().put("ColorChooser.rgbRedMnemonic",Integer.valueOf(KeyEvent.VK_R));
        UIManager.getDefaults().put("ColorChooser.rgbGreenMnemonic",Integer.valueOf(KeyEvent.VK_G));
        UIManager.getDefaults().put("ColorChooser.rgbBlueMnemonic",Integer.valueOf(KeyEvent.VK_B));
        //the application occupies the whole screen
        applicativeFrame.setSize(Toolkit.getDefaultToolkit().getScreenSize());
        //do not dispose or exit, it is handled if the user confirms
        applicativeFrame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        applicativeFrame.addWindowListener(new WindowAdapter(){
            @Override
            public final void windowClosing(WindowEvent e){
                quit(true);
            }
        });
        //set the ESC shortcut to leave the application
        applicativeFrame.getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE,0,false),"quit");
        applicativeFrame.getRootPane().getActionMap().put("quit",new AbstractAction(){			
			private static final long serialVersionUID = 1L;

			@Override
			public final void actionPerformed(ActionEvent e){				
				quit(true);
			}
        });
        //set the save shortcut        
        applicativeFrame.getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_S,InputEvent.CTRL_DOWN_MASK,false),"save");
        applicativeFrame.getRootPane().getActionMap().put("save",new AbstractAction(){           
            private static final long serialVersionUID = 1L;

            @Override
            public final void actionPerformed(ActionEvent e){
            	if(projectManager!=null)
            	    projectManager.saveCurrentWorkspace();
            }
        });
        //set the refresh shortcut
        applicativeFrame.getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_F5,0,false),"refresh");
        applicativeFrame.getRootPane().getActionMap().put("refresh",new AbstractAction(){           
            private static final long serialVersionUID = 1L;

            @Override
            public final void actionPerformed(ActionEvent e){
                if(projectManager!=null)
                    projectManager.loadExistingProjects();
            }
        });
        //TODO: implement undo (CTRL+Z) & redo (CTRL+Y)
        //the frame has to be visible in order to allow to display
        //option panes if there is a problem
        applicativeFrame.setVisible(true);
    }
    
    /**
     * build the weakest part of the application (some exceptions may be thrown) 
     */
    public final void run(){
    	//build the projects manager
    	projectManager=new ProjectManager(this);
    	//build the viewer
        entityViewer=new EntityViewer(projectManager);
        JSplitPane mainSplitPane=new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,true,projectManager,entityViewer);        
        mainSplitPane.setOneTouchExpandable(true);
        applicativeFrame.add(mainSplitPane,BorderLayout.CENTER);
        //some components of this container has been modified
        //we have to force the layout to be rebuilt
        applicativeFrame.invalidate();
        applicativeFrame.validate();
    }
    
    final JFrame getApplicativeFrame(){
    	return(applicativeFrame);
    }
    
    final EntityViewer getEntityViewer(){
    	return(entityViewer);
    }
    
    /**
     * leave the application as cleanly as possible (attempt to save all projects)
     * @param confirm true if the user has to be prompted before closing
     */
    private final void quit(boolean confirm){
    	final boolean quitEnabled=projectManager==null||projectManager.isQuitEnabled();
    	if(quitEnabled)
    	    {final boolean doIt;
        	 if(confirm)
        		 doIt=JOptionPane.showConfirmDialog(applicativeFrame,"Exit JFPSM?","Confirm Exit",JOptionPane.OK_CANCEL_OPTION)==JOptionPane.OK_OPTION;
        	 else
        		 doIt=true;
        	 if(doIt)
        	     {//save all projects
        		  if(projectManager!=null)
            	      projectManager.saveCurrentWorkspace();
        	      //destroy the window
        		  applicativeFrame.dispose();
                  //this call is necessary to remove the application from memory when using Java Webstart
                  System.exit(0);       	 
        	     }
    	    }
    }

    final void displayErrorMessage(Throwable throwable,boolean fatal){
    	String errorMessage="";
    	final String lineSep=System.getProperty("line.separator");
    	Throwable currentThrowable=throwable;
    	while(currentThrowable!=null)
    		{errorMessage+=currentThrowable.getClass().getName();
    		 if(currentThrowable.getMessage()!=null)
    		     errorMessage+=": "+currentThrowable.getMessage()+lineSep;
    		 else
    		     errorMessage+=lineSep;
    		 for(StackTraceElement element:currentThrowable.getStackTrace())
    		     errorMessage+=element.toString()+lineSep;    		     
    		 currentThrowable=currentThrowable.getCause();
    		}
    	if(fatal)
    	    errorMessage+=System.getProperty("line.separator")+"The application will exit.";
    	String errorTitle=(fatal)?"Fatal error":"Application error";
    	JOptionPane.showMessageDialog(applicativeFrame,errorMessage,errorTitle,JOptionPane.ERROR_MESSAGE);
    }
    
    public static final void runInstance(String[] args){
        //launch a minimal GUI to be able to display a popup if something goes wrong later        
        MainWindow mainWindow=new MainWindow(new JFrame());
        //run the application
        try{mainWindow.run();}
        catch(Throwable throwable)
        {//display a popup to tell the user something goes wrong
         mainWindow.displayErrorMessage(throwable,true);
         //force the exit
         mainWindow.quit(false);
        }
    }
}
