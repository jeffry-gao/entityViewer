package myTools;

import java.awt.Dimension;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableRowSorter;

import common.CORBADefinitionXMLCreator;
import common.CORBADefinitionXMLReader;
import common.HelpDialog;
import common.IDLServiceInfo;
import common.IDLStructInfo;
import common.MyFocusListener;
import common.Utility;

@SuppressWarnings("serial")
public class InterfaceViewer extends JPanel {
	private static final String LABEL_TITLE_SERVICE = "Service Name:";
	private static final String LABEL_TITLE_STRUCT = "Structure Name:";
	final String appName="InterfaceViewer";
	final String keyIDLPath="idlDefPath";
	final String keyStructPath="structDefPath";
	
	private HelpDialog			helpText;
    private JTable 				tableInterfaces;
    private JScrollPane 		scrollPaneTable;
    private CORBAServicesModel   modelInterface;
    private JTextField 			textInterfaceFilter;
    private TableRowSorter<CORBAServicesModel> sorterInterface;
    
    private JLabel 				labelServiceName;
    private JTable 				tableInterfaceProps;
    private CORBAServicePropModel  modelInterfaceProps;
    
    private JLabel 				labelStructName;
    private JTextField			textMemberFilter;
    private JTable 				tableStructureMember;
    private CORBAstructureModel modelStructureMember;
    private TableRowSorter<CORBAstructureModel> sorterStructMembers;
     
    public InterfaceViewer() {
    	super();
        setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
        JPanel panelLeft = new JPanel();

        panelLeft.setLayout(new BoxLayout(panelLeft,BoxLayout.Y_AXIS));

        textInterfaceFilter = new JTextField();
        textInterfaceFilter.setMinimumSize(new Dimension(100,30));
        textInterfaceFilter.setMaximumSize(new Dimension(1200,30));
        textInterfaceFilter.setAlignmentX(LEFT_ALIGNMENT);
        textInterfaceFilter.addFocusListener(new MyFocusListener());
        //Whenever filterText changes, invoke newFilter.
        textInterfaceFilter.getDocument().addDocumentListener(
                new DocumentListener() {
                    public void changedUpdate(DocumentEvent e) {
                    	newServiceFilter();
                    }
                    public void insertUpdate(DocumentEvent e) {
                    	newServiceFilter();
                    }
                    public void removeUpdate(DocumentEvent e) {
                    	newServiceFilter();
                    }
                });
        textInterfaceFilter.addKeyListener(new MyKeyListener());
        
        modelInterface = new CORBAServicesModel();
        sorterInterface = new TableRowSorter<CORBAServicesModel>(modelInterface);
        tableInterfaces = new JTable(modelInterface);
        tableInterfaces.setRowSorter(sorterInterface);
        tableInterfaces.setMinimumSize(new Dimension(200,200));
        tableInterfaces.setPreferredScrollableViewportSize(new Dimension(800, 200));
        tableInterfaces.setAlignmentX(LEFT_ALIGNMENT);
        tableInterfaces.setFillsViewportHeight(true);
        tableInterfaces.getSelectionModel().addListSelectionListener(new tableSelectionListener());
 		
        scrollPaneTable = new JScrollPane(tableInterfaces);
        scrollPaneTable.setAlignmentX(LEFT_ALIGNMENT);

        panelLeft.add(textInterfaceFilter);
        panelLeft.add(scrollPaneTable);
        panelLeft.setAlignmentY(TOP_ALIGNMENT);  
        
        JPanel panelRight = new JPanel();
        panelRight.setLayout(new BoxLayout(panelRight,BoxLayout.Y_AXIS));
        labelServiceName = new JLabel(LABEL_TITLE_SERVICE);
        labelServiceName.setAlignmentX(LEFT_ALIGNMENT);
        modelInterfaceProps = new CORBAServicePropModel();
        tableInterfaceProps = new JTable(modelInterfaceProps); 
        tableInterfaceProps.setAlignmentX(LEFT_ALIGNMENT);
        tableInterfaceProps.getSelectionModel().addListSelectionListener(new tableSelectionListener());

        JSeparator sep = new JSeparator();
        sep.setAlignmentX(LEFT_ALIGNMENT);
        sep.setMinimumSize(new Dimension(100, 60));
        labelStructName = new JLabel(LABEL_TITLE_STRUCT);
        labelStructName.setAlignmentX(LEFT_ALIGNMENT);
        textMemberFilter = new JTextField();
        textMemberFilter.setAlignmentX(LEFT_ALIGNMENT);
        textMemberFilter.setMinimumSize(new Dimension(100,30));
        textMemberFilter.setMaximumSize(new Dimension(800,30));
        textMemberFilter.getDocument().addDocumentListener(
                new DocumentListener() {
                    public void changedUpdate(DocumentEvent e) {
                    	memberFilter();
                    }
                    public void insertUpdate(DocumentEvent e) {
                    	memberFilter();
                    }
                    public void removeUpdate(DocumentEvent e) {
                    	memberFilter();
                    }
                });
        modelStructureMember = new CORBAstructureModel();
        sorterStructMembers = new TableRowSorter<CORBAstructureModel>(modelStructureMember);
        tableStructureMember = new JTable(modelStructureMember);
        tableStructureMember.setRowSorter(sorterStructMembers);
        tableStructureMember.addMouseListener(new MyMouseListener());
        JScrollPane 		scrollPaneTableStructure = new JScrollPane(tableStructureMember);
        scrollPaneTableStructure.setAlignmentX(LEFT_ALIGNMENT);
        panelRight.add(labelServiceName);
        panelRight.add(tableInterfaceProps);
        panelRight.add(sep);
        panelRight.add(labelStructName);
        panelRight.add(textMemberFilter);
        panelRight.add(scrollPaneTableStructure);
        panelRight.setAlignmentY(TOP_ALIGNMENT); 
        
        add(panelLeft);
        add(panelRight);
        modelInterface.readInData();
    }
    private void newServiceFilter() {
        RowFilter<CORBAServicesModel, Object> rf = null;
        //If current expression doesn't parse, don't update.
        try {
         	String filter_text = textInterfaceFilter.getText();
        	if(filter_text.length()>0){
        		rf = RowFilter.regexFilter(filter_text, CORBAServicesModel.IDX_SERVICE); 
        	}
        } catch (java.util.regex.PatternSyntaxException e) {
            return;
        }
        sorterInterface.setRowFilter(rf);
        
    }
    private void memberFilter() {
        RowFilter<CORBAstructureModel, Object> rf = null;
        //If current expression doesn't parse, don't update.
        try {
         	String filter_text = textMemberFilter.getText();
        	if(filter_text.length()>0){
        		rf = RowFilter.regexFilter(filter_text, CORBAstructureModel.IDX_PHY_NAME); 
        	}
        } catch (java.util.regex.PatternSyntaxException e) {
            return;
        }
        sorterStructMembers.setRowFilter(rf);
    }
    private static void createAndShowGUI() {
        //Create and set up the window.
        JFrame frame = new JFrame("CORBA Interface Viewer");

        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        //Create and set up the content pane.
        InterfaceViewer newContentPane = new InterfaceViewer();
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
    class MyMouseListener implements MouseListener {

		@Override
		public void mouseClicked(MouseEvent e) {
			if(e.getClickCount()==2){
				System.out.println("double clicked.");
				int viewRow = tableStructureMember.getSelectedRow();
				if(viewRow>=0){
					int modelRow = tableStructureMember.convertRowIndexToModel(viewRow);
					String typeName = (String)tableStructureMember.getModel()
										.getValueAt(modelRow, CORBAstructureModel.IDX_TYPE);
					if(typeName.endsWith("List")){
						typeName = Utility.rmList(typeName);
					}
					IDLStructInfo sif = modelInterface.getStructInfo(typeName);
					if(sif!=null){
						System.out.println("Structure information ["+typeName+"] will be display!");
						modelStructureMember.setStructInfo(sif);
					}
				}
			}
		}

		@Override
		public void mousePressed(MouseEvent e) {
		}

		@Override
		public void mouseReleased(MouseEvent e) {
		}

		@Override
		public void mouseEntered(MouseEvent e) {
		}

		@Override
		public void mouseExited(MouseEvent e) {
		}
    	
    }
    
    class tableSelectionListener implements ListSelectionListener {
        public void valueChanged(ListSelectionEvent event) {
        	DefaultListSelectionModel model = (DefaultListSelectionModel)event.getSource();
        	ListSelectionListener[] listeners = model.getListSelectionListeners();
        	JTable objTable = null;
        	for(int i=0; i<listeners.length;i++){
        		if ( listeners[i]==tableInterfaces || listeners[i]==tableInterfaceProps 
        				|| listeners[i]==tableStructureMember){
        			objTable = (JTable)listeners[i];
        			break;
        		}
        	}
        	if (objTable==null)
        		return;
        	else if(objTable==tableInterfaces){
	            int viewRow = objTable.getSelectedRow();
	            if (viewRow != -1) {
	                int modelRow = objTable.convertRowIndexToModel(viewRow);
	                IDLServiceInfo info = modelInterface.getAt(modelRow);
	                labelServiceName.setText(LABEL_TITLE_SERVICE+info.serviceName);
	                modelInterfaceProps.setInfo(info);
	            } else {
	            	labelServiceName.setText(LABEL_TITLE_SERVICE);
	            	modelInterfaceProps.setInfo(null);
	            }
        	} else if (objTable==tableInterfaceProps){
	            int viewRow = objTable.getSelectedRow();
	            if (viewRow != -1) { // 0:service name
	            	String structName = (String)objTable.getModel().getValueAt(viewRow, 1);
	            	if(structName.contains(" ")){
	            		structName = structName.substring(0,structName.indexOf(" "));
	            	}
            		if(structName.endsWith("List"))
            			structName = structName.substring(0,structName.length()-"List".length());
	            	System.out.println("Structure information ["+structName+"] will be display!");
	            	IDLStructInfo sif = modelInterface.getStructInfo(structName);
	            	if(sif!=null){
	            		labelStructName.setText(LABEL_TITLE_STRUCT+structName);
	            	}else{
	            		labelStructName.setText(LABEL_TITLE_STRUCT);
	            	}
            		modelStructureMember.setStructInfo(sif);
	            }
        	}
        }
    }
    
    class CORBAstructureModel extends AbstractTableModel {
        private String[] columnNames = {"Type Name",
									"physical name",
									"logical name"};
        static public final int IDX_TYPE=0;
        static public final int IDX_PHY_NAME=1;
        private final int IDX_LOG_NAME=2;
        
        private IDLStructInfo info;
        public void setStructInfo(IDLStructInfo newInfo){
        	info=newInfo;
        	fireTableDataChanged();
        	textMemberFilter.setText("");
        	String newStructName="";
        	if(info!=null)
        		newStructName=info.structName;
       		labelStructName.setText(LABEL_TITLE_STRUCT+newStructName);
        }
		@Override
		public int getRowCount() {
			if(info==null)
				return 0;
			else
				return info.listMember.size();
		}

		@Override
		public int getColumnCount() {
			return columnNames.length;
		}

		@Override
		public Object getValueAt(int rowIndex, int columnIndex) {
			String ret = "error";
			if(info!=null){
				if(info.listMember.size()>rowIndex){
					String[] tokens = info.listMember.get(rowIndex).split("\t");
					// [type]+'\t'+[physical name]+'\t'+[logical name]+'\t'+[remark]
					switch(columnIndex){
					case IDX_TYPE:
						ret = tokens[0];
						break;
					case IDX_PHY_NAME:
						ret = tokens[1];
						break;
					case IDX_LOG_NAME:
						ret = tokens[2];
						break;
					}
				}
			}
			return ret;
		}
        public String getColumnName(int col) {
            return columnNames[col];
        }
    }
    
    class CORBAServicePropModel extends AbstractTableModel {
        private String[] columnNames = {"item",
										"type",
						                "Name"
						                };

    	private final int MAX_LEN = 12;
    	private final int IDX_ITEM = 0;
    	private final int IDX_TYPE = 1;
    	private final int IDX_NAME = 2;
    	
    	private IDLServiceInfo info;
    	public CORBAServicePropModel() {
		}
    	public void setInfo(IDLServiceInfo newInfo){
    		info = newInfo;
    		fireTableDataChanged();
    		modelStructureMember.setStructInfo(null);
    	}
    	
		@Override
		public int getRowCount() {
			int paramCount=0;
			if(info!=null)
				paramCount=info.paramInfos.size();
			return Math.max(paramCount+1,MAX_LEN);
		}

		@Override
		public int getColumnCount() {
			return columnNames.length;
		}
		
        public String getColumnName(int col) {
            return columnNames[col];
        }

		@Override
		public Object getValueAt(int rowIndex, int columnIndex) {
			String ret="";
			if(info!=null&&rowIndex<info.paramInfos.size()+1){
				switch(columnIndex){
				case IDX_ITEM:
					if(rowIndex==0)
						ret = "return";
					else
						ret = "param "+rowIndex;
					break;
				case IDX_TYPE:
					if(rowIndex==0)
						ret = info.returnType;
					else
						ret = info.paramInfos.get(rowIndex-1).typeName;
					break;
				case IDX_NAME:
					if(rowIndex==0)
						;
					else
						ret = info.paramInfos.get(rowIndex-1).paraPhysicalName;
					break;
				}
			}
			return ret;
		}
    	
    }
    class CORBAServicesModel extends AbstractTableModel {
        private String[] columnNames = {"Service Name",
        								"Japanese Name",
        								"Home",
                                        "Module"
                                        };
        final static int IDX_SERVICE=0;
        final static int IDX_JAPANESE=1;
        final static int IDX_HOME=2;
        final static int IDX_MODULE=3;
        private List<IDLServiceInfo> idlServices;
        private Map<String, IDLStructInfo> structInfos;
        public CORBAServicesModel(){
        	idlServices = new ArrayList<IDLServiceInfo>();
        }
 
        public IDLServiceInfo getAt(int i){
        	if(idlServices!=null&&idlServices.size()>i){
        		return idlServices.get(i);
        	} else 
        		return null;
        }
        public IDLStructInfo getStructInfo(String key){
        	if(structInfos!=null){
        		return structInfos.get(key);
        	} else
        		return null;
        }
        
		public void readInData(){
			if(idlServices!=null)
				idlServices.clear();
			if(structInfos!=null)
				structInfos.clear();
			CORBADefinitionXMLReader reader = new CORBADefinitionXMLReader();
			reader.readInData("citf.xml");
			idlServices = reader.getServiceList();
			structInfos = reader.getIDLStructs();
			File serviceNameDict = new File("serviceJapanName.txt");
			if(serviceNameDict.exists()){
				//TODO
				Map<String,String> nameDicts = new HashMap<String, String>();
				try {
					BufferedReader rd = new BufferedReader(new FileReader(serviceNameDict));
					String line=null;
					while((line=rd.readLine())!=null){
						String[] namePair = line.split("=");
						if(namePair.length>1){
							nameDicts.put(namePair[0], namePair[1]);
						}
					}
					rd.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
				for(int i=0;i<idlServices.size();i++){
					idlServices.get(i).logicalName = nameDicts.get(idlServices.get(i).serviceName);
				}

			}
        }
        public int getColumnCount() {
            return columnNames.length;
        }

        public String getColumnName(int col) {
            return columnNames[col];
        }

        public Object getValueAt(int row, int col) {
        	String ret = "error";
        	switch(col){
        	case IDX_MODULE:
        		ret = idlServices.get(row).moduleName;
        		break;
        	case IDX_HOME:
        		ret = idlServices.get(row).interfaceName;
        		break;
        	case IDX_SERVICE:
        		ret = idlServices.get(row).serviceName;
        		break;
        	case IDX_JAPANESE:
        		ret = idlServices.get(row).logicalName;
        		break;
        	}
        	return ret;
        }

		@Override
		public int getRowCount() {
			return idlServices.size();
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
									 "1.create citf.xml: input !c<[idl path];[structure path]> in left text field.\n"+
								     "2.filter interface: input in left text field.\n";
				helpText = new HelpDialog(title,helpContent,new MyKeyListener());
				helpText.setModal(true);
				helpText.setVisible(true);
				return;
			}
			if(e.getSource()==textInterfaceFilter){
				if(e.getKeyCode()==KeyEvent.VK_ENTER){
					String text = textInterfaceFilter.getText();
					boolean created = false;
					if(text.equals("!c")){
						List<String> listProps = new ArrayList<String>();
						listProps.add(keyIDLPath);
						listProps.add(keyStructPath);
						Map<String, String> mapProps = Utility.loadProperties(appName, listProps);
						String idlPath = mapProps.get(keyIDLPath);
						String structPath = mapProps.get(keyStructPath);
						if(idlPath!=null&&structPath!=null){
							CORBADefinitionXMLCreator.createCITFXML(idlPath, structPath, "citf.xml");	
							created = true;
						}
					} else if (text.startsWith("!c")){
						if(text.contains("<")&&text.contains(";")&&text.contains(">")){
							String paths = text.substring(text.indexOf("<")+1,text.indexOf(">"));
							String idlPath = paths.split(";")[0];
							String structPath = paths.split(";")[1];
							idlPath = idlPath.replace('\\', '/');
							structPath = structPath.replace('\\', '/');
							File f1 = new File(idlPath);
							File f2 = new File(structPath);
							if(f1.isDirectory()&&f2.isDirectory()){
								CORBADefinitionXMLCreator.createCITFXML(idlPath, structPath, "citf.xml");
								Map<String, String> mapProps = new HashMap<String, String>();
								mapProps.put(keyIDLPath, idlPath);
								mapProps.put(keyStructPath, structPath);
								Utility.saveProperty(appName, mapProps);
								created = true;
							}
						}
					}
					if(created)
						modelInterface.readInData();
					textInterfaceFilter.setText("");
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
    
}
