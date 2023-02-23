package ru.ts.toykernel.plugins.tabfilters;

import ru.ts.factory.IFactory;
import ru.ts.factory.IParam;
import ru.ts.mapkernel.FTableStruct;
import ru.ts.toykernel.consts.INameConverter;
import ru.ts.toykernel.consts.KernelConst;
import ru.ts.toykernel.factory.BaseInitAble;
import ru.ts.toykernel.gui.IViewControl;
import ru.ts.toykernel.gui.apps.SFFacilities;
import ru.ts.toykernel.pcntxt.gui.defmetainfo.MainformMonitor;
import ru.ts.toykernel.plugins.IAnswerBean;
import ru.ts.toykernel.plugins.ICommandBean;
import ru.ts.toykernel.plugins.IGuiModule;
import ru.ts.utils.data.Pair;
import su.mwlib.utils.Enc;
import su.org.imglab.clengine.SetAggFields;
import su.org.imglab.clengine.SetFilter;
import su.org.ms.parsers.mathcalc.Parser;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.List;
import java.util.Map;

/**
 * Test module
 */
public class TabModule extends BaseInitAble implements IGuiModule
{
	public static final String MODULENAME = "TABLE_MODULE";
	public static final String VIEW_SUB_MENU_NAME = Enc.get("DISPLAY");
	private static final String MENU_COUNT_SUM = Enc.get("CALCULATE_TOTAL");
	private static final String DLG_CHOOSE_FIELDS = Enc.get("SELECT_FIELDS_TO_COUNT");
	private static final String MENU_HIDE_SUM = Enc.get("HIDE_TOTAL");
	private static final String MENU_FILTER_BY_FORMULA = Enc.get("FILTER_BY_FORMULA");
	private static final String DLG_FILTER_BY_FORMULA = Enc.get("SET_FILTER_BY_FORMULA");
	private static final String MENU_FILTER_BY_STRING = Enc.get("FILTER_BY_STRING");
	private static final String DLG_FILTER_BY_STRING = Enc.get("SET_FILTER_BY_STRING");
	private static final String DLG_CAPS_FILTER_BY_STRING = Enc.get("REGEXP");
	private IViewControl mainmodule;
	private INameConverter nameConverter;
	private Parser pr;

	public TabModule()
	{
	}

	public TabModule(IViewControl mainmodule)
	{
		this.mainmodule = mainmodule;
	}

	public String getMenuName()
	{
		return VIEW_SUB_MENU_NAME;
	}

	public JMenu addMenu(JMenu inmenu) throws Exception
	{
		final SFFacilities app = ((SFFacilities) mainmodule.getApplication());
		final FTableStruct acttbl = app.getTable();

		if (inmenu.getMenuComponentCount()>0)
			inmenu.addSeparator();

		JMenuItem  menuItem = new JMenuItem(MENU_COUNT_SUM, KeyEvent.VK_C);
		inmenu.add(menuItem);
		menuItem.addActionListener(new ActionListener()
		{
			private SetAggFields choocefield;
			public void actionPerformed(ActionEvent e)
			{
				if (choocefield==null)
				{
					choocefield=new SetAggFields(acttbl.getViewHeaders());
					choocefield.setTitle(DLG_CHOOSE_FIELDS);
				}
				choocefield.pack();
				choocefield.setVisible(true);
				if (choocefield.getValagglist()!=null)
				{
					acttbl.setAgrigate(choocefield.getValagglist());
					app.refreshTable();
				}
			}
		});

		menuItem = new JMenuItem(MENU_HIDE_SUM, KeyEvent.VK_C);
		inmenu.add(menuItem);
		menuItem.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				acttbl.setAgrigate(null);
				app.refreshTable();
			}
		});

		inmenu.addSeparator();

		menuItem = new JMenuItem(MENU_FILTER_BY_FORMULA, KeyEvent.VK_C);
		inmenu.add(menuItem);
		menuItem.addActionListener(new ActionListener()
		{
			SetFilter dialog = null;
			public void actionPerformed(ActionEvent e)
			{
				if (dialog==null)
				{
					dialog = new SetFilter(acttbl.getViewHeaders());
					dialog.setTitle(DLG_FILTER_BY_FORMULA);
					dialog.pack();
				}
				dialog.setVisible(true);
				Pair<String, String> field_formula = dialog.getFileld_formula();
				if (field_formula!=null )
				{
					try
					{
						if (pr==null)
							pr = Parser.createParser(new String[]{});
						app.resetFilter();
						Map<String, List<String>> rv = acttbl.addFilterByFormula(pr, field_formula);
						app.setFilterByKeys(rv);
						app.refreshAll();

						String second = field_formula.second;
						if (second!=null)
						{
							second=second.replace(FTableStruct.FNAME,field_formula.first);
							second=second.replace(FTableStruct.FN,field_formula.first);
						}
						String defTitle = app.getDefaultTitle();
						MainformMonitor.frame.setTitle(defTitle+Enc.get("__FILTER_BY_FORMULA_")+field_formula.first+" "+ second);
					}
					catch (Exception ex)
					{
						handleModuleException(ex);
					}

				}

			}
		});

		menuItem = new JMenuItem(MENU_FILTER_BY_STRING, KeyEvent.VK_C);
		inmenu.add(menuItem);
		menuItem.addActionListener(new ActionListener()
		{
			SetFilter dialog = null;
			public void actionPerformed(ActionEvent e)
			{
				if (dialog==null)
				{
					dialog = new SetFilter(acttbl.getViewHeaders());
					dialog.setTitle(DLG_FILTER_BY_STRING);
					dialog.setFromulaLabel(DLG_CAPS_FILTER_BY_STRING);
					dialog.pack();
				}
				dialog.setVisible(true);
				Pair<String, String> field_formula = dialog.getFileld_formula();
				if (field_formula!=null)
				{
					try {
						Map<String, List<String>> rv = acttbl.addFilterByRegExpr(field_formula);
						app.setFilterByKeys(rv);
						app.refreshAll();
						String defTitle = app.getDefaultTitle();
						MainformMonitor.frame.setTitle(defTitle+Enc.get("__FILTER_BY_STRING_")+field_formula.first+" "+field_formula.second);
					} catch (Exception ex) {
						handleModuleException(ex);
					}
				}
			}
		});

		return inmenu;
	}

	private void handleModuleException(final Exception ex)
	{
		ByteArrayOutputStream barr = new ByteArrayOutputStream();
		ex.printStackTrace(new PrintStream(barr,true));
		JOptionPane.showMessageDialog(MainformMonitor.frame,new String(barr.toByteArray()),Enc.get("FILTERING_ERROR"), JOptionPane.ERROR_MESSAGE);
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
//		JButton button = new JButton();
//		ImageIcon icon = ImgResources.getIconByName("images/unknown.png", "TestGis");
//		if (icon!=null)
//			button.setIcon(icon);
//
//		button.setToolTipText(TEST_HEADER);
//		button.addActionListener(new ActionListener()
//		{
//			public void actionPerformed(ActionEvent e)
//			{
//				execTest();
//			}
//		});
//
//		button.setMargin(new Insets( 0, 0, 0, 0 ));
//		systemtoolbar.add(button);
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
