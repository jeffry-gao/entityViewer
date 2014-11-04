package common;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class Utility {

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
	public static String getDigits(FieldInfo efi){
		if (efi.dataType.equals("DATE")){
			return "14";
		} else if(efi.length.contains(".")){
			return efi.length.substring(0,efi.length.indexOf("."));
		}else{
			return efi.length;
		}
	}
	public static String getDigits2(FieldInfo efi){
		if (efi.dataType.equals("DATE")){
			return "14";
		} else if(efi.length.contains(".")){
			return efi.length.replace('.', ',');
		}else{
			return efi.length;
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
//					reader = new BufferedReader(new FileReader(appName));
					reader = new BufferedReader(new InputStreamReader(new FileInputStream(appName), "utf-8"));
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
//			BufferedWriter writer = new BufferedWriter((new FileWriter(appName)));
			BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(appName), "utf-8"));
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
//				reader = new BufferedReader(new FileReader(appName));
				reader = new BufferedReader(new InputStreamReader(new FileInputStream(appName), "utf-8"));
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

	static public Map<String,String> loadAllProperties(String appName){
		File f = new File(appName);
		if(f.exists()&&f.isFile()){
			Map<String,String> props = new HashMap<String, String>();
			try {
				BufferedReader reader;
//				reader = new BufferedReader(new FileReader(appName));
				reader = new BufferedReader(new InputStreamReader(new FileInputStream(appName), "utf-8"));
				String line=null;
				while((line=reader.readLine())!=null){
					String[] settingLine = line.split("=");
					if(settingLine.length>1){
						props.put(settingLine[0], settingLine[1]);
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

	public static boolean isWhoColumn(FieldInfo efi) {
		return efi.fieldName.equals("REG_PG_ID") ||
				efi.fieldName.equals("REG_USR_ID") ||
				efi.fieldName.equals("REG_DTTM") ||
				efi.fieldName.equals("UPDT_PG_ID") ||
				efi.fieldName.equals("UPDT_USR_ID") ||
				efi.fieldName.equals("UPDT_DTTM") ||
				efi.fieldName.equals("DEL_FLG");
	}

	public static String getStmtMethod(FieldInfo efi, boolean get) {
		String method="getString";
		if(efi.dataType.equals("NUMBER")){
			if(Double.valueOf(efi.length)>=10)
				method="getDouble";
			else
				method="getInt";
		}
		if(!get){
			method = "s" + method.substring(1);
		}
		return method;
	}

	static public void saveStringList(String appName, List<String> strList){
		try {
			BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(appName), "utf-8"));
			for(int i=0;i<strList.size();i++){
				writer.write(strList.get(i)+"\n");
			}
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	static public List<String> loadStringList(String appName){
		List<String> strList = null;
		try {
			strList = new ArrayList<String>();
			BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(appName), "utf-8"));
			String line=null;
			while((line=reader.readLine())!=null){
				strList.add(line);
			}
			reader.close();

		} catch (IOException e) {
			e.printStackTrace();
		}
		return strList;
	}

}
