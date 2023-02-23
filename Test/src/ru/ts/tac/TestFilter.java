package ru.ts.tac;

import javax.imageio.ImageIO;
import javax.imageio.stream.ImageInputStream;
import javax.swing.*;
import java.io.File;
import java.awt.image.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.util.HashSet;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: vladm
 * Date: 05.06.2011
 * Time: 5:58:20
 */
public class TestFilter
{

	static void replaceIt(BufferedImage img)
	{
		int x, y;

		Map<String, Integer> hm = new HashMap<String, Integer>();
		for (x = 0; x < img.getWidth(); x++)
		{
			for (y = 0; y < img.getHeight(); y++)
			{
				WritableRaster raster = img.getRaster();
				int[] rres = raster.getPixel(x, y, new int[4]);
				String res=rres[0]+"_"+rres[1]+"_"+rres[2];
				Integer i=hm.get(res);
				if (i==null)
					i=0;
				hm.put(res,++i);
			}
		}

		Integer imax=0;
		String keymax=null;
		for (String key : hm.keySet())
		{
			int i=hm.get(key);
			if (imax<i)
			{
				imax=i;
				keymax=key;
			}
		}

		if (keymax==null)
		{
			System.out.println("error = ");
			return;
		}

		int[] keyarr=new int[3];
		String[] splits=keymax.split("_");
		for (int i = 0; i < splits.length; i++)
			keyarr[i] = Integer.parseInt(splits[i]);


		for (x = 0; x < img.getWidth(); x++)
		{
			for (y = 0; y < img.getHeight(); y++)
			{
				WritableRaster raster = img.getRaster();
				int[] rres = raster.getPixel(x, y, new int[4]);

				if (rres[0] == keyarr[0] && rres[1] == keyarr[1] && rres[2]==keyarr[2])
					rres[3] = 0;
				raster.setPixel(x, y, rres);
			}
		}
	}

	private static IndexColorModel createColorModel(int n)
	{
		byte[] r = new byte[16];
		byte[] g = new byte[16];
		byte[] b = new byte[16];
		byte[] a = new byte[16];

		for (int i = 0; i < r.length; i++)
		{
			r[i] = (byte) n;
			g[i] = (byte) n;
			b[i] = (byte) n;
			a[i] = (byte) n;
		}
		return new IndexColorModel(4, 16, r, g, b, a);

	}

	protected static LookupOp createColorizeOp()
	{

		short[] alpha = new short[256];
		short[] red = new short[256];
		short[] green = new short[256];
		short[] blue = new short[256];

		for (short i = 0; i < 256; i++)
		{
			alpha[i] = i;

			red[i] = i;
			green[i] = i;
			blue[i] = i;

			if (i == 254 || i == 255)
			{
				red[i] = 0;
				green[i] = 0;
				blue[i] = 0;
			}
		}

		short[][] data = new short[][]{
				red, green, blue, alpha
		};

		LookupTable lookupTable = new ShortLookupTable(0, data);
		return new LookupOp(lookupTable, null);
	}

	public static void bmain(String[] args) throws Exception
	{
		JFrame fr = new JFrame();

		fr.setLayout(new BorderLayout());
//		ImageIcon leftArrow = new ImageIcon("G:\\BACK_UP\\$D\\DownLoads\\2DemoVM\\CURSOR_ENTRY\\102.cur");
		ImageIcon leftArrow = new ImageIcon("G:\\BACK_UP\\$D\\MAPDIR\\MP\\CONVERTSUPPORT\\Swamp_51_1.png");
		JLabel label1 = new JLabel(leftArrow, JLabel.CENTER);
		fr.add(label1);
		fr.setVisible(true);
	}

	public static void main(String[] args) throws Exception
	{

		//BufferedImage bimg=new BufferedImage(img.getWidth(),img.getHeight(),BufferedImage.TYPE_INT_ARGB);
//		Graphics2D g = (Graphics2D)bimg.getGraphics();
//		g.drawImage(img,0,0,new ImageObserver()
//		{
//			public boolean imageUpdate(Image img, int infoflags, int x, int y, int width, int height)
//			{
//				return false;
//			}
//		});

//		ColorModel cm = new ColorModel(32)
//		{
//
//			public boolean isCompatibleRaster(Raster raster)
//			{
//				return true;
//			}
//
//		public int getRed(int pixel)
//
//			{
//				return (pixel >> 16) & 0xff;  //To change body of implemented methods use File | Settings | File Templates.
//			}
//
//			public int getGreen(int pixel)
//			{
//				return (pixel >> 8) & 0xff;  //To change body of implemented methods use File | Settings | File Templates.
//			}
//
//			public int getBlue(int pixel)
//			{
//				return pixel & 0xff;  //To change body of implemented methods use File | Settings | File Templates.
//			}
//
//			public int getAlpha(int pixel)
//			{
//				return (pixel >> 24) & 0xff;  //To change body of implemented methods use File | Settings | File Templates.
//			}
//		};

		//cm = new DirectColorModel(32,0xff0000,0x00ff00,0x0000ff,0xff000000)
		//bimg=new BufferedImage(createColorModel(10),bimg.getRaster(),false,null);

//		BufferedImageOp colorizeFilter = createColorizeOp();
//		BufferedImage targetImage = colorizeFilter.filter(bimg, null);
//
//		BufferedImage bimg = new BufferedImage(img.getWidth(), img.getHeight(), BufferedImage.TYPE_INT_ARGB);
//		g = (Graphics2D)bimg.getGraphics();
//		g.setColor(new Color(200,100,100,255));
//		g.fillRect(0,0,img.getWidth(),img.getHeight());
//		g.drawImage(targetImage,0,0,new ImageObserver()
//		{
//			public boolean imageUpdate(Image img, int infoflags, int x, int y, int width, int height)
//			{
//				return false;
//			}
//		});



//		{
//			BufferedImage img=ImageIO.read(new File("G:\\BACK_UP\\$D\\DownLoads\\2DemoVM\\CURSOR_ENTRY\\XXXX\\14.png"));
//			BufferedImage bimg = new BufferedImage(img.getWidth(), img.getHeight(), BufferedImage.TYPE_INT_ARGB);
//			Graphics2D g = (Graphics2D) bimg.getGraphics();
//			g.drawImage(img, 0, 0, new ImageObserver()
//			{
//				public boolean imageUpdate(Image img, int infoflags, int x, int y, int width, int height)
//				{
//					return false;
//				}
//			});
//			replaceIt(bimg);
//			ImageIO.write(bimg, "PNG", new File("G:\\BACK_UP\\$D\\DownLoads\\2DemoVM\\CURSOR_ENTRY\\XXXX\\14x.png"));
//		}

		{
			BufferedImage img = ImageIO.read(new File("G:\\BACK_UP\\$D\\DownLoads\\2DemoVM\\CURSOR_ENTRY\\XXXX\\14.png"));
			BufferedImage bimg1 = new BufferedImage(img.getWidth(), img.getHeight(), BufferedImage.TYPE_INT_ARGB);
			Graphics2D g1 = (Graphics2D) bimg1.getGraphics();
			g1.setColor(new Color(200, 100, 100, 255));
			g1.fillRect(0, 0, img.getWidth(), img.getHeight());
			g1.drawImage(img, 0, 0, new ImageObserver()
			{
				public boolean imageUpdate(Image img, int infoflags, int x, int y, int width, int height)
				{
					return false;
				}
			});
			ImageIO.write(bimg1, "PNG", new File("G:\\BACK_UP\\$D\\DownLoads\\2DemoVM\\CURSOR_ENTRY\\XXXX\\t.png"));

		}

	}

	public static class MyFilter extends RGBImageFilter
	{
		public MyFilter()
		{
			// The filter's operation does not depend on the
			// pixel's location, so IndexColorModels can be
			// filtered directly.
			//canFilterIndexColorModel = true;
		}

		public int filterRGB(int x, int y, int rgb)
		{

			int red = (rgb & 0x00ff0000) >> 16;
			int green = (rgb & 0x0000ff00) >> 8;
			int blue = rgb & 0x000000ff;

			if (red == 255 && green == 255 && blue == 255)
				return 0x0;
			return rgb;
		}
	}
}
