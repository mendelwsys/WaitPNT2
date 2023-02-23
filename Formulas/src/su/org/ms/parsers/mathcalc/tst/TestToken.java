package su.org.ms.parsers.mathcalc.tst;

import su.org.ms.parsers.mathcalc.IToken;
import su.org.ms.parsers.common.CalcException;

/**
 * Created by IntelliJ IDEA.
 * User: Administrator
 * Date: 12.08.2007
 * Time: 14:21:33
 * To change this template use File | Settings | File Templates.
 */
public class TestToken implements IToken
{
	String token;

	public TestToken(String token)
	{
		this.token = token;
	}

	public String getRepresent()
	{
		if (token.equals("op_a"))
			return "+";
		else if (token.equals("op_m"))
			return "*";
		else
			return token;
	}

	public boolean isFunction()
	{
		return false;  //To change body of implemented methods use File | Settings | File Templates.
	}

	public String getToken()
	{
		return token;
	}

	public double getValue() throws CalcException
	{
		return 3;//For test calculate
	}

	public int getNrule()
	{
		return -1;
	}
}
