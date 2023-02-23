package ru.ts.apps.sapp.app;

import ru.ts.toykernel.gui.apps.SFViewer;
import ru.ts.toykernel.gui.panels.ViewPicturePanel;
import ru.ts.toykernel.plugins.IGuiModule;
import ru.ts.toykernel.proj.stream.def.StreamProjImpl;
import ru.ts.toykernel.drawcomp.rules.def.stream.CnStyleRuleFactory;
import ru.ts.toykernel.drawcomp.rules.def.stream.InscriptionRule;
import ru.ts.toykernel.drawcomp.IDrawObjRule;
import ru.ts.toykernel.plugins.defindrivers.DriverModule;
//import ru.ts.toykernel.plugins.opsvbproj.OpenSaveModule;
//import ru.ts.toykernel.plugins.gissearch.GisSearch;
//import ru.ts.toykernel.plugins.styles.DefDrawAttrEditor;
//import ru.ts.toykernel.plugins.cvviewer.CurveViewer;
//import ru.ts.toykernel.plugins.testmod.TstModule;
import ru.ts.forms.StViewProgress;

import java.io.File;
import java.io.DataInputStream;
import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.util.LinkedList;

/**
 * Класс стартует вьювер
 */
public class StartViewer
{
	public static void main(String[] args) throws Exception
	{

		InParamsApp params = new InParamsApp();
		params.translateOptions(args);

		String binlayer=params.get(InParamsApp.optarr[InParamsApp.O_wfl]);

		StViewProgress progress = new StViewProgress("Загрузка проекта");
		StreamProjImpl proj = new StreamProjImpl(binlayer,new CnStyleRuleFactory()
		 {
			 public IDrawObjRule createByTypeName(String typeRule) throws Exception
			 {
				 if (typeRule.equals(InscriptionRule.RULETYPENAME))
					 return new InscriptionRule();
				 return super.createByTypeName(typeRule);
			 }
		 },null,null,null,null,null, progress);

		File fl = new File(binlayer);
		DataInputStream dis = new DataInputStream(new BufferedInputStream(new FileInputStream(fl)));
		proj.loadFromStream(dis);

		LinkedList<IGuiModule> guiModules = new LinkedList<IGuiModule>();
		ViewPicturePanel picturePanel = new ViewPicturePanel(proj,proj.getConvInitializer(), guiModules);
		{
			guiModules.add(new DriverModule(picturePanel));
//			guiModules.add(new OpenSaveModule(picturePanel));
//			guiModules.add(new GisSearch(picturePanel));
//			guiModules.add(new DefDrawAttrEditor(picturePanel,null));
//			guiModules.add(new CurveViewer(picturePanel,null));
//			guiModules.add(new TstModule(picturePanel));
		}
		SFViewer checkFormRZD = new SFViewer(picturePanel,guiModules);
		checkFormRZD.startApp(params,progress);
	}
}
