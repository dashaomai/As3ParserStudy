package com.layabox.parser.as3.vo;

/**
 * 类型的元数据
 * 
 * @author Bob Jiang
 *
 */
public class TypeMeta {
	public EType type; // 首要类型
	public TypeMeta vectorType; // 当首要类型为 Vector 时，这里是次要类型
	public String otherName; // 当首要类型为 Other 时，这里是类型的文字表示

	public TypeMeta() {
		type = EType.VOID;
		vectorType = null;
		otherName = null;
	}

	public String getName() throws NoSuchFieldException {
		if (type.equals(EType.VECTOR)) {
			if (null == vectorType) {
				throw new NoSuchFieldException("Vector 类型没有指定正确的 vectorType");
			}

			return "Vector.<" + vectorType.getName() + ">";
		} else if (type.equals(EType.OTHER)) {
			if (null == otherName || otherName.equals("")) {
				throw new NoSuchFieldException("Other 类型没有指定正确的 otherName");
			}

			return otherName;
		}

		return type.toString();
	}
}
