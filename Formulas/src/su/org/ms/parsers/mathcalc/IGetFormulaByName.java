package su.org.ms.parsers.mathcalc;

import su.org.ms.parsers.common.ParserException;

/**
 * Created by IntelliJ IDEA.
 * User: Administrator
 * Date: 11.08.2007
 * Time: 20:38:42
 * Implement this interface for getting formulas or values in double format (i.e 12.13 3.12344)
 * from data base
 */
public interface IGetFormulaByName
{
	String getFormulaByName(String parName) throws ParserException;
}
