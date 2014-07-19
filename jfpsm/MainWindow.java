/**
 * Copyright (c) 2006-2014 Julien Gouesse
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
 * main class of the application, it handles several components: the project manager and the viewer.
 * It contains the entry point of the program. 
 * 
 * @author Julien Gouesse
 */
public final class MainWindow{
	
	/**short name of the editor (which should be modified in order to avoid any trademark infringement)*/
	private static final String EDITOR_SHORT_NAME="JFPSM";
	
	/**full name of the editor*/
	private static final String EDITOR_FULL_NAME="Java First Person Shooter Maker";
	
	/**editor title used in the main application frame*/
	private static final String EDITOR_TITLE=EDITOR_SHORT_NAME+": "+EDITOR_FULL_NAME;
    
    private JFrame applicativeFrame;
    
    private ProjectManager projectManager;
    
    private ToolManager toolManager;
    
    private EntityViewer entityViewer;

    /**
     * Creates the most robust part of the application
     * @param applicativeFrame frame that contains the whole GUI
     */
    public MainWindow(JFrame applicativeFrame){
    	this.applicativeFrame=applicativeFrame;
    	applicativeFrame.setTitle(EDITOR_TITLE);
        //forces the use of English in the whole application
        JComponent.setDefaultLocale(Locale.ENGLISH);
        UIManager.getDefaults().setDefaultLocale(Locale.ENGLISH);
        //the application occupies the whole screen
        applicativeFrame.setSize(Toolkit.getDefaultToolkit().getScreenSize());
        //does not dispose or exit, it is handled if the user confirms
        applicativeFrame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        applicativeFrame.addWindowListener(new WindowAdapter(){
            @Override
            public final void windowClosing(WindowEvent e){
                quit(true);
            }
        });
        //sets the ESC shortcut to leave the application
        applicativeFrame.getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE,0,false),"quit");
        applicativeFrame.getRootPane().getActionMap().put("quit",new AbstractAction(){			
			private static final long serialVersionUID = 1L;

			@Override
			public final void actionPerformed(ActionEvent e){				
				quit(true);
			}
        });
        //sets the save shortcut        
        applicativeFrame.getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_S,InputEvent.CTRL_DOWN_MASK,false),"save");
        applicativeFrame.getRootPane().getActionMap().put("save",new AbstractAction(){           
            private static final long serialVersionUID = 1L;

            @Override
            public final void actionPerformed(ActionEvent e){
            	if(projectManager!=null)
            	    projectManager.saveCurrentWorkspace();
            }
        });
        //sets the refresh shortcut
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
     * builds the weakest part of the application (some exceptions may be thrown) 
     */
    public final void run(){
    	toolManager=new ToolManager(this);
    	//builds the projects manager
    	projectManager=new ProjectManager(this);
    	//builds the viewer
        entityViewer=new EntityViewer(projectManager,toolManager);
        JSplitPane leftSplitPane=new JSplitPane(JSplitPane.VERTICAL_SPLIT,true,toolManager,projectManager);
        JSplitPane mainSplitPane=new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,true,leftSplitPane,entityViewer);
        leftSplitPane.setOneTouchExpandable(true);
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
     * leaves the application as cleanly as possible (attempt to save all projects)
     * @param confirm true if the user has to be prompted before closing
     */
    private final void quit(boolean confirm){
    	final boolean quitEnabled=(projectManager==null||projectManager.isQuitEnabled())&&(toolManager==null||toolManager.isQuitEnabled());
    	if(quitEnabled)
    	    {final boolean doIt;
        	 if(confirm)
        		 doIt=JOptionPane.showConfirmDialog(applicativeFrame,"Exit JFPSM?","Confirm Exit",JOptionPane.OK_CANCEL_OPTION)==JOptionPane.OK_OPTION;
        	 else
        		 doIt=true;
        	 if(doIt)
        	     {//saves all projects
        		  if(projectManager!=null)
            	      projectManager.saveCurrentWorkspace();
        	      //destroys the window
        		  applicativeFrame.dispose();
                  //this call is necessary to leave the application when using Java Webstart
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
        //launches a minimal GUI to be able to display a popup if something goes wrong later        
        MainWindow mainWindow=new MainWindow(new JFrame());
        //runs the application
        try{mainWindow.run();}
        catch(Throwable throwable)
        {//displays a popup to tell the user something goes wrong
         mainWindow.displayErrorMessage(throwable,true);
         //forces the exit
         mainWindow.quit(false);
        }
    }
}
