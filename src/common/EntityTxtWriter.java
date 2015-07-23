package common;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.List;
import java.util.Map;

public class EntityTxtWriter implements EntityWriter {
	public void write(List<EntityInfo> listTables, String outputFile){
		BufferedWriter writer;
		try {
//			writer = new BufferedWriter(new FileWriter(ouputFile));
			writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outputFile), "utf-8"));
			int tableCount = listTables.size();
			for(int i=0;i<tableCount;i++){
				int colCount = listTables.get(i).listFieldInfo.size();
				writer.write("[define]"+listTables.get(i).defineFile+"\n");
				String physicalName = listTables.get(i).entityName;
				String logicalName = listTables.get(i).entityNameJP;
				for(int j=0;j<colCount;j++){
					FieldInfo workInfo = listTables.get(i).listFieldInfo.get(j);
					writer.write(physicalName+"\t");
					writer.write(logicalName+"\t");
					writer.write(workInfo.fieldName+"\t");
					writer.write(workInfo.fieldDesc+"\t");
					writer.write(workInfo.dataType+"\t");
					writer.write(workInfo.length+"\t");
					writer.write(workInfo.precision+"\t");
					writer.write("N\t");//TODO
					writer.write(workInfo.pkInfo+"\n"); //new line

				}
			}
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void write(List<EntityInfo> listTables, String outputFileName, Map<String, String> commentMap, String outputComment) {

	}

}
