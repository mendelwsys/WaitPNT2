package ru.ts.drwb;

import javax.imageio.ImageIO;
import java.io.IOException;
import java.io.File;
import java.io.FileInputStream;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;
import java.awt.image.WritableRaster;
import java.awt.*;

/**
 * Заместить все белые точки прозрачными
 */
public class Drwbt
{
	public static void main(String[] args) throws IOException
	{

		String s = "D:\\Vlad\\JavaProj\\ToyGIS\\RasterProvider\\web\\sldtst\\pict\\";
		String pictfl = "vmover.png";
		BufferedImage buff = ImageIO.read(new FileInputStream(s + pictfl));

		BufferedImage buffimg01 = new BufferedImage(buff.getWidth(), buff.getHeight(), BufferedImage.TYPE_INT_ARGB);
		Graphics g = buffimg01.getGraphics();
		g.setColor(new Color(0, true));
		g.drawImage(buff, 0, 0, new ImageObserver()
		{
			public boolean imageUpdate(Image img, int infoflags, int x, int y, int width, int height)
			{
				return false;
			}
		});
		g.fillRect(0, 0, buffimg01.getHeight(), buffimg01.getWidth());

		WritableRaster raster = buffimg01.getRaster();

		int mx = raster.getMinX();
		int my = raster.getMinY();

		int wx = raster.getWidth();
		int hy = raster.getHeight();

		for (int x = mx; x < mx + wx; x++)
			for (int y = my; y < my + hy; y++)
			{
				int[] ints = new int[4];
				raster.getPixel(x, y, ints);

				boolean b = false;
				for (int colcomp : ints)
					if (b = (colcomp != 255))
						break;
				if (!b)
				{
//					for (int i = 0; i < ints.length; i++)
						ints[3] = 0;
					raster.setPixel(x, y, ints);
				}
			}


		ImageIO.write(buffimg01, "PNG", new File(s + pictfl));

	}

}
