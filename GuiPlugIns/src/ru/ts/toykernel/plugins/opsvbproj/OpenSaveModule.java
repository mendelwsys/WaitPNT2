package ru.ts.toykernel.plugins.opsvbproj;

import ru.ts.toykernel.plugins.IGuiModule;
import ru.ts.toykernel.gui.IViewControl;
import ru.ts.toykernel.consts.INameConverter;
import ru.ts.toykernel.consts.KernelConst;
import ru.ts.factory.IFactory;
import ru.ts.factory.IParam;
import ru.ts.stream.ISerializer;
import ru.ts.toykernel.pcntxt.gui.defmetainfo.MainformMonitor;
import ru.ts.toykernel.pcntxt.IProjContext;
import ru.ts.toykernel.converters.ConvB64Initializer;
import ru.ts.toykernel.proj.stream.def.IStreamAbleProj;
import ru.ts.utils.IOperation;
import ru.ts.res.ImgResources;
import ru.ts.toykernel.converters.IProjConverter;
import ru.ts.toykernel.factory.BaseInitAble;
import ru.ts.toykernel.plugins.ICommandBean;
import ru.ts.toykernel.plugins.IAnswerBean;
import ru.ts.gisutils.algs.common.MPoint;
import su.mwlib.utils.Enc;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.io.*;

/**
 * Open Project module
 */
public class OpenSaveModule extends BaseInitAble implements IGuiModule
{
	public static final String MODULENAME = "OPENMODULE";
	private IViewControl mainmodule;

	public OpenSaveModule()
	{
	}

	public OpenSaveModule(IViewControl mainmodule)
	{
		this.mainmodule = mainmodule;
	}

	public String getMenuName()
	{
		return Enc.get("FILE");
	}

	public JMenu addMenu(JMenu inmenu) throws Exception
	{
		JMenuItem menuItem = new JMenuItem(Enc.get("OPEN_PROJECT"), KeyEvent.VK_O);
		inmenu.add(menuItem);
		menuItem.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				loadproject();
			}
		});

		menuItem = new JMenuItem(Enc.get("SAVE_PROJECT"), KeyEvent.VK_S);
		inmenu.add(menuItem);
		menuItem.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				saveproject();
			}
		});
		return inmenu;
	}

	protected void loadproject()
	{
		try
		{
			IProjContext proj = mainmodule.getProjContext();
			File path = IOperation.getFilePath(MainformMonitor.frame, Enc.get("OPEN"), Enc.get("PROJECT_FILE"), "lr", proj.getProjectlocation());
			if (path!=null)
				loadproj(proj, path.getAbsolutePath());
		} catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	private void loadproj(final IProjContext proj, final String binlayer)
			throws Exception
	{
		new Thread()
		{
			public void run()
			{
				try
				{
					if (binlayer != null)
					{
						if (proj instanceof IStreamAbleProj)
						{
							File fl = new File(binlayer);
							DataInputStream dis = new DataInputStream(new BufferedInputStream(new FileInputStream(fl)));
							proj.setProjectlocation(binlayer);
							IStreamAbleProj strproj = (IStreamAbleProj) proj;
							strproj.loadFromStream(dis);
							ConvB64Initializer initializer = strproj.getConvInitializer();
							IProjConverter converter = (IProjConverter) initializer.createByTypeName(initializer.getS_convertertype());
							converter.initByBase64Point(initializer.getB_converter());
							converter.getAsShiftConverter().setBindP0(MPoint.getByBase64Point(initializer.getB_currentP0()));
							mainmodule.getApplication().addProjectContext(proj,converter);
						} 
						else
							throw new UnsupportedOperationException("Error while loading not serializable project");
					}
				} catch (Exception e)
				{
					e.printStackTrace();
				}
			}
		}.start();
	}

	protected void saveproject()
	{
		try
		{
			final IProjContext proj = mainmodule.getProjContext();
			final File binfile = IOperation.getFilePath(MainformMonitor.frame, Enc.get("SAVE"), Enc.get("PROJECT_FILE"), "lr", proj.getProjectlocation());
			if (binfile != null)
			{

				new Thread()
				{
					public void run()
					{
						try
						{
							if (proj instanceof ISerializer)
							{
								DataOutputStream dos = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(binfile)));
								((ISerializer) proj).savetoStream(dos);
								dos.flush();
								dos.close();
								if (!binfile.equals(proj.getProjectlocation()))
									loadproj(proj, binfile.getAbsolutePath());
							} else
								throw new UnsupportedOperationException("Error while loading not serializable project");
						} catch (Exception e)
						{
							e.printStackTrace();
						}
					}
				}.start();
			}
		} catch (Exception e)
		{
			e.printStackTrace();
		}
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
		ImageIcon icon = ImgResources.getIconByName("images/menu-open.png", "OpenProject");
		if (icon != null)
			button.setIcon(icon);
		button.setToolTipText(Enc.get("OPEN_PROJECT"));//TODO воспользоваться конвертором для имен
		button.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				loadproject();
			}
		});
		button.setMargin(new Insets(0, 0, 0, 0));
		systemtoolbar.add(button);

		button = new JButton();
		icon = ImgResources.getIconByName("images/checkOut.png", "SaveProject");
		if (icon != null)
			button.setIcon(icon);
		button.setToolTipText(Enc.get("SAVE_PROJECT"));//TODO воспользоваться конвертором для имен
		button.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				saveproject();
			}
		});
		button.setMargin(new Insets(0, 0, 0, 0));
		systemtoolbar.add(button);

		return systemtoolbar;
	}

	public void paintMe(Graphics g)
	{
	}

	@Override
	public boolean shouldIRepaint() throws Exception {
		return false;
	}

	public void registerNameConverter(INameConverter nameConverter, IFactory<INameConverter> factory)
	{
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


	public Object init(Object obj) throws Exception
	{
		IParam attr=(IParam)obj;
		if (attr.getName().equalsIgnoreCase(KernelConst.VIEWCNTRL_TAGNAME))
			this.mainmodule = (IViewControl) attr.getValue();
		return null;
	}
}
