package ru.ts.utils.gui.elems;

public class EmptyProgress implements IViewProgress
{
	public String getCurrentOperation()
	{
		return "";
	}

	public void setCurrentOperation(String nameoperation)
	{
	}

	public int getMaxProgress()
	{
		return 0;
	}

	public void setMaxProgress(int maxval)
	{
	}

	public int getProgress()
	{
		return 0;
	}

	public void setProgress(double val)
	{
	}

	public boolean isTerminate()
	{
		return false;
	}

	public void setTittle(String title)
	{
	}
}
