package ru.ts.tac;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;

/**
 * Created by IntelliJ IDEA.
 * User: vladm
 * Date: 20.06.2011
 * Time: 16:08:08
 * Перебор индексов
 */
public class FontIndexing
{
	public static void main(String[] args) throws Exception
	{

		String abc_en="qwertyuiopasdfghjklzxcvbnm";
		String abc_ru="йцукенгшщзхъфывапролджэячсмитьбю";


		GraphicsEnvironment e = GraphicsEnvironment.getLocalGraphicsEnvironment();
		Font[] fonts = e.getAllFonts(); // Get the fonts
		for (Font f : fonts)
		{

			if (f.getFontName().equals("Arial"))
			{
				BufferedImage bimg1 = new BufferedImage(100, 100, BufferedImage.TYPE_INT_ARGB);

				Graphics2D gr = (Graphics2D) bimg1.getGraphics();


//				f=f.deriveFont(15);
//				gr.setFont(f);

				Font f1=new Font( f.getFontName(), Font.CENTER_BASELINE, 10);

//				Font f1=gr.getFont();
//				f1=f1.deriveFont(20);

				gr.setFont(f1);

				gr.setColor(new Color(0xFFFFFFFF,true));
				gr.fillRect(0,0,100,100);
				gr.setColor(new Color(0xFF000000,true));
				gr.drawString(abc_en,50,50);

				ImageIO.write(bimg1,"PNG",new File("D:/tst.png"));

				break;
			}
			//System.out.println();
		}
	}
}
