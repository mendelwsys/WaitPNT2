package ru.ts.toykernel.drawcomp.layers;

import ru.ts.toykernel.drawcomp.layers.def.DrawOnlyLayer;
import ru.ts.toykernel.drawcomp.IDrawObjRule;
import ru.ts.toykernel.storages.IBaseStorage;
import ru.ts.toykernel.filters.IBaseFilter;
import ru.ts.toykernel.attrs.IAttrs;
import ru.ts.toykernel.attrs.IDefAttr;
import ru.ts.toykernel.gui.IViewPort;
import ru.ts.toykernel.converters.IProjConverter;
import ru.ts.toykernel.plugins.analitics.ModuleConst;
import ru.ts.toykernel.plugins.analitics.IReliefProvider;
import ru.ts.gisutils.algs.common.IMCoordinate;
import ru.ts.gisutils.potentialmap.*;
import ru.ts.factory.IParam;

import java.util.List;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;

/**
 * Слой показа рельфа на основе
 * ru.ts.toykernel.drawcomp.layers.ReliefLayer
 */
public class ReliefLayer extends DrawOnlyLayer
{

	protected Builder mapbuilder = new Builder();//Построитель потенциалов
	protected IReliefProvider reliefProvider;//провайдер рельефов

	public ReliefLayer()
	{
	}

	public ReliefLayer(IBaseStorage basestorage, IBaseFilter filter, IAttrs attrs, IDrawObjRule drawObjRule)
			throws Exception
	{
		super(basestorage, filter, attrs, drawObjRule);
	}

	public ReliefLayer(IBaseStorage basestorage, List<IBaseFilter> filters, IAttrs attrs, IDrawObjRule drawObjRule)
			throws Exception
	{
		super(basestorage, filters, attrs, drawObjRule);
	}

	public Builder getMapbuilder()
	{
		return mapbuilder;
	}

	public Object init(Object obj) throws Exception
	{
		IDefAttr attr = (IDefAttr) obj;
		if (attr.getName().equalsIgnoreCase(ModuleConst.RELIEF_TAGNAME))
		{
			IReliefProvider reliefProvider = this.reliefProvider;
			this.reliefProvider = (IReliefProvider) attr.getValue();
			IParam rparam = attr.getCopy();
			rparam.setValue(reliefProvider);
			return rparam;
		}
		else
			return super.init(obj);
	}

	/**
	 * Прорисовать слой
	 *
	 * @param graphics -графический контекст в котором надо рисовать, если он null значит
	 *                 сгенерировать BufferedImage и рисовать в нем
	 */
	public int[] paintLayer(
			Graphics graphics, IViewPort viewPort) throws Exception
	{

		Shape shp = graphics.getClip();
		try
		{
			int[] rv = new int[]{0, 0, 0};
			if (graphics == null)
				return rv;

			if (!isVisible())
				return rv;

			BufferedImage l_buffimg = makeBufferedImage(graphics, viewPort);
			graphics.drawImage(l_buffimg, 0, 0, new ImageObserver()
			{
				public boolean imageUpdate(Image img, int infoflags, int x, int y, int width, int height)
				{
					return true;
				}
			});

			rv[0]=1;
			return rv;
		}
		finally
		{
			graphics.setClip(shp);
		}
	}

	protected BufferedImage makeBufferedImage(Graphics graphics, IViewPort viewPort) throws Exception
	{
		IProjConverter iProjConverter = (IProjConverter) viewPort.getCopyConverter();
		IMCoordinate[] ppoints = reliefProvider.getRelief(getVisibleObjects(graphics, iProjConverter, viewPort.getDrawSize()), iProjConverter);
		ppoints=convertPoint4Draw(ppoints);

		double maxvalue = 0;
		for (IMCoordinate ppoint : ppoints)
			if (maxvalue < ppoint.getM())
				maxvalue = ppoint.getM();

		//TODO double[] dxdy = cnv.getDrawDxDyByLinearDxDy(l_cnv.getLinearDxDyByDrawDxDy(new double[]{5, 5}));
//		if (dxdy[0] > 90)
//			dxdy[0] = 90;
		double[] dxdy = new double[]{90};

		GeomProfile profile = GeomProfile.getConicProfile(dxdy[0]);
		ColorScheme colors = new ColorScheme(ColorScheme.palRainbow2, maxvalue);
		colors.set_mergeLevel(0.05f);


		Legend legend = mapbuilder.getLegend();
		legend.drawLegend = false;

		Point sz = viewPort.getDrawSize();
		BufferedImage l_buffimg = new BufferedImage((int) Math.ceil(sz.getX()), (int) Math.ceil(sz.getY()), BufferedImage.TYPE_INT_ARGB);
		l_buffimg = mapbuilder.makeImage(l_buffimg.getRaster().getBounds(), l_buffimg,
				profile, ppoints, colors, 0.1, DoubleMatrix.PM_MAX);
		return l_buffimg;
	}

	public IMCoordinate[] convertPoint4Draw(IMCoordinate[] points)
	{
		return points;
	}

	public IReliefProvider getReliefProvider()
	{
		return reliefProvider;
	}
}
