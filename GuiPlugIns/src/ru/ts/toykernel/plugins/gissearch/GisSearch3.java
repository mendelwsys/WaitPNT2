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
import ru.ts.toykernel.filters.*;
import ru.ts.toykernel.converters.IProjConverter;
import ru.ts.factory.IFactory;
import ru.ts.factory.IParam;

import java.awt.*;
import java.util.*;
import java.util.List;

/**
 * Search gis objects and move viewport to it
 */
public class GisSearch3 extends GisSearch
{
	protected String selectLayerName ="SELLAYER";
	protected String selectfiltername ="SELFILTER";

	public GisSearch3(IViewControl mainmodule) throws Exception
	{
		super(mainmodule);
	}
	public GisSearch3() throws Exception
	{
	}

	public void resetSelection()
	{

		try
		{
			ILayer selLayer;
			IKeySelFilter selfilter;
			if ((selLayer=getSelLayer())!=null && (selfilter = getSelFilter(selLayer))!=null)
			{
				selfilter.clearKeySet();
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
			CnStyleRuleImpl drawRule;
			ILayer selLayer = getSelLayer();
			IKeySelFilter selfilter;
			if (selLayer!=null)
			{
				if (selLayer.getDrawRule()==null)
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
					selLayer.setDrawRule(drawRule);
				}
				if ((selfilter = getSelFilter(selLayer))==null)
					throw new UnsupportedOperationException("No selection filter");
			}
			else
				throw new UnsupportedOperationException("No selection layer");


			selfilter.clearKeySet();

			Set<String> res = getObjectsIdByObjectName(currentName);
			for (String curveId : res)
			{
				if (res.size() == 1)
				{ //Установим тогда центральную точку
					IBaseGisObject giso = mainmodule.getProjContext().getStorage().getBaseGisByCurveId(curveId);
					IProjConverter converter = mainmodule.getViewPort().getCopyConverter();
					Point[] drwpnts = new DefGisOperations().getCentralDrawPoints(converter, giso);
					Point olddrwpnt = mainmodule.getViewPort().getDrawSize();
					converter.getAsShiftConverter().recalcBindPointByDrawDxDy(
							new double[]{drwpnts[0].x - olddrwpnt.x / 2, drwpnts[0].y - olddrwpnt.y / 2});
					mainmodule.getViewPort().setCopyConverter(converter);
				}
				selfilter.addKey2Set(curveId);
			}
			mainmodule.refresh(null);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	private IKeySelFilter getSelFilter(ILayer selLayer)
	{
		List<IBaseFilter> filters = selLayer.getFilters();
		for (IBaseFilter iBaseFilter : filters)
			if ((iBaseFilter instanceof IKeySelFilter) && iBaseFilter.getObjName().equalsIgnoreCase(selectfiltername))
				return ((IKeySelFilter) iBaseFilter);
		return null;
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
		else if (attr.getName().equalsIgnoreCase("SelectFilter"))
			this.selectfiltername = (String) attr.getValue();
		else
			super.init(obj);
		return null;
	}
}