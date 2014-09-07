package myTools;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.HierarchyEvent;
import java.awt.event.HierarchyListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.RowFilter;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.UIManager;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableRowSorter;
import common.MyFocusListener;
import common.ProgressDialog;
import common.WindowsShell;


public class SourceViewer extends JPanel{
	/**
	 * 
	 */
	
	private static final long serialVersionUID = 582601465647537897L;
	private static final int SEP_SIZE = 10;
	private final String unsavedProfileName = "[unsaved]";
	private final int    heightProfile = 30;
	private final int    widthTextField = 300;
	
	private boolean   normalMode = true; // true: normal; false:profile edit
	private JLabel	   labelProfile;
	private JList      listProfileName;
	private JTextField textNewProfileName;
	private JTextField textSourceDir;
	private JButton    buttonSourceChooser;
	private JTextField textFileTypes;	
	private JButton    buttonAddProfile;
	private ProgressDialog pd;
	private Timer		timer;
	
	private JLabel				labelFilter;
	private JTextField 			textFieldFilter;
	private JTable 				tableFiles;
    private SourceListModel 	m_service_model;
    private TableRowSorter<SourceListModel> sorterFileTable;
    
    private List<SourceProfile> listProfile;
    private SourceProfile		curProfile;
    private boolean 			flagDirty=false;
    
    class SourceProfile {
    	String profileName="";
    	String sourceDir="";
    	String fileTypes="";
    	String encodingType="";
    	long   loadTime=0;
    	List<String> listFiles=null;
    	
    	SourceProfile(){
    		listFiles = new ArrayList<String>();
    	}
    	SourceProfile(SourceProfile s){
    		this.profileName=s.profileName;
    		this.sourceDir=s.sourceDir;
    		this.fileTypes=s.fileTypes;
    		this.encodingType=s.encodingType;
    		listFiles = new ArrayList<String>();
    	}
    	boolean equals(SourceProfile s){
    		return this.sourceDir.equals(s.sourceDir)
    				&&this.fileTypes.equals(s.fileTypes)
    				&&this.encodingType.equals(s.encodingType);
    	}
    }
    public SourceViewer() {
        super();
        
        listProfile = new ArrayList<SourceProfile>();
        setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
        addHierarchyListener(new HierarchyListener() {

            @Override
            public void hierarchyChanged(HierarchyEvent e) {
                // This can be optimized by checking the right flags, but I leave that up to you to look into
                boolean connected = setupListenersWhenConnected();

                if (connected) {
                    removeHierarchyListener(this);
                }
            }
        });
        
        JPanel profileMenu = new JPanel();
        profileMenu.setLayout(new BoxLayout(profileMenu, BoxLayout.Y_AXIS));
        
		loadSetting();
		labelProfile = new JLabel("All Profile");
		
		listProfileName = new JList();
		listProfileName.setMinimumSize(new Dimension(widthTextField,heightProfile));
		listProfileName.setMaximumSize(new Dimension(widthTextField,heightProfile*5));
		listProfileName.setAlignmentX(LEFT_ALIGNMENT);
		listProfileName.setAlignmentY(TOP_ALIGNMENT);
		listProfileName.setVisibleRowCount(10);
		labelProfile.setDisplayedMnemonic(KeyEvent.VK_A);
		labelProfile.setLabelFor(listProfileName);
		listProfileName.addListSelectionListener(new ListSelectionListener() {
			
			@Override
			public void valueChanged(ListSelectionEvent e) {
				DefaultListModel dlm = (DefaultListModel)listProfileName.getModel();
				String curProfileName = (String)dlm.get(listProfileName.getSelectedIndex());
				
				System.out.println("old profile:"+curProfile.profileName+curProfile.listFiles.size());
				for(int i=0;i<listProfile.size();i++){
					if(listProfile.get(i).profileName.equals(curProfileName)){
						curProfile = listProfile.get(i);
						break;
					}
				}

				updateProfileControl(curProfile,false);	
				if(m_service_model!=null){
					m_service_model.setExistList(curProfile.listFiles);
				}
				
			}
		});
		
		listProfileName.addKeyListener(new KeyListener() {
			
			// Profile Delete
			public void keyTyped(KeyEvent e) {
				if(e.getKeyChar()==KeyEvent.VK_DELETE){
					System.out.println("delete pressed");
					DefaultListModel dlm = (DefaultListModel)listProfileName.getModel();
					if(dlm.size()>1){
						int index = listProfileName.getSelectedIndex();
						String temp = (String)dlm.get(index);
						listProfileName.setSelectedIndex(0);
						dlm.remove(index);
						for(int i=0;i<listProfile.size();i++){
							if(listProfile.get(i).profileName.equals(temp)){
								listProfile.remove(i);
								flagDirty = true;
								break;
							}
						}
					} else {
						System.err.println("Last one cannot be removed!");
					}
				} else if (e.getKeyChar()==KeyEvent.VK_ENTER){
					if(normalMode){
						System.out.println("get list of profile" );
						if(curProfile.loadTime<2000){
							long start = System.currentTimeMillis();
							m_service_model.initFileList();
							long end = System.currentTimeMillis();
							if((end-start)-curProfile.loadTime>300){
								curProfile.loadTime=end-start;
								flagDirty=true;
							}
					    }else{
					    	//TODO
							Thread thr = new Thread(new Runnable() {
								public void run() {
									System.out.println("work Thread ID: "+Thread.currentThread().getId());
									m_service_model.initFileList();
									timer.stop();
									pd.setVisible(false);
								}
							 });
							thr.start();
							
					    	pd = new ProgressDialog();
					    	pd.setMax((int)curProfile.loadTime);
					    	timer = new Timer(100, new ActionListener() {
								@Override
								public void actionPerformed(ActionEvent e) {
									pd.setProgress(pd.getProgress()+100);
								}
							});
					    	timer.start();
					    	pd.setModal(true);
					    	pd.setVisible(true);
						}
						textFieldFilter.setText(null);
						curProfile.listFiles.clear();
						curProfile.listFiles.addAll(m_service_model.getEntireFileList());
					}
				}
			}
			
			@Override
			public void keyReleased(KeyEvent e) {
			}
			
			@Override
			public void keyPressed(KeyEvent e) {
			}
		});
		textNewProfileName = new JTextField();
		textNewProfileName.setMinimumSize(new Dimension(widthTextField,heightProfile));
		textNewProfileName.setMaximumSize(new Dimension(widthTextField,heightProfile));
		textNewProfileName.setAlignmentX(LEFT_ALIGNMENT);
		textNewProfileName.setVisible(false);
		textNewProfileName.addKeyListener(new KeyListener() {
			
			@Override
			public void keyTyped(KeyEvent e) {
				if(e.getKeyChar()==KeyEvent.VK_ESCAPE){
					switchMode();
				}
			}
			
			@Override
			public void keyReleased(KeyEvent e) {}
			
			@Override
			public void keyPressed(KeyEvent e) {}
		});
		
		buttonAddProfile = new JButton("Add");
		buttonAddProfile.setMinimumSize(new Dimension(widthTextField,heightProfile));
		buttonAddProfile.setMaximumSize(new Dimension(widthTextField,heightProfile));
		buttonAddProfile.setAlignmentX(LEFT_ALIGNMENT);
		buttonAddProfile.setAlignmentY(TOP_ALIGNMENT);
		buttonAddProfile.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				System.out.println("buttonAddProfile clicked.");

				switchMode();
				if(normalMode)// add-profile mode to normal mode
					addProfile();
			}
		});
		JLabel labelPath = new JLabel("Directory");
		labelPath.setAlignmentX(LEFT_ALIGNMENT);
		labelPath.setAlignmentY(TOP_ALIGNMENT);
		textSourceDir = new JTextField();
		textSourceDir.setEditable(false);
		textSourceDir.setMinimumSize(new Dimension(widthTextField,heightProfile));
		textSourceDir.setMaximumSize(new Dimension(widthTextField,heightProfile));
		textSourceDir.setAlignmentX(LEFT_ALIGNMENT);
		textSourceDir.setAlignmentY(TOP_ALIGNMENT);

		buttonSourceChooser = new JButton("...");
		buttonSourceChooser.setPreferredSize(new Dimension(widthTextField/2,heightProfile));
		buttonSourceChooser.setMinimumSize(new Dimension(widthTextField/2,heightProfile));
		buttonSourceChooser.setEnabled(false);
		buttonSourceChooser.addActionListener(new ActionListener() {			
			@Override
			public void actionPerformed(ActionEvent e) {
				JFileChooser fc = new JFileChooser();
				fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
				String defaultDir = (String)textSourceDir.getText();
				fc.setCurrentDirectory(new File(defaultDir));
				int ret = fc.showOpenDialog((Component)e.getSource());
				
				if ( ret == JFileChooser.APPROVE_OPTION ) {
					String selectedDir = fc.getSelectedFile().getPath();
					selectedDir = selectedDir.replace('\\', '/');
					textSourceDir.setText(selectedDir);
					System.out.println("new dir:"+selectedDir);
				}
			}
		});
		JLabel labelFileType = new JLabel("File Types");
		textFileTypes  = new JTextField();
		textFileTypes.setMinimumSize(new Dimension(widthTextField,heightProfile));
		textFileTypes.setMaximumSize(new Dimension(widthTextField,heightProfile));
		textFileTypes.setAlignmentX(LEFT_ALIGNMENT);
		textFileTypes.setAlignmentY(TOP_ALIGNMENT);
		textFileTypes.setEditable(false);
		textFileTypes.setToolTipText("File Types (separate with ; )");

		updateProfileControl(curProfile,true);
		
		profileMenu.add(labelProfile);
		profileMenu.add(listProfileName);
		profileMenu.add(textNewProfileName);
		profileMenu.add(buttonAddProfile);
		JSeparator sep1 = new JSeparator(SwingConstants.HORIZONTAL);
		sep1.setMaximumSize(new Dimension(widthTextField, SEP_SIZE));
		profileMenu.add(sep1);
		profileMenu.add(labelPath);
		profileMenu.add(textSourceDir);
		profileMenu.add(buttonSourceChooser);
		JSeparator sep2 = new JSeparator(SwingConstants.HORIZONTAL);
		sep2.setMaximumSize(new Dimension(widthTextField, SEP_SIZE));
		profileMenu.add(sep2);
		profileMenu.add(labelFileType);
		profileMenu.add(textFileTypes);
		profileMenu.setAlignmentX(LEFT_ALIGNMENT);
		profileMenu.setAlignmentY(TOP_ALIGNMENT);
        
        textFieldFilter = new JTextField();
        labelFilter = new JLabel();
        labelFilter.setLabelFor(textFieldFilter);
        labelFilter.setDisplayedMnemonic(KeyEvent.VK_1);
        //Whenever filterText changes, invoke newFilter.
//        textFieldFilter.setPreferredSize(new Dimension(50,80));
        textFieldFilter.addFocusListener(new MyFocusListener());
        textFieldFilter.getDocument().addDocumentListener(
                new DocumentListener() {
                    public void changedUpdate(DocumentEvent e) {
                    	newTableFilter();
                    }
                    public void insertUpdate(DocumentEvent e) {
                    	newTableFilter();
                    }
                    public void removeUpdate(DocumentEvent e) {
                    	newTableFilter();
                    }
                });
        textFieldFilter.addKeyListener(new KeyListener() {
			
			@Override
			public void keyTyped(KeyEvent e) {
				if(e.getKeyChar()==KeyEvent.VK_ENTER){
					System.out.println("VK_ENTER");
					Cursor orgCurosr = textFieldFilter.getCursor();
					textFieldFilter.setCursor(new Cursor(Cursor.WAIT_CURSOR));
					textFieldFilter.setCursor(orgCurosr);
				} 
			}
			
			@Override
			public void keyReleased(KeyEvent e) {
			}
			
			@Override
			public void keyPressed(KeyEvent e) {
				System.out.println((int)e.getKeyCode());
				if(e.getKeyCode()==KeyEvent.VK_DOWN){
					tableFiles.requestFocus();
				}
			}
		});
        textFieldFilter.setAlignmentX(LEFT_ALIGNMENT);
        
        //Create a table with a sorter.
        m_service_model = new SourceListModel();
//        m_service_model.initFileList();
        sorterFileTable = new TableRowSorter<SourceListModel>(m_service_model);
        tableFiles = new JTable(m_service_model);
        tableFiles.setRowSorter(sorterFileTable);
        tableFiles.setPreferredScrollableViewportSize(new Dimension(500, 900));
        tableFiles.setFillsViewportHeight(true);
        //For the purposes of this example, better to have a single
        //selection.
        tableFiles.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tableFiles.setCellSelectionEnabled(true);
        tableFiles.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0),"Open");
        tableFiles.getActionMap().put("Open", new AbstractAction() {
			private static final long serialVersionUID = -3668137586572365321L;

			@Override
			public void actionPerformed(ActionEvent e) {
				openSelectedItem();
			}
		});

        tableFiles.addMouseListener( new MouseListener() {
			
			@Override
			public void mouseReleased(MouseEvent e) {
			}
			
			@Override
			public void mousePressed(MouseEvent e) {
			}
			
			@Override
			public void mouseExited(MouseEvent e) {
			}
			
			@Override
			public void mouseEntered(MouseEvent e) {
			}
			
			@Override
			public void mouseClicked(MouseEvent e) {
				if (e.getClickCount()==2) {
					openSelectedItem();
				}					
			}

		});
        //Create the scroll pane and add the table to it.
        JScrollPane scrollPaneTable = new JScrollPane(tableFiles);
        scrollPaneTable.setAlignmentX(LEFT_ALIGNMENT);

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.add(labelFilter);
        mainPanel.add(textFieldFilter);
        mainPanel.add(scrollPaneTable);
        mainPanel.setAlignmentY(TOP_ALIGNMENT);

        add(profileMenu);
        add(new JSeparator(SwingConstants.VERTICAL));
        add(mainPanel);
    }

    private void openSelectedItem(){
        int viewCol = tableFiles.getSelectedColumn();
        SourceListModel model = (SourceListModel)tableFiles.getModel();
        if(viewCol==model.COL_FILE_NAME||viewCol==model.COL_PATH){
            int viewRow = tableFiles.getSelectedRow();
            int modelRow = 0;
            if (viewRow < 0) {
            } else {
                modelRow = tableFiles.convertRowIndexToModel(viewRow);
            }
            String fileName = (String)m_service_model.getValueAt(modelRow, model.COL_FILE_NAME);
            String dir = (String)m_service_model.getValueAt(modelRow, model.COL_PATH);

            String param = "";
            if ( viewCol == model.COL_FILE_NAME ) {
            	param = dir+"/"+fileName;
//            	textInfo.setText("open file:"+param);
            	WindowsShell.open(param.replace('/', '\\'));
            } else if ( viewCol == model.COL_PATH ) {
//            	textInfo.setText("explore dir:"+dir);
            	WindowsShell.explore(dir.replace('/', '\\'));
            }
        }

    }
    private void updateProfileControl(SourceProfile profileToSet, boolean updateComobo){
    	if (updateComobo) {
    		DefaultListModel  lm = new DefaultListModel();
    		listProfileName.setModel(lm);
	    	for(int i=0;i<listProfile.size();i++){
	    		lm.addElement(listProfile.get(i).profileName);
	    	}
    	}
    	textSourceDir.setText(profileToSet.sourceDir);
    	textFileTypes.setText(profileToSet.fileTypes);

    }
    private void switchMode(){
    	normalMode = !normalMode;
    	listProfileName.setVisible(normalMode);
		textNewProfileName.setVisible(!normalMode);
		buttonAddProfile.setText(normalMode?"Add":"Save");
		buttonSourceChooser.setEnabled(!normalMode);
		textFileTypes.setEditable(!normalMode);
		if(!normalMode){ // switch to add-profile mode
			textNewProfileName.setText(unsavedProfileName);
			textNewProfileName.requestFocus();
		} 
    }
    
    private void addProfile(){
    	SourceProfile sp = new SourceProfile();
		sp.profileName = textNewProfileName.getText();
		if(sp.profileName.equals(unsavedProfileName)){
			return;
		}
		sp.sourceDir = textSourceDir.getText(); 
		sp.fileTypes = textFileTypes.getText();
		listProfile.add(sp);
		flagDirty = true;
//		int lastIndex = comboProfileName.getItemCount()-1;
//		comboProfileName.removeItemAt(lastIndex);
		DefaultListModel dlm = (DefaultListModel) listProfileName.getModel();
		dlm.addElement(sp.profileName);
		listProfileName.setSelectedIndex(dlm.getSize()-1);
		System.out.println("new profile added:"+
							sp.profileName+","+
							sp.sourceDir+","+
							sp.fileTypes+","+
							sp.encodingType);
    }
    
    private void newTableFilter() {
    	System.out.println("newTableFilter called");
        RowFilter<SourceListModel, Object> rf = null;
        m_service_model.removeKeywordFilter();
        //If current expression doesn't parse, don't update.
        try {
        	String filter_text = textFieldFilter.getText();
        	rf = RowFilter.regexFilter(filter_text, 0); 
        } catch (java.util.regex.PatternSyntaxException e) {
            return;
        }
        sorterFileTable.setRowFilter(rf);
    }
    
    class SourceListModel extends AbstractTableModel {

        /**
		 * 
		 */
    	public final int COL_FILE_NAME = 0;
    	public final int COL_PATH=1;
    	public final int COL_TS=2;
    	
		private static final long serialVersionUID = 6968203745972823854L;
		private String[] columnNames = {"file",
										"path",
                                        "last modified"
                                        };
        private List<String>	listAllFiles;
        private List<String>	listFiles;
         
        public SourceListModel(){
        	listAllFiles = new ArrayList<String>();
        	listFiles = new ArrayList<String>();
        }
        public List<String> getEntireFileList(){
        	return listAllFiles;
        }

        public void removeKeywordFilter(){
        	System.out.println("removeKeywordFilter");
        	listFiles.clear();
        	listFiles.addAll(listAllFiles);
        }
        public void setExistList(List<String> newList){
        	listAllFiles.clear();
        	listFiles.clear();

        	
        	listAllFiles.addAll(newList);
        	listFiles.addAll(listAllFiles);
        	fireTableDataChanged();
        	
        	textFieldFilter.setText(null);
        	tableFiles.clearSelection();
        	tableFiles.getRowSorter().setSortKeys(null);
        }

        public void initFileList(){
        	File srcDir = new File(curProfile.sourceDir);
        	listAllFiles.clear();
        	listFiles.clear();
    		pickSourceFile(srcDir);
    		listFiles.addAll(listAllFiles);
    		if (listFiles.size()>0)
    			fireTableDataChanged();
        }
        
        private void pickSourceFile(File f) {
        	if(f.isDirectory()){
        		File[] list = f.listFiles();
        		for(int i=0;i<list.length;i++){
        			String fileName=list[i].getName();
        			if (list[i].isDirectory()) {
        				pickSourceFile(list[i]);
        			} else {
        				boolean isType = false;
        				if(curProfile.fileTypes.equals("*")||curProfile.fileTypes.isEmpty()) {
        					isType = true;
        				} else {
        					String[] settingFileTypesList = curProfile.fileTypes.split(";");    						
	        				for(int j=0;j<settingFileTypesList.length;j++) {
	        					if( fileName.endsWith("."+settingFileTypesList[j]) ){
	        						isType = true;
	        						break;
	        					}
	        				}
        				}
        				if (isType) {
	        				String fullName = list[i].getPath();
	        				fullName = fullName.replace('\\', '/');
	        				File file2Add = new File(fullName);
	        				long lastModifytime = file2Add.lastModified();
	        				Date date = new Date(lastModifytime);
	        				DateFormat format = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
//	        				System.out.println(format.format(date));
	        				listAllFiles.add(fullName+"!"+format.format(date));
        				}
        			}
        		}
        	}
        }
        public int getColumnCount() {
            return columnNames.length;
        }

        public int getRowCount() {
            return listFiles.size();
        }

        public String getColumnName(int col) {
            return columnNames[col];
        }

        public Object getValueAt(int row, int col) {
        	if(listFiles.size()==0){
        		System.err.println("0 size!");
        		return null;
        	}
        	String fileInfo  = listFiles.get(row);
        	String[] infos = fileInfo.split("!");

        	String fileName = infos[0].substring(infos[0].lastIndexOf('/')+1);
        	String path = infos[0].substring(0,infos[0].lastIndexOf('/'));
        	
        	switch (col) {
        	case COL_FILE_NAME:
        		return fileName;
        	case COL_TS:
            	return infos[1];
        	case COL_PATH:
            	return path;
        	default:
        		System.out.println("null from getValueAt");
        		return "";
        	}
        }
        public boolean isCellEditable(int row, int col) {
            //Note that the data/cell address is constant,
            //no matter where the cell appears onscreen.
            return false;
        }

         public void setValueAt(Object value, int row, int col) {

//            data[row][col] = value; 
//            fireTableCellUpdated(row, col);

        }
    }
 
	private void loadSetting() {
		try {
			BufferedReader reader = new BufferedReader(new FileReader("sourceViewerSetting"));
			String line=null;			
			SourceProfile temp = null;
			while((line=reader.readLine())!=null){
				if(line.contains("[sourceProfile]")){
					temp = new SourceProfile();
					listProfile.add(temp);
					continue;
				}
				if(temp==null){
					System.err.println("format error!");
					break;
				}
				String[] settingLine = line.split("=");
				if(settingLine.length>1&&settingLine[0].equals("profileName")) {
					temp.profileName=settingLine[1];
				} else if(settingLine.length>1&&settingLine[0].equals("sourceDir")) {
					temp.sourceDir=settingLine[1];
				} else if (settingLine.length>1&&settingLine[0].equals("fileTypes")) {
					temp.fileTypes=settingLine[1];					
				} else if (settingLine.length>1&&settingLine[0].equals("encodingType")) {
					temp.encodingType=settingLine[1];
				} else if (settingLine.length>1&&settingLine[0].equals("loadTime")) {
					temp.loadTime=Integer.parseInt(settingLine[1]);
				}
			}
			reader.close();
		} catch (FileNotFoundException e) {
			System.err.println("setting file not found!");
		} catch (IOException e) {
			e.printStackTrace();
		} 
		if ( listProfile.size() == 0  ){
			SourceProfile defaultSourceProfile = new SourceProfile();
			defaultSourceProfile.profileName = "default";
			defaultSourceProfile.sourceDir = "C:/gao/svn/src/server/business";
			defaultSourceProfile.fileTypes = "cpp";
			defaultSourceProfile.loadTime=0;
			listProfile.add(defaultSourceProfile);
			curProfile = defaultSourceProfile;
			
		} else {
			curProfile = listProfile.get(0);			
		}
	}  
	private void saveSetting() {
		try {
			if(!flagDirty)
				return;
			BufferedWriter writer = new BufferedWriter((new FileWriter("sourceViewerSetting")));
			for(int i=0;i<listProfile.size();i++){
				writer.write("[sourceProfile]\n");
				writer.write("profileName="+listProfile.get(i).profileName+"\n");
				writer.write("sourceDir="+listProfile.get(i).sourceDir+"\n");
				writer.write("fileTypes="+listProfile.get(i).fileTypes+"\n");
				writer.write("encodingType="+listProfile.get(i).encodingType+"\n");
				writer.write("loadTime="+listProfile.get(i).loadTime+"\n\n");
			}
			writer.close();

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} 
		
	}

    protected boolean setupListenersWhenConnected() {
        JFrame parentFrame = (JFrame) SwingUtilities.getWindowAncestor(this);
        if (parentFrame == null) {
            return false;
        }
        parentFrame.addWindowListener(new WindowListener() {
			
			@Override
			public void windowOpened(WindowEvent e) {
			}
			
			@Override
			public void windowIconified(WindowEvent e) {
			}
			
			@Override
			public void windowDeiconified(WindowEvent e) {
			}
			
			@Override
			public void windowDeactivated(WindowEvent e) {
			}
			
			@Override
			public void windowClosing(WindowEvent e) {
				System.out.println("setting saved");
				
				saveSetting();
			}
			
			@Override
			public void windowClosed(WindowEvent e) {
			}
			
			@Override
			public void windowActivated(WindowEvent e) {
			}
		});
        return true;
    }
    private static void createAndShowGUI() {
        //Create and set up the window.
        JFrame frame = new JFrame("SourceViwer");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        //Add content to the window.
        frame.add(new SourceViewer(), BorderLayout.CENTER);
        
        //Display the window.
        frame.pack();
        frame.setExtendedState(JFrame.MAXIMIZED_BOTH);

        frame.setVisible(true);
    }
    
    public static void main(String[] args) {
        //Schedule a job for the event dispatch thread:
        //creating and showing this application's GUI.
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                //Turn off metal's use of bold fonts
				UIManager.put("swing.boldMetal", Boolean.FALSE);
				createAndShowGUI();
            }
        });
    }
}
