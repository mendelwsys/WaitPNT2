package su.org.ms.parsers.mathcalc;

import su.org.ms.parsers.common.ParserException;

/**
 * Created by IntelliJ IDEA.
 * User: Administrator
 * Date: 23.09.2007
 * Time: 12:57:51
 * 
 */
public class Parser extends LRParser
{
	private static Parser parser=null;

	protected Parser(String[] args)
			throws ParserException
	{
		super(args);
	}

	public static synchronized Parser createParser(String[] args)
			throws ParserException
	{
		if (parser==null)
			parser = new Parser(args);
		return parser;
	}
}
