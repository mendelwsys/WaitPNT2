package ru.ts.tac;

import ru.ts.utils.RawImageOperations;

import javax.imageio.ImageIO;
import java.awt.image.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;

/**
 * Created by IntelliJ IDEA.
 * User: vladm
 * Date: 09.06.2011
 * Time: 15:39:37
 * Поиск серединных точек изображения
 */
public class FindMidlPoint
{


	static public Point[] getMinMaxPnt(int[][] matrix)
	{
		if (matrix == null || matrix.length == 0 || matrix[0].length == 0)
			return null;


		Point[] minmax = new Point[]{new Point(-1, -1), new Point(-1, -1)};

		for (int i = 0; i < matrix.length; i++)
			for (int j = 0; j < matrix[i].length; j++)
				if (matrix[i][j] != 0)
				{
					if (minmax[0].x > i || minmax[0].x == -1)
						minmax[0].x = i;

					if (minmax[0].y > j || minmax[0].y == -1)
						minmax[0].y = j;

					if (minmax[1].x < i || minmax[1].x == -1)
						minmax[1].x = i;

					if (minmax[1].y < j || minmax[1].y == -1)
						minmax[1].y = j;
				}
		return minmax;
	}

	public static void _main(String[] args) throws Exception
	{
		BufferedImage imgtemplate = ImageIO.read(new File("G:\\BACK_UP\\$D\\MAPDIR\\MP\\CONVERTSUPPORT\\TEMPLATE\\p_f203_Railway_Crossing.png"));


		proc(imgtemplate);

		BufferedImage bimg1 = new BufferedImage(imgtemplate.getWidth(), imgtemplate.getHeight(), BufferedImage.TYPE_INT_ARGB);

		Graphics2D g1 = (Graphics2D) bimg1.getGraphics();
		g1.drawImage(imgtemplate, 0, 0, new ImageObserver()
		{
			public boolean imageUpdate(Image img, int infoflags, int x, int y, int width, int height)
			{
				return false;
			}
		});

		proc(bimg1);

		ImageIO.write(bimg1,"PNG",new File("D:/tst.png"));
		BufferedImage imgtemplate2 = ImageIO.read(new File("D:/tst.png"));

		proc(imgtemplate2);

	}

	private static void proc(BufferedImage imgtemplate)
	{
		ColorModel model = imgtemplate.getColorModel();
		Raster data = imgtemplate.getData();

		int [] res = new int [10];
		res= data.getPixel(14,14, res);

//		byte[] bt= new byte[]{(byte)res[0],(byte)res[1],(byte)res[2],(byte)res[3]};
//		int r=model.getRed(bt);
//		int g=model.getGreen(res);
//		int b=model.getBlue(res);
//		int a=model.getAlpha(res);
		System.out.println("res = " + res.length);
	}


	public static void main(String[] args) throws Exception
	{


		String ftemplate = "G:\\BACK_UP\\$D\\MAPDIR\\MP\\CONVERTSUPPORT\\TEMPLATE\\";
		String cmpfolder = "G:\\BACK_UP\\$D\\MAPDIR\\MP\\CONVERTSUPPORT\\POINTS\\";



		String outfolder = "G:\\BACK_UP\\$D\\MAPDIR\\MP\\CONVERTSUPPORT\\OUT";



		File fl = new File(ftemplate);
		String[] nmtemplate = fl.list();
		for (int i1 = 0; i1 < nmtemplate.length; i1++)
		{
			String fltemplate = nmtemplate[i1];
			BufferedImage imgtemplate = getImage(ftemplate, fltemplate);

			Raster l_raster = imgtemplate.getData();
			Point pnttempl = RawImageOperations.getMidleByImage(RawImageOperations.getImgByRaster(l_raster));
			new File(outfolder + "/" + fltemplate).mkdir();
			String[] cmps = new File(cmpfolder).list();

			double[] maxd = new double[]{-1, -1,-1,-1,-1};
			int[] maxi = new int[5];
			for (int i = 0; i < cmps.length; i++)
			{
				BufferedImage img = getImage(cmpfolder, cmps[i]);
				int[][] dataimg = RawImageOperations.getImgByRaster(img.getData());
				Point pntimg = getMidleByImage(dataimg);

				if (pntimg == null || pnttempl == null)
					continue;

				double v = -1;
				for (int xi = pntimg.x - 1; xi <= pntimg.x + 1; xi++)
					for (int xj = pntimg.y - 1; xj <= pntimg.y + 1; xj++)
					{
						int[][] datatempl = RawImageOperations.getImgByRaster(l_raster);
						int[][][] restempl=setMatrix(datatempl, pnttempl, dataimg, new Point(xi, xj));
//						removeBackGroundImage(datatempl);
//						showImage(imgtemplate,datatempl, fltemplate+"_"+cmps[i]+"_"+xi+"_"+xj+".png");
						double maxv = integrate(restempl);
						if (v < maxv)
							v = maxv;
					}

				for (int i2 = 0; i2 < maxd.length; i2++)
				{
					if (maxd[i2] < v || maxd[i2] < 0)
					{
						shift(maxd, maxi, i2);
						maxd[i2] = v;
						maxi[i2] = i;
						break;
					}
				}
			}

			for (int i2 = 0; i2 < maxd.length; i2++)
			{
				double maxdd = maxd[i2];
				if (maxdd < 0)
					break;
				int maxii = maxi[i2];
				System.out.println("maxi = "+ fltemplate +"=" + cmps[maxii]);

				//new File(outfolder + "/" + fltemplate + "/" + cmps[maxii] + "_" + (int) maxdd).mkdir();
				BufferedImage img2 = getImage(ftemplate, fltemplate);
				ImageIO.write(img2,"PNG",new File(outfolder + "/"  + fltemplate + "/"+ fltemplate));

				BufferedImage img = getImage(cmpfolder, cmps[maxii]);
				ImageIO.write(img,"PNG",new File(outfolder + "/"  + fltemplate + "/"+ (int) maxdd+"_" + cmps[maxii]));
			}

			System.out.println("path" + i1 + " of " + nmtemplate.length);
		}

//		Point[] pnts = getMinMaxPnt(data);
//		int imdlw = img.getWidth() / 2;
//		int imdlh = img.getHeight() / 2;
//
//
//		int dx=Math.abs(pnt.x-imdlw);
//		int dy=Math.abs(pnt.y-imdlh);
//
//		System.out.println("dx = " + dx);
//		System.out.println("dy = " + dy);
	}

	private static void showImage(BufferedImage imgtemplate, int[][] datatempl, String fname)
			throws IOException
	{
		BufferedImage tstimg1 = new BufferedImage(imgtemplate.getWidth(), imgtemplate.getHeight(), BufferedImage.TYPE_INT_ARGB);
		for (int i2 = 0; i2 < datatempl.length; i2++)
			for (int i22 = 0; i22 < datatempl[i2].length; i22++)
			{
				int[] iArray = getRGBA(datatempl[i2][i22]);
				tstimg1.getRaster().setPixel(i2, i22, iArray);
			}
		ImageIO.write(tstimg1, "PNG", new File("D:/" + fname));
	}

	private static void shift(double[] mind, int[] mini, int i2)
	{
		for (int i = mind.length - 1; i > i2; i--)
		{
			mind[i] = mind[i - 1];
			mini[i] = mini[i - 1];
		}
	}

	private static BufferedImage getImage(String folder, String flname)
			throws IOException
	{
		BufferedImage imgtemplate = ImageIO.read(new File(folder + "/" + flname));
		BufferedImage bimg = new BufferedImage(imgtemplate.getWidth(), imgtemplate.getHeight(), BufferedImage.TYPE_INT_ARGB);
		Graphics2D g1 = (Graphics2D) bimg.getGraphics();
		g1.drawImage(imgtemplate, 0, 0, new ImageObserver()
		{
			public boolean imageUpdate(Image img, int infoflags, int x, int y, int width, int height)
			{
				return false;
			}
		});
		return bimg;
	}


	static public void removeBackGroundImage(int[][] matrix)
	{
		for (int i = 0; i < matrix.length; i++)
			for (int j = 0; j < matrix[i].length; j++)
				if (matrix[i][j] != 0)
				{
					int x = matrix[i][j];
					int[] argb = getRGBA(x);
					if (argb[0] == argb[1] && argb[1] == argb[2])
						matrix[i][j] = 0;
				}

	}

	static public Point getMidleByImage(int[][] matrix)
	{
		if (matrix == null || matrix.length == 0 || matrix[0].length == 0)
			return null;

		int cnts = 0;
		int SUMMX = 0;
		int SUMMY = 0;

		for (int i = 0; i < matrix.length; i++)
			for (int j = 0; j < matrix[i].length; j++)
				if (matrix[i][j] != 0)
				{
					int x = matrix[i][j];
					int[] argb = getRGBA(x);

					if (argb[0] == argb[1] && argb[1] == argb[2])
					{
//						matrix[i][j]=0;
						continue;
					}
					cnts++;
					SUMMX += i;
					SUMMY += j;
				}

		if (cnts > 0)
			return new Point(SUMMX / cnts, SUMMY / cnts);
		else
			return null;
	}

	static public int[] getRGBA(int val)
	{
		return new int[]{(val >> 16) & 0xFF, (val >> 8) & 0xFF, val & 0xFF, (val >> 24) & 0xFF};
	}

	static public int setRGBA(int[] val)
	{
		return ((val[0] << 16) & 0xFF0000) | ((val[1] << 8) & 0xFF00) | val[2] | ((val[3] << 24) & 0xFF000000);
	}

	static public double integrate(int[][][] matrixtemplate)
	{
		double cnt = 0;
		double[] rgb=new double[4];

		for (int i = 0; i < matrixtemplate.length; i++)
			for (int j = 0; j < matrixtemplate[i].length; j++)
			{
				int[] rargb = matrixtemplate[i][j];


				for (int c1 = 0; c1 < rargb.length; c1++)
					 rgb[c1]+=rargb[c1];
			}

		for (int c1 = 0; c1 < rgb.length-1; c1++)
			 cnt+=rgb[c1];
		return cnt;
	}

	static public int _evalMatrix(int[][] matrixtemplate)
	{
		int cnt = 0;
		for (int i = 0; i < matrixtemplate.length; i++)
			for (int j = 0; j < matrixtemplate[i].length; j++)
			{
				int[] rargb = getRGBA(matrixtemplate[i][j]);
				if (rargb[0] > 0x12 || rargb[1] > 0x12 || rargb[2] > 0x12)
					cnt++;
			}
		return cnt;
	}


	static public int[][][] setMatrix(int[][] matrixtemplate, Point pnttempl, int[][] matrix, Point pntsearch)
	{
		Point p1 = null;
		Point p4 = null;


		int[][][] res= null;
		for (int i = 0; i < matrixtemplate.length; i++)
		{
			if (res==null)
				res= new int[matrixtemplate.length][matrixtemplate[i].length][4];
			for (int j = 0; j < matrixtemplate[i].length; j++)
			{
				int[] targb = getRGBA(matrixtemplate[i][j]);

				if (targb[3]==0)
					continue;


				int dx = pnttempl.x - i;
				int dy = pnttempl.y - j;


				int ii = pntsearch.x - dx;
				int jj = pntsearch.y - dy;
				if (
						ii >= 0 && ii < matrix.length
						&&
						jj >= 0 && jj < matrix[ii].length
					)
				{
					int[] rargb = getRGBA(matrix[ii][jj]);

					for (int cl = 0; cl < rargb.length; cl++)
						rargb[cl] = Math.abs(targb[cl] * rargb[cl]);

					res[i][j] = rargb;

					if (p1 == null)
					{
						p1 = new Point(ii, jj);
						p4 = new Point(ii, jj);
					}
					else
					{
						if (p1.x > ii)
							p1.x = ii;
						if (p1.y > jj)
							p1.y = jj;

						if (p4.x < ii)
							p4.x = ii;
						if (p4.y < jj)
							p4.y = jj;
					}
				}
			}
		}
		return res;
	}

	static public void _setMatrix(int[][] matrixtemplate, Point pnttempl, int[][] matrix, Point pntsearch)
	{
		Point p1 = null;
		Point p4 = null;


		for (int i = 0; i < matrixtemplate.length; i++)
			for (int j = 0; j < matrixtemplate[i].length; j++)
			{
				int[] targb = getRGBA(matrixtemplate[i][j]);


				int dx = pnttempl.x - i;
				int dy = pnttempl.y - j;


				int ii = pntsearch.x - dx;
				int jj = pntsearch.y - dy;
				if (
						ii >= 0 && ii < matrix.length
						&&
						jj >= 0 && jj < matrix[ii].length
					)
				{
					int[] rargb = getRGBA(matrix[ii][jj]);

					for (int cl = 0; cl < rargb.length; cl++)
						rargb[cl] = Math.abs(targb[cl] * rargb[cl]);

					matrixtemplate[i][j] = setRGBA(rargb);

					if (p1 == null)
					{
						p1 = new Point(ii, jj);
						p4 = new Point(ii, jj);
					}
					else
					{
						if (p1.x > ii)
							p1.x = ii;
						if (p1.y > jj)
							p1.y = jj;

						if (p4.x < ii)
							p4.x = ii;
						if (p4.y < jj)
							p4.y = jj;
					}
				}
			}
	}

//	static public double _getWeight(int [][] matrixtemplate,Point pnttempl,int[][] matrix,Point pntsearch)
//	{
//		Point p1=null;
//		Point p4=null;
//
//		double[] rv=new double[]{0,0,0,0};
//
//		int cnt=0;
//
//		for (int i = 0; i < matrixtemplate.length; i++)
//			for (int j = 0; j < matrixtemplate[i].length; j++)
//			{
//				cnt++;
//				int[] targb=getRGBA(matrixtemplate[i][j]);
//
//
//				int dx=pnttempl.x-i;
//				int dy=pnttempl.y-j;
//
//
//				int ii=pntsearch.x-dx;
//				int jj=pntsearch.y-dy;
//				if (
//						ii>=0 && ii<matrix.length
//						&& jj>=0 && jj<matrix[ii].length
//					)
//				{
//					int[] rargb=getRGBA(matrix[ii][jj]);
//
//
//
//					for (int cl = 0; cl < rv.length; cl++)
//						rv[cl]+=Math.abs(targb[cl]-rargb[cl]);
//
//					if (p1==null)
//					{
//						p1=new Point(ii,jj);
//						p4=new Point(ii,jj);
//					}
//					else
//					{
//						if (p1.x>ii)
//							p1.x=ii;
//						if (p1.y>jj)
//							p1.y=jj;
//
//						if (p4.x<ii)
//							p4.x=ii;
//						if (p4.y<jj)
//							p4.y=jj;
//					}
//				}
//				else
//				{
//					for (int cl = 0; cl < rv.length; cl++)
//						rv[cl]+=targb[cl];
//				}
//			}
//
////		for (int i = 0; i < p1.x; i++)
////			for (int j = 0; j < p1.y; j++)
////			{
////				int template=matrix[i][j];
////				int[] targb=getRGBA(template);
////				for (int cl = 0; cl < targb.length; cl++)
////					rv[cl]+=targb[cl];
////				cnt++;
////			}
////
////		for (int i = p4.x+1; i < matrix.length; i++)
////			for (int j = p4.y+1; j < matrix[i].length; j++)
////			{
////				int template=matrix[i][j];
////				int[] targb=getRGBA(template);
////				for (int cl = 0; cl < targb.length; cl++)
////					rv[cl]+=targb[cl];
////				cnt++;
////			}
//
//
//		double res=0;
//		for (int i = 0; i < rv.length-1; i++)
//		{
//			double v = rv[i];
//			res += v * v;
//		}
//
//		return Math.sqrt(res);
//	}

}
