package ru.ts.tst;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.*;
import java.io.File;
import java.io.IOException;

/**
 * Генератор растровых тестов для последующего их наложения в браузере
 */
public class TestRasterGenerator
{
	public static void main(String[] args) throws IOException
	{
		String dir="D:/";
		BufferedImage buffimg01 = new BufferedImage(250, 250, BufferedImage.TYPE_INT_ARGB);
		BufferedImage buffimg02 = new BufferedImage(250, 250, BufferedImage.TYPE_INT_ARGB);
		BufferedImage buffimg03 = new BufferedImage(250, 250, BufferedImage.TYPE_INT_ARGB);


		Graphics g = buffimg01.getGraphics();
		g.setColor(new Color(0xFFDD3344,true));
		g.fillRect(0,0,250,250);
		ImageIO.write(buffimg01,"PNG",new File(dir+"/b01.png"));

		g = buffimg02.getGraphics();
		g.setColor(new Color(0x0,true));
		g.fillRect(0,0,250,250);

		g.setColor(new Color(0xFF222222,true));
		g.drawLine(0,0,250,250);
		g.drawLine(0,250,250,0);
		ImageIO.write(buffimg02,"PNG",new File(dir+"/b02.png"));

		g = buffimg03.getGraphics();
		g.setColor(new Color(0x0,true));
		g.fillRect(0,0,250,250);
		g.setColor(new Color(0x6622FF44,true));
		int d=40;
		int[] x=new int[]{0,250-d,250,250,d,0};
		int[] y=new int[]{d,250,250,250-d,0,0};
		g.fillPolygon(x,y,x.length);
		ImageIO.write(buffimg03,"PNG",new File(dir+"/b03.png"));
	}

}
