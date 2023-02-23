package ru.ts.apps.rbuilders.app0.kernel.geom;

import ru.ts.toykernel.geom.def.RasterObject;
import ru.ts.gisutils.algs.common.MPoint;
import ru.ts.utils.data.Pair;
import ru.ts.apps.rbuilders.app0.AppGenerator0;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.InputStream;
import java.io.FileInputStream;

/**
 * Объект позволяет генерировать растровые объекты налету
 */

public class GRasterObject extends RasterObject
{
	private AppGenerator0 generator;
	private int[] index;

	public GRasterObject(MPoint projP0, MPoint projP1, Pair<String, String> url2name, String curveId, AppGenerator0 generator,int[] index)
	{
		super(projP0, projP1, url2name, curveId);
		this.generator = generator;
		this.index = index;
	}

	protected void generateFile(int kIndex, int iX, int jY, File file)
			throws Exception
	{
		if (!file.exists() && generator!=null)
		{
			BufferedImage rimg = generator.generateImage(null, iX, jY, kIndex);
			if (rimg!=null)
				ImageIO.write(rimg,"PNG",file);
		}
	}

	public Pair<BufferedImage, String> getRawRaster() throws Exception
	{
		Pair<BufferedImage, String> rv=new Pair<BufferedImage, String>(null,url2name.second);
		File input = new File(url2name.first);

		if (url2name.first!=null && !input.exists())
			generateFile(index[0], index[1], index[2],input);

		if (url2name.first!=null && input.exists())
			rv.first= ImageIO.read(input);
		return rv;
	}


	public Pair<InputStream,String> getStreamRawRaster() throws Exception
	{
		Pair<InputStream, String> rv=new Pair<InputStream, String>(null,url2name.second);
		File input = new File(url2name.first);

		if (url2name.first!=null && !input.exists())
			generateFile(index[0], index[1], index[2],input);

		if (url2name.first!=null && input.exists())
			rv.first= new FileInputStream(input);
		return rv;
	}


}
