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

// <corba_info>
//     <services>
//         <service module='' ...>
//             <parameter type='string' ...></parameter>
//			   ...
//             <parameter type='string' ...></parameter>
//         </service>
//         ...
//         <service>
//         </service>
//     </services>
//     <structrues>
//         <structure ... >
//             <member type='' ...></member>
//             ...
//         </structure>
//     <structrues>
// </corba_info>
public class CORBADefinitionXMLReader {

	static List<IDLServiceInfo> services;
	static Map<String, IDLStructInfo> structs;
	static IDLServiceInfo curService;
	static IDLStructInfo  curStruct;
	static List<String> m_curFiledApplt;
  
	public CORBADefinitionXMLReader() {
		services = new ArrayList<IDLServiceInfo>();
		curService = null;
		curStruct = null;
		structs = new HashMap<String, IDLStructInfo>();
		m_curFiledApplt = null;
	}
	
	public List<IDLServiceInfo> getServiceList() {
		return services;
	}

	public Map<String, IDLStructInfo> getIDLStructs() {
		return structs;
	}
	
	public void readInData(String citfXML){
		XMLInputFactory xmlif = XMLInputFactory.newInstance();
		try {
			String encodingType = getEncodingType(citfXML);
			XMLStreamReader xmlrCitf = xmlif.createXMLStreamReader(new InputStreamReader(
																				new FileInputStream(citfXML), 
																				encodingType)
																	);
			System.out.println(citfXML+" is read as:"+xmlrCitf.getEncoding());
			while(xmlrCitf.hasNext()){
			      readCitf(xmlrCitf);
			      xmlrCitf.next();
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
  
	private void readCitf(XMLStreamReader xmlr) {
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
			  if(localName.equals("service")){
				  curService = new IDLServiceInfo();
				  for (int i=0; i < xmlr.getAttributeCount(); i++) { 
					  String attributeName = xmlr.getAttributeLocalName(i);
					  if(attributeName.equals("module")){ 
						  curService.moduleName = xmlr.getAttributeValue(i); 
					  } else if (attributeName.equals("home")){
						  curService.interfaceName = xmlr.getAttributeValue(i);
					  } else if (attributeName.equals("name")){
						  curService.serviceName = xmlr.getAttributeValue(i);
					  } else if (attributeName.equals("return_type")){
						  curService.returnType = xmlr.getAttributeValue(i);
					  } else if (attributeName.equals("return_name")){
						  curService.returnLogicalName = xmlr.getAttributeValue(i);
					  }
				  }		  
			  } else if (localName=="structure") {
				  curStruct = new IDLStructInfo();
				  for (int i=0; i < xmlr.getAttributeCount(); i++) { 
					  String attributeName = xmlr.getAttributeLocalName(i);
					  if(attributeName.equals("structure_name")){ 
						  curStruct.structName = xmlr.getAttributeValue(i); 
					  } 
				  }		  
			  } else if (localName=="parameter") {
				  ParamInfo pi = new ParamInfo();
				  curService.paramInfos.add(pi);
				  for (int i=0; i < xmlr.getAttributeCount(); i++) { 
					  String attributeName = xmlr.getAttributeLocalName(i);
					  if(attributeName.equals("type")){ 
						  pi.typeName = xmlr.getAttributeValue(i); 
					  } else if (attributeName.equals("physical")){
						  pi.paraPhysicalName = xmlr.getAttributeValue(i);
					  } else if (attributeName.equals("logical")){
						  pi.paraLogicalName = xmlr.getAttributeValue(i);
					  } else if (attributeName.equals("inout")){
						  String inoutInfo = xmlr.getAttributeValue(i);
						  if(inoutInfo.contains("i"))
							  pi.paramInOut += ParamInfo.PARAM_IN;
						  if(inoutInfo.contains("o"))
							  pi.paramInOut += ParamInfo.PARAM_OUT;
					  }
				  }		  
			  } else if (localName=="member") {
				  String type="",physical_name="",logical_name="";	  
				  for (int i=0; i < xmlr.getAttributeCount(); i++) { 
					  String attributeName = xmlr.getAttributeLocalName(i);
					  if(attributeName.equals("type")){ 
						  type = xmlr.getAttributeValue(i); 
					  } else if (attributeName.equals("physical_name")){
						  physical_name = xmlr.getAttributeValue(i);
					  } else if (attributeName.equals("logical_name")){
						  logical_name = xmlr.getAttributeValue(i);
					  } 
				  }
				  curStruct.listMember.add(type+"\t"+physical_name+"\t"+logical_name);
			  } else if (localName=="services") {
				  System.out.println("start to read services...");
			  } else if (localName=="structrues") {
				  System.out.println("start to read structrues...");
			  } 
		  }
	}
  
	private static void readAtElementEnd(XMLStreamReader xmlr){
	  if(xmlr.hasName()){ 
		  String localName = xmlr.getLocalName();
		  if(localName=="service"){
			  services.add(curService);
			  curService = null;
		  } else if (localName=="structure") {
			  structs.put(curStruct.structName, curStruct);
			  curStruct = null;
		  }
	  }
	}

}
