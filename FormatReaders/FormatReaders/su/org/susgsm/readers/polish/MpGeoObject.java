package su.org.susgsm.readers.polish;


import su.org.susgsm.readers.Data;
import su.org.susgsm.readers.ParseRecException;

import java.io.*;
import java.awt.geom.Point2D;
import java.awt.*;
import java.util.*;
import java.util.List;

import ru.ts.utils.data.Pair;

/**
 * Объект для представления единицы в польской записи
 */
public class MpGeoObject extends MpRecord
{


	public String supertype;//Тип из множества arrtype
	protected Map<Integer, List<Data[]>> datas=new HashMap<Integer, List<Data[]>>(); //Данные level -> список данных

	public MpGeoObject()
	{

	}

	public MpGeoObject(MpGeoObject obj,int d_level)
	{
		super(obj);

		this.supertype=obj.supertype;
		List<Data[]> rings=obj.datas.get(d_level);
		if (rings!=null)
		{
			List<Data[]> l_rings = new LinkedList<Data[]>();
			datas.put(d_level, l_rings);

			for (Data[] ring : rings)
			{
				Data[] l_ring=new Data[ring.length];
				for (int i = 0; i < ring.length; i++)
					l_ring[i]=new Data(ring[i]);
				l_rings.add(l_ring);
			}
		}
	}

	public MpGeoObject(String supertype,List<String> startcomments, List<Pair<String, String>> parnames)
	{
		super(startcomments, parnames);
		this.supertype=supertype;
	}

	public static MpRecord createByReader(BufferedReader reader) throws IOException, ParseRecException
	{

		MpRecord rv=null;

		List<String> startcomments = new LinkedList<String>(); //Список комментариев
		String line = null;

		for (; ;)
		{
			line = reader.readLine();
			if (line == null)
				throw new IOException();

			if (line.startsWith(";"))
			{
				startcomments.add(line);//Пополнить комментарии
				continue;
			}
			else if (line.length()==0)
				continue;
			else
			{ //Инициализировать объект записи
				if (line.trim().startsWith("["))
				{
					for (String supertype : arrtype)
						if (line.equalsIgnoreCase(supertype))
						{
							rv=new MpGeoObject(supertype,startcomments,null);
							return rv.initByReader(reader,line);
						}
					rv=new MpRecord(startcomments,null);
					return rv.initByReader(reader,line);
				}
			}
			throw new ParseRecException();
		}
	}

	public String getSupertype()
	{
		return supertype;
	}

	public int getnPoints()
	{
		int npnts=0;
		for (List<Data[]> ldata: datas.values())
			for (Data[] rdata : ldata)
				npnts+=rdata.length;
		return npnts;
	}

	/**
	 * Отдать все данные сооотношение уровень->данные
	 * @return - сооотношение уровень->данные
	 */
	public Map<Integer, List<Data[]>> getDatas()
	{
		return datas;
	}

	/**
	 * @return Отдать наиболеее точные данные
	 */
	public List<Data[]> getPreciseDatasRow()
	{
		if (datas.size()>0)
		{
			int minlevel=Collections.min(datas.keySet());
			return datas.get(minlevel);
		}
		return null;
	}

	public List<Data[]> getRudeDatasRow()
	{
		if (datas.size()>0)
		{
			int max=Collections.max(datas.keySet());
			return datas.get(max);
		}
		return null;
	}

	public Point2D[] getBound()
	{
		if (datas == null)
			return null;

		Point2D pntMinXMinY = null;
		Point2D pntMaxXMaxY = null;

		//find most precise level (мы считаем что данные согласованы)
		int minlevel = Collections.min(datas.keySet());

		List<Data[]> ddatas = datas.get(minlevel);

		for (Data[] ardata : ddatas)
			for (Data data : ardata)
			{
				if (pntMinXMinY == null && pntMaxXMaxY == null)
				{
					pntMinXMinY = new Point.Double(data.getLat(), data.getLon());
					pntMaxXMaxY = new Point.Double(data.getLat(), data.getLon());
				}
				else
				{
					pntMinXMinY.setLocation(
							Math.min(pntMinXMinY.getX(), data.getLat()),
							Math.min(pntMinXMinY.getY(), data.getLon())
					);
					pntMaxXMaxY.setLocation
							(
									Math.max(pntMaxXMaxY.getX(), data.getLat()),
									Math.max(pntMaxXMaxY.getY(), data.getLon())
							);
				}
			}
		return new Point2D[]{pntMinXMinY, pntMaxXMaxY};
	}

	public String toString()
	{
		StringBuffer buff = new StringBuffer();
		buff.append(supertype).append("\n");

		for (Pair<String, String> parname : parnames)
			buff.append(parname.first).append("=").append(parname.second).append("\n");

//		buff.append("Type=").append(type).append("\n");
//		buff.append("Label=").append(label).append("\n");
//		buff.append("StreetDesc=").append(streetDesc).append("\n");
//		buff.append("RoadID=").append(roadID).append("\n");

		TreeSet<Integer> trset = new TreeSet<Integer>(datas.keySet());
		for (Integer level : trset)
		{
			buff.append("Data").append(level).append("=");

			List<Data[]> ddatas = datas.get(level);
			if (ddatas != null)
			{
				for (Data[] ardata : ddatas)
					for (int i = 0; i < ardata.length; i++)
					{
						if (i > 0)
							buff.append(",");
						buff.append(Utils.toString(ardata[i]));
					}
				buff.append("\n");
			}
		}
		buff.append(KEYEND);
		return buff.toString();
	}

	public boolean parseLine(String[] spl)
	{
		if (spl[0].startsWith("Data"))
		{
			int level = Integer.parseInt(spl[0].substring("Data".length()));

			List<Data[]> ldata=datas.get(level);
			if (ldata==null)
				datas.put(level,ldata=new LinkedList<Data[]>());
			Data[] ardata = Utils.generateDataArrayByString(spl[1]);
			ldata.add(ardata);
			return true;
		}
		return false;
	}

//	public static void main(String[] args) throws Exception
//	{
//
//		BufferedReader rd = new BufferedReader(
//				new StringReader(
//						"[POLYLINE]\n" +
//								"Type=0x6\n" +
//								"Label=Металлистов ул.\n" +
//								"StreetDesc=Металлистов ул.\n" +
//								"CityIdx=1\n" +
//								"RoadID=3147\n" +
//								"RouteParam=2,1,0,0,0,0,0,0,0,0,0,1\n" +
//								"Data0=(57.81510,28.35565),(57.81338,28.35404),(57.81186,28.35264),(57.81077,28.35157),(57.80694,28.34792),(57.80669,28.34769),(57.80654,28.34752),(57.80574,28.34687),(57.80538,28.34644),(57.80514,28.34608),(57.80424,28.34524),(57.80416,28.34507),(57.80385,28.34393)\n" +
//								"Nod1=0,4915,0\n" +
//								"Nod2=1,6174,0\n" +
//								"Nod3=2,4913,0\n" +
//								"Nod4=3,4987,0\n" +
//								"Nod5=4,6462,0\n" +
//								"Nod6=5,4912,0\n" +
//								"Nod7=8,4977,0\n" +
//								"Nod8=12,4911,0\n" +
//								"[END]")
//		);
//		MpRecord pol = createByReader(rd);
//		System.out.println("pol.toString() = \n" + pol.toString());
//	}

}
