package ru.ts.utils.gui.elems;

/**
 * Progress viewer
 */
public interface IViewProgress
{
	/**
	 * @return name of current operation
	 */
	String getCurrentOperation();

	/**
	 * set name of current operation
	 * @param nameoperation - name of operation
	 */
	void setCurrentOperation(String nameoperation);

	/**
	 * @return maximal value of progress
	 */
	int getMaxProgress();

	/**
	 * set maximal value of progress
	 * @param maxval - maximal value
	 */
	void setMaxProgress(int maxval);

	/**
	 * get value of progress
	 * @return value of progress
	 */
	int getProgress();

	/**
	 * set progerss value of opeartion
	 * @param val - value of operation
	 */
	void setProgress(double val);

	/**
	 * @return is terminate loading
	 */
	boolean isTerminate();

	void setTittle(String title);
}