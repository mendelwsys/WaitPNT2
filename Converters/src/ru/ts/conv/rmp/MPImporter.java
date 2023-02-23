package ru.ts.conv.rmp;

import ru.ts.gisutils.algs.common.MPoint;
import ru.ts.toykernel.storages.IEditableStorage;
import ru.ts.toykernel.storages.INodeStorage;
import ru.ts.toykernel.consts.KernelConst;
import ru.ts.toykernel.geom.IEditableGisObject;
import ru.ts.toykernel.geom.def.ObjGeomUtils;
import ru.ts.toykernel.attrs.def.DefaultAttrsImpl;
import ru.ts.toykernel.attrs.def.DefAttrImpl;
import ru.ts.utils.data.Pair;
import su.org.susgsm.readers.polish.MpGeoObject;
import su.org.susgsm.readers.polish.PolishReader;
import su.org.susgsm.readers.polish.IMpFilter;
import su.org.susgsm.readers.polish.MpRecord;
import su.org.susgsm.readers.Data;

import java.util.*;
import java.io.FileInputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.InputStream;


/**
 * TODO Ввести фильтр который разделяет данные из одного файла по слоям и формирует базу данных
 */
public class MPImporter
{
	protected Map<String, String> mp2geotype = new HashMap<String, String>();
	private TransformPntsArr trans;


	public MPImporter()
	{
		mp2geotype.put(MpGeoObject.KEYPNT, KernelConst.POINT);
		mp2geotype.put(MpGeoObject.KEYLINE, KernelConst.LINESTRING);
		mp2geotype.put(MpGeoObject.KEYGON, KernelConst.LINEARRING);
	}

	public void setTrans(TransformPntsArr trans)
	{
		this.trans = trans;
	}

	public Iterator<MpRecord> getMPByFileName(String filename,String encoding,IMpFilter filter) throws Exception
	{
		FileInputStream is = new FileInputStream(filename);
		return PolishReader.getObjects(new BufferedReader(new InputStreamReader(is, encoding)),filter);
	}

	public Iterator<MpRecord> getMPByFileName(BufferedReader br,IMpFilter filter) throws Exception
	{
		return PolishReader.getObjects(br,filter);
	}

	public Iterator<MpRecord> getMPByFileName(InputStream is,String encoding,IMpFilter filter) throws Exception
	{
		return PolishReader.getObjects(new BufferedReader(new InputStreamReader(is, encoding)),filter);
	}

	public void addMp2Storage(String attrAsName, Iterator<MpRecord> objs,IEditableStorage storage) throws Exception
	{
		DefaultAttrsImpl defattrs = new DefaultAttrsImpl();
		((INodeStorage)storage).setDefAttrs(defattrs);

		int i=0;
		while (objs.hasNext())
		{

			MpRecord _obj = objs.next();

			if (!(_obj instanceof MpGeoObject))
				continue;
			MpGeoObject obj = (MpGeoObject)_obj;

			if (i==0) //Установка по умолчанию типа первого объекта, т.к. он обычно не должен изменятся
				defattrs.put(PolishReader.ORIG_TYPE,new DefAttrImpl(PolishReader.ORIG_TYPE,obj.getSupertype()));
			List<Pair<String, String>> params = obj.getParnames();
			for (Pair<String, String> param : params)
				if (!defattrs.containsKey(param.first))
					defattrs.put(param.first,new DefAttrImpl(param.first,""));
			addMp2Storage(attrAsName,obj,storage);
			i++;
			if (i%500==0)
				storage.commit();
		}
	}

	public IEditableGisObject addMp2Storage(String asname,MpGeoObject obj,IEditableStorage storage) throws Exception
	{
		String geotype = mp2geotype.get(obj.getSupertype());

		List<Data[]> drings = obj.getPreciseDatasRow();
		List<MPoint[]> rings = new LinkedList<MPoint[]>();

		for (Data[] dring : drings)
			if (trans != null)
				rings.add(trans.transform(dring));
			else
			{
				MPoint[] rv = new MPoint[dring.length];
				for (int i = 0; i < dring.length; i++)
					rv[i]=new MPoint(dring[i].getLat(),dring[i].getLon());
				rings.add(rv);
			}



		IEditableGisObject est = storage.createObject(geotype);
		DefaultAttrsImpl objatrrs = new DefaultAttrsImpl();

		ObjGeomUtils algs = new ObjGeomUtils();

		MPoint[] outerring=null;

		for (MPoint[] ring : rings)
		{
			if (MpGeoObject.KEYGON.equals(obj.getSupertype()))
			{ //проверяем  текущий
				if (ring.length>0)
				{
					if (outerring==null)
						outerring=ring;
					else if (algs.isInPolyGon(ring[0],ring))
					{
						geotype=KernelConst.LINEARRINGH;//Тип становится - полигон с дыркой
						//Делаем так что бы обход внутреннего прямоугольника был противоположным внешнему
						if (algs.isRigthInternal(ring)==algs.isRigthInternal(outerring))
						{
							MPoint[] l_ring=new MPoint[ring.length]; //(Мы создаем копию кольца а не инвертируем сам массив)
							//Реверсируем массив для правильной прорисовки дырок
							for ( int i = 0; i < ring.length/2; i++ )
							{
								l_ring[i]=ring[ring.length-i-1];
								l_ring[ring.length-i-1]=ring[i];
							}

							if (ring.length%2!=0) //Не четное кол-во элементов, дописываем среднмий элемент
							{
								int mix=ring.length/2;
								l_ring[mix]=ring[mix];
							}
							ring=l_ring;
						}
					}
					else
						outerring=ring;
				}
			}
			est.addSegment(est.getSegsNumbers(), ring);
		}


		List<Pair<String, String>> params = obj.getParnames();
		for (Pair<String, String> param : params)
		{
			objatrrs.put(param.first, new DefAttrImpl(param.first, param.second));
			if (param.first.equals(asname))
				objatrrs.put(KernelConst.ATTR_CURVE_NAME, new DefAttrImpl(KernelConst.ATTR_CURVE_NAME, param.second));
		}
		objatrrs.put(PolishReader.ORIG_TYPE,new DefAttrImpl(PolishReader.ORIG_TYPE,obj.getSupertype()));

		est.setCurveAttrs(objatrrs);

		return est;
	}

	public static interface TransformPntsArr
	{
		MPoint[] transform(Data[] pnts) throws Exception;
		}


}
