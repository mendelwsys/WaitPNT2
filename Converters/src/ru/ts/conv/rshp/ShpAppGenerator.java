package ru.ts.conv.rshp;

import ru.ts.gisutils.algs.common.MPoint;
import ru.ts.gisutils.algs.common.MRect;
import ru.ts.toykernel.app.xml.IXMLBuilderContext;
import ru.ts.toykernel.xml.def.ParamDescriptor;
import ru.ts.toykernel.drawcomp.rules.def.CommonStyle;
import ru.ts.toykernel.drawcomp.rules.def.CnStyleRuleImpl;
import ru.ts.toykernel.attrs.def.DefAttrImpl;
import ru.ts.toykernel.consts.KernelConst;
import ru.ts.toykernel.trans.InitAbleMapTransformer;
import ru.ts.toykernel.storages.mem.MemEditableStorageLr;
import ru.ts.toykernel.storages.IEditableStorage;
import ru.ts.toykernel.converters.ILinearConverter;
import ru.ts.toykernel.factory.BaseInitAble;
import ru.ts.xml.IXMLObjectDesc;
import ru.ts.utils.data.Pair;
import ru.ts.utils.data.InParams;
import ru.ts.conv.ConvGenerator;
import ru.ts.conv.ICompiler;
import ru.ts.conv.rmp.SheemLoader;
import ru.ts.factory.IParam;


import java.util.*;
import java.util.List;
import java.io.*;
import java.awt.*;

import org.opengis.referencing.operation.TransformException;

/**
 * Created by IntelliJ IDEA.
 * User: vladm
 * Date: 27.06.2011
 * Time: 13:51:45
 * Shape project generator
 * ru.ts.conv.rshp.ShpAppGenerator
 */
public class ShpAppGenerator extends BaseInitAble
		implements ICompiler
{


	public static int DEF_PPERM = 3000;//Пикселей на метр для преобазования уровней в масштаб
	protected String asname;
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
	protected int pperm = DEF_PPERM;
	protected String indir = "G:\\BACK_UP\\$D\\MAPDIR\\SHPREGS\\";
	protected String cshm = "G:\\BACK_UP\\$D\\MAPDIR\\TVER\\n_ncommon.shm"; //Аттрибутивная схема (определяет как рисовать слои)
	protected String dlshm = ","; //Разделитель в схеме
	//	protected String sсtbl = "D:\\MAPDIR\\MP\\nscale.tbl"; //Определяет в каком масштабе рисовать слои
	//	protected String dlsc = ","; //Разделитель в таблице масштабов
	protected String pdir = "D:\\MAPDIR\\TEST_PSK\\";//Куда сливать результат
	protected String transname = "trans0";//Имя транслятора
	protected String metaname = "meta0"; //Имя метаданных
	protected InParams inparams;
	protected List<IParam> addparams = new LinkedList<IParam>();
	private String pimage;

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

	protected IParam getParamByName(List<IParam> params, String paramName)
	{
		for (IParam param : params)
			if (param.getName() != null && param.getName().equals(paramName))
				return param;
		return null;
	}

	protected String getScheemValByPair(Map<String, String> name2val, String name)
	{
		return name2val.get(name);
	}

	protected void setRuleDesc(String geotype, IXMLObjectDesc ruledesc, Map<String, String> name2val)
	{
		List<IParam> ruleparam = ruledesc.getParams();

		Map<String, IParam> name2param = new HashMap<String, IParam>();
		for (IParam param : ruleparam)
			name2param.put(param.getName(), param);

		Pair<String, String> pr = getHiLoRange(0);
		if (pr != null)
		{
			name2param.put(CommonStyle.HI_RANGE, new DefAttrImpl(CommonStyle.HI_RANGE, pr.first));
			name2param.put(CommonStyle.LOW_RANGE, new DefAttrImpl(CommonStyle.LOW_RANGE, pr.second));
		}

		String val;
		if ((val = getScheemValByPair(name2val, SheemLoader.COLOR_LINE)) != null)
			name2param.put(CommonStyle.COLOR_LINE, new DefAttrImpl(CommonStyle.COLOR_LINE, val));
		else if (!name2param.containsKey(CommonStyle.COLOR_LINE))
			name2param.put(CommonStyle.COLOR_LINE, new DefAttrImpl(CommonStyle.COLOR_LINE, "ff000000"));

		if ((val = getScheemValByPair(name2val, SheemLoader.COLOR_FILL)) != null)
			name2param.put(CommonStyle.COLOR_FILL, new DefAttrImpl(CommonStyle.COLOR_FILL, val));
		else if (!name2param.containsKey(CommonStyle.COLOR_FILL))
			name2param.put(CommonStyle.COLOR_FILL, new DefAttrImpl(CommonStyle.COLOR_FILL, "ff000000"));

		if ((val = getScheemValByPair(name2val, SheemLoader.LINE_STYLE)) != null && val.length() > 0)
			name2param.put(CommonStyle.LINE_STYLE, new DefAttrImpl(CommonStyle.LINE_STYLE, val));

		if ((val = getScheemValByPair(name2val, SheemLoader.LINE_THICKNESS)) != null && val.length() > 0)
			name2param.put(CommonStyle.LINE_THICKNESS, new DefAttrImpl(CommonStyle.LINE_THICKNESS, val));

		if ((val = getScheemValByPair(name2val, SheemLoader.TEXT_MODE)) != null && val.length() > 0)
			name2param.put(CnStyleRuleImpl.TEXT_MODE, new DefAttrImpl(CnStyleRuleImpl.TEXT_MODE, val));

		if ((val = getScheemValByPair(name2val, SheemLoader.IMAGE)) != null && val.length() > 0)
		{
			if (pimage != null && pimage.length() > 0)
			{
				if (!pimage.endsWith("/") && !val.startsWith("/"))
					val = pimage + "/" + val;
				else
					val = pimage + val;
			}
			if (geotype.equals(KernelConst.LINEARRING))
				name2param.put(CommonStyle.TEXTURE_IMAGE_PATH, new DefAttrImpl(CommonStyle.TEXTURE_IMAGE_PATH, val));
			else if (geotype.equals(KernelConst.POINT))
			{
				name2param.put(CommonStyle.POINT_IMAGE_PATH, new DefAttrImpl(CommonStyle.POINT_IMAGE_PATH, val));
				if ((val = getScheemValByPair(name2val, SheemLoader.CENTRALIMGPNT)) != null && val.length() > 0)
					name2param.put(CommonStyle.POINT_IMAGE_СENTRAL, new DefAttrImpl(CommonStyle.POINT_IMAGE_СENTRAL, val));
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
//		try
//		{
//			Pair<String, String> pr = null;
//			if ((pr = level2Scale.get(level)) != null)
//				return pr;
//
//			if (mpHeader != null && tblscale != null)
//			{
////				String scntlvl=mpHeader.getTemplateByName("Levels");
////				int cntlvl = Integer.parseInt(scntlvl);
//
//				//Получим номер масштаба
//				String slvln = mpHeader.getParamByName("Level" + level);
//				int lvln = Integer.parseInt(slvln);
//
////				String slvprev=mpHeader.getTemplateByName("Level"+(level-1));
////				if (slvprev==null)
//				String slvprev = String.valueOf(lvln + 1); //Крупный масштаб данного слой  (предедущий слой должен быть у нас на один больше чем текущий)
//
//				String slvlnext = mpHeader.getParamByName("Level" + (level + 1));//Мелкий масштаб данного слой
//				slvlnext = String.valueOf(Integer.parseInt(slvlnext) + 1); //Следующий слой должен быть номер
//
//				//Запросим масштабы в метрах
//				List<String> lvlcol = tblscale.get("LEVEL");
//				List<String> mcol = tblscale.get("METER");
//				Pair<String, String> big2small = new Pair<String, String>(null, null);
//
//				for (int i = 0; i < lvlcol.size(); i++)
//				{
//					String lvl = lvlcol.get(i);
//					if (lvl.equals(slvprev))
//						big2small.first = mcol.get(i);
//					else if (lvl.equals(slvlnext))
//					{
//						big2small.second = mcol.get(i);
//						break;
//					}
//				}
//				Pair retpr = new Pair<String, String>(getScaleByMeters(big2small.second), getScaleByMeters(big2small.first));
//
//				level2Scale.put(level, retpr);
		return new Pair<String, String>("-1", "-1");
//			}
//		}
//		catch (NumberFormatException e)
//		{
//			e.printStackTrace();
//		}
//		return null;
	}

	public void translate(IXMLBuilderContext bcontext, InParams inparams) throws Exception
	{
		translate(bcontext, inparams, null);
	}

	public void translate(IXMLBuilderContext bcontext, InParams inparams, BufferedReader br) throws Exception
	{
		long starttime = System.currentTimeMillis();

		this.inparams = inparams;
		this.encoding = inparams.get(InParamsBaseConv.O_encoding);
		this.bcontext = bcontext;
		setParams(inparams);

		final String[] criterionfieldnames = {"GRMN_TYPE"};

		viewSz = new Point(1024, 768);//Размер экрана TODO Изменить

		//Загрузка цветовой схемы
		final List<String> headerssheem = new LinkedList<String>();
		Map<String, List<String>> l_tblsheem = new HashMap<String, List<String>>();
		try
		{
			l_tblsheem = SheemLoader.loadScheem(dlshm, headerssheem, cshm);
		}
		catch (Exception e)
		{//
		}
		final Map<String, List<String>> tblsheem = l_tblsheem;


		final Map<String, Map<String, Collection<Integer>>> criterion2Val2Integer = new HashMap<String, Map<String, Collection<Integer>>>();
		//Build index for speed up search in the tblsheem
		for (String criterionfieldname : criterionfieldnames)
		{
			HashMap<String, Collection<Integer>> curix;
			criterion2Val2Integer.put(criterionfieldname, curix = new HashMap<String, Collection<Integer>>());
			List<String> vals = tblsheem.get(criterionfieldname);
			if (vals != null)
				for (int i = 0; i < vals.size(); i++)
				{
					String val = vals.get(i);
					Collection<Integer> ixlist = curix.get(val);
					if (ixlist == null)
						curix.put(val, ixlist = new HashSet<Integer>());
					ixlist.add(i);
				}
		}

//		List<String> typelist = tblsheem.get("NTYPE");
//		for (int i = 0; i < typelist.size(); i++)
//		{
//			int type = Integer.parseInt(typelist.get(i).substring(2), 16);
//			typelist.set(i, "0x" + Integer.toHexString(type));
//		}


		transdesc = createTemplateDescByName(bcontext, KernelConst.TRANSFORMER_TAGNAME, transname);
		final InitAbleMapTransformer trans = (InitAbleMapTransformer) bcontext.getBuilderByTagName(transdesc.getTagname()).initByDescriptor(transdesc, null);

		Collection dstpar = (Collection) inparams.getObject(InParamsBaseConv.O_trpardst);
		trans.reinitMapTransformer(dstpar);


		File infolder = new File(indir);
		String[] fls = infolder.list(new FilenameFilter()
		{

			public boolean accept(File dir, String name)
			{
				return name.endsWith(".shp");
			}
		});

		metadesc = createTemplateDescByName(bcontext, KernelConst.META_TAGNAME, metaname);//Загрузим метаинформацию

		long cntpoints = 0;
		long cntcommit = 1;
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


		Set<Integer> allskip = new HashSet<Integer>();

		MRect mBB = null;//MBB проекта

		final Map<String, Map<String, IEditableStorage>> mainfile2name2storages = new HashMap<String, Map<String, IEditableStorage>>();
		final Map<String, Pair<String, Collection<Integer>>> nmstor2geotypeAixinstyle = new HashMap<String, Pair<String, Collection<Integer>>>();

		final Map<String,String> nmstor2asname = new HashMap<String, String>();

		brtrans:
		for (final String mainfile : fls)
		{
			final String flname = mainfile.substring(0, mainfile.indexOf(".shp"));
			SHPImporter imp = new SHPImporter(indir + "/" + flname + ".shp", indir + "/" + flname + ".dbf", indir + "/" + flname + ".shx", "WINDOWS-1251");
			imp.setTrans(new SHPImporter.TransformPntsArr()
			{
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

			imp.setStorageFactory(new SHPImporter.IStorageFactory()
			{
				public Pair<IEditableStorage, String> getStorage(String geotype, ReadShp.SHPRecord record, Map<String, String> attr2val) throws Exception
				{
					Map<String, IEditableStorage> name2storage = mainfile2name2storages.get(flname);
					if (name2storage == null)
						mainfile2name2storages.put(flname, name2storage = new HashMap<String, IEditableStorage>());

					Collection<Integer> reqres = null; //мно-во индексов в таблице стилей отвечающие данному набору аттрибутов объекта
					for (String criterionfname : criterionfieldnames)
					{

						String val = attr2val.get(criterionfname);
//						List<String> listvals = tblsheem.get(criterionfname);
						if (reqres == null)
						{
							reqres = criterion2Val2Integer.get(criterionfname).get(val);
//							reqres = new LinkedList<Integer>();
//							for (int i = 0; i < listvals.size(); i++)
//							{
//								String listval = listvals.get(i);
//								if (listval.equals(val))
//									reqres.add(i);
//							}
						}
						else
						{
							Set<Integer> l_result = new HashSet<Integer>();
							Set<Integer> reqres2 = new HashSet<Integer>(criterion2Val2Integer.get(criterionfname).get(val));
							for (Integer ix : reqres)
							{
								if (reqres2.contains(ix))
									l_result.add(ix);
//								String listval = listvals.get(ix);
//								if (listval.equals(val))
//									l_result.add(ix);
							}
							reqres = l_result;
						}
					}

					String key = flname;
					if (reqres!=null && reqres.size() > 0)
					{
						int ix = reqres.iterator().next();
						for (String criterionfieldname : criterionfieldnames)
						{
							List<String> listvals = tblsheem.get(criterionfieldname);
							key += "_" + listvals.get(ix);
						}
					}

					String _asname = nmstor2asname.get(key);
					IEditableStorage rv = name2storage.get(key);
					if (rv == null)
					{
						name2storage.put(key, rv = new MemEditableStorageLr(pdir, key));
						nmstor2geotypeAixinstyle.put(key, new Pair(geotype, ((reqres != null) ? reqres : new HashSet())));

						Map<String, String> name2val = getSheemTuple(headerssheem, tblsheem, reqres);
						_asname = name2val.get(SheemLoader.AS_NAME);
						if (_asname == null || _asname.length() == 0)
							_asname = asname;
						nmstor2asname.put(key,_asname);
					}
					return new Pair(rv, _asname);
				}
			});

//			if (maxpnts>0 && cntpoints>maxpnts)
//			{
//				System.out.println("Exit from translator by points number restrictor");
//				break brtrans;
//			}


			mBB = imp.shp2Storage(mBB);
			allskip.addAll(imp.getSkipedtypes());


			System.out.print("Commit all storages ...");
			for (Map<String, IEditableStorage> name2storages : mainfile2name2storages.values())
			{
				System.out.print(" " + name2storages.size() + "...");
				for (IEditableStorage memstor : name2storages.values())
				{
					memstor.commit();
					((MemEditableStorageLr) memstor).releaseStorage();
				}
				System.out.print("Ok");
			}
			System.out.println();
		}


		for (Integer type : allskip)
			System.out.println("type = " + type);
		System.out.println("Total objects = " + " points: " + cntpoints);//+ " mbb: "+mBB);

//Генерация проекта
		String storages = "";
		String filters = "";
		String rules = "";
		String layers="";

		Map<String,List<IXMLObjectDesc>> geotype2layers=new HashMap<String,List<IXMLObjectDesc>>();

		//Генерация хранилищ
		//Описатель главного хранилища
		mainstor = createTemplateDescByName(bcontext, KernelConst.STORAGE_TAGNAME, "MAIN_STORAGE");



		for (String storname : nmstor2geotypeAixinstyle.keySet())
		{
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
			String val = "false";//getScheemValByPair(rgn2type, SheemLoader.TEXT_MODE);
			if ("true".equalsIgnoreCase(val))
				ruledesc = createTemplateDescByName(bcontext, KernelConst.RULE_TAGNAME, textrule);
			else
				ruledesc = createTemplateDescByName(bcontext, KernelConst.RULE_TAGNAME, drawrule);
			ruledesc.setObjname("R_" + storname);

			String geotype = nmstor2geotypeAixinstyle.get(storname).first;
			Collection<Integer> ixstyle = nmstor2geotypeAixinstyle.get(storname).second;
			Map<String, String> attr2val = getSheemTuple(headerssheem, tblsheem, ixstyle);
			setRuleDesc(geotype, ruledesc, attr2val);


			rules += ruledesc.getXMLDescriptor("\t");

			//Генерация слоев
			IXMLObjectDesc layerdesc = createTemplateDescByName(bcontext, KernelConst.LAYER_TAGNAME, "T_LAYER");
			layerdesc.setObjname("L_" + storname);
			List<IParam> lparam = layerdesc.getParams();
			lparam.add(new DefAttrImpl(null, ruledesc));
			lparam.add(new DefAttrImpl(null, filterdesc));
			lparam.add(new DefAttrImpl(null, mainstor));


			List<IXMLObjectDesc> l_layers = geotype2layers.get(geotype);
			if (l_layers==null)
				geotype2layers.put(geotype,l_layers=new LinkedList<IXMLObjectDesc>());
			l_layers.add(layerdesc);
			layers += layerdesc.getXMLDescriptor("\t");
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

			String[] typeorder=SHPImporter.classictypeorder;
			for (String geotype : typeorder)
			{
				List<IXMLObjectDesc> l_layers=geotype2layers.get(geotype);
				if (l_layers!=null)
					for (IXMLObjectDesc l_layer : l_layers)
						lprojcntx.add(new DefAttrImpl(null, l_layer));
			}
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

	public Map<String, String> getSheemTuple(List<String> headerssheem, Map<String, List<String>> tblsheem, Collection<Integer> ixstyle)
	{
		Map<String, String> name2vals = new HashMap<String, String>();
		if (ixstyle!=null && ixstyle.size() > 0)
		{
			int ix = ixstyle.iterator().next();

			for (String name : headerssheem)
			{
				List<String> ll = tblsheem.get(name);
				name2vals.put(name, ll.get(ix));
			}
		}
		return name2vals;
	}

	//Перезаписываем параметры которые были установлены пользователем
	protected void setParams(InParams inparams)
	{
		if (inparams == null)
			inparams = this.inparams = new InParamsBaseConv();
		String val = null;
		if ((val = inparams.get(InParamsBaseConv.O_in)) != null && val.length() > 0)
			indir = val;
		if ((val = inparams.get(InParamsBaseConv.O_pdir)) != null && val.length() > 0)
			pdir = val;
		if ((val = inparams.get(InParamsBaseConv.O_cshm)) != null && val.length() > 0)
			cshm = val;
		if ((val = inparams.get(InParamsBaseConv.O_dlshm)) != null && val.length() > 0)
			dlshm = val;
//		if ((val = inparams.get(InParamsBaseConv.O_sсtbl)) != null && val.length() > 0)
//			sсtbl = val;
//		if ((val = inparams.get(InParamsBaseConv.O_dlsc)) != null && val.length() > 0)
//			dlsc = val;
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
			pname = val;
		if ((val = inparams.get(InParamsBaseConv.O_projname)) != null && val.length() > 0)
			pname = val;
		if ((val = inparams.get(InParamsBaseConv.O_pimage)) != null && val.length() > 0)
			pimage = val;
		if ((val = inparams.get(InParamsBaseConv.O_asname)) != null && val.length() > 0)
			asname = val;

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

	public Object init(Object obj) throws Exception
	{
		IParam attr = (IParam) obj;

		if (attr.getName().equalsIgnoreCase(InParamsBaseConv.getOName(InParamsBaseConv.O_in).substring(1)))
			indir = (String) attr.getValue();
		else if (attr.getName().equalsIgnoreCase(InParamsBaseConv.getOName(InParamsBaseConv.O_cshm).substring(1)))
			cshm = (String) attr.getValue();
		else if (attr.getName().equalsIgnoreCase(InParamsBaseConv.getOName(InParamsBaseConv.O_dlshm).substring(1)))
			dlshm = (String) attr.getValue();
//		else if (attr.getName().equalsIgnoreCase(InParamsBaseConv.getOName(InParamsBaseConv.O_sсtbl).substring(1)))
//			sсtbl = (String) attr.getValue();
//		else if (attr.getName().equalsIgnoreCase(InParamsBaseConv.getOName(InParamsBaseConv.O_dlsc).substring(1)))
//			dlsc = (String) attr.getValue();
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
