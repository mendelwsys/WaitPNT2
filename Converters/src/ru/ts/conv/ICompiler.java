package ru.ts.conv;

import ru.ts.factory.IInitAble;
import ru.ts.toykernel.app.xml.IXMLBuilderContext;
import ru.ts.utils.data.InParams;

import java.io.BufferedReader;

/**
 * Интерфей компилятора входных файлов во внутренний формат
 */
public interface ICompiler extends IInitAble
{
	public void translate(IXMLBuilderContext bcontext, InParams inparams) throws Exception;
	
	void translate(IXMLBuilderContext bcontext, InParams inparams, BufferedReader br) throws Exception;
}
