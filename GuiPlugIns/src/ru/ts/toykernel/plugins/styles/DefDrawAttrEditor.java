package ru.ts.toykernel.plugins.styles;

import ru.ts.toykernel.plugins.IGuiModule;
import ru.ts.toykernel.gui.IViewControl;
import ru.ts.utils.gui.tables.IHeaderSupplyer;
import ru.ts.toykernel.pcntxt.IProjContext;
import ru.ts.toykernel.plugins.consts.DefNameConverter2;
import ru.ts.toykernel.consts.INameConverter;
import ru.ts.toykernel.consts.KernelConst;
import ru.ts.toykernel.factory.BaseInitAble;
import ru.ts.toykernel.plugins.styles.SetStylesDlg;
import ru.ts.toykernel.plugins.ICommandBean;
import ru.ts.toykernel.plugins.IAnswerBean;
import ru.ts.factory.IFactory;
import ru.ts.factory.IParam;
import ru.ts.res.ImgResources;
import su.mwlib.utils.Enc;

import javax.swing.*;
import java.awt.event.KeyEvent;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.*;

/**
 * Draw Attribute Editor
 */
public class DefDrawAttrEditor extends BaseInitAble implements IGuiModule
{
	public static final String HEADER_SUPPLYER = "HeaderSupplyer";
	public static final String MODULENAME = "LayerStyles";
	public static final String LAYER_STYLES_MENU_NAME = Enc.get("LAYER_STYLES");
	public static final String VIEW_SUB_MENU_NAME = Enc.get("DISPLAY");

	private IViewControl mainmodule;
	private ISupplyerFactory headerFactory;

	public DefDrawAttrEditor()
	{
	}

	public DefDrawAttrEditor(IViewControl mainmodule, ISupplyerFactory headerFactory)
	{
		this.mainmodule = mainmodule;
		this.headerFactory = headerFactory;
		initHeaderFactory();
	}



	private INameConverter getStorNmWithCodeNm(IProjContext projContext)
	{
		INameConverter storNm2attrNm = projContext.getNameConverter();
		if (storNm2attrNm==null)
		  storNm2attrNm = new DefNameConverter2();
		return storNm2attrNm;
	}

	public String getMenuName()
	{
		return VIEW_SUB_MENU_NAME;
	}

	public JMenu addMenu(JMenu inmenu) throws Exception
	{

		JMenuItem menuItem = new JMenuItem(LAYER_STYLES_MENU_NAME, KeyEvent.VK_C);
		inmenu.add(menuItem);
		menuItem.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				try
				{
					SetStylesDlg dialog = new SetStylesDlg(mainmodule, headerFactory.getHeaderSupplyer());
					ImageIcon icon= ImgResources.getIconByName("images/poolball.gif","Title");
					if (icon!=null)
					{
						Frame frame = (Frame) dialog.getOwner();
						if (frame!=null)
							frame.setIconImage(icon.getImage());
					}
					dialog.pack();
					dialog.setModal(false);
					dialog.setVisible(true);
				} catch (Exception e1)
				{
					e1.printStackTrace();
				}

			}
		});
		return inmenu;
	}

	public JPopupMenu addPopUpMenu(JPopupMenu inmenu) throws Exception
	{
		return inmenu;
	}

	public void registerListeners(JComponent component) throws Exception
	{
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

	public void registerNameConverter(INameConverter nameConverter, IFactory<INameConverter> factory) throws Exception
	{
		INameConverter l_nameConverter=null;
		if (factory!=null && (l_nameConverter=factory.createByTypeName(MODULENAME))!=null)
			nameConverter.addNameConverter(l_nameConverter);
		//TODO Создать конвертер по умолчанию
	}

	public void unload()
	{
		//To change body of implemented methods use File | Settings | File Templates.
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
					return new DefHeaderSupplyer(getStorNmWithCodeNm(DefDrawAttrEditor.this.mainmodule.getProjContext()));
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
		return null;
	}

}
