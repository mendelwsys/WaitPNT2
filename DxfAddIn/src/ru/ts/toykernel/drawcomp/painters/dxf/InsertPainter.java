package ru.ts.toykernel.drawcomp.painters.dxf;

import ru.ts.toykernel.drawcomp.painters.def.DefPointPainter;
import ru.ts.toykernel.drawcomp.ILayer;
import ru.ts.toykernel.geom.IBaseGisObject;
import ru.ts.toykernel.converters.ILinearConverter;
import ru.ts.toykernel.converters.CrdConverterFactory;
import ru.ts.toykernel.converters.IProjConverter;
import ru.ts.toykernel.gui.IView;
import ru.ts.toykernel.gui.IViewPort;
import ru.ts.toykernel.attrs.IAttrs;
import ru.ts.toykernel.attrs.IDefAttr;
import ru.ts.gisutils.algs.common.MPoint;

import java.awt.*;
import java.util.*;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: VMendelevich
 * Date: 14.03.2012
 * Time: 17:57:44
 * dxf Insert painter
 */
public class InsertPainter extends DefPointPainter
{

	public static final String ATTR_VIEWNAME = "VIEWNAME";

	protected Map<String, IView> name2view; //Мно-во блоков, используется для отображения блоков при рисовании

	public void setIVewMap(Map<String, IView> name2view)
	{
		this.name2view = name2view;
	}

	public int[] paint(Graphics graphics, IBaseGisObject drawMe, ILinearConverter converter, Point drawSize) throws Exception
	{

		setPainterParams(graphics, drawMe, converter, drawSize);

		int[] rv = new int[]{0, 0, 0};
		double[][][] arrpoint = drawMe.getRawGeometry();
		//1. Получим имя блока из атрибутов объектов
		final IView view = getView(drawMe);
		//2. Установим конвертер смещения
		if (view != null)
		{

			//TODO Не верно создан конвертер,
			CrdConverterFactory.ShitConverter conv = new CrdConverterFactory.ShitConverter(new MPoint(arrpoint[0][0][0], arrpoint[1][0][0]));
			final IProjConverter projconv = view.getViewPort().getCopyConverter();
			List<ILinearConverter> converterList = projconv.getConverterChain();
			converterList.add(conv);
			projconv.setConverterChain(converterList);


			//3. Произвести прорисовку блока (собственно так же как и в слое)
			List<ILayer> layers = new LinkedList<ILayer>(view.getProjContext().getLayerList());
			if (layers.size() != 0)
			{
				try
				{
					long tm = System.currentTimeMillis();
					for (ILayer layer : layers)
					{
						int[] rvv = layer.paintLayer(graphics,
								new IViewPort()
								{

									public Point getDrawSize() throws Exception
									{
										return view.getViewPort().getDrawSize();
									}

									public IProjConverter getCopyConverter() throws Exception
									{
										return projconv;
									}

									public void setCopyConverter(IProjConverter converer) throws Exception
									{
										throw new UnsupportedOperationException();
									}
								}
						);
						for (int i = 0; i < rv.length; i++)
							rv[i] += rvv[i];
					}
					System.out.print("Picture panel tm:" + (System.currentTimeMillis() - tm) + " ");
					System.out.print("pnts = " + rv[0] + " ");
					System.out.print("lines = " + rv[1] + " ");
					System.out.println("poly = " + rv[2]);
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}
			}
		}
		return rv;
	}

	private IView getView(IBaseGisObject obj)
	{
		IView view = null;
		IAttrs oattrs = obj.getObjAttrs();
		IDefAttr attr;
		if (oattrs != null && (attr = oattrs.get(ATTR_VIEWNAME)) != null)
		{
			String val = (String) attr.getValue();
			view = name2view.get(val);
		}
		return view;
	}


}
