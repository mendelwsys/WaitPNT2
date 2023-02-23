package ru.ts.apps.rbuilders.app0.kernel.painters;

import ru.ts.toykernel.drawcomp.painters.def.DefInscriptPainter;
import ru.ts.toykernel.geom.IBaseGisObject;
import ru.ts.gisutils.algs.common.MRect;
import ru.ts.gisutils.algs.common.MPoint;
import ru.ts.toykernel.converters.ILinearConverter;
import ru.ts.utils.data.Pair;

import java.util.List;
import java.util.LinkedList;
import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.awt.geom.AffineTransform;

/**
 * Для поддержки рисования растра
 */
public class DefInscriptPainter2 extends DefInscriptPainter
{
	protected List<Pair<MRect,String>> busyrect2=new LinkedList<Pair<MRect,String>>();

	private IBaseGisObject curentobj;
	private ILinearConverter converter;

	public DefInscriptPainter2()
	{
			System.out.println("Call new DefInscriptPainter2");
	}

	protected void drawInscription(Font font, Color colorText, Paint paintFill, String text, Graphics graphics, Point pobj, AffineTransform transform)
	{
		AffineTransform oldtransform=null;

		try
		{
			if (transform!=null)
			{
				oldtransform=((Graphics2D)graphics).getTransform();
				((Graphics2D)graphics).setTransform(transform);
			}

			graphics.setFont(font);

			Rectangle2D rect = graphics.getFontMetrics().getStringBounds(text, graphics);

			int x = pobj.x;
			int y = pobj.y;

			//graphics.setColor(paintFill);
			setFillPainter(graphics,paintFill);

			MRect drawRect=new MRect(
						new MPoint(x,y-rect.getHeight()+3),
						new MPoint(Math.ceil(x+rect.getWidth()+2),Math.ceil(y+3))
					);
			for (Pair<MRect,String> pr : busyrect2)
			{

				if (pr.first.isIIntersect(converter.getRectByDstRect(drawRect,null)))
				{
					if(!curentobj.getCurveId().equals(pr.second))
						return;
					else
						System.out.println("Eq curveId");
				}
			}
			graphics.fillRoundRect((int)drawRect.p1.x,(int)drawRect.p1.y,(int)drawRect.getWidth(),(int)rect.getHeight(),(int)(drawRect.getWidth()*0.1),(int)(drawRect.getHeight()*0.1));
			graphics.setColor(colorText);
			graphics.drawString(text, x, y);
			busyrect2.add(new Pair<MRect,String>(converter.getRectByDstRect(drawRect,null),curentobj.getCurveId()));
		}
		finally
		{
			if (oldtransform!=null)
				((Graphics2D)graphics).setTransform(oldtransform);
		}
	}

	public int[] paint(Graphics graphics, IBaseGisObject drawMe, ILinearConverter converter, Point drawSize) throws Exception
	{
		curentobj= drawMe;
		this.converter=converter;
		return super.paint(graphics, drawMe, converter, drawSize);
	}
}
