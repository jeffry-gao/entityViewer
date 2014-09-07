package myTools;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;

public class ExcelDataCollector {
    public static void main(String[] args) throws IOException {
		HSSFWorkbook inBook = null;
		HSSFSheet inSheetTbl=null;
		HSSFRow row = null;
		HSSFCell cell = null;
		FileInputStream is = null;
		File dir = new File("Home");
		for(File subDir: dir.listFiles()){
			for(File xls: subDir.listFiles()){
				is = new FileInputStream(xls);
				inBook = new HSSFWorkbook(is);
				inSheetTbl = inBook.getSheet("•\Ž†");
				row = inSheetTbl.getRow(18);
				cell = row.getCell(6);
				System.out.print(cell.getStringCellValue()+"=");
				row = inSheetTbl.getRow(20);
				cell = row.getCell(6);
				System.out.println(cell.getStringCellValue());
			}
		}

    }

}
