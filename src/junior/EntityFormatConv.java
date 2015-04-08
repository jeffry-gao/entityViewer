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

	public static void main(String[] args) {

		EntityFormatConv c = new EntityFormatConv();
//		c.convert(FMT_TXT, FMT_XML, "entity.txt", "entity.xml");
		c.convert(FMT_XLS, FMT_TXT, "Y:/3_外部設計/B_アプリ系グループ/6_データ管理チーム/00_全体/90_情報集約/【最新版】1.データ設計/090_テーブル定義書", "C:/gao/temp/entity/entity.txt");
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
		reader.read(inFile);
		System.out.println("writing...");
		writer.write(reader.getEntityList(),outFile);
		System.out.println("over.");
	}

}
