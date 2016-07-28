package com.layabox.parser.as3.vo;

/**
 * AS3 内的数据类型
 * @author Bob Jiang
 *
 */
public enum EType {
	// 原语类型 Primitive Value
	VOID("void"),
	STRING("String"),
	NUMBER("Number"),
	INT("int"),
	UINT("uint"),
	BOOLEAN("Boolean"),
	NULL("Null"),
	
	// 复合类型 Complex Value
	OBJECT("Object"),
	ARRAY("Array"),
	DATE("Date"),
	ERROR("Error"),
	FUNCTION("Function"),
	REGEXP("RegExp"),
	XML("XML"),
	XMLLIST("XMLList"),
	
	// 未列出的类型
	VECTOR("Vector"),
	
	// 特殊的类型
	OTHER("Other");
	
	private String name;
	
	private EType(final String nameToSet) {
		name = nameToSet;
	}
	
	@Override
	public String toString() {
		return name;
	}
}
