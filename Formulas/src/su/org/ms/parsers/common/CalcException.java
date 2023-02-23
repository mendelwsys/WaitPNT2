package su.org.ms.parsers.common;

/**
 * Created by IntelliJ IDEA.
 * User: VLADM
 * Date: 06.05.2006
 * Time: 14:53:51
 * To change this template use File | Settings | File Templates.
 */
public class CalcException
		extends Exception
{
	public CalcException()
	{
	}

	public CalcException(String msg)
	{
		super(msg);
	}

	public CalcException(Throwable cause)
	{
		super(cause.getMessage());
		cause.printStackTrace();

	}


}
