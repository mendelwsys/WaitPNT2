package ru.ts.toykernel.drawcomp.painters.dxf;

import ru.ts.toykernel.drawcomp.painters.def.DefPointPainter;
import ru.ts.toykernel.geom.IBaseGisObject;
import ru.ts.toykernel.converters.ILinearConverter;
import ru.ts.gisutils.algs.common.MPoint;

import java.awt.*;
import java.awt.geom.Arc2D;
import java.awt.geom.AffineTransform;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;

/**
 * Created by IntelliJ IDEA.
 * User: vladm
 * Date: 03.03.2012
 * Time: 12:00:25
 * Рисовальщик для дуг
 */
public class ArcPainter extends DefPointPainter
{
//	public static final String ATTR_DANGL = "DANGL"; //Вынуждены ввести этот параметр, для того что бы рисовать замкнутые дуги
//	double dangle=0;


	public int[] paint(Graphics graphics, IBaseGisObject drawMe, ILinearConverter converter, Point drawSize) throws Exception
	{
		setPainterParams(graphics, drawMe, converter, drawSize);

//		IAttrs oattrs = obj.getObjAttrs();
//		IDefAttr rta = oattrs.get(ATTR_DANGL);
//		rta.getValue();


		double[][][] pnts = drawMe.getRawGeometry();
		int[][] x = new int[pnts[0].length][];
		int[][] y = new int[pnts[1].length][];

//		MPoint cnvPnt=new MPoint();
//		for (int i = 0; i < x.length; i++)
//		{
//			x[i] = new int[pnts[0][i].length];
//			y[i] = new int[pnts[1][i].length];
//
//			for (int j = 0; j < x[i].length; j++)
//			{
//				cnvPnt.x=pnts[0][i][j];
//				cnvPnt.y=pnts[1][i][j];
//				Point pi = converter.getDstPointByPoint(cnvPnt);
//				x[i][j] = pi.x;
//				y[i][j] = pi.y;
//			}
//		}

		convert2drawCrds(converter, pnts, x, y);

		for (int i = 0; i < x.length; i++)
		{
			final AffineTransform transform = getTransform(drawMe, new MPoint(x[i][4], y[i][4]));
			drawArcLine(graphics, x[i],y[i], stroke, transform);
		}
		return new int[]{0,x.length,0};
	}


	public Shape createShape(IBaseGisObject drawMe, ILinearConverter converter) throws Exception
	{
		Path2D p2d=new Path2D.Double();
		double[][][] pnts = drawMe.getRawGeometry();
		int[][] x = new int[pnts[0].length][];
        int[][] y = new int[pnts[1].length][];

		convert2drawCrds(converter, pnts, x, y);
		for (int i = 0; i < x.length; i++)
		{
			final AffineTransform transform = getTransform(drawMe, new MPoint(x[i][4], y[i][4]));
			Shape arcSh = getArcShape(x[i], y[i]);
			if (transform!=null)
				arcSh=transform.createTransformedShape(arcSh);
			p2d.append(arcSh,false);
		}
		return p2d;
	}



	protected void drawArcLine(Graphics graphics, int[] x, int[] y, Stroke stroke, AffineTransform transform)
	{
		AffineTransform oldtransform=null;
		try
		{
			if (transform!=null)
			{
				oldtransform=((Graphics2D)graphics).getTransform();
				((Graphics2D)graphics).setTransform(transform);
			}

			if (graphics instanceof Graphics2D && stroke != null)
				((Graphics2D) graphics).setStroke(stroke);
			graphics.setColor(colorLine);

			Arc2D.Double sh = getArcShape(x, y);
			((Graphics2D)graphics).draw(sh);

//			if (graphics instanceof Graphics2D)
//				((Graphics2D)graphics).draw(sh);
//			else
//				graphics.drawArc(Math.min(x[0],x[2]), Math.min(y[0],y[2]), width, height, (int)Math.round(a1), (int)Math.round(delta));
		}
		finally
		{
			if (oldtransform!=null)
				((Graphics2D)graphics).setTransform(oldtransform);
		}
	}

//	protected Arc2D.Double getArcShape(int[] x, int[] y)
//	{
//		int width = Math.abs(x[2] - x[0]);
//		int height = Math.abs(y[2] - y[0]);
//
//		int sx=x[5]-x[4];
//		int sy=y[4]-y[5];
//
//		double a1=Math.toDegrees(Math.atan2(sy,sx));
//
//		int ex=x[6]-x[4];
//		int ey=y[4]-y[6];
//
//		double a2=Math.toDegrees(Math.atan2(ey,ex));
//
//		if (a2<0)
//			a2=360+a2%360;
//		if (a1<0)
//			a1=360+a1%360;
//
//
//		double delta=a2-a1;
//		if (a2<a1)
//			delta=360-a1+a2;
//
//		Arc2D.Double sh = new Arc2D.Double((double) Math.min(x[0], x[2]), (double) Math.min(y[0], y[2]), (double) width, (double) height, a1, delta, Arc2D.OPEN);
//		return sh;
//	}


	protected Arc2D.Double getArcShape(int[] x, int[] y)
	{
		int width = Math.abs(x[2] - x[0]);
		int height = Math.abs(y[2] - y[0]);

		Arc2D.Double sh=new Arc2D.Double();
		sh.setFrame((double) Math.min(x[0], x[2]), (double) Math.min(y[0], y[2]), (double) width, (double) height);
		sh.setAngles(x[5],y[5],x[6],y[6]);
		return sh;
	}

}
