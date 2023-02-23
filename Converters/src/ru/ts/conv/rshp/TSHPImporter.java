package ru.ts.conv.rshp;

import ru.ts.toykernel.storages.mem.MemEditableStorageLr;
import ru.ts.toykernel.trans.InitAbleMapTransformer;
import ru.ts.gisutils.proj.transform.IMapTransformer;
import ru.ts.gisutils.algs.common.MPoint;
import org.opengis.referencing.operation.TransformException;

/**
 * Тестирование импорта из SHP файлов
 */
public class TSHPImporter
{
	public static void main(String[] args) throws Exception
	{

//		String fin="G:\\GISs\\GEODATA\\SHP\\";
//
//		String[][] fls=new String[][]
//				{
//						{"adm_obl","name_adm1"},
//						{"popol_cen_final","name"},
//						{"popnt_cen_final","name"},
//						{"rrspnt_200","name"},
//						{"rrslin_200","name"},
//				};

		String fin="C:\\MAPDIR\\TEST_LR3\\mosobl\\";

		String[][] fls=new String[][]
				{
						{"mosobl-railway-l","NAME"}, //Первый параметр имя файла, второй параметр имя аттрибута в файле интерпретируемого как имя объекта
						{"mosobl-bus_stop-p","NAME"},
				};

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




		for (String[] fl : fls)
		{
			String fileimp = fl[0];
			MemEditableStorageLr memstor = new MemEditableStorageLr("C:\\MAPDIR\\TEST_LR3\\", fileimp);
			SHPImporter imp = new SHPImporter(fin+fileimp + ".shp", fin+fileimp + ".dbf",null,"UTF8");


			imp.setTrans(new SHPImporter.TransformPntsArr()
			{
			    IMapTransformer trans=new InitAbleMapTransformer(null,null,wktsrc,wktdst);
				public void transform(MPoint[] pnts) throws Exception
				{
					for (MPoint pnt : pnts)
					{
						MPoint mPoint = new MPoint(pnt.y, pnt.x);
						try
						{
							trans.TransformDirect(mPoint, pnt);
						}
						catch (TransformException e)
						{
							throw new Exception(e);
						}
					}
				}
			});

			imp.shp2Storage(memstor, fl[1],null);

			memstor.releaseStorage();
		}
	}
}
