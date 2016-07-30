package com.layabox.parser.as3;

import java.io.IOException;
import java.net.URISyntaxException;

import com.adobe.ac.pmd.parser.IAS3Parser;
import com.adobe.ac.pmd.parser.IParserNode;
import com.adobe.ac.pmd.parser.NodeKind;
import com.adobe.ac.pmd.parser.exceptions.TokenException;
import com.layabox.parser.as3.vo.ClassMeta;
import com.layabox.parser.as3.vo.EAccessSpecifier;
import com.layabox.parser.as3.vo.EType;
import com.layabox.parser.as3.vo.IModifier;
import com.layabox.parser.as3.vo.MethodMeta;
import com.layabox.parser.as3.vo.TypeMeta;
import com.layabox.parser.as3.vo.VariableMeta;

import de.bokelberg.flex.parser.AS3Parser;

/**
 * Created by Bob Jiang on 2016/7/28.
 */
public abstract class BaseAs3Analyser implements IAs3Analyser {

	@Override
	public void Process(final String[] args, final String defaultPath) {
		String filePath = null;

		if (args.length > 0) {
			filePath = args[0];
		} else {
			try {
				filePath = ClassLoader.getSystemResource(defaultPath).toURI().getPath();
			} catch (URISyntaxException ex) {
				System.err.println(ex);
				return;
			}
		}

		if (null != filePath) {
			try {
				Parse(filePath);
			} catch (Exception ex) {
				System.err.println(ex);
			}
		}
	}

	@Override
	public void Parse(final String filePath) throws URISyntaxException, IOException, TokenException {
		IAS3Parser parser = new AS3Parser();

		IParserNode root = parser.buildAst(filePath);
		StringBuffer buffer = new StringBuffer();

		BeginVisit(buffer);
		VisitNode(root, buffer, 1);
		EndVisit(buffer);

		System.out.println(buffer);
	}

	protected abstract void VisitNode(final IParserNode ast, final StringBuffer buffer, final int level,
			final int parentStructId);

	public void VisitNode(final IParserNode ast, final StringBuffer buffer, final int level) {
		VisitNode(ast, buffer, level, 0);
	}

	protected static String EscapeEntities(final String stringToEscape) {
		final StringBuffer buffer = new StringBuffer();

		for (int i = 0; i < stringToEscape.length(); i++) {
			final char currentCharacter = stringToEscape.charAt(i);

			if (currentCharacter == '<') {
				buffer.append("&lt;");
			} else if (currentCharacter == '>') {
				buffer.append("&gt;");
			} else {
				buffer.append(currentCharacter);
			}
		}
		return buffer.toString();
	}

	protected void setupClassMetaByParserNode(final ClassMeta clzMeta, final IParserNode ast, final StringBuffer buffer,
			final int level, final int myId) {
		if (!ast.is(NodeKind.CLASS))
			return;

		for (int i = 0, m = ast.numChildren(); i < m; i++) {
			IParserNode subAst = ast.getChild(i);

			if (subAst.is(NodeKind.NAME)) {
				clzMeta.name = subAst.getStringValue();
			} else if (subAst.is(NodeKind.MOD_LIST)) {
				setupModifierList(clzMeta, ast);
			} else if (subAst.is(NodeKind.CONTENT)) {
				for (int j = 0, n = subAst.numChildren(); j < n; j++) {
					VisitNode(subAst.getChild(j), buffer, level + 1, myId);
				}
			} else if (subAst.is(NodeKind.EXTENDS)) {
				clzMeta.parentClassName = subAst.getStringValue();
			} else if (subAst.is(NodeKind.IMPLEMENTS_LIST)) {
				for (final IParserNode subAst2 : subAst.getChildren()) {
					if (subAst2.is(NodeKind.IMPLEMENTS)) {
						clzMeta.interfaces.add(subAst2.getStringValue());
					}
				}
			}
		}
	}

	protected static void setupModifierList(final IModifier modListMeta, final IParserNode ast) {
		if (!ast.is(NodeKind.MOD_LIST))
			return;

		for (int i = 0, m = ast.numChildren(); i < m; i++) {
			IParserNode subAst = ast.getChild(i);

			if (subAst.is(NodeKind.MODIFIER)) {
				final String strVal = subAst.getStringValue();

				if (null == strVal) {
					continue;
				} else if (strVal.equals("public")) {
					modListMeta.setAccessSpecifier(EAccessSpecifier.PUBLIC);
				} else if (strVal.equals("protected")) {
					modListMeta.setAccessSpecifier(EAccessSpecifier.PROTECTED);
				} else if (strVal.equals("private")) {
					modListMeta.setAccessSpecifier(EAccessSpecifier.PRIVATE);
				} else if (strVal.equals("internal")) {
					modListMeta.setAccessSpecifier(EAccessSpecifier.INTERNAL);
				} else if (strVal.equals("final")) {
					modListMeta.setIsFinal(true);
				} else if (strVal.equals("static")) {
					modListMeta.setIsStatic(true);
				} else if (strVal.equals("override")) {
					modListMeta.setIsOverride(true);
				}
			}
		}
	}

	protected static void setupVariableList(final VariableMeta varMeta, final IParserNode ast) {
		varMeta.isConst = ast.is(NodeKind.CONST_LIST);
		
		if (!varMeta.isConst && !ast.is(NodeKind.VAR_LIST)) {
			return;
		}
		
		for (int i = 0, m = ast.numChildren(); i < m; i++) {
			IParserNode subAst = ast.getChild(i);

			if (subAst.is(NodeKind.MOD_LIST)) {
				setupModifierList(varMeta, subAst);
			} else if (subAst.is(NodeKind.NAME_TYPE_INIT)) {
				setupNameTypeInit(varMeta, subAst);
			}
		}
	}
	
	protected static void setupParameterList(final MethodMeta mthMeta, final IParserNode ast) {
		if (!ast.is(NodeKind.PARAMETER_LIST) || 0 == ast.numChildren())
			return;
		
		for (final IParserNode subAst : ast.getChildren()) {
			if (subAst.is(NodeKind.PARAMETER)) {
				for (final IParserNode subAst2 : subAst.getChildren()) {
					if (subAst2.is(NodeKind.NAME_TYPE_INIT)) {
						VariableMeta varMeta = new VariableMeta();
						
						setupNameTypeInit(varMeta, subAst2);
						
						mthMeta.parameters.add(varMeta);
					}
				}
			}
		}
	}
	
	protected static void setupFunction(final MethodMeta mthMeta, final IParserNode ast) {
		if (!ast.is(NodeKind.FUNCTION))
			return;
		
		for (int i = 0, m = ast.numChildren(); i<m; i++) {
			IParserNode subAst = ast.getChild(i);
			
			if (subAst.is(NodeKind.MOD_LIST)) {
				setupModifierList(mthMeta, subAst);
			} else if (subAst.is(NodeKind.PARAMETER_LIST)) {
				setupParameterList(mthMeta, subAst);
			} else if (subAst.is(NodeKind.NAME)) {
				mthMeta.name = subAst.getStringValue();
			} else if (subAst.is(NodeKind.TYPE)) {
				TypeMeta typMeta = new TypeMeta();
				
				setupType(typMeta, subAst);
				
				mthMeta.type = typMeta;
			} else if (subAst.is(NodeKind.VECTOR)) {
				TypeMeta typMeta = new TypeMeta();
				
				setupVector(typMeta, subAst);
				
				mthMeta.type = typMeta;
			}
		}
	}

	protected static void setupNameTypeInit(final VariableMeta varMeta, final IParserNode ast) {
		if (!ast.is(NodeKind.NAME_TYPE_INIT))
			return;

		for (int i = 0, m = ast.numChildren(); i < m; i++) {
			IParserNode subAst = ast.getChild(i);

			if (subAst.is(NodeKind.NAME)) {
				varMeta.name = subAst.getStringValue();
			} else if (subAst.is(NodeKind.TYPE)) {
				setupType(varMeta, subAst);
			} else if (subAst.is(NodeKind.VECTOR)) {
				setupVector(varMeta, subAst);
			}
		}
	}

	protected static void setupVector(final VariableMeta varMeta, final IParserNode ast) {
		// Vector 类型，只有一个子节点
		varMeta.type = new TypeMeta();
		
		setupVector(varMeta.type, ast);
	}
	
	protected static void setupVector(final TypeMeta typMeta, final IParserNode ast) {
		typMeta.type = EType.VECTOR;

		IParserNode subAst = ast.getChild(0);
		TypeMeta subVarMeta = new TypeMeta();
		typMeta.vectorType = subVarMeta;

		while (subAst.is(NodeKind.VECTOR)) {
			subVarMeta.type = EType.VECTOR;
			subVarMeta.vectorType = new TypeMeta();
			subVarMeta = subVarMeta.vectorType;

			subAst = subAst.getChild(0);
		}

		setupType(subVarMeta, subAst);
	}

	protected static void setupType(final VariableMeta varMeta, final IParserNode ast) {
		varMeta.type = new TypeMeta();
		setupType(varMeta.type, ast);
	}

	protected static void setupType(final TypeMeta typMeta, final IParserNode ast) {
		final String strVal = ast.getStringValue();

		if (null == strVal) {
			return;
		} else if (strVal.equals("int")) {
			typMeta.type = EType.INT;
		} else if (strVal.equals("uint")) {
			typMeta.type = EType.UINT;
		} else if (strVal.equals("Number")) {
			typMeta.type = EType.NUMBER;
		} else if (strVal.equals("void")) {
			typMeta.type = EType.VOID;
		} else if (strVal.equals("String")) {
			typMeta.type = EType.STRING;
		} else if (strVal.equals("Boolean")) {
			typMeta.type = EType.BOOLEAN;
		} else if (strVal.equals("Null")) {
			typMeta.type = EType.NULL;
		} else if (strVal.equals("Object")) {
			typMeta.type = EType.OBJECT;
		} else if (strVal.equals("*")) {
			typMeta.type = EType.OBJECT;
		} else if (strVal.equals("Array")) {
			typMeta.type = EType.ARRAY;
		} else if (strVal.equals("Date")) {
			typMeta.type = EType.DATE;
		} else if (strVal.equals("Error")) {
			typMeta.type = EType.ERROR;
		} else if (strVal.equals("Function")) {
			typMeta.type = EType.FUNCTION;
		} else if (strVal.equals("RegExp")) {
			typMeta.type = EType.REGEXP;
		} else if (strVal.equals("XML")) {
			typMeta.type = EType.XML;
		} else if (strVal.equals("XMLList")) {
			typMeta.type = EType.XMLLIST;
		} else if (strVal.equals("Class")) {
			typMeta.type = EType.CLASS;
		} else {
			typMeta.type = EType.OTHER;
			typMeta.otherName = strVal;
		}
	}
}
