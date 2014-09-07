package common;

import java.awt.Dimension;
import java.awt.event.KeyListener;

import javax.swing.JDialog;
import javax.swing.JTextArea;

public class HelpDialog extends JDialog {
	/**
	 * 
	 */
	private static final long serialVersionUID = -4547397906925243200L;

	public HelpDialog(String title, String content, KeyListener l) {
		setTitle(title);
		setFocusable(true);
		JTextArea text = new JTextArea();
		text.setEditable(false);
		text.setText(content);
		text.setFocusable(false);
		setMinimumSize(new Dimension(400,300));
		add(text);
		addKeyListener(l);
	}
}