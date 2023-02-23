package su.org.susgsm.readers;

/**
 * Created by IntelliJ IDEA.
 * User: Administrator
 * Date: 09.03.2008
 * Time: 14:51:18
 *
 */
public class ParseRecException extends Exception
{
	public ParseRecException()
	{
	}

	public ParseRecException(String message)
	{
		super(message);
	}

	public ParseRecException(String message, Throwable cause)
	{
		super(message, cause);
	}

	public ParseRecException(Throwable cause)
	{
		super(cause);
	}
}
