package ru.ts.conv.rshp;

import ru.ts.toykernel.storages.IEditableStorage;
import ru.ts.toykernel.storages.INodeStorage;
import ru.ts.toykernel.attrs.def.DefaultAttrsImpl;
import ru.ts.toykernel.attrs.def.DefAttrImpl;
import ru.ts.toykernel.attrs.IAttrs;
import ru.ts.toykernel.consts.KernelConst;
import ru.ts.toykernel.geom.IEditableGisObject;
import ru.ts.toykernel.geom.def.ObjGeomUtils;
import ru.ts.gisutils.algs.common.MPoint;
import ru.ts.gisutils.algs.common.MRect;
import ru.ts.utils.data.Pair;
import ru.ts.factory.IInitAble;

import java.io.*;
import java.util.*;

import com.linuxense.javadbf.DBFReader;
import com.linuxense.javadbf.DBFField;

/**
 * Первая версия читателя SHP файла
 */
public class SHPImporter
{

	public static final String[] classictypeorder ={KernelConst.LINEARRING,KernelConst.LINESTRING,KernelConst.POINT};

	protected static final String defcharset = "WINDOWS-1251";
	public boolean testmode =true;
	protected String ixfile;
	protected String charset;
	protected String mainfile;
	protected String dbffile;
	protected String attrasname = KernelConst.ATTR_CURVE_NAME;
	protected Map<Integer, String> shp2geotype = new HashMap<Integer, String>();
	protected TransformPntsArr trans;
	protected IStorageFactory storfact;

	protected ObjGeomUtils algs = new ObjGeomUtils();
	protected ReadShp readShp;
	public SHPImporter(String mainfile,String dbffile,String ixfile,String charset)
	{
		this.mainfile = mainfile;
		this.dbffile = dbffile;
		this.ixfile = ixfile;

		this.charset=charset;
		if (charset==null)
			this.charset=defcharset;
		setShape2Geotype(shp2geotype);
	}

	public  static void setShape2Geotype(Map<Integer, String> shp2geotype)
	{
		shp2geotype.put(SHPConstants.SHP_Point, KernelConst.POINT);
		shp2geotype.put(SHPConstants.SHP_PointM, KernelConst.POINT);
		shp2geotype.put(SHPConstants.SHP_PointZ, KernelConst.POINT);

		shp2geotype.put(SHPConstants.SHP_MultiPoint, KernelConst.POINT);
		shp2geotype.put(SHPConstants.SHP_MultiPointM, KernelConst.POINT);
		shp2geotype.put(SHPConstants.SHP_MultiPointZ, KernelConst.POINT);


		shp2geotype.put(SHPConstants.SHP_PolyLine, KernelConst.LINESTRING);
		shp2geotype.put(SHPConstants.SHP_PolyLineM, KernelConst.LINESTRING);
		shp2geotype.put(SHPConstants.SHP_PolyLineZ, KernelConst.LINESTRING);

		shp2geotype.put(SHPConstants.SHP_Polygon, KernelConst.LINEARRING);
		shp2geotype.put(SHPConstants.SHP_PolygonM, KernelConst.LINEARRING);
		shp2geotype.put(SHPConstants.SHP_PolygonZ, KernelConst.LINEARRING);
	}

	public Map<Integer, String> getShp2geotype()
	{
		return shp2geotype;
	}

	public List<Integer> getSkipedtypes()
	{
		return readShp.getSkipedtypes();
	}

	public void setStorageFactory(IStorageFactory storfact)
	{
		this.storfact = storfact;
	}

	public void setTrans(TransformPntsArr trans)
	{
		this.trans = trans;
	}

	public MRect shp2Storage(MRect inMBB) throws Exception
	{
		DataInputStream shpis=null;
		InputStream dbfis = null;
		try
		{
			shpis = new DataInputStream(new BufferedInputStream(new FileInputStream(mainfile)));
			if (ixfile!=null)
				readShp = new ReadShp(new DataInputStream(new BufferedInputStream(new FileInputStream(ixfile))));
			else
				readShp = new ReadShp();
			readShp.loadFromStream(shpis);
			shpis.close();

			//IEditableStorage storage = storfact.createStorage();

			//Читаем аттрибуты и заполняем пулл пустыми строками?
			dbfis = new FileInputStream(dbffile); // take dbf file as program argument
			DBFReader reader = new DBFReader(dbfis);
			reader.setCharactersetName(this.charset);

			final int reccount = readShp.recs.size();
			System.out.println("record count in the " + dbffile+" = " + reccount);

			for (int j = 0; j < reccount; j++)
			{
				ReadShp.SHPRecord shpRecord = readShp.recs.get(j);
				if (shpRecord.getShptype()<0) //Если мы не распознали тип
				{
					reader.nextRecord();
					System.out.println("skip data for unknown type");
					continue;
				}

				HashMap<String, String> fname2val = new HashMap<String, String>();

				String curveName=null;
				{
//Получим список полей, которые есть в аттрибутах
					List<String> fnames= new LinkedList<String>();
					int numberOfFields = reader.getFieldCount();
					for (int i = 0; i < numberOfFields; i++)
					{
						DBFField field = reader.getField(i);
						String fname = field.getName();
						fnames.add(fname);
					}
					Object[] rawObjects = reader.nextRecord();
					if (rawObjects!=null)
						for (int i = 0; i < fnames.size(); i++)
						{
							String fname = fnames.get(i);
							if (rawObjects[i] != null)
								fname2val.put(fname, String.valueOf(rawObjects[i]).trim());
						}
				}

				String geotype = shp2geotype.get(shpRecord.getShptype());

				Pair<IEditableStorage, String> storAasname = storfact.getStorage(geotype, shpRecord, fname2val);
				IEditableStorage storage= storAasname.first;
				curveName= fname2val.get(storAasname.second);


				if (storage instanceof INodeStorage)
				{
					IAttrs defAttrs = ((INodeStorage) storage).getDefAttrs();
					if (defAttrs==null || !defAttrs.containsKey(SHPConstants.ORIG_TYPE))
					{
						DefaultAttrsImpl defattrs = new DefaultAttrsImpl();
						for (String fname : fname2val.keySet())
							defattrs.put(fname,new DefAttrImpl(fname,""));
						defattrs.put(SHPConstants.ORIG_TYPE,new DefAttrImpl(SHPConstants.ORIG_TYPE,String.valueOf(readShp.mfh.shptype)));
						((INodeStorage)storage).setDefAttrs(defattrs);
					}
				}

				MPoint[][] rings = shpRecord.getPnts();
				if (rings==null)
				{
					System.out.println("rings is null: continue");
					continue;
				}
				for (MPoint[] pnt : rings)
					if (trans != null)
						trans.transform(pnt);


				IEditableGisObject est = null;
				if (geotype.equals(KernelConst.LINEARRING))
				{
					List<List<MPoint[]>> clusters = new LinkedList<List<MPoint[]>>();

					for (MPoint[] ring : rings)
					{
						cl:
						if (ring.length > 3) //Рассматтриваем кольца от трех разных точек
						{
							for (List<MPoint[]> cl_rings : clusters) //1.Проверка взаимовключения по всем сформированным кластрам кластерам
								for (int k = 0; k < cl_rings.size(); k++)  // Пробежимся внтри кластера и найдем место дляя текущего кольца
								{
									MPoint[] cl_ring = cl_rings.get(k);//Следующее концетрическое кольцо
									if (cl_ring.length > 1)
									{
										if (сontainsIn1stArg(cl_ring, ring, -1))
										{ //ring находится внутри cl_ring
											cl_rings.add(k + 1, ring);
											break cl;
										}

										if (сontainsIn1stArg(ring, cl_ring, -1))
										{ //cl_ring находится внутри ring
											cl_rings.add(k, ring);
											break cl;
										}
									}
								}
							List<MPoint[]> ll = new LinkedList<MPoint[]>();
							clusters.add(ll);
							ll.add(ring);
						}
						else
							System.out.println("Skip error length in polygon:"+j);
					}


					boolean dirty = false;
					for (List<MPoint[]> cl_rings : clusters)
					{
						boolean bs = false;
						for (int i = 0; i < cl_rings.size(); i++)
						{

							MPoint[] ring = cl_rings.get(i);

							if (testmode && cl_rings.size() > 1)
							{
								boolean b = algs.isRigthInternal(ring);
								if (i == 0)
									bs = b;
								else if (i == 1)
									bs = (b == bs); //Если внутреннее кольцо имеет тоже направление что и внешнее
								System.out.println("polygon "+j+" ring " + i + " RigthInternal = " + b);
							}

//							if (j==77)
							{
								if (est == null)
									est = storage.createObject(geotype);
								est.addSegment(est.getSegsNumbers(), ring);
							}
						}
						dirty = dirty || (bs && cl_rings.size() > 1);
					}

					if (dirty)
						System.out.println("dirty rings found for polygon:" + j);
					if (est != null)
					{
						if (clusters.size() == rings.length) //Не парим мозг если у нас все кластера разные и не пересекаются
							est.setGeotype(KernelConst.LINEARRING);
						else
							est.setGeotype(KernelConst.LINEARRINGH);
					}
				}
				else
				{

					est = storage.createObject(geotype);
					for (MPoint[] ring : rings)
						est.addSegment(est.getSegsNumbers(), ring);
				}


				if (est != null)
				{
					DefaultAttrsImpl objatrrs = new DefaultAttrsImpl();
					{
						for (String fname : fname2val.keySet())
						{
							String val=fname2val.get(fname);
							objatrrs.put(fname, new DefAttrImpl(fname, val));
						}
						objatrrs.put(SHPConstants.ORIG_TYPE, new DefAttrImpl(SHPConstants.ORIG_TYPE, String.valueOf(shpRecord.getShptype()).trim()));

						if (curveName!=null && curveName.length()>0)
							objatrrs.put(attrasname, new DefAttrImpl(attrasname, curveName));
						est.setCurveAttrs(objatrrs);
					}


					if (storage.getSizeNotCommited()>=40000)
					{
						storage.commit();
						String storgename="";
						if (storage instanceof IInitAble)
							   storgename="for storage:"+((IInitAble)storage).getObjName();
						System.out.println("Commited " + j + " records "+storgename);
					}
				}

				if (est!=null)
				{
					est.rebuildGisValume();
					inMBB=est.getMBB(inMBB);
				}

				if (j%5000== 0)
				{
					System.out.println("proceed " + j + " records");
				}


			}
		}
		finally
		{
			try
			{
				if (shpis!=null)
					shpis.close();
			}
			catch (IOException e)
			{//
			}

			try
			{
				if (dbfis!=null)
					dbfis.close();
			}
			catch (IOException e)
			{//
			}

		}
		return inMBB;
	}

	public MRect shp2Storage(IEditableStorage storage,String curveNameInShape,MRect inMBB) throws Exception
	{
		DataInputStream shpis=null;
		InputStream dbfis = null;
		try
		{

			shpis = new DataInputStream(new BufferedInputStream(new FileInputStream(mainfile)));
			if (ixfile!=null)
				readShp = new ReadShp(new DataInputStream(new BufferedInputStream(new FileInputStream(ixfile))));
			else
				readShp = new ReadShp();
			readShp.loadFromStream(shpis);
			shpis.close();

			//IEditableStorage storage = storfact.createStorage();

			//Читаем аттрибуты и заполняем пулл пустыми строками?
			dbfis = new FileInputStream(dbffile); // take dbf file as program argument
			DBFReader reader = new DBFReader(dbfis);
			reader.setCharactersetName(this.charset);


			List<String> fnames= null;
			int jcurveNameInShape=-1;
			if (storage instanceof INodeStorage)
			{
				fnames= new LinkedList<String>();
				DefaultAttrsImpl defattrs = new DefaultAttrsImpl();
				int numberOfFields = reader.getFieldCount();
				for (int i = 0; i < numberOfFields; i++)
				{
					DBFField field = reader.getField(i);
					String fname = field.getName();
					defattrs.put(fname,new DefAttrImpl(fname,""));
					fnames.add(fname);
				}
				defattrs.put(SHPConstants.ORIG_TYPE,new DefAttrImpl(SHPConstants.ORIG_TYPE,String.valueOf(readShp.mfh.shptype)));
				((INodeStorage)storage).setDefAttrs(defattrs);
				jcurveNameInShape=fnames.indexOf(curveNameInShape);
			}


			final int reccount = readShp.recs.size();
			System.out.println("reccount = " + reccount);
			for (int j = 0; j < reccount; j++)
			{
				ReadShp.SHPRecord shpRecord = readShp.recs.get(j);
				if (shpRecord.getShptype()<0) //Если мы не распознали тип
				{
					reader.nextRecord();
					System.out.println("skip data for unknown type");
					continue;
				}


				String geotype = shp2geotype.get(shpRecord.getShptype());
				MPoint[][] rings = shpRecord.getPnts();
				if (rings==null)
				{
					System.out.println("rings is null: continue");
					continue;
				}
				for (MPoint[] pnt : rings)
					if (trans != null)
						trans.transform(pnt);


				IEditableGisObject est = null;
				if (geotype.equals(KernelConst.LINEARRING))
				{
					List<List<MPoint[]>> clusters = new LinkedList<List<MPoint[]>>();

					for (MPoint[] ring : rings)
					{
						cl:
						if (ring.length > 3) //Рассматтриваем кольца от трех разных точек
						{
							for (List<MPoint[]> cl_rings : clusters) //1.Проверка взаимовключения по всем сформированным кластрам кластерам
								for (int k = 0; k < cl_rings.size(); k++)  // Пробежимся внтри кластера и найдем место дляя текущего кольца
								{
									MPoint[] cl_ring = cl_rings.get(k);//Следующее концетрическое кольцо
									if (cl_ring.length > 1)
									{
										if (сontainsIn1stArg(cl_ring, ring, -1))
										{ //ring находится внутри cl_ring
											cl_rings.add(k + 1, ring);
											break cl;
										}

										if (сontainsIn1stArg(ring, cl_ring, -1))
										{ //cl_ring находится внутри ring
											cl_rings.add(k, ring);
											break cl;
										}
									}
								}
							List<MPoint[]> ll = new LinkedList<MPoint[]>();
							clusters.add(ll);
							ll.add(ring);
						}
						else
							System.out.println("Skip error length in polygon:"+j);
					}


					boolean dirty = false;
					for (List<MPoint[]> cl_rings : clusters)
					{
						boolean bs = false;
						for (int i = 0; i < cl_rings.size(); i++)
						{

							MPoint[] ring = cl_rings.get(i);

							if (testmode && cl_rings.size() > 1)
							{
								boolean b = algs.isRigthInternal(ring);
								if (i == 0)
									bs = b;
								else if (i == 1)
									bs = (b == bs); //Если внутреннее кольцо имеет тоже направление что и внешнее
								System.out.println("polygon "+j+" ring " + i + " RigthInternal = " + b);
							}

//							if (j==77)
							{
								if (est == null)
									est = storage.createObject(geotype);
								est.addSegment(est.getSegsNumbers(), ring);
							}
						}
						dirty = dirty || (bs && cl_rings.size() > 1);
					}

					if (dirty)
						System.out.println("dirty rings found for polygon:" + j);
					if (est != null)
					{
						if (clusters.size() == rings.length) //Не парим мозг если у нас все кластера разные и не пересекаются
							est.setGeotype(KernelConst.LINEARRING);
						else
							est.setGeotype(KernelConst.LINEARRINGH);
					}
				}
				else
				{
					est = storage.createObject(geotype);
					for (MPoint[] ring : rings)
						est.addSegment(est.getSegsNumbers(), ring);
				}


				if (est != null && fnames != null && fnames.size() > 0)
				{
					DefaultAttrsImpl objatrrs = new DefaultAttrsImpl();
					Object[] rawObjects;
					if ((rawObjects = reader.nextRecord()) != null)
					{
						for (int i = 0; i < rawObjects.length; i++)
						{
							String fname = fnames.get(i);
							if (rawObjects[i] != null)
								objatrrs.put(fname, new DefAttrImpl(fname, String.valueOf(rawObjects[i]).trim()));
						}
						objatrrs.put(SHPConstants.ORIG_TYPE, new DefAttrImpl(SHPConstants.ORIG_TYPE, String.valueOf(shpRecord.getShptype()).trim()));

						if (jcurveNameInShape >= 0)
							objatrrs.put(attrasname, new DefAttrImpl(attrasname, objatrrs.get(fnames.get(jcurveNameInShape)).getValue()));
//						else
//							objatrrs.put(KernelConst.attrAsName, new DefAttrImpl(KernelConst.attrAsName, String.valueOf(j)));
						est.setCurveAttrs(objatrrs);
					}

					if (j % 30000 == 0)
					{
						storage.commit();
						System.out.println("Commited " + j + " records");
					}

//					if (j>=10000)
//						break;
				}

				if (est!=null)
				{
					est.rebuildGisValume();
					inMBB=est.getMBB(inMBB);
				}
			}

			storage.commit();
		}
		finally
		{
			try
			{
				if (shpis!=null)
					shpis.close();
			}
			catch (IOException e)
			{//
			}

			try
			{
				if (dbfis!=null)
					dbfis.close();
			}
			catch (IOException e)
			{//
			}

		}
		return inMBB;
	}

	private boolean сontainsIn1stArg(MPoint[] ring, MPoint[] cl_ring,int maxln)
	{
		for (int i = 0; i < (maxln<0?cl_ring.length:maxln); i++)
		{
			MPoint rgnpnt = cl_ring[i];
			if (!algs.isInPolyGon(rgnpnt, ring))
				return false;
		}
		return true;
	}

	public static interface TransformPntsArr
	{
		void transform(MPoint[] pnts) throws Exception;
	}

	public static interface IStorageFactory
	{
		Pair<IEditableStorage,String> getStorage(String geotype, ReadShp.SHPRecord record, Map<String, String> fname2val) throws Exception;
	}


//	private boolean checkContains(MPoint[] ring, MPoint[] cl_ring)
//	{
//		int x[]=new int[ring.length];
//		int y[]=new int[ring.length];
//		for (int i = 0; i < y.length; i++)
//		{
//			x[i] =(int)(ring[i].x);
//			y[i] =(int)(ring[i].y);
//
//		}
//
//		Polygon poly=new Polygon(x,y,x.length-1);
//		boolean b1 = poly.contains(cl_ring[1].x , cl_ring[1].y);
//		boolean b2 = poly.contains(cl_ring[0].x , cl_ring[0].y);
//		boolean b= b2 && b1;
//
//		System.out.println("b = " + b);
//		return b;
//	}


//	private boolean contains(MPoint[] ring,double x, double y) {
//
//
//		int xpoints[]=new int[ring.length];
//		int ypoints[]=new int[ring.length];
//		for (int i = 0; i < ypoints.length; i++)
//		{
//			xpoints[i] =(int)(ring[i].x);
//			ypoints[i] =(int)(ring[i].y);
//
//		}
//
//		int npoints = ypoints.length;
//
//		if (npoints <= 2)
//		{
//			return false;
//		}
//
//
//		int hits = 0;
//
//		int lastx = xpoints[npoints - 1];
//		int lasty = ypoints[npoints - 1];
//		int curx, cury;
//
//		// Walk the edges of the polygon
//		for (int i = 0; i < npoints; lastx = curx, lasty = cury, i++) {
//			curx = xpoints[i];
//			cury = ypoints[i];
//
//			if (cury == lasty) {
//			continue;
//			}
//
//			int leftx;
//			if (curx < lastx) {
//			if (x >= lastx) {
//				continue;
//			}
//			leftx = curx;
//			} else {
//			if (x >= curx) {
//				continue;
//			}
//			leftx = lastx;
//			}
//
//			double test1, test2;
//			if (cury < lasty) {
//			if (y < cury || y >= lasty) {
//				continue;
//			}
//			if (x < leftx) {
//				hits++;
//				continue;
//			}
//			test1 = x - curx;
//			test2 = y - cury;
//			} else {
//			if (y < lasty || y >= cury) {
//				continue;
//			}
//			if (x < leftx) {
//				hits++;
//				continue;
//			}
//			test1 = x - lastx;
//			test2 = y - lasty;
//			}
//
//			if (test1 < (test2 / (lasty - cury) * (lastx - curx))) {
//			hits++;
//			}
//		}
//
//		return ((hits & 1) != 0);
//		}

}
