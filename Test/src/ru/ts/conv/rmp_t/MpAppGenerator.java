package ru.ts.conv.rmp_t;

import ru.ts.gisutils.algs.common.MPoint;
import ru.ts.gisutils.algs.common.MRect;
import ru.ts.toykernel.trans.InitAbleMapTransformer;
import ru.ts.toykernel.storages.mem.MemEditableStorageLr;
import ru.ts.toykernel.storages.mem.NodeStorageImpl;
import ru.ts.toykernel.storages.providers.ProjProvider;
import ru.ts.toykernel.geom.IEditableGisObject;
import ru.ts.toykernel.xml.def.ParamDescriptor;
import ru.ts.toykernel.attrs.def.DefAttrImpl;
import ru.ts.toykernel.filters.stream.NodeFilter;
import ru.ts.toykernel.drawcomp.layers.def.DrawOnlyLayer;
import ru.ts.toykernel.pcntxt.xml.XMLProjContext;
import ru.ts.toykernel.pcntxt.MetaInfoBean;
import ru.ts.toykernel.converters.providers.ServerProvider;
import ru.ts.toykernel.gui.panels.ViewPicturePanel;
import ru.ts.toykernel.gui.apps.SFViewer;
import ru.ts.toykernel.loaders.UrlLoader;
import ru.ts.toykernel.plugins.defindrivers.DriverModule;
import ru.ts.utils.data.Pair;
import ru.ts.xml.IXMLObjectDesc;
import ru.ts.factory.IParam;
import ru.ts.conv.rmp.SheemLoader;
import ru.ts.conv.rmp.MPImporter;
import ru.ts.conv.IColorSheemLoader;
import ru.ts.conv.ConvGenerator;

import java.io.FileInputStream;
import java.io.BufferedInputStream;
import java.io.PrintWriter;
import java.util.*;
import java.util.List;
import java.awt.*;

import su.org.susgsm.readers.Data;
import su.org.susgsm.readers.polish.MpGeoObject;
import su.org.susgsm.readers.polish.MpRecord;
import org.opengis.referencing.operation.TransformException;

/**
 * Конвертер mp в набор стораджей.
 *
 * Параметры:
 * 1 данные проекции
 * 2 источник
 * 3 цветовая схема: соответсвие идентификатора слоя цветовому набору
 * 4 название выходного проекта
 *
 */
public class MpAppGenerator implements ISheemProvider
{
	public static final String KEY_STORAGE = "KEY_STORAGE";
	public static final String KEY_OBJCNTR = "KEY_OBJCNTR";
	public static int DEF_PIXELSPERM=3000;//Пикселей на метр для преобазования уровней в масштаб
	protected Generator storGenerator=new Generator("storage",MemEditableStorageLr.class);
	protected Generator filterGenerator=new Generator("filter",NodeFilter.class);
	protected RuleGenerator ruleGenerator=new RuleGenerator();
	protected Generator layerGenerator=new Generator("layer", DrawOnlyLayer.class);
	protected Generator projGenerator=new Generator("projcont",XMLProjContext.class);
	protected Generator storprovGenerator =new Generator("storprovider",ProjProvider.class);
	protected Generator convprovGenerator =new Generator("convprovider", ServerProvider.class);
	protected Generator viewctrlGenerator =new Generator("viewctrl", ViewPicturePanel.class);
	protected Generator classGenerator =new Generator("classloader", UrlLoader.class);
	protected Generator applicationGenerator =new Generator("application",SFViewer.class.getName());
	protected List<String> headerssheem;
	protected Map<String, List<String>> tblsheem;
	protected List<String> headerscale;
	protected Map<String, List<String>> tblscale;
	protected int pixelsperm=DEF_PIXELSPERM;
	protected MpRecord mpHeader=null; //Заголовок
	protected Map<Integer,Pair<String,String>> level2Scale=new HashMap<Integer,Pair<String,String>>();
	protected Map<Pair<String,String>, List<String>> rgn2type2Props;
	protected Map<String,Integer> headers2ix;

	public static void main(String[] args) throws Exception
	{
		new MpAppGenerator().translate(args);
	}

	public String getEncoding()
	{
		return "WINDOWS-1251";
	}

	public Pair<String,String> getXMLPrefixSuffix()
	{
		return new Pair<String,String>("<?xml version=\"1.0\" encoding=\""+getEncoding()+"\" ?>\n" +
				"<project>\n","</project>\n");
	}

	/**
	 *
	 * @param smeters - МЕТРОВ на местности  на один сантиметр в карте
	 * @return пиксели на метр на местности
	 */
	public String getScaleByMeters(String smeters)
	{
		if (smeters==null)
			return String.valueOf(-1);
		else
		{
			int meters=Integer.parseInt(smeters);
			//(meters*100) - метры на местности на метры на карте
			//А надо метры/пиксели
			return  String.valueOf(1.0*(meters*100)/pixelsperm);
		}
	}

	public Pair<String,String> getHiLoRange(Integer level)
	{
		try
		{
			Pair<String,String> pr=null;
			if ((pr=level2Scale.get(level))!=null)
				return pr;

			if (mpHeader!=null && tblscale!=null)
			{
//				String scntlvl=mpHeader.getParamByName("Levels");
//				int cntlvl = Integer.parseInt(scntlvl);

				//Получим номер масштаба
				String slvln=mpHeader.getParamByName("Level"+level);
				int lvln = Integer.parseInt(slvln);

//				String slvprev=mpHeader.getParamByName("Level"+(level-1));
//				if (slvprev==null)
				String slvprev=String.valueOf(lvln+1); //Крупный масштаб данного слой  (предедущий слой должен быть у нас на один больше чем текущий)

				String slvlnext=mpHeader.getParamByName("Level"+(level+1));//Мелкий масштаб данного слой
				slvlnext = String.valueOf(Integer.parseInt(slvlnext)+1); //Следующий слой должен быть номер

				//Запросим масштабы в метрах
				List<String> lvlcol = tblscale.get("LEVEL");
				List<String> mcol = tblscale.get("METER");
				Pair<String,String> big2small=new Pair<String,String>(null,null);

				for (int i = 0; i < lvlcol.size(); i++)
				{
					String lvl = lvlcol.get(i);
					if (lvl.equals(slvprev))
						big2small.first=mcol.get(i);
					else if (lvl.equals(slvlnext))
					{
						big2small.second=mcol.get(i);
						break;
					}
				}
				Pair retpr = new Pair<String,String>(getScaleByMeters(big2small.second), getScaleByMeters(big2small.first));

				level2Scale.put(level,retpr);
				return retpr;
			}
		}
		catch (NumberFormatException e)
		{
			e.printStackTrace();
		}
		return null;
	}

	public IXMLObjectDesc getDescByName(IXMLObjectDesc[] descs,String descName)
	{
		for (IXMLObjectDesc desc : descs)
			if(desc.getObjname().equals(descName))
				return desc;
		return null;
	}

	/**
	 * Упорядочить слои полигоны -> линии -> точки
	 * @param l_layers - набор слоев
	 * @param level2type2props - номер слоя ->(тип -> свойства)
	 * @return - набор слоев но упорядоченный
	 */
	public List<IXMLObjectDesc> getOrdredLayers(List<IXMLObjectDesc> l_layers,Map<Integer, Map<Pair<String, String>, Properties>> level2type2props)
	{

		List<IXMLObjectDesc> r_layers = new LinkedList<IXMLObjectDesc>();

		Map<String,IXMLObjectDesc> name2Desc=new HashMap<String,IXMLObjectDesc>();

		for (IXMLObjectDesc l_layer : l_layers)
			name2Desc.put(l_layer.getObjname(),l_layer);


		Set<Integer> set = new TreeSet<Integer>(level2type2props.keySet());
		java.util.List<String>[] lrs=new LinkedList[]{new LinkedList<String>(),new LinkedList<String>(),new LinkedList<String>(),new LinkedList<String>()};

		for (Integer level : set)
		{
			Map<Pair<String, String>, Properties> type2props = level2type2props.get(level);
			for (Pair<String, String> rgn2type : type2props.keySet())
			{
				//String objstr = "\t\t\t<layer>L_" + getStorNameByPair(level, rgn2type) + "</layer>\n";
				String objstr ="L_"+getStorNameByPair(level, rgn2type);

				if (rgn2type.first.equals(MpGeoObject.KEYGON))
					lrs[0].add(objstr);
				else if(rgn2type.first.equals(MpGeoObject.KEYLINE))
					lrs[1].add(objstr);
				else if(rgn2type.first.equals(MpGeoObject.KEYPNT))
					lrs[2].add(objstr);
				else
					lrs[3].add(objstr);
			}
		}

		for (java.util.List<String> lr : lrs)
			for (String lrstr : lr)
				r_layers.add(name2Desc.get(lrstr));

		return r_layers;
	}

	public Map<Pair<String,String>, List<String>> formTable(Map<String,Integer> headers2ix,Map<String, List<String>> tsheet)
	{
		Map<Pair<String,String>, List<String>> rgn2type2Props = new HashMap<Pair<String,String>, List<String>>();

		headers2ix.clear();
		for (String header : tsheet.keySet())
		{
			headers2ix.put(header,headers2ix.size());
			List<String> sl = tsheet.get(header);
			if (!(sl instanceof ArrayList))
				tsheet.put(header,new ArrayList<String>(sl));
		}

		List<String> rgns=tsheet.get(SheemLoader.RGN);

		for (int i = 0; i < rgns.size(); i++)
		{
			String rgn =  rgns.get(i);
			String ntype = tsheet.get(SheemLoader.NTYPE).get(i);
			Pair<String,String> key=new Pair<String,String>(rgn,ntype);
			List<String> ll= new LinkedList<String>();

			for (String header : tsheet.keySet())
				ll.add(tsheet.get(header).get(i));
			rgn2type2Props.put(key,new ArrayList<String>(ll));
		}

		return rgn2type2Props;

	}

	public Map<String, Integer> getHeaders2ix()
	{
		getRgn2type2Props();
		return headers2ix;
	}

	public String getStorNameByPair(Integer level,Pair<String,String> rgn2type)
	{
		getRgn2type2Props();
		return getStorNameByPair(rgn2type2Props,headers2ix,level,rgn2type);
	}

	public String getScheemValByPair(Pair<String,String> rgn2type,String propName)
	{
		getRgn2type2Props();
		return getScheemValByPair(rgn2type2Props,headers2ix, rgn2type,propName);
	}

	public Map<Pair<String,String>, List<String>> getRgn2type2Props()
	{
		if (rgn2type2Props == null)
			rgn2type2Props =formTable(headers2ix=new HashMap<String,Integer>(),tblsheem);
		return rgn2type2Props;
	}

	public String getScheemValByPair(Map<Pair<String,String>, List<String>> rgn2type2Props,Map<String,Integer> headers2ix,Pair<String,String> rgn2type,String propName)
	{
		List<String> propList=rgn2type2Props.get(rgn2type);
		if (propList==null)
			return null;
		Integer ix = headers2ix.get(propName);
		if (ix==null)
			return null;
		return propList.get(ix);
	}

	/**
	 *
	 * @param rgn2type2Props - ключ -> список свойств ключа
	 * @param headers2ix - имя столбца -> порядкаовый номер в списке свойств первого аргумента
	 * @param rgn2type - ключ
	 * @return единое имя слоя для формирования имен правил и фильтров
	 */
	public String getStorNameByPair(Map<Pair<String,String>, List<String>> rgn2type2Props,Map<String,Integer> headers2ix,Integer level,Pair<String,String> rgn2type)
	{
		String storName=getScheemValByPair(rgn2type2Props,headers2ix,rgn2type,SheemLoader.LRNAME);
		if (storName==null)
			return String.valueOf(level)+"_"+rgn2type.toString();
		return String.valueOf(level)+"_"+storName+"_"+rgn2type;
	}

	public void translate(String[] args) throws Exception
	{

		long starttime= System.currentTimeMillis();

		String transName="trans0";//Имя транслятора

		final String wktsrc = "GEOGCS[\"Географическая\", \n" +
				"  DATUM[\"Стандартный\", \n" +
				"    SPHEROID[\"Krassowsky_1942\", 6378245.0, 298.3]], \n" +
				"  PRIMEM[\"Greenwich\", 0.0], \n" +
				"  UNIT[\"degree\", 0.017453292519943295], \n" +
				"  AXIS[\"Geodetic latitude\", NORTH], \n" +
				"  AXIS[\"Geodetic longitude\", EAST]]"; //Откуда осуществить преобразование

		final String wktdst = "PROJCS[\"Ламберта коническая конформная с двумя стандартными параллелями\", \n" +
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
				"  AXIS[\"y\", EAST]]"; //Куда преобразовать


		String encoding = "WINDOWS-1251";

		FileInputStream is = new FileInputStream("D:\\MAPDIR\\MP_TOWNS\\Pskov.mp");
		String projName ="psk"; // Имя проекта

//		FileInputStream is = new FileInputStream("D:\\MAPDIR\\MP_TOWNS\\Rostov-na-Donu.mp"); //Файл преобразования
//		String pname ="rost"; // Имя проекта

//		FileInputStream is = new FileInputStream("D:\\MAPDIR\\MP_TOWNS\\krasnodar.mp"); //Файл преобразования
//		String pname ="krs"; // Имя проекта


//		FileInputStream is = new FileInputStream("D:\\MAPDIR\\POLY\\Moskva.mp"); //Файл преобразования
//		String pname ="msk"; // Имя проекта

		String folderstorages = "D:\\MAPDIR\\TEST_PSK\\";//Куда сливать результат

		String  filescheem="D:\\MAPDIR\\MP\\ncommon.shm"; //Аттрибутивная схема (определяет как рисовать слои)
		String delscheem = ","; //Разделитель в схеме


		String  filescale="D:\\MAPDIR\\MP\\nscale.tbl"; //Аттрибутивная схема (определяет как рисовать слои)
		String delscale = ","; //Разделитель в схеме


		String metaname = "meta0"; //Имя метаданных
		Point viewSz = new Point(1024, 768);//Размер экрана

		String unitsname = "METERS";//!!!Здесь должны переметры объеденены!!!, точнее по крайней мере установлены по умолчанию из преобразователя

//---------------------------------  Секция вторичных параметров -----------------------------------------------------//

		String projFName=projName+".xml";
		String provName = "PROV_" + projName;

		//Загрузка цветовой схемы
		headerssheem = new LinkedList<String>();
		tblsheem = SheemLoader.loadScheem(delscheem, headerssheem, filescheem);

		//Загрузка таблицы масштабов
		headerscale = new LinkedList<String>();
		tblscale = SheemLoader.loadScheem(delscale, headerscale, filescale);


		List<String> typelist = tblsheem.get("NTYPE");
		for (int i = 0; i < typelist.size(); i++)
		{
			int type= Integer.parseInt(typelist.get(i).substring(2),16);
			typelist.set(i,"0x"+Integer.toHexString(type));
		}

		//TODO На базе этой таблицы генерируем таблицу отображения номер типа объекта -> имя таблицы
		//Т.о. получим набор слоев с именами в виде ObjectName_TNumber
		MPImporter imp = new MPImporter();

		List transparam = Arrays.asList (
					new  DefAttrImpl("wktsrc",wktsrc),
					new  DefAttrImpl("wktdst",wktdst)
		);
		final IXMLObjectDesc transdesc=new ParamDescriptor("transformer",transName,InitAbleMapTransformer.class.getName(),null,
				transparam,-1);

		imp.setTrans(new MPImporter.TransformPntsArr()
		{
			private InitAbleMapTransformer trans = new InitAbleMapTransformer(transdesc.getObjname(),transdesc, wktsrc, wktdst);
			public MPoint[] transform(Data[] pnts) throws Exception
			{
				MPoint[] rv = new MPoint[pnts.length];

				for (int i = 0; i < pnts.length; i++)
				{
					Data pnt = pnts[i];
					MPoint mPoint = new MPoint(pnt.getLat(), pnt.getLon());
					try
					{
						rv[i] = new MPoint();
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

		//Уровень данных ->(тип -> свойства), где тип супертип (RGN), номер типа объектов
		Map<Integer, Map<Pair<String, String>, Properties>> level2type2props = new HashMap<Integer, Map<Pair<String, String>, Properties>>();

		int cntv=0;
		MRect mBB = null;//MBB проекта

		Map<String,MpRecord> startTag2Rec= new HashMap<String,MpRecord>(); //мно-во записей в исходном файле



		Iterator<MpRecord> itr = imp.getMPByFileName(new BufferedInputStream(is), encoding, null);
		while (itr.hasNext())
		{

			MpRecord _mpGeoObject = itr.next();

			if (_mpGeoObject.getStartRecLine().equals(MpRecord.HEADERSTART))
			{
				if (mpHeader!=null)
					System.out.println("!!!Rewrite Header!!!");
				mpHeader=_mpGeoObject;

//				Pair<String,String> pr=getHiLoRange(0);
//				pr=getHiLoRange(1);
//				pr=getHiLoRange(2);

				continue;
			}

			if (!(_mpGeoObject instanceof MpGeoObject))
			{
				startTag2Rec.put(_mpGeoObject.getStartRecLine(),_mpGeoObject);
				continue;
			}


			MpGeoObject mpGeoObject=(MpGeoObject)_mpGeoObject;
			Map<Integer, List<Data[]>> datas = mpGeoObject.getDatas();

			for (Integer level : datas.keySet())
			{
				Map<Pair<String, String>, Properties> type2props;
				Properties prop;
				if ((type2props = level2type2props.get(level)) == null)
					level2type2props.put(level, type2props = new HashMap<Pair<String, String>, Properties>());


				int type= Integer.parseInt(mpGeoObject.getParamByName("Type").substring(2),16);
				Pair<String, String> rgn2type = new Pair<String, String>(mpGeoObject.getSupertype(),"0x"+Integer.toHexString(type));

				if ((prop = type2props.get(rgn2type)) == null)
					type2props.put(rgn2type, prop = new Properties());

				MemEditableStorageLr memstor; //Хранилище для
				if ((memstor = (MemEditableStorageLr) prop.get(KEY_STORAGE)) == null)
					prop.put(KEY_STORAGE, memstor = new MemEditableStorageLr(folderstorages,getStorNameByPair(level, rgn2type)));

				Integer cntobj;
				if ((cntobj = (Integer) prop.get(KEY_OBJCNTR)) == null)
					prop.put(KEY_OBJCNTR, cntobj=0);
				else
					prop.put(KEY_OBJCNTR, cntobj=(cntobj + 1));

				try
				{
					IEditableGisObject obj = imp.addMp2Storage("Label", new MpGeoObject(mpGeoObject, level), memstor);
					obj.rebuildGisValume();
					mBB=obj.getMBB(mBB);
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}

				if (cntobj % 100000 == 0)
					memstor.commit();
			}
			cntv++;
			if (cntv%30000==0)
				System.out.println("Translated cntv = " + cntv);
		}
		System.out.println("Total objects = " + cntv);

		//Commit all storages
		for (Map<Pair<String, String>, Properties> type2props : level2type2props.values())
			for (Properties prop : type2props.values())
			{
				MemEditableStorageLr memstor; //Хранилище для
				if ((memstor = (MemEditableStorageLr) prop.get(KEY_STORAGE)) != null)
					memstor.commit();
			}

//Генерация проекта
		String storages="";
		String filters="";
		String rules="";

		List<IXMLObjectDesc> l_layers=new LinkedList<IXMLObjectDesc>();
		String layers="";

		//Генерация хранилищ
		//Описатель главного хранилища
		ParamDescriptor mainstor=new ParamDescriptor(storGenerator.getTag(),"MAIN_STORAGE", NodeStorageImpl.class.getName(),null,new LinkedList(),-1);

		for (Integer level : level2type2props.keySet())
		{ // Здесь отображение уровень в масштаб в данной проекции :-) Загружается в виде файла
				Map<Pair<String, String>, Properties> type2props=level2type2props.get(level);
				for (Pair<String, String> rgn2type : type2props.keySet())
				{
					String storname = getStorNameByPair(level, rgn2type);

					//Генерация хранилища
					List storparam = Arrays.asList(new DefAttrImpl(null, folderstorages));
					ParamDescriptor stordesc = new ParamDescriptor(storGenerator.getTag(), storname,storGenerator.getClassName(), null,
								storparam, -1);
					storages+=stordesc.getXMLDescriptor("\t");
					mainstor.getParams().add(new  DefAttrImpl(null,stordesc));

					//Генерация фильтра
					List filterparam = Arrays.asList(new DefAttrImpl(null, storname));
					ParamDescriptor filterdesc = new ParamDescriptor(filterGenerator.getTag(), "F_" + getStorNameByPair(level, rgn2type),
							filterGenerator.getClassName(), null, filterparam, -1);
					filters+=filterdesc.getXMLDescriptor("\t");

					//Генерация правил согласно схеме отображения
					IXMLObjectDesc ruledesc = ruleGenerator.getRuleDesc(this, rgn2type, level);
					rules+= ruledesc.getXMLDescriptor("\t");

					//Генерация слоев
					List layerparam = Arrays.asList(
								new  DefAttrImpl("visible","true"),
								new  DefAttrImpl(null,ruledesc),
								new  DefAttrImpl(null,filterdesc),
								new  DefAttrImpl(null,mainstor)
							);
					ParamDescriptor layerdesc = new ParamDescriptor(layerGenerator.getTag(), "L_" + storname,
							layerGenerator.getClassName(), null, layerparam, -1);
					l_layers.add(layerdesc);
					layers+=layerdesc.getXMLDescriptor("\t");
				}
		}
		storages+=mainstor.getXMLDescriptor("\t");

		ConvGenerator convGenerator =new ConvGenerator();

		Pair<String, String> prefixsuffix = getXMLPrefixSuffix();
		StringBuffer sbuffer=new StringBuffer();
		sbuffer.append(prefixsuffix.first);

		List metaparam = Arrays.asList
				(
					new  DefAttrImpl("FORMAT VERSION","ToyGIS 1.4"),
					new  DefAttrImpl("major","1"),
					new  DefAttrImpl("minor","4"),
					new  DefAttrImpl("projname",projName),
					new  DefAttrImpl("boxColor","ff00ff00"),
					new  DefAttrImpl("backColor","ffffffff"),
					new  DefAttrImpl("mapver","MapVer 1.0"),
					new  DefAttrImpl("units",unitsname)
				);
		IXMLObjectDesc metadesc=new ParamDescriptor("metainfo",metaname,MetaInfoBean.class.getName(),null,
				metaparam,-1);

		sbuffer.append("<").append(metadesc.getTagname()).append("s>\n");
		sbuffer.append(metadesc.getXMLDescriptor("\t"));
		sbuffer.append("</").append(metadesc.getTagname()).append("s>\n");

		sbuffer.append("<").append(transdesc.getTagname()).append("s>\n");
		sbuffer.append(transdesc.getXMLDescriptor("\t"));
		sbuffer.append("</").append(transdesc.getTagname()).append("s>\n");

		double[] rotmatrix = {0.0, 1.0, -1.0, 0.0};


		MPoint[] scaleBpoint = convGenerator.calcScale_BPoint(mBB, rotmatrix, viewSz);


		sbuffer.append("<").append(convGenerator.getConvertersTag()).append("s>\n");
		String projConv = projName + "_PROJCONV";
		String servConv = projName + "_SERVCONV";

		//double scale=Math.min(scaleBpoint[0].x,scaleBpoint[0].y);
		//MPoint sclpnt=new MPoint(scale,scale);
		IXMLObjectDesc[] converters = convGenerator.getServProjConverters(projConv, servConv, rotmatrix, scaleBpoint[0], scaleBpoint[1]);
		for (IXMLObjectDesc projConverter : converters)
			sbuffer.append(projConverter.getXMLDescriptor("\t"));
		sbuffer.append("</").append(convGenerator.getConvertersTag()).append("s>\n");

		//Хранилища
		sbuffer.append("<").append(storGenerator.getTag()).append("s>\n");
		sbuffer.append(storages);
		sbuffer.append("</").append(storGenerator.getTag()).append("s>\n");
		//Фильтр
		sbuffer.append("<").append(filterGenerator.getTag()).append("s>\n");
		sbuffer.append(filters);
		sbuffer.append("</").append(filterGenerator.getTag()).append("s>\n");
		//Правила
		sbuffer.append("<").append(ruleGenerator.getTag()).append("s>\n");
		sbuffer.append(rules);
		sbuffer.append("</").append(ruleGenerator.getTag()).append("s>\n");
		//Слои
		sbuffer.append("<").append(layerGenerator.getTag()).append("s>\n");
		sbuffer.append(layers);
		sbuffer.append("</").append(layerGenerator.getTag()).append("s>\n");

		//Контекс проекта
		List<IParam> projparam = new LinkedList(Arrays.asList(
					new  DefAttrImpl(null,metadesc),
					new  DefAttrImpl(null,mainstor)
				));
		if (transdesc!=null)
			projparam.add(new  DefAttrImpl(null,transdesc));

		l_layers=getOrdredLayers(l_layers,level2type2props);
		for (IXMLObjectDesc l_layer : l_layers)
			projparam.add(new  DefAttrImpl(null,l_layer));
		IXMLObjectDesc projdesc = new ParamDescriptor(projGenerator.getTag(), projName,
				projGenerator.getClassName(), null, projparam, -1);


		sbuffer.append("<").append(projGenerator.getTag()).append("s>\n");
		sbuffer.append(projdesc.getXMLDescriptor("\t"));
		sbuffer.append("</").append(projGenerator.getTag()).append("s>\n");


		List storprovparam = Arrays.asList (
					new  DefAttrImpl(null,getDescByName(converters,projConv)),
					new  DefAttrImpl(null,projdesc)
				);
		IXMLObjectDesc stroprovdesc=new ParamDescriptor(storprovGenerator.getTag(),provName, storprovGenerator.getClassName(),null,storprovparam,-1);

		sbuffer.append("<").append(storprovGenerator.getTag()).append("s>\n");
		sbuffer.append(stroprovdesc.getXMLDescriptor("\t"));
		sbuffer.append("</").append(storprovGenerator.getTag()).append("s>\n");


		List convprovparam = Arrays.asList (
					new  DefAttrImpl(null,getDescByName(converters,servConv))
				);
		IXMLObjectDesc convprovdesc=new ParamDescriptor(convprovGenerator.getTag(),provName, convprovGenerator.getClassName(),null,convprovparam,-1);

		sbuffer.append("<").append(convprovGenerator.getTag()).append("s>\n");
		sbuffer.append(convprovdesc.getXMLDescriptor("\t"));
		sbuffer.append("</").append(convprovGenerator.getTag()).append("s>\n");

		List viewctrlparam = Arrays.asList
			(
					new  DefAttrImpl("InitSz",viewSz.x+" "+viewSz.y),
					new  DefAttrImpl(null,projdesc),
					new  DefAttrImpl(null,getDescByName(converters,projConv))
			);
		IXMLObjectDesc viewctrlprovdesc=new ParamDescriptor(viewctrlGenerator.getTag(), "View0", viewctrlGenerator.getClassName(),null,viewctrlparam,-1);

		List loaderparam = Arrays.asList
		(
				new  DefAttrImpl(null,"file:///D:/Vlad/JavaProj/ToyGIS/GuiPlugIns/PlugIns.jar")
		);
		IXMLObjectDesc classprovdesc=new ParamDescriptor(classGenerator.getTag(), "loader1", classGenerator.getClassName(),null,loaderparam,-1);

		List<IParam> appparam= new LinkedList<IParam>();
		List pluginparam = Arrays.asList
		(
				new  DefAttrImpl(null,viewctrlprovdesc)
		);

		String plugTagName = "plugin";
		appparam.add(new  DefAttrImpl(null,new ParamDescriptor(plugTagName, "drivermod", DriverModule.class.getName(),null,pluginparam,-1)));
		appparam.add(new  DefAttrImpl(null,new ParamDescriptor(plugTagName, "gissearch", "ru.ts.toykernel.plugins.gissearch.GisSearch",classprovdesc,pluginparam,-1)));
		appparam.add(new  DefAttrImpl(null,new ParamDescriptor(plugTagName, "drawattreditor", "ru.ts.toykernel.plugins.styles.DefDrawAttrEditor",classprovdesc,pluginparam,-1)));
		appparam.add(new  DefAttrImpl(null,new ParamDescriptor(plugTagName, "curveviewer", "ru.ts.toykernel.plugins.cvviewer.CurveViewer",classprovdesc,pluginparam,-1)));
		appparam.add(new  DefAttrImpl(null,viewctrlprovdesc));

		IXMLObjectDesc applicationDesc=new ParamDescriptor(applicationGenerator.getTag(),"App0",applicationGenerator.getClassName() ,null,appparam,-1);


		sbuffer.append("<").append(viewctrlGenerator.getTag()).append("s>\n");
		sbuffer.append(viewctrlprovdesc.getXMLDescriptor("\t"));
		sbuffer.append("</").append(viewctrlGenerator.getTag()).append("s>\n");

		sbuffer.append("<").append(classGenerator.getTag()).append("s>\n");
		sbuffer.append(classprovdesc.getXMLDescriptor("\t"));
		sbuffer.append("</").append(classGenerator.getTag()).append("s>\n");

		sbuffer.append("<").append(plugTagName).append("s>\n");
		for (IParam iParam : appparam)
			sbuffer.append(((IXMLObjectDesc)iParam.getValue()).getXMLDescriptor("\t"));
		sbuffer.append("</").append(plugTagName).append("s>\n");

		sbuffer.append("<").append(applicationGenerator.getTag()).append("s>\n");
		sbuffer.append(applicationDesc.getXMLDescriptor("\t"));
		sbuffer.append("</").append(applicationGenerator.getTag()).append("s>\n");

//Закрываем файл
		sbuffer.append(prefixsuffix.second);
		PrintWriter pwr=new PrintWriter(folderstorages+"/"+projFName,getEncoding());
		pwr.println(sbuffer.toString());
		pwr.flush();
		pwr.close();

		System.out.println("totaltime:"+(System.currentTimeMillis()-starttime));

	}

}
