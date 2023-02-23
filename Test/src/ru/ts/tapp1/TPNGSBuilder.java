package ru.ts.tapp1;

import ru.ts.apps.sapp.app.InParamsApp;
import ru.ts.toykernel.proj.xml.def.XMLProjBuilder;
import ru.ts.toykernel.app.xml.IXMLBuilderContext;
import ru.ts.toykernel.gui.IViewPort;
import ru.ts.toykernel.drawcomp.ILayer;
import ru.ts.toykernel.converters.IProjConverter;
import ru.ts.toykernel.converters.ILinearConverter;
import ru.ts.toykernel.pcntxt.IProjContext;
import ru.ts.toykernel.consts.KernelConst;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.imageio.ImageIO;
import java.io.*;
import java.util.List;
import java.awt.image.BufferedImage;
import java.awt.*;

import org.xml.sax.InputSource;

/**
 * проверка генерации в PNG слоев проекта
 */
public class TPNGSBuilder
{
	private IProjConverter converter;
	private IProjContext projectctx;

	public TPNGSBuilder(IProjConverter converter, IProjContext projectctx)
	{
		this.converter = converter;
		this.projectctx = projectctx;
	}

	public static void main(String[] args) throws Exception
	{
		InParamsApp params = new InParamsApp();
		params.translateOptions(args);

		String xmlfilepath=params.get(InParamsApp.optarr[InParamsApp.O_wfl]);

		SAXParser parser= SAXParserFactory.newInstance().newSAXParser();
		Reader rd=new InputStreamReader(new FileInputStream(xmlfilepath),"WINDOWS-1251");

		XMLProjBuilder builder = new XMLProjBuilder();
		parser.parse(new InputSource(rd), builder.getProjBuilderHandler(parser.getXMLReader()));

		IXMLBuilderContext bcontext = builder.getBuilderContext();

		List<ILinearConverter> convs = bcontext.getBuilderByTagName(KernelConst.CONVERTER_TAGNAME).getLT();
		List<IProjContext> contexts = bcontext.getBuilderByTagName(KernelConst.PROJCTXT_TAGNAME).getLT();

		TPNGSBuilder pngb = new TPNGSBuilder((IProjConverter) convs.get(3), contexts.get(0));
		pngb.drawVectorImage();

	}

	public IViewPort getViewPort()
	{
		return new IViewPort()
		{
			public Point getDrawSize()
			{
				return new Point(800,800);
			}

			public IProjConverter getCopyConverter()
			{
				return (IProjConverter) converter.createCopyConverter();
			}

			public void setCopyConverter(IProjConverter _converer)
			{
				converter= (IProjConverter) _converer.createCopyConverter();
			}

		};
	}

	protected void drawVectorImage() throws Exception
	{

		long tm=System.currentTimeMillis();

		IViewPort vport = getViewPort();
		Point sz = vport.getDrawSize();
		BufferedImage buffimg=new BufferedImage(sz.x,sz.y, BufferedImage.TYPE_INT_ARGB);
		Graphics graphics = buffimg.getGraphics();

        List<ILayer> layers = projectctx.getLayerList();
		int[] rv=new int[]{0,0,0};
		for (ILayer layer : layers)
		{
			int[] rvv = layer.paintLayer(graphics, vport);
			for (int i = 0; i < rv.length; i++)
				rv[i]+= rvv[i];
		}
//		System.out.print("Picture panel tm:"+(System.currentTimeMillis()-tm)+" ");
//		tm=System.currentTimeMillis();

		OutputStream os = new FileOutputStream("D:/tst.png");//new BufferedOutputStream();
		try
		{
			ImageIO.write(buffimg,"PNG", os);
		} catch (IOException e)
		{
			e.printStackTrace();
		}
		os.flush();
		os.close();
		System.out.print("tm2:"+(System.currentTimeMillis()-tm)+" ");

//		System.out.print("pnts = " + rv[0]+" ");
//		System.out.print("lines = " + rv[1]+" ");
//		System.out.println("poly = " + rv[2]);

	}
}
