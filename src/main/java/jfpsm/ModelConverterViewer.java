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
package jfpsm;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.border.TitledBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;

import com.ardor3d.scenegraph.Spatial;

import jfpsm.service.EngineServiceProvider;

/**
 * Graphical user interface of the model converter
 * 
 * TODO: - show the 3D model - display settings specific to some formats -
 * export the data of the key frames in the comments of the WaveFront OBJ file -
 * move the operations for the OBJ OSM files into this class
 * 
 * @author Julien Gouesse
 *
 */
public class ModelConverterViewer extends JFPSMToolUserObjectViewer {

    private static final long serialVersionUID = 1L;

    private final JLabel convertibleModelContentLabel;

    private final JLabel convertibleModelFormatContentLabel;

    private final JLabel convertedModelDirectoryLabel;

    private final JTextField convertedModelFilenameTextField;

    private final JLabel convertedModelExtensionLabel;

    private final JComboBox<ModelFileFormat> convertedModelFormatCombobox;

    private final JButton conversionButton;

    private final FileNameExtensionFilter convertibleModelFileNameExtensionFilter;

    public ModelConverterViewer(final ModelConverter modelConverter, final ToolManager toolManager) {
        super(modelConverter, toolManager);
        final ArrayList<String> convertibleFileFormatsExtensions = new ArrayList<>();
        for (ModelFileFormat modelFileFormat : ModelFileFormat.values())
            if (EngineServiceProvider.getInstance().isLoadable(modelFileFormat))
                convertibleFileFormatsExtensions.add(modelFileFormat.getExtension());
        convertibleModelFileNameExtensionFilter = new FileNameExtensionFilter("Convertible models",
                convertibleFileFormatsExtensions.toArray(new String[convertibleFileFormatsExtensions.size()]));
        setLayout(new BorderLayout());
        final JPanel modelConversionSetupPanel = new JPanel(new GridBagLayout());
        final TitledBorder setupBorder = BorderFactory.createTitledBorder("Setup");
        modelConversionSetupPanel.setBorder(setupBorder);
        add(modelConversionSetupPanel, BorderLayout.SOUTH);
        final JLabel convertibleModelLabel = new JLabel("Model to convert:");
        modelConversionSetupPanel.add(convertibleModelLabel, new GridBagConstraints(0, 0, 1, 1, 1, 1,
                GridBagConstraints.FIRST_LINE_START, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
        final JLabel convertibleModelFormatLabel = new JLabel("Input file format:");
        modelConversionSetupPanel.add(convertibleModelFormatLabel, new GridBagConstraints(0, 1, 1, 1, 1, 1,
                GridBagConstraints.LINE_START, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
        final JLabel convertedModelLabel = new JLabel("Converted model:");
        modelConversionSetupPanel.add(convertedModelLabel, new GridBagConstraints(0, 2, 1, 1, 1, 1,
                GridBagConstraints.LINE_START, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
        final JLabel convertedModelFormatLabel = new JLabel("Output file format:");
        modelConversionSetupPanel.add(convertedModelFormatLabel, new GridBagConstraints(0, 3, 1, 1, 1, 1,
                GridBagConstraints.LINE_START, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
        final JLabel conversionStatusLabel = new JLabel("Conversion status:");
        modelConversionSetupPanel.add(conversionStatusLabel, new GridBagConstraints(0, 4, 1, 1, 1, 1,
                GridBagConstraints.LAST_LINE_START, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
        convertibleModelContentLabel = new JLabel("");
        modelConversionSetupPanel.add(convertibleModelContentLabel, new GridBagConstraints(1, 0, 1, 1, 10, 1,
                GridBagConstraints.PAGE_START, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
        convertibleModelFormatContentLabel = new JLabel("");
        modelConversionSetupPanel.add(convertibleModelFormatContentLabel, new GridBagConstraints(1, 1, 1, 1, 10, 1,
                GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
        convertedModelDirectoryLabel = new JLabel("");
        convertedModelFilenameTextField = new JTextField();
        convertedModelFilenameTextField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void removeUpdate(DocumentEvent de) {
                convertedModelFilenameTextFieldUpdated(de);
            }

            @Override
            public void insertUpdate(DocumentEvent de) {
                convertedModelFilenameTextFieldUpdated(de);
            }

            @Override
            public void changedUpdate(DocumentEvent de) {
                convertedModelFilenameTextFieldUpdated(de);
            }
        });
        convertedModelFilenameTextField.setColumns(20);
        convertedModelExtensionLabel = new JLabel("");
        final JPanel convertedModelEditionPanel = new JPanel(new FlowLayout());
        convertedModelEditionPanel.add(convertedModelDirectoryLabel);
        convertedModelEditionPanel.add(convertedModelFilenameTextField);
        convertedModelEditionPanel.add(convertedModelExtensionLabel);
        modelConversionSetupPanel.add(convertedModelEditionPanel, new GridBagConstraints(1, 2, 1, 1, 10, 1,
                GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
        final ArrayList<ModelFileFormat> writableModelFileFormatsList = new ArrayList<>();
        for (ModelFileFormat modelFileFormat : ModelFileFormat.values())
            if (EngineServiceProvider.getInstance().isSavable(modelFileFormat))
                writableModelFileFormatsList.add(modelFileFormat);
        convertedModelFormatCombobox = new JComboBox<>(
                writableModelFileFormatsList.toArray(new ModelFileFormat[writableModelFileFormatsList.size()]));
        convertedModelFormatCombobox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                convertedModelFormatComboboxActionPerformed(ae);
            }
        });
        modelConversionSetupPanel.add(convertedModelFormatCombobox, new GridBagConstraints(1, 3, 1, 1, 10, 1,
                GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
        final JLabel conversionStatusContentLabel = new JLabel("");
        modelConversionSetupPanel.add(conversionStatusContentLabel, new GridBagConstraints(1, 4, 1, 1, 10, 1,
                GridBagConstraints.PAGE_END, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
        final JButton convertibleModelButton = new JButton("Choose");
        convertibleModelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                convertibleModelButtonActionPerformed(ae);
            }
        });
        modelConversionSetupPanel.add(convertibleModelButton, new GridBagConstraints(2, 0, 1, 1, 1, 1,
                GridBagConstraints.FIRST_LINE_END, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
        final JButton convertedModelButton = new JButton("Choose");
        convertedModelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                convertedModelButtonActionPerformed(ae);
            }
        });
        modelConversionSetupPanel.add(convertedModelButton, new GridBagConstraints(2, 2, 1, 1, 1, 1,
                GridBagConstraints.LINE_END, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
        conversionButton = new JButton("Run");
        conversionButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                conversionButtonActionPerformed(ae);
            }
        });
        modelConversionSetupPanel.add(conversionButton, new GridBagConstraints(2, 4, 1, 1, 1, 1,
                GridBagConstraints.LAST_LINE_END, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
        if (getEntity().getConvertedModelFileFormat() == null)
            getEntity().setConvertedModelFileFormat(ModelFileFormat.values()[0]);
        convertedModelFormatCombobox.setSelectedItem(getEntity().getConvertedModelFileFormat());
        if (getEntity().getConvertedModelFilename() != null)
            convertedModelFilenameTextField.setText(getEntity().getConvertedModelFilename());
        if (getEntity().getConvertedModelDirectoryPath() != null)
            convertedModelDirectoryLabel
                    .setText(getEntity().getConvertedModelDirectoryPath() + System.getProperty("file.separator"));
        if (getEntity().getConvertibleModelFilePath() != null)
            convertibleModelContentLabel.setText(getEntity().getConvertibleModelFilePath());
        updateConvertedModelExtensionLabel();
        updateConversionButton();
    }

    @Override
    public ModelConverter getEntity() {
        return ((ModelConverter) super.getEntity());
    }

    private void convertibleModelButtonActionPerformed(ActionEvent ae) {
        final JFileChooser fileChooser = new JFileChooser();
        fileChooser.setCurrentDirectory(getEntity().getConvertibleModelFilePath() == null ? null
                : new File(getEntity().getConvertibleModelFilePath()));
        fileChooser.setSelectedFile(null);
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        for (FileFilter fileFilter : Arrays.asList(fileChooser.getChoosableFileFilters()))
            fileChooser.removeChoosableFileFilter(fileFilter);
        fileChooser.setFileFilter(convertibleModelFileNameExtensionFilter);
        if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {// updates
                                                                              // the
                                                                              // data
                                                                              // model
                                                                              // first
            getEntity().setConvertibleModelFilePath(fileChooser.getSelectedFile().getAbsolutePath());
            // updates the GUI
            convertibleModelContentLabel.setText(getEntity().getConvertibleModelFilePath());
            final ModelFileFormat modelFileFormat = ModelFileFormat.get(getEntity().getConvertibleModelFilePath());
            if (modelFileFormat != null)
                convertibleModelFormatContentLabel.setText(modelFileFormat.getDescription());
            updateConversionButton();
        }
    }

    private void convertedModelButtonActionPerformed(ActionEvent ae) {
        final JFileChooser fileChooser = new JFileChooser();
        fileChooser.setCurrentDirectory(getEntity().getConvertedModelDirectoryPath() == null ? null
                : new File(getEntity().getConvertedModelDirectoryPath()));
        fileChooser.setSelectedFile(null);
        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {// updates
                                                                              // the
                                                                              // data
                                                                              // model
                                                                              // first
            getEntity().setConvertedModelDirectoryPath(fileChooser.getSelectedFile().getAbsolutePath());
            // updates the GUI
            convertedModelDirectoryLabel
                    .setText(getEntity().getConvertedModelDirectoryPath() + System.getProperty("file.separator"));
            updateConversionButton();
        }
    }

    private void convertedModelFilenameTextFieldUpdated(DocumentEvent de) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                getEntity().setConvertedModelFilename(convertedModelFilenameTextField.getText());
                updateConversionButton();
            }
        });
    }

    private void convertedModelFormatComboboxActionPerformed(ActionEvent ae) {
        getEntity().setConvertedModelFileFormat((ModelFileFormat) convertedModelFormatCombobox.getSelectedItem());
        updateConvertedModelExtensionLabel();
    }

    private void conversionButtonActionPerformed(ActionEvent ae) {
        convert();
    }

    private void convert() {
        final File inputModelFile = new File(getEntity().getConvertibleModelFilePath());
        if (!inputModelFile.exists())
            throw new IllegalArgumentException(
                    "The input file " + inputModelFile.getAbsolutePath() + " does not exist");
        if (!inputModelFile.canRead())
            throw new IllegalArgumentException(
                    "The input file " + inputModelFile.getAbsolutePath() + " cannot be read");
        final File convertedModelDirectory = new File(getEntity().getConvertedModelDirectoryPath());
        final String convertedModelFilename = getEntity().getConvertedModelFilename();
        final File outputModelFile = new File(convertedModelDirectory,
                convertedModelFilename + "." + getEntity().getConvertedModelFileFormat().getExtension());
        if (!outputModelFile.exists()) {
            boolean success = false;
            try {
                success = outputModelFile.createNewFile();
            } catch (IOException ioe) {
                throw new IllegalArgumentException(
                        "The output file " + outputModelFile.getAbsolutePath() + " cannot be created", ioe);
            }
            if (!success)
                throw new IllegalArgumentException(
                        "The output file " + outputModelFile.getAbsolutePath() + " cannot be created");
        }
        if (!outputModelFile.canWrite())
            throw new IllegalArgumentException(
                    "The output file " + outputModelFile.getAbsolutePath() + " cannot be written");
        final File secondaryOutputModelFile;
        if (getEntity().getConvertedModelFileFormat().getSecondaryExtension() == null)
            secondaryOutputModelFile = null;
        else {
            secondaryOutputModelFile = new File(convertedModelDirectory,
                    convertedModelFilename + "." + getEntity().getConvertedModelFileFormat().getSecondaryExtension());
            if (!secondaryOutputModelFile.exists()) {
                boolean success = false;
                try {
                    success = secondaryOutputModelFile.createNewFile();
                } catch (IOException ioe) {
                    throw new IllegalArgumentException("The secondary output file "
                            + secondaryOutputModelFile.getAbsolutePath() + " cannot be created", ioe);
                }
                if (!success)
                    throw new IllegalArgumentException("The secondary output file "
                            + secondaryOutputModelFile.getAbsolutePath() + " cannot be created");
            }
            if (!secondaryOutputModelFile.canWrite())
                throw new IllegalArgumentException(
                        "The secondary output file " + outputModelFile.getAbsolutePath() + " cannot be written");
        }
        final ModelFileFormat inputModelFileFormat = ModelFileFormat.get(getEntity().getConvertibleModelFilePath());
        final ModelFileFormat outputModelFileFormat = getEntity().getConvertedModelFileFormat();
        // prevents another conversion from being run at the same time
        toolManager.setQuitEnabled(false);
        updateConversionButton();
        // runs the current one
        new ModelConversionSwingWorker(inputModelFile, outputModelFile, secondaryOutputModelFile, inputModelFileFormat,
                outputModelFileFormat, toolManager, toolManager.progressDialog, this).execute();
    }

    private static final class ModelConversionSwingWorker extends SwingWorker</* Spatial */Object, String> {

        private final File inputModelFile;

        private final File outputModelFile;

        private final File secondaryOutputModelFile;

        private final ModelFileFormat inputModelFileFormat;

        private final ModelFileFormat outputModelFileFormat;

        private final ToolManager toolManager;

        private final ProgressDialog dialog;

        private final ModelConverterViewer modelConverterViewer;

        private ModelConversionSwingWorker(final File inputModelFile, final File outputModelFile,
                final File secondaryOutputModelFile, final ModelFileFormat inputModelFileFormat,
                final ModelFileFormat outputModelFileFormat, final ToolManager toolManager, final ProgressDialog dialog,
                final ModelConverterViewer modelConverterViewer) {
            super();
            this.inputModelFile = inputModelFile;
            this.outputModelFile = outputModelFile;
            this.secondaryOutputModelFile = secondaryOutputModelFile;
            this.inputModelFileFormat = inputModelFileFormat;
            this.outputModelFileFormat = outputModelFileFormat;
            this.toolManager = toolManager;
            this.dialog = dialog;
            this.modelConverterViewer = modelConverterViewer;
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public final void run() {
                    dialog.reset();
                    dialog.setVisible(true);
                }
            });
        }

        @Override
        protected Object doInBackground() throws Exception {
            try {
                final Spatial convertible = EngineServiceProvider.getInstance().load(inputModelFile, inputModelFileFormat);
                publish("Loading successful");
                // FIXME call dialog.setValue(50) on the EDT
                EngineServiceProvider.getInstance().save(outputModelFile, outputModelFileFormat, secondaryOutputModelFile,
                        convertible);
                publish("Conversion successful");
                // FIXME call dialog.setValue(100) on the EDT
                return (convertible);
            } catch (Exception e) {// forces the call to done() even though the
                                   // conversion has just failed
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        done();
                    }
                });
                // FIXME the exception shouldn't be rethrown here because the
                // SwingWorker just silently stops itself without reporting
                // anything
                throw e;
            }
        }

        @Override
        protected final void process(List<String> chunks) {
            final StringBuilder builder = new StringBuilder();
            for (String chunk : chunks) {
                builder.append(chunk);
                builder.append(" ");
            }
            dialog.setText(builder.toString().trim());
        }

        @Override
        protected final void done() {
            // allows the user to leave the application
            toolManager.setQuitEnabled(true);
            modelConverterViewer.updateConversionButton();
            dialog.setVisible(false);
            dialog.reset();
        }
    }

    private void updateConversionButton() {
        // verifies that the tool manager is ready, all required fields are set
        // and the input format is different from the output format
        final boolean conversionEnabled = toolManager.isQuitEnabled()
                && getEntity().getConvertibleModelFilePath() != null
                && getEntity().getConvertedModelDirectoryPath() != null
                && getEntity().getConvertedModelFilename() != null && !getEntity().getConvertedModelFilename().isEmpty()
                && getEntity().getConvertedModelFileFormat() != null
                && !ModelFileFormat.get(getEntity().getConvertibleModelFilePath())
                        .equals(getEntity().getConvertedModelFileFormat());
        conversionButton.setEnabled(conversionEnabled);
    }

    private void updateConvertedModelExtensionLabel() {
        convertedModelExtensionLabel.setText("." + getEntity().getConvertedModelFileFormat().getExtension());
        updateConversionButton();
    }
}
