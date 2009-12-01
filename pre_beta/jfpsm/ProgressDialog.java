package jfpsm;

import java.awt.Dimension;
import java.awt.Toolkit;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.border.TitledBorder;

public final class ProgressDialog extends JDialog{

	
	private static final long serialVersionUID=1L;
	
	private final JLabel label;
	
	private final JProgressBar progressBar;
	
	
	public ProgressDialog(JFrame owner,String title){
		//set as modal
		super(owner,title,true);
		JPanel panel=new JPanel();
		panel.setLayout(new BoxLayout(panel,BoxLayout.Y_AXIS));
		final TitledBorder border=BorderFactory.createTitledBorder(getTitle());
		border.setTitleJustification(TitledBorder.CENTER);
		panel.setBorder(border);
		setResizable(false);
		setUndecorated(true);
		Dimension screenSize=Toolkit.getDefaultToolkit().getScreenSize();
		setLocation(((int)screenSize.getWidth()/2)-200,((int)screenSize.getHeight()/2)-150);
		label=new JLabel("");
		progressBar=new JProgressBar(0,100);      
        panel.add(progressBar);
        panel.add(label);
        add(panel);
        setSize(400,300);
	}

	
	public final void reset(){
		progressBar.setValue(0);
		label.setText("");	
	}
	
	public final void setText(final String text){
		label.setText(text);
	}
	
	public final void setValue(final int value){
		progressBar.setValue(value);
	}
}
