package ru.ts.toykernel.drawcomp.painters.def;

import ru.ts.toykernel.drawcomp.IPainter;
import ru.ts.toykernel.geom.IBaseGisObject;
import ru.ts.toykernel.geom.def.RasterObject;
import ru.ts.toykernel.converters.ILinearConverter;
import ru.ts.gisutils.algs.common.MRect;

import java.awt.*;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;
import java.awt.geom.AffineTransform;

/**
 * Painer of rasters
 */
public class DefRasterPainter  implements IPainter
{
	public int[] paint(Graphics g, IBaseGisObject drawMe, ILinearConverter converter, Point drawSize) throws Exception
	{
		if (drawMe instanceof RasterObject)
		{

			RasterObject rasterObject = (RasterObject) drawMe;
			MRect projRect= rasterObject.getMBB(null);

			MRect drwRect=converter.getDstRectByRect(projRect);

			BufferedImage img = rasterObject.getRawRaster().first;

			int[] ints = {0, 0, 0};

			if (img!=null)
			{
				double scale[]=new double[]{drwRect.getWidth()/img.getWidth(),drwRect.getHeight()/img.getHeight()};
				if (scale[0]!=1.0 || scale[1]!=1.0)
				{
					AffineTransform xformscale = AffineTransform.getScaleInstance(scale[0],scale[1]);
					AffineTransformOp tranopscale = new AffineTransformOp(xformscale, AffineTransformOp.TYPE_BILINEAR);
					img = tranopscale.filter(img, null);
				}


				g.drawImage(img,(int)Math.round(drwRect.p1.x),(int)Math.round(drwRect.p1.y),new ImageObserver()
				{
					public boolean imageUpdate(Image img, int infoflags, int x, int y, int width, int height)
					{
						return false;
					}
				});
//++DEBUG
//				if (scale[0]!=1.0 || scale[1]!=1.0)
//				{
//					if (
//							img.getHeight()!=(int)Math.round(drwRect.getHeight()) ||
//							img.getWidth()!=(int)Math.round(drwRect.getWidth())
//						)
//					{
//						BufferedImage img2 = new BufferedImage(img.getWidth(),img.getHeight(), BufferedImage.TYPE_INT_ARGB);
//						Graphics g2 = img2.getGraphics();
//						g2.setColor(new Color(0xFF00FFFF));
//						g2.fillRect(0,0,img.getWidth(),img.getHeight());
//
//						g2.drawImage(img,0,0,new ImageObserver()
//						{
//							public boolean imageUpdate(Image img, int infoflags, int x, int y, int width, int height)
//							{
//								return false;
//							}
//						});
//
//						ImageIO.write(img2,"PNG",new File("D:/"+drwRect.p1.x+"_"+drwRect.p1.y+".png"));
//						System.out.println("!!!STRIPPP!!!:");
//					}
//					System.out.println("scale[0] = " + scale[0]);
//				}
//--DEBUG
				ints[2]++;
			}
			return ints;
		}
		throw new UnsupportedOperationException();
	}

	public MRect getRect(Graphics graphics, IBaseGisObject obj, ILinearConverter converter) throws Exception
	{
		return null;  //To change body of implemented methods use File | Settings | File Templates.
	}

	public MRect getDrawRect(Graphics graphics, IBaseGisObject obj, ILinearConverter converter) throws Exception
	{
		return null;  //To change body of implemented methods use File | Settings | File Templates.
	}

	public Shape createShape(IBaseGisObject drawMe, ILinearConverter converter) throws Exception
	{
		return null;  //To change body of implemented methods use File | Settings | File Templates.
	}
}
