package ru.ts.apps.bldapp.app;

import ru.ts.toykernel.gui.apps.SFViewer;
import ru.ts.toykernel.plugins.IGuiModule;
import ru.ts.toykernel.gui.panels.ViewPicturePanel;
import ru.ts.toykernel.proj.stream.def.StreamProjImpl;
import ru.ts.toykernel.drawcomp.rules.def.stream.CnStyleRuleFactory;
import ru.ts.toykernel.drawcomp.rules.def.stream.InscriptionRule;
import ru.ts.toykernel.drawcomp.IDrawObjRule;
import ru.ts.toykernel.plugins.defindrivers.DriverModule;
import ru.ts.apps.bldapp.rule.AssetRule;

import java.io.File;
import java.io.DataInputStream;
import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.util.LinkedList;

/**
 * Class for starting applications RZD assets
 */
public class StartBuilding
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
				System.out.println("typeRule = " + typeRule);
				if (typeRule.equals(InscriptionRule.RULETYPENAME))
					return new InscriptionRule();
				else if (typeRule.equals(AssetRule.RULETYPENAME))
					return new AssetRule();
				return super.createByTypeName(typeRule);
			}
		}, null,null, null, null, null,null);

		File fl = new File(binlayer);
		DataInputStream dis = new DataInputStream(new BufferedInputStream(new FileInputStream(fl)));
		proj.loadFromStream(dis);

		LinkedList<IGuiModule> guiModules = new LinkedList<IGuiModule>();
		ViewPicturePanel picturePanel = new ViewPicturePanel(proj,proj.getConvInitializer(), guiModules);
		{
			guiModules.add(new DriverModule(picturePanel));
//			guiModules.add(new GisSearch(picturePanel));
//			guiModules.add(new DefDrawAttrEditor(picturePanel,null));
//			guiModules.add(new CurveViewer(picturePanel,null));
//			guiModules.add(new TstModule(picturePanel));
		}
		SFViewer checkFormRZD = new SFViewer(picturePanel,guiModules);
		checkFormRZD.startApp(params,null);
	}
}