package common;

import java.io.DataInputStream;
import java.util.ArrayList;
import java.util.List;
import giopUtility.BdyViewer;

public class IDLStructInfo {
	public String 		 structName;
	public boolean		isList;
	public List<String> listMember; // [type]+'\t'+[physical name]+'\t'+[logical name]+'\t'+[remark]
	public IDLStructInfo(){
		listMember=new ArrayList<String>();
	}
	public void translateStruct(BdyViewer bv,DataInputStream dis, long[] offset, StringNode treeNodeCur) {
		int index = 0;
		StringNode structNode = new StringNode("","<"+structName+">","");
		treeNodeCur.add(structNode);
		while(index<listMember.size()){
			String[] splits = listMember.get(index).split("\t");
			System.out.println(structName+"."+splits[1]+"("+splits[0]+")");
			bv.translateParam(dis, offset, splits[0], splits[1],structNode);
			index++;
		}
	}
}
