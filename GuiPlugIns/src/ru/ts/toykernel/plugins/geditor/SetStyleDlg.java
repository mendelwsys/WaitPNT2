package ru.ts.toykernel.plugins.geditor;

import ru.ts.toykernel.gui.util.GuiFormEncoder;
import ru.ts.toykernel.plugins.styles.ColorPanel;
import ru.ts.toykernel.drawcomp.rules.def.CommonStyle;
import ru.ts.toykernel.drawcomp.rules.def.CnStyleRuleImpl;
import ru.ts.toykernel.drawcomp.ILayer;
import ru.ts.toykernel.gui.IViewControl;
import su.mwlib.utils.Enc;

import javax.swing.*;
import java.awt.event.*;

public class SetStyleDlg extends JDialog
{
	private JPanel contentPane;
	private JButton buttonOK;
	private JButton buttonCancel;
	private JTextField colorLine;
	private JTextField colorFill;
	private JTextField scaleFrom;
	private JTextField scaleTo;
	private ColorPanel colorLinePane;
	private ColorPanel colorFillPane;
	private JTextField lineStyle;
	private JTextField lineThickness;
	private JButton updateb;
	private JTextField radPnt;


	private IViewControl mainmodule;
	private ILayer editlr;


	private MyFocusAdapter fAColorFill;
	private MyFocusAdapter fAColorLine;

	public SetStyleDlg(IViewControl mainmodule, ILayer editlr)
	{
		this();
		this.mainmodule=mainmodule;
		this.editlr=editlr;
		CnStyleRuleImpl drwRule = (CnStyleRuleImpl) editlr.getDrawRule();
		if (drwRule instanceof CnStyleRuleImpl)
			style2Dlg(drwRule.getDefStyle());
		GuiFormEncoder.getInstance().rec(contentPane);
	}

	public SetStyleDlg()
	{
		setContentPane(contentPane);
		setModal(true);
		setTitle(Enc.get("SET_EDIT_LAYER_DISPLAY_STYLES"));
		getRootPane().setDefaultButton(buttonOK);

		buttonOK.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				try
				{
					update();
				}
				catch (Exception e1)
				{
					e1.printStackTrace();
				}
				dispose();
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


		updateb.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				try
				{
					update();
				}
				catch (Exception e1)
				{
					e1.printStackTrace();
				}
			}
		});

		fAColorLine = new MyFocusAdapter(colorLinePane, colorLine);
		fAColorFill = new MyFocusAdapter(colorFillPane, colorFill);

		colorLine.addFocusListener(fAColorLine);
		colorFill.addFocusListener(fAColorFill);

		GuiFormEncoder.getInstance().rec(contentPane);

	}

	public static void main(String[] args)
	{
		SetStyleDlg dialog = new SetStyleDlg();
		dialog.pack();
		dialog.setVisible(true);
		System.exit(0);
	}

	private void update() throws Exception
	{
// add your code here
		CnStyleRuleImpl drwRule = (CnStyleRuleImpl) editlr.getDrawRule();
		if (drwRule instanceof CnStyleRuleImpl)
		{
			CommonStyle style=((CnStyleRuleImpl) drwRule).getDefStyle();
			if (isModified(style))
				dlg2Style(style);
			mainmodule.refresh(null);
		}
	}

	private void onCancel()
	{
// add your code here if necessary
		dispose();
	}

	public void style2Dlg(CommonStyle data)
	{
		if (data!=null)
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

			this.radPnt.setEditable(true);
			this.radPnt.setText(data.getsRadPnt());

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
			this.radPnt.setEditable(false);
			this.radPnt.setText("");
		}

	}

	public void dlg2Style(CommonStyle data)
	{
		data.setsHexColorLine(colorLine.getText());
		data.setsHexColorFill(colorFill.getText());
		data.setsHexLineStyle(lineStyle.getText());
		data.setsLineThickness(lineThickness.getText());

		data.setLowRange(scaleFrom.getText());
		data.setHiRange(scaleTo.getText());
		data.setsRadPnt(radPnt.getText());
	}

	public boolean isModified(CommonStyle data)
	{

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

		if (radPnt.getText() != null ? !radPnt.getText().equals(
				data.getsRadPnt()) : data.getsRadPnt() != null) return true;

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
