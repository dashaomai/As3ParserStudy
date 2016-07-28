package com.layabox.parser.as3;

import com.adobe.ac.pmd.parser.IParserNode;
import com.adobe.ac.pmd.parser.NodeKind;

public class TestAs3Analyser extends BaseAs3Analyser {

	public static void main(String[] args) {
		IAs3Analyser parser = new TestAs3Analyser();
        parser.Process(args, "test/examples/JPEGEncoder.as");
	}

    private int currentStructId;
    private Boolean inTheClass;

    public void BeginVisit(final StringBuffer buffer) {
        currentStructId = 0;
        inTheClass = false;

        buffer.setLength(0);
        buffer.append("digraph structs {\n");
        buffer.append("\tnode [shape=record];\n");
    }

    public void EndVisit(final StringBuffer buffer) {
        buffer.append("}\n");
    }
	
	@Override
    protected void VisitNode(final IParserNode ast, final StringBuffer buffer, final int level, final int parentStructId) {

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

        if (level < 777) {
            final int numChildren = ast.numChildren();
            if (numChildren > 0) {
                for (int i = 0; i<numChildren; i++) {
                    VisitNode(ast.getChild(i), buffer, level + 1, myId);
                }
            }
        }

        if (touchClassNode) {
            inTheClass = false;
        }
    }


}
