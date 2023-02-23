package ru.ts.toykernel.plugins.gissearch2;


import ru.ts.toykernel.gui.util.GuiFormEncoder;
import ru.ts.utils.data.StringStorageManipulations;

import javax.swing.*;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.ListSelectionEvent;
import java.awt.event.*;
import java.util.LinkedList;
import java.util.List;

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
	private String command;
	private AppGisSearch searchmodule;
	private JTextField currentName;
	private JList attrNames;
	private JPanel mainit;
	private JComboBox attributeName;
	private JButton SearchByPrefix;
	private int winstartpos;
	private String oldtext;
	/**
	 * @param namesstorage - имена для выбора объекта по имени
	 * @param dialog	   - диалог который заполняется текущей паенелью
	 * @param searchmodule - поисковый модуль
	 * @param command - обрабатываемая команда
	 */
	public AttrForm(StringStorageManipulations namesstorage, JDialog dialog, AppGisSearch searchmodule,String command)
	{
		this.storageman = namesstorage;
		this.searchmodule = searchmodule;

		this.storageman=namesstorage;
		this.command = command;

		attributeName.addItem("name");
		attributeName.setSelectedIndex(0);

//		currentName.addCaretListener(new CaretListener()
//		{
//
//			public void caretUpdate(CaretEvent e)
//			{
//				//setByCuretEvent(false);
//			}
//		});

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
						if (value==null &&
							listdata!=null
							&& listdata.size()>0)
								value=listdata.get(0);

						if (AttrForm.this.storageman.namesstorage.size()>0 && value !=null)
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
					int listindex=attrNames.getSelectedIndex();

					storageman.resetsearchstorage();

					currentName.setText(text);
					AttrForm.this.storageman.namesstorage.setposon(winstartpos+listindex);
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
						if (attrNames.getSelectedIndex()==0
							&& AttrForm.this.storageman.namesstorage.hasPrevElements())
						{
							AttrForm.this.storageman.namesstorage.prevElement();
							winstartpos--;
							setCursorByStoragePos();
						}
						break;
					case 40://вниз
						if (listdata!=null
							&& listdata.size()>0
							&& attrNames.getSelectedIndex()==listdata.size()-1
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
				}

			}

			public void keyReleased(KeyEvent e)
			{
			}
		});

		attrNames.addMouseListener(new MouseListener()
		{

			final static long DELTAM =300;
			long tm=System.currentTimeMillis();

			public void mouseClicked(MouseEvent e)
			{
				long exp = (System.currentTimeMillis() - tm);
				if (DELTAM > exp)
					setPictureByEnter();
				tm=System.currentTimeMillis();
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

		SearchByPrefix.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				setByCuretEvent(true);
			}
		});

		GuiFormEncoder.getInstance().rec(mainit);

		dialog.setContentPane(mainit);
		dialog.pack();
	}

	public void setNamesstorage(StringStorageManipulations namesstorage)
	{
		this.storageman=namesstorage;
		if (currentName.getText().length()>0)
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
			String text=currentName.getText();

			if ((!storageman.islock && text.startsWith(storageman.currentString) && storageman.currentString.length()+1==text.length()))
			{
				storageman.setAttrNameByLetter(text.charAt(storageman.currentString.length()));
				setCursorPos(!storageman.islock);
			}
			else if (!text.equals(oldtext))
			{
				if (this.storageman.namesstorage.size()>0)
					this.storageman.namesstorage.setposon(0);

				storageman.resetsearchstorage();
				storageman.setAttrByString(text);
				setCursorPos(!storageman.islock);
			}
			oldtext=text;
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
				try
				{
					searchmodule.setSelectByName(currentName,command);
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}
		}
	}


	private void setCursorPos( boolean issetcursor)
	{
		attributeName.removeAll();
		attrNames.setSelectedIndex(0);
		listdata = new LinkedList<String>();
		int size = this.storageman.namesstorage.size();
		if (size >0)
		{
			int curpos = this.storageman.namesstorage.getCurpos();

			winstartpos = curpos - INDEXWIDTH;
			if (winstartpos<0)
				winstartpos=0;


			for (int i = winstartpos; i<size && i < winstartpos+2*INDEXWIDTH; i++)
				if (i>=0)
					listdata.add(this.storageman.namesstorage.get(i));

			attrNames.setListData(listdata.toArray());
			if (issetcursor)
				attrNames.setSelectedIndex(curpos-winstartpos);
		}

	}

	private void setCursorByStoragePos()
	{
		attributeName.removeAll();
		listdata = new LinkedList<String>();
		int size = storageman.namesstorage.size();
		if (size >0)
		{
			int curpos = storageman.namesstorage.getCurpos();

			for (int i = winstartpos; i<size && i < winstartpos+2*INDEXWIDTH; i++)
				if (i>=0)
					listdata.add(storageman.namesstorage.get(i));
			attrNames.setListData(listdata.toArray());

			attrNames.setSelectedIndex(curpos-winstartpos);
		}
		else
		{
			attrNames.setListData(listdata.toArray());
			currentName.setText("");
		}

	}

}
