package ru.ts.drwb;

import javax.imageio.ImageIO;
import java.io.IOException;
import java.io.File;
import java.awt.image.BufferedImage;
import java.awt.*;

/**
 * Нарисовать полупрозрачную картинку
 */
public class Drwbt2
{
	public static void main(String[] args) throws IOException
	{

		String s = "D:\\Vlad\\JavaProj\\ToyGIS\\RasterProvider\\web\\mslder\\pict\\";
		String pictfl = "backgrnd.png";

		BufferedImage buffimg01 = new BufferedImage(20,20, BufferedImage.TYPE_INT_ARGB);
		Graphics g = buffimg01.getGraphics();
		g.setColor(new Color(0x00BD58586F,true));
		g.fillRect(0, 0, buffimg01.getHeight(), buffimg01.getWidth());
		ImageIO.write(buffimg01, "PNG", new File(s + pictfl));

	}

}
