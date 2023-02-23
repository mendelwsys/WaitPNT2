package ru.ts.conv.rmp_t;

import ru.ts.utils.data.Pair;

/**
 * Введено что бы несколько вариантов генератора попробывать
 */
public interface ISheemProvider
{
	Pair<String,String> getHiLoRange(Integer level);

	String getScheemValByPair(Pair<String,String> rgn2type,String propName);

	String getStorNameByPair(Integer level,Pair<String,String> rgn2type);
}
