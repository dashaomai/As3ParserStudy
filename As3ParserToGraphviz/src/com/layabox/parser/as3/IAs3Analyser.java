package com.layabox.parser.as3;

import java.io.IOException;
import java.net.URISyntaxException;

import com.adobe.ac.pmd.parser.IParserNode;
import com.adobe.ac.pmd.parser.exceptions.TokenException;

public interface IAs3Analyser {
	void Process(final String[] args, final String defaultPath);
	
	void Parse(final String filePath) throws URISyntaxException, IOException, TokenException;
	
	void BeginVisit(final StringBuffer buffer);
	void EndVisit(final StringBuffer buffer);
	
	void VisitNode(final IParserNode ast, final StringBuffer buffer, final int level);
}