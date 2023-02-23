package ru.ts.apps.rbuilders.app0;

import ru.ts.toykernel.factory.BaseInitAble;
import ru.ts.toykernel.proj.xml.def.XMLProjBuilder;
import ru.ts.toykernel.app.xml.IXMLBuilderContext;
import ru.ts.apps.rbuilders.app0.kernel.rapp.RasterApp;
import ru.ts.gisutils.algs.common.MPoint;
import ru.ts.factory.IParam;
import ru.ts.utils.data.Pair;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.Reader;
import java.io.InputStreamReader;
import java.io.FileInputStream;
import java.io.File;
import java.util.*;
import java.awt.image.BufferedImage;

import org.xml.sax.InputSource;

/**
 * Встариваемый в проект класс
 * ru.ts.apps.rbuilders.app0.AppGenerator0
 */
public class AppGenerator0 extends BaseInitAble
{
	public static final Long LTIMEOUT = 5 * 60 * 1000L;
	protected static final Map<String, Pair<RasterApp, Pair<Long, Long>>> xml2proj = new HashMap<String, Pair<RasterApp, Pair<Long, Long>>>();
	protected static boolean terminate = false;
	protected static Thread thr;
	protected String xmlfilepath; //Проект для генерации
	protected double dScale = 1.3;
	protected Generator gener;
	MPoint drwsz = new MPoint(400, 400);
	public AppGenerator0(String xmlfilepath, double dScale, MPoint drwsz, Generator gener)
	{
		this.xmlfilepath = xmlfilepath;
		this.dScale = dScale;
		this.drwsz = drwsz;
		this.gener = gener;
	}

	public AppGenerator0()
	{
	}

	public static void setTerminate() throws Exception
	{
		AppGenerator0.terminate = true;
		if (thr!=null)
			thr.join();
		thr=null;
	}

	static void startServiceThread()
	{
		terminate=false;
		(thr=new Thread()
		{
			public void run()
			{

				while (!terminate)
				{
					try
					{
						Set<String> fnames;
						synchronized (xml2proj)
						{
							fnames = new HashSet<String>(xml2proj.keySet());
						}

						for (String fname : fnames)
						{
							File fl = new File(fname);
							synchronized (xml2proj)
							{
								Pair<Long, Long> pr = xml2proj.get(fname).second;
								if (!fl.exists() || fl.lastModified() > pr.first || pr.second + LTIMEOUT < System.currentTimeMillis())
									xml2proj.remove(fname);
							}
						}
						for (int i=0;i<10;i++)
						{
							if (terminate) return;
							sleep(100);
						}
					}
					catch (Exception e)
					{
						e.printStackTrace();
					}
				}
			}
		}).start();
	}

	public void initgenerator() throws Exception
	{

		long tm= System.currentTimeMillis();
		RasterApp app = null;
		synchronized (xml2proj)
		{
			Pair<RasterApp, Pair<Long, Long>> pr = xml2proj.get(xmlfilepath);
			if (pr!=null)
			{
				app = pr.first;
				pr.second.second=System.currentTimeMillis();
			}
		}


		File file = new File(xmlfilepath);

		if (app == null && file.exists())
		{
			synchronized (LTIMEOUT)
			{
				synchronized (xml2proj)
				{
					Pair<RasterApp, Pair<Long, Long>> pr = xml2proj.get(xmlfilepath);
					if (pr!=null)
					{
						app = pr.first;
						pr.second.second=System.currentTimeMillis();
					}
				}

				if (app == null) //Нет тогда размещаем
				{

					SAXParser parser = SAXParserFactory.newInstance().newSAXParser();
					Reader rd = new InputStreamReader(new FileInputStream(file), "WINDOWS-1251");

					XMLProjBuilder builder = new XMLProjBuilder();
					parser.parse(new InputSource(rd), builder.getProjBuilderHandler(parser.getXMLReader()));

					IXMLBuilderContext bcontext = builder.getBuilderContext();

					List apps = bcontext.getBuilderByTagName(ModuleConst.APPLICATION_TAGNAME).getLT();
					if (apps != null && apps.size() > 0)
					{
						app = ((RasterApp) apps.get(0));
						synchronized (xml2proj)
						{
							xml2proj.put(xmlfilepath, new Pair<RasterApp, Pair<Long, Long>>(app, new Pair<Long, Long>(file.lastModified(), System.currentTimeMillis())));
						}
					}
				}
			}
		}

		if (app != null)
		{
			gener = new Generator(app.getProjectctx(), app.getConverter(),app.getWholerect(),dScale, drwsz);
			System.out.println("End of init generator tm:"+(System.currentTimeMillis()-tm));
		}
		else
			System.out.println("Can't init generator loaded application is null");
	}

	synchronized public BufferedImage generateImage(BufferedImage buffimg, int iX, int jY, int kScale)
			throws Exception
	{
		long tm = System.currentTimeMillis();
		if (buffimg == null)
			buffimg = new BufferedImage((int) drwsz.x, (int) drwsz.y, BufferedImage.TYPE_INT_ARGB);

		if (gener != null)
			gener.drawVectorImage(buffimg, iX, jY, kScale);
		System.out.println("tm_imgg:" + (System.currentTimeMillis() - tm));
//		if (rv[0]!=0 || rv[1]!=0 || rv[2]!=0)
		return buffimg;
//		return null;
	}

	public Object[] init(Object... obj) throws Exception
	{
		super.init(obj);
//Инициалиазация генератора (Загрузка проекиа и подготовка к рисованию картинок)
		initgenerator();
		return null;
	}

	private MPoint setPt(IParam attr)
			throws Exception
	{
		String pointsBind = ((String) attr.getValue());
		String[] splited = pointsBind.split(" ");
		try
		{
			return new MPoint(Integer.parseInt(splited[0]), Integer.parseInt(splited[1]));
		}
		catch (NumberFormatException e)
		{
			throw new Exception("Error prasing of setPr", e);
		}
	}

	public Object init(Object obj) throws Exception
	{
		IParam attr = (IParam) obj;
		if (attr.getName().equalsIgnoreCase("PXMLFILEPATH"))
			xmlfilepath = (String) attr.getValue();
		else if (attr.getName().equalsIgnoreCase("dScale"))
		{
			try
			{
				dScale = Double.parseDouble((String) attr.getValue());
			}
			catch (NumberFormatException e)
			{
				e.printStackTrace();
			}
		}
		else if (attr.getName().equalsIgnoreCase("drwSz"))
			drwsz = setPt(attr);
		return null;
	}
}
