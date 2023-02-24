package ru.ts.conv.rdxf;

import ru.ts.toykernel.factory.BaseInitAble;
import ru.ts.toykernel.app.xml.IXMLBuilderContext;
import ru.ts.toykernel.xml.def.ParamDescriptor;
import ru.ts.toykernel.consts.KernelConst;
import ru.ts.toykernel.attrs.def.DefAttrImpl;
import ru.ts.toykernel.attrs.def.DefaultAttrsImpl;
import ru.ts.toykernel.converters.ILinearConverter;
import ru.ts.toykernel.storages.IEditableStorage;
import ru.ts.toykernel.storages.mem.MemEditableStorageLr;
import ru.ts.toykernel.geom.IEditableGisObject;
import ru.ts.toykernel.drawcomp.rules.def.CnStyleRuleImpl;
import ru.ts.toykernel.drawcomp.rules.def.CommonStyle;
import ru.ts.toykernel.drawcomp.rules.dxfex.DxfCnStyleRule2;
import ru.ts.toykernel.drawcomp.painters.dxf.InsertPainter2;
import ru.ts.toykernel.drawcomp.painters.def.DefPointPainter;
import ru.ts.conv.ICompiler;
import ru.ts.conv.ConvGenerator;
import ru.ts.conv.rshp.SHPConstants;
import ru.ts.utils.data.InParams;
import ru.ts.utils.data.Pair;
import ru.ts.gisutils.algs.common.MPoint;
import ru.ts.gisutils.algs.common.MRect;
import ru.ts.xml.IXMLObjectDesc;
import ru.ts.factory.IParam;
import ru.ts.factory.IInitAble;

import java.io.BufferedReader;
import java.io.PrintWriter;
import java.awt.Point;
import java.util.*;
import java.util.List;

import org.kabeja.parser.Parser;
import org.kabeja.parser.ParserBuilder;
import org.kabeja.dxf.*;
import org.kabeja.dxf.helpers.*;

/**
 * Created by IntelliJ IDEA.
 * User: vladm
 * Date: 25.02.2012
 * Time: 10:23:39
 * Парсер dxf формата
 */
public class DXFAppGenerator
		extends BaseInitAble
		implements ICompiler
{

	public static int DEF_PPERM = 3000;//Пикселей на метр для преобазования уровней в масштаб
	protected String asname;
	protected String pname;
	protected Point viewSz;
	protected MPoint[] scaleBpoint;
	protected IXMLBuilderContext bcontext;
	protected IXMLObjectDesc metadesc;//Дескриптор метаинформации
	protected IXMLObjectDesc servcnvdesc;
	protected IXMLObjectDesc projconvdesc;
	protected IXMLObjectDesc projcntx;
	protected List<IXMLObjectDesc> addlayersdesc= new LinkedList<IXMLObjectDesc>();
	protected  StringBuffer addstorages= new StringBuffer();
	protected  StringBuffer addfilters= new StringBuffer();
	protected  StringBuffer addrules= new StringBuffer();
	protected  StringBuffer addlayers= new StringBuffer();
	//Проекты описывающие блоки.
	protected Map<String,Pair<StringBuffer,Pair<IXMLObjectDesc,MRect> > > name2projblock =  new HashMap<String,Pair<StringBuffer,Pair<IXMLObjectDesc,MRect>>>();
	protected String attrasname = KernelConst.ATTR_CURVE_NAME; //TODO Имя объекта в хранилище (???? Может и не стои генерировать его)
	protected String encoding;
	protected String textrule = "T_TEXTRULE";
	protected String drawrule = "T_DRAWRULE";
	protected int pperm = DEF_PPERM;//TODO !!!! Пересмотреть
	protected String infile = null; //Откуда брать результат
	protected String pdir = null;//Куда сливать результат
	protected String metaname = "meta0"; //Имя метаданных
	protected InParams inparams;
	protected java.util.List<IParam> addparams = new LinkedList<IParam>();

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
		java.util.List<IXMLObjectDesc> params = bcontext.getBuilderByTagName(tagName).getParamDescs();
		for (IXMLObjectDesc param : params)
			if (name == null || param.getObjname().equals(name))
				return new ParamDescriptor(param);
		if (name != null && params.size() > 0)
			throw new Exception("Can't find descriptor for name " + name + " tagname:" + tagName);
		else
			throw new Exception("No descriptors in tagname:" + tagName);
	}

	protected IXMLObjectDesc getParamDescByTag(java.util.List<IParam> params, String paramTagName)
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

	protected IParam getParamByName(java.util.List<IParam> params, String paramName)
	{
		for (IParam param : params)
			if (param.getName() != null && param.getName().equals(paramName))
				return param;
		return null;
	}

	//Перезаписываем параметры которые были установлены пользователем
	protected void setParams(InParams inparams)
	{
		if (inparams == null)
			inparams = this.inparams = new InParamsBaseConv();
		String val = null;
		if ((val = inparams.get(InParamsBaseConv.O_in)) != null && val.length() > 0)
			infile = val;
		if ((val = inparams.get(InParamsBaseConv.O_pdir)) != null && val.length() > 0)
			pdir = val;
//		if ((val = inparams.get(InParamsBaseConv.O_cshm)) != null && val.length() > 0)
//			cshm = val;
//		if ((val = inparams.get(InParamsBaseConv.O_dlshm)) != null && val.length() > 0)
//			dlshm = val;
//		if ((val = inparams.get(InParamsBaseConv.O_transname)) != null && val.length() > 0)
//			transname = val;
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
//		if ((val = inparams.get(InParamsBaseConv.O_pimage)) != null && val.length() > 0)
//			pimage = val;
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
			infile = (String) attr.getValue();
//		else if (attr.getName().equalsIgnoreCase(InParamsBaseConv.getOName(InParamsBaseConv.O_cshm).substring(1)))
//			cshm = (String) attr.getValue();
//		else if (attr.getName().equalsIgnoreCase(InParamsBaseConv.getOName(InParamsBaseConv.O_dlshm).substring(1)))
//			dlshm = (String) attr.getValue();
		else if (attr.getName().equalsIgnoreCase(InParamsBaseConv.getOName(InParamsBaseConv.O_pdir).substring(1)))
			pdir = (String) attr.getValue();
		else if (attr.getName().equalsIgnoreCase(InParamsBaseConv.getOName(InParamsBaseConv.O_metaname).substring(1)))
			metaname = (String) attr.getValue();
//		else if (attr.getName().equalsIgnoreCase(InParamsBaseConv.getOName(InParamsBaseConv.O_transname).substring(1)))
//			transname = (String) attr.getValue();
		else if (attr.getName().equalsIgnoreCase(InParamsBaseConv.getOName(InParamsBaseConv.O_pperm).substring(1)))
			pperm = Integer.parseInt((String) attr.getValue());
		else if (attr.getName().equalsIgnoreCase(InParamsBaseConv.getOName(InParamsBaseConv.O_textrule).substring(1)))
			textrule = (String) attr.getValue();
		else if (attr.getName().equalsIgnoreCase(InParamsBaseConv.getOName(InParamsBaseConv.O_drawrule).substring(1)))
			drawrule = (String) attr.getValue();
//		else if (attr.getName().equalsIgnoreCase(InParamsBaseConv.getOName(InParamsBaseConv.O_pimage).substring(1)))
//			pimage = (String) attr.getValue();
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

	public void translate(IXMLBuilderContext bcontext, InParams inparams) throws Exception
	{
		translate(bcontext, inparams, null);
	}

	public void translate(IXMLBuilderContext bcontext, InParams inparams, BufferedReader br) throws Exception
	{
		long starttime = System.currentTimeMillis();

		try
		{
			this.inparams = inparams;
			this.encoding = inparams.get(ru.ts.conv.rshp.InParamsBaseConv.O_encoding);
			this.bcontext = bcontext;
			setParams(inparams);

			if (infile == null)
				throw new Exception("Need to set input dxf file");
			if (pdir == null)
				throw new Exception("Need to set output dir");


			viewSz = new Point(1024, 768);//Размер экрана TODO Изменить
			metadesc = createTemplateDescByName(bcontext, KernelConst.META_TAGNAME, metaname);//Загрузим метаинформацию
			MRect inMBB = null;//MBB проекта



//------------------------Разбор входного фала генерация хранилищь правил слоев и фильтров ---------------------------//
//Генерация проекта
			Map<String, IEditableStorage> name2storage = new HashMap<String, IEditableStorage>();
			List<String> ordlrnames = new LinkedList<String>();//Упорядоченый список имен входных слоев для сохранения порядка рисования
			//(TODO Хотя возможно это и не актуально для dxf)
			//TODO Генерируем правило для слоя сразу
			Map<String, IXMLObjectDesc> name2Ruledesc = new HashMap<String, IXMLObjectDesc>();

			Parser parser = ParserBuilder.createDefaultParser();
			parser.parse(infile);
			DXFDocument doc = parser.getDocument();
			doc.getBounds(); //TODO Использовать для обрамляющего прямоугольника

			DXFHeader docHeader = doc.getDXFHeader();
			double gscale= docHeader.getLinetypeScale();

//1. Прасим все блоки для того что бы обеспечить в последующем ссылки на на них
			{
				Map<String, IEditableStorage> l_name2storage = new HashMap<String, IEditableStorage>();

				Iterator blIt = doc.getDXFBlockIterator();
				List<DXFBlock> skipBloks= new LinkedList<DXFBlock>(); //пропущенные блоки
				int prevSkipBlCount=-1; //кол-во пропущенных блоков на предедущем шаге
				do
				{
					skipBloks.clear();
					while (blIt.hasNext())
					{
						DXFBlock bl = (DXFBlock) blIt.next();
						String blname = bl.getName();

						List<String> l_ordlrnames = new LinkedList<String>();//Упорядоченый список имен входных слоев для сохранения порядка рисования
						Map<String, IXMLObjectDesc> l_name2Ruledesc = new HashMap<String, IXMLObjectDesc>();

						DXFLayer layer = doc.getDXFLayer(bl.getLayerID());
						String lrlinetype=layer.getLineType();





						blname=blname.replace("*","0x2A");
						String storname = 	blname + "_"+lrlinetype;
						IEditableStorage storage = createStorage(storname, l_name2storage, l_ordlrnames);

						int lw = layer.getLineWeight();

						String scolor=DXFColor.getRGBString(layer.getColor());
						String[] srgb=scolor.split(",");
						int lcolor=Integer.parseInt(srgb[0])<<16;
						lcolor|=Integer.parseInt(srgb[1])<<8;
						lcolor|=Integer.parseInt(srgb[2]);
						lcolor|=0xFF000000;

		//					List<IParam> iParams = ruledesc.getParams();
		//					iParams.add(new DefAttrImpl(KernelConst.ATTR_COLOR_LINE,));
		//					fname2val.put(KernelConst.ATTR_COLOR_FILL,0xff0000FF);
		//					ruledesc.setParams(iParams);
		//					fname2val.put(KernelConst.ATTR_COLOR_LINE,0xff00ff00);
		//					fname2val.put(KernelConst.ATTR_COLOR_FILL,0xff0000FF);

						IXMLObjectDesc ruledesc=null;
						if (lrlinetype.equalsIgnoreCase(DXFConstants.ENTITY_TYPE_TEXT))
							ruledesc = createTemplateDescByName(bcontext, KernelConst.RULE_TAGNAME, textrule);
						else
						{
							ruledesc = createTemplateDescByName(bcontext, KernelConst.RULE_TAGNAME, drawrule);
							IParam pcfill = ruledesc.getParamByName(CommonStyle.COLOR_FILL);
							pcfill.setValue(Integer.toHexString(lcolor));
						}

						IParam pcline = ruledesc.getParamByName(CommonStyle.COLOR_LINE);
						pcline.setValue(Integer.toHexString(lcolor));
						ruledesc.setObjname("R_" + storname);
						try
						{
							inMBB = null;
							//Обработать все объекты блока
							inMBB = processEntities(inMBB, bl.getDXFEntitiesIterator(),lrlinetype,ruledesc,storage);
							if (inMBB!=null) //Если есть объекты содержащие геометрию сохранить определение правила рисования
								l_name2Ruledesc.put(storname,ruledesc);
							else //Нет геометрии - нет хранилища геометрии и правил для ее рисования
								l_name2storage.remove(storname);
						}
						catch (Exception e)
						{ //Блок еще не определен, так что
							skipBloks.add(bl); // добавить их в пропущенные блоки
							l_name2storage.remove(storname); // удалить из определения хранилищ хранилище неопределенного блока
							continue;
						}

						if (inMBB!=null)
						{//Блок содержит геометрию, создадим проект что бы можно было рисовать блок
							StringBuffer sbuffer=new StringBuffer();
							StringBuffer storages = new StringBuffer();
							StringBuffer filters = new StringBuffer();
							StringBuffer rules = new StringBuffer(); //Строка правил
							StringBuffer layers = new StringBuffer(); //Строка слоев

							//Генерация сокращенного хранилища для блоков (состоящие из одного слоя)
							IXMLObjectDesc mainstor = createTemplateDescByName(bcontext, KernelConst.STORAGE_TAGNAME, "T_STOR");
							mainstor.setObjname(storname);
							mainstor.getParams().add(new DefAttrImpl(null, pdir));
							storages.append(mainstor.getXMLDescriptor("\t"));

							List<IXMLObjectDesc> layerdescriptors =desc2XMLDesc(bcontext, l_ordlrnames, mainstor, l_name2Ruledesc, storages, filters, rules, layers);
							IXMLObjectDesc projcntx = generateProjXML(bcontext, layerdescriptors, sbuffer, storages, filters, rules, layers,"P_"+blname, mainstor);
							name2projblock.put(blname,new Pair<StringBuffer,Pair<IXMLObjectDesc,MRect>>(sbuffer,
									new Pair<IXMLObjectDesc,MRect>(projcntx,inMBB)));
						}
						else
							name2projblock.put(blname,new Pair<StringBuffer,Pair<IXMLObjectDesc,MRect>>(null,null));//Блок не содержит геометрии
					}
					blIt=new LinkedList<DXFBlock>(skipBloks).iterator();

					if (prevSkipBlCount<0)
						prevSkipBlCount=skipBloks.size();
					else if (prevSkipBlCount<=skipBloks.size())
						throw new Exception("Can't reduce undefined blocks in input file");
				}
				while (skipBloks.size()>0);

				for (IEditableStorage iEditableStorage : l_name2storage.values())
					iEditableStorage.commit();
			}

//			Iterator itv = doc.getDXFHeader().getVarialbeIterator();
//			while (itv.hasNext())
//			{
//				DXFVariable v = (DXFVariable) itv.next();
//				Iterator ikv = v.getValueKeyIterator();
//				int cnt=0;
//				while (ikv.hasNext())
//				{
//
//					cnt++;
//					Object o = ikv.next();
//					//System.out.println("o = " + o);
//				}
//
//				if (cnt>1)
//					System.out.println(""+v.getName());
//
//			}

			//Генерация хранилищ (Получить слои)
			inMBB= null;
			Iterator layerit = doc.getDXFLayerIterator();
			while (layerit.hasNext())
			{
				DXFLayer layer = (DXFLayer) layerit.next();
				String linetypename=layer.getLineType();

				Iterator eit = layer.getDXFEntityTypeIterator(); //по всем типам объектов, которые содержат геометрию
				while (eit.hasNext())
				{
					String type = (String) eit.next();
					List entities = layer.getDXFEntities(type);
					//Генерация хранилища

					String storname = 	layer.getName() + "_" + type;
					IEditableStorage storage = createStorage(storname, name2storage, ordlrnames);

					int lw = layer.getLineWeight();

					String scolor=DXFColor.getRGBString(layer.getColor());
					String[] srgb=scolor.split(",");
					int lcolor=Integer.parseInt(srgb[0])<<16;
					lcolor|=Integer.parseInt(srgb[1])<<8;
					lcolor|=Integer.parseInt(srgb[2]);
					lcolor|=0xFF000000;

//					List<IParam> iParams = ruledesc.getParams();
//					iParams.add(new DefAttrImpl(KernelConst.ATTR_COLOR_LINE,));
//					fname2val.put(KernelConst.ATTR_COLOR_FILL,0xff0000FF);
//					ruledesc.setParams(iParams);
//					fname2val.put(KernelConst.ATTR_COLOR_LINE,0xff00ff00);
//					fname2val.put(KernelConst.ATTR_COLOR_FILL,0xff0000FF);

					IXMLObjectDesc ruledesc=null;
					if (type.equalsIgnoreCase(DXFConstants.ENTITY_TYPE_TEXT))
						ruledesc = createTemplateDescByName(bcontext, KernelConst.RULE_TAGNAME, textrule);
					else
					{
						ruledesc = createTemplateDescByName(bcontext, KernelConst.RULE_TAGNAME, drawrule);
						IParam pcfill = ruledesc.getParamByName(CommonStyle.COLOR_FILL);
						pcfill.setValue(Integer.toHexString(lcolor));
					}
					IParam pcline = ruledesc.getParamByName(CommonStyle.COLOR_LINE);
					pcline.setValue(Integer.toHexString(lcolor));

					DXFLineType dxfLineType = doc.getDXFLineType(linetypename);

					ruledesc.setObjname("R_" + storname);

					if (dxfLineType!=null)
					{
						double [] dbl=dxfLineType.getPattern();
						if (dbl!=null && dbl.length>0)
						{
							String dashes="";
							for (int i = 0; i < dbl.length; i++)
							{
								if (i>0)
									dashes += CnStyleRuleImpl.FLOATSEP;
								if (dbl[i]>=0)
									dashes += new Float(dbl[i]*gscale);
								else
									dashes += new Float(-dbl[i]*gscale);

							}
							List<IParam> params = ruledesc.getParams();
							params.add(new DefAttrImpl(CommonStyle.DASH_ARRAY,dashes));
						}
					}
					name2Ruledesc.put(storname,ruledesc);
					//Обработка объектов слоя
					inMBB = processEntities(inMBB, entities.iterator(),type,ruledesc,storage);
				}
			}

//------END-------------Разбор входного файла генерация хранилищь правил слоев и фильтров ---------------------------//

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

//---------------- Сохранить в буфер блоки используемые для отображения ----------------------------------------------//
			for (Pair<StringBuffer, Pair<IXMLObjectDesc,MRect>> block : name2projblock.values())
				if (block.first!=null)
					sbuffer.append(block.first);
//-END------------ Сохранить в буфер блоки используемые для отображения ----------------------------------------------//
			StringBuffer storages = new StringBuffer();
			StringBuffer filters = new StringBuffer();
			StringBuffer rules = new StringBuffer(); //Строка правил
			StringBuffer layers = new StringBuffer(); //Строка слоев

			IXMLObjectDesc mainstor = null;
			if (ordlrnames.size()==1)
			{
				String storname= ordlrnames.iterator().next();
				mainstor = createTemplateDescByName(bcontext, KernelConst.STORAGE_TAGNAME, "T_STOR");
				mainstor.setObjname(storname);
				mainstor.getParams().add(new DefAttrImpl(null, pdir));
				storages.append(mainstor.getXMLDescriptor("\t"));
			}
			else
			    mainstor = createTemplateDescByName(bcontext, KernelConst.STORAGE_TAGNAME, "MAIN_STORAGE");
			List<IXMLObjectDesc> layerdescriptors =desc2XMLDesc(bcontext, ordlrnames, mainstor, name2Ruledesc, storages, filters, rules, layers);
			for (IEditableStorage iEditableStorage :name2storage.values())
				iEditableStorage.commit();
//--------------------------------------- Инициализация конвертеров --------------------------------------------------//
			{
				IXMLObjectDesc rotdesc = createTemplateDescByName(bcontext, KernelConst.CONVERTER_TAGNAME, "rot0");
				ILinearConverter rotconv = (ILinearConverter) bcontext.getBuilderByTagName(rotdesc.getTagname()).initByDescriptor(rotdesc, null);
				scaleBpoint = convGenerator.calcScale_BPoint(inMBB, rotconv, viewSz);

				IXMLObjectDesc scaledesc = createTemplateDescByName(bcontext, KernelConst.CONVERTER_TAGNAME, "scale1");
				scaledesc.getParams().add(new DefAttrImpl("initscale", scaleBpoint[0].x + " " + scaleBpoint[0].y));

				IXMLObjectDesc shiftdesc = createTemplateDescByName(bcontext, KernelConst.CONVERTER_TAGNAME, "shift2");
				shiftdesc.getParams().add(new DefAttrImpl("bindp", scaleBpoint[1].x + " " + scaleBpoint[1].y));

				sbuffer.append("<").append(KernelConst.CONVERTER_TAGNAME).append("s>\n");
				sbuffer.append(rotdesc.getXMLDescriptor("\t"));
				sbuffer.append(scaledesc.getXMLDescriptor("\t"));
				sbuffer.append(shiftdesc.getXMLDescriptor("\t"));

				String servConv = pname + "_SERVCONV";
				servcnvdesc = createTemplateDescByName(bcontext, KernelConst.CONVERTER_TAGNAME, "T_SERVCONV");
				servcnvdesc.setObjname(servConv);
				sbuffer.append(servcnvdesc.getXMLDescriptor("\t"));

				String projConv = pname + "_PROJCONV";
				projconvdesc = createTemplateDescByName(bcontext, KernelConst.CONVERTER_TAGNAME, "T_PROJCONV");
				projconvdesc.setObjname(projConv);
				sbuffer.append(projconvdesc.getXMLDescriptor("\t"));

				sbuffer.append("</").append(KernelConst.CONVERTER_TAGNAME).append("s>\n");
			}
//--------------------------------------------------------------------------------------------------------------------//

			storages.append(addstorages);
			filters.append(addfilters);
			rules.append(addrules);
			layers.append(addlayers);
			layerdescriptors.addAll(addlayersdesc);

			projcntx=generateProjXML(bcontext, layerdescriptors, sbuffer, storages, filters, rules, layers,pname, mainstor);
			generateSuffix(sbuffer);
//Закрываем файл
			sbuffer.append(prefixsuffix.second);
			PrintWriter pwr = new PrintWriter(pdir + "/" + projFName, getEncoding());
			pwr.println(sbuffer.toString());
			pwr.flush();
			pwr.close();
		}
		finally
		{
			System.out.println("transalte totaltime:" + (System.currentTimeMillis() - starttime));
		}


	}

	private IXMLObjectDesc generateProjXML
			(
					IXMLBuilderContext bcontext, List<IXMLObjectDesc> layerdescriptors,
					StringBuffer sbuffer,
					StringBuffer storages,
					StringBuffer filters,
					StringBuffer rules,
					StringBuffer layers,
					String pname,
					IXMLObjectDesc mainstor)
			throws Exception
	{

		if (storages.length()>0)
		{//Хранилища
			sbuffer.append("<").append(KernelConst.STORAGE_TAGNAME).append("s>\n");
			sbuffer.append(storages);
			sbuffer.append("</").append(KernelConst.STORAGE_TAGNAME).append("s>\n");
		}

		if (filters.length()>0)
		{//Фильтры
			sbuffer.append("<").append(KernelConst.FILTER_TAGNAME).append("s>\n");
			sbuffer.append(filters);
			sbuffer.append("</").append(KernelConst.FILTER_TAGNAME).append("s>\n");
		}

		if (rules.length()>0)
		{//Правила
			sbuffer.append("<").append(KernelConst.RULE_TAGNAME).append("s>\n");
			sbuffer.append(rules);
			sbuffer.append("</").append(KernelConst.RULE_TAGNAME).append("s>\n");
		}

		if (layers.length()>0)
		{//Слои
			sbuffer.append("<").append(KernelConst.LAYER_TAGNAME).append("s>\n");
			sbuffer.append(layers);
			sbuffer.append("</").append(KernelConst.LAYER_TAGNAME).append("s>\n");
		}

		{
			IXMLObjectDesc projcntx = createTemplateDescByName(bcontext, KernelConst.PROJCTXT_TAGNAME, "T_CTXT");
			sbuffer.append("<").append(KernelConst.PROJCTXT_TAGNAME).append("s>\n");
			projcntx.setObjname(pname);
			List<IParam> lprojcntx = projcntx.getParams();
			lprojcntx.add(new DefAttrImpl(null, metadesc));
			lprojcntx.add(new DefAttrImpl(null, mainstor));

			for (IXMLObjectDesc desc : layerdescriptors)
				lprojcntx.add(new DefAttrImpl(null, desc));

			sbuffer.append(projcntx.getXMLDescriptor("\t"));
			sbuffer.append("</").append(KernelConst.PROJCTXT_TAGNAME).append("s>\n");

			return projcntx;
		}
	}

	private List<IXMLObjectDesc> desc2XMLDesc(IXMLBuilderContext bcontext, Collection<String> ordlrnames, IXMLObjectDesc mainstor, Map<String, IXMLObjectDesc> name2Ruledesc, StringBuffer storages, StringBuffer filters, StringBuffer rules, StringBuffer layers)
			throws Exception
	{
		//Описатель главного хранилища
		List<IXMLObjectDesc> layerdescriptors = new LinkedList<IXMLObjectDesc>(); //Список дескрипторов слоев в порядке поступления из исходного файла
		int sz = ordlrnames.size();
		if (sz >1)
		{
			for (String storname : ordlrnames)
			{
				//Генерация хранилища
				IXMLObjectDesc stordesc = createTemplateDescByName(bcontext, KernelConst.STORAGE_TAGNAME, "T_STOR");
				stordesc.setObjname(storname);
				stordesc.getParams().add(new DefAttrImpl(null, pdir));
				storages.append(stordesc.getXMLDescriptor("\t"));
				mainstor.getParams().add(new DefAttrImpl(null, stordesc));

			//Генерация фильтра
				IXMLObjectDesc filterdesc = createTemplateDescByName(bcontext, KernelConst.FILTER_TAGNAME, "T_STORFILTER");
				filterdesc.setObjname("F_" + storname);
				filterdesc.getParams().add(new DefAttrImpl(null, storname));
				filters.append(filterdesc.getXMLDescriptor("\t"));

				//Генерация правил согласно схеме отображения
				IXMLObjectDesc ruledesc = name2Ruledesc.get(storname);
				rules.append(ruledesc.getXMLDescriptor("\t"));

				//Генерация слоев
				IXMLObjectDesc layerdesc = createTemplateDescByName(bcontext, KernelConst.LAYER_TAGNAME, "T_LAYER");
				layerdesc.setObjname("L_" + storname);
				List<IParam> lparam = layerdesc.getParams();
				lparam.add(new DefAttrImpl(null, ruledesc));
				lparam.add(new DefAttrImpl(null, filterdesc));
				lparam.add(new DefAttrImpl(null, mainstor));

				layerdescriptors.add(layerdesc);
				layers.append(layerdesc.getXMLDescriptor("\t"));
			}
			storages.append(mainstor.getXMLDescriptor("\t"));
		}
		else if (sz==1)
		{
			String storname=ordlrnames.iterator().next();

			IXMLObjectDesc ruledesc = name2Ruledesc.get(storname);
			rules.append(ruledesc.getXMLDescriptor("\t"));

			//Генерация слоев напрямую ссылающееся на хранилище (т.е. передается уже готовое хранилище)
			IXMLObjectDesc layerdesc = createTemplateDescByName(bcontext, KernelConst.LAYER_TAGNAME, "T_LAYER");
			layerdesc.setObjname("L_" + storname);
			List<IParam> lparam = layerdesc.getParams();
			lparam.add(new DefAttrImpl(null, ruledesc));
			lparam.add(new DefAttrImpl(null, mainstor));

			layerdescriptors.add(layerdesc);
			layers.append(layerdesc.getXMLDescriptor("\t"));
		}

		return layerdescriptors;
	}

	private IEditableStorage createStorage(String storname, Map<String, IEditableStorage> name2storage, List<String> ordlrnames)
			throws Exception
	{
		IEditableStorage storage = name2storage.get(storname);
		if (storage == null)
		{
			name2storage.put(storname, storage = new MemEditableStorageLr(pdir, storname));
			ordlrnames.add(storname);
		}
		else
			System.out.println("Dublicate storname  = " + storname + " !?");
		return storage;
	}

	/**
	 * Обработать входящие геометрические объекты
	 * @param inMBB - минимальный обрамляющий прямоугольник полученый на предыдущих итерациях
	 * @param entities - мно-во сущностей
	 * @param type - тип сущностей (????)
	 * @param ruledesc - описание правила для отображенея сущностей
	 * @param storage - хранилище сущностей
	 * @return - обрамляющий прямоугольник который включает все обработанные сущности
	 * @throws Exception -
	 */
	private MRect processEntities(MRect inMBB,Iterator entities,
								  String type,IXMLObjectDesc ruledesc,IEditableStorage storage)
			throws Exception
	{

		if (!entities.hasNext())
			return null;

		int objCounter=0;//Счетчик созданных объектов
		boolean usebound=true;//Вычислять обрамляющий прямоугольник
		Set<String> bIdsInRule=new HashSet<String>();//Блоки, проекты которых были добавлены в правило рисования ruledesc
/*
Это свзяано с тем что правило рисования инициализирует паинтеры блоков и инсертов их проектами для того что
бы прорисовать соответсвующий блок или инсерт.
 */


		while (entities.hasNext())
		{
			DXFEntity ent = (DXFEntity) entities.next();
			Bounds bnd = ent.getBounds();

			IEditableGisObject est = null;
			Map<String, Object> objAname2val = new HashMap<String, Object>();

			int color = ent.getColor();
			byte[] bt = ent.getColorRGB();
			int w = ent.getLineWeight();
			double th = ent.getThickness();

			String linetype = ent.getLineType(); //!!!TODO Генерировать аттрибут только тогда,
			//TODO когда он не пустой и отличается от аттрибута слоя!!!

			String enttype = ent.getType();
			if (!type.equals(enttype))
				System.out.println("Alert in type = " + type + " " + " entity type = " + enttype);

			if (enttype.equals(DXFConstants.ENTITY_TYPE_LINE))
			{
				DXFLine dxfLine = (DXFLine) ent;
				org.kabeja.dxf.helpers.Point p0 = dxfLine.getStartPoint();
				org.kabeja.dxf.helpers.Point p1 = dxfLine.getEndPoint();
				est = storage.createObject(KernelConst.LINESTRING);
				est.addSegment(0, new MPoint[]{new MPoint(p0.getX(), p0.getY()), new MPoint(p1.getX(), p1.getY())});
			}
			else if (enttype.equals(DXFConstants.ENTITY_TYPE_POINT))
			{

				DXFPoint dxfPoint = (DXFPoint) ent;
				org.kabeja.dxf.helpers.Point p0 = dxfPoint.getPoint();
				est = storage.createObject(KernelConst.POINT);
				est.addSegment(0, new MPoint[]{new MPoint(p0.getX(), p0.getY())});
			}
			else if (
					enttype.equals(DXFConstants.ENTITY_TYPE_POLYLINE)  ||
					enttype.equals(DXFConstants.ENTITY_TYPE_LWPOLYLINE)
					)
			{
				DXFPolyline dxfPoly = (DXFPolyline) ent;
				est = processPolyLine(null,storage, dxfPoly);
			}
			else if (
					enttype.equals(DXFConstants.ENTITY_TYPE_CIRCLE))
			{
//				DXFCircle crcl=(DXFCircle)ent;
//							org.kabeja.dxf.helpers.Point pt = crcl.getCenterPoint();
//							double rad=crcl.getRadius();
				double minX = bnd.getMinimumX();
				double minY = bnd.getMinimumY();
				double maxX = bnd.getMaximumX();
				double maxY = bnd.getMaximumY();
				est = storage.createObject(KernelConst.ELLIPS);
				est.addSegment(0, new MPoint[]{new MPoint(minX, minY), new MPoint(maxX, minY), new MPoint(maxX, maxY), new MPoint(minX, maxY)});
			}
			else if (
					enttype.equals(DXFConstants.ENTITY_TYPE_ELLIPSE))
			{ //TODO Учитывать что эллипс может быть не замкнут
				DXFEllipse ellips=(DXFEllipse)ent; //TODO
				org.kabeja.dxf.helpers.Point p=ellips.getCenterPoint();
				double ew=ellips.getHalfMajorAxisLength();
				double eh=ew*ellips.getRatio();

				// Здесь брать параметры элипса, изменить расовальщик элипса, для того что сделать возможным не замыкать параметры
				double affineRot =Math.toDegrees(ellips.getRotationAngle());
				if (affineRot!=0.0)
					objAname2val.put(DefPointPainter.ATTR_ROT,affineRot);

				double minX = p.getX()-ew;
				double minY = p.getY()-eh;

				double maxX = p.getX()+ew;
				double maxY = p.getY()+eh;

				double par=ellips.getStartParameter();
				double epar=ellips.getEndParameter();

				if (par==0 && epar==2*Math.PI)
				{
					est = storage.createObject(KernelConst.ELLIPS);
					est.addSegment(0, new MPoint[]{new MPoint(minX, minY), new MPoint(maxX, minY), new MPoint(maxX, maxY), new MPoint(minX, maxY)});
				}
				else
				{
					//TODO При совпадении двух точек (конечной и начальной он не рисует, эту проблему надо решать видимо просто заменой элипса????, а скорее всего добавлением
					//TODO  аттррибута ????!)
					final MPoint center = new MPoint(p.getX(), p.getY());

					final MPoint sp = new MPoint(center.getX()+ew * Math.cos(par),center.getY()+eh * Math.sin(par));
					final MPoint ep = new MPoint(center.getX()+ew * Math.cos(epar),center.getY()+eh * Math.sin(epar));

					est = storage.createObject(DxfCnStyleRule2.ARC);
					est.addSegment(0, new MPoint[]{
							new MPoint(minX, minY), new MPoint(maxX, minY), new MPoint(maxX, maxY), new MPoint(minX, maxY),
							//Ограничивающий четырех угольник
							center,//Центр
							sp, //Точка старта дуги
							ep,//Точка стопа дуги
					});
				}
			}
			else if (enttype.equals(DXFConstants.ENTITY_TYPE_TEXT))
			{
				DXFText tent = (DXFText) ent;
				String text = tent.getText();
				double scalex = tent.getScaleX();
				String style = tent.getTextStyle();
				double txtRot=tent.getRotation();
				if (txtRot!=0.0)
					objAname2val.put(DefPointPainter.ATTR_ROT,txtRot);

				org.kabeja.dxf.helpers.Point pt = tent.getAlignmentPoint();

				double h = tent.getHeight();
				org.kabeja.dxf.helpers.Point p = tent.getInsertPoint();

				IParam asAttrName = ruledesc.getParamByName(KernelConst.USE_AS_ATTRIBUTENAME);
				if (asAttrName != null && asAttrName.getValue() != null)
					objAname2val.put(asAttrName.getValue().toString(), text);
				objAname2val.put(CnStyleRuleImpl.FONT_SIZE, h);

				est = storage.createObject(KernelConst.POINT_TEXT);
				est.addSegment(0, new MPoint[]{new MPoint(p.getX(), p.getY())});
			}
			else if (enttype.equals(DXFConstants.ENTITY_TYPE_SOLID))
			{
				DXFSolid sol = (DXFSolid) ent;
				org.kabeja.dxf.helpers.Point[] ps = {sol.getPoint1(), sol.getPoint2(), sol.getPoint3(), sol.getPoint4()};

				List<MPoint> seg = new LinkedList<MPoint>();
				for (org.kabeja.dxf.helpers.Point p : ps)
					seg.add(new MPoint(p.getX(), p.getY()));


				if (ps[3].getX() == ps[2].getX() && ps[3].getY() == ps[2].getY())
					seg.get(3).setXY(seg.get(0));
				else
				{
					seg.add(seg.get(0));
				}

				est = storage.createObject(KernelConst.LINEARRING);
				est.addSegment(0, seg.toArray(new MPoint[seg.size()]));
//							objAname2val.put(KernelConst.ATTR_COLOR_LINE,0xff00ff00);
//							objAname2val.put(KernelConst.ATTR_COLOR_FILL,0xff0000FF);
			}
			else if (enttype.equals(DXFConstants.ENTITY_TYPE_DIMENSION))
			{
				//http://www.cadtutor.net/tutorials/autocad/dimensioning.php
				DXFDimension dim = (DXFDimension) ent;

				String bId = dim.getDimensionBlock();
				bId = bId.replace("*","0x2A");

				Pair<StringBuffer, Pair<IXMLObjectDesc,MRect>> projBlock = name2projblock.get(bId);
				if (projBlock!=null )
				{
//					if (bId.contains("D84"))
					{
						est = storage.createObject(DxfCnStyleRule2.DIMENTION);
						double dimRot = dim.getRotate();
						org.kabeja.dxf.helpers.Point inspt=dim.getInsertPoint();
						if (projBlock.first!=null && projBlock.second!=null)
						{

							DXFBlock block = dim.getDXFDocument().getDXFBlock(dim.getDimensionBlock());
							org.kabeja.dxf.helpers.Point referencePoint = block.getReferencePoint();
							//Пополнить аттрибуты объекта типа Dimention для рисования блока, который ему принадлежит
							objAname2val.put(InsertPainter2.ATTR_PCTXNAME,"P_"+ bId); // ссылка на проект описывающий блок

							objAname2val.put(DefPointPainter.ATTR_SHX,inspt.getX()+referencePoint.getX()); // точка куда где надо рисовать блок по X
							objAname2val.put(DefPointPainter.ATTR_SHY,inspt.getY()+referencePoint.getY()); // точка куда где надо рисовать блок по Y
							if (dimRot!=0.0)
								objAname2val.put(DefPointPainter.ATTR_ROT,dimRot); // угол поворота блока
							//Только тогда у нас блок содержит геометрию и мы добвляем ссылку на его проект в правило рисования
							MRect blmbb=projBlock.second.second;
							//TODO Сдеалть еще поворот для корректного определения границ объекта
							//TODO Учитывать еще обрамляющий прямоугольник текста
							blmbb=new MRect(new MPoint(blmbb.p1.x+inspt.getX(), blmbb.p1.y+inspt.getY()), new MPoint(blmbb.p4.x+inspt.getX(), blmbb.p4.y+inspt.getY()));
		//Добавляем в нулевой сегмент границы блока, рисовать же блок будем через ссылку на проект блока
							est.addSegment(0, new MPoint[]{new MPoint(blmbb.p1),new MPoint(blmbb.p4)});
							inMBB = new MRect(blmbb.p1, blmbb.p4).getMBB(inMBB);
							usebound = false;
							if (!bIdsInRule.contains(bId))
							{
								bIdsInRule.add(bId);
								List<IParam> lruleparams = ruledesc.getParams();
								lruleparams.add(new DefAttrImpl(null, projBlock.second.first));
							}
						}
						else
							est.addSegment(0, new MPoint[]{});
					}
				}
				else
					throw new Exception("Skip the project for bId:"+ bId);
			}
			else if (enttype.equals(DXFConstants.ENTITY_TYPE_INSERT))
			{
				DXFInsert inst = (DXFInsert) ent;
				String bid = inst.getBlockID();
				bid=bid.replace("*","0x2A");

				//Вычислить  блок в проектных координатах
				Pair<StringBuffer, Pair<IXMLObjectDesc,MRect>> projblock = name2projblock.get(bid);
				if (projblock!=null)
				{
					double sx=inst.getScaleX();
					double sy = inst.getScaleY();
					double rt = inst.getRotate();
					org.kabeja.dxf.helpers.Point pt = inst.getPoint();
					est = storage.createObject(DxfCnStyleRule2.INSERT);
					objAname2val.put(InsertPainter2.ATTR_PCTXNAME,"P_"+bid);

					objAname2val.put(DefPointPainter.ATTR_SHX,pt.getX());
					objAname2val.put(DefPointPainter.ATTR_SHY,pt.getY());

					if (rt!=0.0)
						objAname2val.put(DefPointPainter.ATTR_ROT,rt);
					if (sx!=1)
						objAname2val.put(DefPointPainter.ATTR_SCX,sx);
					if (sy!=1)
						objAname2val.put(DefPointPainter.ATTR_SCY,sy);

	//				est.addSegment(0, new MPoint[]{new MPoint(pt.getX(),pt.getY())});


					if (projblock.first!=null && projblock.second!=null)
					{ //Только тогда у нас блок содержит геометрию и мы добвляем ссылку на его проект в правило рисования
						MRect blmbb=projblock.second.second;
						//TODO Сделать еще поворот и масштабирование
						blmbb=new MRect(new MPoint(blmbb.p1.x+pt.getX(), blmbb.p1.y+pt.getY()), new MPoint(blmbb.p4.x+pt.getX(), blmbb.p4.y+pt.getY()));
						est.addSegment(0, new MPoint[]{new MPoint(blmbb.p1),new MPoint(blmbb.p4)});
						inMBB = new MRect(blmbb.p1, blmbb.p4).getMBB(inMBB);
						usebound = false;
						if (!bIdsInRule.contains(bid))
						{
							bIdsInRule.add(bid);
							List<IParam> lruleparams = ruledesc.getParams();
							lruleparams.add(new DefAttrImpl(null, projblock.second.first));
						}
					}
				}
				else
					throw new Exception("Skip the project for bid:"+bid);
			}
//			else if (enttype.equals(DXFConstants.ENTITY_TYPE_VIEWPORT))
//			{
//				DXFViewport vp = (DXFViewport) ent;
//				double ar = vp.getAspectRatio();
//				double cz = vp.getCircleZoom();
//				Bounds b = vp.getBounds();
//				double min = b.getMaximumX();
//
//			}
			else if (enttype.equals(DXFConstants.ENTITY_TYPE_ARC))
			{
				DXFArc arc = (DXFArc) ent;
				org.kabeja.dxf.helpers.Point p = arc.getCenterPoint();
				double r=arc.getRadius();
				org.kabeja.dxf.helpers.Point sp;
				org.kabeja.dxf.helpers.Point ep;
				boolean cn=arc.isCounterClockwise();
				if (cn)
				{
					ep = arc.getStartPoint();
					sp = arc.getEndPoint();
				}
				else
				{
					sp = arc.getStartPoint();
					ep = arc.getEndPoint();
				}

				est = storage.createObject(DxfCnStyleRule2.ARC);

				MPoint lup = new MPoint(p.getX() - r, p.getY() - r);
				MPoint rdwn = new MPoint(p.getX() + r, p.getY() + r);


				est.addSegment(0, new MPoint[]{
						lup,
						new MPoint(rdwn.getX(),lup.getY()),
						rdwn,
						new MPoint(lup.getX(),rdwn.getY()), //Ограничивающий четырех угольник
						new MPoint(p.getX(), p.getY()),//Центр
						new MPoint(sp.getX(), sp.getY()), //Точка старта дуги
						new MPoint(ep.getX(), ep.getY()),//Точка стопа дуги
				});
			}
			else if (enttype.equals(DXFConstants.ENTITY_TYPE_HATCH))
			{

				DXFHatch hatch = (DXFHatch) ent;

				IXMLObjectDesc hatchstordesc = createTemplateDescByName(bcontext, KernelConst.STORAGE_TAGNAME, "T_STOR");
				String storname = "HATCH_" + ent.getID();
				hatchstordesc.setObjname(storname);
				hatchstordesc.getParams().add(new DefAttrImpl(null, pdir));
				addstorages.append(hatchstordesc.getXMLDescriptor("\t"));

//				IXMLObjectDesc hatchruledesc = createTemplateDescByName(bcontext, KernelConst.RULE_TAGNAME, drawrule);
//				IParam hatchpcfill = hatchruledesc.getParamByName(CommonStyle.COLOR_FILL);
//				IParam pcfill = ruledesc.getParamByName(CommonStyle.COLOR_FILL);
//				hatchpcfill.setValue(pcfill.getValue());
//
//
//				IParam hatchpcline = hatchruledesc.getParamByName(CommonStyle.COLOR_LINE);
//				IParam pcline = ruledesc.getParamByName(CommonStyle.COLOR_LINE);
//				hatchpcline.setValue(pcline.getValue());
//				hatchruledesc.setObjname("R_" + storname);
//				addrules.append(hatchruledesc.getXMLDescriptor("\t"));


				IXMLObjectDesc hatchselfilter = createTemplateDescByName(bcontext, KernelConst.FILTER_TAGNAME, "T_SELFILTER");
				hatchselfilter.setObjname("F_" + storname);
				IParam isnegateparam = hatchselfilter.getParamByName("ISNEGATE");
				isnegateparam.setValue("TRUE");
				hatchselfilter.getParams().add(new DefAttrImpl(null, storname));

				//Генерация слоя геометрии штриховки
				IXMLObjectDesc layerdesc = createTemplateDescByName(bcontext, KernelConst.LAYER_TAGNAME, "T_LAYER");
				{
					layerdesc.setObjname("L_" + storname);
					List<IParam> lparam = layerdesc.getParams();
					lparam.add(new DefAttrImpl(null, ruledesc));
					lparam.add(new DefAttrImpl(null, hatchselfilter));
					lparam.add(new DefAttrImpl(null, hatchstordesc));
				}



//				boolean hsol=hatch.isSolid();
				int hstyle=hatch.getHatchStyle();

				IEditableStorage hatchStorage = createStorage(storname, new HashMap(),new LinkedList());
				Iterator boundIt = hatch.getBoundaryLoops();
				int boundryLoopIx=0;
				while (boundIt.hasNext())
				{
					HatchBoundaryLoop boundaryLoop = (HatchBoundaryLoop)boundIt.next();

					boolean om=boundaryLoop.isOutermost(); //TODO Что же делать с этим флагом на этапе отображения!!!?????

					hatchStorage.commit();
					//Обоработка объектов границы штриховки
					processEntities(inMBB,boundaryLoop.getBoundaryEdgesIterator(),type,null,hatchStorage);
					{
						//Генерация фильтра для слоя описывающего границу
						IXMLObjectDesc boundselfilter = createTemplateDescByName(bcontext, KernelConst.FILTER_TAGNAME, "T_SELFILTER");
						boundselfilter.setObjname("F_BOUND_"+boundryLoopIx+"_"+ storname);
						boundselfilter.getParams().add(new DefAttrImpl(null, storname));

						//Генерация слоя границы
						IXMLObjectDesc layerbounddesc = createTemplateDescByName(bcontext, KernelConst.LAYER_TAGNAME, "T_BOUND");
						{
							layerbounddesc.setObjname("L_BOUND_"+boundryLoopIx+"_"+storname);
							List<IParam> lparam = layerbounddesc.getParams();
							lparam.add(new DefAttrImpl(null, ruledesc));
							lparam.add(new DefAttrImpl(null, boundselfilter));
							lparam.add(new DefAttrImpl(null, hatchstordesc));
						}
						//Добавить слой в слои границ
						List<IParam> lparam = layerdesc.getParams();
						lparam.add(new DefAttrImpl(null, layerbounddesc));

						addlayers.append(layerbounddesc.getXMLDescriptor("\t"));


						//Get objects whitch descript the bound
						Set<String> bound_ids = hatchStorage.getNotCommited();
						for (String b_id : bound_ids)
							boundselfilter.getParams().add(new DefAttrImpl("ID",b_id));
						addfilters.append(boundselfilter.getXMLDescriptor("\t"));
					}

					boundryLoopIx++;
				}

				addlayers.append(layerdesc.getXMLDescriptor("\t"));
				addlayersdesc.add(layerdesc);

				//Генерация фильтра для штрихов
				Iterator<String> bound_ids = hatchStorage.getCurvesIds();
				while (bound_ids.hasNext())
					hatchselfilter.getParams().add(new DefAttrImpl("ID",bound_ids.next()));

				est = hatchStorage.createObject(KernelConst.LINESTRING);
				String id = hatch.getDXFHatchPatternID();
				DXFHatchPattern pattern = hatch.getDXFDocument().getDXFHatchPattern(id);
				Iterator it = pattern.getLineFamilyIterator();
				int seg=0;
				while (it.hasNext())
				{
					HatchLineFamily hatchLineFamily = (HatchLineFamily)it.next();
					Iterator li = new HatchLineIterator(hatch, hatchLineFamily);
					while (li.hasNext())
					{
						 HatchLineSegment hatchLineSegment = (HatchLineSegment) li.next();
						 boolean sol=hatchLineSegment.isSolid();
						 if (sol)
						 {
						 	org.kabeja.dxf.helpers.Point p0=hatchLineSegment.getStartPoint();
						 	org.kabeja.dxf.helpers.Point p1=hatchLineSegment.getPointAt(hatchLineSegment.getLength());
							est.addSegment(seg, new MPoint[]{new MPoint(p0.getX(),p0.getY()), new MPoint(p1.getX(),p1.getY())});
							seg++;
						 }
						 else
						 {
							 double length = 0;
							 org.kabeja.dxf.helpers.Point p0=hatchLineSegment.getStartPoint();
							 while (hatchLineSegment.hasNext())
							 {
								 double ls = hatchLineSegment.next();
								 length += Math.abs(ls);
								 org.kabeja.dxf.helpers.Point p1 = hatchLineSegment.getPointAt(length);
								 if (ls>0)
								 { //Рисуем штрих
									 est.addSegment(seg, new MPoint[]{new MPoint(p0.getX(),p0.getY()), new MPoint(p1.getX(),p1.getY())});
									 seg++;
								 }
								 else if (ls==0)
								 {  //Рисуем точку
									 double ln=hatchLineSegment.getLength()*DefPointPainter.DOTLEN;
									 org.kabeja.dxf.helpers.Point p11 = hatchLineSegment.getPointAt(length+ln);
									 est.addSegment(seg, new MPoint[]{new MPoint(p0.getX(),p0.getY()), new MPoint(p11.getX(),p11.getY())});
									 seg++;
								 }
								 p0=p1;
							 }
						 }
					}
				}
				hatchStorage.commit();
				addfilters.append(hatchselfilter.getXMLDescriptor("\t"));
			}
			else
				System.out.println("typename = " + enttype);

			if (est != null)
			{
				//Create object
				DefaultAttrsImpl objatrrs = new DefaultAttrsImpl();
				{
					for (String fname : objAname2val.keySet())
					{
						Object val = objAname2val.get(fname);
						objatrrs.put(fname, new DefAttrImpl(fname, val));
					}
					objatrrs.put(SHPConstants.ORIG_TYPE, new DefAttrImpl(SHPConstants.ORIG_TYPE, String.valueOf(enttype).trim()));
					String entID = ent.getID();
					if (entID != null && entID.length() > 0)
						objatrrs.put(attrasname, new DefAttrImpl(attrasname, entID));
					est.setCurveAttrs(objatrrs);
				}


				if (storage.getSizeNotCommited() >= 40000)
				{
					storage.commit();
					String storgename = "";
					if (storage instanceof IInitAble)
						storgename = "for storage:" + ((IInitAble) storage).getObjName();
					System.out.println("Commited " + objCounter + " records " + storgename);
				}
			}

			if (est != null)
			{
//							est.rebuildGisValume();
//							inMBB=est.getMBB(inMBB); //TODO !!!И это верно!!! Пскольку у нас различные фигуры в которых по разному
// 	 можно интерпретировать геометрию
				if (usebound)
					inMBB = new MRect(new MPoint(bnd.getMinimumX(), bnd.getMinimumY()), new MPoint(bnd.getMaximumX(), bnd.getMaximumY())).getMBB(inMBB);
			}

			if (objCounter % 5000 == 0)
				System.out.println("proceed " + objCounter + " records");
			objCounter++;
		}

		return inMBB;
	}

	private IEditableGisObject processPolyLine(IEditableGisObject est,IEditableStorage storage, DXFPolyline dxfPoly)
			throws Exception
	{
		int cols=dxfPoly.getColumns();
		if (cols!=0)
			System.out.println("cols = " + cols);

		double sw = dxfPoly.getStartWidth();
		double ew = dxfPoly.getEndWidth();
		int fl = dxfPoly.getFlags(); //TODO1 Выяснить что означают флаги, они означают в том числе как раз закрывать или не закрывать фигуру.
		if (fl!=0 && fl!=1)
		{
			System.out.println("fl = " + fl);
		}
		int vcont = dxfPoly.getVertexCount();
		List<MPoint> currentSeg = new LinkedList<MPoint>();

		for (int j = 0; j < vcont-1; j++)
		{
			DXFVertex vx1 = dxfPoly.getVertex(j);
			final MPoint p1 = new MPoint(vx1.getX(), vx1.getY());
			DXFVertex vx2 = dxfPoly.getVertex(j+1);
			final MPoint p2 = new MPoint(vx2.getX(), vx2.getY());
			double bulge=vx1.getBulge();
			if (bulge!=0.0)
			{
				est=addCurrentSeg(est, currentSeg, storage);
				est=createArcSeg(est, storage, p1, p2, bulge);
			}
			else
			{
				currentSeg.add(p1);
				currentSeg.add(p2);
			}
		}

		if (dxfPoly.isClosed())
		{

			DXFVertex vx1 = dxfPoly.getVertex(vcont-1);
			final MPoint p1 = new MPoint(vx1.getX(), vx1.getY());
			DXFVertex vx2 = dxfPoly.getVertex(0);
			final MPoint p2 = new MPoint(vx2.getX(), vx2.getY());
			double bulge=vx1.getBulge();
			if (bulge!=0.0)
			{
				est=addCurrentSeg(est, currentSeg, storage);
				est=createArcSeg(est, storage, p1, p2, bulge);
			}
			else
			{
				if (currentSeg.size()==0)
					currentSeg.add(new MPoint(vx1.getX(), vx1.getY()));
				currentSeg.add(new MPoint(vx2.getX(), vx2.getY()));
			}
		}
		est=addCurrentSeg(est, currentSeg, storage);
		return est;
	}

//	private IEditableGisObject processPolyLine(IEditableGisObject est,IEditableStorage storage, DXFPolyline dxfPoly)
//			throws Exception
//	{
//		int cols=dxfPoly.getColumns();
//		if (cols!=0)
//			System.out.println("cols = " + cols);
//
//		double sw = dxfPoly.getStartWidth();
//		double ew = dxfPoly.getEndWidth();
//		int fl = dxfPoly.getFlags(); //TODO1 Выяснить что означают флаги, они означают в том числе как раз закрывать или не закрывать фигуру.
//		if (fl!=0 && fl!=1)
//		{
//			System.out.println("fl = " + fl);
//		}
//		int vcont = dxfPoly.getVertexCount();
//		List<MPoint> points = new LinkedList<MPoint>();
//		for (int j = 0; j < vcont; j++)
//		{
//			DXFVertex vx = dxfPoly.getVertex(j);
//			double bulge=vx.getBulge();
//			if (bulge!=0.0)
//			{ //Начинаем обрабатывать вершины, что бы построить набор линий
//
//			}
//
//
//
//			points.add(new MPoint(vx.getX(), vx.getY()));
//		}
//		if (dxfPoly.isClosed() && vcont>2)
//		{
//			DXFVertex vx = dxfPoly.getVertex(0);
//			points.add(new MPoint(vx.getX(), vx.getY()));
//		}
//		if (est==null)
//			est = storage.createObject(KernelConst.LINESTRING);
//		int segs=est.getSegsNumbers();
//		est.addSegment(segs, points.toArray(new MPoint[points.size()]));
//		return est;
//	}

	private IEditableGisObject addCurrentSeg(IEditableGisObject est, List<MPoint> currentSeg, IEditableStorage storage) throws Exception
	{
		if (currentSeg.size()>0)
		{
			if (est==null || !(KernelConst.LINESTRING.equalsIgnoreCase(est.getGeotype())))
				est = storage.createObject(KernelConst.LINESTRING);
			int segs=est.getSegsNumbers();
			est.addSegment(segs, currentSeg.toArray(new MPoint[currentSeg.size()]));
			currentSeg.clear();
		}
		return est;
	}

	private IEditableGisObject createArcSeg(IEditableGisObject estArc, IEditableStorage storage, MPoint p1, MPoint p2, double _bulge)
			throws Exception
	{
		double bulge=Math.abs(_bulge);
		//Начинаем обрабатывать вершины, что бы построить набор линий
		//Получить высоту над хордой и радиус
		double dx=(p2.x-p1.x);
		double dy=(p2.y-p1.y);
		double l2=dx*dx+dy*dy;
		double l=Math.sqrt(l2);

//		double h=l/2*bulge; //высота на хордой
//		double r=l2/(8*h)+h/2;//радиус окуржности

		double r=l/(4*bulge)+l*bulge/4; //После подстановки h в формулу

		//Вычислим центр окуржности
//		double xc=p1.x+dx/2+(r-h)*dy/l;
//		double yc=p1.y+dy/2-(r-h)*dx/l;

		double xc=p1.x+dx/2+(1/(4*bulge)-bulge/4)*dy; //После подставновки r и h в формулу для xc
		double yc=p1.y+dy/2-(1/(4*bulge)-bulge/4)*dx; //После подставновки r и h в формулу для yc

		//Обрамляющий прямоугольник
		if (estArc==null || !(DxfCnStyleRule2.ARC.equalsIgnoreCase(estArc.getGeotype())))
			estArc = storage.createObject(DxfCnStyleRule2.ARC);
		estArc.addSegment(estArc.getSegsNumbers(), new MPoint[]{
				new MPoint(xc-r, yc-r), new MPoint(xc+r, yc-r), new MPoint(xc+r, yc+r), new MPoint(xc-r, yc+r),
				//Ограничивающий четырех угольник
				new MPoint(xc,yc),//Центр
				_bulge>0?p1:p2, //Точка старта дуги
				_bulge>0?p2:p1,//Точка стопа дуги
		});
		return estArc;
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
