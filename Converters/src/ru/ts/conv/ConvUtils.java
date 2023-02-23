package ru.ts.conv;

import ru.ts.toykernel.app.xml.IXMLBuilderContext;
import ru.ts.toykernel.proj.xml.def.XMLProjBuilder;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.SAXException;
import org.xml.sax.InputSource;

import java.io.IOException;
import java.io.FileInputStream;
import java.io.Reader;
import java.io.InputStreamReader;

/**
 * Created by IntelliJ IDEA.
 * User: vladm
 * Date: 04.07.2011
 * Time: 12:43:17
 * Some utils for convertor support
 */
public class ConvUtils
{
	/**
	 * Create descriptor context
	 * @param infile - input file
	 * @param encoding - file encoding
	 * @return -
	 * @throws ParserConfigurationException -
	 * @throws SAXException
	 * @throws IOException
	 */
	public static IXMLBuilderContext createXMLContext(String infile, String encoding)
			throws ParserConfigurationException, SAXException, IOException
	{
		Reader rd= null;
		try
		{
			FileInputStream webxmltemplate = new FileInputStream(infile); //Файл описателя тегов
			SAXParser parser= SAXParserFactory.newInstance().newSAXParser();
			rd=new InputStreamReader(webxmltemplate,encoding);

			XMLProjBuilder builder = new XMLProjBuilder(true);
			parser.parse(new InputSource(rd), builder.getProjBuilderHandler(parser.getXMLReader()));
			return builder.getBuilderContext();
		}
		finally
		{
			if (rd!=null)
				rd.close();
		}
	}

}
