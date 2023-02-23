package ru.ts.tst;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.io.File;
import java.io.IOException;

/**
 * Генератор растровых тестов для последующего их наложения в браузере
 */
public class TestRasterGenerator4
{
	public static void main(String[] args) throws IOException
	{
		String dir="c:/";
		BufferedImage buffimg01 = new BufferedImage(250, 250, BufferedImage.TYPE_INT_ARGB);


		Graphics2D g = (Graphics2D) buffimg01.getGraphics();
		g.setColor(new Color(0xFFDD3344,true));
		g.fillRect(0,0,250,250);


		g.setColor(new Color(0xFF000000,true));
		int ax = 100;
		int ay = 100;

		g.drawString("ASDFGT", ax, ay);
		AffineTransform trans = AffineTransform.getRotateInstance(-Math.PI / 2, ax, ay);
		g.setTransform(trans);
		
		g.drawString("ASDFGT", ax, ay);
		ImageIO.write(buffimg01,"PNG",new File(dir+"/b01.png"));
	}

}