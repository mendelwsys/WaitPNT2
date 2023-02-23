package su.org.ms.parsers.mathcalc;

import su.org.ms.parsers.common.CalcException;

/**
 * Created by IntelliJ IDEA.
 * User: Administrator
 * Date: 11.08.2007
 * Time: 14:33:25
 * To change this template use File | Settings | File Templates.
 */
public interface IToken
{
	String getRepresent()
			;

	boolean isFunction()
			;

	String getToken()
			;

	double getValue() throws CalcException
			;

	int getNrule()
			;
}
