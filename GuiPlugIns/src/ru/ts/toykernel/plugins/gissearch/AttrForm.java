package ru.ts.toykernel.plugins.gissearch;

import javax.swing.*;
import javax.swing.event.CaretListener;
import javax.swing.event.CaretEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.ListSelectionEvent;
import java.awt.event.*;
import java.util.List;
import java.util.LinkedList;
import java.util.SortedSet;

import ru.ts.toykernel.gui.util.GuiFormEncoder;
import ru.ts.utils.data.StringStorageManipulations;

/**
 * Created by IntelliJ IDEA.
 * User: vladm
 * Date: 06.02.2007
 * Time: 13:03:53
 * Форма предназначена для ввода текстовых атрибутов графических объектов
 */
public class AttrForm
{
	private static final int INDEXWIDTH = 15;
	private List<String> listdata;
	private StringStorageManipulations storageman;
	private IGisSearch searchmodule;
	private JTextField currentName;
	private JList attrNames;
	private JPanel mainit;
	private JComboBox attributeName;
	private int winstartpos;
	private String oldtext;
	/**
	 * @param namesstorage - имена для выбора объекта по имени
	 * @param dialog	   - диалог который заполняется текущей паенелью
	 * @param searchmodule - поисковый модуль
	 */
	public AttrForm(StringStorageManipulations namesstorage, JDialog dialog, IGisSearch searchmodule)
	{
		this.storageman = namesstorage;
		this.searchmodule = searchmodule;

		SortedSet<String> attrs = searchmodule.getAttributeNames();
		String currentname = searchmodule.getCurrentName();
		int ix = 0;
		{
			int i = 0;
			for (String attr : attrs)
			{
				if (attr.equals(currentname))
					ix = i;
				attributeName.addItem(attr);
				i++;
			}
		}
		attributeName.setSelectedIndex(ix);


		attributeName.setEnabled(attrs.size() > 1);

		currentName.addCaretListener(new CaretListener()
		{

			public void caretUpdate(CaretEvent e)
			{
				setByCuretEvent(false);
			}
		});

		currentName.addKeyListener(new KeyListener()
		{

			public void keyTyped(KeyEvent e)
			{

			}

			public void keyPressed(KeyEvent e)
			{
//				System.out.println("e.getKeyCode() = " + e.getKeyCode());
				switch (e.getKeyCode())
				{
					case 10://"Enter"
						setPictureByEnter();
						break;
					case 40://Вниз (переход на список)
						Object value = attrNames.getSelectedValue();
						if (value == null &&
								listdata != null
								&& listdata.size() > 0)
							value = listdata.get(0);

						if (AttrForm.this.storageman.namesstorage.size() > 0 && value != null)
							currentName.setText(value.toString());

						attrNames.requestFocus();

						break;
				}
			}

			public void keyReleased(KeyEvent e)
			{
			}
		});

		attrNames.addListSelectionListener(new ListSelectionListener()
		{

			public void valueChanged(ListSelectionEvent e)
			{

				if (attrNames.isFocusOwner() && attrNames.getSelectedValue() != null)
				{

					String text = attrNames.getSelectedValue().toString();
					int listindex = attrNames.getSelectedIndex();

					storageman.resetsearchstorage();

					currentName.setText(text);
					AttrForm.this.storageman.namesstorage.setposon(winstartpos + listindex);

				}
			}
		});

		attrNames.addKeyListener(new KeyListener()
		{
			public void keyTyped(KeyEvent e)
			{
			}

			public void keyPressed(KeyEvent e)
			{
				switch (e.getKeyCode())
				{
					case 38://вверх
						if (attrNames.getSelectedIndex() == 0
								&& AttrForm.this.storageman.namesstorage.hasPrevElements())
						{
							AttrForm.this.storageman.namesstorage.prevElement();
							winstartpos--;
							setCursorByStoragePos();
						}
						break;
					case 40://вниз
						if (listdata != null
								&& listdata.size() > 0
								&& attrNames.getSelectedIndex() == listdata.size() - 1
								&& AttrForm.this.storageman.namesstorage.hasMoreElements()
								)
						{
							AttrForm.this.storageman.namesstorage.nextElement();
							winstartpos++;
							setCursorByStoragePos();
						}
						break;
					case 10://"Enter" (Установка для редактирования атрибутов следующего объекта)
						setPictureByEnter();
						currentName.requestFocus();
						break;
					case 127://"Del" (удаление названий из словаря)
					{
						int curpos = AttrForm.this.storageman.namesstorage.getCurpos();
						AttrForm.this.storageman.namesstorage.remove(curpos);
						setCursorByStoragePos();
					}
					break;
				}

			}

			public void keyReleased(KeyEvent e)
			{
			}
		});

		attrNames.addMouseListener(new MouseListener()
		{

			final static long DELTAM = 300;
			long tm = System.currentTimeMillis();

			public void mouseClicked(MouseEvent e)
			{
				long exp = (System.currentTimeMillis() - tm);
				if (DELTAM > exp)
					setPictureByEnter();
				tm = System.currentTimeMillis();
			}

			public void mousePressed(MouseEvent e)
			{
				//To change body of implemented methods use File | Settings | File Templates.
			}

			public void mouseReleased(MouseEvent e)
			{
				//To change body of implemented methods use File | Settings | File Templates.
			}

			public void mouseEntered(MouseEvent e)
			{
				//To change body of implemented methods use File | Settings | File Templates.
			}

			public void mouseExited(MouseEvent e)
			{
				//To change body of implemented methods use File | Settings | File Templates.
			}
		});

		dialog.setContentPane(mainit);
		GuiFormEncoder.getInstance().rec(mainit);
		dialog.pack();

		attributeName.addItemListener(new ItemListener()
		{
			public void itemStateChanged(ItemEvent e)
			{
				System.out.println("e = " + e);
			}
		});
	}

	public void setNamesstorage(StringStorageManipulations namesstorage)
	{
		this.storageman = namesstorage;
		if (currentName.getText().length() > 0)
			storageman.setAttrByString(currentName.getText());
	}

	public String getCurrentName()
	{

		return currentName.getText();
	}

	public JList getAttrNames()
	{
		return attrNames;
	}

	public JPanel getMainit()
	{
		return mainit;
	}

	public void setByCuretEvent(boolean isexternal)
	{
		if (currentName.isFocusOwner() || isexternal)
		{
			String text = currentName.getText();

			if ((!storageman.islock && text.startsWith(storageman.currentString) && storageman.currentString.length() + 1 == text.length()))
			{
				storageman.setAttrNameByLetter(text.charAt(storageman.currentString.length()));
				setCursorPos(!storageman.islock);
			}
			else if (!text.equals(oldtext))
			{
				if (this.storageman.namesstorage.size() > 0)
					this.storageman.namesstorage.setposon(0);

				storageman.resetsearchstorage();
				storageman.setAttrByString(text);
				setCursorPos(!storageman.islock);
			}
			oldtext = text;
		}
	}

	private void setPictureByEnter()
	{

		int index = attrNames.getSelectedIndex();
		if (index >= 0)
		{
			String currentName = listdata.get(index);
			this.currentName.setText(currentName);
			if (searchmodule != null)
			{
				searchmodule.setSelectByName(currentName);
			}
		}
	}


	private void setCursorPos(boolean issetcursor)
	{
		attributeName.removeAll();
		attrNames.setSelectedIndex(0);
		listdata = new LinkedList<String>();
		int size = this.storageman.namesstorage.size();
		if (size > 0)
		{
			int curpos = this.storageman.namesstorage.getCurpos();

			winstartpos = curpos - INDEXWIDTH;
			if (winstartpos < 0)
				winstartpos = 0;


			for (int i = winstartpos; i < size && i < winstartpos + 2 * INDEXWIDTH; i++)
				if (i >= 0)
					listdata.add(this.storageman.namesstorage.get(i));

			attrNames.setListData(listdata.toArray());
			if (issetcursor)
				attrNames.setSelectedIndex(curpos - winstartpos);
		}

	}

	private void setCursorByStoragePos()
	{
		attributeName.removeAll();
		listdata = new LinkedList<String>();
		int size = storageman.namesstorage.size();
		if (size > 0)
		{
			int curpos = storageman.namesstorage.getCurpos();

			for (int i = winstartpos; i < size && i < winstartpos + 2 * INDEXWIDTH; i++)
				if (i >= 0)
					listdata.add(storageman.namesstorage.get(i));
			attrNames.setListData(listdata.toArray());

			attrNames.setSelectedIndex(curpos - winstartpos);
		}
		else
		{
			attrNames.setListData(listdata.toArray());
			currentName.setText("");
		}

	}

}
