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

import java.awt.CardLayout;
import java.awt.Component;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;


final class TileViewer extends Viewer{

    
    private static final long serialVersionUID=1L;
    
    private static final String CUBOID="cuboid";
    
    private static final String NOVOLUME="no volume";
    
    private final CardLayout volumeParametersCardLayout;
    
    private final JPanel volumeParametersPanel;
    
    private final JRadioButton[] volumeTypeButtons;

    
    TileViewer(final Tile tile,final Project project,final ProjectManager projectManager){
        super(tile,project,projectManager);
        setLayout(new GridLayout(1,1));
        if(tile.getVolumeParameters()==null)
            tile.setVolumeParameters(new CuboidParameters());
        //Create the radio buttons for the volume type
        ButtonGroup volumeTypeButtonGroup=new ButtonGroup();
        volumeTypeButtons=new JRadioButton[VolumeType.values().length];
        for(VolumeType volumeType:VolumeType.values())
            {volumeTypeButtons[volumeType.ordinal()]=new JRadioButton(volumeType.getLabel());
             volumeTypeButtonGroup.add(volumeTypeButtons[volumeType.ordinal()]);
            }      
        JPanel volumePanel=new JPanel();
        volumePanel.setLayout(new BoxLayout(volumePanel,BoxLayout.X_AXIS));
        //Add the panel to choose a volume type
        volumePanel.add(createVolumeChoicePanel(volumeTypeButtons));
        //Add the panel for volume parameters
        volumeParametersPanel=new JPanel();
        volumeParametersCardLayout=new CardLayout();
        volumeParametersPanel.setLayout(volumeParametersCardLayout);
        volumeParametersPanel.setAlignmentY(Component.TOP_ALIGNMENT);
        //Add a panel for no volume
        JPanel noVolumePanel=new JPanel();
        volumeParametersPanel.add(noVolumePanel,NOVOLUME);
        volumeParametersCardLayout.addLayoutComponent(noVolumePanel,NOVOLUME);       
        //Add a panel for cuboids
        JPanel cuboidParametersPanel=new CuboidParametersPanel((Tile)getEntity());
        volumeParametersPanel.add(cuboidParametersPanel,CUBOID);
        volumeParametersCardLayout.addLayoutComponent(cuboidParametersPanel,CUBOID);
        volumePanel.add(volumeParametersPanel);
        add(volumePanel);
        //Add action listeners
        for(VolumeType volumeType:VolumeType.values())
            volumeTypeButtons[volumeType.ordinal()].addActionListener(new VolumeTypeActionListener(this,volumeType));        
    }
    
    @Override
    public final void setVisible(boolean visible){
        super.setVisible(visible);
        if(isVisible())
            {//Select the button that matches with the current volume type
             final VolumeParameters volumeParam=((Tile)getEntity()).getVolumeParameters();
             if(volumeParam!=null)
                  {volumeTypeButtons[volumeParam.getVolumeType().ordinal()].setSelected(true);
                   showVolumeParametersPart(volumeParam.getVolumeType());
                  }
            }
    }
    
    private static final class VolumeTypeActionListener implements ActionListener{

        
        private final TileViewer viewer;
        
        private final VolumeType type;
        
        
        private VolumeTypeActionListener(final TileViewer viewer,final VolumeType type){
            this.viewer=viewer;
            this.type=type;
        }
        
        
        @Override
        public void actionPerformed(ActionEvent e){
            //Change the volume parameters if needed
            final Tile tile=(Tile)viewer.getEntity();
            VolumeParameters volumeParam=tile.getVolumeParameters();
            if(type.equals(VolumeType.CUBOID))
                {if(volumeParam==null)
                     tile.setVolumeParameters(new CuboidParameters());
                }
            else
                {if(volumeParam!=null)
                    tile.setVolumeParameters(null);
                }
            //Show the editor part
            viewer.showVolumeParametersPart(type);
        }
        
    }
    
    private final void showVolumeParametersPart(VolumeType volumeType){
        if(volumeType.equals(VolumeType.CUBOID))
            volumeParametersCardLayout.show(volumeParametersPanel,CUBOID);
        else
            volumeParametersCardLayout.show(volumeParametersPanel,NOVOLUME);
    }
    
    private final JPanel createVolumeChoicePanel(JRadioButton[] volumeTypeButtons){        
        JPanel volumeChoicePanel=new JPanel();
        volumeChoicePanel.setLayout(new BoxLayout(volumeChoicePanel,BoxLayout.Y_AXIS));
        volumeChoicePanel.setAlignmentY(Component.TOP_ALIGNMENT);
        volumeChoicePanel.add(new JLabel("volume type"));            
        for(JRadioButton button:volumeTypeButtons)
            volumeChoicePanel.add(button);
        return(volumeChoicePanel);
    }
}
