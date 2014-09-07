package common;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.stream.*;


public class EntityXMLReader {

	static List<EntityInfo> m_tables;
	static EntityInfo m_curTable;
	static EntityFieldInfo m_curField;
	static List<String> m_curFiledApplt;
	static int m_curNo;
	static Map<String, List<String>> m_fieldToAddApplt;
  
	public EntityXMLReader() {
		m_tables = new ArrayList<EntityInfo>();
		m_curTable = null;
		m_curField = null;
		m_curNo = 1;
		m_fieldToAddApplt = new HashMap<String, List<String>>();
		m_curFiledApplt = null;
	}
	
	public List<EntityInfo> getEntityList() {
		return m_tables;
	}

	public Map<String, List<String>> getAppltMap() {
		return m_fieldToAddApplt;
	}
	
	public void readInData(String entityXML){
		XMLInputFactory xmlif = XMLInputFactory.newInstance();
		try {
			String encodingType = getEncodingType(entityXML);
			XMLStreamReader xmlrEntity = xmlif.createXMLStreamReader(new InputStreamReader(
																				new FileInputStream(entityXML), 
																				encodingType)
																	);
			System.out.println(entityXML+" is read as:"+xmlrEntity.getEncoding());
			while(xmlrEntity.hasNext()){
			      readEntity(xmlrEntity);
			      xmlrEntity.next();
			}
			System.out.println("Entity Read over!");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (XMLStreamException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}  
	}
  
	private String getEncodingType(String xmlFileName){
		String encodingType="utf-8";
		try {
			FileInputStream fos = new FileInputStream(xmlFileName);
			byte b[] = new byte[64];
			fos.read(b);
			String firstLine = new String(b);
			if(firstLine.contains("encoding")){
				if(!firstLine.contains("utf-8") && !firstLine.contains("UTF-8")){
					encodingType="SJIS";
				}
			}
			fos.close();
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println("Open xml with "+encodingType);
		return encodingType;
	}
  
	private void readEntity(XMLStreamReader xmlr) {
		switch (xmlr.getEventType()) {

		case XMLStreamConstants.START_ELEMENT:
			readAtElementStart(xmlr);
			break;

		case XMLStreamConstants.END_ELEMENT:
			// System.out.print("End Element\n");
			// printName(xmlr);
			readAtElementEnd(xmlr);
			// System.out.print("\n");
			break;
		case XMLStreamConstants.START_DOCUMENT:
			// System.out.print("Start Document\n");
			break;

		case XMLStreamConstants.END_DOCUMENT:
			// System.out.print("End Document\n");
			break;

		}
	}
  
	private static void readAtElementStart(XMLStreamReader xmlr){
		  if(xmlr.hasName()){ 
			  String localName = xmlr.getLocalName();
			  if(localName.equals("entity")){
				  m_curTable = new EntityInfo();
				  for (int i=0; i < xmlr.getAttributeCount(); i++) { 
					  String attributeName = xmlr.getAttributeLocalName(i);
					  if(attributeName.equals("physical")){ 
						  m_curTable.m_physical = xmlr.getAttributeValue(i); 
					  } else if (attributeName.equals("id")){
						  m_curTable.m_id = xmlr.getAttributeValue(i);
					  } else if (attributeName.equals("logical")){
						  m_curTable.m_logical = xmlr.getAttributeValue(i);
					  }
				  }		  
			  } else if (localName=="field") {
				  m_curField = new EntityFieldInfo();
				  m_curField.m_no = m_curNo;
				  m_curNo++;
				  for (int i=0; i < xmlr.getAttributeCount(); i++) { 
					  String attributeName = xmlr.getAttributeLocalName(i);
					  if(attributeName.equals("physical")){ 
						  m_curField.m_physical = xmlr.getAttributeValue(i); 
					  } else if (attributeName.equals("datatype")){
						  m_curField.m_dataType = xmlr.getAttributeValue(i);
					  } else if (attributeName.equals("digits")){
						  m_curField.m_digits = xmlr.getAttributeValue(i);
					  } else if (attributeName.equals("logical")){
						  m_curField.m_logical = xmlr.getAttributeValue(i);
					  } else if (attributeName.equals("indexInfo")){
						  m_curField.m_indexInfo = xmlr.getAttributeValue(i);
					  } else if (attributeName.equals("remark")){
						  m_curField.remark = xmlr.getAttributeValue(i);
					  }
				  }		  
			  } else if (localName=="applts") {
				  System.out.println("start to read applts...");
			  } else if (localName=="applt") {
				  if(xmlr.getAttributeCount()>0&&xmlr.getAttributeLocalName(0).equals("field_name")){
					  m_curFiledApplt = new ArrayList<String>();
					  m_fieldToAddApplt.put(xmlr.getAttributeValue(0), m_curFiledApplt);
				  }
			  } else if (localName=="applt_option") {
				  String value="", name="";
				  for(int i=0;i<xmlr.getAttributeCount();i++){
					  if(xmlr.getAttributeLocalName(i).equals("applt_value")){
						  value = xmlr.getAttributeValue(i);
					  } else if(xmlr.getAttributeLocalName(i).equals("applt_name")){
						  name = xmlr.getAttributeValue(i);						  
					  }
				  }
				  if(m_curFiledApplt!=null)
					  m_curFiledApplt.add(value+"\t"+name);
			  }
		  }
	}
  
	private static void readAtElementEnd(XMLStreamReader xmlr){
	  if(xmlr.hasName()){ 
		  String localName = xmlr.getLocalName();
		  if(localName=="entity"){
			  m_tables.add(m_curTable);
			  m_curTable = null;
			  m_curNo = 1;
		  } else if (localName=="field") {
			  if (m_curField.m_indexInfo==null) {
				  m_curField.m_indexInfo = new String();
			  }
			  m_curTable.addField(m_curField);
			  m_curField = null;
		  }
	  }
	}

	// not used.
	void readApplt(XMLStreamReader xmlr) {
		switch (xmlr.getEventType()) {

		case XMLStreamConstants.START_ELEMENT:
			if(xmlr.hasName()){
				if(xmlr.getLocalName().equals("applt") && xmlr.getAttributeCount()!=0){
					findFiledNode(xmlr);
				} else if (xmlr.getLocalName().equals("field")){
					addApplt(xmlr);
				}
			}
			break;
		case XMLStreamConstants.END_ELEMENT:
			if(xmlr.hasName()){
				String localName = xmlr.getLocalName();
				if(localName!=null && 
					localName.equals("applt") && m_curFiledApplt!=null){
					m_curFiledApplt = null;
				} 
			}
			
			break;
		}
	}

	// not used.
	private static void findFiledNode(XMLStreamReader xmlr){
		String physicalName = "";
		for (int i=0; i < xmlr.getAttributeCount(); i++) { 
			String attributeName = xmlr.getAttributeLocalName(i);
			if (attributeName=="phy_fld_nm"){
				physicalName = xmlr.getAttributeValue(i);
			} 
		}	
		if (physicalName.length()>0) {
			m_curFiledApplt = new ArrayList<String>();
			m_fieldToAddApplt.put(physicalName, m_curFiledApplt);
		}
	}
	  
	// not used.
	private static void addApplt(XMLStreamReader xmlr){
		  String attributeName = "";
		  String key = "";
		  String value = "";
		  for (int i=0; i < xmlr.getAttributeCount(); i++) { 
			  attributeName = xmlr.getAttributeLocalName(i);
			  if(attributeName=="applt_dtl_cd"){ 
			  key = xmlr.getAttributeValue(i);
		  } else if (attributeName=="applt_dtl_nm"){
				  value = xmlr.getAttributeValue(i);
			  } 
		  }	
		  if (key.length()>0)
			  m_curFiledApplt.add(key+"\t"+value);
	}
}
