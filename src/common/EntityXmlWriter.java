package common;

import java.io.FileOutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamWriter;

public class EntityXmlWriter implements EntityWriter{


	@Override
	public void write(List<EntityInfo> listTables,
			Map<String, List<String>> mapApplt, String ouputFile) {
		FileOutputStream out = null;

		try {

			XMLOutputFactory xof =  XMLOutputFactory.newInstance();
			XMLStreamWriter xtwEntity = null;

			xtwEntity = xof.createXMLStreamWriter(new FileOutputStream(ouputFile),"utf-8");
			xtwEntity.writeStartDocument("utf-8","1.0");
			xtwEntity.writeCharacters("\n");
			xtwEntity.writeStartElement("entity_info");
			xtwEntity.writeCharacters("\n");
			xtwEntity.writeCharacters("\t");
			xtwEntity.writeStartElement("entitys");
			xtwEntity.writeCharacters("\n");
			for(int i=0;i<listTables.size();i++){
				System.out.println("writing " + String.valueOf(i+1) + ":" + listTables.get(i).entityName);
				xtwEntity.writeCharacters("\t\t");
				xtwEntity.writeStartElement("entity");
				xtwEntity.writeAttribute("id", listTables.get(i).m_id);
				xtwEntity.writeAttribute("logical", listTables.get(i).entityDesc);
				xtwEntity.writeAttribute("physical", listTables.get(i).entityName);
				xtwEntity.writeCharacters("\n");
				List<FieldInfo> fieldInfos = listTables.get(i).getFields();
				int fieldCount = fieldInfos.size();
				for(int j=0;j<fieldCount;j++){
					xtwEntity.writeCharacters("\t\t\t");
					xtwEntity.writeStartElement("field");
					xtwEntity.writeAttribute("no", String.valueOf(j+1));
					xtwEntity.writeAttribute("logical", fieldInfos.get(j).fieldDesc);
					xtwEntity.writeAttribute("physical", fieldInfos.get(j).fieldName);
					xtwEntity.writeAttribute("datatype", fieldInfos.get(j).dataType);
					xtwEntity.writeAttribute("digits", fieldInfos.get(j).length);
					xtwEntity.writeAttribute("remark", fieldInfos.get(j).remark);
					xtwEntity.writeAttribute("indexInfo", fieldInfos.get(j).pkInfo);
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
			if( mapApplt==null )
				mapApplt = new  HashMap<String,List<String>>();
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
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
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
