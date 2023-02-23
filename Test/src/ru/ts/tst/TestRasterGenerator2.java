package ru.ts.tst;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.awt.geom.AffineTransform;
import java.io.File;
import java.io.IOException;

/**
 * Генератор растровых тестов для отладки заполнения полигоном текстурой
 */
public class TestRasterGenerator2
{


	public static void main(String[] args) throws IOException
	{

		String[] slits="0,0,2;4".split("[\\,,;]");

		String dir = "G:/";

//		BufferedImage bi = ImageIO.read(new File(dir + "ftv2doc.png"));
//		Paint tp = new TexturePaint(bi, new Rectangle(0, 0,  bi.getWidth()/2, bi.getHeight() / 2))
//		{
//			int i=0;
//			 public PaintContext 	createContext(ColorModel cm, Rectangle deviceBounds, Rectangle2D userBounds, AffineTransform xform, RenderingHints hints)
//			 {
//				 PaintContext rv = super.createContext(cm, deviceBounds, userBounds, xform, hints);
//				 return rv;
//			 }
//		};

		//Paint tp=new GradientPaint(20,20,new  Color(0x00FF), 150, 150,new  Color(0xFF0000),true);


		BufferedImage buffimg01 = new BufferedImage(450, 450, BufferedImage.TYPE_INT_ARGB);
//		setTp(tp, buffimg01);
//		BufferedImage buffimg02 = new BufferedImage(450, 450, BufferedImage.TYPE_INT_ARGB);
//		setTp(tp, buffimg02);

//		final CompositeStroke compositeStroke = new CompositeStroke(new BasicStroke(10f), new BasicStroke(0.5f));
		Graphics2D g = (Graphics2D) buffimg01.getGraphics();
//		g.setStroke(compositeStroke);

		Stroke str = g.getStroke();

		str=new BasicStroke(1,2,0,10,new float[]{1,5},0);
//		g.setStroke(new BasicStroke( 0.5f ));
////		g.drawRect(1,1,248,248);
//		//Shape resch = compositeStroke.createStrokedShape(new Rectangle(1, 1, 248, 248));
//		//g.draw();
		g.setPaint(new Color(0x990000));
		g.setStroke(str);
		g.drawLine(10,10,10,40);


		ImageIO.write(buffimg01, "PNG", new File(dir + "/b01.png"));
		//ImageIO.write(buffimg02, "PNG", new File(dir + "/b02.png"));
	}

//	public static void main(String[] args) throws IOException
//	{
//		BufferedImage bi = ImageIO.read(new File("G:\\BACK_UP\\$D\\MAPDIR\\ICO\\PNG\\10.png"));
//
//		ImageIO.write(bi, "PNG", new File("G:\\BACK_UP\\$D\\MAPDIR\\ICO\\10.png"));
//	}

	private static void setTp(Paint tp, BufferedImage buffimg01)
	{
		Graphics2D g = (Graphics2D) buffimg01.getGraphics();
//		g.setPaint(new Color(0x36009999,true));
//		g.setColor();
		g.setPaint(tp);
		g.fillRect(10, 10, 390, 390);
//
//
//		final float dash1[] = {70.0f, 60, 180, 20};
//		BasicStroke dashed = new BasicStroke(2.0f,
//				BasicStroke.CAP_ROUND,
//				BasicStroke.JOIN_MITER,
//				10.0f, dash1, 65f);
//		g.setPaint(new Color(0x999999));
//		g.setStroke(dashed);
//		g.draw(new Rectangle(10, 10, 390, 390));
//		g.drawLine(10,10,390,390);
	}

	static class CompositeStroke implements Stroke
	{
		private Stroke stroke1, stroke2;

		public CompositeStroke(Stroke stroke1, Stroke stroke2)
		{
			this.stroke1 = stroke1;
			this.stroke2 = stroke2;
		}

		public Shape createStrokedShape(Shape shape)
		{
			return stroke2.createStrokedShape(stroke1.createStrokedShape(shape));
		}
	}

}