package ru.ts.utils;

/**
 * Created by IntelliJ IDEA.
 * User: vladm
 * Date: 25.05.2007
 * Time: 15:18:29
 * To change this template use File | Settings | File Templates.
 */
public class ParserUtils
{
	public static String getLexem(String instr, String separator)
	{
		if (instr == null || instr.length() == 0)
			return instr;

		int ix=instr.indexOf(separator);
		if (ix<0)
			return instr;
		return instr.substring(0,ix);

//		int i = 0;
//		StringBuffer bufer = new StringBuffer();
//		while (i < instr.length() && !instr.substring(i).startsWith(separator))
//		{
//			bufer.append(instr.charAt(i));
//			++i;
//		}
//		return bufer.toString();
	}

	public static String getLexem(String instr)
	{
		return getLexem(instr, " ");
	}

	public static String getNextSubstring(String lexem, String string) throws Exception
	{
		if (!string.startsWith(lexem))
			throw new Exception("Parser Error");
		return string.substring(lexem.length()).trim();
	}

}
