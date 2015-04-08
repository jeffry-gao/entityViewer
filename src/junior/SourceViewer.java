package junior;

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
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.AbstractAction;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
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
import common.Utility;
import common.WindowsShell;


public class SourceViewer extends JPanel{
	/**
	 *
	 */

	private static final long serialVersionUID = 582601465647537897L;
	private static final int SEP_SIZE = 10;
	private final String unsavedProfileName = "[unsaved]";
	private final int    heightProfile = 30;
	private final int    sideMenuWidth = 300;

	private boolean   normalMode = true; // true: normal; false:profile edit
	private JPanel    sideMenu;
	private JLabel    labelProfile;
	private JList<String>      listProfileName;
	private JTextField textNewProfileName;
	private JTextField textSourceDir;
	private JButton    buttonSourceChooser;
	private JTextField textFileTypes;
	private JButton    buttonAddProfile;
	private JCheckBox   checkClearCache;
	private Timer		timer;

	private JLabel				labelFilter;
	private JTextField 			textFileFilter;
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
    	boolean recursive=true;
    	boolean getLastModified=true;
    	String prefix = null;
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

        sideMenu = new JPanel();
        sideMenu.setLayout(new BoxLayout(sideMenu, BoxLayout.Y_AXIS));
        sideMenu.setMaximumSize(new Dimension(sideMenuWidth,1028));

		loadSetting();
		labelProfile = new JLabel("All Profile");

		listProfileName = new JList<String>();
		listProfileName.setMinimumSize(new Dimension(sideMenuWidth,heightProfile));
		listProfileName.setMaximumSize(new Dimension(sideMenuWidth,heightProfile*5));
		listProfileName.setAlignmentX(LEFT_ALIGNMENT);
		listProfileName.setAlignmentY(TOP_ALIGNMENT);
		listProfileName.setVisibleRowCount(10);
		labelProfile.setDisplayedMnemonic(KeyEvent.VK_A);
		labelProfile.setLabelFor(listProfileName);
		listProfileName.addListSelectionListener(new ListSelectionListener() {

			@Override
			public void valueChanged(ListSelectionEvent e) {
				DefaultListModel<String> dlm = (DefaultListModel<String>)listProfileName.getModel();
				int index = listProfileName.getSelectedIndex();
				if(index==-1){
					listProfileName.setSelectedIndex(0);
					index = 0;
				}
				String curProfileName = (String)dlm.get(index);

				System.out.println("old profile:"+curProfile.profileName+" with size="+curProfile.listFiles.size());
				for(int i=0;i<listProfile.size();i++){
					if(listProfile.get(i).profileName.equals(curProfileName)){
						curProfile = listProfile.get(i);
						break;
					}
				}

				System.out.println("new profile:"+curProfile.profileName+" with size="+curProfile.listFiles.size());
				updateProfileControl(curProfile,false);
				if(m_service_model!=null){
					m_service_model.setExistList(curProfile.listFiles);
					System.out.println("new profile:"+curProfile.profileName+" is set.");
				}

			}
		});

		listProfileName.addKeyListener(new KeyListener() {

			// Profile Delete
			public void keyTyped(KeyEvent e) {
				if(e.getKeyChar()==KeyEvent.VK_DELETE){
					System.out.println("delete pressed");
					DefaultListModel<String> dlm = (DefaultListModel<String>)listProfileName.getModel();
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
								}
							 });
							thr.start();

					    	timer = new Timer(100, new ActionListener() {
								@Override
								public void actionPerformed(ActionEvent e) {
								}
							});
					    	timer.start();
						}
						textFileFilter.setText(null);
						curProfile.listFiles.clear();
						curProfile.listFiles.addAll(m_service_model.getEntireFileList());
						System.out.println("listFiles is saved in "+curProfile.profileName+" count="+curProfile.listFiles.size() );
					} else {
						System.out.println("not in normal mode!" );
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
		textNewProfileName.setMinimumSize(new Dimension(sideMenuWidth,heightProfile));
		textNewProfileName.setMaximumSize(new Dimension(sideMenuWidth,heightProfile));
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
		buttonAddProfile.setMinimumSize(new Dimension(sideMenuWidth,heightProfile));
		buttonAddProfile.setMaximumSize(new Dimension(sideMenuWidth,heightProfile));
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
		textSourceDir.setMinimumSize(new Dimension(sideMenuWidth,heightProfile));
		textSourceDir.setMaximumSize(new Dimension(sideMenuWidth,heightProfile));
		textSourceDir.setAlignmentX(LEFT_ALIGNMENT);
		textSourceDir.setAlignmentY(TOP_ALIGNMENT);

		buttonSourceChooser = new JButton("...");
		buttonSourceChooser.setPreferredSize(new Dimension(sideMenuWidth/2,heightProfile));
		buttonSourceChooser.setMinimumSize(new Dimension(sideMenuWidth/2,heightProfile));
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
		textFileTypes.setMinimumSize(new Dimension(sideMenuWidth,heightProfile));
		textFileTypes.setMaximumSize(new Dimension(sideMenuWidth,heightProfile));
		textFileTypes.setAlignmentX(LEFT_ALIGNMENT);
		textFileTypes.setAlignmentY(TOP_ALIGNMENT);
		textFileTypes.setEditable(false);
		textFileTypes.setToolTipText("File Types (separate with ; )");

		checkClearCache = new JCheckBox();
		checkClearCache.setText("Clear Cache");

		updateProfileControl(curProfile,true);

		sideMenu.add(labelProfile);
		sideMenu.add(listProfileName);
		sideMenu.add(textNewProfileName);
//		profileMenu.add(buttonAddProfile);
		JSeparator sep1 = new JSeparator(SwingConstants.HORIZONTAL);
		sep1.setMaximumSize(new Dimension(sideMenuWidth, SEP_SIZE));
		sideMenu.add(sep1);
		sideMenu.add(labelPath);
		sideMenu.add(textSourceDir);
//		profileMenu.add(buttonSourceChooser);
		JSeparator sep2 = new JSeparator(SwingConstants.HORIZONTAL);
		sep2.setMaximumSize(new Dimension(sideMenuWidth, SEP_SIZE));
		sideMenu.add(sep2);
		sideMenu.add(labelFileType);
		sideMenu.add(textFileTypes);
		sideMenu.add(checkClearCache);
		sideMenu.setAlignmentX(LEFT_ALIGNMENT);
		sideMenu.setAlignmentY(TOP_ALIGNMENT);

        textFileFilter = new JTextField();
        textFileFilter.setToolTipText("F5 to get file list; 'show' to show the side panel.");
        labelFilter = new JLabel();
        labelFilter.setLabelFor(textFileFilter);
        labelFilter.setDisplayedMnemonic(KeyEvent.VK_1);
        //Whenever filterText changes, invoke newFilter.
        textFileFilter.setPreferredSize(new Dimension(2000,30));
        textFileFilter.setMaximumSize(new Dimension(2000,30));
        textFileFilter.addFocusListener(new MyFocusListener());
        textFileFilter.getDocument().addDocumentListener(
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
        textFileFilter.addKeyListener(new KeyListener() {

			@Override
			public void keyTyped(KeyEvent e) {
				if(e.getKeyChar()==KeyEvent.VK_ENTER){
					System.out.println("VK_ENTER");
					Cursor orgCurosr = textFileFilter.getCursor();
					textFileFilter.setCursor(new Cursor(Cursor.WAIT_CURSOR));
					if(textFileFilter.getText().equals("show"))
						sideMenu.setVisible(true);
					else if (textFileFilter.getText().equals("hide"))
						sideMenu.setVisible(false);
					textFileFilter.setText("");
					textFileFilter.setCursor(orgCurosr);
				}
			}

			@Override
			public void keyReleased(KeyEvent e) {
			}

			@Override
			public void keyPressed(KeyEvent e) {
//				System.out.println((int)e.getKeyCode());
//				if(e.getKeyCode()==KeyEvent.VK_F5){
//					Cursor orgCurosr = textFileFilter.getCursor();
//					textFileFilter.setCursor(new Cursor(Cursor.WAIT_CURSOR));
//					m_service_model.initFileList();
//					textFileFilter.setCursor(orgCurosr);
//				}
				if(e.getModifiersEx()==e.CTRL_DOWN_MASK){
					if(e.getKeyCode()==KeyEvent.VK_S){
						System.out.println("Show panel");
						sideMenu.setVisible(true);
						listProfileName.requestFocus();
					}
				}
				if(e.getKeyCode()==KeyEvent.VK_DOWN){
					tableFiles.requestFocus();
				}
			}
		});
        textFileFilter.setAlignmentX(LEFT_ALIGNMENT);

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
        tableFiles.getColumnModel().getColumn(m_service_model.COL_FILE_NAME).setMinWidth(150);
//        tableFiles.getColumnModel().getColumn(m_service_model.COL_FILE_NAME).setMaxWidth(150);
        tableFiles.getColumnModel().getColumn(m_service_model.COL_TS).setMinWidth(150);
        tableFiles.getColumnModel().getColumn(m_service_model.COL_TS).setMaxWidth(150);
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
        mainPanel.add(textFileFilter);
        mainPanel.add(scrollPaneTable);
        mainPanel.setAlignmentY(TOP_ALIGNMENT);

        sideMenu.setVisible(false);
        add(sideMenu);
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
    		DefaultListModel<String>  lm = new DefaultListModel<String>();
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
		DefaultListModel<String> mdl = (DefaultListModel<String>)listProfileName.getModel();
//		DefaultListModel dlm = (DefaultListModel) listProfileName.getModel();

		mdl.addElement(sp.profileName);
		listProfileName.setSelectedIndex(mdl.getSize()-1);
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
        	String filter_text = textFileFilter.getText();
        	rf = RowFilter.regexFilter(filter_text, 0,3);
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
    	public final int COL_DESC=1;
    	public final int COL_TS=2;
    	public final int COL_PATH=3;

		private static final long serialVersionUID = 6968203745972823854L;
		private String[] columnNames = {"file",
                						"desc(editable)",
                                        "last modified",
                                        "path"
                                        };
        private List<String>	listAllFiles;
        private List<String>	listFiles;
        private Map<String, String> fileDescs;

        public SourceListModel(){
        	listAllFiles = new ArrayList<String>();
        	listFiles = new ArrayList<String>();
        	fileDescs = new HashMap<String, String>();
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

        	textFileFilter.setText(null);
        	tableFiles.clearSelection();
        	tableFiles.getRowSorter().setSortKeys(null);
        }

        private boolean loadCache(List<String> stringList, String name){
    		try {
    			File f = new File(name);
    			if(!f.exists()){
    				return false;
    			}

    			BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(name), "utf-8"));
    			String line=null;
    			while((line=reader.readLine())!=null){
    				stringList.add(line);
    			}
    			reader.close();
    		} catch (FileNotFoundException e) {
    			System.err.println("cache file not found!");
    		} catch (IOException e) {
    			e.printStackTrace();
    		}
    		return true;
        }

        private void saveCache(List<String> stringList, String name){
    		try {
    			BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(name), "utf-8"));
    			for(int i=0;i<stringList.size();i++){
    				writer.write(stringList.get(i)+"\n");
    			}
    			writer.close();

    		} catch (FileNotFoundException e) {
    			e.printStackTrace();
    		} catch (IOException e) {
    			e.printStackTrace();
    		}

        }

        public void initFileList(){
        	File srcDir = new File(curProfile.sourceDir);
        	listAllFiles.clear();
        	listFiles.clear();
        	Cursor orgCursor = sideMenu.getCursor();
        	sideMenu.setCursor(new Cursor(Cursor.WAIT_CURSOR));
        	if(checkClearCache.isSelected()){
        		pickSourceFile(srcDir);
        		Utility.saveStringList(curProfile.profileName,listAllFiles);
        	} else {
        		listAllFiles = Utility.loadStringList( curProfile.profileName);
        		if(listAllFiles==null){
            		pickSourceFile(srcDir);
            		Utility.saveStringList(curProfile.profileName,listAllFiles);
        		}
        	}
        	sideMenu.setCursor(orgCursor);
    		listFiles.addAll(listAllFiles);
    		if (listFiles.size()>0)
    			fireTableDataChanged();
    		loadFileDescs();
    		sideMenu.setVisible(false);
        }

        private void loadFileDescs(){
        	fileDescs = Utility.loadAllProperties("fileDescs");
        }

        public void saveFileDescs(){
			Utility.saveProperty("fileDescs", fileDescs);
        }

        private void pickSourceFile(File f) {
        	if(f.isDirectory()){
        		File[] list = f.listFiles();
        		for(int i=0;i<list.length;i++){
        			String fileName=list[i].getName();
        			if (list[i].isDirectory()) {
        				if(curProfile.recursive)
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
        				if(curProfile.prefix!=null){
        					if(!fileName.startsWith(curProfile.prefix))
        						continue;
        				}
        				if (isType) {
	        				String fullName = list[i].getPath();
	        				fullName = fullName.replace('\\', '/');
	        				String modifiedTime = "-";
	        				if(curProfile.getLastModified){
		        				File file2Add = new File(fullName);
		        				long lastModifytime = file2Add.lastModified();
		        				Date date = new Date(lastModifytime);
		        				DateFormat format = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		        				modifiedTime = format.format(date);
	        				}
	        				String insertString = fullName+"!"+modifiedTime;
//	        				System.out.println(insertString);
	        				listAllFiles.add(insertString);
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
        	String desc = fileDescs.get(infos[0]);

        	switch (col) {
        	case COL_FILE_NAME:
        		return fileName;
        	case COL_TS:
            	return infos[1];
        	case COL_PATH:
            	return path;
        	case COL_DESC:
        		return desc==null?"":desc;
        	default:
        		System.out.println("null from getValueAt");
        		return "";
        	}
        }
        public boolean isCellEditable(int row, int col) {
            return col==COL_DESC;
        }

         public void setValueAt(Object value, int row, int col) {
        	 if(col==COL_DESC){
        		 String[] infos = listFiles.get(row).split("!");
        		 String fullPathName = infos[0];
        		 fileDescs.put(fullPathName, (String)value);
        	 }
        }
    }

	private void loadSetting() {
		try {
			BufferedReader reader = new BufferedReader(new FileReader("sourceViewerSetting"));
			String line=null;
			SourceProfile temp = null;
			while((line=reader.readLine())!=null){
				if(line.startsWith(";")){
					continue;
				}
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
				} else if (settingLine.length>1&&settingLine[0].equals("recursive")){
					temp.recursive=Boolean.parseBoolean(settingLine[1]);
				} else if (settingLine.length>1&&settingLine[0].equals("getLastModified")){
					temp.getLastModified=Boolean.parseBoolean(settingLine[1]);
				} else if (settingLine.length>1&&settingLine[0].equals("prefix")){
					temp.prefix=settingLine[1];
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
			m_service_model.saveFileDescs();

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
