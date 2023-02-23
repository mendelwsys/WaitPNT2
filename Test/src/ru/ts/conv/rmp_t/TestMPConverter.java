package ru.ts.conv.rmp_t;

import su.org.susgsm.readers.polish.MpGeoObject;
import su.org.susgsm.readers.polish.IMpFilter;
import su.org.susgsm.readers.polish.MpRecord;
import su.org.susgsm.readers.Data;

import java.util.List;
import java.util.Iterator;
import java.io.FileInputStream;
import java.io.BufferedInputStream;

import ru.ts.toykernel.storages.mem.MemEditableStorageLr;
import ru.ts.toykernel.trans.InitAbleMapTransformer;
import ru.ts.gisutils.proj.transform.IMapTransformer;
import ru.ts.gisutils.algs.common.MPoint;
import ru.ts.conv.rmp.MPImporter;
import org.opengis.referencing.operation.TransformException;

/**
 * Тестировщик конвертера из польского формата
 */
public class TestMPConverter
{


	public static void main(String[] args) throws Exception
	{


		final String wktsrc="GEOGCS[\"Географическая\", \n" +
				"  DATUM[\"Стандартный\", \n" +
				"    SPHEROID[\"Krassowsky_1942\", 6378245.0, 298.3]], \n" +
				"  PRIMEM[\"Greenwich\", 0.0], \n" +
				"  UNIT[\"degree\", 0.017453292519943295], \n" +
				"  AXIS[\"Geodetic latitude\", NORTH], \n" +
				"  AXIS[\"Geodetic longitude\", EAST]]";

		final String wktdst="PROJCS[\"Ламберта коническая конформная с двумя стандартными параллелями\", \n" +
				"  GEOGCS[\"Временная\", \n" +
				"    DATUM[\"Стандартный\", \n" +
				"      SPHEROID[\"Krassowsky_1942\", 6378245.0, 298.3]], \n" +
				"    PRIMEM[\"Greenwich\", 0.0], \n" +
				"    UNIT[\"degree\", 0.017453292519943295], \n" +
				"    AXIS[\"Geodetic latitude\", NORTH], \n" +
				"    AXIS[\"Geodetic longitude\", EAST]], \n" +
				"  PROJECTION[\"Lambert_Conformal_Conic_2SP\"], \n" +
				"  PARAMETER[\"central_meridian\", 90.0], \n" +
				"  PARAMETER[\"latitude_of_origin\", 55.0], \n" +
				"  PARAMETER[\"standard_parallel_1\", 1.0], \n" +
				"  PARAMETER[\"false_easting\", 0.0], \n" +
				"  PARAMETER[\"false_northing\", 0.0], \n" +
				"  PARAMETER[\"standard_parallel_2\", 56.0], \n" +
				"  UNIT[\"m\", 1.0], \n" +
				"  AXIS[\"x\", NORTH], \n" +
				"  AXIS[\"y\", EAST]]";

		FileInputStream is = new FileInputStream("C:\\MAPDIR\\MP_TOWNS\\mosobl_2010-05-03\\00400050.mp");
		String encoding = "WINDOWS-1251";

		IMpFilter filter = new IMpFilter()
		{
			int i = 0;

			public MpRecord accept(MpRecord _obj)  //конвертим трамвайные остановки
			{
				i++;

				if (!(_obj instanceof MpGeoObject))
					return null;

				MpGeoObject obj = (MpGeoObject)_obj;

				if (
						(obj.supertype.equals(MpGeoObject.KEYPNT) || obj.supertype.equals(MpGeoObject.KEYLINE))
								&&
								("0x2F08".equals(obj.getParamByName("Type")) || "0x010f14".equals(obj.getParamByName("Type")))
								&&
								"Москва".equals(obj.getParamByName("CityName"))

						)
				{ //Нас интересуют точки - остановки общественного транспорта
					List<String> startcomments = obj.getStartcomments();
					if (startcomments.size() > 0
							&&
							(
									startcomments.get(startcomments.size() - 1).contains("tram_stop")
											||
									startcomments.get(startcomments.size() - 1).contains("railway=tram")
							)
							) //точки с комментариями
						return obj;
				}
				if (i % 100 == 0)
					System.out.println("found " + i + " objects");
				return null;
			}
		};

		MPImporter imp = new MPImporter();
		imp.setTrans(new MPImporter.TransformPntsArr()
		{
			IMapTransformer trans=new InitAbleMapTransformer(null,null,wktsrc,wktdst);
			public MPoint[] transform(Data[] pnts) throws Exception
			{
				MPoint[] rv = new MPoint[pnts.length];

				for (int i = 0; i < pnts.length; i++)
				{
					Data pnt = pnts[i];
					MPoint mPoint = new MPoint(pnt.getLat(), pnt.getLon());
					try
					{
						rv[i]=new MPoint();
						trans.TransformDirect(mPoint, rv[i]);
					}
					catch (TransformException e)
					{
						throw new Exception(e);
					}
				}
				return rv;
			}
		});


		Iterator<MpRecord> itr = imp.getMPByFileName(new BufferedInputStream(is), encoding, filter);

		MemEditableStorageLr memstor = new MemEditableStorageLr("C:\\MAPDIR\\TEST_LR4\\", "mosobl-bus_stop-p");


		imp.addMp2Storage("Label",itr,memstor);

		memstor.commit();
		memstor.releaseStorage();

//		List<MpGeoObject> geoObjects = PolishReader.setByReader(new BufferedReader(new InputStreamReader(is, encoding)),
//				filter);
//		System.out.println("geoObjects = " + geoObjects.size());
	}
}
