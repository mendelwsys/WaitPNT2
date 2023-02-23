package ru.ts.tapp;

import org.xml.sax.InputSource;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.Reader;
import java.io.InputStreamReader;
import java.io.FileInputStream;
import java.util.List;

import ru.ts.toykernel.proj.xml.def.XMLProjBuilder;
import ru.ts.toykernel.gui.IApplication;
import ru.ts.toykernel.app.xml.IXMLBuilderContext;
import ru.ts.toykernel.consts.KernelConst;
import ru.ts.forms.StViewProgress;
import ru.ts.apps.sapp.app.InParamsApp;
import su.mwlib.utils.Enc;

/**
 *  Простейшей построитель приложения из описателя.
 *  Производится инициализация всех дескрипторов, описанных в xml, во время их парсинга
 */
public class TBuilderProject0
{
	public static void main(String[] args) throws Exception
	{

		InParamsApp params = new InParamsApp();
		params.translateOptions(args);
		String lng=params.get(InParamsApp.O_lng);
		Enc.initEncoder(Enc.class,lng);

		String xmlfilepath=params.get(InParamsApp.optarr[InParamsApp.O_wfl]);

		SAXParser parser= SAXParserFactory.newInstance().newSAXParser();
		Reader rd=new InputStreamReader(new FileInputStream(xmlfilepath),"WINDOWS-1251");

		XMLProjBuilder builder = new XMLProjBuilder();
		parser.parse(new InputSource(rd), builder.getProjBuilderHandler(parser.getXMLReader()));

		IXMLBuilderContext bcontext = builder.getBuilderContext();


		List apps = bcontext.getBuilderByTagName(KernelConst.APPLICATION_TAGNAME).getLT();
		if (apps!=null && apps.size()>0)
		{
			((IApplication)apps.get(0)).startApp(params,
					new StViewProgress(Enc.get("LOAD_PROJECT")));
		}
	}

}