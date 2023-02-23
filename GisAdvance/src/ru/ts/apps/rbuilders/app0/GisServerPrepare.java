package ru.ts.apps.rbuilders.app0;

import ru.ts.toykernel.proj.xml.def.XMLProjBuilder;
import ru.ts.toykernel.app.xml.IXMLBuilderContext;
import ru.ts.apps.rbuilders.app0.kernel.rapp.RasterApp;
import ru.ts.gisutils.algs.common.MPoint;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.imageio.ImageIO;
import java.io.*;
import java.util.List;
import java.util.LinkedList;
import java.awt.image.BufferedImage;

import org.xml.sax.InputSource;

/**
 * Генератор размещения растровых описателей
 */
public class GisServerPrepare
{
	protected MPoint drwsz;
	protected double dScale;
	protected int[] kScaleInterval;
	protected String xmlfilepath;
	protected String baseDir;

	public GisServerPrepare(String xmlfilepath,final String baseDir, MPoint drwsz,double dScale,int[] kScaleInterval)
	{

		this.xmlfilepath = xmlfilepath;
		this.baseDir = baseDir;
		this.drwsz = drwsz;
		this.dScale = dScale;
		this.kScaleInterval = kScaleInterval;
	}

	public static void main(String[] args) throws Exception
	{
			InParamsApp params = new InParamsApp();
			params.translateOptions(args);
			String xmlfilepath = params.get(InParamsApp.optarr[InParamsApp.O_wfl]);

			String baseDir = "C:/MAPDIR/Rasters7/raster";
			MPoint drwsz = new MPoint(400, 400);
			double dScale = 1.3;
			int[] kScaleInterval = {0, 8};
			new GisServerPrepare(xmlfilepath,baseDir,drwsz,dScale,kScaleInterval).generateRastersDescriptors();
	}

	public void generateRastersDescriptors() throws Exception
	{
		SAXParser parser = SAXParserFactory.newInstance().newSAXParser();
		Reader rd = new InputStreamReader(new FileInputStream(xmlfilepath), "WINDOWS-1251");

		XMLProjBuilder builder = new XMLProjBuilder();
		parser.parse(new InputSource(rd), builder.getProjBuilderHandler(parser.getXMLReader()));

		IXMLBuilderContext bcontext = builder.getBuilderContext();

		List apps = bcontext.getBuilderByTagName(ModuleConst.APPLICATION_TAGNAME).getLT();
		if (apps != null && apps.size() > 0)
		{
			RasterApp app = ((RasterApp) apps.get(0));


			Generator gener = new Generator(app.getProjectctx(), app.getConverter(),app.getWholerect(),dScale, drwsz);


			int[][] msiXjY = gener.getMaxsiXjY(kScaleInterval);

			final int[][] iXInterval = new int[msiXjY.length][];
			final int[][] jYInterval = new int[msiXjY.length][];
			for (int i = 0; i < iXInterval.length; i++)
			{
				System.out.println("cnt:" + msiXjY[i][0] * msiXjY[i][1]);
				iXInterval[i] = new int[]{0, msiXjY[i][0]};
				jYInterval[i] = new int[]{0, msiXjY[i][1]};
			}

			long tm= System.currentTimeMillis();

			gener.drawVectorImages(iXInterval, jYInterval, kScaleInterval, new Generator.IImageScaleGetPut()
			{

				protected int jYMax,iXMax;
				List<List<String>> imgarr_y_x = new LinkedList<List<String>>();
				double wasK = -1;
				MPoint[] bpnst = new MPoint[]{new MPoint(0, 0), new MPoint(1000, 1000)};
				int currentN;

				{
					try
					{
						new File(baseDir).mkdirs();
						new File(baseDir + "_d").mkdirs();
						new File(baseDir + "_xml").mkdirs();
						new FileOutputStream(baseDir + "_xml/add.xml").close();
					}
					catch (Exception e)
					{
						e.printStackTrace();
					}
				}

				public BufferedImage get(int iX, int jY, int kScale)
				{
					currentN = kScale;
					if (iX==10 && jY==10 && kScale==3)
						return new BufferedImage((int)drwsz.x,(int)drwsz.y, BufferedImage.TYPE_INT_ARGB);
					return null;
				}

				public void put(BufferedImage buffimg, int iX, int jY, int kScale, int[] ocnts) throws IOException
				{
					String dir = baseDir  + "/" +  String.valueOf(kScale);
					new File(dir).mkdirs();
					String imgname = "img_" + jY + "_" + iX + ".png";
					if (buffimg != null && (ocnts[0] > 0 || ocnts[1] > 0 || ocnts[2] > 0))
						ImageIO.write(buffimg, "PNG", new File(dir + "/" + imgname));


					if (imgarr_y_x.size() <= jY)
						imgarr_y_x.add(new LinkedList<String>());
					imgarr_y_x.get(jY).add(imgname);

					if (jYMax < jY)
						jYMax = jY;
					if (iXMax < iX)
						iXMax = iX;
				}

				public MPoint[] getRBindPoints()
				{
					return bpnst;
				}

				public void saveBindPoints(MPoint currentScale, MPoint[] lbnd, MPoint[] rbnd) throws Exception
				{
					String dir = baseDir  + "_d/" +  String.valueOf(currentN);
					new File(dir).mkdirs();

					double nextScale;
					if (wasK==-1)
						nextScale=(currentScale.x+currentScale.x*dScale)/2;
					else
						nextScale=(wasK+currentScale.x*dScale)/2;

					PrintWriter descwr = new PrintWriter(new FileOutputStream(baseDir + "_xml/add.xml", true));
					descwr.println("\t\t\t<param Nm=\"rast\" Val=\"" + currentN + "\"/>\n" +
							"\t\t\t<param Nm=\"path\" Val=\"" + baseDir + "_d/" + currentN + "/bgdescn.txt" + "\"/>\n" +
							"\t\t\t<param Nm=\"scale\" Val=\"" + wasK + " " + nextScale + "\"/>\n" +
							"\t\t\t<param Nm=\"pr0\"\tVal=\"" + lbnd[0].x + " " + lbnd[0].y + " " + rbnd[0].x + " " + rbnd[0].y + "\"/>\n" +
							"\t\t\t<param Nm=\"pr1\"\tVal=\"" + lbnd[1].x + " " + lbnd[1].y + " " + rbnd[1].x + " " + rbnd[1].y + "\"/>");

					wasK = nextScale;
					descwr.flush();
					descwr.close();

					PrintWriter pwr = new PrintWriter(new FileOutputStream(baseDir + "_d/" + String.valueOf(currentN) + "/bgdescn.txt"));

					jYMax++;
					iXMax++;

					pwr.println("Y X");
					pwr.println(jYMax + " " + iXMax);
					pwr.println((int) (jYMax * drwsz.x) + " " + (int) (iXMax * drwsz.y));

					for (List<String> imgarr_x : imgarr_y_x)
					{
						pwr.print("  ");
						for (String img : imgarr_x)
							pwr.print(img + " ");
						pwr.print("\n");
					}
					pwr.print("\n\n" + baseDir + "/" + currentN + "/\n");
					pwr.flush();
					pwr.close();

					jYMax = iXMax = 0;
					imgarr_y_x.clear();

					bpnst[0].x*=dScale;
					bpnst[0].y*=dScale;

					bpnst[1].x*=dScale;
					bpnst[1].y*=dScale;

				}
			});
			System.out.println("tm:"+ (System.currentTimeMillis()-tm));
		}
	}



}
