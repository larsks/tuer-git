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

    
    TileViewer(final Tile tile,final Project project,final ProjectManager projectManager){
        super(tile,project,projectManager);
        setLayout(new GridLayout(1,1));
        ButtonGroup volumeTypeButtonGroup=new ButtonGroup();
        JRadioButton cuboidButton=new JRadioButton("cuboid");
        JRadioButton parallelepipedButton=new JRadioButton("parallelepiped");
        JRadioButton quadFrustumButton=new JRadioButton("quadrilateral frustum");
        JRadioButton teleporterButton=new JRadioButton("displacement teleporter");
        JRadioButton floorLinkButton=new JRadioButton("floor link");
        final JRadioButton[] volumeTypeButtons=new JRadioButton[]{cuboidButton,parallelepipedButton,quadFrustumButton,teleporterButton,floorLinkButton};
        for(JRadioButton button:volumeTypeButtons)
            volumeTypeButtonGroup.add(button);       
        JPanel volumePanel=new JPanel();
        volumePanel.setLayout(new BoxLayout(volumePanel,BoxLayout.X_AXIS));
        //Add the panel to choose a volume type
        volumePanel.add(createVolumeChoicePanel(volumeTypeButtons));
        //Add the panel for volume parameters
        final JPanel volumeParametersPanel=new JPanel();
        final CardLayout volumeParametersCardLayout=new CardLayout();
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
        ActionListener noVolumeActionListener=new ActionListener(){         
            @Override
            public void actionPerformed(ActionEvent e){
                volumeParametersCardLayout.show(volumeParametersPanel,NOVOLUME);
            }
        };
        ActionListener cuboidActionListener=new ActionListener(){         
            @Override
            public void actionPerformed(ActionEvent e){
                volumeParametersCardLayout.show(volumeParametersPanel,CUBOID);
            }
        };
        cuboidButton.addActionListener(cuboidActionListener);
        parallelepipedButton.addActionListener(noVolumeActionListener);
        quadFrustumButton.addActionListener(noVolumeActionListener);
        teleporterButton.addActionListener(noVolumeActionListener);        
        floorLinkButton.addActionListener(noVolumeActionListener);
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
        final JSlider[][] sliders=new JSlider[][]{createSizeAndOffsetSliders(),
                                                  createSizeAndOffsetSliders(),
                                                  createSizeAndOffsetSliders()};
        cuboidParametersPanel.add(sliders[0][0]);
        cuboidParametersPanel.add(sliders[1][0]);
        cuboidParametersPanel.add(sliders[2][0]);
        cuboidParametersPanel.add(new JLabel("translation"));
        cuboidParametersPanel.add(sliders[0][1]);
        cuboidParametersPanel.add(sliders[1][1]);
        cuboidParametersPanel.add(sliders[2][1]);
        return(cuboidParametersPanel);
    }
    
    private final JSlider[] createSizeAndOffsetSliders(){
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
        TileParametersChangeListener changeListener=new TileParametersChangeListener(sizeSlider,offsetSlider);
        sizeSlider.addChangeListener(changeListener);
        offsetSlider.addChangeListener(changeListener);
        return(new JSlider[]{sizeSlider,offsetSlider});
    }
    
    private static final class TileParametersChangeListener implements ChangeListener{
        
        
        private final JSlider sizeSlider;
        
        private final JSlider offsetSlider;
        
        
        private TileParametersChangeListener(final JSlider sizeSlider,
                                             final JSlider offsetSlider){
            this.sizeSlider=sizeSlider;
            this.offsetSlider=offsetSlider;
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
                              }
                         }
                }            
        }       
    }
}
