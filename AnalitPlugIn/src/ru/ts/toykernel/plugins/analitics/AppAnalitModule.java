package ru.ts.toykernel.plugins.analitics;

import ru.ts.toykernel.plugins.*;
import ru.ts.toykernel.gui.IViewControl;
import ru.ts.toykernel.consts.KernelConst;
import ru.ts.toykernel.consts.INameConverter;
import ru.ts.toykernel.factory.BaseInitAble;
import ru.ts.toykernel.proj.ICliConfigProvider;
import ru.ts.factory.IParam;
import ru.ts.factory.IFactory;
import su.mwlib.utils.Enc;

import javax.swing.*;
import java.awt.event.KeyEvent;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;


/**
 * Итак базовый апплетный модуль для показа аналитики
 * ru.ts.toykernel.plugins.analitics.AppAnalitModule
 */
public class AppAnalitModule  extends BaseInitAble implements IGuiModule
{
	protected ICliConfigProvider conf_provider;
	protected IModule cmdprovider;
	protected  IViewControl mainmodule;


	public AppAnalitModule(IModule cmdprovider, ICliConfigProvider conf_provider,IViewControl mainmodule)
	{
		this.cmdprovider = cmdprovider;
		this.conf_provider = conf_provider;
		this.mainmodule = mainmodule;
	}

	public AppAnalitModule()
	{

	}

	public String getMenuName()
	{
		return Enc.get("ANALYTICS");
	}


	public Object init(Object obj) throws Exception
	{
		IParam attr=(IParam)obj;
		if (attr.getName().equalsIgnoreCase(KernelConst.CMDPROVIDER_TAGNAME))
			cmdprovider = (IModule) attr.getValue();
		else if (attr.getName().equalsIgnoreCase(KernelConst.CONFPROVIDERS_TAGNAME))
			conf_provider = (ICliConfigProvider) attr.getValue();
		if (attr.getName().equalsIgnoreCase(KernelConst.VIEWCNTRL_TAGNAME))
			this.mainmodule = (IViewControl) attr.getValue();
		return null;
	}

	public String getModuleName()
	{
		return "AppAnalitModule";
	}

	public IAnswerBean execute(ICommandBean cmd) throws Exception
	{
		String session = conf_provider.getSession();
		if (session!=null)
		{
			cmd.setSessionId(session);
			return cmdprovider.execute(cmd);
		}
		return new AnswerBean(cmd,"",new byte[]{});
	}

	public void registerNameConverter(INameConverter nameConverter, IFactory<INameConverter> factory) throws Exception
	{
	}

	public void unload()
	{
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
					Map<String, byte[]> parmap = new HashMap<String, byte[]>();
					ICommandBean cmdbean=new CommandBean(BaseAnalitModule.ANALIT,parmap, ICommandBean.APPCLI,"");
					IAnswerBean answer = execute(cmdbean);
					if (answer!=null)
					{
						byte[] bt=answer.getbAnswer();
						if (bt==null || bt[0]==0)
							menuItem.setText(Enc.get("SHOW_ANALYTICS"));
						else
							menuItem.setText(Enc.get("HIDE_ANALYTICS"));
						mainmodule.refresh(null);
					}
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
	}

	@Override
	public boolean shouldIRepaint() throws Exception {
		return false;
	}
}
