package ru.ts.toykernel.plugins.geditor;


import ru.ts.toykernel.storages.IEditableStorage;
import ru.ts.toykernel.geom.IEditableGisObject;
import ru.ts.toykernel.geom.IBaseGisObject;
import ru.ts.toykernel.attrs.IDefAttr;
import ru.ts.toykernel.attrs.def.DefAttrImpl;

import javax.swing.*;
import java.awt.event.*;
import java.util.LinkedList;
import java.util.Iterator;

public class InAttribute extends JDialog
{
	private JPanel contentPane;
	private JButton buttonOK;//Ввод аттрибута
	private JButton buttonCancel;//Выход из режима
	private JButton back;//К предедущему объекту если есть
	private JButton next;//К следующему объекту если есть
	private JTextField attrValue;


	private String attrname;//Имя аттрибута для редактирования
	private IEditableStorage storage;//
	private GEditor editor;
	private LinkedList<String> storedlist = new LinkedList<String>();//Объекты которые были редактированы (для возврата к ним)
	private int indexin = 0;
	private Iterator<String> okeys;//Итератор объектов

	public InAttribute(String attrname, IEditableStorage storage, GEditor editor) throws Exception
	{
		this();
		setTitle(attrname);
		this.attrname = attrname;
		this.storage = storage;
		this.editor = editor;
		okeys = storage.getCurvesIds();

		back.setEnabled(false);
		next.setEnabled(false);
		buttonOK.setEnabled(false);

		indexin=setNextIndex(-1);
		setAttrByIndex(indexin);
	}


	protected InAttribute()
	{
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

		next.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{

				try
				{
					indexin=setNextIndex(indexin);
					setAttrByIndex(indexin);

				}
				catch (Exception e1)
				{
					e1.printStackTrace();
				}
			}
		});

		back.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				try
				{
					if (indexin>0)
					{
						indexin--;
						setAttrByIndex(indexin);
					}
					back.setEnabled(indexin>0);
				}
				catch (Exception e1)
				{
					e1.printStackTrace();
				}
			}
		});

	}

	public static void main(String[] args)
	{
		InAttribute dialog = new InAttribute();
		dialog.pack();
		dialog.setVisible(true);
		System.exit(0);
	}

	private void setAttrByIndex(int indexin)
			throws Exception
	{
		if (indexin<storedlist.size() && indexin>=0)
		{
			IEditableGisObject eo = storage.getEditableObject(storedlist.get(indexin));
			IDefAttr iDefAttr = eo.getObjAttrs().get(attrname);
			if (iDefAttr !=null && iDefAttr.getValue()!=null)
				attrValue.setText(iDefAttr.getValue().toString());
			else
				attrValue.setText("");
			editor.setSelectedByCurveId(eo.getParentId());
		}
	}

	/**
	 * Установить следующий индекс объекта начиная с указанного
	 * @param indexin - указанный объект
	 * @return индекс нового объекта
	 * @throws Exception -
	 */
	private int setNextIndex(int indexin)
			throws Exception
	{
		indexin++;
		if (indexin >= storedlist.size()-1)
		{//Ищем следущий объект

			next.setEnabled(false);
			while (okeys.hasNext())
			{
				String curveId = okeys.next();
				IBaseGisObject eo = storage.getBaseGisByCurveId(curveId);
				IDefAttr iDefAttr = eo.getObjAttrs().get(attrname);
				if (iDefAttr == null || (iDefAttr.getValue() instanceof String && (((String) iDefAttr.getValue()) == null || ((String) iDefAttr.getValue()).length() == 0)))
				{
					storedlist.add(curveId);
					if (indexin<=storedlist.size()-2)
					{
						next.setEnabled(true);
						break;
					}
				}
			}
		}

		if (indexin >= storedlist.size())
			indexin=storedlist.size() - 1;

		back.setEnabled(indexin>0);
		buttonOK.setEnabled(indexin>=0 && indexin<storedlist.size());

		return indexin;
	}

	private void onOK()
	{
		try
		{
			String value = attrValue.getText();
			IEditableGisObject eo = storage.getEditableObject(storedlist.get(indexin));
			eo.setCurveAttr(new DefAttrImpl(attrname, value));
			editor.commit();
			int ix_was = indexin;
			indexin=setNextIndex(ix_was);
			if (ix_was!=indexin)
				setAttrByIndex(indexin);
			else
				dispose();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	private void onCancel()
	{
		dispose();
	}
}
