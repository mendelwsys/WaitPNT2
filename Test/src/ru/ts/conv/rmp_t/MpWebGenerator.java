package ru.ts.conv.rmp_t;

import ru.ts.gisutils.algs.common.MPoint;
import ru.ts.gisutils.algs.common.MRect;
import ru.ts.toykernel.trans.InitAbleMapTransformer;
import ru.ts.toykernel.storages.mem.MemEditableStorageLr;
import ru.ts.toykernel.geom.IEditableGisObject;
import ru.ts.toykernel.xml.def.ParamDescriptor;
import ru.ts.toykernel.attrs.def.DefAttrImpl;
import ru.ts.toykernel.proj.xml.def.XMLProjBuilder;
import ru.ts.toykernel.app.xml.IXMLBuilderContext;
import ru.ts.toykernel.consts.KernelConst;
import ru.ts.toykernel.converters.ILinearConverter;
import ru.ts.toykernel.drawcomp.rules.def.CommonStyle;
import ru.ts.utils.data.Pair;
import ru.ts.xml.IXMLObjectDesc;
import ru.ts.factory.IParam;
import ru.ts.conv.rmp.SheemLoader;
import ru.ts.conv.rmp.MPImporter;
import ru.ts.conv.ConvGenerator;

import java.io.*;
import java.util.*;
import java.util.List;
import java.awt.*;

import su.org.susgsm.readers.Data;
import su.org.susgsm.readers.polish.MpGeoObject;
import su.org.susgsm.readers.polish.MpRecord;
import org.opengis.referencing.operation.TransformException;
import org.xml.sax.InputSource;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

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
public class MpWebGenerator implements ISheemProvider
{
	public static final String KEY_STORAGE = "KEY_STORAGE";
	public static final String KEY_OBJCNTR = "KEY_OBJCNTR";
	public static int DEF_PIXELSPERM=3000;//Пикселей на метр для преобазования уровней в масштаб
	protected List<String> headerssheem;
	protected Map<String, List<String>> tblsheem;
	protected List<String> headerscale;
	protected Map<String, List<String>> tblscale;
	protected int pixelsperm=DEF_PIXELSPERM;
	protected MpRecord mpHeader=null; //Заголовок
	protected Map<Integer,Pair<String,String>> level2Scale=new HashMap<Integer,Pair<String,String>>();
	protected Map<Pair<String,String>, List<String>> rgn2type2Props;
	protected Map<String,Integer> headers2ix;

	public String getEncoding()
	{
		return "WINDOWS-1251";
	}

	public Pair<String,String> getXMLPrefixSuffix()
	{
		return new Pair<String,String>("<?xml version=\"1.0\" encoding=\""+getEncoding()+"\" ?>\n" +
				"<project>\n","</project>\n");
	}

	protected IParam getParamByName(List<IParam> params,String paramName)
	{
		for (IParam param : params)
			if (param.getName()!=null && param.getName().equals(paramName))
				return param;
		return null;
	}

	protected void setRuleDesc(IXMLObjectDesc ruledesc,Pair<String, String> rgn2type,Integer level)
	{
		List<IParam> ruleparam = ruledesc.getParams();

		Map<String,IParam> name2param=new HashMap<String,IParam>();
		for (IParam param : ruleparam)
			name2param.put(param.getName(),param);

		Pair<String,String> pr=getHiLoRange(level);
		if (pr!=null)
		{
			name2param.put(CommonStyle.HI_RANGE, new DefAttrImpl(CommonStyle.HI_RANGE, pr.first));
			name2param.put(CommonStyle.LOW_RANGE, new DefAttrImpl(CommonStyle.LOW_RANGE, pr.second));
		}
		String val;

		if ((val=getScheemValByPair(rgn2type, SheemLoader.COLOR_LINE))!=null)
			name2param.put(CommonStyle.COLOR_LINE,new DefAttrImpl(CommonStyle.COLOR_LINE, val));

		if ((val=getScheemValByPair(rgn2type,SheemLoader.COLOR_FILL))!=null)
			name2param.put(CommonStyle.COLOR_FILL,new DefAttrImpl(CommonStyle.COLOR_FILL, val));

		if ((val=getScheemValByPair(rgn2type,SheemLoader.LINE_STYLE))!=null)
			name2param.put(CommonStyle.LINE_STYLE,new DefAttrImpl(CommonStyle.LINE_STYLE, val));

		if ((val=getScheemValByPair(rgn2type,SheemLoader.LINE_THICKNESS))!=null)
			name2param.put(CommonStyle.LINE_THICKNESS,new DefAttrImpl(CommonStyle.LINE_THICKNESS, val));

		String[] defOrderParam= new String[]{
				CommonStyle.HI_RANGE,
				CommonStyle.LOW_RANGE,
				CommonStyle.COLOR_LINE,
				CommonStyle.COLOR_FILL,
				CommonStyle.LINE_STYLE,
				CommonStyle.LINE_THICKNESS,
		};

		ruleparam.clear();
		for (String paramname : defOrderParam)
			ruleparam.add(name2param.get(paramname));
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
//				String scntlvl=mpHeader.getTemplateByName("Levels");
//				int cntlvl = Integer.parseInt(scntlvl);

				//Получим номер масштаба
				String slvln=mpHeader.getParamByName("Level"+level);
				int lvln = Integer.parseInt(slvln);

//				String slvprev=mpHeader.getTemplateByName("Level"+(level-1));
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
		List<String>[] lrs=new LinkedList[]{new LinkedList<String>(),new LinkedList<String>(),new LinkedList<String>(),new LinkedList<String>()};

		for (Integer level : set)
		{
			Map<Pair<String, String>, Properties> type2props = level2type2props.get(level);
			for (Pair<String, String> rgn2type : type2props.keySet())
			{
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

		for (List<String> lr : lrs)
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
		return getStorNameByPair(rgn2type2Props,headers2ix,level, rgn2type);
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

		FileInputStream is = new FileInputStream("D:\\MAPDIR\\MP_TOWNS\\Pskov.mp");

//		FileInputStream is = new FileInputStream("D:\\MAPDIR\\MP_TOWNS\\Rostov-na-Donu.mp"); //Файл преобразования
//		String pname ="rost"; // Имя проекта

//		FileInputStream is = new FileInputStream("D:\\MAPDIR\\MP_TOWNS\\krasnodar.mp"); //Файл преобразования
//		String pname ="krs"; // Имя проекта


//		FileInputStream is = new FileInputStream("D:\\MAPDIR\\POLY\\Moskva.mp"); //Файл преобразования
//		String pname ="msk"; // Имя проекта

		String transName="trans0";//Имя транслятора
		String metaname = "meta0"; //Имя метаданных
		String webtemlplugname = "mmap";

		String folderstorages = "D:\\MAPDIR\\TEST_PSK\\";//Куда сливать результат

		String  filescheem="D:\\MAPDIR\\MP\\ncommon.shm"; //Аттрибутивная схема (определяет как рисовать слои)
		String delscheem = ","; //Разделитель в схеме

		String  filescale="D:\\MAPDIR\\MP\\nscale.tbl"; //Определяет в каком масштабе рисовать слои
		String delscale = ","; //Разделитель в схеме

		Point viewSz = new Point(700, 700);//Размер экрана
		String fwebxml = "D:\\MAPDIR\\MP\\webpart.xml";//

//------------------  Загрузка описателей необходимых объектов для генерации проекта ---------------------------------//
		FileInputStream webxmltemplate = new FileInputStream(fwebxml); //Файл преобразования
		SAXParser parser= SAXParserFactory.newInstance().newSAXParser();
		Reader rd=new InputStreamReader(webxmltemplate,getEncoding());

		XMLProjBuilder builder = new XMLProjBuilder(true);//Строим только серверное приложение, остальное строительство отдаем на клиента
		parser.parse(new InputSource(rd), builder.getProjBuilderHandler(parser.getXMLReader()));
		IXMLBuilderContext bcontext = builder.getBuilderContext();
//-----------------------------  Секция вторичных параметров ---------------------------------------------------------//

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

		IXMLObjectDesc transdesc= createTemplateDescByName(bcontext,KernelConst.TRANSFORMER_TAGNAME,transName);
		final InitAbleMapTransformer trans= (InitAbleMapTransformer) bcontext.getBuilderByTagName(transdesc.getTagname()).initByDescriptor(transdesc, null);

		imp.setTrans(new MPImporter.TransformPntsArr()
		{
			//private InitAbleMapTransformer trans = new InitAbleMapTransformer(transdesc.getObjname(),transdesc, wktsrc, wktdst);
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

		Iterator<MpRecord> itr = imp.getMPByFileName(new BufferedInputStream(is), getEncoding(), null);
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
		IXMLObjectDesc mainstor= createTemplateDescByName(bcontext,KernelConst.STORAGE_TAGNAME,"MAIN_STORAGE");

		for (Integer level : level2type2props.keySet())
		{ // Здесь отображение уровень в масштаб в данной проекции :-) Загружается в виде файла
				Map<Pair<String, String>, Properties> type2props=level2type2props.get(level);
				for (Pair<String, String> rgn2type : type2props.keySet())
				{
					String storname = getStorNameByPair(level, rgn2type);

					//Генерация хранилища
					IXMLObjectDesc stordesc= createTemplateDescByName(bcontext,KernelConst.STORAGE_TAGNAME,"T_STOR");
					stordesc.setObjname(storname);
					stordesc.getParams().add(new DefAttrImpl(null, folderstorages));
					storages+=stordesc.getXMLDescriptor("\t");
					mainstor.getParams().add(new  DefAttrImpl(null,stordesc));

					//Генерация фильтра
					IXMLObjectDesc filterdesc= createTemplateDescByName(bcontext,KernelConst.FILTER_TAGNAME,"T_STORFILTER");
					filterdesc.setObjname("F_" + storname);
					filterdesc.getParams().add(new DefAttrImpl(null, storname));
					filters+=filterdesc.getXMLDescriptor("\t");

					//Генерация правил согласно схеме отображения

					IXMLObjectDesc ruledesc= createTemplateDescByName(bcontext,KernelConst.RULE_TAGNAME,"T_RULE");
					ruledesc.setObjname("R_" + storname);
					setRuleDesc(ruledesc, rgn2type, level);
					rules+= ruledesc.getXMLDescriptor("\t");


					//Генерация слоев
					IXMLObjectDesc layerdesc= createTemplateDescByName(bcontext,KernelConst.LAYER_TAGNAME,"T_LAYER");
					layerdesc.setObjname("L_" + storname);
					List<IParam> lparam = layerdesc.getParams();
					lparam.add(new  DefAttrImpl(null,ruledesc));
					lparam.add(new  DefAttrImpl(null,filterdesc));
					lparam.add(new  DefAttrImpl(null,mainstor));

					l_layers.add(layerdesc);
					layers+=layerdesc.getXMLDescriptor("\t");
				}
		}
		storages+=mainstor.getXMLDescriptor("\t");

		ConvGenerator convGenerator =new ConvGenerator();

		Pair<String, String> prefixsuffix = getXMLPrefixSuffix();
		StringBuffer sbuffer=new StringBuffer();
		sbuffer.append(prefixsuffix.first);



		IXMLObjectDesc metadesc= createTemplateDescByName(bcontext,KernelConst.META_TAGNAME,metaname);
		sbuffer.append("<").append(metadesc.getTagname()).append("s>\n");
		sbuffer.append(metadesc.getXMLDescriptor("\t"));
		sbuffer.append("</").append(metadesc.getTagname()).append("s>\n");

		//Получим из меты имя проекта
		String projName=(String)(getParamByName(metadesc.getParams(),"projname").getValue());
		String projFName=projName+".xml";
		String sprovName = projName+"_SPROV";
		String cprovName = projName+"_CPROV";


		sbuffer.append("<").append(transdesc.getTagname()).append("s>\n");
		sbuffer.append(transdesc.getXMLDescriptor("\t"));
		sbuffer.append("</").append(transdesc.getTagname()).append("s>\n");


//--------------------------------------- Инициализация конвертеров --------------------------------------------------//
		String rotname = "rot0";
		IXMLObjectDesc rotdesc= createTemplateDescByName(bcontext,KernelConst.CONVERTER_TAGNAME, rotname);
		ILinearConverter rotconv= (ILinearConverter) bcontext.getBuilderByTagName(rotdesc.getTagname()).initByDescriptor(rotdesc, null);
		MPoint[] scaleBpoint = convGenerator.calcScale_BPoint(mBB, rotconv, viewSz);

		IXMLObjectDesc scaledesc= createTemplateDescByName(bcontext,rotdesc.getTagname(), "scale1");
		scaledesc.getParams().add(new DefAttrImpl("initscale",scaleBpoint[0].x+" "+scaleBpoint[0].y));

		IXMLObjectDesc shiftdesc= createTemplateDescByName(bcontext,rotdesc.getTagname(), "shift2");
		shiftdesc.getParams().add(new DefAttrImpl("bindp", scaleBpoint[1].x+" "+scaleBpoint[1].y));


		String servConv = projName + "_SERVCONV";
		String projConv = projName + "_PROJCONV";

		sbuffer.append("<").append(rotdesc.getTagname()).append("s>\n");

		sbuffer.append(rotdesc.getXMLDescriptor("\t"));
		sbuffer.append(scaledesc.getXMLDescriptor("\t"));
		sbuffer.append(shiftdesc.getXMLDescriptor("\t"));

		IXMLObjectDesc servcnvdesc= createTemplateDescByName(bcontext,rotdesc.getTagname(), "T_SERVCONV");
		servcnvdesc.setObjname(servConv);
		sbuffer.append(servcnvdesc.getXMLDescriptor("\t"));

		IXMLObjectDesc projcnvdesc= createTemplateDescByName(bcontext,rotdesc.getTagname(), "T_PROJCONV");
		projcnvdesc.setObjname(projConv);
		sbuffer.append(projcnvdesc.getXMLDescriptor("\t"));

		sbuffer.append("</").append(rotdesc.getTagname()).append("s>\n");
//--------------------------------------------------------------------------------------------------------------------//

		//Хранилища
		sbuffer.append("<").append(KernelConst.STORAGE_TAGNAME).append("s>\n");
		sbuffer.append(storages);
		sbuffer.append("</").append(KernelConst.STORAGE_TAGNAME).append("s>\n");


		//Фильтр
		sbuffer.append("<").append(KernelConst.FILTER_TAGNAME).append("s>\n");
		sbuffer.append(filters);
		sbuffer.append("</").append(KernelConst.FILTER_TAGNAME).append("s>\n");
		//Правила
		sbuffer.append("<").append(KernelConst.RULE_TAGNAME).append("s>\n");
		sbuffer.append(rules);
		sbuffer.append("</").append(KernelConst.RULE_TAGNAME).append("s>\n");
		//Слои
		sbuffer.append("<").append(KernelConst.LAYER_TAGNAME).append("s>\n");
		sbuffer.append(layers);
		sbuffer.append("</").append(KernelConst.LAYER_TAGNAME).append("s>\n");


		{
			IXMLObjectDesc projcntx= createTemplateDescByName(bcontext,KernelConst.PROJCTXT_TAGNAME,"T_CTXT");
			sbuffer.append("<").append(projcntx.getTagname()).append("s>\n");

			projcntx.setObjname(projName);
			List<IParam> lprojcntx = projcntx.getParams();
			lprojcntx.add(new  DefAttrImpl(null,metadesc));
			lprojcntx.add(new  DefAttrImpl(null,mainstor));
			if (transdesc!=null)
				lprojcntx.add(new  DefAttrImpl(null,transdesc));
			l_layers=getOrdredLayers(l_layers,level2type2props);
			for (IXMLObjectDesc l_layer : l_layers)
				lprojcntx.add(new  DefAttrImpl(null,l_layer));
			sbuffer.append(projcntx.getXMLDescriptor("\t"));
			sbuffer.append("</").append(projcntx.getTagname()).append("s>\n");

			IXMLObjectDesc stroprovdesc= createTemplateDescByName(bcontext,KernelConst.STORPROVIDER_TAGNAME,"T_STORPOJPROV");
			sbuffer.append("<").append(stroprovdesc.getTagname()).append("s>\n");
			stroprovdesc.setObjname(sprovName);
			List<IParam> lpstorprov = stroprovdesc.getParams();
			lpstorprov.add(new  DefAttrImpl(null,projcnvdesc));
			lpstorprov.add(new  DefAttrImpl(null,projcntx));
			sbuffer.append(stroprovdesc.getXMLDescriptor("\t"));
			sbuffer.append("</").append(stroprovdesc.getTagname()).append("s>\n");

			IXMLObjectDesc convprovdesc= createTemplateDescByName(bcontext,KernelConst.CONVPROVIDER_TAGNAME,"T_CONVPROV");
			sbuffer.append("<").append(convprovdesc.getTagname()).append("s>\n");
			convprovdesc.setObjname(cprovName);
			convprovdesc.getParams().add(new  DefAttrImpl(null,servcnvdesc));
			sbuffer.append(convprovdesc.getXMLDescriptor("\t"));
			sbuffer.append("</").append(convprovdesc.getTagname()).append("s>\n");

			IXMLObjectDesc mmapdesc= createTemplateDescByName(bcontext,KernelConst.PLUGIN_TAGNAME,webtemlplugname);
			sbuffer.append("<").append(mmapdesc.getTagname()).append("s>\n");
			List<IParam> lpmmap = mmapdesc.getParams();
				lpmmap.add(new  DefAttrImpl("bindp",scaleBpoint[1].x+" "+scaleBpoint[1].y));
				lpmmap.add(new  DefAttrImpl(null,convprovdesc));
				lpmmap.add(new  DefAttrImpl(null,stroprovdesc));
			sbuffer.append(mmapdesc.getXMLDescriptor("\t"));
			sbuffer.append("</").append(mmapdesc.getTagname()).append("s>\n");
		}

//Закрываем файл
		sbuffer.append(prefixsuffix.second);
		PrintWriter pwr=new PrintWriter(folderstorages+"/"+projFName,getEncoding());
		pwr.println(sbuffer.toString());
		pwr.flush();
		pwr.close();

		System.out.println("totaltime:"+(System.currentTimeMillis()-starttime));
	}

	/**
	 * Найти описатель по имени в xml файле
	 * @param bcontext - контекст загруженного файла
	 * @param tagName - имя тага среди которого ищется
	 * @param name - имя которое ищется
	 * @return - описатель если найден, если не найден тогда
	 * @throws Exception - исключение если не найден дескриптор
	 */
	IXMLObjectDesc createTemplateDescByName(IXMLBuilderContext bcontext,String tagName,String name) throws Exception
	{
		List<IXMLObjectDesc> params = bcontext.getBuilderByTagName(tagName).getParamDescs();
		for (IXMLObjectDesc param : params)
			if (name==null || param.getObjname().equals(name))
				return new ParamDescriptor(param);
		if (name!=null && params.size()>0)
			throw  new Exception("Can't find descriptor for name "+ name+" tagname:"+tagName);
		else
		    throw  new Exception("No descriptors in tagname:"+tagName);
	}
}