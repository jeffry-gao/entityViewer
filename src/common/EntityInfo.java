package common;

import java.util.ArrayList;
import java.util.List;

public class EntityInfo {
	public String entityID=""; //種別＋連番とか、あまり使わない
	public String entityNameJP=""; //日本語名
	public String entityName=""; //英語名
	public String defineFile = "";
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
