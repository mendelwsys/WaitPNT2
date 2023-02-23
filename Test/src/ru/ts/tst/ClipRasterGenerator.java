package ru.ts.tst;

import org.opengis.geometry.coordinate.Bezier;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.PathIterator;
import java.awt.geom.Path2D;
import java.awt.geom.Rectangle2D;
import java.io.File;
import java.io.IOException;

/**
 * Генератор растровых тестов для последующего их наложения в браузере
 */
public class ClipRasterGenerator
{
	public static void main(String[] args) throws IOException
	{
		String dir="D:/";
		BufferedImage buffimg01 = new BufferedImage(250, 250, BufferedImage.TYPE_INT_ARGB);

		Graphics2D g = (Graphics2D) buffimg01.getGraphics();

		Ellipse2D.Double ell1 = new Ellipse2D.Double(100,100,80,50);

		PathIterator it = ell1.getPathIterator(null);

		//it.currentSegment()

		Ellipse2D.Double ell2 = new Ellipse2D.Double(70,100,80,50);

		Rectangle2D r2d = new Rectangle2D.Double(20, 20, 210, 210);

//Устанавливаем граниицу шейпом, при это пересекающаяся область лепсов принадлежит клипу!!!
//Осталось понять как впихнуть это в наши правила (я имею ввиду клип ареа да еше и с произвольным пас объектом)		
		Path2D.Double p2d = new Path2D.Double(Path2D.WIND_EVEN_ODD);
		p2d.append(r2d,true);


		p2d.append(ell1,false);
		p2d.append(ell2,false);




		g.setColor(new Color(0xFF0000FF,true));
		g.setClip(p2d);
		g.setColor(new Color(0xFFDD3344,true));
//		g.draw(p2d);
		g.fillRect(0,0,250,250);

		ImageIO.write(buffimg01,"PNG",new File(dir+"/b01.png"));


	}

}