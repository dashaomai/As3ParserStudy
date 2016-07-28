package com.layabox.parser.as3;

import java.io.IOException;
import java.net.URISyntaxException;

import com.adobe.ac.pmd.parser.IAS3Parser;
import com.adobe.ac.pmd.parser.IParserNode;
import com.adobe.ac.pmd.parser.exceptions.TokenException;

import de.bokelberg.flex.parser.AS3Parser;

/**
 * Created by Bob Jiang on 2016/7/28.
 */
public abstract class BaseAs3Analyser implements IAs3Analyser {

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

    public void Parse(final String filePath) throws URISyntaxException, IOException, TokenException {
        IAS3Parser parser = new AS3Parser();

        IParserNode root = parser.buildAst(filePath);
        StringBuffer buffer = new StringBuffer();

        BeginVisit(buffer);
        VisitNode(root, buffer, 1);
        EndVisit(buffer);

        System.out.println(buffer);
    }

    protected abstract void VisitNode(final IParserNode ast, final StringBuffer buffer, final int level, final int parentStructId);

    public void VisitNode(final IParserNode ast, final StringBuffer buffer, final int level) {
        VisitNode(ast, buffer, level, 0);
    }
    
    protected String EscapeEntities( final String stringToEscape )
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
