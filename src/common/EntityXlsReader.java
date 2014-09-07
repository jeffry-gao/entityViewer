package common;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;

public class EntityXlsReader {
	public static EntityInfo read(String path, Map<String,List<String>> appltMap){
		EntityInfo table = new EntityInfo();
		FileInputStream is = null;
		HSSFWorkbook inBook = null;
		HSSFSheet inSheetTbl;
		HSSFSheet inSheetIdx = null;
		HSSFRow row = null;
		HSSFCell cell = null;
		String field = null;
		int intField = 0;
//		final String sheetEntityName = "テーブル定義書";
//		final String sheetIndexName = "INDEX定義書";
//		final String sheetDoMainName = "ドメイン定義書";
		String tableId = null;

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
			table.m_logical = field;

			row = inSheetTbl.getRow(1);
			cell = row.getCell(27);
			field = cell.getStringCellValue();
			table.m_physical = field;

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
				
				EntityFieldInfo curField = new EntityFieldInfo();
				table.getFields().add(curField);
				
				field = cell.getStringCellValue();
				curField.m_no = no++;
				curField.m_logical = field;

				cell = row.getCell(12);  
				field = cell.getStringCellValue();
				curField.m_physical = field;

				cell = row.getCell(21);  
				field = cell.getStringCellValue();
				curField.m_dataType = field;

				cell = row.getCell(24);  // 
				int cellType = cell.getCellType();
				if (cellType != 3) {  // 
					if (cell.getCellType() == 0) {  // numeric
						intField = (int) cell.getNumericCellValue();
					} else {  // text
						intField = Integer.parseInt(cell.getStringCellValue());
					}
					String digits = String.valueOf(intField);
					curField.m_digits = digits;
				}
				cell = row.getCell(39);  // 
				if(cell!=null){
					field = cell.getStringCellValue();
					if(field!=null){
						curField.remark = field.replace('\n', '\t');
					}
				}

//				row = inSheetIdx.getRow(idx+2);
//				if (row == null) {
//					idx++;
//					continue;
//				}
//				cell = row.getCell(2);
//				if (cell != null && cell.getCellType() != 3) {  // 
//					//sb.append("\" pk=\"1");
//					intField = (int) cell.getNumericCellValue();  // 
//					curField.m_indexInfo = "pk("+intField+")";
//
//				}
//				for (int j = 1; j < 8; j++) {
//					cell = row.getCell(j+2);
//					if (cell != null && cell.getCellType() != 3) {  // 
//						//sb.append("\" idx").append(j).append("=\"1");
//						intField = (int) cell.getNumericCellValue();  // 
//						curField.m_indexInfo = curField.m_indexInfo + "I" + j + "(" + intField + ")";
//					}
//				}
				idx++;
			}

//			if(appltMap!=null){
//				inSheetTbl = inBook.getSheet(sheetDoMainName);
//				// A:0 B:1 C:2 D:3 E:4 F:5 G:6 H:7 I:8 J:9 K:10 L:11 M:12 N:13
//				String conditionVal = null;
//				String conditionNm = null;
//	
//				idx = 9;
//				while (true) {
//					// cell.getCellType() :: 0:numeric / 1:text / 3:
//					row = inSheetTbl.getRow(idx++);
//					cell = row.getCell(0);  // No : A10
//					if (cell == null || cell.getCellType() == HSSFCell.CELL_TYPE_BLANK) 
//						break;  //
//					if (cell.getCellType() == 1 && cell.getStringCellValue().equals("")) 
//						break;
//	
//					cell = row.getCell(12);  // M ｺﾝﾃﾞｨｼｮﾝ値
//					if (cell == null || cell.getCellType() == HSSFCell.CELL_TYPE_BLANK) 
//						continue;  //
//					if (cell.getCellType() == HSSFCell.CELL_TYPE_NUMERIC) 
//						conditionVal = String.valueOf((int)cell.getNumericCellValue());
//					else 
//						conditionVal = cell.getStringCellValue();
//					if (conditionVal.equals("")) 
//						continue;  // 
//					
//					// 
//					field = row.getCell(3).getStringCellValue();  // 
//	
//					List<String> listValue = new ArrayList<String>();
//	
//					appltMap.put(field, listValue);
//	
//					cell = row.getCell(10);
//					if (cell.getCellType() == HSSFCell.CELL_TYPE_NUMERIC) 
//						conditionNm = String.valueOf((int)cell.getNumericCellValue());
//					else 
//						conditionNm = cell.getStringCellValue();  // 
//					listValue.add(conditionVal+"\t"+conditionNm);
//	
//					while (true) {
//						row = inSheetTbl.getRow(idx++);
//						cell = row.getCell(12);  // M ｺﾝﾃﾞｨｼｮﾝ値
//						if (cell == null || cell.getCellType() == 3) break;  //
//						if (cell.getCellType() == 0) 
//							conditionVal = String.valueOf((int)cell.getNumericCellValue());
//						else 
//							conditionVal = cell.getStringCellValue();
//						if (conditionVal.equals("")|| conditionVal.equals("⑫")) break;  // 
//	
//						cell = row.getCell(3);  // D ｶﾗﾑ物理名
//						if (cell != null && cell.getCellType() != 3 && !cell.getStringCellValue().equals("")) {
//							idx--;							
//							break;
//						}
//	
//						cell = row.getCell(10); // K ｺﾝﾃﾞｨｼｮﾝ名
//						if (cell.getCellType() == 0) 
//							conditionNm = String.valueOf((int)cell.getNumericCellValue());
//						else 
//							conditionNm = cell.getStringCellValue();  //
//						listValue.add(conditionVal+"\t"+conditionNm);
//					}
//	
//					cell = row.getCell(0);  // No :
//					if (cell == null || cell.getCellType() == 3) break;  //
//					if (cell.getCellType() == 1 && cell.getStringCellValue().equals("")) break;
//				}
//	
//				inSheetTbl = null;
//				inSheetIdx = null;
//				inBook = null;
//				is.close();
//				is = null;
//	
//			}			
		} catch (FileNotFoundException e){
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return table;
	}
}
