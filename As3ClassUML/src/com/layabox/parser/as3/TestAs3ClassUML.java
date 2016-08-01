package com.layabox.parser.as3;

import java.util.ArrayList;
import java.util.List;

import com.adobe.ac.pmd.parser.IParserNode;
import com.adobe.ac.pmd.parser.NodeKind;
import com.layabox.parser.as3.vo.ClassMeta;
import com.layabox.parser.as3.vo.EAccessSpecifier;
import com.layabox.parser.as3.vo.EType;
import com.layabox.parser.as3.vo.MethodMeta;
import com.layabox.parser.as3.vo.TypeMeta;
import com.layabox.parser.as3.vo.VariableMeta;

/**
 * Created by Bob Jiang on 2016/7/28.
 */
public class TestAs3ClassUML extends BaseAs3Analyser {
	public static void main(String[] args) {
		if (args.length > 0)
			testVo();

		IAs3Analyser parser = new TestAs3ClassUML();
		parser.Process(args, "test/examples/Enum.as");
	}

	private static void testVo() {
		// 构造类
		ClassMeta clzMeta = new ClassMeta();

		clzMeta.name = "TestClass";
		clzMeta.accessSpecifier = EAccessSpecifier.PUBLIC;

		// 构造变量 1
		VariableMeta varMeta = new VariableMeta();
		varMeta.name = "Var1";
		varMeta.accessSpecifier = EAccessSpecifier.PUBLIC;

		TypeMeta typMeta = new TypeMeta();
		typMeta.type = EType.VECTOR;

		TypeMeta typMeta2 = new TypeMeta();
		typMeta2.type = EType.INT;

		typMeta.vectorType = typMeta2;

		varMeta.type = typMeta;

		clzMeta.variables.add(varMeta);

		// 构造变量 2
		varMeta = new VariableMeta();
		varMeta.name = "Var2";
		varMeta.accessSpecifier = EAccessSpecifier.PRIVATE;

		typMeta = new TypeMeta();
		typMeta.type = EType.REGEXP;

		varMeta.type = typMeta;

		clzMeta.variables.add(varMeta);

		// 构造变量 3
		varMeta = new VariableMeta();
		varMeta.name = "Var3";
		varMeta.accessSpecifier = EAccessSpecifier.PROTECTED;

		typMeta = new TypeMeta();
		typMeta.type = EType.OTHER;
		typMeta.otherName = "ConInfoCustom";

		varMeta.type = typMeta;

		clzMeta.variables.add(varMeta);

		// 构造方法 1
		MethodMeta mthMeta = new MethodMeta();
		mthMeta.name = "Method1";
		mthMeta.accessSpecifier = EAccessSpecifier.PUBLIC;

		typMeta = new TypeMeta();
		typMeta.type = EType.VOID;

		mthMeta.type = typMeta;

		varMeta = new VariableMeta();
		varMeta.name = "Parameter1";

		typMeta = new TypeMeta();
		typMeta.type = EType.UINT;

		varMeta.type = typMeta;

		mthMeta.parameters.add(varMeta);

		varMeta = new VariableMeta();
		varMeta.name = "Parameter2";

		typMeta = new TypeMeta();
		typMeta.type = EType.STRING;

		varMeta.type = typMeta;

		mthMeta.parameters.add(varMeta);

		clzMeta.methods.add(mthMeta);

		try {
			System.out.println("digraph Clz {");
			System.out.println("\tnode [shape=\"record\",fontname=\"Courier New\",fontsize=9];");
			System.out.print('\t');
			System.out.println(EscapeEntities(clzMeta.getName()));
			System.out.println("}");
		} catch (NoSuchFieldException ex) {
			System.err.println(ex);
		}
	}

	private int currentStructId;

	private List<ClassMeta> clazzes;
	private List<ClassMeta> openClazzes;

	@Override
	public void Process(final String[] args, final String defaultPath) {
		super.Process(args, defaultPath);

		StringBuffer buffer = new StringBuffer();
		buffer.append("digraph structs {\n");
		buffer.append("\tnode [shape=record,fontname=\"Courier New\",fontsize=9];\n");

		try {
			for (int i = 0, m = clazzes.size(); i < m; i++) {
				ClassMeta clzMeta = clazzes.get(i);

				buffer.append('\t');
				buffer.append(EscapeEntities(clzMeta.getName()));
				buffer.append('\n');
			}
		} catch (NoSuchFieldException ex) {
			System.err.println(ex);
		} catch (NullPointerException ex) {
			System.err.println(ex);
		}

		buffer.append("}");

		System.out.println(buffer);
	}

	@Override
	public void BeginVisit(StringBuffer buffer) {
		currentStructId = 0;

		if (null == clazzes) {
			clazzes = new ArrayList<ClassMeta>();
			openClazzes = new ArrayList<ClassMeta>();
		} else {
			clazzes.clear();
			openClazzes.clear();
		}

		buffer.setLength(0);
		buffer.append("digraph structs {\n");
		buffer.append("\tnode [shape=record,fontname=\"Courier New\",fontsize=9];\n");
	}

	@Override
	public void EndVisit(StringBuffer buffer) {
		buffer.append('}');
	}

	@Override
	protected void VisitNode(IParserNode ast, StringBuffer buffer, int level, int parentStructId) {
		if (ast.is(NodeKind.BLOCK) || ast.is(NodeKind.META_LIST) || ast.is(NodeKind.INIT))
			return;

		currentStructId++;

		final int myId = currentStructId;
		final Boolean touchClassNode = ast.is(NodeKind.CLASS);

		ClassMeta clzMeta;

		if (touchClassNode) {

			clzMeta = new ClassMeta();
			openClazzes.add(clzMeta);

			setupClassMetaByParserNode(clzMeta, ast, buffer, level, myId);

		} else if (openClazzes.size() > 0) {

			clzMeta = openClazzes.get(openClazzes.size() - 1);

			if (ast.is(NodeKind.VAR_LIST) || ast.is(NodeKind.CONST_LIST)) {
				VariableMeta varMeta = new VariableMeta();

				setupVariableList(varMeta, ast);

				clzMeta.variables.add(varMeta);
			} else if (ast.is(NodeKind.FUNCTION)) {
				MethodMeta mthMeta = new MethodMeta();
				
				setupFunction(mthMeta, ast);
				
				clzMeta.methods.add(mthMeta);
			}
		}

		if (touchClassNode) {
			clazzes.add(openClazzes.remove(openClazzes.size() - 1));
		} else if (0 == openClazzes.size()) {
			// 进入类以前的太初节点，需要遍历子节点
			for (int i = 0, m = ast.numChildren(); i < m; i++) {
				try {
				VisitNode(ast.getChild(i), buffer, level + 1, myId);
				} catch (Exception ex) {
					System.err.println(ex);
				}
			}
		}
	}
}
