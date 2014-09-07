package common;

import java.awt.Dimension;

import javax.swing.BoxLayout;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;

public class ProgressDialog extends JDialog implements Progress {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	JProgressBar bar;
	JLabel currentStatus;
	/**
	 * 
	 */
	public ProgressDialog() {
		setTitle("Progress");
		setMinimumSize(new Dimension(200,60));
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		panel.setMinimumSize(new Dimension(200,60));
		bar = new JProgressBar();
		bar.setPreferredSize(new Dimension(200,30));
		bar.setAlignmentX(LEFT_ALIGNMENT);
		currentStatus = new JLabel();
		currentStatus.setPreferredSize(new Dimension(200,30));
		currentStatus.setAlignmentX(LEFT_ALIGNMENT);
		panel.add(bar);
		panel.add(currentStatus);
		add(panel);
	}
	
	public void setMax(int max){
		bar.setMaximum(max);
	}
	public void setProgress(int progress){
		bar.setValue(progress);
		if(progress>=bar.getMaximum())
			setVisible(false);
	}
	@Override
	public void setCurrentWork(String desc) {
		currentStatus.setText(desc);
	}

	@Override
	public int getProgress() {
		return bar.getValue();
	}
}

