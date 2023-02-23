package ru.ts.tapp;

import ru.ts.toykernel.storages.raster.RasterStorageIR;
import ru.ts.toykernel.storages.raster.RasterStorage;
import ru.ts.toykernel.storages.providers.RasterServerProvider;
import ru.ts.toykernel.storages.mem.NodeStorageImpl;
import ru.ts.toykernel.storages.INodeStorage;
import ru.ts.toykernel.converters.ServerConverter;
import ru.ts.toykernel.converters.IRProjectConverter;
import ru.ts.toykernel.converters.CrdConverterFactory;
import ru.ts.toykernel.converters.CliConverter;
import ru.ts.toykernel.converters.providers.ServerProvider;
import ru.ts.toykernel.consts.KernelConst;
import ru.ts.toykernel.drawcomp.rules.def.SimpleRasterRule;
import ru.ts.toykernel.drawcomp.layers.def.DrawOnlyLayer;
import ru.ts.toykernel.drawcomp.ILayer;
import ru.ts.toykernel.filters.stream.NodeFilter;
import ru.ts.toykernel.attrs.def.DefAttrImpl;
import ru.ts.toykernel.attrs.def.DefaultAttrsImpl;
import ru.ts.toykernel.pcntxt.SimpleProjContext;
import ru.ts.toykernel.gui.apps.SFViewer;
import ru.ts.toykernel.plugins.IGuiModule;
import ru.ts.toykernel.gui.panels.ViewPicturePanel;
import ru.ts.gisutils.algs.common.MPoint;
import ru.ts.apps.sapp.app.InParamsApp;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

/**
 * Тест связки сервер-клиентских конвертеров и сервер-клиентских растер провайдеров 
 *
 */
public class TRemotePrototype0
{
	public static void main(String[] args) throws Exception
	{
		InParamsApp params = new InParamsApp();
		params.translateOptions(args);

		//Создаем серверный конвертер
		IRProjectConverter sconv=new ServerConverter(new CrdConverterFactory.RotateConverter(new double[]{1,0,0,1}),new CrdConverterFactory.ScaledConverter(new MPoint(1,1)));
		//Создаем клиентский конвертер (скармливаем напрямую провайдер как точку разрыва)
		CliConverter cliconv = new CliConverter(new MPoint(), new ServerProvider(sconv));

		//создаем серверное хранилище
		RasterStorage.BindStruct bstr = new RasterStorage.BindStruct("C:\\MAPDIR\\Rasters\\R1_B\\bgdesc.txt", new MPoint[]{new MPoint(), new MPoint(1061, 1220)}, new MPoint[]{new MPoint(), new MPoint(1061, 1220)}, new double[]{-1, -1});
		bstr.loadDesc();
		List<RasterStorage.BindStruct> ll=new LinkedList<RasterStorage.BindStruct>();
		ll.add(bstr);
		RasterStorage servstor = new RasterStorage(sconv,ll,null,"RASTER_0");

		//Создаем клиентское хранилище (скармливаем провайдер)
		RasterStorageIR clistor = new RasterStorageIR("RASTER_0",new RasterServerProvider(servstor,""));
		Map<String, INodeStorage> storages= new HashMap<String, INodeStorage>();
		storages.put(clistor.getNodeId(),clistor);
		NodeStorageImpl climainstor = new NodeStorageImpl("MAIN_STORAGE", storages, null, null);
		clistor.setParentStorage(climainstor);

		//Создаем правило
		SimpleRasterRule rr = new SimpleRasterRule();
		//Создаем фильтр
		NodeFilter nf=new NodeFilter(clistor.getNodeId());
		//Создаем слой
		DefaultAttrsImpl attrs = new DefaultAttrsImpl();
		attrs.put(KernelConst.LAYER_VISIBLE,new DefAttrImpl(KernelConst.LAYER_VISIBLE,true));
		DrawOnlyLayer lr = new DrawOnlyLayer(climainstor, nf, attrs, rr);
		List<ILayer> lrl = new LinkedList<ILayer>();
		lrl.add(lr);
		SimpleProjContext projectctx = new SimpleProjContext(null, lrl, clistor, null, null);

		
		LinkedList<IGuiModule> guiModules = new LinkedList<IGuiModule>();
		ViewPicturePanel picturePanel = new ViewPicturePanel(projectctx,cliconv,guiModules);
		SFViewer appForm = new SFViewer(picturePanel,guiModules);
		appForm.startApp(params,null);
	}
}
