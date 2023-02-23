package ru.ts.apps.rbuilders.app0;

import ru.ts.toykernel.proj.stream.def.StreamProjImpl;
import ru.ts.toykernel.pcntxt.IProjContext;
import ru.ts.toykernel.pcntxt.IMetaInfoBean;
import ru.ts.toykernel.drawcomp.rules.def.stream.CnStyleRuleFactory;
import ru.ts.toykernel.drawcomp.rules.def.stream.InscriptionRule;
import ru.ts.toykernel.drawcomp.IDrawObjRule;
import ru.ts.toykernel.drawcomp.ILayer;
import ru.ts.factory.DefIFactory;
import ru.ts.toykernel.gui.IViewPort;
import ru.ts.forms.StViewProgress;
import ru.ts.toykernel.converters.*;
import ru.ts.gisutils.algs.common.MPoint;
import ru.ts.gisutils.algs.common.MRect;
import ru.ts.apps.rbuilders.app0.kernel.rules.IncriptionRule2;
import ru.ts.apps.rbuilders.app0.kernel.layers.DrawRasterLayer;

import javax.imageio.ImageIO;
import java.io.*;
import java.awt.image.BufferedImage;
import java.awt.*;
import java.awt.geom.Point2D;
import java.util.List;
import java.util.LinkedList;

/**
 * Конвертирует вектор в растровую информацию
 */
public class Vector2RasterApp
{
	private IProjContext project;
	private MRect wholerect;
	private IProjConverter converter;

	public static void main(String[] args) throws Exception
	{

		InParamsApp params = new InParamsApp();
		params.translateOptions(args);

		String binlayer=params.get(InParamsApp.optarr[InParamsApp.O_wfl]);

		StViewProgress progress = null;//new StViewProgress(Enc.get("DOWNLOAD_PROJECT"));
		StreamProjImpl proj = new StreamProjImpl(binlayer,new CnStyleRuleFactory()
		 {
			 public IDrawObjRule createByTypeName(String typeRule) throws Exception
			 {
				 if (typeRule.equals(InscriptionRule.RULETYPENAME))
					 return new IncriptionRule2();
				 return super.createByTypeName(typeRule);
			 }
		 },null,null,new DefIFactory<ILayer>()
			{
				public ILayer createByTypeName(String typeStorage) throws Exception
				{
					return new DrawRasterLayer();
				}
			},null,null, progress);

		File fl = new File(binlayer);
		DataInputStream dis = new DataInputStream(new BufferedInputStream(new FileInputStream(fl)));
		proj.loadFromStream(dis);
		Vector2RasterApp app = new Vector2RasterApp();
		app.setProjectContext(proj);
		String dir="C:\\MAPDIR\\Rasters7\\raster";
		app.process(dir,11);
	}

	protected void process(String baseDir,int cnt)
			throws Exception
	{

		boolean israstergener=false;
		converter.getAsScaledConverterCtrl().increaseMap(2.5);

		new File(baseDir).mkdir();
		new File(baseDir+"_d").mkdir();
		new File(baseDir+"_xml").mkdir();
		PrintWriter descwr=new PrintWriter(new FileOutputStream(baseDir+"_xml\\add.xml"));

		MPoint pt=converter.getAsScaledConverterCtrl().getScale();
		double wasK=-1,K=Math.min(pt.x,pt.y);
		System.out.println("K = " + K);
		double multK=1.3;
		Point rbnd0=new Point(0,0);
		Point rbnd1=new Point(110,110);

		MPoint lbnd0=new MPoint(rbnd0);
		MPoint lbnd1=new MPoint(rbnd1);


		for (int k=0;k<cnt;k++)
		{
			new File(baseDir+"\\"+k+"\\").mkdir();
			new File(baseDir+"_d\\"+k+"\\").mkdir();

			MRect drawRect = converter.getDstRectByRect(wholerect);//drawRect относительно экрана, поэтому смещение
			//на +drawRect.p1, приведет ViewPort к левому верхнему углу карты
			MPoint lp=drawRect.p1;
			converter.getAsShiftConverter().recalcBindPointByDrawDxDy(new double[]{lp.x,lp.y});
			MPoint P0=converter.getAsShiftConverter().getBindP0();
			int[] drawSize = getDrawSize();//Размер элемента рисования.

			List<List<String>> imgarr_y_x=new LinkedList<List<String>>();

			int i=0,x=0,j=0,y=0;
			if (israstergener)
			{
				for (i=0,x=0;x<=drawRect.p4.x+1;x+= drawSize[0],i++)
				{
					for (j=0,y=0;y<=drawRect.p4.y+1;y+=drawSize[1],j++)
					{
						BufferedImage buffimg = new BufferedImage(drawSize[0], drawSize[1], BufferedImage.TYPE_INT_ARGB);
						drawVectorImage(buffimg);
						try
						{

							String imgname="img_"+j+"_"+i+".png";
							if (imgarr_y_x.size()<=j)
								imgarr_y_x.add(new LinkedList<String>());
							imgarr_y_x.get(j).add(imgname);

							ImageIO.write(buffimg,"PNG", new FileOutputStream(baseDir+"\\"+k+"\\"+imgname));
						}
						catch (IOException e)
						{
							e.printStackTrace();
						}
						converter.getAsShiftConverter().recalcBindPointByDrawDxDy(new double[]{0,drawSize[1]});
					}
					converter.getAsShiftConverter().setBindP0(P0);
					converter.getAsShiftConverter().recalcBindPointByDrawDxDy(new double[]{(i+1)*drawSize[0],0});
				}
			}

			if (converter instanceof IRProjectConverter)
			{
				lbnd0 = (((IRProjectConverter) converter).getSync2DstConverter()).getPointByDstPoint(rbnd0);
				lbnd1=((IRProjectConverter)converter).getSync2DstConverter().getPointByDstPoint(rbnd1);
			}
			else
			{
				lbnd0 = converter.getPointByDstPoint(rbnd0);
				Point2D.Double blbnd0=new CrdConverterFactory.RotateConverter(converter.getAsRotateConverter().getRotMatrix()).getDstPointByPointD(lbnd0);
				lbnd0=new MPoint(blbnd0);
				lbnd1=converter.getPointByDstPoint(rbnd1);
				Point2D.Double blbnd1=new CrdConverterFactory.RotateConverter(converter.getAsRotateConverter().getRotMatrix()).getDstPointByPointD(lbnd1);
				lbnd1=new MPoint(blbnd1);
			}

			descwr.println("\t\t\t<param Nm=\"rast\" Val=\""+k+"\"/>\n" +
					"\t\t\t<param Nm=\"path\" Val=\""+baseDir+"_d\\"+k+"\\bgdescn.txt"+"\"/>\n" +
					"\t\t\t<param Nm=\"scale\" Val=\""+wasK+" "+(K+K/30)+"\"/>\n" +
					"\t\t\t<param Nm=\"pr0\"\tVal=\""+lbnd0.x+" "+lbnd0.y+" "+rbnd0.x+" "+rbnd0.y+"\"/>\n" +
					"\t\t\t<param Nm=\"pr1\"\tVal=\""+lbnd1.x+" "+lbnd1.y+" "+rbnd1.x+" "+rbnd1.y+"\"/>");

			wasK=K+K/30;
			K*=multK;

			rbnd0.x*=multK;
			rbnd0.y*=multK;

			rbnd1.x*=multK;
			rbnd1.y*=multK;


			PrintWriter pwr=new PrintWriter(new FileOutputStream(baseDir+"_d\\"+k+"\\bgdescn.txt"));
			pwr.println("Y X");
			pwr.println(j+" "+i);
			pwr.println(j*drawSize[1]+" "+i*drawSize[0]);

			for (List<String> imgarr_x : imgarr_y_x)
			{
				pwr.print("  ");
				for (String img : imgarr_x)
					pwr.print(img+" ");
				pwr.print("\n");
			}
			pwr.print("\n\n" +baseDir+"\\"+k+"\\\n");
			pwr.flush();
			pwr.close();

			List<ILayer> layers = project.getLayerList();
			for (ILayer layer : layers)
				layer.getDrawRule().resetPainters();
			converter.getAsShiftConverter().setBindP0(P0);
			converter.getAsScaledConverterCtrl().increaseMap(multK);
		}
		descwr.flush();
		descwr.close();

	}

	protected void setProjectContext(IProjContext project)
			throws Exception
	{
		this.project=project;
		converter = (IProjConverter) new CrdConverterFactory().createByTypeName(CrdConverterFactory.LinearConverterAB.LINEARPROJAB);
		setInitScale();
	}

	protected void setInitScale() throws Exception
	{
        List<ILayer> layers = project.getLayerList();
        if (layers.size() != 0)
			wholerect = null;
		for (ILayer layer : layers)
			wholerect = layer.getMBBLayer(wholerect);

		IMetaInfoBean metaInfo = project.getProjMetaInfo();
		try
		{
				//Вычисления массштаба производим исходя из прямоугольника индекса
				int[] size = getFullDrawSize();
				converter.getAsScaledConverterCtrl().recalcScale(wholerect, size);
		}
		catch (Exception e)
		{//
			e.printStackTrace();
		}

		IScaledConverter scaledConv = converter.getAsScaledConverter();
		IShiftConverter shiftconverter = converter.getAsShiftConverter();
		MPoint pnt0 = shiftconverter.getBindP0();
		MPoint scale = scaledConv.getScale();
		MPoint drawpnt = new MPoint(pnt0.x * scale.x, pnt0.y  * scale.y);
		converter = new CrdConverterFactory.LinearConverterRSS
				(
						converter.getAsRotateConverter().getRotMatrix(),
						scaledConv.getScale(),
						drawpnt
				);
	}

	private int[] getFullDrawSize()
	{
		return new int[]{1000,1000};
	}

	protected void drawVectorImage(BufferedImage buffimg) throws Exception
	{

		long tm=System.currentTimeMillis();

		Graphics graphics = buffimg.getGraphics();

        java.util.List<ILayer> layers = project.getLayerList();
		int[] rv=new int[]{0,0,0};
		for (ILayer layer : layers)
		{

			int[] rvv = layer.paintLayer(graphics,
			new IViewPort()
			{
				public Point getDrawSize()
				{
					int[] dsz = Vector2RasterApp.this.getDrawSize();
					return new Point(dsz[0], dsz[1]);
				}

				public IProjConverter getCopyConverter()
				{
					return (IProjConverter) converter;
				}

				public void setCopyConverter(IProjConverter _converer)
				{
					converter= (IProjConverter) _converer.createCopyConverter();
				}

			});
			for (int i = 0; i < rv.length; i++)
				rv[i]+= rvv[i];
		}
		System.out.print("tm:"+(System.currentTimeMillis()-tm)+" ");
		System.out.print("pnts = " + rv[0]+" ");
		System.out.print("lines = " + rv[1]+" ");
		System.out.println("poly = " + rv[2]);

	}

	private int[] getDrawSize()
	{
		return new int[]{400,400};
	}
}