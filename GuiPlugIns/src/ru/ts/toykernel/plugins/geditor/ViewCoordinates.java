package ru.ts.toykernel.plugins.geditor;

import ru.ts.toykernel.gui.util.GuiFormEncoder;
import ru.ts.utils.data.Pair;
import ru.ts.gisutils.algs.common.MPoint;

import javax.swing.*;
import java.awt.event.*;

public class ViewCoordinates extends JDialog
{
	private JPanel contentPane;
	private JButton buttonOK;
	private JButton buttonCancel;
	private JTextField drawX;
	private JTextField drawY;
	private JTextField projX;
	private JTextField projY;
	private GEditor editor;

	public ViewCoordinates(GEditor editor,String title)
	{
		this();
		this.editor = editor;
		this.setTitle(title);
		setBEnable(false);//editor.newobj!=null && editor.indexp>=0);
		GuiFormEncoder.getInstance().rec(contentPane);
	}

	public ViewCoordinates()
	{
		setContentPane(contentPane);
		setModal(false);
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

		drawX.addFocusListener(new FocusAdapter()
		{
			public void focusLost(FocusEvent e)
			{
				syncPoints(true);
			}
		});

		drawY.addFocusListener(new FocusAdapter()
		{
			public void focusLost(FocusEvent e)
			{
				syncPoints(true);
			}
		});


		projX.addFocusListener(new FocusAdapter()
		{
			public void focusLost(FocusEvent e)
			{
				syncPoints(false);
			}
		});

		projY.addFocusListener(new FocusAdapter()
		{
			public void focusLost(FocusEvent e)
			{
				syncPoints(false);
			}
		});

		GuiFormEncoder.getInstance().rec(contentPane);
	}

	public static void main(String[] args)
	{
		ViewCoordinates dialog = new ViewCoordinates();
		dialog.pack();
		dialog.setVisible(true);
		System.exit(0);
	}

	private void syncPoints(boolean drw2proj)
	{
		try
				{
					Pair<String, String> drwx2y = new Pair<String, String>(drawX.getText(), drawY.getText());
					Pair<String, String> projx2y = new Pair<String, String>(projX.getText(), projY.getText());
					editor.syncPointWithDstPoint(drwx2y, projx2y,drw2proj);
					drawX.setText(drwx2y.first);
					drawY.setText(drwx2y.second);
					projX.setText(projx2y.first);
					projY.setText(projx2y.second);

		}
		catch (Exception e1)
		{
			e1.printStackTrace();
		}
	}

	private void onOK()
	{
		try
		{
			editor.setDstPointByProjPair(new Pair<String,String>(projX.getText(),projY.getText()));
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	private void onCancel()
	{
// add your code here if necessary
		dispose();
	}

	public void resetPCoordinates()
	{
		projX.setText("");
		projY.setText("");
		drawX.setText("");
		drawY.setText("");
		setBEnable(false);
	}

	public void setPCoordinates(MPoint point)
	{
		projX.setText(String.valueOf(point.x));
		projY.setText(String.valueOf(point.y));
		syncPoints(false);
		setBEnable(true);
	}

	private void setBEnable(boolean isEnabled)
	{
		if (projX.isEnabled()==isEnabled)
			return;

		buttonOK.setEnabled(isEnabled);
		projX.setEnabled(isEnabled);
		projY.setEnabled(isEnabled);
		drawY.setEnabled(isEnabled);
		drawX.setEnabled(isEnabled);
		if (isEnabled && !drawX.isFocusOwner())
			drawX.grabFocus();
	}
}
