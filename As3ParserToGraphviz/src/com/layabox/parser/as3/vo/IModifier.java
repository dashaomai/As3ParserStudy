package com.layabox.parser.as3.vo;

public interface IModifier {
	public EAccessSpecifier getAccessSpecifier();
	public void setAccessSpecifier(EAccessSpecifier value);

	public Boolean getIsFinal();
	public void setIsFinal(Boolean value);
	
	public Boolean getIsStatic();
	public void setIsStatic(Boolean value);
	
	public Boolean getIsOverride();
	public void setIsOverride(Boolean value);
}
