package ru.ts.toykernel.plugins.cvviewer;

import ru.ts.toykernel.plugins.IGuiModule;
import ru.ts.toykernel.gui.IViewControl;
import ru.ts.utils.gui.tables.IHeaderSupplyer;
import ru.ts.toykernel.geom.IBaseGisObject;
import ru.ts.toykernel.geom.def.ObjGeomUtils;
import ru.ts.toykernel.filters.DefMBBFilter;
import ru.ts.toykernel.consts.INameConverter;
import ru.ts.toykernel.consts.KernelConst;
import ru.ts.toykernel.converters.ILinearConverter;
import ru.ts.toykernel.factory.BaseInitAble;
import ru.ts.toykernel.plugins.styles.ISupplyerFactory;
import ru.ts.toykernel.plugins.ICommandBean;
import ru.ts.toykernel.plugins.IAnswerBean;
import ru.ts.factory.IFactory;
import ru.ts.factory.IParam;
import ru.ts.gisutils.algs.common.MRect;
import ru.ts.gisutils.algs.common.MPoint;
import ru.ts.res.ImgResources;

import javax.swing.*;
import java.awt.event.MouseListener;
import java.awt.event.MouseEvent;
import java.awt.*;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * View curve attributes module
 */
public class CurveViewer  extends BaseInitAble implements IGuiModule
{
	public static final String MODULENAME = "CURVE_VIEWER";
	public static final String HEADER_SUPPLYER = "HeaderSupplyer";

	protected int warea = KernelConst.DEF_WAREA; //Область интереса вокруг точки курсора в координтах отображения по x
	protected int harea = KernelConst.DEF_HAREA; //Область интереса вокруг точки курсора в координтах отображения по у
	protected String attrasname;
	private IViewControl mainmodule;
	private ISupplyerFactory headerFactory;


	public CurveViewer()
	{
	}

	public CurveViewer(IViewControl mainmodule,ISupplyerFactory headerFactory)
	{
		this.mainmodule = mainmodule;
		this.headerFactory = headerFactory;
		initHeaderFactory();
	}


	public String getMenuName()
	{
		return null;
	}

	public JMenu addMenu(JMenu inmenu) throws Exception
	{
		return inmenu;
	}

	public JPopupMenu addPopUpMenu(JPopupMenu inmenu) throws Exception
	{
		return inmenu;
	}

	public void registerNameConverter(INameConverter nameConverter, IFactory<INameConverter> factory) throws Exception
	{
		INameConverter l_nameConverter=null;
		if (factory!=null && (l_nameConverter=factory.createByTypeName(MODULENAME))!=null)
			nameConverter.addNameConverter(l_nameConverter);
		else
			nameConverter.addNameConverter(new DefModNameConverter());
	}

	public void unload()
	{
	}

	public void registerListeners(JComponent component)
	{
		component.addMouseListener(new MouseListener()
		{
			public void mouseClicked(MouseEvent e)
			{
				try
				{
					if (!e.isConsumed() && e.getClickCount() == 2 && e.getButton() == MouseEvent.BUTTON3)
						showAttrsByDrawPoint(e.getPoint());
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
		return systemtoolbar;
	}

	public void paintMe(Graphics g) throws Exception
	{
	}

	@Override
	public boolean shouldIRepaint() throws Exception {
		return false;
	}

	private void showAttrsByDrawPoint(Point drwpnt) throws Exception
	{
		//формируем фильтр по точке
		ILinearConverter linearConverter = mainmodule.getViewPort().getCopyConverter();
		MRect drwRect = new MRect(new MPoint(drwpnt.x - warea, drwpnt.y - harea), new MPoint(drwpnt.x + warea, drwpnt.y + harea));
		MRect proj_rect = linearConverter.getRectByDstRect(drwRect,null);
		//получаем все объекты попавшие в этот фильтр
		Iterator<IBaseGisObject> itobj = mainmodule.getProjContext().getStorage().filterObjs(new DefMBBFilter(proj_rect));

		List<IBaseGisObject> filteredcurves= new LinkedList<IBaseGisObject>();
		while (itobj.hasNext())
		{
			IBaseGisObject baseGisObject = itobj.next();
			String geotype = baseGisObject.getGeotype();
			MRect rect=baseGisObject.getMBB(null);
			if (

					geotype.equals(KernelConst.LINESTRING) || geotype.equals(KernelConst.LINEARRING) || geotype.equals(KernelConst.LINEARRINGH)
					|| (geotype.equals(KernelConst.POINT) && (rect.p1.x!=rect.p4.x || rect.p1.y!=rect.p4.y))

				)
			{
				ObjGeomUtils objGeomUtils = new ObjGeomUtils();
				if
				(
					((geotype.equals(KernelConst.LINEARRING) || geotype.equals(KernelConst.LINEARRINGH))&& objGeomUtils.isInPolyPolyGon(baseGisObject,proj_rect.getMidle()))
					||
					objGeomUtils.ObjIntersects(baseGisObject,proj_rect)
				)
				filteredcurves.add(baseGisObject);
			}
			else
				filteredcurves.add(baseGisObject);
		}
		CurveOptions option=new CurveOptions(mainmodule,filteredcurves,headerFactory.getHeaderSupplyer(),attrasname);
		ImageIcon icon= ImgResources.getIconByName("images/poolball.gif","Title");
		if (icon!=null)
		{
			Frame frame = (Frame) option.getOwner();
			if (frame!=null)
				frame.setIconImage(icon.getImage());
		}
		option.pack();
		option.setVisible(true);
	}

	public String getModuleName()
	{
		return MODULENAME;
	}

	public IAnswerBean execute(ICommandBean cmd)
	{
		throw new UnsupportedOperationException();
	}

	public Object[] init(Object ... obj) throws Exception
	{
		super.init(obj);
		initHeaderFactory();
		return null;
	}

	private void initHeaderFactory()
	{
		if (this.headerFactory==null)
		{
			headerFactory=new ISupplyerFactory()
			{
				public IHeaderSupplyer getHeaderSupplyer() throws Exception
				{
					return new DefHeaderSupplyer(CurveViewer.this.mainmodule.getProjContext().getNameConverter());
				}
			};
		}
	}

	public Object init(Object obj) throws Exception
	{
		IParam attr = (IParam) obj;
		if (attr.getName().equalsIgnoreCase(KernelConst.VIEWCNTRL_TAGNAME))
			this.mainmodule = (IViewControl) attr.getValue();
		else if (attr.getName().equalsIgnoreCase(HEADER_SUPPLYER))
			this.headerFactory = (ISupplyerFactory)attr.getValue();
		else if (attr.getName().equalsIgnoreCase(KernelConst.USE_AS_ATTRIBUTENAME))
			this.attrasname = (String) attr.getValue();
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
}
