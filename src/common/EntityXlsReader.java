package common;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;

public class EntityXlsReader implements EntityReader{
	private List<EntityInfo> listEntity;
	private Map<String, List<String>> mapApplt;

	public void read(String dirPath){
		File f = new File(dirPath);
		if (!f.isDirectory()) {
			return;
		}

		String[] sfs = f.list();
		listEntity = new ArrayList<EntityInfo>();
		mapApplt = new HashMap<String, List<String>>();
		for (int i = 0; i < sfs.length; i++) {
			if (sfs[i].endsWith(".xls")) {
				String entityDefineXls = dirPath + "/" + sfs[i];
				EntityInfo curTable = readEntity(entityDefineXls, mapApplt);
				listEntity.add(curTable);
			}
		}
	}

	// TODO not implemented
	public EntityInfo readEntity(String path, Map<String,List<String>> appltMap){


		EntityInfo table = new EntityInfo();
		FileInputStream is = null;
		HSSFWorkbook inBook = null;
		HSSFSheet inSheetTbl;
//		HSSFSheet inSheetIdx = null;
		HSSFRow row = null;
		HSSFCell cell = null;
		String field = null;
		int intField = 0;
//		String tableId = null;

		try {
//			System.out.println("reading from "+path);
			is = new FileInputStream(path);
			inBook = new HSSFWorkbook(is);

			inSheetTbl = inBook.getSheetAt(0);
//			inSheetIdx = inBook.getSheet(sheetIndexName);

			// A:0 B:1 C:2 D:3 E:4 F:5 G:6 H:7 I:8 J:9 K:10 L:11 M:12 N:13 O:14 P:15
			// Q:16 R:17 S:18 T:19 U:20 V:21 W:22 X:23 Y:24 Z:25

//			Table table = new Table();

			//
			row = inSheetTbl.getRow(0);
			cell = row.getCell(27);
			field = cell.getStringCellValue();
			table.entityDesc = field;

			row = inSheetTbl.getRow(1);
			cell = row.getCell(27);
			field = cell.getStringCellValue();
			table.entityName = field;

			int idx = 8;
			int no = 1;
			while (true) {
				// cell.getCellType() :: 0:numeric / 1:text / 3:
				row = inSheetTbl.getRow(idx);
				if(row==null){
					break;
				}
				cell = row.getCell(2);  //
				if (cell == null || cell.getCellType() == 3) break;  //

				FieldInfo curField = new FieldInfo();
				table.getFields().add(curField);

				field = cell.getStringCellValue();
				curField.no = no++;
				curField.fieldDesc = field;

				cell = row.getCell(12);
				field = cell.getStringCellValue();
				curField.fieldName = field;

				cell = row.getCell(21);
				field = cell.getStringCellValue();
				curField.dataType = field;

				cell = row.getCell(24);  //
				int cellType = cell.getCellType();
				if (cellType != 3) {  //
					if (cell.getCellType() == 0) {  // numeric
						intField = (int) cell.getNumericCellValue();
					} else {  // text
						intField = Integer.parseInt(cell.getStringCellValue());
					}
					String digits = String.valueOf(intField);
					curField.length = digits;
				}
				cell = row.getCell(39);  //
				if(cell!=null){
					field = cell.getStringCellValue();
					if(field!=null){
						curField.remark = field.replace('\n', '\t');
					}
				}

				idx++;
			}


		} catch (FileNotFoundException e){
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return table;
	}

	public List<EntityInfo> getEntityList(){
		return listEntity;

	}
	public Map<String, List<String>> getAppltMap(){
		return mapApplt;
	}

}
