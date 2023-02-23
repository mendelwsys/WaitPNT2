package ru.ts.apps.rbuilders.app0.kernel.filters;

import ru.ts.toykernel.geom.IBaseGisObject;
import ru.ts.toykernel.drawcomp.IDrawObjRule;
import ru.ts.toykernel.drawcomp.ILayer;
import ru.ts.toykernel.drawcomp.IPainter;
import ru.ts.toykernel.storages.IBaseStorage;
import ru.ts.toykernel.converters.ILinearConverter;
import ru.ts.toykernel.filters.IKeyFilter;
import ru.ts.gisutils.algs.common.MRect;
import ru.ts.factory.IObjectDesc;

import java.awt.*;
import java.util.Map;
import java.util.HashMap;

/**
 * Build raster filter
 */
public class RasterBuildFilter implements IKeyFilter
{
	public static final String TYPENAME ="F_RBF";//minimal boundig box filter
	protected String filterName;
	protected Map<String, MRect> cahegraphobkects = new HashMap<String, MRect>();//curveId-><linear_rect>
	private double l_x0;
	private double l_y0;
	private double l_w;
	private double l_h;
	private IBaseStorage storage;
	private Graphics graph;
	private ILinearConverter converter;
	private IDrawObjRule rule;
	private ILayer lr;

	public RasterBuildFilter(IBaseStorage storage,MRect lrect,ILayer lr,Graphics graph, ILinearConverter converter, IDrawObjRule rule)
	{
		this.storage = storage;
		this.graph = graph;
		this.converter = converter;
		this.rule = rule;
		this.lr = lr;
		setRect(lrect);
	}

	public String getTypeName()
	{
		return TYPENAME;
	}

	public void setRect(MRect lrect)
	{
		this.l_x0 = lrect.p1.x;
		this.l_y0 = lrect.p1.y;
		this.l_w = lrect.getWidth();
		this.l_h = lrect.getHeight();
	}

	public String getObjName()
	{
		return filterName;
	}

	public boolean acceptObject(IBaseGisObject obj)
	{
		return acceptObject(obj.getCurveId());
	}

	public boolean acceptObject(String key)
	{
		MRect proj_rect=cahegraphobkects.get(key);
		try
		{
			if (proj_rect==null)
			{
				IBaseGisObject obj=storage.getBaseGisByCurveId(key);
				IPainter painter=rule.createPainter(graph,lr,obj);
				proj_rect=painter.getRect(graph,obj,converter);
				cahegraphobkects.put(key,proj_rect);
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		boolean b = proj_rect != null && proj_rect.isIIntersect(l_x0, l_y0, l_w, l_h);
//		try
//		{
//			if (b)
//			{
//				IBaseGisObject obj=storage.getBaseGisByCurveId(key);
//				MRect rct = converter.getDstRectByRect(obj.getMBB(null));
//				System.out.println("Intersect");
//			}
//		} catch (Exception e)
//		{
//			e.printStackTrace();
//		}
		return b;
	}

	public Object[] init(Object... objs) throws Exception
	{
		throw new UnsupportedOperationException();
	}

	public Object init(Object obj) throws Exception
	{
		throw new UnsupportedOperationException();
	}

	public IObjectDesc getObjectDescriptor()
	{
		throw new UnsupportedOperationException();
	}
}
