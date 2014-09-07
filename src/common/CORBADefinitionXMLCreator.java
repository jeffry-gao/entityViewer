package common;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.List;
import java.util.Map;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamWriter;

public class CORBADefinitionXMLCreator {
	public static final String CORBA_INTERFACE_FILE = "citf.xml";
	public static final String IDL_DEFINE_PATH= "C:/jsc-dev/eclipse/workspace/tool/data/IDL_INTERFACE";
	public static final String STRUCTURE_DEFINE_PATH= "C:/jsc-dev/eclipse/workspace/tool/data/IDL_STRUCT";
	
	public static void main(String[] args) {
		createCITFXML(IDL_DEFINE_PATH,STRUCTURE_DEFINE_PATH,CORBA_INTERFACE_FILE);
	}
	
	public static void createCITFXML(String idlDefPath, String structureDefPath, String ouputFile) {
		
		
		FileInputStream is = null;
		FileOutputStream out = null;
		
		String entityFilePath = null;

		try {
			
			File f = new File(idlDefPath);
			if (!f.isDirectory()) {
				System.exit(-1);
			}
			f = new File(structureDefPath);
			if (!f.isDirectory()) {
				System.exit(-1);
			}
			IDLInfoReader.readInIDLInfos(idlDefPath);
			IDLInfoReader.readInStructInfos(structureDefPath);
			List<IDLServiceInfo> listInfos = IDLInfoReader.serviceInfos;
			Map<String,IDLStructInfo> mapStructInfos = IDLInfoReader.structMaps;

			XMLOutputFactory xof =  XMLOutputFactory.newInstance();
			XMLStreamWriter xtwEntity = null;
			
			xtwEntity = xof.createXMLStreamWriter(new FileOutputStream(ouputFile),"utf-8");
			xtwEntity.writeStartDocument("utf-8","1.0");
			xtwEntity.writeCharacters("\n");
			xtwEntity.writeStartElement("corba_info");
			xtwEntity.writeCharacters("\n");
			xtwEntity.writeCharacters("\t");
			xtwEntity.writeStartElement("services");
			xtwEntity.writeCharacters("\n");
			for(int i=0;i<listInfos.size();i++){
				xtwEntity.writeCharacters("\t\t");
				xtwEntity.writeStartElement("service");
				xtwEntity.writeAttribute("module", listInfos.get(i).moduleName);
				xtwEntity.writeAttribute("home", listInfos.get(i).interfaceName);
				xtwEntity.writeAttribute("name", listInfos.get(i).serviceName);
				xtwEntity.writeAttribute("return_type", listInfos.get(i).returnType);
				xtwEntity.writeAttribute("return_name", listInfos.get(i).returnLogicalName);
				xtwEntity.writeCharacters("\n");
				List<ParamInfo> pis = listInfos.get(i).paramInfos;
				int paramCount = pis.size();
				for(int j=0;j<paramCount;j++){
					xtwEntity.writeCharacters("\t\t\t");
					xtwEntity.writeStartElement("parameter");
					xtwEntity.writeAttribute("type", pis.get(j).typeName);
					xtwEntity.writeAttribute("physical", pis.get(j).paraPhysicalName);
					xtwEntity.writeAttribute("logical", pis.get(j).paraLogicalName);
					String inoutInfo="";
					if((pis.get(j).paramInOut&ParamInfo.PARAM_IN)==ParamInfo.PARAM_IN)
						inoutInfo += "i";
					if((pis.get(j).paramInOut&ParamInfo.PARAM_OUT)==ParamInfo.PARAM_OUT)
						inoutInfo += "o";
					xtwEntity.writeAttribute("inout", inoutInfo);
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
			xtwEntity.writeStartElement("structrues");
			xtwEntity.writeCharacters("\n");
			for(String key: mapStructInfos.keySet()){
				xtwEntity.writeCharacters("\t\t");
				xtwEntity.writeStartElement("structure");
				int count = mapStructInfos.get(key).listMember.size();
				xtwEntity.writeAttribute("structure_name", key);
				xtwEntity.writeCharacters("\n");
				for(int i=0;i<count;i++){
					String infos[] = mapStructInfos.get(key).listMember.get(i).split("\t");
					//[type]+'\t'+[physical name]+'\t'+[logical name]+'\t'+[remark]
					if(infos.length>1){
						xtwEntity.writeCharacters("\t\t\t");
						xtwEntity.writeStartElement("member");
						xtwEntity.writeAttribute("type", infos[0]);
						xtwEntity.writeAttribute("physical_name", infos[1]);
						xtwEntity.writeAttribute("logical_name", infos[2]);
						xtwEntity.writeEndElement();
						xtwEntity.writeCharacters("\n");
					}
				}
				xtwEntity.writeCharacters("\t\t");
				xtwEntity.writeEndElement();
				xtwEntity.writeCharacters("\n");
			}
			xtwEntity.writeCharacters("\t");
			xtwEntity.writeEndElement();//structures
			xtwEntity.writeCharacters("\n");
			xtwEntity.writeEndElement();//corba_info
			xtwEntity.flush();
			xtwEntity.close();
			
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
