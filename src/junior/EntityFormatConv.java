package junior;

import common.EntityReader;
import common.EntityTxtReader;
import common.EntityTxtWriter;
import common.EntityWriter;
import common.EntityXlsReader;
import common.EntityXlsWriter;
import common.EntityXmlReader;
import common.EntityXmlWriter;



public class EntityFormatConv {
	final static int FMT_XLS = 0;
	final static int FMT_TXT = 1;
	final static int FMT_XML = 2;
//	final static String srcPath = "C:/temp/table";
	final static String srcPath="Y:/0_全フェーズ/B_アプリ系グループ/6_データ管理チーム/90_変更管理/10_ERStudio/10_受渡/10_設計書/040_テーブル定義書";
//	final static String srcPath="Y:/0_全フェーズ/B_アプリ系グループ/6_データ管理チーム/90_変更管理/10_ERStudio/20_公開/10_設計書/040_テーブル定義書/20150629";
	final static String destPath="Y:/2_要件定義後半/B_アプリ系グループ/4_共通・移行チーム/1_共通業務・業績管理/99_個人フォルダ/300_椿原/個人/高/entityViewer";
	final static String destFile=destPath+"/entity.txt";

	public static void main(String[] args) {


		EntityFormatConv c = new EntityFormatConv();
//		c.convert(FMT_TXT, FMT_XML, "entity.txt", "entity.xml");
		c.convert(FMT_XLS, FMT_TXT, srcPath, destFile);


//		c.convert(FMT_XLS, FMT_TXT, "C:/gao/temp/entity", "C:/gao/temp/entity.txt");
	}

	public void convert(int formatFrom, int formatTo, String inFile, String outFile){
		EntityReader reader = null;
		EntityWriter writer = null;

		switch(formatFrom){
		case FMT_XLS:
			reader = new EntityXlsReader();
			break;
		case FMT_TXT:
			reader = new EntityTxtReader();
			break;
		case FMT_XML:
			reader = new EntityXmlReader();
			break;
		}

		switch(formatTo){
		case FMT_XLS:
			writer = new EntityXlsWriter();
			break;
		case FMT_TXT:
			writer = new EntityTxtWriter();
			break;
		case FMT_XML:
			writer = new EntityXmlWriter();
			break;
		}

		System.out.println("reading...");
		reader.setKeyword("_テーブル定義書_");
		reader.setExclusiveKeyword("~$");
		reader.read(inFile);
		System.out.println("writing...");
		writer.write(reader.getEntityList(),outFile);
		System.out.println("over.");
	}

}
