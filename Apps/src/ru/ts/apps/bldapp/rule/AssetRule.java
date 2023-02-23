package ru.ts.apps.bldapp.rule;

import ru.ts.toykernel.drawcomp.rules.def.stream.CnSerialDrawRule;
import ru.ts.toykernel.drawcomp.ILayer;
import ru.ts.toykernel.drawcomp.IParamPainter;
import ru.ts.toykernel.drawcomp.painters.def.symbols.*;
import ru.ts.toykernel.consts.KernelConst;
import ru.ts.toykernel.attrs.IDefAttr;
import ru.ts.toykernel.filters.IBaseFilter;
import ru.ts.toykernel.geom.IBaseGisObject;

import java.util.Map;
import java.util.HashMap;
import java.awt.*;

/**
 * Rule for draw assets of railway roads
 * ru.ts.apps.bldapp.rule.AssetRule
 */
public class AssetRule extends CnSerialDrawRule
{
	public static final String RULETYPENAME ="ASSET_RL";


	public static final String VOK_2 = "Вокзал 2 класса";
	public static final String VOK = "Вокзал";
	public static final String BLD_PASS = "Здание пассажирское";
	public static final String PAV_PASS = "Павильон пассажирский";
	public static final String PAV_TICK = "Павильон билетно-кассовый";
	public static final String PAV_OTHER = "Павильон прочий";
	public static final String BLD_OTHER = "Прочее здание";

	static Map<String, IParamPainter> lrn2pict=new HashMap<String,IParamPainter>();
	private IBaseFilter filter;

	public AssetRule()
	{
		//Павильон пасажирский ffeeeeee
		StarSymbol symbol1 = new StarSymbol();
		lrn2pict.put(VOK_2, symbol1);
		symbol1.setSizePnt(20);

		StarSymbol symbol2 = new StarSymbol();
		lrn2pict.put(VOK, symbol2);
		symbol2.setSizePnt(15);

		SimpleRect symbol3 = new SimpleRect();
		lrn2pict.put(BLD_PASS, symbol3);
		symbol3.setSizePnt(16);


		Ellipse symbol4 = new Ellipse();
		lrn2pict.put(PAV_TICK, symbol4);
		symbol4.setWidth(10);
		symbol4.setHeight(17);

		SimpleTriangle symbol5 = new SimpleTriangle();
		lrn2pict.put(PAV_OTHER, symbol5);
		symbol5.setWidth(9);
		symbol5.setHeight(15);
		symbol5.setDirection(SimpleTriangle.LEFT);

		SimpleRombus symbol6 = new SimpleRombus();
		lrn2pict.put(PAV_PASS, symbol6);
		symbol6.setSizePnt(17);

		SimpleRombus symbol7 = new SimpleRombus();
		lrn2pict.put(BLD_OTHER, symbol7);
		symbol7.setSizePnt(10);
	}

	public String getRuleType()
	{
		return RULETYPENAME;
	}

	public void setSelFilter(IBaseFilter filter)
	{
		this.filter = filter;
	}

	protected void pointPainter(Paint paintfill, Integer linecolor, Stroke stroke, Integer radPnt, ILayer lr, IBaseGisObject obj, Integer composite, Image pointImg, Point central) throws Exception
	{

		IDefAttr lrnmattr=lr.getLrAttrs().get(KernelConst.LAYER_NAME);
		Object objlrnm=null;
		if (lrnmattr!=null && (objlrnm=lrnmattr.getValue())!=null && objlrnm instanceof String)
		{
			IParamPainter rv = null;
		    if ((rv=lrn2pict.get(objlrnm.toString()))!=null)
			{
				cacheParamPainter=rv;
				if (
						filter==null ||
						filter.acceptObject(obj)
					)
				{
					setPainterParams(rv, paintfill, linecolor, stroke, radPnt, composite);
				}
				else
					setPainterParams(rv, new Color(0xFF888888), 0xFF888888, stroke, radPnt, composite);

				return;
			}
		}
		cacheTextParamPainter=null;
		super.pointPainter(paintfill,linecolor, stroke,radPnt,lr,obj, composite, null, central);
	}
}