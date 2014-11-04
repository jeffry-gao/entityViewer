package common;

import java.util.List;
import java.util.Map;

public interface EntityWriter {
	public void write(List<EntityInfo> listTables, Map<String,List<String>> mapApplt, String outputFileName);
}
