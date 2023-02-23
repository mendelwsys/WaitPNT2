package ru.ts.toykernel.drawcomp.painters.dxf;

import ru.ts.toykernel.geom.IBaseGisObject;
import ru.ts.toykernel.converters.ILinearConverter;
import ru.ts.toykernel.attrs.IDefAttr;
import ru.ts.toykernel.consts.KernelConst;
import ru.ts.gisutils.algs.common.MPoint;

import java.awt.*;
import java.awt.geom.AffineTransform;

/**
 * Created by IntelliJ IDEA.
 * User: vladm
 * Date: 26.05.2012
 * Time: 19:54:30
 * To change this template use File | Settings | File Templates.
 */
public class DimPainter extends InsertPainter2
{
	private String attrAsName;//the name of attribute which text value will be draw

	public int[] paint(Graphics graphics, IBaseGisObject drawMe, ILinearConverter converter, Point drawSize) throws Exception
	{
		//1. Рисуем блок
		int[] res = super.paint(graphics, drawMe, converter, drawSize);

//		//2.Рисуем текст, в случае если у нас есть текст
//		double[][][] pnts = drawMe.getRawGeometry();
//
//
//		String text=null;
//		IDefAttr defName = null;
//
//		defName = drawMe.getObjAttrs().get((nameConverter.codeAttrNm2StorAttrNm(attrAsName)));
//		if (defName!=null && defName.getValue()!=null)
//		{
////			if (rv.getFont() == null)
////				rv.setFont(getFont());
//			text=defName.getValue().toString();
//		}
//
//		if (text != null && text.length() > 0)
//		{
//			Point txtp = converter.getDstPointByPoint(new MPoint(pnts[0][1][0],pnts[1][1][0]));
//			Font ft=graphics.getFont();
//			AffineTransform oldtransform=null;
//			try
//			{
//				AffineTransform transform=getTransform(drawMe, new MPoint(txtp),ATTR_TROT);
//
//				if (transform!=null)
//				{
//					oldtransform=((Graphics2D)graphics).getTransform();
//					((Graphics2D)graphics).setTransform(transform);
//				}
////				graphics.setFont(font);
////				int x = pobj.x;
////				int y = pobj.y;
////				graphics.setColor(colorText);
//				graphics.drawString(text, txtp.x, txtp.y);
//			}
//			finally
//			{
//				if (oldtransform!=null)
//					((Graphics2D)graphics).setTransform(oldtransform);
//				graphics.setFont(ft);
//			}
//		}
		return res;
	}

	public Object init(Object obj) throws Exception
	{

		IDefAttr attr=(IDefAttr)obj;
		if (attr.getName().equalsIgnoreCase(KernelConst.USE_AS_ATTRIBUTENAME))
			this.attrAsName = (String) attr.getValue();
		else
			return super.init(obj);
		return null;
	}

}
