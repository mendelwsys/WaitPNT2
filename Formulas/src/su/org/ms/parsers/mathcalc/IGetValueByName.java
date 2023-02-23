package su.org.ms.parsers.mathcalc;

import su.org.ms.parsers.common.CalcException;

/**
 * Created by IntelliJ IDEA.
 * User: Administrator
 * Date: 22.09.2007
 * Time: 20:05:04
 * To change this template use File | Settings | File Templates.
 */
public interface  IGetValueByName
{
	double getValueByName(String parName) throws CalcException;
}
