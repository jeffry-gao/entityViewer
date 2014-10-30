package common;

public class EntityFormatConv {
	final static int FMT_XLS = 0;
	final static int FMT_TXT = 1;
	final static int FMT_XML = 2;

	public static void main(String[] args) {

		EntityFormatConv c = new EntityFormatConv();
		c.convert(FMT_TXT, FMT_XML, "entity.txt", "entity.xml");
//		c.convert(FMT_XLS, FMT_XML, "D:/gao/git/entityViewer", "entity.xml");
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
		reader.read(inFile);
		System.out.println("writing...");
		writer.write(reader.getEntityList(),reader.getAppltMap(),outFile);
	}

}
