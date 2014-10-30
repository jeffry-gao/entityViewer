package common;

import java.util.List;
import java.util.Map;

public interface EntityReader {
	public void read(String fileName);
	public List<EntityInfo> getEntityList();
	public Map<String, List<String>> getAppltMap();
}
