package ru.ts.tst;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;
import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;

/**
 * 
 */
public class TestRasterGenerator3
{


	public static void main(String[] args) throws Exception
	{
//		String objId="ASDFGE#$ACBBB".replaceAll("[#]","\\\\#");

//		g.setColor(new Color(0xFF990000,true));
////		int[] X=new int[]{1,3,8,1,3,4,5,3}; //Рисование фигуры с одной дырой посередине
////		int[] Y=new int[]{1,5,1,1,2,3,2,2};
//
////Рисование фигуры с двумя дырами посередине (мы заканчиваем по тому же пути что и обрисовываем внутренние фигуры, т.о. кол-во становится сторон становится четным)
//		int[] X=new int[]{2,2,10,10,2,4,4,6,6,4,7,7,9,9,7,4,2};
//		int[] Y=new int[]{2,6,6 ,2 ,2,3,5,5,3,3,3,5,5,3,3,3,2};
//
//		for (int i = 0; i < Y.length; i++)
//		{
//			Y[i] = 10*Y[i];
//			X[i] = 10*X[i];
//		}

		BufferedImage buffimg01 = new BufferedImage(350, 350, BufferedImage.TYPE_INT_ARGB); //Это то что я хочу видеть

		Graphics2D g = (Graphics2D) buffimg01.getGraphics();
		g.setColor(new Color(0xFF00FF00,true));
		g.fillRect(0,0,250,250);

		g.setColor(new Color(0xFFAAFF00,true));
		g.fillRect(0,0,150,150);

//		BufferedImage buffimg02 = new BufferedImage(250, 250, BufferedImage.TYPE_INT_ARGB);
//		g = (Graphics2D) buffimg02.getGraphics();

//		g.setColor(new Color(0xFFFF0000,true));
//		g.fillRect(0,0,250,250);

		//Сначала сброс пересечения
		g.setComposite(AlphaComposite.getInstance(AlphaComposite.DST_OUT));
		g.setColor(new Color(0xFF000000,true));

		Ellipse2D.Double ellps = new Ellipse2D.Double(100,100,180,50);
		g.fill(ellps);

//		g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER));
//		ellps = new Ellipse2D.Double(100,100,110,55);
//		g.setColor(new Color(0x770000DD,true));
//		g.fill(ellps);
//
//
//		buffimg01.getGraphics().drawImage(buffimg02,0,0,new ImageObserver()
//		{
//
//			public boolean imageUpdate(Image img, int infoflags, int x, int y, int width, int height)
//			{
//				return false;
//			}
//		});


		String dir="D:/";
		ImageIO.write(buffimg01,"PNG",new File(dir+"/b01.png"));

//		int composite=-100;
//		String sComposite="CLEAR";
//		Class comp=AlphaComposite.class;
//		Field[] flds = comp.getFields();
//		for (Field fld : flds)
//		{
//			if (fld.getName().equals(sComposite))
//			{
//				composite=fld.getInt(null);
//				break;
//			}
//		}
//
//		System.out.println("composite = " + composite);
	}

}