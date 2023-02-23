package ru.ts.conv;

import java.util.Iterator;

/**
 * Created by IntelliJ IDEA.
 * User: vladm
 * Date: 22.06.2011
 * Time: 13:25:37
 * Table of drawing attributes (fill and line color, styles of line and fillings)
 */
public interface IColorSheemLoader
	extends Iterator
{

//TODO Сдеалть классическую таблицу, возвращать объект стилей и получать аттрибуты из объекта 	

	void reset();

	/**
	 * set row in table style
	 * @param key -
	 * @return
	 * @throws Exception
	 */
	int findByKey(String key) throws Exception;

	String getObjectType();
	String getLineColor();
	String getFillColor();

	/**
	 * @return line stroke
	 */
	String getLineStroke();

	/**
	 * @return line thickness
	 */
	String getLineThickness();

	/**
	 * @return get name of group objects, as a rule objects group define define object layer
	 */
	String getGroupName();
}
