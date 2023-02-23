package ru.ts.apps.dbapp.app;

import ru.ts.utils.data.InParams;

/**
 *
 */
public class InParamsApp extends InParams

{
	// имена используемых параметров (префиксы в командной строке)
	public static final String optarr[] = {"-wfl","-sfl","-gldr","-rmg",
			"-url","-proxy","-pport","-grp","-rot","-mobres","-rzd"};
	// значения параметров по умолчанию
	public static final String defarr[] =
	{
			"C:\\MAPDIR\\RZD_LAYERS_NEW_NEW\\msk_rzd_1.lr",
			"C:\\MAPDIR\\RZD_SCHEDULES",
			"D:\\Vlad\\JavaProj\\DEMO_GPS_MAPS_V3.0_RZD\\data\\googlepic\\",
			"C:\\MAPDIR\\Rasters\\layers.txt",
			"192.168.105.194/GPSServer/chk1",
			"vela.rg.ts",
			"81",
			"C:\\MAPDIR\\RZD_GRAPH\\msk_rzd_1.grp",
			"C:\\MAPDIR\\RZD_ROUTE",
			"D:\\Vlad\\JavaProj\\MobileService2\\Connector\\CheckGenerator3\\WaitPNT2\\translate",
			"false"
	};

	public static final int O_wfl=0;//Слой карты
	public static final int O_sfl=1;//Расписание
	public static final int O_gld=2;//Гугловая директория с изображениями
	public static final int O_rmg=3;//Файл манагера растра по умолчанию

	public static final int O_url =4;//Урл по умолчанию
	public static final int O_proxy =5;//Урл по умолчанию
	public static final int O_pport =6;//Урл по умолчанию
	public static final int O_grp =7;//Файл графа для загрузки
	public static final int O_rot =8;//Маршруты котороые есть для загрузки
	public static final int O_mobres =9;//Ресурсы мобильной карты
	public static final int O_rzd =10;//Ресурсы мобильной карты

	public InParamsApp()
	{
		super(optarr, defarr);
	}


}