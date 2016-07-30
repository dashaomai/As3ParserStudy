package com.layabox.parser.as3.vo;

/**
 * 变量元数据
 * 
 * @author Bob Jiang
 *
 */
public class VariableMeta implements IModifier {
	public String name;
	public EAccessSpecifier accessSpecifier;
	public Boolean isStatic;
	public Boolean isFinal;
	public Boolean isConst;
	public TypeMeta type;

	public VariableMeta() {
		name = null;
		accessSpecifier = null;
		isStatic = false;
		isFinal = false;
		isConst = false;
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
		
		if (isConst)
			buffer.append(" [Const]");
		
		if (isStatic)
			buffer.append(" [Static]");

		return buffer.toString();
	}

	public String getTinyName() throws NoSuchFieldException {
		if (null == name || null == type) {
			throw new NoSuchFieldException("属性不完整");
		}

		return name + ": " + type.getName();
	}

	@Override
	public EAccessSpecifier getAccessSpecifier() {
		return accessSpecifier;
	}

	@Override
	public void setAccessSpecifier(EAccessSpecifier value) {
		accessSpecifier = value;
	}

	@Override
	public Boolean getIsFinal() {
		return isFinal;
	}

	@Override
	public void setIsFinal(Boolean value) {
		isFinal = value;
	}

	@Override
	public Boolean getIsStatic() {
		return isStatic;
	}

	@Override
	public void setIsStatic(Boolean value) {
		isStatic = value;
	}

	@Override
	public Boolean getIsOverride() {
		return false;
	}

	@Override
	public void setIsOverride(Boolean value) {
		// do nothing;
	}
}
