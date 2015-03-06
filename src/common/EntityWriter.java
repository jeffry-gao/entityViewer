package common;

import java.util.List;
import java.util.Map;

public interface EntityWriter {
	public void write(List<EntityInfo> listTables, String outputFileName);
	public void write(List<EntityInfo> listTables, String outputFileName, Map<String,String> commentMap, String outputComment);
}
