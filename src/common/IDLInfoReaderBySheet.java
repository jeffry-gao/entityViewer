package common;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;

public class IDLInfoReaderBySheet {
	final static String sheetNameIDLList="ÇhÇcÇkàÍóó";
	final static String sheetNameExceptionList="ÇhÇcÇkó·äOàÍóó";
	final static int    rowIDLStart=6;
	final static int    countIDL1Page=65;
	final static int    countInterPageRows=6;

	//service sheet
	final static int    rowServiceBasic=5;
	final static int    colModuleName=7;
	final static int    colInterfaceName=27;
	final static int    colServiceName=46;
	
	final static int    rowServiceRemark=11;
	final static int    rowReturnType=17;
	final static int 	rowOneway=5;
	final static int 	rowException=37;
	final static int 	colExceptionModule=2;
	final static int 	colExceptionName=15;
	final static int 	colExceptionRemark=36;
	final static int    colReturnRemark=46;	
	final static int    colReturnType=36;
	final static int    colReturnLogicalName=2;	
	final static int    rowParamStart=20;
	final static int    colParamLogicalName=2;
	final static int    colParamPhysicalName=15;
	final static int    colParamType=36;
	final static int    colParamIn=47;
	final static int    colParamOut=50;
	final static int    colParamRemark=52;
	final static int    colOneway=63;
	
	// struct
	final static String sheetNameStructList="ç\ë¢ëÃàÍóó";
	final static int    rowStructListStart=6;
	final static int    colStructList=2;
	final static int    rowStructMemberStart=12;
	final static int    countStructMember1Page=64;
	final static int    countStructMemberInterPageRows=8;
	final static int    colStructMemberLogicalName=2;
	final static int    colStructMemberPhysicalName=17;
	final static int    colStructMemberType=45;
	final static int    colStructMemberRemark=56;
	private static final int CLBR_ADD_END = 1;
	private static final int CLBR_CHANGE_BRCTS = 2;
	private static final int CLBR_TRIM = 3;
	private static final int CLBR_GUESS_SHEET = 4;
	
	public static List<IDLServiceInfo>    serviceInfos;
	public static Map<String,IDLStructInfo> structMaps;
	public static List<String>		  exceptions;	
	
	public static void readInIDLInfos(String path){
		if(path==null||path.isEmpty()){
			path="IDL_INTERFACE";
		}
		File interfaceDir = new File(path);
		if(interfaceDir.exists()){
			serviceInfos = new ArrayList<IDLServiceInfo>();
			File[] idls = interfaceDir.listFiles();
			for(int i=0;i<idls.length;i++){
				if(idls[i].getName().equals("IDLó·äOàÍóó.xls")){
					readInIDLException(idls[i]);
				} else {
					readInIDLInfo(idls[i]);
				}
			}
			System.out.println("readInIDLInfos over.");
		}
	}
	private static void readInIDLException(File idlFile){
		HSSFWorkbook objWorkBook = null;
		HSSFSheet objWorkSheet=null;
		HSSFRow row = null;
		HSSFCell cell = null;
		FileInputStream fis;
		try {
			exceptions = new ArrayList<String>();
			fis = new FileInputStream(idlFile);
			objWorkBook = new HSSFWorkbook(fis );
			objWorkSheet = objWorkBook.getSheet(sheetNameExceptionList);
			int rowIndex = rowIDLStart;
			String moduleName="", exceptionName="";
			while(true){
				if(rowIndex%countIDL1Page==0)
					rowIndex += countInterPageRows;
				row = objWorkSheet.getRow(rowIndex);
				cell = row.getCell(15);
				if(cell==null||cell.getStringCellValue()==null||cell.getStringCellValue().equals("")){
					break;
				}
				exceptionName = cell.getStringCellValue();
				cell = row.getCell(colModuleName);
				if(cell==null||cell.getStringCellValue()==null||cell.getStringCellValue().equals("")){
				} else {
					moduleName = cell.getStringCellValue();
				}
				cell = row.getCell(34);
				if(cell==null||cell.getStringCellValue()==null||cell.getStringCellValue().equals("")){
				} else {
//					remark = cell.getStringCellValue();
				}

				exceptions.add(moduleName+"::"+exceptionName);
				rowIndex++;
			}
			fis.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		
	}
	private static void readInIDLInfo(File idlFile){
		HSSFWorkbook objWorkBook = null;
		HSSFSheet objWorkSheet=null;
		FileInputStream fis;
		try {
			fis = new FileInputStream(idlFile);
			objWorkBook = new HSSFWorkbook(fis );
			for(int i=1;i<objWorkBook.getNumberOfSheets();i++){
				objWorkSheet = objWorkBook.getSheetAt(i);
				readIDLInfoDetail(objWorkSheet);
			}
			fis.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		
	}
	
	static void readIDLInfoDetail(HSSFSheet objWorkSheet){

		IDLServiceInfo service = new IDLServiceInfo();
		serviceInfos.add(service);
		
		HSSFRow row = null;
		HSSFCell cell = null;
		// servcie basic info
		row = objWorkSheet.getRow(rowServiceBasic);
		cell = row.getCell(colModuleName);
		service.moduleName = cell.getStringCellValue();
		cell = row.getCell(colInterfaceName);
		service.interfaceName = cell.getStringCellValue();
		cell = row.getCell(colServiceName);
		service.serviceName = cell.getStringCellValue();
		
		// service remark
		for(int i=0;i<4;i++){
			row = objWorkSheet.getRow(rowServiceRemark+i);
			cell = row.getCell(0);
			String temp = cell.getStringCellValue();
			service.remarkDetail += calibrate(CLBR_TRIM,temp);
			service.remarkDetail = calibrate(CLBR_ADD_END,service.remarkDetail);
			if(i==0)
				service.remarkSummary = calibrate(CLBR_CHANGE_BRCTS,temp);
		}
		service.remarkDetail = calibrate(CLBR_CHANGE_BRCTS, service.remarkDetail);
		// return value
		row = objWorkSheet.getRow(rowReturnType);
		cell = row.getCell(colReturnType);
		service.returnType = cell.getStringCellValue();
		cell = row.getCell(colReturnLogicalName);
		service.returnLogicalName = cell.getStringCellValue();
		cell = row.getCell(colReturnRemark);
		service.returnRemark = cell.getStringCellValue();
		cell = objWorkSheet.getRow(rowOneway).getCell(colOneway);
		service.oneway = cell.getStringCellValue().equals("oneway");
		String logicalName="", physicalName="", dataType="", paramIn="", paramOut="", paramRemark="";
		int rowIndex = rowParamStart;
		while(true){
			row = objWorkSheet.getRow(rowIndex);
			cell = row.getCell(colParamLogicalName);
			if(cell==null||cell.getStringCellValue()==null||cell.getStringCellValue().equals("")){
				break;
			}
			logicalName = cell.getStringCellValue();
			cell = row.getCell(colParamPhysicalName); // i.e. session_id
			physicalName = cell.getStringCellValue();
			cell = row.getCell(colParamType); // i.e. string
			dataType = cell.getStringCellValue();
			cell = row.getCell(colParamIn);
			paramIn = cell.getStringCellValue();
			cell = row.getCell(colParamOut);
			paramOut = cell.getStringCellValue();
			cell = row.getCell(colParamRemark);
			paramRemark = cell.getStringCellValue();
			
			
			ParamInfo pi = new ParamInfo();
			service.paramInfos.add(pi);
			pi.paraLogicalName = logicalName;
			pi.paraPhysicalName = physicalName;
			pi.typeName = dataType;
			if(paramIn!=null&&paramIn.equals("Åõ"))
				pi.paramInOut |= ParamInfo.PARAM_IN;
			if(paramOut!=null&&paramOut.equals("Åõ"))
				pi.paramInOut |= ParamInfo.PARAM_OUT;
			pi.remark = paramRemark;
			
			rowIndex++;
		}
		rowIndex = rowException;
		String curModuleName="";
		while(true){
			row = objWorkSheet.getRow(rowIndex);
			cell = row.getCell(colExceptionName);
			String exceptionName = cell.getStringCellValue();
			if(exceptionName.isEmpty()){
				break;
			}else if (exceptionName.equals("ó·äOñº")){
				rowIndex++;
			} else {
				cell = row.getCell(colExceptionModule);
				String mn = cell.getStringCellValue();
				if(!mn.isEmpty()&&!mn.equals(curModuleName)){
					curModuleName=mn;
				}
				cell = row.getCell(colExceptionRemark);
				String rm = cell.getStringCellValue();
				service.exceptions.add(curModuleName+"\t"+exceptionName+"\t"+rm);
				rowIndex++;
			}
		}
	}
	private static String calibrate(int type, String content) {
		switch(type){
		case CLBR_ADD_END:
			if(!content.endsWith("ÅB")&&!content.isEmpty())
				content += "ÅB";
			break;
		case CLBR_CHANGE_BRCTS:
			content = content.replace('Åi','(');
			content = content.replace('Åj',')');
			break;
		case CLBR_TRIM:
			content = content.trim();
			while(content.startsWith("Å@"))
				content = content.substring("Å@".length());
			while(content.endsWith("Å@"))
				content = content.substring(0,content.length()-"Å@".length());
			break;
		case CLBR_GUESS_SHEET:
			//TODO
			if(content.equals("getOrderTerminalStatusSummaryInfos"))
				content = "getOrderTerminalStatusSummary..";
			else if(content.equals("CallHistoryCriteriaInfo"))
				content = "Call_historyCriteriaInfo";
			else if(content.equals("CallHistoryBasicInfo"))
				content = "Call_historyBasicInfo";
			else if(content.equals("DeliveryOrderInfo"))
				content = "DeliverOrderInfo";
			else if(content.equals("EnqCntInfo"))
				content = "EnqCnt";
			break;
		}
		return content;
	}
	public static void readInStructInfos(){
		File structDir = new File("IDL_STRUCT");
		if(structDir.exists()){
			structMaps = new LinkedHashMap<String, IDLStructInfo>();
			File[] structs = structDir.listFiles();
			for(int i=0;i<structs.length;i++){
//				System.out.println("reading "+structs[i].getName());
				readInStructInfo(structs[i]);
			}
			System.out.println("readInStructInfos over.");
		}
	}
	// read the list sheet
	private static void readInStructInfo(File structFile){
		HSSFWorkbook objWorkBook = null;
		HSSFSheet objWorkSheet=null;
		HSSFRow row = null;
		HSSFCell cell = null;
		FileInputStream fis;
		try {
			fis = new FileInputStream(structFile);
			objWorkBook = new HSSFWorkbook(fis );
			objWorkSheet = objWorkBook.getSheet(sheetNameStructList);
			
			int rowIndex = rowStructListStart;
			while(true){
				row = objWorkSheet.getRow(rowIndex);
				cell = row.getCell(colStructList);
				if(cell==null||cell.getStringCellValue()==null||cell.getStringCellValue().equals("")){
					break;
				}

				IDLStructInfo info = new IDLStructInfo();
				info.structName=cell.getStringCellValue();
				structMaps.put(info.structName, info);
				readInStructInfoDetail(objWorkBook,info);
				rowIndex++;
			}
			fis.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	// read the detail sheet
	private static void readInStructInfoDetail(HSSFWorkbook xlsBook, IDLStructInfo info){
		String sheetName = info.structName;
		if(sheetName.length()>31)
			sheetName = sheetName.substring(0,31);
		HSSFSheet objWorkSheet=xlsBook.getSheet(sheetName);
		if(objWorkSheet==null){
			sheetName = calibrate(CLBR_GUESS_SHEET, info.structName);
			objWorkSheet=xlsBook.getSheet(sheetName);
		}
		if(objWorkSheet==null){
			System.err.println("failed to open sheet "+sheetName + " for struct "+info.structName );
		} else {
			System.out.println("reading detail of "+info.structName+"...");
			HSSFRow row = null;
			HSSFCell cell = null;
			
			String logicalName="", physicalName="", dataType="", paramRemark="";
			int rowIndex = rowStructMemberStart;
			while(true){
				if((rowIndex+1)%countStructMember1Page==0)
					rowIndex += countStructMemberInterPageRows;
				row = objWorkSheet.getRow(rowIndex);
				cell = row.getCell(colParamLogicalName);
				if(cell==null||cell.getStringCellValue()==null||cell.getStringCellValue().equals("")){
					break;
				}
				logicalName = cell.getStringCellValue();
				cell = row.getCell(colStructMemberPhysicalName);
				physicalName = cell.getStringCellValue();
				if(physicalName.equals("item_name")||physicalName.equals("parent_item_number")){
					System.out.println(physicalName);
				}
				cell = row.getCell(colStructMemberType);
				dataType = cell.getStringCellValue();
				cell = row.getCell(colStructMemberRemark);
				paramRemark = cell.getStringCellValue();
				info.listMember.add(dataType+"\t"+physicalName+"\t"+calibrate(CLBR_TRIM, logicalName)+"\t"+paramRemark);
				
				rowIndex++;
			}
		}		
	}
}
