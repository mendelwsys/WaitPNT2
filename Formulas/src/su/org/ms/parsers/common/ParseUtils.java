package su.org.ms.parsers.common;

/**
 * Created by IntelliJ IDEA.
 * User: VLADM
 * Date: 06.07.2006
 * Time: 11:21:48
 * 
 */
public class ParseUtils
{
	public static String[] split2(String src,String spliter)
	{
	    int i=src.indexOf(spliter);
		if (i<0)
			return null;
		String[] retVal= new String[2];
		retVal[0]=src.substring(0,i);
		retVal[1]=src.substring(i+1);
		return retVal;
	}
}
