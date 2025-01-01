package me.eldodebug.jsmp.parser;

import java.io.File;

public abstract class JsmpParser {
	
	protected final File file;
	
	public JsmpParser(File file) {
		this.file = file;
	}
	
	public abstract JsmpResult onParse();
}
