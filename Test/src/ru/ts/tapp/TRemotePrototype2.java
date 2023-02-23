package ru.ts.tapp;

import ru.ts.toykernel.storages.raster.RasterStorageIR;
import ru.ts.toykernel.storages.raster.ProjRasterStorageIR;
import ru.ts.toykernel.storages.raster.RasterStorage;

import ru.ts.toykernel.storages.mem.NodeStorageImpl;
import ru.ts.toykernel.storages.INodeStorage;
import ru.ts.toykernel.storages.providers.ProjProvider;
import ru.ts.toykernel.storages.providers.RasterServerProvider;
import ru.ts.toykernel.converters.*;
import ru.ts.toykernel.converters.providers.ServerProvider;
import ru.ts.toykernel.consts.KernelConst;
import ru.ts.toykernel.drawcomp.rules.def.SimpleRasterRule;
import ru.ts.toykernel.drawcomp.layers.def.DrawOnlyLayer;
import ru.ts.toykernel.drawcomp.ILayer;
import ru.ts.toykernel.filters.stream.NodeFilter;
import ru.ts.toykernel.attrs.def.DefAttrImpl;
import ru.ts.toykernel.attrs.def.DefaultAttrsImpl;
import ru.ts.toykernel.pcntxt.SimpleProjContext;
import ru.ts.toykernel.pcntxt.IProjContext;
import ru.ts.toykernel.gui.apps.SFViewer;
import ru.ts.toykernel.gui.IApplication;
import ru.ts.toykernel.gui.IViewControl;
import ru.ts.toykernel.plugins.IGuiModule;
import ru.ts.toykernel.gui.panels.ViewPicturePanel;
import ru.ts.toykernel.proj.xml.def.XMLProjBuilder;
import ru.ts.toykernel.app.xml.IXMLBuilderContext;
import ru.ts.gisutils.algs.common.MPoint;
import ru.ts.apps.sapp.app.InParamsApp;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.io.Reader;
import java.io.InputStreamReader;
import java.io.FileInputStream;

import org.xml.sax.InputSource;

/**
 * Тест связки сервер-клиентских конвертеров и сервер-клиентских растер провайдеров
 * теперь для проектов
  */
public class TRemotePrototype2
{
	public static void main(String[] args) throws Exception
	{
		InParamsApp params = new InParamsApp();
		params.translateOptions(args);


		//Загружаем проект
		String xmlfilepath=params.get(InParamsApp.optarr[InParamsApp.O_wfl]);

		SAXParser parser= SAXParserFactory.newInstance().newSAXParser();
		Reader rd=new InputStreamReader(new FileInputStream(xmlfilepath),"WINDOWS-1251");

		XMLProjBuilder builder = new XMLProjBuilder();
		parser.parse(new InputSource(rd), builder.getProjBuilderHandler(parser.getXMLReader()));

		IXMLBuilderContext bcontext = builder.getBuilderContext();


		List apps = bcontext.getBuilderByTagName(KernelConst.APPLICATION_TAGNAME).getLT();
		if (apps!=null && apps.size()>0)
		{
			IApplication app=((IApplication)apps.get(0));
			List<IProjContext> contexts=app.getIProjContexts();
			if (contexts!=null && contexts.size()>0)
			{
				IProjContext context=contexts.get(0);
				IViewControl view = app.getViewControl(context);
				IProjConverter originalconv = view.getViewPort().getCopyConverter();

				//получаем цепочку копий конвертеров лежащих в оригинальном конвертере
				List<ILinearConverter> convchain = originalconv.getConverterChain();

				//Создаем серверный конвертер который не включает в себя конвертер сдвига
				IRProjectConverter sconv=new ServerConverter((IRotateConverter)convchain.get(0),(IScaledConverter)convchain.get(1));

			    //создаем проектный конвертер, который состоит из тех же конвертеров плюс конвертер сдвига
				//Создаение копии связано с тем что цепочка конвертеров ворзвращается как копии конвертеров лежащих в исходном конвертере, а надо оригиналы здесь соединить
				IProjConverter projconv=new CrdConverterFactory.LinearConverterRSS(
						(IRotateConverter)convchain.get(0),(IScaledConverter)convchain.get(1),
						(new CrdConverterFactory.ShitConverter(new MPoint()))
				);

				//Создаем клиентский конвертер (скармливаем напрямую провайдер как точку разрыва)
				CliConverter cliconv = new CliConverter(((IShiftConverter)convchain.get(2)).getBindP0(), new ServerProvider(sconv));


				//создаем серверное хранилище для обычного растра
				RasterStorageIR rclistor;
				{
					RasterStorage.BindStruct bstr = new RasterStorage.BindStruct("C:\\MAPDIR\\Rasters\\R1_B\\bgdesc.txt",
							new MPoint[]{new MPoint(2033.0,2296.0), new MPoint(1960.0,2268.0)},
							new MPoint[]{new MPoint(-168950.6689787326,-4815978.916577781), new MPoint(-169751.36720358467,-4816302.005335176)},
							new double[]{-1, -1});
					bstr.loadDesc();
					List<RasterStorage.BindStruct> ll=new LinkedList<RasterStorage.BindStruct>();
					ll.add(bstr);
					RasterStorage rservstor = new RasterStorage(sconv,ll,null,"RASTER_0");
					//Клиентское хранилище для обычного растра
					rclistor = new RasterStorageIR("RASTER_0",new RasterServerProvider(rservstor,""));
				}


				//Создаем клиентское хранилище (скармливаем провайдер)
				RasterStorageIR pclistor = new ProjRasterStorageIR("RASTER_1",new ProjProvider(context,projconv,""));

				Map<String, INodeStorage> storages= new HashMap<String, INodeStorage>();
				storages.put(rclistor.getNodeId(),rclistor);
				storages.put(pclistor.getNodeId(),pclistor);
				NodeStorageImpl climainstor = new NodeStorageImpl("MAIN_STORAGE", storages, null, null);
				pclistor.setParentStorage(climainstor);

				List<ILayer> lrl = new LinkedList<ILayer>();
				{
					//Создаем правило
					SimpleRasterRule rr = new SimpleRasterRule();
					//Создаем фильтр
					NodeFilter nf=new NodeFilter(rclistor.getNodeId());
					//Создаем слой
					DefaultAttrsImpl attrs = new DefaultAttrsImpl();
					attrs.put(KernelConst.LAYER_VISIBLE,new DefAttrImpl(KernelConst.LAYER_VISIBLE,true));
					DrawOnlyLayer lr = new DrawOnlyLayer(climainstor, nf, attrs, rr);
					lrl.add(lr);
				}
				
				{
					//Создаем правило
					SimpleRasterRule rr = new SimpleRasterRule();
					//Создаем фильтр
					NodeFilter nf=new NodeFilter(pclistor.getNodeId());
					//Создаем слой
					DefaultAttrsImpl attrs = new DefaultAttrsImpl();
					attrs.put(KernelConst.LAYER_VISIBLE,new DefAttrImpl(KernelConst.LAYER_VISIBLE,true));
					DrawOnlyLayer lr = new DrawOnlyLayer(climainstor, nf, attrs, rr);
					lrl.add(lr);
				}

				SimpleProjContext projectctx = new SimpleProjContext(null, lrl, pclistor, null, null);

				LinkedList<IGuiModule> guiModules = new LinkedList<IGuiModule>();
				ViewPicturePanel picturePanel = new ViewPicturePanel(projectctx,cliconv,guiModules);
				SFViewer appForm = new SFViewer(picturePanel,guiModules);
				appForm.startApp(params,null);

			}


		}

	}
}