package su.org.ms.parsers.common;

/**
 * Created by IntelliJ IDEA.
 * User: Administrator
 * Date: 07.05.2006
 * Time: 13:55:00
 * To change this template use File | Settings | File Templates.
 */
public class ParserException extends Exception
{
	public ParserException(String s)
	{
		super (s);
	}

	public ParserException(Throwable cause)
	{
		super(cause);
	}
}
