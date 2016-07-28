package com.layabox.parser.as3.vo;

/**
 * 变量元数据
 * @author Bob Jiang
 *
 */
public class VariableMeta {
	public String name;
	public EAccessSpecifier accessSpecifier;
	public TypeMeta type;
	
	public VariableMeta() {
		name = null;
		accessSpecifier = null;
		type = null;
	}
	
	public String getName() throws NoSuchFieldException {
		if (null == name || null == accessSpecifier || null == type) {
			throw new NoSuchFieldException("属性不完整");
		}
		
		StringBuffer buffer = new StringBuffer();
		
		if (accessSpecifier.equals(EAccessSpecifier.PUBLIC)) {
			buffer.append("+ ");
		} else {
			buffer.append("- ");
		}
		
		buffer.append(getTinyName());
		
		return buffer.toString();
	}
	
	public String getTinyName() throws NoSuchFieldException {
		if (null == name || null == type) {
			throw new NoSuchFieldException("属性不完整");
		}
		
		return name + ": " + type.getName();
	}
}
