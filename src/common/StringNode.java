package common;

import java.util.ArrayList;
import java.util.List;

public class StringNode {
	public String name;
	public String type;
	public String data;
	public List<StringNode> children;
	public StringNode(String name, String type, String data){
		this.name=name;
		this.type=type;
		this.data=data;
		children = null;
	}
	public StringNode add(String name, String type, String data){
		if(children==null)
			children = new ArrayList<StringNode>();
		StringNode newChild = new StringNode(name,type,data);
		children.add(newChild);
		return newChild;
	}
	public void add(StringNode childNode){
		if(children==null)
			children = new ArrayList<StringNode>();
		children.add(childNode);
	}
}
