package su.org.susgsm.readers.polish;

import su.org.susgsm.readers.ParseRecException;

import java.util.*;
import java.io.BufferedReader;
import java.io.IOException;

/**
 * Чтец польского формата
 */
public class PolishReader
{
	public static final String ORIG_TYPE = "origtype";//Аттрибут с которым сохраняется в нашу БД
	//  тип объекта польского формата ([POI],[POLYLINE],[POLYGON])

//	public static MpGeoObject createByReader(BufferedReader reader) throws IOException, ParseRecException
//	{
//
//		MpGeoObject rv = new MpGeoObject();
//
//		String line = null;
//
//		br:
//		for (; ;)
//		{
//			line = reader.readLine();
//			if (line == null)
//				throw new IOException();
//
//			if (line.startsWith(";"))
//			{
//				rv.startcomments.add(line);//Пополнить комментарии
//				continue;
//			}
//			else
//			{
//				for (String anArrtype : MpRecord.arrtype)
//					if (line.equalsIgnoreCase(anArrtype))
//					{
//						rv.supertype = line;
//						break br;
//					}
//			}
//			throw new ParseRecException();
//		}
//
//		while (!(line = reader.readLine()).equalsIgnoreCase(MpRecord.KEYEND))
//		{
//			String[] spl = line.split("=");
//			if (spl == null || spl[0] == null)
//				break;
//
//			if (spl[0].startsWith("Data"))
//			{
//				int level = Integer.parseInt(spl[0].substring("Data".length()));
//
//				List<Data[]> ldata=rv.datas.get(level);
//				if (ldata==null)
//					rv.datas.put(level,ldata=new LinkedList<Data[]>());
//				Data[] ardata = Utils.generateDataArrayByString(spl[1]);
//				ldata.add(ardata);
//			}
//			else if (spl.length > 1)
//				rv.parnames.add(new Pair<String, String>(spl[0], spl[1]));
//		}
//
////		if (rv.streetDesc == null)
////			rv.streetDesc = rv.label;
//
//		return rv;
//	}

	/**
	 * Отдать набор объектов прочитав их из файла в  польском формате
	 * @param br - откуда читаем
	 * @param filter - фильтр объектов
	 * @return - итератор объектов
	 */
	public static Iterator<MpRecord> getObjects(final BufferedReader br, final IMpFilter filter)
	{
		return new Iterator<MpRecord>()
		{

			MpRecord geoobj=null;

			public boolean hasNext()
			{
				if (geoobj==null)
					geoobj=getNextElem(br, filter);
				return geoobj!=null;
			}

			public MpRecord next()
			{
				MpRecord rv=geoobj;
				geoobj=null;
				if (rv==null)
					rv=getNextElem(br, filter);
				if (rv!=null)
					return rv;
				throw new NoSuchElementException();
			}

			public void remove()
			{
				throw new UnsupportedOperationException();
			}
		};
	}

	public static MpRecord  getNextElem(BufferedReader br, IMpFilter filter)
	{
		for (; ;)
		{

			try
			{
				MpRecord geoobj = MpGeoObject.createByReader(br);
				if (filter == null || (geoobj = filter.accept(geoobj))!=null)
					return geoobj;
			}
			catch (ParseRecException e)
			{
			}
			catch (IOException e)
			{
				break;
			}
		}
		return null;
	}

//	public static List<MpRecord> setByReader(BufferedReader br, IMpFilter filter)
//	{
//		List<MpRecord> geoObjects = new LinkedList<MpRecord>();
//		for (; ;)
//		{
//			try
//			{
//				MpRecord geoobj = MpGeoObject.createByReader(br);
//				if (filter == null || (geoobj = filter.accept(geoobj))!=null)
//					geoObjects.add(geoobj);
//			}
//			catch (ParseRecException e)
//			{
////				e.printStackTrace();
//			}
//			catch (IOException e)
//			{
//				break;
//			}
//		}
//		return geoObjects;
//	}

}
