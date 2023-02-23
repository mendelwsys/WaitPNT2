package ru.ts.conv.rmp;

import ru.ts.gisutils.algs.common.MPoint;
import ru.ts.gisutils.algs.common.MRect;
import ru.ts.toykernel.trans.InitAbleMapTransformer;
import ru.ts.toykernel.storages.mem.MemEditableStorageLr;
import ru.ts.toykernel.geom.IEditableGisObject;
import ru.ts.toykernel.xml.def.ParamDescriptor;
import ru.ts.toykernel.attrs.def.DefAttrImpl;
import ru.ts.toykernel.app.xml.IXMLBuilderContext;
import ru.ts.toykernel.consts.KernelConst;
import ru.ts.toykernel.converters.ILinearConverter;
import ru.ts.toykernel.factory.BaseInitAble;
import ru.ts.toykernel.drawcomp.rules.def.CommonStyle;
import ru.ts.toykernel.drawcomp.rules.def.CnStyleRuleImpl;
import ru.ts.utils.data.Pair;
import ru.ts.utils.data.InParams;
import ru.ts.xml.IXMLObjectDesc;
import ru.ts.factory.IParam;
import ru.ts.conv.ICompiler;
import ru.ts.conv.ConvGenerator;

import java.io.*;
import java.util.*;
import java.util.List;
import java.awt.*;

import su.org.susgsm.readers.Data;
import su.org.susgsm.readers.polish.MpGeoObject;
import su.org.susgsm.readers.polish.MpRecord;
import org.opengis.referencing.operation.TransformException;

/**
 * Конвертер mp в набор стораджей.
 * <p/>
 * Параметры:
 * 1 данные проекции
 * 2 источник
 * 3 цветовая схема: соответсвие идентификатора слоя цветовому набору
 * 4 название выходного проекта
 * <p/>
 * ru.ts.conv.rmp.MpAppGenerator2
 */
public class MpAppGenerator2 extends BaseInitAble
		implements ICompiler

{
	public static final String KEY_STORAGE = "KEY_STORAGE";
	public static final String KEY_OBJCNTR = "KEY_OBJCNTR";
	public static int DEF_PPERM = 3000;//Пикселей на метр для преобазования уровней в масштаб
	protected String pname;
	protected Point viewSz;
	protected MPoint[] scaleBpoint;
	protected IXMLBuilderContext bcontext;
	protected IXMLObjectDesc transdesc;//Проектор координат (трансформер)
	protected IXMLObjectDesc mainstor; //Главное хранилище
	protected IXMLObjectDesc metadesc;//Дескриптор метаинформации
	protected IXMLObjectDesc servcnvdesc;
	protected IXMLObjectDesc projconvdesc;
	protected IXMLObjectDesc projcntx;
	protected String encoding;
	protected String textrule="T_TEXTRULE";
	protected String drawrule="T_DRAWRULE";
	protected List<String> headerssheem;
	protected Map<String, List<String>> tblsheem;
	protected List<String> headerscale;
	protected Map<String, List<String>> tblscale;
	protected int pperm = DEF_PPERM;
	protected MpRecord mpHeader = null; //Заголовок
	protected Map<Integer, Pair<String, String>> level2Scale = new HashMap<Integer, Pair<String, String>>();
	protected Map<Pair<String, String>, List<String>> rgn2type2Props;//Схема преобразования [POLYLINE]_type -> мно-во свойств
	protected Map<String, Integer> headers2ix;//Имя заголовка в его порядковый номер
	protected String mp = "D:\\MAPDIR\\MP_TOWNS\\Pskov.mp";
	protected String cshm = "D:\\MAPDIR\\MP\\ncommon.shm"; //Аттрибутивная схема (определяет как рисовать слои)
	protected String dlshm = ","; //Разделитель в схеме
	protected String sсtbl = "D:\\MAPDIR\\MP\\nscale.tbl"; //Определяет в каком масштабе рисовать слои
	protected String dlsc = ","; //Разделитель в таблице масштабов
	protected String pdir = "D:\\MAPDIR\\TEST_PSK\\";//Куда сливать результат
	protected String transname = "trans0";//Имя транслятора
	protected String metaname = "meta0"; //Имя метаданных
	protected InParams inparams;
	protected List<IParam> addparams = new LinkedList<IParam>();
	private String pimage;
	private String asname;// = "Label";

	public String getEncoding()
	{
		return encoding;
	}

	public Pair<String, String> getXMLPrefixSuffix()
	{
		return new Pair<String, String>("<?xml version=\"1.0\" encoding=\"" + getEncoding() + "\" ?>\n" +
				"<project>\n", "</project>\n");
	}

	/**
	 * Упорядочить слои полигоны -> линии -> точки
	 *
	 * @param l_layers		 - набор слоев
	 * @param level2type2props - номер слоя ->(тип -> свойства)
	 * @return - набор слоев но упорядоченный
	 */
	public List<IXMLObjectDesc> getOrdredLayers(List<IXMLObjectDesc> l_layers, Map<Integer, Map<Pair<String, String>, Properties>> level2type2props)
	{
		List<IXMLObjectDesc> r_layers = new LinkedList<IXMLObjectDesc>();

		Map<String, IXMLObjectDesc> name2Desc = new HashMap<String, IXMLObjectDesc>();

		for (IXMLObjectDesc l_layer : l_layers)
			name2Desc.put(l_layer.getObjname(), l_layer);


		Set<Integer> set = new TreeSet<Integer>(level2type2props.keySet());
		List<String>[] lrs = new LinkedList[]{new LinkedList<String>(), new LinkedList<String>(), new LinkedList<String>(), new LinkedList<String>()};


		List<String> rgns=tblsheem.get("RGN");
		List<String> types=tblsheem.get("NTYPE");

		Map<Integer,Integer> types2order= new HashMap<Integer,Integer>();

		for (int i = 0; i < rgns.size(); i++)
		{
			String rgn = rgns.get(i);
			if (rgn.equalsIgnoreCase("[POLYGON]"))
				types2order.put(Integer.parseInt(types.get(i).substring(2),16),i);
		}

		int addorderindex=rgns.size()+1;//Пордковый индекс для тех полгинов которые не вошли в схему отображения

		Map<Integer,Map<Integer,String>> level2order2layer=new HashMap<Integer,Map<Integer,String>>();
		for (Integer level : set)
		{
			Map<Pair<String, String>, Properties> type2props = level2type2props.get(level);
			for (Pair<String, String> rgn2type : type2props.keySet())
			{
				String objstr = "L_" + getStorNameByPair(level, rgn2type);
				Integer ntype=Integer.parseInt(rgn2type.second.substring(2),16);

				if (rgn2type.first.equals(MpGeoObject.KEYGON))
				{
					Map<Integer,String> lobjstr=level2order2layer.get(level);
					if (lobjstr==null)
						level2order2layer.put(level,lobjstr = new HashMap<Integer,String>());

					Integer order=types2order.get(ntype);
					if (order==null)
						order=(++addorderindex);
					lobjstr.put(order,objstr);
//					lrs[0].add(objstr);
				}
				else if (rgn2type.first.equals(MpGeoObject.KEYLINE))
					lrs[1].add(objstr);
				else if (rgn2type.first.equals(MpGeoObject.KEYPNT))
					lrs[2].add(objstr);
				else
					lrs[3].add(objstr);
			}
		}


		Set<Integer> levelset=new TreeSet<Integer>(level2order2layer.keySet());
		for (Integer level : levelset)
		{
			Map<Integer, String> order2layer = level2order2layer.get(level);
			Set<Integer> orders=new TreeSet<Integer>(order2layer.keySet());
			for (Integer order : orders)
				lrs[0].add(order2layer.get(order));
		}


		for (List<String> lr : lrs)
			for (String lrstr : lr)
				r_layers.add(name2Desc.get(lrstr));

		return r_layers;
	}

	public Map<Pair<String, String>, List<String>> formTable(Map<String, Integer> headers2ix, Map<String, List<String>> tsheet)
	{
		Map<Pair<String, String>, List<String>> rgn2type2Props = new HashMap<Pair<String, String>, List<String>>();

		headers2ix.clear();
		for (String header : tsheet.keySet())
		{
			headers2ix.put(header, headers2ix.size());
			List<String> sl = tsheet.get(header);
			if (!(sl instanceof ArrayList))
				tsheet.put(header, new ArrayList<String>(sl));
		}

		List<String> rgns = tsheet.get(SheemLoader.RGN);

		for (int i = 0; i < rgns.size(); i++)
		{
			String rgn = rgns.get(i);
			String ntype = tsheet.get(SheemLoader.NTYPE).get(i);
			Pair<String, String> key = new Pair<String, String>(rgn, ntype);
			List<String> ll = new LinkedList<String>();

			for (String header : tsheet.keySet())
				ll.add(tsheet.get(header).get(i));
			rgn2type2Props.put(key, new ArrayList<String>(ll));
		}

		return rgn2type2Props;

	}

	/**
	 * Найти описатель по имени в xml файле
	 *
	 * @param bcontext - контекст загруженного файла
	 * @param tagName  - имя тага среди которого ищется
	 * @param name	 - имя которое ищется
	 * @return - описатель если найден, если не найден тогда
	 * @throws Exception - исключение если не найден дескриптор
	 */
	IXMLObjectDesc createTemplateDescByName(IXMLBuilderContext bcontext, String tagName, String name) throws Exception
	{
		List<IXMLObjectDesc> params = bcontext.getBuilderByTagName(tagName).getParamDescs();
		for (IXMLObjectDesc param : params)
			if (name == null || param.getObjname().equals(name))
				return new ParamDescriptor(param);
		if (name != null && params.size() > 0)
			throw new Exception("Can't find descriptor for name " + name + " tagname:" + tagName);
		else
			throw new Exception("No descriptors in tagname:" + tagName);
	}

	/**
	 *
	 * @param rgn2type2Props
	 * @param headers2ix
	 * @param rgn2type
	 * @param propName - имя свойста схемы которое надо вернуть @return - свойство схемы
	 */
	public String getScheemValByPair(Map<Pair<String, String>, List<String>> rgn2type2Props, Map<String, Integer> headers2ix, Pair<String, String> rgn2type, String propName)
	{
		List<String> propList = rgn2type2Props.get(rgn2type);
		if (propList == null)
			return null;
		Integer ix = headers2ix.get(propName);
		if (ix == null)
			return null;
		return propList.get(ix);
	}

	/**
	 * @param rgn2type2Props - ключ -> список свойств ключа
	 * @param headers2ix	 - имя столбца -> порядкаовый номер в списке свойств первого аргумента
	 * @param rgn2type -
	 * @return единое имя слоя для формирования имен правил и фильтров
	 */
	public String getStorNameByPair(Map<Pair<String, String>, List<String>> rgn2type2Props, Map<String, Integer> headers2ix, Integer level, Pair<String, String> rgn2type)
	{
		String storName = getScheemValByPair(rgn2type2Props, headers2ix, rgn2type, SheemLoader.LRNAME);
		if (storName == null)
			return String.valueOf(level) + "_" + rgn2type.toString();
		return String.valueOf(level) + "_" + storName + "_" + rgn2type;
	}

	protected IXMLObjectDesc getParamDescByTag(List<IParam> params, String paramTagName)
	{
		for (IParam param : params)
			if (
					param.getName() == null
							&& param.getValue() instanceof IXMLObjectDesc
							&& ((IXMLObjectDesc) param.getValue()).getTagname().equals(paramTagName)
					)
				return (IXMLObjectDesc) param.getValue();
		return null;
	}

	protected IParam getParamByName(List<IParam> params, String paramName)
	{
		for (IParam param : params)
			if (param.getName() != null && param.getName().equals(paramName))
				return param;
		return null;
	}

	protected void setRuleDesc(IXMLObjectDesc ruledesc, Pair<String, String> rgn2type, Integer level)
	{
		List<IParam> ruleparam = ruledesc.getParams();

		Map<String, IParam> name2param = new HashMap<String, IParam>();
		for (IParam param : ruleparam)
			name2param.put(param.getName(), param);

		Pair<String, String> pr = getHiLoRange(level);
		if (pr != null)
		{
			name2param.put(CommonStyle.HI_RANGE, new DefAttrImpl(CommonStyle.HI_RANGE, pr.first));
			name2param.put(CommonStyle.LOW_RANGE, new DefAttrImpl(CommonStyle.LOW_RANGE, pr.second));
		}
		String val;

		if ((val = getScheemValByPair(rgn2type, SheemLoader.COLOR_LINE)) != null)
			name2param.put(CommonStyle.COLOR_LINE, new DefAttrImpl(CommonStyle.COLOR_LINE, val));
		else
			name2param.put(CommonStyle.COLOR_LINE, new DefAttrImpl(CommonStyle.COLOR_LINE,"ff000000"));

		if ((val = getScheemValByPair(rgn2type, SheemLoader.COLOR_FILL)) != null)
			name2param.put(CommonStyle.COLOR_FILL, new DefAttrImpl(CommonStyle.COLOR_FILL, val));
		else
			name2param.put(CommonStyle.COLOR_FILL, new DefAttrImpl(CommonStyle.COLOR_FILL,"ff000000"));

		if ((val = getScheemValByPair(rgn2type, SheemLoader.LINE_STYLE)) != null && val.length()>0)
			name2param.put(CommonStyle.LINE_STYLE, new DefAttrImpl(CommonStyle.LINE_STYLE, val));

		if ((val = getScheemValByPair(rgn2type, SheemLoader.LINE_THICKNESS)) != null  && val.length()>0)
			name2param.put(CommonStyle.LINE_THICKNESS, new DefAttrImpl(CommonStyle.LINE_THICKNESS, val));

		if ((val = getScheemValByPair(rgn2type, SheemLoader.TEXT_MODE)) != null && val.length()>0)
			name2param.put(CnStyleRuleImpl.TEXT_MODE, new DefAttrImpl(CnStyleRuleImpl.TEXT_MODE, val));

		if ((val = getScheemValByPair(rgn2type, SheemLoader.IMAGE)) != null && val.length()>0)
		{
			if (pimage !=null && pimage.length()>0)
			{
				if (!pimage.endsWith("/") &&  !val.startsWith("/"))
					val= pimage +"/"+val;
				else
					val= pimage +val;
			}
			if (rgn2type.first.equals(MpRecord.KEYGON))
				name2param.put(CommonStyle.TEXTURE_IMAGE_PATH, new DefAttrImpl(CommonStyle.TEXTURE_IMAGE_PATH, val));
			else if (rgn2type.first.equals(MpRecord.KEYPNT))
			{
				name2param.put(CommonStyle.POINT_IMAGE_PATH, new DefAttrImpl(CommonStyle.POINT_IMAGE_PATH, val));
				if ((val = getScheemValByPair(rgn2type, SheemLoader.CENTRALIMGPNT)) != null && val.length()>0)
					name2param.put(CommonStyle.POINT_IMAGE_СENTRAL,new DefAttrImpl(CommonStyle.POINT_IMAGE_СENTRAL, val));
			}
	   }


		String[] defOrderParam = new String[]
		{
				CommonStyle.HI_RANGE,
				CommonStyle.LOW_RANGE,
				CommonStyle.COLOR_LINE,
				CommonStyle.COLOR_FILL,
				CommonStyle.LINE_STYLE,
				CommonStyle.LINE_THICKNESS,
				CnStyleRuleImpl.TEXT_MODE,
				CommonStyle.TEXTURE_IMAGE_PATH,
				CommonStyle.TEXTURE_RECT,
				CommonStyle.POINT_IMAGE_PATH,
				CommonStyle.POINT_IMAGE_СENTRAL,
				CommonStyle.POINT_IMAGE_RECT

		};

		ruleparam.clear();
		for (String paramname : defOrderParam)
		{
			IParam param = name2param.get(paramname);
			if (param!=null)
				ruleparam.add(param);
		}
	}

	/**
	 * @param smeters - МЕТРОВ на местности  на один сантиметр в карте
	 * @return пиксели на метр на местности
	 */
	public String getScaleByMeters(String smeters)
	{
		if (smeters == null)
			return String.valueOf(-1);
		else
		{
			int meters = Integer.parseInt(smeters);
			//(meters*100) - метры на местности на метры на карте
			//А надо метры/пиксели
			return String.valueOf(1.0 * (meters * 100) / pperm);
		}
	}

	public Pair<String, String> getHiLoRange(Integer level)
	{
		try
		{
			Pair<String, String> pr = null;
			if ((pr = level2Scale.get(level)) != null)
				return pr;

			if (mpHeader != null && tblscale != null)
			{
//				String scntlvl=mpHeader.getTemplateByName("Levels");
//				int cntlvl = Integer.parseInt(scntlvl);

				//Получим номер масштаба
				String slvln = mpHeader.getParamByName("Level" + level);
				int lvln = Integer.parseInt(slvln);

//				String slvprev=mpHeader.getTemplateByName("Level"+(level-1));
//				if (slvprev==null)
				String slvprev = String.valueOf(lvln + 1); //Крупный масштаб данного слой  (предедущий слой должен быть у нас на один больше чем текущий)

				String slvlnext = mpHeader.getParamByName("Level" + (level + 1));//Мелкий масштаб данного слой
				slvlnext = String.valueOf(Integer.parseInt(slvlnext) + 1); //Следующий слой должен быть номер

				//Запросим масштабы в метрах
				List<String> lvlcol = tblscale.get("LEVEL");
				List<String> mcol = tblscale.get("METER");
				Pair<String, String> big2small = new Pair<String, String>(null, null);

				for (int i = 0; i < lvlcol.size(); i++)
				{
					String lvl = lvlcol.get(i);
					if (lvl.equals(slvprev))
						big2small.first = mcol.get(i);
					else if (lvl.equals(slvlnext))
					{
						big2small.second = mcol.get(i);
						break;
					}
				}
				Pair retpr = new Pair<String, String>(getScaleByMeters(big2small.second), getScaleByMeters(big2small.first));

				level2Scale.put(level, retpr);
				return retpr;
			}
		}
		catch (NumberFormatException e)
		{
			e.printStackTrace();
		}
		return null;
	}

	public Map<String, Integer> getHeaders2ix()
	{
		getRgn2type2Props();
		return headers2ix;
	}

	public String getStorNameByPair(Integer level, Pair<String, String> rgn2type)
	{
		getRgn2type2Props();
		return getStorNameByPair(rgn2type2Props, headers2ix, level, rgn2type);
	}

	public String getScheemValByPair(Pair<String, String> rgn2type, String propName)
	{
		getRgn2type2Props();
		return getScheemValByPair(rgn2type2Props, headers2ix, rgn2type, propName);
	}

	public Map<Pair<String, String>, List<String>> getRgn2type2Props()
	{
		if (rgn2type2Props == null)
			rgn2type2Props = formTable(headers2ix = new HashMap<String, Integer>(), tblsheem);
		return rgn2type2Props;
	}

	public void translate(IXMLBuilderContext bcontext, InParams inparams) throws Exception
	{
		translate(bcontext,inparams,null);
	}

	public void translate(IXMLBuilderContext bcontext, InParams inparams,BufferedReader br) throws Exception
	{
		long starttime = System.currentTimeMillis();

		this.inparams = inparams;
		this.encoding = inparams.get(InParamsBaseConv.O_encoding);
		this.bcontext = bcontext;
		setParams(inparams);

		if (br==null)
			br=new BufferedReader(new InputStreamReader(new FileInputStream(mp), getEncoding()));

		viewSz = new Point(1024, 768);//Размер экрана TODO Изменить

		//Загрузка цветовой схемы
		headerssheem = new LinkedList<String>();
		tblsheem = SheemLoader.loadScheem(dlshm, headerssheem, cshm);

		//Загрузка таблицы масштабов
		headerscale = new LinkedList<String>();
		tblscale = SheemLoader.loadScheem(dlsc, headerscale, sсtbl);


		List<String> typelist = tblsheem.get("NTYPE");
		for (int i = 0; i < typelist.size(); i++)
		{
			int type = Integer.parseInt(typelist.get(i).substring(2), 16);
			typelist.set(i, "0x" + Integer.toHexString(type));
		}


		transdesc = createTemplateDescByName(bcontext, KernelConst.TRANSFORMER_TAGNAME, transname);
		final InitAbleMapTransformer trans = (InitAbleMapTransformer) bcontext.getBuilderByTagName(transdesc.getTagname()).initByDescriptor(transdesc, null);

		Collection dstpar = (Collection)inparams.getObject(InParamsBaseConv.O_trpardst);
		trans.reinitMapTransformer(dstpar);

		//TODO На базе этой таблицы генерируем таблицу отображения номер типа объекта -> имя таблицы
		//Т.о. получим набор слоев с именами в виде ObjectName_TNumber
		MPImporter imp = new MPImporter();

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

		long cntrecord = 0;
		MRect mBB = null;//MBB проекта

		Map<String, MpRecord> startTag2Rec = new HashMap<String, MpRecord>(); //мно-во записей в исходном файле
		Iterator<MpRecord> itr = imp.getMPByFileName(br, null);

		metadesc = createTemplateDescByName(bcontext, KernelConst.META_TAGNAME, metaname);//Загрузим метаинформацию

		long maxpnts=-1; //Допустимое максимальное кол-во точек в карте
		{
			IParam pmaxpnts = getParamByName(metadesc.getParams(), "maxpnts");
			Object val;
			if (pmaxpnts!=null && (val=pmaxpnts.getValue())!=null)
			{
				try
				{
					maxpnts=Long.parseLong((String)val);
				}
				catch (NumberFormatException e)
				{//
				}
			}
		}
		long cntpoints=0;
		long cntcommit=1;
		brtrans:
		while (itr.hasNext())
		{

			MpRecord _mpGeoObject = itr.next();
			if (_mpGeoObject.getStartRecLine().equals(MpRecord.HEADERSTART))
			{
				if (mpHeader != null)
					System.out.println("!!!Rewrite Header!!!");
				mpHeader = _mpGeoObject;
//				Pair<String,String> pr=getHiLoRange(0);
//				pr=getHiLoRange(1);
//				pr=getHiLoRange(2);
				continue;
			}

			if (!(_mpGeoObject instanceof MpGeoObject))
			{
				startTag2Rec.put(_mpGeoObject.getStartRecLine(), _mpGeoObject);
				continue;
			}


			MpGeoObject mpGeoObject = (MpGeoObject) _mpGeoObject;
			Map<Integer, List<Data[]>> datas = mpGeoObject.getDatas();
			for (Integer level : datas.keySet())
			{
				Map<Pair<String, String>, Properties> type2props;
				Properties prop;
				if ((type2props = level2type2props.get(level)) == null)
					level2type2props.put(level, type2props = new HashMap<Pair<String, String>, Properties>());


				int type = Integer.parseInt(mpGeoObject.getParamByName("Type").substring(2), 16);
				Pair<String, String> rgn2type = new Pair<String, String>(mpGeoObject.getSupertype(), "0x" + Integer.toHexString(type));

				if ((prop = type2props.get(rgn2type)) == null)
					type2props.put(rgn2type, prop = new Properties());


				MemEditableStorageLr memstor; //Хранилище для текущего слоя
				if ((memstor = (MemEditableStorageLr) prop.get(KEY_STORAGE)) == null)
					prop.put(KEY_STORAGE, memstor = new MemEditableStorageLr(pdir, getStorNameByPair(level, rgn2type)));

				Integer cntobj;
				if ((cntobj = (Integer) prop.get(KEY_OBJCNTR)) == null)
					prop.put(KEY_OBJCNTR, cntobj = 0);
				else
					prop.put(KEY_OBJCNTR, cntobj = (cntobj + 1));

				try
				{
					IEditableGisObject obj = imp.addMp2Storage(asname, new MpGeoObject(mpGeoObject, level), memstor);
					obj.rebuildGisValume();
					mBB = obj.getMBB(mBB);

				}
				catch (Exception e)
				{
					e.printStackTrace();
				}

				cntpoints+=mpGeoObject.getnPoints();

				if (maxpnts>0 && cntpoints>maxpnts)
				{
					System.out.println("Exit from translator by points number restrictor");
					break brtrans;
				}

//				if (memstor.getNotCommited().size()>=10000)
//					memstor.commit();
//				else
				if (cntcommit*100000<cntpoints)
				{
					cntcommit++;
					commitAllStorages(level2type2props,new ICommitChecker()
					{
						public boolean iscommit(MemEditableStorageLr storage)
						{
							return storage.getNotCommited().size()>=20000;
						}
					});
				}
			}
			cntrecord++;
			if (cntrecord % 30000 == 0)
				System.out.println("Translated cntrecord = " + cntrecord);
		}
		System.out.println("Total objects = " + cntrecord+" points: "+cntpoints);//+ " mbb: "+mBB);

		//Commit all storages
		commitAllStorages(level2type2props,null);

//Генерация проекта
		String storages = "";
		String filters = "";
		String rules = "";

		List<IXMLObjectDesc> l_layers = new LinkedList<IXMLObjectDesc>();
		String layers = "";

		//Генерация хранилищ
		//Описатель главного хранилища
		mainstor = createTemplateDescByName(bcontext, KernelConst.STORAGE_TAGNAME, "MAIN_STORAGE");

		for (Integer level : level2type2props.keySet())
		{
			Map<Pair<String, String>, Properties> type2props = level2type2props.get(level);
			for (Pair<String, String> rgn2type : type2props.keySet())
			{
				String storname = getStorNameByPair(level, rgn2type);
				//Генерация хранилища
				IXMLObjectDesc stordesc = createTemplateDescByName(bcontext, KernelConst.STORAGE_TAGNAME, "T_STOR");
				stordesc.setObjname(storname);
				stordesc.getParams().add(new DefAttrImpl(null, pdir));
				storages += stordesc.getXMLDescriptor("\t");
				mainstor.getParams().add(new DefAttrImpl(null, stordesc));

				//Генерация фильтра
				IXMLObjectDesc filterdesc = createTemplateDescByName(bcontext, KernelConst.FILTER_TAGNAME, "T_STORFILTER");
				filterdesc.setObjname("F_" + storname);
				filterdesc.getParams().add(new DefAttrImpl(null, storname));
				filters += filterdesc.getXMLDescriptor("\t");

				//Генерация правил согласно схеме отображения

				IXMLObjectDesc ruledesc;
				String val=getScheemValByPair(rgn2type, SheemLoader.TEXT_MODE);
				if ("true".equalsIgnoreCase(val))
					ruledesc = createTemplateDescByName(bcontext, KernelConst.RULE_TAGNAME, textrule);
				else
					ruledesc = createTemplateDescByName(bcontext, KernelConst.RULE_TAGNAME, drawrule);
				ruledesc.setObjname("R_" + storname);
				setRuleDesc(ruledesc, rgn2type, level);
				rules += ruledesc.getXMLDescriptor("\t");

				//Генерация слоев
				IXMLObjectDesc layerdesc = createTemplateDescByName(bcontext, KernelConst.LAYER_TAGNAME, "T_LAYER");
				layerdesc.setObjname("L_" + storname);
				List<IParam> lparam = layerdesc.getParams();
				lparam.add(new DefAttrImpl(null, ruledesc));
				lparam.add(new DefAttrImpl(null, filterdesc));
				lparam.add(new DefAttrImpl(null, mainstor));

				l_layers.add(layerdesc);
				layers += layerdesc.getXMLDescriptor("\t");
			}
		}
		storages += mainstor.getXMLDescriptor("\t");

		ConvGenerator convGenerator = new ConvGenerator();

		Pair<String, String> prefixsuffix = getXMLPrefixSuffix();
		StringBuffer sbuffer = new StringBuffer();
		sbuffer.append(prefixsuffix.first);



		sbuffer.append("<").append(metadesc.getTagname()).append("s>\n");
		sbuffer.append(metadesc.getXMLDescriptor("\t"));
		sbuffer.append("</").append(metadesc.getTagname()).append("s>\n");

		//Получим из меты имя проекта
		IParam pnameparam = getParamByName(metadesc.getParams(), "projname");
		if (pname==null)
			pname = (String) (pnameparam.getValue());
		else
			pnameparam.setValue(pname);

		String projFName = pname + ".xml";

		sbuffer.append("<").append(transdesc.getTagname()).append("s>\n");
		sbuffer.append(transdesc.getXMLDescriptor("\t"));
		sbuffer.append("</").append(transdesc.getTagname()).append("s>\n");

//--------------------------------------- Инициализация конвертеров --------------------------------------------------//
		{
			String rotname = "rot0";
			IXMLObjectDesc rotdesc = createTemplateDescByName(bcontext, KernelConst.CONVERTER_TAGNAME, rotname);
			ILinearConverter rotconv = (ILinearConverter) bcontext.getBuilderByTagName(rotdesc.getTagname()).initByDescriptor(rotdesc, null);
			scaleBpoint = convGenerator.calcScale_BPoint(mBB, rotconv, viewSz);

			IXMLObjectDesc scaledesc = createTemplateDescByName(bcontext, rotdesc.getTagname(), "scale1");
			scaledesc.getParams().add(new DefAttrImpl("initscale", scaleBpoint[0].x + " " + scaleBpoint[0].y));

			IXMLObjectDesc shiftdesc = createTemplateDescByName(bcontext, rotdesc.getTagname(), "shift2");
			shiftdesc.getParams().add(new DefAttrImpl("bindp", scaleBpoint[1].x + " " + scaleBpoint[1].y));


			String servConv = pname + "_SERVCONV";
			String projConv = pname + "_PROJCONV";

			sbuffer.append("<").append(rotdesc.getTagname()).append("s>\n");

			sbuffer.append(rotdesc.getXMLDescriptor("\t"));
			sbuffer.append(scaledesc.getXMLDescriptor("\t"));
			sbuffer.append(shiftdesc.getXMLDescriptor("\t"));

			servcnvdesc = createTemplateDescByName(bcontext, rotdesc.getTagname(), "T_SERVCONV");
			servcnvdesc.setObjname(servConv);
			sbuffer.append(servcnvdesc.getXMLDescriptor("\t"));

			projconvdesc = createTemplateDescByName(bcontext, rotdesc.getTagname(), "T_PROJCONV");
			projconvdesc.setObjname(projConv);
			sbuffer.append(projconvdesc.getXMLDescriptor("\t"));

			sbuffer.append("</").append(rotdesc.getTagname()).append("s>\n");
		}
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
			projcntx = createTemplateDescByName(bcontext, KernelConst.PROJCTXT_TAGNAME, "T_CTXT");
			sbuffer.append("<").append(projcntx.getTagname()).append("s>\n");

			projcntx.setObjname(pname);
			List<IParam> lprojcntx = projcntx.getParams();
			lprojcntx.add(new DefAttrImpl(null, metadesc));
			lprojcntx.add(new DefAttrImpl(null, mainstor));
			if (transdesc != null)
				lprojcntx.add(new DefAttrImpl(null, transdesc));

			l_layers = getOrdredLayers(l_layers, level2type2props);
			for (IXMLObjectDesc l_layer : l_layers)
				lprojcntx.add(new DefAttrImpl(null, l_layer));
			sbuffer.append(projcntx.getXMLDescriptor("\t"));
			sbuffer.append("</").append(projcntx.getTagname()).append("s>\n");

		}

		generateSuffix(sbuffer);

//Закрываем файл
		sbuffer.append(prefixsuffix.second);
		PrintWriter pwr = new PrintWriter(pdir + "/" + projFName, getEncoding());
		pwr.println(sbuffer.toString());
		pwr.flush();
		pwr.close();

		System.out.println("totaltime:" + (System.currentTimeMillis() - starttime));
	}

	private void commitAllStorages(Map<Integer, Map<Pair<String, String>, Properties>> level2type2props,ICommitChecker cheker)
			throws Exception
	{
		for (Map<Pair<String, String>, Properties> type2props : level2type2props.values())
			for (Properties prop : type2props.values())
			{
				MemEditableStorageLr memstor; //Хранилище для
				if ((memstor = (MemEditableStorageLr) prop.get(KEY_STORAGE)) != null && (cheker == null || cheker.iscommit(memstor)))
					memstor.commit();
				else
					System.out.println("Skip commit for storage");
			}
	}

	//Перезаписываем параметры которые были установлены пользователем
	protected void setParams(InParams inparams)
	{
		if (inparams==null)
			inparams=this.inparams=new InParamsBaseConv();
		String val = null;
		if ((val = inparams.get(InParamsBaseConv.O_in)) != null && val.length() > 0)
			mp = val;
		if ((val = inparams.get(InParamsBaseConv.O_pdir)) != null && val.length() > 0)
			pdir = val;
		if ((val = inparams.get(InParamsBaseConv.O_cshm)) != null && val.length() > 0)
			cshm = val;
		if ((val = inparams.get(InParamsBaseConv.O_dlshm)) != null && val.length() > 0)
			dlshm = val;
		if ((val = inparams.get(InParamsBaseConv.O_sсtbl)) != null && val.length() > 0)
			sсtbl = val;
		if ((val = inparams.get(InParamsBaseConv.O_dlsc)) != null && val.length() > 0)
			dlsc = val;
		if ((val = inparams.get(InParamsBaseConv.O_transname)) != null && val.length() > 0)
			transname = val;
		if ((val = inparams.get(InParamsBaseConv.O_metaname)) != null && val.length() > 0)
			metaname = val;
		if ((val = inparams.get(InParamsBaseConv.O_pperm)) != null && val.length() > 0)
			pperm = Integer.parseInt(val);
		if ((val = inparams.get(InParamsBaseConv.O_textrule)) != null && val.length() > 0)
			textrule = val;
		if ((val = inparams.get(InParamsBaseConv.O_drawrule)) != null && val.length() > 0)
			drawrule = val;
		if ((val = inparams.get(InParamsBaseConv.O_pname)) != null && val.length() > 0)
			pname =val;
		if ((val = inparams.get(InParamsBaseConv.O_projname)) != null && val.length() > 0)
			pname =val;
		if ((val = inparams.get(InParamsBaseConv.O_pimage)) != null && val.length() > 0)
			pimage =val;
		if ((val = inparams.get(InParamsBaseConv.O_asname)) != null && val.length() > 0)
			asname =val;
	}

	protected void generateSuffix(StringBuffer sbuffer)
			throws Exception
	{
		String viewername = "View0";//Имя вьювера по умолчанию
		String appname = "App0"; //Имя приложения по умолчанию
		InParamsConv inparams = new InParamsConv();
		inparams.translateOptions(this.inparams.getArgs());
//Сначала прасим конфигурационный файл
		for (IParam addparam : addparams)
		{
			if (addparam.getName().equalsIgnoreCase(InParamsConv.getOName(InParamsConv.O_viewername).substring(1)))
				viewername = (String) addparam.getValue();
			else if (addparam.getName().equalsIgnoreCase(InParamsConv.getOName(InParamsConv.O_appname).substring(1)))
				appname = (String) addparam.getValue();
		}
//Потом смотрим не изменил ли user значения полей в этом файле
		String val = null;
		if ((val = inparams.get(InParamsConv.O_viewername)) != null && val.length() > 0)
			viewername = val;
		if ((val = inparams.get(InParamsConv.O_appname)) != null && val.length() > 0)
			appname = val;

		IXMLObjectDesc viewdesc = createTemplateDescByName(bcontext, KernelConst.VIEWCNTRL_TAGNAME, viewername);
		sbuffer.append("<").append(viewdesc.getTagname()).append("s>\n");
		viewdesc.getParams().add(new DefAttrImpl("InitSz", viewSz.x + " " + viewSz.y));
		viewdesc.getParams().add(new DefAttrImpl(null, projcntx));
		viewdesc.getParams().add(new DefAttrImpl(null, projconvdesc));
		sbuffer.append(viewdesc.getXMLDescriptor("\t"));
		sbuffer.append("</").append(viewdesc.getTagname()).append("s>\n");

		Set<String> loadersnames = new HashSet<String>();

		StringBuffer _sbuffer = new StringBuffer();

		List<IXMLObjectDesc> params = bcontext.getBuilderByTagName(KernelConst.PLUGIN_TAGNAME).getParamDescs();
		_sbuffer.append("<").append(KernelConst.PLUGIN_TAGNAME).append("s>\n");
		for (IXMLObjectDesc param : params)
		{
			IXMLObjectDesc loaderdesc = param.getClassloader();
			if (loaderdesc != null)
				loadersnames.add(loaderdesc.getObjname());
			_sbuffer.append(param.getXMLDescriptor("\t"));
		}
		_sbuffer.append("</").append(KernelConst.PLUGIN_TAGNAME).append("s>\n");
//Добавляем загрузчиков
		if (loadersnames.size()>0)
		{
			sbuffer.append("<").append(KernelConst.CLASSLOADER_TAGNAME).append("s>\n");
			for (String loadername : loadersnames)
			{
				IXMLObjectDesc loadedesc = createTemplateDescByName(bcontext, KernelConst.CLASSLOADER_TAGNAME, loadername);
				sbuffer.append(loadedesc.getXMLDescriptor("\t"));
			}
			sbuffer.append("</").append(KernelConst.CLASSLOADER_TAGNAME).append("s>\n");
		}

//Теперь добавляем накопленные плагины
		sbuffer.append(_sbuffer);
//Ну и наконец-то приложение
		IXMLObjectDesc appdesc = createTemplateDescByName(bcontext, KernelConst.APPLICATION_TAGNAME, appname);
		sbuffer.append("<").append(appdesc.getTagname()).append("s>\n");
		sbuffer.append(appdesc.getXMLDescriptor("\t"));
		sbuffer.append("</").append(appdesc.getTagname()).append("s>\n");
	}

	public Object init(Object obj) throws Exception
	{
		IParam attr = (IParam) obj;

		if (attr.getName().equalsIgnoreCase(InParamsBaseConv.getOName(InParamsBaseConv.O_in).substring(1)))
			mp = (String) attr.getValue();
		else if (attr.getName().equalsIgnoreCase(InParamsBaseConv.getOName(InParamsBaseConv.O_cshm).substring(1)))
			cshm = (String) attr.getValue();
		else if (attr.getName().equalsIgnoreCase(InParamsBaseConv.getOName(InParamsBaseConv.O_dlshm).substring(1)))
			dlshm = (String) attr.getValue();
		else if (attr.getName().equalsIgnoreCase(InParamsBaseConv.getOName(InParamsBaseConv.O_sсtbl).substring(1)))
			sсtbl = (String) attr.getValue();
		else if (attr.getName().equalsIgnoreCase(InParamsBaseConv.getOName(InParamsBaseConv.O_dlsc).substring(1)))
			dlsc = (String) attr.getValue();
		else if (attr.getName().equalsIgnoreCase(InParamsBaseConv.getOName(InParamsBaseConv.O_pdir).substring(1)))
			pdir = (String) attr.getValue();
		else if (attr.getName().equalsIgnoreCase(InParamsBaseConv.getOName(InParamsBaseConv.O_metaname).substring(1)))
			metaname = (String) attr.getValue();
		else if (attr.getName().equalsIgnoreCase(InParamsBaseConv.getOName(InParamsBaseConv.O_transname).substring(1)))
			transname = (String) attr.getValue();
		else if (attr.getName().equalsIgnoreCase(InParamsBaseConv.getOName(InParamsBaseConv.O_pperm).substring(1)))
			pperm = Integer.parseInt((String) attr.getValue());
		else if (attr.getName().equalsIgnoreCase(InParamsBaseConv.getOName(InParamsBaseConv.O_textrule).substring(1)))
			textrule = (String) attr.getValue();
		else if (attr.getName().equalsIgnoreCase(InParamsBaseConv.getOName(InParamsBaseConv.O_drawrule).substring(1)))
			drawrule = (String) attr.getValue();
		else if (attr.getName().equalsIgnoreCase(InParamsBaseConv.getOName(InParamsBaseConv.O_pimage).substring(1)))
			pimage = (String) attr.getValue();
		else if (attr.getName().equalsIgnoreCase(InParamsBaseConv.getOName(InParamsBaseConv.O_asname).substring(1)))
			asname = (String) attr.getValue();
		else
			addparams.add(attr);
//		if 	(
//				attr.getName().equalsIgnoreCase(params.get(InParamsBaseConv.O_pname).substring(1))
//				||
//				attr.getName().equalsIgnoreCase(params.get(InParamsBaseConv.O_projname).substring(1))
//			)
//			pname = (String) attr.getValue();
		return null;
	}
	public interface ICommitChecker
	{
		boolean iscommit(MemEditableStorageLr storage);
	}

	private static class InParamsConv extends InParamsBaseConv
	{
		public static final int O_viewername = InParamsBaseConv.optarr.length;//Файл
		public static final int O_appname = O_viewername + 1;//Файл приложений
		public static String optarr[] =
				{
						"-viewername",
						"-appname"
				};
		public static  String defarr[] =
				{
						"",
						"",
				};
		public static String comments[] =
				{
						"",
						""
				};

		static
		{
			optarr= InParams.mergeArrays(InParamsBaseConv.optarr,optarr);
			defarr= InParams.mergeArrays(InParamsBaseConv.defarr,defarr);
			comments= InParams.mergeArrays(InParamsBaseConv.comments,comments);
		}

		InParamsConv()
		{
			setArrays(optarr,defarr,comments);
		}

		static public String getOName(int optIdx)
		{
			try
			{
				return optarr[optIdx];
			}
			catch (Exception e)
			{//
			}
			return null;
		}


	}
}