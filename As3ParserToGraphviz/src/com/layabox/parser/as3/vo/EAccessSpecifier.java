package com.layabox.parser.as3.vo;


public enum EAccessSpecifier {
	PUBLIC("public"),
	PROTECTED("protected"),
	PRIVATE("private");
	
	private String name;
	
	private EAccessSpecifier(final String nameToSet) {
		name = nameToSet;
	}
	
	@Override
	public String toString() {
		return name;
	}
}