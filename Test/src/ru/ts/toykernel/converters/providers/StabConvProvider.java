package ru.ts.toykernel.converters.providers;

import ru.ts.toykernel.factory.BaseInitAble;
import ru.ts.toykernel.proj.ICliConfigProvider;
import ru.ts.toykernel.consts.KernelConst;
import ru.ts.toykernel.attrs.IDefAttr;
import ru.ts.toykernel.converters.IScaleDescBean;
import ru.ts.gisutils.algs.common.MPoint;

/**
 * Заглушка для серверного провайдра
 * ru.ts.toykernel.converters.providers.StabConvProvider
 */
public class StabConvProvider extends BaseInitAble implements IConvProvider
{
	private ICliConfigProvider conf_provider;

	public Object init(Object obj) throws Exception
	{
		IDefAttr attr=(IDefAttr)obj;
		if (attr.getName().equalsIgnoreCase(KernelConst.CONFPROVIDERS_TAGNAME))
				conf_provider=(ICliConfigProvider)attr.getValue();
		return null;
	}

	public MPoint multScale(double mscale) throws Exception
	{
		//return StabConfigRovider.getProvider().getServBySession(conf_provider.getSession()).getConverterProvider().multScale(mscale);
		return null;
	}

	public MPoint multScale(MPoint mscale, boolean evently) throws Exception
	{
		return null; 
	}

	public void setScaleRange(IScaleDescBean scalebean) throws Exception
	{
//		StabConfigRovider.getProvider().getServBySession(conf_provider.getSession()).getConverterProvider().setScaleRange(scalebean);
		
	}

}
