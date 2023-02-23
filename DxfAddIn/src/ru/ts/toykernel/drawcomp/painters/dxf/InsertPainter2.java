package ru.ts.toykernel.drawcomp.painters.dxf;

import ru.ts.toykernel.drawcomp.painters.def.DefPointPainter;
import ru.ts.toykernel.drawcomp.ILayer;
import ru.ts.toykernel.geom.IBaseGisObject;
import ru.ts.toykernel.converters.ILinearConverter;
import ru.ts.toykernel.converters.CrdConverterFactory;
import ru.ts.toykernel.converters.IProjConverter;
import ru.ts.toykernel.gui.IViewPort;
import ru.ts.toykernel.attrs.IAttrs;
import ru.ts.toykernel.attrs.IDefAttr;
import ru.ts.toykernel.pcntxt.IProjContext;
import ru.ts.toykernel.consts.KernelConst;
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
public class InsertPainter2 extends DefPointPainter
{
	public static final String ATTR_PCTXNAME = "PCTXNAME";
	protected Map<String, IProjContext> name2projctx = new HashMap<String, IProjContext>(); //Мно-во блоков, используется для отображения блоков при рисовании

	public int[] paint(Graphics graphics, IBaseGisObject drawMe, ILinearConverter converter, Point drawSize) throws Exception
	{

		setPainterParams(graphics, drawMe, converter, drawSize);

		int[] rv = new int[]{0, 0, 0};

		//1. Получим имя блока из атрибутов объектов
		final IProjContext projctx = getProjCtx(drawMe);
		//2. Установим конвертер смещения
		if (projctx != null)
		{

//			Point shp = converter.getDstPointByPoint(new MPoint(arrpoint[0][0][0],arrpoint[1][0][0]));
////			Point shp0 = converter.getDstPointByPoint(new MPoint());
////			int dx=shp.x-shp0.x;
////			int dy=shp.y-shp0.y;

			IAttrs attrs = drawMe.getObjAttrs();

			MPoint shp;
			{
				IDefAttr shx=attrs.get(ATTR_SHX);
				IDefAttr shy=attrs.get(ATTR_SHY);
				shp = new MPoint((Double)shx.getValue(),(Double)shy.getValue());
			}


//			{//TODO Тестовый код
//				double[][][] arrpoint = obj.getRawGeometry();
//				Point p1=converter.getDstPointByPoint(new MPoint(arrpoint[0][0][0],arrpoint[1][0][0]));
//				Point p4=converter.getDstPointByPoint(new MPoint(arrpoint[0][0][1],arrpoint[1][0][1]));
//
//				graphics.setColor(new Color(0xFF,0,0,0xFF));
//				graphics.fillOval(p1.x,p1.y, 7,7);
//				graphics.setColor(new Color(0,0,0xFF,0xFF));
//				graphics.fillOval(p4.x,p4.y, 10,10);
//
//				Point shp0=converter.getDstPointByPoint(shp);
//				graphics.setColor(new Color(0,0xFF,0,0xFF));
//				graphics.fillOval(shp0.x,shp0.y, 25,25);
//			}


			CrdConverterFactory.ScaledConverter scaleconv=null;
			{
				MPoint scp=new MPoint(1,1);
				IDefAttr scx=attrs.get(ATTR_SCX);
				if (scx!=null)
					scp.setX((Double)scx.getValue());
				IDefAttr scy=attrs.get(ATTR_SCY);
				if (scy!=null)
					scp.setY((Double)scy.getValue());
				 if (scp.getX()!=1 || scp.getY()!=1)
				 	scaleconv= new CrdConverterFactory.ScaledConverter(scp);
			}

			CrdConverterFactory.RotateConverter rotconv=null;
			{
				IDefAttr rt=attrs.get(ATTR_ROT);
				if (rt!=null)
				{
					double angl= (Double) rt.getValue();

					double rangl = Math.PI * angl / 180;
					rotconv =new CrdConverterFactory.RotateConverter
					(
							new double[]
									{
										Math.cos(rangl),Math.sin(rangl),
										-Math.sin(rangl),Math.cos(rangl)
									}
					);
				}
			}

//				CrdConverterFactory.ShitConverter shitConverter1 = new CrdConverterFactory.ShitConverter(new MPoint(-dx,-dy));
//				IProjConverter projconv1 = (IProjConverter) converter.createCopyConverter();
//				List<ILinearConverter> converterList = projconv1.getConverterChain();
//				converterList.add(shitConverter1);
//				projconv1.setConverterChain(converterList);
//				Point pt1 = projconv1.getDstPointByPoint(new MPoint());


				//CrdConverterFactory.ShitConverter shitConverter2 = new CrdConverterFactory.ShitConverter(new MPoint(-arrpoint[0][0][0],-arrpoint[1][0][0]));
			CrdConverterFactory.ShitConverter shitConverter2 = new CrdConverterFactory.ShitConverter(new MPoint(-shp.x,-shp.y));
			final IProjConverter projconv=(IProjConverter) converter.createCopyConverter();
			List<ILinearConverter> converterList = projconv.getConverterChain();

			List<ILinearConverter> nwcv=new LinkedList<ILinearConverter>();

			 if (rotconv!=null)
				nwcv.add(rotconv);
			nwcv.add(shitConverter2);
			nwcv.addAll(converterList);
			projconv.setConverterChain(nwcv);

			//3. Произвести прорисовку блока (собственно так же как и в слое)
			List<ILayer> layers = new LinkedList<ILayer>(projctx.getLayerList());
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
										return null;
									}

									public IProjConverter getCopyConverter() throws Exception
									{
										return (IProjConverter)projconv.createCopyConverter();
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
					System.out.print("Insert painter tm:" + (System.currentTimeMillis() - tm) + " ");
					System.out.print("Insert painter pnts = " + rv[0] + " ");
					System.out.print("Insert painter lines = " + rv[1] + " ");
					System.out.println("Insert painter poly = " + rv[2]);
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}
			}
		}
		return rv;
	}

	private IProjContext getProjCtx(IBaseGisObject obj)
	{
		IProjContext projctx = null;
		IAttrs oattrs = obj.getObjAttrs();
		IDefAttr attr;
		if (oattrs != null && (attr = oattrs.get(ATTR_PCTXNAME)) != null)
		{
			String val = (String) attr.getValue();
			projctx = name2projctx.get(val);
		}
		return projctx;
	}


	public Object init(Object obj) throws Exception
	{

		IDefAttr attr=(IDefAttr)obj;
		if (attr.getName().equalsIgnoreCase(getNameConverter().codeAttrNm2StorAttrNm(KernelConst.PROJCTXT_TAGNAME)))
		{ //Создадим набор блоков отображения связанный с этим правилом
			IProjContext projctx=(IProjContext)attr.getValue();
			name2projctx.put(projctx.getObjName(),projctx);
		}
		else
			return super.init(obj);
		return null;
	}



}