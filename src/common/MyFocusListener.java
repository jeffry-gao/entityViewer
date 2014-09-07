package common;

import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

import javax.swing.JTextField;

public class MyFocusListener implements FocusListener {

	@Override
	public void focusGained(FocusEvent e) {
		JTextField text = (JTextField)e.getSource();
		text.selectAll();
	}

	@Override
	public void focusLost(FocusEvent e) {
	}
	
}

