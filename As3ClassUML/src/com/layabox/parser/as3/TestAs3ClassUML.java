package com.layabox.parser.as3;

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
    		System.out.println(EscapeEntities(clzMeta.getName()));
    	} catch (NoSuchFieldException ex) {
    		System.err.println(ex);
    	}
    }

    private int currentStructId;
    private Boolean inTheClass;

	@Override
	public void BeginVisit(StringBuffer buffer) {
        currentStructId = 0;
        inTheClass = false;

        buffer.setLength(0);
        buffer.append("digraph structs {\n");
        buffer.append("\tnode [shape=record];\n");
	}

	@Override
	public void EndVisit(StringBuffer buffer) {
        buffer.append("}\n");
	}

	@Override
	protected void VisitNode(IParserNode ast, StringBuffer buffer, int level, int parentStructId) {
        if (
            ast.is(NodeKind.BLOCK) ||
            ast.is(NodeKind.META_LIST) ||
            ast.is(NodeKind.INIT)
        )
            return;

        currentStructId++;

        final int myId = currentStructId;

        final Boolean touchClassNode = !inTheClass && ast.is(NodeKind.CLASS);

        if (touchClassNode) {
            inTheClass = true;
        } else if (inTheClass) {

            buffer.append("\t");
            buffer.append("struct");
            buffer.append(myId);
            buffer.append(" [label=\"");
            buffer.append(ast.getId());
            buffer.append("(");
            buffer.append(level);
            buffer.append(")");

            final String stringVal = ast.getStringValue();
            if (stringVal != null && !stringVal.equals("")) {
                buffer.append("\\n");
                buffer.append(EscapeEntities(stringVal));
            }

            buffer.append("\"];\n");

            if (0 != parentStructId) {
                buffer.append("\tstruct");
                buffer.append(parentStructId);
                buffer.append(" -> struct");
                buffer.append(myId);
                buffer.append(";\n");
            }

        }

        final int numChildren = ast.numChildren();
        if (numChildren > 0) {
            for (int i = 0; i<numChildren; i++) {
                VisitNode(ast.getChild(i), buffer, level + 1, myId);
            }
        }

        if (touchClassNode) {
            inTheClass = false;
        }
	}
}
