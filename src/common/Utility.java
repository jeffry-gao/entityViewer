package common;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;


public class Utility {
	public static Map<String,IDLStructInfo> getStructsInMultiService(List<IDLServiceInfo> services,	Map<String,IDLStructInfo> structMaps) {
		Map<String,IDLStructInfo> usedStruct = new LinkedHashMap<String,IDLStructInfo>();
		for (int i = 0; i < services.size(); i++) {
			usedStruct.putAll(getStructsInService(services.get(i), structMaps));
		}
		return usedStruct;
	}

	public static Map<String,IDLStructInfo> getStructsInMultiService3(List<IDLServiceInfo> services,	Map<String,IDLStructInfo> structMaps, Map<String,Boolean> withListDef) {
		Map<String,IDLStructInfo> usedStruct = new LinkedHashMap<String,IDLStructInfo>();
		for (int i = 0; i < services.size(); i++) {
			usedStruct.putAll(getStructsInService3(services.get(i), structMaps, withListDef));
		}
		return usedStruct;
	}

	static Map<String,IDLStructInfo> getStructsInService(IDLServiceInfo service,	Map<String,IDLStructInfo> structMaps) {
		Map<String,IDLStructInfo> usedStruct = new LinkedHashMap<String,IDLStructInfo>();

		Map<String,IDLStructInfo> inReturnType = checkStructType(service.returnType,	structMaps);
		usedStruct.putAll(inReturnType);

		for (int j = 0; j < service.paramInfos.size(); j++) {
			String typeName = service.paramInfos.get(j).typeName;
			Map<String,IDLStructInfo> inParams = checkStructType(typeName, structMaps);
			usedStruct.putAll(inParams);
		}

		return usedStruct;
	}

	
	// TODO 
	// the same function as getStructsInService. But the list order is rearranged to existing tool,
	// which is parameter first, return type second.
	public static Map<String,IDLStructInfo> getStructsInService2(IDLServiceInfo service,	Map<String,IDLStructInfo> structMaps) {
		Map<String,IDLStructInfo> usedStruct = new LinkedHashMap<String,IDLStructInfo>();

		for (int j = 0; j < service.paramInfos.size(); j++) {
			String typeName = service.paramInfos.get(j).typeName;
			Map<String,IDLStructInfo> inParams = checkStructType(typeName, structMaps);
			usedStruct.putAll(inParams);
		}
		Map<String,IDLStructInfo> inReturnType = checkStructType(service.returnType,	structMaps);
		usedStruct.putAll(inReturnType);

		return usedStruct;
	}

	static Map<String,IDLStructInfo> getStructsInService3(IDLServiceInfo service,	Map<String,IDLStructInfo> structMaps, Map<String, Boolean> withListDef) {
		Map<String,IDLStructInfo> usedStruct = new LinkedHashMap<String,IDLStructInfo>();

		Map<String,IDLStructInfo> inReturnType = checkStructType3(service.returnType,	structMaps, withListDef);
		usedStruct.putAll(inReturnType);

		for (int j = 0; j < service.paramInfos.size(); j++) {
			String typeName = service.paramInfos.get(j).typeName;
			Map<String,IDLStructInfo> inParams = checkStructType3(typeName, structMaps, withListDef);
			usedStruct.putAll(inParams);
		}

		return usedStruct;
	}
	
	static Map<String,IDLStructInfo> checkStructType(String typeName, Map<String,IDLStructInfo> structMaps) {
		Map<String,IDLStructInfo> usedStruct = new LinkedHashMap<String,IDLStructInfo>();
		if(isBuiltInType(typeName)
				||typeName.startsWith("array_")
				||typeName.equals("square_string"))
			return usedStruct;
		else {
			IDLStructInfo si = typeName.endsWith("List")?
							structMaps.get(rmList(typeName)):structMaps.get(typeName);
			if(si!=null){
				usedStruct.put(si.structName,si);
			}
			if(si==null){
				System.err.println("no struct info found!("+typeName+")");
			} else {
				for(int i=0;i<si.listMember.size();i++){
					String[] temp = si.listMember.get(i).split("\t");
					if(isBuiltInType(temp[0])){
						continue;
					} else {
						usedStruct.putAll(checkStructType(temp[0],structMaps));
					}
				}
			}
		}
		return usedStruct;
	}

	private static Map<String,IDLStructInfo> checkStructType3(String typeName, Map<String,IDLStructInfo> structMaps, Map<String,Boolean> withListDef) {
		Map<String,IDLStructInfo> usedStruct = new LinkedHashMap<String,IDLStructInfo>();
		if(isBuiltInType(typeName)
				||typeName.startsWith("array_")
				||typeName.equals("square_string"))
			return usedStruct;
		else {
			IDLStructInfo si = null;
			if(typeName.endsWith("List")){
				si = structMaps.get(rmList(typeName));
				withListDef.put(si.structName, true);
			} else
				si = structMaps.get(typeName);

			if(si==null){
				System.err.println("no struct info found!("+typeName+")");
			} else {
				for(int i=0;i<si.listMember.size();i++){
					String[] temp = si.listMember.get(i).split("\t");
					if(isBuiltInType(temp[0])){
						continue;
					} else {
						usedStruct.putAll(checkStructType3(temp[0],structMaps,withListDef));
					}
				}
				usedStruct.put(si.structName,si);
			}
		}
		return usedStruct;
	}
	
	public static Map<String,IDLStructInfo> checkStructType4(String typeName, Map<String,IDLStructInfo> structMaps) {
		Map<String,IDLStructInfo> usedStruct = new LinkedHashMap<String,IDLStructInfo>();
		if(isBuiltInType(typeName)
				||typeName.startsWith("array_")
				||typeName.equals("square_string"))
			return usedStruct;
		else {
			IDLStructInfo si = typeName.endsWith("List")?
							structMaps.get(rmList(typeName)):structMaps.get(typeName);
			if(si!=null){
				usedStruct.put(si.structName,si);
			}
			if(si==null){
				System.err.println("no struct info found!("+typeName+")");
			} else {
				for(int i=0;i<si.listMember.size();i++){
					String[] temp = si.listMember.get(i).split("\t");
					if(isBuiltInType(temp[0])){
						continue;
					} else {
						IDLStructInfo sisub = temp[0].endsWith("List")?
								structMaps.get(rmList(temp[0])):structMaps.get(temp[0]);
						if(sisub!=null){
							usedStruct.put(sisub.structName,sisub);
						}
					}
				}
			}
		}
		return usedStruct;
	}
	public static boolean isBuiltInType(String typeName) {
		return  !typeName.endsWith("List")&&
				!typeName.endsWith("Info")&&
				!typeName.startsWith("array")&&
				!typeName.startsWith("square");
	}
	public static String rmList(String type) {
		return type.substring(0,type.length()-"List".length());
	}
	public static String convTypeNameIDL2MOS(String typeName){
		if(typeName.equals("string")){
			return "std::string";
		} else if (typeName.equals("array_string")){
			return "std::vector<std::string>";
		} else if (typeName.equals("array_long_long")){
			return "std::vector<long long>";
		}
		return typeName;
	}
	public static String getDigits(EntityFieldInfo efi){
		if (efi.m_dataType.equals("DATE")){
			return "14";
		} else if(efi.m_digits.contains(".")){
			return efi.m_digits.substring(0,efi.m_digits.indexOf("."));
		}else{
			return efi.m_digits;
		}
	}
	public static String getDigits2(EntityFieldInfo efi){
		if (efi.m_dataType.equals("DATE")){
			return "14";
		} else if(efi.m_digits.contains(".")){
			return efi.m_digits.replace('.', ',');
		}else{
			return efi.m_digits;
		}
	}
	public static String convPhysical2Camel(String physicalName) {
		String result = "";
		boolean capital = true;
		for(int i=0;i<physicalName.length();i++){
			if(physicalName.charAt(i)=='_'){
				capital = true;
			} else {
				if(capital)
					result += physicalName.substring(i,i+1);
				else
					result += physicalName.substring(i,i+1).toLowerCase();
				capital = false;
			}					
		}
		return result;
	}

	static public void saveProperty(String appName, Map<String,String> properties){
		try {
			Map<String,String> allProps = new HashMap<String,String>();
			File f = new File(appName);
			if(f.exists()&&f.isFile()){
				try {
					BufferedReader reader;
					reader = new BufferedReader(new FileReader(appName));
					String line=null;
					while((line=reader.readLine())!=null){
						String[] settingLine = line.split("=");
						if(settingLine.length>1){
							allProps.put(settingLine[0], settingLine[1]);
						}
					}
					reader.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			BufferedWriter writer = new BufferedWriter((new FileWriter(appName)));
			for(String key: properties.keySet()){
				allProps.put(key, properties.get(key));
			}
			for(String key: allProps.keySet()){
				writer.write(key+"="+allProps.get(key)+"\n");
			}
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	static public Map<String,String> loadProperties(String appName, List<String> keys){
		File f = new File(appName);
		if(f.exists()&&f.isFile()){
			Map<String,String> props = new HashMap<String, String>();
			try {
				BufferedReader reader;
				reader = new BufferedReader(new FileReader(appName));
				String line=null;
				while((line=reader.readLine())!=null){
					String[] settingLine = line.split("=");
					if(settingLine.length>1){
						for(int i=0;i<keys.size();i++){
							if(keys.get(i).equals(settingLine[0])){
								props.put(keys.get(i), settingLine[1]);
								break;
							}
						}
					}
				}
				reader.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			return props;
		}else
			return null;
	}
	public static boolean isWhoColumn(EntityFieldInfo efi) {
		return efi.m_physical.equals("REG_PG_ID") ||
				efi.m_physical.equals("REG_USR_ID") ||
				efi.m_physical.equals("REG_DTTM") ||
				efi.m_physical.equals("UPDT_PG_ID") ||
				efi.m_physical.equals("UPDT_USR_ID") ||
				efi.m_physical.equals("UPDT_DTTM") ||
				efi.m_physical.equals("DEL_FLG");
	}

	public static String getStmtMethod(EntityFieldInfo efi, boolean get) {
		String method="getString";
		if(efi.m_dataType.equals("NUMBER")){
			if(Double.valueOf(efi.m_digits)>=10)
				method="getDouble";
			else
				method="getInt";
		}
		if(!get){
			method = "s" + method.substring(1);
		}
		return method;
	}

}
