package common;

import java.util.ArrayList;
import java.util.List;

public class IDLServiceInfo {
	public String serviceName;
	public String interfaceName;// home name
	public String moduleName;
	public String logicalName="unknown";
	public String returnType;
	public String returnLogicalName;
	public String returnRemark;
	public String remarkSummary="";
	public String remarkDetail="";
	public boolean oneway=false;
	public List<ParamInfo> paramInfos;
	public List<String> exceptions; //module name \t name \t remark
	public IDLServiceInfo() {
		paramInfos = new ArrayList<ParamInfo>();
		exceptions = new ArrayList<String>();
	}
}
