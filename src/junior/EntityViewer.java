package junior;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.HierarchyEvent;
import java.awt.event.HierarchyListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.AbstractAction;
import javax.swing.BoxLayout;
import javax.swing.DefaultListSelectionModel;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.RowFilter;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableRowSorter;

import common.EntityInfo;
import common.EntityReader;
import common.EntityTxtReader;
import common.EntityTxtWriter;
import common.EntityWriter;
import common.EntityXmlReader;
import common.EntityXmlWriter;
import common.FieldInfo;
import common.HelpDialog;
import common.MyFocusListener;
import common.Utility;
import common.WindowsShell;

@SuppressWarnings("serial")
public class EntityViewer extends JPanel {
    /**
	 *
	 */
	private final String SETTING1="favoriteTables";

	private final int HEIGHT_TEXT=30;
	private final int WIDTH_ENTITY_NAME=0;
	private final int WIDTH_ENTITY_DESC=0;
	private final int WIDTH_ENTITY_PIN=0;
	private final int WIDTH_FIELD_NO=0;
	private final int WIDTH_FIELD_NAME=0;
	private final int WIDTH_FIELD_DESC=0;
	private final int WIDTH_FIELD_TYPE=0;
	private final int WIDTH_FIELD_LEN=0;
	private final int WIDTH_FIELD_PK=0;

	private String entityDataFile = "entity.txt";//default
	private String entityPrefix = "";
    private boolean m_filtered_by_field = false;
    private String  fieldName2Filter = "";
    private boolean flagDataAvailable = false;
//    private Map<String,List<String>> m_applt_map;

    private HelpDialog helpText;
    private JPanel panelLeft;
    private JTable tableEntity;
    private JScrollPane scrollPaneTable;
    private MyEntityModel modelEntity;
    private JLabel	labelEntityFilter;
    private JTextField textEntityFilter;

    private JTable tableField;
    private MyFieldModel modelField;
    private JLabel	labelFieldFilter;
    private JTextField textFieldFilter;
    private JTextArea textUserComment;
    private TableRowSorter<MyEntityModel> sorterEntity;
    private TableRowSorter<MyFieldModel> sorterField;

    FieldInfo curField = null;

    private Map<String,String> mapComment = new HashMap<String,String>();

    public EntityViewer(String dataFile){
    	super();
    	entityDataFile = dataFile;
    	init();
    }

    public EntityViewer(String dataFile, String prefix){
    	super();
    	entityDataFile = dataFile;
    	entityPrefix = prefix;
    	init();
    }

    public EntityViewer() {
    	super();
    	init();
    }

    private void init() {
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

        modelEntity = new MyEntityModel();

        panelLeft = new JPanel();

        panelLeft.setLayout(new BoxLayout(panelLeft,BoxLayout.Y_AXIS));


        labelEntityFilter = new JLabel();
        labelEntityFilter.setDisplayedMnemonic(KeyEvent.VK_1);
        textEntityFilter = new JTextField();
        labelEntityFilter.setLabelFor(textEntityFilter);
//        textEntityFilter.setMinimumSize(new Dimension(100,HEIGHT_TEXT));
        textEntityFilter.setMaximumSize(new Dimension(1000,HEIGHT_TEXT));
        textEntityFilter.setPreferredSize(new Dimension(1000,HEIGHT_TEXT));
        textEntityFilter.setAlignmentX(LEFT_ALIGNMENT);
        textEntityFilter.addFocusListener(new MyFocusListener());
        //Whenever filterText changes, invoke newFilter.
        textEntityFilter.getDocument().addDocumentListener(
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
        textEntityFilter.getInputMap().put(KeyStroke.getKeyStroke(
                KeyEvent.VK_ENTER, 0),
                "search field");
        textEntityFilter.getActionMap().put("search field", new AbstractAction()
										{
											public void actionPerformed(ActionEvent e) {
												String text = textEntityFilter.getText();
												if ( text.isEmpty() ) {
													modelEntity.resetEntryList();
												} else if ( modelEntity != null ) {
													modelEntity.filterEntity(text, false);
												}

											}
										}
									);

        textEntityFilter.addKeyListener(new MyKeyListener());

        textEntityFilter.setToolTipText("[Default]by table name; [Enter]by field name.");
//        textEntityFilter.setVisible(false);

        sorterEntity = new TableRowSorter<MyEntityModel>(modelEntity);
        tableEntity = new JTable(modelEntity);
        tableEntity.setRowSorter(sorterEntity);
        tableEntity.setMinimumSize(new Dimension(200,200));
        tableEntity.setPreferredScrollableViewportSize(new Dimension(500, 200));
//        tableEntity.setMaximumSize(new Dimension(800,500));
        tableEntity.getColumnModel().getColumn(2).setMaxWidth(50);
        tableEntity.setAlignmentX(LEFT_ALIGNMENT);
        tableEntity.setFillsViewportHeight(true);
        tableEntity.setCellSelectionEnabled(true);
        //For the purposes of this example, better to have a single
        //selection.
//        tableEntity.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        //When selection changes, provide user with row numbers for
        //both view and model.

        tableEntity.getSelectionModel().addListSelectionListener(new tableSelectionListener());
        tableEntity.addKeyListener(new MyKeyListener());
        tableEntity.addMouseListener(new MouseListener() {

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
				if (e.getClickCount()==2 ) {
			        int viewCol = tableEntity.getSelectedColumn();
			        MyEntityModel model = (MyEntityModel)tableEntity.getModel();
			        if(viewCol==model.COL_NAME){
			            int viewRow = tableEntity.getSelectedRow();
			            int modelRow = 0;
			            if (viewRow < 0) {
			            } else {
			                modelRow = tableEntity.convertRowIndexToModel(viewRow);
			            }
			            String entityName = (String)modelEntity.m_curEntities.get(modelRow).defineFile;

			            WindowsShell.open(entityName);
			        }
				}
			}
		});

		//Create the scroll pane and add the table to it.
        tableEntity.getInputMap().put(KeyStroke.getKeyStroke(
                KeyEvent.VK_TAB, 0),
                "goto field filter");
        tableEntity.getActionMap().put("goto field filter", new AbstractAction()
										{
											public void actionPerformed(ActionEvent e) {
												textFieldFilter.requestFocus();
											}
										}
									);

        scrollPaneTable = new JScrollPane(tableEntity);
        scrollPaneTable.setAlignmentX(LEFT_ALIGNMENT);
//        scrollPaneTable.setVisible(false);

        panelLeft.add(labelEntityFilter);
        panelLeft.add(textEntityFilter);
        panelLeft.add(scrollPaneTable);
        panelLeft.setAlignmentY(TOP_ALIGNMENT);


        modelField = new MyFieldModel();
        sorterField = new TableRowSorter<MyFieldModel>(modelField);
        tableField = new JTable(modelField);
        tableField.setRowSorter(sorterField);
//        tableField.setPreferredSize(new Dimension(500,1000));
        tableField.setPreferredScrollableViewportSize(new Dimension(500, 400));
        tableField.setMinimumSize(new Dimension(500, 400));
        tableField.setMaximumSize(new Dimension(800, 500));
        tableField.getColumnModel().getColumn(MyFieldModel.COL_NO).setMaxWidth(40);
        tableField.getColumnModel().getColumn(MyFieldModel.COL_TYPE).setMaxWidth(100);
        tableField.getColumnModel().getColumn(MyFieldModel.COL_LEN).setMaxWidth(40);
        tableField.getColumnModel().getColumn(MyFieldModel.COL_PREC).setMaxWidth(30);
        tableField.getColumnModel().getColumn(MyFieldModel.COL_PK).setMaxWidth(80);
        tableField.setFillsViewportHeight(true);
        tableField.addMouseListener(new MouseListener() {

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
				if (e.getClickCount()==2 ) {
					int viewCol = tableField.getSelectedColumn();
					if ( viewCol==MyFieldModel.COL_DESC )
						return;

					System.out.println("mouseClicked 2");
					if ( modelField.tableFields!= null) {
				        int viewRow = tableField.getSelectedRow();
				        if (viewRow != -1) {
				            int modelRow = tableField.convertRowIndexToModel(viewRow);
				            curField = modelField.tableFields.get(modelRow);
				        }
				        if (curField!=null) {
				        	fieldName2Filter = curField.fieldName;
				        	modelEntity.filterEntity(fieldName2Filter,true);
				        }
					}
				}
			}
		});
        tableField.setBackground(new Color(0x00DDFFEE));
        tableField.setCellSelectionEnabled(true);

        tableField.getSelectionModel().addListSelectionListener( new tableSelectionListener() );

        JScrollPane scrollPaneField = new JScrollPane(tableField);
        scrollPaneField.setMinimumSize(new Dimension(500, 500));
//        scrollPaneField.setMaximumSize(new Dimension(800, 500));

        JPanel panelRight = new JPanel();
//        JSplitPane panelRight = new JSplitPane();
        panelRight.setLayout(new BoxLayout(panelRight,BoxLayout.Y_AXIS));

        labelFieldFilter = new JLabel();
        labelFieldFilter.setDisplayedMnemonic('2');
        textFieldFilter = new JTextField();
        labelFieldFilter.setLabelFor(textFieldFilter);
        textFieldFilter.setPreferredSize(new Dimension(1000,HEIGHT_TEXT));
//        textFieldFilter.setMinimumSize(new Dimension(100,HEIGHT_TEXT));
        textFieldFilter.setMaximumSize(new Dimension(1000,HEIGHT_TEXT));
        textFieldFilter.addFocusListener(new MyFocusListener());
        //Whenever filterText changes, invoke newFilter.
        textFieldFilter.getDocument().addDocumentListener(
                new DocumentListener() {
                    public void changedUpdate(DocumentEvent e) {
                        newFieldFilter();
                    }
                    public void insertUpdate(DocumentEvent e) {
                    	newFieldFilter();
                    }
                    public void removeUpdate(DocumentEvent e) {
                    	newFieldFilter();
                    }
                });
        textFieldFilter.addKeyListener(new MyKeyListener());

        panelRight.add(labelFieldFilter);
        panelRight.add(textFieldFilter);
        panelRight.add(scrollPaneField);

        textUserComment = new JTextArea();
        textUserComment.setBackground(new Color(0x00EEFFDD));
        JScrollPane scrollPaneApplt = new JScrollPane(textUserComment);

        scrollPaneApplt.setPreferredSize(new Dimension(500, 500));
//        scrollPaneApplt.setMaximumSize(new Dimension(500, 500));
//        scrollPaneApplt.setMinimumSize(new Dimension(500, 500));
//        textApplt.setMaximumSize(new Dimension(500,500));
//        textApplt.setMinimumSize(new Dimension(500,500));
//        textApplt.setPreferredSize(new Dimension(500,500));
//        scrollPaneApplt.setMinimumSize(new Dimension(100,100));
//        scrollPaneApplt.setMaximumSize(new Dimension(100,100));
        panelRight.add(scrollPaneApplt);
        panelRight.setAlignmentY(TOP_ALIGNMENT);

        add(panelLeft);
//        add(new JSeparator(SwingConstants.VERTICAL));
        add(panelRight);

        flagDataAvailable=modelEntity.readInData();
        if(flagDataAvailable){
        	sorterEntity.toggleSortOrder(MyEntityModel.COL_PIN);
        	sorterEntity.toggleSortOrder(MyEntityModel.COL_PIN);
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
				System.out.println("windowClosing");
				if(modelEntity.isDirty()||modelField.isDirty())
					modelEntity.save();
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
    /**
     * Update the row filter regular expression from the expression in
     * the text box.
     */
    private void newTableFilter() {
        RowFilter<MyEntityModel, Object> rf = null;
        //If current expression doesn't parse, don't update.
        try {
        	if(modelEntity.getEntityList()==null||modelEntity.getEntityList().isEmpty())
        		return;
        	modelEntity.resetEntryList();
        	String filter_text = textEntityFilter.getText().toUpperCase();
        	if(filter_text.length()>0){
        		int index=MyEntityModel.COL_DESC;
        		char first_char = filter_text.charAt(0);
        		if('A'<=first_char && first_char<='Z'||first_char=='_'){
        			index=MyEntityModel.COL_NAME;
        		}
        		rf = RowFilter.regexFilter(filter_text, index);
        		m_filtered_by_field = false;
        	}
        } catch (java.util.regex.PatternSyntaxException e) {
            return;
        }
        sorterEntity.setRowFilter(rf);

    }

    private void newFieldFilter() {
        RowFilter<MyFieldModel, Object> rf = null;
        //If current expression doesn't parse, don't update.
        try {
        	String filter_text = textFieldFilter.getText().toUpperCase();
        	if(filter_text.length()>0){
        		rf = RowFilter.regexFilter(filter_text, MyFieldModel.COL_NAME, MyFieldModel.COL_DESC);
        	}
        } catch (java.util.regex.PatternSyntaxException e) {
            return;
        }
        sorterField.setRowFilter(rf);
    }

    class MyFieldModel extends AbstractTableModel {

    	private boolean dirty = false;

        public boolean isDirty() {
			return dirty;
		}

		private String[] columnNames = {"NO.",
        		"field name",
                "desc(editable)",
                "type",
                "length",
                "prec",
                "pk"
                };
        private List<FieldInfo>	tableFields;

        public final static int COL_NO   = 0;
        public final static int COL_NAME = 1;
        public final static int COL_DESC = 2;
        public final static int COL_TYPE = 3;
        public final static int COL_LEN  = 4;
        public final static int COL_PREC  = 5;
        public final static int COL_PK   = 6;

    	public void setFields(List<FieldInfo> fields) {
    		tableFields = fields;
    	}
        public MyFieldModel(){
        	tableFields=null;
        }

        public int getRowCount() {
        	if ( tableFields == null )
        		return 0;
            return tableFields.size();
        }

        public String getColumnName(int col) {
            return columnNames[col];
        }

        public Object getValueAt(int row, int col) {
        	if ( tableFields == null )
        		return null;
        	switch (col) {
        	case COL_NO:
        		return tableFields.get(row).seqNo;
        	case COL_NAME:
        		return tableFields.get(row).fieldName;
        	case COL_DESC:
        		return tableFields.get(row).fieldDesc;
        	case COL_TYPE:
        		return tableFields.get(row).dataType;
        	case COL_LEN:
        		return tableFields.get(row).length;
        	case COL_PREC:
        		return tableFields.get(row).precision;
         	case COL_PK:
        		return tableFields.get(row).pkInfo;
        	default:
        		return null;
        	}
        }

        /*
         * JTable uses this method to determine the default renderer/
         * editor for each cell.  If we didn't implement this method,
         * then the last column would contain text ("true"/"false"),
         * rather than a check box.
         */
        @SuppressWarnings({ "unchecked", "rawtypes" })
		public Class getColumnClass(int c) {
            return getValueAt(0, c).getClass();
        }

        /*
         * Don't need to implement this method unless your table's
         * editable.
         */
        public boolean isCellEditable(int row, int col) {
            //Note that the data/cell address is constant,
            //no matter where the cell appears onscreen.
        	return col==COL_DESC;
        }

        /*
         * Don't need to implement this method unless your table's
         * data can change.
         */
        public void setValueAt(Object value, int row, int col) {

        	if(col==COL_DESC){
        		tableFields.get(row).fieldDesc = (String)value;
        		dirty = true;
        	}

        }

		@Override
		public int getColumnCount() {
            return columnNames.length;
		}

    }

    class tableSelectionListener implements ListSelectionListener {

        public void valueChanged(ListSelectionEvent event) {

        	DefaultListSelectionModel model = (DefaultListSelectionModel)event.getSource();
        	ListSelectionListener[] listeners = model.getListSelectionListeners();
        	JTable objTable = null;
        	for(int i=0; i<listeners.length;i++){
        		if ( listeners[i]==tableEntity || listeners[i]==tableField){
        			objTable = (JTable)listeners[i];
        			break;
        		}
        	}
        	if (objTable==null)
        		return;
        	else if(objTable==tableEntity){
	            int viewRow = objTable.getSelectedRow();
	            if (viewRow != -1) {
	                int modelRow = objTable.convertRowIndexToModel(viewRow);
	                String phyName = (String)objTable.getModel().getValueAt(modelRow, MyEntityModel.COL_NAME);
	                setNewTableInField(phyName);//TODO
	            } else {
	            	modelField.setFields(null);
	            	tableField.updateUI();
	            	textUserComment.updateUI();
	            }
        	} else if (objTable==tableField){
	            int viewRow = tableField.getSelectedRow();
	            if (viewRow < 0) {
	            } else {
	                int modelRow = tableField.convertRowIndexToModel(viewRow);

	                curField = modelField.tableFields.get(modelRow);
	                textUserComment.setText(curField.userComment.replace('\t', '\n'));
//	                List<String> appltList = m_applt_map.get(curField.m_physical);
//	                if(appltList!=null){
//	                	String textToSet = "";
//	                	for (int i=0;i<appltList.size();i++) {
//	                	    textToSet = textToSet + appltList.get(i) + "\n";
//	                	}
//	                	textApplt.setText(textToSet);
//	                } else {
//	                	textApplt.setText(null);
//	                }
	                textUserComment.updateUI();
	            }
        	}
        }
    }

    class MyKeyListener implements KeyListener {

		@Override
		public void keyTyped(KeyEvent e) {}

		@Override
		public void keyPressed(KeyEvent e) {
			if (e.getKeyCode()==KeyEvent.VK_F1){
				String title = "Help";
				String helpContent = "Created by Jeffry (2014/05)\n"+
									 "\n"+
									 "Tips\n"+
									 "1.create entity.xml: input !c<Entity definition xls path> in left text field.\n"+
								     "2.filter entity: input in left text field.\n"+
								     "3.filter field: input in right text field.\n"+
								     "4.search field name 1: input in left text field and press enter.\n"+
								     "5.search field name 2: double click field name.\n"+
								     "6.copy all fields of an entity: press ctrl+t.\n";
				helpText = new HelpDialog(title,helpContent,new MyKeyListener());
				helpText.setModal(true);
				helpText.setVisible(true);
				return;
			}
			if(e.getSource()==textEntityFilter){
				if(e.getKeyCode()==KeyEvent.VK_DOWN){
					tableEntity.requestFocus();
				}
			} else if(e.getSource()==textFieldFilter){
				if(e.getKeyCode()==KeyEvent.VK_DOWN){
					tableField.requestFocus();
				}
			} else if(e.getSource()==tableEntity){
				if(e.getKeyCode()==KeyEvent.VK_T&&
						(e.getModifiersEx()&KeyEvent.CTRL_DOWN_MASK)==KeyEvent.CTRL_DOWN_MASK){
					JTable objTable = (JTable)e.getSource();
					int viewRow = objTable.getSelectedRow();
					if (viewRow != -1) {
		                int modelRow = objTable.convertRowIndexToModel(viewRow);
		                String phyName = (String)objTable.getModel().getValueAt(modelRow, MyEntityModel.COL_NAME);//TODO

		                EntityInfo selectedTable = modelEntity.getEntityInfo(phyName);
						Clipboard c = Toolkit.getDefaultToolkit().getSystemClipboard();
						String line1="";
						String line2="";
						List<FieldInfo> fields = selectedTable.getFields();
						for(int i=0;i<fields.size();i++){
							line1 = line1 + fields.get(i).fieldDesc + "\t";
							line2 = line2 + fields.get(i).fieldName + "\t";
						}
						String text = line1.substring(0, line1.length()-1) + "\n"
										+ line2.substring(0, line2.length()-1);
						StringSelection selection = new StringSelection(text);
						c.setContents(selection, selection);
					}
				}
			} else if(e.getSource()==helpText){
				if(e.getKeyCode()==KeyEvent.VK_ESCAPE){
//					JOptionPane.showConfirmDialog((Component)e.getSource(), "esc");
					helpText.setVisible(false);
				}
			}
		}

		@Override
		public void keyReleased(KeyEvent e) {
		}

    }

    class MyEntityModel extends AbstractTableModel {
        private String[] columnNames = {"entity name",
                                        "desc(editable)",
        								"pin"
                                        };
        private boolean dirty = false;

        public boolean isDirty() {
			return dirty;
		}

		private List<EntityInfo>	m_allEntities;
        private List<EntityInfo> m_curEntities;
        public final static int COL_NAME = 0;
        public final static int COL_DESC = 1;
        public final static int COL_PIN = 2;

        public MyEntityModel(){
        	m_allEntities = null;
        	m_curEntities = new ArrayList<EntityInfo>();
        }
        public void save() {

			if (m_allEntities == null)
				return;

			List<String> tableNames = new ArrayList<String>();
			for (int i = 0; i < m_allEntities.size(); i++) {
				if (m_allEntities.get(i).favorite)
					tableNames.add(m_allEntities.get(i).entityName);
			}
			Utility.saveStringList(entityPrefix+SETTING1, tableNames);
			// update entity.xml
			// EntityWriter writer = new EntityTxtWriter();
			// writer.write(m_allEntities, null, "entity_new.txt");

			EntityWriter writer = null;
			if (entityDataFile.endsWith("txt")) {
				writer = new EntityTxtWriter();
			} else {
				writer = new EntityXmlWriter();
			}
			writer.write(m_allEntities, entityDataFile);

		}
		public void resetEntryList() {
			m_curEntities.clear();
			m_curEntities.addAll(m_allEntities);
			modelEntity.fireTableDataChanged();
		}
		public EntityInfo getEntityInfo(String phyName){
			EntityInfo ret = null;
			for(int i=0;i<m_curEntities.size();i++){
				if(phyName.equals(m_curEntities.get(i).entityName)){
					ret=m_curEntities.get(i);
					break;
				}
			}
			return ret;
		}
		public List<EntityInfo> getEntityList(){
        	return m_curEntities;
        }

		public boolean readInData(){
        	EntityReader entity_reader = null;

        	String entityFileName = entityDataFile;
        	if(entityFileName.endsWith("txt")){
        		entity_reader = new EntityTxtReader();
        	} else if (entityFileName.endsWith("xml")) {
        		entity_reader = new EntityXmlReader();
        	} else {
        		System.err.println(entityFileName+" cannot be read!");
        		return false;

        	}

        	File entityFile = new File(entityFileName);
        	if (entityFile.exists()) {
        		entity_reader.read(entityFileName);
        		m_allEntities = entity_reader.getEntityList();
        		String favoriteTables = entityPrefix + SETTING1;
            	File file = new File(favoriteTables);
            	if ( file.exists() ) {
            		List<String> tableNames = Utility.loadStringList(favoriteTables);

					if(tableNames!=null&&tableNames.size()>0){
						for(int i=0;i<m_allEntities.size();i++){
							for(int j=0;j<tableNames.size();j++){
								if(m_allEntities.get(i).entityName.equals(tableNames.get(j))){
									m_allEntities.get(i).favorite = true;
									break;
								}
							}

						}
					}
            	}
//        		m_applt_map = entity_reader.getAppltMap();
        		m_curEntities.addAll(m_allEntities);
        	} else {
        		textEntityFilter.setText("!c<fill entity definition xls path here>");
        		System.err.println(entityFileName+" not found!");
        		return false;
        	}

        	return true;
        }
        public int getColumnCount() {
            return columnNames.length;
        }

        public int getRowCount() {
            return m_curEntities.size();
        }

        public String getColumnName(int col) {
            return columnNames[col];
        }

        public Object getValueAt(int row, int col) {
        	if ( m_curEntities.size()==0 ) {
        		return null;
        	}
        	switch (col) {
        	case COL_NAME:
        		return m_curEntities.get(row).entityName;
        	case COL_DESC:
        		return m_curEntities.get(row).entityNameJP;
        	case COL_PIN:
        		return m_curEntities.get(row).favorite;
        	default:
        		return null;
        	}
        }

        /*
         * JTable uses this method to determine the default renderer/
         * editor for each cell.  If we didn't implement this method,
         * then the last column would contain text ("true"/"false"),
         * rather than a check box.
         */
        @SuppressWarnings({ "unchecked", "rawtypes" })
		public Class getColumnClass(int c) {
            return getValueAt(0, c).getClass();
        }

        /*
         * Don't need to implement this method unless your table's
         * editable.
         */
        public boolean isCellEditable(int row, int col) {
            //Note that the data/cell address is constant,
            //no matter where the cell appears onscreen.
        	return col==COL_PIN||col==COL_DESC;
        }

        /*
         * Don't need to implement this method unless your table's
         * data can change.
         */
        public void setValueAt(Object value, int row, int col) {
        	switch (col){
        	case COL_DESC:
        		m_curEntities.get(row).entityNameJP=(String)value;
        		dirty = true;
        		break;
        	case COL_PIN:
        	default:
            	if((Boolean)value!=m_curEntities.get(row).favorite){
            		m_curEntities.get(row).favorite=(Boolean)value;
            		dirty = true;
    	        	sorterEntity.toggleSortOrder(COL_PIN);
    	        	sorterEntity.toggleSortOrder(COL_PIN);
            	}
        		break;
        	}
        }

		public void filterEntity(String keyword, boolean quick_mode) {

//			System.out.println("The list will filtered by field name: "+keyword + " in table "+m_cur_table.m_logical);
//			String tableId = m_cur_table.m_id;
			m_curEntities.clear();
			m_filtered_by_field = true;
			fieldName2Filter = keyword;
			for(int i=0;i<m_allEntities.size();i++) {
				for(int j=0;j<m_allEntities.get(i).getFields().size();j++) {
					if (quick_mode) {
						if (keyword.equals(m_allEntities.get(i).getFields().get(j).fieldName)){
							m_curEntities.add(m_allEntities.get(i));
							break;
						}
					} else {
						if (m_allEntities.get(i).getFields().get(j).fieldName.contains(keyword) ||
								m_allEntities.get(i).getFields().get(j).fieldName.contains(keyword) ){
							m_curEntities.add(m_allEntities.get(i));
							break;
						}
					}
				}
			}
//			int newIndex = 0;
//			for(newIndex=0;newIndex<m_curEntities.size();newIndex++) {
//				if(m_curEntities.get(newIndex).m_id.equals(tableId)){
//					m_cur_table = m_curEntities.get(newIndex);
//					break;
//				}
//			}

			modelEntity.fireTableDataChanged();
	        RowFilter<MyEntityModel, Object> rf = null;
	        //If current expression doesn't parse, don't update.
	        try {
	        	rf = RowFilter.regexFilter("", 0);
	        } catch (java.util.regex.PatternSyntaxException e) {
	            return;
	        }
	        sorterEntity.setRowFilter(rf);

//			System.out.println("Count:"+tableEntity.getRowCount());
//			for(int i=0;i<tableEntity.getRowCount();i++){
//				String work = (String)tableEntity.getValueAt(i, 0);
//				if(work.equals(tableId)){
//					tableEntity.changeSelection(i, 1, true, false);
//					System.out.println(tableId+" is found!");
//					tableEntity.updateUI();
//					break;
//				}
//				//TODO
//			}
		}
    }

    /**
     * Create the GUI and show it.  For thread safety,
     * this method should be invoked from the
     * event-dispatching thread.
     */
    private static void createAndShowGUI() {
        //Create and set up the window.
        JFrame frame = new JFrame("Entity Viewer");

        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        //Create and set up the content pane.
        EntityViewer newContentPane = new EntityViewer();
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

	private void setNewTableInField(String phyName) {
        EntityInfo selectedTable = modelEntity.getEntityInfo(phyName);
        if ( selectedTable != null ) {
            modelField.setFields(selectedTable.getFields());
            modelField.fireTableDataChanged();
            textFieldFilter.setText(null);
        	tableField.clearSelection();
        	tableField.getRowSorter().setSortKeys(null);
        	if(m_filtered_by_field){
        		int count = tableField.getRowCount();
        		String logicalName, physicalName;
        		for(int i=0;i<count;i++){
        			logicalName = (String)tableField.getModel().getValueAt(i, 1);
        			physicalName = (String)tableField.getModel().getValueAt(i, 2);
        			if(logicalName.contains(fieldName2Filter) || physicalName.contains(fieldName2Filter)){
        				tableField.changeSelection(i, 1, true, false);
        			}
        		}
        	}
        }

	}


}
