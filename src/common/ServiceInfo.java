package common;

import java.util.ArrayList;
import java.util.List;

public class ServiceInfo {
	public String 	  serviceName;
	public String    homeName;
	public String    moduleName;
	public ParamInfo returnInfo;
	public List<ParamInfo> paramInfos;
	public ServiceInfo() {
		paramInfos = new ArrayList<ParamInfo>();
		returnInfo = new ParamInfo();
		returnInfo.paramInOut = 0;
		returnInfo.paraName = "";
	}
}
