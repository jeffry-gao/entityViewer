package common;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamWriter;



public class EntityXMLCreator {
	public static final String NEW_ENTITY_FILE = "entity.xml";
	public static final String DEFINE_XLS= "C:/gao/doc/テーブル設計書";
	static final int createProgress=5;
	
	public static void main(String[] args) {
		createEntityXML(DEFINE_XLS,NEW_ENTITY_FILE,null);
	}
	
	public static void createEntityXML(String xlsDir, String ouputFile, Progress progress) {
		
		
		FileInputStream is = null;
		FileOutputStream out = null;
		
		String entityFilePath = null;

		try {
			
			File f = new File(xlsDir);
//			if (!f.isDirectory()) {
//				System.exit(-1);
//			}
			String[] sfs = f.list();
			List<EntityInfo> listTables = new ArrayList<EntityInfo>();
			Map<String,List<String>> mapApplt = new HashMap<String, List<String>>();
			int prog=0;
			for (int i = 0; i < sfs.length; i++) {
				System.out.println(i + " : " + sfs[i]);
				File subFold = new File(DEFINE_XLS+"/"+sfs[i]);
				if(subFold.isDirectory()){
					File xlss[] = subFold.listFiles();
					for( int j = 0; j<xlss.length; j++){
						if(xlss[j].getName().endsWith(".xls")){
							EntityInfo curTable = EntityXlsReader.read(xlss[j].getAbsolutePath(), mapApplt);
							listTables.add(curTable);
							if(progress!=null)
								progress.setProgress(++prog);
						}
					}
				}
			}
			
			if(progress!=null)
				progress.setCurrentWork("writing entity.xml...");

			XMLOutputFactory xof =  XMLOutputFactory.newInstance();
			XMLStreamWriter xtwEntity = null;
			
			xtwEntity = xof.createXMLStreamWriter(new FileOutputStream(NEW_ENTITY_FILE),"utf-8");
			xtwEntity.writeStartDocument("utf-8","1.0");
			xtwEntity.writeCharacters("\n");
			xtwEntity.writeStartElement("entity_info");
			xtwEntity.writeCharacters("\n");
			xtwEntity.writeCharacters("\t");
			xtwEntity.writeStartElement("entitys");
			xtwEntity.writeCharacters("\n");
			for(int i=0;i<listTables.size();i++){
				xtwEntity.writeCharacters("\t\t");
				xtwEntity.writeStartElement("entity");
				xtwEntity.writeAttribute("id", listTables.get(i).m_id);
				xtwEntity.writeAttribute("logical", listTables.get(i).m_logical);
				xtwEntity.writeAttribute("physical", listTables.get(i).m_physical);
				xtwEntity.writeCharacters("\n");
				List<EntityFieldInfo> fieldInfos = listTables.get(i).getFields();
				int fieldCount = fieldInfos.size();
				for(int j=0;j<fieldCount;j++){
					xtwEntity.writeCharacters("\t\t\t");
					xtwEntity.writeStartElement("field");
					xtwEntity.writeAttribute("no", String.valueOf(j+1));
					xtwEntity.writeAttribute("logical", fieldInfos.get(j).m_logical);
					xtwEntity.writeAttribute("physical", fieldInfos.get(j).m_physical);
					xtwEntity.writeAttribute("datatype", fieldInfos.get(j).m_dataType);
					xtwEntity.writeAttribute("digits", fieldInfos.get(j).m_digits);
					xtwEntity.writeAttribute("remark", fieldInfos.get(j).remark);
					xtwEntity.writeAttribute("indexInfo", fieldInfos.get(j).m_indexInfo);
					xtwEntity.writeEndElement();
					xtwEntity.writeCharacters("\n");
				}
				xtwEntity.writeCharacters("\t\t");
				xtwEntity.writeEndElement();
				xtwEntity.writeCharacters("\n");
			}
			xtwEntity.writeCharacters("\t");
			xtwEntity.writeEndElement();
			xtwEntity.writeCharacters("\n");
			
			xtwEntity.writeCharacters("\t");
			xtwEntity.writeStartElement("applts");
			xtwEntity.writeCharacters("\n");
			for(String key: mapApplt.keySet()){
				xtwEntity.writeCharacters("\t\t");
				xtwEntity.writeStartElement("applt");
				int count = mapApplt.get(key).size();
				xtwEntity.writeAttribute("field_name", key);
				xtwEntity.writeCharacters("\n");
				for(int i=0;i<count;i++){
					String pair[] = mapApplt.get(key).get(i).split("\t");
					if(pair.length>1){
						xtwEntity.writeCharacters("\t\t\t");
						xtwEntity.writeStartElement("applt_option");
						xtwEntity.writeAttribute("applt_value", pair[0]);
						xtwEntity.writeAttribute("applt_name", pair[1]);
						xtwEntity.writeEndElement();
						xtwEntity.writeCharacters("\n");
					}
				}
				xtwEntity.writeCharacters("\t\t");
				xtwEntity.writeEndElement();
				xtwEntity.writeCharacters("\n");
			}
			xtwEntity.writeCharacters("\t");
			xtwEntity.writeEndElement();//applts
			xtwEntity.writeCharacters("\n");
			xtwEntity.writeEndElement();//entity_info
			xtwEntity.flush();
			xtwEntity.close();
			if(progress!=null){
				progress.setProgress(prog+createProgress);
				progress.setCurrentWork("over!");
			}
		} catch (FileNotFoundException fnfe) {
			System.out.println("1:" + entityFilePath);
			fnfe.printStackTrace();
		} catch (Exception e) {
			System.out.println("3:" + entityFilePath);
			e.printStackTrace();
		} finally {
			if (is != null) {
				try {
					is.close();
				} catch (Exception e) {}
				is = null;
			}
			if (out != null) {
				try {
					out.close();
				} catch (Exception e) {}
				out = null;
			}
		}
		System.out.println("Over.");
	}
}
