package ru.ts.toykernel.plugins.gissearch2;

import ru.ts.toykernel.factory.BaseInitAble;
import ru.ts.toykernel.plugins.*;
import ru.ts.toykernel.plugins.gissearch2.gsconn.*;
import ru.ts.toykernel.consts.INameConverter;
import ru.ts.toykernel.consts.KernelConst;
import ru.ts.toykernel.proj.ICliConfigProvider;
import ru.ts.toykernel.gui.IViewControl;
import ru.ts.toykernel.gui.IViewPort;
import ru.ts.toykernel.converters.IProjConverter;
import ru.ts.factory.IFactory;
import ru.ts.factory.IParam;
import ru.ts.gisutils.algs.common.MPoint;
import ru.ts.utils.data.Storage;
import ru.ts.utils.data.StringStorageManipulations;
import ru.ts.utils.data.IReadStorage;
import ru.ts.res.ImgResources;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;

import su.mwlib.utils.Enc;
import su.org.coder.utils.String0AHelper;
import org.apache.xerces.utils.Base64;

/**
 * Клиентский Модуль поиска объекта по имени
 * ru.ts.toykernel.plugins.gissearch2.AppGisSearch
 */
public class AppGisSearch extends BaseInitAble implements IGuiModule
{
	protected ICliConfigProvider conf_provider;
	protected IModule cmdprovider;
	protected  IViewControl mainmodule;
	Map<String,Set<String>> m2idset=new HashMap<String,Set<String>>();
	private JDialog searchDialog;

	public AppGisSearch(IModule cmdprovider, ICliConfigProvider conf_provider,IViewControl mainmodule)
	{
		this.cmdprovider = cmdprovider;
		this.conf_provider = conf_provider;
		this.mainmodule = mainmodule;
	}

	public AppGisSearch()
	{
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

	public String getMenuName()
	{
		return Enc.get("SEARCH");
	}

	public JMenu addMenu(JMenu inmenu) throws Exception
	{
		JMenuItem menuItem = new JMenuItem(Enc.get("SEARCH_ITEM"), KeyEvent.VK_S);
		inmenu.add(menuItem);
		menuItem.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				try
				{
					ShowDialog(new RemoteNameAdapter(cmdprovider,conf_provider),"ONAMES");
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
							m2idset.clear();

							Point pt=e.getPoint();
							HashMap<String, byte[]> parmap = new HashMap<String, byte[]>();
							ICommandBean cmdbean=new CommandBean("CRD",parmap, ICommandBean.APPCLI,"");

							MPoint mpt=mainmodule.getViewPort().getCopyConverter().getPointByDstPoint(pt);

							parmap.put("SCRDX",String.valueOf((int)Math.round(mpt.x)).getBytes());
							parmap.put("SCRDY",String.valueOf((int)Math.round(mpt.y)).getBytes());
							IAnswerBean answr = execute(cmdbean);
							if (answr!=null && answr.getCommand().getCommand().equals("CRD"))
							{
								FoundObjs fobjs= (FoundObjs) new FoundObjs0AHelper().createChannelObj(answr.getbAnswer());

								Set<String> set= new TreeSet<String>();
								for (int i = 0; i < fobjs.objs.length; i++)
								{
									FoundObj foundObj = fobjs.objs[i];
									set.add(foundObj.nameobj);

									Set<String> sids=m2idset.get(foundObj.nameobj);
									if (sids==null)
										m2idset.put(foundObj.nameobj,sids=new HashSet<String>());
									sids.addAll(Arrays.asList(foundObj.objids));
								}
								Storage<String> stor = new Storage<String>();
								stor.addToStorage(set);
								ShowDialog(stor,"OIDS");

								System.out.println("fobjs.objs.length:"+fobjs.objs.length);


							}

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

	private void resetSelection() throws Exception
	{
		setSelectByName(null,"OIDS");
	}

	protected void ShowDialog(IReadStorage<String> stor,String command)
	{

		if (searchDialog != null && searchDialog.isShowing())
			searchDialog.setVisible(false);

		searchDialog = new JDialog();
		AttrForm attrForm = new AttrForm(new StringStorageManipulations(stor), searchDialog, this,command);
		ImageIcon icon = ImgResources.getIconByName("images/find.png", "SearchGis2");
		if (icon!=null)
		{
			Frame frame = (Frame) searchDialog.getOwner();
			if (frame!=null)
				frame.setIconImage(icon.getImage());
		}
		searchDialog.setTitle(Enc.get("SEARCH_ITEM"));//TODO воспользоваться конвертором для имен
		searchDialog.setSize(300, 500);
		searchDialog.setVisible(true);
		attrForm.setByCuretEvent(true);

	}
	public JToolBar addInToolBar(JToolBar systemtoolbar)
	{
		JButton button = new JButton();
		ImageIcon icon = ImgResources.getIconByName("images/ejbFinderMethod.png", "SearchGis");
		if (icon!=null)
			button.setIcon(icon);

		button.setToolTipText(Enc.get("SEARCH_ITEM"));//TODO воспользоваться конвертором для имен
		button.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{

				try
				{ //Будем обрабатывать команду поска по имени
					ShowDialog(new RemoteNameAdapter(cmdprovider,conf_provider),"ONAMES");
				}
				catch (Exception e1)
				{
					e1.printStackTrace();
				}
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
		return "AppGisSearch";
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

	public void setSelectByName(String currentName,String currentcmd) throws Exception
	{

		IAnswerBean answer = null;
		if (currentcmd.equalsIgnoreCase("OIDS"))
		{
			Set<String> oids=m2idset.get(currentName);
			if (oids==null)
				oids=new HashSet<String>();
			SelObjIds selobj = new SelObjIds(oids.toArray(new String[oids.size()]));
			byte[] bt=new SelObjIds0AHelper().serialChannelObj(selobj);

			HashMap<String, byte[]> parmap = new HashMap<String, byte[]>();
			ICommandBean cmdbean=new CommandBean("OIDS",parmap, ICommandBean.APPCLI,"");
			parmap.put("OIDS", Base64.encode(bt));
			answer = execute(cmdbean);
		}
		else if (currentcmd.equalsIgnoreCase("ONAMES"))
		{
			HashMap<String, byte[]> parmap = new HashMap<String, byte[]>();
			ICommandBean cmdbean=new CommandBean("ONAMES",parmap, ICommandBean.APPCLI,"");
			byte[] data = new String0AHelper().serialChannelObj(currentName);
			parmap.put("ONAMES", Base64.encode(data));
			answer = execute(cmdbean);

		}

		if (answer!=null)
		{
			TObjPoints objpnts= (TObjPoints) new TObjPoints0AHelper().createChannelObj(answer.getbAnswer());

//TODO Пока Устанавливаем на первую точку остальные игнорируем
			if (objpnts!=null && objpnts.objs.length>0)
			{
//TODO Сделать установку центральной точки свйоством порта
				int x=objpnts.objs[0].pnts[0].x;
				int y=objpnts.objs[0].pnts[0].y;
//					System.out.println("taget x = " + x);
//					System.out.println("taget y = " + y);

				IViewPort vport = mainmodule.getViewPort();
				Point sz= vport.getDrawSize();
				IProjConverter converter = vport.getCopyConverter();

				MPoint bpnt = converter.getAsShiftConverter().getBindP0();
				MPoint pcenter = new MPoint(bpnt.x + sz.x/2, bpnt.y + sz.y/2);

//					System.out.println("pcenter.x = " + pcenter.x);
//					System.out.println("pcenter.y = " + pcenter.y);

				converter.getAsShiftConverter().recalcBindPointByDrawDxDy(new double[]{x-pcenter.x,y-pcenter.y});
				vport.setCopyConverter(converter);
			}
		}
		mainmodule.refresh(null);


	}
}
