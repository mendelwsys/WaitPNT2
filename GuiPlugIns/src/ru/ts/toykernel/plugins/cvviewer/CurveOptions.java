package ru.ts.toykernel.plugins.cvviewer;


import ru.ts.utils.gui.tables.IHeaderSupplyer;
import ru.ts.utils.gui.tables.THeader;
import ru.ts.utils.IOperation;
import ru.ts.toykernel.gui.IViewControl;
import ru.ts.toykernel.geom.IBaseGisObject;
import ru.ts.toykernel.geom.IEditableGisObject;
import ru.ts.toykernel.attrs.IAttrs;
import ru.ts.toykernel.attrs.IDefAttr;
import ru.ts.toykernel.attrs.def.DefAttrImpl;
import ru.ts.toykernel.consts.INameConverter;
import ru.ts.toykernel.consts.KernelConst;
import ru.ts.toykernel.storages.IBaseStorage;
import ru.ts.toykernel.storages.INodeStorage;
import ru.ts.toykernel.storages.IEditableStorage;
import su.mwlib.utils.Enc;

import javax.swing.*;
import javax.swing.event.TableModelEvent;
import javax.swing.table.TableModel;
import javax.swing.table.AbstractTableModel;
import java.awt.event.*;
import java.util.*;


public class CurveOptions
		extends JDialog
{
	private JPanel contentPane;
	private JButton buttonOK;
	private JButton buttonCancel;
	private JTable optionsTable;
	private JButton addRowButton;
	private JButton delRowButton;
	private JScrollPane scrollPane;
	private JComboBox gisObjCombox;


	private boolean iseditable=false;
	private boolean waschange=false;
	private IHeaderSupplyer headersupplyer;

	private Map<String, List<String>> attrlist = new HashMap<String, List<String>>();
	private List<IBaseGisObject> gisObjsList;
	private IViewControl mainmodule;



	public CurveOptions(IViewControl mainmodule, List<IBaseGisObject> gisObjsList, IHeaderSupplyer headersupplyer1,String attrasname) throws Exception
	{
		this.gisObjsList = gisObjsList;
		this.mainmodule = mainmodule;
		INameConverter nameConverter = mainmodule.getProjContext().getNameConverter();

		for (IBaseGisObject iBaseGisObject : gisObjsList)
		{
			IAttrs objAttrs = iBaseGisObject.getObjAttrs();
			IDefAttr defAttr;
			String curveName = "";
			if (
					objAttrs != null
					&&
					attrasname!=null
					&&
					(defAttr = objAttrs.get(nameConverter.codeAttrNm2StorAttrNm(attrasname))) != null
					&&
					defAttr.getValue() != null
				)
				curveName = defAttr.getValue().toString();

			String viewString = curveName + "[" + iBaseGisObject.getCurveId() + "]";
			gisObjCombox.addItem(viewString);
		}

		if (gisObjsList.size() > 0)
			setAttrList(0);

		gisObjCombox.addItemListener(new ItemListener()
		{

			private int index = 0;

			public void itemStateChanged(ItemEvent e)
			{
				int curindex = gisObjCombox.getSelectedIndex();
				if (index != curindex)
				{
					saveChangedParameters(index);
					index = curindex;
					setAttrList(index);
					optionsTable.tableChanged(new TableModelEvent(optionsTable.getModel(), TableModelEvent.HEADER_ROW));
				}
			}

		});


		this.headersupplyer = headersupplyer1;
		this.setTitle(nameConverter.codeAttrNm2ViewNm(ModuleConst.CAPSOBJOPTIONS));

		setContentPane(contentPane);
		setModal(true);
		getRootPane().setDefaultButton(buttonOK);

		buttonOK.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				onOK();
			}
		});

		buttonCancel.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				onCancel();
			}
		});

// call onCancel() when cross is clicked
		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
		addWindowListener(new WindowAdapter()
		{
			public void windowClosing(WindowEvent e)
			{
				onCancel();
			}
		});

// call onCancel() on ESCAPE
		contentPane.registerKeyboardAction(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				onCancel();
			}
		}, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);



		TableModel dataModel = new AbstractTableModel()
		{
			public int getColumnCount()
			{
				return headersupplyer.getOptionsRepresent().length;
			}

			public int getRowCount()
			{
//				List<IBaseGisObject> objs = CurveOptions.this.gisObjsList;
//				if (objs.size() > 0)
//				{
//					IAttrs objAttrs = objs.get(gisObjCombox.getSelectedIndex()).getObjAttrs();
//					if (objAttrs != null)
//						return objAttrs.size();
//				}
				List<String> ll = attrlist.get(ModuleConst.ATTRNAME);
				if (ll!=null)
					return ll.size();
				return 0;
			}

			public void setValueAt(Object obj, int row, int col)
			{
				THeader tblheader = headersupplyer.getOptionsRepresent()[col];
				waschange=tblheader.setValueAt(obj, col, row, CurveOptions.this.attrlist);
			}

			public boolean isCellEditable(int rowIndex, int columnIndex)
			{
				return CurveOptions.this.iseditable;
			}

			public Class getColumnClass(int col)
			{
				THeader tblheader = headersupplyer.getOptionsRepresent()[col];
				return tblheader.getClassValue();
			}

			public Object getValueAt(int row, int col)
			{
				THeader tblheader = headersupplyer.getOptionsRepresent()[col];
				return tblheader.getValueAt(col, row, CurveOptions.this.attrlist);
			}

			public String getColumnName(int col)
			{
				return headersupplyer.getOptionsRepresent()[col].getNameField(0);
			}

		};

		addRowButton.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{

				if (iseditable)
				{
					List<String> attrnm = attrlist.get(ModuleConst.ATTRNAME);
					if (attrnm==null)
						attrlist.put(ModuleConst.ATTRNAME,attrnm=new LinkedList<String>());
					List<String> attrval = attrlist.get(ModuleConst.ATTRVALUE);
					if (attrval==null)
						attrlist.put(ModuleConst.ATTRVALUE,attrval=new LinkedList<String>());


					String attrname = "NEWATTRNAME";
					int i=0;
					while(attrnm.contains(attrname))
						attrname+=i++;

					attrnm.add(attrname);
					attrval.add("");


					optionsTable.tableChanged(new TableModelEvent(optionsTable.getModel(), TableModelEvent.INSERT));
					int selrow = attrnm.size() - 1;
					optionsTable.setRowSelectionInterval(selrow, selrow);
					delRowButton.setEnabled(true);
					waschange=true;
					refreshAll();
				}
			}
		});

		delRowButton.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				int selrow = optionsTable.getSelectedRow();
				if (selrow >= 0)
				{
					List<String> attrnm = attrlist.get(ModuleConst.ATTRNAME);
					if (attrnm!=null)
						attrnm.remove(selrow);
					List<String> attval = attrlist.get(ModuleConst.ATTRVALUE);
					if (attval!=null)
					{
						attval.remove(selrow);
						if (attval.size()>selrow)
							optionsTable.setRowSelectionInterval(selrow, selrow);
						else if ((selrow=attval.size())>0)
							optionsTable.setRowSelectionInterval(selrow-1, selrow-1);
					}
					delRowButton.setEnabled(attrnm!=null && attrnm.size()>0);
					waschange=true;
					refreshAll();
				}

			}
		});

		optionsTable.registerKeyboardAction(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				buttonOK.requestFocus();
			}
		}, KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), JComponent.WHEN_FOCUSED);

		optionsTable.setModel(dataModel);
	}

	public static void main(String[] args)
	{
//		LinkedList<Map<String, Object>> layersopt = new LinkedList<Map<String, Object>>();
//		HashMap<String, Object> row = new HashMap<String, Object>();
//		row.put(ModuleConst.ATTRNAME, Enc.get("NAME"));
//		row.put(ModuleConst.ATTRVALUE, Enc.get("VALUE"));
//		layersopt.add(row);
//
//		CurveOptions dialog = new CurveOptions(true, layersopt, Curve.getHeaderSupplyer());
//		dialog.pack();
//		dialog.setVisible(true);
//		System.exit(0);
	}

	public int getSelectedRow()
	{
		return optionsTable.getSelectedRow();
	}

	private void saveChangedParameters(int index)
	{
		if (waschange)
		{
			Object[] options = {Enc.get("VERIFY"), Enc.get("ROLLBACK")};
			int ch = JOptionPane.showOptionDialog(null, "Сохранить изменения?", Enc.get("CHANGES_MADE"),
					JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE, null, options, options[0]);
			if (ch==0)
			{
				try
				{
					IBaseGisObject baseGisObject = CurveOptions.this.gisObjsList.get(index);
					IBaseStorage stor = getStorageByCurve(baseGisObject.getCurveId());
					IEditableGisObject edobj = ((IEditableStorage) stor).getEditableObject(baseGisObject.getCurveId());
					Map<String,IDefAttr> sattrs=new HashMap<String,IDefAttr>();
					List<String> names=attrlist.get(ModuleConst.ATTRNAME);
					List<String> values=attrlist.get(ModuleConst.ATTRVALUE);
					for (int i = 0; i < names.size(); i++)
						sattrs.put(names.get(i),new DefAttrImpl(names.get(i),values.get(i)));
					edobj.setCurveAttrs(sattrs);
					((IEditableStorage) stor).commit();
				}
				catch (Exception e1)
				{
					IOperation.showError(e1, Enc.get("ERROR"));
				}
			}
			waschange=false;
		}
	}

	private void setAttrList(int index)
	{
		IBaseGisObject baseGisObject = gisObjsList.get(index);

		setEditableStorageF(baseGisObject);

		IAttrs objAttrs = baseGisObject.getObjAttrs();
		attrlist.clear();

		if (objAttrs != null)
		{
			List<String> names = new LinkedList<String>();
			List<String> ldef = new LinkedList<String>();
			for (String key : objAttrs.keySet())
			{
				String name = objAttrs.get(key).getName();
				Object value = objAttrs.get(key).getValue();
				names.add(name);
				if (value != null)
					ldef.add(value.toString());
				else
				{
					System.out.println("name = " + name);
					ldef.add("");
				}
			}
			attrlist.put(ModuleConst.ATTRNAME, names);
			attrlist.put(ModuleConst.ATTRVALUE, ldef);
			gisObjCombox.setSelectedIndex(index);
		}
		else
			delRowButton.setEnabled(false);

	}

	private void setEditableStorageF(IBaseGisObject baseGisObject)
	{
		try
		{
			iseditable=false;
			IBaseStorage iBaseStorage = getStorageByCurve(baseGisObject.getCurveId());
			iseditable=iBaseStorage instanceof IEditableStorage;
		}
		catch (Exception e)
		{//
		}
		finally
		{
			addRowButton.setVisible(iseditable);
			delRowButton.setVisible(iseditable);
			buttonOK.setVisible(iseditable);
		}
	}

	private IBaseStorage getStorageByCurve(String curveId)
			throws Exception
	{
		IBaseStorage iBaseStorage = mainmodule.getProjContext().getStorage();
		if (iBaseStorage instanceof INodeStorage)
			iBaseStorage=((INodeStorage)iBaseStorage).getStorageByCurveId(curveId);
		else if (iBaseStorage.getBaseGisByCurveId(curveId)==null)
			iBaseStorage=null;
		return iBaseStorage;
	}

	private void refreshAll()
	{
		optionsTable.revalidate();
		optionsTable.repaint();

		scrollPane.revalidate();
		scrollPane.repaint();
	}

	private void onOK()
	{
		int index = gisObjCombox.getSelectedIndex();
		if (index>=0)
			saveChangedParameters(index);
		dispose();
	}

	private void onCancel()
	{
		dispose();
	}

	public void pack()
	{
		super.pack();
		optionsTable.requestFocus();
		if (optionsTable.getRowCount() > 0)
			optionsTable.setRowSelectionInterval(0, 0);
	}

}
