package ru.ts.apps.rbuilders.app0.kernel.layers;

import ru.ts.toykernel.drawcomp.layers.def.DrawOnlyLayer;
import ru.ts.toykernel.geom.IBaseGisObject;
import ru.ts.toykernel.converters.IScaledConverter;
import ru.ts.toykernel.converters.ILinearConverter;
import ru.ts.toykernel.converters.IProjConverter;
import ru.ts.gisutils.algs.common.MRect;
import ru.ts.gisutils.algs.common.MPoint;
import ru.ts.apps.rbuilders.app0.kernel.filters.RasterBuildFilter;

import java.util.Iterator;
import java.awt.*;

/**
 * form raster imge from vector layer
 * Формировать растровую подложку исходя из векторной информации
 * ru.ts.apps.rbuilders.app0.kernel.layers.DrawRasterLayer
 */
public class DrawRasterLayer extends DrawOnlyLayer
{
	protected RasterBuildFilter currentfilter = null;
	protected MPoint unitsonpixel=new MPoint(-1,-1);

	protected Iterator<IBaseGisObject> getVisibleObjects(Graphics graphics, ILinearConverter converter,
										Point drawSize) throws Exception
	{

		IScaledConverter l_converter=null;
		if (converter instanceof IScaledConverter)
			l_converter=(IScaledConverter)converter;
		else if (converter instanceof IProjConverter)
			l_converter=((IProjConverter)converter).getAsScaledConverter();

		MRect drawrect = new MRect(new MPoint(),
				new MPoint(drawSize.x, drawSize.y));
		MRect proj_rect = l_converter.getRectByDstRect(drawrect,null);

		if (currentfilter==null || !l_converter.getUnitsOnPixel().equals(unitsonpixel))
		{
			currentfilter = new RasterBuildFilter(storage, proj_rect, this, graphics, l_converter, drawObjRule);
			unitsonpixel=l_converter.getUnitsOnPixel();
		}
		else
			currentfilter.setRect(proj_rect);

		return storage.filterObjs(currentfilter);
	}
}
