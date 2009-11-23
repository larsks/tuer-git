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

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSlider;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

final class CuboidParametersPanel extends JPanel{
    
    
    private static final long serialVersionUID=1L;

    private final JSlider[][] sliders;
    
    private final JRadioButton[][] radioButtons;
    
    private final JSpinner[][] uvTexCoordSpinners;
    
    private final Tile tile;
    
    
    CuboidParametersPanel(Tile tile){
        this.tile=tile;
        sliders=new JSlider[][]{createSizeAndOffsetSliders(0),
                createSizeAndOffsetSliders(1),
                createSizeAndOffsetSliders(2)};
        setLayout(new BoxLayout(this,BoxLayout.Y_AXIS));
        add(new JLabel("size"));       
        add(sliders[0][0]);
        add(sliders[1][0]);
        add(sliders[2][0]);
        add(new JLabel("translation"));
        add(sliders[0][1]);
        add(sliders[1][1]);
        add(sliders[2][1]);
        add(new JLabel("orientation"));
        CuboidParameters cuboidParam=(CuboidParameters)tile.getVolumeParameters();
        ButtonGroup sideButtonGroup;
        final JPanel orientationSubPanel=new JPanel(new FlowLayout());
        JPanel sideSubPanel;
        radioButtons=new JRadioButton[CuboidParameters.Side.values().length][CuboidParameters.Orientation.values().length];
        uvTexCoordSpinners=new JSpinner[CuboidParameters.Side.values().length][4];
        for(CuboidParameters.Side side:CuboidParameters.Side.values())
            {sideButtonGroup=new ButtonGroup();
             sideSubPanel=new JPanel();
             sideSubPanel.setLayout(new BoxLayout(sideSubPanel,BoxLayout.Y_AXIS));
             sideSubPanel.add(new JLabel(side.toString().toLowerCase()));
             for(int spIndex=0;spIndex<4;spIndex++)
                 {uvTexCoordSpinners[side.ordinal()][spIndex]=new JSpinner(new SpinnerNumberModel(Float.valueOf(0),Float.valueOf(0),Float.valueOf(1),Float.valueOf(0.01f)));
                  uvTexCoordSpinners[side.ordinal()][spIndex].setEditor(new JSpinner.NumberEditor(uvTexCoordSpinners[side.ordinal()][spIndex]));
                  uvTexCoordSpinners[side.ordinal()][spIndex].setMinimumSize(new Dimension(25,uvTexCoordSpinners[side.ordinal()][spIndex].getMinimumSize().height));
                  uvTexCoordSpinners[side.ordinal()][spIndex].setPreferredSize(new Dimension(uvTexCoordSpinners[side.ordinal()][spIndex].getMinimumSize().width,uvTexCoordSpinners[side.ordinal()][spIndex].getPreferredSize().height));
                  uvTexCoordSpinners[side.ordinal()][spIndex].addChangeListener(new UVSpinnerChangeListener(side,cuboidParam,spIndex));                 
                 }            
             for(CuboidParameters.Orientation orientation:CuboidParameters.Orientation.values())
                 {radioButtons[side.ordinal()][orientation.ordinal()]=new JRadioButton(orientation.toString().toLowerCase());
                  radioButtons[side.ordinal()][orientation.ordinal()].addActionListener(new OrientationActionListener(orientation,side,cuboidParam,uvTexCoordSpinners[side.ordinal()]));
                  sideButtonGroup.add(radioButtons[side.ordinal()][orientation.ordinal()]);
                  sideSubPanel.add(radioButtons[side.ordinal()][orientation.ordinal()]);
                 }
             for(JSpinner spinner:uvTexCoordSpinners[side.ordinal()])
                 sideSubPanel.add(spinner);
             orientationSubPanel.add(sideSubPanel);
            }
        add(orientationSubPanel);
    }
    
    
    @Override
    public final void setVisible(boolean visible){
        super.setVisible(visible);
        if(isVisible())
            {CuboidParameters cuboidParam=(CuboidParameters)tile.getVolumeParameters();
             // Set the sliders to the position corresponding to the stored data
             for(int i=0;i<3;i++)
                 for(int j=0;j<2;j++)
                     sliders[i][j].setValueIsAdjusting(true);
             for(int i=0;i<3;i++)
                 {sliders[i][0].setValue(Math.round(cuboidParam.getSize()[i]*100));
                  sliders[i][1].setValue(Math.round(cuboidParam.getOffset()[i]*100));
                 }
             for(int i=0;i<3;i++)
                 for(int j=0;j<2;j++)
                     sliders[i][j].setValueIsAdjusting(false);
             // Set the radio buttons to the selection corresponding to the stored data            
             CuboidParameters.Orientation orientation;
             boolean isSpinnerEnabled;
             for(CuboidParameters.Side side:CuboidParameters.Side.values())
                 {orientation=cuboidParam.getOrientation(side);
                  isSpinnerEnabled=orientation!=CuboidParameters.Orientation.NONE;
                  radioButtons[side.ordinal()][orientation.ordinal()].setSelected(true);
                  for(JSpinner spinner:uvTexCoordSpinners[side.ordinal()])
                      spinner.setEnabled(isSpinnerEnabled);
                  if(isSpinnerEnabled)
                      {// Update the spinners too
                       for(int spIndex=0;spIndex<4;spIndex++)
                           uvTexCoordSpinners[side.ordinal()][spIndex].setValue(Float.valueOf(((CuboidParameters)tile.getVolumeParameters()).getTexCoord(side,spIndex)));
                      }
                 }
            }
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
        SizeAndOffsetChangeListener changeListener=new SizeAndOffsetChangeListener(sizeSlider,offsetSlider,index,tile);
        sizeSlider.addChangeListener(changeListener);
        offsetSlider.addChangeListener(changeListener);
        return(new JSlider[]{sizeSlider,offsetSlider});
    }
    
    private static final class SizeAndOffsetChangeListener implements ChangeListener{
        
        
        private final JSlider sizeSlider;
        
        private final JSlider offsetSlider;
        
        private final int index;
        
        private final Tile tile;
        
        
        private SizeAndOffsetChangeListener(final JSlider sizeSlider,
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
                 CuboidParameters cuboidParam=(CuboidParameters)tile.getVolumeParameters();
                 if(e.getSource()==sizeSlider)
                     {size=Math.max(1,size);
                      int remaining=100-size;
                      int excess=offset-remaining;
                      if(excess>0)
                          {offset=Math.max(0,offset-excess);
                           offsetSlider.setValueIsAdjusting(true);
                           offsetSlider.setValue(offset);
                           offsetSlider.setValueIsAdjusting(false);                          
                           cuboidParam.setOffset(index,offset/100.0f);
                          }
                      cuboidParam.setSize(index,size/100.0f);
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
                               cuboidParam.setSize(index,size/100.0f);
                              }
                          cuboidParam.setOffset(index,offset/100.0f);
                         }
                }            
        }       
    }
    
    private static final class OrientationActionListener implements ActionListener{
        
        
        private final CuboidParameters.Orientation orientation;
        
        private final CuboidParameters.Side side;
        
        private final CuboidParameters cuboidParam;
        
        private final JSpinner[] uvTexCoordSpinners;
        
        
        private OrientationActionListener(final CuboidParameters.Orientation orientation,
                final CuboidParameters.Side side,final CuboidParameters cuboidParam,final JSpinner[] uvTexCoordSpinners){
            this.orientation=orientation;
            this.side=side;
            this.cuboidParam=cuboidParam;
            this.uvTexCoordSpinners=uvTexCoordSpinners;
        }
        
        
        @Override
        public final void actionPerformed(final ActionEvent ae){
            cuboidParam.setOrientation(side,orientation);
            final boolean isEnabled=orientation!=CuboidParameters.Orientation.NONE;
            for(JSpinner spinner:uvTexCoordSpinners)
                spinner.setEnabled(isEnabled);
        }
    }
    
    private static final class UVSpinnerChangeListener implements ChangeListener{
        
        
        private final CuboidParameters.Side side;
        
        private final CuboidParameters cuboidParam;
        
        private final int texCoordIndex;
        
        
        private UVSpinnerChangeListener(final CuboidParameters.Side side,
                final CuboidParameters cuboidParam,final int texCoordIndex){
            this.side=side;
            this.cuboidParam=cuboidParam;
            this.texCoordIndex=texCoordIndex;
        }
        
        @Override
        public final void stateChanged(final ChangeEvent ce){
            cuboidParam.setTexCoord(side,texCoordIndex,((Number)((JSpinner)ce.getSource()).getValue()).floatValue());
        }
    }
}
