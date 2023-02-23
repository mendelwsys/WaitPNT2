package ru.ts.toykernel.plugins.gissearch;

import ru.ts.toykernel.plugins.IGuiModule;
import ru.ts.toykernel.gui.IViewControl;
import ru.ts.toykernel.consts.INameConverter;
import ru.ts.toykernel.plugins.consts.DefNameConverter2;
import ru.ts.toykernel.plugins.ICommandBean;
import ru.ts.toykernel.plugins.IAnswerBean;
import ru.ts.toykernel.consts.KernelConst;
import ru.ts.toykernel.pcntxt.IProjContext;
import ru.ts.toykernel.storages.IBaseStorage;
import ru.ts.toykernel.storages.mem.SimpleMemStorage;
import ru.ts.toykernel.geom.IBaseGisObject;
import ru.ts.toykernel.geom.def.ObjGeomUtils;
import ru.ts.toykernel.attrs.IDefAttr;
import ru.ts.toykernel.attrs.IAttrs;
import ru.ts.toykernel.attrs.def.DefaultAttrsImpl;
import ru.ts.toykernel.attrs.def.DefAttrImpl;
import ru.ts.toykernel.shared.DefGisOperations;
import ru.ts.toykernel.drawcomp.layers.def.DrawOnlyLayer;
import ru.ts.toykernel.drawcomp.ILayer;
import ru.ts.toykernel.drawcomp.IParamPainter;
import ru.ts.toykernel.drawcomp.painters.def.DefPntImgPainter;
import ru.ts.toykernel.drawcomp.rules.def.CnStyleRuleImpl;
import ru.ts.toykernel.drawcomp.rules.def.CommonStyle;
import ru.ts.toykernel.filters.DefMBBFilter;
import ru.ts.toykernel.filters.IBaseFilter;
import ru.ts.toykernel.converters.IProjConverter;
import ru.ts.toykernel.factory.BaseInitAble;
import ru.ts.factory.IFactory;
import ru.ts.factory.IParam;
import ru.ts.utils.data.StringStorageManipulations;
import ru.ts.utils.data.Storage;
import ru.ts.gisutils.algs.common.MRect;
import ru.ts.gisutils.algs.common.MPoint;
import ru.ts.res.ImgResources;
import su.mwlib.utils.Enc;

import javax.swing.*;
import java.awt.event.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.*;
import java.util.List;
import java.lang.reflect.Field;

/**
 * Created by IntelliJ IDEA.
 * User: vladm
 * Date: 10.03.2009
 * Time: 12:39:06
 * Search gis objects and move viewport to it
 * (TODO !!! Ввести фильтр по аттрибуту имени и отдавать его в модуль !!!!)
 * (TODO имя строкового атрибута тоже должно передаваться)
 */
public class GisSearch extends BaseInitAble implements IGuiModule, IGisSearch
{

	public static final String DIALOG_TITLE = Enc.get("SEARCH_ITEM"); //TODO !!!GET MENU item form loaclization provider
	public static final String MENU_NAME_ITEM = Enc.get("SEARCH");
	public static final String ERROR_MESAGE = Enc.get("MISSING_SEARCH_ATTRIBUTE_SPECIFICATION");
	public static final String ERROR_HEADER = Enc.get("ERROR");


	public static final String MODULENAME = "GIS_SEARCH";

	public static final String SEARCH_IMAGEPICTPATH = "IMAGEPICTPATH";
	public static final String SEARCH_IMAGEPATH = "IMAGEPATH";
	public static final String SEARCH_FONT_NAME = "fontName";
	public static final String SEARCH_FONT_SIZE = "fontSize";
	public static final String SEARCH_FONT_STYLE = "fontStyle";

	public static final String SEARCH_CHOOSEENABLE = "CHOOSEENABLE";


	protected boolean chooseenable = false;

	protected String attrasname = KernelConst.ATTR_CURVE_NAME;
	protected String img_path = "C:/MAPDIR/VOK_IMG/";
	protected String img_pict_path = "C:/MAPDIR/VOK_IMG_PICT/";


	protected IViewControl mainmodule;

	protected JDialog searchDialog;

	//Имя объекта -> <набор идентификаторов объектов>
	protected Map<String, Set<String>> snames = new HashMap<String, Set<String>>();
	protected SortedSet<String> attributes = new TreeSet<String>(); //Мно-во имен аттрибутов которые есть в карте

	protected FilteredObjects fobjs = null; //объект содержащий отфильтрованные по точке объекты
	protected int colorLine = 0xFF000000;
	protected int colorFill = 0x90AAAAAA;
	protected int fontSize = 15;
	protected int fontStyle = Font.BOLD;
	protected String fontName = "sunserif";
	protected int warea = KernelConst.DEF_WAREA; //Область интереса вокруг точки курсора в координтах отображения по x
	protected int harea = KernelConst.DEF_HAREA; //Область интереса вокруг точки курсора в координтах отображения по у
	protected ObjGeomUtils algs = new ObjGeomUtils();
	protected IBaseGisObject selectedGisObject;
	protected int[] drawMetrics;
	private long lastMod;

	public GisSearch(IViewControl mainmodule) throws Exception
	{
		this.mainmodule = mainmodule;
		init();
	}


	public GisSearch() throws Exception
	{

	}

	public StringStorageManipulations reInitSearch(String attrName) throws Exception
	{
		attrasname = attrName;
		FilteredObjects l_fobj = fobjs;
		if (l_fobj != null)
		{
//	        getNamesByDrawPoint(l_fobj.pnt)
//		 		 	//производится показ новых аттрибутов
//			return getBySet(l_fobj.snames);
		}
		else
			init();
		return null;
		///return getBySet(snames);
	}

	public void resetSelection()
	{
		//TODO !!!Этот вариант тестовый после создания текстового слоя сделать через правила отображения!!!
		try
		{
			List<ILayer> layers = mainmodule.getProjContext().getLayerList();
			if ((layers.get(layers.size() - 1).getLrAttrs().get(KernelConst.LAYER_NAME).getValue().equals("View selections")))
			{
				layers.remove(layers.size() - 1);
				mainmodule.refresh(null);
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		synchronized (this)
		{
			selectedGisObject = null;
			drawMetrics=null;
		}
	}

	public void setSelectByName(String currentName)
	{
		try
		{
			Set<String> res = getObjectsIdByObjectName(currentName);
			if (res.iterator().hasNext())
			{
				String curveId = res.iterator().next();
				setViewByCurveId(curveId);
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	public void setViewByCurveId(String curveId) throws Exception
	{
		IBaseGisObject giso = mainmodule.getProjContext().getStorage().getBaseGisByCurveId(curveId);
		if (giso!=null)
		{
			IProjConverter converter = mainmodule.getViewPort().getCopyConverter();
			Point[] drwpnts = new DefGisOperations().getCentralDrawPoints(converter, giso);
			Point olddrwpnt = mainmodule.getViewPort().getDrawSize();
			((IProjConverter) (converter)).getAsShiftConverter().recalcBindPointByDrawDxDy(
					new double[]{drwpnts[0].x - olddrwpnt.x / 2, drwpnts[0].y - olddrwpnt.y / 2});

			//TODO !!!Этот вариант тестовый после создания текстового слоя сделать через правила отображения!!!
			{
				List<ILayer> layers = mainmodule.getProjContext().getLayerList();
				if (!(layers.get(layers.size() - 1).getLrAttrs().get(KernelConst.LAYER_NAME).getValue().equals("View selections"))) {
					IAttrs attrs = new DefaultAttrsImpl();
					attrs.put(KernelConst.LAYER_NAME, new DefAttrImpl(KernelConst.LAYER_NAME, "View selections"));
					attrs.put(KernelConst.LAYER_VISIBLE, new DefAttrImpl(KernelConst.LAYER_VISIBLE, true));


					CnStyleRuleImpl drawRule = new CnStyleRuleImpl(new CommonStyle(colorLine, colorFill), new Font(fontName, fontStyle, fontSize), attrasname);
					drawRule.setPointTextFactory(new IFactory<IParamPainter>() {
						public void addFactory(IFactory<IParamPainter> iParamPainterIFactory) {
							throw new UnsupportedOperationException("");
						}

						public IParamPainter createByTypeName(String typeObj) throws Exception {
							return new DefPntImgPainter(img_path, img_pict_path, getStorNmWithCodeNm(mainmodule.getProjContext()))
							{
								public boolean beforeImageDraw(BufferedImage bufferedImage, int[] xywh_draw)
								{
									int w=bufferedImage!=null?bufferedImage.getWidth():xywh_draw[2];
									int h=bufferedImage!=null?bufferedImage.getHeight():xywh_draw[3];
									int[] drawMetrics = new int[]{xywh_draw[0], xywh_draw[1],w,h};
									synchronized (GisSearch.this)
									{
										if (selectedGisObject!=null)
											GisSearch.this.drawMetrics=drawMetrics;
									}
									return true;
								}
							};
						}
					});
					layers.add(new DrawOnlyLayer(new SimpleMemStorage(giso), (IBaseFilter) null, attrs, drawRule));
				} else {
					layers.get(layers.size() - 1).setBaseStorage(new SimpleMemStorage(giso));
					layers.get(layers.size() - 1).setFilters(null);
				}

				synchronized (this)
				{
					selectedGisObject = giso;
				}

			}
			mainmodule.getViewPort().setCopyConverter(converter);
			mainmodule.refresh(null);
		}
		else
			resetSelection();

	}

	public SortedSet<String> getAttributeNames()
	{
		return attributes;
	}

	public String getCurrentName()
	{
		return attrasname;
	}

	protected Set<String> getObjectsIdByObjectName(String currentName)
	{
		Set<String> res = null;
		if (fobjs != null)
			res = fobjs.snames.get(currentName);
		else
			res = snames.get(currentName);
		return res;
	}

	protected INameConverter getStorNmWithCodeNm(IProjContext projContext)
	{
		INameConverter storNm2attrNm = projContext.getNameConverter();
		if (storNm2attrNm == null)
			storNm2attrNm = new DefNameConverter2();
		return storNm2attrNm;
	}

	public String getMenuName()
	{
		return MENU_NAME_ITEM;
	}

	public JMenu addMenu(JMenu inmenu) throws Exception
	{
		JMenuItem menuItem = new JMenuItem(Enc.get("FIND_ITEM_BY_NAME"), KeyEvent.VK_S);
		inmenu.add(menuItem);
		menuItem.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				try
				{
					startSearchGis();
				}
				catch (Exception e1)
				{
					e1.printStackTrace();
				}
			}
		});
		return inmenu;
	}

	private void startSearchGis() throws Exception
	{
		fobjs = null;
		if (lastMod < mainmodule.getProjContext().getStorage().getLastModified())
		{
			init();
			searchDialog = null;

		}

		TreeSet<String> set = new TreeSet<String>(new Comparator<String>()
		{
			public int compare(String s1, String s2)
			{
				return s1.compareToIgnoreCase(s2);
			}
		});
		set.addAll(snames.keySet());
		ShowDialog(set);
	}

	protected void ShowDialog(TreeSet<String> set)
	{

		if (attrasname == null)
		{

			JOptionPane.showMessageDialog(null, ERROR_MESAGE, ERROR_HEADER, JOptionPane.ERROR_MESSAGE);
			return;
		}

		if (searchDialog != null && searchDialog.isShowing())
			searchDialog.setVisible(false);
		searchDialog = new JDialog();

		AttrForm attrForm = new AttrForm(getBySet(set), searchDialog, this);
		ImageIcon icon = ImgResources.getIconByName("images/find.png", "SearchGis2");
		if (icon != null)
		{
			Frame frame = (Frame) searchDialog.getOwner();
			if (frame != null)
				frame.setIconImage(icon.getImage());
		}
		searchDialog.setTitle(DIALOG_TITLE);//TODO воспользоваться конвертором для имен
		searchDialog.setSize(300, 500);
		searchDialog.setVisible(true);
		attrForm.setByCuretEvent(true);
	}

	protected StringStorageManipulations getBySet(Set<String> set)
	{
		return new StringStorageManipulations(new Storage<String>(set));
	}

	protected boolean viewSelectedObjectInDetail(IBaseGisObject drawMe) throws Exception
	{
		return false;
	}

	protected void getNamesByDrawPoint(Point pnt) throws Exception
	{
		//формируем фильтр по точке
		IProjConverter linearConverter = mainmodule.getViewPort().getCopyConverter();

		MRect proj_rect = linearConverter.getRectByDstRect(new MRect(new MPoint(pnt.x - warea, pnt.y - harea), new MPoint(pnt.x + warea, pnt.y + harea)), null);
		//получаем все объекты попавшие в этот фильтр
		Iterator<IBaseGisObject> itobj = mainmodule.getProjContext().getStorage().filterObjs(new DefMBBFilter(proj_rect));
		Map<String, Set<String>> snames = new HashMap<String, Set<String>>();


		//показываем диалог
		while (itobj.hasNext())
		{
			IBaseGisObject bobj = itobj.next();

			if (algs.ObjIntersects(bobj, proj_rect))
			{

				IAttrs objAttrs = bobj.getObjAttrs();
				IDefAttr attr;

				if (
						objAttrs != null
								&&
								(attr = objAttrs.get(getStorNmWithCodeNm(mainmodule.getProjContext()).codeAttrNm2StorAttrNm(attrasname))) != null
						)
				{
					String name = attr.getValue().toString();
					Set<String> keyset = snames.get(name);
					if (keyset == null)
						snames.put(name, keyset = new HashSet<String>());
					keyset.add(bobj.getCurveId());
				}
			}
		}

		TreeSet<String> set = new TreeSet<String>(
				new Comparator<String>()
				{
					public int compare(String s1, String s2)
					{
						return s1.compareToIgnoreCase(s2);
					}
				});

		fobjs = new FilteredObjects(pnt, snames);


		set.addAll(snames.keySet());
		ShowDialog(set);
	}

	public JPopupMenu addPopUpMenu(JPopupMenu inmenu) throws Exception
	{
		return inmenu;
	}

	public void registerListeners(JComponent component) throws Exception
	{
		//TODO Можно конечно зарегистрировать листенер, НО!!!! 1-ое как ими управлять, нужно
		//TODO как-то договорится о том как разраничивать обработку событий между модулями

		component.addMouseListener(new MouseListener()
		{

			public void mouseClicked(MouseEvent e)
			{
				try
				{
					if (!e.isConsumed())
					{
						if (e.getClickCount() == 2 && e.getButton() == MouseEvent.BUTTON1)
						{
							int[] drawMetrics=null;
							IBaseGisObject selectedGisObject=null;
							synchronized (GisSearch.this)
							{
								if (GisSearch.this.selectedGisObject!=null)
								{
									selectedGisObject = GisSearch.this.selectedGisObject;
									drawMetrics=GisSearch.this.drawMetrics;
								}
							}


							MRect rect= null;
							MPoint mPoint=null;

							if (selectedGisObject!=null
									&& drawMetrics!=null && drawMetrics.length>=4)
							{
								rect = new MRect(
										new MPoint(drawMetrics[0],drawMetrics[1]),
										new MPoint(drawMetrics[0]+drawMetrics[2],drawMetrics[1]+drawMetrics[3])
										);
								mPoint =  new MPoint(e.getX(), e.getY());
							}

							if(
									selectedGisObject==null || rect==null ||
									!rect.isInRect(mPoint) ||
									!viewSelectedObjectInDetail(selectedGisObject)
								)
								getNamesByDrawPoint(e.getPoint());

							e.consume();

						}
						else if (e.isControlDown() && e.getClickCount() == 1 && e.getButton() == MouseEvent.BUTTON3)
						{
							resetSelection();
							e.consume();
						}
					}
				}
				catch (Exception e1)
				{
					e1.printStackTrace();
				}
			}

			public void mousePressed(MouseEvent e)
			{
			}

			public void mouseReleased(MouseEvent e)
			{
			}

			public void mouseEntered(MouseEvent e)
			{
			}

			public void mouseExited(MouseEvent e)
			{
			}
		});
	}

	public JToolBar addInToolBar(JToolBar systemtoolbar)
	{
		JButton button = new JButton();
		ImageIcon icon = ImgResources.getIconByName("images/ejbFinderMethod.png", "SearchGis");
		if (icon != null)
			button.setIcon(icon);

		button.setToolTipText(DIALOG_TITLE);//TODO воспользоваться конвертором для имен
		button.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				try
				{
					startSearchGis();
				}
				catch (Exception e1)
				{
					e1.printStackTrace();
				}
			}
		});

		button.setMargin(new Insets(0, 0, 0, 0));
		systemtoolbar.add(button);
		return systemtoolbar;
	}

	public void paintMe(Graphics g) throws Exception
	{
	}

	@Override
	public boolean shouldIRepaint() throws Exception {
		return false;
	}

	public void registerNameConverter(INameConverter nameConverter, IFactory<INameConverter> factory) throws Exception
	{
		INameConverter l_nameConverter = null;
		if (factory != null && (l_nameConverter = factory.createByTypeName(MODULENAME)) != null)
			nameConverter.addNameConverter(l_nameConverter);
		//TODO Создать конвертер по умолчанию
	}

	public void unload()
	{
	}

	public String getModuleName()
	{
		return MODULENAME;
	}

	public IAnswerBean execute(ICommandBean cmd)
	{
		throw new UnsupportedOperationException();
	}

	public Object[] init(Object... obj) throws Exception
	{
		super.init(obj);
		init();
		return null;
	}

	protected void init()
			throws Exception
	{
		long tm = System.currentTimeMillis();
		snames.clear();
		attributes.clear();
		IProjContext projcontext = mainmodule.getProjContext();
		IBaseStorage stor = projcontext.getStorage();

		Iterator<String> itid = stor.getCurvesIds();//Формируем базу имен
		while (itid.hasNext())
		{
			String objectId = itid.next();
			IAttrs attrs = stor.getObjAttrs(objectId);
			IDefAttr ixattr;
			String strattr;
			if (
					attrs != null
							&&
							(ixattr = attrs.get(getStorNmWithCodeNm(projcontext).codeAttrNm2StorAttrNm(attrasname))) != null
							&&
							(strattr = ixattr.getValue().toString()) != null
							&&
							strattr.length() > 0
					)
			{

				Set<String> lv = snames.get(strattr);
				if (lv == null)
					snames.put(strattr, lv = new HashSet<String>());
				lv.add(objectId);
			}

			if (chooseenable && attrs != null)
				for (String attrname : attrs.keySet())
					attributes.add(attrname);
		}
		System.out.println("Module " + MODULENAME + " was init:" + (System.currentTimeMillis() - tm));

		if (!chooseenable)
			attributes.add(attrasname);
		else if (!attributes.contains(attrasname))
		{
			if (attributes != null && attributes.size() > 0)
				attrasname = attributes.first();
			else
				attrasname = null;
		}

		lastMod = System.currentTimeMillis();
	}

	public Object init(Object obj) throws Exception
	{
		IParam attr = (IParam) obj;
		if (attr.getName().equalsIgnoreCase(SEARCH_CHOOSEENABLE))
		{
			try
			{
				chooseenable = Boolean.parseBoolean((String) attr.getValue());

			}
			catch (Exception e)
			{//
			}
		}
		else if (attr.getName().equalsIgnoreCase(KernelConst.VIEWCNTRL_TAGNAME))
			mainmodule = (IViewControl) attr.getValue();
		else if (attr.getName().equalsIgnoreCase(SEARCH_IMAGEPATH))
			img_path = (String) attr.getValue();
		else if (attr.getName().equalsIgnoreCase(SEARCH_IMAGEPICTPATH))
			img_pict_path = (String) attr.getValue();
		else if (attr.getName().equalsIgnoreCase(KernelConst.USE_AS_ATTRIBUTENAME))
			this.attrasname = (String) attr.getValue();
		else if (attr.getName().equalsIgnoreCase(CommonStyle.COLOR_LINE))
			colorLine = Integer.parseInt((String) attr.getValue(), 16);
		else if (attr.getName().equalsIgnoreCase(CommonStyle.COLOR_FILL))
			colorFill = Integer.parseInt((String) attr.getValue(), 16);
		else if (attr.getName().equalsIgnoreCase(SEARCH_FONT_NAME))
			fontName = (String) attr.getValue();
		else if (attr.getName().equalsIgnoreCase(SEARCH_FONT_SIZE))
			fontSize = Integer.parseInt((String) attr.getValue());
		else if (attr.getName().equalsIgnoreCase(SEARCH_FONT_STYLE))
		{
			String sStyle = (String) attr.getValue();
			Class font = Font.class;
			Field[] flds = font.getFields();
			for (Field fld : flds)
				if (fld.getName().equals(sStyle))
				{
					fontStyle = fld.getInt(null);
					break;
				}
		}
		else if (attr.getName().equalsIgnoreCase(KernelConst.ATTR_HAREA))
		{
			try
			{
				harea=Integer.parseInt((String) attr.getValue());
			}
			catch (NumberFormatException e)
			{//
			}
		}
		else if (attr.getName().equalsIgnoreCase(KernelConst.ATTR_WAREA))
		{
			try
			{
				warea=Integer.parseInt((String) attr.getValue());
			}
			catch (NumberFormatException e)
			{//
			}
		}
		return null;
	}

	static protected class FilteredObjects
	{
		Point pnt; //точка вокруг, которой были отфильтрованы
		Map<String, Set<String>> snames;//Имя объекта -> <набор идентификаторов объектов>

		public FilteredObjects(Point pnt, Map<String, Set<String>> snames)
		{
			this.pnt = pnt;
			this.snames = snames;
		}
	}
}
