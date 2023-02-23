package ru.ts.toykernel.plugins.gissearch;

import ru.ts.toykernel.gui.IViewControl;
import ru.ts.toykernel.consts.KernelConst;
import ru.ts.toykernel.geom.IBaseGisObject;
import ru.ts.toykernel.shared.DefGisOperations;
import ru.ts.toykernel.drawcomp.ILayer;
import ru.ts.toykernel.drawcomp.IParamPainter;
import ru.ts.toykernel.drawcomp.painters.def.DefPntImgPainter;
import ru.ts.toykernel.drawcomp.rules.def.CnStyleRuleImpl;
import ru.ts.toykernel.drawcomp.rules.def.CommonStyle;
import ru.ts.toykernel.drawcomp.rules.def.FilteredInterceptorRule;
import ru.ts.toykernel.filters.DefSelFilterByKeys;
import ru.ts.toykernel.converters.IProjConverter;
import ru.ts.factory.IFactory;
import ru.ts.factory.IParam;

import java.awt.*;
import java.util.*;
import java.util.List;

/**
 * Search gis objects and move viewport to it
 */
public class GisSearch2 extends GisSearch
{
	protected String selectLayerName ="SELLAYER";

	public GisSearch2(IViewControl mainmodule) throws Exception
	{
		super(mainmodule);
	}

	public GisSearch2() throws Exception
	{

	}
	public void resetSelection()
	{

		try
		{
			ILayer selLayer = getSelLayer();
			if (selLayer.getDrawRule().getInterceptor()!=null)
			{
				selLayer.getDrawRule().setInterceptor(null);
				mainmodule.refresh(null);
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		synchronized (this)
		{
			selectedGisObject = null;
			drawMetrics=null;
		}
	}
	public void setSelectByName(String currentName)
	{
		try
		{
			/**
			 * TODO Чем плох этот вариант? тем что  слой обращается к хранилищу даже тогда,
			 * когда отсутсвуют выбранные объекты, хотя это происходит достаточно быстро,
			 * но это происходит каждый раз при обращении к проекту. (Имеется ввиду накладные расходы на инкарнацию объектов)
			 *
			 * Поэтому надо продумать как наложить фильтр или их цепочки непосредственно на хранилище
			 * (а это кстати достаточно просто промежду прочим, надо создать хранилище которые ссылается на данное и отображает объекты согласно фильтру)
		    */

			CnStyleRuleImpl drawRule;
			ILayer selLayer = getSelLayer();
			if (selLayer!=null)
			{
					drawRule = new CnStyleRuleImpl(new CommonStyle(colorLine, colorFill), new Font(fontName, fontStyle, fontSize),attrasname);
					drawRule.setPointTextFactory(new IFactory<IParamPainter>()
					{
						public void addFactory(IFactory<IParamPainter> iParamPainterIFactory)
						{
							throw new UnsupportedOperationException("");
						}
						public IParamPainter createByTypeName(String typeObj) throws Exception
						{
							return new DefPntImgPainter(img_path, img_pict_path,getStorNmWithCodeNm(mainmodule.getProjContext()));
						}
					});
					selLayer.getDrawRule().setInterceptor(drawRule);
			}
			else
				throw new UnsupportedOperationException("No selection layer");


			Set<String> res = getObjectsIdByObjectName(currentName);
			DefSelFilterByKeys filterkey = new DefSelFilterByKeys();
			if (res.iterator().hasNext())
			{
				String curveId = res.iterator().next();
				IBaseGisObject giso = mainmodule.getProjContext().getStorage().getBaseGisByCurveId(curveId);
				IProjConverter converter = mainmodule.getViewPort().getCopyConverter();
				Point[] drwpnts = new DefGisOperations().getCentralDrawPoints(converter, giso);
				Point olddrwpnt = mainmodule.getViewPort().getDrawSize();
				((IProjConverter)(converter)).getAsShiftConverter().recalcBindPointByDrawDxDy(
						new double[]{drwpnts[0].x - olddrwpnt.x / 2, drwpnts[0].y - olddrwpnt.y / 2});

				filterkey.addKey2Set(giso.getCurveId());

				drawRule.setInterceptor(new FilteredInterceptorRule(filterkey, null));
				mainmodule.getViewPort().setCopyConverter(converter);
				mainmodule.refresh(null);
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	private ILayer getSelLayer()
			throws Exception
	{
		List<ILayer> layers = mainmodule.getProjContext().getLayerList();
		for (ILayer iLayer : layers)
			if (iLayer.getLrAttrs().get(KernelConst.LAYER_NAME).getValue().equals(selectLayerName))
				return iLayer;
		return null;
	}

	public Object init(Object obj) throws Exception
	{
		IParam attr=(IParam)obj;
		if (attr.getName().equalsIgnoreCase("SelectLayerName"))
			this.selectLayerName = (String) attr.getValue();
		else
			super.init(obj);
		return null;
	}
}