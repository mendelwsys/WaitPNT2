package ru.ts.conv.rshp;

import ru.ts.stream.ISerializer;
import ru.ts.gisutils.algs.common.MPoint;
import ru.ts.gisutils.algs.common.MPointZM;
import ru.ts.gisutils.algs.common.MRect;

import java.io.*;
import java.util.List;
import java.util.LinkedList;

/**
 * Read shp files, fill data base
 */
public class ReadShp implements ISerializer
{
	static int rnmr = 0;
	protected HeaderOfMainFile ifh = new HeaderOfMainFile(); //Index file header
	protected HeaderOfMainFile mfh = new HeaderOfMainFile();//Main file header
	protected List<SHPRecord> recs = new LinkedList<SHPRecord>(); //records in the asname file
	protected List<Integer> skipedtypes = new LinkedList<Integer>();
	private DataInputStream ixdis;//input stream for index files


	public ReadShp()
	{
	}

	/**
	 * Индексный файл, если присутсвует тогда читаем с помощью него
	 *
	 * @param ixdis - входной поток индексного файла
	 */
	public ReadShp(DataInputStream ixdis)
	{
		this.ixdis = ixdis;
	}

	public static void main(String[] args) throws Exception
	{
		ReadShp readShp = new ReadShp();
		readShp.loadFromStream(new DataInputStream(new BufferedInputStream(new FileInputStream("D:/rrspnt_200.shp"))));
		System.out.println("readShp = " + readShp.recs.size());
	}

	public List<Integer> getSkipedtypes()
	{
		return skipedtypes;
	}

	public void savetoStream(DataOutputStream dos) throws Exception
	{
		throw new UnsupportedOperationException();
	}

	public void loadFromStream(DataInputStream dis) throws Exception
	{
		if (ixdis != null)
		{
			IXRecord rec=new IXRecord();

			dis.mark(1000);
			ifh.loadFromStream(ixdis);
			mfh.loadFromStream(dis);
			long current=0;
			dis.reset();
			try
			{
				while (true)
				{
					rec.loadFromStream(ixdis);//Узнать следующую позицию

					long n = 2 * (rec.offset - current);
					long res=0;
					while((res+=dis.skip(n-res))<n)
						System.out.println("res = " + res);

					if (res!=n)
						System.out.println("Error skiped res = " + res);

					SHPRecord shpRecord = new SHPRecord();
					shpRecord.loadFromStream(dis);
					current=(rec.offset+rec.contentlength+4); //8- байтов занимает заголовок считанной записи или 4 16-ти разрядных слов
					recs.add(shpRecord);
				}
			}
			catch (IOException e)
			{//
			}
			ixdis.close();
		}
		else
			_loadFromStream(dis);
	}

	protected void _loadFromStream(DataInputStream dis) throws Exception
	{
		try
		{
			mfh.loadFromStream(dis);
			while (true)
			{
				SHPRecord shpRecord = new SHPRecord();
				shpRecord.loadFromStream(dis);
				recs.add(shpRecord);
			}
		}
		catch (EOFException e)
		{//

		}
	}

	public double readDBOrder(DataInputStream is) throws Exception
	{
		byte[] b = new byte[8];
		is.readFully(b);
		long rv = 0;
		for (int i = 0; i < b.length; i++)
		{
			long i1 = ((long) b[i]) & 0x0FFL;
			rv |= i1 << (8 * i);
		}
		return Double.longBitsToDouble(rv);

	}

	public int readIBOrder(DataInputStream is) throws Exception
	{
		byte[] b = new byte[4];
		is.readFully(b);
		int rv = 0;
		for (int i = 0; i < b.length; i++)
		{
			int b1 = (b[i]) & 0x00FF;
			rv |= (b1 << (8 * i));
		}
		return rv;
	}

	public void readMainFile()
	{

	}

	public class IXRecord implements ISerializer
	{
		int offset;
		int contentlength;

		public void loadFromStream(DataInputStream dis) throws Exception
		{
			offset = dis.readInt();
			contentlength= dis.readInt();
		}

		public void savetoStream(DataOutputStream dos) throws Exception
		{
			throw new UnsupportedOperationException();
		}

	}

	public class  HeaderOfMainFile implements ISerializer
	{

		public int lengthfile;
		public int version;
		public int shptype;

		public double xMin;
		public double yMin;
		public double xMax;
		public double yMax;

		public double zMin;
		public double zMax;

		public double mMin;
		public double mMax;

		public void savetoStream(DataOutputStream dos) throws Exception
		{
			throw new UnsupportedOperationException();
		}

		public void loadFromStream(DataInputStream dis) throws Exception
		{
			int fc = dis.readInt();
			if (fc != SHPConstants.MNM)
				throw new Exception("Error of format");

			int d_rd = 0;
			for (int i = 0; i < 5; i++)
				d_rd = dis.readInt();

			lengthfile = dis.readInt();
			version = readIBOrder(dis);
			shptype = readIBOrder(dis);

			xMin = readDBOrder(dis);
			yMin = readDBOrder(dis);

			xMax = readDBOrder(dis);
			yMax = readDBOrder(dis);

			zMin = readDBOrder(dis);
			zMax = readDBOrder(dis);

			mMin = readDBOrder(dis);
			mMax = readDBOrder(dis);

		}
	}

	public class SHPRecord_Headers implements ISerializer
	{
		int rNumber;
		int length;

		public void savetoStream(DataOutputStream dos) throws Exception
		{
			dos.writeInt(rNumber);
			dos.writeInt(length);
		}

		public void loadFromStream(DataInputStream dis) throws Exception
		{
			rNumber = dis.readInt();
			length = dis.readInt();
		}
	}

	public class SHPRecord implements ISerializer
	{
		private int shptype;
		private SHPRecord_Headers rech;
		private MPoint[][] pnts = null;
		private MRect mbb = null;

		public SHPRecord()
		{
			rech = new SHPRecord_Headers();
		}

		public int getShptype()
		{
			return shptype;
		}

		public SHPRecord_Headers getRech()
		{
			return rech;
		}

		public MPoint[][] getPnts()
		{
			return pnts;
		}

		public MRect getMbb()
		{
			return mbb;
		}

		public void savetoStream(DataOutputStream dos) throws Exception
		{
			rech.savetoStream(dos);
			throw new UnsupportedOperationException();
		}

		public void loadFromStream(DataInputStream dis) throws Exception
		{
			rech.loadFromStream(dis);
			shptype = readIBOrder(dis);
			switch (shptype)
			{
				case SHPConstants.SHP_Point:
				case SHPConstants.SHP_PointM:
				case SHPConstants.SHP_PointZ:
				{
					if (shptype == SHPConstants.SHP_Point)
						setPnt(dis, new MPoint());
					else
					{
						setPnt(dis, new MPointZM());
						if (shptype == SHPConstants.SHP_PointZ)
							setZ(dis);
						setM(dis);
					}
					break;
				}
				case SHPConstants.SHP_MultiPoint:
				case SHPConstants.SHP_MultiPointM:
				case SHPConstants.SHP_MultiPointZ:
				{
					if (shptype == SHPConstants.SHP_MultiPoint)
						setMultiPoint(dis, new MPoint());
					else
					{
						setMultiPoint(dis, new MPointZM());
						if (shptype == SHPConstants.SHP_MultiPointZ)
							setZ(dis);
						setM(dis);
					}
					break;
				}

				case SHPConstants.SHP_PolyLine:
				case SHPConstants.SHP_Polygon:
				case SHPConstants.SHP_PolyLineM:
				case SHPConstants.SHP_PolygonM:
				case SHPConstants.SHP_PolyLineZ:
				case SHPConstants.SHP_PolygonZ:
				{
					if (shptype == SHPConstants.SHP_PolyLine || shptype == SHPConstants.SHP_Polygon)
						setPolyObject(dis, new MPoint());
					else
					{
						setPolyObject(dis, new MPointZM());
						if (shptype == SHPConstants.SHP_PolyLineZ || shptype == SHPConstants.SHP_PolygonZ)
							setZ(dis);
						setM(dis);
					}
					break;
				}
				case SHPConstants.SHP_NULL:
				{
					System.out.println("Found Null asname");
					int cntb = (rech.length) * 4 - 4;
					dis.skipBytes(cntb);
					break;
				}
				default:
				{

					System.out.println("skip = " + shptype);
					//if (!skipedtypes.contains(shptype))
					skipedtypes.add(shptype);
					shptype=-1;

				}
			}

		}


		private void setMBB(DataInputStream dis, MPoint factory)
				throws Exception
		{
			mbb = new MRect(factory.getCopyOfObject(), factory.getCopyOfObject());
			mbb.p1.setXY(readDBOrder(dis), readDBOrder(dis));
			mbb.p4.setXY(readDBOrder(dis), readDBOrder(dis));
			mbb.resetInternalPoint();
		}


		//public boolean npo = false;


		private void setPnt(DataInputStream dis, MPoint factory)
				throws Exception
		{
			MPoint[] lpnts = new MPoint[]{factory.getCopyOfObject()};
			pnts = new MPoint[][]{lpnts};
			pnts[0][0].setXY(readDBOrder(dis), readDBOrder(dis));

//			if (npo)
//			{
//				TODO Не ясно откуда взялись эти вот точки из шейпов предоставленных НПО-регионом, если так читать
//				 тогда все типы совпадают и читается нормально, если нет тогда лезут какие то левый данные
//				double a = readDBOrder(dis);
//				double b = readDBOrder(dis);
//
//				System.out.println("a = " + a);
//				System.out.println("b = " + b);
//			}
			mbb = new MRect(pnts[0][0], pnts[0][0]);
		}

		private void setMultiPoint(DataInputStream dis, MPoint factory)
				throws Exception
		{
			setMBB(dis, factory);
			int pntnum = readIBOrder(dis);
			pnts = new MPoint[pntnum][];
			for (int i = 0; i < pnts.length; i++)
			{
				pnts[i] = new MPoint[]{factory.getCopyOfObject()};
				pnts[i][0].setXY(readDBOrder(dis), readDBOrder(dis));
			}
		}

		private void setPolyObject(DataInputStream dis, MPoint factory)
				throws Exception
		{

			setMBB(dis, factory);

			int cntsegs = readIBOrder(dis);
			if (cntsegs > 1)
				System.out.println("cntsegs:" + cntsegs + " rec:" + rech.rNumber);
			int pntnum = readIBOrder(dis);
			int[] parts = new int[cntsegs];
			for (int i = 0; i < parts.length; i++)
				parts[i] = readIBOrder(dis);

			pnts = new MPoint[cntsegs][];
			List<MPoint> lp = new LinkedList<MPoint>();

			for (int i = 0, j = 0; i < pntnum; i++)
			{
				if (j < parts.length && i == parts[j])
				{
					if (j > 0)
					{
						pnts[j - 1] = lp.toArray(new MPoint[lp.size()]);
						lp.clear();
					}
					j++;
				}
				MPoint pnt;
				lp.add(pnt = factory.getCopyOfObject());
				pnt.setXY(readDBOrder(dis), readDBOrder(dis));
			}
			pnts[cntsegs - 1] = lp.toArray(new MPoint[lp.size()]);//Заполним последний сегмент
		}

		private void setZ(DataInputStream dis)
				throws Exception
		{
			((MPointZM) mbb.p1).setZ(readDBOrder(dis));
			((MPointZM) mbb.p4).setZ(readDBOrder(dis));
			for (MPoint[] pnt : pnts)
				for (MPoint aPnt : pnt)
					((MPointZM) aPnt).setZ(readDBOrder(dis));
		}

		private void setM(DataInputStream dis)
				throws Exception
		{
			((MPointZM) mbb.p1).setM(readDBOrder(dis));
			((MPointZM) mbb.p4).setM(readDBOrder(dis));
			for (MPoint[] pnt : pnts)
				for (MPoint aPnt : pnt)
					((MPointZM) aPnt).setM(readDBOrder(dis));
		}
	}
}
