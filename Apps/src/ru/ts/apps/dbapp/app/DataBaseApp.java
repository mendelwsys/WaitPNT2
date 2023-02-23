package ru.ts.apps.dbapp.app;

import ru.ts.toykernel.gui.apps.SFViewer;
import ru.ts.toykernel.plugins.IGuiModule;
import ru.ts.toykernel.gui.panels.ViewPicturePanel;
import ru.ts.toykernel.proj.stream.def.StreamProjImpl;
import ru.ts.toykernel.drawcomp.rules.def.stream.CnStyleRuleFactory;
import ru.ts.toykernel.drawcomp.rules.def.stream.InscriptionRule;
import ru.ts.toykernel.drawcomp.IDrawObjRule;
import ru.ts.toykernel.storages.INodeStorage;
import ru.ts.toykernel.storages.IBaseStorage;
import ru.ts.toykernel.attrs.AObjAttrsFactory;
import ru.ts.toykernel.attrs.IAttrs;
import ru.ts.toykernel.plugins.defindrivers.DriverModule;
import ru.ts.apps.dbapp.db.DbAttrs;

import java.io.File;
import java.io.DataInputStream;
import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.util.Collection;
import java.util.LinkedList;

/**
 * Class demo for late binding of objects in data base
 */
public class DataBaseApp
{
	public static void main(String[] args) throws Exception
	{

		InParamsApp params = new InParamsApp();
		params.translateOptions(args);

		String binlayer = params.get(InParamsApp.optarr[InParamsApp.O_wfl]);

		StreamProjImpl proj = new StreamProjImpl(binlayer,new CnStyleRuleFactory()
		{
			public IDrawObjRule createByTypeName(String typeRule) throws Exception
			{
				if (typeRule.equals(InscriptionRule.RULETYPENAME))
					return new InscriptionRule();
				return super.createByTypeName(typeRule);
			}
		}, null, null,null, null, null,null);

		File fl = new File(binlayer);
		DataInputStream dis = new DataInputStream(new BufferedInputStream(new FileInputStream(fl)));
		proj.loadFromStream(dis);

//пример горячего связывания для текущей имплементации

		Collection<INodeStorage> storages = ((INodeStorage) (proj.getStorage())).getChildStorages();
		for (INodeStorage storage : storages)
		{
			storage.rebindByObjAttrsFactory(new AObjAttrsFactory()
			{

				public IAttrs createLocaleByGisObjId(String objId, IBaseStorage storage, IAttrs boundAttrs) throws Exception
				{
					//Связываем здесь по известным критериям, идентифкатор объекта начинается с LAYER_260581#$S, тогда заменяем входящие аттрибуты на DbAttrs
					//здесь же можно инстаницировать объект и произвести дополнительные запросы к БД учитывая загруженные аттрибуты
					if  (objId.startsWith("LAYER_260581#$S"))
						return new DbAttrs(boundAttrs,objId);
					else
					return boundAttrs;
				}
			});
		}


		LinkedList<IGuiModule> guiModules = new LinkedList<IGuiModule>();
		ViewPicturePanel picturePanel = new ViewPicturePanel(proj,proj.getConvInitializer(), guiModules);
		{
			guiModules.add(new DriverModule(picturePanel));
//			guiModules.add(new GisSearch(picturePanel));
//			guiModules.add(new DefDrawAttrEditor(picturePanel,null));
//			guiModules.add(new CurveViewer(picturePanel,null));
//			guiModules.add(new TstModule(picturePanel));
		}
		SFViewer appform = new SFViewer(picturePanel,guiModules);
		appform.startApp(params, null);
	}
}