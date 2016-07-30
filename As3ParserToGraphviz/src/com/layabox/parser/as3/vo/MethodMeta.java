package com.layabox.parser.as3.vo;

import java.util.ArrayList;
import java.util.List;

/**
 * 方法的元数据
 * 
 * @author Bob Jiang
 *
 */
public class MethodMeta implements IModifier {
	public String name;
	public EAccessSpecifier accessSpecifier;
	public Boolean isStatic;
	public Boolean isFinal;
	public Boolean isOverride;
	public TypeMeta type;

	public List<VariableMeta> parameters;

	public MethodMeta() {
		name = null;
		accessSpecifier = null;
		isStatic = false;
		isFinal = false;
		isOverride = false;
		type = null;
		parameters = new ArrayList<VariableMeta>();
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

		buffer.append(name);
		buffer.append('(');

		for (int i = 0, m = parameters.size(); i < m; i++) {
			VariableMeta varMeta = parameters.get(i);

			if (null == varMeta) {
				throw new NoSuchFieldException("参数列表内有空对象");
			}

			buffer.append(varMeta.getTinyName());

			if (i < m - 1) {
				buffer.append(", ");
			}
		}

		buffer.append("): ");
		buffer.append(type.getName());
		
		if (isStatic)
			buffer.append(" [Static]");

		return buffer.toString();
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
		return isOverride;
	}

	@Override
	public void setIsOverride(Boolean value) {
		isOverride = value;
	}
}
