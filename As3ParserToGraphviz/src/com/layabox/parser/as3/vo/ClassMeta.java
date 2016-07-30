package com.layabox.parser.as3.vo;

import java.util.ArrayList;
import java.util.List;

/**
 * 解析出的类的元数据
 * 
 * @author Bob Jiang
 *
 */
public class ClassMeta implements IModifier {
	public String name;
	public EAccessSpecifier accessSpecifier; // 访问控制符
	public Boolean isFinal;
	
	public String parentClassName;					// 扩展的父类名称

	public List<VariableMeta> variables;
	public List<MethodMeta> methods;
	public List<String> interfaces;					// 实现的接口

	public ClassMeta() {
		name = null;
		accessSpecifier = EAccessSpecifier.PUBLIC;
		isFinal = false;
		
		parentClassName = null;

		variables = new ArrayList<VariableMeta>();
		methods = new ArrayList<MethodMeta>();
		interfaces = new ArrayList<String>();
	}

	public String getName() throws NoSuchFieldException {
		if (null == name || null == accessSpecifier) {
			throw new NoSuchFieldException("属性不完整");
		}

		StringBuffer buffer = new StringBuffer();

		buffer.append(name);
		buffer.append(" [ label = \"{");
		buffer.append(name);
		
		if (null != parentClassName) {
			buffer.append(" extends ");
			buffer.append(parentClassName);
		}
		
		if (0 < interfaces.size()) {
			buffer.append(" implements ");
			for (int i = 0, m = interfaces.size(); i<m; i++) {
				buffer.append(interfaces.get(i));
				
				if (i < m - 1) {
					buffer.append(", ");
				}
			}
		}

		if (variables.size() > 0) {
			buffer.append('|');

			for (int i = 0, m = variables.size(); i < m; i++) {
				VariableMeta varMeta = variables.get(i);

				if (null == varMeta) {
					throw new NoSuchFieldException("变量列表内有空对象");
				}

				buffer.append(varMeta.getName());
				buffer.append("\\l");
			}
		}

		if (methods.size() > 0) {
			buffer.append('|');

			for (int i = 0, m = methods.size(); i < m; i++) {
				MethodMeta mthMeta = methods.get(i);

				if (null == mthMeta) {
					throw new NoSuchFieldException("函数列表内有空对象");
				}

				buffer.append(mthMeta.getName());
				buffer.append("\\l");
			}
		}

		buffer.append("}\" ];");

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
		return false;
	}

	@Override
	public void setIsStatic(Boolean value) {
		// do nothing;
	}

	@Override
	public Boolean getIsOverride() {
		return false;
	}

	@Override
	public void setIsOverride(Boolean value) {
		// do nothing
	}
}
