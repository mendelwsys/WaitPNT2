package ru.ts.forms;

import ru.ts.utils.gui.elems.IViewProgress;
import ru.ts.res.ImgResources;

import javax.swing.*;
import java.awt.*;

/**
 * Standart View progress for log time process
 */
public class StViewProgress implements IViewProgress
{
	private Uprocess dialog;
	
	public StViewProgress(String title)
	{
		dialog = new Uprocess(title);
	}

	public void setTittle(String title)
	{
		dialog.setTitle(title);
	}

	protected void initProgress()
	{
		if (!dialog.isVisible())
		{
			ImageIcon icon=ImgResources.getIconByName("images/poolball.gif","Title");
			if (icon!=null)
			{
				Frame frame = (Frame) dialog.getOwner();
				if (frame!=null)
					frame.setIconImage(icon.getImage());
			}
			dialog.buttonCancel.setVisible(false);
			dialog.pack();
			dialog.setAlwaysOnTop(true);
			dialog.setModal(true);

			new Thread()
			{
				public void run()
				{
					dialog.setLocation(500,500);
					dialog.setVisible(true);
				}
			}.start();
		}
	}

	public String getCurrentOperation()
	{
		return dialog.operation.getText();

	}

	public void setCurrentOperation(String nameoperation)
	{
		initProgress();
		dialog.operation.setText(nameoperation);
	}

	public int getMaxProgress()
	{
		return dialog.uploadProgress.getMaximum();
	}

	public void setMaxProgress(int maxval)
	{
		dialog.uploadProgress.setMaximum(maxval);
	}

	public int getProgress()
	{
		return dialog.uploadProgress.getValue();
	}

	public void setProgress(double val)
	{
		initProgress();
		if (getMaxProgress()<=val)
			dialog.dispose();
		else
			dialog.uploadProgress.setValue((int)val);

	}

	public boolean isTerminate()
	{
		return getMaxProgress()<=getProgress();
	}
}
