package ru.ts.drwb;

import javax.imageio.ImageIO;
import java.io.IOException;
import java.io.File;
import java.awt.image.BufferedImage;
import java.awt.*;

/**
 * Нарисовать полупрозрачную картинку
 */
public class Drwbt3
{
	public static void main(String[] args) throws IOException
	{

		BufferedImage buffimg01 = new BufferedImage(30,30, BufferedImage.TYPE_INT_ARGB);
		Graphics g = buffimg01.getGraphics();
		g.setColor(new Color(0x88AF1010,true));
		g.fillRect(0, 0, buffimg01.getHeight(), buffimg01.getWidth());
		ImageIO.write(buffimg01, "PNG", new File("D:/sel.png"));

	}

}