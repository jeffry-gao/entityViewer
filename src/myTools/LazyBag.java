package myTools;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.UIManager;

public class LazyBag extends JPanel {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private final String rootDir = "C:/";
	private final String destFolder = "jsc2013jpn";
	private final String envTest="ŒŸØ";
	private final String envDev="ŠJ”­";
	private final String execPath="C:/jsc2013jpn/jsc/sc/cc/bin/cur/Exec.exe";
	
	LazyBag(){
        super();
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        JButton button1=new JButton(envTest+"(K)");
        button1.setMnemonic('K');
        button1.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				envSwitch(envTest);
			}
		});
        JButton button2=new JButton(envDev+"(D)");
        button2.setMnemonic('D');
        button2.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				envSwitch(envDev);
			}
		});
        add(button1);
        add(button2);
	}
	private void envSwitch(String destEnv){
		File jscRoot = new File(rootDir);
		boolean backed = false;

		for(File jscClient : jscRoot.listFiles()){
			String folderName=jscClient.getName();
			if(folderName.equals(destFolder+destEnv)){
				backed = true;
				break;
			}
		}
		if(backed){
			File oldJsc = new File(rootDir+destFolder);
			if(oldJsc.isDirectory()){
				boolean renameOK=false;
				for(File subFold : oldJsc.listFiles()){
					String foldName = subFold.getName();
					if(foldName.startsWith("ENV")){
						String envName[] = foldName.split(" ");
						if(envName.length>1){
							oldJsc.renameTo(new File(rootDir+destFolder+envName[1]));
							
							File newFold = new File(rootDir+destFolder+destEnv);
							newFold.renameTo(new File(rootDir+destFolder));
							renameOK = true;
							break;
						}
					}
				}
				if(!renameOK){
					return;
				}
			}
		}
		try {
			File jsc2run = new File(rootDir+destFolder);
			for(File subFold : jsc2run.listFiles()){
				String foldName = subFold.getName();
				if(foldName.startsWith("ENV")){
					String envName = foldName.split(" ")[1];
					if(envName.equals(destEnv)){
						Runtime.getRuntime().exec(execPath);
						break;
					}
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
    private static void createAndShowGUI() {
        //Create and set up the window.
        JFrame frame = new JFrame("TableFilterDemo");

        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        //Create and set up the content pane.
        LazyBag newContentPane = new LazyBag();
        newContentPane.setOpaque(true); //content panes must be opaque
        frame.setContentPane(newContentPane);

        //Display the window.
        frame.pack();
        frame.setVisible(true);
    }

    public static void main(String[] args) {
        //Schedule a job for the event-dispatching thread:
        //creating and showing this application's GUI.
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
				UIManager.put("swing.boldMetal", Boolean.FALSE);
                createAndShowGUI();
            }
        });

    }
}
