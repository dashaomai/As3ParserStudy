package com.layabox.parser.as3.vo;

import java.util.ArrayList;
import java.util.List;

/**
 * 方法的元数据
 * @author Bob Jiang
 *
 */
public class MethodMeta {
	public String name;
	public EAccessSpecifier accessSpecifier;
	public TypeMeta type;
	
	public List<VariableMeta> parameters;
	
	public MethodMeta() {
		name = null;
		accessSpecifier = null;
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
		
		for (int i = 0, m = parameters.size(); i<m; i++) {
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
		
		return buffer.toString();
	}
}
