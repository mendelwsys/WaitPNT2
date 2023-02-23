package ru.ts.tapp;

import ru.ts.toykernel.conf.ServerConfigProvider;
import ru.ts.toykernel.proj.StabConfigRovider;
import ru.ts.toykernel.proj.IConfigProvider;
import ru.ts.toykernel.proj.ICliConfigProvider;
import ru.ts.toykernel.proj.xml.def.XMLProjBuilder;
import ru.ts.toykernel.xml.IXMLBuilder;
import ru.ts.toykernel.consts.KernelConst;
import ru.ts.toykernel.app.xml.IXMLBuilderContext;
import ru.ts.toykernel.gui.IApplication;
import ru.ts.utils.data.Pair;
import ru.ts.forms.StViewProgress;

import java.io.FileInputStream;
import java.io.Reader;
import java.io.InputStreamReader;
import java.io.ByteArrayInputStream;
import java.util.List;

import org.xml.sax.InputSource;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

/**
 * финальное тестрование соединение с серверной частью
 */
public class TBuilderProject3
{

	public static void main(String[] args) throws Exception
	{
		InParamsApp params = new InParamsApp();
		params.translateOptions(args);

		//Серверная часть.
		String projName = "TestApp";
		ServerConfigProvider provider = new ServerConfigProvider(projName);
		provider.initProjects(new FileInputStream(params.get(InParamsApp.optarr[InParamsApp.O_wfl])), "WINDOWS-1251");

		//Клиентская часть I (TODO провайдер конфигурации загружается вов время первичной инициализации приложения)
		StabConfigRovider.setProvider(provider);//TODO Это уйдет, при инициализации мы будет отдавать адрес сервера
		IConfigProvider initconf = new StabConfigRovider();
		List<String> apps_info = initconf.getApplications("");//Запрос информации о приложения размещенных на сервере

		if (apps_info != null && apps_info.size() > 0) //TODO Здесь должен быть выбор одного приложения из списка
		{
			String app_info = apps_info.get(0);

			//Клиентская часть II (Запрос конфигурации у провайдера )
			Pair<Boolean, String> b2xml = initconf.getDescriptorByAppInfo(projName, app_info);//Получим строку описания клиентского приложения с сервера

			if (b2xml != null && b2xml.second != null && b2xml.second.length() > 0)
			{

				XMLProjBuilder builder = new XMLProjBuilder(true);
				{//Парсим клиентскую часть
					SAXParser parser = SAXParserFactory.newInstance().newSAXParser();
					Reader rd = new InputStreamReader(new ByteArrayInputStream(b2xml.second.getBytes("WINDOWS-1251")), "WINDOWS-1251");
					parser.parse(new InputSource(rd), builder.getProjBuilderHandler(parser.getXMLReader()));
				}

				IXMLBuilderContext bcontext = builder.getBuilderContext();
				IXMLBuilder<IApplication> appbuilder = bcontext.getBuilderByTagName(KernelConst.APPLICATION_TAGNAME);

				appbuilder.clearInitAbles();
				appbuilder.initByDescriptors();//строим клиентское приложение

				IXMLBuilder<ICliConfigProvider> confbuilder = bcontext.getBuilderByTagName(KernelConst.CONFPROVIDERS_TAGNAME);
				List<ICliConfigProvider> iCliConfigProviderList = confbuilder.getLT();
				for (ICliConfigProvider clisessionprovider : iCliConfigProviderList)
					clisessionprovider.openSession(projName,app_info);//Откроем сессии с сервером


				List<IApplication> bult_apps = appbuilder.getLT();//Запускаем клиентское приложение
				if (apps_info != null && apps_info.size() > 0)
				{
					bult_apps.get(0).startApp(params,
							new StViewProgress("Загрузка проекта"));
				}
			}
		}


	}
}