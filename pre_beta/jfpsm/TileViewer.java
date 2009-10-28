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
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;


final class TileViewer extends Viewer{

    
    private static final long serialVersionUID=1L;
    
    private static final String CUBOID="cuboid";
    
    private static final String NOVOLUME="no volume";
    
    private final CardLayout volumeParametersCardLayout;
    
    private final JPanel volumeParametersPanel;

    
    TileViewer(final Tile tile,final Project project,final ProjectManager projectManager){
        super(tile,project,projectManager);
        setLayout(new GridLayout(1,1));
        if(tile.getVolumeParameters()==null)
            //FIXME: set a cuboid instead
            tile.setVolumeParameters(null);
        //Create the radio buttons for the volume type
        ButtonGroup volumeTypeButtonGroup=new ButtonGroup();
        final JRadioButton[] volumeTypeButtons=new JRadioButton[VolumeType.values().length];
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
        JPanel cuboidParametersPanel=createCuboidParametersPanel();
        volumeParametersPanel.add(cuboidParametersPanel,CUBOID);
        volumeParametersCardLayout.addLayoutComponent(cuboidParametersPanel,CUBOID);
        volumePanel.add(volumeParametersPanel);
        add(volumePanel);
        //Add action listeners
        for(VolumeType volumeType:VolumeType.values())
            volumeTypeButtons[volumeType.ordinal()].addActionListener(new VolumeTypeActionListener(this,volumeType));
        //Select the button that matches with the current volume type
        final VolumeParameters<?> volumeParam=tile.getVolumeParameters();
        if(volumeParam!=null)
            volumeTypeButtons[volumeParam.getVolumeType().ordinal()].setSelected(true);
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
            viewer.showVolumeParametersPart(type);
            //((Tile)viewer.getEntity()).getVolumeParameters();
            //TODO: if the volume type has changed, change the volume parameter
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
    
    private final JPanel createCuboidParametersPanel(){
        JPanel cuboidParametersPanel=new JPanel();
        cuboidParametersPanel.setLayout(new BoxLayout(cuboidParametersPanel,BoxLayout.Y_AXIS));
        cuboidParametersPanel.add(new JLabel("size"));
        final JSlider[][] sliders=new JSlider[][]{createSizeAndOffsetSliders(0),
                                                  createSizeAndOffsetSliders(1),
                                                  createSizeAndOffsetSliders(2)};
        cuboidParametersPanel.add(sliders[0][0]);
        cuboidParametersPanel.add(sliders[1][0]);
        cuboidParametersPanel.add(sliders[2][0]);
        cuboidParametersPanel.add(new JLabel("translation"));
        cuboidParametersPanel.add(sliders[0][1]);
        cuboidParametersPanel.add(sliders[1][1]);
        cuboidParametersPanel.add(sliders[2][1]);
        return(cuboidParametersPanel);
    }
    
    private final JSlider[] createSizeAndOffsetSliders(final int index){
        final JSlider sizeSlider=new JSlider(0,100,100);
        sizeSlider.setMinorTickSpacing(1);
        sizeSlider.setMajorTickSpacing(10);
        sizeSlider.setPaintTicks(true);
        sizeSlider.setPaintLabels(true);
        final JSlider offsetSlider=new JSlider(0,100,0);
        offsetSlider.setMinorTickSpacing(1);
        offsetSlider.setMajorTickSpacing(10);
        offsetSlider.setPaintTicks(true);
        offsetSlider.setPaintLabels(true);
        CuboidParametersChangeListener changeListener=new CuboidParametersChangeListener(sizeSlider,offsetSlider,index,(Tile)getEntity());
        sizeSlider.addChangeListener(changeListener);
        offsetSlider.addChangeListener(changeListener);
        return(new JSlider[]{sizeSlider,offsetSlider});
    }
    
    private static final class CuboidParametersChangeListener implements ChangeListener{
        
        
        private final JSlider sizeSlider;
        
        private final JSlider offsetSlider;
        
        private final int index;
        
        private final Tile tile;
        
        
        private CuboidParametersChangeListener(final JSlider sizeSlider,
                                             final JSlider offsetSlider,
                                             final int index,
                                             final Tile tile){
            this.sizeSlider=sizeSlider;
            this.offsetSlider=offsetSlider;
            this.index=index;
            this.tile=tile;
        }
        
        
        @Override
        public void stateChanged(ChangeEvent e){
            if(!sizeSlider.getValueIsAdjusting()&&
               !offsetSlider.getValueIsAdjusting())
                {int size=sizeSlider.getValue();
                 int offset=offsetSlider.getValue();
                 if(e.getSource()==sizeSlider)
                     {size=Math.max(1,size);
                      int remaining=100-size;
                      int excess=offset-remaining;
                      if(excess>0)
                          {offset=Math.max(0,offset-excess);
                           offsetSlider.setValueIsAdjusting(true);
                           offsetSlider.setValue(offset);
                           offsetSlider.setValueIsAdjusting(false);
                           //TODO: update the offset
                           tile.getVolumeParameters();
                          }
                     }
                 else
                     if(e.getSource()==offsetSlider)
                         {offset=Math.min(99,offset);
                          int remaining=100-offset;
                          int excess=size-remaining;
                          if(excess>0)
                              {size=Math.max(1,size-excess);
                               sizeSlider.setValueIsAdjusting(true);
                               sizeSlider.setValue(size);
                               sizeSlider.setValueIsAdjusting(false);
                               //TODO: update the size
                               tile.getVolumeParameters();
                              }
                         }
                }            
        }       
    }
}
