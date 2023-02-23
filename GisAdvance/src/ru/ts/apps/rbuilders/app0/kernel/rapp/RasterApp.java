package ru.ts.apps.rbuilders.app0.kernel.rapp;

import ru.ts.toykernel.factory.BaseInitAble;
import ru.ts.toykernel.attrs.IDefAttr;
import ru.ts.toykernel.consts.KernelConst;
import ru.ts.toykernel.converters.IProjConverter;
import ru.ts.toykernel.pcntxt.IProjContext;
import ru.ts.toykernel.drawcomp.ILayer;
import ru.ts.gisutils.algs.common.MRect;

/**
 * Класс связывает проектный контекст с системой рисования
 * ru.ts.apps.rbuilders.app0.kernel.rapp.RasterApp
 */
public class RasterApp extends BaseInitAble
{

	protected IProjConverter converter;
	protected IProjContext projectctx;
	protected MRect wholerect;

	public IProjConverter getConverter()
	{
		return converter;
	}

	public IProjContext getProjectctx()
	{
		return projectctx;
	}

	public MRect getWholerect()
	{
		return wholerect;
	}

	void initRect() throws Exception
	{
	java.util.List<ILayer> layers = projectctx.getLayerList();
	if (layers.size() != 0)
		wholerect = null;
	for (ILayer layer : layers)
		wholerect = layer.getMBBLayer(wholerect);
	}

	public Object[] init(Object... objs) throws Exception
	{
		super.init(objs);
		initRect();
		return null;
	}

	public Object init(Object obj) throws Exception
	{
		IDefAttr attr=(IDefAttr)obj;
		if (attr.getName().equalsIgnoreCase(KernelConst.CONVERTER_TAGNAME))
			converter=(IProjConverter)attr.getValue();
		else if (attr.getName().equalsIgnoreCase(KernelConst.PROJCTXT_TAGNAME))
			projectctx=(IProjContext)attr.getValue();
		return null;
	}
}
