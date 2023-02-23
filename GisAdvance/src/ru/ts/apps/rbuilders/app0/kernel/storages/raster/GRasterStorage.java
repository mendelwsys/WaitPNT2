package ru.ts.apps.rbuilders.app0.kernel.storages.raster;

import ru.ts.toykernel.storages.raster.RasterStorage;
import ru.ts.toykernel.geom.def.RasterObject;
import ru.ts.factory.IParam;
import ru.ts.apps.rbuilders.app0.ModuleConst;
import ru.ts.apps.rbuilders.app0.AppGenerator0;
import ru.ts.apps.rbuilders.app0.kernel.geom.GRasterObject;
import ru.ts.gisutils.algs.common.MPoint;

/**
 * Растровый
 * ru.ts.apps.rbuilders.app0.kernel.storages.raster.GRasterStorage
 */
public class GRasterStorage extends RasterStorage
{
	protected AppGenerator0 generator;

	public Object init(Object obj) throws Exception
	{
		IParam attr=(IParam)obj;
		if (attr.getName().equalsIgnoreCase(ModuleConst.GENERATOR_TAGNAME))
			this.generator = (AppGenerator0) attr.getValue();
		else
			super.init(obj);
		return null;
	}

	protected RasterObject createRasterObject(int[] indexobj, MPoint pt1, MPoint pt2)
			throws Exception
	{
		return new GRasterObject(new MPoint(Math.min(pt1.x, pt2.x), Math.min(pt1.y, pt2.y)),
				new MPoint(Math.max(pt1.x, pt2.x), Math.max(pt1.y, pt2.y)),
				getImageUrl2NameRequest(indexobj), getCurveIdByIndex(indexobj[0], indexobj[1], indexobj[2]),generator,indexobj);
	}

}
