package ru.ts.toykernel.plugins.analitics;

import ru.ts.toykernel.plugins.IModule;
import ru.ts.toykernel.plugins.IAnswerBean;
import ru.ts.toykernel.plugins.ICommandBean;
import ru.ts.toykernel.plugins.AnswerBean;
import ru.ts.toykernel.factory.BaseInitAble;
import ru.ts.toykernel.consts.INameConverter;
import ru.ts.toykernel.consts.KernelConst;
import ru.ts.toykernel.drawcomp.ILayer;
import ru.ts.toykernel.attrs.IDefAttr;
import ru.ts.toykernel.storages.INodeStorage;
import ru.ts.toykernel.pcntxt.IProjContext;
import ru.ts.factory.IFactory;
import ru.ts.factory.IParam;

import java.util.List;

/**
 * Суперкаласс для того что бы от него наследовать как тонкий так и толстый клиент
 */
public class BaseAnalitModule extends BaseInitAble implements IModule
{
	public static final String ANALIT = "ANALIT";
	public static final String MODULENAME = "BASE_ANALIT";
	protected boolean fanalitcs=false;

	protected String attrasname = KernelConst.ATTR_CURVE_NAME;


	protected IProjContext projcontext; //Контекст проекта показа аналитики
	protected String showLayerName; //Имя слоя показа аналитики

	protected IReliefProvider reliefProvider;
	private ILayer showlayer;//Слоя для показа аналитики, он либо напрямую передается либо генерируется из проекта и имени слоя
	//(Если передаются оба, тогда проект и имя слоя более приорететны)

	public Object[] init(Object... objs) throws Exception
	{
		super.init(objs);
		if (reliefProvider!=null)
		{
			ILayer layer = getLayerByName(projcontext, showLayerName);
			((INodeStorage) layer.getStorage()).rebindByObjAttrsFactory(reliefProvider.getObjFactory());
		}
		return null;
	}

	protected ILayer getLayerByName(IProjContext projcontext,String layerName)
	{
		if (projcontext!=null)
		{
			List<ILayer> layers = projcontext.getLayerList();
			for (ILayer iLayer : layers)
				if (iLayer.getLrAttrs().get(KernelConst.LAYER_NAME).getValue().equals(layerName))
					return iLayer;
		}
		return showlayer;
	}


	public Object init(Object obj) throws Exception
	{
		IParam attr=(IParam)obj;
		if (attr.getName().equalsIgnoreCase(KernelConst.PROJCTXT_TAGNAME))
			this.projcontext = (IProjContext) attr.getValue();
		else if (attr.getName().equalsIgnoreCase(KernelConst.LAYER_TAGNAME))
			this.showlayer = (ILayer) attr.getValue();
		else if (attr.getName().equalsIgnoreCase("ShowLayer"))
			this.showLayerName = (String) attr.getValue();
		else if (attr.getName().equalsIgnoreCase(ModuleConst.RELIEF_TAGNAME))
			reliefProvider = (IReliefProvider) attr.getValue();
		else if (attr.getName().equalsIgnoreCase(KernelConst.USE_AS_ATTRIBUTENAME))
			this.attrasname = (String) attr.getValue();
		return null;
	}

	public String getModuleName()
	{
		return MODULENAME;
	}

	protected boolean performCommand(ICommandBean cmd)
	{
		if (cmd.getCommand().equals(ANALIT))
		{
			ILayer iLayer = getLayerByName(projcontext, showLayerName);
			IDefAttr rv = iLayer.getLrAttrs().get(KernelConst.LAYER_VISIBLE);
			Boolean isvis=(Boolean) rv.getValue();
			isvis = (isvis == null || !isvis);
			rv.setValue(isvis);
			return isvis;
		}
		return false;
	}

	public IAnswerBean execute(ICommandBean cmd) throws Exception
	{
		return new AnswerBean(cmd,"",new byte[]{(byte)(performCommand(cmd)?1:0)});
	}

	public void registerNameConverter(INameConverter nameConverter, IFactory<INameConverter> factory) throws Exception
	{
	}

	public void unload()
	{
	}
}
