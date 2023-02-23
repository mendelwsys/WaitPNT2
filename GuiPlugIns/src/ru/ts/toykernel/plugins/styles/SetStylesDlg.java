package ru.ts.toykernel.plugins.styles;

import ru.ts.toykernel.gui.util.GuiFormEncoder;
import ru.ts.utils.gui.tables.IHeaderSupplyer;
import ru.ts.utils.gui.tables.THeader;
import ru.ts.toykernel.pcntxt.IProjContext;

import ru.ts.toykernel.drawcomp.rules.def.CommonStyle;
import ru.ts.toykernel.drawcomp.rules.def.CnStyleRuleImpl;
import ru.ts.toykernel.drawcomp.ILayer;
import ru.ts.toykernel.drawcomp.IDrawObjRule;
import ru.ts.toykernel.gui.IViewControl;
import su.mwlib.utils.Enc;

import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.table.TableModel;
import javax.swing.table.AbstractTableModel;
import java.util.LinkedList;
import java.util.List;


public class SetStylesDlg
		extends JDialog
{
	public boolean isOk = false;
	LinkedList<byte[]> b_Styles = new LinkedList<byte[]>();
	private JPanel contentPane;
	private JButton buttonOK;
	private JButton buttonCancel;
	private JTextField colorLine;
	private JTextField colorFill;
	private JTextField lineStyle;
	private JTextField scaleFrom;
	private JTextField scaleTo;
	private ColorPanel colorLinePane;
	private ColorPanel colorFillPane;
	private JTable tlayers;
	private JButton buttonRefresh;
	private JButton setAllButton;
	private JButton resetAllButton;
	private JTextField lineThickness;
	private JTextField colorBox;
	private JTextField colorBackground;
	private ColorPanel colorBackgroundPane;
	private ColorPanel colorBoxPane;
	private IHeaderSupplyer headersupplyer;
	private IViewControl mainmodule;
	private IProjContext projContext;
	TableModel dataModel = new AbstractTableModel()
	{
		public int getColumnCount()
		{
			return headersupplyer.getOptionsRepresent().length;
		}

		public int getRowCount()
		{
			return projContext.getLayerList().size();
		}

		public boolean isCellEditable(int row, int col)
		{
			return headersupplyer.getOptionsRepresent()[col].isEditable(col, row, getValueAt(row, col));
		}

		public Class getColumnClass(int col)
		{
			THeader tblheader = headersupplyer.getOptionsRepresent()[col];
			return tblheader.getClassValue();
		}

		public void setValueAt(Object val, int row, int col)
		{
			THeader tblheader = headersupplyer.getOptionsRepresent()[col];
			tblheader.setValueAt(val, col, row, projContext.getLayerList().get(row).getLrAttrs());
		}

		public Object getValueAt(int row, int col)
		{
			THeader tblheader = headersupplyer.getOptionsRepresent()[col];
			return tblheader.getValueAt(col, row, projContext.getLayerList().get(row).getLrAttrs());
		}

		public String getColumnName(int col)
		{
			return headersupplyer.getOptionsRepresent()[col].getNameField(0);
		}

	};
	private MyFocusAdapter fAColorFill;
	private MyFocusAdapter fAColorLine;


	public SetStylesDlg(IViewControl mainmodule, IHeaderSupplyer headsuplyer) throws Exception
	{
		this.mainmodule = mainmodule;
		this.projContext = mainmodule.getProjContext();
		this.headersupplyer = headsuplyer;
		setContentPane(contentPane);
		setTitle(Enc.get("SET_LAYER_DISPLAY_STYLES"));
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


		fAColorLine = new MyFocusAdapter(colorLinePane, colorLine);
		fAColorFill = new MyFocusAdapter(colorFillPane, colorFill);

		colorLine.addFocusListener(fAColorLine);
		colorFill.addFocusListener(fAColorFill);

		MyFocusAdapter fAColorBackground = new MyFocusAdapter(colorBackgroundPane, colorBackground);
		colorBackground.addFocusListener(fAColorBackground);
		MyFocusAdapter fAColorBox = new MyFocusAdapter(colorBoxPane, this.colorBox);
		this.colorBox.addFocusListener(fAColorBox);


		colorBackground.setText(Integer.toHexString(projContext.getProjMetaInfo().getBackgroundColor()));
		colorBox.setText(Integer.toHexString(projContext.getProjMetaInfo().getBoxColor()));

		fAColorBackground.refreshcolorePane();
		fAColorBox.refreshcolorePane();

		buttonRefresh.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				try
				{
					ILayer lr = SetStylesDlg.this.projContext.getLayerList().get(tlayers.getSelectedRow());

					IDrawObjRule drwRule = lr.getDrawRule();
					if (drwRule instanceof CnStyleRuleImpl)
					{
						CommonStyle style = ((CnStyleRuleImpl) drwRule).getDefStyle();
						dlg2Style(style);
						SetStylesDlg.this.mainmodule.refresh(null);
					}
				}
				catch (Exception e1)
				{//
				}
			}
		});

		setAllButton.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				setLayersVisible(true);
			}
		});

		resetAllButton.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				setLayersVisible(false);
			}
		});


		tlayers.setModel(dataModel);
		tlayers.getSelectionModel().addListSelectionListener(new ListSelectionListener()
		{
			int oldIndex = -1;

			public void valueChanged(ListSelectionEvent e)
			{
				int index = tlayers.getSelectedRow();
				if (oldIndex != index)
				{
					ILayer lr = SetStylesDlg.this.projContext.getLayerList().get(index);
					IDrawObjRule drwRule = lr.getDrawRule();
					if (drwRule instanceof CnStyleRuleImpl)
					{
						CommonStyle style = ((CnStyleRuleImpl) drwRule).getDefStyle();
						style2Dlg(style);
						fAColorLine.refreshcolorePane();
						fAColorFill.refreshcolorePane();
					}
					else
						style2Dlg(null);
				}
			}
		});
		tlayers.setRowSelectionInterval(0, 0);
		GuiFormEncoder.getInstance().rec(contentPane);
	}

	public static void main(String[] args)
	{
		try
		{
			SetStylesDlg dialog = new SetStylesDlg(null, null);
			dialog.pack();
			dialog.setVisible(true);
			System.exit(0);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	private void setLayersVisible(boolean isvisible)
	{
		try
		{
			List<ILayer> lrlist = this.projContext.getLayerList();
			for (ILayer lr : lrlist)
				lr.setVisible(isvisible);
			tlayers.repaint();
		}
		catch (Exception e1)
		{//
		}
	}

	private void onOK()
	{
		isOk = true;
		try
		{
			mainmodule.refresh(null);
		}
		catch (Exception e)
		{//
		}
		dispose();
	}

	private void onCancel()
	{
		dispose();
	}

	public void style2Dlg(CommonStyle data)
	{
		if (data != null)
		{

			String colorLine = data.getsHexColorLine();
			this.colorLine.setEnabled(true);
			this.colorLine.setText(colorLine);
			colorLinePane.setColor(colorLine);

			String colorFill = data.getsHexColorFill();
			this.colorFill.setEnabled(true);
			this.colorFill.setText(colorFill);
			colorFillPane.setColor(colorFill);

			this.lineThickness.setEnabled(true);
			lineThickness.setText(data.getsLineThickness());

			this.scaleTo.setEnabled(true);
			this.scaleFrom.setEnabled(true);

			scaleTo.setText(data.getScaleHiRange());
			scaleFrom.setText(data.getScaleLowRange());

			lineStyle.setText(data.getsHexLineStyle());
			this.lineStyle.setEnabled(true);

			colorLinePane.setVisible(true);
			colorFillPane.setVisible(true);
		}
		else
		{
			this.colorLine.setText("");
			this.colorLine.setEnabled(false);
			this.colorFill.setText("");
			this.colorFill.setEnabled(false);
			this.lineThickness.setText("");
			this.lineThickness.setEnabled(false);
			this.scaleTo.setText("");
			this.scaleTo.setEnabled(false);
			this.scaleFrom.setText("");
			this.scaleFrom.setEnabled(false);
			this.lineStyle.setText("");
			this.lineStyle.setEnabled(false);
			colorLinePane.setVisible(false);
			colorFillPane.setVisible(false);
		}

	}

	public void dlg2Style(CommonStyle data)
	{
		try
		{
			projContext.getProjMetaInfo().setBackgroundColor((int) Long.parseLong(colorBackground.getText(), 16));
		}
		catch (NumberFormatException e)
		{
			colorBackground.setText("0x0");
		}
		try
		{
			projContext.getProjMetaInfo().setBoxColor((int) Long.parseLong(colorBox.getText(), 16));
		}
		catch (NumberFormatException e)
		{
			colorBox.setText("0x0");
		}
		data.setsHexColorLine(colorLine.getText());
		data.setsHexColorFill(colorFill.getText());
		data.setsHexLineStyle(lineStyle.getText());
		data.setsLineThickness(lineThickness.getText());

		data.setLowRange(scaleFrom.getText());
		data.setHiRange(scaleTo.getText());
	}

	public boolean isModified(CommonStyle data)
	{
		if (colorBackground.getText() == null || !colorBackground.getText().equalsIgnoreCase(Integer.toHexString(projContext.getProjMetaInfo().getBackgroundColor())))
			return true;

		if (colorBox.getText() == null || !colorBox.getText().equalsIgnoreCase(Integer.toHexString(projContext.getProjMetaInfo().getBoxColor())))
			return true;

		if (colorLine.getText() != null ? !colorLine.getText().equals(
				data.getsHexColorLine()) : data.getsHexColorLine() != null) return true;
		if (colorFill.getText() != null ? !colorFill.getText().equals(
				data.getsHexColorFill()) : data.getsHexColorFill() != null) return true;

		if (lineStyle.getText() != null ? !lineStyle.getText().equals(
				data.getsHexLineStyle()) : data.getsHexLineStyle() != null) return true;

		if (lineThickness.getText() != null ? !lineThickness.getText().equals(
				data.getsLineThickness()) : data.getsLineThickness() != null) return true;

		if (scaleFrom.getText() != null ? !scaleFrom.getText().equals(
				data.getScaleLowRange()) : data.getScaleLowRange() != null) return true;

		if (scaleTo.getText() != null ? !scaleTo.getText().equals(
				data.getScaleHiRange()) : data.getScaleHiRange() != null) return true;

		return false;
	}

	class MyFocusAdapter extends FocusAdapter
	{
		private ColorPanel colorPane;
		private JTextField colorText;

		public MyFocusAdapter(ColorPanel colorLinePane, JTextField colorLine)
		{

			this.colorPane = colorLinePane;
			this.colorText = colorLine;
		}

		public void focusGained(FocusEvent e)
		{
			super.focusGained(e);
		}

		public void focusLost(FocusEvent e)
		{
			super.focusLost(e);

			refreshcolorePane();
		}

		private void refreshcolorePane()
		{
			try
			{
				String strcolor = colorText.getText();

				int color = (int) Long.parseLong(strcolor, 16);
				colorPane.setColor(color);
				colorPane.repaint();
			}
			catch (NumberFormatException e1)
			{
				colorText.setText("");
			}
		}

	}

}
