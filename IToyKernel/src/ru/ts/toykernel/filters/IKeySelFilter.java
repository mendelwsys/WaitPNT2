package ru.ts.toykernel.filters;

import java.util.Set;

/**
 * Фильтр для отбора объекта по колючу
 */

public interface IKeySelFilter extends IKeyFilter
{
	void addKey2Set(String key);

	Set<String> getKeySet();

	void setKeySet(Set<String> keyset);

	void clearKeySet();
}
