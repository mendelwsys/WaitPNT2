package ru.ts.conv;

import ru.ts.gisutils.algs.common.MPoint;
import ru.ts.gisutils.algs.common.MRect;
import ru.ts.toykernel.converters.CrdConverterFactory;
import ru.ts.toykernel.converters.ServerConverter;
import ru.ts.toykernel.converters.ILinearConverter;
import ru.ts.toykernel.xml.def.ParamDescriptor;
import ru.ts.toykernel.attrs.def.DefAttrImpl;
import ru.ts.xml.IXMLObjectDesc;

import java.awt.*;
import java.awt.geom.Point2D;
import java.util.*;
import java.util.List;

/**
 * Генератор и утитлиты  конвертера
 */
public class ConvGenerator
{

	public MPoint[] calcScale_BPoint(Point2D minmax[],double[] rotmatrix,Point sz)
	{
		MPoint[] _minmax=new MPoint[]{new MPoint(minmax[0].getX(),minmax[0].getY()),new MPoint(minmax[1].getX(),minmax[1].getY())};
		return calcScale_BPoint(_minmax,rotmatrix,sz);
	}

	public MPoint[] calcScale_BPoint(MRect rect,double[] rotmatrix,Point sz)
	{
		return calcScale_BPoint(new MPoint[]{rect.p1,rect.p4},rotmatrix,sz);
	}

	public MPoint[] calcScale_BPoint(MPoint minmax[],double[] rotmatrix,Point sz)
	{
		CrdConverterFactory.RotateConverter rot = new CrdConverterFactory.RotateConverter(rotmatrix);
		return calcScale_BPoint(minmax,rot,sz);
	}


	public MPoint[] calcScale_BPoint(MRect rect,ILinearConverter rot,Point sz)
	{
		return calcScale_BPoint(new MPoint[]{rect.p1,rect.p4},rot,sz);
	}

	public MPoint[] calcScale_BPoint(MPoint minmax[], ILinearConverter rot,Point sz)
	{

		double[] szxy=rot.getDstSzBySz(new MRect(minmax[0],minmax[1]));
		double scalex=sz.x/(szxy[0]);//Отношение пиксель/ед.карты (метрам)
		double scaley=sz.y/(szxy[1]);//Отношение пиксель/ед.карты (метрам)

		double scale = Math.min(scalex,scaley);

		Point2D.Double midle = rot.getDstPointByPointD(new MPoint((minmax[0].x + minmax[1].x) / 2, (minmax[0].y + minmax[1].y) / 2));

		//MPoint bpoint=new MPoint(midle.x*scale-sz.x/2,midle.y*scale-sz.y/2);
		MPoint bpoint=new MPoint(midle.x*scale,midle.y*scale);

		return new MPoint[]{new MPoint(scale,scale),bpoint};
	}


	public MPoint[] calcScale_BPoint2(MRect rect,ILinearConverter rot,Point sz,boolean eqscale)
	{
		return calcScale_BPoint2(new MPoint[]{rect.p1,rect.p4},rot,sz,eqscale);
	}

	public MPoint[] calcScale_BPoint2(MPoint minmax[], ILinearConverter rot,Point sz,boolean eqscale)
	{

		double[] szxy=rot.getDstSzBySz(new MRect(minmax[0],minmax[1]));
		double scalex=sz.x/(szxy[0]);//Отношение пиксель/ед.карты (метрам)
		double scaley=sz.y/(szxy[1]);//Отношение пиксель/ед.карты (метрам)

		if (eqscale)
		{
			double scale = Math.min(scalex,scaley);
			scalex=scale;
			scaley=scale;
		}

		Point2D.Double midle = rot.getDstPointByPointD(new MPoint((minmax[0].x + minmax[1].x) / 2, (minmax[0].y + minmax[1].y) / 2));
		MPoint bpoint=new MPoint(midle.x*scalex,midle.y*scaley);

		return new MPoint[]{new MPoint(scalex,scaley),bpoint};
	}

	public String getConvertersTag()
	{
		return "converter";
	}


	public IXMLObjectDesc[] getServProjConverters(String projConverterName,
								String servConverterName,
								double[] rotmatrix,
								MPoint initscale,
								MPoint ibindpnt)
	{
		//servConverterName SERVERCONVERTER
		//projConverterName PROJ0
		//scale 0.007955764406310689 0.007955764406310689
		//ibindpnt -3003.552192249083 -136.43182148519912

		List<IXMLObjectDesc> l_converters=new LinkedList<IXMLObjectDesc>();

		String matrix="";
		for (int i = 0; i < rotmatrix.length; i++)
		{
			matrix += String.valueOf(rotmatrix[i]);
			if (i<matrix.length()-1)
				matrix+=" ";
		}


		String tagConverter =getConvertersTag();

		List rotparam =Arrays.asList(
					new DefAttrImpl("matrix",matrix)
				);

		IXMLObjectDesc rotconv=new ParamDescriptor
				(
					tagConverter,
					"rot0",
					CrdConverterFactory.RotateConverter.class.getName(),null,rotparam,-1
				);
		l_converters.add(rotconv);

		List scaleparam = Arrays.asList(
					new DefAttrImpl("initscale",initscale.x+" "+initscale.y)
				);
		IXMLObjectDesc scaleconv=new ParamDescriptor
				(
					tagConverter,
					"scale1",
					CrdConverterFactory.ScaledConverter.class.getName(),null,scaleparam,-1
				);
		l_converters.add(scaleconv);

		List shiftparam = Arrays.asList(
					new DefAttrImpl("bindp", ibindpnt.x+" "+ ibindpnt.y)
				);

		IXMLObjectDesc shiftconv=new ParamDescriptor
				(
					tagConverter,
					"shift2",
					CrdConverterFactory.ShitConverter.class.getName(),null,shiftparam,-1
				);
		l_converters.add(shiftconv);



		List servparam = Arrays.asList(
					new DefAttrImpl(null,rotconv),
					new DefAttrImpl(null,scaleconv)
				);

		IXMLObjectDesc servconv=new ParamDescriptor
				(
					tagConverter,
					servConverterName,
					ServerConverter.class.getName(),null,servparam,-1
				);
		l_converters.add(servconv);


		List projparam = Arrays.asList(
					new DefAttrImpl(null,rotconv),
					new DefAttrImpl(null,scaleconv),
					new DefAttrImpl(null,shiftconv)
			);

		IXMLObjectDesc projconv=new ParamDescriptor
		(
				tagConverter,
				projConverterName,
				CrdConverterFactory.LinearConverterRSS.class.getName(),null,projparam,-1
		);
		l_converters.add(projconv);

		return l_converters.toArray(new IXMLObjectDesc[l_converters.size()]);
	}

}
