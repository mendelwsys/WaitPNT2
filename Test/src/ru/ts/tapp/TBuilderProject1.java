package ru.ts.tapp;

import org.xml.sax.InputSource;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.Reader;
import java.io.InputStreamReader;
import java.io.FileInputStream;
import java.io.ByteArrayInputStream;
import java.util.List;

import ru.ts.toykernel.proj.xml.def.XMLProjBuilder;
import ru.ts.toykernel.gui.IApplication;
import ru.ts.toykernel.app.xml.IXMLBuilderContext;
import ru.ts.toykernel.consts.KernelConst;
import ru.ts.toykernel.xml.IXMLBuilder;
import ru.ts.xml.IXMLObjectDesc;
import ru.ts.forms.StViewProgress;
import ru.ts.utils.data.Pair;
import ru.ts.apps.sapp.app.InParamsApp;

/**
 * Отладка отложенной сборки приложения и сборки XML клиента
 *
 * Ближайшая стратегическая цель выход на спецификацию плагинов приложения
 */
public class TBuilderProject1
{
	public static void main(String[] args) throws Exception
	{
		Pair<Boolean, String> b2xml = null;
		InParamsApp params = new InParamsApp();
		params.translateOptions(args);

		{

		String xmlfilepath=params.get(InParamsApp.optarr[InParamsApp.O_wfl]);

		SAXParser parser= SAXParserFactory.newInstance().newSAXParser();
		Reader rd=new InputStreamReader(new FileInputStream(xmlfilepath),"WINDOWS-1251");

		XMLProjBuilder builder = new XMLProjBuilder(true);
		parser.parse(new InputSource(rd), builder.getProjBuilderHandler(parser.getXMLReader()));

		IXMLBuilderContext bcontext = builder.getBuilderContext();


			IXMLBuilder appbuilder = bcontext.getBuilderByTagName(KernelConst.APPLICATION_TAGNAME);

			List<IXMLObjectDesc> paramsdesc = appbuilder.getParamDescs();
			IXMLObjectDesc param = paramsdesc.get(0);
			b2xml = bcontext.getFullXML(param); //Генерируем XML заново
		}

		{
			SAXParser parser= SAXParserFactory.newInstance().newSAXParser();
			Reader rd=new InputStreamReader(new ByteArrayInputStream(b2xml.second.getBytes("WINDOWS-1251")),"WINDOWS-1251");

			XMLProjBuilder builder = new XMLProjBuilder(b2xml.first);
			parser.parse(new InputSource(rd), builder.getProjBuilderHandler(parser.getXMLReader()));

			IXMLBuilderContext bcontext = builder.getBuilderContext();


			IXMLBuilder byTagName = bcontext.getBuilderByTagName(KernelConst.APPLICATION_TAGNAME);

			byTagName.clearInitAbles();
			byTagName.initByDescriptors();

			List apps = byTagName.getLT();
			if (apps!=null && apps.size()>0)
			{
			((IApplication)apps.get(0)).startApp(params,
					new StViewProgress("Загрузка проекта"));
			}
		}
	}

}