package com.layabox.parser.as3;

import java.io.IOException;
import java.net.URISyntaxException;

import com.adobe.ac.pmd.parser.IAS3Parser;
import com.adobe.ac.pmd.parser.IParserNode;
import com.adobe.ac.pmd.parser.NodeKind;
import com.adobe.ac.pmd.parser.exceptions.TokenException;

import de.bokelberg.flex.parser.AS3Parser;

public class TestAs3Parser {

	public static void main(String[] args) {
		String filePath = null;
		
		if (args.length > 0) {
			filePath = args[0];
		} else {
			try {
				 filePath = ClassLoader.getSystemResource("test/examples/JPEGEncoder.as").toURI().getPath();
			} catch (URISyntaxException ex) {
				System.err.println(ex);
				return;
			}
		}
		
		if (null != filePath) {
			try {
				TestAs3Parser.Parse(filePath);
			} catch (Exception ex) {
				System.err.println(ex);
			}
		}
	
	}

    private static void Parse(final String filePath) throws URISyntaxException, IOException, TokenException {
        IAS3Parser parser = new AS3Parser();

        IParserNode root = parser.buildAst(filePath);
        StringBuffer buffer = new StringBuffer();

        BeginVisit(buffer);
        VisitNode(root, buffer, 1);
        EndVisit(buffer);

        System.out.println(buffer);
    }

    private static int currentStructId;

    private static void BeginVisit(final StringBuffer buffer) {
        currentStructId = 0;

        buffer.setLength(0);
        buffer.append("digraph structs {\n");
        buffer.append("\tnode [shape=record];\n");
    }

    private static void EndVisit(final StringBuffer buffer) {
        buffer.append("}\n");
    }

    private static void VisitNode(final IParserNode ast, final StringBuffer buffer, final int level) {
        VisitNode(ast, buffer, level, 0);
    }

    private static void VisitNode(final IParserNode ast, final StringBuffer buffer, final int level, final int parentStructId) {

        if (
            ast.is(NodeKind.BLOCK) ||
            ast.is(NodeKind.META_LIST) ||
            ast.is(NodeKind.INIT)
        )
            return;

        currentStructId++;

        final int myId = currentStructId;

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

        if (level < 777) {
            final int numChildren = ast.numChildren();
            if (numChildren > 0) {
                for (int i = 0; i<numChildren; i++) {
                    VisitNode(ast.getChild(i), buffer, level + 1, myId);
                }
            }
        }
    }

    private static String EscapeEntities( final String stringToEscape )
    {
        final StringBuffer buffer = new StringBuffer();

        for ( int i = 0; i < stringToEscape.length(); i++ )
        {
            final char currentCharacter = stringToEscape.charAt( i );

            if ( currentCharacter == '<' )
            {
                buffer.append( "&lt;" );
            }
            else if ( currentCharacter == '>' )
            {
                buffer.append( "&gt;" );
            }
            else
            {
                buffer.append( currentCharacter );
            }
        }
        return buffer.toString();
    }

}
