package common;

import java.util.ArrayList;
import java.util.List;

public class EntityInfo {
	public String m_id="";
	public String entityDesc="";
	public String entityName="";
	public boolean favorite=false;
	List<FieldInfo> listFieldInfo;
	public EntityInfo(){
		listFieldInfo = new ArrayList<FieldInfo>();
	}
	public void addField(FieldInfo f){
		listFieldInfo.add(f);
	}
	public List<FieldInfo> getFields() {
		return listFieldInfo;
	}
}
