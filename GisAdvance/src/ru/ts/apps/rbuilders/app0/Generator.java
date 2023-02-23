package ru.ts.apps.rbuilders.app0;

import ru.ts.toykernel.drawcomp.ILayer;
import ru.ts.toykernel.gui.IViewPort;

import ru.ts.toykernel.pcntxt.IProjContext;
import ru.ts.toykernel.converters.IProjConverter;
import ru.ts.toykernel.converters.IRProjectConverter;
import ru.ts.toykernel.converters.CrdConverterFactory;
import ru.ts.gisutils.algs.common.MPoint;
import ru.ts.gisutils.algs.common.MRect;

import java.awt.image.BufferedImage;

import java.awt.geom.Point2D;
import java.awt.*;
import java.util.LinkedList;
import java.util.List;

/**
 * Генератор растра
 * Usage:
 * 	1. Загружается проект и инициализируется генератор растра с промощью вызова конструктора
	2.(Опционально) имплементируется интерфейс IImageScaleGetPut для того что бы сохранять и  сгенерированные картинки и описатели растровых слоев
 * 	3. Вызывается один из методов генерации растровых картинок
 *
 */
public class Generator
{
	protected  IProjContext project;
	protected IProjConverter converter;//Конвертер
	protected double dScale;
	protected MPoint drwsz;

	protected MRect wholerect;

	public Generator(IProjContext project, IProjConverter converter,MRect wholerect,double dScale, MPoint drwsz) throws Exception
	{

		this.project = project;
		this.converter = converter;
		this.dScale = dScale;
		this.drwsz = drwsz;

//		java.util.List<ILayer> layers = project.getLayerList();
//		if (layers.size() != 0)
//			wholerect = null;
//		for (ILayer layer : layers)
//			wholerect = layer.getMBBLayer(wholerect);

		this.wholerect=wholerect;

	}

	public int[] getMaxiXjY(int kScale) throws Exception
	{
		final IProjConverter l_converter =(IProjConverter) converter.createCopyConverter();
		l_converter.getAsScaledConverterCtrl().increaseMap(Math.pow(dScale,kScale));
		MRect drawRect = l_converter.getDstRectByRect(wholerect);//drawRect относительно экрана, поэтому смещение

		return new int[]{(int)Math.ceil(drawRect.getWidth()/drwsz.getX()),(int)Math.ceil(drawRect.getHeight()/drwsz.getY())};
	}

	public int[][] getMaxsiXjY(int[] kScaleInterval) throws Exception
	{
		List<int[]> rcl=new LinkedList<int[]>();
		for (int k = kScaleInterval[0]; k < kScaleInterval[1]; k++)
			rcl.add(getMaxiXjY(k));
		return rcl.toArray(new int[rcl.size()][]);
	}

	public MPoint[] calcBindPoints(IProjConverter l_converter,MPoint[] rbnd) throws Exception
	{
		MPoint lbnd0;
		MPoint lbnd1;

		if (l_converter instanceof IRProjectConverter)
		{
			lbnd0 = (((IRProjectConverter) l_converter).getSync2DstConverter()).getPointByDstPoint(rbnd[0]);
			lbnd1=((IRProjectConverter)l_converter).getSync2DstConverter().getPointByDstPoint(rbnd[1]);
		}
		else
		{
			lbnd0 = l_converter.getPointByDstPoint(rbnd[0]);
			Point2D.Double blbnd0=new CrdConverterFactory.RotateConverter(l_converter.getAsRotateConverter().getRotMatrix()).getDstPointByPointD(lbnd0);
			lbnd0=new MPoint(blbnd0);
			lbnd1=l_converter.getPointByDstPoint(rbnd[1]);
			Point2D.Double blbnd1=new CrdConverterFactory.RotateConverter(l_converter.getAsRotateConverter().getRotMatrix()).getDstPointByPointD(lbnd1);
			lbnd1=new MPoint(blbnd1);
		}
		return new MPoint[]{lbnd0,lbnd1};
	}

	public void drawVectorImages(int[][] iXInterval,int[][] jYInterval,int[] kScaleInterval, IImageScaleGetPut imgScaleGetPut) throws Exception
	{
		final IProjConverter l_converter =(IProjConverter) converter.createCopyConverter();
		l_converter.getAsScaledConverterCtrl().increaseMap(Math.pow(dScale,kScaleInterval[0]));

		for (int kScale=kScaleInterval[0];kScale<kScaleInterval[1];kScale++)
		{
			MRect drawRect = l_converter.getDstRectByRect(wholerect);//drawRect относительно экрана, поэтому смещение
			//на +drawRect.p1, приведет ViewPort к левому верхнему углу карты
			MPoint lp=drawRect.p1;
			l_converter.getAsShiftConverter().recalcBindPointByDrawDxDy(new double[]{lp.x,lp.y});
			MPoint P0=l_converter.getAsShiftConverter().getBindP0();

			for (int ix=iXInterval[kScale-kScaleInterval[0]][0];ix<iXInterval[kScale-kScaleInterval[0]][1];ix++)
			{
				l_converter.getAsShiftConverter().setBindP0(P0);
				l_converter.getAsShiftConverter().recalcBindPointByDrawDxDy(new double[]{ix*drwsz.getX(),jYInterval[kScale-kScaleInterval[0]][0]*drwsz.getY()});
				for (int jy=jYInterval[kScale-kScaleInterval[0]][0];jy<jYInterval[kScale-kScaleInterval[0]][1];jy++)
				{
					BufferedImage buffimg = imgScaleGetPut.get(ix, jy, kScale);
					int[] cntobjs = drawVectorImage(buffimg, new IViewPort()
					{
						public Point getDrawSize() throws Exception
						{
							return new Point((int) Math.ceil(drwsz.x), (int) Math.ceil(drwsz.y));
						}

						public IProjConverter getCopyConverter() throws Exception
						{
							return l_converter;
						}

						public void setCopyConverter(IProjConverter converer) throws Exception
						{
							throw new UnsupportedOperationException();
						}
					});
					imgScaleGetPut.put(buffimg,ix, jy, kScale,cntobjs);
					l_converter.getAsShiftConverter().recalcBindPointByDrawDxDy(new double[]{0,drwsz.getY()});
				}
			}

			MPoint[] rbinds=imgScaleGetPut.getRBindPoints();
			l_converter.getAsShiftConverter().setBindP0(P0);//Обязательно установить точку привязки (Почему?)
			MPoint[] lbinds=calcBindPoints(l_converter,rbinds);
			imgScaleGetPut.saveBindPoints(l_converter.getAsScaledConverterCtrl().getScale(),lbinds,rbinds);
			l_converter.getAsScaledConverterCtrl().increaseMap(dScale);
		}
	}

	public int[] drawVectorImage(BufferedImage buffimg, int iX,int jY,int kScale) throws Exception
	{
		int[] rv = new int[]{0,0,0};
		final IProjConverter l_converter =(IProjConverter) converter.createCopyConverter();
		l_converter.getAsScaledConverterCtrl().increaseMap(Math.pow(dScale,kScale));
		MRect drawRect = l_converter.getDstRectByRect(wholerect);//drawRect относительно экрана, поэтому смещение
		//на +drawRect.p1, приведет ViewPort к левому верхнему углу карты
		MPoint lp=drawRect.p1;


		l_converter.getAsShiftConverter().recalcBindPointByDrawDxDy(new double[]{lp.x,lp.y});
		l_converter.getAsShiftConverter().recalcBindPointByDrawDxDy(new double[]{iX*drwsz.getX(),jY*drwsz.getY()});

		rv=drawVectorImage(buffimg,new IViewPort()
		{
			public Point getDrawSize() throws Exception
			{
				return new Point((int)Math.ceil(drwsz.x),(int)Math.ceil(drwsz.y));
			}

			public IProjConverter getCopyConverter() throws Exception
			{
				return l_converter;
			}

			public void setCopyConverter(IProjConverter converer) throws Exception
			{
				throw new UnsupportedOperationException();
			}
		});
		return rv;
	}

	/**
	 * Для того что бы получить изображение растра
	 * @param buffimg - куда рисовать растр
	 * @param vport - вью порт
	 * @throws Exception -
	 * @return - counts of draw objects
	 */
	protected int[] drawVectorImage(BufferedImage buffimg, IViewPort vport) throws Exception
	{



		long tm=System.currentTimeMillis();

		int[] rv = new int[]{0,0,0};
		if (buffimg!=null)
		{
			java.util.List<ILayer> layers = project.getLayerList();
			Graphics graphics = buffimg.getGraphics();
			for (ILayer layer : layers)
			{

				int[] rvv = layer.paintLayer(graphics,vport);
				for (int i = 0; i < rv.length; i++)
					rv[i]+= rvv[i];
			}
		}
		System.out.print("tm:"+(System.currentTimeMillis()-tm)+" ");
		System.out.print("pnts = " + rv[0]+" ");
		System.out.print("lines = " + rv[1]+" ");
		System.out.println("poly = " + rv[2]);

		return rv;
	}

	public static interface IImageScaleGetPut
	{
		 BufferedImage get(int iX,int jY,int kScale) throws Exception;
		 void put(BufferedImage buffimg,int iX,int jY,int kScale,int[] ocnts) throws Exception;

		MPoint[] getRBindPoints() throws Exception;
		void saveBindPoints(MPoint currentScale,MPoint[] lbnd,MPoint[] rbnd) throws Exception;
	}

}
