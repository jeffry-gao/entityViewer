package common;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class EntityXlsReader implements EntityReader{
	private List<EntityInfo> listEntity;
	private Map<String, String> mapComment;
	private String keyword = null;
	private String exclusiveWord = null;

	private int rowEntityNameJP = 2;
	private int colEntityNameJP = 'H'-'A';

	private int rowEntityNamePh = 2;
	private int colEntityNamePh = 'C'-'A';

	private int rowRowStart = 6;
	private int colFieldNameJP = 'C' - 'A';
	private int colFieldNamePh = 'B' - 'A';
	private int colFieldType = 'E' - 'A';
	private int colLenTotal = 'F' - 'A';
	private int colLenDecimal = 'G' - 'A';
	private int colRemark = 'M' - 'A';
	private int colPKFlag = 'J' - 'A';

	private String sheetName = "テーブル定義書";

	class DomainType{
		DomainType(String type, String len){
			this.type = type;
			this.len = len;
		}
		String type;
		String len;
	}

	private Map<String,DomainType> domainMap = new HashMap<String,DomainType>();

	public EntityXlsReader(){
		domainMap.put("取引先コード", 	new DomainType("CHAR",	"7"));
		domainMap.put("契約番号", 		new DomainType("CHAR",	"12"));
		domainMap.put("金額", 			new DomainType("NUMBER","13"));
		domainMap.put("年月日", 		new DomainType("NUMBER","8"));
		domainMap.put("フラグ",			new DomainType("NUMBER","1"));
		domainMap.put("台数６", 		new DomainType("NUMBER","6"));
	}

	public void setKeyword(String keyword){
		this.keyword = keyword;
	}

	public void read(String dirPath){
		File f = new File(dirPath);
		if (!f.isDirectory()) {
			return;
		}

		String[] sfs = f.list();
		listEntity = new ArrayList<EntityInfo>();
		mapComment = new HashMap<String, String>();
		for (int i = 0; i < sfs.length; i++) {
			if(keyword!=null&&!sfs[i].contains(keyword))
				continue;
			if(exclusiveWord!=null&&sfs[i].contains(exclusiveWord))
				continue;
			if (sfs[i].endsWith(".xls")||sfs[i].endsWith(".xlsx")) {
				String entityDefineXls = dirPath + "/" + sfs[i];
				System.out.println("<"+(i+1)+">reading from "+entityDefineXls);
				EntityInfo curTable = readEntity(entityDefineXls, mapComment);
				listEntity.add(curTable);
			}
		}
	}

	// 2015/03/06 SMASテーブル定義書のフォーマット対応
	public EntityInfo readEntity(String path, Map<String,String> commentMap){


		EntityInfo entity = new EntityInfo();
		FileInputStream is = null;
		Workbook inBook = null;
		Sheet inSheetTbl;
		Row row = null;
		Cell cell = null;
		String field = null;
		int intField = 0;

		try {
			is = new FileInputStream(path);

			entity.defineFile = path;

			if(path.endsWith("xls"))
				inBook = new HSSFWorkbook(is);
			else if (path.endsWith("xlsx"))
				inBook = new XSSFWorkbook(is);

			inSheetTbl = inBook.getSheet(sheetName);

			row = inSheetTbl.getRow(rowEntityNameJP);
			cell = row.getCell(colEntityNameJP);
			field = cell.getStringCellValue();
			entity.entityNameJP = field;

			row = inSheetTbl.getRow(rowEntityNamePh);
			cell = row.getCell(colEntityNamePh);
			field = cell.getStringCellValue();
			entity.entityName = field;

			int idx = rowRowStart;
			int no = 1;
			while (true) {
				// cell.getCellType() :: 0:numeric / 1:text / 3:
				row = inSheetTbl.getRow(idx);
				if(row==null){
					break;
				}
				cell = row.getCell(colFieldNameJP);  //
				if (cell == null || cell.getCellType() == 3) break;  //

				FieldInfo curField = new FieldInfo();
				entity.getFields().add(curField);

				field = cell.getStringCellValue();
				curField.seqNo = no++;
				curField.fieldDesc = field;

				cell = row.getCell(colFieldNamePh);
				field = cell.getStringCellValue();
				curField.fieldName = field;

				if(curField.fieldDesc.isEmpty())
					curField.fieldDesc = curField.fieldName;
				if(curField.fieldName.isEmpty())
					curField.fieldName = curField.fieldDesc;

				cell = row.getCell(colFieldType);
				field = cell.getStringCellValue();
				if(field.isEmpty()){
					cell = row.getCell('D'-'A');
					String domainName = cell.getStringCellValue();
					if(domainMap.get(domainName)!=null){
						curField.dataType = domainMap.get(domainName).type;
						curField.length = domainMap.get(domainName).len;
					}
				} else {
					curField.dataType = field;

					cell = row.getCell(colLenTotal);  //
					int cellType = cell.getCellType();
					if (cellType != 3) {  //
						if (cell.getCellType() == 0) {  // numeric
							intField = (int) cell.getNumericCellValue();
						} else {  // text
							try {
								intField = Integer.parseInt(cell.getStringCellValue());
							} catch (NumberFormatException e) {
								intField = -1;
							}
						}
						if(intField==-1)
							curField.length = cell.getStringCellValue();
						else {
							String digits = String.valueOf(intField);
							curField.length = digits;
						}
					}

					if(colLenDecimal!=-1){
						cell = row.getCell(colLenDecimal);  //
						cellType = cell.getCellType();
						if (cellType != 3) {  //
							if (cell.getCellType() == 0) {  // numeric
								intField = (int) cell.getNumericCellValue();
							} else {  // text
								try{
									intField = Integer.parseInt(cell.getStringCellValue());
								} catch (NumberFormatException e){
									intField = -1;
								}
							}
							if(intField==-1)
								curField.precision = cell.getStringCellValue();
							else {
								String digits = String.valueOf(intField);
								curField.precision = digits;
							}
						}
					}
				}

				cell = row.getCell(colPKFlag);
				if(cell!=null&&cell.getCellType()==Cell.CELL_TYPE_STRING){
					if(cell.getStringCellValue().equals("○")){
						curField.pkInfo = "○";
					}
				}

				cell = row.getCell(colRemark);  //
				if(cell!=null){
					field = cell.getStringCellValue();
					if(field!=null){
						curField.userComment = field.replace('\n', '\t');
					}
				}

				idx++;
			}


		} catch (NumberFormatException e){
			System.err.println("予想外のフォーマットで変換失敗。ファイルを確認してください。");
			e.printStackTrace();
		} catch (FileNotFoundException e){
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e){
			System.err.println("読み込み失敗。ファイルを確認してください。");
			e.printStackTrace();
		}
		return entity;
	}

	public List<EntityInfo> getEntityList(){
		return listEntity;

	}
	public Map<String, String> getCommentMap(){
		return mapComment;
	}

	@Override
	public void read(String fileName, String appltFile) {

	}

	@Override
	public void setExclusiveKeyword(String exclusiveWord) {
		this.exclusiveWord = exclusiveWord;
	}

}
