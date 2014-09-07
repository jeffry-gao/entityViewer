package common;

import java.util.ArrayList;
import java.util.List;

public class EntityInfo {
	public String m_id="";
	public String m_logical="";
	public String m_physical="";
	public boolean favorite=false;
	List<EntityFieldInfo> listFieldInfo;
	public EntityInfo(){
		listFieldInfo = new ArrayList<EntityFieldInfo>();
	}
	public void addField(EntityFieldInfo f){
		listFieldInfo.add(f);
	}
	public List<EntityFieldInfo> getFields() {
		return listFieldInfo;
	}
}
