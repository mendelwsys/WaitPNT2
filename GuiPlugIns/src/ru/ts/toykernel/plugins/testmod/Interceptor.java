package ru.ts.toykernel.plugins.testmod;

import ru.ts.toykernel.drawcomp.rules.def.stream.CnSerialDrawRule;
import ru.ts.toykernel.drawcomp.rules.def.CommonStyle;
import ru.ts.toykernel.drawcomp.ILayer;
import ru.ts.toykernel.drawcomp.painters.def.symbols.SimpleRombus;
import ru.ts.toykernel.geom.IBaseGisObject;
import ru.ts.toykernel.consts.INameConverter;
import ru.ts.toykernel.gui.IViewControl;
import ru.ts.toykernel.converters.ILinearConverter;
import ru.ts.gisutils.algs.common.MPoint;

import java.awt.*;

/**
 * Тестовая реализация перехватчика рисования
 * Рисует крест из прямоугольников вместо точек попавших в определенную зону рисования
 */
public class Interceptor  extends CnSerialDrawRule
{

	protected IViewControl parentmodule;
	Interceptor(IViewControl parentmodule, INameConverter nameConverter)
	{
		super(new CommonStyle(0,0), null, nameConverter);
		this.parentmodule=parentmodule;
	}
	protected void pointPainter(Paint paintfill, Integer linecolor, Stroke stroke, Integer radPnt, ILayer lr, IBaseGisObject obj, Integer composite, Image pointImg, Point central) throws Exception
	{
		cacheTextParamPainter=null;
		cacheParamPainter=null;

		ILinearConverter converter = parentmodule.getViewPort().getCopyConverter();

		Point drawsize=parentmodule.getViewPort().getDrawSize();
		double[][][] rawGeometry = obj.getRawGeometry();
		Point drwpnt=converter.getDstPointByPoint(new MPoint(rawGeometry[0][0][0], rawGeometry[1][0][0]));

		if (
				(drawsize.x/2.0*(1.0-0.1)<drwpnt.x && drwpnt.x<drawsize.x/2.0*(1.0+0.1))
					||
				(drawsize.y/2.0*(1.0-0.1)<drwpnt.y && drwpnt.y<drawsize.y/2.0*(1.0+0.1))
			)
		{
			SimpleRombus rombus = new SimpleRombus();
			rombus.setSizePnt(17);
			setPainterParams(rombus, new Color(0xFF88FF88,true), 0xFF000000, new BasicStroke(1), radPnt, composite);
			cacheParamPainter=rombus;
		}
	}

}
