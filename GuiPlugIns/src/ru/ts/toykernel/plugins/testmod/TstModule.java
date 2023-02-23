package ru.ts.toykernel.plugins.testmod;

import ru.ts.toykernel.plugins.IGuiModule;
import ru.ts.toykernel.gui.IViewControl;
import ru.ts.toykernel.consts.INameConverter;
import ru.ts.toykernel.consts.KernelConst;
import ru.ts.factory.IFactory;
import ru.ts.factory.IParam;
import ru.ts.toykernel.geom.IBaseGisObject;
import ru.ts.toykernel.drawcomp.ILayer;
import ru.ts.toykernel.pcntxt.IProjContext;
import ru.ts.toykernel.factory.BaseInitAble;
import ru.ts.toykernel.plugins.ICommandBean;
import ru.ts.toykernel.plugins.IAnswerBean;
import ru.ts.res.ImgResources;
import su.mwlib.utils.Enc;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.util.*;
import java.util.List;

/**
 * Test module
 */
public class TstModule  extends BaseInitAble implements IGuiModule
{
	public static final String MODULENAME = "GIS_TEST";
	public static final String VIEW_SUB_MENU_NAME = Enc.get("DISPLAY");
	private static final String TEST_MENU_NAME = Enc.get("PERFORM_TEST");
	boolean switchoff =true;
	private IViewControl mainmodule;
	private INameConverter nameConverter;

	public TstModule()
	{
	}

	public TstModule(IViewControl mainmodule)
	{
		this.mainmodule = mainmodule;
	}

	protected void execTest()
	{
		try
		{
			IProjContext projctx = mainmodule.getProjContext();
			List<ILayer> ll = projctx.getLayerList();

			Interceptor interseptor = null;
			if (switchoff)
				interseptor = new Interceptor(mainmodule,nameConverter);

			for (ILayer iLayer : ll)
				if (switchoff)
				{
					Iterator<IBaseGisObject> it = iLayer.getStorage().getAllObjects();
					if (it.hasNext())
					{
						IBaseGisObject iBaseGisObject = it.next();
						if (iBaseGisObject.getGeotype().equals(nameConverter.codeAttrNm2StorAttrNm(KernelConst.POINT)))
							iLayer.getDrawRule().setInterceptor(interseptor);
					}
				}
				else
					iLayer.getDrawRule().setInterceptor(interseptor);
			switchoff=!switchoff;
			mainmodule.refresh(null);
		} catch (Exception e)
		{
			e.printStackTrace();
		}

	}

	public String getMenuName()
	{
		return VIEW_SUB_MENU_NAME;
	}

	public JMenu addMenu(JMenu inmenu) throws Exception
	{

		JMenuItem menuItem = new JMenuItem(TEST_MENU_NAME, KeyEvent.VK_T);
		inmenu.add(menuItem);
		menuItem.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				execTest();
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

	public JToolBar addInToolBar(JToolBar systemtoolbar) throws Exception
	{
		JButton button = new JButton();
		ImageIcon icon = ImgResources.getIconByName("images/unknown.png", "TestGis");
		if (icon!=null)
			button.setIcon(icon);

		button.setToolTipText(TEST_MENU_NAME);
		button.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				execTest();
			}
		});

		button.setMargin(new Insets( 0, 0, 0, 0 ));
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

	public String getModuleName()
	{
		return MODULENAME;
	}

	public IAnswerBean execute(ICommandBean cmd)
	{
		throw new UnsupportedOperationException();
	}

	public void registerNameConverter(INameConverter nameConverter, IFactory<INameConverter> factory) throws Exception
	{
		this.nameConverter=nameConverter;
	}

	public void unload()
	{
		//To change body of implemented methods use File | Settings | File Templates.
	}

	public Object init(Object obj) throws Exception
	{
		IParam attr=(IParam)obj;
		if (attr.getName().equalsIgnoreCase(KernelConst.VIEWCNTRL_TAGNAME))
			this.mainmodule = (IViewControl) attr.getValue();
		return null;
	}
}
