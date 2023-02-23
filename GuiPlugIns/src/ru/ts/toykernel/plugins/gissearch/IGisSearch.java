package ru.ts.toykernel.plugins.gissearch;

import ru.ts.utils.data.StringStorageManipulations;

import java.util.SortedSet;

/**
 * Created by IntelliJ IDEA.
 * User: vladm
 * Date: 02.07.2009
 * Time: 18:58:00
 * Interface beetween Attribute Form and GisSearch Module
 */
public interface IGisSearch
{
	StringStorageManipulations reInitSearch(String attrName) throws Exception;

	void resetSelection();

	void setSelectByName(String currentName);

	SortedSet<String> getAttributeNames();

	String getCurrentName();
}
