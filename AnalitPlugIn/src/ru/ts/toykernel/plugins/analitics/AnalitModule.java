package ru.ts.toykernel.plugins.analitics;

import ru.ts.toykernel.plugins.IGuiModule;
import ru.ts.toykernel.consts.KernelConst;
import ru.ts.toykernel.gui.IViewControl;
import ru.ts.factory.IParam;
import su.mwlib.utils.Enc;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

/**
 * Модуль аналитики, требует для исполнения
 * провайдер рельефа, при этом может показывать только один тип аналитики
 * ru.ts.toykernel.plugins.analitics.AnalitModule
 *
 */
public class AnalitModule extends BaseAnalitModule implements IGuiModule
{
	protected IViewControl mainmodule;

	public String getMenuName()
	{
		return Enc.get("ANALYTICS");
	}

	public Object init(Object obj) throws Exception
	{
		IParam attr=(IParam)obj;
		if (attr.getName().equalsIgnoreCase(KernelConst.VIEWCNTRL_TAGNAME))
			this.mainmodule = (IViewControl) attr.getValue();
		else
			return super.init(obj);
		return null;
	}

	public JMenu addMenu(JMenu inmenu) throws Exception
	{
		final JMenuItem menuItem = new JMenuItem(Enc.get("SHOW_ANALYTICS"), KeyEvent.VK_S);
		inmenu.add(menuItem);
		menuItem.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				try
				{
					fanalitcs=!fanalitcs;

					if (fanalitcs)
						menuItem.setText(Enc.get("HIDE_ANALYTICS"));
					else
					    menuItem.setText(Enc.get("SHOW_ANALYTICS"));

					mainmodule.refresh(null);
				}
				catch (Exception e1)
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

	public JToolBar addInToolBar(JToolBar systemtoolbar) throws Exception
	{
		return systemtoolbar;
	}

	public void paintMe(Graphics g) throws Exception
	{
		if (fanalitcs)
			getLayerByName(projcontext,showLayerName).paintLayer(g,mainmodule.getViewPort());
	}

	@Override
	public boolean shouldIRepaint() throws Exception {
		return false;
	}
}
