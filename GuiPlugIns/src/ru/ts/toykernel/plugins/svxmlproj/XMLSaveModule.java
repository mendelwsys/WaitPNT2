package ru.ts.toykernel.plugins.svxmlproj;

import ru.ts.toykernel.plugins.IGuiModule;
import ru.ts.toykernel.gui.IViewControl;
import ru.ts.toykernel.consts.INameConverter;
import ru.ts.toykernel.consts.KernelConst;
import ru.ts.factory.IFactory;
import ru.ts.factory.IParam;
import ru.ts.toykernel.pcntxt.gui.defmetainfo.MainformMonitor;
import ru.ts.toykernel.pcntxt.IProjContext;
import ru.ts.utils.IOperation;
import ru.ts.res.ImgResources;
import ru.ts.toykernel.factory.BaseInitAble;
import ru.ts.toykernel.plugins.ICommandBean;
import ru.ts.toykernel.plugins.IAnswerBean;
import ru.ts.toykernel.proj.xml.IXMLProjBuilder;
import ru.ts.toykernel.app.xml.IXMLBuilderContext;
import su.mwlib.utils.Enc;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.io.*;

/**
 * module for saving xml descriptor of project
 * ru.ts.toykernel.plugins.svxmlproj.XMLSaveModule
 */
public class XMLSaveModule extends BaseInitAble implements IGuiModule
{
	public static final String MODULENAME = "SAVEXMLMODULE";
	private IViewControl mainmodule;
	private IXMLProjBuilder builder;


	public XMLSaveModule()
	{
	}

	public XMLSaveModule(IViewControl mainmodule)
	{
		this.mainmodule = mainmodule;
	}

	public String getMenuName()
	{
		return Enc.get("FILE");
	}

	public JMenu addMenu(JMenu inmenu) throws Exception
	{
		JMenuItem menuItem = new JMenuItem(Enc.get("SAVE_PROJECT_DESCRIPTOR"), KeyEvent.VK_S);
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

	protected void saveproject()
	{
		OutputStream os = null;
		try
		{
			final IProjContext proj = mainmodule.getProjContext();
			final File xmldesc = IOperation.getFilePath(MainformMonitor.frame, Enc.get("SAVE"), Enc.get("PROJECT_FILE"), "xml", proj.getProjectlocation());
			if (xmldesc != null)
			{
				os =  new FileOutputStream(xmldesc);

				IXMLBuilderContext context = builder.getBuilderContext();
				final String enc = "WINDOWS-1251";
				os.write(context.getFullXML(enc, false).getBytes(enc));
				os.flush();
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			try
			{
				if (os!=null)
					os.close();
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
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
		ImageIcon icon = ImgResources.getIconByName("images/checkOut.png", "SaveProject");
		if (icon != null)
			button.setIcon(icon);
		button.setToolTipText(Enc.get("SAVE_PROJECT_DESCRIPTOR"));//TODO воспользоваться конвертором для имен
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
		else if (attr.getName().equalsIgnoreCase(KernelConst.APPBUILDER_TAGNAME))
			this.builder=(IXMLProjBuilder) attr.getValue();
		return null;
	}
}