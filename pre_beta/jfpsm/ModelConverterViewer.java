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
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.Arrays;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.border.TitledBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileFilter;

/**
 * Graphical user interface of the model converter
 * 
 * @author Julien Gouesse
 *
 */
public class ModelConverterViewer extends JFPSMToolUserObjectViewer{
	
	private static final long serialVersionUID=1L;
	
	private final JFileChooser fileChooser;
	
	private final JLabel convertibleModelContentLabel;
	
	private final JLabel convertibleModelFormatContentLabel;
	
	private final JLabel convertedModelDirectoryLabel;
	
	private final JTextField convertedModelFilenameTextField;
	
	private final JLabel convertedModelExtensionLabel;
	
	private final JComboBox<ModelFileFormat> convertedModelFormatCombobox;
	
	private final JButton conversionButton;

	public ModelConverterViewer(final ModelConverter modelConverter,final ToolManager toolManager){
		super(modelConverter,toolManager);
		fileChooser=new JFileChooser();
		setLayout(new BorderLayout());
		final JPanel modelConversionSetupPanel=new JPanel(new GridBagLayout());
		final TitledBorder setupBorder=BorderFactory.createTitledBorder("Setup");
		modelConversionSetupPanel.setBorder(setupBorder);
		add(modelConversionSetupPanel,BorderLayout.SOUTH);
		final JLabel convertibleModelLabel=new JLabel("Model to convert:");
		modelConversionSetupPanel.add(convertibleModelLabel,new GridBagConstraints(0,0,1,1,1,1,GridBagConstraints.FIRST_LINE_START,GridBagConstraints.HORIZONTAL,new Insets(0,0,0,0),0,0));
		final JLabel convertibleModelFormatLabel=new JLabel("Input file format:");
		modelConversionSetupPanel.add(convertibleModelFormatLabel,new GridBagConstraints(0,1,1,1,1,1,GridBagConstraints.LINE_START,GridBagConstraints.HORIZONTAL,new Insets(0,0,0,0),0,0));
		final JLabel convertedModelLabel=new JLabel("Converted model:");
		modelConversionSetupPanel.add(convertedModelLabel,new GridBagConstraints(0,2,1,1,1,1,GridBagConstraints.LINE_START,GridBagConstraints.HORIZONTAL,new Insets(0,0,0,0),0,0));
		final JLabel convertedModelFormatLabel=new JLabel("Output file format:");
		modelConversionSetupPanel.add(convertedModelFormatLabel,new GridBagConstraints(0,3,1,1,1,1,GridBagConstraints.LINE_START,GridBagConstraints.HORIZONTAL,new Insets(0,0,0,0),0,0));
		final JLabel conversionStatusLabel=new JLabel("Conversion status:");
		modelConversionSetupPanel.add(conversionStatusLabel,new GridBagConstraints(0,4,1,1,1,1,GridBagConstraints.LAST_LINE_START,GridBagConstraints.HORIZONTAL,new Insets(0,0,0,0),0,0));
		convertibleModelContentLabel=new JLabel("");
		modelConversionSetupPanel.add(convertibleModelContentLabel,new GridBagConstraints(1,0,1,1,10,1,GridBagConstraints.PAGE_START,GridBagConstraints.HORIZONTAL,new Insets(0,0,0,0),0,0));
		convertibleModelFormatContentLabel=new JLabel("");
		modelConversionSetupPanel.add(convertibleModelFormatContentLabel,new GridBagConstraints(1,1,1,1,10,1,GridBagConstraints.CENTER,GridBagConstraints.HORIZONTAL,new Insets(0,0,0,0),0,0));
		convertedModelDirectoryLabel=new JLabel("");
		convertedModelFilenameTextField=new JTextField();
		convertedModelFilenameTextField.getDocument().addDocumentListener(new DocumentListener(){
			@Override
			public void removeUpdate(DocumentEvent de){
				convertedModelFilenameTextFieldUpdated(de);
			}

			@Override
			public void insertUpdate(DocumentEvent de){
				convertedModelFilenameTextFieldUpdated(de);
			}
			
			@Override
			public void changedUpdate(DocumentEvent de){
				convertedModelFilenameTextFieldUpdated(de);
			}
		});
		convertedModelFilenameTextField.setColumns(20);
		convertedModelExtensionLabel=new JLabel("");
		final JPanel convertedModelEditionPanel=new JPanel(new FlowLayout());
		convertedModelEditionPanel.add(convertedModelDirectoryLabel);
		convertedModelEditionPanel.add(convertedModelFilenameTextField);
		convertedModelEditionPanel.add(convertedModelExtensionLabel);
		modelConversionSetupPanel.add(convertedModelEditionPanel,new GridBagConstraints(1,2,1,1,10,1,GridBagConstraints.CENTER,GridBagConstraints.HORIZONTAL,new Insets(0,0,0,0),0,0));
		convertedModelFormatCombobox=new JComboBox<>(new ModelFileFormat[]{ModelFileFormat.ARDOR3D_BINARY,ModelFileFormat.WAVEFRONT_OBJ});
		convertedModelFormatCombobox.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent ae){
				convertedModelFormatComboboxActionPerformed(ae);
			}
		});
		modelConversionSetupPanel.add(convertedModelFormatCombobox,new GridBagConstraints(1,3,1,1,10,1,GridBagConstraints.CENTER,GridBagConstraints.HORIZONTAL,new Insets(0,0,0,0),0,0));
		final JLabel conversionStatusContentLabel=new JLabel("");
		modelConversionSetupPanel.add(conversionStatusContentLabel,new GridBagConstraints(1,4,1,1,10,1,GridBagConstraints.PAGE_END,GridBagConstraints.HORIZONTAL,new Insets(0,0,0,0),0,0));
		final JButton convertibleModelButton=new JButton("Choose");
		convertibleModelButton.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent ae){
				convertibleModelButtonActionPerformed(ae);
			}
		});
		modelConversionSetupPanel.add(convertibleModelButton,new GridBagConstraints(2,0,1,1,1,1,GridBagConstraints.FIRST_LINE_END,GridBagConstraints.HORIZONTAL,new Insets(0,0,0,0),0,0));
		final JButton convertedModelButton=new JButton("Choose");
		convertedModelButton.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent ae){
				convertedModelButtonActionPerformed(ae);
			}
		});
		modelConversionSetupPanel.add(convertedModelButton,new GridBagConstraints(2,2,1,1,1,1,GridBagConstraints.LINE_END,GridBagConstraints.HORIZONTAL,new Insets(0,0,0,0),0,0));
		conversionButton=new JButton("Run");
		conversionButton.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent ae){
				conversionButtonActionPerformed(ae);
			}
		});
		modelConversionSetupPanel.add(conversionButton,new GridBagConstraints(2,4,1,1,1,1,GridBagConstraints.LAST_LINE_END,GridBagConstraints.HORIZONTAL,new Insets(0,0,0,0),0,0));
		if(getEntity().getConvertedModelFileFormat()==null)
		    getEntity().setConvertedModelFileFormat(ModelFileFormat.values()[0]);
		convertedModelFormatCombobox.setSelectedItem(getEntity().getConvertedModelFileFormat());
		if(getEntity().getConvertedModelFilename()!=null)
			convertedModelFilenameTextField.setText(getEntity().getConvertedModelFilename());
		if(getEntity().getConvertedModelDirectoryPath()!=null)
			convertedModelDirectoryLabel.setText(getEntity().getConvertedModelDirectoryPath()+System.getProperty("file.separator"));
		if(getEntity().getConvertibleModelFilePath()!=null)
			convertibleModelContentLabel.setText(getEntity().getConvertibleModelFilePath());
		updateConvertedModelExtensionLabel();
		updateConversionButton();
	}
	
	@Override
	public ModelConverter getEntity(){
		return((ModelConverter)super.getEntity());
	}
	
	private void convertibleModelButtonActionPerformed(ActionEvent ae){
		fileChooser.setCurrentDirectory(getEntity().getConvertibleModelFilePath()==null?null:new File(getEntity().getConvertibleModelFilePath()));
		fileChooser.setSelectedFile(null);
		fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		for(FileFilter fileFilter:Arrays.asList(fileChooser.getChoosableFileFilters()))
			fileChooser.removeChoosableFileFilter(fileFilter);
		fileChooser.addChoosableFileFilter(new ConvertibleModelFileFilter());
		if(fileChooser.showOpenDialog(this)==JFileChooser.APPROVE_OPTION)
		    {//updates the data model first
			 getEntity().setConvertibleModelFilePath(fileChooser.getSelectedFile().getAbsolutePath());
			 //updates the GUI
		     convertibleModelContentLabel.setText(getEntity().getConvertibleModelFilePath());
		     final ModelFileFormat modelFileFormat=ModelFileFormat.get(getEntity().getConvertibleModelFilePath());
		     if(modelFileFormat!=null)
		         convertibleModelFormatContentLabel.setText(modelFileFormat.getDescription());
			 updateConversionButton();
		    }
	}
	
	private void convertedModelButtonActionPerformed(ActionEvent ae){
		fileChooser.setCurrentDirectory(getEntity().getConvertedModelDirectoryPath()==null?null:new File(getEntity().getConvertedModelDirectoryPath()));
		fileChooser.setSelectedFile(null);
		fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		for(FileFilter fileFilter:Arrays.asList(fileChooser.getChoosableFileFilters()))
			fileChooser.removeChoosableFileFilter(fileFilter);
		if(fileChooser.showOpenDialog(this)==JFileChooser.APPROVE_OPTION)
	        {//updates the data model first
			 getEntity().setConvertedModelDirectoryPath(fileChooser.getSelectedFile().getAbsolutePath());
			 //updates the GUI
	         convertedModelDirectoryLabel.setText(getEntity().getConvertedModelDirectoryPath()+System.getProperty("file.separator"));
			 updateConversionButton();
	        }
	}
	
	private void convertedModelFilenameTextFieldUpdated(DocumentEvent de){
		SwingUtilities.invokeLater(new Runnable(){
			@Override
			public void run(){
				getEntity().setConvertedModelFilename(convertedModelFilenameTextField.getText());
				updateConversionButton();
			}
		});
	}
	
	private void convertedModelFormatComboboxActionPerformed(ActionEvent ae){
		getEntity().setConvertedModelFileFormat((ModelFileFormat)convertedModelFormatCombobox.getSelectedItem());
	    updateConvertedModelExtensionLabel();
	}
	
	private void conversionButtonActionPerformed(ActionEvent ae){
		convert();
	}
	
	private void convert(){
		final File convertedModelFile=new File(new File(getEntity().getConvertedModelDirectoryPath()),getEntity().getConvertedModelFilename()+getEntity().getConvertedModelFileFormat().getExtension());
		//FIXME implement the conversions
	}
	
	private void updateConversionButton(){
		//verifies that all required fields are set and the input format is different from the output format
		final boolean conversionEnabled=getEntity().getConvertibleModelFilePath()!=null&&
				getEntity().getConvertedModelDirectoryPath()!=null&&
				getEntity().getConvertedModelFilename()!=null&&!getEntity().getConvertedModelFilename().isEmpty()&&
				getEntity().getConvertedModelFileFormat()!=null&&
				!ModelFileFormat.get(getEntity().getConvertibleModelFilePath()).equals(getEntity().getConvertedModelFileFormat());
		conversionButton.setEnabled(conversionEnabled);
	}
	
	private void updateConvertedModelExtensionLabel(){
		convertedModelExtensionLabel.setText(getEntity().getConvertedModelFileFormat().getExtension());
		updateConversionButton();
	}
	
	private static final class ConvertibleModelFileFilter extends FileFilter{

		private static final String[] convertibleFileFormatsExtensions=new String[]{".abin",".dae",".md2",".obj"};
		
		@Override
		public final boolean accept(File f){
			boolean result=false;
			if(f!=null)
				if(f.isDirectory())
					result=true;
				else
				    if(f.isFile())
			            {for(String convertibleFileFormatExtension:convertibleFileFormatsExtensions)
				             if(f.getName().endsWith(convertibleFileFormatExtension))
				    	         {result=true;
			    		          break;
			                     }
			            }
			return(result);
		}

		@Override
		public final String getDescription(){
			return("Convertible models");
		}
	}
}
