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
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.TitledBorder;

public class ModelConverterViewer extends JFPSMToolUserObjectViewer{
	
	private static final long serialVersionUID=1L;
	
	private final JLabel convertibleModelContentLabel;
	
	private final JButton conversionButton;
	
	private File convertibleModelFile;
	
	private File convertedModelDir;
	
	private File convertedModelFile;

	public ModelConverterViewer(final ModelConverter modelConverter,final ToolManager toolManager){
		super(modelConverter,toolManager);
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
		final JLabel convertibleModelFormatContentLabel=new JLabel("");
		modelConversionSetupPanel.add(convertibleModelFormatContentLabel,new GridBagConstraints(1,1,1,1,10,1,GridBagConstraints.CENTER,GridBagConstraints.HORIZONTAL,new Insets(0,0,0,0),0,0));
		final JLabel convertedModelContentLabel=new JLabel("");
		modelConversionSetupPanel.add(convertedModelContentLabel,new GridBagConstraints(1,2,1,1,10,1,GridBagConstraints.CENTER,GridBagConstraints.HORIZONTAL,new Insets(0,0,0,0),0,0));
		final JComboBox<String> convertedModelFormatCombobox=new JComboBox<>(new String[]{"WaveFront OBJ","Ardor3D Binary"});
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
		updateConversionButton();
	}
	
	private void convertibleModelButtonActionPerformed(ActionEvent ae){
		//TODO use file format filters
		final JFileChooser fileChooser=new JFileChooser(convertibleModelFile);
		if(fileChooser.showOpenDialog(this)==JFileChooser.APPROVE_OPTION)
		    {convertibleModelFile=fileChooser.getSelectedFile();
		     convertibleModelContentLabel.setText(convertibleModelFile.getAbsolutePath());
		     //TODO update the input format
			 updateConversionButton();
		    }
		
	}
	
	private void convertedModelButtonActionPerformed(ActionEvent ae){
		//TODO
		updateConversionButton();
	}
	
	private void conversionButtonActionPerformed(ActionEvent ae){
		
	}
	
	private void updateConversionButton(){
		final boolean conversionEnabled=convertibleModelFile!=null&&convertedModelDir!=null&&convertedModelFile!=null;
		conversionButton.setEnabled(conversionEnabled);
	}
}
