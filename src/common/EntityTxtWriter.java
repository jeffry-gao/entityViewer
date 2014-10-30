package common;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Map;

public class EntityTxtWriter implements EntityWriter {
	public void write(List<EntityInfo> listTables, Map<String,List<String>> mapApplt, String ouputFile){
		BufferedWriter writer;
		try {
			writer = new BufferedWriter(new FileWriter(ouputFile));
			int tableCount = listTables.size();
			for(int i=0;i<tableCount;i++){
				int colCount = listTables.get(i).listFieldInfo.size();
				String physicalName = listTables.get(i).entityName;
				String logicalName = listTables.get(i).entityDesc;
				for(int j=0;j<colCount;j++){
					FieldInfo workInfo = listTables.get(i).listFieldInfo.get(j);
					writer.write(physicalName+"\t");
					writer.write(logicalName+"\t");
					writer.write(workInfo.fieldName+"\t");
					writer.write(workInfo.fieldDesc+"\t");
					writer.write(workInfo.dataType+"\t");
					writer.write(workInfo.length+"\t");
					writer.write("N\t");//TODO
					writer.write(workInfo.pkInfo+"\n"); //new line

				}
			}
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
