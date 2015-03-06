package common;

import java.util.List;
import java.util.Map;

public interface EntityReader {
	public void read(String fileName, String commentFile);
	public void read(String fileName);
	public void setKeyword(String prefix);
	public List<EntityInfo> getEntityList();
	public Map<String, String> getCommentMap();
}
