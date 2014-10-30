package common;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/*
 *
 * get data from oracle DB by SQL query as follows and save as txt
SELECT
A.TABLE_NAME
,C.COMMENTS
,B.COLUMN_NAME
,D.COMMENTS
,B.DATA_TYPE
,B.DATA_LENGTH
,B.NULLABLE
,EF.POSITION AS PK
FROM
DBA_TABLES A
,ALL_TAB_COLS B
,USER_TAB_COMMENTS C
,USER_COL_COMMENTS D
,(SELECT
      E.OWNER
      ,E.TABLE_NAME
      ,F.COLUMN_NAME
      ,F.POSITION
  FROM ALL_CONSTRAINTS E
      ,ALL_CONS_COLUMNS F
  WHERE E.OWNER=F.OWNER
  AND E.TABLE_NAME=F.TABLE_NAME
  AND E.CONSTRAINT_NAME=F.CONSTRAINT_NAME
  AND E.OWNER='KKNST2'
  AND E.CONSTRAINT_TYPE='P'
) EF
WHERE
A.OWNER=B.OWNER
AND A.TABLE_NAME=B.TABLE_NAME
AND A.TABLE_NAME=C.TABLE_NAME
AND A.TABLE_NAME=D.TABLE_NAME
AND B.TABLE_NAME=EF.TABLE_NAME(+)
AND B.COLUMN_NAME=D.COLUMN_NAME
AND B.COLUMN_NAME=EF.COLUMN_NAME(+)
AND A.OWNER='KKNST2'
ORDER BY A.TABLE_NAME,B.COLUMN_ID

 *
 */
public class EntityTxtReader implements EntityReader{

	private List<EntityInfo> listEntity;
	private Map<String, List<String>> mapApplt;

	public void read(String fileName){
		FileInputStream is = null;

		try {

			File infoFile = new File(fileName);
			if(!infoFile.exists())
				return;

			listEntity = new ArrayList<EntityInfo>();
			BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(fileName),"utf-8"));
			String line;
			EntityInfo workEntity=null;
			int fieldIndex = 1;
			while((line=reader.readLine())!=null){
				String[] splitRes = line.split("\t");
				if(workEntity==null || !workEntity.entityName.equals(splitRes[0])){
					if( workEntity!=null )
						listEntity.add(workEntity);
					workEntity = new EntityInfo();
					workEntity.entityName = splitRes[0];
					workEntity.entityDesc = splitRes[1];
					fieldIndex = 1;
				}
				FieldInfo workField = new FieldInfo();
				workField.no = fieldIndex++;
				workField.fieldName = splitRes[2];
				workField.fieldDesc = splitRes[3];
				workField.dataType = splitRes[4];
				workField.length = splitRes[5];
				if ( splitRes.length > 7 )
					workField.pkInfo = splitRes[7];
				workEntity.listFieldInfo.add(workField);
			}
			if( workEntity!=null )
				listEntity.add(workEntity);
			reader.close();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (is != null) {
				try {
					is.close();
				} catch (Exception e) {}
				is = null;
			}
		}
	}

	@Override
	public List<EntityInfo> getEntityList() {
		return listEntity;
	}

	@Override
	public Map<String, List<String>> getAppltMap() {
		return mapApplt;
	}
}