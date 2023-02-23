package ru.ts.conv.reg;

import ru.ts.gisutils.algs.common.MPoint;
import ru.ts.gisutils.algs.common.MRect;
import ru.ts.gisutils.algs.common.GradRadConverters;
import ru.ts.toykernel.storages.mem.MemEditableStorageLr;
import ru.ts.toykernel.geom.IEditableGisObject;
import ru.ts.toykernel.xml.def.ParamDescriptor;
import ru.ts.toykernel.attrs.def.DefAttrImpl;
import ru.ts.toykernel.attrs.def.DefaultAttrsImpl;
import ru.ts.toykernel.app.xml.IXMLBuilderContext;
import ru.ts.toykernel.consts.KernelConst;
import ru.ts.toykernel.converters.ILinearConverter;
import ru.ts.toykernel.factory.BaseInitAble;
import ru.ts.toykernel.drawcomp.rules.def.CommonStyle;
import ru.ts.toykernel.drawcomp.rules.def.CnStyleRuleImpl;
import ru.ts.toykernel.trans.InitAbleMapTransformer;
import ru.ts.utils.data.Pair;
import ru.ts.utils.data.InParams;
import ru.ts.xml.IXMLObjectDesc;
import ru.ts.factory.IParam;
import ru.ts.conv.ICompiler;
import ru.ts.conv.ConvGenerator;
import ru.ts.conv.IColorSheemLoader;
import ru.tg.db.DBUtils;

import java.io.*;
import java.util.*;
import java.util.List;
import java.awt.*;
import java.sql.Connection;

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
 * ru.ts.conv.reg.RegAppGenerator
 */
public class RegAppGenerator extends BaseInitAble
		implements ICompiler

{
	public static final String KEY_STORAGE = "KEY_STORAGE";
	public static final String KEY_OBJCNTR = "KEY_OBJCNTR";


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

	protected String textrule = "T_TEXTRULE";
	protected String drawrule = "T_DRAWRULE";
	protected String transname = "trans0";//Имя транслятора
	protected List<String> headerssheem;
	protected IColorSheemLoader tblsheem;
	protected boolean isaxis = false;//Генерировать карту относительно оси дороги
	protected String in = "";
	protected String pdir = "";//Куда сливать результат
	//protected String transname = "trans0";//Имя транслятора
	protected String metaname = "meta0"; //Имя метаданных
	protected InParams inparams;
	protected List<IParam> addparams = new LinkedList<IParam>();
	protected Connection conn;
	private String pimage;

//	/**
//	 * @param smeters - МЕТРОВ на местности  на один сантиметр в карте
//	 * @return пиксели на метр на местности
//	 */
//	public String getScaleByMeters(String smeters)
//	{
//		if (smeters == null)
//			return String.valueOf(-1);
//		else
//		{
//			int meters = Integer.parseInt(smeters);
//			//(meters*100) - метры на местности на метры на карте
//			//А надо метры/пиксели
//			return String.valueOf(1.0 * (meters * 100) / pperm);
//		}
//	}

//	protected Map<Integer, Pair<String, String>> level2Scale = new HashMap<Integer, Pair<String, String>>();

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
	 * @param l_layers		  - набор слоев
	 * @param lrname2ObjectType - отображение имени слоя -> в тип слоя
	 * @return - набор слоев но упорядоченный
	 */
	public List<IXMLObjectDesc> getOrdredLayers(List<IXMLObjectDesc> l_layers, Map<String, String> lrname2ObjectType)
	{
		List<IXMLObjectDesc> r_layers = new LinkedList<IXMLObjectDesc>();

		Map<String, IXMLObjectDesc> name2Desc = new HashMap<String, IXMLObjectDesc>();

		List<String>[] lrs = new LinkedList[]{new LinkedList<String>(), new LinkedList<String>(), new LinkedList<String>()};

		for (IXMLObjectDesc l_layer : l_layers)
		{
			String lrname = l_layer.getObjname();
			String regtype = lrname2ObjectType.get(lrname);
			if (KernelConst.POINT.equals(regtype))
				lrs[2].add(lrname);
			else if (KernelConst.LINESTRING.equals(regtype))
				lrs[1].add(lrname);
			else if (KernelConst.LINEARRING.equals(regtype))
				lrs[0].add(lrname);
			else
			{
				System.out.println("Unknown type:" + regtype);
				lrs[1].add(lrname);
			}
			name2Desc.put(lrname, l_layer);
		}

		for (List<String> lr : lrs)
			for (String lrstr : lr)
				r_layers.add(name2Desc.get(lrstr));
		return r_layers;
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

//	private void commitAllStorages(Map<Integer, Map<Pair<String, String>, Properties>> level2type2props,ICommitChecker cheker)
//			throws Exception
//	{
//		for (Map<Pair<String, String>, Properties> type2props : level2type2props.values())
//			for (Properties prop : type2props.values())
//			{
//				MemEditableStorageLr memstor; //Хранилище для
//				if ((memstor = (MemEditableStorageLr) prop.get(KEY_STORAGE)) != null && (cheker == null || cheker.iscommit(memstor)))
//					memstor.commit();
//				else
//					System.out.println("Skip commit for storage");
//			}
//	}

	//Перезаписываем параметры которые были установлены пользователем

	protected IParam getParamByName(List<IParam> params, String paramName)
	{
		for (IParam param : params)
			if (param.getName() != null && param.getName().equals(paramName))
				return param;
		return null;
	}

	protected void setRuleDesc(IXMLObjectDesc ruledesc, SheemLoader.GroupDesc groupdesc, Map<String, RegReader.IconRec> iconId2IconRec) throws Exception
	{

		List<IParam> ruleparam = ruledesc.getParams();

		Map<String, IParam> name2param = new HashMap<String, IParam>();
		for (IParam param : ruleparam)
			name2param.put(param.getName(), param);

		Pair<String, String> pr = new Pair<String, String>(String.valueOf(-1), String.valueOf(-1));
		if (pr != null)
		{
			name2param.put(CommonStyle.HI_RANGE, new DefAttrImpl(CommonStyle.HI_RANGE, pr.first));
			name2param.put(CommonStyle.LOW_RANGE, new DefAttrImpl(CommonStyle.LOW_RANGE, pr.second));
		}
		String val;

		if ((val = tblsheem.getLineColor()) != null)
			name2param.put(CommonStyle.COLOR_LINE, new DefAttrImpl(CommonStyle.COLOR_LINE, val));
		else
			name2param.put(CommonStyle.COLOR_LINE, new DefAttrImpl(CommonStyle.COLOR_LINE, "ff000000"));

		if ((val = tblsheem.getFillColor()) != null)
			name2param.put(CommonStyle.COLOR_FILL, new DefAttrImpl(CommonStyle.COLOR_FILL, val));
		else
			name2param.put(CommonStyle.COLOR_FILL, new DefAttrImpl(CommonStyle.COLOR_FILL, "ff000000"));

		if ((val = tblsheem.getLineThickness()) != null && val.length() > 0)
			name2param.put(CommonStyle.LINE_THICKNESS, new DefAttrImpl(CommonStyle.LINE_THICKNESS, val));


		if (KernelConst.POINT.equals(getTypeCurrentObject()))
		{
			RegReader.IconRec iconrec = iconId2IconRec.get(groupdesc.ID_Icon);
			if (iconrec != null && iconrec.ID_Icon != null && iconrec.ID_Icon.length() > 0)
			{
				val = iconrec.ID_Icon + ".png";
				if (pimage != null && pimage.length() > 0)
				{
					if (!pimage.endsWith("/") && !val.startsWith("/"))
						val = pimage + "/" + val;
					else
						val = pimage + val;
				}
				if (new File(val).exists())
					name2param.put(CommonStyle.POINT_IMAGE_PATH, new DefAttrImpl(CommonStyle.POINT_IMAGE_PATH, val));
				else
					System.out.println("ID_Icon ="+iconrec.ID_Icon+" whith name="+ iconrec.Name+" not found");
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
			if (param != null)
				ruleparam.add(param);
		}
	}

	public void translate(IXMLBuilderContext bcontext, InParams inparams) throws Exception
	{
		translate(bcontext, inparams, null);
	}

	public String getTypeCurrentObject()
	{
		String regtype = tblsheem.getObjectType();
		if (regtype.equals("0"))
			return KernelConst.POINT;
		if (regtype.equals("1"))
			return KernelConst.LINESTRING;
		if (regtype.equals("2"))
			return KernelConst.LINEARRING;
		else
		{
			System.out.println("Unknown type:" + regtype);
			return KernelConst.LINESTRING;
		}
	}

	public void translate(IXMLBuilderContext bcontext, InParams inparams, BufferedReader br) throws Exception
	{
		long starttime = System.currentTimeMillis();

		this.inparams = inparams;
		this.encoding = inparams.get(InParamsBaseConv.O_encoding);
		this.bcontext = bcontext;
		setParams(inparams);


		viewSz = new Point(1024, 768);//Размер экрана TODO Изменить

		//Загрузка цветовой схемы
		headerssheem = new LinkedList<String>();
		tblsheem = SheemLoader.getInstance(conn);

//		List<String> typelist = tblsheem.get("NTYPE");
//		for (int i = 0; i < typelist.size(); i++)
//		{
//			int type = Integer.parseInt(typelist.get(i).substring(2), 16);
//			typelist.set(i, "0x" + Integer.toHexString(type));
//		}

		InitAbleMapTransformer trans = null;
		if (!isaxis)
		{
			transdesc = createTemplateDescByName(bcontext, KernelConst.TRANSFORMER_TAGNAME, transname);
			trans = (InitAbleMapTransformer) bcontext.getBuilderByTagName(transdesc.getTagname()).initByDescriptor(transdesc, null);
			Collection dstpar = (Collection)inparams.getObject(InParamsBaseConv.O_trpardst);
			trans.reinitMapTransformer(dstpar);
		}


		long cntrecord = 0;
		MRect mBB = null;//MBB проекта


		metadesc = createTemplateDescByName(bcontext, KernelConst.META_TAGNAME, metaname);//Загрузим метаинформацию


		long maxpnts = -1; //Допустимое максимальное кол-во точек в карте
		{
			IParam pmaxpnts = getParamByName(metadesc.getParams(), "maxpnts");
			Object val;
			if (pmaxpnts != null && (val = pmaxpnts.getValue()) != null)
			{
				try
				{
					maxpnts = Long.parseLong((String) val);
				}
				catch (NumberFormatException e)
				{//
				}
			}
		}
		long cntpoints = 0;
		long cntcommit = 1;

		Map<String, List<RegReader.Record>> type2record = RegReader.getType2Objects(conn);
		Map<String, RegReader.IconRec> iconId2IconRec = RegReader.geticonId2IconRec(conn);



		for (String typeid : type2record.keySet())
		{
			int cnt = tblsheem.findByKey(typeid);
			SheemLoader.GroupDesc sheemobj = null;
			if (cnt > 0)
				sheemobj = (SheemLoader.GroupDesc) tblsheem.next();
			else
			{
				System.out.println("Can't find descriptor type = " + typeid);
				continue;
			}

			MemEditableStorageLr memstor = null;

			DefaultAttrsImpl defattrs = new DefaultAttrsImpl();
			{
				DefAttrImpl attr = new DefAttrImpl("объекты", tblsheem.getGroupName());
				defattrs.put(attr.getName(), attr);
			}

			{
				DefAttrImpl attr = new DefAttrImpl("TYPEID", typeid);
				defattrs.put(attr.getName(), attr);
			}

			{
				DefAttrImpl attr = new DefAttrImpl("GISTYPE",getTypeCurrentObject());
				defattrs.put(attr.getName(), attr);
			}

			{

				RegReader.IconRec iconrec = iconId2IconRec.get(sheemobj.ID_Icon);
				if (iconrec != null)
				{
					{
						DefAttrImpl attr = new DefAttrImpl("ID_ICON", iconrec.ID_Icon);
						defattrs.put(attr.getName(), attr);
					}
					{
						DefAttrImpl attr = new DefAttrImpl("ICON_PATH", iconrec.Path);
						defattrs.put(attr.getName(), attr);
					}
					{
						DefAttrImpl attr = new DefAttrImpl("ICON_NAME", iconrec.Name);
						defattrs.put(attr.getName(), attr);
					}
				}
			}

			List<RegReader.Record> objs = type2record.get(typeid);
			br:
			for (RegReader.Record gobj : objs)
			{

				if (!gobj.ID_High.equals("62")) //TODO Отсев всех не x-x объектов
					continue;

				if (gobj.typeId.equals("00")) //TODO Отсев нулевой точки
					continue;

				if (gobj.typeId.equals("23120104")) //TODO Отсев кривой  для 8 и для 51
					continue;

				if (gobj.typeId.equals("23120201")) //TODO Отсев кривой  для 8 и для 51
					continue;

				final double[] xdim;
				final double[] ydim;
				if (!isaxis)
				{
					xdim = gobj.getDimARRAY(RegReader.Record.XDIM);
					ydim = gobj.getDimARRAY(RegReader.Record.YDIM);
//Пересчет координат x,y в координаты долгота,широта  - градусы
					try
				{
					//1. Сначала получим точки отсчета
						double[] latlng = DbAcces.getLatLong(gobj.ID_High, conn);
						//2. Посчитаем коэфицента градусы/метр по X (долгота ось направлена на запад???? влево) и по Y (широта ось направлена на ЮГ - вниз)
	//					double[] kdks=GpsParser.getKdKsRad(latlng[0],latlng[1]);

						//3. Перемножим все коэфиценты получи градусы
						for (int i = 0; i < xdim.length; i++)
						{

	//						double l_lon=GradRadConverters.rad2GWD(latlng[1]-(xdim[i]/kdks[0])); //xdim[i] - долгота
	//						double l_lat=GradRadConverters.rad2GWD(latlng[0]-(ydim[i]/kdks[1])); //ydim[i] - широта


							double[] lat = new double[1];
							double[] lon = new double[1];
							RegConv.MAPtoGEO(-xdim[i], -ydim[i], latlng[0], latlng[1], lat, lon);

							lat[0] = GradRadConverters.rad2GWD(lat[0]);
							lon[0] = GradRadConverters.rad2GWD(lon[0]);

	//						double dlat=l_lat-lat[0];
	//						double dlon=l_lon-lon[0];
	//						System.out.println("dlon = " + dlon+" dlat = "+dlat);


							xdim[i] = lon[0];
							ydim[i] = lat[0];

							//4.Добавить преобразователь координат
							MPoint mPoint = new MPoint(ydim[i], xdim[i]);
							MPoint rv = new MPoint();
							trans.TransformDirect(mPoint, rv);

							xdim[i] = rv.x;
							ydim[i] = rv.y;
						}
					}
					catch (TransformException e)
					{
						e.printStackTrace();
						System.out.println("Skip record:" + gobj.attrid);
						continue;
					}
					catch (IllegalArgumentException e)
					{
						e.printStackTrace();
						System.out.println("Skip record:" + gobj.attrid);
						continue;
					}
				}
				else
				{
					ydim = gobj.getDimARRAY(RegReader.Record.LDIM);
					xdim = gobj.getDimARRAY(RegReader.Record.ADIM);
					for (double v : ydim)    //TODO Отсев больше объектов дальше 800 метров
					{
						if (v>800)
							continue br;
					}

				}


				if (memstor == null)
				{
					String groupname = getGroupName(typeid);
					memstor = new MemEditableStorageLr(pdir, groupname);
					memstor.setDefAttrs(defattrs);
				}

				IEditableGisObject obj = memstor.createObject(getTypeCurrentObject());
				obj.addSegment(obj.getSegsNumbers(), xdim, ydim);


				DefaultAttrsImpl objatrrs = new DefaultAttrsImpl();
				{
					DefAttrImpl attr = new DefAttrImpl("ZDIM", gobj.getDimARRAY(RegReader.Record.ZDIM));
					objatrrs.put(attr.getName(),attr);
				}

				{
					DefAttrImpl attr = new DefAttrImpl("L1", gobj.L1);
					objatrrs.put(attr.getName(),attr);
				}

				{
					DefAttrImpl attr = new DefAttrImpl("L2", gobj.L2);
					objatrrs.put(attr.getName(),attr);
				}


				{
					DefAttrImpl attr = new DefAttrImpl("ID_High", gobj.ID_High);
					objatrrs.put(attr.getName(),attr);
				}

//				{
//					DefAttrImpl attr = new DefAttrImpl("ADIM", gobj.getDimARRAY(Record.ADIM));
//					objatrrs.put(attr.getName(),attr);
//				}
//
//				{
//					DefAttrImpl attr = new DefAttrImpl("LDIM", gobj.getDimARRAY(Record.LDIM));
//					objatrrs.put(attr.getName(),attr);
//				}

//				{
//					DefAttrImpl attr = new DefAttrImpl("TNAME", tblsheem.getGroupName());
//					objatrrs.put(attr.getName(), attr);
//				}

				{
					DefAttrImpl attr = new DefAttrImpl("ATTRID", gobj.attrid);
					objatrrs.put(attr.getName(), attr);
				}

				obj.setCurveAttrs(objatrrs);

				obj.rebuildGisValume();
				mBB = obj.getMBB(mBB);
			}

			if (memstor!=null)
				memstor.commit();
		}
		System.out.println("Total objects = " + cntrecord + " points: " + cntpoints);//+ " mbb: "+mBB);

//Генерация проекта
		String storages = "";
		String filters = "";
		String rules = "";

		List<IXMLObjectDesc> l_layers = new LinkedList<IXMLObjectDesc>();
		String layers = "";

		//Генерация хранилищ
		//Описатель главного хранилища
		mainstor = createTemplateDescByName(bcontext, KernelConst.STORAGE_TAGNAME, "MAIN_STORAGE");

		Map<String, String> lrname2ObjectType = new HashMap<String, String>();

		for (String typeid : type2record.keySet())
		{
			int cnt = tblsheem.findByKey(typeid);
			SheemLoader.GroupDesc sheemobj = null;
			if (cnt > 0)
				sheemobj = (SheemLoader.GroupDesc) tblsheem.next();
			else
				continue;

			String groupname = getGroupName(typeid);

			//Генерация хранилища
			IXMLObjectDesc stordesc = createTemplateDescByName(bcontext, KernelConst.STORAGE_TAGNAME, "T_STOR");
			stordesc.setObjname(groupname);
			stordesc.getParams().add(new DefAttrImpl(null, pdir));
			storages += stordesc.getXMLDescriptor("\t");
			mainstor.getParams().add(new DefAttrImpl(null, stordesc));

			//Генерация фильтра
			IXMLObjectDesc filterdesc = createTemplateDescByName(bcontext, KernelConst.FILTER_TAGNAME, "T_STORFILTER");
			filterdesc.setObjname("F_" + groupname);
			filterdesc.getParams().add(new DefAttrImpl(null, groupname));
			filters += filterdesc.getXMLDescriptor("\t");

			//Генерация правил согласно схеме отображения

			IXMLObjectDesc ruledesc;

//				String val=getScheemValByPair(rgn2type, SheemLoader.TEXT_MODE);
//				if ("true".equalsIgnoreCase(val))
//					ruledesc = createTemplateDescByName(bcontext, KernelConst.RULE_TAGNAME, textrule);
//				else
//					ruledesc = createTemplateDescByName(bcontext, KernelConst.RULE_TAGNAME, drawrule);


			ruledesc = createTemplateDescByName(bcontext, KernelConst.RULE_TAGNAME, drawrule);

			ruledesc.setObjname("R_" + groupname);
			setRuleDesc(ruledesc, sheemobj, iconId2IconRec);
			rules += ruledesc.getXMLDescriptor("\t");

			//Генерация слоев
			IXMLObjectDesc layerdesc = createTemplateDescByName(bcontext, KernelConst.LAYER_TAGNAME, "T_LAYER");
			layerdesc.setObjname("L_" + groupname);
			List<IParam> lparam = layerdesc.getParams();
			lparam.add(new DefAttrImpl(null, ruledesc));
			lparam.add(new DefAttrImpl(null, filterdesc));
			lparam.add(new DefAttrImpl(null, mainstor));

			l_layers.add(layerdesc);
			layers += layerdesc.getXMLDescriptor("\t");

			lrname2ObjectType.put(layerdesc.getObjname(), getTypeCurrentObject());
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
		if (pname == null)
			pname = (String) (pnameparam.getValue());
		else
			pnameparam.setValue(pname);

		String projFName = pname + ".xml";

		if (!isaxis)
		{
			sbuffer.append("<").append(transdesc.getTagname()).append("s>\n");
			sbuffer.append(transdesc.getXMLDescriptor("\t"));
			sbuffer.append("</").append(transdesc.getTagname()).append("s>\n");
		}

//--------------------------------------- Инициализация конвертеров --------------------------------------------------//
		{
			String rotname = "rot0";
			IXMLObjectDesc rotdesc = createTemplateDescByName(bcontext, KernelConst.CONVERTER_TAGNAME, rotname);
			ILinearConverter rotconv = (ILinearConverter) bcontext.getBuilderByTagName(rotdesc.getTagname()).initByDescriptor(rotdesc, null);
			scaleBpoint = convGenerator.calcScale_BPoint2(mBB, rotconv, viewSz,!isaxis);

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

			l_layers = getOrdredLayers(l_layers, lrname2ObjectType);
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

	private String getGroupName(String typeid)
	{
		String groupname = tblsheem.getGroupName() + "_" + typeid;
//		groupname=groupname.replaceAll("\\.","_");
		groupname = groupname.replaceAll("\\\"", "_");
		groupname = groupname.replaceAll("\\*", "_");
		groupname = groupname.replaceAll("/", "_");
		return groupname;
	}

	protected void setParams(InParams inparams)
	{
		if (inparams == null)
			inparams = this.inparams = new InParamsBaseConv();
		String val = null;
		if ((val = inparams.get(InParamsBaseConv.O_in)) != null && val.length() > 0)
			in = val;
		if ((val = inparams.get(InParamsBaseConv.O_pdir)) != null && val.length() > 0)
			pdir = val;
		if ((val = inparams.get(InParamsBaseConv.O_metaname)) != null && val.length() > 0)
			metaname = val;
		if ((val = inparams.get(InParamsBaseConv.O_textrule)) != null && val.length() > 0)
			textrule = val;
		if ((val = inparams.get(InParamsBaseConv.O_drawrule)) != null && val.length() > 0)
			drawrule = val;
		if ((val = inparams.get(InParamsBaseConv.O_pname)) != null && val.length() > 0)
			pname = val;
		if ((val = inparams.get(InParamsBaseConv.O_projname)) != null && val.length() > 0)
			pname = val;
		if ((val = inparams.get(InParamsBaseConv.O_pimage)) != null && val.length() > 0)
			pimage = val;
		if ((val = inparams.get(InParamsBaseConv.O_transname)) != null && val.length() > 0)
			transname = val;
		if ((val = inparams.get(InParamsBaseConv.O_axis)) != null && val.length() > 0)
			isaxis = Boolean.parseBoolean(val);
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
		if (loadersnames.size() > 0)
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

	public Object[] init(Object... objs) throws Exception
	{
		super.init(objs);
		Properties props = new Properties();
		props.setProperty("charSet", "windows-1251");
		conn = DBUtils._getConnection("sun.jdbc.odbc.JdbcOdbcDriver",
				"jdbc:odbc:Driver={Microsoft Access Driver (*.mdb)};DBQ=", in, props);
		return null;
	}

	public Object init(Object obj) throws Exception
	{
		IParam attr = (IParam) obj;

		if (attr.getName().equalsIgnoreCase(InParamsBaseConv.getOName(InParamsBaseConv.O_in).substring(1)))
			in = (String) attr.getValue();
		else if (attr.getName().equalsIgnoreCase(InParamsBaseConv.getOName(InParamsBaseConv.O_pdir).substring(1)))
			pdir = (String) attr.getValue();
		else if (attr.getName().equalsIgnoreCase(InParamsBaseConv.getOName(InParamsBaseConv.O_metaname).substring(1)))
			metaname = (String) attr.getValue();
		else if (attr.getName().equalsIgnoreCase(InParamsBaseConv.getOName(InParamsBaseConv.O_textrule).substring(1)))
			textrule = (String) attr.getValue();
		else if (attr.getName().equalsIgnoreCase(InParamsBaseConv.getOName(InParamsBaseConv.O_drawrule).substring(1)))
			drawrule = (String) attr.getValue();
		else if (attr.getName().equalsIgnoreCase(InParamsBaseConv.getOName(InParamsBaseConv.O_pimage).substring(1)))
			pimage = (String) attr.getValue();
		else if (attr.getName().equalsIgnoreCase(InParamsBaseConv.getOName(InParamsBaseConv.O_transname).substring(1)))
			transname = (String) attr.getValue();
		else if (attr.getName().equalsIgnoreCase(InParamsBaseConv.getOName(InParamsBaseConv.O_axis).substring(1)))
			isaxis = Boolean.parseBoolean((String) attr.getValue());
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
		public static String defarr[] =
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
			optarr = InParams.mergeArrays(InParamsBaseConv.optarr, optarr);
			defarr = InParams.mergeArrays(InParamsBaseConv.defarr, defarr);
			comments = InParams.mergeArrays(InParamsBaseConv.comments, comments);
		}

		InParamsConv()
		{
			setArrays(optarr, defarr, comments);
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