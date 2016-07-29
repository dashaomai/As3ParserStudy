package com.layabox.parser.as3.vo;

import java.util.ArrayList;
import java.util.List;

/**
 * 解析出的类的元数据
 * @author Bob Jiang
 *
 */
public class ClassMeta implements IModifier {
	public String name;
	public EAccessSpecifier accessSpecifier;				// 访问控制符
	public Boolean isFinal;
	
	public List<VariableMeta> variables;
	public List<MethodMeta> methods;
	
	public ClassMeta() {
		name = null;
		accessSpecifier = EAccessSpecifier.PUBLIC;
		isFinal = false;
		
		variables = new ArrayList<VariableMeta>();
		methods = new ArrayList<MethodMeta>();
	}
	
	public String getName() throws NoSuchFieldException {
		if (null == name || null == accessSpecifier) {
			throw new NoSuchFieldException("属性不完整");
		}
		
		StringBuffer buffer = new StringBuffer();
		
		buffer.append(name);
		buffer.append(" [ label = \"{");
		buffer.append(name);
		
		if (variables.size() > 0) {
			buffer.append('|');
			
			for (int i = 0, m = variables.size(); i<m; i++) {
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
			
			for (int i = 0, m = methods.size(); i<m; i++) {
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
}
