package su.org.ms.parsers.common;

/**
 * Created by IntelliJ IDEA.
 * User: VLADM
 * Date: 06.05.2006
 * Time: 12:06:02
 * 
 */
public class LexException extends Exception
{
//  public LexException()
//  {
//  }
	
  public LexException(String msg)
  {
	  super (msg);
  }

	public LexException(Throwable cause)
	{
		super(cause);
	}
}
